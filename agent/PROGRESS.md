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

## 2026-10-22 — T111 The edition page on v0.14.0, no such ouvrage — done
- Did: the backend pins `v0.14.0`; `CatalogueController.find` serves
  `GET /api/v1/books/{id}` through `FindCatalogueEdition`, which answers
  `NoSuchEdition` to every reader until T112; both S2 cases run and pass.
- Decided: nothing the brief left open; the steps ran in its order, one
  commit each, `ApiContractTest` going from 38 to 41 cases.
- Deviations from the brief: none.
- Left over: T112 finds the edition behind the same use case, result type
  and controller method, and removes the `@Suppress("UnusedParameter")`
  on `FindCatalogueEdition.invoke`; `S1` stays skipped for it.

## 2026-10-22 — T112 The reader opens an ouvrage — done
- Did: `FindCatalogueEdition` takes `bookshelves`, `copies`, `editions` and
  `seriesRepository` and finds the edition with its series entry and the
  reader's copies of it, or `NoSuchEdition`; S1 of `EditionPageScenarios`
  runs and passes, both S2 cases stay green.
- Decided: the reader's bookshelves are read with `findByMember`, as
  `BrowseCatalogue` and `FindCatalogueSeries` do, rather than `findById`
  per copy with `hasMember`: one query, and the map gives each copy its
  bookshelf's name. The copies are read first and `NoSuchEdition` answered
  before the edition is read, so an edition the reader cannot see is never
  read. Step 2's green kept only the reader's copies, since
  `NoSuchEdition` needs them, so step 3 is a guard, not a red.
- Deviations from the brief: none.
- Left over: the `CopyOnBookshelf` mapping, now in three use cases of the
  package, in `agent/PROPOSED.md`; T113 to T115 on the frontend.

## 2026-10-22 — T113 The edition page on v0.14.0, no such ouvrage — done
- Did: the frontend pins `v0.14.0`; `CatalogueApi` gains `find(id)`, its
  client tested against the mock on `200`, `404` and `400`, the interim
  `EditionPageApi` folded into it; `/catalogue/ouvrages/{id}` shows the
  card's skeleton, then *Ouvrage introuvable.*; both S2 cases run and pass.
- Decided: `HeldEditionCardSkeleton` repeats `SourceEditionCardSkeleton`'s
  top part rather than a third component shared for nine lines. Two runs,
  the first handing off after step 10.
- Deviations from the brief: S2's un-skip came first, as the implementer
  rule asks (red read: *Unable to find … Ouvrage introuvable.*), not last
  as the brief's step 14; its mutation ran once green. `CatalogueView.spec.ts`
  gains one line, a `find` on the `CatalogueApi` it builds by hand, or
  `vue-tsc` refuses it after the fold, so the stat criterion on
  `ui/views/catalogue` holds for everything but that line.
- Left over: T114 shows the found ouvrage, T115 the failure; the shape of
  the skeleton against `1-loading.jpg` is Tophe's to check on the phone.
