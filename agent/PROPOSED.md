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
  the footer shows the previous revision until a second load. Register
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
- Backend: no BnF function code maps to `COLOURIST`. The UNIMARC `$4`
  codes the recordings carry are `070` (writer), and the role map adds
  `440` (artist) and `730` (translator); nothing in them names a colourist,
  and a code invented from nothing would be a guess. To settle with Tophe
  against a real record of an album whose colourist is credited (found on
  2026-09-13).
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
- Frontend: the tab bar of U03 is nowhere. `App.vue` carries the footer and
  a `RouterView` only, so the reader moves between *Accueil* and *Ajouter*
  through a link in the home view; the bar itself — the two tabs with
  `IconHome` and `IconPlus`, the active one read from the route, the bottom
  safe area — is chrome shared by every screen and belongs to a task of its
  own. That task is also when `HomeView` gets drawn to U02 and U03: it still
  wears `text-2xl font-bold` and `p-4`, which are not steps of the scale
  (found on T017, 2026-09-14).
- Frontend: the lookup screen has a button, not a form, so the go key of the
  phone keyboard does not start the search. A `<form @submit.prevent>` buys
  it at the price of a behaviour no scenario describes and of jsdom's own
  form submission in the tests; the task that adds it is the one that decides
  what the spec says about the keyboard (found on T017, 2026-09-14).
