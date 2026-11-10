# Libris — Gotchas, frontend

Read whole by a run that changes `frontend/`, after `every-run.md`.

## Frontend build and tests
- `npm test` runs past the Bash tool's 120 s default timeout: give the
  call a 600 s timeout.
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
  `camory/libris-api` (`v0.18.0` since T159); it starts in about four seconds
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
  response, so `9782723488525` answers 200 and `9782000000013` answers 404
  (`9782000000006` before `v0.9.0`, a generated 200 since).
  A value that matches no example gets a generated 200. A `POST` is matched
  on the path parameter and the body together, so one bookshelf id answers
  201 to the body `NewOnePiece1` and 400 to `NewOnePiece1WrongDigit`, and
  the 404 id answers 404 to `NewOnePiece1`. Since `v0.9.0` those bodies carry
  `coverSource: "inventaire.io"`: the same body with `coverSource: null`
  matches no example and gets a generated 201 on either id, and a body
  without the field is refused. A scenario whose key sits on
  the request alone got its status from the mock with no body and no
  `Content-Type` until Contracteer 4.1.1, which generates the body from the
  schema. The error responses of `v0.7.0` still hold examples; from
  `v0.8.0` none does, and the mock answers the right status and
  `application/problem+json` with a random `type`, `title` and `status` in
  the body, so an adapter spec asserts the outcome of the HTTP status,
  never the body's `type`. The generated `200` of `GET
  /bookshelves/{id}` held one or two members in 20 tries, never none
  (T082). The add of a member answers `400` for bookshelf
  `0b1e2d3c-4f5a-4b6c-8d7e-9f0a1b2c3d4e`, reader
  `00000000-0000-4000-8000-000000000000`, `VIEWER`; `404` for bookshelf
  `9e8d7c6b-5a4f-4e3d-8c2b-1a0f9e8d7c6b`, reader
  `7f8091a2-b3c4-4d5e-8f6a-7b8c9d0e1f2a`, `VIEWER`; `409` for bookshelf
  `0b1e2d3c-…`, reader `6e7f8091-a2b3-4c4d-9e5f-6a7b8c9d0e1f`, `OWNER`
  (`v0.11.0`). The choice of the default, `PUT /me/default-bookshelf`,
  answers `404` for the body `{ id: "9e8d7c6b-5a4f-4e3d-8c2b-1a0f9e8d7c6b" }`,
  `400` for `{ id: "not-a-uuid" }` and a `204` for any other uuid.
  `DELETE /copies/{id}` answers `404` for
  `8b9c0d1e-2f3a-4b4c-9d5e-6f7a8b9c0d1e`, `400` for `not-a-uuid` and a
  `204` for any other uuid (T134). The generated `200` of `GET /series` held one or two series in 30 tries,
  never none (T096). `GET /series/{id}` answers `404` for
  `5b6c7d8e-9f0a-4b1c-8d2e-3f4a5b6c7d8e`, `400` for `not-a-uuid` and
  `a%2Fb`, and a generated `200` of one or two books in 30 tries, never none
  (T103). `GET /books/{id}` answers `404` for
  `3e4f5a6b-7c8d-4e9f-a0b1-c2d3e4f5a6b7`, `400` for `not-a-uuid` and
  `a%2Fb`, and a generated `200` of one or two copies in 30 tries, never
  none (T113). `GET /series-names` answers a generated `200` of one or two
  names of one character in 30 tries, never none (T121); `GET
  /author-names` the same in 30 tries (T144). `PUT /books/{id}` answers a
  `200` with a generated book to a valid body on any uuid, `404` to
  `2a3b4c5d-6e7f-4a8b-9c0d-1e2f3a4b5c6d` with exactly the `EditOnePiece2`
  body (a `200` to another body), and `400` to `not-a-uuid` or a mistyped
  field (T159). `FetchIsbnApi`, `FetchBookshelfApi`, `FetchBookshelvesApi`,
  `FetchSeriesApi`'s find, `FetchCatalogueApi`'s find, `FetchCopiesApi`
  and `FetchEditionsApi` hand a non-2xx answer to `infra/api/problemOf`,
  which maps the statuses the contract names (400, 404, 409 for the
  creation and the add of a member, 400 and 404 for the choice of the
  default, the finds of a series and of an ouvrage, the removal of a copy
  and the save of an edition, and 503 for the lookup) to the `/problems/…`
  slugs only when the `Content-Type` is `application/problem+json`, and
  reads the body's `type` otherwise: a proxy's `text/plain` 404 or 503 then
  rejects on `response.json()`, as it did before the mapping, and is never
  taken for an answer of Libris. A client whose contract names 400 and 404
  alone passes `problemOf`'s `validationOrNotFound`; one naming another
  status (409, 503) writes its own map.
- A scenario file builds the application through `createLibrisApp` over the
  fakes of `src/fixture`; only `infra/api` specs and the smoke case *the
  application runs over the mock* use `inject("mockBaseUrl")`. A new port
  joins `LibrisPorts`, and `vue-tsc` then reds `bootstrap`, which builds the
  real client, `createLibrisApp.spec.ts` and the `open()` of every scenario
  file, all in the same commit.
- A fixture may import `domain` and `application` only, so a shared
  app-over-fakes builder cannot live in `src/fixture`: each scenario file
  keeps its own `open()`, naming the ports its spec varies.
- The scan beep's port is `Beeper` (`application/Beeper.ts`), `beeper` of
  `LibrisPorts` since T138, provided under `beeperKey`: `beep()` sounds,
  `ready()` readies the browser's audio on a tap; `FakeBeeper` counts
  `beeps` and `readies`. `WebAudioBeeper` (`infra/audio`) takes a
  `() => AudioContext` from `bootstrap`, builds the context on its first
  `ready()` or `beep()` (jsdom has no `AudioContext`: one built earlier
  reds the smoke case on `ReferenceError`); `ready()` calls `resume()`
  whatever the state, and `beep()` calls it first only on a `suspended`
  context, the one a cold load on `/isbn` builds before any tap (T138's
  rework); its spec runs over a stand-in context written in it
  (`browserAudio()`), which records the oscillators and counts `resumes`.
  `IsbnView` calls `ready()` first in `openCamera`, holds
  `useBeepAtEachRead(beeper, beepSetting)`, which exposes `beep()`, its verb, and
  `tap(at)` (`tap($event.timeStamp)` on the `h1`'s click, the instant in
  milliseconds, a tap 500 ms or more after the one before starting a new
  row of which it is the first, the third of a row turning the beep on,
  sounding it, or off, in silence, and the tap after it starting a new
  row however soon it comes, so six quick taps turn it on, then off;
  `beep()` on a non-`null` camera read before `search`, sounding only
  while the trick left it on), so the composable reads no clock, its
  spec writes each tap's instant. The state is the port `BeepSetting`
  (`application/BeepSetting.ts`), `beepSetting` of `LibrisPorts` since
  T141, provided under `beepSettingKey`: `isOn()` read once at the
  composable's setup, `keep(on)` at each trick. `WebStorageBeepSetting`
  (`infra/storage`) takes a `Storage`, `localStorage` from `bootstrap`,
  and keeps the one entry `beep` set to `on`, off being no entry;
  `FakeBeepSetting(on = false)` exposes `on`. `IsbnView.spec.ts`'
  `mountView` takes the beeper sixth and provides a `FakeBeepSetting`
  itself. `ScanBeepScenarios` gives the fake beeper and the real
  `WebStorageBeepSetting(localStorage)` through the ports, the other
  scenario files a `FakeBeepSetting`, does the trick through three clicks
  on the `h1`, and mounts S6's second app over the same `localStorage`,
  which it clears after each test; S2 fakes
  `Date`, `performance`, `setTimeout` and `clearTimeout` for its taps only,
  and jsdom stamps `event.timeStamp` with `Date.now()`.
- The removal of a copy is `CopiesApi.remove(copyId)` (`removed`, or a
  problem), `copiesApi` of `LibrisPorts` since T134, provided under
  `copiesApiKey`; `FetchCopiesApi` sends the `DELETE`, the id unencoded, and
  hands any answer but `204` to `problemOf`.
  `useRemoveCopy(copiesApi, afterRemoval)` exposes `state` (`ready`,
  `removing`, `removed`, `unavailable`) and `remove(copyId)`, which never
  rejects as long as its `afterRemoval` does not, and stays `removing`
  until `afterRemoval` is done. The
  `/problems/not-found` answer is taken as `removed`, `afterRemoval` run;
  a rejection, `/problems/validation` and any other type are
  `unavailable`, `afterRemoval` not run. `EditionPageView` passes
  `removing` and `removal-failed` (`unavailable`) to the card; its
  `@remove` goes through `removeCopy`, which keeps the copy's id, and its
  `afterRemoval` calls `refresh` while the ouvrage holds a copy other than
  that one (on any bookshelf, whatever the role), and otherwise leaves
  the page without asking: `router.back()` when
  `openedFromAnotherScreen()` (`router.options.history.state.back` set)
  answers true, awaited through a one-shot
  `router.afterEach` so the spinner turns until the page is left, and
  `router.replace("/catalogue")` when it is `null`.
  `IsbnView` (T137) passes `removing` and `removal-failed` to both its
  `SourceEditionCard`s, the lookup's and the added one, and holds its
  `useRemoveCopy` in a `shallowRef` rebuilt by `copyRemoval()` on each
  lookup, whose `afterRemoval` acts only while that removal is still the
  current one, so a late answer leaves the next card alone. It never asks
  Libris again: `dropRemovedCopy` keeps the shown copies (the lookup's and
  the added one) less the removed id and, unless an add is being sent,
  rebuilds `addBookToBookshelf`, which ends the *Added* state and brings
  the dock back; after an add it
  also makes `edition` the sent one, `covers` the sent candidate, `held`
  true and `editing` false, so the card is the house's: no dots, and the
  pencil once none of the reader's copies is left on it.
  `useFindCatalogueEdition`'s `refresh(id)` asks again with no `loading`
  step, the card staying mounted, while `find(id)` shows the skeleton
  first; its spec's `mountEditionPageView` takes the copies' port third, a
  `FakeCopiesApi` rejecting by default, and the series' and the authors'
  ports fifth and sixth, `new FakeSeriesApi([])` and `new
  FakeAuthorsApi([])` by default; `pageOf(book, seriesApi?, authorsApi?)`
  mounts the found book and `tapPencil(screen)` opens its form. `Copy.bookshelf` is a `ReaderBookshelf` (`src/domain/Copy.ts`),
  a `Bookshelf` with a required `role`, as in the contract: a test builds
  a copy's bookshelf by spreading a fixture with a role, `{ ...salon,
  role: "OWNER" }`, and the card draws the trash on an `OWNER` row only.
  Every client maps a copy with `copyOf` (`infra/api/copyOf.ts`, beside
  its `CopyResponse`), and its spec checks one with
  `copiesWithIdAndReaderBookshelf` from `fixture/EditionShapes.ts`.
  `FakeCatalogueApi`'s find takes a single answer, given at every find, or
  a list answered in order, whose last answer then repeats: the page
  before a removal, then after it.
- The series' names are `SeriesApi.names()`, and `FakeSeriesApi` takes them
  third, `string[] | Error`, `[]` by default; a scenario that varies them
  passes `undefined` second to keep the find's default.
  `EditionFormScenarios`' `open()` gives `world.names` as its `seriesApi`.
  `FetchSeriesApi.names()` rejects any answer but `200`. `IsbnView` holds
  them through `useListSeriesNames(seriesApi)` (`names`, `null` until
  answered and when Libris does not answer, never `[]` then, so the form
  tells "the house holds none" from "unknown"; `list()` never rejects), calls
  `list()` unawaited on each accepted lookup and passes `names` to the form
  as `seriesNames`. `IsbnView.spec.ts`' `mountView` and `open` take a
  `seriesApi` fourth, `new FakeSeriesApi([])` by default, and a
  `copiesApi` fifth, a `FakeCopiesApi` rejecting by default; its
  `removeCopy(screen, row?)` taps the trash of the row whose text is `row`
  (the first by default), then *Retirer*. While a row asks, its *Dans …*
  text is gone, so "no *Dans Chambre*" holds before the removal answers:
  count the trashes too.
- The authors' names are `AuthorsApi.names()` (`application/AuthorsApi.ts`,
  `authorsApiKey`), and `FakeAuthorsApi` takes them, `string[] | Error`.
  `FetchAuthorsApi.names()` rejects any answer but `200`;
  `authorsApi` is a port of `LibrisPorts` since T144, provided by
  `createLibrisApp`. `AuthorsEditScenarios`' `open()` gives `world.authors`,
  the other scenario files a `FakeAuthorsApi([])`. `IsbnView` holds them
  through `useListAuthorNames(authorsApi)` (`names`, `null` until answered
  and when Libris does not answer; `list()` never rejects), calls `list()`
  unawaited on each accepted lookup beside the series' names and passes
  `names` to the form as `authorNames`. `IsbnView.spec.ts`' `mountView`
  takes an `authorsApi` seventh, `new FakeAuthorsApi([])` by default, and
  `open` sixth.
- The save of a held edition is `EditionsApi.save(id, edition)`
  (`application/EditionsApi.ts`, `editionsApiKey`), `edition` an
  `EditionDetails` (a `SourceEdition` without `isbn13`), answering `saved`
  with the `Book` or a problem; `FakeEditionsApi` takes its answer,
  `SaveAnswer | Error`, and records `asked`. `editionsApi` is a port of
  `LibrisPorts` since T159, provided by `createLibrisApp`; `FetchEditionsApi`
  sends the `PUT /books/{id}`, the id through `encodeURIComponent`, and
  hands any answer but `200` to `problemOf`. `HeldEditionEditScenarios`'
  `open()` gives `world.save`, the other scenario files a
  `FakeEditionsApi(new Error("no save in this scenario"))`; no view reads
  the port yet.
- `EditionForm` takes `isbn13`, `edition` (a `SourceEdition` or a `Book`,
  absent for an unknown ISBN), `copies`, `covers`, `held` (handed to
  `CoverChoice`, `IsbnView` passing its own), `action` (`"add"` from
  `IsbnView`, `"save"` from `EditionPageView`, handed to `DockedAdd`),
  `adding`, `failed` and
  `seriesNames` and `authorNames`, required,
  `shownSource`, optional, as the card's, and the named slot `corner` as
  the card's, emits `coverSource` as the card does, and `add` with the edition its
  fields read as, `editionOf(isbn13, draft, locale)`
  (`src/domain/EditionDraft.ts`), whose `draft.authors` are the boxes
  (`Author[]`), sent as one `SourceAuthor` per name and role, in order,
  each name trimmed, a box whose trimmed name is empty sending nothing
  whatever its roles. Its root is a plain block holding the
  card-shaped `<form>` then `DockedAdd`. While `adding` its textboxes,
  radios, author names, trashes and roles' lines and *Ajouter un auteur*
  are `disabled`. It fills its text once, at setup: `kind`
  from `edition.kind`, a `values` record for the nine `input`s drawn by one
  `v-for` over `fields` (`isbn.form.<field>`), numbers as their digits,
  `null` as `""`, the language through `languageWord`, and `summary`, a
  `textarea` of its own after them; the camera keeps it, edits included,
  and a new lookup remounts it through the skeleton. *Auteurs* is drawn
  after *Tome* inside that `v-for`, also with no author, then one
  `AuthorBox` per box of `authors`, a `ref` list built once at setup from
  `authorsInOrder(answer's authors, locale)`, each `{ key, name, roles }`
  keyed by a counter (`keyed`), never by its name, bound `v-model:name`
  and `v-model:roles`, given the chosen `kind`, not the answer's; then the text button
  *Ajouter un auteur*, which pushes `{ name: "", roles: ["WRITER"] }` and,
  after `nextTick`, calls `focusName()` on it, reached through a function
  `:ref` into `authorBoxes`, a `Map` by the box's `key`, never by its
  position, which a box removed from the middle shifts (a focus case
  awaits `flushPromises()` after the tap). On a box's `remove` the form
  filters that key out of `authors` and focuses *Ajouter un auteur*,
  reached through a function `:ref` into `addAuthorButton`.
  `AuthorBox` (props `kind`, `authorNames`, required, and `disabled`,
  optional, on the name, the trash and the roles' line, models `name` and
  `roles`, emitting `remove` with no payload, exposing
  `focusName()` and `focusRoles()`, which focuses the roles' line and
  `scrollIntoView({ block: "center" })`s the bordered box, through
  `defineExpose`; prop `refusal`, optional, an `AuthorRefusal`: while set
  the box takes `border-danger` and neither `accent` class, and a line
  under the box, 4 under it plus the root's `gap-1`, draws `CircleAlert`
  and `isbn.form.authors.refusal.<code>` (*Rôle manquant*, *Auteur en
  double*)) draws the name's line, the
  textbox *Nom de l'auteur* then the trash, an `IconButton` drawing
  `Trash2` at 22, named `isbn.form.authors.remove` (*Retirer {name}*) or
  `removeUnnamed` (*Retirer l'auteur*) while the name is `""`; then a
  hairline, and the roles line, a button
  with `aria-expanded`, `aria-label` *Rôles* then its words, and
  `ChevronDown`; the words are `isbn.form.authors.{label,name,roles,add}`.
  Its root is a block holding the box then, 4 under it while the line is
  open, the group *Rôles* (`aria-label`) of four `aria-pressed` rows from
  `roleChoicesOf(roles)` (`SourceEdition.ts`, the card's order), a row's
  tap writing `rolesToggled(roles, role)` to the model; a `click` on
  `document` whose `composedPath()` holds neither its line nor its list
  closes it, the listener added on mount and removed on unmount. Under
  the box, 4 under it (the root's `gap-1`), before the roles' group,
  `SuggestionList` draws `suggestionsFor(name, authorNames ?? [], locale)`
  while the name has the focus and no row was tapped since the last
  keystroke or focus; a tap writes the name to the model. In the name's
  line, between the textbox and the trash, 8 before the trash,
  `NameStatusWord` draws `nameStatus(name, authorNames, locale)` as
  `isbn.form.authors.nameStatus.<status>` (*Existant*, *Nouveau*), none
  while `authorNames` is `null`. The name's
  `focus` calls `scrollIntoView({ block: "start" })` on the bordered box
  (name and roles' line, `scroll-mt-28`). Its spec's `box()` and
  `screen()` take `authorNames` fifth and fourth, `null` by default, then
  `refusal`, optional, and
  the file's `afterEach` deletes a `scrollIntoView` a case defined.
  Its spec's `valuesOf` leaves out the name boxes, `suggestions` the roles
  lines (`aria-expanded`), the trashes (`trashes()`, buttons named from
  *Retirer*) and *Ajouter un auteur*; `disabledOf` reads the fields, the
  kinds, the dots, the names, the trashes, the roles' lines and *Ajouter
  un auteur*, 19 over `onePiece1`. The volume's
  label is `isbn.form.volume.<kind>` (*Album* for a BD), built with the
  chosen kind, as `withoutSeries`' words are. Each label points at its
  box through `for` and an id built on `useId()`; the kinds are three
  `sr-only` radios inside their labels. The form's *ISBN* is a `<p>`, so
  `IsbnView.spec.ts`'s `field()` still finds the one `ISBN` textbox. A
  case that types in a field the mutation makes a `number` input finds
  it with `getByLabelText`: a role query reds on the `spinbutton` before
  the value is read. On the add's tap it asks `refusalsOf(draft, locale)`
  (beside `editionOf`, a `Refusals` record from a text field to
  `missing`, `invalidNumber`, `unknownLanguage` or `withoutSeries`, a
  volume number with a blank series, `invalidNumber` winning over it)
  and `authorRefusalsOf(draft.authors, locale)` (one `"twice"`,
  `"missingRole"` or `null` per box, in order, a box whose trimmed name is
  empty never refused, `"twice"` for one whose trimmed name equals under
  `nameOrder(locale)` that of a box before it, whatever the roles, and
  winning over `"missingRole"`) and
  emits `add` only when both answer none; otherwise it keeps those
  answers until the next tap, the authors' in `authorRefusals`, a `Map` by
  the box's `key` given to each box as `refusal` (an author box takes no
  `aria-invalid`), marks each refused field's box `aria-invalid="true"`
  (outlined through `aria-invalid:border-danger`), draws
  `isbn.form.refusal.<code>` under it (`withoutSeries.<kind>`, in the
  kind chosen now, not at the tap), and `focusFirstRefused` focuses then
  `scrollIntoView({ block: "center" })`s the first refused in the form's
  order (title … volume, the authors, collection … pages), reaching a
  field through a function `:ref` into `boxes`, a `"twice"` author through
  its box's `focusName()` (whose own `block: "start"` scroll is the only
  one), any other through `focusRoles()`. Under
  the field of `offered` (*Série*, `seriesNames ?? []`, and *Langue*,
  `languageWords(locale)`) that has the focus (`suggesting`, set on
  `focus` and cleared on `blur`), while no row has been tapped since the
  last keystroke or focus (`chosen`), `SuggestionList` (prop
  `suggestions`, emitting `choose` with the name, no spec of its own,
  `AuthorBox` its other caller, given `class="-mt-1"` here) draws
  `suggestionsFor(values[field], offered[field], locale)`
  (`src/domain/Suggestion.ts`, with `nameOf`, the three parts joined) as
  `type="button"` rows of three spans, `before`, `matched` in
  `font-semibold`, `after`; a row's `mousedown` is prevented so the box
  keeps the focus, and *Série*'s `focus` alone calls
  `scrollIntoView({ block: "start" })` on it. *Série*'s box, in a
  `relative` wrapper, draws at its right `nameStatus(values.series,
  seriesNames, locale)` (`src/domain/NameStatus.ts`, `existing` or `new`,
  `null` for a blank text or unknown names) through `NameStatusWord`
  (props `status` and `word`, the caller translating the word and placing
  it through a falling-through `class`; `Check` at 22 and `text-accent`
  for `existing`, `text-muted` alone for `new`, no spec of its own,
  `AuthorBox` its other caller) as `isbn.form.seriesNameStatus.<status>`,
  and takes `pr-32` while it does. Its spec's `form()` takes `{ adding, failed, corner, shownSource,
  attachTo, seriesNames, authorNames, held, action }` fourth, `false` and
  none by default (`seriesNames` and `authorNames` `null`, so no word,
  `action` `"add"`), and gives the edition's own `isbn13`
  when there is one; the focus case passes `attachTo: document.body` and
  unmounts at its end, and the file's `afterEach` deletes a
  `scrollIntoView` a case defined.
- On a detached tree a `fireEvent.click` on a radio checks nothing and fires
  no `change`, so `v-model` keeps the kind: a component spec chooses a kind
  with `fireEvent.change(radio, { target: { checked: true } })`. The
  scenarios mount on an attached host, where the click works.
- jsdom has no `Element.prototype.scrollIntoView`: `EditionFormScenarios`
  defines it as a no-op for its tests and deletes it after each; a component
  spec that reaches a call to it needs the same.
- A port that gains a method reds `vue-tsc` on every object literal typed
  as that port, specs included: `CatalogueView.spec.ts` builds a
  `CatalogueApi` by hand, so `find` reached it when the port gained it
  (T113). `npx vue-tsc --build` from `frontend/` lists them in seconds,
  before the gate.
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
  `nginx.conf`. `npx prettier --write src` also rewrites `src/domain/Copy.ts`,
  `src/scenario/CoversScenarios.spec.ts` and three calls of
  `src/ui/components/SourceEditionCard.spec.ts`, which `main` holds
  unformatted: format the files the task touches by name, and in that spec
  compare `npx prettier <file> | diff <file> -` instead of writing it.
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
- A `click` dispatched on an element of a detached tree bubbles up to the
  tree's own root and never reaches `document`: a listener on `document`
  sees a click on `document.body`, but not one on the component's own
  elements. A case about where such a click lands inside the component
  mounts with `attachTo: document.body` and unmounts at its end
  (`AuthorBox`'s *closes its list on a tap on its name*, *keeps its list
  open on a role's tap*).
- A browser runs a microtask checkpoint after each listener of a click the
  reader makes, so Vue re-renders between a row's `@click` and a listener
  higher up: an element the render drops (a `v-if` icon, the `svg` that
  was `event.target`) is detached when `document` hears the click, and
  `contains(event.target)` answers false. `fireEvent` dispatches
  synchronously, with no render in between, and hides it. A `document`
  listener reads `event.composedPath()`, fixed at dispatch; a case
  re-renders mid-dispatch from a `document.body` listener calling
  `wrapper.vm.$.update()` (`AuthorBox`'s *keeps its list open on a tap on
  a chosen role's check*, T147's rework). After the dispatch,
  `composedPath()` answers `[]`, so deferring the listener does not model
  the browser.
- `aria-labelledby` does not resolve on a detached tree: a `getByRole(…, {
  name })` over it throws `root.getElementById is not a function`, and every
  component spec and scenario mounts detached. A list is named with
  `aria-label` holding the catalogue word its visible label shows
  (`HomeView`'s *Mes bibliothèques*).
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
  nodes only. Between block parts that must read apart, `CatalogueRow` and
  `SourceEditionCard` (its overline `<p>` and its heading, since T114) write
  an interpolated `{{ " " }}` on its own line; it survives the compiler and,
  in a flex container, renders nothing. Dropping the card's reds four cases
  of `KindScenarios` and `FastEntryScenarios`, whose `/One piece\D{0,12}1\b/`
  reads *tome 1Romance* otherwise. A literal space between two `<span>`s
  does not last: Prettier moves each span to its own line and Vue then
  drops the space.
- Testing Library's default normaliser trims and collapses `\s+`, the
  non-breaking space included, so a query typed with a plain space (the
  scenarios' *…de Bibliothèque de Léa ?*) finds a text holding U+00A0 and
  proves nothing about it: a case asserting the non-breaking space passes
  `{ normalizer: (text) => text }` (`SourceEditionCard.spec.ts`, the
  question).
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
- `SourceEditionCard` and `EditionForm` draw two parts in common, each a
  component of `ui/components` with no spec of its own, and the card a
  third, `AuthorLines`, the form's authors being `AuthorBox`es since T144:
  `CoverChoice`
  (props `covers`, `title`, `held`, and `disabled`, optional, which
  disables its dots, and `shownSource`, optional, the source of the
  candidate it shows first, read once at setup; emitting `coverSource`) keeps one `img`
  per candidate of the lookup's `covers`, the shown one alone accessible,
  and the stand-in `BookOpen` when none is left, the dots' row and the
  source's name drawn only when `held` is false; `AuthorLines` (props
  `authors`, `kind`) and `BookshelvesHolding` (prop `copies`), which draws
  one row per bookshelf, the trash *Retirer un exemplaire* on an `OWNER`'s
  alone and on none with the optional prop `withoutTrash`, which
  `EditionForm` passes, *Dans* and the name (`isbn.card.bookshelf`) and, past one copy,
  the count on a second line of its own (`isbn.card.count`, *2
  exemplaires*), and holds `asking`, the id of the bookshelf whose trash
  was tapped, whose row it draws as the question (`isbn.card.removal`),
  the cross *Annuler*, which puts `asking` back to `null`, and the check
  *Retirer*, which emits `remove` with one copy id of that row's
  bookshelf; with the optional prop `removing` the check becomes a
  disabled `BusySpinner` button *Retrait en cours…* and the cross and
  every trash are disabled; new `copies` close the question. With the
  optional prop `removalFailed` the asking row draws `CircleAlert` and
  `isbn.card.removal.failed` under its question, inside its `bg` block,
  its buttons enabled; the row keeps its own `failed`, set from the
  prop's changes and cleared by the cross and every trash, so a question
  opened after a failure shows no message while the prop stays true. `SourceEditionCard` passes `removing`, `removalFailed` and `remove` through;
  `EditionForm` passes none of them, and `IsbnView` listens on both its cards;
  the form draws the same rows. A row's text
  reads its name and its count as two elements: `getByText("Dans
  Bibliothèque de Léa")` and `getByText("2 exemplaires")`, never one
  sentence. The card's
  props are `edition` (a `SourceEdition` or a `Book`, whose `isbn13` may be
  `null`), `copies`, `covers` and `held`, required, `pageTitle`,
  optional, which makes its heading the `h1` instead of the `h2`, and
  `shownSource` and `disabled`, optional, passed to `CoverChoice`; the
  heading is named after the title alone, the overline outside it. The
  named slot `corner`, card and form alike, is drawn at the right of the
  top part's text column, inside a `flex min-w-0 flex-1 gap-2` wrapper;
  `IsbnView` fills it with the pencil *Modifier* on the card of an ISBN
  the house lacks or holds with none of the reader's copies shown
  (`!held || shownCopies.length === 0`) and the cross *Annuler les
  modifications* on the form that pencil opens (`editing`), both disabled
  while `adding`, as the
  card's dots; `EditionPageView` fills it with the pencil on the card of
  a book with an ISBN, whatever the reader's role, and with the cross on
  the form that pencil opens (`editing`), drawing then the title as
  answered in an `sr-only` `h1` above the form; each an `IconButton` (prop `label`, the icon in its
  default slot, `class`, `disabled` and `@click` falling through to its
  `button`), as the camera's toggle. Its
  callers are `IsbnView`, for every found answer, and
  `EditionPageView`, which passes the stored cover as the one candidate
  `{ source: "Libris", url }`, `held`, `page-title`. Its spec's `card()`,
  `show()` and `screen()` default to no candidate and `held` false, so a
  case counting the rows' `BookOpen`s passes a candidate, or the stand-in
  adds one; `card()` and `screen()` take `pageTitle` fifth and
  `{ corner, shownSource, disabled }` sixth.
  `getAllByRole("img", { hidden: true })` reaches every candidate's image,
  in the answer's order.
- jsdom clears its computed-style cache only when the tree is attached to
  the document, and every component spec and scenario mounts detached: an
  element whose `hidden` attribute or `v-show` style changes keeps the
  `display` read first, so Testing Library keeps it hidden or visible.
  Toggle what a query must see with `aria-hidden`, which is read as an
  attribute, and hide it on screen with a class, which draws nothing in the
  specs (`CoverChoice`'s images).
- A component's `emit` called during `setup`, as a `watch` with
  `immediate: true` does, reaches the parent's `@event` listener and
  Vue Test Utils' `wrapper.emitted()`: `CoverChoice` gives out its first
  `coverSource` that way, with no `onMounted`, and `SourceEditionCard`
  and `EditionForm` pass it on.
- jsdom loads no image: an `<img>` with a `src` fires neither `load` nor
  `error`, whatever the URL, so a test of what a broken cover shows dispatches
  `new Event("error")` on the element itself and awaits `nextTick()`. Vue Test
  Utils' `findComponent(SomeIcon)` is how a rendered icon is asserted: an
  `aria-hidden` SVG carries no text and no role, so no `getBy*` query reaches
  it. Every `SourceEditionCard` and `EditionForm` draws `BookOpen` on its
  bookshelf rows or its absence row (`BookshelvesHolding`), so the cover's
  stand-in is proven by counting, `findAllComponents(BookOpen)`, never by
  `exists()`.
- A spec importing a component that does not exist yet fails the whole file
  at load (`Failed to resolve import`, `Tests no tests`), not the one case:
  write the component first, drawing nothing the case asserts, and the red is
  the case's own assertion (`expected [] to have a length of 1`).
- `vue-i18n`'s `t` answers an unknown key with the key itself, so a catalogue
  entry a component builds and never wrote renders on the screen as
  `role.BOOK.WRITER` or `isbn.card.series.BOOK`. Nothing throws and no type
  catches it — `t` takes a template string — so the only proof of a key that
  exists is a case asserting the French word it holds.
- A catalogue key built from a field that may be `null`, as
  `` t(`series.count.${series.kind}`, { count }, count) ``, reads the entry
  named `null` (`series.count.null` in `i18n.ts`), and the third argument
  picks its form. Neither `vue-tsc` nor ESLint's recommended set refuses
  `null` in a template literal, so only a case asserting the word of the
  `null` entry proves it is there (`SeriesRow`'s *counts in tomes when the
  editions differ in kind*).
- A catalogue entry of two forms, `"{bookshelf} | {bookshelf} · {count}
  exemplaires"` (`catalogue.copies`), is picked by `<i18n-t :plural="n">`
  or `t(key, { … }, n)`: vue-i18n's default rule answers the first form for
  1 and the second for any other count, 0 included. An entry of three forms, `"{first} | {first}, {last} |
  {first} à {last}"` (`seriesPage.run`), follows vue-i18n 11's other rule:
  the first form for 0, the second for 1, the third for any other count, so
  `t("seriesPage.run", { first, last }, last - first)` reads a run as one
  tome, two or its ends. In `<i18n-t>` the placeholders are filled by
  named slots (`AuthorLines`, `MemberRow`); a slot is the only part of the
  sentence a class can style, so a class on a number would cover the number
  alone, not the words around it: words styled apart are entries of their
  own (`isbn.card.bookshelf` and `isbn.card.count`). Where no part is
  styled, `t(key, { … }, count)` picks the form as a plain string
  (`CatalogueRow`'s bookshelves), which several can then join.
- A type step worn at another weight is the step's class plus a weight
  class, `text-body font-semibold`: Tailwind 4 emits `.text-body` with
  `font-weight: var(--tw-font-weight, 400)` and `font-semibold` sets that
  variable, so the weight wins whatever the order of the two classes, with
  no new token in `style.css`.
- `grep -rn <text> frontend` walks `frontend/node_modules`, so a search for a
  version or a package name answers pages of changelogs. `git grep -n <text>
  -- frontend` searches the tracked tree alone, which is what a criterion
  about this repository means.
- A word `SourceEditionCard` displays is asserted in several files, not
  one: the card's own spec, `IsbnView.spec.ts` (*shows the card of an ISBN
  the house holds*, over the `held` answer) and `FastEntryScenarios.spec.ts`
  (`showsTheOnePieceCard`); a word of `CoverChoice` or
  `BookshelvesHolding` is asserted in `EditionForm.spec.ts` too. Taking a word off the card reds them all; grep the word over `src/`
  before calling the change done. An ISBN the house lacks (`id: null`)
  shows the card first, its pencil *Modifier* opening the form, whose
  fields hold their text as values: a `getByText` for the title finds it
  on the card and no longer once the pencil is pressed. The form is
  remounted on each pencil, and the view keeps `coverSource` across the
  switch, handing it to each as `shownSource`; a lookup forgets both.
  `IsbnView` draws the answer, the added card, the form or the card, as a
  `v-if` chain on `answerShown` (`null` while `scanning`) inside
  `<KeepAlive :max="1">`: the camera leaves no component there, so the one
  shown is deactivated and comes back as left (edits, tapped cover,
  unfolded summary), while any switch between branches mounts the new one
  and prunes the old, built afresh on the pencil, the cross and the add; a
  lookup unmounts the whole block through the skeleton. A `:max` above 1,
  or an `include`/`exclude` that keeps the card out of the cache, lets the
  cross's form come back on the next pencil with its edits (S13 reds) and
  builds the card afresh when the camera closes; one that keeps the form
  out builds the form afresh then (S14 reds). The block wrapping it stays
  mounted while the camera is open and drops its `mt-5` then, so nothing
  of it draws under the camera.
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
  `exact-active-class`. *Accueil* (since T082) and *Catalogue* (since T096)
  are the exceptions: `/` is a component-less parent of `""` (`HomeView`)
  and `bibliotheques/:id`, `/catalogue` one of `""` (`CatalogueView`) and
  `series` (`SeriesView`), and each tab is a `RouterLink` `custom` slot
  writing `aria-current` and `text-accent` from `isActive`, so every screen
  nested under its parent marks it, while `/isbn` marks neither. A screen
  below the home page or the catalogue goes under that parent's
  `children`, or its tab stops being current there.
- A plain `RouterLink`, no `custom`, to a component-less parent's `""` child
  is active but not exactly active on a sibling child: at
  `/catalogue/series` the link to `/catalogue` carries `router-link-active`
  and no `aria-current`. A switch between sibling views styles its current
  half with `exact-active-class`, never `active-class` nor a `custom` slot
  over `isActive`, which mark *Ouvrages* on the series too
  (`CatalogueSwitch`).
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
- vue-router decodes a route parameter (`/bibliotheques/a%2Fb` gives `id`
  `"a/b"`), so a client that puts an id read from the address into a path
  writes `encodeURIComponent(id)`, or `/`, `?` and `#` reach a path the
  contract does not declare, where the mock answers an empty `404` and
  `problemOf` rejects. No encoding sends `.` or `..` as a segment: `fetch`
  normalises them, `%2E` and `%2e%2e` included; the browser does the same
  to the address before the router reads it, so neither reaches a view.
- Mounting a component that holds a `RouterLink` needs the route settled first:
  `await router.push(path)` and `await router.isReady()` before `mount`, then
  `window.history.replaceState(null, "", "/")` in an `afterEach`, since
  `createWebHistory` writes to jsdom's real history and a leftover path makes a
  later spec mount the wrong screen.
- A navigation in jsdom (T136): `router.back()` moves `window.location`
  a task after the call, not by `flushPromises`, so a case waits with
  `waitFor` from `@testing-library/dom`; `router.replace` and `push` move
  it by their awaited promise. A fresh `createLibrisRouter()`'s first
  `push` replaces its start entry (`state.back` `null`), so a case
  "opened from another screen" pushes that screen, then the page.
  `window.history.length` read before and after is the proof that no
  entry was added (it grows by one on a `push`). `EditionPageView.spec.ts`'
  `mountEditionPageView` takes the router fourth, `routerOn(...paths)`
  builds one already there. Vitest hides the `console.log` of a passing
  case: a `throw` shows a value while debugging.
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
  (`addState`). Its function is called from the script through `.value`
  (`send`'s `addBookToBookshelf.value.add(edition, sentCoverSource.value)`),
  the template's `@add="send(edition)"` narrowed by `vue-tsc` to a
  defined `edition` by the dock's own `v-if="answerShown === 'card' &&
  edition"`.
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
- Testing Library's accessible name trims each inline child of a button:
  a suggestion row *One * + *pie* + *ce* is named *Onepiece*, though its
  `textContent` reads *One piece*. A case that queries a row by name types
  a text whose parts carry no space at their edges (`"one pi"`), or reads
  the rows' `textContent`, as `EditionForm.spec.ts`' `suggestions()` and
  the scenarios do.
- `toLocaleLowerCase` reads a capital sigma by its context: at the end of
  a word in a string it gives `ς`, alone it gives `σ`, so *ΟΔΟΣ* folded
  whole differs from *ΟΔΟΣ* folded letter by letter. Lower-casing alone
  also keeps a written `ς` apart from `σ`. `suggestionsFor` folds the text
  and the names alike, one character at a time (`foldsOf`), each through
  `toLocaleUpperCase` then `toLocaleLowerCase`, which joins `ς` and `σ`; a
  new rule that ignores case folds both sides that way.
- jsdom has no `IntersectionObserver`: every spec that mounts
  `CatalogueView`, directly or through the router on `/catalogue`, stubs it
  with `vi.stubGlobal` or fails on `ReferenceError: IntersectionObserver is
  not defined`. The view builds it in `setup`, so the stub goes in before
  the mount, and the view spec's stub records the targets per observer
  (`unobserve` drops one, `disconnect` all). A `watch` with
  `flush: "post"` runs in the same flush as the render, before
  `findByText` resolves, which is what lets a scenario call back on the
  last row right after finding it.
- The screens sort names with `nameOrder(locale)` (`src/domain/nameOrder.ts`,
  `Intl.Collator` at `sensitivity: "base"`), the component passing
  `useI18n().locale.value` (`authorsInOrder`, `bookshelvesHolding`). Spaces
  count, so *À lire* sorts before *Albums*, and *Øre* equals *Ore*, unlike
  the backend's `BookshelfName` folding. The sandbox's default locale is
  `en-US`: a collator built with no locale is not the screen's.
- A language is shown as `languageWord(code, locale)`
  (`src/domain/Language.ts`, `Intl.DisplayNames` of `type: "language"`),
  the component passing `useI18n().locale.value`: `ja` reads *japonais*,
  and a code the browser has no word for (`qq`) reads as itself.
  `languageCode(word, locale)` reads a word back: the first code of the
  module's private `CODES`, the 183 codes of ISO 639-1 written out in their
  order, whose word equals it under `nameOrder(locale)`, `null` for none;
  `refusalsOf` reads the form's *Langue* through it, `null` refused as
  `unknownLanguage`. `languageWords(locale)` answers the words of `CODES`,
  each once: the browser names `ak` and `tw` both *akan*.
  The list is never built from two-letter pairs: the browser has words for
  withdrawn codes (`iw`, `mo`, `in`). The catalogue holds no `language`
  block.
- The lookup's add is docked: `DockedAdd` (props `action`, required,
  `"add"` reading `isbn.add` and `"save"` `isbn.save` *Enregistrer*,
  `adding`, `failed`, emitting `add`) draws the button, *Ajout en cours…*
  while `adding`, and
  the message under it when `failed`. It is drawn in two places: inside
  `EditionForm`'s root block, after the `<form>`, and in `IsbnView` after
  the card of every found answer, the two sharing the view's `flex flex-col`
  block, so that each dock sticks over the block it closes; the view's dock
  sits after the `KeepAlive`, which caches one component and no fragment,
  and is not drawn while the camera is open. Its root is `sticky
  bottom-0 -mx-5` with `[container-type:scroll-state]`; its inner
  `div` draws the hairline under the arbitrary variant
  `[@container_scroll-state(stuck:_bottom)]:border-t`, which Tailwind
  emits as `@container scroll-state(stuck:bottom)` (read in `dist/assets`
  after `npm run build`). A container query styles the container's
  descendants, never the container itself. jsdom lays nothing out: the dock
  is checked by hand. Once added, `IsbnView` draws `SourceEditionCard` of
  the edition it sent, with the one candidate whose source it sent, both
  kept by `send` on the tap (`sent`, `sentCoverSource`); the form's dots
  are disabled while the add runs.
- Icons come from `@lucide/vue` (`1.56.0`), imported by name from the
  package root (`import { Check } from "@lucide/vue"`); it has no sub-path
  exports. An icon given no `aria-label`, `title` or `aria-*` prop renders
  `aria-hidden="true"`, so a test reaches the button that holds it by the
  button's `aria-label`, and the icon itself through `findComponent`. The
  stroke comes from `app.provide(LUCIDE_CONTEXT, { strokeWidth: 1.8 })` in
  `createLibrisApp`: a component spec mounted alone gets no context and
  draws Lucide's default stroke, 2. A `:stroke-width` on the icon beats the
  context (`NewBookshelfRow`'s check, 3). An icon draws at 24 unless given
  `:size` (`:size="22"`), which writes the `svg`'s own `width` and
  `height`, and carries no `shrink-0` of its own: one beside a sentence
  in a flex row says `class="shrink-0"`.
- `findComponent(SomeIcon)` over a tree that draws none answers an empty
  wrapper whose `.element` throws `Cannot call element on an empty
  VueWrapper`: that is the red of a case asserting an icon before the icon is
  drawn, not a broken spec (`IsbnView`'s *draws a cross while the camera is
  open*).
- A home row is a stretched link: `BookshelfRow`'s `RouterLink` wraps the
  name and role, and its `after:absolute after:inset-0` covers the whole
  `relative` row, so a tap anywhere opens the bookshelf.
  A control added to the row (the star) sits outside the link and needs
  `relative` to be above that cover, or the tap opens the bookshelf. A
  message under the row (the default's error) sits after the `relative`
  row, in the component's outer `div`, out of the cover; its `-mt-1.5`
  pulls it back under the cover, so it is `relative` too. jsdom
  neither lays out nor hit-tests, so no spec sees the cover: it is checked
  on the running app. A series list row holds no control, so `SeriesRow`'s
  root is its `RouterLink` itself, and so is `CatalogueRow`'s since T114:
  every spec mounting either, or a view that draws one (`CatalogueView`,
  `SeriesPageView`, `SeriesView`), mounts a router; `within(wrapper.element)` then misses
  the link, since it searches descendants only, and their specs query from
  the root's parent.
- A `defineModel` prop the parent binds one way (`:name`, no
  `@update:name`) keeps a local copy: the child's input keeps the typed
  text when the parent redraws, a kind change in `EditionForm` included,
  while an input bound `:value` alone is reset to the prop on that redraw.
  A case "the typed text survives a redraw" is green on the child's
  `v-model` alone; the parent holding the value shows only through what it
  emits.
- An `aria-label` of `"Rôles "`, trailing space, is named *Rôles* by
  Testing Library, which trims the name, even with a function matcher
  (`name: (name) => name === "Rôles"`): a case about the exact label reads
  `getAttribute("aria-label")`.
- A string `ref` (or `useTemplateRef`) on an element inside a `v-for`
  collects an array, even when a `v-if` draws the element once: the
  `EditionForm`'s fields loop holds *Auteurs*, so `focus()` on such a ref
  throws `is not a function` inside the event handler, which Vue logs as
  *Unhandled error during execution of component event handler*. Reach the
  element through a function `:ref` instead. A plain `let` it writes to is
  declared `let x = null as T | null`: written `let x: T | null = null`,
  `vue-tsc` narrows it to `null` in the template and refuses the write.
- A spec's subclass of a fixture fake (`LibrisNotAnswering`,
  `LibrisAnsweringInTurn`) cannot declare a private member of the same name
  as one of the base's: `FakeBookshelvesApi` holds a private `answers`, and
  a constructor parameter `private readonly answers` fails `vue-tsc` with
  `TS2415 … Types have separate declarations of a private property`.
- jsdom 30 has no `scrollIntoView` (`HTMLElement.prototype.scrollIntoView`
  is `undefined`): a component that calls it throws under every test that
  reaches the call, unit and scenario alike. The call goes through optional
  chaining, `element?.scrollIntoView?.(…)`, or a test that asserts it
  defines it on the element first.
- jsdom moves the focus only to an element in the document: on the detached
  tree every spec mounts, `focus()` leaves `document.activeElement` on the
  body. A case about the focus mounts with `attachTo: document.body` and
  calls `wrapper.unmount()` at its end (`NewBookshelfRow`'s *takes the focus
  when it opens*). A focus case that fails before its `unmount()` leaves
  its tree attached, and later focus cases of the file red on it while
  they pass alone: read the first red of the file.
- Tailwind 4 emits the `aria-*` variants after `focus` and
  `focus-within` (read in `dist/assets` after `npm run build`), so
  `aria-invalid:border-danger` beats `focus:border-accent` on a refused
  field, while a plain colour class (`border-danger`) loses to `focus:`:
  `IsbnView`'s ISBN field adds `focus:border-accent` only while not
  refused. Every field takes `outline-none` and `focus:border-accent`, an
  `AuthorBox` `focus-within:border-accent` while not refused.
- A block that may draw nothing sits with the block after it in a
  `flex flex-col gap-5` parent (`SeriesPageView`'s card and list): a
  component whose root is `v-if`'d away renders a comment, which takes no
  gap, where an `mt-5` on the next block would stay. The sandbox has no
  browser (no Chromium, no Playwright), so a run cannot look at the running
  app: a layout check goes to the PR as left to Tophe.
- A block drawn across the content's 20 of side padding, the series page's
  band, keeps the padding on `<main>` and gives itself `-mx-5` and
  `w-[calc(100%+2.5rem)]`; an `img` also needs `max-w-none`, since
  Tailwind's preflight caps it at `max-width: 100%`. The preflight already
  makes an `img` `display: block` (`node_modules/tailwindcss/preflight.css`),
  so `block` on it repeats it. The image's name is `series.cover`, which
  `SeriesRow` reads too.
- jsdom lays nothing out and answers 0 to `scrollHeight` and
  `clientHeight`, so `SourceEditionCard`'s summary never overflows and
  never offers *Lire la suite* in a spec unless the case stubs both on
  `HTMLElement.prototype` with `Object.defineProperty(…, { configurable:
  true, get: () => n })` before it mounts, and the file's `afterEach`
  removes them with `Reflect.deleteProperty`, which gives jsdom's back (they
  live on `Element.prototype`), as `SourceEditionCard.spec.ts` does. The
  card measures in `onMounted`, so the button is drawn on the next render:
  the case awaits `nextTick()` after the mount before it looks.
