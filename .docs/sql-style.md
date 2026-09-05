# SQL 脚本规范

本文件约定**SQL**的写法：简洁、可回滚、可复制到任意项目使用。  
应用运行时的查询与批量写入模式，由各项目自己的并发/ORM 规范覆盖，不在本文展开。

## 核心原则

1. **简洁有效**：直接写能达成目标的 DDL/DML，不要堆砌「以防万一」的校验。
2. **事务可回滚**：业务变更放在同一事务中；报错后同会话回滚即可。
3. **不做过度断言**：禁止大段过程块去枚举「允许存在的索引/约束清单」或反复探测对象是否存在。前置条件用文件头注释写清，由执行人人工确认。
4. **最小改动**：只改当前需求涉及的表/字段/索引；不顺手加外键、非空、默认值，除非需求明确要求。
5. **备份与迁移可分段**：需要保留快照时，可先单独事务提交备份，再开第二段迁移事务（第二段失败不影响已提交备份）。

## 推荐结构

```sql
-- 一句话说明改什么
-- 表：schema.table_name（按实际库填写）
-- 说明：
-- 1. 背景与约束（如：某字段现网有重复，不可建唯一索引）
-- 2. 执行前人工确认项（源表存在 / 目标索引尚未创建 / 备份表不存在）
-- 3. 整段同一事务；报错立即 ROLLBACK

BEGIN;

-- 实际 DDL/DML（分段用短注释标明意图）
ALTER TABLE schema.table_name
    ADD CONSTRAINT pk_tablename PRIMARY KEY (id);

CREATE INDEX idx_tablename_status
    ON schema.table_name (status);

COMMIT;
-- 报错时执行：ROLLBACK;
```

有备份时，用两段事务：

```sql
-- ========== 1. 备份 ==========
BEGIN;

CREATE TABLE schema.table_name_bak_20260803 AS
SELECT * FROM schema.table_name;

COMMIT;

-- ========== 2. 迁移（同会话整段执行；失败 ROLLBACK） ==========
BEGIN;

-- 去重 / 改类型 / 建索引 ...
DELETE FROM schema.table_name
WHERE id IN (
    SELECT id
    FROM (
        SELECT id,
               row_number() OVER (PARTITION BY biz_key ORDER BY id) AS rn
        FROM schema.table_name
    ) t
    WHERE rn > 1
);

CREATE UNIQUE INDEX uk_tablename_bizkey
    ON schema.table_name (biz_key);

COMMIT;
-- 报错时执行：ROLLBACK;
```

## 风格细则

- 文件头注释说明「做什么、为何、执行注意」即可，不要写成长篇操作手册。
- 关键步骤前用一行注释说明业务意图。
- 能一条语句解决的，不要拆成一堆临时检查。
- 对象已存在会报错时：注释写明「请先确认未创建，或手工 DROP 后再执行」；不要在脚本里写巨型存在性探测。
- 普通索引可用 `IF NOT EXISTS` 做有限度幂等；主键/唯一约束若不宜幂等，保持直接创建，依赖事务回滚。
- 不默认使用不能包进事务的在线建索引语法；确需在线构建时单独说明并脱离事务执行。

## 正例与反例

### 正例：短注释 + 直接变更 + 事务

```sql
-- 路径表去重：同 typ_id + last_time 只留 id 最小的一条
-- 执行前确认：唯一索引尚未创建；报错立即 ROLLBACK

BEGIN;

DELETE FROM schema.typhoon_address
WHERE id IN (
    SELECT id
    FROM (
        SELECT id,
               row_number() OVER (PARTITION BY typ_id, last_time ORDER BY id) AS rn
        FROM schema.typhoon_address
    ) t
    WHERE rn > 1
);

CREATE UNIQUE INDEX uk_typhoonaddress_typid_lasttime
    ON schema.typhoon_address (typ_id, last_time);

COMMIT;
-- 报错时执行：ROLLBACK;
```

### 反例：过度校验（禁止）

```sql
-- ❌ 不要这样写：大段探测表/索引/约束是否「符合白名单」，正式库细微差异就整单失败
BEGIN;

DO $$
DECLARE
    problem_names text;
BEGIN
    SELECT string_agg(indexname, ', ')
    INTO problem_names
    FROM pg_indexes
    WHERE schemaname = 'schema'
      AND tablename = 'table_name'
      AND indexname <> ALL (ARRAY['允许的索引1', '允许的索引2', '...很长的白名单...']);

    IF problem_names IS NOT NULL THEN
        RAISE EXCEPTION '发现未登记索引：%', problem_names;
    END IF;
END $$;

-- 真正有用的变更反而淹没在校验里
ALTER TABLE schema.table_name ADD COLUMN remark varchar(255);

COMMIT;
```

正确做法：文件头写清「执行前确认索引/备份状态」，脚本本身只保留必要变更。

## 索引命名规范

### 命名基本原则

1. **统一小写 + 下划线分隔**（snake_case），不使用大写或特殊字符。  
2. **表名或字段名如果有下划线要去掉，但连接表名和字段名还是用下划线**
3. **前缀表示索引类型**（如 pk、uk、idx、ft）。  
4. **索引名中包含表名与关键字段名**，避免不同表冲突。  
5. **多字段索引字段名按查询使用顺序排列**（符合最左前缀原则）。  
6. **索引命名长度不要超过限制**
7. **索引名具有业务含义**，清晰表达索引作用。  
8. **必须加`IF NOT EXISTS`，防止重复执行报错**

-----

### 命名规则

| 索引类型             | 前缀   | 命名格式                     | 示例                          | 说明         |
| -------------------- | ------ | ---------------------------- | ----------------------------- | ------------ |
| 主键索引             | `pk_`  | `pk_表名`                    | `pk_user`                     | 每表仅一个   |
| 唯一索引             | `uk_`  | `uk_表名_字段1_字段2`        | `uk_user_email`               | 字段组合唯一 |
| 普通索引             | `idx_` | `idx_表名_字段1_字段2`       | `idx_order_status_date`       | 常规索引     |
| 全文索引（仅 MySQL） | `ft_`  | `ft_表名_字段`               | `ft_article_content`          | 全文检索     |
| 组合索引             | 同上   | `idx_表名_字段1_字段2_字段3` | `idx_order_userid_createtime` | 复合索引     |
| 函数索引（Oracle）   | `fx_`  | `fx_表名_函数说明`           | `fx_emp_uppername`            | 函数结果索引 |

### 命名长度限制

| 数据库             | 索引名最大长度 | 单位 |
| ------------------ | -------------- | ---- |
| MySQL              | **64**         | 字符 |
| PostgreSQL         | **63**         | 字节 |
| Oracle 12.2+       | **128**        | 字节 |
| Oracle 12.1 及以前 | **30**         | 字节 |

**注意：索引长度不超过对应数据库限制，所以太长时可以简写表名和字段**

```sql
-- `boats_history_track`表，唯一索引，简写
CREATE UNIQUE INDEX IF NOT EXISTS uk_bhtrack_boatid_dwtime ON nanao_prod.boats_history_track USING btree (boat_id, dw_time);
```

## 查询条件与索引设计

已知条件可独立也可自由组合时，优先用**最左前缀覆盖全部子集**的最少索引集：  
不要只建单列再完全指望位图合并，也不要把所有排列都建一遍。

三条件 `port_id` / `indexcode` / `name` 示例：

```text
(port_id, indexcode, name)  → port_id | port_id+indexcode | 三条件
(port_id, name)            → port_id+name
(indexcode, name)          → indexcode | indexcode+name
(name)                     → name
```

```sql
BEGIN;

CREATE INDEX IF NOT EXISTS idx_plantvideohk_portid_indexcode_name
    ON schema.plant_video_hk (port_id, indexcode, name);

CREATE INDEX IF NOT EXISTS idx_plantvideohk_portid_name
    ON schema.plant_video_hk (port_id, name);

CREATE INDEX IF NOT EXISTS idx_plantvideohk_indexcode_name
    ON schema.plant_video_hk (indexcode, name);

CREATE INDEX IF NOT EXISTS idx_plantvideohk_name
    ON schema.plant_video_hk (name);

COMMIT;
-- 报错时执行：ROLLBACK;
```

设计前先在库里只读核对：目标唯一键是否真唯一、普通索引字段是否大量重复、主键列是否有空值。  
核对在执行前完成，**不要把核对逻辑写进上线脚本**。

## 完成前自检

- 是否 `BEGIN` / `COMMIT`，并注明报错 `ROLLBACK`。
- 是否避免了大段对象探测、白名单断言等过度校验。
- 索引名是否符合 `pk_` / `uk_` / `idx_` 规则；组合字段顺序是否匹配查询。
- 是否因「保险」多加了外键、非空或无用索引。
- 需要备份时，备份与迁移的事务边界是否清楚。
