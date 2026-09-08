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

## 2026-09-08 — T001 first attempt (PR #11) — closed by Tophe, re-planned
- Did: the loop ran end to end for the first time: planner brief, implementer
  (85 turns, 34 min, PR #11 with 27 files), reviewer (APPROVE, after a rerun
  past a session limit). Tophe reviewed the PR with Claude and closed it
  unmerged.
- Decided: the PR did what its brief asked, and the brief asked for too much.
  New rules: `CLAUDE.md` (only what the task uses; no rationale and no
  decision numbers in code), D07 (one test source set, no suffix, tests of
  framework wiring are temporary), the planner prompts (minimal task lines,
  no invented convention), the reviewer (scope check), the implementer (a
  new dependency, plugin or toolchain is structural). T001 trimmed to webmvc
  + actuator + the gate; datasource, Flyway and ArchUnit moved to T005.
  `gh pr diff 11` stays readable for reference (wrapper bootstrap, catalog,
  `detekt.yml`, README); take from it only what the T001 line asks for.
- Left over / gotchas, learned by the first attempt:
  - No Gradle in the sandbox: bootstrap the wrapper from the 9.7.1
    distribution zip (`gradle wrapper --gradle-version 9.7.1`), commit the
    four wrapper files unmodified, `gradlew` with mode 100755.
  - Spring Boot 4.1.1 names: `spring-boot-starter-webmvc`,
    `spring-boot-starter-flyway`, `tools.jackson.module:jackson-module-kotlin`
    (Jackson 3); `RestTestClient` comes from `spring-boot-resttestclient`,
    not from `spring-boot-starter-web-server-test`; `kotlin-reflect` is
    needed at runtime by Spring's Kotlin support. Kotlin 2.3.21 is the
    version Boot 4.1.1 manages.
  - detekt 1.23.8 on JDK 25: its embedded Kotlin 2.0.21 compiler fails with
    `IllegalArgumentException: 25.0.x` only when handed a JDK 25 as
    `jdkHome`. The plugin's type-resolution tasks (`detektMain`,
    `detektTest`) set that by convention; clear it and they run in-process:
    `tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach { jdkHome.convention(null as Directory?); jvmTarget = "21" }`.
    Measured on Gradle 9.7.1: `!!` is then reported on Kotlin-typed
    receivers, not on Java-typed ones (no JDK on detekt's classpath); the
    plain `detekt` task reports no `!!` at all. No JDK 21 toolchain, no
    foojay resolver. `LateinitUsage` lives in `potential-bugs`, not `style`.
    detekt 2.0 is still alpha.
  - Kotlin 2.3.21 emits no compiler warning for an unused local variable:
    prove warnings-as-errors with a useless cast. `runApplication(*args)`
    trips detekt's `SpreadOperator`; suppress it on `main`.
  - ktlint's default style is `ktlint_official`; `ktlint_code_style =
    intellij_idea` in `.editorconfig` selects the official Kotlin
    conventions that `kotlin.code.style=official` means.
  - The proof hook counts any command containing `gradlew … check` as a gate
    run, `--dry-run` and heredoc text included: write files with the Write
    tool and keep the gate plain.
  - The `gradle-cache` volume is owned by `agent` since PR #12; no
    `GRADLE_USER_HOME` override is needed.
  - `gh pr edit` can fail with a GraphQL error about classic Projects; the
    REST API (`gh api -X PATCH repos/…/pulls/N`) works.

## 2026-09-08 — T001 Backend skeleton — done (PR pending)
- Did: `backend/` scaffolded — Gradle 9.7.1 wrapper, `settings.gradle.kts`,
  `gradle.properties`, `gradle/libs.versions.toml`, `build.gradle.kts`,
  `config/detekt/detekt.yml`, `LibrisApplication.kt`, `README.md`. Verified by
  command, as the brief asked, not by a shipped test: the gate exits 0; a
  useless cast fails `compileKotlin`; `!!`, `lateinit var` and a wrong
  indentation each fail `detekt`; a throwaway JUnit 5 + Kotest test ran and was
  deleted; `bootRun` answers UP; the gate stays green with `LIBRIS_DB_*` unset.
- Decided:
  - The plain `detekt` task gets `classpath.from(main.compileClasspath,
    main.output)`. Without it the task has no type resolution and never reports
    `!!` (the 2026-09-08 entry measured exactly that), so the acceptance
    criterion "`./gradlew detekt` fails on a `!!`" could not hold. `check`
    depends on that task, so the whole of what detekt can enforce of D10 is in
    the gate. `dependsOn(compileKotlin)` is not needed: the source set output
    carries its own task dependency.
  - `.editorconfig` left alone. The root file already sets `indent_size = 4`
    for `*.{kt,kts}` and detekt's formatting ruleset reported the wrong
    indentation correctly against it, so nothing proved
    `ktlint_code_style = intellij_idea` necessary. A rule that the two styles
    disagree about will prove it later.
  - `.gitignore` gained `.kotlin/`. Proof: during a build the Kotlin plugin
    writes `backend/.kotlin/sessions/kotlin-compiler-*.salive`, untracked and
    not covered by any existing pattern; a crashed build leaves it behind (the
    first attempt left a `.kotlin/` directory in `backend/`).
- Left over / gotchas:
  - Boot 4.1.1's `/actuator/health` answers
    `{"groups":["liveness","readiness"],"status":"UP"}`, not the bare
    `{"status":"UP"}` the brief quoted. Availability groups are a Boot default;
    no configuration was added to hide them.
  - `Detekt.jdkHome.convention(null as Directory?)` is kept for the type
    resolution tasks (`detektMain`, `detektTest`), which set a JDK 25 jdkHome
    by convention and would crash. The plain `detekt` task sets none, so it was
    already safe; the `withType` block covers both.
  - The proof hook rejects `./gradlew check --dry-run | grep …` too: it wants
    the gate last in its command, pipe included. Run `--dry-run` plainly and
    read the output.
  - detekt has no rule for D10's "sealed types for states" or "constructor
    injection"; that part of D10 is unenforced until an ArchUnit rule exists.
