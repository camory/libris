# Libris — Architecture and decisions

> Read by every agent run. Decisions here are binding until changed by a human.
> Each decision has an ID so tasks and PRs can reference it.
> Reviewed decision by decision with Tophe on 2026-09-07.
> D09 amended on 2026-09-08: image tags, version exposure, hand-managed routing.

## Overview

```
phone / browser ──► https://libris.amory.fr
                             │
              Traefik (Kimsufi, existing, Let's Encrypt)
              forward-auth ──► Authelia (existing) ──► Remote-* headers
                             │
             ┌───────────────┴───────────────┐
          /api/**                           /**
             ▼                               ▼
   backend (Kotlin, Spring Boot,      frontend (Vue PWA,
   JRE 25, no published port)         static, nginx)
        │             │
   PostgreSQL 18   covers volume
   (data volume)
        │
   Gordien (existing): hourly pg_dump + restic

   backend ──► Google Books / Open Library / BnF   (outbound, optional, P2)
```

## Decisions

### D01 — Monorepo: two applications, one contract
`backend/` (Gradle, Kotlin DSL) and `frontend/` (npm, Vite) live in one
repository, with `api/openapi.yaml` at the root, shared by both. One PR can
change the contract, the backend and the frontend of a feature. Each app has
its own build, tests and Dockerfile. There is no root build: the whole
interface is `cd backend && ./gradlew check` and `cd frontend && npm test`.
CI runs each app's job only when that app or `api/` changed; a push to `main`
runs everything.

### D02 — Backend: Kotlin + Spring Boot, light hexagon
Spring Boot, Kotlin, JDK 25, one Gradle module. Persistence with
**Spring Data JDBC** (no JPA, no Exposed, no jOOQ). Migrations with
**Flyway**, SQL files, never `ddl-auto`.

Packages under `fr.amory.libris`:
- `domain` — aggregates as data classes, value types, domain rules, and the
  ports as plain Kotlin interfaces. Framework-free, with one concession:
  Spring Data's mapping annotations (`@Id`, `@Table`, `@MappedCollection`)
  are allowed so that no second persistence model exists.
- `application` — use-case services and transaction boundaries. Depends on
  `domain` only.
- `infra.web` — controllers, request/response DTOs, problem details.
- `infra.persistence` — Spring Data JDBC repositories, the port
  implementations, the SQL for search and listings.
- `infra.lookup` — Google Books, Open Library and BnF clients (P2).

Enforced by ArchUnit rules in the test suite (see D07):
1. `domain` depends only on the Kotlin/Java standard libraries and the Spring
   Data mapping annotations.
2. `application` depends only on `domain` (plus `@Service` / `@Transactional`).
3. `infra.*` packages depend on `domain` and `application`, never on each
   other.
4. No cycles between top-level packages.
5. Ports declared in `domain` are implemented only in `infra`.
6. `CrudRepository.save` is never called (see D11).

Considered and rejected: Ktor and http4k (the human reviewer's fluency is the
merge gate), Spring Modulith (over-engineering at this size).

### D03 — Database: PostgreSQL only
PostgreSQL 18, nothing else. Search is one ranked SQL query over:
- `item.search_text`, maintained by the application on every item save:
  title, subtitle, series name, author names, tag names. Renaming a series,
  an author or a tag re-saves its items.
- `item.search_vector`, a generated column derived from `search_text` and
  indexed with GIN, using two custom text search configurations built on
  `unaccent`: French stemming and plain tokens, so that both "Astérix" and a
  romanised manga title match.
- a `pg_trgm` index on `search_text` for typo tolerance on short queries.

The first migration creates the `unaccent` and `pg_trgm` extensions; both are
trusted, so the application user, which owns the database, needs no superuser.
No Elasticsearch, Meilisearch, ParadeDB or embedded Lucene. If relevance ever
proves insufficient, `search_text` is exactly the document a Meilisearch
container would index.

### D04 — Contract-first API, Contracteer on both sides
`api/openapi.yaml` (OpenAPI 3.1) is the single source of truth of the HTTP
API.
- Backend: verified in the test suite by
  `tech.sabai.contracteer:contracteer-verifier-junit` (pinned). The contract
  test's setup method truncates the tables and seeds whatever the document's
  examples reference, before every case.
- Frontend: developed and tested against `contracteer mock api/openapi.yaml`
  — the Vite dev proxy, the Vitest global setup and, later, Playwright all
  point at it. **No code generation**: request and response types are written
  by hand in `frontend/src/infra/api`; the mock is what catches drift.
- Named examples are optional. They document and disambiguate a request or a
  response; when present, request and response examples share a key and use
  fixed identifiers.
- The contract is edited with Tophe, never by a headless run alone. Before
  touching it, read <https://contracteer.dev/latest>. A gap in Contracteer
  blocks the task with a question; it is never worked around.
- Order of work: contract, then backend, then frontend.

### D05 — Frontend: Vue 3 + Vite + TypeScript, light hexagon
Vue 3 with `<script setup>`, Vite, TypeScript strict, Vue Router, Pinia (only
for the session and the offline catalogue cache), `vue-i18n` with the single
locale `fr`, Vitest with Vue Test Utils. Tailwind CSS only, hand-written
components, no UI library. Mobile first. Node latest LTS, pinned in
`frontend/.node-version`.

PWA via `vite-plugin-pwa` (Workbox): precached app shell, API GET responses
cached network-first with cache fallback, any non-GET fails immediately
offline with a clear message. No sync queue.

Layers under `frontend/src`:
- `domain/` — pure TypeScript: types and pure functions (series gaps, sort
  orders, reading-state transitions, validation rules). No Vue, no fetch, no
  DOM.
- `application/` — the use cases as composables, the Pinia stores, and the
  ports (plain interfaces such as `ItemsApi`).
- `infra/` — port implementations: `infra/api` (hand-written types, one fetch
  client per resource); later `infra/storage` if offline needs more than
  Workbox.
- `ui/` — `components/` (presentational: props in, events out, no API calls),
  `views/`, the router, the i18n messages.

Wiring happens once in `main.ts`, through provide/inject with typed keys;
tests provide fakes. Enforced by `eslint-plugin-boundaries`:
1. `domain` imports only `domain`.
2. `application` imports `domain` and its own ports, never `infra` or `ui`.
3. `infra` imports `domain` and `application` ports, never `ui`.
4. `ui/components` import `domain` and other components only; `ui/views` may
   import `application`.
5. Nothing imports `ui/views`, not even another view; the router alone does.
   Each view lives in its own folder under `ui/views`.

Test files (`*.spec.ts`) are outside the layer rules: nothing imports a spec,
so a spec may import any layer and any package to set up what it exercises.

### D06 — Authentication: delegated to Authelia
Authentication is delegated to the household's existing Authelia, through
Traefik forward auth. The backend trusts the `Remote-User`, `Remote-Groups`,
`Remote-Name` and `Remote-Email` headers via Spring Security's
pre-authenticated header filter. No passwords, no login screen, no app
session, no BCrypt.
- Trust boundary: in production the backend publishes no port and is reachable
  only through Traefik, which overwrites the Remote headers from Authelia's
  answer. Locally, a `dev` profile trusts the headers the Vite proxy adds.
  Integration tests set the headers directly. The contract-test profile
  authenticates every request as a fixed test member.
- Roles: every user Authelia lets through is `MEMBER`; members of the
  `libris-admin` group are `ADMIN`. The policy (one or two factors) is
  Authelia's.
- A member profile is created on first visit from the headers.
  `GET /api/v1/me` returns the current member and role.
- CSRF: Authelia's cookie is SameSite, and the backend refuses any non-GET
  request lacking the `X-Requested-With` header.
- The frontend treats a 401 or an unexpected redirect on an API call as an
  expired session and reloads the page. Logout is a link to Authelia's logout.
- The contract declares no security scheme: authentication is upstream.
- Risk to verify on real phones with the first deployed screen: the portal
  redirect inside an installed PWA (iOS opens other origins in an in-app
  browser). Fallback if it fails: app-native accounts behind Authelia's rule.

### D07 — Tests are the oracle
"Green" means these two commands pass; each lists exactly what it runs.
- Backend, `cd backend && ./gradlew check`: compile with warnings as errors;
  detekt with its formatting ruleset; JUnit 5 unit tests with Kotest
  assertions (JUnit 5 is the only test framework, Kotest is used as an
  assertion library only); integration tests of `infra.persistence` against
  the PostgreSQL of D08; the ArchUnit rules of D02; Contracteer verification
  against the running app under the contract-test profile; a Kover coverage
  report (XML, JaCoCo format).
- Frontend, `cd frontend && npm test`: `vue-tsc` type check; ESLint with the
  boundaries rules; Vitest unit tests plus `infra/api` against
  `contracteer mock`, started by the global setup; a V8 coverage report
  (LCOV).
- One database, shared sequentially: Spring-level tests run in a transaction
  rolled back at the end; HTTP-level tests (Contracteer) truncate and seed
  before every case. Test classes do not run in parallel.
- One test source set and one `test` task. No suffix sorts tests by what
  they need: a test that needs the database gets it from D08 like any other.
  Test classes are named after the Libris code they exercise. A test of
  framework or library wiring may be written while learning and is deleted
  before the pull request.
- Coverage: no total threshold. CI reports changed-line coverage with
  `diff-cover`; informational until the loop runs without human review, then
  a gate.
- No end-to-end tests before deployment; then a single Playwright smoke.

### D08 — The environment is given, not created
The build creates no infrastructure. It receives, identically in local dev,
the sandbox and CI:
- a PostgreSQL of the production major, through `LIBRIS_DB_URL`,
  `LIBRIS_DB_USER`, `LIBRIS_DB_PASSWORD` (default
  `jdbc:postgresql://localhost:5432/libris`, user and password `libris`);
- the `contracteer` binary on the PATH;
- the pinned JDK and Node.

Tests migrate the given database with Flyway and never drop or recreate it.
No Docker, no Testcontainers (it needs the Docker socket the sandbox
deliberately lacks), no other service. A test that needs more blocks the task.

### D09 — Runtime packaging and deployment
- Backend: multi-stage Dockerfile, JRE 25 image running the boot jar as a
  non-root user. Configuration by environment variables only.
- Frontend: multi-stage Dockerfile, static assets served by nginx with SPA
  fallback; `index.html` and the service worker uncached, hashed assets
  immutable.
- Images: `ghcr.io/camory/libris-backend` and `ghcr.io/camory/libris-frontend`,
  amd64 only, always sharing one tag since the contract couples them. CI
  builds both on every pull request after the test jobs, which is the proof
  for a Dockerfile change since the sandbox has no Docker, and pushes them
  on every push to `main` tagged `sha-<short sha>`. A release is a git tag `vX.Y.Z` created by
  a human; CI then re-tags the sha images with the version, so the image
  tested on `main` is the one released. No `latest`, no moving tag. The CI
  workflow is edited by humans only.
- Every image carries the OCI labels (`version`, `revision`, `created`,
  `source`); `version` is the `sha-<short sha>` tag, since a re-tagged image
  cannot know its release name. The application exposes that revision: the
  backend on `/actuator/info` (Spring Boot build info), the frontend in a
  footer. The release name lives in the registry and in `deploy/.env`.
- `deploy/` holds the production compose: PostgreSQL 18, backend and frontend
  pulled by `LIBRIS_TAG` from an uncommitted `.env`, joined to the existing
  Traefik network, no published ports, no labels, named volumes for data and
  covers. Traefik routing (`libris.amory.fr` to the frontend, `/api` to the
  backend, the Authelia forward-auth middleware on both routers) and the
  Authelia access rule are declared by hand in the server's Traefik dynamic
  configuration files and Authelia configuration, outside this repository;
  `deploy/README.md` states what they must contain. Spring Actuator's
  endpoints sit outside `/api`, are never routed by Traefik, and serve the
  container healthcheck.
- Deploy is manual: set `LIBRIS_TAG`, then `docker compose pull && docker
  compose up -d` on the Kimsufi box. Rollback is the previous tag.
- Backups are not the app's job: the server's Gordien (hourly `pg_dump` +
  restic) covers the database and the covers volume. Deployment registers
  Libris there; `deploy/README.md` documents the restore.

### D10 — Conventions
- Kotlin: official style, immutable by default, sealed types for states,
  constructor injection, no `!!`, no `lateinit` in production code — all
  enforced by detekt. Package root `fr.amory.libris`.
- TypeScript: strict; ESLint (with the boundaries rules) and Prettier.
- Versions: latest stable at scaffold time, pinned — Gradle version catalog
  `gradle/libs.versions.toml` on the backend, exact versions in `package.json`
  (save-exact) on the frontend. Upgrades are their own tasks.
- Commits: Conventional Commits with scopes `backend`, `frontend`, `api`,
  `agent`, `docs`, `deploy`; imperative; subject ≤ 72 characters; trailer
  `Co-Authored-By: Claude <noreply@anthropic.com>` on agent commits.
- Branches: `task/T###-slug` (implementer), `plan/<date>` (planner), free for
  humans. `main` takes PRs only, squash-merged, linear history. A PR title is
  a Conventional Commit subject: it becomes the squash commit's subject, and
  the PR body its message.
- API: paths under `/api/v1/`, camelCase JSON, RFC 9457 problem details,
  `X-Requested-With` required on every non-GET request.
- Language: code, commits, documents and identifiers in English; UI text in
  French.

### D11 — Data and API conventions
Identifiers
- Every table has a `uuid` primary key generated by the application — UUID
  version 7 via `com.fasterxml.uuid:java-uuid-generator` — and non-null in the
  domain. Persistence ports expose `insert` and `update`, implemented with
  `JdbcAggregateTemplate`; `CrudRepository.save` is never called (ArchUnit).
  Ids are strings in JSON.

Time
- Instants in `timestamptz`, exchanged as ISO-8601 UTC with the `Z` suffix.
  Calendar dates in `date`, exchanged as `YYYY-MM-DD`. Publication year is an
  integer.
- Every table has `created_at` and `updated_at`, filled by Spring Data JDBC
  auditing, never by hand.

Schema
- snake_case, singular table names (`item`, `copy`, `reading_state`). Join
  tables are named after both sides (`item_author`), with the role column on
  them.
- Enumerations stored as text with a CHECK constraint, never as PostgreSQL
  enum types. Values in UPPER_SNAKE (`BOOK`, `MANGA`, `BD`), identical in
  Kotlin, SQL and JSON.
- Foreign keys always declared. Deletes are hard; rows meaningless without
  their parent cascade (author and tag links, copies, reading states, loans).
  Whether deleting an item that still has copies must be refused is a product
  question (PRD open question 5).
- ISBN-13 / EAN stored normalised: thirteen digits, no separators, validated
  on input.
- Flyway files `V001__short_description.sql`, three digits, never edited once
  merged.
- No optimistic locking in v1.

API shapes
- Pagination: `page` (from 0) and `size` (default 50, maximum 200); the
  response carries `content`, `page`, `size`, `totalElements`. Sorting:
  `sort=<field>&order=asc|desc`, accepted fields listed per operation in the
  contract.
- Filters are query parameters named after the field, repeated for multiple
  values.
- Absent optional values are serialised as `null`, never omitted.
- Errors are RFC 9457 problem details. `type` is a slug under `/problems/`
  (`/problems/validation`, `/problems/not-found`, …) that the frontend
  switches on. Validation problems add `errors: [{ field, message }]`.
  Messages are in French; they reach the user.

Contract
- Every schema in `api/openapi.yaml` states `required` and `nullable`
  explicitly, since the frontend types are written by hand from it.

## Local development (human)

```
docker compose -f agent/compose.yaml up -d postgres   # throwaway DB on localhost:5432 (tmpfs: gone when recreated)
cd backend && ./gradlew bootRun                      # http://localhost:8080 — `dev` profile trusts Remote-* headers
cd frontend && npm run dev                           # http://localhost:5173 — proxies /api to :8080, adds a dev admin's Remote-* headers
contracteer mock api/openapi.yaml -p 9090            # the API from the contract alone
cd frontend && npm run dev:mock                      # like dev, but proxies /api to the mock
```

The Contracteer CLI comes from Homebrew (`brew install
sabai-tech/contracteer/contracteer`) or the GitHub release zip. Green means
`./gradlew check` and `npm test`, as defined in D07. Toolchain: JDK 25 and
Node 24, the latter pinned in `frontend/.node-version`.
