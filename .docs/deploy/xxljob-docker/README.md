### 1. 描述

**xxl-job镜像**

### 2. 目录结构

```txt
/xxl-job-docker/
├── compose.yml     
├── logs/
│   ├── gc/
│   ├── heap-dumps/  
```

### 3. 拉取镜像

#### 3.1 配置

**compose.yaml**

```yaml
services:
  xxl-job-admin:
    image: crpi-cerz1i20r7cju768.cn-hangzhou.personal.cr.aliyuncs.com/whiskey/xxl-job-admin:3.1.0
    container_name: xxl-job-admin
    restart: always
    environment:
      - TZ=Asia/Shanghai
      - DB_URL=jdbc:postgresql://postgresql:5432/test_db
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

数据库配置说明：

- `DB_URL` 格式为 `jdbc:postgresql://<host>:5432/<database>`，示例中的 PostgreSQL 主机名为 `postgresql`、数据库名为 `test_db`，请按实际环境替换。
- 启动前使用 [PostgreSQL 初始化脚本](../tables_xxl_job.sql) 在目标数据库中完成全新初始化。
- 应用固定使用 `app` 模式；Druid 创建物理连接时会执行 `SET search_path TO app`，因此 JDBC URL 无需重复指定 schema 参数。
- `DB_USERNAME` 对应的数据库用户需要具有 `app` 模式的 `USAGE` 权限，以及 XXL-JOB 表和 sequence 的运行权限。

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
