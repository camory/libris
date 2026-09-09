# libris-backend

Spring Boot / Kotlin application. JDK 25.

## Database

`bootRun` and the tests need a PostgreSQL 18, given to them, never created by
the build. Flyway migrates it at startup. Three environment variables address
it, with these defaults:

| Variable | Default |
| --- | --- |
| `LIBRIS_DB_URL` | `jdbc:postgresql://localhost:5432/libris` |
| `LIBRIS_DB_USER` | `libris` |
| `LIBRIS_DB_PASSWORD` | `libris` |

## Run

```
./gradlew bootRun
```

The application listens on <http://localhost:8080>. Health:
<http://localhost:8080/actuator/health>.

It reads the caller's identity from the `Remote-*` headers Traefik forwards
from Authelia, so a local call passes them itself. As a reader:

```
curl http://localhost:8080/api/v1/me \
  -H 'Remote-User: juliette' -H 'Remote-Name: Juliette' \
  -H 'Remote-Email: juliette@amory.fr' -H 'Remote-Groups: family'
```

As an admin:

```
curl http://localhost:8080/api/v1/me \
  -H 'Remote-User: tophe' -H 'Remote-Name: Tophe' \
  -H 'Remote-Email: tophe@amory.fr' -H 'Remote-Groups: family,libris-admin'
```

## Test

```
./gradlew check
```

It compiles with warnings as errors, runs detekt with its formatting ruleset,
runs the JUnit 5 tests with Kotest assertions against the database above, and
writes a Kover XML coverage report to `build/reports/kover/report.xml`.

## Image

```
docker build . -t libris-backend:sha-abc1234 --build-arg VERSION=sha-abc1234
```

The image runs the boot jar as the non-root user `libris` on port 8080, and is
configured by environment variables only. `GET /actuator/info` then reports the
version the image was built with:

```
{"build":{"artifact":"libris-backend","name":"libris-backend","time":"…","version":"sha-abc1234"}}
```
