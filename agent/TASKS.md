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

## Edition page — specs/edition-page.md

Contract: release `v0.14.0` of `camory/libris-api`, after `v0.13.0`: one
operation added, an edition of the reader's catalogue with its copies on
their bookshelves, answered as the catalogue's `Book`; no schema changes.
An operation is added, so the backend goes first. Its pin moves once, in
T111, the thinnest answer that serves the operation; T112 finds the
ouvrage, red on S1's scenario test. The frontend pin moves from `v0.13.0`
in T113, once T112 is deployed; T114 and T115 each add one scenario to
the page. No other task touches the contract (D04).

- [x] T111 Backend: the edition page on `v0.14.0`, no such ouvrage.
      `ApiContractTest` pins `v0.14.0`, the backend's one bump (D04).
      Libris serves an edition by its id and answers every reader that
      there is no such ouvrage, `404` `/problems/not-found`, until T112
      finds it; an id that is not one is `400` `/problems/validation`.
      Contracteer verifies the new operation's cases,
      `404_NOT_IN_MY_CATALOGUE` and the generated ones, every other answer
      unchanged (D07, D11). The backend's walking skeleton, the thinnest
      scenario; realises S2; un-skips the backend tests of S2, both cases.

- [x] T112 Backend: the reader opens an ouvrage.
      Libris answers the edition of that id as the catalogue answers it, a
      `Book`, with its copies on the bookshelves the reader belongs to, at
      least one, and no other copy, each with its bookshelf's name, in no
      promised order (D11); its series and tome as the catalogue reads
      them. An edition none of whose copies sits on a bookshelf of the
      reader's stays no such ouvrage. A read across both contexts: the
      bibliography answers the edition, the library its copies on top of
      it, through a use case the controller calls (D02, D12).
      Realises S1 and keeps S2; un-skips the backend test of S1.

- [x] T113 Frontend: the edition page on `v0.14.0`, no such ouvrage.
      Precondition (human): T112 deployed (D04).
      `vitest.global-setup.ts` pins `v0.14.0`, the frontend's one bump; the
      catalogue's port gains the find by id, its client tested against
      `contracteer mock` on every response the operation declares, `200`,
      `400` and `404`; the page's interim port and fake fold into it, the
      scenario file changing for that alone, as #301 records (D04, D07).
      `/catalogue/ouvrages/{id}`, under the catalogue with its tab current,
      asks the ouvrage on each arrival: no title and the card's skeleton,
      its top part, one bookshelf row and three field rows; then for no
      such ouvrage or an id that is not one *Ouvrage introuvable.* with the
      alert icon as the screen's one message, no title (U03, U05, U08).
      The frontend's walking skeleton, the thinnest scenario; realises S2;
      un-skips the frontend tests of S2, both cases.

- [ ] T114 Frontend: the reader opens an ouvrage.
      A row of the catalogue and a row of a series' page are each a link
      to their ouvrage's page, the whole row, no chevron (U03). The page
      has no header: its first block, 20 under the top of the content, is
      the card as it shows an edition the house holds, the stored cover or
      its stand-in, neither dots nor a source name, no button under it
      (U06); its card title the screen's `h1`, the overline outside it
      (U02, U08); its rows *Dans* each bookshelf of the reader holding a
      copy, with the count past one; its field rows and its summary. The
      lookup's card reads as before. Going back with the phone's button is
      checked by hand on the Pixel. Realises S1; un-skips the frontend
      tests of S1, both cases.

- [ ] T115 Frontend: Libris unavailable on an ouvrage.
      When the ouvrage does not come: no title, *Erreur lors du
      chargement, veuillez réessayer plus tard.* with the alert icon as
      the screen's one message, nothing of the ouvrage shown; every word
      from the `fr` catalogue (U05, U07, U08).
      Realises S3; un-skips the frontend test of S3.

*Done (Tophe, on the Pixel, from the installed app on staging): in
Catalogue, tap One Piece tome 1 and read its card, its title at the top and
the bookshelves holding it; go back with the phone's button and land on the
catalogue; in Séries, tap Tintin, then tome 20, and read its card. On the
second account of the family, paste the address of tome 20's page and read
Ouvrage introuvable.*

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
- Series list, T092 to T099, done 2026-10-16, `specs/series-list.md`: the
  catalogue switches between its ouvrages and the series the reader's
  bookshelves hold, one row per series with the cover of its first edition
  and its count in tomes or albums, the kind every edition shares;
  checked on the Pixel from the installed app on staging.
- Icons, T100, done 2026-10-16, `docs/DESIGN.md`: every icon is Lucide's,
  from `@lucide/vue`, at the size and colour the hand-drawn one had, the
  hand-drawn files deleted; checked on the Pixel on staging.
- Series aggregate, T109 to T110, done 2026-10-18, `docs/ARCHITECTURE.md`:
  the bibliography holds the edition an ouvrage adds, `HoldEdition`, and
  the series is an aggregate there, `Series(id, name)`, found or inserted
  by its name; no behaviour changed. Deployed on staging.
- Series page, T101 to T108, done 2026-10-19, `specs/series-page.md`: a
  series of the series list opens on its page, the band of its lowest
  tome's cover, the owned tomes or albums in runs and the missing ones,
  its authors and publishers, the reader's editions with the gaps between
  them; no such series and Libris unavailable each say so alone; checked
  on the Pixel from the installed app on staging.

## Questions for the human

- **No spec yet**, so nothing is planned for them: PRD §4.1 beyond the add,
  the listing and the edition page — editing an edition, removing a copy,
  the filters and sorts of the list, and on the edition page the link to
  its series and its tags — §4.2 search, §4.3 beyond creating a
  bookshelf, adding a member and choosing the default —
  removing a member, changing a member's role, adding to a bookshelf other
  than the default, moving and lending a copy, its condition and
  acquisition date — §4.4 reading, §4.5 beyond the series list and the
  series page — the series details, the tomes after the highest owned,
  the number of published volumes and the completion, the series filter
  and renaming a series — §4.6 wishlist, §4.9 import and export,
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
- **The sentences that say a row leads nowhere.** `specs/catalogue.md`
  ("A row leads nowhere yet") and `specs/series-page.md` ("row leads
  nowhere: the edition page comes with its own spec") stop being true with
  T114, and #299 says the task that makes the rows links rewrites them. A
  headless run edits no spec but its *Tasks* section, so T114 leaves them
  as they are; say if it should rewrite them, or rewrite them with the
  phase's *Done*.
