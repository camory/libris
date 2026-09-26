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

## Bookshelf — specs/bookshelf.md

Contract: release `v0.6.0` of `camory/libris-api`, published on 2026-09-18,
after `v0.5.0`. The backend pin moves once, in T037, with the whole of
`v0.6.0` at once, because the verifier reads the whole document and the two
added fields are required; T033 to T036 build what that task then serves. The
frontend pin moves in T038. No other task touches the contract (D04).

- [x] T033 Backend: the bookshelf created with the reader.
      Precondition (human): the scenario class
      `backend/src/test/kotlin/fr/amory/libris/scenario/BookshelfScenarios.kt`,
      one skipped test per backend scenario, each bearing the scenario's
      exact title, over HTTP like the other scenario classes, so it compiles
      before the inside exists; T037 un-skips it (D07).
      `domain`: `Bookshelf` with its name and its members, a member joining a
      reader to a bookshelf with a role, `OWNER` the only role this phase
      creates, and the repository that stores one and reads a reader's
      bookshelves (D02).
      `infra.persistence`: `V002__bookshelf.sql`, the bookshelf, its members
      and the reader's default bookshelf; the `JdbcClient` repository proven
      by a slice test, uuid v7 keys and the role as text with a CHECK (D11).
      `application`: on the first visit of a reader Libris has never seen,
      `ReaderVisit` also creates one bookshelf named *Bibliothèque de* their
      name, which they own, and keeps it as their default; a reader it knows
      is answered as before.
      No contract edit and no change to any answer: `me` keeps its body until
      T037 (D04).
      Realises S1; un-skips nothing, its scenario test waiting for the API of
      T037.

- [x] T034 Backend: the house's edition stored.
      `bibliography.domain`: `Edition`, what an ISBN identifies as the house
      holds it — the fields the lookup answers and its kind, at most one
      series entry with a volume number, its contributions each with a role
      — with its `EditionId`, and the repository that stores one and finds
      one by its ISBN-13 (D02).
      `bibliography.infrastructure.persistence`: `V003__edition.sql`, the
      edition, its series, its contributions and their roles; a series or a
      contributor is matched by name whatever its capitalisation and never
      doubled (PRD §3), uuid v7 keys and the role as text (D11).
      Slice test: an edition stored and read back whole, and a second edition
      whose series and contributor names differ only in case landing on the
      rows of the first.
      Nothing of the API, of the lookup or of any use case changes: this is
      the storage T035 and T036 need.
      Serves S2, S3 and S4; realises none on its own and un-skips nothing.

- [x] T035 Backend: the ouvrage added to a bookshelf.
      `library.domain.copy`: `Copy`, one edition on one bookshelf, with its
      `CopyId` and its repository;
      `library.infrastructure.persistence`: `V004__copy.sql` and the
      `JdbcClient` repository, proven by a slice test (D02, D11).
      `library.application`: the add use case takes the reader, a bookshelf
      and the ouvrage as the card shows it, kind included; it matches the
      house's edition by `isbn13` and creates it on the way when the house
      lacks it,
      then puts a copy on that bookshelf and answers it; it refuses a
      bookshelf the reader is not a member of, and refuses an `isbn13` that
      is not an ISBN-13, as the lookup refuses its path; without an
      `isbn13` there is nothing to match, so the house gets a new edition
      every time.
      Tests over fake repositories: the house lacking the ISBN (S2); another
      reader adding an ISBN the house holds, whose copy is the edition's
      second on a second bookshelf, and the same reader adding it again, the
      house still holding one edition for that ISBN (S3); the two refusals; the
      add without ISBN.
      Realises S2 and S3 on the backend; un-skips nothing, their scenario
      tests waiting for the API of T037.

- [x] T036 Backend: the lookup answers the house's edition.
      The bibliography's lookup looks in the house first — when an edition
      of that ISBN exists it answers it as the house holds it and asks no
      source at all; otherwise it behaves as `specs/fast-entry.md` says,
      sources, merge and answers unchanged, and the tests of that spec stay
      green. The library's lookup takes the reader who asks and adds to that
      answer the copies on the bookshelves the reader belongs to, each with
      the name of its bookshelf; an edition whose copies all sit on
      bookshelves the reader does not belong to comes with no copy.
      The ports read an edition by its ISBN-13 and the copies of an edition;
      the bookshelf says who its members are; no new table and no migration.
      Tests over fake repositories with sources that must not be asked, one
      per case of S4.
      Realises S4 on the backend; un-skips nothing, its scenario test waiting
      for the API of T037.

- [x] T037 Backend: the API of the bookshelf, on `v0.6.0`.
      `ApiContractTest` pins `v0.6.0`: the backend's one bump, and one task,
      since the verifier reads the whole document and both added fields are
      required (D04). The reader's answer names their `defaultBookshelf`,
      its id and name. The ISBN lookup is answered by the library: the
      bibliography's answer with the reader's copies, each with its
      bookshelf's id and name, empty when their bookshelves hold none (D02).
      Adding a book to a bookshelf takes a `NewBook` and answers `201` the
      copy with its bookshelf; `400` `/problems/validation`, one error per
      refused field, `isbn13` checked as the lookup checks its ISBN; `404`
      `/problems/not-found` when the reader is a member of no such bookshelf
      and, until a release adds `403`, when they see it without owning it
      (`agent/PROPOSED.md`); a `Problem` too for the verifier's own cases, an
      id that is not a uuid and a body of the wrong types
      (`agent/GOTCHAS.md`). Contracteer verifies `ADD_ONE_PIECE_1`,
      `400_NOT_AN_ISBN`, `404_NOT_MY_BOOKSHELF` and `ONE_PIECE_2_OWNED`; the
      fast-entry scenario comparing the whole lookup answer gains its empty
      `copies`, declared in the pull request (D07).
      Carries S1 to S4 to the API; un-skips the six backend tests of the
      bookshelf scenarios.

- [x] T038 Frontend: the copies on the card, on `v0.6.2`.
      Precondition (human): the frontend tests of the bookshelf scenarios,
      one skipped test per frontend scenario, each bearing the scenario's
      exact title, against `contracteer mock` (D07).
      `vitest.global-setup.ts` pins `v0.6.2`, the only contract edit of the
      task (D04). The lookup's answer, as the app reads it, gains its copies,
      each with the id and name of its bookshelf (D05).
      The card shows, between the authors and the field rows, one row per
      bookshelf of the reader holding a copy, *Dans Bibliothèque de Léa*,
      followed by *· 2 exemplaires* when it holds more than one, and no row
      when the reader's bookshelves hold none; every word from the `fr`
      catalogue (U06, U08).
      Component tests over no copy, one copy, two copies on one bookshelf and
      copies on two bookshelves; a word the card shows is asserted in three
      places (`agent/GOTCHAS.md`).
      Realises S4 on the frontend; un-skips
      `S4 The ouvrage is already in a bookshelf`.

- [x] T041 Frontend: the bookshelf rows of the card, more visible, and the absence.
      Precondition (human): the S4 scenario of no bookshelf of the reader's
      expects the absence row, skipped until this task (D07).
      Each bookshelf row of the card, between the authors and the field rows,
      is in `body` semibold with the book icon at 22 before its words, so the
      reader in a bookstore sees at a glance where their copies are; when no
      bookshelf of the reader holds a copy, the card shows one row of the
      same shape, *Dans aucune de vos bibliothèques*, on an edition the house
      holds as on one the sources answer; every word from the `fr` catalogue
      (U06, U07, U08).
      Component tests over no copy and one copy read the weight, the icon
      and the words; the absence word is asserted in three places
      (`agent/GOTCHAS.md`); the fast-entry scenarios stay green with the
      row they gain.
      Placed before T039 so the button lands on the final rows. Un-skips
      `S4 The ouvrage is already in a bookshelf, none of the reader's`.

- [x] T039 Frontend: the ouvrage added from the card.
      The reader the app knows already carries their default bookshelf, and
      the `BookshelfApi` port with its `FetchBookshelfApi` client already
      posts the `NewBook` the card holds and reads back the copy or a
      failure; the task builds what stands between the card and that port
      (D05, D07).
      Under the card, found or already there, the full-width primary button
      *Ajouter à ma bibliothèque*; while the add runs it reads *Ajout en
      cours…* with a spinner and accepts nothing; once added, the copies row
      shows the new copy on the reader's default bookshelf and the button is
      gone, with no message. A new lookup replaces the card, its rows and the
      button. Every word from the `fr` catalogue (U04, U05, U06, U08).
      Component and view tests over the found, adding and added states.
      Realises S2 on the frontend; un-skips `S2 The ouvrage is added`.

- [x] T040 Frontend: the add Libris does not answer.
      When Libris does not answer the add, *Erreur lors de l'ajout, veuillez
      réessayer plus tard.* as a message in `danger` under the button, which
      is back, and the card as it was, no copy added; a new lookup replaces
      the message with the rest (U04, U05, U08).
      The add leaves the view for the first composable,
      `useAddBookToBookshelf(meApi, bookshelfApi)` in `application/`, that
      exposes its state and `add(edition)`; the view renders that state
      (D05, D10). The lookup stays in the view.
      Composable tests over the fakes with no component mounted, the failure
      among them; view test of the not-added state; the scenario with the
      API failing.
      Realises S5; un-skips `S5 Libris unavailable during the add`.

*Done (Tophe, on the Pixel, from the installed app): scan One Piece 1 and add
it, the card shows Dans Bibliothèque de Christophe; scan it again, the row is
there before any tap; add it again, the row reads · 2 exemplaires; on a
second account of the family, scan it and read the card without a place.*

## Catalogue — specs/catalogue.md

Contract: release `v0.7.0` of `camory/libris-api`, after `v0.6.2`: one
operation, the reader's catalogue, and three schemas added. The backend pin
moves once, in T043, the thinnest answer that serves the operation, so the
verifier, which reads the whole document, stays green; T044 and T045
complete that answer, each starting red on its own scenario. The operation
is added, so the backend goes first and the frontend pin moves in T046,
once T045 is deployed. No other task touches the contract (D04).

- [ ] T043 Backend: the catalogue API on `v0.7.0`, the thinnest answer.
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

- [ ] T044 Backend: the catalogue in the order of a shelf.
      The answer of T043 in the order a shelf reads: by series name, or
      title when there is none, ignoring case and accents, then by tome as
      a number, an edition of the series without a tome after its numbered
      tomes, then by title, ordered in the one query (D12).
      Tests against PostgreSQL: the order over editions that differ only
      in case, accents, tome, a missing tome, or title.
      Realises S1; un-skips the backend test of S1.

- [ ] T045 Backend: the catalogue in pages of fifty.
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

- [ ] T046 Frontend: the catalogue tab and its page, on `v0.7.0`.
      Precondition (human): T045 deployed (D04); the frontend piece of the
      spec — the tests of the catalogue scenarios, one skipped test per
      frontend scenario each bearing its exact title, the catalogue's port
      and its fake, the catalogue as the app reads it, and its client
      tested against `contracteer mock` (D07).
      `vitest.global-setup.ts` pins `v0.7.0`, the only contract edit of the
      task (D04).
      The tab bar gains its third column, the three book spines at 32 over
      *Catalogue*, in `accent` on its own page, leading to `/catalogue`; the
      page shows the title *Parcourir le catalogue* and the line *Les
      ouvrages de toutes vos bibliothèques.*, and nothing more yet; every
      word from the `fr` catalogue (U03, U07, U08).
      Tab bar tests with three tabs, the third active on its page.
      No scenario of its own; un-skips nothing.

- [ ] T047 Frontend: the catalogue listed.
      On each arrival on the page the first page is asked anew, five
      skeleton rows standing until it comes; then one block on `surface`,
      hairlines between rows, 14 of padding, a row per edition in the order
      of the answer: the cover 48 by 74 or its stand-in, the overline série
      · tome when it has a series, the title, the authors' names on one line
      separated by commas, then in `muted` the reader's bookshelves holding
      a copy, each with *· 2 exemplaires* when it holds more than one. A row
      leads nowhere. The listing is a use case of its own, a composable
      over the port, the page rendering its state (D05, D10, U05, U06, U08).
      Composable tests over the fake; view tests of the loading and listed
      states and of a row with and without series, one and two bookshelves.
      Realises S1 on the frontend; un-skips the frontend test of S1.

- [ ] T048 Frontend: the catalogue empty, and Libris unavailable.
      When the first page is empty, in place of the list the outlined book
      icon over *Les ouvrages de vos bibliothèques apparaîtront ici.*; when
      the first page does not come, in place of the list *Erreur lors du
      chargement, veuillez réessayer plus tard.* with the alert icon, and
      nothing listed; every word from the `fr` catalogue (U05, U07, U08).
      Composable tests over the fake, empty and failing; view tests of the
      two states.
      Realises S3 and the first-page case of S4 on the frontend; un-skips
      the frontend tests of S3 and of S4 on the first page.

- [ ] T049 Frontend: the next page.
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
