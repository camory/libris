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

## 2026-11-04 — Edition form done, with Tophe
- Did: Tophe checked the *Done* of `specs/edition-form.md` on staging. The
  spec is done; the Edition form section (T119–T129) of `agent/TASKS.md`
  is one line under *Done*.
- Decided: its entries moved to `agent/archive/progress-edition-form.md`,
  as the phases before it did.
- Left over: the backlog is empty; the next feature needs its spec,
  written with Tophe.

## 2026-11-07 — Remove a copy done, with Tophe
- Did: Tophe checked the *Done* of `specs/remove-copy.md` on staging. The
  spec is done; the Remove a copy section (T130–T137) of `agent/TASKS.md`
  is one line under *Done*.
- Decided: its entries moved to `agent/archive/progress-remove-copy.md`,
  as the phases before it did.
- Left over: the backlog is empty; the next feature needs its spec,
  written with Tophe.

## 2026-11-08 — Scan beep done, with Tophe
- Did: Tophe checked the *Done* of `specs/scan-beep.md` on staging. The
  spec is done; the Scan beep section (T138–T141) of `agent/TASKS.md` is
  one line under *Done*.
- Decided: its entries moved to `agent/archive/progress-scan-beep.md`,
  as the phases before it did.
- Left over: the backlog is empty; the next feature needs its spec,
  written with Tophe.

## 2026-11-09 — Authors edit done, with Tophe
- Did: Tophe checked the *Done* of `specs/authors-edit.md` on staging. The
  spec is done; the Authors edit section (T142–T152) of `agent/TASKS.md`
  is one line under *Done*.
- Decided: its entries moved to `agent/archive/progress-authors-edit.md`,
  as the phases before it did.
- Left over: the backlog is empty; the next feature needs its spec,
  written with Tophe.

## 2026-11-10 — T153 The save on v0.18.0, an unknown edition refused — done
- Did: the backend pins `v0.18.0`; `PUT /api/v1/books/{id}` is served by
  `CatalogueController.save` through `SaveEdition`, which answers
  `NoSuchEdition` to every id and stores nothing. S7 runs and passes.
- Decided: `SaveEditionResult.NoSuchEdition` is written qualified in the
  controller, since `find` already imports the read's `NoSuchEdition`
  (the form of `AddMemberResult.NoSuchBookshelf` in `BookshelfController`).
  The two id fixtures are named after their keys, `ONE_PIECE_2` and
  `UNKNOWN_BOOK`, and sit last, below `NOT_MY_COPY`, as the last helper
  first reaches them.
- Deviations from the brief: none. Step 2's red was Boot's plain `405`,
  not the `404` the gotcha named (the path is mapped for `GET`), thrown
  when the answer is built, at `lea.saves`; the `backend.md` item now
  says so.
- Left over: T154 writes the edition behind `SaveEdition` and removes the
  two `@Suppress("UnusedParameter")` (its brief); S2, S4, S5, S6 stay
  skipped for their tasks. The reviewer's suggestion (#381):
  `EditionDetailsRequest` repeats eleven fields of `NewBookRequest`; when
  T154 adds its validation, the two requests share one shape or one
  validation, so the field rules are not written twice.

## 2026-11-10 — T154 The ouvrage saved as edited — handoff
- Done: steps 1 to 12, one commit each. Start at step 13 (`SaveEditionTest`,
  the copy on another reader's bookshelf left out; the signature change, the
  controller, `ApiContractTest`'s stubs and both `@Suppress` with it).
- Step 1's red: S4's first case on the page's title, S4's second on the
  not-found refusal, S5's other series `Astérix` for `Tintin`, other author
  `[]`. `ApiContractTest` on `main`: `tests="50"`.
- Decided: `EditEdition` grew its constructor one cycle at a time (step 8 a
  constant `NoSuchEdition` with `@Suppress("UnusedParameter")`, as T153's
  `SaveEdition`, gone at step 9); it reads the held edition by
  `findByIds(listOf(id))`, writes in `executeWithoutResult`, then reads it
  back. Step 12's double is `EditionsRespellingAuthors`, delegating to
  `EditionsInMemory`. No guard so far, no mutation run.
- Deviations: none. Gate: red only on the four scenarios of step 1.
