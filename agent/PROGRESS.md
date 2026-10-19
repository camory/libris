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
