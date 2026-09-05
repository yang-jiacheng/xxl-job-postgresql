**[English](https://github.com/yang-jiacheng/xxl-job-postgresql/blob/master/README.md) | 中文**

# xxl-job-postgresql

基于分布式任务调度平台 [XXL-JOB](https://github.com/xuxueli/xxl-job) 改造的 PostgreSQL 版本。

本仓库在保留各上游版本既有应用行为的基础上，将 MySQL 数据源、初始化结构和 Mapper SQL 改造为 PostgreSQL 兼容实现。

## 仓库地址

- 当前项目：[yang-jiacheng/xxl-job-postgresql](https://github.com/yang-jiacheng/xxl-job-postgresql)
- XXL-JOB 官方仓库：[xuxueli/xxl-job](https://github.com/xuxueli/xxl-job)

## 支持分支

| 分支 | XXL-JOB 版本 | Java | Spring Boot | 数据库 |
| --- | --- | --- | --- | --- |
| `master` / `v3.1.0` | 3.1.0 | 21 | 3.5.7 | PostgreSQL 16 |
| `v2.4.1` | 2.4.1 | 8 | 2.7.18 | PostgreSQL 16 |

当前 `master` 与 `v3.1.0` 指向同一个提交。

## PostgreSQL 改造内容

- PostgreSQL JDBC 驱动和 Druid 数据源配置。
- PostgreSQL 兼容的 MyBatis Mapper SQL。
- 在 `app` 模式中进行全新初始化。
- 按对应 XXL-JOB 版本适配 identity、索引、种子数据和 sequence 同步。
- 提供基于环境变量配置数据库的 Docker 部署示例。

## 快速开始

1. 准备 PostgreSQL 数据库，以及具有 `app` 模式使用权限的数据库用户。
2. 在目标数据库中执行 [`.docs/deploy/tables_xxl_job.sql`](https://github.com/yang-jiacheng/xxl-job-postgresql/blob/master/.docs/deploy/tables_xxl_job.sql)。
3. 配置 `DB_URL`、`DB_USERNAME`、`DB_PASSWORD` 和 `ACCESS_TOKEN`。覆盖默认 JDBC URL 时需要保留 `currentSchema=app`。
4. 构建并启动应用：

```shell
mvn clean package -DskipTests
java -jar target/xxl-job-admin.jar
```

默认访问地址为 `http://localhost:8060/xxl-job-admin`。

## License

xxl-job-postgresql 是根据 Apache 许可证 2.0 版获得许可的。有关完整的许可证文本，请参阅 [LICENSE](https://github.com/yang-jiacheng/xxl-job-postgresql/blob/master/LICENSE)。
