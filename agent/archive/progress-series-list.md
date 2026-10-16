# Libris — Progress log, Series list

The entries of `agent/PROGRESS.md` of the series list, T092 to T099, done
2026-10-16, moved here on 2026-10-16 once what a run needs of them was in
`agent/gotchas/`. Nothing is appended here.

---

## 2026-10-15 — T092 The series list on v0.12.1, the thinnest answer — done
- Did: the backend pins `v0.12.1` and serves `GET /api/v1/series` from
  `SeriesController` through `ListCatalogueSeries`, which answers no
  series to any reader; S5 No series un-skipped and green.
- Decided: the reds of steps 1 and 2 are commits of their own, the
  controller their green, so the log reads the test plan step by step.
- Deviations from the brief: `listCatalogueSeries` joined the constructor
  of `ApiContractTest` in step 4, with `seriesAnswer()`, not in step 3:
  unread until the stub, detekt's `UnusedPrivateProperty` fails it.
- Left over: T093 finds the reader's series and their count behind the
  same use case, red on S1.

## 2026-10-15 — T093 The reader's series, each counted — done
- Did: `ListCatalogueSeries` reads the reader's bookshelves, the copies on
  them through the new `CopyRepository.findByBookshelfIds`, and their
  editions, and answers one series per name with its number of editions;
  S1 The reader's series un-skipped and green.
- Decided: the series are counted with `groupingBy { name }.eachCount()`
  over the editions `findByIds` answers, each once whatever the duplicates
  of the ids given, so no `distinct()` on the edition ids; the red of S1 is
  a commit of its own, as in T092.
- Deviations from the brief: none.
- Left over: T094 fills each series' kind, T095 its cover, behind the same
  read.

## 2026-10-15 — T093 The reader's series, each counted — reworked
- Did: `ListCatalogueSeriesTest`'s fixtures, `ROMANCE_DAWN` and `BAGGY`,
  moved below the class and its `bookshelfOf` helper, in the order the
  class reaches them; nothing else changed.
- Decided: nothing; the reviewer's verdict of 2026-10-15 asked what D10's
  top-down rule already required. The brief's model, `BrowseCatalogueTest`,
  predates that rule; the older files that put fixtures above their class
  are named in `agent/gotchas/backend.md`.
- Deviations from the brief: none.
- Left over: nothing beyond T094 and T095.

## 2026-10-15 — T094 The kind a series shares — done
- Did: `ListCatalogueSeries` answers each series with the kind every one
  of its editions shares, or none when two differ; T093's case now expects
  `MANGA`, and two unit cases add the BD series and the mixed one.
- Decided: the grouping pairs each edition with its series' name and keeps
  the editions (`groupBy` with a value selector), so the count is the size
  of a series' editions and the kind `map { kind }.distinct().singleOrNull()`
  over them, in a private `seriesOf` beside `invoke`; each step's green was
  the smallest (the constant `MANGA`, then the first edition's kind), so
  every case was red before green and none is a guard.
- Deviations from the brief: none.
- Left over: T095 picks each series' cover behind the same read.

## 2026-10-15 — T095 The cover of a series — done
- Did: `CatalogueEditions.findFirstOfEachSeries` finds in SQL the first
  edition of each series the reader holds, in the order a shelf reads, and
  `ListCatalogueSeries` answers each series with that edition's cover.
- Decided: one edition per series through `DISTINCT ON (edition.series_id)`,
  the partition the series row, the name being unique under `LOWER`; the
  tome, the title under `ignoring_case_and_accents` and the id order each
  series' rows, the reader's scope the semi-join of `findPage`, its SQL
  untouched. The use case reads the set once and picks the series' edition
  it names; `CatalogueEditionsAnswering`'s page defaults to empty.
- Deviations from the brief: step 4's case was green on arrival, the
  semi-join being planned as a `HashAggregate` on `copy.edition_id`, so
  that among equal keys the rows come in the hash order of their ids, not
  in the order inserted: without a title key the case passed or failed by
  the draw of fresh ids. Its two editions now carry `LESSER_TWIN` and
  `GREATER_TWIN`, the expected one the greater, and the case is red on
  every run without the title key and with the plain title. Step 6 was
  green on arrival for the same reason, the lesser twin coming first: it
  is a guard, as the brief foresaw, its mutation the id key `DESC`.
- Left over: T096 moves the frontend pin, once T095 is deployed.

## 2026-10-15 — T096 The series of the catalogue, none yet — done
- Did: the frontend pins `v0.12.1`; `/catalogue/series` shows the
  catalogue's header, five skeleton rows, then the empty state, through
  `FetchSeriesApi`, `useListCatalogueSeries` and `SeriesView`; S5 green.
- Decided: `SeriesRowSkeleton` centres its two lines on the cover, as the
  mockup *2-loading* draws them; `useListCatalogueSeries` reads any answer
  as empty, the brief's choice, until T097 lists.
- Deviations from the brief: S5 was un-skipped and its red read and
  committed right after the pin, before any unit test, as the implementer
  prompt orders, not last as the brief planned; it stayed red until step
  11 and its mutation (no empty state drawn) was run at the end as step
  12 planned.
- Left over: T097 lists, sorts and counts the series; T098 the failure,
  whose rejection `list()` does not catch yet; T099 the switch.

## 2026-10-16 — T097 The reader's series listed — done
- Did: `/catalogue/series` lists the reader's series by name in the
  catalogue's block, each `SeriesRow` its cover or the stand-in, its name
  and its count in albums or tomes; S1, S2 (three runs) and S3 green.
- Decided: the view's sorted list is a `computed`, `seriesByName`, over
  the `listed` state, as `HomeView`'s `memberships`; the block is the `ul`
  itself, carrying the card's classes, with no wrapping `div`, the list
  having no skeleton rows to append as the catalogue's has; `listed` holds
  the answer as given, `empty` when it is empty; no `{{ " " }}` between
  the name and the count, none being asked.
- Deviations from the brief: step 1's red was `findByText` on the name for
  all five runs, not `rows(screen)` for S1 and S2: the name is drawn
  nowhere before `SeriesRow`, so the first query of each fails first.
  A `style` commit after step 10 fixes a line a `sed` of mine half-joined.
- Left over: T098 the failure, `list()` still not catching; T099 the
  switch. The `: []` arm of `seriesByName` is never read (the template
  reads it under `listed` only), the branch coverage's one gap in
  `SeriesView.vue`.

## 2026-10-16 — T098 Libris unavailable on the series — done
- Did: `useListCatalogueSeries` catches the port's rejection into
  `{ status: "unavailable" }`, and `SeriesView` draws, in place of the
  list, `IconAlert` and *Erreur lors du chargement, veuillez réessayer plus
  tard.* from `series.error`; S6 green.
- Decided: the message is the catalogue's markup, a `p` in `text-body
  text-danger` with `gap-2`, in a `v-else-if` of its own between the list
  and the empty state, so the empty state keeps its `v-else`.
- Deviations from the brief: none. S6 went green with step 3, as the brief
  foresaw; step 4's guard was red under its mutation (the empty state drawn
  beside the message).
- Left over: T099 the switch, which un-skips S4.

## 2026-10-16 — T099 The reader switches the view — done
- Did: `CatalogueSwitch`, a `nav` *Vue du catalogue* of two `RouterLink`s,
  *Ouvrages* and *Séries*, the view shown on `accent` through
  `exact-active-class`, drawn by `CatalogueView` and `SeriesView` under
  their header; S4 green, the last scenario of the series list.
- Decided: the 20 between the switch and the list is the view's
  `class="mb-5"` on the component, falling through to its `nav`, so the
  component holds no outer spacing; the inset 2 is the `nav`'s `p-0.5`
  inside its 44, the halves filling the grid's two columns.
- Deviations from the brief: none. S4 went green with step 7, its last
  red; steps 4 and 5 were guards, each red under its mutation.
- Left over: Tophe's *Done* of the series list on the Pixel, the back
  button from the series to the ouvrages included.
