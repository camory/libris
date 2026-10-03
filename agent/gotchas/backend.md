# Libris — Gotchas, backend

Read whole by a run that changes `backend/`, after `every-run.md`.

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
- Jackson 3's `asString()` coerces: a JSON `null` answers `""` and an object
  or array throws `JsonNodeException`, so a source's text is read as
  `takeIf { it.isString }?.asString()` and an adapter catches
  `JsonNodeException` beside `RestClientException`.
- Jackson 3 leaves `FAIL_ON_UNKNOWN_PROPERTIES` off: a body field no DTO
  declares is dropped, never refused.
- Kotlin 2.3.21 emits no warning for an unused local: prove
  warnings-as-errors with a useless cast.
- detekt 1.23.8 runs in-process with `jdkHome` cleared; handing it a JDK 25
  `jdkHome` crashes its embedded Kotlin 2.0.21 compiler. The plain `detekt`
  task has `classpath.from(main.compileClasspath, main.output)`, which is
  what makes a `!!` fail the gate: do not drop it in a cleanup. It has no
  JDK on that classpath, so two JDK exception types caught in one `try`
  (`IOException` and `SAXException`) read as one class to
  `UnreachableCatchBlock`, and `!!` on a Java-typed receiver goes unreported.
- Kotlin is indented by 2 spaces, the Gradle scripts too; detekt refuses
  anything else and `./gradlew detekt --auto-correct` reindents a file. It
  also refuses a comma after the last parameter of a declaration (a
  function, a constructor, an enum's last entry); a call keeps its own.
- Kept by hand, since detekt neither reports nor corrects them
  (`NoMultipleSpaces`, `Wrapping` and `ParameterListWrapping` are off for
  them, and `FunctionSignature` cannot be on: it puts the closing
  parenthesis on its own line):
  the body of a function written with `=` starts on the next line; the last
  parameter, the closing parenthesis, the return type and the `=` or `{`
  share one line, in a constructor too; a chain broken over several lines
  has one call per line, the receiver alone on the first, in the production
  code only, a test keeps its chains as they read best; the arrows of a
  `when` are aligned in a column, one column per `when`, and a branch whose
  body would span several lines calls a method instead.
- `./gradlew detekt` reports on the test sources as well as the main ones.
  Run `./gradlew detekt` before each commit: `ImportOrdering` fails an
  import added by hand out of its order, lexicographic but for `java.`,
  `javax.` and `kotlin.`, which come last in that order
  (`kotlin.text.Charsets.UTF_8` after `org.…`), `VariableNaming` refuses a backticked property
  (`@ArchTest fun \`name\`(classes: JavaClasses)` instead of a `val`),
  `ReturnCount` allows two returns, `LongParameterList` refuses a function of
  more than six parameters but exempts data classes (a shared fixture is a
  value plus `copy(...)`, not a builder), `SpreadOperator` trips on
  `runApplication(*args)`, `UnusedPrivateProperty` fails a constructor
  argument no test uses yet and `UnusedParameter` a function parameter no
  case reads yet (write the case that motivates it first: a use case reaches
  the signature its brief names one cycle at a time),
  `SwallowedException`, `TooGenericExceptionCaught` and `UnusedParameter`
  stay quiet when the parameter is named `ignored`. An elvis over a
  platform type Kotlin reads as non-null is unreachable code.
- A gradle command piped into `tail` or `grep` answers the pipe's exit
  status, not Gradle's: a red `detekt` read through `| tail -5` looks green
  unless `BUILD FAILED` is in the lines kept. Read for `BUILD SUCCESSFUL`,
  or run it unpiped.
- `UnusedPrivateMember` fails two private overloads of one name that are
  called only from lambdas (`map { responseOf(it) }`), though both are used;
  give them distinct names (`bookOf`, `copyOf`).
- `LongParameterList` counts a test class's `@Autowired` constructor too
  (`ApiContractTest` at seven); `@Suppress("LongParameterList")` goes on its
  own line above the class, since an annotation inside
  `class X @Suppress(...) @Autowired constructor(` trips
  `AnnotationOnSeparateLine`.
- `check` also runs `jar`, which writes a `-plain.jar` beside the boot jar
  in `build/libs`; the image's build stage runs `bootJar` only.
- A named volume mounted where the image has no directory is root-owned, so
  the non-root backend cannot write it until the image creates that
  directory for its user.

## Backend tests
- The reviewer counts statements, not phases: a case of two or more
  statements carries the markers even when they are all assertions on one
  call. The tree's forms are `// Given / When / Then` above a run of
  assertions (`IsbnTest`, `CoverNameTest`), and `// Given` then
  `// When / Then` when a `val` sets up the case (`FileCoverStoreTest`).
  Older unmarked cases (`NewBookRequestTest`'s first five) are not a model.
- The first HTTP request and the first XML parse of a JVM cost more than a
  second. A client test with a short timeout warms the client once in
  `@BeforeAll` under a long timeout, then resets the stubs. A delay stub
  (`answersTooLate`) must delay a real recorded answer, otherwise the case
  passes without the timeout, on the unreadable empty body.
- Spring injects a test constructor only with `@Autowired` on it. A
  `@SpringBootTest` with explicit `classes` does not detect nested
  `@TestConfiguration` classes: `@Import` them.
- The web slice (`@WebSliceTest`, in the `fixture` package) component-scans
  the packages of `MeController`, `CoverController` and `ProblemAdvice`
  (`library.infrastructure.web`, `bibliography.infrastructure.web`,
  `shared.infrastructure.web`), so every use case a controller of them
  takes must be in the class-level `@MockitoBean(types = [...])` of every
  web-slice test, not only the one
  exercised. Its `WebSliceConfiguration` cannot live in the root test
  package: a `@SpringBootTest` without `classes` looks for one
  `@SpringBootConfiguration` in the test's own package and finds two there,
  `@TestComponent` notwithstanding. Mockito stubs a method taking a value
  class from Kotlin call syntax, `invoke` included
  (`given(lookupEditionByIsbn(isbnOf("…")))`).
- A Mockito matcher on a value-class argument must match the underlying
  value: the mangled JVM method receives the bare `UUID`, so
  `eq(contracteer.id)` never matches (the stub answers `null`, seen as a
  `403` in `ApiContractTest`), and Kotlin's null check on the matcher's
  `null` throws `eq(...) must not be null`. The form that works:
  `browseCatalogue(ReaderId(eq(id.value) ?: id.value), any())`; `any()` is
  fine for a nullable value-class parameter.
- A cover is the file named by its `CoverName` alone, no extension, in
  `LIBRIS_COVERS_DIR` (`libris.covers.directory: ${LIBRIS_COVERS_DIR:}`);
  its media type is the text of the file `<name>.type` beside it, and a
  picture without one, or with one that is not `image/…`, is no cover.
  Nothing reads a format from the bytes. The name is `Cover.name`, the
  SHA-256 of the bytes in 64 lower-case digits (`"test"` names
  `9f86d0…0a08`, the contract's example); `FileCoverStore.write` writes the
  picture before its `.type`, and leaves a cover already stored as it is.
  An edition holds the name as `coverName`, the `cover_name` column since
  `V007`. `ScenarioTest` gives the variable a temporary directory,
  `LibrisApplicationTest` its own property. A Mockito matcher on a
  `CoverName` argument takes a valid fallback, since the
  constructor checks it: `findCover(CoverName(any() ?: NO_COVER))`.
- The binder keeps an unresolved `${VAR}` as its literal text: a setting
  bound from `${VAR}` alone starts without the variable (T051 found
  the covers directory bound to the path `${LIBRIS_COVERS_DIR}`). An empty
  default, `${VAR:}`, binds null, which a non-null property refuses at
  start (seen with `bootRun`; no test covers it).
- A variable must not carry the environment form of its own setting's name
  (`LIBRIS_COVERS_DIR` for `libris.covers.dir`): the binder then reads it
  straight from the environment, above `application.yaml`, and an exported
  variable (from `backend/.env`) beats the value a test sets for it. Measured
  2026-10-01; hence `libris.covers.directory`.
- A scenario class boots the whole application and commits what its
  requests write; `FreshSchema` on `JdbcSliceTest` and `ScenarioTest` cleans
  and migrates before each case, so no case meets a row another case wrote
  (JUnit's method order is not alphabetical: *S10* ran before *S1*).
- The sandbox PostgreSQL survives between runs (tmpfs: gone when the
  container is recreated). Editing a migration the database has already
  applied (the one of the current task, before its PR is merged) breaks
  every context start: Flyway validates the checksums and migrates while
  the context loads, before `FreshSchema` cleans, so every database-backed
  class fails whole with `initializationError`, `Failed to load
  ApplicationContext` and `Validate failed: Migrations have failed
  validation`, and running one slice class alone changes nothing. Restoring
  the file's bytes is the cheap fix as long as the edited version never
  applied. Otherwise forget the row,
  `DELETE FROM flyway_schema_history WHERE version = '003'` (the version is
  zero-padded), and drop its tables. From the host:
  `docker compose --env-file agent/.env -f agent/compose.yaml exec -T
  postgres psql -U libris -d libris -c "…"`. Inside the sandbox there is
  neither `docker` nor `psql`: run the statement through `jshell` and the
  driver already in the Gradle cache,
  `jshell --class-path "$(find ~/.gradle/caches -name 'postgresql-*.jar' |
  head -1)" -q <script>`, the script opening
  `java.sql.DriverManager.getConnection(System.getenv("LIBRIS_DB_URL"),
  System.getenv("LIBRIS_DB_USER"), System.getenv("LIBRIS_DB_PASSWORD"))`.
  D11 forbids editing a merged migration anyway, and the price of the trap
  is that a constraint a migration declares cannot be mutation-checked once
  it has run.
- `reader.default_bookshelf_id` references `bookshelf` `deferrable initially
  deferred`: the reader is inserted before the bookshelf that is their
  default, and PostgreSQL checks the reference at commit. A JDBC slice test
  rolls back, so that check never runs there: a reader whose default
  bookshelf the test never inserts is not refused. `membership.reader_id` is
  checked at the statement, in the slice too, and so are `copy.edition_id`
  and `copy.bookshelf_id`: a slice case proves them with
  `shouldThrow<DataIntegrityViolationException>`, and inserting a copy needs
  the reader, the bookshelf and the edition rows first, hence four
  repositories in the `@Import`.
- `ArchitectureTest`'s application rule lists what `application` may see of
  Spring: `org.springframework.stereotype..`,
  `org.springframework.transaction.support..` and the one type
  `TransactionStatus`, which the lambda given to `TransactionOperations`
  takes, since the rule reads lambda parameters. `@Transactional` is outside
  the list on purpose: a boundary is `TransactionOperations`, never the
  annotation. A use case that needs another Spring type either widens the
  rule in the same cycle or does not carry it.
- `ArchitectureTest`'s port rule matches any non-interface class assignable
  to a domain interface, so the variants of a sealed *interface* in `domain`
  break it: a state is a sealed *class*.
- A bean of `bibliography.infrastructure.lookup` may not be named `bnf`: the
  scenario harness owns
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
- inventaire.io's `/img/entities/{W}x{H}/{hash}` scales a picture to *cover*
  the box (the larger of the two ratios) and never enlarges, so `100x600`
  answers at most 600 tall while `480x600` answers a 1000×1500 picture at
  480×720 (measured 2026-10-01: 322×500 at `300x300` is 300×466, at
  `100x400` 258×400; `0x300` and `1x300` are `400`). Its
  `/api/entities?action=by-uris&uris=isbn:<13 digits>` answers an unknown
  ISBN `200` with `entities: {}` and the ISBN under `notFound`, a known one
  under its `inv:` id, the picture's hash at `claims["invp:P2"][0]`.
- A test double that proves two calls overlap waits on a `CyclicBarrier` with
  a bounded `await(timeout, unit)`: sequential code then fails with
  `TimeoutException` instead of hanging the suite forever.
- `BnfStubs` matches the search by `withQueryParam("query", containing(isbn))`,
  so a stub keeps matching when the query grows clauses; assert the query in
  full from `server.allServeEvents` instead.
- A problem detail is built in the controller with `problem(status, type)`
  of `Problems.kt` and answered with `.asResponse()`; Spring writes
  `application/problem+json` and derives `title` from the status.
  `ProblemAdvice` answers what fails before a controller runs: a path
  variable of the wrong type and an unreadable body, as
  `/problems/validation` without `detail`, through one `@ExceptionHandler`
  of the two exceptions. It extends nothing, so any other framework failure
  keeps Boot's plain error answer. No `spring.mvc.problemdetails.enabled`:
  turned on alone, it answers these two with no `type` (Spring leaves
  `about:blank` out) and with a `detail`, and three contract cases go red.
- A request that is not a `GET` and carries no `X-Requested-With` is denied,
  so every test posting through the security chain sends it: the web-slice
  tests by hand, `ApiContractTest`'s `FixedReaderHeaders` in its map, the
  scenarios through the `restTestClient` bean of `StubbedSources` as a
  default header. That bean replaces the autoconfigured client, so a
  `RestTestClientBuilderCustomizer` would not reach it.
- In `ApiContractTest` an exception nothing answers shows as `403`, not
  `500`: `FixedReaderHeaders` is a filter registered for the `REQUEST`
  dispatch only, so the error dispatch to `/error` arrives without the
  reader headers and the security chain refuses it. A `403` there is a stub
  that answered `null` (an argument no stub names, a matcher that missed)
  or a framework failure no handler turned into a problem.
- The JDK `HttpClient` writes header values as US-ASCII (`Léa` leaves as
  `L?a`) and Tomcat reads them as ISO-8859-1. The scenario client is built on
  `SimpleClientHttpRequestFactory`, which sends the UTF-8 bytes, and
  `RemoteIdentity.of` decodes `Remote-Name` from ISO-8859-1 bytes to UTF-8;
  the other `Remote-*` headers are read as they come.
- The scenario WireMock servers live as long as the context, across classes;
  `FreshSources` on `ScenarioTest` resets their stubs and request journal
  before each case, so a case stubs every source it needs and `verify(0, …)`
  counts only the case's own requests.
- `/actuator/health` answers `{"groups":["liveness","readiness"],
  "status":"UP"}`, not the bare status.
- `FastEntryScenarios > S1 Typed ISBN, found` compares the whole `200` body
  with `JsonCompareMode.STRICT`, so a field added to `IsbnResponse` reds it
  with `AssertionError: Unexpected: <field>` wherever else the task is green.
  A task that adds a response field names the field in that JSON too; only the
  full gate sees it, never the class the task is working in.
- UNIMARC field 105 `$a` is fixed-length coded data whose form-of-contents
  codes sit at positions 4 to 7, `t` marking a comic strip; a record without
  the field names no form. The language an ouvrage was translated from is
  field 101 `$c`, in the BnF's own three-letter codes (`jpn`, `kor`, `chi`),
  which are not the ISO 639-1 codes the `LANGUAGES` map of `BnfEditionLookup`
  answers for the `language` field.
- A name row shared by many parents is inserted or matched in one statement:
  `insert into author (id, name) values (:id, :name) on conflict (lower(name))
  do update set name = author.name returning id`. The conflict target is the
  expression the unique index carries, `lower(name)`, not the column; and
  `do update` is what makes `returning` answer the row that already exists,
  where `do nothing` answers no row at all and costs a second statement. The
  row keeps the spelling that created it, so an edition naming it otherwise
  reads back with that first spelling.
- `ResultSet.getInt` and `getLong` read a null column as `0`. A nullable
  integer is read with `rs.getObject("page_count", Int::class.javaObjectType)`,
  which answers `null`; the mutation to `getInt` reds a case asserting an
  absent page count only if some case of the class leaves it absent.
- A unique column that is nullable counts no null in PostgreSQL, so any number
  of editions without an `isbn13` sit side by side while two editions cannot
  share one.
- `@JdbcSliceTest` lives in `fr.amory.libris.fixture` beside `FreshSchema` and
  `WebSliceTest`, and serves both contexts. A slice test
  imports the repository it proves with `@Import(Jdbc…Repository::class)` and
  takes it through an `@Autowired` constructor, with `JdbcClient` beside it
  only when a case queries through it: detekt's `UnusedPrivateProperty` fails
  a constructor argument no case reads.
- `JdbcClient` expands an empty collection parameter to `IN ()`, which
  PostgreSQL refuses (`BadSqlGrammarException`): a finder over a list of ids
  answers `emptyList()` without a statement when given none, and a slice case
  (`no edition finds no copy`, `no id finds no edition`) proves it.
- `awaited_cover` is keyed by `edition.isbn13` and references it: a slice
  case inserts the edition before its awaited cover, hence
  `JdbcEditionRepository` in the `@Import` of `JdbcAwaitedCoverRepositoryTest`.
  The port has no read yet, so that class reads the row through `JdbcClient`.
- `EditionsInMemory.insert` appends without looking at the ISBN, so a use
  case that inserts a held edition a second time fails at
  `editions.stored.single()` ("List has more than one element") before any
  later assertion of the case runs; there is no `update` on the port.
- `LookupAnswering`, in `bibliography.fixture`, records the ISBNs it was asked
  and answers them as `asked`, so a case proves a source was never called with
  `source.asked shouldBe emptyList()`.
- kotest's `shouldBeInstanceOf<T>()`, from `io.kotest.matchers.types`, answers
  the value narrowed to `T`: a case asserting one field of a result variant
  chains onto it and needs no cast.
- The catalogue order is the PostgreSQL collation `ignoring_case_and_accents`
  (ICU `und-u-ks-level1`, nondeterministic, created by `V005`): it ignores
  case and accents but not spaces and hyphens, so `One piece` sorts before
  `Onepiece`. Names equal under it fall to the next key, never to byte order,
  which is why the keyset of `JdbcCatalogueEditions` ends on the id. A
  `COLLATE` on a column of the `ranked` CTE carries into its `ORDER BY` and
  its comparisons. `SELECT DISTINCT` requires every `ORDER BY` expression
  in its select list (`for SELECT DISTINCT, ORDER BY expressions must
  appear in select list`), collation or not; a semi-join (`EXISTS`) keeps
  an edition once without widening the answer.
- A row comparison `(a, b) > (c, d)` with a nullable member answers `NULL`
  when it meets one: the keyset of `JdbcCatalogueEditions` spells the
  nullable tome out (`IS NULL`, `IS NOT DISTINCT FROM`) instead.
- `placing` is a reserved word of PostgreSQL: as a table alias it is a
  syntax error (`syntax error at or near "placing"`).
