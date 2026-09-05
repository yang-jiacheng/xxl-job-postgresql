**English | [中文](https://github.com/yang-jiacheng/xxl-job-postgresql/blob/master/README.zh-CN.md)**

# xxl-job-postgresql

A PostgreSQL adaptation of [XXL-JOB](https://github.com/xuxueli/xxl-job), the distributed task scheduling platform.

This repository keeps the XXL-JOB application behavior of each supported upstream version while replacing the MySQL-specific datasource, schema, and mapper SQL with PostgreSQL-compatible implementations.

## Repositories

- Project: [yang-jiacheng/xxl-job-postgresql](https://github.com/yang-jiacheng/xxl-job-postgresql)
- Upstream: [xuxueli/xxl-job](https://github.com/xuxueli/xxl-job)

## Supported branches

| Branch | XXL-JOB version | Java | Spring Boot | Database |
| --- | --- | --- | --- | --- |
| `master` / `v3.1.0` | 3.1.0 | 21 | 3.5.7 | PostgreSQL 16 |
| `v2.4.1` | 2.4.1 | 8 | 2.7.18 | PostgreSQL 16 |

The `master` branch currently points to the same commit as `v3.1.0`.

## PostgreSQL adaptation

- PostgreSQL JDBC driver and Druid datasource configuration.
- PostgreSQL-compatible MyBatis mapper SQL.
- Fresh-install schema under the `app` namespace.
- Identity columns, indexes, seed data, and sequence synchronization tailored to each XXL-JOB version.
- Docker deployment example with environment-based database configuration.

## Quick start

1. Prepare a PostgreSQL database and a database user with permission to use the `app` schema.
2. Run [`.docs/deploy/tables_xxl_job.sql`](https://github.com/yang-jiacheng/xxl-job-postgresql/blob/master/.docs/deploy/tables_xxl_job.sql) in the target database.
3. Configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `ACCESS_TOKEN`. Keep `currentSchema=app` in the JDBC URL when overriding the default URL.
4. Build and start the application:

```shell
mvn clean package -DskipTests
java -jar target/xxl-job-admin.jar
```

The default service endpoint is `http://localhost:8060/xxl-job-admin`.

## License

xxl-job-postgresql is licensed under the Apache License 2.0. See [LICENSE](https://github.com/yang-jiacheng/xxl-job-postgresql/blob/master/LICENSE) for the full license text.
