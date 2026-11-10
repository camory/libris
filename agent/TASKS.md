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

## Held edition edit — specs/held-edition-edit.md

Contract: release `v0.18.0` of `camory/libris-api`, after `v0.17.0`: one
operation added, the save of an edition the house holds, its body
`EditionDetails` split from `Edition` with no shape changed; the add of an
ISBN the house holds now writes the fields sent, and the series' and the
authors' names are those some edition carries, both said in reworded
descriptions. An operation is added, so the backend goes first: its pin
moves in T153 with the save answering not found, T154 writes the fields
sent, T155 drops from the names those no edition carries, T156 and T157
respell an edition's own series and authors, and T158 writes the add of a
held ISBN as the save does. The frontend pin moves in T159, once T158 is
deployed: the pencil of the lookup's card first, then the ouvrage's page,
its form, its cross, its save and its failure. No other task touches the
contract (D04). The frontend tests of edition-form and authors-edit, and
S1's second case here, edition-form's S4 moved (#379), stay green and
guard every frontend task.

- [x] T153 Backend: the save on `v0.18.0`, an unknown edition refused.
      `ApiContractTest` pins `v0.18.0`, the backend's one bump (D04). The
      save of an edition is served by a use case the controller calls
      (D02), answering, for now, not found whatever the id and storing
      nothing; an id that is not one is the validation problem. Contracteer
      verifies both keys, the generated `400` cases and every other answer
      unchanged (D07). Walking skeleton; realises S7; un-skips its backend
      test.

- [ ] T154 Backend: the ouvrage saved as edited.
      An edition the house holds is written with the fields sent over its
      own, kind to summary, its ISBN and its cover kept, the last save
      winning (D11); its series found or inserted by name (D12) and its
      authors matched as the add matches them (PRD §3), so a name of the
      house lands on it, kept. Libris answers it with the reader's copies,
      none included (D12). The `agent/PROPOSED.md` item on the edition's
      `update` goes, answered by S6. Realises S4 and S5's other names;
      un-skips the tests of S4, both, and S5's other series and author.

- [ ] T155 Backend: a name no edition carries is not suggested.
      The series' names and the authors' names answer only those some
      edition of the house carries, each once, in no promised order (D11);
      the house keeps the others, and an edition that names one again lands
      on it under its spelling (D12). Contracteer verifies both operations
      on their shape as before (D07). Realises S6; un-skips its backend
      test.

- [ ] T156 Backend: an edition's own series respelled for the house.
      A save whose series' name matches the edition's own series, ignoring
      case, accents and end spaces (PRD §3), gives that series the spelling
      sent, for every edition of it: the house holds no second series of
      it, and no respelling merges two (D12). Any other name lands as T154
      says. Kept atomically where the series is found or inserted (D12),
      the rule unit tested where it is decided (D07). Realises S5, its
      series; un-skips that backend test.

- [ ] T157 Backend: an edition's own author respelled for the house.
      A save naming an author who matches one of the edition's own authors,
      as PRD §3 matches names and whatever roles the save gives them, gives
      that author the spelling sent, for every edition carrying them (D12);
      the house holds no second author of it, and any other name lands as
      T154 says. The rule unit tested where it is decided (D07). Realises
      S5, its author; un-skips that backend test.

- [ ] T158 Backend: a held ISBN added as edited.
      The add of an ISBN the house holds writes the fields sent over the
      house's as the save does, its own names respelled as T156 and T157
      say, and puts the copy on the bookshelf as before (PRD §4.1); the
      stored cover stays, the cover source ignored. An add of what the
      lookup answered changes nothing; the add of an ISBN the house lacks
      creates the edition, as before, and never respells. Realises S2 and
      S5, the add of a held ISBN; un-skips their backend tests.

- [ ] T159 Frontend: the save on `v0.18.0`, the pencil of a held ISBN.
      Precondition (human): T158 deployed (D04). `vitest.global-setup.ts`
      pins `v0.18.0`, the frontend's one bump; the editions' client, tested
      on the mock's `200`, `400` and `404`, joins the application's ports,
      the scenario file's provision moving into them (#379, D05, D07). The
      lookup's card of an edition the house holds with none of the reader's
      copies carries the pencil *Modifier*, its form the house's fields and
      cover, and adds it as edited; a card with a copy of the reader's has
      none (U04, U06). Walking skeleton; realises S1, S2; un-skips their
      frontend tests.

- [ ] T160 Frontend: the pencil opens the form on the ouvrage's page.
      The page's card carries the pencil *Modifier* in its top right corner
      whatever the reader's role; a tap shows in its place the lookup's
      form filled as Libris answered, the authors as rows, the stored cover
      or its stand-in and the ISBN under its label, neither edited, the
      bookshelf rows without trash, the cross *Annuler les modifications*
      (U04, U06), and *Enregistrer* docked above the tab bar (U03); the
      `h1` stays the title as answered, visually hidden (U02). Realises S3;
      un-skips its frontend test.

- [ ] T161 Frontend: the cross drops the edits.
      On the ouvrage's page the cross drops the edits with no question, as
      edition-form S13: the card shows again as Libris answered it, its
      pencil and its trash back, *Enregistrer* gone, and the pencil reopens
      the form as answered. Realises S8; un-skips its frontend test.

- [ ] T162 Frontend: the ouvrage saved as edited.
      *Enregistrer* saves the form through the editions' port: each text
      field without the spaces at its ends, spaces alone as no value, the
      authors as authors-edit S5 and S9 say, neither the ISBN nor the
      cover. While it runs, the button reads *Enregistrement en cours…*
      with its spinner and the form, its cross included, accepts nothing
      (U04). Saved, the card shows the edition as Libris answered it, with
      its copies and its pencil, the form and the button gone. Realises S4;
      un-skips its frontend test.

- [ ] T163 Frontend: a refused or failed save.
      A save answered by the validation or the not-found problem, or not
      answered, says *Erreur lors de l'enregistrement, veuillez réessayer
      plus tard.* 8 under the button, docked with it, the form left as the
      reader left it, open to edits (U04, U05). Before any request the tap
      checks the fields and the authors as the add's does, the first
      refused focused, nothing sent (edition-form S10, authors-edit S8,
      S9). Realises S9; un-skips its four frontend tests, the title emptied
      perhaps green already through the form's own check.

*Done (Tophe, on the Pixel, from the installed app on staging): in
Catalogue, open an ouvrage and tap the pencil; read its form filled, its
authors as rows, its bookshelf row without trash, Enregistrer docked above
the tab bar. Change its title, tap the cross, read the card as it was. Tap
the pencil, change its title, retype its series in other capitals, tap
Enregistrer: the card shows the new title and the series as typed; in
Séries, the series reads the new spelling. Open an ouvrage of another
series, type the name of a series of the house in small letters, save: the
card reads it under the house's spelling. Remove the last copy of an
ouvrage from its page and scan its ISBN: the card says Dans aucune de vos
bibliothèques and carries the pencil; tap it, change its title, add it:
the card shows the new title and Dans Bibliothèque de Christophe. Scan an
ouvrage you hold: its card has no pencil.*

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
- Edition page, T111 to T115, done 2026-10-22, `specs/edition-page.md`:
  a row of the catalogue or of a series' page opens on its ouvrage's
  page, the card of an edition the house holds, its title the screen's
  heading, the reader's bookshelves holding a copy; no such ouvrage and
  Libris unavailable each say so alone; checked on staging.
- Summary, T116 to T118, done 2026-10-31, `specs/summary.md`: the card,
  on the lookup and on an ouvrage's page, shows the summary the BnF gives,
  one paragraph per line, folded after six lines under *Lire la suite*;
  the editions stored before it gained theirs at start; checked on the
  Pixel from the installed app on staging.
- Edition form, T119 to T129, done 2026-11-04, `specs/edition-form.md`:
  an ISBN the house lacks shows its card, whose pencil opens the form
  filled, and an ISBN no source knows opens it empty; the form marks
  each refused field, suggests the house's series and the languages, says
  *Existante* or *Nouvelle*, and adds the ouvrage as edited; checked on
  the Pixel from the installed app on staging.
- Remove a copy, T130 to T137, done 2026-11-07, `specs/remove-copy.md`:
  an owner removes one copy from a row of the card, on the ouvrage's page
  and on the lookup screen, through the row's trash and its question; the
  edition stays the house's, the last copy taking the page back where it
  came from; checked on the Pixel from the installed app on staging.
- Scan beep, T138 to T141, done 2026-11-08, `specs/scan-beep.md`: three
  quick taps on the lookup screen's title turn on a checkout's beep at
  each camera read, the same trick turning it off in silence, the phone
  keeping it as the trick left it; checked on the Pixel from the
  installed app on staging.
- Authors edit, T142 to T152, done 2026-11-09, `specs/authors-edit.md`:
  in the form of an ISBN the house lacks, the reader adds, removes and
  respells the authors and chooses each one's roles, the house's authors
  suggested with *Existant* or *Nouveau* and a name matched to the
  house's ignoring case, accents and end spaces, a row without a role or
  named twice refused; checked on the Pixel from the installed app on
  staging.

## Questions for the human

- **No spec yet**, so nothing is planned for them: PRD §4.1 beyond the
  add, its form and the authors in it, the listing, the edition page,
  removing a copy and editing an edition the house holds — changing an
  edition's cover, adding an ouvrage with no ISBN, the filters and sorts
  of the list, and on the edition page the link to its series and its
  tags — §4.2 search, §4.3 beyond
  creating a bookshelf, adding a member and choosing the default —
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
  show their S9 sentence; say if one should read otherwise. T135 reads
  the removal's `400`, which the screen never provokes, the same way.
