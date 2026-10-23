# Libris — Progress log, Edition page

The entries of `agent/PROGRESS.md` of the edition page, T111 to T115, done
2026-10-22, moved here on 2026-10-23 once what a run needs of them was in
`agent/gotchas/`. Nothing is appended here.

---

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

## 2026-10-22 — T114 The reader opens an ouvrage — done
- Did: a catalogue row, on the catalogue and on a series' page, is one
  `RouterLink` to `/catalogue/ouvrages/{id}`; the page draws the lookup's
  card over the found `Book`, its title the `h1`, the stored cover or the
  stand-in, `held`; both S1 cases run and pass, S2 green, S3 skipped.
- Decided: the heading level through `<component :is>` over `pageTitle`;
  the view builds the one `Libris` candidate in a `computed` from
  `book.coverUrl`; step 3's case mounts inline with `pageTitle`, the
  spec's positional `card()` helpers left as they are. Two runs, the first
  handing off after step 9, the second doing the self-review, the gotchas
  and the PR.
- Deviations from the brief: none. Step 9's first expectation listed the
  links in the wrong order; the catalogue keeps the answer's order
  (Astérix, then Romance dawn), corrected before its commit.
- Left over: the sentences of `specs/catalogue.md` and
  `specs/series-page.md` saying a row leads nowhere, waiting for Tophe in
  `agent/TASKS.md`; the back button and the layout against `2-shown.jpg`
  and `3-shown-sparse.jpg`, Tophe's on the Pixel; T115 the failure.

## 2026-10-22 — T115 Libris unavailable on an ouvrage — done
- Did: `useFindCatalogueEdition` reads a rejected find, and any problem but
  `/problems/not-found` and `/problems/validation`, as `unavailable`; the
  ouvrage's page then says *Erreur lors du chargement, veuillez réessayer
  plus tard.* alone, with its alert; S3 runs and passes, S1 and S2 green.
- Decided: the view picks its sentence through a `failure` computed, as
  `SeriesPageView` does, so one `<p>` holds both messages; the composable
  takes `useFindCatalogueSeries`'s `stateOf` shape.
- Deviations from the brief: none. S3's red was read before step 1, then
  put back skipped, uncommitted, as the brief allows.
- Left over: nothing of this spec's frontend; the layout against
  `5-unavailable.jpg` is Tophe's to check on the phone.
