# Libris — Progress log, Series aggregate

The entries of `agent/PROGRESS.md` of the series aggregate, T109 and
T110, done 2026-10-18, moved here on 2026-10-20 once what a run needs of
them was in `agent/gotchas/`. Nothing is appended here.

---

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
