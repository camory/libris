# Libris — Progress log

Append-only. Newest entry last. One entry per loop iteration (or per human
session that changed something a future run must know).

Format:

```
## YYYY-MM-DD — T### short title — status
- Did: …
- Decided: … (or "nothing")
- Left over / gotchas: …
```

---

## 2026-09-06 — bootstrap — done by Tophe + Claude (interactive)
- Did: chose the stack (Vue + Vite PWA, Kotlin + Spring Boot, PostgreSQL FTS),
  hosting (Kimsufi behind Traefik), loop style (external `claude -p` loop in a
  Docker sandbox), approval model (branch + PR per task). Wrote PRD,
  architecture, loop explainer, backlog, sandbox image, driver script.
- Decided: see `docs/ARCHITECTURE.md` D01–D10.
- Verified: sandbox image builds (claude 2.1.263, gh 2.100, JDK 21.0.12,
  Node 22.23); PostgreSQL sidecar reachable; bind mount writes as the host
  user; an authenticated `claude -p` run inside the sandbox honoured the
  PreToolUse guard (push to main denied), ran a command, and returned
  schema-validated JSON (4 turns, $0.41 list price).
- Left over: initial commit; GitHub repo, branch protection and tokens not yet
  created; PRD open questions; backlog not yet reviewed by Tophe.

## 2026-09-08 — T001 Backend skeleton — pr_opened
- Did: `backend/` — Gradle 9.7.1 wrapper, Spring Boot 4.1.1 / Kotlin 2.3.21 on
  JDK 25, catalog in `gradle/libs.versions.toml`, starters webmvc, data-jdbc,
  flyway, actuator, PostgreSQL driver, no Spring Security. Datasource from
  `LIBRIS_DB_*`. `V001__extensions.sql` (unaccent, pg_trgm). D02 packages as
  `.gitkeep` directories. The gate (`gradlew` + `check`) = compile with
  warnings as errors, detekt + formatting, JUnit 5 + Kotest assertions
  (`ArchitectureTest` with the six D02 rules, `FlywayMigrationIT`,
  `ActuatorHealthIT`), Kover XML. `backend/README.md`. Root `.editorconfig`
  gains `ktlint_code_style = intellij_idea`.
- Decided: detekt 1.23.8 cannot run in-process on JDK 25 (its embedded Kotlin
  2.0.21 compiler rejects a runtime newer than 24 while opening the JDK's
  `jrt` filesystem, and it refuses a swapped compiler). The `detekt` task is
  therefore a `JavaExec` of `detekt-cli` on a JDK 21 toolchain provisioned by
  `org.gradle.toolchains.foojay-resolver-convention` (settings plugin), with
  type resolution over the test compile classpath so that
  `UnsafeCallOnNullableType` sees real types. Same rules, same config file,
  same `:detekt` task name. Constructor injection in tests through
  `src/test/resources/spring.properties` (`spring.test.constructor.autowire.mode=all`).
  `ActuatorHealthIT` uses `RestTestClient` (`spring-boot-resttestclient`,
  Boot-managed): Boot 4 keeps `TestRestTemplate` there too, but its
  auto-configuration needs the restclient module as well.
- Deviations: see the PR body — detekt as CLI on JDK 21; Kotlin 2.3.21 emits
  no warning for an unused local variable (verified: `compileKotlin` stays
  green), so warnings-as-errors was proven with a useless cast and the unused
  local is caught by detekt's `UnusedPrivateProperty` instead;
  `spring-boot-starter-web-server-test` does not carry `TestRestTemplate` in
  Boot 4 (`spring-boot-resttestclient` does); `kotlin-reflect` added
  (Boot-managed, needed by Spring's Kotlin support and jackson-module-kotlin).
- Left over / gotchas:
  - Sandbox: `/home/agent/.gradle` is a root-owned volume, Gradle fails with
    "Could not initialize native services". Workaround this run:
    `export GRADLE_USER_HOME=/home/agent/gradle-home` before every `./gradlew`
    (nothing is cached between runs until the image creates the directory as
    `agent`; proposed in `agent/TASKS.md`).
  - The `detekt` task downloads a Temurin JDK 21 into `$GRADLE_USER_HOME/jdks`
    on first run (about 200 MB); CI runners usually have one preinstalled.
  - detekt parses sources with the Kotlin 2.0 grammar: syntax newer than 2.0
    (guard conditions, context parameters) would be reported as a parse
    error by detekt before the compiler sees it.
  - The sandbox's proof hook counts any `gradlew … check` invocation as the
    gate, `--dry-run` included, and refuses a command that merely contains
    that text (a heredoc writing a README, for instance): use the Write tool
    for such files, run partial tasks (`test`, `detekt`) for exploration and
    keep the gate plain.
  - Flyway 12.4.0 accepted PostgreSQL 18 without complaint.
