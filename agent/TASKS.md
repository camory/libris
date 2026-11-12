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

## Search — specs/search.md

Contract: release `v0.19.0` of `camory/libris-api`, after `v0.18.0`: the
catalogue's page takes the query parameter `q`, optional, and `BookPage`
gains `near`, required; nothing else changed. A parameter and a response
field are added, so the backend goes first: its pin moves in T164 with a
whole ISBN read, T165 finds the words in an added edition, T166 in a saved
one, T167 reads a tome's number, and T168 answers the near matches. The
frontend pin moves in T169, once T168 is deployed: the box and its first
answer, then the pause, the cross, the sentence of no match, the pages, the
latest search alone, the failure and the address. No other task touches
the contract (D04). The catalogue's and the series list's tests on both
sides, and S10's frontend test, running already, stay green and guard
every task.

- [x] T164 Backend: the search on `v0.19.0`, a whole ISBN.
      `ApiContractTest` pins `v0.19.0`, the backend's one bump (D04). The
      catalogue's page reads the words `q` and answers `near`, false for
      now: absent, empty or blank, the catalogue as before; a query that
      reads as a whole ISBN, its hyphens and spaces ignored, an ISBN-10
      converted (PRD §3), answers the reader's edition of that ISBN alone
      or none (D03); any other query answers none, for now. Contracteer
      verifies the page's shape, `near` included, over its generated cases,
      each carrying a random `q` (D07). Walking skeleton; realises S2;
      un-skips its backend test, its four cases.

- [x] T165 Backend: words find an added edition.
      Each word of a query, ignoring case and accents, is found when it
      begins a word of the edition's title, subtitle, series' name or one
      of its authors' names; every word is required, in any order (D03).
      An edition added keeps what it is searched by with it, and the
      editions stored before are filled by the migration that brings it,
      with only the extension it uses: the trigrams' come with T168 (D02,
      D03). The answer is the catalogue's rows of the reader's editions,
      in its order and its pages of fifty, `after` naming the last one
      received (D11), `near` false. Realises S5, an edition added, and S4;
      un-skips their backend tests.

- [x] T166 Backend: a saved edition found by its new words.
      A save of an edition the house holds, and the add of a held ISBN
      written as a save (held-edition-edit S2), rewrite what it is searched
      by: found by its new title, never by its old one (D03). A name
      respelled for the house changes only what the search ignores, its
      other editions found as before. The `agent/PROPOSED.md` item on the
      search columns goes, answered by T165 and this task. Realises S5, an
      edition whose title a reader changed; un-skips that backend test.

- [x] T167 Backend: a tome found by its number.
      A word of digits alone is also found in the edition whose tome is
      that number, exactly: *one piece 2* answers tome 2, neither tome 12
      nor tome 20 (D03); *tome* and *album* are words like any other. A
      title typed whole, apostrophe included, finds its edition; words of
      two fields find the edition holding both; an edition on another
      reader's bookshelf alone is never answered (PRD §3). Realises S1;
      un-skips its backend test, its six cases, those but the tome perhaps
      green already through T165.

- [x] T168 Backend: near matches when the words find nothing.
      When the words find no edition of the reader's catalogue, Libris
      answers the nearest by their trigrams, said near: *asterik* answers
      the *Astérix* albums; it looks for them only then, so an edition the
      words find never stands among near ones (D03). Near matches come in
      the catalogue's order and pages, each page said near; a page with no
      edition is never near, and a whole ISBN finds its edition or none,
      never a near match (S2). The trigram index and its extension arrive
      here (D02, D03). Realises S3; un-skips its backend test, both cases.

- [x] T169 Frontend: the search box on `v0.19.0`.
      Precondition (human): T168 deployed (D04). `vitest.global-setup.ts`
      pins `v0.19.0`, the frontend's one bump; the search's client over the
      catalogue's page with `q`, tested on the mock's `200` and `400`,
      joins the application's ports, the scenario file's provision moving
      into them (D05, D07). The ouvrages of the catalogue show the search
      field under the switch, scrolling with the list (U04): the box's
      text without the spaces at its ends asks Libris, and the rows of its
      answer's first page replace the catalogue's, in its order. The box
      shows with the loading rows and leaves over an empty catalogue.
      Walking skeleton; realises S1, S10; un-skips S1's frontend test.

- [x] T170 Frontend: the search waits for a pause.
      The box asks Libris 300 ms after the last keystroke, a keystroke
      within them starting the pause again: one request for a word typed
      at speed. The keyboard's search key asks at once and closes the
      keyboard. Fewer than two characters, the spaces at the ends not
      counted, ask nothing and show the whole catalogue. While Libris
      answers, the rows shown stay, with no skeleton, until the answer
      replaces them. Realises S6 but the cross; un-skips its three
      frontend tests, the search key perhaps green already.

- [x] T171 Frontend: the cross empties the search box.
      Once the box holds text, the cross *Effacer la recherche* sits at
      its right, an icon button, and the browser's own clear button never
      shows (U04, U07). A tap empties the box and shows the whole
      catalogue at once, no pause waited, the focus in the box and the
      keyboard open. Realises S6, the cross; un-skips its frontend test.

- [x] T172 Frontend: no ouvrage matches.
      An answer with no edition says *Aucun ouvrage ne correspond à
      « gaston ».*, the query as sent between guillemets, in the list's
      place (U05); an answer said near says it too, then *Les plus
      proches :* labels the block of its rows under it (U03). Realises S3;
      un-skips its two frontend tests.

- [x] T173 Frontend: the next pages of a search.
      Reaching the end of a search's rows asks the next page of the same
      query after the last one's `next` (D11), its rows following the
      first without a gap or a repeat, the catalogue's two skeleton rows
      under the last while it comes (U05); a page naming no next asks
      nothing more. Realises S4; un-skips its frontend test.

- [x] T174 Frontend: only the latest search counts.
      An answer to a search asked before the latest one is dropped,
      whenever it arrives: the screen lists the latest search's answer
      alone, and a next page of an earlier search arriving late is dropped
      too. Realises S8; un-skips its frontend test.

- [x] T175 Frontend: Libris unavailable during a search.
      A search not answered says *Erreur lors de la recherche, veuillez
      réessayer plus tard.* with the alert icon in the list's place, no
      row, the box keeping its text and its outline (U05); a next page not
      answered leaves the rows listed and puts the sentence under them.
      Realises S9; un-skips its two frontend tests.

- [x] T176 Frontend: the search kept in the address.
      The address carries the query of the list shown,
      `/catalogue?q=goscinny`, none under two characters, each search
      replacing the history's entry instead of adding one. An address with
      a query opens on its search, the box holding it and the catalogue's
      loading rows under it until the answer. *Ouvrages* of the switch and
      the *Catalogue* tab open the catalogue with no query (U03, U04).
      Realises S7; un-skips its three frontend tests.

*Done (Tophe, on the Pixel, from the installed app on staging): in
Catalogue, read the box under the switch. Type goscinny: after a pause, the
rows of his ouvrages in the order of the catalogue. Open one and come back:
the box and the rows as left. Type a series and the number of a tome you
hold: that tome alone. Type asterik: the sentence, Les plus proches : and
the Astérix albums. Type gaston: the sentence alone. Type the ISBN of an
ouvrage with its hyphens: that ouvrage alone. Type the title of an ouvrage
stored before the release: it is found. Tap the cross: the whole catalogue,
the keyboard open.*

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
- Held edition edit, T153 to T163, done 2026-11-10,
  `specs/held-edition-edit.md`: a reader corrects an ouvrage the house
  holds, from its page with *Enregistrer* or from the card of a scan that
  finds none of their copies, in the add's form, the ISBN and the cover
  fixed; the card shows the edition as Libris stored it, a series or an
  author retyped in other capitals respelled for the house, a name no
  edition carries no longer suggested; checked on the Pixel from the
  installed app on staging.

## Questions for the human

- **No spec yet**, so nothing is planned for them: PRD §4.1 beyond the
  add, its form and the authors in it, the listing, the edition page,
  removing a copy and editing an edition the house holds — changing an
  edition's cover, adding an ouvrage with no ISBN, the filters and sorts
  of the list, and on the edition page the link to its series and its tags
  — §4.3 beyond creating a bookshelf, adding a member and choosing the
  default — removing a member, changing a member's role, adding to a
  bookshelf other than the default, moving and lending a copy, its
  condition and acquisition date — §4.4 reading, §4.5 beyond the series
  list and the series page — the series details, the tomes after the
  highest owned, the number of published volumes and the completion, the
  series filter and renaming a series — §4.6 wishlist, §4.9 import and
  export, §4.10 administration, and the offline browsing of §4.8.
- **A refusal the bookshelves screens never provoke.** The spec says what
  each form shows when Libris does not answer (S9), and the taken name
  (S3), not what it shows when Libris refuses otherwise: a blank name the
  screen never sends, an add of a member answered `400`, `404` or `409`, a
  default answered `400` or `404`, all reachable only by a race with
  another reader. T081, T088 and T084 read such a refusal as no answer and
  show their S9 sentence; say if one should read otherwise. T135 reads
  the removal's `400`, which the screen never provokes, the same way.
