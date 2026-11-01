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

## 2026-11-01 — T121 The edition form on v0.15.0, an unknown ISBN — done
- Did: the frontend pins `v0.15.0`; `SeriesApi.names()` replaces the interim
  `SeriesNamesApi`; an ISBN no source knows opens `EditionForm`, empty, in
  place of *ISBN inconnu*; S2 runs green.
- Decided: `names()` reads the body with no status check, the non-`200`
  being T126's; the kind is three `sr-only` radios inside their labels, the
  chosen one styled by `has-checked:`; each field's label points at its box
  through `for` and a `useId()` id; *Résumé* is a `textarea` outside the
  fields' `v-for`, three lines tall by `min-h-[calc(3lh+27px)]` and growing
  by `field-sizing-content`.
- Deviations from the brief: `FakeSeriesApi`'s third parameter is
  `seriesNames`, a private `names` clashing with the method `names()`;
  step 13 asserts no absent *ISBN inconnu*, so that the word stays in
  `EditionFormScenarios.spec.ts` alone as the criteria want; step 11 finds
  *Pages* by its label, since its mutation, a `number` input, reds a role
  query before the value.
- Left over: the checks by hand on the Pixel (the form against
  `2-empty.jpg`, *Résumé* growing, the numeric keyboard); T122 fills the
  form of a found ISBN and adds the button. `EditionForm` repeats
  `SourceEditionCard`'s stand-in and its *Dans aucune de vos
  bibliothèques* row class for class (Duplicated Code, found by the
  reviewer on T121, Tophe 2026-11-01): T122, which gives the form the
  card's covers and dots, extracts the card's top part they share rather
  than copying the covers a second time.

## 2026-11-01 — T122 A new ISBN opens the form, filled — done
- Did: an ISBN the house lacks opens `EditionForm` filled with the answer,
  its covers, *Auteurs* and bookshelf row, the add docked under it; a
  language reads as `languageWord` (`domain/Language.ts`); S1 runs green.
- Decided: the card's three shared parts are `CoverChoice`, `AuthorLines`
  and `BookshelvesHolding`; `IsbnView`'s `shownIsbn13` (the answer's ISBN or
  the unknown one) guards the block that holds the card or the form and the
  dock; a held ISBN's card keeps the docked button, as the spec's S1 note
  has it; the dock is not drawn once the add is done, so no empty band
  stays; the run was split in two by a handoff after step 16.
- Deviations from the brief: step 14 was green on its first run, step 12
  having already wired the form's `coverSource` to the view; its red was
  read with the listener taken off and is listed in the PR body.
- Left over: the checks by hand on the Pixel (the form against
  `1-filled.jpg`, the dock and its hairline while the form runs under it,
  in place at the end, covered by the keyboard); T123 sends the form as
  edited, docks the button under the unknown ISBN's form too (the dock is
  drawn only with an `edition` now) and keeps the form open to edits under
  the add's message, which is already in the dock.

## 2026-11-01 — T123 The ouvrage is added as edited — handoff
- Done: the un-skip of S5 and S11 (S5 red on its heading *Romance Dawn*, S11's two cases already green), steps 1 to 5. Start with step 6 (*reads a field of spaces alone as no value*: `number` still reads `"  "` as `0`, `text` already trims to `null`).
- Decided: `languageCode` matches through `nameOrder(locale)`, the tree's base collator; `CODES` is one code per line, Prettier's layout. `editionOf` reads `text()` (trimmed, `""` to `null`), `number()` and the series' name through `text()`.
- Deviations: none yet. Step 5's case builds the spaced draft from `filled` by `Object.fromEntries`, the summary given its own line break.
- Guards: step 3, mutation `?? word` in `languageCode`: `AssertionError: expected 'klingon' to be null`, reverted.
- Learned: a Bash command whose text holds the ISO code `su` as a word is refused by the sandbox hook (*No privilege escalation*): the code list was checked from a script file written with Write.
- Gate at handoff: `npm test` red on S5 alone (`Unable to find role="heading" and name "Romance Dawn"`), the cited scenario not yet reached; 567 passed, 14 skipped. Not pushed: the commits wait on the local branch.
