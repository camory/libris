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

- [ ] T034 Backend: the house's edition stored.
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

- [ ] T035 Backend: the ouvrage added to a bookshelf.
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

- [ ] T036 Backend: the lookup answers the house's edition.
      `library.application`, since the bibliography knows nothing of the
      library (D02): the lookup takes the reader who asks and looks in the
      house first — when an edition of that ISBN exists it answers it as the
      house holds it, with its copies on the bookshelves the reader belongs
      to, each with the name of its bookshelf, and asks no source at all; an
      edition whose copies all sit on bookshelves the reader does not belong
      to is answered the same, with no copy.
      When the house lacks the ISBN the lookup behaves as
      `specs/fast-entry.md` says, sources, merge and answers unchanged, and
      the copies are empty; the tests of that spec stay green.
      The ports read an edition by its ISBN-13 and the copies of an edition
      visible to a reader; no new table and no migration.
      Tests over fake repositories with sources that must not be asked, one
      per case of S4.
      Realises S4 on the backend; un-skips nothing, its scenario test waiting
      for the API of T037.

- [ ] T037 Backend: the API of the bookshelf, on `v0.6.0`.
      `ApiContractTest` pins `v0.6.0`: the backend's one bump, and one task,
      since the verifier reads the whole document and both added fields are
      required — the two fields and the operation land together (D04).
      `infrastructure.web` of each context: `me` answers `defaultBookshelf`
      `{id, name}`; the lookup answers `copies`, empty when the reader's
      bookshelves hold none, the
      current reader read from the forwarded headers as `me` reads them
      (D06).
      `POST /api/v1/bookshelves/{id}/books`: the body `NewBook` → `201` the
      `Copy` with its bookshelf; `400` `/problems/validation` with one error
      per refused field; `404` `/problems/not-found` when the reader is a
      member of no such bookshelf (D11).
      The verifier adds two cases of its own, an `id` that is not a uuid and
      a body of the wrong types, both answered `400` with a `Problem` before
      any use case is reached (`agent/GOTCHAS.md`).
      Web-slice tests over the three answers and the two refusals, every use
      case the web slice scans mocked (`agent/GOTCHAS.md`); Contracteer verifies
      `ADD_ONE_PIECE_1`, `400_NOT_AN_ISBN`, `404_NOT_MY_BOOKSHELF` and
      `ONE_PIECE_2_OWNED`.
      Carries S1 to S4 to the API; un-skips the tests of
      `BookshelfScenarios.kt`, `S1 The first visit creates the bookshelf`,
      `S2 The ouvrage is added`, `S3 A known ISBN reaches the existing
      edition` and `S4 The ouvrage is already in a bookshelf`.

- [ ] T038 Frontend: the copies on the card, on `v0.6.0`.
      Precondition (human):
      `frontend/src/scenario/BookshelfScenarios.spec.ts`, one skipped test
      per frontend scenario, each bearing the scenario's exact title, against
      `contracteer mock` (D07).
      `vitest.global-setup.ts` pins `v0.6.0`, the only contract edit of the
      task (D04).
      `domain` and `infra/api`: `SourceEdition` gains its `copies`, each with
      its bookshelf, read from the answer by `FetchIsbnApi` (D05).
      `ui`: `SourceEditionCard` shows, between the authors and the field
      rows, one row per bookshelf of the reader holding a copy, *Dans
      Bibliothèque de Léa*, followed by *· 2 exemplaires* when it holds more
      than one, and no row at all when the reader's bookshelves hold none;
      every word from the `fr` catalogue (U04, U07, U08).
      Component tests over no copy, one copy, two copies on one bookshelf and
      copies on two bookshelves; a word the card shows is asserted in three
      files (`agent/GOTCHAS.md`).
      Realises S4 on the frontend; un-skips
      `S4 The ouvrage is already in a bookshelf`.

- [ ] T039 Frontend: the ouvrage added from the card.
      `domain` and `application`: the reader carries their
      `defaultBookshelf`, read by `MeApi`, so the app knows where the add
      goes from its first request; a port that adds a book to a bookshelf,
      with its `infra/api` adapter posting the `NewBook` the card holds, its
      copies apart, and answering the copy or the failure (D05).
      `ui`: under the card, the full-width primary button *Ajouter à ma
      bibliothèque*; while the add runs it reads *Ajout en cours…* with a
      spinner and accepts nothing; once added, the copies row shows the new
      copy on the reader's default bookshelf and the button is gone, with no
      message; when Libris does not answer, *Erreur lors de l'ajout,
      veuillez réessayer plus tard.* in red under the button, which is back,
      and the card unchanged. A new lookup replaces the card, its rows and
      the button (U02, U04, U06).
      Every word from the `fr` catalogue (U04, U07, U08).
      Component tests over the four states of the button, a unit test of the
      adapter over a stubbed `fetch`, and the two scenarios against
      `contracteer mock`.
      Realises S2 and S5; un-skips `S2 The ouvrage is added` and
      `S5 Libris unavailable during the add`.

*Done (Tophe, on the Pixel, from the installed app): scan One Piece 1 and add
it, the card shows Dans Bibliothèque de Christophe; scan it again, the row is
there before any tap; add it again, the row reads · 2 exemplaires; on a
second account of the family, scan it and read the card without a place.*

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

## Questions for the human

- **Staging.** The *Done* of both specs planned here asks for a deploy of
  staging, as the update spec's did, while D09 knows one environment, the
  Kimsufi box. No task depends on the answer; the hand check is Tophe's step
  either way.
- **No spec yet**, so nothing is planned for them: PRD §4.1 catalogue beyond
  the add of `specs/bookshelf.md`, §4.2 search, §4.3 bookshelves and copies
  beyond the default bookshelf — other bookshelves, members, the `VIEWER`
  role, moving and lending a copy — §4.4 reading, §4.5 series tracking, §4.6
  wishlist, §4.9 import and export, §4.10 administration, and the offline
  browsing of §4.8.
