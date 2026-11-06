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

## Remove a copy — specs/remove-copy.md

Contract: release `v0.16.0` of `camory/libris-api`, after `v0.15.0`: every
copy's bookshelf gains the reader's role on it, and the removal of a copy
is added, answered `204` or the not-found problem. An addition, so the
backend goes first: its pin moves in T130, which answers the role on every
copy and serves the removal with the thinnest answer, not found whatever
the copy, which is all S3's backend tests ask; T131 then removes a copy
the reader owns, and S1 and S2 go green together, since the edition stays
by itself once only the copy goes. The frontend pin moves in T132, once
T131 is deployed: the trash and its question first, then each task adds
one or two scenarios to the card's rows, the ouvrage's page before the
lookup screen. No other task touches the contract (D04). The edition is
never removed (PRD §3, D11).

- [ ] T130 Backend: the reader's role on every copy, on `v0.16.0`.
      `ApiContractTest` pins `v0.16.0`, the backend's one bump (D04). Every
      copy Libris answers (the ouvrage, the catalogue's page, a series'
      editions, the lookup, the add) names its bookshelf with the reader's
      role on it, `OWNER` or `VIEWER`, read from its memberships (D12) and
      unit tested where computed (D07). The removal is served by a use case
      (D02) answering, for now, not found whatever the id; Contracteer
      verifies both keys, the generated `400` and every copy's new schema.
      Walking skeleton; realises S3; un-skips the backend tests of S3.

- [ ] T131 Backend: an owner removes a copy.
      The removal deletes the copy for good (D11) when its bookshelf is one
      the reader owns, the bookshelf answering that (D12), else not found as
      before. The edition stays with its fields (PRD §3): the lookup of its
      ISBN answers it with no copy, the sources not asked. The catalogue's
      page drops an edition whose copies of the reader's leave between its
      two reads, rather than failing, proven by the use case's test (the
      race the spec asks weighed). Realises
      S1, S2; un-skips the backend tests of S1 and S2, both cases.

- [ ] T132 Frontend: the trash on an owned row, on `v0.16.0`.
      Precondition (human): T131 deployed (D04).
      `vitest.global-setup.ts` pins `v0.16.0`, the frontend's one bump; the
      lookup, add, ouvrage, catalogue and series clients read each copy's
      role against `contracteer mock` (D07). On the card, an owned row
      carries the trash, *Retirer un exemplaire*, `Trash2`, a guest's none;
      a tap turns the row into its question, *Retirer un exemplaire de* the
      bookshelf's name *?*, the cross *Annuler* and the check *Retirer*
      (U01, U04, U06, U07), both inert until T133 and T134. Walking
      skeleton; realises S3; un-skips its frontend test.

- [ ] T133 Frontend: the count on its own line, Annuler, one question.
      Every bookshelf row of the card is 44 tall; a bookshelf holding more
      than one copy says *2 exemplaires* on a second line under its name,
      with no middle dot, proven by the card's tests, the sizes by hand
      (U06); the catalogue rows keep theirs. Edition-page S1's frontend
      test moves with the row as the spec says, declared in the PR (D07).
      *Annuler* shows the row again as it was and sends nothing; another
      row's trash moves the question there (U04). Realises S4 and S5;
      un-skips their frontend tests.

- [ ] T134 Frontend: Retirer removes one copy, on the ouvrage's page.
      The removal reaches Libris through a client of its own, tested
      against `contracteer mock` on its `204`, `400` and `404`; its port
      joins the application's ports, the scenario file handing its fake
      through them, changing for that alone, as #340 records (D05, D07).
      *Retirer* sends one of the row's copies, a spinner in the check's
      place, the row accepting nothing (U04); removed, the page asks the
      ouvrage anew and the row reads *Dans* and the bookshelf's name.
      Realises S1; un-skips its frontend test.

- [ ] T135 Frontend: Libris unavailable, or the copy already gone.
      Libris not answering the removal, the row says *Erreur lors du
      retrait, veuillez réessayer plus tard.* under its question, inside
      it, ready for another try (U05); a `400` the screen never provokes
      reads as no answer, as the bookshelves screens read theirs. Answered
      not found, the copy is taken as removed, the ouvrage asked anew with
      no message. Realises S6 and S7; un-skips their frontend tests.

- [ ] T136 Frontend: the last copy removed, back where the page came from.
      With none of the reader's copies of the ouvrage left, removed or
      answered not found, the screen goes back to the one the page was
      opened from, the page leaving the history so the phone's back button
      does not return to it, checked by hand on the Pixel; opened from its
      address alone, it goes to the catalogue. Realises S2; un-skips its
      frontend tests, both cases.

- [ ] T137 Frontend: removing from the lookup screen.
      The lookup screen's card asks and answers as the ouvrage's page does,
      S3 to S7 holding there, and nothing navigates: the card stays and its
      row drops the copy; with none of the reader's copies left it reads
      *Dans aucune de vos bibliothèques* and the button *Ajouter à ma
      bibliothèque* shows again, after an add too, the added copy carrying
      the role the add answered. Realises S8; un-skips its frontend tests,
      both cases.

*Done (Tophe, on the Pixel, from the installed app on staging): scan One
Piece 1, held once on Bibliothèque de Christophe, add it again, read 2
exemplaires under the bookshelf's name; tap the trash, read the question,
tap Annuler, the row is back; tap the trash and Retirer, read Dans
Bibliothèque de Christophe. In Catalogue, open an ouvrage on two of your
bookshelves and remove one copy, the row updates. Remove your last copy of
an ouvrage and land back on the catalogue, the ouvrage gone from it; the
phone's back button does not return to its page. Scan that ISBN again and
read Dans aucune de vos bibliothèques, its card as the house holds it. On
the second account of the family, a guest of Salon, open an ouvrage on
Salon and see no trash on its row.*

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

## Questions for the human

- **No spec yet**, so nothing is planned for them: PRD §4.1 beyond the
  add, its form, the listing, the edition page and removing a copy —
  editing an edition the house holds, editing its authors, adding an
  ouvrage with no ISBN, the filters and sorts of the list, and on the
  edition page the link to its series and its tags — §4.2 search, §4.3 beyond
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
