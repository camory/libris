# Libris — Architecture and decisions

> Read by every agent run. Decisions here are binding until changed by a human.
> Each decision has an ID so tasks and PRs can reference it.

## Overview

```
phone / browser
      │  https://libris.amory.fr
      ▼
  Traefik (Kimsufi, existing, Let's Encrypt)
      ├── /api/**  ───────►  backend   (Kotlin + Spring Boot, JVM 21)
      └── /**      ───────►  frontend  (static Vue PWA served by nginx)
                                 │
                             PostgreSQL 17 (volume: data, nightly pg_dump)
```

## Decisions

### D01 — Monorepo, two applications
`backend/` (Gradle, Kotlin DSL) and `frontend/` (npm, Vite) in one repository.
One PR can change both sides of a feature. Each app has its own build, tests
and Dockerfile. Root-level scripts only orchestrate.

### D02 — Backend: Kotlin + Spring Boot
Latest stable Spring Boot via Spring Initializr; Kotlin latest stable; JVM
toolchain **21**. Modules: `web` (controllers), `domain` (entities, services),
`persistence` (repositories, migrations). Use Spring Data JDBC or JPA? →
**Spring Data JPA** for the reading model simplicity, with explicit queries
for search. Migrations with **Flyway**, SQL files, never `ddl-auto`.

### D03 — Database: PostgreSQL only
PostgreSQL 17. Full-text search uses a generated `tsvector` column combining
title, subtitle, series, authors and tags with the `french` and `simple`
configurations, plus the `unaccent` and `pg_trgm` extensions for accent
folding and fuzzy matching. No Elasticsearch / Meilisearch: one fewer process
to run and back up. Revisit only if relevance proves insufficient.

### D04 — Contract-first API
`api/openapi.yaml` is the source of truth for the HTTP API. The backend is
verified against it in the test suite using **Contracteer**
(`tech.sabai.contracteer:contracteer-verifier-junit`, Tophe's own tool). The
frontend generates its TypeScript client types from the same document
(`openapi-typescript`). A task that changes the API changes the contract
first, then both sides.

### D05 — Frontend: Vue 3 + Vite + TypeScript
Composition API with `<script setup>`, Pinia for state, Vue Router, Vitest
for unit tests, `vite-plugin-pwa` (Workbox) for manifest and service worker.
Styling: Tailwind CSS with a small set of shadcn-vue components; mobile first.
UI strings in French via `vue-i18n` from day one (single locale `fr`).

### D06 — Authentication
Session cookie authentication with Spring Security, form login, BCrypt.
Roles `ADMIN` and `MEMBER`. Accounts created by the admin. CSRF enabled for
the browser session. No JWT, no OAuth provider in v1.

### D07 — Tests are the oracle
- Backend: unit tests (JUnit 5 + Kotest assertions), integration tests
  against a real PostgreSQL provided by the environment (see D08), contract
  verification via Contracteer.
- Frontend: Vitest component tests; Playwright end-to-end added once the
  first screens exist (T-series 4x).
- A task is done only when `./gradlew check` and `npm test` pass.

### D08 — No Docker-in-Docker
The agent sandbox has no Docker socket. Integration tests therefore connect to
a PostgreSQL given by environment variables
(`LIBRIS_DB_URL`, `LIBRIS_DB_USER`, `LIBRIS_DB_PASSWORD`), defaulting to
`jdbc:postgresql://localhost:5432/libris`. The same variables serve local
dev, the sandbox (`postgres` sidecar) and CI (service container).
**Testcontainers is not used.**

### D09 — Runtime packaging
Backend: multi-stage Dockerfile producing a JRE 21 image running the boot jar.
Frontend: multi-stage Dockerfile building static assets served by nginx with
SPA fallback and correct service-worker caching headers. Production compose
in `deploy/` with Traefik labels for `libris.amory.fr`; data on named volumes;
nightly `pg_dump` to a backup volume.

### D10 — Conventions
- Kotlin: official style, immutable by default, sealed types for states,
  no `!!`. Package root `fr.amory.libris`.
- TypeScript: strict mode, ESLint + Prettier defaults from the scaffold.
- Commits: Conventional Commits (`feat(backend): ...`, `fix(frontend): ...`,
  `chore(agent): ...`), imperative, ≤ 72 chars subject.
- Branches: `task/T###-short-slug`. `main` is protected; only PRs land there.
- API paths under `/api/v1/`. JSON in `camelCase`. Errors as RFC 9457
  problem details.

## Local development (human)

```
docker compose -f agent/compose.yaml up -d postgres   # a database on localhost:5432
cd backend && ./gradlew bootRun                      # http://localhost:8080
cd frontend && npm run dev                           # http://localhost:5173 (proxies /api)
```
