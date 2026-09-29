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

## Catalogue — specs/catalogue.md

Contract: release `v0.7.0` of `camory/libris-api`, after `v0.6.2`: one
operation, the reader's catalogue, and three schemas added. The backend pin
moves once, in T043, the thinnest answer that serves the operation, so the
verifier, which reads the whole document, stays green; T044 and T045
complete that answer, each starting red on its own scenario. The operation
is added, so the backend goes first and the frontend pin moves in T046,
once T045 is deployed. No other task touches the contract (D04).

- [x] T043 Backend: the catalogue API on `v0.7.0`, the thinnest answer.
      Precondition (human): the backend tests of the catalogue scenarios,
      one skipped test per backend scenario, each bearing the scenario's
      exact title, over HTTP like the other scenario classes (D07).
      `ApiContractTest` pins `v0.7.0`: the backend's one bump (D04).
      The library answers a reader's catalogue as a `BookPage`: every
      edition with a copy on a bookshelf they belong to, once each, a
      `Book` with its `id` and its `copies` on those bookshelves, each with
      its bookshelf's id and name, and nothing of a copy on a bookshelf
      they do not belong to; `next` null, since nothing pages yet; `after`
      accepted as a uuid and answered `400` `/problems/validation` with a
      `Problem` otherwise (PRD §3, §4.1, D11). A read across the two
      contexts, answered from one query of the library's own, proven
      against PostgreSQL, in no order yet (D02, D12). Contracteer verifies
      its generated case and the wrong-type case; the answers of the lookup
      and the add keep their bodies.
      Tests: the read over copies on the reader's bookshelves and on
      another's, an edition with copies on two of them listed once; an
      empty catalogue for a reader whose bookshelves hold nothing.
      Realises S3, and S1 but its order; un-skips the backend test of S3.

- [x] T044 Backend: the catalogue in the order of a shelf.
      The answer of T043 in the order a shelf reads: by series name, or
      title when there is none, ignoring case and accents, then by tome as
      a number, an edition of the series without a tome after its numbered
      tomes, then by title, ordered in the one query (D12).
      Tests against PostgreSQL: the order over editions that differ only
      in case, accents, tome, a missing tome, or title.
      Realises S1; un-skips the backend test of S1.

- [x] T045 Backend: the catalogue in pages of fifty.
      The answer of T043 comes fifty editions at a time and names the next
      page by the id of the last edition answered; from that id as `after`
      the following page starts just after it, with no gap or repeat, and
      the last page names no next. The order of T044 is made total, the id
      its last key, so two editions equal in it neither repeat nor skip;
      an edition added between two pages lands in its place without
      shifting them. An `after` naming an edition of the house that the
      reader's catalogue no longer holds continues after its place; an
      `after` naming no edition answers an empty page naming no next (D11).
      Tests against PostgreSQL: fifty-one editions read as fifty then one;
      editions of the same series, tome and title split across a page
      boundary; an edition added between the two pages; an `after` of an
      edition the reader's catalogue no longer holds; an `after` of no
      edition.
      Realises S2; un-skips the backend test of S2.

- [x] T046 Frontend: the catalogue tab, its page and the empty catalogue,
      on `v0.7.0`.
      Precondition (human): T045 deployed (D04); the frontend tests of the
      catalogue scenarios, one skipped test per frontend scenario, each
      bearing the scenario's exact title, over the catalogue's fake; the
      catalogue's port, its fake and the catalogue as the app reads it,
      the least the tests compile on (D05, D07).
      `vitest.global-setup.ts` pins `v0.7.0`, the only contract edit of the
      task (D04). The port joins `LibrisPorts` with its client, tested
      against `contracteer mock` (D05, D07).
      The tab bar gains its third column, the three book spines at 32 over
      *Catalogue*, in `accent` on its own page, leading to `/catalogue`; the
      page shows the title *Parcourir le catalogue* and the line *Les
      ouvrages de toutes vos bibliothèques.*, then asks the first page anew
      on each arrival, five skeleton rows standing until it comes; when it
      is empty, the outlined book icon over *Les ouvrages de vos
      bibliothèques apparaîtront ici.*; a page with rows is T047's; every
      word from the `fr` catalogue (U03, U05, U07, U08). The catalogue is a
      use case of its own, a composable over the port, the page rendering
      its state (D05, D10).
      Tab bar tests with three tabs, the third active on its page;
      composable tests over the fake answering an empty page; view tests
      of the loading and empty states.
      The frontend's walking skeleton, the thinnest scenario; realises S3
      on the frontend; un-skips the frontend test of S3.

- [x] T047 Frontend: the catalogue listed.
      When the first page has rows, in place of the empty state one block
      on `surface`, hairlines between rows, 14 of padding, a row per
      edition in the order
      of the answer: the cover 48 by 74 or its stand-in, the overline série
      · tome when it has a series, the title, the authors' names on one line
      separated by commas, then in `muted` the reader's bookshelves holding
      a copy, each with *· 2 exemplaires* when it holds more than one. A row
      leads nowhere (U05, U06, U08).
      Composable tests over the fake answering rows; view tests of the
      listed state and of a row with and without series, one and two
      bookshelves.
      Realises S1 on the frontend; un-skips the frontend test of S1.

- [x] T048 Frontend: Libris unavailable on the first page.
      When the first page does not come, in place of the list *Erreur lors
      du chargement, veuillez réessayer plus tard.* with the alert icon,
      and nothing listed; every word from the `fr` catalogue (U05, U07,
      U08).
      Composable tests over the fake failing; view tests of the state.
      Realises the first-page case of S4 on the frontend; un-skips the
      frontend test of S4 on the first page.

- [x] T049 Frontend: the next page.
      When the last row comes into view and the page received names a
      next, two skeleton rows stand under it while the next page is asked
      with that `after`, and its rows take their place; under the last row
      of the last page, nothing. When the next page does not come, the rows
      listed stay and the sentence of S4 with its icon sits where the
      skeleton rows stood (U05).
      Composable tests over a fake of two pages: the second follows the
      first with no gap or repeat, and no third is asked; the next page
      failing; view tests of the loading-more state and of the sentence
      under the rows.
      Realises S2 and the next-page case of S4 on the frontend; un-skips
      the frontend tests of S2 and of S4 on the next page.

*Done (Tophe, on the Pixel, from the installed app on staging): open
Catalogue; the ouvrages added in the bookshelf spec's check are there, One
Piece 1 first with Bibliothèque de Christophe · 2 exemplaires, and the rows
read as a shelf; on the second account of the family, open Catalogue and
read the empty sentence.*

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

## Questions for the human

- **Staging.** The *Done* of `specs/bookshelf.md`, and now that of
  `specs/catalogue.md`, asks for the installed app on staging, as the update
  spec's did, while D09 knows one environment, the Kimsufi box. No task
  depends on the answer; the hand check is Tophe's step either way.
- **No spec yet**, so nothing is planned for them: PRD §4.1 beyond the add
  and the listing — viewing and editing an edition, removing a copy, the
  filters and sorts of the list, the edition page — §4.2 search, §4.3
  bookshelves and copies beyond the default bookshelf — other bookshelves,
  members, the `VIEWER` role, moving and lending a copy — §4.4 reading, §4.5
  series tracking, §4.6 wishlist, §4.9 import and export, §4.10
  administration, and the offline browsing of §4.8.
- **A refused add on the card.** The spec's screen says what the card shows
  when Libris does not answer the add (S5), not when it answers `400` or
  `404`, which the card's own ouvrage and the reader's default bookshelf
  should never draw. T039 reads either as a failure and T040 shows the S5
  sentence for every failure; say if a refusal should read otherwise.
