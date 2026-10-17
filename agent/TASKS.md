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

## Series page — specs/series-page.md

Contract: release `v0.13.0` of `camory/libris-api`, after `v0.12.1`: one
operation added, a series of the reader's catalogue with its editions,
and one schema, `SeriesWithBooks`; `Series` gains its `id`, required. An
operation and a response field are added, so the backend goes first. Its
pin moves once, in T101, the thinnest answer that serves the operation;
T102 finds the series, red on S1's scenario test. The frontend pin moves
from `v0.12.1` in T103, once T102 is deployed; T104 to T108 each add one
scenario to the page. No other task touches the contract (D04).

- [ ] T101 Backend: the series page on `v0.13.0`, no such series.
      `ApiContractTest` pins `v0.13.0`, the backend's one bump (D04).
      Each series of the series list answers its `id`, the row its name
      has in persistence (D12). Libris serves a series by its id and
      answers every reader that there is no such series, `404`
      `/problems/not-found`, until T102 finds it; an id that is not one
      is `400` `/problems/validation`. Contracteer verifies the new
      operation's cases, `404_NOT_IN_MY_CATALOGUE` and the generated ones,
      and the series list with its id, every other answer unchanged (D07,
      D11). The backend's walking skeleton, the thinnest scenario;
      realises S5; un-skips the backend tests of S5, both cases.

- [ ] T102 Backend: the reader opens a series.
      Libris answers the series of that id with its editions on a
      bookshelf the reader belongs to, each once and no other, each a
      `Book` with its copies on those bookshelves, in no promised order,
      the screen sorting them (D11). Its name, count, cover and kind are
      those the series list answers for it, by the same rules (series-list
      S2, S3). A series none of whose editions sits on the reader's
      bookshelves stays no such series. A read across both contexts, the
      series a value of the edition, never an aggregate (D02, D12).
      Realises S1 and keeps S5; un-skips the backend test of S1.

- [ ] T103 Frontend: the series page on `v0.13.0`, no such series.
      Precondition (human): T102 deployed (D04).
      `vitest.global-setup.ts` pins `v0.13.0`, the frontend's one bump; a
      series carries its `id`, required, and the series port gains the
      find by id, its client tested against `contracteer mock` on every
      response the operation declares; the page's interim port and fake
      fold into it, the scenario files changing for that and the id alone,
      as #282 records (D04, D07). `/catalogue/series/{id}`, under the
      catalogue, asks the series on each arrival: the spec's loading
      skeletons, then for no such series or an id that is not one *Série
      introuvable.* as the screen's one message, no title (U03, U05, U08).
      The frontend's walking skeleton, the thinnest scenario; realises S5;
      un-skips the frontend tests of S5, both cases.

- [ ] T104 Frontend: the reader opens a series.
      A row of the series list is a link to its page, the whole row, no
      chevron; the home page's bookshelf rows lose theirs (U03, U07). The
      page's title is the series' name, its hint the count then *dans vos
      bibliothèques.* (*3 albums dans vos bibliothèques.*); then the
      catalogue's block of its editions in the order a shelf reads, by
      tome, an edition without a tome after the numbered ones, then by
      title, ignoring case and accents, whatever the answer's order (D11);
      each the catalogue's row, its overline *Tome 23* or *Album 23* for a
      BD, none without a tome, leading nowhere (U02, U08). Unit test of the
      order. Realises S1; un-skips the frontend test of S1.

- [ ] T105 Frontend: the owned tomes and the gaps.
      Under the header, a card of field rows (U06): *Tomes possédés*, the
      runs owned, and *Tomes manquants*, the runs missing from 1 to the
      highest tome owned, no row when none is; *Albums* for a BD. A run of
      three or more reads as its ends (*26 à 35*), of two as both (*4,
      5*), a tome alone as itself; an edition without a tome fills none,
      two of one tome fill it once. Among the editions, in its place, a
      row per missing run on `bg`, a dashed `muted` outline for its cover,
      *Tomes 26 à 35* over *Manquants*, *Tome 4* over *Manquant* (U02,
      U08). Unit tests of the runs and the gaps, one per case of the spec.
      Realises S2; un-skips the frontend tests of S2, its three cases.

- [ ] T106 Frontend: the authors and the publishers.
      The card gains *Auteur*, or *Auteurs* for more than one: the series'
      authors once each, an author who only translated left out, by first
      role then by name as the card orders them (U06); and *Éditeur* or
      *Éditeurs*: each publisher once, by name. Both ignore case and
      accents, an author the same one by name (PRD §3), no row when none
      is left. Unit tests of the authors and of the publishers.
      Realises S3; un-skips the frontend test of S3.

- [ ] T107 Frontend: the band of a series.
      Above the header, the series' cover as a band (U03): cropped from
      its top, the column's full width, 130 tall, named *Couverture de*
      and the series' name; the header 12 under it in place of 20, and
      the card right after the header's 12, with no other space. No band
      when the series has no cover or its cover does not load. The crop,
      the height and the spacing are checked by hand on the Pixel.
      Realises S4; un-skips the frontend tests of S4, its three cases.

- [ ] T108 Frontend: Libris unavailable on a series.
      When the series does not come: no band, no title, *Erreur lors du
      chargement, veuillez réessayer plus tard.* with the alert icon as
      the screen's one message, nothing of the series shown; every word
      from the `fr` catalogue (U05, U07, U08).
      Realises S6; un-skips the frontend test of S6.

*Done (Tophe, on the Pixel, from the installed app on staging): in
Catalogue, Séries, tap Tintin, owning tomes 20 and 21; read Tomes manquants
1 à 19, the run of missing tomes as the first row, and the cover of tome 20
as the band; go back with the phone's button and land on the series list;
tap Dragon Quest, owning tome 30, and read 1 à 29 missing. On the second
account of the family, paste the address of Tintin's page and read Série
introuvable.*

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

## Questions for the human

- **No spec yet**, so nothing is planned for them: PRD §4.1 beyond the add
  and the listing — viewing and editing an edition, removing a copy, the
  filters and sorts of the list, the edition page — §4.2 search, §4.3
  beyond creating a bookshelf, adding a member and choosing the default —
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
