# 使用Spring Boot官方推荐的分层构建方式
# 阶段1: 构建阶段
FROM openjdk:25-slim AS builder

# 设置工作目录
WORKDIR /app

# 复制Gradle包装器
COPY gradlew .
COPY gradle gradle

# 复制settings.gradle和build.gradle
COPY settings.gradle .
COPY build.gradle .

# 复制模块构建文件
COPY mom-common mom-common
COPY mom-infra mom-infra
COPY mom-admin mom-admin

# 赋予执行权限
RUN chmod +x ./gradlew

# 构建应用，只构建mom-admin模块
RUN ./gradlew :mom-admin:bootJar

# 阶段2: 运行阶段
FROM openjdk:25-slim

# 设置工作目录
WORKDIR /app

# 从构建阶段复制jar文件
COPY --from=builder /app/mom-admin/build/libs/mom-admin-1.0-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 9091

# 设置环境变量
ENV SPRING_PROFILES_ACTIVE=prod

# 健康检查配置
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:9091/actuator/health || exit 1

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]