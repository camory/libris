# Libris — Architecture and decisions

> Read by every agent run. Decisions here are binding until changed by a human.
> Each decision has an ID so tasks and PRs can reference it.
> A decision states a rule. An example beside it names code that exists, so
> the rule can be read against the tree; no example decides the shape of code
> not yet written, which is a spec's or a brief's to decide.
> Reviewed decision by decision with Tophe on 2026-09-07; the history of
> each decision is in git.

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
        │
   PostgreSQL 18
   (data volume)
        │
   Gordien (existing): hourly pg_dump + restic

   backend ──► BnF, Open Library                   (outbound, optional)
```

## Decisions

### D01 — Monorepo: two applications, one contract
`backend/` (Gradle, Kotlin DSL) and `frontend/` (npm, Vite) live in one
repository. The contract they share lives in its own repository and each side
pins the release it implements (D04). One PR can change the backend and the
frontend of a feature. Each app has its own build, tests and Dockerfile. There
is no root build: the whole interface is `cd backend && ./gradlew check` and
`cd frontend && npm test`. CI runs every job on each push to `main`; on a
pull request the backend and frontend jobs run only when their directory or
the workflow changed, and the images job builds the sides that ran. A job
skips itself while its application does not exist.

### D02 — Backend: Kotlin + Spring Boot, a hexagon per bounded context
Spring Boot, Kotlin, JDK 25, one Gradle module. Persistence with
**Spring JDBC** (`JdbcClient`, hand-written SQL; no Spring Data, no JPA, no
Exposed, no jOOQ). Migrations with **Flyway**, SQL files, never `ddl-auto`.

Packages under `fr.amory.libris`: one per bounded context, `bibliography`
(what an edition is and where it is looked up) and `library` (who reads,
and the bookshelves they keep), each with the same three layers.
- `<context>.domain` — aggregates as data classes, value types, domain rules,
  and the ports as plain Kotlin interfaces. Framework-free: no annotation, no
  framework type; the only library is the uuid generator of D11, used by the
  id types (`ReaderId.new()`), never by an aggregate, which takes its id.
  A concern lives in a sub-package: `bibliography.domain.lookup` holds the
  external lookup port, what it answers, and the `EditionPreview` it answers
  with; `library.domain.reader` and `library.domain.bookshelf` hold one
  aggregate each with its repository port. The port's contract on time: a
  source answers within `LIBRIS_SOURCE_TIMEOUT` or answers `Failed`; the
  bound is the adapter's, and the use case never sees time.
- `<context>.application` — use-case services and transaction boundaries,
  the latter through Spring's `TransactionOperations`. Depends on `domain`
  only.
- `<context>.infrastructure.web` — controllers, request/response DTOs,
  problem details. A controller calls use cases and never a repository: a
  read of one aggregate with no rule is still a use case of its own
  (`FindDefaultBookshelf`), so the first rule that read gains lands in the
  use case, not at the edge. `library.infrastructure.web` also holds the
  security filter chain: the identity a request carries is a reader.
- `<context>.infrastructure.persistence` — the port implementations over
  `JdbcClient`: the SQL of every insert, update, lookup, search and listing,
  and the row-to-aggregate mapping, which is the aggregate's constructor.
- `bibliography.infrastructure.lookup` — the BnF and Open Library clients;
  Google Books later.

Enforced by ArchUnit rules in the test suite (see D07):
1. `domain` depends only on the Kotlin/Java standard libraries and the uuid
   generator of D11.
2. `application` depends only on `domain` (plus `@Service` and Spring's
   `TransactionOperations`, never `@Transactional`).
3. `infrastructure.*` packages depend on `domain` and `application`, never
   on each other.
4. No cycles between contexts, and `bibliography` never depends on
   `library`.
5. Ports declared in `domain` are implemented only in `infrastructure`.

Considered and rejected: Ktor and http4k (the human reviewer's fluency is the
merge gate), Spring Modulith (over-engineering at this size), Spring Data
JDBC (2026-09-09: its aggregate mapping needs `@Id` on the persisted class,
so a framework-free domain would carry a second persistence model and a
mapper per aggregate, which is all the framework saved), jOOQ (typed SQL is
the upgrade path if queries multiply; its code generator needs the migrated
schema at build time, machinery this size does not justify), Exposed and
SQLDelight (a second schema definition beside the Flyway files).

### D03 — Database: PostgreSQL only
PostgreSQL 18, nothing else. Search is one ranked SQL query over:
- `edition.search_text`, written by the persistence adapter on every insert
  and update of an edition from the names the aggregate holds: title,
  subtitle, series name, author names, tag names. The domain never sees it.
- `edition.search_vector`, a generated column derived from `search_text` and
  indexed with GIN, using two custom text search configurations built on
  `unaccent`: French stemming and plain tokens, so that both "Astérix" and a
  romanised manga title match.
- a `pg_trgm` index on `search_text` for typo tolerance on short queries.

The search migration creates the `unaccent` and `pg_trgm` extensions; both are
trusted, so the application user, which owns the database, needs no superuser.
No Elasticsearch, Meilisearch, ParadeDB or embedded Lucene. If relevance ever
proves insufficient, `search_text` is exactly the document a Meilisearch
container would index.

### D04 — Contract-first API, Contracteer on both sides
The OpenAPI 3.0.3 document `openapi.yaml` in the repository
`camory/libris-api` is the single source of truth of the HTTP API. It is
released, never consumed from a branch: one GitHub release per change, tagged
`v<info.version>`, with release immutability on, so a released tag never
moves and its content never changes. Each side pins the raw file of the
release it implements:
`https://raw.githubusercontent.com/camory/libris-api/v<version>/openapi.yaml`.
- Backend: verified in the test suite by
  `dev.contracteer:contracteer-verifier-junit` (pinned), loading the pinned
  URL. The contract test runs over the web slice of D07; its setup method
  stubs the use cases with whatever the document's examples reference, before
  every case.
- Frontend: developed and tested against `contracteer mock <pinned URL>` —
  the Vite dev proxy and the Vitest global setup both point at it. **No code generation**: request and response types are written
  by hand in `frontend/src/infra/api`; the mock is what catches drift.
- Named examples are optional. They document and disambiguate a request or a
  response; when present, request and response examples share a key and use
  fixed identifiers.
- The contract is written and released with Tophe, never by a headless run: a
  feature's contract is released in the session that writes its spec, and the
  spec's Contract section names the release. The first backend task and the
  first frontend task of the feature bump their side's pin to that release;
  the bump is the only contract-related edit a run makes, and it decides
  nothing about the contract itself. Before touching it, read
  <https://contracteer.dev/latest>. A gap in Contracteer blocks the task with
  a question; it is never worked around.
- Order of work: the contract is released first, then each side moves in its
  own task, and the second side moves only once the first is deployed. The
  order of the sides follows the change, so that the pair running at any
  moment stays compatible: an operation or a response field added goes
  backend first; a response field removed goes frontend first; a request
  field added goes backend first, optional until the frontend sends it. Only
  a change no order keeps compatible moves both sides at once, and the
  release says so. What keeps the pair compatible: the frontend reads the
  fields it knows and ignores the rest; the backend sends only what its pinned
  release declares. A release may describe operations no side implements yet:
  each gate checks only the release its side pins.

### D05 — Frontend: Vue 3 + Vite + TypeScript, light hexagon
Vue 3 with `<script setup>`, Vite, TypeScript strict, Vue Router, Pinia (only
for the session and the offline catalogue cache), `vue-i18n` with the single
locale `fr`, Vitest with Vue Test Utils. Tailwind CSS only, hand-written
components, no UI library. Mobile first. Node latest LTS, pinned in
`frontend/.node-version`.

PWA via `vite-plugin-pwa` (Workbox): precached app shell, API GET responses
cached network-first with cache fallback, any non-GET fails immediately
offline with a clear message. No sync queue.

`main.ts` alone reads `import.meta.env` and `window`; every other module
receives its configuration as an argument. The API client takes its base URL
from its constructor: `bootstrap` passes the origin it is given and a spec
passes the mock's. The revision the home page shows comes from `VITE_APP_VERSION`, the
one `VITE_*` variable; no other exists until a task needs one.
`createLibrisApp(ports, revision)` builds the application with a router, i18n
and Pinia of its own over the given port implementations;
`bootstrap(origin, revision)` builds the real ports over that origin and
calls it; `main.ts` reads the origin and the revision and mounts what
`bootstrap` answers. A scenario test calls `createLibrisApp` over the fakes of
`src/fixture`; one smoke case calls `bootstrap` with the mock's origin.

Layers under `frontend/src`:
- `domain/` — pure TypeScript: types and pure functions (series gaps, sort
  orders, reading-state transitions, validation rules). No Vue, no fetch, no
  DOM.
- `application/` — the use cases as composables, the Pinia stores, and the
  ports (plain interfaces such as `IsbnApi`).
- `infra/` — port implementations: `infra/api` (hand-written types, one fetch
  client per resource); later `infra/storage` if offline needs more than
  Workbox.
- `ui/` — `components/` (presentational: props in, events out, no API calls),
  `views/`, the router, the i18n messages.

Wiring happens once, in `createLibrisApp`, through provide/inject with typed
keys: a composable takes its port as an argument, the view that calls it
injects the port; tests provide fakes. Enforced by `eslint-plugin-boundaries`:
1. `domain` imports only `domain`.
2. `application` imports `domain` and its own ports, never `infra` or `ui`.
3. `infra` imports `domain` and `application` ports, never `ui`.
4. `ui/components` import `domain` and other components only; `ui/views` may
   import `application`, and the app shell may inject the ports of the chrome
   it owns.
5. Nothing imports `ui/views`, not even another view; the router alone does.
   Each view lives in its own folder under `ui/views`.

Test files (`*.spec.ts`) are outside the layer rules: nothing imports a spec,
so a spec may import any layer and any package to set up what it exercises.
`src/fixture` holds the shared fakes of the ports: it may import `domain` and
`application`, and only specs import it.

### D06 — Authentication: delegated to Authelia
Authentication is delegated to the household's existing Authelia, through
Traefik forward auth. The backend trusts the `Remote-User`, `Remote-Groups`,
`Remote-Name` and `Remote-Email` headers via a header filter of its own
that sets a pre-authenticated token. No passwords, no login screen, no app
session, no BCrypt.
- The headers are read in one class, the filter, which maps them to a
  username, an email, a display name and a list of groups; nothing else in
  the backend sees a header. There is no standard for header SSO (the names
  are Authelia's set of an old convention); the standard is OIDC, which
  Authelia also serves. Moving to OIDC later replaces that one filter by the
  mapping of the `preferred_username`, `email`, `name` and `groups` claims,
  and nothing else. Deferred until Libris leaves the household and must
  know its readers on its own: it needs a client registered in Authelia, a
  redirect flow, and the installed PWA's behaviour checked (see the risk
  below).
- Trust boundary: in production the backend publishes no port and is reachable
  only through Traefik, which overwrites the Remote headers from Authelia's
  answer. The backend trusts the headers in every profile; locally the Vite
  proxy adds them. Integration tests set the headers directly. The
  contract test adds a fixed reader's headers to every request through a
  test configuration of its own.
- The domain calls the person a `Reader`: one aggregate, id, username,
  email, display name and default bookshelf, keyed by username. On every
  request the filter hands the headers to `WelcomeReader`, which finds the
  reader by username or, on their first visit, creates them with their
  bookshelf, and sets the reader as the principal of the authentication.
  Controllers receive it with `@AuthenticationPrincipal`; no custom
  principal class.
- Roles are Spring authorities, never stored: every user Authelia lets
  through is `ROLE_READER`; members of the `libris-admin` group are also
  `ROLE_ADMIN`. The policy (one or two factors) is Authelia's.
- `GET /api/v1/me` returns the current reader and their role.
- CSRF: Authelia's cookie is SameSite, and the backend refuses any non-GET
  request lacking the `X-Requested-With` header.
- A 401 on an API call means an expired session. Authelia answers it, the
  backend never does, and the contract does not declare it. The frontend
  sends `Accept: application/json, application/problem+json` on every
  request; the absence of `text/html` is what makes Authelia answer 401
  rather than redirect. The frontend does not handle
  the 401 yet; the task that adds it gives the API client an
  `onUnauthenticated` callback that `bootstrap` wires to a navigation, so
  that the OIDC move changes `bootstrap` and not the client. A reload is not
  enough: the service worker answers every navigation from its cache, so
  the browser never reaches Traefik and Authelia never redirects. The
  navigation goes to a path the service worker leaves to the network,
  `/session`, which nginx answers with a redirect to `/`; Authelia
  intercepts it, logs the reader in and sends them back. Logout is a link
  to Authelia's logout.
- The contract declares no security scheme: authentication is upstream.
- Android only: nobody in the household has an iPhone. The portal redirect
  inside the installed PWA was checked on the Pixel with the first deployed
  screen, on 2026-09-10.
- The manifest link carries `crossorigin="use-credentials"`: a browser
  fetches a manifest without cookies otherwise, and behind Authelia that
  request is refused, which makes the app uninstallable.

### D07 — Tests are the oracle
"Green" means these two commands pass; each lists exactly what it runs.
- Backend, `cd backend && ./gradlew check`: compile with warnings as errors;
  detekt with its formatting ruleset; JUnit 5 unit tests with Kotest
  assertions (JUnit 5 is the only test framework, Kotest is used as an
  assertion library only); the JDBC slice of `infrastructure.persistence` against
  the PostgreSQL of D08; the ArchUnit rules of D02; Contracteer verification
  against the web slice on a real port; one test booting the whole
  application.
- Frontend, `cd frontend && npm test`: `vue-tsc` type check; ESLint with the
  boundaries rules; Vitest unit tests plus `infra/api` against
  `contracteer mock`, started by the global setup; a V8 coverage report
  (LCOV).
- Each layer is tested in isolation, and a test asserts what its layer
  owns. `domain` and `application` own the behaviour and the values:
  `application` runs plain JUnit over the fakes of its ports. `infrastructure.web` is
  proven by the Contracteer test: the web slice, the package with its
  security chain on a real port and no datasource, over stubbed use cases
  whose stubs mirror the document's examples; it proves structure and types,
  never values, by design. A hand-written test in the web slice exists only
  for behaviour the contract cannot express, such as a role derived from a
  header, and asserts no value the contract leaves free. A task that
  implements an operation of the contract opens with the pin bump: the new
  verification cases are its first red, the controller their green.
  `infrastructure.persistence` runs in the JDBC slice against the PostgreSQL of D08,
  each test in a transaction rolled back at the end. Every class that
  touches the database, the JDBC slice and the scenario classes, starts
  from a schema cleaned and migrated by Flyway before the class. One test
  boots the whole application and reads its health. Test classes do not run
  in parallel.
- The frontend follows the same slicing. `domain` is plain Vitest, no
  doubles. `application` composables and stores run over fakes of their
  ports, with a fresh Pinia per test and no component mounted: a composable
  is called with its fake port as an argument. `ui/components` mount with
  props and assert the rendered text and the emitted events. `ui/views`
  mount with the real i18n and a fake port provided through its injection
  key, and await `flushPromises` before asserting what the port answered. A
  view or component test asserts on what the reader sees, text and roles,
  never on tags or classes. `infra/api` runs against `contracteer mock`, one
  spec per operation the client implements, every response the contract
  declares and none it does not; the 401 of D06 is outside the contract and
  waits for its own task. One test creates the application through
  `createLibrisApp` over fake ports and checks the home view renders.
- Each feature spec has one scenario test class per side,
  `fr.amory.libris.scenario` on the backend and `src/scenario` on the
  frontend, one method per scenario or case, bearing its exact title. The
  backend boots the whole application over WireMock stubs of the sources;
  the frontend creates it through `createLibrisApp` over the fakes of its
  ports, its Given naming the reader, what each port answers and what it
  fails on, in the words of the spec. A fake answers what it was built with,
  and a scenario asserts what the reader sees, never what a fake recorded.
  The mock proves the communication with the API in `infra/api` and in one
  smoke case of the fast-entry scenarios, which boots through `bootstrap`. A
  scenario asserts the exact values its spec names: with the domain and
  application tests, it is where values are proven. Tophe writes them with
  the spec, committed skipped, and since the gate type-checks a skipped
  test, the spec piece also brings the port a scenario fakes, its fake in
  `src/fixture` and the domain types it names. A task un-skips the scenario
  tests its line cites and changes nothing else in them; the inside behind
  the ports, use cases, adapters, screens and their tests, is the run's. A
  scenario test that has to change is a spec conversation, not a task, with
  one exception: a scenario of another spec that compares a whole answer
  gains the field a task adds to it, declared in the pull request.
- One test source set and one `test` task. No suffix sorts tests by what
  they need: a test that needs the database gets it from D08 like any other.
  Test classes are named after the Libris code they exercise. Tests live
  beside the code they exercise: on the backend in its package, on the
  frontend as a sibling `.spec.ts`; a test of the whole application
  (contract, architecture, boot) lives in the backend's root package; shared
  test doubles and helpers live in the `fixture` package on the backend and
  in `src/fixture` on the frontend. A test body is laid out as Given, When,
  Then, marked by those three comments, unless it is a single statement. A
  test of framework or library wiring may be written while learning and is
  deleted before the pull request.
- Coverage: the frontend gate writes a V8 report that nothing reads; the
  backend has none. No threshold and no gate until the loop runs without
  human review.
- No end-to-end tests. The feature-level check is the *Done* of each spec,
  made by Tophe on the Pixel.

### D08 — The environment is given, not created
The build creates no infrastructure. It receives, identically in local dev,
the sandbox and CI:
- a PostgreSQL of the production major, through `LIBRIS_DB_URL`,
  `LIBRIS_DB_USER`, `LIBRIS_DB_PASSWORD`; on a developer's machine they
  may come from `backend/.env`, copied from `backend/.env.example`, which
  Gradle's `test` and `bootRun` read when the environment does not define
  them;
- the `contracteer` binary on the PATH;
- the pinned JDK and Node.

Tests migrate the given database with Flyway and never drop or recreate it.
Production differs from tests and local dev by these environment variables
only: there is no profile-specific configuration file.
No Docker, no Testcontainers (it needs the Docker socket the sandbox
deliberately lacks), no other service. A test that needs more blocks the task.

### D09 — Runtime packaging and deployment
- Backend: multi-stage Dockerfile, JRE 25 image running the boot jar as a
  non-root user. Configuration by environment variables only.
- Frontend: multi-stage Dockerfile, static assets served by nginx as a
  non-root user with SPA fallback; `index.html` and the service worker
  uncached, hashed assets immutable.
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
  backend on `/actuator/info` (Spring Boot build info), the frontend at the
  foot of the home page. The release name lives in the registry and in
  `deploy/.env`.
- `deploy/` holds the production compose: PostgreSQL 18, backend and frontend
  pulled by `LIBRIS_TAG` from an uncommitted `.env`, joined to the existing
  Traefik network, no published ports, no labels, one named volume for the
  data. Traefik routing (`libris.amory.fr` to the frontend, `/api` to the
  backend, the Authelia forward-auth middleware on both routers) and the
  Authelia access rule are declared by hand in the server's Traefik dynamic
  configuration files and Authelia configuration, outside this repository.
  The runbook (what those files contain, first deploy, upgrade, rollback,
  backup registration, restore) lives on the server beside the compose file,
  never in this public repository. Spring Actuator's
  endpoints sit outside `/api`, are never routed by Traefik, and serve the
  container healthcheck.
- Deploy is manual: set `LIBRIS_TAG`, then `docker compose pull && docker
  compose up -d` on the Kimsufi box. Rollback is the previous tag.
- Backups are not the app's job: the server's Gordien (hourly `pg_dump` +
  restic) covers the database. Deployment registers
  Libris there; the server's runbook documents the restore.

### D10 — Conventions
- Kotlin: official style, immutable by default, sealed types for states,
  constructor injection, no `!!`, no `lateinit` in production code — all
  enforced by detekt. Package root `fr.amory.libris`. A member is imported,
  not qualified, whenever its bare name is unambiguous: `OPEN_LIBRARY`,
  `Failed`, `RANDOM_PORT`, `ofSeconds(5)`; `MissingNode.getInstance()` stays
  qualified because `getInstance()` alone says nothing.
- The words of `docs/PRD.md` §3 name variables and parameters as they name
  types: an aggregate is called by its name, never shortened (`bookshelf`,
  not `shelf`), and an identifier is the aggregate's name with `Id`
  (`readerId`, `bookshelfId`), never the bare name (`reader` is a `Reader`).
- A use case is named by the sentence of what it does, an imperative verb
  first (`LookupEditionByIsbn`, `AddBookToBookshelf`, `WelcomeReader`), and
  is called by its name: it exposes `operator fun invoke` and nothing else
  public, and the variable holding it is the class name in lower camel case,
  so a call reads as the sentence, `lookupEditionByIsbn(isbn)`,
  `addBookToBookshelf(readerId, bookshelfId, book)`. A port keeps a verb of
  its own (`ExternalEditionLookup.lookUp`, `CopyRepository.findByEditionId`):
  its name says what it is, not what it does.
- A member's visibility is what the type exposes: a private member is not
  made public for a new caller. A caller that needs what the private member
  does either goes through the public door (`Isbn.of`) or the type gains a
  method of its own, decided in the task's brief.
- Spring test classes receive their beans through an `@Autowired`
  constructor; no field injection in tests.
- SQL: keywords and functions in upper case, identifiers in lower
  snake_case, in migrations and in the statements of the repositories:
  `SELECT edition.id FROM edition WHERE edition.isbn13 = :isbn13`. A
  migration already applied is never recased: Flyway checks its checksum.
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
- Every table has a `uuid` primary key, UUID version 7 via
  `com.fasterxml.uuid:java-uuid-generator`, wrapped in a value class per
  aggregate (`ReaderId`, `BookshelfId`) whose `new()` mints it. An aggregate
  takes its id as a constructor parameter and never generates one: the use
  case that creates it mints the id, and a row read back passes its own. Persistence ports expose
  `insert` and `update`, each one SQL statement over `JdbcClient`; nothing
  decides between the two from the state of the aggregate. Ids are strings
  in JSON.

Time
- Instants in `timestamptz`, exchanged as ISO-8601 UTC with the `Z` suffix.
  Calendar dates in `date`, exchanged as `YYYY-MM-DD`. Publication year is an
  integer.
- A date the product shows (added, acquired, lent since) is an attribute of
  its aggregate, set by the use case that creates it; there are no audit
  columns.

Schema
- snake_case, singular table names (`edition`, `copy`, `bookshelf`). The
  children an aggregate owns sit in a table named after the child
  (`membership`, the bookshelf's; `contribution`, the edition's), and a
  child that references a shared name row does so by that row's id
  (`contribution.author_id`). A join table between two aggregates, when one
  comes, is named after both sides. The role column sits on the child's
  table.
- Enumerations stored as text, never as PostgreSQL enum types, and without
  a CHECK constraint: the Kotlin enum is the one source of truth, and a row
  is read through its `valueOf`. Values in UPPER_SNAKE (`BOOK`, `MANGA`,
  `BD`), identical in Kotlin, SQL and JSON.
- Foreign keys always declared. Deletes are hard; rows meaningless without
  their parent cascade (author and tag links, copies, reading states, loans).
  Removing the last copy of an edition deletes the edition (PRD §3).
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
- Errors are RFC 9457 problem details, `application/problem+json`, with
  `type`, `title` and `status`, no `detail`: the wording is the frontend's,
  which switches on `type`, a slug under `/problems/` (`/problems/validation`,
  `/problems/not-found`, …). Validation problems add
  `errors: [{ field, code }]`: `field` names the request field, `code` is a
  slug that says why it was refused. A `code` is documentation until a
  client must tell two refusals of one field apart; then the contract
  enumerates it for that operation, and only then does a test assert it. A
  problem carries no nullable field, since Spring omits the empty fields of
  a `ProblemDetail`.
- Every error the API describes carries a problem body, whatever its status.
  Traefik and Authelia answer plain text or HTML, so a problem body is how
  the client tells an answer of Libris from one of the infrastructure: a
  5xx without one means Libris itself is unavailable.
- The API sends keys, the frontend owns the wording: `type` is the key of a
  problem, `code` the key of a field's refusal, and the frontend maps them to
  its own i18n entries with a fallback for a key it does not know. When a
  message needs data (a limit, a name), the problem gains a structured
  extension member declared in the contract, never a sentence. An outcome of
  a use case (not found, sources unavailable, a refused value) is a result the
  controller answers, not an exception; Spring's problem advice
  (`spring.mvc.problemdetails.enabled`, or an advice extending
  `ResponseEntityExceptionHandler`) is for the failures the framework raises
  before a controller runs (an unreadable body, a parameter of the wrong
  type) and arrives with the first operation whose contract declares one,
  shaped as above. Considered and rejected: sentences rendered by the server
  under `Accept-Language` (wording in two repositories, a new sentence is a
  deploy, the screen cannot adapt it to its context, an offline PWA has none);
  a message-key field beside `type` and `code` (a key twice, or the API
  coupled to the layout of a translation file).

Contract
- Every schema in the contract states `required` and `nullable`
  explicitly, since the frontend types are written by hand from it.

### D12 — The domain model
- An aggregate keeps its own rules. What must hold for it to exist is
  checked where it is built, never in a use case: an `init` block when the
  rule reads the constructor's parameters (`Bookshelf` refuses memberships
  without an `OWNER`), a factory on the companion when the rule also decides
  a value (`Bookshelf.ownedBy` names the bookshelf after its owner and makes
  them its one member). A row read back passes through the same
  constructor, so the database never holds what the domain refuses. A use
  case that checks a rule an aggregate could check itself has the rule in
  the wrong place.
- A rule that spans two aggregates is the use case's, kept by one
  transaction: the reader's default bookshelf is one they own, which neither
  `Reader` nor `Bookshelf` can check alone, so `WelcomeReader` builds both
  and inserts both inside one `TransactionOperations` block, which is what
  guarantees no reader exists without their bookshelf. The use case's test
  proves the boundary, not only the result: its fake of
  `TransactionOperations` records what the stores held before and after
  each block. The schema keeps of such a rule only what a foreign key can
  say.
- An aggregate refers to another by its id, never by the object, across the
  boundary D02 keeps one-way as well: a `Copy` of the library holds an
  `EditionId` of the bibliography. A repository reads and writes one
  aggregate and joins no other; a use case that needs two reads two, each
  through its own port.
- A child an aggregate owns has no id and no repository of its own: it is a
  value in the aggregate's list, written and read with the aggregate by its
  repository, in the table D11 names after the child (`membership`).
  Changing a child is building the aggregate again with the new list and
  calling `update`; nothing addresses a child from outside its aggregate.
- A value the domain can refuse has two doors. Its constructor `require`s
  and throws, the last defence against a caller that builds it by hand. A
  factory `of(...)` on the companion answers `null` for what the domain
  refuses, and the caller decides what the `null` means: an adapter maps
  through `of` and drops or refuses what it answers `null` to (`Isbn.of` for
  a text typed or scanned). No use case or adapter catches the constructor's
  exception.
- A collection with a rule of its own is a type of its own, built through
  `of(...)` and never by hand, and held by the aggregate, so that no lookup,
  persistence or use case carries the rule and the order stored or answered
  is the house's, not a source's: `Contributions.of(list)` keeps one
  contribution per author and role, the names matched as PRD §3 matches
  them, ordered by role then by name. An enumeration whose order carries
  meaning declares it as a field (`ContributionRole.order`), never through
  its declaration order.
- A read that spans aggregates or contexts is the use case's first: it
  reads each aggregate through its own repository, applies the aggregates'
  rules and answers a data class of its `application` sub-package shaped
  for the answer, with no rule and no repository (`CopyOnBookshelf`, the
  lookup's copies with the names of their bookshelves). Each context answers
  what it knows: the bibliography whether the house holds the edition or
  what the sources say, the library the reader's copies on top of that
  answer. Only a read the repositories cannot serve within a few queries
  gets a query port of its own, in the `domain` of the context that asks and
  in the sub-package of its concern, answering a read model the persistence
  adapter builds from one query. No aggregate is rebuilt from a join, and no
  aggregate carries another's data to spare a query.
- A series, an author and a tag are values of the edition, not aggregates:
  `SeriesEntry(name, volumeNumber)` and `Contribution(name, role)` on the
  `Edition`, matched by name as PRD §3 says. Each name has a row of its own
  in persistence, with a uuid key and never doubled, and the edition's child
  tables reference it: that row is what a filter or a sort by the name
  reads. There is no renaming: a name is corrected edition by edition. Such
  a value becomes an aggregate the day it gains a fact of its own.

## Local development (human)

```
docker compose -f agent/compose.yaml up -d postgres   # throwaway DB on localhost:5432 (tmpfs: gone when recreated)
cp backend/.env.example backend/.env                  # once per checkout; the three database variables
cd backend && ./gradlew bootRun                      # http://localhost:8080 — trusts the Remote-* headers the caller sets
cd frontend && npm run dev                           # http://localhost:5173 — proxies /api to :8080, adds a dev admin's Remote-* headers
contracteer mock <pinned URL> -p 9090                # the API from the contract alone (URL: see D04)
cd frontend && npm run dev:mock                      # like dev, but proxies /api to the mock
```

The Contracteer CLI comes from Homebrew (`brew install
contracteer-dev/contracteer/contracteer`) or the GitHub release zip. Green means
`./gradlew check` and `npm test`, as defined in D07. Toolchain: JDK 25 and
Node 24, the latter pinned in `frontend/.node-version`.
