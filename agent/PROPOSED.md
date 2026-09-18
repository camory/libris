# Libris — Proposed

> Follow-ups found by agent runs and ideas from Tophe, one bullet each, with
> the date and where it was found. Nothing here is picked up by a run: a
> human promotes an item by writing a spec for it in `specs/` or by adding a
> task line to an open phase in `agent/TASKS.md`.

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
  the home page shows the previous revision until a second load. Register
  through `virtual:pwa-register` and reload when the new worker takes
  control, or show a "new version" notice (seen by Tophe on 2026-09-10 with
  the first release).
- Backend: narrow the ArchUnit port rule. "A port of the domain is
  implemented in the infrastructure only" matches every class assignable to
  any `domain` interface, so the variants of a sealed interface, or an enum
  implementing a domain interface, break it; T013 chose a sealed class for
  `SourceAnswer` because of it. Remedy decided with Tophe on 2026-09-13: add
  one clause, `.and().resideOutsideOfPackage("..domain..")`, so a class
  outside `domain` that implements a domain interface must reside in
  `infra`, and the domain may use its own interfaces; no naming convention,
  no marker. To apply in the first task that needs a domain implementer of
  a domain interface, one clause and nothing else (seen on T013,
  2026-09-12).
- Backend: Open Library in one request. `/api/books?bibkeys=ISBN:<isbn>&jscmd=data&format=json`
  answers title, subtitle, publishers, publish date, page count and the
  author names inline, with no redirect and no per-author request; nothing
  known is a 200 with an empty object, and the cover comes by cover id
  rather than by ISBN. Measured on 2026-09-12 against `9782723488525`: it
  lists one author (尾田栄一郎) where the edition document lists two (the
  second is Shueisha, a publisher filed as an author), so the two endpoints
  do not answer the same list. A spec change: `specs/fast-entry.md` names
  the edition endpoint, the recorded answers and the scenario stubs follow
  it. To weigh with Tophe when T014 is planned (found reviewing T013).
- Contract: the unknown-ISBN example is known. `9782000000006`, the
  `404_UNKNOWN_ISBN` example of `libris-api`, is a real Open Library record
  on 2026-09-12 ("Test", John le Carré, Michelin Editions des Voyages, a
  placeholder someone created). The scenarios stub the sources, so nothing
  fails, but a manual S4 against the real source finds a book.
  `9782000000013` and `9791000000008` are unknown at both endpoints; the
  latter is already the `503_SOURCES_DOWN` example. To change in the
  contract with Tophe on its next release (found reviewing T013).
- Backend: the web slice grows a mock per controller. Every `@WebSliceTest`
  class must name every use case of `infra.web` in its `@MockitoBean`, because
  `WebSliceConfiguration` scans the whole package; the list will be copied into
  each new test class as controllers arrive. Declaring the mocks on the
  `@WebSliceTest` annotation itself would keep one list (found on T020,
  2026-09-12).
- Backend: the source timeout is per HTTP request, not per lookup. The Open
  Library client bounds each of its requests (the edition, then one per
  author), so a source slow on every request may take several timeouts and
  still answer, while the spec's "does not answer in time" reads per lookup.
  Rare in practice (a source is down or hanging rather than uniformly slow);
  a per-lookup deadline is more code in every client. To decide with Tophe
  if it ever bites (found on 2026-09-13).
- Backend: the wait when every source hangs. The default timeout is 5 s per
  request and T014 asks the sources in turn, so two hanging sources mean
  10 s before the 503; asking in parallel would halve it at the price of an
  executor in `application`. Sequential is the boring choice; a shorter
  default is a product number for Tophe (found on 2026-09-13).
- Backend: the BnF never says colourist. Three Murena records read on
  2026-09-16: on 9782505010166 (tome 8, 2010) and 9782505083399 (tome 11,
  2020) the colourist is prose in the title statement only (`200 $g
  couleurs, Jérémy Petiqueux`), with no 70x entry and no `$4`; on
  9782505125990 (tome 13, 2025) he draws and colours and carries the one
  code `440`. The BnF gives one function per person, so `COLOURIST` has no
  code to map and stays a role the BnF never produces. The same records
  code their artists `070` on the 2010 and 2020 ones and `440` on the 2025
  one, so the card says *scénario* for an artist on a share of BD records;
  the only correction would parse the `$g` prose, which nothing does
  (found on 2026-09-13, settled 2026-09-16).
- Backend: the BnF language map holds one entry, `fre → fr`. The mapping
  from UNIMARC `101$a` to the two-letter code the contract wants grows one
  language at a time, with the recordings that exercise it; a whole ISO
  639-2 to 639-1 table would be code no test asks for. To revisit if the
  library turns out to hold much beyond French (found on 2026-09-13).
- Frontend: `FetchIsbnApi` has no `onUnauthenticated` callback. D06 gives the
  API client one and T012 wired it for `/me` only, so a reader whose Authelia
  session expires while on the lookup screen sees the promise reject instead
  of a navigation to `/session`. The mock cannot produce a 401 and the
  contract does not declare it, so the task that adds it is the one that
  decides how it is exercised — and it is also when the two clients' shared
  request shape is worth extracting (found on T016, 2026-09-14).
- Frontend: the lookup screen has a button, not a form, so the go key of the
  phone keyboard does not start the search. A `<form @submit.prevent>` buys
  it at the price of a behaviour no scenario describes and of jsdom's own
  form submission in the tests; the task that adds it is the one that decides
  what the spec says about the keyboard (found on T017, 2026-09-14).
- Frontend: a rejected `lookUp` (the network down, the API unreachable)
  leaves the lookup screen's button busy for ever and says nothing, since
  `search()` awaits it without a `finally`. What the screen says then is a
  scenario of `specs/fast-entry.md` to write with Tophe; the button coming
  back is a `finally` (raised by the reviewer of PR #71, 2026-09-14).
- Frontend: the silhouette of the card inherits the busy button's bug. A
  rejected `lookUp` leaves the skeleton standing where the card would be, for
  ever and saying nothing, exactly as it leaves the button busy (the bullet
  above); the `finally` that frees the button frees the answer's place too
  (found on T018, 2026-09-14).
- Frontend: the `language` section of the `fr` catalogue holds `fr` alone, so
  a lookup answering any other code shows the code itself in the *Langue* row
  (`en`). That is the decided fallback, not a bug, but the words are added one
  at a time with a case each, and the codes that will arrive are the ones the
  backend's map learns (found on T018, 2026-09-14).
- Frontend: a reader who refuses the camera sees the camera block disappear
  and nothing else; the screen never says why, and the words for a refused
  camera are in no document. A sentence under the field, or a state of the
  icon button, is a scenario of `specs/fast-entry.md` to write with Tophe
  (found on T019, 2026-09-14).
- Frontend: the camera keeps looking for ever past codes the ISBN rule
  refuses, which is what a viewfinder does, but a shelf that answers nothing
  usable for a while says nothing either. Whether a *rien trouvé* message
  arrives after a while, and when, is product behaviour for Tophe (found on
  T019, 2026-09-14).
- Backend: the same record comes back empty in `unimarcxchange`, the schema
  `BnfSource` parses: the record slot holds a diagnostic (`erreur de
  traitement`, details `-20`) and no fields, on every attempt, while
  `intermarcxchange` and `dublincore` return it whole. A fallback schema for
  a record that fails in UNIMARC is a decision for the T021 spec conversation
  (measured 2026-09-14, for T021's brief).
- Contract: `ONE_PIECE_1`, the `200_FOUND` example of `libris-api` `v0.3.0`,
  still carries an Open Library cover and `["BNF", "OPEN_LIBRARY"]`, while
  since T021 Libris answers the catalogue's cover and `["BNF"]` alone. The
  contract only checks the shape, so `ApiContractTest` passes as it stands,
  but the example now describes an answer the application cannot give. Give
  the example the catalogue's cover URL and the single source on the next
  release of `camory/libris-api`, with Tophe (found on T021, 2026-09-14).
- Backend: a book whose UNIMARC record is a diagnostic is now unknown. The
  BnF answers `9782253098058` (*Le comte de Monte-Cristo*) with an empty
  record in `unimarcxchange` and the whole record in `intermarcxchange`;
  with Open Library gone, nothing else answers, so the card says the ISBN is
  unknown for a book the catalogue holds. The fallback schema bullet above
  is what fixes it (found on T021, 2026-09-14).
- Frontend: Open Library still shows on the frontend side. `SourceEditions.ts`
  gives its fixtures a `covers.openlibrary.org` cover, `SourceEditionCard`'s
  cases name Open Library, and the scenario spec lists two sources; the
  backend answers one source and a `catalogue.bnf.fr` cover since T021. The
  words the card shows come from the API, so nothing is broken, but the
  fixtures no longer look like an answer (found on T021, 2026-09-14).
- Frontend: an expired session during a lookup leaves the search hanging.
  `FetchIsbnApi` has no 401 branch, unlike `FetchMeApi`, so the body parse
  throws, `search()` in `IsbnView` never releases `searching`, and the button
  stays disabled with its spinner until the app is relaunched (seen by Tophe
  on the Pixel, in production, 2026-09-15). Give the lookup client the same
  `onUnauthenticated` as the me client, release `searching` in a `finally`
  with the generic message, and add the expired session to the spec as a
  scenario with Tophe.
- Frontend: `SourceEditionCard` never forgets a cover that failed. The card
  remembers the failure in a ref and no `watch` resets it when `edition`
  changes, which is sound today because `IsbnView` unmounts the card between
  two answers; the day a screen shows the card for two editions in a row
  without unmounting it, that screen's task adds the reset and the case that
  proves it (decided on T022, 2026-09-15).
- Frontend: the domain `Isbn` derives no ISBN-10. The backend's does, for the
  BnF's CQL query; nothing on the frontend reads a ten, so no test of T023
  motivated the property. The day a screen shows the ten of a book, its task
  adds `isbn10` to `frontend/src/domain/Isbn.ts` with the case that wants it
  (decided on T023, 2026-09-15).
- Docs: `docs/ARCHITECTURE.md`'s D02 still lists `Isbn13` among what lives at
  the root of `domain`; the class is `Isbn` since T023. The run proposed the
  one-word amendment in its pull request body rather than editing the document
  (CLAUDE.md); Tophe settles it on the review (found on T023, 2026-09-15).
- Frontend: Prettier disagrees with `frontend/vitest.global-setup.ts`, the
  pinned URL sitting past the print width in its array. It is not in the gate
  (`npm test` runs `vue-tsc`, ESLint and Vitest), it is already so on `main`,
  and reformatting it would put a line of noise in a task that only moves the
  pin; the next task that touches the file runs `npm run format` on it (found
  on T025, 2026-09-15).
- Backend: `OpenLibrarySource` reads a 404 as `NothingKnown` whoever answered
  it, so a 404 on `/search.json` would say the ISBN is unknown instead of
  failing. Open Library's search answers 200 with no doc when it finds
  nothing, so no case of T024 could make it wrong; the day the two requests
  need telling apart, the adapter catches around each one (found on T024,
  2026-09-15).
- Backend: the BnF's `330 $a` is a summary and no source fills
  `SourceEdition.summary`. 9782371025219 carries some six hundred characters
  of publisher's blurb there, which the card has a place for; the day the
  summary is wanted, its task decides what to do with prose that long
  (found on T027, 2026-09-16).
- Frontend: the six icons of `src/ui/components/icons/` repeat the same
  eleven-line `<svg>` shell (the 24 grid, `stroke-width` 1.8, the round caps,
  `aria-hidden`), differing only by their paths and, for `IconBook`, by the
  drawn size. A shared shell the icons pass their paths to would keep the grid
  and the stroke in one place; six files are still cheaper to read than to
  abstract, so the day a seventh arrives is the day to weigh it (found on
  T030, 2026-09-16).
- Frontend: nothing tells the reader an update that does not finish. The
  banner's `updating` state ends only on the reload, so a worker that never
  activates leaves *Mise à jour…* on the screen for ever; `specs/update.md`
  describes no such state and the adapter waits without a deadline. The day it
  is seen in the wild, its task decides what the reader is told and how long
  the app waits (found on T028, 2026-09-18).

- Frontend: every case of `ServiceWorkerAppUpdate.spec.ts` builds an adapter
  that adds a `visibilitychange` listener to the shared jsdom `document` and
  never removes it, so one dispatched event wakes every adapter of the cases
  before, each with the registration it captured. The run's cases do not see
  it, each asserting on its own fresh spy, but a case counting timers cannot
  be written (17 stale adapters armed an hour each on the review fix-up), and
  `check()` still re-arms with a `null` registration for the same reason: no
  case can observe it. The day either matters, the adapter gets a way to stop
  listening (an `AbortController` on its listeners) that the spec's
  `afterEach` calls (found on T029, 2026-09-18).
