# Libris — Task backlog

> Ordered. The loop takes the **first unchecked** task. Keep tasks small enough
> for one PR a human reads in ten minutes. Each task states its acceptance
> criteria; "tests pass" is implied everywhere (see `CLAUDE.md`).
>
> Human steps are not tasks. A task that needs one states it as
> *Precondition (human)* on its line; Tophe does it between runs, and the
> planner reports `blocked` while it is missing.
>
> Phase 0 reviewed by Tophe on 2026-09-08. The phases after it will be
> derived from feature specifications, one feature at a time, once Phase 0
> is deployed; the first draft of those phases was dropped in PR #6.

## Phase 0 — Foundations, ending with `/api/v1/me` deployed

- [x] T001 Backend skeleton. Spring Boot + Kotlin, JDK 25, Gradle Kotlin DSL
      with `gradle/libs.versions.toml`; starters webmvc and actuator only:
      no Spring Security and no datasource yet, both arrive with T005.
      `./gradlew check` is the D07 gate for the code that exists: warnings
      as errors, detekt + formatting with the D10 rules (the JDK 25 gotcha
      is in `agent/PROGRESS.md`), JUnit 5 + Kotest assertions, Kover XML.
      Verified by commands, not tests: the gate exits 0; a `!!`, a
      `lateinit` and a formatting violation each fail it;
      `GET /actuator/health` on `bootRun` answers UP (D09).
      `backend/README.md`: run, test.
- [x] T002 Frontend skeleton. Vue 3 `<script setup>`, Vite, TypeScript strict,
      Vue Router, Pinia, vue-i18n (`fr` only), Tailwind, Vitest + Vue Test
      Utils, Prettier; `.node-version` 24, `save-exact` (D05, D10). Layers
      `domain/ application/ infra/ ui/` with eslint-plugin-boundaries enforcing
      the five D05 rules. `vite-plugin-pwa`: manifest (name Libris, theme
      colour, placeholder icons) and precached app shell only; API caching
      comes with the offline feature (PRD 4.8, P2). Dev server proxies `/api` to `localhost:8080`. Home view:
      static French title from i18n. `npm test` = `vue-tsc` + ESLint + Vitest
      with V8 coverage (D07); one component test; `npm run build` green.
      `frontend/README.md`: commands.
- [x] T003 Backend image. Precondition (human): the `images` job in
      `.github/workflows/ci.yml` (D09). Multi-stage `backend/Dockerfile`:
      Gradle build stage taking a `VERSION` build argument, JRE 25 runtime
      running the boot jar as a non-root user, configuration by environment
      only, `HEALTHCHECK` on `/actuator/health`, OCI labels (D09).
      `GET /actuator/info` reports the revision passed as `VERSION` (Spring
      Boot build info), with a test. The sandbox has no Docker (D08): the PR's `images`
      job is the oracle, and the PR body says so.
- [ ] T004 Frontend image. Multi-stage `frontend/Dockerfile`: Node build
      taking `VERSION`, nginx serving `dist/` with SPA fallback; `index.html`
      and the service worker `no-cache`, hashed assets `immutable`; OCI labels
      (D09). The revision shows in a footer, with a component test. Verified
      as T003.
- [ ] T005 Member profile, backend. Precondition (human): `GET /api/v1/me` in
      `api/openapi.yaml` (D04). Spring Security pre-authenticated header
      filter on `Remote-User/Groups/Name/Email`; starters data-jdbc and
      flyway, PostgreSQL driver, datasource from `LIBRIS_DB_*` (D08); `ADMIN`
      when in `libris-admin`; profile created on first visit (PRD 4.10);
      `V001__member.sql` with uuid v7 ids and `created_at`/`updated_at`
      auditing (D11); non-GET refused without `X-Requested-With` (D06); `dev`
      profile trusts the headers, `contract-test` profile authenticates a
      fixed member; Contracteer verifier-junit with the truncate + seed setup
      (D04). Tests: filter, first visit, repository test, contract. The six
      ArchUnit rules of D02 arrive with these first classes.
- [ ] T006 Member profile, frontend. `MeApi` port in `application/`, fetch
      client with hand-written types in `infra/api` (D04); Vitest global setup
      starts `contracteer mock`; home view greets the member by display name;
      401 or unexpected redirect reloads the page (D06); dev proxy adds a dev
      admin's `Remote-*` headers; `npm run dev:mock` (D05). Tests: client
      against the mock, home view with a fake port.
- [ ] T007 Production compose and runbook. `deploy/compose.yaml`: PostgreSQL
      18, backend and frontend from `ghcr.io/camory/libris-*:${LIBRIS_TAG}`,
      joined to the existing Traefik network, no published ports, no labels,
      named volumes for data and covers, secrets from an uncommitted `.env`
      (`deploy/.env.example` committed). `deploy/README.md`: prerequisites
      done by hand outside the repo (DNS, Traefik routers for
      `libris.amory.fr` and `/api` behind the Authelia forward-auth
      middleware, the Authelia rule and the `libris-admin` group), first
      deploy by version tag, upgrade, rollback, Gordien registration, restore
      from a dump (D09). No Docker in the sandbox: verified by Tophe's
      deployment.

Phase 0 is done when Tophe has tagged `v0.1.0`, deployed it, and checked from
a phone on mobile data: the Authelia login, the home page greeting the member
by name, the footer revision matching `git rev-parse v0.1.0`, the PWA installed on iOS and Android, and
what happens when the installed app is reopened after the Authelia session
expired (the D06 risk). The result goes in `agent/PROGRESS.md`; if the
redirect fails in the installed app, the D06 fallback becomes a task.

## Proposed (added by agent runs; a human promotes them into a phase)

- Loop: `sandbox_down` in `agent/loop.sh` runs `compose down`, which stops
  the sidecar but not the one-off `run` container, so an interrupted loop
  leaves the agent running and spending. Remove the run containers too
  (`docker compose rm -sf agent`, to verify) and cover it in
  `agent/test-loop.sh` if the fake `gh` pattern extends to `docker`
  (found 2026-09-08 when a launch was stopped after twenty seconds).
- Loop: `branch_pr` in `agent/loop.sh` matches pull requests by head
  branch name, so a task retried under the same slug after its PR was
  closed is refused as "closed without merge". Ignore a closed PR whose
  `headRefOid` is not an ancestor of the local branch, with a case in
  `agent/test-loop.sh` (found 2026-09-08 on T001's second attempt; worked
  around by renaming the branch to `task/T001-backend`).
- Frontend: upgrade to TypeScript 7 once `typescript-eslint` accepts it. T002
  pinned `typescript` 6.0.3 because `typescript-eslint` 8.70.0 declares
  `>=4.8.4 <6.1.0`, so the ESLint step of the gate cannot run with 7
  (found 2026-09-08 while scaffolding the frontend).
- Backend image: the Dockerfile declares only `org.opencontainers.image.version`
  and `.source`; `revision` and `created` come from the `images` job's
  `--label` flags, so a hand-built image carries two of the four D09 labels.
  Declaring all four needs build arguments the workflow does not pass, and the
  workflow is edited by humans only (found 2026-09-08 while writing T003).
