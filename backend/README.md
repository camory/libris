# libris-backend

Spring Boot / Kotlin application. JDK 25.

## Database

`bootRun` and the tests need a PostgreSQL 18, given to them, never created by
the build. Flyway migrates it at startup and creates the extensions `unaccent`
and `pg_trgm`, so the server must ship them (the official image and most
packages do) and the user must be allowed to create them (owning the database
is enough).

A container answering the values of `.env.example`:

```
docker run -d --name libris-postgres -p 127.0.0.1:5432:5432 \
  -e POSTGRES_DB=libris -e POSTGRES_USER=libris -e POSTGRES_PASSWORD=libris \
  postgres:18-alpine
```

`docker start libris-postgres` brings it back after a reboot, with its data.

## Settings

The application has no default for the database: these variables come from
the environment, or from `backend/.env`, which `test` and `bootRun` read for
each variable the environment does not define. To use it:

```
cp .env.example .env
```

| Variable | Value in `.env.example` | Meaning |
| --- | --- | --- |
| `LIBRIS_DB_URL` | `jdbc:postgresql://localhost:5432/libris` | the database |
| `LIBRIS_DB_USER` | `libris` | its user |
| `LIBRIS_DB_PASSWORD` | `libris` | its password |
| `LIBRIS_COVERS_DIR` | `build/covers` | where the covers are kept; the working directory when unset |

Without the three `LIBRIS_DB_*`, or without the database they address, no
Spring context starts: every test that needs one fails with *ApplicationContext
failure threshold (1) exceeded*, and only the first failure carries the cause.

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
and runs the JUnit 5 tests with Kotest assertions against the database above.

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
