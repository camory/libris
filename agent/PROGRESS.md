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

## 2026-10-17 — T101 The series page on v0.13.0, no such series — handoff
- Done: steps 1 to 9 of the test plan, one commit per green (steps 1–3 in one, steps 4 and 8 together, since step 4's red lasts until step 8). Start at step 10: tidy, gate, gotcha, tick, entry, PR.
- Red of step 1, the PR needs it: 4 of 38 cases, `GET /api/v1/series -> 200 (generated)` (`'[0].id': is required`), and on `/api/v1/series/{id}` `404 with scenario '404_NOT_IN_MY_CATALOGUE'`, `200 (generated)` and `400|404 (auto: path 'id' type mismatch)`, each `Actual: 403`. After step 9: `tests=38`, 0 failures (partial run; read it again after the gate).
- Step 2 red: `not a problem: 404 NOT_FOUND` (Boot's error body, path unmapped). Step 4 red: `404` expected, `400` `/problems/validation` (the list answered no id).
- Guard, step 6: mutation dropping `membership.reader_id = :readerId` from `FIND_SERIES_IDS` reds `an edition on a bookshelf the reader does not belong to answers no series id` (`Values differed at keys`: Tom's *Astérix* answered too); reverted, green.
- Decided: `ListCatalogueSeries` reads the id as `seriesIds.getValue(seriesEditions.first().id)` of its name groups; `ApiContractTest` holds `ASTERIX` (series id) and `ASTERIX_SERIES` (the `CatalogueSeries` shared by the list's stub and `Found`), fixtures at the foot.
- Deviations: none so far.
- Left over for step 10: the `additionalProperties: false` item of `agent/gotchas/every-run.md` (backend pins `v0.13.0` since T101, frontend `v0.12.1` since T096); self-review of `git diff main...HEAD`.
