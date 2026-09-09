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
  - Gate requirement, not a deviation: the `classpath.from(...)` on the plain
    `detekt` task in `backend/build.gradle.kts` is what makes a `!!` fail the
    gate. With those two lines removed, `./gradlew detekt` passes the same
    `!!` source with `BUILD SUCCESSFUL` (measured by the reviewer of PR #15).
    Do not drop it in a cleanup.

## 2026-09-08 — T002 Frontend skeleton — done (PR pending)
- Did: `frontend/` scaffolded by hand — `package.json` (exact pins, the
  brief's version set, unchanged by npm), `package-lock.json`, `.npmrc`,
  `.node-version`, `index.html`, `vite.config.ts` (vue, Tailwind, PWA, the
  `/api` dev proxy, the Vitest block), `tsconfig.json`, `eslint.config.ts`,
  `src/main.ts`, `src/ui/{App.vue,router.ts,i18n.ts,style.css}`,
  `src/ui/views/HomeView{.vue,.spec.ts}`, two placeholder icons, `README.md`.
  The component test was written first and failed on the missing view.
  Verified by command: the gate exits 0 and writes `coverage/lcov.info`; a
  type error, an `any` and a wrong assertion each fail their own step of the
  gate; the five D05 boundary rules each fail `eslint` on a throwaway import;
  `npm run build` emits the manifest, `sw.js` precaching `index.html`, and
  Tailwind's `.p-4`/`.text-2xl` rules; `npm run dev` answers 502 on
  `/api/v1/ping` and 200 on `/`.
- Decided:
  - Elements for `eslint-plugin-boundaries` differ from the brief's table on
    the three single files. `mode: "file"` is deprecated in v7 (it prints a
    warning on every run and goes away in v8), and its replacement, file
    descriptors, cannot carry an element type. So `src/ui` is one `ui-shell`
    element (`App.vue`, `router.ts`, `i18n.ts`, `style.css`) and the router's
    permission to import a view is granted by a `boundaries/files` category on
    `src/ui/router.ts`. All five D05 rules still fail as required.
  - `settings["import/resolver"] = { node: { extensions: [".ts", ".vue"] } }`
    is what makes the boundaries rule see local imports at all. Measured:
    without it, `import { probe } from "../application/probe"` resolves to
    nothing, the dependency is reported with a null element and the rule
    stays silent; only the extension-carrying `.vue` and the external
    packages were checked. `eslint-import-resolver-node` is a dependency of
    `eslint-plugin-boundaries` itself, so no package was added. Do not drop
    those two lines in a cleanup: the rule passes on a real violation without
    them.
  - `checkAllOrigins: true` on the rule, because D05 rule 1 forbids `domain`
    an external package and external dependencies are unchecked by default.
  - `vue-tsc --noEmit` over `--build`: one `tsconfig.json` covers `src/**`,
    `vite.config.ts` and `eslint.config.ts`, so there is no project to
    reference. Proven: a wrong type in `vite.config.ts` fails the step.
  - `registerType: "autoUpdate"` on the PWA plugin. The default, `"prompt"`,
    needs update UI the app does not have, and would leave an installed phone
    on a stale shell.
  - The home title is `La bibliothèque de la maison`, a placeholder: no
    document names the screen. It is one message in `src/ui/i18n.ts`.
  - `npm run format` runs `prettier --write --ignore-path ../.gitignore .`.
    Without the flag Prettier rewrites `dist/` and `coverage/`, since it looks
    for a `.gitignore` in the working directory and the ignores live at the
    root. The tree is committed in Prettier's default style; no Prettier
    configuration file was added.
  - `.gitignore` gained `coverage/`. Proof: `npm test` writes
    `frontend/coverage/`, untracked and matched by no existing pattern.
    `dev-dist/` was not added: the PWA plugin's dev service worker is off, and
    a real `npm run dev` left nothing behind.
- Left over / gotchas:
  - ESLint 10 depends on `jiti`, so `eslint.config.ts` loads with no extra
    package.
  - The Vitest block lives in `vite.config.ts` with `defineConfig` imported
    from `vitest/config`; the one from `vite` does not type a `test` key.
  - `src/domain/`, `src/application/`, `src/infra/` and `src/ui/components/`
    do not exist yet (`CLAUDE.md` forbids placeholder directories). The
    boundaries policies already name them; T006 creates the first files.
  - Element descriptors are matched in array order, first match wins, so
    `src/ui/components` and `src/ui/views` must stay above `src/ui`, and
    `src/ui` above `src`, in `eslint.config.ts`.

## 2026-09-08 — T003 Backend image — done (PR pending)
- Did: the first backend test, `LibrisApplicationTest`, written red against
  `GET /actuator/info` (404 with the default exposure) and made green by
  `springBoot { buildInfo() }`, the project version read from the `version`
  Gradle property with a `dev` default, and a new
  `src/main/resources/application.yaml` holding the single setting
  `management.endpoints.web.exposure.include: health,info`. Then
  `backend/Dockerfile` (Temurin JDK 25 build stage taking `ARG VERSION`,
  Temurin JRE 25 runtime, non-root `libris` user, `HEALTHCHECK` on
  `/actuator/health`, exec-form `CMD`), `backend/.dockerignore` and the image
  section of `backend/README.md`.
  Verified by command: the gate exits 0; dropping `info` from the exposure
  setting fails the test with
  `AssertionError: Status expected:<200 OK> but was:<404 NOT_FOUND>`;
  `./gradlew test -Pversion=sha-abc1234` passes; `bootJar -Pversion=sha-abc1234`
  writes `libris-backend-sha-abc1234.jar` whose
  `META-INF/build-info.properties` says `build.version=sha-abc1234`; that jar
  run with `java -jar` answers `/actuator/info` with
  `{"build":{…,"version":"sha-abc1234"}}` and `/actuator/health` with
  `{"groups":["liveness","readiness"],"status":"UP"}`; a plain `bootJar` writes
  `libris-backend-dev.jar`; `runtimeClasspath` mentions no spring-security, no
  postgresql, no flyway; the gate passes with `LIBRIS_DB_*` unset.
- Decided:
  - `@AutoConfigureMockMvc` does not exist in Spring Boot 4.1.1: the whole of
    `spring-boot-test-autoconfigure` is three packages
    (`autoconfigure`, `.jdbc`, `.json`) and no MockMvc support. Took the
    brief's named fallback — `org.springframework.boot:spring-boot-resttestclient`
    (BOM-managed) with `@SpringBootTest(webEnvironment = RANDOM_PORT)` and
    `@AutoConfigureRestTestClient`. `RestTestClient` itself lives in spring-test
    at `org.springframework.test.web.servlet.client`.
  - Spring's parameter resolution does not inject a test constructor without
    `@Autowired` on it: the run failed with
    `ParameterResolutionException: No ParameterResolver registered for parameter
    [RestTestClient client]` until `class LibrisApplicationTest @Autowired
    constructor(...)`.
  - The expected version reaches the test as the system property
    `libris.version`, set by `tasks.test` from `project.version`, so
    `-Pversion=…` and the default `dev` both assert something real.
- Left over / gotchas:
  - `build/libs` holds the boot jar alone only after `bootJar` on a clean
    tree; `check` also runs `jar`, which writes a `-plain.jar` beside it. The
    image is safe because its build stage runs `bootJar` only and
    `.dockerignore` keeps the host's `build/` out of the context — but a
    `COPY … /libs/*.jar` after a `check` in the same stage would break.
  - No `.gitignore` line was needed: `git status --porcelain` stayed empty
    after the gate, after `bootJar` and after running the jar.
  - Tophe's review of PR #20: `LibrisApplicationTest` removed. It exercised
    Spring Boot's build info reaching the actuator through the exposure
    setting, no Libris class, so D07 calls it a wiring test and the command
    verification above already covers the requirement. The REST test client
    dependency, the unused `spring-boot-starter-test` line the reviewer
    blocked on, and the `libris.version` test property went with it. The
    backend has no test until T005 brings real code.
  - Nothing here runs the image. The `HEALTHCHECK`, the non-root user and
    `/actuator/info` served from a container are first exercised by T007 and
    the Phase 0 phone check; the PR's `images` job proves only that the image
    builds.

## 2026-09-08 — T004 Frontend image — done (PR pending)
- Did: `src/ui/components/AppFooter.spec.ts` written first and failed with
  `Failed to resolve import "./AppFooter.vue"`, then
  `src/ui/components/AppFooter.vue` (a `defineProps<{ revision: string }>()`
  and a `<footer>`), then `src/ui/App.vue` reading
  `import.meta.env.VITE_APP_VERSION ?? "dev"` and passing it as a prop. Then
  `frontend/Dockerfile` (Node 24 build stage assigning `ARG VERSION` to
  `VITE_APP_VERSION`, nginx runtime), `frontend/nginx.conf`,
  `frontend/.dockerignore` and the image section of `frontend/README.md`.
  Verified by command: the gate exits 0 with two specs;
  `VITE_APP_VERSION=sha-abc1234 npm run build` puts one occurrence of the
  literal in `dist/assets/index-<hash>.js`, a plain build puts none and
  `grep -o 'revision:[^,}]*'` shows ``revision:Yt(`dev`)``; `npm run build`
  writes exactly the nine files `nginx.conf` keys on; `git status --porcelain`
  stayed empty after the gate and after both builds.
- Decided: nothing the brief had not decided. Its measured shapes for
  `App.vue`, the Dockerfile and `nginx.conf` all held on contact.
  After review, with Tophe: the runtime stage is
  `nginxinc/nginx-unprivileged`, the nginx team's image running as uid 101
  `nginx` with its pid and temp files under `/tmp`, so the container listens
  on 8080, not 80. D09 now asks a non-root user of both images; T007's
  compose and Traefik router target port 8080 on the frontend container.
- Left over / gotchas:
  - Nothing in this repository can run nginx: no Docker and no nginx binary in
    the sandbox (D08), and the `images` job only *builds* the image. The SPA
    fallback and the three cache rules are first exercised by T007's compose
    and the Phase 0 phone check; `nginx.conf` is reviewed by reading.
  - No `HEALTHCHECK` in the frontend image, on purpose: nothing in the
    frontend answers a health question, and how compose watches a static
    server is T007's call.
  - No dependency was added and no dependency was needed: `import.meta.env`
    carries a string index signature, so the `?? "dev"` line passes
    `vue-tsc --noEmit` and `eslint .` with no `env.d.ts` and no `any`.
  - `src/ui/components/` now exists, and `AppFooter.vue` is its first file;
    the `ui-components` element the boundaries policy already named is
    exercised for the first time.

## 2026-09-09 — T005 preparation — done by Tophe + Claude (interactive)
- Did: wrote `api/openapi.yaml` with `GET /api/v1/me` (OpenAPI 3.0.3, the
  four Authelia header fields and a role); split the old T005 into T005
  (authentication, no datasource) and T006 (persistence and member profile),
  renumbering the frontend and compose tasks to T007 and T008; fixed D04
  (group id `dev.contracteer`, Homebrew tap `contracteer-dev/contracteer`,
  OpenAPI 3.0.3).
- Decided: `/me` mirrors the headers in T005 and gains the id in T006; the
  role is derived from `Remote-Groups` on every request and never stored;
  the identity key is `Remote-User`; the display name is seeded from
  `Remote-Name` once and then owned by Libris (T006); `CurrentMember` is
  closed (`additionalProperties: false`) so a field added on one side fails
  verification on the other; no 401 in the contract since authentication is
  upstream (D06); no example on `/me`.
- Verified: in the sandbox image, `contracteer mock api/openapi.yaml` answers
  200 with schema-valid data and `contracteer verify` against it generates
  and passes one case.
- Left over / gotchas:
  - Contracteer 4.0.0's CLI (native image) cannot load any OpenAPI 3.1
    document: the mock fails to resolve `$ref`s and the verifier dies on
    `io.swagger.v3.oas.models.media.JsonSchema` reflection. Fixed upstream,
    ships with the next release; the JVM verifier is unaffected. Until then
    the contract stays 3.0.3, and `nullable` is the 3.0 keyword.
  - On an operation without parameters, a response example keyed `TOPHE` or
    `200_TOPHE` creates no scenario in 4.0.0: the verifier emits one
    `(generated)` case and the mock returns random values. Examples on such
    operations are documentation only.
  - This machine has no `contracteer` binary: run it from the sandbox image
    (`docker run --rm --entrypoint sh -v "$PWD/api:/api:ro" libris-agent:local`).

## 2026-09-09 — T005 Backend authentication — done (PR pending)
- Did: eleven cycles, one commit each. `MeControllerTest` first — the four
  `Remote-*` headers must answer the contract's four fields with `ADMIN`, red
  on `Status expected:<200 OK> but was:<401 UNAUTHORIZED>` — then
  `domain/Member.kt` (`Role`, `Member`), `infra/web/SecurityConfig.kt`
  (`RemoteHeaderAuthenticationFilter`, `MemberPrincipal`, the
  `PreAuthenticatedAuthenticationProvider` and the chain) and
  `infra/web/MeController.kt`. Then the role and display-name cases, the
  refusals in `SecurityConfigTest` (no identity headers, missing email, blank
  email, write without `X-Requested-With`, write with it, open actuator),
  `ApiContractTest` and `ArchitectureTest`. Dependencies: the security starter,
  `spring-boot-starter-test` and `spring-boot-resttestclient` (BOM-managed),
  `contracteer-verifier-junit` 4.0.0 and `archunit-junit5` 1.5.0 in the
  catalogue.
  Verified by command: the gate exits 0, and exits 0 again with `LIBRIS_DB_URL`,
  `LIBRIS_DB_USER` and `LIBRIS_DB_PASSWORD` unset; `dependencies --configuration
  runtimeClasspath` matches neither `postgresql` nor `flyway` nor `spring-data`;
  on `bootRun` the two README `curl` lines answer `MEMBER` and `ADMIN` and a
  bare call answers 403; `git status --porcelain` stayed empty after the gate.
- Decided:
  - Nothing the brief had not decided. Its measured shapes held on contact:
    the chain answers 403 without an entry point of its own, the `denyAll`
    matcher before `anyRequest().authenticated()` gives 403 without
    `X-Requested-With` and 405 with it, and the nested `@TestConfiguration`
    wrapping the request with a fixed member's headers is what makes the
    Contracteer case pass.
  - Two mutations recorded on the way, because four tests were green on
    arrival: forcing `Role.ADMIN` fails the plain-member case, and dropping
    the display-name fallback fails the username case. Cycle 1 wrote the group
    conditional and the fallback, so those cycles confirmed rather than drove.
- Left over / gotchas:
  - `detekt` runs on the test sources too, and only inside `check`:
    `./gradlew detekt` alone is the main sources. `VariableNaming` rejects a
    backticked property name, so ArchUnit's `@ArchTest val` fields fail the
    gate; ``@ArchTest fun `name`(classes: JavaClasses)`` with `.check(classes)`
    passes and keeps the readable test name. Backticked function names are
    fine.
  - `ReturnCount` allows two returns: the filter guards `Remote-User` and
    `Remote-Email` in one `if`, not with two `?: return null`.
  - `git checkout <file>` after a mutation check reverts to the last commit,
    not to the working tree — twice it wiped uncommitted implementation.
    Commit the cycle before mutating.
  - The contract test's failure without the fixed member is
    `Status code does not match. Expected: 200, Actual: 403`, and the
    ArchUnit rule's is `Class <fr.amory.libris.domain.Member> is annotated with
    <org.springframework.web.bind.annotation.RestController>`; both are the
    red step of their cycle, so no throwaway edit was needed after the fact.
  - No `application-dev.yaml` and no `application-contract-test.yaml`: the
    default chain trusts the headers in every profile, as the brief measured.
    The PR body asks Tophe whether D06's sentence about the `dev` profile
    should be reworded.

## 2026-09-09 — T006 Backend persistence: the member table and its repository — done (PR pending)
- Did: six cycles, one commit each. `MemberProfileTest` first (red: no class),
  then `domain/MemberProfile.kt` with the aggregate and the file-level
  `Generators.timeBasedEpochGenerator()`, which turned the domain ArchUnit rule
  red with the brief's four violations, so the rule gained
  `com.fasterxml.uuid..` in the same cycle. Then
  `JdbcMemberProfileRepositoryTest` — the big one: red on the missing port, on
  the missing bean (`No qualifying bean of type
  'fr.amory.libris.domain.MemberProfileRepository'`), then on the missing table
  (`relation "member" does not exist`), green with the port, the four
  dependencies, the datasource block, `V001__member.sql` and
  `JdbcMemberProfileRepository`. Then the unknown username, the duplicate
  username, and the two new ArchUnit rules, each written from its red step.
  The brief's measured shapes all held on contact; nothing was adapted.
- Verified by command: `cd backend && ./gradlew check` exits 0 — 20 tests in
  1 min 3 s, the four pre-existing classes included — with `git status
  --porcelain` empty afterwards; `gitleaks dir backend` finds no leak;
  `git diff main` is empty for `api/openapi.yaml`, `infra/web`,
  `domain/Member.kt`, `frontend/`, `deploy/` and `.github/`.
- Decided:
  - The naming scheme no document states: a persistence port is
    `<Aggregate>Repository`, its adapter `Jdbc<Aggregate>Repository`. The PR
    body proposes the sentence for D02 so a future run does not guess again.
  - Two tests were green on arrival (the unknown username, the duplicate), so
    each got a recorded mutation instead of a red step: `.optional().orElse(null)`
    replaced by `.single()` fails with `EmptyResultDataAccessException:
    Incorrect result size: expected 1, actual 0`, and dropping `unique` from
    `V001__member.sql` fails with `Expected exception
    org.springframework.dao.DuplicateKeyException but no exception was thrown`.
- Left over / gotchas:
  - **Mutating `V001__member.sql` needs the database dropped first, and again
    after.** Flyway validates the checksum at context startup, so once the
    mutated file has been applied, no `@SpringBootTest` can start — including
    a test written to drop the tables. The way out is
    `@SpringBootTest(properties = ["spring.flyway.enabled=false"])` on a
    throwaway class that runs `drop table if exists member` and
    `drop table if exists flyway_schema_history`; the next run re-applies the
    real migration. Drop *before* mutating and the detour is avoided. The
    throwaway class was deleted before the pull request.
  - The gate re-applying `V001` from scratch is also what proves nobody
    created the table by hand.
  - `ImportOrdering` fired on the third test's import and only inside `check`
    (`Imports must be ordered in lexicographic order…`), as the T005 entry
    warned: a partial `./gradlew test` will not catch it, so run the gate
    before believing a cycle is finished.
  - The datasource is now required: every `@SpringBootTest` fails without
    `LIBRIS_DB_URL` / `LIBRIS_DB_USER` / `LIBRIS_DB_PASSWORD`. T005's criterion
    that `check` passes with them unset is dead; `backend/README.md` says so.
