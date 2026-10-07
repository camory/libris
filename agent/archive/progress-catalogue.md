# Libris — Progress log, Catalogue

The entries of `agent/PROGRESS.md` of the catalogue feature, T043 to T050,
done 2026-10-07, with the one of T042 before them, moved here on
2026-10-07 once what a run needs of them was in `agent/gotchas/`. Nothing
is appended here.

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
