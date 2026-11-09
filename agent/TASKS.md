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

## Authors edit — specs/authors-edit.md

Contract: release `v0.17.0` of `camory/libris-api`, after `v0.16.0`: one
read-only operation added, the names of the authors the house holds; the
add is unchanged, the form sending the same `NewBook`, one `Author` per
name and role. An operation is added, so the backend goes first: its pin
moves in T142 with the operation, and T143 matches an author's name as PRD
§3 says. The frontend pin moves in T144, once T143 is deployed; each task
after it adds one scenario to the authors of the form of an ISBN the house
lacks, which replace its read-only *Auteurs* in the same place. No other
task touches the contract (D04). The frontend tests of edition-form, S12
without its line on *Eiichirō Oda* (#363), stay green and guard every
frontend task.

- [x] T142 Backend: the authors' names on `v0.17.0`.
      `ApiContractTest` pins `v0.17.0`, the backend's one bump (D04).
      Libris answers the names of every author the house holds, on a
      bookshelf of the reader's or not, each once and none blank, in no
      promised order (D11), through a use case of the bibliography the
      controller calls (D02, D12). Contracteer verifies the operation's
      generated case, every other answer unchanged (D07). The backend's
      walking skeleton; realises S6 on the backend; un-skips its test.

- [x] T143 Backend: an author's name matched as PRD §3 says.
      An ouvrage added under an author's name is linked to the author the
      house holds under it whatever its capitals, accents and end spaces,
      the stored spelling kept; any other name creates an author. Kept
      atomically where the name is found or inserted, as the series' name
      is (D12); no author stored on 2026-11-08 folds onto another. The
      contributions keep one per author and role, two names matching so
      kept once, unit tested (D12). The item of `agent/PROPOSED.md` on
      authors unique on `LOWER(name)` goes, answered. Realises S5; un-skips
      its backend test.

- [x] T144 Frontend: the authors on `v0.17.0`, open as rows.
      Precondition (human): T143 deployed (D04). `vitest.global-setup.ts`
      pins `v0.17.0`, the frontend's one bump; the authors' port gets its
      client, tested on the mock's `200`, and joins the application's
      ports, the scenario file's provision moving into them (#363, D04,
      D05, D07). Under *Auteurs*, shown with no author too, one box per
      author in the card's order: its name, named *Nom de l'auteur*, its
      roles' line, `aria-expanded`, in the kind's words, a chevron (U04,
      U06, U07), named *Rôles* followed by the roles it reads, *Rôles*
      alone when it reads none. Walking skeleton; realises S1; un-skips
      its two cases.

- [x] T145 Frontend: an author is added.
      *Ajouter un auteur*, under the last box or the label, adds a box
      after the last, its name empty and focused, the kind's first role
      chosen, texte for a Livre, scénario for a BD or a Manga; no box ever
      moves. The add sends the boxes in order, each name once per role
      chosen. A box, as every field of the app, takes the `accent` outline
      while it has the focus, checked by hand (U01, U04). Realises S2;
      un-skips the frontend tests of S2, an unknown ISBN and after the others.

- [x] T146 Frontend: an author is removed.
      The trash at the right of a name, Lucide `Trash2`, *Retirer* followed
      by the name, *Retirer l'auteur* while it is empty, removes its box at
      once with no question, the focus going to *Ajouter un auteur*; the
      add sends the authors left, and *Annuler les modifications* brings
      every author back (U04, U07). While the add runs, the names, the
      trashes and *Ajouter un auteur* accept nothing, as the fields do.
      Realises S3; un-skips the frontend test of S3.

- [x] T147 Frontend: an author's roles chosen.
      A tap on a roles' line opens under its box the list *Rôles*: the four
      roles in the kind's words and the card's order, each a toggle,
      `aria-pressed`, checked in `accent` when chosen, open while the reader
      chooses, closed by its line or anywhere else, the box in `accent` and
      the chevron up meanwhile (U04, U07). A change of kind rewords the
      roles and keeps them (edition-form S6). The add sends the roles
      chosen; the lines accept nothing while it runs. Realises S4; un-skips
      the frontend test of S4.

- [x] T148 Frontend: each name sent trimmed, an empty row dropped.
      The add sends each author's name without the spaces at its ends, and
      no author for a box whose name is empty or holds spaces alone,
      whatever its roles, with no message; the rule unit tested in the
      domain (D05, D07). Realises S5 on the frontend and S9; un-skips the
      frontend tests of S5 and of S9, both cases; S9's spaces alone with no
      role starts green and guards T151's refusal.

- [x] T149 Frontend: the authors of the house suggested.
      While a name has the focus and holds text, up to five of the house's
      author names containing it, ignoring case, accents and end spaces,
      sorted in the reader's language (D11), show under its box as the
      series' do, its rule serving unchanged (U04); a tap puts one in place
      of the text, the tap or leaving the name closing them; the names
      failing, none shows. A name taking the focus brings its box to the
      top of what the keyboard leaves, checked by hand. Realises S6's
      suggestions and S7; un-skips their frontend tests, S6 the suggestions.

- [ ] T150 Frontend: Existant or Nouveau.
      Once a name holds text, its line says *Existant* after a check in
      `accent` when it names an author of the house, ignoring case,
      accents and end spaces, else *Nouveau* in `muted`, 8 before the
      trash, and neither while the names fail (U04, U07). Realises S6's
      word; un-skips the frontend tests of S6, Existant, Nouveau and a
      tap, which ends on *Existant*; S7 stays green.

- [ ] T151 Frontend: an author without a role refused.
      On the add's tap, with the fields of edition-form S10, a box with a
      name and no role takes the `danger` outline whole, kept with the
      focus, *Rôle manquant* 8 under it; the first refused in the form's
      order is focused, a box on its roles' line, and scrolled into view,
      checked by hand; no request leaves; an empty name is never refused
      (U04, U05, U08). The rule unit tested. Realises S8, a missing role;
      un-skips the frontend test of S8, Rôle manquant; S9 stays green.

- [ ] T152 Frontend: an author named twice refused.
      Of two boxes whose names match, ignoring case, accents and end
      spaces, whatever their roles, the second is refused, *Auteur en
      double* 8 under it, its name focused, shown and ordered as T151's
      refusal; no request leaves (U04, U05). The rule unit tested. Realises
      S8, a name twice; un-skips the frontend test of S8, Auteur en double.

*Done (Tophe, on the Pixel, from the installed app on staging): scan an
Astérix album the house lacks and tap the pencil; read two rows, René
Goscinny on scénario, then Albert Uderzo on dessin. Tap Ajouter un auteur:
a row at the end, scénario under its name, the name focused and brought to
the top; type gosc, read Nouveau and the suggestion René Goscinny above
the keyboard; tap it, read Existant. Tap Ajouter à ma bibliothèque, read
Auteur en double under that row, its name focused and scrolled into view;
type a name of your own instead, open its roles, release scénario, add,
read Rôle manquant under the row; choose couleurs. Respell the first row
rene goscinny. Tap Ajouter un auteur twice, remove one of the two new rows
with its trash, no question asked, and leave the other empty. Add: the
card shows rene goscinny, your colourist, Albert Uderzo and Dans
Bibliothèque de Christophe, and no empty author. Scan it again: the card
says René Goscinny, the house's spelling.*

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

## Questions for the human

- **No spec yet**, so nothing is planned for them: PRD §4.1 beyond the
  add, its form and the authors in it, the listing, the edition page and
  removing a copy — editing an edition the house holds, its authors
  included, adding an ouvrage with no ISBN, the filters and sorts of the
  list, and on the edition page the link to its series and its tags —
  §4.2 search, §4.3 beyond
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
