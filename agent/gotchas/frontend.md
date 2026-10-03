# Libris — Gotchas, frontend

Read whole by a run that changes `frontend/`, after `every-run.md`.

## Frontend build and tests
- Two TypeScript programs: `tsconfig.app.json` (`src/`, `vite/client` types)
  and `tsconfig.node.json` (`vite.config.ts`, `vitest.global-setup.ts`,
  `eslint.config.ts`, `node` types), checked by `vue-tsc --build`. A `node`
  type package or a `/// <reference types="node" />` in the app program puts
  Node's globals into every file under `src/` unnoticed by the boundaries
  rule.
- `eslint-plugin-boundaries`: elements are matched in array order, first
  match wins, so `src/ui/components`, `src/ui/views` and `src/fixture` must
  sit above `src/ui` and `src` in `eslint.config.ts`. `settings["import/
  resolver"] = { node: { extensions: [".ts", ".vue"] } }` is what makes the
  rule see extension-less local imports: do not drop it in a cleanup.
  `mode: "file"` is deprecated; a single file gets its permission through a
  `boundaries/files` category.
- The Vitest block lives in `vite.config.ts` with `defineConfig` from
  `vitest/config`. `fetch` works in the `jsdom` environment on Node 24 with
  no polyfill. `vi.stubGlobal("fetch", …)` needs `vi.unstubAllGlobals()` in
  an `afterEach`, or the contract case passes only by running first.
- `vitest.global-setup.ts` starts `contracteer mock` on port 9099 over the
  contract the frontend pins, a `raw.githubusercontent.com` URL of
  `camory/libris-api` (`v0.7.0` until T060); it starts in about four seconds
  and logs `Contracteer mock server started on port 9099` last, and the
  setup resolves on that line and kills the process in its teardown. The
  mock generates values, so an `infra/api` spec asserts shape and types,
  never a value, and cannot catch a swapped mapping between two strings.
- `contracteer mock` answers a request whose `Accept` does not list
  `application/problem+json` with a plain-text refusal, not with the problem
  the document declares: the client's `response.json()` then throws
  `SyntaxError: Unexpected token 'A', "Accept hea"...`. That header is what
  makes the three problem cases of `FetchIsbnApi.spec.ts` possible.
- The mock picks its response from the request's path parameter: a value the
  document gives as a named example of that parameter gets that example's
  response, so `9782723488525` answers 200 and `9782000000006` answers 404
  (`9782000000013` from `v0.9.0`).
  A value that matches no example gets a generated 200. A `POST` is matched
  on the path parameter and the body together, so one bookshelf id answers
  201 to the body `NewOnePiece1` and 400 to `NewOnePiece1WrongDigit`, and
  the 404 id answers 404 to `NewOnePiece1`. A scenario whose key sits on
  the request alone got its status from the mock with no body and no
  `Content-Type` until Contracteer 4.1.1, which generates the body from the
  schema. The error responses of `v0.7.0` still hold examples; from
  `v0.8.0` none does, and the mock answers the right status and
  `application/problem+json` with a random `type`, `title` and `status` in
  the body, so an adapter spec asserts the outcome of the HTTP status,
  never the body's `type`.
- A scenario file builds the application through `createLibrisApp` over the
  fakes of `src/fixture`; only `infra/api` specs and the smoke case *the
  application runs over the mock* use `inject("mockBaseUrl")`. A new port
  joins `LibrisPorts`, and `vue-tsc` then reds `bootstrap`, which builds the
  real client, `createLibrisApp.spec.ts` and the `open()` of every scenario
  file: five places, all in the same commit.
- A fixture may import `domain` and `application` only, so a shared
  app-over-fakes builder cannot live in `src/fixture`: each scenario file
  keeps its own `open()`, naming the ports its spec varies.
- A fake built with `Promise.reject(...)` in a Given is an unhandled
  rejection by the time the When awaits it: the test passes, and Vitest
  reports an *Unhandled Rejection* that fails the run (measured 2026-09-25).
  `FakeBookshelfApi` takes the `Error` itself and rejects when `add()` is
  called; do the same for any fake that has to fail.
- Over the fakes nothing calls `fetch`, so `vi.spyOn(globalThis, "fetch")`
  is always uncalled and proves nothing. *S3 Not an ISBN* proves the
  sources are not asked through the screen: its fake knows One Piece 1 and
  answers any ISBN, and the card must not show "Romance dawn".
- `expect.toSatisfy(predicate, message)` is an asymmetric matcher in
  Vitest 5: it is how one `toEqual` over a whole object asserts a nullable
  field (`value === null || typeof value === "string"`) against the values
  the mock generates.
- `npm run format` passes `--ignore-path ../.gitignore`, or Prettier
  rewrites `dist/` and `coverage/`; `prettier --check` cannot parse
  `nginx.conf`.
- `registerType: "prompt"` and `injectRegister: false` on the PWA plugin: no
  `registerSW.js` is emitted, the app registers `/sw.js` itself through
  `ServiceWorkerAppUpdate`, and Workbox writes the `SKIP_WAITING` message
  listener into `dist/sw.js` exactly because `skipWaiting` stays false. A new
  worker waits for the reader's order instead of taking over on the next load.
- A worker can already be `installing` when `register()`'s promise resolves:
  its `updatefound` fired before anything could listen. An adapter that only
  listens for `updatefound` misses that version and the scenario *S2 The
  reader updates* fails, because its harness finds the new version on the tick
  that mounts the app. Watch `registration.installing` when the promise
  resolves as well as on `updatefound`.
- jsdom 30 seals `window.location`: neither `reload` nor `location` itself
  can be redefined or spied, so a reload or a redirect is proven by handing
  the adapter a function from `bootstrap` and passing a fake in its spec;
  `navigator.serviceWorker` is absent and is stubbed with
  `Object.defineProperty`, like `navigator.mediaDevices`. The Update
  scenarios build the real adapter over a no-op reload; a real
  `location.reload()`, what `bootstrap` hands it, logs `Not implemented:
  navigation to another Document` on the console and nothing else.
- vue-router's first navigation is asynchronous: `app.use(router)` starts it
  and nothing of the route is rendered on the tick `mount()` returns, nor
  after a microtask flush. `FastEntryScenarios`' `open()` queries the host
  synchronously, so `createLibrisApp` sets `router.currentRoute` from
  `router.options.history.location` right after `app.use(router)`; the eager
  navigation still runs, so the history listeners and `isReady()` are wired
  as usual. Drop that line and every scenario that does not query through
  `findBy*` fails on an empty host holding the tab bar alone.
- `getByRole("textbox")` finds `<input type="text">` only: `type="number"` is
  a `spinbutton` and `type="search"` a `searchbox`. A numeric keyboard comes
  from `inputmode="numeric"`. An accessible name from `<label for>` resolves
  on a detached element tree, so a test needs no `document.body`.
- Vue's template compiler condenses whitespace: a whitespace-only text node
  between two elements that holds a newline is dropped, so `textContent` and
  `wrapper.text()` glue siblings with nothing between them
  (`CollectionShonen manga`, `tome 1Romance dawn`). Assert the order of the
  parts by their positions in the normalised text, or query with
  `@testing-library/dom`, whose default matcher reads an element's own text
  nodes only. Whitespace beside an interpolation survives as one space, which
  is what gives `SourceEditionCard`'s heading the boundary
  `FastEntryScenarios`' `/One piece\D{0,12}1\b/` asks for.
  Between block parts that must read apart, `CatalogueRow` writes an
  interpolated `{{ " " }}` on its own line; it survives the compiler and, in
  a flex container, renders nothing. A literal space between two `<span>`s
  does not last: Prettier moves each span to its own line and Vue then
  drops the space.
- The colour roles and the type steps of `docs/DESIGN.md` are Tailwind theme
  tokens in `src/ui/style.css`: custom properties on `:root`, overridden in a
  `prefers-color-scheme: dark` block, exposed through `@theme inline` as
  `--color-*` and `--text-*`. A template names `bg-surface` or `text-body`;
  a colour shade or a type size written in a class is a step that is missing
  from the file. The box of a control, `h-[50px]` or `border-[1.5px]`, is
  not a token and is written as is. Tailwind 4's spacing scale is dynamic, so
  an odd-but-regular length is a fraction of it rather than an arbitrary
  value: `py-1.25` is 5 and `size-5.5` is 22.
- Vitest reads its configuration from the working directory: run it from
  `frontend/`, never from the repository root. From the root it still finds
  the spec files but runs them under the default `node` environment, and
  every DOM global is missing (`ReferenceError: HTMLMediaElement is not
  defined`, `document is not defined`) on tests that are green one directory
  down.
- jsdom has no camera and no media element behind `<video>`:
  `navigator.mediaDevices` is undefined, so an adapter that reaches it must
  do so inside a `try`; a test installs it with `Object.defineProperty(…, {
  configurable: true })` and removes it with `Reflect.deleteProperty` in an
  `afterEach`, since `vi.stubGlobal` does not reach a property of
  `navigator`. `HTMLMediaElement.prototype.play` is jsdom's
  `notImplementedMethod` and prints an error unless it is replaced
  (`vi.spyOn(HTMLMediaElement.prototype, "play").mockResolvedValue()`).
  `srcObject` is implemented nowhere in jsdom, so assigning a plain object
  with `getTracks()` to it is an ordinary property assignment: no IDL
  conversion, no throw.
- jsdom loads no image: an `<img>` with a `src` fires neither `load` nor
  `error`, whatever the URL, so a test of what a broken cover shows dispatches
  `new Event("error")` on the element itself and awaits `nextTick()`. Vue Test
  Utils' `findComponent(SomeIcon)` is how a rendered icon is asserted: an
  `aria-hidden` SVG carries no text and no role, so no `getBy*` query reaches
  it. Every `SourceEditionCard` draws `IconBook` on its bookshelf rows or
  its absence row, so the cover's stand-in is proven by counting,
  `findAllComponents(IconBook)`, never by `exists()`.
- A spec importing a component that does not exist yet fails the whole file
  at load (`Failed to resolve import`, `Tests no tests`), not the one case:
  write the component first, drawing nothing the case asserts, and the red is
  the case's own assertion (`expected [] to have a length of 1`).
- `vue-i18n`'s `t` answers an unknown key with the key itself, so a catalogue
  entry a component builds and never wrote renders on the screen as
  `role.BOOK.WRITER` or `isbn.card.series.BOOK`. Nothing throws and no type
  catches it — `t` takes a template string — so the only proof of a key that
  exists is a case asserting the French word it holds.
- A catalogue entry of two forms, `"Dans {bookshelf} | Dans {bookshelf} ·
  {count} exemplaires"`, is picked by `<i18n-t :plural="n">`: vue-i18n's
  default rule answers the first form for 1 and the second for any other
  count, 0 included. The placeholders are filled by the named slots
  (`#bookshelf`, `#count`); a slot is the only part of the sentence a class
  can style, so a class on the count would cover the number alone, not the
  words around it. Where no part is styled, `t(key, { … }, count)`
  picks the form as a plain string (`CatalogueRow`'s bookshelves), which
  several can then join.
- A type step worn at another weight is the step's class plus a weight
  class, `text-body font-semibold`: Tailwind 4 emits `.text-body` with
  `font-weight: var(--tw-font-weight, 400)` and `font-semibold` sets that
  variable, so the weight wins whatever the order of the two classes, with
  no new token in `style.css`.
- `grep -rn <text> frontend` walks `frontend/node_modules`, so a search for a
  version or a package name answers pages of changelogs. `git grep -n <text>
  -- frontend` searches the tracked tree alone, which is what a criterion
  about this repository means.
- A word `SourceEditionCard` displays is asserted in three files, not one:
  the card's own spec, `IsbnView.spec.ts` (*asks for the ISBN-13 the rule
  computes and shows the card*) and `FastEntryScenarios.spec.ts`
  (`showsTheOnePieceCard`). Taking a word off the card reds all three; grep the
  word over `src/` before calling the change done.
- The backend denies every non-GET without `X-Requested-With`, and the
  contract does not state the header, so `contracteer mock` answers the same
  without it: a frontend client that sends a `POST`, `PUT` or `DELETE` sets
  `"X-Requested-With": "XMLHttpRequest"` itself, proven by a case over a
  stubbed `fetch` that reads the request's `init`, as
  `FetchBookshelfApi.spec.ts` *says the add comes from the application*.
- A case that stubs `fetch` and still builds its client on
  `inject("mockBaseUrl")` passes whether or not the stub installed, the mock
  answering the same example: give such a client a base URL that resolves
  nowhere, so the stub is the only thing that can answer.
- `/` is a prefix of every path, so `router-link-active` sits on a link to `/`
  on every screen. What follows the screen shown is the *exactly* active link:
  `RouterLink` writes `aria-current="page"` and `router-link-exact-active` on
  it alone, and only when the router resolves the link's `to` to a route:
  a link to a path no route declares is never current, whatever the
  location, so an active-tab case needs the route first. A test reads it
  with `getByRole("link", { current: "page" })`, a template styles it with
  `exact-active-class`.
- Two Tailwind utilities for the same property on the same element are settled
  by the order Tailwind emits them, not by the order in the attribute. Where a
  state must win — the active tab's colour and weight over the bar's — put the
  common value on the ancestor, to be inherited, and the state's value on the
  element: its own declaration beats an inherited one whatever the order.
- `App.vue` is the full-height shell: a `fixed inset-0` flex column, a
  `min-h-0 flex-1 overflow-y-auto` wrapper around `RouterView`, then the
  tab bar. It is fixed at the four edges of the window, not measured in
  `dvh`: Chrome on Android resolves a viewport unit stale right after
  `location.reload()` in the installed app, until the next resize, and laid
  the tab bar out below the window after the update's reload (seen on the
  Pixel 2026-09-19). A view that fills the screen writes `min-h-full` on its
  `<main>`, never `h-full`, or content longer than the viewport is clipped
  instead of scrolling.
- Mounting a component that holds a `RouterLink` needs the route settled first:
  `await router.push(path)` and `await router.isReady()` before `mount`, then
  `window.history.replaceState(null, "", "/")` in an `afterEach`, since
  `createWebHistory` writes to jsdom's real history and a leftover path makes a
  later spec mount the wrong screen.
- With `vi.useFakeTimers()` on, a helper that waits on a real timer never
  resolves: the `settled()` of `ServiceWorkerAppUpdate.spec.ts` leaves the
  case to fail on Vitest's five-second timeout.
  `await vi.advanceTimersByTimeAsync(0)` is what flushes a pending
  registration promise — it drains the microtask queue as well as the
  zero-delay timers — and `await vi.advanceTimersByTimeAsync(betweenChecks)`
  runs a timer whose callback chains another promise. `vi.useRealTimers()`
  belongs in the file's `afterEach`, or the next case starts on frozen time.
- `document.visibilityState` is stubbed like `navigator.serviceWorker`:
  `Object.defineProperty(document, "visibilityState", { configurable: true,
  get: () => "hidden" })` shadows jsdom's accessor, a handler reading it
  during a dispatched `visibilitychange` sees the stubbed value, and
  `Reflect.deleteProperty(document, "visibilityState")` in the `afterEach`
  puts jsdom's back.
- A `vi.fn()` attaches its own handler to the promise it returns, so a
  rejection a spy hands back is never an unhandled one: a case whose stub
  rejects stays green whether or not the code catches. Prove the catch by
  mutation — the run reports `Unhandled Errors` and exits non-zero when the
  rejection really escapes.
- A template unwraps a top-level ref only: a composable held in a
  `shallowRef`, so that the view can build a fresh one (`IsbnView.vue`'s
  `addBookToBookshelf`, rebuilt on each lookup), exposes its `state` to the
  template as a ref still, and the view reads it through a `computed`
  (`addState`). Its function is called on the unwrapped object,
  `@click="addBookToBookshelf.add(edition)"`, and `vue-tsc` narrows
  `edition` there from the enclosing `v-else-if`.
- `@typescript-eslint/no-unused-vars` has no `argsIgnorePattern`: an
  `_`-prefixed parameter is still an error, so a cycle whose signature is
  fixed before its body uses the argument leaves `eslint .`, and the gate,
  red until the cycle that reads it.
- `BarcodeDetector` is not in TypeScript's DOM library. The two interfaces
  the camera adapter needs are written in
  `src/infra/camera/CameraBarcodeScanner.ts` and are not `declare global`:
  an ambient declaration would tell every file the type exists on every
  browser. The global is read through a function on each call, never captured
  in a module constant, so a `vi.stubGlobal("BarcodeDetector", …)` installed
  after the module loads is seen, and `vi.unstubAllGlobals()` is enough to
  forget it.
- A mutation that keeps state at module scope of a composable leaks from one
  case to the next of the same spec file and reddens unrelated cases; run
  the guard's case alone (`npx vitest run <file> -t "<name>"`) to read its
  own red.
- jsdom has no `IntersectionObserver`: every spec that mounts
  `CatalogueView`, directly or through the router on `/catalogue`, stubs it
  with `vi.stubGlobal` or fails on `ReferenceError: IntersectionObserver is
  not defined`. The view builds it in `setup`, so the stub goes in before
  the mount, and the view spec's stub records the targets per observer
  (`unobserve` drops one, `disconnect` all). A `watch` with
  `flush: "post"` runs in the same flush as the render, before
  `findByText` resolves, which is what lets a scenario call back on the
  last row right after finding it.
