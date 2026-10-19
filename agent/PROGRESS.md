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

## 2026-10-07 — Catalogue and covers done, with Tophe
- Did: Tophe checked the *Done* of `specs/catalogue.md` and `specs/covers.md`
  on the Pixel; both specs read done 2026-10-07, and the catalogue, web
  layer and covers sections of `agent/TASKS.md` are one line each under
  *Done*. Their entries moved to `agent/archive/progress-catalogue.md` (T042
  to T050) and `agent/archive/progress-covers.md` (T051 to T071, T068 and
  the sessions with Tophe between them).
- Decided: nothing.
- Deviations from the brief: none; no brief.
- Left over: the backlog is empty; the next feature waits for its spec,
  written with Tophe.

## 2026-10-14 — Bookshelves and the order on the screen done, with Tophe
- Did: Tophe checked the *Done* of `specs/bookshelves.md` on the Pixel on
  staging, the second account reached on a second port of the staging
  proxy. The spec is done; the Order on the screen (T086–T087) and
  Bookshelves (T072–T090) sections of `agent/TASKS.md` are one line each
  under *Done*, and the staging question left with D09's staging bullet.
- Decided: their entries moved to `agent/archive/progress-order.md` and
  `agent/archive/progress-bookshelves.md`, as the phases before them did.
- Left over: the backlog is empty; the next feature needs its spec,
  written with Tophe.

## 2026-10-16 — Series list done, with Tophe
- Did: Tophe checked the *Done* of `specs/series-list.md` on the Pixel on
  staging. The spec is done; the Series list section (T092–T099) of
  `agent/TASKS.md` is one line under *Done*.
- Decided: its entries moved to `agent/archive/progress-series-list.md`, as
  the phases before it did.
- Left over: the backlog is empty; the next feature, the series page, needs
  its spec, written with Tophe.

## 2026-10-16 — Icons done, with Tophe
- Did: Tophe checked the *Done* of T100 on the Pixel on staging. The Icons
  section (T100) of `agent/TASKS.md` is one line under *Done*.
- Decided: its entry moved to `agent/archive/progress-icons.md`, as the
  phases before it did; U07's wording, "the stand-in of a row's missing
  cover", landed with T100's pull request on Tophe's go.
- Left over: the backlog is empty; the next feature, the series page, needs
  its spec, written with Tophe.

## 2026-10-18 — T109 The bibliography holds the edition an ouvrage adds — done
- Did: `HoldEdition` in `bibliography.application.edition` answers the
  edition of a `NewEdition` (the held one of its ISBN, else a new one with
  its awaited cover, in one block); `NewBook` is that `NewEdition`;
  `AddBookToBookshelf` calls the hold inside its block, six parameters.
- Decided: `HoldEdition`'s constructor grew one parameter per cycle
  (`editions`, then `awaitedCovers`, then `transactions`), since detekt
  fails a constructor property no case reads yet. `NewBookRequest`'s local
  `book` and `NewEditionTest`'s `book` are `newEdition` too, D10's word
  for the type. The add builds and inserts the copy inside its block, after
  the hold, and reads it back from `execute`.
- Deviations from the brief: `NewEditionTest`'s two bodies rename their
  local `book` to `newEdition`, where the brief said bodies unchanged.
  An extra mutation beside the brief's: the hold called before the add's
  block reds step 8's transaction case.
- Left over: nothing new; T110 gives `HoldEdition` the series'
  find-or-insert.

## 2026-10-18 — T110 The series, an aggregate of the bibliography — done
- Did: `Series`, `SeriesId`, `SeriesVolume` and `SeriesRepository` in
  `bibliography.domain.series`, `JdbcSeriesRepository` beside the edition's;
  an edition refers to its series by `seriesVolume`, and the held ISBN's
  preview, the catalogue's page and the series list read it by id.
- Decided: `HoldEdition` reads the edition back from `transactions.execute`,
  wrapped in `checkNotNull`, since Spring's `execute` answers a nullable
  `T`. `JdbcEditionRepositoryTest` stores its two series in a `@BeforeEach`,
  after `FreshSchema`'s callback. The work spanned two runs, a handoff after
  step 7.
- Deviations from the brief: `findById` reads with `.optional().orElse(null)`,
  the tree's idiom, since `JdbcClient` has no `singleOrNull`; step 4's
  mutation was `.single()` in its place. Steps 12 and 13 add no case: step 7
  adapted their fixtures, so their guards are the brief's mutations alone,
  run on the existing cases. Steps 8 and 11 are guards, since step 7's
  green already kept the answered series and read the held preview's.
- Left over: nothing new; T101 and T102 hand the series' `id` to the series
  list and page.

## 2026-10-18 — T110 The series, an aggregate of the bibliography — reworked
- Did: `BrowseCatalogueTest`'s fixtures, `TINTIN`, `CIGARES` and `PIERRE`,
  moved below the class and its helpers in that order, as
  `ListCatalogueSeriesTest` has them; no case changed.
- Decided: the reviewer's verdict of 2026-10-18 on #288 settled that a
  fixture added to a file whose fixtures sit above the class moves all of
  them below it, by D10's top-down rule. No other file of the branch added
  a fixture above a class; the older files that keep theirs there are left
  as they are.
- Deviations from the brief: none.
- Left over: nothing new.

## 2026-10-18 — T101 The series page on v0.13.0, no such series — done
- Did: the backend pins `v0.13.0`; `GET /api/v1/series/{id}` is served by
  `SeriesController` through `FindCatalogueSeries`, which answers
  `NoSuchSeries` to every reader; each series of the list answers its id.
- Decided: `ApiContractTest`'s two series stubs share one fixture,
  `ASTERIX_IN_MY_CATALOGUE`, the series list's answer and the `Found` of
  the find, so the id step 5 adds is written once and the stub line stays
  within detekt's length.
- Deviations from the brief: the generated `200` of the new operation
  stayed red after step 3 and went green in step 5: `SeriesWithBooks` is
  `Series` plus `books`, and `Series` requires the `id` that
  `CatalogueSeries` gains in step 5, so `SeriesWithBooksResponse` gains its
  `id` there, beside `SeriesResponse`'s.
- Left over: T102 finds the series and its books behind the same use
  case, and removes the `@Suppress("UnusedParameter")` of its `invoke`.

## 2026-10-19 — T102 The reader opens a series — done
- Did: `FindCatalogueSeries` answers the series of the reader's catalogue,
  the summary the series list answers for it, with its editions on the
  reader's bookshelves, each with its copies there; S1 runs.
- Decided: the books come from the reader's copies grouped by edition id,
  so steps 4 and 5 were guards, not reds; S1 went green at step 3.
- Deviations from the brief: Tophe's fix-up after review. The find no
  longer calls `ListCatalogueSeries`, which read the reader's bookshelves,
  copies and editions a second time (9 queries, now 5): the summary rule
  is `catalogueSeriesOf`, an `internal` function beside `CatalogueSeries`
  that both use cases call, and the find answers `NoSuchSeries` when none
  of the reader's editions is of the series. Its first case gains a cover,
  a guard against the find passing a wrong first-of-each-series.
- Left over: nothing new; T103 moves the frontend once T102 is deployed.

## 2026-10-19 — T103 The series page on v0.13.0, no such series — done
- Did: the frontend pins `v0.13.0`; a series carries its id and the series
  port finds one by id, `SeriesPageApi` folded into it; `/catalogue/series/{id}`
  shows its skeletons, then *Série introuvable.*; S5 runs, both cases.
- Decided: `useFindCatalogueSeries` reads any answer as `notFound` until T104
  brings `found`, so step 9 is a guard; its `find` resets to `loading`
  first (fix-up). The view asks through a `watch` on `id`
  with `immediate`, as the brief's step 12 asks. `PageHintSkeleton` is a 14
  bar in a 20 line, `FieldRowSkeleton` two 14 bars at the ends of a card
  row, padded 14.
- Deviations from the brief: `aBook` moved to `EditionShapes.ts` before step
  3's red, which uses it; `bookOf.ts` written in step 3's green and
  `FetchCatalogueApi` moved onto it in its own refactor commit. S5 was
  un-skipped and its red read before any unit test (*Unable to find an
  element with the text: Série introuvable.*), then put back skipped,
  uncommitted, so its un-skip stays step 15. The run handed off after step
  9 and a second run finished it.
- Left over: T104 adds `found` to the composable and the page; T108 the
  failure and any other problem type.

## 2026-10-19 — T104 The reader opens a series — done
- Did: a series row is a link to `/catalogue/series/{id}`, which shows the
  series' name, *N albums dans vos bibliothèques.* and its editions in the
  order a shelf reads, each overlined with its tome alone; no chevron left.
- Decided: `booksInOrder` reads a missing tome as `Infinity`, and `byTome`
  answers 0 for two equal tomes, since `Infinity - Infinity` is `NaN` and
  two editions without a tome must reach the title. The hint is
  `seriesPage.hint` filled with `series.count.<kind>`, in a `computed` of
  the view. `SeriesRow`'s root is its `RouterLink`, wrapping the row: it
  holds no control, so no stretched cover is needed.
- Deviations from the brief: `SeriesRow.spec.ts`'s `show` builds its
  `screen` on `wrapper.element.parentElement`, since `within(root)` searches
  descendants only and the root is now the link; no assertion changed. S1
  was un-skipped and its red read before any unit test, then put back
  skipped, uncommitted, so its un-skip stayed step 14, as in T103. The run
  handed off after step 12 and a second run finished it.
- Left over: T105 the card of owned tomes and the missing rows; T106 the
  authors and publishers; T107 the band; T108 the failure.

## 2026-10-19 — T105 The owned tomes and the gaps — done
- Did: the series page reads, in a card under its header, the tomes or
  albums owned and missing as runs (*1 à 25, 36, 37, 50*), and lists a row
  per run of missing tomes in its place among the editions; S2 runs.
- Decided: `booksAndGapsInOrder` keys each gap by the tome after it,
  `last + 1`, and places it before the first edition of that tome, so two
  editions of one tome get one gap and an edition without a tome none.
  `runsOf` reads only the tomes 1 to the highest, so the owned and the
  missing runs come from one walk. The card's rows are padded 14 in a
  divided card, the loading card's box, rather than the 7 of
  `SourceEditionCard`'s rows, so nothing jumps; the row of a gap keeps
  `CatalogueRow`'s 48 by 74 cover block, dashed.
- Deviations from the brief: S2 was un-skipped and its red read before any
  unit test (*expected [] to deeply equal [ …(2) ]*), then put back
  skipped, uncommitted, so its un-skip stayed step 19, as in T103 and T104.
  The component specs read their rows through a `words` helper of their
  own, the scenario's, over the whole card or row.
- Left over: T106 adds the authors and the publishers to `SeriesCard`;
  T107 the band; T108 the failure.

## 2026-10-19 — T106 The authors and the publishers — done
- Did: the series page's card reads *Auteur(s)* and *Éditeur(s)* after the
  tomes' rows, each name once, translators left out, and is not drawn when
  no row is left; `authorsOf` and `publishersOf` in `Book.ts`; S3 runs.
- Decided: one helper, `firstSpelling`, maps each name to the first
  spelling the collator calls equal, over the books in `booksInOrder`; the
  authors then go through `authorsInOrder`, which joins their roles and
  sorts them, so steps 9 and 11 were guards. The 20 between card and list
  is the `gap-5` of a flex column holding both in `SeriesPageView`, so a
  card that draws nothing takes the space with it, no view rule.
- Deviations from the brief: S3 was un-skipped and its red read before any
  unit test (*expected … to contain 'Auteurs Jean-Yves Ferri, …'*), then
  put back skipped, uncommitted, so its un-skip stayed step 17, as in T103
  to T105. The running-app check of the spacing could not be made: the
  sandbox has no browser; it is left to Tophe on the Pixel.
- Left over: T107 the band; T108 the failure.

## 2026-10-19 — T107 The band of a series — done
- Did: the series page opens with the series' cover as a band above its
  name, named *Couverture de* and the series' name, and draws none when the
  series has no cover or its cover fails to load; S4 runs, its three cases.
- Decided: the band is an `img` of `SeriesPageView` itself, as the brief
  chose. It crosses the content's 20 of side padding with `-mx-5` and a
  width of `calc(100% + 2.5rem)` on the image, `max-w-none` lifting the
  preflight's `max-width: 100%`; the padding stays on `<main>`, so the
  blocks under the band are untouched. The view remembers the cover URL
  that failed rather than a flag, so the next series' own cover shows
  (step 4); the header's top padding reads the same `band` computed, 12
  under a band and 20 without.
- Deviations from the brief: S4 was un-skipped and its red read before any
  unit test (*expected undefined to be '/api/v1/covers/…'* and *Unable to
  find … "Couverture de Astérix"*; *no cover* was already green), then put
  back skipped, uncommitted, so its un-skip stayed step 5, as in T103 to
  T106. Step 1 drew the band under the condition of a cover, so step 2 is
  the guard the brief names.
- Left over: the crop, the 130, the full width and the 12 above the header
  are left to Tophe on the Pixel, the sandbox having no browser; T108 the
  failure.
