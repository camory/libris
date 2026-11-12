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
  six parameters or more and a constructor of seven or more
  (`functionThreshold: 6`, read as `count >= threshold`) but exempts data
  classes (a shared fixture is a
  value plus `copy(...)`, not a builder), `SpreadOperator` trips on
  `runApplication(*args)`, `UnusedPrivateProperty` fails a constructor
  argument no test uses yet and `UnusedParameter` a function parameter no
  case reads yet (write the case that motivates it first: a use case reaches
  the signature its brief names one cycle at a time),
  `SwallowedException`, `TooGenericExceptionCaught` and `UnusedParameter`
  stay quiet when the parameter is named `ignored`. An elvis over a
  platform type Kotlin reads as non-null is unreachable code, and so, in a
  test source, is every line after `val x = list.find { … } ?: return …`
  (`UnreachableCode`, T156): write the branch with an `if` instead.
- A gradle command piped into `tail` or `grep` answers the pipe's exit
  status, not Gradle's: a red `detekt` read through `| tail -5` looks green
  unless `BUILD FAILED` is in the lines kept. Read for `BUILD SUCCESSFUL`,
  or run it unpiped.
- detekt's `TooManyFunctions` keeps its default `thresholdInClasses: 11`
  (`config/detekt/detekt.yml` does not override it) and reports a class
  that reaches 11 functions, private ones included: `JdbcEditionRepository`
  sits at 10 since T066 (T157 traded `nameIdOf` for `authorIdsOf`), so a
  function it gains costs another its place.
- `UnusedPrivateMember` fails two private overloads of one name that are
  called only from lambdas (`map { responseOf(it) }`), though both are used;
  give them distinct names (`bookOf`, `copyOf`).
- `LongParameterList` counts a test class's `@Autowired` constructor too
  (`ApiContractTest` at seven); `@Suppress("LongParameterList")` goes on its
  own line above the class, since an annotation inside
  `class X @Suppress(...) @Autowired constructor(` trips
  `AnnotationOnSeparateLine`.
- A class moved to another package leaves its old `.class` in
  `build/classes`: every context boot then fails with
  `ConflictingBeanDefinitionException` on two beans of one name. Run
  `./gradlew clean` after a move.
- `check` also runs `jar`, which writes a `-plain.jar` beside the boot jar
  in `build/libs`; the image's build stage runs `bootJar` only.
- A named volume mounted where the image has no directory is root-owned, so
  the non-root backend cannot write it until the image creates that
  directory for its user.
- `gradle/wrapper/gradle-wrapper.properties` carries `networkTimeout=30000`,
  `retries=3` and `retryBackOffMs=2000`; `./gradlew wrapper` rewrites the
  file with Gradle's defaults, so an upgrade of the wrapper keeps those
  three lines.

## Backend tests
- The reviewer counts statements, not phases: a case of two or more
  statements carries the markers even when they are all assertions on one
  call. The tree's forms are `// Given / When / Then` above a run of
  assertions (`IsbnTest`, `CoverNameTest`), and `// Given` then
  `// When / Then` when a `val` sets up the case (`FileCoverStoreTest`).
  Older unmarked cases (`NewBookRequestTest`'s first five) are not a model.
- A test file's top-level fixtures sit below its class and the class's
  helpers, in the order the class reaches them; the reviewer blocks one
  above. A fixture's place is the first helper that reaches it, and within
  that helper the order in which its lines reach them, never beside a
  fixture of its kind, even when a brief says so (T130's `MY_COPY` was
  sent back for sitting beside `NOT_MINE`). A few older files put them
  above the class: they are not a model.
  A fixture added to such a file moves all of them below the class (T110's
  `BrowseCatalogueTest` was sent back for it), in the order written, since
  top-level vals initialise in that order and a later one reads an earlier.
- A test written as an expression body, `fun \`case\`() = x shouldBe y`,
  returns what `shouldBe` answers, not `Unit`, and JUnit silently skips a
  non-`void` test method: the run is green with the case never run. A
  one-statement case keeps a block body.
- Un-skipping the last `@Disabled` of a scenario class leaves
  `import org.junit.jupiter.api.Disabled` unused, which detekt's
  `NoUnusedImports` fails: the un-skip removes the import too.
- The first HTTP request and the first XML parse of a JVM cost more than a
  second. A client test with a short timeout warms the client once in
  `@BeforeAll` under a long timeout, then resets the stubs. A delay stub
  (`answersTooLate`) must delay a real recorded answer, otherwise the case
  passes without the timeout, on the unreadable empty body.
- Spring injects a test constructor only with `@Autowired` on it. A
  `@SpringBootTest` with explicit `classes` does not detect nested
  `@TestConfiguration` classes: `@Import` them.
- The web slice (`@WebSliceTest`, in the `fixture` package) component-scans
  `fr.amory.libris.web`, so every use case a controller there takes must be
  in the class-level `@MockitoBean(types = [...])` of every web-slice test,
  not only the one exercised. Its `WebSliceConfiguration` cannot live in the root test
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
  fine for a nullable value-class parameter. Mockito pairs matchers with
  arguments in the order they were recorded: a matcher put in a `val` to
  shorten a stub line must come from the first arguments, or it lands on
  the wrong one and the stub answers `null`.
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
  constructor checks it: `findCover(CoverName(any() ?: NO_COVER))`; so
  does a `BookshelfName` argument, whose fallback must not be blank:
  `createBookshelf(…, BookshelfName(any() ?: "Salon"))`.
- Libris reads a WebP through TwelveMonkeys `imageio-webp`, a `runtimeOnly`
  dependency no code names: ImageIO's service loader finds its reader on the
  class path. The JDK's JPEG reader throws `IIOException` on a broken JPEG
  (the first 200 bytes of `covers/tall.jpg`: *missing SOS marker*) and
  decodes at whatever sides its header claims, so a 20 000 × 20 000 header
  takes the test JVM's 512 MB heap down; `bibliography.fixture.jpegClaiming`
  patches the SOF0 frame of a 1×1 JPEG to claim any sides. `ImageIO.write`
  of an image with an alpha channel as `jpeg` answers `false` and writes
  nothing, so a JPEG is written from an image without alpha
  (`TYPE_INT_RGB`); the fixture's `pictureOf` checks what `write` answers.
  A cut-short WebP (the first 200 bytes of `covers/small.webp`) or BMP
  throws a plain `EOFException`, not an `IIOException`.
  `Cover.normalised()` answers `null` for what is no picture: no reader,
  a reader's `IOException`, or a header side over 5 000 pixels, read
  before any pixel. `CoverFetches` applies it to each source's answer.
  Measured on JDK 25 with 3.15.3, 2026-10-07.
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
  `CleanSchema`, in the root `fixture`, imported by `JdbcSliceTest`,
  `ScenarioTest` and `LibrisApplicationTest`, is a `FlywayMigrationStrategy`
  that cleans before the migration of the context's start too.
- The sandbox PostgreSQL survives between runs (tmpfs: gone when the
  container is recreated). Editing a migration the database has already
  applied (the one of the current task, before its PR is merged) breaks no
  context that imports `CleanSchema`, since it cleans before Flyway
  validates (T120 edited its `V014` in place, 2026-11-01). A context
  without it (a new test annotation, or `CleanSchema` dropped) fails whole
  with `initializationError`, `Failed to load ApplicationContext` and
  `Validate failed: Migrations have failed validation`. Restoring the
  file's bytes is the cheap fix as long as the edited version never
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
  repositories in its constructor.
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
- `RestClient` parses an answer's `Content-Type` before any converter runs,
  and a malformed one (`webp`, no slash) throws `InvalidMediaTypeException`,
  an `IllegalArgumentException`, not a `RestClientException`. `uri(String)`
  reads its address as a URI template, so an address built from a source's
  answer throws an `IllegalArgumentException` on `{x}` or `50%zz`.
  A cover fetch and inventaire.io's lookup read through `nullOnFailure`, in
  `SourceHttp.kt`, which answers `null` on any `Exception`, and every fetch
  reads its picture through `RestClient.pictureAt(address)` beside it, the
  media type the answer's `Content-Type`; the BnF and Open
  Library lookups tell a miss (`NotFound`) from a failure with catches of
  their own, which leave both out. `pictureAt` reads the body as a stream
  through `exchange`, up to five megabytes (5 × 1024 × 1024 bytes): a longer
  picture, an empty body or a non-2xx status is no cover. The source's
  timeout closes a body still coming, so no code of its own bounds the time.
- WireMock serves the most recently added matching stub, so
  `OpenLibraryStubs.answers("/search.json", body)` called after `knows(isbn)`
  replaces the recorded search of that lookup.
- `OpenLibraryStubs.hasCoverDribbledOver(isbn, picture, millis)` sends the
  headers at once and the body in ten chunks over the given millis, to
  prove a timeout that falls while the body is read.
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
  of `Problems.kt`, a validation problem with its `errors` with
  `validationProblem(errors)` beside it, and answered with `.asResponse()`;
  Spring writes `application/problem+json` and derives `title` from the
  status.
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
- In `ApiContractTest` an unstubbed use case answering a `List` answers
  Mockito's empty list, so a generated case of an array answer passes on
  `[]` and checks no item. Stubbed with items, the verifier checks each
  item's `minLength` and `pattern` and the array's `uniqueItems` (a name
  answered twice: `Array contains duplicate items but uniqueItems is true`,
  Contracteer on `v0.15.0`, 2026-11-01, and on `v0.17.0`, 2026-11-08).
- In `ApiContractTest` an exception nothing answers shows as `403`, not
  `500`: `FixedReaderHeaders` is a filter registered for the `REQUEST`
  dispatch only, so the error dispatch to `/error` arrives without the
  reader headers and the security chain refuses it. A `403` there is a stub
  that answered `null` (an argument no stub names, a matcher that missed)
  or a framework failure no handler turned into a problem, or a path no
  controller maps yet, its `auto:` cases included. A scenario sending to
  a path no controller maps gets Boot's plain `404` JSON instead, and to a
  path mapped for another method only (a `PUT` where a `GET` is served)
  Boot's plain `405`; the DSL's `refusal` throws on either (`not a
  problem: 404 NOT_FOUND`), when the answer is built, so the stack points
  at the call (`lea.saves`), not at the line reading `refusal`.
- The JDK `HttpClient` writes header values as US-ASCII (`Léa` leaves as
  `L?a`) and Tomcat reads them as ISO-8859-1. The scenario client and the
  web slice's client are built on `SimpleClientHttpRequestFactory`, which
  sends the UTF-8 bytes as Authelia does; `RemoteIdentity.of` decodes
  `Remote-Name` from ISO-8859-1 bytes to UTF-8, and the security chain's
  `StrictHttpFirewall` checks every header value after the same decoding;
  the other `Remote-*` headers are read as they come.
- Tomcat itself answers `400` to a C0 control character (`\u0007`) in a
  header value, before Spring: a case on the firewall's header check sends a
  C1 one (`\u0085`, on the wire `C2 85`), which only the firewall refuses.
- The scenario WireMock servers live as long as the context, across classes;
  `FreshSources` on `ScenarioTest` resets their stubs and request journal
  before each case, so a case stubs every source it needs and `verify(0, …)`
  counts only the case's own requests.
- The scenario DSL reads answers with a plain Jackson 3 `JsonMapper`, which
  refuses a non-null primitive missing from the JSON (`Cannot map null into
  type boolean`, `FAIL_ON_NULL_FOR_PRIMITIVES`): a non-null `Boolean` or
  `Int` added to a DSL type fails every live scenario reading that type
  until the backend sends it. So `SearchPage`, read by the search
  scenarios alone, holds `near`, and `BookPage`, read by the catalogue's,
  does not (measured 2026-11-11, the backend on `v0.18.0`).
- `/actuator/health` answers `{"groups":["liveness","readiness"],
  "status":"UP"}`, not the bare status.
- UNIMARC field 105 `$a` is fixed-length coded data whose form-of-contents
  codes sit at positions 4 to 7, `t` marking a comic strip; a record without
  the field names no form. The language an ouvrage was translated from is
  field 101 `$c`, in the BnF's own three-letter codes (`jpn`, `kor`, `chi`),
  which are not the ISO 639-1 codes the `LANGUAGES` map of `BnfSource`
  answers for the `language` field.
- UNIMARC field 330 `$a` is the summary, as the BnF holds it: HTML markup
  escaped in the XML (`&lt;p&gt;`), so `UnimarcRecord.value` hands it over
  as `<p>`, `<br/>`, `<b>`, `<i>`, with entities of the markup left as
  written (`&amp;`, `&nbsp;`, `&#231;`). `summaryOf`, in `BnfSummary.kt`,
  is the one cleaning: markup dropped before its entities are decoded, and
  only `&amp;`, `&lt;`, `&gt;`, `&quot;`, `&apos;`, `&nbsp;` and numeric
  references decoded; any other named entity, a numeric reference that
  names no code point (`&#1114112;`, more digits than an `Int`), and one
  that names a character PostgreSQL cannot store (`&#0;`, a surrogate
  `&#xD800;`), stays as written. A paragraph break is
  `</p>` then `<p…>`, with or without whitespace between them. Mont Tombe
  (9782344008980) and Le Larousse du Pain (9782035973689) are the
  recordings with a 330 worth cleaning; 9782371025219's needs none.
- A migration that reaches a source is a Kotlin class named as the SQL
  files, `V013__summary_for_stored_editions` extending `BaseJavaMigration`
  (which reads version and description from the name), with
  `@Suppress("ClassNaming")` on it and on its test; a `@Component` of
  `bibliography.infrastructure.persistence`, since Flyway's own scan builds
  without arguments and Spring Boot hands every `JavaMigration` bean to
  Flyway with its ports injected. Its SQL runs on `context.connection`,
  through `JdbcClient.create(SingleConnectionDataSource(context.connection,
  true))`, never a `JdbcClient` or `DataSource` bean (they wait for Flyway).
  Its slice test calls `migrate` with a `Context` of its own whose
  connection is `DataSourceUtils.getConnection(dataSource)`, the test's
  transaction. A JDBC slice context holds no such bean, so it resolves no
  `V013`: without `CleanSchema` it fails at start on a database a scenario
  migrated (`Detected applied migration not resolved locally: 013`), and a
  scenario context fails on one a slice migrated (`Detected resolved
  migration not applied to database: 013`); measured 2026-11-01 with
  `V014`. `CleanSchema` on both annotations is what lets them share the
  database.
- A source's mapping never throws on the text a source sends: what it
  cannot read stays as written or is left out. An exception that leaves a
  source fails the whole lookup, the other sources' answers included, and
  a migration reading through the same mapping would refuse the start.
- A name row shared by many parents is inserted or matched in one statement:
  `INSERT INTO author (id, name) VALUES (:id, :name) ON CONFLICT
  ((BTRIM(name)) COLLATE ignoring_case_and_accents) DO UPDATE SET name = …
  RETURNING id`. The conflict target is the expression the unique index
  carries, not the column; and `DO UPDATE` is what makes `RETURNING`
  answer the row that already exists, where `DO NOTHING` answers no row at
  all and costs a second statement. `JdbcEditionRepository`'s author
  statement, `RESPELL_OR_INSERT_AUTHOR`, sets `name = CASE WHEN EXISTS
  (SELECT 1 FROM contribution WHERE contribution.edition_id = :editionId
  AND contribution.author_id = author.id) THEN EXCLUDED.name ELSE
  author.name END`: an author the edition does not reference yet keeps the
  spelling that created the row, so an edition naming it otherwise reads
  back with that stored spelling; in `update` it respells an author the
  edition already references, whatever the role, so `update` resolves its
  author ids before it deletes the edition's contributions; `insert` runs
  the same statement and never respells, since an edition not yet stored
  has no contribution rows. Authors (since `V015`) and series (since
  `V014`) are unique on `((BTRIM(name)) COLLATE ignoring_case_and_accents)`,
  so case, accents and the spaces at the ends do not count.
  `JdbcSeriesRepository.respellOrFindOrInsert` is that statement with
  `SET name = CASE WHEN series.id = :ownSeriesId THEN EXCLUDED.name ELSE
  series.name END RETURNING id, name`, answering the stored series whole,
  and `findOrInsert` calls it with `ownSeriesId = null` (a null parameter
  there is typed by the comparison, PostgreSQL accepts it). With the
  collation inside the inner parentheses,
  `ON CONFLICT (((BTRIM(name)) COLLATE …))` or, for an index on the bare
  column, `ON CONFLICT ((name COLLATE …))`, PostgreSQL infers no index
  (`there is no unique or exclusion constraint matching the
  ON CONFLICT specification`); the collation goes after the expression.
  Two names of one edition and role that the index matches but
  `Contributions.of` does not (`Jo Nesbø`, `Jo Nesbo`) meet one author id:
  the contribution insert's `ON CONFLICT (edition_id, author_id, role) DO
  NOTHING` keeps the first link.
- An edition writes only its `series_id`, which references `series`: a slice
  case inserting an edition with a `seriesVolume` stores its series first,
  hence `JdbcSeriesRepository` in the constructor of
  `JdbcEditionRepositoryTest` and `JdbcCatalogueEditionsTest`. A use case reading an edition's series
  name takes `SeriesRepository` (`SeriesInMemory` in `bibliography.fixture`,
  names matched with `lowercase()` alone, unlike the index: no accents, no
  trimming; its `respellOrFindOrInsert` respells only the own series).
- `JdbcClient`'s `query(…)` has `single()`, `optional()`, `list()` and
  `set()` (`JdbcCarriedSeries`), no `singleOrNull()`: a finder of one row
  that may be missing answers `.optional().orElse(null)`; `single()` throws
  `EmptyResultDataAccessException` on no row.
  `query(String::class.java).list()` answers a `List<String?>`, which a
  port's `List<String>` refuses: a column read alone maps itself,
  `query { rs, _ -> rs.getString("name") }` (`JdbcAuthorNames`).
- `ResultSet.getInt` and `getLong` read a null column as `0`. A nullable
  integer is read with `rs.getObject("page_count", Int::class.javaObjectType)`,
  which answers `null`; the mutation to `getInt` reds a case asserting an
  absent page count only if some case of the class leaves it absent.
- A unique column that is nullable counts no null in PostgreSQL, so any number
  of editions without an `isbn13` sit side by side while two editions cannot
  share one.
- The test JVM caches every Spring context it builds until it ends, one per
  distinct configuration, and each context with a datasource holds a Hikari
  pool of ten connections; the sandbox PostgreSQL takes 100. With one
  context per JDBC slice class, each with its own `@Import`, T142's ninth
  slice failed the gate with `FATAL: sorry, too many clients already`
  (2026-11-08). Every JDBC slice test now shares one context: the suite
  starts six, four with a pool (the JDBC slices, `ScenarioTest`,
  `StoredSummaryScenarios`, `LibrisApplicationTest`), 40 connections at
  peak. An annotation or a property added to one slice class alone makes a
  context of its own, with ten more connections.
- `@JdbcSliceTest` lives in `fr.amory.libris.fixture` beside `FreshSchema` and
  `WebSliceTest`, and serves both contexts. It imports every `Jdbc…` adapter
  of `infrastructure.persistence`, so a new adapter joins its `@Import` and a
  slice test carries no `@Import` of its own. A slice test takes the
  adapters it uses through an `@Autowired` constructor, with `JdbcClient`
  beside them only when a case queries through it: detekt's
  `UnusedPrivateProperty` fails a constructor argument no case reads.
- `JdbcClient` expands an empty collection parameter to `IN ()`, which
  PostgreSQL refuses (`BadSqlGrammarException`): a finder over a list of ids
  answers `emptyList()` without a statement when given none, and a slice case
  (`no edition finds no copy`, `no id finds no edition`) proves it.
- `awaited_cover` is keyed by `edition.isbn13` and references it: a slice
  case inserts the edition before its awaited cover, hence
  `JdbcEditionRepository` in the constructor of
  `JdbcAwaitedCoverRepositoryTest`.
  The port reads with `findAll`, the chosen source back through
  `CoverSource.of`; that class still queries `JdbcClient` for the label stored.
- An `UPDATE` moves a row to the end of a PostgreSQL scan, so a finder
  without `ORDER BY` answers an updated row last. `awaited_cover.arrival`, an
  identity column of `V010`, is the order of `JdbcAwaitedCoverRepository.findAll`;
  the insert names no new column.
- `EditionsInMemory.insert` appends without looking at the ISBN, so a use
  case that inserts a held edition a second time fails at
  `editions.stored.single()` ("List has more than one element") before any
  later assertion of the case runs. A changed edition goes through
  `update`, which replaces the edition of that id, and
  `JdbcEditionRepository.update` writes the whole aggregate, its
  contributions deleted and inserted again.
- The application's `Clock` is the bean `systemClock` on
  `LibrisApplication`, UTC; the scenario harness's `MutableClock` (bean
  `clock` of `StubbedSources`) is `@Primary` over it. A use case takes
  `Clock`, never `MutableClock`; its plain tests pass a `MutableClock` from
  the root `fixture`.
- A `Bookshelf` takes `createdBy` and `createdAt`, a `Copy` `addedBy` and
  `addedAt`. A test that does not look at them builds through the
  `library.fixture` helpers: `bookshelfOf(id, name, memberships)` (the
  first member as creator), `bookshelfOwnedBy(reader)` and
  `copyOn(bookshelf, editionId)` (the bookshelf's creator as adder), all
  dated with `MutableClock`'s default instant, so a use case built on a
  fresh `MutableClock()` stores what they build.
- `ReaderRepository` has a second test double besides `ReadersInMemory`:
  `ReadersLosingTheRace`, private to `WelcomeReaderTest`. A method the port
  gains fails `compileTestKotlin` there until that double implements it.
- `TransactionsObserving` and `Transaction` live in the root
  `fr.amory.libris.fixture`, shared by both contexts' use-case tests.
- A use case called inside another's block (`HoldEdition` inside
  `AddBookToBookshelf`'s, `EditEdition` inside the add's since T158)
  opens a block of its own, which Spring's
  `TransactionTemplate` joins to the caller's. `TransactionsObserving` would
  record it as a second transaction: the caller's test builds the callee over
  `withoutTransaction()`, so the one block recorded is the caller's, and the
  callee's own test proves its block.
- `InventaireStubs.picturePaths()` answers the URLs of the picture requests
  served, `pictureRequests()` their count; `knowsButThePictureFails(isbn)`
  serves the entity, then `500` on the picture; `knows(isbn, picture,
  mediaType)` serves the picture under the given `Content-Type`,
  `image/webp` by default, and `knowsWithEntityServedAs(isbn, mediaType)`
  the recorded entity under one.
- The cover worker, `ExecutorCoverWorker` built by `WorkerConfig`, runs
  `FetchAwaitedCovers` on one virtual thread of its own, at every start of
  a context of the whole application (the scenario classes,
  `LibrisApplicationTest`; never a web or JDBC slice), every
  `libris.worker.every` (`LIBRIS_WORKER_PERIOD`, `P1D` by default) and
  after each add answering a copy. Runs queue, never overlap:
  a run that outlives its case (S3's waits a second on a picture that never
  comes, *S10 …, not yet stored* gives up on Open Library's one-second
  timeout) delays the next case's run by up to that second. An add of an
  edition the house already holds inserts no wait, so its run asks nothing
  unless another wait exists. A run dates each awaited cover it takes with
  the clock's instant before asking its source, and passes by one attempted
  within the day of the clock (`AwaitedCover.isDueAt`). A throw in one
  edition's fetch, normalisation or storing ends that edition's take, its
  attempt dated, and the run goes on to the next; it logs `Fetching the
  awaited cover of <isbn> failed` at WARN with the exception. Each run logs
  `Fetching the awaited covers` at INFO. A class logs through kotlin-logging,
  `private val logger = KotlinLogging.logger {}` and `logger.warn(e) { … }`,
  never SLF4J's `getLogger`. `LibrisApplicationTest` imports
  `CleanSchema`, which cleans before the start migration, since its run at
  start would ask the real sources for whatever a scenario left awaiting.
  With no chosen source the run asks inventaire.io, Open Library, then the
  BnF's SRU and covers address, so a scenario counting a source's requests
  resets them after its Given (`BookshelfScenarios`' S4, `aStoredCover`).
  `CoverWorkerObserving`, in `bibliography.fixture`, records what its
  `observe` answers at each waking.
- The BnF's fetch asks `libris.sources.bnf-covers-url`
  (`LIBRIS_BNF_COVERS_URL`, the stub's `/couverture` in the scenarios) with
  the record's ark, read from the same SRU search the lookup sends; the BnF
  answers `500` `text/html` for an ark without a cover, never a blank picture
  (seen 2026-10-04). The lookup's candidate keeps the public
  `catalogue.bnf.fr` address, which *FastEntryScenarios* asserts.
  `BnfStubs.hasCover(picture, mediaType)` serves the picture under the given
  `Content-Type`, `image/jpeg` by default.
- Open Library's fetch and its lookup's candidate both build on
  `libris.sources.open-library-covers-url` (`LIBRIS_OPEN_LIBRARY_COVERS_URL`,
  the stub's `/b/isbn` in the scenarios), so a test expecting the Open
  Library candidate's address expects the stub's (`OpenLibrarySourceTest`
  builds it on the server's base URL), never `covers.openlibrary.org`.
  `?default=false` makes Open Library answer `404`
  for a missing cover instead of a blank picture.
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
- In Kotlin, a bookshelf name's sameness ignoring case and accents is
  `BookshelfName.isSameAs`, over a private `folded` in `BookshelfName.kt`;
  `Contributions.kt` holds a second private folding of its own for the
  authors' names, the same without `œ`/`æ` and with the spaces at the ends
  dropped (`trim(' ')`, as `BTRIM` drops the space and leaves a tab).
  Both leave `Ø` whole where the index matches it (see the name-row item).
  `BookshelfName` is not
  `Comparable` and its `equals` stays exact. No Kotlin code sorts a list
  the screen shows. The folding: `Normalizer` NFD, the `\p{Mn}`
  marks dropped, `lowercase()`, then `œ` and `æ` spelt `oe` and `ae`,
  compared as a plain `String`, so spaces and hyphens still count (`À lire`
  is not `Alire`). It is not the catalogue's collation: NFD leaves letters
  with a stroke whole (`Ø`, `Ł`), so `Øre` is not `Ore`, where
  `ignoring_case_and_accents` would match them (measured both, 2026-10-11).
  `java.text.Collator` at `PRIMARY` (French or root locale) ignores spaces:
  it answers `0` for `One piece` against `Onepiece` (measured on JDK 25,
  2026-10-11), which is why it was set aside.
- `Contributions` holds a `Set`: it is equal whatever the order it was
  given, and iterates in no promised order. A kotest collection matcher
  takes `contributions.toList()`, since `Contributions` is an `Iterable`,
  not a `Collection`. The scenario DSL's `Card.authors` is a `List` in the
  answer's order: a scenario with more than one author compares it with
  `shouldContainExactlyInAnyOrder`.
- A row comparison `(a, b) > (c, d)` with a nullable member answers `NULL`
  when it meets one: the keyset of `JdbcCatalogueEditions` spells the
  nullable tome out (`IS NULL`, `IS NOT DISTINCT FROM`) instead.
- `JdbcCatalogueEditions.findFirstOfEachSeries`'s semi-join is planned as a
  `HashAggregate` on `copy.edition_id`, so before the sort the rows come in
  the hash order of their ids, not in the order inserted, and with the few
  rows of a slice case the sort happened to keep that order among equal
  keys (PostgreSQL promises no stable sort): a case whose red needs a
  missing key to pick the wrong edition fixes the ids (`LESSER_TWIN` came
  out before `GREATER_TWIN`), or it passes or fails by the draw of fresh
  ones (measured 2026-10-15).
- `JdbcCatalogueEditions` builds the catalogue's `FIND_PAGE` and the
  search's two statements, `FIND_PAGE_OF_ISBN` (a query `Isbn.of` reads)
  and `FIND_PAGE_OF_WORDS` (any other), from four `const` parts (`RANKED`,
  which carries `isbn13`, `search_text` and `search_vector`,
  `HELD_BY_READER`, `HELD_BY_READER_AFTER`, which is it plus the keyset,
  `IN_CATALOGUE_ORDER`) and one `pageOf`; `searchOf` picks the statement.
  Each statement selects `id` and `near`, which `pageOf` reads (the page is
  near when one of its rows is; the catalogue's and the ISBN's say `FALSE`).
  The words' statement is CTEs: `found`, the editions the words find among
  the reader's whole catalogue (`HELD_BY_READER`, no keyset), then
  `matched`, `found` `UNION ALL` the near ones, asked `WHERE NOT EXISTS
  (SELECT FROM found) AND WITHOUT_ACCENTS(:words) <%
  WITHOUT_ACCENTS(ranked.search_text)`; the keyset and the order apply to
  `ranked JOIN matched`, so near editions page as the found ones do.
  The words' clause hands the query's text to `TO_TSVECTOR` through
  `words_ignoring_case_and_accents` and builds one `tsquery` of its
  lexemes, `STRING_AGG` over `UNNEST` joined by ` & `, each lexeme a
  prefix, `QUOTE_LITERAL(lexeme) || ':*'`, and a lexeme of digits alone
  (`~ '^[0-9]+$'`, inside a `CASE` so no other lexeme is cast) that prefix
  or the tome's token, `('02':* | '#2')`, cast to `numeric` and back to
  `text` so `02` is tome 2 (`integer` overflows on twelve digits, `bigint`
  on 24, a `500`). Never the text to `TO_TSQUERY`, whose syntax `&`, `!`,
  `:` and parentheses break (`syntax error in tsquery`). A query of no
  word gives `STRING_AGG` no row, `NULL`, which matches nothing. The
  `tsquery` is an `InitPlan` the GIN index serves. A `const` string
  template cannot reference a `const`
  declared below it (`Variable 'RANKED' must be initialized`), so the
  parts sit above their users. The fake
  `CatalogueEditionsAnswering` records a catalogue page as `asked` and a
  search as `searched` (`SearchAsked`), both answering the one `answer`.
- `edition.search_text` (`V016`, `NOT NULL`) is written by
  `JdbcEditionRepository`'s `INSERT_EDITION` and `UPDATE_EDITION`, through
  the `const` part `SEARCH_TEXT` above them both, from the title, the
  subtitle, the name of the series its `series_id` names, read in the
  statement, and the contributions' names, the parameter `authorNames`
  that `write` sets. A respelled author or series is not rewritten into
  its other editions' text: a respell changes only what the search
  configuration ignores. A raw `INSERT INTO edition` in a test must give
  it.
- `edition.search_vector` is generated from `search_text` and, for an
  edition with a tome, one lexeme `#` then the number (`'#2'`), through
  `ARRAY_TO_TSVECTOR`, which refuses a `NULL` element, hence the `CASE`.
  `V016` created it and `V017` changed it with `ALTER COLUMN … SET
  EXPRESSION`, which rewrites every row and rebuilds the GIN index. The
  parser drops a typed `#` (`#2` reads `'2'`), so only the words' clause
  reaches that token. Right after a bulk write, the GIN index's pending
  list makes the planner scan `edition` instead: on 10 000 editions, 243
  pending pages gave a `Seq Scan`, and `SELECT
  gin_clean_pending_list('edition_search_vector')` (or autovacuum) gave the
  `Bitmap Index Scan` back, so a timing taken just after seeding measures
  the scan (2026-11-12).
- Flyway 12.4's clean drops no text search configuration, but drops each
  extension the user owns with `CASCADE`, which takes the configuration
  built on `unaccent`'s dictionary and the generated column using it, so
  `V016` re-runs on every cleaned schema. A configuration depending on no
  extension (a plain `COPY = simple`) survives the clean, and the next
  migration fails on `duplicate key value violates unique constraint
  "pg_ts_config_cfgname_index"`; drop it by hand through `jshell` (seen
  2026-11-11).
- PostgreSQL's default parser reads a word of letters as `word`
  (`asciiword` when ASCII alone) and a word joining letters and digits as
  `numword` (`Pokémon2`, `Été85`), hyphenated ones as `hword`/`numhword`
  with their parts `hword_part`/`hword_numpart`/`hword_asciipart`, a word
  of letters, digits and a dot as `file` (`Café2.1`) and an address as
  `email` (`été2@x.fr`); a mapping that leaves out a type keeps that
  type's accents, so `words_ignoring_case_and_accents` puts all nineteen
  types `simple` maps through `unaccent, simple`, `unaccent` handing a
  token without accents to `simple` as it is. `ts_debug`'s `lexemes` shows
  `unaccent`'s output alone (`Café` reads `{Cafe}`); `TO_TSVECTOR` is the
  truth (`'cafe'`). A query's word that is a prefix of the whole
  hyphenated word also matches it, so a case proving `hword_numpart` puts
  the digits in a part after the first (probed 2026-11-11). A word of
  digits reads as `uint` whatever its length (twelve digits,
  `978201210134`, or 24) and keeps its leading zeros (`02`); `n°2` reads
  `n` and `2` (probed 2026-11-11).
- `placing` is a reserved word of PostgreSQL: as a table alias it is a
  syntax error (`syntax error at or near "placing"`).
- A generated column and an index expression take only `IMMUTABLE`
  functions. `unaccent()` is `STABLE`, so a trigram index over unaccented
  text calls it through an `IMMUTABLE` wrapper function; `concat_ws` is
  `STABLE` too, so a generated column joins its texts with `||`.
  `to_tsvector` with its configuration given as a `regconfig` is accepted
  in a generated column, and so is `'#' || volume_number`, although
  `textanycat` is `STABLE`: a SQL function, inlined before the check
  (probed 2026-11-12). `edition.search_text` cannot be one: it carries
  the series' and the authors' names, held in other tables. Probed during
  the search grill, 2026-11-11. `V018`'s wrapper is `without_accents(text)`,
  whose body is SQL-standard, `RETURN UNACCENT('unaccent', $1)`: on
  PostgreSQL 18 an index build runs with `search_path` restricted to
  `pg_catalog, pg_temp`, so a wrapper with a quoted `AS $$ … $$` body
  fails `CREATE INDEX` (`function unaccent(unknown, text) does not
  exist`), while a SQL-standard body binds `unaccent` and its dictionary at
  creation and depends on the extension, so Flyway's clean drops it with
  `unaccent`'s cascade (probed 2026-11-12). A quoted body with `SET
  search_path` is not inlined, so the index's expression would not match
  the clause's.
- `<%` is pg_trgm's word similarity (the query against the nearest extent
  of the text) at `pg_trgm.word_similarity_threshold`, 0.6 by default;
  Libris sets no threshold. Both sides go through `WITHOUT_ACCENTS`:
  *asterik* to the *Astérix* texts is 0.75, 0.375 with the accents kept.
  The slice cases answering none nearest to the threshold are *one piece
  tome 2* (0.588) and *serpe loup* (0.545): a fixture or a threshold that
  moves them past 0.6 turns them near. The plan names
  `edition_search_trigrams` (`Index Cond: (without_accents(search_text) %>
  …)`) with `SET enable_seqscan = off` (2026-11-12).
- The full text search never matches an ISBN typed with its hyphens. A
  query that reads as a whole ISBN, `Isbn.of` answering one (hyphens and
  spaces dropped, an ISBN-10 converted), is answered from `isbn13` alone,
  never from `search_vector`. Probed during the search grill, 2026-11-11.
