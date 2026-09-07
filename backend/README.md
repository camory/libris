# Libris backend

Kotlin + Spring Boot 4 on JDK 25, Gradle Kotlin DSL with the version catalog in
`gradle/libs.versions.toml`. Decisions in `../docs/ARCHITECTURE.md` (D02, D07,
D08, D10, D11) are binding.

## Run

The application needs a PostgreSQL 18 it does not create (D08). Locally, the
throwaway database of the agent compose file is enough:

```
docker compose -f ../agent/compose.yaml up -d postgres   # localhost:5432, user/password libris
./gradlew bootRun                                        # http://localhost:8080
curl http://localhost:8080/actuator/health               # {"status":"UP"}
```

Flyway migrates the database on start-up (`src/main/resources/db/migration`,
`V001__extensions.sql` creates `unaccent` and `pg_trgm`).

Configuration is by environment variables only:

| Variable             | Default                                   |
|----------------------|-------------------------------------------|
| `LIBRIS_DB_URL`      | `jdbc:postgresql://localhost:5432/libris` |
| `LIBRIS_DB_USER`     | `libris`                                  |
| `LIBRIS_DB_PASSWORD` | `libris`                                  |

## Test

```
./gradlew check
```

is the whole backend gate (D07). It runs, against the database above:

- `compileKotlin` / `compileTestKotlin` with warnings as errors (`-Werror` for Java too);
- `detekt`: detekt 1.23.8 with the formatting ruleset, default config plus
  `config/detekt/detekt.yml` (no `!!`, no `lateinit` in `src/main`, `val`
  over `var`), on main and test sources with type resolution. It runs on a
  JDK 21 toolchain that Gradle provisions, because detekt 1.23 cannot run on
  JDK 25;
- `test`: JUnit 5 with Kotest assertions. Classes suffixed `IT` need the
  database: `FlywayMigrationIT` checks the migration, `ActuatorHealthIT`
  starts the app on a random port. `ArchitectureTest` holds the six ArchUnit
  rules of D02;
- `koverXmlReport`: coverage in JaCoCo XML format.

Reports land in `build/reports/`: `detekt/detekt.html`, `tests/test/index.html`,
`kover/report.xml`.

Partial runs while working: `./gradlew test`, `./gradlew detekt`,
`./gradlew test --tests fr.amory.libris.ArchitectureTest`.
