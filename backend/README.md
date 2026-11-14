# libris-backend

Spring Boot / Kotlin application. JDK 25.

## Database

The application has no default for its database: `LIBRIS_DB_URL`,
`LIBRIS_DB_USER` and `LIBRIS_DB_PASSWORD` come from the environment, or from
`backend/.env`, which `test` and `bootRun` read for each variable the
environment does not define. To use it:

```
cp .env.example .env
```

| Variable | Value in `.env.example` | Meaning |
| --- | --- | --- |
| `LIBRIS_DB_URL` | `jdbc:tc:postgresql:18-alpine:///libris` | the database |
| `LIBRIS_DB_USER` | `libris` | its user |
| `LIBRIS_DB_PASSWORD` | `libris` | its password |
| `LIBRIS_COVERS_DIR` | `build/covers` | where the covers are kept; the working directory when unset |

### For the tests

With the URL of `.env.example`, each test run starts its own PostgreSQL 18 in
a container at the first connection and removes it when the run ends: the
machine needs Docker only.

The sandbox and CI give a running PostgreSQL instead, through a
`jdbc:postgresql://` URL in the environment.

### For `bootRun`

`bootRun` needs a running PostgreSQL 18, given to it: the Testcontainers
driver is on the test classpath only. A container:

```
docker run -d --name libris-postgres -p 127.0.0.1:5432:5432 \
  -e POSTGRES_DB=libris -e POSTGRES_USER=libris -e POSTGRES_PASSWORD=libris \
  postgres:18-alpine
```

`docker start libris-postgres` brings it back after a reboot, with its data.

Flyway migrates the database at startup and creates the extensions `unaccent`
and `pg_trgm`, so a PostgreSQL given this way must ship them (the official
image and most packages do) and its user must be allowed to create them
(owning the database is enough).

### When it fails

Without the three `LIBRIS_DB_*`, or without the database they address, no
Spring context starts: every test that needs one fails with *ApplicationContext
failure threshold (1) exceeded*, and only the first failure carries the cause.

## Run

With a PostgreSQL given as in *For `bootRun`* above, the environment winning
over `.env`:

```
LIBRIS_DB_URL=jdbc:postgresql://localhost:5432/libris ./gradlew bootRun
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
