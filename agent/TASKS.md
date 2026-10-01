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

- [x] T050 Backend: the catalogue ordered and paged by the database.
      The answer of T045 unchanged in every field and every order, but
      the order of T044 and the page of T045 are the database's: one
      statement of the library's own names the fifty editions of a page
      and its next from the place of `after`, and nothing of the
      catalogue beyond the page is read; the editions and copies of the
      page are then read through their repositories, as today (D02, D12).
      The text order ignores case and accents through a collation of the
      database, declared once by a migration; the last in-memory order
      goes with it.
      Tests against PostgreSQL: the order and page cases of T044 and T045
      over the statement, including an `after` the reader no longer holds
      and an `after` of no edition.
      Realises S1 and S2 anew; the scenario tests of S1 and S2 stay green
      throughout, no test is un-skipped.

*Done (Tophe, on the Pixel, from the installed app on staging): open
Catalogue; the ouvrages added in the bookshelf spec's check are there, One
Piece 1 first with Bibliothèque de Christophe · 2 exemplaires, and the rows
read as a shelf; on the second account of the family, open Catalogue and
read the empty sentence.*

## Covers — specs/covers.md

Contract: release `v0.8.2` of `camory/libris-api`, after `v0.7.0`: `Edition`
loses `coverUrl`, `IsbnLookup` gains `id` and `covers`, `NewBook` gains
`coverSource`, `Book` gains `coverUrl`, and the cover operation is new; the
lookup keeps its deprecated `coverUrl`, so each side deploys alone. Fields and
an operation are added, so the backend goes first and its pin moves in T051;
the frontend pin moves in T060, once T059 is deployed. Release `v0.9.0`, the
lookup losing `coverUrl`, removes a field, so the frontend goes first: the
backend pins it in T064, once T063 is deployed. No other task touches the
contract (D04). `v0.8.1` only keys the add's `201` again, so that its case
sends an ISBN whose check digit holds; `v0.8.2` adds the cover operation's
`400` for a text that is not a cover name; nothing pins `v0.8.0` or
`v0.8.1`.

The backend is deployed once T059 is merged, not before: from T051 the
catalogue rows show a cover only once the worker has stored it (Tophe,
2026-09-30).

- [x] T051 Backend: covers on `v0.8.2`, the thinnest answer.
      `ApiContractTest` pins `v0.8.2` (D04). The lookup answers `id`, the
      house's edition or null, `covers` empty, `coverUrl` as today; the add
      takes `coverSource`, no longer a cover address; `Book.coverUrl` is null.
      The cover operation serves by name a picture of the covers directory,
      one environment variable, with its year-long `Cache-Control`, else `404`
      `/problems/not-found`, a text that is not a cover name refused with
      `400` `/problems/validation` (D09, D11); `deploy/compose.yaml` gives the
      backend that variable and mounts there a second named volume, `covers`
      (D09); the fast entry's whole-body case gains the fields (D07).
      Realises S5's not-found and S10 not yet stored;
      un-skips the backend tests *S5 …, an address naming no cover* and *S10
      …, not yet stored*.

- [x] T052 Backend: the lookup offers the sources' covers.
      For an ISBN the house lacks, the bibliography answers the candidates
      `{source, url}` in the order inventaire.io, Open Library, BnF, a source
      with no record or no picture absent; `coverUrl` is the first `url` or
      null. inventaire.io is asked by ISBN for its picture alone, at 600 tall;
      no card field comes from it and no picture is fetched (D02, D11). Tests
      of the inventaire.io reading; the fast entry's whole-body case follows.
      Realises S1 on the backend; un-skips the backend test of S1.

- [ ] T053 Backend: the add carries the source, the worker fetches it.
      An added edition keeps its `coverSource` when it names a source, none
      otherwise, `Libris` included, ignored on a held ISBN, and awaits a
      picture. The add answers the copy, then wakes the worker, one piece
      behind an application port the tests call the same way (D02). Asked by
      ISBN, a chosen inventaire.io's picture is stored under a name made from
      its bytes; the catalogue answers `coverUrl`, the cover operation's
      address (D11). Realises S3, S4; un-skips the backend tests of S3, S4.

- [ ] T054 Backend: Open Library as the chosen source.
      The worker of T053 fetches the picture of an edition whose chosen
      source is Open Library from its covers by ISBN, and stores it as T053
      does, as fetched; the cover operation serves it as JPEG with its
      year-long `Cache-Control`. Only the chosen source is asked.
      Realises S5 on a stored cover; un-skips the backend test *S5 Libris
      serves a stored cover*.

- [ ] T055 Backend: a held edition offers its own cover.
      For an ISBN the house holds, the lookup answers the edition's `id` and,
      once its picture is stored, one candidate named `Libris` at the cover
      operation's address, `coverUrl` the same; no source is asked for the
      edition or its picture. Not yet stored stays as T051 answers it: the
      `id` and no candidate (D11). Realises S10; un-skips the backend test
      *S10 A held edition offers its own cover*.

- [ ] T056 Backend: the picture is normalised.
      Before it is stored, a fetched picture taller than 600 pixels is scaled
      to 600 tall, its proportions kept, and encoded as JPEG; one 600 tall or
      less is kept as fetched, in its own format, its bytes unchanged.
      Unit tests of the rule over generated pictures, one per case.
      Realises S6; un-skips the backend tests of S6, both cases.

- [ ] T057 Backend: the cascade for an edition with no chosen source.
      The worker asks, for an edition with an ISBN and no chosen source,
      inventaire.io, then Open Library, then the BnF, its picture read from
      the record's ark, stopping at the first picture and storing it as T053
      does; an edition no source has a picture for stays without. The
      editions stored before this feature, each with an ISBN, await a picture
      with no chosen source, a migration of the gate proving it (D03, D07).
      Realises S7 and S12; un-skips the backend tests of S7, its three cases.

- [ ] T058 Backend: a failed fetch waits a day.
      A fetch that gets no answer, an error, or what is not a picture leaves
      the edition without one and dates the attempt from the application's
      clock; a run within a day passes it by, a run a day later tries again.
      A fetch gives up after five seconds or five megabytes.
      Tests of the limits over a stubbed source; the scenario tests advance
      the clock. Realises S8; un-skips the backend tests of S8, its three
      cases.

- [ ] T059 Backend: the worker runs on its own.
      Besides each add, the worker runs when Libris starts and once a day, its
      schedule a configuration property, and takes the editions awaiting a
      picture one at a time, in the order they were added; two wakings never
      fetch at once (D02). Realises S9, and S12's run at start; un-skips the
      backend test of S9; the schedule is checked by hand in the log. The
      backend `Dockerfile` creates `/var/lib/libris/covers` owned by the
      `libris` user, so the `covers` volume mounted there is writable; the
      first stored cover on staging is checked by hand.

- [ ] T060 Frontend: covers on `v0.8.2`, the card shows the first candidate.
      Precondition (human): T059 deployed (D04).
      `vitest.global-setup.ts` pins `v0.8.2` (D04). The lookup's found answer
      holds `id` and `covers` as required, its edition no `coverUrl`; `Book`
      holds its own `coverUrl`, which the rows show; the add sends
      `coverSource` null until T062. The card shows the first candidate, the
      stand-in when none (D05); clients tested against `contracteer mock`
      (D07). The contract gives a problem's `type` no value, so the clients
      tell the lookup's and the add's problems apart by their HTTP status,
      404, 503 and 400, and their specs assert the status's outcome, never the
      body's `type` (Tophe, 2026-09-30). Realises S1, S11; un-skips the
      frontend tests of S1 and S11.

- [ ] T061 Frontend: the card shows the first cover that loads.
      Under the card's cover block, one dot per candidate that loaded, the
      shown one filled, each a button *Couverture <source>* with its pressed
      state, then the shown cover's source name in `muted`; a tap on a dot
      shows that cover and its name; a candidate that does not load gets no
      dot; none loading shows the stand-in, no name, no dot (U06, U08; the
      mockups of the spec). The PR body proposes the U06 rule the spec names.
      Realises S2; un-skips the frontend tests of S2, its four cases.

- [ ] T062 Frontend: the add carries the cover's source.
      The add sends as `coverSource` the source of the cover the card shows,
      null when it shows the stand-in; once added, the cover and its name
      stay as they were (D05). Realises S3 on the frontend; un-skips the
      frontend tests of S3, both cases.

- [ ] T063 Frontend: a held edition offers its own cover.
      When the lookup answers an `id`, the choice is over: the card shows the
      candidate's picture with no dot and no name under it, and the stand-in
      when there is no candidate; no side reads the candidate's source name.
      Realises S10 on the frontend; un-skips the frontend tests of S10, both
      cases.

- [ ] T064 Backend: the lookup on `v0.9.0`, its deprecated cover gone.
      Precondition (human): T063 deployed; `v0.9.0` released (D04).
      `ApiContractTest` pins `v0.9.0`, the only contract edit of the task
      (D04): the lookup no longer answers `coverUrl`, the candidates of
      `covers` saying it all; nothing else changes, and the fast entry's
      whole-body case loses the field (D07). Un-skips nothing: a field
      removed has no scenario, the spec's contract section asks for it, and
      every scenario test stays green throughout.

*Done (Tophe, on the Pixel, from the installed app on staging): scan an
ouvrage the house lacks; the card shows a cover with its source under it; tap
a dot, the cover changes; add it; open the catalogue, the row shows that
cover, after a second visit if the first came too soon; scan it again, the
card shows that cover with nothing under it. The editions from before the
deploy show their covers, save those no source has a picture for.*

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

- **Staging.** The *Done* of `specs/catalogue.md`, and now that of
  `specs/covers.md` and its S9, which checks the worker's schedule in
  staging's log, ask for staging, as the update and bookshelf specs did,
  while D09 knows one environment, the Kimsufi box. No task depends on the
  answer; the hand check is Tophe's step either way.
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
