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

## Summary — specs/summary.md

Contract: no release. `IsbnLookup` and `Book` carry `summary`, a string or
`null`, since the bookshelf spec, so no task bumps a pin and no task waits
on another side's deploy (D04). The backend reads the summary on the
lookup first, T116; T117 fills the stored editions with that same reading.
The frontend's one task, T118, draws the card's summary from the line feeds
it already receives.

- [ ] T116 Backend: the BnF gives a summary.
      The bibliography's BnF source answers the summary of its record's
      field 330 `$a`, cleaned as S1 lists, each clause its own unit test:
      markup dropped and its entities decoded; a paragraph end, a markup
      line break or a line feed, or a run of them, one line feed; a run of
      spaces one space, a lone non-breaking space kept; trimmed; a space
      after `.`, `!`, `?` or `…` glued to a capital or `•`. A summary that
      cleans to nothing is `null`; Open Library still answers none, and the
      BnF's own defects stay as they come. No length limit.
      Realises S1; un-skips the backend test of S1, both cases (D02, D07).

- [ ] T117 Backend: stored editions gain their summary.
      At start, every stored edition with an ISBN and no summary asks the
      BnF alone once and keeps the summary it gives, cleaned as T116 cleans
      it; an edition without an ISBN, or with a summary, is not asked and
      stays as it was. The BnF having none or failing leaves the edition
      without one, is never asked again, and never refuses the start. A
      Kotlin migration in the numbering of the SQL files, through the
      lookup port the application already uses (D02, D11).
      Realises S4; un-skips the backend tests of S4, its three cases.

- [ ] T118 Frontend: the summary in paragraphs, folded.
      The card, on the lookup and on the ouvrage's page alike, shows the
      summary one paragraph per line of its text, 8 between two
      paragraphs, still its last part (U06). Past six lines it stops on an
      ellipsis with the text button *Lire la suite* right under it, its
      word on the summary's left edge, from the `fr` catalogue; a tap shows
      the whole summary and the button goes for as long as the card is on
      screen, no *Réduire* (U04, U06, U08). Six lines or fewer show whole,
      no button. Realises S2 and S3; un-skips the frontend tests of S2,
      both screens. S3 has no scenario test (#311): its proof is the card's
      component test of the tap showing the whole summary and removing the
      button, and the fold at six lines and its absence for a short summary
      are checked by hand on the Pixel.

*Done (Tophe, on the Pixel, from the installed app on staging): scan Mont
Tombe, 9782344008980, and read its summary in paragraphs, cut after six
lines on …, Lire la suite under it; tap it and read the whole summary, the
button gone; scan a book whose summary is short and read it whole, with no
button. Open an ouvrage of the catalogue stored before the release whose
BnF record carries a summary, and read it on its page.*

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
