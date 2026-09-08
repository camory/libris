# libris-frontend

Vue 3 / Vite application, in French. Node 24 (`.node-version`).

## Install

```
npm ci
```

## Run

```
npm run dev
```

The dev server listens on <http://localhost:5173> and proxies `/api` to
<http://localhost:8080>.

## Build

```
npm run build
```

It writes the static site to `dist/`, with the PWA manifest and the service
worker precaching the app shell.

## Test

```
npm test
```

It runs the `vue-tsc` type check, ESLint with the architecture boundary rules,
and the Vitest suite with a V8 coverage report written to `coverage/lcov.info`.

## Format

```
npm run format
```

## Image

```
docker build . -t libris-frontend:sha-abc1234 --build-arg VERSION=sha-abc1234
```

The image serves `dist/` with nginx as the `nginx` user on port 8080: unknown
paths fall back to `/index.html`, `index.html` and the service worker are sent
`no-cache`, and the hashed assets `immutable`. The footer of every page then
shows the version the image was built with, `sha-abc1234`; a build without
`--build-arg` shows `dev`.
