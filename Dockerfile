ARG RUNTIME_IMAGE=eclipse-temurin:24-jre

FROM ${RUNTIME_IMAGE} AS jdk

WORKDIR /app
USER nobody
COPY target/hw3-logs-1.0.jar ./app.jar

ENTRYPOINT ["java", "-cp", "app.jar", "academy.Application"]
