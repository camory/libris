# Libris — Progress log

Append-only. Newest entry last. One entry per loop iteration (or per human
session that changed something a future run must know).

An entry is the story of one task in about twenty-five lines, never more
than forty. It repeats nothing the pull request holds: what was built and
how it was verified are its body, and the diff is the diff. A fact a future
run must know goes in `agent/gotchas/`, not here. The entries of a
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

## 2026-09-25 — T042 The button *Chercher* beside the field — done
- Did: *Chercher* is the 50 square at the right of the field, on its line,
  8 apart, drawing the new `IconMagnifier` alone and named by its
  `aria-label`; while the lookup runs a 22 spinner takes the glyph's place.
- Decided: the magnifier is a circle of radius 6 at (10.5, 10.5) and a
  handle from (15, 15) to (20, 20) on the 24 grid, drawn like its siblings.
- Decided: the button drops `gap-2` and `text-button` with its word, since
  it holds one child and no text.
- Deviations from the brief: in step 2 `IconMagnifier.vue` was written
  before the view used it, so that the red was the case's assertion
  (no magnifier rendered) rather than the spec file failing to load; the
  brief allowed either red.
- Left over: the layout is Tophe's check on the Pixel, jsdom laying nothing
  out; the shared primary button, rewritten in `agent/PROPOSED.md`.

## 2026-09-26 — T043 The catalogue API on v0.7.0, the thinnest answer — done
- Did: the contract pin moves to `v0.7.0`; `GET /api/v1/books` answers the
  reader's catalogue in one page, `next` null, through `ListCatalogue` and
  the query port `Catalogue`, whose `JdbcCatalogue` builds
  `CatalogueEdition`s with their `CatalogueCopy`s from one query over
  membership, bookshelf, copy, edition, series, contribution and author.
- Did: `S3 The catalogue is empty` un-skipped and green; S1 and S2 stay
  skipped for T044 and T045.
- Decided: the rows of the one query are grouped by edition in Kotlin,
  authors from the distinct contributions and copies `distinct()`, since
  an edition with two authors on two bookshelves comes as four rows.
- Decided: the controller takes `after` as `UUID?` and does not read it,
  so a text that is not an id is refused as `/problems/validation` by the
  existing advice; the parameter is named `ignored` for detekt.
- Decided: the mapping helpers of the controller are `bookOf` and
  `copyOf`; two private `responseOf` overloads failed
  `UnusedPrivateMember` (`agent/GOTCHAS.md`).
- Guard: the empty-catalogue case was green on arrival; deleting the
  `WHERE membership.reader_id` line turned it red ("Unexpected elements
  from index 0"), then it was reverted.
- Deviations from the brief: none.
- Left over: the order of a shelf (T044) and pages of fifty with `next`
  (T045); the query has no `ORDER BY` until then.

## 2026-09-27 — T043 The catalogue API on v0.7.0, the thinnest answer — reworked
- Did: `ListCatalogue` composes the catalogue from three repositories:
  `BookshelfRepository.findByMember`, `CopyRepository.findByBookshelfIds`,
  `EditionRepository.findByIds`, one statement each, and answers
  `HeldEdition(edition, copies: List<CopyOnBookshelf>)` in
  `library.application.catalogue`. The port `Catalogue`, its read model in
  `library.domain.catalogue` and `JdbcCatalogue` are gone.
- Decided (review of 2026-09-27, Tophe): the catalogue is a use case
  composing aggregates, not a domain concept. It owns no rule: who sees a
  bookshelf is the bookshelf's membership, the order of a shelf a rule over
  the edition's values. A type with no invariant is a projection, so the
  read model is a DTO beside its use case. The brief's rejected
  alternative, `ListCatalogue` reading the three repositories, is the
  design; a list of ids goes in one statement, never a query per row.
- Decided: `HeldEdition` holds the `Edition` read through its repository and
  reuses `CopyOnBookshelf` of the lookup, the same copy-with-its-bookshelf
  shape; nothing is rebuilt from a join.
- Decided: the three `JdbcCatalogueTest` claims move to `ListCatalogueTest`
  over the fakes; the empty catalogue is not a use-case case (green on
  arrival, no mutation that the first case does not already catch) but the
  slice cases `no bookshelf finds no copy` and `no id finds no edition`,
  with S3 end to end.
- Deviations from the brief: the design above, by the review.
- Left over: T044's order and T045's page are rules `ListCatalogue` applies
  over `HeldEdition`, adding no domain concept (for the planner).

## 2026-09-27 — T044 The catalogue in the order of a shelf — done
- Did: `Edition.BY_SERIES_AND_VOLUME` orders editions by series name or
  title, then tome as a number with no tome last, then title; its text
  comparison ignores case and accents. `BrowseCatalogue` sorts with it.
- Did: `S1 The catalogue lists the house's editions` un-skipped and green.
- Decided: accents go through `java.text.Collator` for French at primary
  strength, private to `Edition.kt`, used for both the first key and the
  title; one comparison of text for the whole order.
- Decided: step 2's green was a plain `lowercase()`, so step 3 was a real
  red (`é` after `f`), not a guard; the collator replaced it there.
- Decided: `BrowseCatalogue` sorts its `HeldEdition`s with
  `compareBy(BY_SERIES_AND_VOLUME) { it.edition }`; `HeldEdition` has no rule.
- Deviations from the brief: none; the backlog line's "one query" and
  "tests against PostgreSQL" were superseded by the brief itself.
- Left over: two editions equal on every key keep the order of the copies;
  T045 makes the order total with the id and pages it.

## 2026-09-27 — T044 The catalogue in the order of a shelf — reworked
- Did: `EditionTest > the tomes of a series are ordered as numbers` sets
  tome 10 *Le vrai visage d'Arlong* against tome 3 *Une vérité qui blesse*,
  so neither the title nor the tome read as text passes it; the private
  collator of `Edition.kt` is renamed `IGNORING_CASE_AND_ACCENTS`.
- Decided (reviewer, 2026-09-27): the brief's proof of "tome 2 before tome
  10" must fail without the numeric tome; the old titles sorted in the
  expected order, so the title tie-break of step 6 passed it alone. Both
  mutants (tome presence only, tome as a string) are now red on it.
- Decided: the collator's name says what it ignores, as the reviewer
  suggested; the spaces it also ignores went to `agent/GOTCHAS.md`.
- Deviations from the brief: none.
- Left over: the reviewer's two notes on the brief, for the planner: the
  test plan should ask titles that run against the key under test, and
  D12's last bullet (the series name row "is what a filter or a sort by the
  name reads") must be settled before T045 pages over this in-memory order.

## 2026-09-27 — T045 The catalogue in pages of fifty — done
- Did: `BrowseCatalogue(readerId, after)` answers a `CataloguePage(held,
  next)` of fifty editions; `Edition.BY_SERIES_AND_VOLUME` ends on the id;
  `GET /api/v1/books` reads `after` and answers `next`. S2 un-skipped, green.
- Decided: the place of an `after` is read first, through
  `EditionRepository.findByIds(listOf(after))`; an `after` naming nothing
  answers the empty page before any other statement runs.
- Decided: the page after is every edition of the order greater than the
  place (`compare > 0`), so a place the reader no longer holds and an
  edition added before the place both follow from one rule.
- Decided: step 2's green named `next` only when an edition follows, so
  step 4 (exactly fifty) was a guard; its mutation (next whenever fifty are
  answered) was red on `next`. Steps 3, 5 and 6 were real reds: step 3's
  green looked for `after` among the reader's editions, which step 5 broke.
- Decided: `ApiContractTest` stubs the use case for any `after` with a
  matcher on the `UUID` inside `ReaderId`; the form is in `GOTCHAS.md`.
- Deviations from the brief: none.
- Left over: the cost of reading the whole catalogue per page, in
  `agent/PROPOSED.md`; the PR body quotes the brief on D12's series-name
  bullet for Tophe. T046 moves the frontend pin, T049 walks the pages.

## 2026-09-28 — T046 The catalogue tab, its page and the empty catalogue — done
- Did: the frontend pins `v0.7.0`; `FetchCatalogueApi` reads
  `GET /api/v1/books`; `useBrowseCatalogue` and `CatalogueView` show five
  skeleton rows, then the empty catalogue; a third tab leads to `/catalogue`.
  S3 un-skipped, green.
- Decided: the list block of the skeletons takes the card's
  `rounded-[14px] border`; the empty state spaces its icon and text with
  `gap-3`; the skeleton's text lines are 12/16/14/14 px with gaps of 6,
  74 px beside a 74 px cover.
- Decided: `FetchCatalogueApi` checks the status from its first cycle, so
  the refused-page case (400 on `after=not-an-id`) is a guard; its mutation
  (no status check) was red.
- Decided: guards proven by mutation and reverted: the refused page, the
  first page asked on each arrival (module-scope state, red alone with
  `expected [ null ] to deeply equal [ null, null ]`), spines for Catalogue
  (`IconBook` in place of `IconBooks`, red).
- Deviations from the brief: the route arrived in the active-tab cycle,
  not at the wiring step, since `aria-current` needs a matching route; S3
  went green there while its file still provided the port, and the wiring
  step's red was `vue-tsc` on `LibrisPorts` and S3 once the file's provide
  was dropped. The un-skip was committed first, red. The French-literal
  grep matches `CatalogueView.spec.ts`, which the brief asks for; checked
  with `':!*.spec.ts'`.
- Left over: the unhandled rejection of a failing port until T048, and two
  notes for the planner, in `agent/PROPOSED.md`.

## 2026-09-28 — T047 The catalogue listed — done
- Did: `useBrowseCatalogue` leaves `listed` with the first page's books;
  `CatalogueView` lists them, one `CatalogueRow` per book; the `row title`
  step joins `style.css`, the shared `src/fixture/Books.ts` arrives.
  S1 un-skipped, committed red first, green at the end.
- Decided: the parts of a row are kept apart in its text by an interpolated
  `{{ " " }}` between them, since Vue drops the newline whitespace between
  elements and Prettier reflows spaced inline spans onto lines of their own
  (tried and red); the space renders nothing in the flex column.
- Decided: the bookshelves line is one string built with
  `t("catalogue.copies", …, count)` and joined by commas, not `<i18n-t>`,
  since no part of it is styled apart and the whole line is `muted`.
- Decided: the list is a `ul` taking the skeleton block's classes, each
  `li` holding a row; the row itself stays a `div`, so it can be mounted
  alone in its spec.
- Decided: guards proven by mutation and reverted: *tome* for a BD
  (`isbn.card.series.${kind}`, red on *Astérix · tome 1*), the order of the
  answer (a sort by title, red on *Romance dawn* in the first item), a row
  leads nowhere (the title in `<a href="#">`, red with two links).
- Deviations from the brief: none.
- Fix-up with Tophe on 2026-09-29: the row follows the kind, *album* for a
  BD, in the spec, S1, the row and the docs; the *tome* question is closed.
- Left over: two items in `agent/PROPOSED.md`: the copy grouping shared
  with the card, the icon size for U07.
  T048 takes the failed page and its unhandled rejection, T049 the next page.

## 2026-09-28 — T048 Libris unavailable on the first page — done
- Did: `useBrowseCatalogue` catches whatever the port rejects with and
  leaves `{ status: "unavailable" }`; `CatalogueView` draws the alert icon
  and `catalogue.error` in place of the list. S4 on the first page un-skipped,
  green; the rejection that escaped `void browse()` since T046 is caught.
- Decided: the list of `CatalogueView` reads `v-else-if="state.status ===
  'listed'"` and the message takes the `v-else`: the `unavailable` member
  left the old `v-else` unnarrowed for `vue-tsc`, so the narrowing came with
  the state's cycle.
- Decided: the message carries no top margin, unlike `IsbnView`'s `mt-5`:
  it takes the list's place right under the header, which already spaces
  its content with `pb-3`.
- Deviations from the brief: the view change above landed in step 2's
  commit, since `vue-tsc` refused the tree without it.
- Left over: the next page and its failure, S2 and the next-page S4: T049.
  The shared message block of `IsbnView` and `CatalogueView` is not
  proposed: two copies, as the brief says, do not yet make a component.

## 2026-09-29 — T049 The next page — done
- Did: `useBrowseCatalogue.browse` asks the page after the rows listed with
  the `next` received, through `loadingMore`, and keeps the rows on
  `unavailable`; `CatalogueView` watches its last row with an
  `IntersectionObserver`. S2 and the next-page S4 un-skipped, green.
- Decided: `browse` asks on the first call or when `listed` with a next,
  and on nothing else: one rule covers the page on its way, the last page
  and the failed page, rather than one early return each.
- Decided: the view finds its last row as the `lastElementChild` of the
  `ul`, in a `flush: "post"` watch of the state, and moves the observer
  from the old row to the new one there; no function ref on the `li`,
  which Vue calls again on every patch.
- Decided: the message under the rows takes `mt-5` only when rows stand
  above it; on a failed first page it keeps T048's place under the header.
- Decided: guards proven by mutation and reverted: the last row alone
  (no `unobserve`, red with three targets), watching nothing once left (no
  `disconnect`, red with one target), no list on a failed first page (the
  list drawn for any `unavailable`, red on an empty `ul`).
- Deviations from the brief: step 9 was a guard, not a red: step 7's green
  already moved the observer off the old row, since watching the last row
  meant finding it on each render. Step 15 was red, as the brief allowed.
- Left over: nothing; the catalogue spec is complete on the frontend.

## 2026-09-29 — T050 The catalogue ordered and paged by the database — done
- Did: the port `CatalogueEditions` and `JdbcCatalogueEditions` answer a
  page of the reader's edition ids, ordered and cut by one keyset statement;
  `BrowseCatalogue` reads those editions and their copies alone
  (`findByEditionIds`). `BY_SERIES_AND_VOLUME`, the `Collator` and
  `findByBookshelfIds` are gone. S1 to S3 of the catalogue green unchanged.
- Decided: the order ignores case and accents through a nondeterministic ICU
  collation (`V005`), applied in a `NOT MATERIALIZED` CTE whose columns carry
  it into the `ORDER BY` and the keyset.
- Decided: the keyset reads the place of `after` from the edition table,
  whoever holds it, so an `after` no longer held continues after its place
  and one of no edition answers an empty page; the tome is spelled out with
  `IS NULL` and `IS NOT DISTINCT FROM`, since a row comparison answers
  `NULL` on a missing tome.
- Decided: an edition on two of the reader's bookshelves is kept once by an
  `EXISTS` over copy and membership, not by `DISTINCT`, which would need
  every `ORDER BY` key in the select list.
- Decided: guards proven by mutation and reverted: accents (`LOWER`), no
  tome last (`NULLS FIRST`), the last page (`page.size == size`), an after
  no longer held (the place joined to the reader's copies), an after of no
  edition, twins (id out of the keyset), an edition added between pages
  (`OFFSET`), and what the port is asked (`null` for after, 51).
- Deviations from the brief: step 12 also asks the page after tome one, so
  the `volume_number >` branch is motivated; step 21 was red on a missing
  bookshelf key rather than on an extra copy; `CopiesInMemory` gained
  `findByEditionIds` at step 18, the port needing it to compile.
- Left over: under ICU level 1, spaces and hyphens now count (`One piece`
  before `Onepiece`), where the JDK `Collator` ignored them; no scenario
  names it. A copy removed between the page and the copies would fail
  `getValue`; nothing removes copies yet.

## 2026-09-30 — T051 Backend: covers on `v0.8.1`, the thinnest answer — done
- Did: the backend pins `v0.8.2`, `v0.8.1` until the review; the lookup answers `id` and `covers: []`,
  the add stores a new edition with no cover address, the catalogue answers
  `coverUrl: null`. `GET /api/v1/covers/{name}` serves the file `{name}` of
  `LIBRIS_COVERS_DIR` as JPEG or WebP by its bytes, kept a year, else `404`,
  and `400` to a text that is not a cover name.
- Decided: `CoverName`, `Cover` and the port `CoverStore` in
  `bibliography.domain.cover`; `FindCover` in `bibliography.application.cover`;
  `FileCoverStore`, `CoversProperties` and `CoversConfig` in
  `bibliography.infrastructure.persistence`, as the brief placed them.
- Decided, on review with Tophe: the backend knows no picture format, the
  contract answering `image/*`. `Cover` is a media type and bytes: its
  constructor refuses a media type that is not an image and `Cover.of`
  answers null for one. The media type is saved beside the picture, in the
  file `<name>.type`; `FileCoverStore` reads both through `Cover.of`, a
  picture without an image's media type is no cover, and `CoverController`
  passes the type on. `CoverControllerTest` is gone: `ApiContractTest`
  proves the operation. The brief's `CoverFormat` and the reading of the
  first bytes are gone.
- Decided, on review with Tophe: `CoverController` is the bibliography's,
  in the new `bibliography.infrastructure.web`, since it calls `FindCover`;
  the brief had it in the library's web package. `Problems.kt` and
  `ProblemAdvice` moved to `shared.infrastructure.web`, beside the
  contexts, so both web packages answer the same problems;
  `WebSliceConfiguration` scans the three. `ArchitectureTest` gained *what
  is shared knows nothing of the contexts*, and its infrastructure rule
  lets a package depend on `shared`. `SecurityConfig` and its filter moved
  there too: the filter asks `RequestPrincipal`, an interface of `shared`,
  for the principal, and the library's `ReaderPrincipal` answers the reader
  `WelcomeReader` welcomes. `SecurityConfigTest` moved with the chain and
  stubs `RequestPrincipal`.
- Decided, on review with Tophe, after measuring by mutation what the
  contract and scenario tests catch without them: `IsbnControllerTest`
  keeps one case, the `id` of a held edition, which nothing else proves
  (the contract leaves `id` nullable). Its field-by-field case went, a
  swapped mapping being caught by `FastEntryScenarios`; its four refusals
  went, the verifier sending none of them: the EAN is `IsbnTest`'s, the
  separators `NewBookRequestTest`'s, and the ten digits and the trailing
  space are two plain cases of the new `Isbn13Test` on `isbn13Of`.
  `CatalogueControllerTest` keeps the null `coverUrl` of an edition that
  carries an old cover address, which nothing else proves, and leaves the
  presence of the field to `ApiContractTest`. The `PROPOSED.md` item on
  the refusals is closed.
- Left over, for the brief of the task that stores a picture: the media
  type is extracted before the store, the one of the picture as stored
  (after normalisation when it is taller than 600), and the use case builds
  the `Cover`, which checks it; the store writes the picture and
  `<name>.type`.
- Decided, on review with Tophe: a malformed name goes through
  `CoverName.of` in `CoverController` and never reaches `findCover` or the
  file system; it is `400` `/problems/validation` with
  `{field: "name", code: "not-a-cover-name"}`, on contract `v0.8.2`, keyed
  `400_NOT_A_COVER_NAME`, `ApiContractTest` its only proof. The brief had
  it `404`.
- Decided: guards proven by mutation and reverted: a name of any length
  (`{64}` dropped, red on 63 digits), bytes shorter than `WEBP`'s offset (the
  size check dropped, red on `RIFF` alone with an index out of bounds).
- Decided: `ApiContractTest`'s constructor reached seven parameters with
  `FindCover`; `@Suppress("LongParameterList")` on the class, as `NewBook.of`.
- Deviations from the brief: step 1 was also red on `404_NOT_MY_BOOKSHELF`
  (403), the stubbed `NewBook` no longer matching, as for the keyed `201`;
  step 4 made it green. Step 10 was green on its first run, step 9's
  offset check already refusing both; its mutation was run instead. Step 14
  also answered `404` to a name `findCover` does not find, so `404_NO_COVER`
  went green there rather than at step 17. The backend started without
  `LIBRIS_COVERS_DIR`, seen by `bootRun` without the variable: the
  binder keeps `${LIBRIS_COVERS_DIR}` as a literal path. The setting is
  `${LIBRIS_COVERS_DIR:}` and `LibrisApplicationTest` gained a case proving
  the start refused; Tophe dropped it and the binding case on review, tests
  of a setting's binding being unwanted. `FastEntryScenarios` *S1* gained
  `id` and `covers`, as the brief allowed.
- Left over: the `Dockerfile` must create the covers directory for the
  `libris` user before T053 writes (in `PROPOSED.md`); `Edition.coverUrl` and
  `cover_url` stay, read by the held lookup and by T057.

## 2026-10-01 — T052 Backend: the lookup offers the sources' covers — done
- Did: for an ISBN the house lacks, `LookupEditionByIsbn` asks inventaire.io
  (new `ExternalCoverLookup` port, `InventaireCoverLookup` at `100x600`) at
  once with the edition sources and answers `Found(preview, covers)`, the
  candidates in the order inventaire.io, Open Library, BnF; `/isbn` answers
  them as `covers` `{source, url}`, `coverUrl` the first; *S1* un-skipped.
- Decided: `Source` carries `coverOrder`; `CoverCandidates` sorts by it. A
  picture alone, no edition source knowing the ISBN, is an unknown ISBN. The
  house's edition asks no source, the cover lookup included.
- Decided: guards proven by mutation and reverted: a source without a cover
  (`coverUrl.orEmpty()`), the held edition asking no cover (cover lookup
  before the house), a picture alone (`Found` on an empty preview), no
  candidate means no cover (`?: preview.coverUrl`).
- Deviations from the brief: step 8 could not add the unused port parameter
  (detekt `UnusedPrivateProperty`), so the port and its wiring came at step
  9. Steps 4 and 5 were green on arrival; their mutations were run.
  `FastEntryScenarios` *S1* gained the BnF entry in `covers`, as allowed.
  *S1* met the edition *S10* had added (JUnit ran *S10* first): `FreshSchema`
  now cleans before each case, not each class, and `FreshSources` resets the
  stubs and journal before each case. `LibrisApplicationTest`'s defaults
  gained `inventaireUrl`.
- Left over (in `PROPOSED.md`): the previews' merge still orders by the
  declaration order of `Source`; the contract's `ONE_PIECE_1` offers
  inventaire.io at `480x600`; a possible cold-start `503` of
  `BookshelfScenarios` *S2* under the 1 s scenario timeout. D02 amendment
  naming the inventaire.io client and the cover port proposed in the PR.

## 2026-10-01 — T052 Backend: the lookup offers the sources' covers — reworked
- Did: `InventaireCoverLookup` catches `JsonNodeException` and takes the
  hash only from a non-blank text node, three new cases (an object, `null`
  and `""` as the claim); `Source` carries its name as `label`, read by
  `IsbnController`; the two candidate cases of `IsbnControllerTest` are gone.
- Decided, by the reviewer's verdict: an unreadable answer from inventaire.io
  offers no candidate, as a failure does, and a null or empty claim offers
  none rather than an address ending in an empty hash.
- Decided, by Tophe's review: the names handed out live on `Source`
  (`label`), not in the controller; *S1* and `FastEntryScenarios` *S1* are
  their proof, and renaming `INVENTAIRE`'s label to `Inventaire` reds *S1*
  (`expected:<["inventaire.io", …]> but was:<["Inventaire", …]>`), checked
  and reverted. The controller's two candidate cases repeated *S1* or fed it
  a state the use case cannot produce; removed. `INVENTAIRE` stays in
  `Source`; the D07 wording (schema cleaned before each case) is proposed in
  the PR body beside the D02 amendment.
- Deviations from the brief: none beyond the reviews.
- Left over (in `PROPOSED.md`): whether the cover sources get a type of
  their own beside `Source`, to discuss with Tophe before T053.

## 2026-10-02 — T052 Backend: the lookup offers the sources' covers — reshaped with Tophe
- Did: the ports are `EditionLookup` and `CoverLookup`, a source's answer
  `EditionSourceAnswer`; `Source` is split into `EditionSource` (`precedence`,
  the merge's order) and `CoverSource` (`label`, `order`); `Known` carries
  the source's own `CoverCandidate`, and `EditionPreview` loses `coverUrl`.
- Decided, by Tophe: two result types stay, one source's answer and the
  house's; the cover is not a field of the card, so each edition source
  answers its candidate beside its preview and the BnF's `coverOf` builds it.
- Changed on the way: a held edition's lookup answers `coverUrl` `null`
  whatever its stored `cover_url`, until T055 offers the stored cover.
- Left over: `Edition.coverUrl` and its column, null on every add since
  T051, for T053; `docs/ARCHITECTURE.md` D10 still names
  `ExternalEditionLookup.lookUp`, the wording is proposed in the PR body.

## 2026-10-02 — T053 Backend: the add keeps the chosen source — done
- Did: `CoverSource.of(label)` reads a source back from its name; the add's
  `coverSource` reaches `NewBook` and the edition the add creates; `V006`
  gives `edition` a nullable `cover_source`, written and read as the enum's name.
- Decided: the request maps the name through `coverSource?.let {
  CoverSource.of(it) }`, so an absent field and a name no source bears are
  both "no source"; no error is added to `validate()`.
- Decided: step 5's first red was a compile error (`Edition` had no
  `coverSource`); the behavioural red was read too, by having the use case
  pass `null`: `data class diff … Edition`, then reverted.
- Deviations from the brief: `EditionPreviewTest` builds two `Edition`s and
  gains `coverSource = null` too, a file the brief did not list. The guard of
  step 6 reds under the brief's mutation at `editions.stored.single()`, which
  the case already asserted, not at the new `coverSource` line; the port has
  no `update`, so no other mutation reaches a held edition's source.
- Left over: nothing; the worker reading the source is T066, `coverUrl`'s
  removal T065.

## 2026-10-02 — T053 Backend: the add keeps the chosen source — reworked
- Did: the three cases of several statements the task added gain the D07
  markers: `CoverSourceTest` *each source is found by its name* takes
  `// Given / When / Then`, and the two `coverSource` cases of
  `NewBookRequestTest` take `// Given` and `// When / Then`.
- Decided: the reviewer's verdict of 2026-10-02 amended nothing; its two
  findings were D07 unmet. The older unmarked cases of `NewBookRequestTest`
  stay as they are, as the finding excluded them.
- Deviations from the brief: none.
- Left over: nothing.

## 2026-10-02 — T053 Backend: the add records the awaited cover — reshaped with Tophe
- Did: the chosen source left `Edition`. An `AwaitedCover` (the ISBN, the
  chosen source or none) is inserted by `AddBookToBookshelf` beside a new
  edition that has an ISBN, in the add's transaction, through
  `AwaitedCoverRepository`; `V006` creates `awaited_cover` instead of the
  column on `edition`; `CoverSource` moved to `bibliography.domain.cover`.
- Decided (Tophe): nothing that reads an edition back needs the source,
  only the worker does, so the source belongs to the wait, not to the
  edition. The awaited cover is keyed by the ISBN alone; the table stores
  the source under the name the contract uses, written from `label`.
- Deviations from the brief: the brief describes the first shape and is
  kept as written. Two guards have no red of their own: the no-ISBN case
  (the type refuses an `AwaitedCover` without an ISBN) and the foreign key
  (a migration that has run cannot be mutated).
- Decided (Tophe): `CoverSource.order` stays on the enum, as D12 says. The
  T066, T057 and T058 lines are reworded to the awaited cover in this PR.
- Left over: D11 says a uuid key on every table and UPPER_SNAKE enum values
  in SQL; Tophe accepted both exceptions, their wording is proposed in the
  PR body for a docs PR.
- Decided (Tophe): only a stored picture ends the wait; a cascade that finds
  no picture is dated and tried again a day later, like a failed fetch (S7's
  note, the T057 and T058 lines).

## 2026-10-02 — T065 Backend: the house keeps a cover — done
- Did: `Cover.name` is the SHA-256 of the bytes; `FileCoverStore.write` puts
  the picture, then its `.type`, under that name; `Edition.coverName` replaces
  `coverUrl`, `V007` swaps `cover_url` for `cover_name`; the catalogue answers
  `/api/v1/covers/<name>`, null without one.
- Decided: `Cover.name` is computed once, at construction, with
  `HexFormat`, so step 2 was a guard from the start (its mutation read).
- Decided: step 5's first red was a compile error; the behavioural red was
  read by having the repository read `null` (`data class diff … Edition`).
- Deviations from the brief: step 6 has no commit of its own: `bare()` had
  to take `coverName = null` in step 5 to compile, so step 6 is the
  mutation alone, read and listed in the PR. Step 2's mutation also reds
  *a picture and the media type of an image are a cover*, whose bytes
  `1, 2, 3` hash to a name beginning with `0`.
- Left over: the worker that writes and records a cover, T066; the record of
  a name on an edition already stored needs a port write, T066's to shape.

## 2026-10-02 — T065 Backend: the house keeps a cover — reworked
- Did: the PR body's `ApiContractTest` line now says 15 cases, as the run's
  report gives them (`/me` 1, `/isbn` 5, `/books` 2, the add 4, `/covers` 3),
  not 17; no code changed.
- Decided: the reviewer's verdict of 2026-10-02 amended nothing; its one
  finding was a verification claim the run contradicted.
- Deviations from the brief: none.
- Left over: nothing.

## 2026-10-02 — T065 Backend: the house keeps a cover — fixed up with Tophe
- Did: `Cover.name` is computed when asked, so a cover read to be served is
  not hashed; `FileCoverStore.write` leaves a cover already stored as it is,
  so a write cut short never breaks a stored picture; the GOTCHAS item on
  the contract's case count names no number; the `CatalogueControllerTest`
  case on the stored cover's address is deleted; `Cover`'s constructor is
  private, `Cover.of` its only door, and tests build one with the fixture
  `coverOf`.
- Decided (Tophe): that case tested a mapping, which is not the
  controller's to prove; the address's form goes to the contract, a
  pattern on `Book.coverUrl` in `v0.9.0`.
- Deviations from the brief: `Cover.name` is no longer computed once at
  construction; the store no longer overwrites a stored cover (a first
  version moved each file into place whole, dropped for this simpler rule);
  step 7's case is gone, so the acceptance criterion naming
  `CatalogueControllerTest` holds for the null case alone.
- Left over: the address built in `CatalogueController.bookOf` has no test
  until the contract's pattern or S3 at T066.

## 2026-10-03 — Covers: the plan on contract v0.9.0 — with Tophe
- Did: `v0.9.0` of `camory/libris-api` is released (OpenAPI 3.1.0, a pattern
  on `Book.coverUrl`, `format: uuid` on the add's bookshelf id, the unknown
  ISBN `9782000000013`); T067 pins it on the backend, before T066; T060
  pins it on the frontend; the removal of the lookup's `coverUrl` becomes
  `v0.10.0`, pinned in T064.
- Decided (Tophe): the move to 3.1 and the new constraints change no byte
  of an answer, so they ship as their own release ahead of the removal; the
  backend pins it before the worker, so the cover address the catalogue
  answers is checked by the contract before covers are stored; the frontend
  never pins `v0.8.2`. D04 and D11 now say 3.1 and "which fields may be
  null", since `nullable` is gone.
- Deviations from the brief: none; no brief, a human session.
- Left over: nothing.

## 2026-10-03 — Web layer: testing and mapping rules — with Tophe
- Did: D07 says a value is tested where it is computed, its form is the
  contract's, the stubs of the Contracteer test answer any value the schema
  accepts, and a hand-written web test tests the class that decides, never
  a controller; D10 says a DTO of the web layer maps itself (`from` on a
  response); the tdd skill, the brief planner and the reviewer follow;
  T068 brings the backend in line, before Covers.
- Decided (Tophe): when the pin bump brings no red, the first red is in the
  test of the component computing the value, and a copied value gets none;
  the reading of the proxy's headers leaves the filter for a class of its
  own, so `MeControllerTest` has nothing left; one task, not split.
  Measured on `v0.8.2`: the keyed `200`s pass with stubs answering values
  unrelated to the examples (15 cases), and fail on a wrong status.
- Deviations from the brief: none; no brief, a human session.
- Left over: the GOTCHAS item on the web slice still says
  `library.infrastructure.web` is the only web package; T068's run meets it.

## 2026-10-03 — T068 Backend: the web layer as D07 and D10 say — done
- Did: `RemoteIdentity` in `shared.infrastructure.web` reads the proxy's
  headers, the filter builds its token from it; every response is built by
  `from` on its companion; three controller tests gone; `ApiContractTest`
  answers its own Astérix values. Two runs, a handoff after step 16.
- Decided: `READER_AUTHORITY` and `ADMIN_AUTHORITY` moved beside
  `ADMIN_GROUP` in `RemoteIdentity.kt`, still public; the role is computed
  inline in `CurrentReaderResponse.from`, no private `roleOf`; `IsbnResponse`
  keeps a private `from(id, preview, copies)` the two public ones share.
- Decided: the contract test's held copy sits on a random bookshelf id
  named `Grenier`; the found edition offers one candidate at an
  `example.org` address; the catalogue's edition has no cover, as before.
- Deviations from the brief: step 14 was a guard, not a red: detekt's
  `UnusedParameter` refused a `from` ignoring `authorities`, so step 13
  already read the role. Step 15's guard mutation, `displayName` from the
  username, was run in `RemoteIdentity.of`: in `CurrentReaderResponse.from`
  it leaves S1 green, which asserts the bookshelf name only. Two `style`
  commits fix `MaxLineLength` in `RemoteIdentityTest`, committed when a
  piped detekt hid its failure.
- Left over: D06 still names the filter as the class reading the headers;
  the PR proposes the wording. The criterion's grep on `copyOf` also
  answers `JdbcCopyRepository.copyOf`, a row mapper outside the web layer.

## 2026-10-03 — T068 Backend: the web layer as D07 and D10 say — reworked
- Did: `RemoteIdentity` and `RemoteIdentityTest` import `ISO_8859_1` and
  `UTF_8` from `kotlin.text.Charsets`, `CurrentReaderResponseTest` imports
  `READER` and `ADMIN` from `Role`; the three use the bare names.
- Decided: the reviewer's verdict of 2026-10-03 amended nothing; its
  blocking finding was D10's import rule, unmet on four added lines. The
  companion factories (`from`, `of`, `new`) stay qualified, their bare name
  saying nothing, as `MissingNode.getInstance()`. Its suggestion on the two
  `style` commits asks for nothing: history is not rewritten.
- Deviations from the brief: none.
- Left over: as in the entry above; D06's wording is Tophe's to settle.

## 2026-10-03 — T068 Backend: the web layer as D07 and D10 say — fixed up with Tophe
- Did: each controller file reads from the controller down, its responses
  in the order reached, constants last; `ApiContractTest` composes its
  stubs into five named steps, fixtures below the class.
- Decided: with Tophe, a D10 bullet: a file reads from the top down (the
  newspaper metaphor and the stepdown rule, Robert C. Martin), a long
  function or a test's setup is composed (Compose Method, Kent Beck).
- Left over: the rest of the tree predates the bullet; it is applied to
  the files a task touches.

## 2026-10-03 — T067 Backend: the API on `v0.9.0`, the contract in OpenAPI 3.1 — done
- Did: `ApiContractTest` pins `v0.9.0`; its unknown ISBN stub is keyed on
  `9782000000013`, and the catalogue's edition holds a stored cover, so the
  address `BookResponse.from` builds is checked by the contract's pattern.
- Decided: the stored cover's name is a fixed 64-digit hexadecimal literal,
  distinct from `NO_COVER`, as the brief asks; no production file changed.
- Deviations from the brief: none.
- Left over: whether `ProblemAdviceTest`'s case on a bookshelf id that is
  not a uuid may go, now that the contract varies it too, is Tophe's
  (`agent/PROPOSED.md`). The frontend's pin and `9782000000006`: T060.

## 2026-10-03 — The gotchas split by side, with Tophe
- Did: `agent/GOTCHAS.md` became `agent/gotchas/every-run.md`,
  `backend.md` and `frontend.md`, each read whole with the Read tool; the
  prompts, `CLAUDE.md`, `docs/LOOP.md` and `agent/README.md` name them.
  Then every item was checked against the tree: 120 became 117.
- Decided: a file per side, since 48 KB no longer fit one Bash read (the
  CLI answers a 2 KB preview past about 30 KB) and T067's implementer read
  lines 1 to 200, missing *Backend tests*. `ProblemAdvice` stays: Spring's
  `spring.mvc.problemdetails.enabled` needs four `messages.properties` keys
  to give the same bodies (measured on the T067 branch).
- Left over: `FixedReaderHeaders` could register for the error dispatch too,
  so a missing stub reads `500`; not decided. The implementer prompt still
  names `api/` as a side the proof hook records, a directory that no longer
  exists.

## 2026-10-03 — T066 Backend: the worker's run fetches the chosen inventaire.io cover — done
- Did: `FetchAwaitedCovers` takes each awaited cover whose chosen source is
  inventaire.io, fetches the picture through `CoverFetch` (implemented by
  `InventaireCoverLookup`), stores it, names it on the edition and ends the wait.
- Decided: `JdbcEditionRepository`'s `seriesIdOf` and `authorIdOf` became
  one `nameIdOf(statement, name)`, detekt's `TooManyFunctions` refusing a
  twelfth function. `FetchAwaitedCovers` carries `@Service`, as `FindCover`.
  `findAll` drops a row whose ISBN `Isbn.of` refuses. The edition is read
  before the transaction, its update and the delete inside it.
- Decided: two runs, a handoff after step 12; steps 13 to 16 in the second.
- Deviations from the brief: step 11 was green on arrival: step 8 already
  used the constructor's `transactions` (detekt's `UnusedPrivateProperty`
  refuses an unread one), so it is a guard, its mutations in the PR body.
- Left over: running the use case, S3 and S4 un-skipped (T069); the other
  sources' fetches (T054); the D02 sentence on
  `bibliography.infrastructure.lookup` is proposed in the PR, Tophe's to settle.

## 2026-10-03 — T066 Backend: the worker's run fetches the chosen inventaire.io cover — reworked
- Did: `InventaireCoverLookup` answers no picture when the entity or the
  picture comes with a malformed `Content-Type`, and no cover for a picture
  served empty; a case serves the picture as `image/jpeg`.
- Decided: the reviewer's verdict of 2026-10-03 amended nothing; its
  blocking finding was the brief's "fails to serve is no cover" and "each
  … is taken", unmet. `InvalidMediaTypeException` is caught beside
  `RestClientException` in both requests, the lookup's included, since the
  fetch goes through it; the use case keeps no catch of its own, the port
  answering `null` for a failure. The empty body keeps the wait, as S7's
  "only a stored picture ends the wait" reads.
- Decided: the `TooManyFunctions` trap is in `agent/gotchas/backend.md`;
  the entry above says "a twelfth function", it was the eleventh.
  `b90ba59` mixing a refactor with its cycle is reported, not rewritten.
- Deviations from the brief: none.
- Left over: the brief's notes, the adapter's failure modes and D11's
  "one SQL statement", are the planner's; as in the entry above otherwise.

## 2026-10-03 — T066 Backend: the worker's run fetches the chosen inventaire.io cover — fixed up with Tophe
- Did: the second verdict's two blocking findings, on Tophe's ask. A picture
  claim `RestClient` cannot read as an address (`{x}`, `50%zz`) is no cover:
  `pictureAt` catches `IllegalArgumentException`, which covers
  `InvalidMediaTypeException` too, so that catch left it. `pictureAt` moved
  below `pictureOf` and `entities`, in the order it is reached. Tophe's IDE
  edit (`_` catch names, `toEntity<ByteArray>()`, `body<JsonNode>()`) is a
  commit of its own before them.
- Decided: the claim is still read as a URI template; no case asks for
  `URI.create`. The gotchas item on `RestClient` now names the template
  trap and says only `InventaireCoverLookup` catches these.
- Left over: the lookup's candidate address carries an unchecked claim to
  the reader as it is; as in the entries above otherwise.

## 2026-10-03 — T066 Backend: the worker's run fetches the chosen inventaire.io cover — reviewed by Tophe
- Did: `InventaireCoverLookup` is `InventaireSource`, its bean
  `inventaireSource`, its test `InventaireSourceTest`; the guard is
  `the picture is asked at 100x600`, the size it asserts.
- Decided: `CoverLookup` and `CoverFetch` stay two ports: different callers
  (the lookup, the worker), different answers (a candidate, a cover),
  different implementers (inventaire.io alone offers a cover without an
  edition; every chosen source fetches). An adapter is named after its
  source; the siblings follow in their fetch tasks, in `agent/PROPOSED.md`.
- Left over: as in the entries above.

## 2026-10-03 — T069 Backend: the add wakes the worker — done
- Did: an add that answers a copy wakes `CoverWorker` (bibliography's
  application port) after its transaction; `ExecutorCoverWorker` runs
  `FetchAwaitedCovers` on Spring Boot's `applicationTaskExecutor`. S3, S4 un-skipped.
- Decided: the `Executor` is injected by type, unqualified: the booted
  application resolves it to `applicationTaskExecutor` with no ambiguity.
- Deviations from the brief: the scenario un-skip was the first cycle, as
  the implementer prompt asks (S4 red, S3 green on arrival), not steps 6
  and 7; their guards' mutations were still checked last.
- Left over: runs one at a time, at start and daily (T059); the other
  chosen sources (T054); the D02 bullet on `bibliography.infrastructure.worker`
  is proposed in the PR, Tophe's to settle.

## 2026-10-04 — T054 Backend: Open Library as the chosen source — done
- Did: `OpenLibraryEditionLookup` is `OpenLibrarySource`, which implements
  `CoverFetch` too, at `libris.sources.open-library-covers-url`;
  `FetchAwaitedCovers` runs the fetch whose `coverSource` the awaited cover
  chose. S5 *Libris serves a stored cover* un-skipped and green.
- Decided: the fetch's answer is turned into a `Cover` the way
  `InventaireSource.pictureAt` does it, written again rather than shared:
  two copies, and their catches differ (Open Library catches
  `RestClientException` alone until T058 sets the malformed answers).
- Deviations from the brief: none. Step 9's mutation was written as an
  elvis around `firstOrNull`, since `?.fetch` after a non-null fallback
  fails the compile under warnings-as-errors.
- Left over: the BnF's fetch and the cascade (T057), which iterate the
  same list of fetches; the D02 sentence on `bibliography.infrastructure.
  lookup` is proposed in the PR, Tophe's to settle; `BnfEditionLookup`
  keeps its name until its fetch (`agent/PROPOSED.md`).

## 2026-10-04 — T054 fixed up with Tophe
- Did: `OpenLibrarySource.fetch` let `InvalidMediaTypeException` out on a
  malformed `Content-Type`, which stopped `FetchAwaitedCovers` before the
  awaited covers after it; a case reds it. Every cover fetch and
  inventaire.io's lookup now read through `nullOnFailure` of
  `SourceHttp.kt`, one catch of `Exception` answering `null`, in place of
  each adapter's list of exceptions.
- Decided with Tophe: `CoverFetch.coverSource` stays, the adapter naming the
  source it serves as `EditionLookup.source` does; one catch-all at the
  source boundary rather than lists that miss the next unknown type. The
  diary entry above on Open Library's catches is superseded.
- Did, second ask: the lookup's Open Library candidate is built on the
  covers setting too, the fetch's address, and the `COVERS` constant is
  gone; *S5 Merged answer, from Open Library alone* and two
  `OpenLibrarySourceTest` expectations now read the stub's address.
- Did, third ask: `LibrisApplicationTest`'s *the sources are configured
  with their defaults* deleted, a binding test from T013 that every new
  source setting extended; the sources' defaults show at `bootRun` and
  deploy, no test carries the public addresses any more.
- Left over: the BnF and Open Library lookups keep their lists
  (`agent/PROPOSED.md`).

## 2026-10-04 — S10's Given with Tophe
- Did: the brief planner blocked T055: *S10 A held edition offers its own
  cover* stores its cover through the worker's Open Library fetch, and its
  `noSourceWasAsked()` counted that fetch, since the stubs' request logs
  are cleared only before each case. The Given is now `aStoredCover`,
  which stores a cover and then clears the three stubs' request logs.
- Verified: S10 un-skipped with only `noSourceWasAsked()` in its Then is
  green with the clearing, red without it (1 request); restored skipped.
- Decided with Tophe: keep the real fetch in the Given and clear the logs,
  rather than writing the picture and the edition's cover name by hand.

## 2026-10-04 — T055 Backend: a held edition offers its own cover — done
- Did: `Held` carries the edition's `coverName`; the ISBN answer offers it
  as the one `Libris` candidate at the cover operation's address, and
  `coverUrl` is the first candidate's url, set once in the shared `from`.
  S10 *A held edition offers its own cover* un-skipped and green.
- Decided: the address is `coverPathOf(CoverName)`, a top-level `internal`
  function in its own file `CoverPath.kt` of `library.infrastructure.web`,
  as `isbn13Of` sits in `Isbn13.kt`; `BookResponse.from` reads it too.
- Decided: `ApiContractTest`'s held stub and the catalogue's edition share
  one constant, `ASTERIX_1_COVER`, the name the catalogue already used.
- Deviations from the brief: none.
- Left over: the frontend's *Already there* card is T063. The path is
  still spelled twice: `CoverController`'s mapping in
  `bibliography.infrastructure.web` and `coverPathOf` in
  `library.infrastructure.web`; the infrastructure rule forbids the
  library's web from reading the bibliography's.
- Reviewed by Tophe: the web layer is one adapter of one contract, split by
  context only by the package rule. Decided with him, as a PR of its own
  right after this one: every controller and DTO, and the whole of
  `shared`, move to one package `fr.amory.libris.web`; `shared`
  disappears, `RequestPrincipal` goes, `coverPathOf` sits beside
  `CoverController`, and the package rules of ARCHITECTURE are rewritten
  with him.

## 2026-10-04 — One web package, with Tophe
- Did: every controller and DTO of both contexts, and the whole of
  `shared`, live in `fr.amory.libris.web`, one sub-package per path of the
  contract (`me`, `isbn`, `bookshelf`, `catalogue`, `cover`) and two for
  what every operation uses (`problem`, `security`); `shared` is gone.
- Decided: the identity filter calls `WelcomeReader` itself;
  `RequestPrincipal`, `ReaderPrincipal` and its test go. `coverPathOf` and
  the private `COVERS` sit in `CoverController.kt`, the mapping reads it.
- Decided: `ArchitectureTest` gains *the controllers sit in the web* and
  *the contexts know nothing of the web* (kept, though the cycles rule
  already reds a context reading the web); the shared rule and its
  exception go. D02's package section and rules rewritten, D07, D10 and the
  tdd skill follow.
- Left over: `bibliography.infrastructure.worker` is still missing from
  D02's package list. `BookResponse` reads `web.isbn`'s author and series
  responses and `web.me` reads `web.bookshelf`'s `BookshelfResponse`.

## 2026-10-04 — T056 Backend: the picture is normalised — done
- Did: `Cover.normalised()` scales a picture taller than 600 to 600 tall,
  its proportions kept, as JPEG, and keeps any other as fetched, a picture
  the JDK cannot read included; `FetchAwaitedCovers` stores the normalised
  cover. S6, both cases, un-skipped and green.
- Decided: the scaled width is `width * 600 / height`, integer division,
  and the drawing is bicubic over an RGB image (no alpha, so `ImageIO`
  writes the JPEG); the writer's default quality is kept.
- Decided: the failure caught is `IIOException`, the one the broken JPEG
  of step 6 throws; nothing broader, since no case motivates it.
- Decided: the fixture's `pictureOf(format, width, height, type)` takes the
  image type, `TYPE_INT_RGB` by default, so CoverTest's alpha channel reads
  in the case, and checks what `ImageIO.write` answers.
- Deviations from the brief: none.
- Left over: a picture more than 600 times taller than wide scales to a
  width of 0 and throws (`agent/PROPOSED.md`); D02's rule 1 naming the
  JDK's image libraries is proposed in the PR, Tophe's to settle.


## 2026-10-04 — T056 Backend: the picture is normalised — reworked
- Did: the scaled width is at least one pixel, so a picture more than 600
  times taller than wide (1×1201) is answered as a JPEG 1×600 instead of
  throwing out of the worker's run; one `CoverTest` case proves it.
- Decided: the reviewer's verdict of 2026-10-04 amended nothing; its
  blocking finding was the brief's "a picture taller than 600 is normalised
  to 600 tall", unmet for such a picture. S6 had already decided it is
  scaled, so keeping it as fetched was no option; the width is
  `(width * 600 / height).coerceAtLeast(1)`, the nearest proportion a pixel
  allows. The `agent/PROPOSED.md` item that left it to the spec is removed.
- Deviations from the brief: none.
- Left over: the reviewer's note on where a deferred guard's mutation is
  recorded is the planner's; D02's rule 1 wording is still Tophe's.

## 2026-10-04 — The commit trailer, with Tophe
- Did: `.claude/settings.json` sets `attribution.commit` to the trailer
  `CLAUDE.md` names, and `attribution.sessionUrl` to `false`. Claude Code's
  default trailer names the model (`Claude Opus 5.5`), so every loop commit
  since the model pin carried it, T055's and T056's included.
- Verified: in the sandbox image (Claude Code 2.1.283), `claude -p` making
  an empty commit in a clone of this branch writes `Co-Authored-By: Claude
  <noreply@anthropic.com>`; in a clone of `main`, `Co-Authored-By: Claude
  Haiku 4.5 <noreply@anthropic.com>`.
- Left over: the pull request line stays Claude Code's default.

## 2026-10-04 — Bookshelf S4's Given with Tophe
- Did: the brief planner blocked T057: both *S4 The ouvrage is already in
  a bookshelf* cases add One Piece 3 with no chosen source, then check
  that no source was asked; once T057's cascade runs for that add, its
  requests reach the stubs during the case. The Given now adds the book
  through `addedWithItsCover`: Open Library has the picture, the book is
  added with that source, the worker stores it, and the stubs' request
  logs are cleared. Sam's add, of an edition already held, waits for no
  cover. The fixture sends `coverSource` instead of the old `coverUrl`,
  and no longer a page count, which no case reads.
- Verified: with a stand-in cascade in `FetchAwaitedCovers` (an awaited
  cover with no chosen source asks every source), the old Given is red in
  both cases (1 and 2 requests), the new one green; stand-in reverted.
  `./gradlew check` green, 300 tests, 7 skipped.
- Decided with Tophe: a chosen source that answers, as S10's Given, rather
  than stubbing inventaire.io for the cascade, which would be red on
  `main` until T057; the page count leaves the fixture to stay within
  detekt's parameter limit.

## 2026-10-05 — UTF-8 header values pass the firewall, with Tophe
- Did: the T057 implementer blocked on S7 *none has it*: `Remote-Name: Ève`
  is refused with `400` before the When. Authelia writes the display name
  as raw UTF-8 bytes (fasthttp `SetBytesK`), Tomcat reads them as
  ISO-8859-1, and `È` (`C3 88`) becomes `Ã` and the control character
  `U+0088`, which `StrictHttpFirewall` refuses; `é` (`C3 A9`) passes. Any
  reader whose display name holds É, È, À, Ç… got `400` on every request.
  `SecurityConfig` declares a `StrictHttpFirewall` whose header values are
  checked after the UTF-8 decoding `RemoteIdentity` already applied
  (assigned, no control character), the recipe of Spring Security's
  reference; the decoding is one function shared by both. The web slice's
  client is now `SimpleClientHttpRequestFactory`, as the scenarios', since
  the JDK client sends `È` as `?`.
- Verified: staging backend, outside the proxy: `Léa` 200, `Ève` and
  `Élodie` 400. New `SecurityConfigTest` case red (`400`) then green; the
  guard *a control character in a header value is still refused* (`\u0085`)
  is red with `setAllowedHeaderValues { true }`, reverted. Tomcat refuses a
  C0 character itself, so `\u0007` proved nothing. `./gradlew check` green,
  302 tests, 7 skipped.
- Decided with Tophe: decode then check, for every header (the predicate
  does not know the header's name), rather than admit every value or an
  ASCII name in S7; the UTF-8 client for the whole web slice rather than in
  `SecurityConfigTest` alone. No connector setting of Tomcat or switch of
  Spring Security does it.
- Left over: T057's branch resumes at step 10 once this is on `main`.

## 2026-10-04 — T057 Backend: the cascade for an edition with no chosen source — done
- Did: an awaited cover with no chosen source is asked of every fetch in
  `CoverSource.order` until one brings a picture; the BnF fetches its
  picture by the record's ark; `V008` gives the editions stored before
  covers an awaited cover. S7's three cases are green.
- Decided: the BnF's public candidate and its fetch share
  `coverAddress(coversUrl, ark)` and `arkOf`; the candidate keeps
  `https://catalogue.bnf.fr/couverture`.
- Decided: `V008` checked by hand on four rows in a rolled-back transaction
  (an edition stored before covers, one named, one already awaited, one
  without an ISBN): only the first gains a wait.
- Deviations from the brief: `nameOf` and `publicationOf` left `BnfSource`
  for file-level private functions, since `fetch` and `pictureOf` would put
  the class at detekt's `TooManyFunctions` threshold (11). Steps 10 and 11
  were green on arrival and became guards.
- Blocked once, after step 9: *S7 …, none has it* was refused `400` on
  `Remote-Name: Ève` by `StrictHttpFirewall`; resolved by #196 with Tophe,
  merged into the branch, then steps 10–13.
- Left over: the BnF candidate on the covers setting, in
  `agent/PROPOSED.md`; S8 (T058) and S9 (T059).

## 2026-10-04 — T057 Backend: the cascade for an edition with no chosen source — reworked
- Did: `publicationOf` and `nameOf` sit below `BnfSource`, before the record
  types they read; the three cover fetches read their picture through one
  `RestClient.pictureAt(address)` in `SourceHttp.kt`; `BnfStubs.hasCover`
  takes a media type, and the BnF's fetch case serves `image/png`.
- Decided: the reviewer's verdict of 2026-10-04 amended nothing; its
  blocking file-order finding and its three suggestions are all applied.
  `pictureAt` leaves `nullOnFailure` to each caller, since Open Library and
  the BnF already wrap their whole fetch in it.
- Deviations from the brief: `BnfStubs` changes (a media-type parameter,
  `image/jpeg` by default, so the scenarios stay unedited), as the review
  asked. `nameOf` and `publicationOf` stay out of the class: with `pictureOf`
  gone it holds nine functions, and the two would bring it to eleven.
- Left over: nothing new.
- Reviewed by Tophe: choosing the fetches an awaited cover asks is a rule of
  its own, so it leaves the use case for a domain type,
  `CoverFetches.of(list)` sorted by `CoverSource.order`, whose
  `askedFor(chosenSource)` answers the chosen source's fetch alone, or every
  one in order when there is none. It only chooses: asking the sources stays
  the use case's. Built in `FetchAwaitedCovers`' constructor, no bean;
  `CoverFetchesTest` holds its two cases.

## 2026-10-05 — a source's methods name their port, the BnF source split, with Tophe
- Did: `lookUp` and `fetch` read alike in a source that implements two
  ports, and `lookUp` meant an edition in `BnfSource` but a cover address
  in `InventaireSource`. The three port methods are now `lookUpEdition`
  (`EditionLookup`), `lookUpCover` (`CoverLookup`) and `fetchCover`
  (`CoverFetch`). `BnfSource.kt` held four concerns in a scattered order;
  it is now three files: `UnimarcRecord.kt` (the XML parsing and the two
  record classes, `UnimarcRecord.parse`), `BnfRecord.kt` (the BnF's reading
  of a record: `previewFor(isbn)`, `ark`, the field readers) and
  `BnfSource.kt` (the SRU search, the covers address, the two port
  methods). The reader tests moved to `BnfRecordTest`; the `coverOf`
  test became the `arkOf` test of the same three cases, the candidate's
  address staying asserted by `BnfSourceTest`'s lookups.
- Verified: `./gradlew check` green after each commit, 310 tests, 4
  skipped, as on `main`.
- Decided with Tophe: verb and object (`lookUpEdition`) rather than the
  answer's noun (`editionOf`); three files; one PR. Left as it was:
  `PUBLIC_COVERS` beside the injected covers URL, a change of behaviour
  for later; the two error styles, `Failed` and no cover.
- Left over: the domain types over a collection of ports
  (`CoverFetches`, `EditionLookups`), still to discuss.
