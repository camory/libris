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

## Order on the screen — docs/ARCHITECTURE.md

No contract release: no OpenAPI document promises these orders (D11).
A promise leaves, so the frontend goes first: T086 sorts what the screens
show today in the backend's order, and T087 drops that order once T086 is
deployed.

- [x] T086 Frontend: the card and a catalogue row sort their authors and
      bookshelves.
      The card sorts its authors by their first role, scénario, dessin,
      couleurs, traduction, then by name, and its rows *Dans …* by name; a
      catalogue row sorts its authors in the card's order and its
      bookshelves by name; each name ignoring case and accents, compared in
      the reader's language, the comparison the bookshelves' screens sort
      with (U06, D11). Unit tests of both orders, over names
      that differ only in case or accents, given in another order than the
      screen's.

- [x] T087 Backend: authors and a card's bookshelves in no promised order.
      Precondition (human): T086 deployed (D04).
      `Contributions.of` keeps one contribution per author and role and
      orders nothing, and `LookupIsbnForReader` sorts nothing (D11, D12);
      their tests of the order leave, and `ContributionRole.order` with
      them, which nothing else reads.

## Bookshelves — specs/bookshelves.md

Contract: release `v0.11.0` of `camory/libris-api`, after `v0.10.0`: six
operations added, no existing answer changed, and no schema says
`additionalProperties: false` any more. Operations are added, so the backend
goes first. Its pin moves once, in T072, which serves all six in their
thinnest answer, since the verifier reads the whole document; T073 to T078
complete that answer, each red on its own scenario. The frontend pin moves
from `v0.9.0` in T079, once T078 is deployed; the `v0.10.0` it crosses only
takes the lookup's `coverUrl` away, which the screen no longer reads. No
other task touches the contract (D04). The first side to pin `v0.11.0`
rewrites the gotcha on `additionalProperties: false`, which the contract no
longer says.

- [x] T072 Backend: bookshelves on `v0.11.0`, the thinnest answer.
      Precondition (human): the backend tests of the bookshelves scenarios,
      skipped, one per scenario or case, bearing its exact title (D07).
      `ApiContractTest` pins `v0.11.0`, the backend's one bump (D04). The
      library answers the bookshelves a reader belongs to with their role on
      each, in no order yet, and creates one with the name asked, the reader
      its one owner, not their default (D12). Until their own tasks, opening
      a bookshelf, adding a member and choosing the default answer that
      there is no such bookshelf, and the readers Libris knows answer none.
      Each `400`, `404` and `409` declared comes from a use case's outcome,
      `/problems/conflict` the new type (D11); Contracteer verifies every
      case of the six operations (D07).
      Realises S2; un-skips the backend test of S2.

- [x] T073 Backend: an owner adds a member.
      The readers Libris knows are answered, every one, the asker among
      them, in no promised order. An owner adds to their bookshelf a reader
      who does not belong to it, as owner or as viewer: the answer is the
      new member, and the bookshelf is among that reader's bookshelves, its
      copies in their catalogue and on the card of their ISBN (PRD §3, D12).
      Realises S5; un-skips the backend test of S5, both cases.

- [x] T074 Backend: the reader's bookshelves in order.
      The bookshelves of T072 come in the order of S1: the reader's default
      first, then the others by name, ignoring case and accents; one the
      reader does not belong to is never answered, whatever its members
      (PRD §3, D12). Unit test of the order over names that differ only in
      case or accents, the default's name the last of them.
      Realises S1; un-skips the backend test of S1.

- [x] T075 Backend: a name Libris refuses.
      Creating a bookshelf with a blank name is answered `400`
      `/problems/validation` on `name`; with the name of a bookshelf the
      reader belongs to, whatever their role on it, compared ignoring case
      and accents, `409` `/problems/conflict`; neither creates a bookshelf.
      The blank name is refused where the name is built, the taken one by
      the creation, which spans the reader's bookshelves (D11, D12).
      Realises S3; un-skips the backend tests of S3, both cases.

- [x] T076 Backend: a member opens a bookshelf.
      Opening a bookshelf answers its members alone its name and its
      members, each a reader's id and display name with their role, the
      owners first, then the viewers, each by display name ignoring case and
      accents; a reader who does not belong to it is answered, as for an id
      naming no bookshelf, that there is no such bookshelf. A read across
      the bookshelf and its readers, each through its own repository (D12).
      Unit test of the order.
      Realises S4; un-skips the backend tests of S4, both cases.

- [x] T085 Backend: bookshelves and members in no promised order.
      The reader's bookshelves (S1) and a bookshelf's members (S4) come in
      no promised order, the screen sorting them (D11):
      `ListReaderBookshelves` and `FindBookshelf` sort nothing, their unit
      tests of the order leave, and `MembershipRole.order` with them.
      `IgnoringCaseAndAccents`, left with `BookshelfName` as its one user,
      goes back to a private folding beside it. The backend tests of S1 and
      S4 pass unedited.

- [x] T077 Backend: an add Libris refuses.
      A viewer of the bookshelf, or a reader who does not belong to it,
      adding a member is answered that there is no such bookshelf; an owner
      adding a reader Libris does not know is answered `400`
      `/problems/validation` on `readerId`; adding a reader already a
      member, whatever their role, `409` `/problems/conflict`; in all four
      the bookshelf's members stay as they were (D11, D12).
      Realises S6; un-skips the backend tests of S6, all four cases.

- [ ] T078 Backend: a reader chooses their default.
      A reader makes a bookshelf they own their default: the answer is
      `204`, and the reader's answer names it as their default from then
      on. A bookshelf they are a
      viewer of, or do not belong to, is answered that there is no such
      bookshelf, their default unchanged. The default being one they own
      spans the reader and the bookshelf: the use case keeps it in one
      transaction (D12).
      Realises S7, S8; un-skips the backend tests of S7 and of S8, both cases.

- [ ] T079 Frontend: the home page lists the reader's bookshelves, on
      `v0.11.0`.
      Precondition (human): T078 deployed (D04); the frontend piece of the
      spec, its skipped scenario tests over the fakes with the ports, fakes
      and domain types they name (D07).
      `vitest.global-setup.ts` pins `v0.11.0`, the frontend's one bump
      (D04). The home page reads *Libris*, the greeting, then the block *Mes
      bibliothèques*: a row per bookshelf, the default first, then the
      others by name ignoring case and accents, its name over
      *propriétaire* or *invité*, *· par défaut* on the reader's
      default; skeleton rows while they come, S9's loading sentence when
      Libris does not answer; the link *Ajouter un ouvrage* leaves (U03, U05,
      U08). Unit test of the order, sorted with the comparison of T086
      (D11), the one T082 and T083 sort with. Its client tested against
      `contracteer mock` (D07); the PR body proposes the spec's U08 title. Realises S1 and S9's home page case;
      un-skips the frontend tests of S1 and of S9, the home page.

- [ ] T080 Frontend: a reader creates a bookshelf.
      Once the rows are there, the plus *Nouvelle bibliothèque* right of
      *Mes bibliothèques* adds a last row, the field *Nom de la
      bibliothèque* and the check *Créer*, absent while the name is blank,
      the plus gone meanwhile; *Création en cours…* while it runs, then the
      new bookshelf among the rows, the field's row gone, the plus back
      (U03, U04, U05, U08). Lucide arrives with its first icons,
      `circle-plus` and `check`; the PR body proposes the spec's U03, U04
      and U07 amendments. Its client tested against `contracteer mock`
      (D07). Realises S2; un-skips the frontend test of S2.

- [ ] T081 Frontend: a name Libris refuses, or no answer.
      A name taken reads *Vous avez déjà une bibliothèque de ce nom.* under
      the field, in its row, the field keeping what was typed; no answer
      reads S9's creating sentence in the same place; in both the row stays
      and no bookshelf is added. Another refusal, which the screen never
      provokes, reads as no answer (U05, U08).
      Realises S3 on the frontend and S9's creating case; un-skips the
      frontend tests of S3 and of S9, creating a bookshelf.

- [ ] T082 Frontend: a member opens a bookshelf.
      A home page row gains its chevron and leads to `/bibliotheques/{id}`,
      the *Accueil* tab shown: the bookshelf's name as the title, no hint,
      then *Membres*, a row per member, the owners first, then the viewers,
      each by display name ignoring case and accents, *Léa ·
      propriétaire*, the reader's own reading *Vous*; skeletons while it
      comes; *Bibliothèque introuvable.* alone for no such bookshelf or an
      id that is not one; S9's loading sentence when Libris does not answer
      (U03, U05, U07, U08). Unit test of the order. Its client tested
      against `contracteer mock` (D07); the PR body proposes the spec's U03
      header with no hint.
      Realises S4 and S9's opening case; un-skips the frontend tests of S4,
      both cases, and of S9, opening a bookshelf.

- [ ] T083 Frontend: an owner adds a member.
      For an owner, the readers Libris knows asked with the bookshelf, the
      plus *Ajouter un membre* right of *Membres* adds a last row: *Choisir
      un lecteur* among the readers not members, by display name ignoring
      case and accents, the crown toggle *Propriétaire*, off at first, and
      *Ajouter*, absent until a reader is chosen; *Ajout en cours…*, then
      the new member, *invité* or with the crown *propriétaire*. Every
      reader a member, no plus and *Tous les lecteurs sont déjà membres.*;
      no answer, S9's adding sentence in the row, reader and crown kept; a
      refusal reads as no answer (U04, U05, U07, U08). Unit test of the
      readers' order; both clients tested against `contracteer mock` (D07).
      Realises S5 and S9's adding case; un-skips the frontend tests of S5
      and of S9, adding a member.

- [ ] T084 Frontend: a reader chooses their default.
      Left of the chevron of a row the reader owns, a star in `accent`,
      filled on the default as a mark, outlined elsewhere as the button
      *Choisir comme bibliothèque par défaut*; none on a viewer's row. A tap
      shows *Changement en cours…*, the stars accepting nothing; on the
      answer the stars and *· par défaut* move, the row goes first, and the
      card's next add puts the copy there. No answer reads S9's sentence in
      the tapped row, the stars as they were; a refusal reads as no answer
      (U03, U04, U05, U07, U08). Its client tested against `contracteer
      mock` (D07); the PR body proposes the spec's U03 row with an icon
      button. Realises S7, S8 and S9's default case; un-skips the frontend
      tests of S7, S8 and of S9, choosing the default.

*Done (Tophe, on the Pixel, from the installed app on staging): create
Bibliothèque familiale, make it your default, add the second account of the
family as propriétaire; on that account, the bookshelf is on the home page,
make it the default, scan an ouvrage and add it; on yours, the card of that
ISBN reads Dans Bibliothèque familiale. Then create Salon and add the second
account as invité: on that account, the row of Salon has no star, and its
copies are in the catalogue.*

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

## Questions for the human

- **Staging.** The *Done* of `specs/bookshelves.md`, like those of the
  catalogue and covers specs before it, asks for the installed app on
  staging, while D09 knows one environment, the Kimsufi box. No task depends
  on the answer; the hand check is Tophe's step either way.
- **No spec yet**, so nothing is planned for them: PRD §4.1 beyond the add
  and the listing — viewing and editing an edition, removing a copy, the
  filters and sorts of the list, the edition page — §4.2 search, §4.3
  beyond creating a bookshelf, adding a member and choosing the default —
  removing a member, changing a member's role, adding to a bookshelf other
  than the default, moving and lending a copy, its condition and
  acquisition date — §4.4 reading, §4.5 series tracking, §4.6 wishlist,
  §4.9 import and export, §4.10 administration, and the offline browsing of
  §4.8.
- **A refusal the bookshelves screens never provoke.** The spec says what
  each form shows when Libris does not answer (S9), and the taken name
  (S3), not what it shows when Libris refuses otherwise: a blank name the
  screen never sends, an add of a member answered `400`, `404` or `409`, a
  default answered `400` or `404`, all reachable only by a race with
  another reader. T081, T083 and T084 read such a refusal as no answer and
  show their S9 sentence; say if one should read otherwise.
- **A refused add on the card.** The spec's screen says what the card shows
  when Libris does not answer the add (S5), not when it answers `400` or
  `404`, which the card's own ouvrage and the reader's default bookshelf
  should never draw. T039 reads either as a failure and T040 shows the S5
  sentence for every failure; say if a refusal should read otherwise.
