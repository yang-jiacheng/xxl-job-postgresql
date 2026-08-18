FROM crpi-cerz1i20r7cju768.cn-hangzhou.personal.cr.aliyuncs.com/whiskey/jdk:21.0.9_10-jdk-jammy
WORKDIR /java/xxl-job-admin
COPY xxl-job-admin.jar xxl-job-admin.jar
EXPOSE 8060
ENV JAVA_OPTS="-Xms512m -Xmx512m -XX:+UseG1GC"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar xxl-job-admin.jar"]

## docker build -t crpi-cerz1i20r7cju768-vpc.cn-hangzhou.personal.cr.aliyuncs.com/whiskey/xxl-job-admin:3.1.0 .
