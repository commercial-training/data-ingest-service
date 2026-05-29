# syntax=docker/dockerfile:1.7
# -----------------------------------------------------------------------------
# data-ingest-service - Spring Boot 4 / Java 21 Event Hubs consumer
#
# Build context is this service repository:
#
#   docker build -t <acr>.azurecr.io/data-ingest-service:0.1.0 .
# -----------------------------------------------------------------------------

FROM maven:3.9-eclipse-temurin-21 AS mvn

WORKDIR /build

COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B -ntp -DskipTests package

FROM eclipse-temurin:21-jre AS extract

WORKDIR /extract
COPY --from=mvn /build/target/data-ingest-service-*.jar application.jar
RUN java -Djarmode=tools -jar application.jar extract --layers --launcher

FROM eclipse-temurin:21-jre

ARG OTEL_JAVA_AGENT_VERSION=2.27.0

WORKDIR /application
RUN useradd --system --uid 1001 --shell /usr/sbin/nologin spring
RUN mkdir -p /otel

ADD --chmod=0644 https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v${OTEL_JAVA_AGENT_VERSION}/opentelemetry-javaagent.jar /otel/opentelemetry-javaagent.jar
RUN chown -R spring /otel

USER spring

ENV JAVA_TOOL_OPTIONS="-javaagent:/otel/opentelemetry-javaagent.jar" \
    OTEL_EXPORTER_OTLP_ENDPOINT="http://127.0.0.1:4318" \
    OTEL_EXPORTER_OTLP_PROTOCOL="http/protobuf" \
    OTEL_INSTRUMENTATION_LOGBACK_APPENDER_ENABLED="true" \
    OTEL_INSTRUMENTATION_MICROMETER_ENABLED="true" \
    OTEL_LOGS_EXPORTER="otlp" \
    OTEL_METRIC_EXPORT_INTERVAL="15000" \
    OTEL_METRICS_EXPORTER="otlp" \
    OTEL_SERVICE_NAME="data-ingest-service" \
    OTEL_TRACES_EXPORTER="none"

COPY --from=extract --chown=spring /extract/application/dependencies/ ./
COPY --from=extract --chown=spring /extract/application/spring-boot-loader/ ./
COPY --from=extract --chown=spring /extract/application/snapshot-dependencies/ ./
COPY --from=extract --chown=spring /extract/application/application/ ./

EXPOSE 8083

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
