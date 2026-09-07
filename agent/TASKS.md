# Libris — Task backlog

> Ordered. The loop takes the **first unchecked** task. Keep tasks small enough
> for one PR a human reads in ten minutes. Each task states its acceptance
> criteria; "tests pass" is implied everywhere (see `CLAUDE.md`).
>
> Human steps are not tasks. A task that needs one states it as
> *Precondition (human)* on its line; Tophe does it between runs, and the
> planner reports `blocked` while it is missing.
>
> Phase 0 reviewed by Tophe on 2026-09-08. Phases 1–4 are still the first
> draft and predate the architecture review of 2026-09-07; the planner's
> backlog mode refreshes them before they are taken.

## Phase 0 — Foundations, ending with `/api/v1/me` deployed

- [ ] T001 Backend skeleton. Spring Boot + Kotlin, JDK 25, Gradle Kotlin DSL
      with `gradle/libs.versions.toml`; starters web, data-jdbc, flyway,
      actuator, PostgreSQL driver; no Spring Security yet. Datasource from
      `LIBRIS_DB_*` (D08). `V001__extensions.sql` creates `unaccent` and
      `pg_trgm` (D03). Packages `domain`, `application`, `infra.web`,
      `infra.persistence` exist (D02). `./gradlew check` runs all of D07:
      warnings as errors, detekt + formatting with the D10 rules, JUnit 5 +
      Kotest assertions, the six ArchUnit rules, Kover XML. Tests: Flyway
      migrated the given database and both extensions exist;
      `GET /actuator/health` is UP (D09). `backend/README.md`: run, test.
- [ ] T002 Frontend skeleton. Vue 3 `<script setup>`, Vite, TypeScript strict,
      Vue Router, Pinia, vue-i18n (`fr` only), Tailwind, Vitest + Vue Test
      Utils, Prettier; `.node-version` 24, `save-exact` (D05, D10). Layers
      `domain/ application/ infra/ ui/` with eslint-plugin-boundaries enforcing
      the five D05 rules. `vite-plugin-pwa`: manifest (name Libris, theme
      colour, placeholder icons) and precached app shell only; API caching
      stays in T040. Dev server proxies `/api` to `localhost:8080`. Home view:
      static French title from i18n. `npm test` = `vue-tsc` + ESLint + Vitest
      with V8 coverage (D07); one component test; `npm run build` green.
      `frontend/README.md`: commands.
- [ ] T003 Backend image. Precondition (human): the `images` job in
      `.github/workflows/ci.yml` (D09). Multi-stage `backend/Dockerfile`:
      Gradle build stage taking a `VERSION` build argument, JRE 25 runtime
      running the boot jar as a non-root user, configuration by environment
      only, `HEALTHCHECK` on `/actuator/health`, OCI labels (D09).
      `GET /actuator/info` reports the build version (Spring Boot build
      info), with a test. The sandbox has no Docker (D08): the PR's `images`
      job is the oracle, and the PR body says so.
- [ ] T004 Frontend image. Multi-stage `frontend/Dockerfile`: Node build
      taking `VERSION`, nginx serving `dist/` with SPA fallback; `index.html`
      and the service worker `no-cache`, hashed assets `immutable`; OCI labels
      (D09). The version shows in a footer, with a component test. Verified
      as T003.
- [ ] T005 Member profile, backend. Precondition (human): `GET /api/v1/me` in
      `api/openapi.yaml` (D04). Spring Security pre-authenticated header
      filter on `Remote-User/Groups/Name/Email`; `ADMIN` when in
      `libris-admin`; profile created on first visit (PRD 4.10);
      `V002__member.sql` with uuid v7 ids and `created_at`/`updated_at`
      auditing (D11); non-GET refused without `X-Requested-With` (D06); `dev`
      profile trusts the headers, `contract-test` profile authenticates a
      fixed member; Contracteer verifier-junit with the truncate + seed setup
      (D04). Tests: filter, first visit, repository IT, contract.
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
by name, the version in the footer, the PWA installed on iOS and Android, and
what happens when the installed app is reopened after the Authelia session
expired (the D06 risk). The result goes in `agent/PROGRESS.md`; if the
redirect fails in the installed app, the D06 fallback becomes a task.

## Phase 1 — Catalogue

- [ ] T010 Item model and migration. Tables `item`, `series`, `person`,
      `item_person` (role enum: WRITER, ARTIST, COLORIST, TRANSLATOR, OTHER),
      `tag`, `item_tag` per PRD 4.1 and the glossary. Kotlin entities and
      repositories. Migration `V002__catalogue.sql`. Repository tests.
- [ ] T011 Items API. Contract for `GET/POST /api/v1/items`,
      `GET/PUT/DELETE /api/v1/items/{id}` with validation (title required,
      ISBN-13 checksum, volume ≥ 1) and RFC 9457 errors. Named examples in the
      contract; Contracteer verification green; service + controller tests.
- [ ] T012 Items UI. List page with type filter and sort, detail page,
      create/edit form (mobile first, French labels), delete with confirm.
      Pinia store using the generated client types. Component tests for the
      form validation.
- [ ] T013 Full-text search. Generated `tsvector` column + GIN index +
      trigram index per D03 (migration `V003__search.sql`). `GET /api/v1/items?q=`
      ranks by `ts_rank` with accent-insensitive prefix matching and a trigram
      fallback for short queries. Contract updated. Tests prove `asterix` finds
      *Astérix* and `one pice` finds *One Piece*. Frontend search box with
      debounce on the list page.

## Phase 2 — Family, ownership, reading

- [ ] T020 Accounts and login. Spring Security session login per D06, `member`
      table, roles ADMIN/MEMBER, admin-only `POST /api/v1/members`. Seed the
      first admin from env vars `LIBRIS_ADMIN_USER` / `LIBRIS_ADMIN_PASSWORD`
      on first start. All `/api/v1/**` except health require login. Frontend
      login page, auth store, route guard, logout.
- [ ] T021 Copies, location, loans. `copy` table (owner, location, condition,
      acquired_on, format), `loan` table. API and UI on the item detail page:
      add copy, mark lent / returned.
- [ ] T022 Reading state. `reading_state` per member × item with status,
      rating 1–5, notes, dates. API, item detail section, "Mes lectures" page.
- [ ] T023 Wishlist. `wish` per member × item; list page; "convert to copy"
      action.

## Phase 3 — Fast entry and series

- [ ] T030 ISBN lookup service. `GET /api/v1/lookup/isbn/{isbn}` querying Google
      Books, Open Library and BnF SRU in parallel with timeouts, merging into a
      prefill DTO. Unit tests with recorded responses; no live calls in tests.
- [ ] T031 Barcode scanning. Camera scan in the PWA (`BarcodeDetector` with a
      `@zxing/browser` fallback) → lookup → prefilled create form.
- [ ] T032 Series completeness. Series page: owned volumes, gaps, optional total
      volumes; series list with completion badges.

## Phase 4 — PWA polish

- [ ] T040 Offline read-only catalogue. Cache the item list and details with
      Workbox runtime caching; offline banner; edits blocked offline with a
      French message. Real icons and splash screens.
- [ ] T041 Cover images. Upload and lookup-fetched covers stored on a volume,
      thumbnails, served under `/api/v1/covers/{id}`.
- [ ] T042 CSV export and import with preview and error report.

## Proposed (added by agent runs; a human promotes them into a phase)

_None yet._
