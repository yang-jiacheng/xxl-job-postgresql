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
      - DB_URL=jdbc:mysql://mysql:3306/xxl_job?serverTimezone=Asia/Shanghai&characterEncoding=utf8&useUnicode=true&useSSL=false&allowPublicKeyRetrieval=true
      - DB_USERNAME=
      - DB_PASSWORD=
      - ACCESS_TOKEN=
      - JAVA_OPTS=-Xms512m -Xmx512m -XX:+UseG1GC -XX:CICompilerCount=2 -XX:MaxDirectMemorySize=128m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m -Xss256k -XX:MaxGCPauseMillis=100 -XX:G1HeapRegionSize=2m -XX:+UseStringDeduplication -XX:+ParallelRefProcEnabled -Xlog:gc:/java/logs/gc/gc-xxljob-admin.log:time:filecount=5,filesize=50M -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/java/logs/heap-dumps/xxljob-admin.hprof
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
