# Libris — Gotchas

What a run must know before it starts, learned on this tree and still true.
Every role reads this file whole. An item is a fact that costs a cycle when
unknown: a name, a command, a tool's behaviour, a trap. A rule belongs in
`docs/ARCHITECTURE.md` or `docs/DESIGN.md`, a follow-up in
`agent/PROPOSED.md`, the story of a task in `agent/PROGRESS.md`. An item is
rewritten or removed the day it stops being true; the diary keeps the date it
was found.

## The sandbox and the tools
- The proof hook counts any command containing `gradlew … check` or
  `npm test` as a gate run, `--dry-run`, pipes and heredoc text included:
  run the gate plainly, last in its command, and write files with the Write
  tool.
- A server started inside a tool call dies with the call (`bootRun`, `npm
  run dev`, `contracteer mock`). Start it as a background task, wait with
  `curl --retry 30 --retry-connrefused`, stop it by task id. A foreground
  `sleep` is refused.
- `pkill -f` or `pgrep -af` with a pattern that appears in the tool call's
  own command line kills or matches the call itself (exit 144). Match on
  something else, or stop the process from what started it.
- `git checkout <file>` reverts to the last commit, not to the working tree:
  commit the cycle before a mutation check.
- `gh pr edit` fails with a GraphQL error about classic Projects; use
  `gh api -X PATCH repos/camory/libris/pulls/N`. `gh` GraphQL calls are
  rate-limited: poll `gh pr checks` every 30 s or more, and merge through
  REST (`gh api -X PUT repos/camory/libris/pulls/N/merge -f
  merge_method=squash`) when `gh pr merge` is throttled.
- The sandbox PostgreSQL survives between runs (tmpfs: gone when the
  container is recreated). Editing an applied migration breaks every context
  start with a Flyway checksum mismatch until the database is recreated;
  D11 forbids editing a merged one anyway.
- The loop takes the agent PostgreSQL down at the end of a run; a host gate
  then fails with connection refused until
  `docker compose --env-file agent/.env -f agent/compose.yaml up -d postgres`.
  `backend/.env` points the host gate at that database, and `FreshSchema`
  wipes it: what was entered by hand through `bootRun` is gone after a gate.
- Tophe's host box has no `contracteer` binary and an old Node: from the host,
  the frontend gate and the Contracteer CLI run inside the sandbox image
  (`docker run --rm --network none -v "$PWD":/work -w /work/frontend
  libris-agent:local 'npm test'`). The sandbox itself is the other way round:
  Node 24, `npm` and `/usr/local/bin/contracteer` are there and there is no
  `docker`, so a run inside it calls the gate directly.

## Backend build and detekt
- Spring Boot 4.1.1 names: `spring-boot-starter-webmvc`,
  `spring-boot-starter-flyway`, `tools.jackson.module:jackson-module-kotlin`
  (Jackson 3, `tools.jackson.databind`; `com.fasterxml.jackson` 2.x is on the
  test runtime only). `RestTestClient` comes from
  `spring-boot-resttestclient`, lives in `org.springframework.test.web.
  servlet.client`, and `@AutoConfigureMockMvc` does not exist. `kotlin-reflect`
  is needed at runtime.
- Jackson 3 renames: `asText()` is `asString()`; a `JsonNode` declares its
  own `map(Function)` that shadows Kotlin's `Iterable.map`, so an array is
  read through `path(field).values()`; `required(field)` throws
  `JsonNodeException` on a missing property.
- Kotlin 2.3.21 emits no warning for an unused local: prove
  warnings-as-errors with a useless cast.
- detekt 1.23.8 runs in-process with `jdkHome` cleared; handing it a JDK 25
  `jdkHome` crashes its embedded Kotlin 2.0.21 compiler. The plain `detekt`
  task has `classpath.from(main.compileClasspath, main.output)`, which is
  what makes a `!!` fail the gate: do not drop it in a cleanup. It has no
  JDK on that classpath, so two JDK exception types caught in one `try`
  (`IOException` and `SAXException`) read as one class to
  `UnreachableCatchBlock`, and `!!` on a Java-typed receiver goes unreported.
- detekt runs on the test sources too, but only inside `check`:
  `./gradlew detekt` alone is the main sources. Run `./gradlew detekt`
  before each commit anyway: `ImportOrdering` fails an import added by hand
  out of lexicographic order, `VariableNaming` refuses a backticked property
  (`@ArchTest fun \`name\`(classes: JavaClasses)` instead of a `val`),
  `ReturnCount` allows two returns, `LongParameterList` refuses a function of
  more than six parameters but exempts data classes (a shared fixture is a
  value plus `copy(...)`, not a builder), `SpreadOperator` trips on
  `runApplication(*args)`, `UnusedPrivateProperty` fails a constructor
  argument no test uses yet (write the case that motivates it first),
  `SwallowedException` and `TooGenericExceptionCaught` stay quiet when the
  parameter is named `ignored`. An elvis over a platform type Kotlin reads
  as non-null is unreachable code.
- `check` also runs `jar`, which writes a `-plain.jar` beside the boot jar
  in `build/libs`; the image's build stage runs `bootJar` only.

## Backend tests
- The first HTTP request and the first XML parse of a JVM cost more than a
  second. A client test with a short timeout warms the client once in
  `@BeforeAll` under a long timeout, then resets the stubs. A delay stub
  (`answersTooLate`) must delay a real recorded answer, otherwise the case
  passes without the timeout, on the unreadable empty body.
- Spring injects a test constructor only with `@Autowired` on it. A
  `@SpringBootTest` with explicit `classes` does not detect nested
  `@TestConfiguration` classes: `@Import` them.
- The web slice (`@WebSliceTest`) component-scans `infra.web`, so every use
  case a controller of the package takes must be in the class-level
  `@MockitoBean(types = [...])` of every web-slice test, not only the one
  exercised. Mockito stubs a method taking a value class from Kotlin call
  syntax (`given(lookup.lookUp(isbnOf("…")))`).
- A scenario class boots the whole application and commits what its
  requests write; `FreshSchema` on `JdbcSliceTest` and `ScenarioTest` is
  what keeps the JDBC slice from meeting a reader it did not insert.
- `ArchitectureTest`'s port rule matches any non-interface class assignable
  to a domain interface, so the variants of a sealed *interface* in `domain`
  break it: a state is a sealed *class*.
- A bean of `infra.lookup` may not be named `bnf`: the scenario harness owns
  that name for its WireMock server.
- A marcxchange `controlfield` is not a `datafield`: it has a `tag` and text,
  no subfield, and `getElementsByTagNameNS(MARCXCHANGE, "datafield")` never
  sees it. The BnF writes the record's ark in control field 003, as a whole
  URL (`http://catalogue.bnf.fr/ark:/12148/cb43636708p`).
- A marcxchange `datafield` carries `ind1` and `ind2`, one character each and
  a space when empty, and two fields of a record may share a tag: the BnF
  writes the publisher's 214 with `ind2="0"` and the printer's with `ind2="3"`,
  in either order. Field `100 $a` is fixed-length and holds the date of
  publication at positions 9 to 12, after the eight digits of the date the
  record was entered and one letter.
- The BnF writes a provisional record before the book is published:
  9782371025219 carries no 461 and names its series in `200 $a` and its tome
  in `200 $h` (`tome 7`), writes its page count without the stop
  (`1 volume 348 p`) and marks its date of publication `u`. The catalogue
  replaces such a record with a final one, so its recording drifts from the
  live API.
- `BnfStubs.answers(isbn, body)` serves a body written in the test,
  `knows(isbn)` the recording of that ISBN under
  `backend/src/test/resources/scenarios/bnf/`. A hand-written body is not
  saved there: it is not a recording.
- `RestClient`'s `retrieve().body()` throws `HttpClientErrorException.NotFound`
  on a 404, and `NotFound` is a `RestClientException`: a source that tells a
  miss from a failure catches `NotFound` first, or every miss reads as a
  failure.
- WireMock serves the most recently added matching stub, so
  `OpenLibraryStubs.answers("/search.json", body)` called after `knows(isbn)`
  replaces the recorded search of that lookup.
- Open Library writes the edition's own `key` as a path (`/books/OL50534552M`)
  and the search's `edition_key` as bare keys (`OL50534552M`): matching one
  against the other needs the last segment.
- `@Order` on the `@Bean` methods of a `@Configuration` orders the
  `List<T>` Spring injects; the order of the methods in the file does not.
- A test double that proves two calls overlap waits on a `CyclicBarrier` with
  a bounded `await(timeout, unit)`: sequential code then fails with
  `TimeoutException` instead of hanging the suite forever.
- `BnfStubs` matches the search by `withQueryParam("query", containing(isbn))`,
  so a stub keeps matching when the query grows clauses; assert the query in
  full from `server.allServeEvents` instead.
- A problem detail is built in the controller (`ProblemDetail.forStatus`
  with `type`) and answered as a `ResponseEntity` body; Spring writes
  `application/problem+json` and derives `title` from the status. No advice,
  no exception, no `spring.mvc.problemdetails.enabled`.
- `/actuator/health` answers `{"groups":["liveness","readiness"],
  "status":"UP"}`, not the bare status.

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
- `contracteer mock api/openapi.yaml -p 9099` starts in about four seconds
  and logs `Contracteer mock server started on port 9099` last; the global
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
  response, so `9782723488525` answers 200 and `9782000000006` answers 404.
  A value that matches no example gets a generated 200.
- `expect.toSatisfy(predicate, message)` is an asymmetric matcher in
  Vitest 5: it is how one `toEqual` over a whole object asserts a nullable
  field (`value === null || typeof value === "string"`) against the values
  the mock generates.
- `npm run format` passes `--ignore-path ../.gitignore`, or Prettier
  rewrites `dist/` and `coverage/`; `prettier --check` cannot parse
  `nginx.conf`.
- `registerType: "autoUpdate"` on the PWA plugin; after a release the first
  load still shows the previous revision, the second the new one.
- vue-router's first navigation is asynchronous: `app.use(router)` starts it
  and nothing of the route is rendered on the tick `mount()` returns, nor
  after a microtask flush. `FastEntryScenarios`' `open()` queries the host
  synchronously, so `createLibrisApp` sets `router.currentRoute` from
  `router.options.history.location` right after `app.use(router)`; the eager
  navigation still runs, so the history listeners and `isReady()` are wired
  as usual. Drop that line and every scenario that does not query through
  `findBy*` fails on an empty host holding the footer alone.
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
- The colour roles and the type steps of `docs/DESIGN.md` are Tailwind theme
  tokens in `src/ui/style.css`: custom properties on `:root`, overridden in a
  `prefers-color-scheme: dark` block, exposed through `@theme inline` as
  `--color-*` and `--text-*`. A template names `bg-surface` or `text-body`;
  a colour shade or a type size written in a class is a step that is missing
  from the file. The box of a control, `h-[50px]` or `border-[1.5px]`, is
  not a token and is written as is.
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
  it.
- A word `SourceEditionCard` displays is asserted in three files, not one:
  the card's own spec, `IsbnView.spec.ts` (*asks for the ISBN-13 the rule
  computes and shows the card*) and `FastEntryScenarios.spec.ts`
  (`showsTheOnePieceCard`). Taking a word off the card reds all three; grep the
  word over `src/` before calling the change done.
- A case that stubs `fetch` and still builds its client on
  `inject("mockBaseUrl")` passes whether or not the stub installed, the mock
  answering the same example: give such a client a base URL that resolves
  nowhere, so the stub is the only thing that can answer.
- `/` is a prefix of every path, so `router-link-active` sits on a link to `/`
  on every screen. What follows the screen shown is the *exactly* active link:
  `RouterLink` writes `aria-current="page"` and `router-link-exact-active` on
  it alone. A test reads it with `getByRole("link", { current: "page" })`, a
  template styles it with `exact-active-class`.
- Two Tailwind utilities for the same property on the same element are settled
  by the order Tailwind emits them, not by the order in the attribute. Where a
  state must win — the active tab's colour and weight over the bar's — put the
  common value on the ancestor, to be inherited, and the state's value on the
  element: its own declaration beats an inherited one whatever the order.
- `App.vue` is the full-height shell: `h-dvh` flex column, a
  `min-h-0 flex-1 overflow-y-auto` wrapper around `RouterView`, then the footer
  and the tab bar. A view that fills the screen writes `min-h-full` on its
  `<main>`, never `h-full`, or content longer than the viewport is clipped
  instead of scrolling.
- Mounting a component that holds a `RouterLink` needs the route settled first:
  `await router.push(path)` and `await router.isReady()` before `mount`, then
  `window.history.replaceState(null, "", "/")` in an `afterEach`, since
  `createWebHistory` writes to jsdom's real history and a leftover path makes a
  later spec mount the wrong screen.
- `BarcodeDetector` is not in TypeScript's DOM library. The two interfaces
  the camera adapter needs are written in
  `src/infra/camera/CameraBarcodeScanner.ts` and are not `declare global`:
  an ambient declaration would tell every file the type exists on every
  browser. The global is read through a function on each call, never captured
  in a module constant, so a `vi.stubGlobal("BarcodeDetector", …)` installed
  after the module loads is seen, and `vi.unstubAllGlobals()` is enough to
  forget it.

## Contract and release
- Contracteer 4.0.0's CLI cannot load an OpenAPI 3.1 document: the contract
  stays 3.0.3 and `nullable` is the 3.0 keyword. On an operation without
  parameters a response example creates no scenario; the verifier emits one
  generated case.
- A release is `git tag vX.Y.Z <merge sha> && git push origin vX.Y.Z`, then
  `gh release create vX.Y.Z --title vX.Y.Z --generate-notes`; `--target
  <sha>` is refused. Tag only after the CI run on `main` has pushed the
  `sha-` images. The ghcr images are public: pulling needs no login.
- The `images` job proves an image builds, nothing runs it: nginx, the
  `HEALTHCHECK`, the SPA fallback and the `/session` redirect are exercised
  only by a deploy and the phone check.
