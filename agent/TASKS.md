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

## Edition form — specs/edition-form.md

Contract: release `v0.15.0` of `camory/libris-api`, after `v0.14.0`: one
read-only operation added, the names of the series the house holds; the
add is unchanged, the form sending the same `NewBook`, its language the
ISO 639-1 code. An operation is added, so the backend goes first: its pin
moves in T119 with the operation, and T120 matches a series' name as PRD
§3 says. The frontend pin moves in T121, once T120 is deployed; each task
after it adds one scenario to the form, which replaces the card of an
ISBN the house lacks on the lookup screen. No other task touches the
contract (D04). The frontend tests of S3 and S4 run green already and
guard every frontend task.

- [ ] T119 Backend: the series names on `v0.15.0`.
      `ApiContractTest` pins `v0.15.0`, the backend's one bump (D04).
      Libris answers the names of every series the house holds, on a
      bookshelf of the reader's or not, each once and none blank, in no
      promised order (D11); the bibliography answers them through a use
      case the controller calls (D02, D12). Contracteer verifies the new
      operation's generated case, `uniqueItems` included, every other
      answer unchanged (D07). The backend's walking skeleton; realises S7
      on the backend; un-skips the backend test of S7.

- [ ] T120 Backend: a series' name matched as PRD §3 says.
      An ouvrage added under a series name lands on the series the house
      holds under that name whatever its capitalisation, its accents and
      the spaces at its ends, which keeps its stored spelling; any other
      name creates a series. The rule is kept atomically where the series
      is found or inserted, the schema's unique index its one
      implementation (D12). Realises S5 on the backend; un-skips the
      backend test of S5, a series of the house; its first case stays
      green.

- [ ] T121 Frontend: the edition form on `v0.15.0`, an unknown ISBN.
      Precondition (human): T120 deployed (D04).
      `vitest.global-setup.ts` pins `v0.15.0`, the frontend's one bump; the
      series' port gains the house's series names, its client tested
      against `contracteer mock` on the `200` the operation declares; the
      interim port and its fake fold into it, the scenario file changing
      for that alone, as #318 records (D04, D07).
      An ISBN no source knows shows, in place of *ISBN inconnu*, which
      leaves the `fr` catalogue, the form shaped like a card: the stand-in,
      *ISBN* and the ISBN-13, *Dans aucune de vos bibliothèques*, the kind
      *Livre* · *BD* · *Manga* with *Livre* chosen, then *Titre* to
      *Résumé* empty, each labelled, no placeholder; *Tome*, *Année* and
      *Pages* on the numeric keyboard, keeping the text as typed; *Résumé*
      of several lines, growing, checked by hand (U04, U06, U08). No button
      yet, T122's. The frontend's walking skeleton; realises S2; un-skips
      the frontend test of S2.

- [ ] T122 Frontend: a new ISBN opens the form, filled.
      An ISBN the house lacks and a source knows shows the form in place of
      the card: each field holding what Libris answered, its kind chosen,
      its covers with their dots and source as on the card, *Auteurs* read
      only, grouped and worded as the card's, absent with none (U06). The
      language's field and the card's *Langue* row say the browser's French
      name of the ISO 639-1 code, the rule unit tested, the `fr`
      catalogue's languages gone (S9, U06). The button *Ajouter à ma
      bibliothèque*, docked above the tab bar while the form runs under it
      (U03, checked by hand), adds as the card's did, so that bookshelf S2
      and S5, bookshelves S7 and covers S1 to S3 stay green; a new lookup
      replaces the form. Realises S1 and S9's word of a code, and keeps S3
      and S4, a held ISBN's card and no form when the sources are down;
      un-skips the frontend test of S1.

- [ ] T123 Frontend: the ouvrage is added as edited.
      The button, on the unknown ISBN's form too, sends the form as the
      reader left it: each text without the spaces at its ends, a field of
      spaces alone as no value, the numbers as numbers, the language as the
      code its word names, ignoring case and accents (S9), the kind chosen
      and the shown cover's source. While it runs the button is busy, *Ajout
      en cours…*, and the fields accept nothing (U04); then the card of the
      fields sent shows with its copy's row, form and button gone. Refused
      or unanswered, *Erreur lors de l'ajout, veuillez réessayer plus
      tard.* shows 8 under the button, docked with it, the form as left and
      open to edits (U05). Realises S5 and S11; un-skips the frontend tests
      of S5 and of S11, both cases.

- [ ] T124 Frontend: a refused field.
      On the add's tap, and not before, each field the domain refuses takes
      the `danger` outline, kept while it has the focus, and its message
      under it; the first refused is focused and scrolled into view, the
      scroll checked by hand; no request leaves (U04, U05). A title blank or
      empty, *Titre manquant*; a volume number, a year or a page count not a
      whole number from 1, *Nombre invalide*; a language word that names no
      language, *Langue inconnue*. Each rule unit tested, every word from
      the `fr` catalogue (U08). Realises S10 field by field; un-skips the
      frontend test of S10 field by field, its six cases.

- [ ] T125 Frontend: the kind's words, and a tome without a series.
      Choosing a kind rewords the form: *Album* for a BD, *Tome* for Livre
      and Manga, the authors' roles in the kind's words, keyed by the kind
      as the card's (U06), proven by the form's component test. A volume
      number with no series is refused on its field, *Tome sans série*,
      *Album sans série* for a BD, its rule unit tested; with several
      refused, each shows its message and the first in the form's order
      takes the focus. Realises S6 and S10 for the series; un-skips the
      frontend tests of S10 *Tome sans série*, *Album sans série* and two
      fields refused.

- [ ] T126 Frontend: the series of the house suggested.
      The form asks Libris for the house's series names; while the series'
      field has the focus and holds text, up to five names that contain it,
      ignoring case, accents and the spaces at its ends, sorted by name in
      the reader's language (D11), show in the suggestions block under it,
      the matched part in semibold (U04); a tap fills the field, the tap or
      leaving the field closes them, the text staying as typed until then.
      The names failing, none shows and the field takes the text as
      without them. The matching rule unit tested; the field brought to the
      top of what the keyboard leaves visible on focus, checked by hand.
      Realises S7's suggestions and S8; un-skips the frontend tests of S7,
      the suggestions and a tap, and of S8.

- [ ] T127 Frontend: Existante or Nouvelle, and the language suggested.
      Once the series' field holds text, its box says *Existante* after a
      check in `accent` when the text names a series of the house, ignoring
      case, accents and the spaces at its ends, else *Nouvelle* in `muted`,
      both `body` semibold, and neither while the names fail (U04, U07).
      The language's field suggests, as the series' does, up to five words
      of a language that contain the text, ignoring case and accents, with
      no word in its box, proven by its component test. Realises S7's word
      and S9's suggestions; un-skips the frontend tests of S7, Existante
      and Nouvelle; S8 stays green.

*Done (Tophe, on the Pixel, from the installed app on staging): scan a book
the house lacks and read its form, filled; change its title, type the first
letters of a series the house holds and tap its suggestion, read Existante;
add it, the card shows the new title and Dans Bibliothèque de Christophe;
scan it again, the card, with the new title. Type an ISBN no source knows,
read the empty form, add it with no title, read Titre manquant on a focused
field scrolled into view; type a volume number alone, read Tome sans série.
Type jap in Langue and tap japonais. Type a summary of several lines and
watch the field grow. Scroll the form: the button stays docked above the
tab bar, and the keyboard covers it; tap Série: it comes to the top, its
suggestions above the keyboard.*

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

## Questions for the human

- **No spec yet**, so nothing is planned for them: PRD §4.1 beyond the
  add, its form, the listing and the edition page — editing an edition
  the house holds, editing its authors, adding an ouvrage with no ISBN,
  removing a copy, the filters and sorts of the list, and on the edition
  page the link to its series and its tags — §4.2 search, §4.3 beyond
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
  show their S9 sentence; say if one should read otherwise.
