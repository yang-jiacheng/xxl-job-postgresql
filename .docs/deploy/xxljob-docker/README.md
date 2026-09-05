### 1. 描述

XXL-JOB Admin 2.4.1 的 PostgreSQL 16 容器部署示例。

### 2. 前置条件

- PostgreSQL 目标数据库为 `test_db`，模式 `app` 已存在。
- 应用数据库用户对 `app` 至少具有 `USAGE` 权限，并具有运行所需的表访问权限。
- 首次部署前，由数据库维护者在 `test_db` 中执行 [`../tables_xxl_job.sql`](../tables_xxl_job.sql)。应用启动过程不会创建或升级表结构。
- PostgreSQL 容器或主机应能从 `whiskey-network` 访问。

### 3. 部署示例

#### 3.1 Compose 配置

**compose.yaml**

```yaml
services:
  xxl-job-admin:
    image: crpi-cerz1i20r7cju768.cn-hangzhou.personal.cr.aliyuncs.com/whiskey/xxl-job-admin:2.4.1
    container_name: xxl-job-admin
    restart: always
    environment:
      - TZ=Asia/Shanghai
      - DB_URL=jdbc:postgresql://postgresql:5432/test_db?currentSchema=app
      - DB_USERNAME=
      - DB_PASSWORD=
      - ACCESS_TOKEN=
      - JAVA_OPTS=-Xms512m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication -XX:+ParallelRefProcEnabled -Xlog:gc:/java/logs/gc/gc-xxljob-admin.log:time:filecount=5,filesize=50M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/java/logs/heap-dumps/xxljob-admin.hprof
    ports:
      - "8060:8060"
    volumes:
      - ./logs:/java/logs
    networks:
      - whiskey-network

networks:
  whiskey-network:
    external: true

```

`postgresql` 是示例主机名，应替换为该网络内实际可解析的 PostgreSQL 服务名或地址。覆盖 `DB_URL` 时必须保留 `currentSchema=app`，否则未限定模式的 Mapper SQL 不会稳定访问 `app`。

不要把真实数据库密码或访问令牌提交到仓库；应由部署环境注入。

#### 3.2. 运行

```shell
docker compose -f compose.yaml up -d
```

### 4. 检查

```shell
docker logs -f xxl-job-admin
docker top xxl-job-admin
docker exec -it xxl-job-admin jcmd 1 VM.flags
docker exec -it xxl-job-admin jcmd 1 GC.heap_info
```
