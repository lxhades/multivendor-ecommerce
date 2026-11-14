# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace
ARG SERVICE_MODULE
COPY pom.xml ./
COPY common ./common
COPY services ./services
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -pl "services/${SERVICE_MODULE}" -am -Dmaven.test.skip=true package \
    && mkdir /output \
    && cp services/${SERVICE_MODULE}/target/*.jar /output/app.jar

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/*
RUN groupadd --system app && useradd --system --gid app app \
    && mkdir -p /app/uploads && chown -R app:app /app
COPY --from=build --chown=app:app /output/app.jar ./app.jar
USER app
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
