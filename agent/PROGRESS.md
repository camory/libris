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

## 2026-10-20 — Series aggregate and series page done, with Tophe
- Did: Tophe checked the *Done* of `specs/series-page.md` on the Pixel on
  staging. The spec is done; the Series aggregate (T109–T110) and Series
  page (T101–T108) sections of `agent/TASKS.md` are one line each under
  *Done*.
- Decided: their entries moved to `agent/archive/progress-series-aggregate.md`
  and `agent/archive/progress-series-page.md`, as the phases before them did.
- Left over: the backlog is empty; the next feature, the series details,
  needs its spec, written with Tophe.

## 2026-10-23 — Edition page done, with Tophe
- Did: Tophe checked the *Done* of `specs/edition-page.md` on staging. The
  spec is done; the Edition page section (T111–T115) of `agent/TASKS.md` is
  one line under *Done*.
- Decided: its entries moved to `agent/archive/progress-edition-page.md`, as
  the phases before it did.
- Left over: the backlog is empty; the next feature needs its spec, written
  with Tophe.

## 2026-10-31 — Summary done, with Tophe
- Did: Tophe checked the *Done* of `specs/summary.md` on staging. The
  spec is done; the Summary section (T116–T118) of `agent/TASKS.md` is one
  line under *Done*.
- Decided: its entries moved to `agent/archive/progress-summary.md`, as
  the phases before it did.
- Left over: the backlog is empty; the next feature, the edition form,
  needs its spec, written with Tophe.

## 2026-11-01 — T119 The series names on v0.15.0 — done
- Did: the backend pins `v0.15.0` and serves `GET /api/v1/series-names`,
  the names of every series of the house, through `ListSeriesNames` in
  `bibliography.application.series` over `SeriesRepository.findAll`.
- Decided: nothing the brief left open.
- Deviations from the brief: the `listSeriesNames` constructor parameter
  of `ApiContractTest` came with step 6, its first reader, not step 5:
  detekt's `UnusedPrivateProperty` fails a constructor argument no case
  reads.
- Left over: T120 matches a series' name as PRD §3 says; T121 moves the
  frontend onto `v0.15.0` once T120 is deployed.

## 2026-11-01 — T120 A series' name matched as PRD §3 says — done
- Did: `V014` makes `series_name_unique` ignore case, accents and the
  spaces at a name's ends, and `JdbcSeriesRepository.findOrInsert`
  resolves its conflict on it; S5, a series of the house, runs green.
- Decided: nothing the brief left open.
- Deviations from the brief: none.
- Left over: authors stay unique on `LOWER(name)` (`agent/PROPOSED.md`);
  T121 moves the frontend onto `v0.15.0` once T120 is deployed.

## 2026-11-01 — T121 The edition form on v0.15.0, an unknown ISBN — handoff
- Done: steps 1 to 7 of the test plan, one commit each; start with step 8.
- Read first, before any unit test: S2 un-skipped fails on `Unable to find a
  label with the text of: Titre`; it was put back to `it.skip` uncommitted,
  and is un-skipped for good as step 15.
- Deviation: `FakeSeriesApi`'s third parameter is `seriesNames`, not `names`:
  a private property named `names` clashes with the method `names()`.
- Decided: `names()` reads the body with no status check (T126's); the kind
  is three `sr-only` radios inside their labels, the chosen one styled by
  `has-checked:`; the component spec reads the radios' order by role queries.
- No guard run yet (steps 9, 11, 14, 15 are left).
