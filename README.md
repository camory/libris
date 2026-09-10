# Libris

A progressive web app for the Amory household to manage its books, mangas and
BD (bandes dessinées): what we own, where it is, who is reading it, what is
missing from a series, and what we would like next.

Served at `https://libris.amory.fr` from the family server.

## Stack

- **Frontend**: Vue 3 + Vite + TypeScript, installable PWA (`vite-plugin-pwa`)
- **Backend**: Kotlin + Spring Boot, Gradle (Kotlin DSL)
- **Database**: PostgreSQL, full-text search built in (`tsvector`, `unaccent`, `pg_trgm`)
- **Deployment**: Docker Compose behind the existing Traefik on the Kimsufi box

See [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) for the decisions and
[`docs/PRD.md`](docs/PRD.md) for what the product must do.

## How it is being built

Libris is built by an **agentic loop**: a script that repeatedly runs Claude
Code inside a Docker sandbox, one task per run, each run ending in a pull
request that a human reviews. The loop, its prompt, its task list and its
guardrails live in [`agent/`](agent/). The concepts are explained in
[`docs/LOOP.md`](docs/LOOP.md).

```
agent/loop.sh 3     # run up to three tasks, one PR each, waiting for review between them
```

## Repository layout

```
CLAUDE.md          directives every agent run reads first
docs/              PRD, architecture decisions, loop explainer
agent/             loop driver, sandbox image, task backlog, progress log
backend/           Spring Boot API            (created by task T001)
frontend/          Vue PWA                    (created by task T002)
api/               OpenAPI contract           (created by task T011)
deploy/            production compose         (created by task T008)
```
