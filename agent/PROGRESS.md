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

## 2026-11-10 — T154 The ouvrage saved as edited — done
- Did: `PUT /api/v1/books/{id}` validates the body as `EditionDetails` and
  writes it over the held edition through `EditEdition` (bibliography),
  which `SaveEdition` (library) answers with the reader's copies and the
  house's series name. S4, both, and S5's other series and author pass.
- Decided: both use cases grew their constructor one cycle at a time, a
  parameter no case read yet held for one cycle under
  `@Suppress("UnusedParameter")` (step 8's `EditEdition`, step 13's
  `SaveEdition`), gone the next. `EditEdition` reads the held edition by
  `findByIds`, writes in `executeWithoutResult`, then reads it back; step
  12's double is `EditionsRespellingAuthors`, private to `EditEditionTest`.
  The contract stubs' fixture is `EDIT_ONE_PIECE_2`, below `ONE_PIECE_2`.
- Deviations from the brief: the tidy step moved the reader's copies and
  the series entry, written twice once `SaveEdition` had them, from
  `FindCatalogueEdition` into `HeldEditionReads.kt`, two internal functions
  both use cases call (a file outside *Files and modules*).
  `ApiContractTest` on `main` and after: `tests="50"`.
- Left over: T155–T158 per the backlog. Two PROPOSED items: `NewEdition`
  and `EditionDetails` (and their requests) repeating the edition's fields,
  for T158; the validation problem built in four controllers.

## 2026-11-10 — T155 A name no edition carries is not suggested — done
- Did: the authors' names are those a contribution references
  (`JdbcAuthorNames`, `EXISTS`); the series' names are those of the ids
  `CarriedSeries` answers, read through `SeriesRepository.findByIds`.
  `SeriesRepository.findAll` is gone. S6 runs and passes.
- Decided: `JdbcCarriedSeries` reads `series_id` from `edition` alone,
  `IS NOT NULL`, and answers a `Set` through `JdbcClient`'s `set()`, so no
  `DISTINCT`. The authors' statement keeps each name once with a semi-join
  rather than `DISTINCT`, the tree's form (see the catalogue order item of
  `backend.md`). `ListSeriesNamesTest` builds its `SeriesInMemory` in two
  statements instead of `apply`, since the case needs *Astérix*'s id.
- Deviations from the brief: the criterion's `grep -rn "findAll"` over
  `bibliography/domain/series` answers one line, the brief's own
  `CarriedSeries.findAll`; `SeriesRepository` has none. Nothing else.
  `ApiContractTest`: `tests="50"` on the untouched tree and after.
- Left over: T156–T158 per the backlog; the name rows no edition carries
  stay for an administration page (PRD §4.10).

## 2026-11-10 — T156 An edition's own series respelled for the house — done
- Did: `SeriesRepository.respellOrFindOrInsert(series, ownSeriesId)`; its
  statement gives the row the name lands on the name sent only when that
  row is the own series. `EditEdition` calls it with the held edition's
  series id. *S5 …, its series* runs and passes.
- Decided: the tidy step made `findOrInsert` the same statement with
  `ownSeriesId = null`, so the adapter holds one insert of a series; the
  fake's method goes through its `findOrInsert` and respells the match
  only when its id is the own series'. The JDBC case's local is `asterix`,
  not the brief's `astérix` (detekt's naming pattern).
- Deviations from the brief: none in approach. Step 3 was a real red
  (step 2's smallest green set `EXCLUDED.name` on any conflict), so no
  guard. A throwaway JDBC case, deleted, proved the statement takes a null
  `ownSeriesId` before the tidy step made every `findOrInsert` prove it.
  `ApiContractTest`: `tests="50"` on main and after.
- Left over: T157 (the own author) and T158 (the add of a held ISBN
  respelling its own series, `HoldEdition`) per the backlog.

## 2026-11-10 — T157 An edition's own author respelled for the house — done
- Did: `JdbcEditionRepository`'s author statement gives the row a name
  lands on the name sent only when the edition already references that
  row, whatever the role. *S5 …, its author* runs and passes.
- Decided: `update` resolves the author ids before it deletes the
  edition's contributions, so the statement's `EXISTS` still sees the own
  authors; `nameIdOf` became `authorIdsOf`, which answers a
  `Map<Contribution, UUID>` that `insertContributionsOf` links, keeping
  the class at 10 functions. The tidy step gave `insert` the same
  statement: an edition not yet stored has no contribution rows, so it
  never respells, as T156's `findOrInsert` shares the series statement.
- Deviations from the brief: none in approach. Step 3 was a real red
  against step 2's smallest green (an update-only statement with
  `EXCLUDED.name` on every conflict), so it needed no guard mutation; step 4
  was the guard, its mutation run and reverted.
  `ApiContractTest`: `tests="50"` on main and after.
- Left over: T158 (the add of a held ISBN respelling its own names,
  `HoldEdition`) per the backlog.

## 2026-11-10 — T158 A held ISBN added as edited — done
- Did: `HoldEdition` takes `EditEdition` and writes an ISBN the house holds
  through it with `NewEdition.toEditionDetails()`, answering the edition
  read back. *S2* and *S5 …, the add of a held ISBN* run and pass.
- Decided: the held branch reads the result as
  `checkNotNull(… as? Edited).edition`, so an impossible `NoSuchEdition`
  fails as `EditEdition`'s own read-back does; `editEdition` is the last
  constructor parameter of `HoldEdition`.
- Deviations from the brief: none. Step 2's red was `List is empty.` on
  the stored series (the held edition came back untouched, no series
  written); step 3 needed no fix, both scenarios went green with step 2.
  The tidy step found nothing to change beyond the repetition the
  PROPOSED item names. `ApiContractTest`: `tests="50"` on main and after.
- Left over: T159 (the frontend's pencil on the lookup's card, the pin
  move); whether `NewEdition` composes `EditionDetails` stays in
  `agent/PROPOSED.md`.

## 2026-11-10 — T159 The save on v0.18.0, the pencil of a held ISBN — done
- Did: the frontend pins `v0.18.0`; `FetchEditionsApi` sends the `PUT
  /books/{id}` and `editionsApi` joins `LibrisPorts`; the lookup's card of
  a held ISBN with none of the reader's copies carries *Modifier*, its form
  the house's cover without dots. S1 and S2 run and pass.
- Decided: the corner shows on `!held || shownCopies.length === 0`, so an
  ISBN the house lacks keeps its pencil whatever its copies; the
  `held` prop of `EditionForm` is required, the spec's `form()` giving
  `false` by default. The two scenario lines of `CoversScenarios` were
  written by hand, Prettier rewriting the rest of that file.
- Deviations from the brief: none. Steps 3 and 4 were reds as foreseen;
  steps 10 to 12 are guards, their mutations run and reverted (listed in
  the PR). The run handed off once, after step 11; the second run did
  step 12 and the close.
- Left over: T160 to T163 per the backlog; the lookup card and form of a
  held ISBN checked by hand by Tophe, in both schemes.

## 2026-11-10 — T160 The pencil opens the form on the ouvrage's page — done
- Did: the ouvrage's page offers *Modifier* on the card of a book with an
  ISBN, whatever the role; its tap shows the lookup's form with the cross,
  *Enregistrer* docked and the answered title as a hidden `h1`. S3 passes.
- Decided: the form and its `sr-only` `h1` sit in one `<template>` branch
  of the page's `v-if` chain, before the card's, on `editing` and a
  non-null `isbn13`, so the form's `isbn13` is narrowed with no cast.
  `edit()` sets `editing` and asks both lists of names, unawaited.
  The spec's `pageOf` and `tapPencil` helpers carry the pencil's cases.
- Deviations from the brief: none. Steps 1 to 3 and 5 to 8 were reds as
  foreseen; steps 4, 9 and 10 are guards, their mutations run and
  reverted (listed in the PR).
- Left over: T161 to T163 per the backlog; the `DockedAdd` naming item in
  `agent/PROPOSED.md`; the page card and form checked by hand by Tophe, in
  both schemes.

## 2026-11-10 — T161 The cross drops the edits — done
- Did: the cross of the ouvrage's page form puts `editing` back to false:
  the card shows again as Libris answered it, its pencil and its trash
  back, and the next pencil builds the form afresh. S8 runs and passes.
- Decided: the spec's `tapCross(screen)` helper joined `tapPencil` in the
  refactor step, the two cases repeating the cross's tap.
- Deviations from the brief: step 2's mutation hid the kept-mounted form
  with `aria-hidden` instead of `v-show`: under `v-show` jsdom keeps the
  `display: none` read first on the detached tree, and five cases red on
  the form never shown, not on the edits coming back. With `aria-hidden`
  the guard alone reds, on the typed title (in the PR). Step 1 was a red as
  foreseen; steps 2 and 3 are guards, run and reverted.
- Left over: T162 (the save, S4) and T163 (its failure, S9) per the
  backlog; the cross checked by hand by Tophe on the page.
