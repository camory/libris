# Libris — Progress log

Append-only. Newest entry last. One entry per loop iteration (or per human
session that changed something a future run must know).

An entry is the story of one task in about twenty-five lines, never more
than forty. It repeats nothing the pull request holds: what was built and
how it was verified are its body, and the diff is the diff. A fact a future
run must know goes in `agent/GOTCHAS.md`, not here. The entries of a
finished phase move to `agent/archive/`, one file per phase.

Format:

```
## YYYY-MM-DD — T### short title — status
- Did: what exists now that did not, in three lines at most
- Decided: one item per choice the brief left open, what and why (or "nothing")
- Deviations from the brief: what and why (or "none")
- Left over: what the task leaves to another, and where it is written (or "nothing")
```

---

## 2026-09-12 — T013 The ISBN-13 value type and the Open Library source
- Did: `Isbn13` in `domain` (thirteen digits, check digit verified, a refusal
  answered as `null`), the `IsbnSource` port with `Source`, `AuthorRole`,
  `SourceAnswer` (known, nothing known, failed), `SourceEdition`,
  `SourceAuthor`, `SourceSeries`, and `OpenLibrarySource` in `infra.lookup`
  over a `RestClient` built on a JDK `HttpClient` that follows redirects.
  Eleven cycles, `Isbn13Test` and `OpenLibrarySourceTest` against WireMock
  and the recorded answers. `./gradlew check` green, the six scenario methods
  still skipped, `LibrisApplicationTest` green with neither
  `LIBRIS_OPEN_LIBRARY_URL` nor `LIBRIS_SOURCE_TIMEOUT` set.
- Decided, for a future run:
  - **Jackson 3 is the one on the main classpath.** Spring Boot 4 brings
    `tools.jackson.databind` (3.1.5); `com.fasterxml.jackson` 2.21.5 is on
    the test runtime only. The renames that matter here: `asText()` is
    `asString()`. A `JsonNode` is an `Iterable<JsonNode>`, but it declares
    its own `map(Function)`, which shadows Kotlin's `Iterable.map` on the
    node itself, so an array is read through `path(field).values()`. It also
    has `required(field)`, which throws `JsonNodeException` on a missing
    property or on a `MissingNode`; that is how a body the client cannot read
    becomes a failure.
  - **Settings go through `application.yaml` and a properties class**, as
    the datasource already does: the yaml maps `LIBRIS_*` variables with
    their defaults onto `libris.sources.*`, `SourcesProperties` binds them,
    `LookupConfig` declares the source beans from it, and a client is a plain
    class the tests construct. Defaults live in the yaml only. A bean in
    `infra.lookup` may not be named `openLibrary` or `bnf`: the scenario
    harness owns those names for its WireMock servers.
  - **A source is stubbed through a fixture, not copied stubs.**
    `fixture/OpenLibraryStubs` (`knows`, `doesNotKnow`, `fails`, `failsOn`,
    `answers`, `answersTooLate`) and `fixture/BnfStubs` (`knows`,
    `partiallyKnows`, `doesNotKnow`, `fails`) each wrap a `WireMockServer`;
    `fixture/recorded()` reads `src/test/resources/scenarios/`.
    `FastEntryScenarios` holds the scenarios, `ask` and the reader headers
    only, so a run never needs to touch it: a source test uses the fixture,
    and a case the fixture cannot express is a method added to the fixture.
  - **`SourceAnswer` is a sealed class, not a sealed interface.**
    `ArchitectureTest`'s "a port of the domain is implemented in the
    infrastructure only" rule matches any non-interface class assignable to a
    domain interface, so variants of a sealed interface living in `domain`
    break it; a sealed class does not.
  - **The first HTTP request of the JVM costs more than a second**, so a
    client built with a 200 ms timeout times out on the first case whatever
    it stubs. `OpenLibrarySourceTest` warms the client once in `@BeforeAll`
    with a generous timeout; the cases then run under 200 ms, the delayed one
    included. T015 should expect the same of the scenarios, whose timeout is
    `1s`.
  - detekt counts returns: three in one function is one too many, and an
    elvis over a platform type Kotlin reads as non-null is unreachable code.
- Deviations from the brief: a properties class and a yaml block instead of
  `@param:Value` (decided with Tophe on review); an answer
  the client cannot read — an edition with no title, an author with no name,
  an empty body — is a failure through Jackson's `required()` and a second
  catch, not through the HTTP one; the warm-up call in `@BeforeAll`.
- Reviewed with Tophe on 2026-09-12: `Isbn13` accepts ASCII digits only
  (`isDigit()` and `digitToInt()` are Unicode-aware); two failure cases added
  for the unreadable answers above; the ArchUnit port rule noted in
  `agent/PROPOSED.md`; the source port and what it answers moved to
  `domain.lookup`, `Isbn13` and `AuthorRole` staying at the root (D02
  amended with Tophe); `Redirect.NORMAL`; the settings moved to
  `application.yaml` and `SourcesProperties`, proven by two cases added to
  `LibrisApplicationTest` on the same context. The Open Library and BnF stubs extracted
  to `fixture`, the scenario file edited with Tophe for that alone.
- Left over: nothing of T013. The port has no caller yet — T014 (the BnF
  source) and T015 (the lookup use case) are next.

## 2026-09-12 — T020 the ISBN endpoint and its four answers — done
- Did: `application/IsbnLookup.kt` — the use case over the T013 source port and
  the sealed `LookupResult` (`Found(edition, sources)`, `UnknownIsbn`,
  `SourcesUnavailable`); `infra/web/IsbnController.kt` — `GET
  /api/v1/isbn/{isbn}`, its response types and its three problem details;
  `fixture/SourceAnswering` — the fake `IsbnSource` answering a fixed answer,
  which T014 reuses. Eleven red-green cycles, one commit each. Moved the
  backend pin to `v0.3.0`: Contracteer now runs five cases, `/api/v1/me` and
  the four examples of the ISBN operation, all green. Un-skipped `S4 Unknown
  ISBN` and `S7 Every source down`; `S1`, `S5` and the two `S6` stay skipped.
  `./gradlew check` green, run plainly.
- Decided:
  - **A problem detail is built in the controller, never thrown.** A private
    `problem(status, type)` of `IsbnController.kt` returns
    `ProblemDetail.forStatus(status)` with `type` set, and the controller
    answers it as the body of a `ResponseEntity`. No `@RestControllerAdvice`,
    no exception, no `spring.mvc.problemdetails.enabled`. Measured: Spring
    then writes `application/problem+json` on its own and derives `title`
    from the status (`ProblemDetail.getTitle()` falls back to the reason
    phrase), so nothing sets it. Spring adds `instance` (the request path);
    the contract tolerates it.
  - **Mockito stubs a method taking a value class.** `given(lookup.lookUp(
    isbn13Of("9782723488525")))` works from Kotlin call syntax despite the JVM
    mangling of `Isbn13`; the hand-written fake the brief kept in reserve was
    not needed.
  - **A web-slice test must mock every use case of `infra.web`, not only the
    one it exercises.** `WebSliceConfiguration` component-scans the package, so
    the new `IsbnController` broke `MeControllerTest` and `SecurityConfigTest`
    with `No qualifying bean of type IsbnLookup`. Both now declare
    `@MockitoBean(types = [ReaderVisit::class, IsbnLookup::class])`, which also
    keeps one shared context for the three classes.
  - **A scenario writes to the database and commits.** `ScenarioTest` is a full
    `@SpringBootTest`, so the reader headers of an un-skipped scenario insert
    `juliette` for good, and `JdbcReaderRepositoryTest` then failed on the
    unique username. The slice test now starts from an empty table
    (`@Sql(statements = ["delete from reader"])`, rolled back with the test
    transaction); any future scenario that authenticates would have broken it
    the same way.
  - The cold-JVM timeout T013 warned about did not materialise: `S4` answers
    404 and `S7` 503, on two runs of the class alone and on the full gate.
- Deviations from the brief: `MeControllerTest.kt` and `SecurityConfigTest.kt`
  (the mocked `IsbnLookup`) and `JdbcReaderRepositoryTest.kt` (the empty table)
  are changed although the brief's *Changed* list names neither; none is in its
  *Not changed* list and no acceptance criterion covers them. Both changes are
  consequences of the endpoint existing, above.
- Reviewed with Tophe on 2026-09-13: `IsbnControllerTest` deleted. Contracteer
  is the web slice test: it proves structure and types, never values, by
  design; a hand-written controller test exists only for behaviour the
  contract cannot express, `MeController`'s role from the groups header being
  the example. Exact values are asserted in domain, application and scenario
  tests, never in a slice. The no-interaction check on a bad ISBN is proven by
  the types: no `Isbn13`, no call. The explicit `title` line removed, the
  reviewer's finding, confirmed by running the cases without it. The
  `isbn13Of` helper, written three times (the contract test, the use case
  test, T013's source test), is one function of `fixture`. The use case test
  answers with `AN_EDITION`, a title and nulls: it proves the edition is
  passed through, not its values, and `ONE_PIECE_1` names the document's
  example in the contract test alone. An edition builder with defaults waits
  for T014, whose merge tests need it. From now on a
  contract task opens with the pin bump: the new cases are the red, the
  controller the green. The D07 wording is to be reviewed with Tophe as a
  whole, not amended piecemeal.
- Left over: nothing of T020. The `sources` of an answer is the one source that
  replied until T014 merges several; the BnF stubs of `S4` and `S7` stay
  unused, as the brief says. Two follow-ups in `agent/PROPOSED.md`.

## 2026-09-13 — the test schema is fresh for every database-backed class — done by Tophe + Claude (interactive)
- Did: `fixture/FreshSchema`, a JUnit `BeforeAllCallback` that takes the
  `Flyway` bean of the Spring context and runs `clean()` then `migrate()`;
  `JdbcSliceTest` and `ScenarioTest` carry it and set
  `spring.flyway.clean-disabled=false` for their contexts. Found on T020: a
  scenario class boots the whole application on a real port, so the reader
  visit of its first request commits `juliette`, and `JdbcReaderRepositoryTest`
  then failed on the unique username on the next run against the same
  database (the sandbox one survives between runs; CI's is fresh).
- Decided: the class that commits owns the state, and every class that touches
  the database starts from a freshly migrated schema, before the class rather
  than after so that a run that dies mid-way leaves nothing for the next.
  Cleaning the scenario context alone is not enough: the scenarios recommit
  the reader after their own clean, so the slice needs the same start. A
  cleanup in the victim test (`@Sql(statements = ["delete from reader"])`,
  T020's first answer) is dropped: one per table, and it says nothing of why.
  Gotcha: `backend/.env` points the host gate at the throwaway compose
  database `bootRun` uses, so a gate run wipes what was entered by hand.
- Left over / gotchas: the `@Sql` line leaves `JdbcReaderRepositoryTest` once
  #60 is merged. D07's sentence on the slices does not say it yet; to be
  written when D07 is reviewed as a whole.

## 2026-09-13 — T014 the merge rule and several sources — done
- Did: `domain/lookup/Merge.kt`, the first domain rule written as a function:
  `merge(isbn, editions)` answers one `SourceEdition` whose every field is the
  first edition that gives one, `authors` and `series` taken whole, the ISBN
  the asked one and the cover always Open Library's by that ISBN. Beside it
  `openLibraryCoverOf(isbn)`, the one place the address is written now:
  `OpenLibrarySource` lost its private `COVERS` constant and calls it, its
  answer unchanged. `IsbnLookup` takes `List<IsbnSource>`, asks them in the
  order Spring gave them and answers `Found(merge(…), the sources that knew)`,
  `SourcesUnavailable` only when every source failed, `UnknownIsbn` as soon as
  one replied and none knew. `MergeTest` (six cases) and `IsbnLookupTest`
  (five cases, rewritten over two fakes) are new or rewritten;
  `fixture/SourceEditions.kt` holds the shared edition the two use.
- Decided:
  - **The shared fixture is a value, not a builder.** The brief asked for a
    function with every parameter defaulted; detekt's `LongParameterList`
    refuses a function of twelve parameters (threshold 6, and
    `ignoreDefaultParameters` is false by default) while it exempts data
    classes. Raising the threshold would have meant editing
    `config/detekt/detekt.yml`, which the brief's own criterion forbids, and
    a local `@Suppress` buys a lint escape for nothing. So
    `A_SOURCE_EDITION` is one value and a test varies one field with
    `copy(…)` — the same "name only the field you are about" the builder was
    for, in the idiom Kotlin already gives. Any future fixture of a data
    class with many fields should follow it rather than fight the rule.
  - **The gate's own order.** A test file whose imports are not in
    lexicographic order fails `detekt` (`ImportOrdering`) though it compiles
    and runs: adding an import by hand costs a `./gradlew detekt` before the
    commit.
  - The merge is one expression over `firstNotNullOfOrNull`, so the rule of
    every nullable field is the same line; `authors` is the first non-empty
    list, `firstOrNull { it.authors.isNotEmpty() }`.
- Deviations from the brief: the fixture above, `A_SOURCE_EDITION` +
  `copy` instead of `sourceEdition(…)`, for the reason given. Everything the
  builder was needed for is unchanged. Nothing else.
- Gotcha, not a deviation: three cycles of `MergeTest` (authors whole, series
  whole, the cover) and four of `IsbnLookupTest` passed the moment they were
  written. The field-by-field rule of the third cycle is one expression that
  covers the whole of S5 at once, so the later cases pin behaviour instead of
  driving it; they are still the criteria's proofs and each would fail on a
  rule that merged inside `authors` or `series`, or that let an edition's own
  `coverUrl` through. Their commits say `test(backend)`, not `feat`.
- Left over: nothing of T014. The four skipped scenario methods (`S1`, both
  `S6`, `S5`) stay skipped and go green with T015's BnF, which is also what
  ranks a source before another — here the order is the one Spring gives.

## 2026-09-13 — T015 the BnF source — done

- Did: `infra.lookup.BnfSource`, the SRU 1.2 client on `LIBRIS_BNF_URL`,
  asking `?version=1.2&operation=searchRetrieve&recordSchema=unimarcxchange&maximumRecords=1&query=bib.isbn all "<isbn>"`
  and mapping the marcxchange record the JDK's own `DocumentBuilderFactory`
  parses — no new dependency. Title `200$a`, subtitle `200$e`, authors
  `700`/`701`/`702` in document order (`$b` + `$a`, role from `$4`), series
  `461$t` with `461$v` as a number, collection `410$t`, publisher `210$c`,
  year the first four digits of `210$d`, pages the number before `p.` in
  `215$a`, language `101$a` through a map, summary and cover `null`, the
  `isbn13` the one asked. A failure or an unparseable answer is `Failed`, no
  marcxchange `record` is `NothingKnown`. `SourcesProperties` gained
  `bnfUrl`, `application.yaml` its default `https://catalogue.bnf.fr/api/SRU`,
  `LookupConfig` ranks the two beans with `@Order`. `BnfSourceTest` (eight
  cases) over the recorded answers, `BnfStubs` gained `answersTooLate`, and
  the four scenario methods `S1`, `S5` and both `S6` are un-skipped:
  `FastEntryScenarios` now runs seven of seven, none skipped.
- Decided:
  - **The role map is a top-level `internal` function.** No recording carries
    a `440` or a `730`, so the only honest way to prove the whole map is to
    call it: `authorRoleOf(functionCode)` sits beside the class and its test
    reads the five cases in one place. Mapping through the HTTP seam would
    have meant inventing records the BnF never sent.
  - **The ISBN of the answer is the one asked, not `010$a`.** The BnF writes
    it hyphenated; the contract wants thirteen digits, and the search already
    matched on the digits we hold.
  - **Ranking lives in the wiring.** `@Order(1)` on the BnF bean and
    `@Order(2)` on Open Library is what fixes the list Spring injects into
    `IsbnLookup` — verified by mutation: with `@Order(9)` the application
    test reports `[OPEN_LIBRARY, BNF]`. Declaration order alone does not
    rank them, and the use case stays ignorant of who comes first.
  - **One request client for both sources.** `SourceHttp.kt` holds
    `sourceRestClient(baseUrl, timeout)`, the `JdkClientHttpRequestFactory`
    with the connect and read timeouts that `OpenLibrarySource` had built
    privately; both clients call it now.
  - **The cold JVM costs more than the 200 ms test timeout.** The first
    lookup of the class loads the HTTP client, the XML parser and their
    modules, and it fails the timeout cases into a false green — or the real
    cases into a false red. A `@BeforeAll` does one lookup under a 20 s
    timeout and resets the stubs; every case then runs at 200 ms. Any future
    source test with a short timeout needs the same warm-up.
- Deviations from the brief: the order of the cases inside `BnfSourceTest`.
  The brief put `the source names itself` first, but a class whose
  constructor takes `baseUrl` and `timeout` that no test yet uses fails
  detekt (`UnusedPrivateProperty`), so the SRU-request case came first — it
  is what motivates both arguments — and the naming case second. Every case
  of the plan is there, nothing else changed.
- Gotcha, not a deviation: the mapping is one expression, so the whole
  record is pinned by one field-by-field assertion in the commit that maps
  it (`a178a87 feat(backend): the BnF source maps the UNIMARC record`); the
  fields have no case of their own, but each would fail on a wrong tag or
  subfield.
- Review fix-ups (2026-09-14): the parser read the answer as bytes, so a
  prolog naming an encoding the JDK lacks made `DocumentBuilder.parse` throw
  `IOException`, which escaped `BnfSource.lookUp` and took the whole lookup
  down. The answer is a `String` the HTTP layer already decoded, so the parser
  now reads it through a `StringReader`: the prolog's encoding is ignored, no
  `IOException` can arise, and one `catch` of `SAXException` covers what the
  parser throws — the case `an answer that cannot be read is a failure` pins
  it over `BnfStubs.answersUnreadably`, the one hand-written body in the
  fixture. A second `catch` of a JDK type was tried first and detekt reported
  it unreachable: the plain `detekt` task has no JDK on its classpath, so two
  unresolved JDK exception types read as one class to `UnreachableCatchBlock`.
  `answersTooLate` delays the recorded answer, not an empty body, so only the
  read timeout can turn that case green. Year and page count go through
  `toIntOrNull()` like the volume number. Any source test with a delay stub
  should delay a real answer for the same reason
  (`OpenLibraryStubs.answersTooLate` still delays an empty body).
- Left over: nothing of T015. `COLOURIST` has no BnF function code and the
  language map holds only `fre → fr`; both are in `agent/PROPOSED.md` for
  Tophe.

## 2026-09-14 — T016 the ISBN rule and the lookup client — done
- Did: `domain/Isbn13.ts` (`isbn13Of`, separators dropped, the old ten
  converted after its own mod-11 check, the `978`/`979` prefix and the
  check digit verified, everything else `null`) with nine cases;
  `domain/SourceEdition.ts`, `application/IsbnApi.ts` (the port, `IsbnAnswer`,
  the key) and `infra/api/FetchIsbnApi.ts` with its four cases against the
  mock; the pin moved to `v0.3.0` and `FetchMeApi` gained the `Accept` header.
- Decided:
  - **The shape regex is the length rule.** `^97[89][0-9]{10}$` refuses a
    text of the wrong length, a letter and the empty string before the check
    digit is ever computed; the comparison with `digits.slice(12)` did that
    job until the prefix case brought the regex, which is why four of the
    nine cases went green on arrival and their commits say `test(frontend)`,
    not `feat`.
  - **The ten is verified, then converted, then verified again.** The
    conversion recomputes the thirteenth digit, so the converted value always
    passes the ISBN-13 check: only the mod-11 sum over the first nine plus the
    check character (`X` worth ten, either case) can refuse a wrong ten. The
    letters of the first nine go through `Number`, which makes them `NaN` and
    refuses them, so no `X` can hide inside the ISBN.
  - **The client reads the body once, after branching on the status.** A 200
    reads `IsbnResponse`, anything else reads `ProblemResponse` and answers
    its `type`; no status list, and the wire types stay private to the file.
- Deviations from the brief: none.
- Review fix-ups (2026-09-14): a ten whose check character was a tab, a
  newline or a non-breaking space was converted, `Number()` reading those as
  zero; `thirteenOf` now refuses any ten outside `^[0-9]{9}[0-9Xx]$` first,
  with the case that motivates it. The client hands the 200 body over as the
  edition: the wire type names the thirteen fields, the domain type is the
  same shape, and a field-by-field copy between them was three edits per
  contract change.
- Left over: nothing of T016. The 401 on the lookup call is in
  `agent/PROPOSED.md`; the port is not wired into `createLibrisApp`, which is
  T017's, and no fake of it exists yet.

## 2026-09-14 — T017 the lookup screen, typed ISBN — done
- Did: `ui/views/isbn/IsbnView.vue` and its eleven cases (the header, the field
  and its label, the *Chercher* button, `isbn13Of` before anything leaves,
  the title of the answer, the three messages, the fallback for a problem
  named like an object member), `fixture/FakeIsbnApi.ts`,
  `ui/components/icons/IconAlert.vue`, the colour roles and the type steps in
  `ui/style.css`, the `/isbn` route, the eight strings of the `fr` catalogue,
  `isbnApi` in `LibrisPorts` and in `bootstrap`, the home page's link, and the
  four scenario methods un-skipped.
- Decided:
  - **The screen renders on the tick it is mounted.** `FastEntryScenarios`'
    `open()` calls `bootstrap(…).mount(host)` and then queries the host
    synchronously, but vue-router starts its first navigation at
    `app.use(router)` and finishes it a macrotask later, so the host held the
    footer alone and all four methods failed on
    `Unable to find an accessible element with the role "textbox" and name
    "ISBN"`. `createLibrisApp` now sets `router.currentRoute` from
    `router.options.history.location` immediately after `app.use(router)`.
    The eager navigation still runs to its end, so `finalizeNavigation`
    replaces the history entry, `markAsReady` wires the popstate listener and
    `isReady()` resolves as before; only the first paint stops waiting for a
    guard queue this application does not have. The assignment needs a cast to
    `RouteLocationNormalizedLoaded` because `resolve()` types `name` as
    nullable. This is the one thing on the branch the scenarios forced and the
    brief did not foresee.
  - **The tokens carry the scheme, the templates carry role names.** The six
    roles are custom properties on `:root` overridden under
    `prefers-color-scheme: dark`, exposed to Tailwind through `@theme inline`;
    the accent is a literal of that block, the same in both schemes; the type
    steps are `--text-*` tokens of the same block. No component names a
    scheme and no class holds a colour shade or a type size; the box of a
    control is written as is.
  - **The busy button is markup, not a component.** A `<span>` with
    `animate-spin` inside the button, and the label switched by the same ref
    that disables it; the field is never disabled.
  - **`card title` is declared now.** The brief lists the step among the ones
    this screen uses and the title of the answer is an *ouvrage*'s title; the
    sentence saying it arrives with the card was read as the older one.
- Deviations from the brief: the `router.currentRoute` assignment in
  `createLibrisApp.ts`, above. The file was already on the brief's changed
  list and every acceptance criterion still holds.
- Left over: nothing of T017. The tab bar of U03, the look of `HomeView` and
  the keyboard's go key are in `agent/PROPOSED.md`. The hint sentence names
  scanning, which T019 ships; it is the spec's own wording and the PR says so.

## 2026-09-14 — T018 the card of the answer — done
- Did: `SourceEditionCard.vue` shows what the sources know — cover, overline
  *One piece · tome 1*, title, subtitle, one line per author with the French
  words of their roles, the six rows, the summary and the source chips — with
  `SourceEditionCardSkeleton.vue` standing in its place while the lookup runs;
  `IsbnView` shows both, the words live in `src/ui/i18n.ts`, the sample edition
  in `src/fixture/SourceEditions.ts`, and the two `S1` scenarios run.
- Decided:
  - **The overline lives inside the heading.** `<h2>` holds the série · tome
    span and the title, so the card has one heading naming the ouvrage and a
    screen reader reads *One piece · tome 1 Romance dawn* rather than a
    stray paragraph followed by a title. It is also what makes the scenario's
    `/One piece\D{0,12}1\b/` match: Vue drops the whitespace between two
    sibling elements, so a `<p>` above the `<h2>` renders `tome 1Romance dawn`
    in `textContent` and the word boundary never arrives. The rule is in
    `agent/GOTCHAS.md`.
  - **The author line and the overline are `<i18n-t>` and a span.** The middle
    dot stays a pattern of the `fr` catalogue (*{name} · {roles}*) while the
    roles keep their `muted` span; `vue-tsc` and the boundaries rules accept
    the global component with no configuration.
  - **The words for the API's codes are three sections of the catalogue** —
    `role`, `source`, `language` — keyed by the code, beside `home` and `isbn`,
    as the brief proposed; only `language` falls back, through `te()`, and only
    `fr` is written.
  - **The card's place is one element.** The skeleton and the card carry the
    view's `mt-5` through attribute fallthrough instead of a wrapper `<div>`.
- Deviations from the brief: the overline is a span inside the `<h2>` rather
  than a paragraph above it, above; every acceptance criterion still holds.
- Left over: the skeleton is judged by the phone check of the spec's *Done*,
  since it is `aria-hidden` and carries no text; `S2` waits for T019's camera;
  two bullets are in `agent/PROPOSED.md` — the silhouette inherits the busy
  button's endless wait on a rejected lookup, and the `language` section holds
  one word.

## 2026-09-14 — T019 The barcode scan — done
- Did: the `BarcodeScanner` port and its injection key in `application`, the
  `CameraBarcodeScanner` adapter in the new `src/infra/camera` over the
  browser's `BarcodeDetector` and `getUserMedia`, and `FakeBarcodeScanner` in
  `fixture`; `IsbnView` opens the camera by itself when the detector announces
  `ean_13`, shows the viewfinder with its icon button toggling between
  *Scanner le code-barres* and *Fermer la caméra*, and runs T017's lookup with
  the first code the ISBN rule accepts; `S2 Scanned barcode` runs.
- Decided:
  - **The camera opens on arrival, no tap.** `S2` opens `/isbn` and expects
    the card with no click, and its body may not be edited, so the screen acts
    before the reader touches it. The mockup's *Ready* state is what a browser
    with no detector, a refused camera and a pressed cross all show. The pull
    request asks Tophe to confirm it; wanting the tap instead means changing
    the scenario, which is a spec conversation.
  - **`stop()` answers the pending `read()` with `null`, even when a look is
    in flight and finds a code.** One value, one `if` in the view: `null`
    means "no code", whether the reader refused the camera, pressed the cross
    or left the screen, and the screen does the same thing in every case. A
    look the detector could not take counts as nothing seen and the camera
    keeps looking; a picture that cannot start gives the camera back with
    `null`. The fake honours the same contract, which is what lets the view's
    cases and the adapter's cases tell the same story. (Review fix-ups: the
    adapter first returned a code after `stop()` and let a rejecting
    `detect()` or `play()` escape with the stream still open.)
  - **The adapter keeps the whole loop.** It applies `isbn13Of` itself, so a
    shelf full of EAN-13s that are not ISBNs never reaches the view and no
    scan ever shows *ISBN invalide*, and it stops every track of the stream
    it opened, on success and on `stop()`.
  - **The icon button is a sibling of the `<input>`** inside a `relative`
    wrapper, never inside the `<label>`, or its content would join the
    field's accessible name and the eleven existing cases and the four
    scenario methods would stop finding *ISBN*.
- Deviations from the brief: `src/createLibrisApp.spec.ts` changed too, one
  line, though the brief's file list does not name it — `LibrisPorts` gained a
  required `barcodeScanner`, so the spec that builds the ports inline no
  longer compiled. Two view cases passed the moment they were written, since
  the first cycle's `if (scanned !== null)` branch already covered them; they
  are committed as `test:` rather than `feat:`.
- Left over: the camera block's height, a screen that explains a refused
  camera, and a *rien trouvé* after a long fruitless look are three bullets in
  `agent/PROPOSED.md`; what jsdom lacks and where Vitest reads its
  configuration are in `agent/GOTCHAS.md`. The hand check of the spec's *Done*
  needs HTTPS or `localhost`, since `getUserMedia` exists nowhere else.

## 2026-09-14 — T021 The BnF alone, with its cover — done
- Did: Open Library left the backend — source, stubs, recordings, the merge
  rule and their tests deleted, `LIBRIS_OPEN_LIBRARY_URL` with them — and
  `IsbnLookup` now asks the one `IsbnSource` it is given; `Isbn13` answers the
  ten digits a 978 ISBN was made from, `BnfSource` searches on both in one
  CQL `or` query and fills `coverUrl` from control field 003.
- Decided:
  - **The ten lives on `Isbn13`, not in the source.** Converting a thirteen to
    the ten it was made from is a rule of the number, not of the BnF, so
    `isbn10` is a property of the value type and answers `null` for a 979,
    which is what makes the query of a 979 a single clause with no branch in
    `BnfSource`.
  - **One request, not two.** `bib.isbn all "9782723488525" or bib.isbn all
    "2723488527"` answers the record in one round trip, measured against the
    real SRU on 2026-09-14, so the source keeps one request, one timeout and
    one failure mode.
  - **The use case takes one source, not a list of one.** `List<IsbnSource>`
    with a single element would keep the shape of the merge rule alive with
    no rule behind it; the bean keeps the name `bnfSource` and loses its
    `@Order`, since order has nothing left to order.
  - **The cover URL is built, never fetched.** `coverUrlOf` sits beside
    `authorRoleOf` and cuts the ark out of the control field from `ark:/` on;
    a record with no 003, or one holding the old `FRBNF…` number, gives no
    cover, and what the card shows then is T022.
  - **`FastEntryScenarios` was edited**, which a task normally may not do: the
    spec conversation for the one-source answer already happened (#73, #76)
    and the brief authorises it. The two S6 cases and S5 went with the rule;
    the past-the-timeout case became the second S7.
- Deviations from the brief: none in substance; the plan's step 1 became two
  cycles (the ten, then the 979 that has none) and its steps 7 to 10 one
  commit, since nothing compiles between the deletions and the tests that
  follow them.
- Left over: the contract's `200_FOUND` example still carries an Open Library
  cover and two sources, and the frontend fixtures still name Open Library —
  both are bullets in `agent/PROPOSED.md`, with the one that matters most: a
  book whose UNIMARC record comes back as a diagnostic (`9782253098058`) is
  now answered as an unknown ISBN, since no second source is left to know it.
  The hand check of the phase therefore needs a pre-2007 book the BnF serves
  whole in `unimarcxchange`.

## 2026-09-15 — T022 The stand-in of the cover — done
- Did: `IconBook.vue` joins the three icons, an outlined book on the 24 grid
  drawn at 40; `SourceEditionCard`'s cover block is now one element holding
  the picture or, when there is none and when the image errors, that icon.
- Decided:
  - **The three cases the brief left to the code are one `v-if`.** The image
    shows when `edition.coverUrl` is there and the card's `coverFailed` ref is
    false, the icon otherwise, so "no cover" and "the cover did not load" are
    the same branch and the same stand-in, which is what U06 asks for.
  - **The cover block became a box that holds one of the two.** Before, the
    `<img>` was itself the 96 by 149 box and the empty `v-else` div repeated
    its four classes; U06 says "the same block", so the box moved out and the
    image fills it with `object-contain`, unstretched as before. The
    `bg-border` behind the picture, the radius and the size are unchanged.
  - **`text-muted opacity-60` is given by the card, not by the icon.** The
    icon names no colour, like its three neighbours; the card fades the icon
    alone, never the `border` block behind it.
- Deviations from the brief: none in substance. The first green gave the image
  a `cover` computed folding the URL and the failure together; the self-review
  dropped it for the brief's shape, a `v-if` reading both, one indirection
  less.
- Left over: the failure is remembered for the life of the card and is not
  reset when the prop changes — a bullet in `agent/PROPOSED.md` for the day a
  screen shows two editions in a row without unmounting. What jsdom does with
  an `<img>` is in `agent/GOTCHAS.md`. The fixtures' Open Library cover URL,
  already proposed on T021, is untouched.

## 2026-09-15 — T023 The ISBN in one class, both writings — done
- Did: `Isbn13` is `Isbn` on both sides, one class holding every rule of the
  identifier — both writings in, the thirteen digits out — with one factory,
  `of`, for what a reader types or a barcode carries; the frontend's
  `isbn13Of` became `Isbn.of` on a nominal class, and the view, the barcode
  adapter and the controller ask it. The writing the API's path admits is the
  controller's rule: it matches the contract's pattern before asking `Isbn.of`.
- Decided:
  - **The writing of the path is the controller's rule, not a second
    factory.** The run had added `Isbn.ofThirteenDigits` for it; Tophe's
    review moved the check to the controller, where the contract's pattern
    is, and the cases about the writing to `IsbnControllerTest`. The domain
    answers whether a text is an ISBN, the API which writing it admits.
  - **The Bookland prefix is a rule of the domain.** The two halves
    disagreed: the backend took any thirteen digits with a right check digit,
    the frontend only a `97[89]` one. The frontend's rule stands, because the
    contract's path pattern is `^97[89][0-9]{10}$`, the PRD ties the ISBN to
    the EAN-13 barcode, and S2 reads only 978 and 979 codes. The one visible
    change: `GET /api/v1/isbn/4006381333931` answers 400 instead of asking
    the BnF — a request the contract's pattern already excluded.
  - **The ten writing answers through the thirteen rule.** It verifies its
    own mod-11 check, builds the twelve and then hands the result to the
    thirteen rule rather than constructing an `Isbn` itself, so no path into
    the value skips the check digit and the prefix.
  - **`SourceEdition.isbn13` became `isbn`.** The type says thirteen no more.
    The JSON field stays `isbn13`, as does the frontend type mirroring it.
- Deviations from the brief: none in substance. The brief's step 7 named one
  tidy; the self-review added a second, the controller's local `isbn13` now
  holding an `Isbn`, so the path variable binds to `text` and the value is
  `isbn`.
- Left over: the frontend `Isbn` still derives no ten, and D02 still names
  `Isbn13` — both are bullets in `agent/PROPOSED.md`, the second proposed as
  an amendment in the pull request body since a run does not edit the
  document. No scenario moved; S5 and S6 stay skipped for T024.


## 2026-09-15 — T025 The Found state without its sources — done
- Did: the frontend pins release `v0.4.0` of the contract, whose `Isbn` schema
  declares twelve fields; `sources` and the `Source` type left `IsbnResponse`,
  `SourceEdition` and `onePiece1`, and the *Sources* row with its chips left
  `SourceEditionCard`, the summary now the card's last part (U06). The word
  *Sources* and the names of the two sources left the `fr` catalogue with it
  (U08). The client still hands the parsed body through, so the `v0.3.0`
  backend in production keeps being read whole until T026 ships.
- Decided:
  - **The compatibility case builds its client on a host that resolves
    nowhere**, `http://an-older-backend`, not on `inject("mockBaseUrl")` as
    `FetchMeApi`'s stubbed case does. On the mock's base URL the case would
    pass whether or not `vi.stubGlobal` installed, the mock answering the same
    `ONE_PIECE_1` example; on a dead host the stub is the only thing that can
    answer, so the case fails loudly when it stops being installed.
  - **The view's third assertion is deleted, not replaced.** Title and
    publisher still prove the card is on screen; inventing another word to
    assert would add a claim the task did not ask for.
- Deviations from the brief: one. `src/ui/views/isbn/IsbnView.spec.ts` is a
  tenth changed file, against the criterion that lists nine: its case *asks
  for the ISBN-13 the rule computes and shows the card* ended on
  `expect(screen.getByText("BnF")).toBeDefined()`, a word the card no longer
  shows. The brief did not name the file. The edit is one deleted line, of the
  same kind and for the same reason as the two the brief authorises in
  `FastEntryScenarios.spec.ts`, and it contradicts no document, so the run
  deviated rather than blocking. The pull request states it.
- Left over: the backend still sends `sources` and still pins `v0.3.0`; T026
  moves it once this frontend is deployed, which is Tophe's step. Prettier
  disagrees with `frontend/vitest.global-setup.ts`, on `main` already and not
  in the gate — a bullet in `agent/PROPOSED.md`. The three places that assert
  a word of the card are in `agent/GOTCHAS.md`.


## 2026-09-15 — T026 The answer without its sources — done
- Did: the backend pins release `v0.4.0`, whose `Isbn` schema declares twelve
  fields and allows no other, so `sources` left `IsbnResponse`, `responseOf`
  and, with it, `LookupResult.Found`, which now carries the edition alone;
  `IsbnLookup` maps `Known` to `Found(answer.edition)` and still tells the
  three outcomes apart. The S1 body lost its `sources` line and the four cases
  skipped for T024 their `$.sources` assertions.
- Decided:
  - **The four skipped assertions went with the field, in the same commit as
    the controller.** The brief left the split of commits open: the scenario
    file is one edit of one kind — five lines that read a field which stops
    existing — and splitting it would have put a compiling but false assertion
    on the branch for one commit.
  - **Nothing was tidied in the controller.** With `sources` gone `responseOf`
    takes one parameter and the `when` reads as before; renaming or reshaping
    anything else would have been a second change the task did not ask for.
  - **The launch condition was taken as met.** Nothing in the tree names the
    deployed revision — `deploy/compose.yaml` takes its tag from `LIBRIS_TAG` —
    so there was no evidence against the brief's assumption to stop on.
- Deviations from the brief: one, on Tophe's review. The brief kept `Source`
  and `IsbnSource.source` for T024; Tophe judged them not worth keeping, since
  the enum existed to fill the field this task removes and the order of the
  sources in T024 is the order of the beans, not a name. Both left in a
  fix-up commit with the BnF's *names itself* case and the GOTCHAS item that
  explained the gap; `LibrisApplicationTest` asserts the only source by type.
- Left over: merging is not deploying. This backend stops sending `sources`
  the moment it runs, and the frontend that stopped reading it (T025) must be
  in production first; releasing and deploying are Tophe's step. S5 and S6
  stay skipped for T024.


## 2026-09-15 — T024 Open Library back, in two requests — done
- Did: `domain/lookup/Merge.kt` came back as a free function over a list of
  editions — the first source's value for every field it gives, the next
  filling the empties, the authors whole from the first source that has any,
  the cover an ordinary merged field; `IsbnLookup` took the list of ports and
  asks them all at once on `Executors.newVirtualThreadPerTaskExecutor()`
  through `invokeAll`, which hands the answers back in the order of the
  sources, and still tells `Found` from `UnknownIsbn` and `SourcesUnavailable`
  from the answers alone; `OpenLibrarySource` returned in its two-request
  shape (the edition document for the fields, one search for the authors of
  the work whose `edition_key` holds the edition's key, the cover built from
  the ISBN), wired second behind the BnF with `LIBRIS_OPEN_LIBRARY_URL`. The
  four scenario methods Tophe left skipped are green untouched.
- Decided:
  - **The work is picked on the last segment of the edition's `key`.** The
    edition document writes `/books/OL50534552M`, the search writes
    `OL50534552M`. Both works of the recorded 9782253098058 search carry
    the same author, so `the authors come from the work that holds the
    edition` stays green on an implementation that takes the first work;
    the case that is red on it is `a search naming no work holding the
    edition gives no authors`.
  - **`NotFound` is caught before `RestClientException`.** Spring's
    `HttpClientErrorException.NotFound` is a `RestClientException`; in the
    other order Open Library's miss would have read as a failure.
  - **`@Order(1)` and `@Order(2)` order the injected list**, not the order of
    the `@Bean` methods in `LookupConfig`, and `LibrisApplicationTest` asserts
    the two classes in order rather than trusting it.
  - **The rendezvous fake waits with a bound.** `CyclicBarrier.await(2 s)`
    makes a sequential use case fail with `TimeoutException` in two seconds
    instead of hanging the suite; the case was watched red on the old
    single-source code before the executor arrived.
- Deviations from the brief: two, both in the shape of tests, no criterion
  weakened.
  - The criterion asks `MergeTest` for *one case per field group*. It has two
    whole-edition cases instead — both sources give every field, then the
    first leaves every field empty — which assert all ten fields at once, plus
    the named author, series, cover and ISBN cases. The brief's own test plan
    sends the file back from T014's history "and adapt", and that is the shape
    it had; a case per field would have been ten copies of one assertion.
  - The four failure cases of `OpenLibrarySourceTest` (failing edition,
    failing search, unreadable body, past the timeout) were written after the
    catches that answer `Failed`, which arrived with the first case of the
    adapter, so they passed on the first run instead of being red first. They
    are kept: each one pins a distinct path out of the adapter, and the 404
    case, the only failure case that needed production code, was red before
    it.
- Left over: nothing of the task. A 404 answered by `/search.json` would read
  as `NothingKnown`, since the adapter catches around both requests at once —
  no case can make it wrong today, and the bullet is in `agent/PROPOSED.md`.
  Merging is not deploying: the phase's *Done* asks Tophe to type
  9782380751673 on a deployed Libris, which is his step.


## 2026-09-16 — T027 The BnF's newer records — done
- Did: `BnfSource` reads the publisher from the 214 whose second indicator is
  `0` and falls back to 210; the publication year from field `100 $a`,
  positions 9 to 12, falling back to the publication field's `$d`; a page
  count written with or without the stop after `p`; and, when a record carries
  no 461, the series from `200 $a` with the tome from `200 $h`. `UnimarcField`
  carries its second indicator and `UnimarcRecord` answers for a field by tag
  and indicator. Three records recorded from the live API.
- Decided:
  - **The year is wired through the publication field, not through 210.** The
    backlog line reads the fallback from "214 then 210", and the field the
    publisher comes from is the field whose `$d` names the year; the two are
    read once per edition. No recorded record exercises the fallback, whose
    proof is the unit case over the two texts.
  - **A `$h` that names no tome gives no series.** The rule reads the series
    from the title statement only when the part number is of the shape
    `tome <n>`; the hand-written record of the indicator case has a `200 $a`,
    no `$h` and no 461, and answers no series at all.
  - **The partial recording lost its field 100.** With the year read from 100,
    `9782723488525-without-pages-and-year.xml` would have named 2013 and the
    file's name would have stopped being true; its 215 and its `210 $d` were
    already blanked for the same reason. `S5 Merged answer` is untouched: its
    2013 comes from Open Library.
  - **The hand-written body holds a `200 $a` and two 214 fields and nothing
    else**, under an invented ISBN, and is not saved as a recording: it is the
    only case that is red on an implementation taking the first 214.
- Deviations from the brief: none.
- Left over: nothing of the task. The BnF's `330 $a` summary stays unread and
  is in `agent/PROPOSED.md`; 9782371025219 is provisional and the catalogue
  will replace it, which `agent/GOTCHAS.md` now says.


## 2026-09-16 — T030 The tab bar — done
- Did: `AppTabBar.vue` shows a house linking to `/` and, in the middle of the
  bar, a barcode in an `accent` circle linking to `/isbn`, *Accueil* and
  *Ajouter* their `aria-label`s, on `surface` with a hairline above, 48 tall,
  and a bottom padding of `env(safe-area-inset-bottom)`; `App.vue` became the full-height
  shell that scrolls the view and holds the footer and the bar; `HomeView` was
  redrawn to the column and the type scale, and the lookup screen's camera
  block now grows into the space left instead of keeping a ratio. New entries
  `tabs.home` and `tabs.add`.
- Decided:
  - **The bar after BDGest's** (Tophe, on review): the run drew two equal
    tabs, a house and a plus, each its icon over its word. The review settled
    on the shape of BDGest's bar without its labels: the house alone on the
    left, and in the middle an `accent` circle holding the barcode the field
    already uses, *Accueil* and *Ajouter* as `aria-label`s, no plus; three
    columns, the third empty until a screen claims it. `IconPlus` and the
    `tab` step were removed, U02, U03 and U07 and the spec's *Screen* section
    amended in the same pull request.
  - **The inactive look is inherited, the active look is the element's own.**
    `text-muted` sits on the `<nav>` and `text-accent` in
    `exact-active-class` on the anchor, so the active tab wins by the
    cascade rather than by the order Tailwind happens to emit its utilities.
    Both on the same element would make the colour depend on that order.
  - **The bar reads the route through the exactly-active link.** `/` is a
    prefix of every path, so `router-link-active` marks *Accueil* everywhere;
    only `aria-current="page"` and `exact-active-class` follow the screen
    shown. Both route cases were green the moment the two links existed, so a
    mutation check (`to="/isbn"` pointed elsewhere) was run to prove they
    disagree with the code.
  - **The lookup column is `min-h-full`, not `h-full`.** The camera block takes
    the leftover space with `flex-1`, and a long *Found* card still grows past
    the viewport and scrolls.
- Deviations from the brief: none.
- Left over: nothing of the task. The footer keeps the scaffold's look
  (`p-4 text-sm`), which the brief puts out of scope; it is in
  `agent/PROPOSED.md`.

## 2026-09-17 — The footer retired, the revision on the home page — done
- Did: `App.vue` is the scrolling view over the tab bar and nothing else;
  `AppFooter.vue` and its spec are gone. The revision reaches `HomeView`
  through `revisionKey` (`application/Revision.ts`, beside the port keys, the
  only place a view may import from), provided by `createLibrisApp`,
  and ends the home page in the `label` step, `muted`. U03 and D09 say so.
  Written with Tophe on the review of T030, no task line: two cycles, gate
  green.
- Decided: **nothing needs to stay visible under every screen.** The footer
  held only the revision, a support aid for Tophe; the update banner (U09)
  will tell the reader when a newer version exists, so the line moves to the
  foot of the home page and every screen gets its height back.
- Left over: nothing.

## 2026-09-18 — T028 The new version and its banner — done
- Did: `application/AppUpdate.ts` declares the port (`onNewVersion`,
  `install`) and its key; `infra/pwa/ServiceWorkerAppUpdate.ts` registers
  `/sw.js`, keeps the worker that reaches `installed` while a controller is
  already running, sends `{ type: "SKIP_WAITING" }` on the order and reloads
  once on `controllerchange`; `ui/components/AppUpdateBanner.vue` draws U09
  from a `state` prop, `App.vue` holds that state and the three words come
  from the catalogue. The plugin moved to `registerType: "prompt"` with
  `injectRegister: false`, `nginx.conf` lost its `/registerSW.js` block, and
  the three Update scenarios are un-skipped and green: thirteen cycles, gate
  green, 90 tests.
- Decided:
  - **The shell owns the banner, so the shell injects the port.** No view
    owns chrome that stands above every screen, and a store for two refs
    would be a layer for nothing. `eslint.config.ts` widens `src/ui/App.vue`
    alone through a `shell` category; the same import from `src/ui/i18n.ts`
    is still refused, which the mutation probe proved. D05 rule 4 says the
    views alone inject the ports: the pull request proposes its amendment.
  - **A worker can be installing before the adapter can listen.** `S2` finds
    the new version on the tick that mounts the app, so `updatefound` fires
    while `register()`'s promise is still pending and the announcement is
    lost. The adapter watches `registration.installing` when the promise
    resolves too; that is the ninth adapter case.
  - **The first install says nothing.** A worker reaching `installed` with no
    controller in charge is the app's first worker, not a newer version. The
    scenarios cannot catch it — their container always has a controller — so
    it has its own case.
- Deviations from the brief: the ninth adapter case above, which the brief's
  eight did not foresee; `FakeAppUpdate` is the port's two methods and no
  more, the `announce()` and the order count the brief sketches having no
  caller now that `createLibrisApp.spec.ts` keeps its two cases.
- Left over: nothing of the task. The update that never finishes is in
  `agent/PROPOSED.md`.
- Fix-ups on review with Tophe: the state type declared once, the failed
  install as a tenth adapter case, `BusySpinner` shared by the busy button and
  the banner, and D05 rule 4 amended for the app shell.

## 2026-09-18 — T029 The check while the app stays open — done
- Did: `infra/pwa/ServiceWorkerAppUpdate.ts` keeps the registration once it
  resolves and holds a clock of its own: `betweenChecks` (one hour) arms a
  `setTimeout` that calls `registration.update()`, catches its refusal and
  arms the next hour; a `visibilitychange` listener on `document` checks and
  restarts the hour when the app is visible, and cancels the pending check
  when it is hidden, so nothing survives the app going away. Eight cases in
  the adapter's spec, over fake timers and an `update` spy on the file's own
  stubbed registration: eight cycles, gate green, 99 tests.
- Decided:
  - **The rejection a spy returns is not an unhandled one.** Vitest's `vi.fn`
    attaches its own handler to the promise it records, so a rejecting
    `update()` left uncaught raises nothing the spec can see: *keeps asking
    when a check fails* is green whether or not the `catch` is there. Its
    proof is the mutation probe instead — a schedule re-armed inside
    `.then()` reds the case at one call *and* makes the run report an
    unhandled error — and the gate's own silence on the committed tree.
  - **The restarted hour is proven from half an hour in.** The foreground
    return written at t=0, as the brief sketched it, passes against an adapter
    that ignores the foreground entirely: the hour armed by the registration
    was already about to fire. The case lets half the hour pass first, so the
    pending check and the restarted one fall an hour apart and only the
    cancel-and-re-arm keeps the count at one.
  - **`check()` arms the next hour whether or not it had a registration to
    ask.** The one `null` guard the brief allows is the optional call;
    splitting the schedule on it would be a branch no case motivates.
- Deviations from the brief: none of substance. The brief counts eight
  existing cases in the adapter's spec and 90 on `main`; the file holds ten,
  the run counted 99 with the eight new ones, so `main` carried 91.
- Left over: nothing of the task. The first foreground check after a long
  Android background may fire before the network is back, and the next answer
  is then an hour away; the pull request says so, and a retry is a spec
  conversation, not a fix inside a run.
- Fix-ups on review with Tophe: a guard in `scheduleCheck()` so an app hidden
  before its registration resolved arms nothing, with its case; the
  no-service-worker case named for what it asserts; the null-registration
  re-arm left as is, the spec's leaked `document` listeners making it
  unobservable (in `agent/PROPOSED.md`).

## 2026-09-18 — T031 The kind of a scanned ouvrage — done
- Did: `domain/Kind.kt` holds `BOOK, BD, MANGA`; `internal fun kindOf` in
  `BnfSource.kt` reads the form of contents of field 105 `$a` and the language
  of 101 `$c`; `SourceEdition` carries `kind: Kind?`, the merge takes the first
  source that names one, `IsbnResponse.kind` is non-null and `responseOf`
  writes the `BOOK` default. The backend pin is `v0.5.0`; the four scenarios of
  `KindScenarios` are un-skipped and green, gate green, 106 tests.
- Decided:
  - **The default lives in one place.** `SourceEdition.kind` is nullable with
    no default, so every construction site had to state its kind and the
    compiler listed them; `edition.kind ?: BOOK` in `responseOf` is the only
    `BOOK` the run writes, and *S4 No record says* is its end-to-end proof.
    Undone on review, see the fix-up below.
  - **The rule reads the slice, not the whole field.** `COMIC_STRIP !in
    codedData.orEmpty().drop(4).take(4)` mirrors `publicationYearOf`'s slice,
    keeps a `t` elsewhere in the coded data out of the answer, and gives the
    no-105 record `BOOK` without a guard of its own.
  - **The three manga languages are the BnF's codes.** Field 101 `$c` answers
    `jpn`, `kor`, `chi`; the `LANGUAGES` map that turns `fre` into `fr` serves
    the `language` field only and is not in the rule's way.
- Deviations from the brief: two, both small. Cycle 3's `SourceEdition`
  parameter with no default reds `ApiContractTest` at compile time, so
  `kind = MANGA` on the `ONE_PIECE_1` stub was written there instead of in
  cycle 4 as the test plan scheduled; the case itself stayed red until cycle 4.
  And the gate found `FastEntryScenarios > S1`, which the brief lists under
  *Not changed*: it compares the whole body strictly, so it gains the line
  `"kind": "MANGA"` — the record it stubs is the manga, the comparison stays
  `STRICT` and the case asserts one field more than before.
- Left over: nothing of the task. T032 carries the field to the frontend;
  nothing stores a kind yet, which the bookshelf brings.
- Fix-up on review with Tophe: no kind is ever unknown, since everything the
  rule does not mark is a book, so `SourceEdition.kind` is non-null and no
  default exists anywhere. Open Library answers `BOOK`, the merge takes the
  first source's kind as it takes the title, the controller copies it.

## 2026-09-19 — T032 The card in the words of its kind — done
- Did: the frontend pin moved to `v0.5.0`, `SourceEdition` gained
  `kind: Kind` read straight off the answer by `FetchIsbnApi`, and the `fr`
  catalogue grew a series pattern and a block of four role words per kind, so
  `SourceEditionCard` builds `isbn.card.series.<kind>` and
  `role.<kind>.<role>` and holds no French word and no branch on a kind. Its
  `authorLines` now groups the authors by name, compares their whole sets of
  roles and answers either one line of names or a line per author with its
  words. Eleven cycles, `S1 A manga` un-skipped, gate green: 14 files, 111
  tests, no skip.
- Decided:
  - **The grouping branch is a rendering shape, not two computeds.** The
    computed answers `{ name, roles }` with `roles: null` for the shared-set
    line, and the template picks `i18n-t` or a plain `<p>` on it. One list,
    one `v-for`, and the case *shows no author line…* falls out of the empty
    list with no guard.
  - **The canonical key is the sorted role codes, the display order the
    answer's.** A mutation check proved the point: dropping `.sort()` left
    all 21 cases green, so the run added *reads the same roles in another
    order as the same set*, watched it red against the unsorted variant, and
    put the sort back — 22 green. The words still read *scénario, dessin* in
    the order the BnF gave them.
  - **Every kind carries its full set of words, as the brief asked.** The
    reviewer is asked to weigh the six repeated words (*couleurs* and
    *traduction* three times, the manga's pattern repeating the livre's)
    against a shared entry that the first divergence would have to split.
- Deviations from the brief: none. The test plan's order held cycle for
  cycle, including the pin red opening the task and Prettier over
  `vitest.global-setup.ts` alone at the end.
- Left over: nothing of the task. Two follow-ups went to `agent/PROPOSED.md`
  — the grouping moving to `domain` the day a second screen names authors,
  and the two files still unformatted on `main`. Nothing stores a kind yet;
  the bookshelf brings it over `v0.6.0`.
- Fix-up on review with Tophe: a role an answer lists twice for one author
  is kept once while grouping, so the key compares sets and the words never
  repeat; its case added, 112 tests.
