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

## 2026-09-19 — T033 The bookshelf created with the reader — done
- Did: `Bookshelf`, `Member`, `MemberRole` and `BookshelfRepository` in
  `domain`, `V002__bookshelf.sql` with `JdbcBookshelfRepository`, the reader's
  default column and `ReaderRepository.update`, and `FirstVisit`, the bean
  whose one `@Transactional` method welcomes a reader Libris has never seen
  with the bookshelf they own. Eight cycles, the gate green, the six
  `BookshelfScenarios` tests still skipped.
- Decided:
  - **`FirstVisit` is the transaction boundary and `ReaderVisit` keeps the
    race.** The brief named both traps and both held: a boundary must be a
    bean's method called from another bean, and PostgreSQL aborts the
    transaction on the unique violation, so the `DuplicateUsernameException`
    catch and its second `findByUsername` stay in `ReaderVisit`, outside.
    `ReaderVisit` is left plain: find, delegate, catch.
  - **The reader's default is a nullable column and a nullable field with
    `= null`.** It keeps every existing `Reader(...)` construction site out of
    the diff, which is what keeps `infra.web` untouched; `me` builds its own
    `CurrentReaderResponse`, so the new field reaches no answer.
  - **One join query, grouped in Kotlin, ordered by the bookshelf's name then
    the member's reader id.** The order the slice test asserts is the order the
    SQL states, never the one the database happens to give.
  - **`update` writes the whole row.** One statement sets the four columns, so
    the new case of `JdbcReaderRepositoryTest` proves both that the other
    columns survive and that `findByUsername` maps the new one.
- Deviations from the brief: three, all of order, none of substance.
  `JdbcReaderRepository.update` was written at step 1, where the port's new
  method forced it to compile, though its test only arrives at step 5. Step 4
  was run as four cycles, one case and one commit each, the migration whole in
  the first. Steps 1 to 3 leave `LibrisApplicationTest` and the scenario
  classes red — `FirstVisit` wants a `BookshelfRepository` bean that arrives
  with the adapter at step 4 — which the plan's order makes unavoidable.
- Left over: the readers stored before this task keep no default bookshelf and
  T037 makes the field required; the question went to `agent/PROPOSED.md` as
  the brief asked. The brief's advice for a migration edited after a run — run
  a slice class alone, it cleans and migrates — is false and cost a mutation
  check; the true shape of the trap is now in `agent/GOTCHAS.md`.

## 2026-09-20 — Domain rework with Tophe: two contexts, the aggregates renamed — done, on the T033 branch
- Did: the backend split into `bibliography` (`Isbn`, `Kind`,
  `Contribution`, `ContributionRole`, `SeriesEntry`; `domain.lookup` with
  `ExternalEditionLookup`, `ExternalLookupResult`, `EditionPreview` and its
  `merge`; `application.lookup` with `LookupEditionByIsbn` and
  `EditionLookupResult`; `BnfEditionLookup`, `OpenLibraryEditionLookup`,
  `IsbnController`) and `library` (`domain.reader` with `Reader`, `ReaderId`,
  `ReaderRepository`; `domain.bookshelf` with `Bookshelf`, `BookshelfId`,
  `Membership`, `MembershipRole`, `BookshelfRepository`; `ReaderVisit`; the
  two JDBC repositories, `MeController`, `SecurityConfig`), each with
  `domain`, `application`, `infrastructure`. Tests, fixtures and the ArchUnit
  rules moved with them, and the three scenario classes, which a task may
  not edit, changed in their fixture imports alone, the fixtures having moved
  into the contexts; `V002__bookshelf.sql` rewritten (never merged).
  Gate green: 114 tests, the six `BookshelfScenarios` still skipped.
- Decided, with Tophe:
  - **The bookshelf owns its memberships.** `Bookshelf(id, name,
    memberships)` with `Membership(readerId, role)`; `Reader(id, username,
    email, displayName, defaultBookshelfId)`.
  - **An aggregate takes its id.** `ReaderId` and `BookshelfId` are value
    classes with a `new()`; the use case mints them, so it holds both ids before
    either insert.
  - **`FirstVisit` folded into `ReaderVisit`** through an injected
    `TransactionOperations`: the welcome runs in `executeWithoutResult`, the
    find and the duplicate catch stay outside, which keeps both traps of
    2026-09-19 answered in one class.
  - **`SecurityConfig` lives in `library.infrastructure.web`**, not in a
    root package: `MeController` reads its authority constants and it reads
    `ReaderVisit`, which would have been a cycle between the root and the
    context.
  - **The reader keeps their default bookshelf.** Removed on a word of
    Tophe's, restored on his next: `Reader(id, username, email, displayName,
    defaultBookshelfId)`, the column not null. The reader is inserted
    before the bookshelf that is their default, so `reader.default_bookshelf_id`
    is `deferrable initially deferred` and a membership's reader is checked
    at the statement (swapped on the review of 2026-09-21, below). The
    invariant "the default is one of yours" spans two aggregates and stays
    out of the constructor: the use case that creates both guarantees it.
  - **The role has one source of truth, the Kotlin enum.** No `CHECK` on
    `membership.role`; the case that inserted `LENDER` through `JdbcClient`
    is gone with it.
  - **A blank name never reaches `Contribution` or `SeriesEntry`.** Both
    refuse one, so the BnF and Open Library clients leave out a contributor
    or a series whose name is blank, one case each over a hand-written
    record.
  - **Precedence between sources is the domain's** (Tophe, same evening):
    `Source { BNF, OPEN_LIBRARY }` in `bibliography.domain.lookup`, declared
    in order of precedence; the port names its source and
    `LookupEditionByIsbn` sorts its lookups by it. The `@Order` annotations
    and the boot test on the bean order are gone; the unit test gives Open
    Library first and still gets the BnF's title.
- Deviation from the T033 task line, decided with Tophe: `BookshelfRepository`
  reads a bookshelf by its id, not the bookshelves a reader is a member of.
  No use case of this phase reads them (`me` answers from the reader's own
  reference), so `findByReaderId` waits for the task that lists them;
  `findById` stays as the read that proves the insert, and the one the
  add-a-book route will call.
- Left over: `agent/TASKS.md` task lines T034–T039 still say `infra.*` and
  `SourceEdition`; `docs/PRD.md` §3 and `specs/bookshelf.md` still say
  *Member*; `Copy`, `CopyId` and `CopyRepository` of `library.domain.copy`
  arrive with the task that needs them. The naming rule
  *Bibliothèque de …* moved into `Bookshelf.ownedBy(owner, ownerName, id)`
  on Tophe's call the same evening; `ReaderVisit` only mints the two ids.
  Then, on his call too, a bookshelf without an OWNER membership is refused
  by the constructor; the bookshelf query joins its memberships inner.

## 2026-09-21 — Review of PR #113 with Tophe: the transaction proven, the deferred reference swapped — on the T033 branch
- Did: a deep review of the PR, then two pieces on its findings. The
  transaction of the welcome is proven by `ReaderVisitTest` over
  `TransactionsObserving`, a `TransactionOperations` fake that records what
  the in-memory stores held before and after each `execute`: one transaction,
  from nothing to the reader and their bookshelf; mutation-checked twice (the
  wrapper removed, one insert moved out), red both times. Then the deferred
  reference moved from `membership.reader_id` to `reader.default_bookshelf_id`,
  the welcome inserting the reader first, the slice tests inserting their
  readers before their bookshelves, and a new slice case: a membership of a
  reader Libris does not know is refused, red on the old schema. Then a
  blank `Remote-Name` falls back to the username as a missing one does, so
  no bookshelf is named *Bibliothèque de* nothing; one case in
  `MeControllerTest`, red with a 500 first. Then the ArchUnit application
  rule narrowed from the whole transaction package to its `support` package
  plus `TransactionStatus`: `@Transactional` on the private `welcome` stayed
  green under the old rule and is red under the new one. Then the blank-name
  rule written once per type: `Contribution.of(name, role)` and
  `SeriesEntry.of(name, volumeNumber)` answer none for a blank or absent name,
  the two clients `mapNotNull` through them and their five guards are gone;
  the constructors keep their `require` as last defence. A BnF contributor
  with a forename and a blank surname, dropped before, is now kept under the
  forename, a malformed record no test pinned. Gate green: 120 tests, 6
  skipped. Then D11 amended twice, with Tophe: an enumeration column carries
  no CHECK, and the children an aggregate owns sit in a table named after the
  child, `membership`, a join between aggregates keeping both sides' names.
- Decided, with Tophe: the reference checked at commit is the one crossed
  once per reader, the default; the membership's reader, which every later
  use case will write, is checked at the statement, in the slice too.
- Decided by Tophe on 2026-09-21: `V002` cannot run on a `reader` table that
  holds rows (the `not null` column has no default, proven against the
  sandbox PostgreSQL), and no backfill is written: the staging schema is
  dropped before this PR deploys. The `agent/PROPOSED.md` item of 2026-09-19
  is closed by it.
- Left over, from the review: sixteen commits of the branch carry the
  harness trailer and a session line instead of the trailer of `CLAUDE.md`,
  one revert none, and the two reverts have git's default subject. The
  branch is pushed, so its history stays; the squash commit on `main` takes
  the PR title and a message given explicitly to the merge call, with the
  one trailer.

## 2026-09-21 — T034 The house's edition stored — done
- Did: `Edition`, `EditionId` and `EditionRepository` in
  `bibliography.domain.edition`, `V003__edition.sql` with its `edition`,
  `series`, `author` and `contribution` tables, and `JdbcEditionRepository`,
  proven by the six cases of `JdbcEditionRepositoryTest`; the slice annotation
  moved to `fr.amory.libris.fixture`, where both contexts reach it. Seven
  cycles, the gate green, no use case and no answer of the API touched.
- Decided:
  - **The insert-or-match shape the brief suggested holds:** `on conflict
    (lower(name)) do update set name = <table>.name returning id`, the
    conflict target being the expression of the unique index. A `do nothing`
    would have needed a second statement, `returning` answering no row when
    the name already exists.
  - **`contribution.position` is written from the index in the list and read
    back with an `order by`**, so the aggregate read equals the one stored;
    the mutation to `order by author.name` reds two cases, the helper's two
    authors sorting against their order on purpose.
  - **A `series` or an `author` row keeps the spelling that created it**, so
    the second edition of the shared-names case reads back with the first's
    capitalisation. Pinned by the case, as the brief asked, rather than left
    to be discovered by the task that displays a name.
  - **The search columns of D03 are not in `V003`** (no `search_text`, no
    `search_vector`, no extension): no test of this phase exercises them and
    D03 puts the extensions in a migration of their own. Nothing of D03
    changes; the pull request says so for the reviewer.
- Deviations from the brief: the cases of steps 3 to 6 passed on their first
  run instead of failing. Step 2 requires the migration written whole in its
  cycle, and the adapter that satisfied the first case already covered the
  four that follow; each was proven to bite by a mutation of the code it
  claims instead, reverted and re-run green. The nullable unique column is the
  one exception, a migration already applied being unusable for a mutation
  check: it is proven by its case alone.
- Left over: `update` on `EditionRepository`, the `tag` table, the search
  migration and the promotion of the test's edition helper to a fixture
  package are in `agent/PROPOSED.md`. No task of this phase deletes an
  edition, so PRD §3's "when the last copy goes, the edition goes with it"
  waits for the task that removes a copy.

## 2026-09-21 — Review of PR #117 with Tophe: the contributions, one per author and role, in the house's order — on the T034 branch
- Did: the review's fix-ups, then a design pass Tophe led on the
  contributions. `Contributions` in `bibliography.domain`, built by
  `Contributions.of(list)` alone: one contribution per author and role, the
  author matched whatever the capitalisation of the name, the first spelling
  kept; ordered by `ContributionRole.order` (an explicit field, the
  declaration order not relied on) then by name, the name compared as one
  string. `Edition` and `EditionPreview` hold a `Contributions`, so the
  lookup dedups and orders by construction (`BnfEditionLookupTest`: a person
  listed under two function codes the house reads as one role is one
  contribution, red first) and the stored edition too. The `position` column
  of `contribution` is gone with the source's order: the house defines its
  own, and the read passes its rows through `Contributions.of`. `V003`
  edited before any merge, the sandbox database recreated. `Edition` refuses
  a blank title, as `Contribution` and `SeriesEntry` refuse a blank name.
  Then the reviewer's two suggestions: the slice case of the absent optional
  fields renamed for what it holds, and the `@JdbcSliceTest` gotcha taking
  `JdbcClient` only when a case reads it. D12 gained the two rules.
  detekt's `MagicNumber` ignores enumerations now (`ignoreEnums`), for the
  role's order. Tophe's second review pass: SQL keywords in upper case, the
  rule in D10 and the three repositories recased (V001 and V002 stay: applied
  migrations keep their checksum); `Contributions` iterates and answers
  `isEmpty()`, its list private, so a later `plus` dedups inside the type;
  the upserts of series and author are two named methods, the series
  resolved before the edition insert; `EditionTest` builds the edition with
  the title under test; the ISBN-less case asserts only the two rows. The
  `on conflict` on series and author stays: it folds names across editions,
  `Contributions.of` within one.
- Decided by Tophe: the order of the contributions is the house's, role then
  name, whatever a source's order; no recording names one person twice in
  one role, the one way it can happen is the BnF client folding every
  function code it does not know onto `WRITER`.
- Left over: a richer `ContributionRole` with a fallback role for the codes
  the house does not know; in `agent/PROPOSED.md`.

## 2026-09-22 — T035 The ouvrage added to a bookshelf — done
- Did: `library.domain.copy` — `Copy`, `CopyId`, `CopyRepository` —, the
  `copy` table of `V004` with `JdbcCopyRepository` and its slice, and
  `AddBookToBookshelf` reading a `NewBook` and answering an `AddBookResult`.
- Decided: the membership is tested in the use case, on the memberships the
  bookshelf already carries, and `Bookshelf` gained no method for it — the
  rule joins a reader and a shelf, it is not the aggregate's own; an unknown
  bookshelf and a bookshelf the reader is not a member of are one
  `NoSuchBookshelf`, as the brief asks.
- Review with Tophe (2026-09-22): the brief said *member* where the PRD says
  *owners add*, so the use case asks the aggregate — `Bookshelf.hasMember`
  and `Bookshelf.isOwnedBy`, the rule beside the memberships it reads — and a
  viewer is refused with a third result, `NotAnOwner`; a stranger still gets
  `NoSuchBookshelf`, a bookshelf being visible only to its members. The
  contract's POST answers no `403`, so where T037 maps `NotAnOwner` is in
  `agent/PROPOSED.md`. Same review: `Isbn.ofThirteen` back to private — a
  private method made public changes what the value type exposes, and no rule
  asked for it; `NewBook` carries `isbn: Isbn?`, the value type being the
  door, so `NotAnIsbn` and its two cases are gone and the `400` of a bad
  `isbn13` is the T037 controller's, checking the contract's pattern and
  mapping through `Isbn.of` as `IsbnController` does. Names follow the
  tree's: an id is `readerId`/`bookshelfId`, the aggregate is `bookshelf`.
- Decided: `add` is one `when` over three outcomes rather than guard clauses,
  detekt's `ReturnCount` allowing two returns and a third refusal being
  likely when the API arrives; the two inserts sit in a private `added(...)`,
  which is also the transaction block.
- Decided: the edition is matched by ISBN and, when the house lacks it,
  minted before the block, so the block holds the two inserts and nothing
  else; it is entered even when the edition was already held, which keeps one
  path at the price of an empty transaction.
- Deviations from the brief: the `reader` parameter of `add` arrived with the
  membership case (plan step 8) instead of with the first case (step 5) —
  detekt fails a parameter no case reads, so the signature had to wait for
  the case that motivates it. The delivered signature is the brief's.
- Left over: nothing of the API — the controller, the JSON of a copy and the
  `v0.6.0` pin are T037, which also un-skips the six `BookshelfScenarios`
  tests, untouched here. Two adds of the same unknown ISBN at once, which the
  unique `isbn13` makes the second insert throw, is in `agent/PROPOSED.md`.

## 2026-09-22 — T036 The lookup answers the house's edition — done
- Did: `LookupEditionByIsbn` (bibliography) looks in the house first through
  `EditionRepository.findByIsbn` and answers
  `EditionLookupResult.Held(id, preview)` without asking a source, the
  preview built by `EditionPreview.of(edition)` (null for an edition without
  an ISBN, which the lookup by ISBN never meets), the three other answers
  unchanged; `IsbnController` keeps its one mapping of a preview. `LookupIsbnForReader` in
  `library.application.lookup` answers `IsbnLookup(answer, copies)`: the
  bibliography's answer as is, plus, when held, the copies read through
  `CopyRepository.findByEditionId` (new) and `BookshelfRepository.findById`,
  kept when `Bookshelf.hasMember(readerId)`, as
  `CopyOnBookshelf(copyId, bookshelfId, bookshelfName)` ordered by the name
  of the bookshelf. Eleven cycles by the run, then reworked on Tophe's
  review, the gate green.
- Decided on review (Tophe): the run had followed D12's read-model rule to
  the letter — a `ReaderCopies` query port and a `CopyOnBookshelf` read model
  in `library.domain.lookup`, a `JdbcReaderCopies` adapter joining `copy`,
  `bookshelf` and `membership`, and an `IsbnLookupResult` mirroring
  `EditionLookupResult` with a thirteen-field `previewOf` in the use case.
  Too much for this read: the repositories serve it in a few queries and the
  membership rule already lives on the aggregate, so the SQL join was the
  rule written twice. D12 amended: a read that spans aggregates goes through
  the repositories and the aggregates' rules first, answered by a data class
  of `application`; a query port with its read model is for the read the
  repositories cannot serve, the catalogue search.
- Decided on review (Tophe): each context answers what it knows. Whether the
  house holds the ISBN is the bibliography's to say, its `Edition` being the
  bibliography's aggregate, so the house-first branch moved from the library
  use case into `LookupEditionByIsbn` — the task line had placed it in the
  library, and the run obeyed. The library's answer is a product,
  `IsbnLookup(answer, copies)`, not a second sealed class beside the
  bibliography's. The use case stays in the library, in its own
  `application.lookup` sub-package as the bibliography's; no root
  `application` package for one orchestrator.
- Lesson for the task lines: a line that names the package and the ports
  designs the inside; the run delivers it faithfully, wrong or right. The
  line says what the reader gets and which context answers; the brief and
  the run find the shape.
- Deviations from the brief: the `readerId` parameter of `lookUp` and the
  `EditionRepository` constructor argument arrived with the cases that read
  them (plan steps 10 and 9) instead of with the first case (step 6) —
  detekt fails a parameter or a constructor argument no case reads, the same
  wait T035 recorded. The delivered signature is the brief's.
- Left over: nothing of the API — the JSON of the copies and the pin bump
  are T037, which also un-skips the six `BookshelfScenarios` tests. The
  controller serving `LookupIsbnForReader` cannot sit in
  `bibliography.infrastructure.web`, the bibliography may never see the
  library (D02): T037 moves `/api/v1/isbn/{isbn}` to
  `library.infrastructure.web`. Nothing new in `agent/PROPOSED.md`.

## 2026-09-23 — A use case is called by its name — done
- Did: the four use cases expose `operator fun invoke` and are held in
  variables named after them, `lookupEditionByIsbn(isbn)`,
  `lookupIsbnForReader(readerId, isbn)`, `addBookToBookshelf(readerId,
  bookshelfId, book)`; `ReaderVisit` renamed `WelcomeReader`, the imperative
  of what it does. D10 gained the rule; the ports keep their verbs.
- Decided (Tophe, on the review of T036): `editionLookup.lookUp(isbn)`
  stuttered — the class was already the sentence. The T037 line rewritten
  to say what the reader gets and which context answers, the endpoint's
  move to the library, and the viewer answered `404` until a release adds
  `403`; no package or port named beyond what the contract fixes.

