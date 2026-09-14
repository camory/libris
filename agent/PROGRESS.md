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
