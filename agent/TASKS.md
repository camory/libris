# Libris — Task backlog

> Ordered. The loop takes the **first unchecked** task. Keep tasks small enough
> for one PR a human reads in ten minutes. Each task states its acceptance
> criteria; "tests pass" is implied everywhere (see `CLAUDE.md`).
>
> DRAFT — Tophe reviews and reorders before the first run.

## Phase 0 — Foundations

- [ ] T001 Backend skeleton. Generate a Spring Boot + Kotlin + Gradle (Kotlin DSL)
      project in `backend/` (latest stable via start.spring.io: web, validation,
      data-jpa, postgresql, flyway, actuator, security excluded for now). JVM
      toolchain 21, package `fr.amory.libris`. Datasource from `LIBRIS_DB_*` env
      vars per D08. Flyway baseline migration `V001__init.sql` creating the
      `unaccent` and `pg_trgm` extensions. `GET /api/v1/health` returns
      `{"status":"UP"}`. Tests: one integration test hitting the endpoint
      against the real database. `./gradlew check` green. Add `backend/README.md`
      with run/test commands.
- [ ] T002 Frontend skeleton. Create `frontend/` with Vue 3 + Vite + TypeScript
      (strict), Vue Router, Pinia, vue-i18n (locale `fr`), Tailwind CSS,
      Vitest, ESLint + Prettier. `vite-plugin-pwa` with a manifest (name
      "Libris", theme colour, placeholder icons) and a generated service worker.
      Dev server proxies `/api` to `http://localhost:8080`. Home page shows the
      backend health status. One component test. `npm test` and `npm run build`
      green. `frontend/README.md` with commands.
- [ ] T003 API contract bootstrap. Add `api/openapi.yaml` (OpenAPI 3.0)
      describing `/api/v1/health` with a named example. Backend: add
      Contracteer verifier-junit test that verifies the running app against the
      contract (D04). Frontend: generate `src/api/schema.d.ts` with
      `openapi-typescript` via an npm script and use it for the health call.
      Document the "contract first" workflow in `api/README.md`.

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

## Phase 5 — Production

- [ ] T050 Production images and compose. Multi-stage Dockerfiles for backend
      and frontend per D09; `deploy/compose.yaml` with Traefik labels for
      `libris.amory.fr`, named volumes, nightly `pg_dump` sidecar.
- [ ] T051 Deployment runbook `deploy/README.md`: DNS record, first deploy,
      upgrade, backup restore, admin bootstrap.

## Proposed (added by agent runs; a human promotes them into a phase)

_None yet._
