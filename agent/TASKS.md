# Libris — Task backlog

> Ordered. The loop takes the **first unchecked** task. Keep tasks small enough
> for one PR a human reads in ten minutes. Each task states its acceptance
> criteria; "tests pass" is implied everywhere (see `CLAUDE.md`).
>
> Human steps are not tasks. A task that needs one states it as
> *Precondition (human)* on its line; Tophe does it between runs, and the
> planner reports `blocked` while it is missing.
>
> Phase 0 reviewed by Tophe on 2026-09-08 and closed on 2026-09-10 with
> `v0.1.3` on the Pixel. Phase 1 drafted on 2026-09-10 from PRD §4.1 and
> §4.2; the phases after it come one feature at a time, once this one is
> deployed.

## Phase 0 — Foundations, ending with `/api/v1/me` deployed

- [x] T001 Backend skeleton. Spring Boot + Kotlin, JDK 25, Gradle Kotlin DSL
      with `gradle/libs.versions.toml`; starters webmvc and actuator only:
      no Spring Security and no datasource yet, both arrive with T005.
      `./gradlew check` is the D07 gate for   the code that exists: warnings
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
- [x] T004 Frontend image. Multi-stage `frontend/Dockerfile`: Node build
      taking `VERSION`, nginx serving `dist/` with SPA fallback; `index.html`
      and the service worker `no-cache`, hashed assets `immutable`; OCI labels
      (D09). The revision shows in a footer, with a component test. Verified
      as T003.
- [x] T005 Backend authentication. Precondition (human): `GET /api/v1/me` in
      `api/openapi.yaml` (D04). Spring Security pre-authenticated header
      filter on `Remote-User/Groups/Name/Email`; `ADMIN` when in
      `libris-admin`, `MEMBER` otherwise; `GET /api/v1/me` answers the
      username, display name, email and role read from the headers, nothing
      stored and no datasource; non-GET refused without `X-Requested-With`
      (D06); `dev` profile trusts the headers, `contract-test` profile
      authenticates a fixed member; Contracteer verifier-junit (D04). Tests:
      filter, role, refusal, contract. The D02 ArchUnit rules that these
      classes give something to check arrive here, the rest with T006.
- [x] T006 Backend persistence: the reader entity and its repository.
      Starter jdbc, `spring-boot-flyway` with `flyway-database-postgresql`,
      and the PostgreSQL driver; datasource from `LIBRIS_DB_URL` /
      `LIBRIS_DB_USER` / `LIBRIS_DB_PASSWORD`, migrated by Flyway, never by
      `ddl-auto` (D02, D08). `V001__reader.sql`: `id` uuid primary key,
      `username` unique and not null, `email` not null, `display_name` not
      null; no other column (D11). `domain`: the one entity for a person,
      `Reader(id, username, email, displayName)`, its `id` defaulted to a
      version 7 uuid from `com.fasterxml.uuid:java-uuid-generator`, no
      annotation (D02, D11, D06); it replaces T005's `Member` class: the
      filter builds a `Reader` from the headers (its id not stored until
      T009) and maps the groups to the Spring authorities `ROLE_READER` and
      `ROLE_ADMIN`; the T005 principal class carries the reader and the
      authorities; T005's `Role` enum survives only as the JSON type of the
      `/me` response in `infra.web`, derived from the authorities. Contract
      edit, decided with Tophe on 2026-09-09 (D04): the schema
      `CurrentMember` is renamed `CurrentReader`, its `role` enum becomes
      `[READER, ADMIN]`, the operation summary becomes "The reader making
      the request, as Authelia describes them" and the response description
      "The reader's identity and role"; nothing else in `api/openapi.yaml`
      changes, and `ApiContractTest` going red on the role value is the
      task's first red step.
      The port `ReaderRepository` exposes `insert` and `findByUsername`;
      `infra.persistence`: `JdbcReaderRepository` over `JdbcClient`, one
      insert and one select, the row mapped by the entity's constructor
      (D02). Tests: the repository against the PostgreSQL of D08 — insert
      then find by username returns an equal reader, an unknown username
      finds none, a second row with the same username refused — in a
      transaction rolled back at the end (D07); the T005 tests keep passing.
      ArchUnit: the domain rule gains the uuid generator; new rules: `infra`
      packages never depend on each other, a port declared in `domain` is
      implemented only in `infra` (D02); the `application` rule arrives with
      T009.
- [x] T009 Reader on every request. Starts with the contract, decided with
      Tophe on 2026-09-09 (D04): `CurrentReader` gains `id`, `type: string`,
      `format: uuid`, `nullable: false`, listed in `required`; the operation
      summary becomes "The reader making the request: their profile and
      role"; nothing else in `api/openapi.yaml` changes, and
      `ApiContractTest` going red on it is the task's first red step.
      `application`: the visit use case — given the username, email and
      display name of the request, find the reader by username or insert
      them; the display name is seeded from the header on the first visit and
      then owned by Libris (PRD 4.10); two simultaneous first visits end with
      one row. `infra.web`: the filter calls the use case on every request
      and sets the `Reader` entity as the principal of the authentication,
      with the authorities mapped from `Remote-Groups`; the T005 principal
      class goes, controllers take `@AuthenticationPrincipal` (D06). `/me`
      answers the stored reader plus the id, the role derived from the
      authorities. The Contracteer setup truncates and seeds before every
      case (D04). Tests: first visit inserts, the next visit returns the
      stored reader and keeps the display name Libris owns, two concurrent
      first visits, the filter with a stored reader, contract. The last D02
      ArchUnit rule: `application` depends only on `domain`.
- [x] T007 Frontend API client for the current reader. `src/domain`: the reader
      type, pure TypeScript (`id`, `username`, `displayName`, `email`, and the
      role `READER | ADMIN`); `src/application`: the `MeApi` port, one method
      answering that type; `src/infra/api`: the hand-written type of the
      `CurrentReader` response and the fetch client implementing the port,
      taking its base URL as a constructor argument and sending
      `Accept: application/json` (D04, D05, D06). No code generation, and
      `api/openapi.yaml` does not change. A Vitest global setup starts
      `contracteer mock api/openapi.yaml -p <port>` before the suite and stops
      it after; the spec passes that base URL to the client (D04, D07). Test:
      one spec beside the client, the single operation the contract declares,
      every response it declares (`200` only) and none it does not; the
      contract carries no examples, so the mock answers generated values and
      the spec asserts the shape, not the values. `frontend/README.md` says
      the gate now needs the `contracteer` binary on the PATH (D08); the CI
      `frontend` job already installs it. Nothing imports the client yet: the
      home view is T010.
- [x] T010 The reader on the home page. `createLibrisApp(ports, revision)`
      builds a router, i18n and Pinia of its own over the given ports and
      provides them through typed injection keys; `main.ts` alone reads
      `import.meta.env`, builds the real `MeApi` over the same-origin base URL
      and mounts the application; `App.vue` receives the revision instead of
      reading it (D05). The home view injects `MeApi` and greets the reader by
      display name, the message in the `fr` catalogue (PRD §4.10, §5). Shared
      fakes of the ports live in `src/fixture` (D07), with the ESLint
      boundaries element that lets a spec import them. The dev proxy adds a
      dev admin's `Remote-*` headers, and `npm run dev:mock` proxies `/api` to
      the Contracteer mock instead of the backend (D05, D06). Tests: the home
      view mounted with the real i18n and a fake port provided through its
      key, awaiting `flushPromises`; the application created through
      `createLibrisApp` over fake ports renders the home view (D07).
- [x] T008 Production compose and runbook. `deploy/compose.yaml`: PostgreSQL
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

- [x] T011 PWA installable behind Authelia. `useCredentials: true` in the
      `VitePWA` options, so the built `index.html` links the manifest with
      `crossorigin="use-credentials"` and the browser sends the Authelia
      cookie when it fetches it. Verified by the built `dist/index.html` and
      by the install prompt on an Android phone (D06). Hand task.
- [x] T012 Expired session in the browser and the installed app. The service
      worker's navigation route excludes `/session`
      (`workbox.navigateFallbackDenylist`), `frontend/nginx.conf` answers
      `/session` with a 302 to `/`, `FetchMeApi` takes an `onUnauthenticated`
      callback called on a 401 (its spec produces the 401 from a fake `fetch`,
      the Contracteer mock never serves one), and `main.ts` wires it to
      `window.location.assign("/session")`. Verified by the specs, the built
      `sw.js`, and by hand on the Pixel: reopen the site and the installed app
      after the Authelia session expired, land on the login, come back greeted
      (D06). If the installed app fails the redirect, the D06 fallback becomes
      a task.

Phase 0 is done when Tophe has released a version, deployed it, and checked
from an Android phone on mobile data: the Authelia login, the home page
greeting the reader by name, the footer revision matching
`git rev-parse --short <tag>`, the PWA installed, and the reopen after the
Authelia session expired (T012). The result goes in `agent/PROGRESS.md`.
Checked with `v0.1.0` on 2026-09-10: login, greeting and revision pass; the
install prompt is missing (T011); the greeting vanishes on an expired
session (T012). Checked with `v0.1.3` on 2026-09-10: installed from Brave on
the Pixel and on the Mac; the reopen after an expired session, in Brave and
in the installed app, goes through the login and comes back greeted.
**Phase 0 is done.**

## Phase 1 — The catalogue: enter it, browse it, find it again

> Serves PRD §4.1 and §4.2. Every backend task changes `api/openapi.yaml`, so
> every one of them opens on a *Precondition (human)*: Tophe writes the
> operation and its schemas with Claude in an interactive session (D04), the
> line below saying what it must carry. Copies, loans, reading states,
> wishlist, series gaps and the reader profiles Authelia does not own
> (PRD §4.3, §4.4, §4.5, §4.6, §4.10) are Phase 2, planned once this phase is
> deployed.

- [ ] T020 Backend: create and list items. Precondition (human):
      `POST /api/v1/items` and `GET /api/v1/items` in `api/openapi.yaml`,
      carrying an `Item` (id, type `BOOK|MANGA|BD`, title, and the nullable
      subtitle, isbn13, publisher, publicationYear, language, pageCount,
      summary), a `NewItem` without the id, a page (`content`, `page`, `size`,
      `totalElements`, `sort=title`) and `/problems/validation` with
      `errors: [{field, message}]` (D04, D11). `V002__item.sql` creates those
      columns with `type` under a text CHECK. `domain.Item` defaults its id to
      a version 7 uuid, refuses a blank title and keeps an ISBN as thirteen
      digits or refuses it; `application` gains the add and the list use case;
      `JdbcItemRepository` holds the insert, the page query and the count;
      `ItemController` maps the refusals to the validation problem.
      Tests: the use cases over a fake port, the JDBC slice against the D08
      database, the web slice on both operations and on a 400, contract
      (PRD §4.1, D02, D03, D11).

- [ ] T021 Frontend: the catalogue list. `domain/Item.ts`, the `ItemsApi` port
      with `list()`, `infra/api/FetchItemsApi.ts` with the hand-written page
      and item types, and its fake in `src/fixture` (D05, D07).
      `ui/views/catalogue/CatalogueView.vue` on the route `/items`, injecting
      the port, listing each item's title and type in French, with an empty
      state and a link to it from the home view.
      Tests: the client against `contracteer mock`, the one operation the
      contract declares and every response it declares; the view mounted with
      the real i18n and the fake port, awaiting `flushPromises` — the titles
      the port answered, and the empty state when it answers none
      (PRD §4.1, D04, D05, D07).

- [ ] T022 Frontend: add an item. `ItemsApi.add`, implemented by
      `FetchItemsApi` with `X-Requested-With` and `Accept: application/json`,
      turning a `/problems/validation` body into errors by field (D06, D11).
      `ui/views/item-new/NewItemView.vue` on `/items/new`, one French-labelled
      control per property of `NewItem`, reached from the catalogue; a
      successful save routes to `/items`.
      Tests: the client against the mock on the 201 and the 400; the view over
      the fake port — a save calls the port with what was typed and routes, a
      validation problem shows its message beside the named field
      (PRD §4.1, D05, D07).

- [ ] T023 Backend: read, edit and delete an item. Precondition (human):
      `GET`, `PUT` and `DELETE /api/v1/items/{id}` in `api/openapi.yaml`, the
      `PUT` taking a `NewItem` and answering the `Item`, the `DELETE`
      answering `204`, both plus `GET` answering `/problems/not-found` (D04).
      `ItemRepository` and `JdbcItemRepository` gain `findById`, `update` and
      `delete`, one SQL statement each (D11); one use case each, the update
      keeping the id and refusing what the add refuses.
      Tests: the JDBC slice — an update reads back changed, a delete leaves
      nothing, an unknown id finds none; the use cases over the fake port; the
      web slice on the 404; contract (PRD §4.1, D02, D11).

- [ ] T024 Frontend: the item page. `ItemsApi.byId`, the route `/items/:id`
      and `ui/views/item/ItemView.vue` showing every property the item carries
      under a French label, hiding the ones it does not; each row of the
      catalogue links to it; an unknown id shows a not-found message rather
      than an empty page.
      Tests: the client against the mock on the 200 and the 404; the view over
      the fake port on an item with every property, on one with only the
      required ones, and on the not-found answer (PRD §4.1, D05, D07).

- [ ] T025 Frontend: edit and delete an item. `ItemsApi.update` and
      `ItemsApi.remove` on the client; the T022 form extracted into a
      component that takes initial values, mounted by
      `ui/views/item-edit/EditItemView.vue` on `/items/:id/edit`; the item
      page offers the edit link and a delete that asks for confirmation and
      then routes to `/items`.
      Tests: the client against the mock on the `PUT` and the `DELETE`; the
      form component rendering its initial values and emitting what was
      changed; the item view — a confirmed delete calls the port and routes,
      a cancelled one does neither (PRD §4.1, D05, D07).

- [ ] T026 Backend: series and volume number. Precondition (human): `Item`
      gains `series` (nullable object of id and name) and `volumeNumber`
      (nullable integer, minimum 1); `NewItem` gains `seriesName` (nullable)
      and `volumeNumber` (D04). `V003__series.sql`: `series(id, name unique
      not null)`, `item.series_id` a nullable foreign key, `item.volume_number`
      a nullable integer (D11). The add and update use cases find the series
      by name or insert it, and a blank name leaves the item without one.
      Tests: the use case — an existing name is reused, a new one is inserted,
      a blank one clears the series; the JDBC slice on the join; the web
      slice; contract (PRD §3, §4.1, D03, D11).

- [ ] T027 Frontend: series and volume on the item. The hand-written types and
      the form gain the series name and the volume number; the catalogue row
      and the item page show them in French next to the title.
      Tests: the client against the mock on an item with a series and one
      without; the form component round-tripping both fields; the catalogue
      and item views showing the series and volume the port answered, and
      neither when it answers null (PRD §4.1, D05, D07).

- [ ] T028 Backend: authors by role. Precondition (human): `Item` gains
      `authors`, a required array (possibly empty) of `{id, name, role}` with
      `role` in `WRITER|ARTIST|COLOURIST|TRANSLATOR`; `NewItem` gains the same
      array without the ids (D04). `V004__author.sql`: `author(id, name unique
      not null)` and `item_author(item_id, author_id, role)` with the role on
      the join table and a cascade from the item (D11). The use cases find an
      author by name or insert them; saving an item replaces its whole author
      list; the repository reads them back ordered by role then name.
      Tests: the use case on a reused and on a new author and on a replaced
      list; the JDBC slice on the join and on the cascade; the web slice;
      contract (PRD §3, §4.1, D02, D11).

- [ ] T029 Frontend: authors on the item. The form gains a list of author rows
      — a name and a role chosen from the four, added and removed one at a
      time; the item page groups the authors under their French role name; the
      catalogue row shows the first author.
      Tests: the client against the mock on an item with several authors and
      one with none; the form component adding, filling and removing a row;
      the item and catalogue views on what the port answered
      (PRD §4.1, D05, D07).

- [ ] T030 Backend: free tags. Precondition (human): `Item` and `NewItem` gain
      `tags`, a required array (possibly empty) of non-blank strings (D04).
      `V005__tag.sql`: `tag(id, name unique not null)` and
      `item_tag(item_id, tag_id)` cascading from the item (D11). The use cases
      trim each tag, drop the blanks, treat two tags differing only by case as
      one, and replace the item's whole tag list on every save.
      Tests: the use case on a reused tag, a new one, a duplicate differing by
      case and a blank; the JDBC slice on the join and the cascade; the web
      slice; contract (PRD §4.1, D11).

- [ ] T031 Frontend: tags on the item. The form gains a tag input that adds a
      tag on Enter and removes one by its own control; the item page lists the
      tags; the catalogue row does not.
      Tests: the client against the mock on a tagged item and an untagged one;
      the form component adding and removing tags and emitting the list; the
      item view on the tags the port answered (PRD §4.1, D05, D07).

- [ ] T032 Backend: search. Precondition (human): `GET /api/v1/items` gains
      the nullable query parameter `q`; with it the page is ordered by
      relevance instead of by title (D04). `V006__search.sql` creates the
      `unaccent` and `pg_trgm` extensions and the two text search
      configurations of D03, adds `item.search_text` and the generated,
      GIN-indexed `item.search_vector`, the trigram index, and backfills the
      rows already there. The use cases write `search_text` on every save from
      the title, subtitle, series name, author names and tag names; the
      repository's ranked query answers it (D03).
      Tests: the JDBC slice — `asterix` finds *Astérix*, an author name and a
      tag find their item, a one-word typo finds it, the title match outranks
      the tag match, an ISBN finds its item; the web slice; contract
      (PRD §4.2, §5, D03).

- [ ] T033 Frontend: the search box. The catalogue view gains a single search
      field, French-labelled, that calls `list({ q })` as the reader types
      (debounced), keeps `q` in the query string so the page can be reloaded
      or shared, shows how many items matched and an empty state naming what
      was searched.
      Tests: the client against the mock with and without `q`; the view over
      the fake port — typing calls the port once with the text, the results
      replace the list, no match shows the empty state, and a `q` already in
      the route is searched on mount (PRD §4.2, D05, D07).

- [ ] T034 Backend: filter and sort the catalogue. Precondition (human):
      `GET /api/v1/items` gains the repeatable filters `type`, `series` and
      `tag`, and accepts `sort` in `title|series|addedAt` with
      `order=asc|desc` (D04, D11). `V007__item_added_at.sql` adds
      `added_at timestamptz not null`, set by the add use case (D11); the sort
      by `series` orders by series name then volume number, the items without
      a series last.
      Tests: the JDBC slice — one case per filter, one per combination of two,
      one per sort in both orders; the web slice on an unaccepted sort field
      answering the validation problem; contract (PRD §4.1, D11).

- [ ] T035 Frontend: filter, sort and page through the catalogue. The
      catalogue view gains a type filter, a series filter, a tag filter, a
      sort control and a way to reach the pages after the first, all kept in
      the query string beside `q`.
      Tests: the client against the mock passing each parameter; the view over
      the fake port — choosing a filter calls the port with it, choosing a
      sort calls the port with it, the next page appends or replaces as the
      view shows it, and the query string of the route drives the first call
      (PRD §4.1, D05, D07).

Phase 1 is done when Tophe has released a version, deployed it, and from an
Android phone: added a book with a series, an author and a tag, found it by an
accented and by a misspelt search, edited it, filtered the catalogue by type
and deleted the book. The result goes in `agent/PROGRESS.md`.

## Questions for the human

1. Cover images (PRD §4.1, open question 2). No task plans them: the draft
   assumes we store our own on the covers volume, which needs an upload
   operation, a served path and a volume `deploy/compose.yaml` does not
   declare yet. Store our own, link external URLs, or leave covers out of v1?
2. Genres (PRD §4.1 says "genres and free tags"). T030 plans free tags only.
   Are genres the same mechanism, or a closed list the admin maintains?
3. Series (T026). A series is created by typing its name on the item form, and
   nothing renames, merges or deletes one; D03 anticipates a rename re-saving
   the items. Is a series screen in v1, or later?
4. Author roles (T028). The enum proposed is `WRITER`, `ARTIST`, `COLOURIST`,
   `TRANSLATOR`, from PRD §3 and §4.1. Enough for v1? Is a BD's scenario
   writer a `WRITER`?
5. Deleting an item (T023) is unambiguous while no copy exists, so it is
   planned. PRD open question 5 must be answered before the copies phase, and
   open question 1 (digital copies) before the copy form is shaped.
6. Once the catalogue exists, should `/` be the catalogue rather than the
   greeting, with the reader's name moved into the header?

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
- Backend: a blank `Remote-User` header authenticates a reader whose username
  and display name are empty strings, and a blank `Remote-Name` gives an empty
  display name; both break `CurrentReader` (`minLength: 1`). T005 refuses a
  missing user and a missing or blank email; refuse a blank user the same way
  and treat a blank name as absent, so it falls back to the username, each
  with a test (found 2026-09-09 while writing and reviewing T005).
- Backend gate speed: on the sandbox host `./gradlew check` takes about 42 s
  warm (test 28 s, detekt 10 s). The first Spring context boots in 10 s and
  the second in 0.9 s, so the cost is cold-JVM class loading, not Spring.
  Measured 2026-09-09 on T005's 13 tests: `clean test` 7 s on a Mac, 20 s on
  Gordien, 32 s on bestheda. Levers, in order: a JDK 25 AOT cache or an AppCDS
  archive plus `-XX:TieredStopAtLevel=1` on the test JVM (`jvmArgs` of
  `tasks.test`; measure in a scratch worktree first, expected test task
  28 s to about 20 s); the implementer runs one test class during red and
  green and the full gate at commit; briefs keep every `@SpringBootTest`
  class on one context configuration.
- Loop: resume after the usage window resets. When a role's JSON ends with
  `is_error: true` and a result like "You've hit your session limit · resets
  12:20am (UTC)", `run_role` sees no report and the loop exits 6 with the
  task half done. Detect it, sleep until the reset time, rerun the role once
  with `claude -p --resume <session_id>` (the id is in the JSON) and the same
  flags, with a case in `agent/test-loop.sh` (seen 2026-09-08 on T001's
  reviewer).
- CI: path filtering. Skip `backend`, `frontend` and `images` on a pull
  request that changes nothing under `backend/`, `frontend/` or `api/`: a
  `changes` job (`git diff --name-only` against the base, no marketplace
  action) whose outputs gate the three jobs with a job-level `if:` on
  `pull_request` only, because a workflow-level `paths:` filter leaves a
  required check pending while a skipped job reports success. `images` stays
  unconditional on push to `main`, since `release.yml` re-tags `sha-<short>`
  of the tagged commit. `ci.yml` is edited by humans only (Tophe,
  2026-09-09).
- Frontend: reload on a new service worker. `registerSW.js` only registers;
  with `registerType: "autoUpdate"` the new worker takes over on the first
  load after a release but the page shown came from the old precache, so
  the footer shows the previous revision until a second load. Register
  through `virtual:pwa-register` and reload when the new worker takes
  control, or show a "new version" notice (seen by Tophe on 2026-09-10 with
  the first release).
