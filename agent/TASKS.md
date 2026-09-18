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

## Kind — specs/kind.md

Contract: release `v0.5.0` of `camory/libris-api`, published on 2026-09-18.
T031 moves the backend pin and T032 the frontend one, each as its line spells
out; no other task of the phase touches the contract (D04). The backend goes
first: the answer gains a field (D04).

- [ ] T031 Backend: the kind of a scanned ouvrage.
      Precondition (human): the scenario class
      `backend/src/test/kotlin/fr/amory/libris/scenario/KindScenarios.kt`,
      one skipped test per scenario, each bearing the scenario's exact
      title (D07).
      `ApiContractTest` pins `v0.5.0`, the only contract edit of the task;
      the renamed schemas and the folded `ValidationProblem` change no byte
      of an answer, so the `400` of the lookup stays as it is (D04).
      `domain`: `Kind`, and the rule over a BnF record — a comic strip,
      marked in field 105, translated from Japanese, Korean or Chinese, read
      from field 101 `$c`, is a `MANGA`; any other comic strip a `BD`;
      everything else a `BOOK`. Unit tests of the rule, the comic strip
      translated from English among them (D02).
      `infra.lookup`: the BnF source carries the kind it read, Open Library
      names none, and the merge answers the kind a source named, `BOOK` when
      none did, so the field is never absent.
      `infra.web`: the answer carries `kind`, and Contracteer verifies
      `ONE_PIECE_1` with `kind: MANGA`.
      S4 runs over the recorded answer of `9782380751673`, the ISBN Open
      Library alone knows; the record without a field 105 is the rule's
      unit test, and no new recording.
      Realises S1, S2, S3, S4; un-skips the four tests of `KindScenarios.kt`.

- [ ] T032 Frontend: the card in the words of its kind.
      Precondition (human): `frontend/src/scenario/KindScenarios.spec.ts`,
      one skipped test bearing the exact title of `S1 A manga`, against
      `contracteer mock` (D07).
      `vitest.global-setup.ts` pins `v0.5.0`, the only contract edit of the
      task (D04).
      `domain` and `infra/api`: `SourceEdition` gains its `kind`, read from
      the answer by `FetchIsbnApi` (D05).
      `ui`: `SourceEditionCard` writes the series line with *tome* for a
      livre and a manga and *album* for a BD; the authors are grouped by
      their whole set of roles — the names alone on one line, separated by
      commas, when every author shares one set, otherwise one line per author
      with their role words; the role words follow the kind, texte and
      illustration for a livre, scénario and dessin for a BD and a manga,
      couleurs and traduction unchanged; every word from the `fr` catalogue
      (U04, U07, U08).
      Component tests over the three kinds and over authors whose role sets
      differ; a word the card shows is asserted in three files
      (`agent/GOTCHAS.md`).
      Realises S1, S2, S3; un-skips `S1 A manga`.

*Done (Tophe, on the Pixel, from the installed app): scan One Piece 1 and
read Eiichirō Oda alone under One piece · tome 1; scan an Astérix and read
album and the two lines scénario and dessin; scan a novel and read its author
without a role word.*

## Bookshelf — specs/bookshelf.md

Contract: release `v0.6.0` of `camory/libris-api`, published on 2026-09-18,
after `v0.5.0`. The backend pin moves once, in T037, with the whole of
`v0.6.0` at once, because the verifier reads the whole document and the two
added fields are required; T033 to T036 build what that task then serves. The
frontend pin moves in T038. No other task touches the contract (D04).

- [ ] T033 Backend: the bookshelf created with the reader.
      Precondition (human): the scenario class
      `backend/src/test/kotlin/fr/amory/libris/scenario/BookshelfScenarios.kt`,
      one skipped test per backend scenario, each bearing the scenario's
      exact title, over fake repositories as the spec's proofs ask (D07).
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
      Realises S1; un-skips `S1 The first visit creates the bookshelf`.

- [ ] T034 Backend: the house's book stored.
      `domain`: `Book`, an edition the house holds — the fields the lookup
      answers and its kind, at most one series with a volume number, its
      authors each with a role — and the repository that stores one and finds
      one by its ISBN-13 (D02).
      `infra.persistence`: `V003__book.sql`, the book, its series, its
      authors and their roles; a series or an author is matched by name
      whatever its capitalisation and never doubled (PRD §3), uuid v7 keys
      and the role as text with a CHECK (D11).
      Slice test: a book stored and read back whole, and a second book whose
      series and author names differ only in case landing on the rows of the
      first.
      Nothing of the API, of the lookup or of any use case changes: this is
      the storage T035 and T036 need.
      Serves S2, S3 and S4; realises none on its own and un-skips nothing.

- [ ] T035 Backend: the ouvrage added to a bookshelf.
      `domain`: `Copy`, one book on one bookshelf, and its repository;
      `infra.persistence`: `V004__copy.sql` and the `JdbcClient` repository,
      proven by a slice test (D02, D11).
      `application`: the add use case takes the reader, a bookshelf and the
      ouvrage as the card shows it, kind included; it matches the house's
      book by `isbn13` and creates it on the way when the house lacks it,
      then puts a copy on that bookshelf and answers it; it refuses a
      bookshelf the reader is not a member of, and refuses an `isbn13` that
      is not an ISBN-13, as the lookup refuses its path; without an
      `isbn13` there is nothing to match, so the house gets a new book every
      time.
      Tests over fake repositories: the house lacking the ISBN (S2); another
      reader adding an ISBN the house holds, whose copy is the edition's
      second on a second bookshelf, and the same reader adding it again, the
      house still holding one book for that ISBN (S3); the two refusals; the
      add without ISBN.
      Realises S2 and S3 on the backend; un-skips `S2 The ouvrage is added`
      and `S3 A known ISBN reaches the existing edition`.

- [ ] T036 Backend: the lookup answers the house's book.
      `application`: the lookup takes the reader who asks and looks in the
      house first — when a book of that ISBN exists it answers it as the
      house holds it, with its copies on the bookshelves the reader belongs
      to, each with the name of its bookshelf, and asks no source at all; an
      edition whose copies all sit on bookshelves the reader does not belong
      to is answered the same, with no copy.
      When the house lacks the ISBN the lookup behaves as
      `specs/fast-entry.md` says, sources, merge and answers unchanged, and
      the copies are empty; the tests of that spec stay green.
      The ports read a book by its ISBN-13 and the copies of a book visible
      to a reader; no new table and no migration.
      Tests over fake repositories with sources that must not be asked, one
      per case of S4.
      Realises S4 on the backend; un-skips
      `S4 The ouvrage is already in a bookshelf`.

- [ ] T037 Backend: the API of the bookshelf, on `v0.6.0`.
      `ApiContractTest` pins `v0.6.0`: the backend's one bump, and one task,
      since the verifier reads the whole document and both added fields are
      required — the two fields and the operation land together (D04).
      `infra.web`: `me` answers `defaultBookshelf` `{id, name}`; the lookup
      answers `copies`, empty when the reader's bookshelves hold none, the
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
      case of `infra.web` mocked (`agent/GOTCHAS.md`); Contracteer verifies
      `ADD_ONE_PIECE_1`, `400_NOT_AN_ISBN`, `404_NOT_MY_BOOKSHELF` and
      `ONE_PIECE_2_OWNED`.
      Carries S2, S3 and S4 to the API; un-skips nothing, their scenario
      tests being the ones T035 and T036 un-skipped.

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
