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
> Follow-ups go to `agent/PROPOSED.md`; only its *Promoted* entries come
> here, as the phase *Tidy*.

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
- Search, T164 to T176, done 2026-11-12, `specs/search.md`: a box over
  the catalogue's ouvrages narrows it, as the reader pauses, to the
  editions whose title, subtitle, series or authors hold every word typed,
  ignoring case and accents, a word of digits also finding the tome it
  numbers, a whole ISBN with or without hyphens its edition; words that
  find nothing show the nearest under *Les plus proches :*, the cross
  empties the box, only the latest search counts, the search stays in the
  address, and no box sits over an empty catalogue; checked on the Pixel
  from the installed app on staging.

## Tidy — agent/PROPOSED.md

The entries of *Promoted*, each prefixed with the ID of the task that takes
it; the run that ticks a task removes its entries. The bugs come first, the
frontend's before the backend's, then the refactors. No task touches the
contract (D04), and none edits a scenario test: every scenario test running
today stays green and guards each task.

- [x] T177 Frontend: a lookup Libris does not answer no longer hangs.
      Bug, its entry T177. Goes red: a lookup answered `401` does not take
      the reader through the expired session's navigation as `/me` does
      (D06), and a lookup whose fetch is rejected leaves *Chercher* busy and
      the skeleton standing. Green: the `401` goes through the same
      callback the application wires, and a rejected lookup gives the
      button back and takes the skeleton down. No sentence is added: what
      the screen says then is a *Still to spec* line of PRD §4.7 (D05).

- [x] T178 Frontend: the form suggests the names of the latest lookup.
      Bug, its entry T178. Goes red: when the series' and the authors'
      names asked by a first lookup land after those of a second, the
      older answer replaces the newer and the form suggests it. Green: only
      the answer to the latest ask counts, for the series and the authors
      alike (D05).

- [x] T179 Backend: a tome of digits alone in the BnF's record.
      Bug, its entry T179. Goes red: the BnF record of 9782845808461, a
      recording of the BnF's answer, reads no series and no volume number.
      Green: it reads its series and tome 14, a tome of digits alone read
      beside *tome N*; every record recorded before reads as it did. The lookup of fast entry answers (D02, D07).

- [x] T180 Backend: the proxy's headers read whole.
      Bug, its entry T180. Goes red: a blank `Remote-User` makes a reader
      with an empty username, and a `Remote-User`, `Remote-Email` or
      `Remote-Groups` outside ASCII reads mangled. Green: a blank username
      is refused as a blank email is, and every header is decoded from
      UTF-8 as `Remote-Name` is, by the one class that reads them (D06),
      tested on its own (D07).

- [ ] T181 Backend: an add out of the contract's bounds is refused.
      Bug, its entry T181. Goes red: an add whose language, publication
      year, page count or volume number lies outside the bounds of the
      pinned contract answers `500` once the database refuses it. Green:
      the web adapter refuses each with a validation problem naming its
      field, as it refuses the fields it checks today (D02, D11);
      Contracteer stays green (D04, D07).

- [ ] T182 Frontend: the failure message drawn once.
      Refactor, its entry T182: the message of U05, its icon and its
      sentence in danger, is one presentational component with its own
      spec, used by every component and view that draws it by hand (D05).
      Behaviour stays as it is: every view, component and scenario test
      green unchanged.

- [ ] T183 Frontend: every icon button on the one icon button.
      Refactor, its entry T183: the views and components that still write
      the icon button of U04 by hand use the one that exists (D05).
      Behaviour stays as it is: their tests unchanged and green.

- [ ] T184 Frontend: one cover for the catalogue's rows.
      Refactor, its entry T184: the cover of a catalogue row and of a
      series row, its box, its picture falling back on error and its
      stand-in, is one presentational component with its own spec (D05).
      Behaviour stays as it is: both rows' tests unchanged and green.

- [ ] T185 Frontend: one reading of a find's failure.
      Refactor, its entry T185: the finds of a bookshelf, of a catalogue
      series and of a catalogue edition map their answer through one
      mapping, and the pages of a series and of an edition pick their
      failure's words through one (D05). Behaviour stays as it is: the
      composables' and views' tests green unchanged.

- [ ] T186 Frontend: the removal hands back the removed copy.
      Refactor, its entry T186: the removal of a copy tells what follows it
      which copy went, so the ouvrage's page and the lookup screen keep no
      copy of their own (D05). Behaviour stays as it is: the views' tests
      and the removal's scenario tests green unchanged.

- [ ] T187 Frontend: the catalogue's client reads a page once.
      Refactor, its entry T187: the browse and the search of the catalogue
      read their page alike through one reading (D05). Behaviour stays as
      it is: the client's specs against the Contracteer mock green
      unchanged (D07).

- [ ] T188 Frontend tests: the browser's missing scroll defined once.
      Refactor of tests, its entry T188: the test setup defines the scroll
      jsdom lacks, so the components call it plainly and their specs stop
      defining it; the home view's spec gains its Given, When and Then
      (D07). The jsdom gotchas become one item. Behaviour stays as it is:
      `npm test` green, no scenario test edited.

- [ ] T189 Backend: the reader's bookshelves by id built once.
      Refactor, its entry T189: the catalogue's browse, the catalogue's
      series and the held edition's reads build the reader's bookshelves
      by id through one function (D02). Behaviour stays as it is: their
      tests green unchanged.

- [ ] T190 Backend tests: the web slice's mocks declared once.
      Refactor of tests, its entry T190: the mocked use cases of the web
      slice are declared once, on the slice's own annotation, and every
      slice test shares one context (D07). Behaviour stays as it is:
      `./gradlew check` green, as many cases as before.

- [ ] T191 Backend tests: fixtures below their class, a proven case gone.
      Refactor of tests, its entries T191: the four test classes the entry
      names keep their fixtures below the class and its helpers (D10), and
      the advice's case of a bookshelf id that is not a uuid goes, which
      the contract's verification proves (D07). `./gradlew check` green,
      one case fewer.

## Questions for the human

- **No spec yet**, so nothing is planned for them: the *Still to spec*
  lines of PRD §4. Seen while specifying and in no PRD line yet: changing
  an edition's cover, adding an ouvrage with no ISBN, a link from the
  edition page to its series, changing a member's role, renaming a series.
- **A refusal the bookshelves screens never provoke.** The spec says what
  each form shows when Libris does not answer (S9), and the taken name
  (S3), not what it shows when Libris refuses otherwise: a blank name the
  screen never sends, an add of a member answered `400`, `404` or `409`, a
  default answered `400` or `404`, all reachable only by a race with
  another reader. T081, T088 and T084 read such a refusal as no answer and
  show their S9 sentence; say if one should read otherwise. T135 reads
  the removal's `400`, which the screen never provokes, the same way.
