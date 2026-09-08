FROM eclipse-temurin:25.0.4_7-jdk-noble AS build
WORKDIR /workspace
COPY . .
ARG VERSION=dev
RUN ./gradlew --no-daemon bootJar -Pversion=$VERSION

FROM eclipse-temurin:25.0.4_7-jre-noble
ARG VERSION=dev
LABEL org.opencontainers.image.version=$VERSION
LABEL org.opencontainers.image.source=https://github.com/camory/libris
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
RUN useradd --system --create-home libris
COPY --from=build /workspace/build/libs/*.jar /app/app.jar
USER libris
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD curl -fsS http://localhost:8080/actuator/health || exit 1
CMD ["java", "-jar", "/app/app.jar"]
