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
