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

## Questions for the human

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
  another reader. T081, T088 and T084 read such a refusal as no answer and
  show their S9 sentence; say if one should read otherwise.
- **A refused add on the card.** The spec's screen says what the card shows
  when Libris does not answer the add (S5), not when it answers `400` or
  `404`, which the card's own ouvrage and the reader's default bookshelf
  should never draw. T039 reads either as a failure and T040 shows the S5
  sentence for every failure; say if a refusal should read otherwise.
