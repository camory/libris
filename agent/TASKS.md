# Libris — Task backlog

> Ordered. The loop takes the **first unchecked** task. Keep tasks small enough
> for one PR a human reads in ten minutes. Each task states its acceptance
> criteria; "tests pass" is implied everywhere (see `CLAUDE.md`).
>
> Every phase is one feature spec in `specs/`. The planner derives the tasks
> from the spec's scenarios, each task line citing the scenarios it realises,
> and the phase ends when the spec's *Done* is checked. A done phase is one
> line under *Done* below; its story stays in `specs/`, `agent/briefs/` and
> `agent/PROGRESS.md`.
>
> Human steps are not tasks. A task that needs one states it as
> *Precondition (human)* on its line; Tophe does it between runs, and the
> planner reports `blocked` while it is missing.
>
> Follow-ups and ideas go to `agent/PROPOSED.md`, never here.

## Series list — specs/series-list.md

Contract: release `v0.12.1` of `camory/libris-api`, after `v0.11.0`,
superseding `v0.12.0`, which nothing pins: one operation, the reader's
series in no promised order, and two schemas added, `Kind` and `Series`;
the edition's `Series` renamed `SeriesEntry`, which changes no byte. An
operation is added, so the backend goes first. Its pin moves once, in T092,
the thinnest answer that serves the operation; T093 to T095 complete it,
T093 red on S1's scenario test, T094 on the unit tests the spec gives S2,
T095 on the persistence tests it gives S3, neither having a backend
scenario test. The frontend pin moves from `v0.11.0` in T096, once T095 is
deployed. No other task touches the contract (D04).

- [x] T092 Backend: the series list on `v0.12.1`, the thinnest answer.
      `ApiContractTest` pins `v0.12.1`, the backend's one bump (D04).
      Libris serves the series of the reader's catalogue, an array of
      `Series`, each its `name`, `volumeCount`, `coverUrl` and `kind`, and
      answers none to every reader until T093 finds them. Contracteer
      verifies the new operation's generated case, every other answer
      unchanged (D07, D11).
      The backend's walking skeleton, the thinnest scenario; realises S5;
      un-skips the backend test of S5.

- [x] T093 Backend: the reader's series, each counted.
      Libris answers each series with an edition on a bookshelf the reader
      belongs to, the catalogue's rule, once each; none for a bookshelf
      they do not belong to, nor for an edition with no series; in no
      promised order, the screen sorting them (D11). Each series counts
      its editions, each once however many copies it has; its kind and
      cover null until T094 and T095. A read across both contexts, each
      answering what it knows (D02, D12).
      Unit test of the count, an edition in two copies counted once.
      Realises S1 and S2's count; un-skips the backend test of S1.

- [x] T094 Backend: the kind a series shares.
      Each series of T093 answers the kind every one of its editions
      shares, `BD`, `MANGA` or `BOOK`, or none when two of them differ
      (PRD §3). Unit tests of the shared kind and of the count, one per
      case: a BD series, a manga series, a series mixing a BD and a manga,
      answered none; S2's first red, the spec giving it no backend
      scenario test. Realises S2.

- [ ] T095 Backend: the cover of a series.
      Each series of T093 answers the cover of its first edition in the
      order a shelf reads, catalogue S1's: by tome, an edition without a
      tome after the numbered tomes, then by title; its address as a
      `Book`'s, or none when that edition has none, another edition's
      never standing in (D11, a pick by position). The first edition of
      each series is found in SQL through `CatalogueEditions`, with the
      catalogue page's comparisons: titles through
      `ignoring_case_and_accents`, ties by id; no Kotlin compares titles.
      Persistence tests of the order, one per case: the first tome; an
      edition without a tome after the numbered tomes; no edition has a
      tome, the first by title; two titles that differ at a space, *Le
      secret de l'Espadon* before *Les cigares du pharaon*; two editions
      alike but for their id. Unit tests of the use case: the first
      edition's cover; none when the first has none and the second has
      one. S3's first red, the spec giving it no backend scenario test.
      Realises S3.

- [ ] T096 Frontend: the series of the catalogue, none yet, on `v0.12.1`.
      Precondition (human): T095 deployed (D04); the frontend piece of the
      spec, its skipped scenario tests over the fakes with the port, fake
      and domain types they name (D07).
      `vitest.global-setup.ts` pins `v0.12.1`, the frontend's one bump
      (D04). `/catalogue/series` shows the catalogue's header unchanged,
      the *Catalogue* tab in `accent`, and asks the series anew on each
      arrival: five skeleton rows, a cover and two lines each, until the
      answer; for none, the outlined book icon over *Les séries de vos
      bibliothèques apparaîtront ici.* (U03, U05, U08). Its client tested
      against `contracteer mock` (D07).
      The frontend's walking skeleton, the thinnest scenario; realises S5;
      un-skips the frontend test of S5.

- [ ] T097 Frontend: the reader's series listed.
      In place of the empty state, the catalogue's block, a row per series
      by name, ignoring case and accents, in the reader's language, with
      `nameOrder`, whatever the order of the answer (D11): the cover at
      the left, 48 by 74, or its stand-in; at its right, 6 apart, the name
      in the row title step and, in `muted`, the count, *1 album*, *6
      albums* for a BD series, *1 tome*, *3 tomes* otherwise; no overline,
      no chevron. The series' kind chooses the word in its key, every word
      from the `fr` catalogue (U02, U05, U06, U08). Unit test of the
      order: an answer out of order, shown by name.
      Realises S1, S2 and S3 on the frontend; un-skips the frontend tests
      of S1, of S2, its three cases, and of S3.

- [ ] T098 Frontend: Libris unavailable on the series.
      When the series do not come, in place of the list *Erreur lors du
      chargement, veuillez réessayer plus tard.* with the alert icon, and
      nothing listed; every word from the `fr` catalogue (U05, U07, U08).
      Realises S6; un-skips the frontend test of S6.

- [ ] T099 Frontend: the reader switches the view.
      On `/catalogue` and `/catalogue/series`, the first block of the
      content is the segmented choice: *Ouvrages* then *Séries*, each a
      link to its view, in a `nav` named *Vue du catalogue*, the view shown
      on `accent` and marked current; the *Catalogue* tab still opens
      `/catalogue`, and nothing under the switch changes there (U03, U04,
      U08). The phone's back button, from the series to the ouvrages, is
      the *Done*'s to check.
      Realises S4; un-skips the frontend test of S4.

*Done (Tophe, on the Pixel, from the installed app on staging): open
Catalogue and see Ouvrages current; choose Séries and read One Piece with
its count in tomes and the cover of its first tome, and a BD series counted
in albums, adding one first if staging holds none; go back with the phone's
button and land on the ouvrages; on the second account of the family,
choose Séries and read the empty sentence.*

## Done

- Phase 0 — Foundations, T001 to T012, done 2026-09-10 with `v0.1.3` on the
  Pixel: `/api/v1/me` deployed behind Authelia, the PWA installable, the
  expired session handled. Reviewed by Tophe on 2026-09-08.
- Phase 1 — Fast entry, T013 to T027, done 2026-09-16 with the build of T027
  (`68154ac`) on the Pixel: a manga and a BD scanned and both cards read, an
  ISBN-10 typed, a wrong ISBN's message, 9782380751673 filled by Open Library
  alone.
- Screen, T030, done 2026-09-17, `docs/DESIGN.md`: the tab bar of U03 every
  screen is reached from, redrawn on Tophe's review and checked on the
  Pixel.
- Update, T028 to T029, done 2026-09-18, `specs/update.md`: the banner of a
  waiting version, the reader's order to install it and the check while the
  app stays open, checked on the Pixel from the installed app.
- Kind, T031 to T032, done 2026-09-19, `specs/kind.md`: the kind read from
  the BnF record and answered on every lookup, the card in the words of its
  kind, checked on the Pixel from the installed app.
- Fast entry, the button beside the field, T042, done 2026-09-25,
  `specs/fast-entry.md`: the button *Chercher* on the field's line, the
  screen checked on the Pixel with its deploy.
- Bookshelf, T033 to T041, done 2026-09-29, `specs/bookshelf.md`: the
  default bookshelf created with the reader, the house's edition stored, the
  ouvrage added from the card and its copies read on it, checked on the Pixel
  from the installed app with the deploy of T049.
- Catalogue, T043 to T050, done 2026-10-07, `specs/catalogue.md`: the
  house's ouvrages listed in the order of a shelf, in pages of fifty ordered
  and paged by the database, the catalogue tab and its empty sentence,
  checked on the Pixel from the installed app.
- Web layer, T068, done 2026-10-03, `docs/ARCHITECTURE.md`: each response
  built by `from` on its companion, the controllers mapping nothing, the
  proxy's headers read by a class of their own; no behaviour changed.
- Covers, T051 to T071, done 2026-10-07, `specs/covers.md`: the lookup
  offers the sources' covers and the card shows the first that loads, its
  dots choosing another; the add keeps the chosen source; the worker fetches
  each awaited cover on its own, normalised, bounded to 5 000 pixels a side,
  a failed fetch waiting a day and one edition never stopping the run; the
  catalogue rows show the stored cover; checked on the Pixel from the
  installed app on staging.
- Order on the screen, T086 to T087, done 2026-10-12,
  `docs/ARCHITECTURE.md`: the card and a catalogue row sort their authors by
  first role then name and their bookshelves by name, in the reader's
  language; the backend promises no order for them (D11). Deployed on
  staging.
- Bookshelves, T072 to T090, done 2026-10-14, `specs/bookshelves.md`: a
  reader creates a bookshelf, an owner adds the readers Libris knows as
  owner or viewer, a member opens it and reads its members, a reader
  chooses the default their one-tap add fills; checked on the Pixel on
  staging, the second account's ouvrage typed, its origin having no camera.
- Creation audit, T091, done 2026-10-14, `docs/ARCHITECTURE.md`: a
  bookshelf records the reader who created it and when, a copy the reader
  who added it and when, set by the use cases from the `Clock`, in the
  database only; the rows already there filled by the migration. Deployed
  on staging.

## Questions for the human

- **No spec yet**, so nothing is planned for them: PRD §4.1 beyond the add
  and the listing — viewing and editing an edition, removing a copy, the
  filters and sorts of the list, the edition page — §4.2 search, §4.3
  beyond creating a bookshelf, adding a member and choosing the default —
  removing a member, changing a member's role, adding to a bookshelf other
  than the default, moving and lending a copy, its condition and
  acquisition date — §4.4 reading, §4.5 beyond the series list — the
  series page, its gaps, the number of published volumes, the series
  filter and renaming a series — §4.6 wishlist, §4.9 import and export,
  §4.10 administration, and the offline browsing of §4.8.
- **A refusal the bookshelves screens never provoke.** The spec says what
  each form shows when Libris does not answer (S9), and the taken name
  (S3), not what it shows when Libris refuses otherwise: a blank name the
  screen never sends, an add of a member answered `400`, `404` or `409`, a
  default answered `400` or `404`, all reachable only by a race with
  another reader. T081, T088 and T084 read such a refusal as no answer and
  show their S9 sentence; say if one should read otherwise.
- **A refused add on the card.** The spec's screen says what the card shows
  when Libris does not answer the add (S5), not when it answers `400` or
  `404`, which the card's own ouvrage and the reader's default bookshelf
  should never draw. T039 reads either as a failure and T040 shows the S5
  sentence for every failure; say if a refusal should read otherwise.
