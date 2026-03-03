FROM openjdk:25-slim AS builder
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY settings.gradle .
COPY build.gradle .
COPY mom-common mom-common
COPY mom-infra mom-infra
COPY mom-ops mom-ops
RUN chmod +x ./gradlew
RUN ./gradlew :mom-ops:bootJar

FROM openjdk:25-slim
WORKDIR /app
COPY --from=builder /app/mom-ops/build/libs/mom-ops-1.0-SNAPSHOT.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
