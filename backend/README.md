# libris-backend

Spring Boot / Kotlin application. JDK 25.

## Run

```
./gradlew bootRun
```

The application listens on <http://localhost:8080>. Health:
<http://localhost:8080/actuator/health>.

## Test

```
./gradlew check
```

It compiles with warnings as errors, runs detekt with its formatting ruleset,
runs the JUnit 5 tests with Kotest assertions, and writes a Kover XML coverage
report to `build/reports/kover/report.xml`.
