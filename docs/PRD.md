# Libris — Product Requirements (DRAFT v0.3, 2026-09-11)

> Status: **draft for review by Tophe**. The agentic loop treats this file as
> the source of truth for *what* to build. Edit it freely before the first run;
> after that, change it deliberately and add tasks for the delta.

## 1. Purpose

Libris is the family's private library catalogue. It answers, from a phone in
a bookshop or from the sofa:

- Do we already own this? Which volume? Where is it?
- What is missing from this series?
- Who is reading what, and what did they think of it?
- What do we want next (wishlist), and who did we lend that book to?

It is used by a handful of people (the household), in French, mostly on
phones. It is not a social network, not a store, not a public site.

## 2. Users

| Role | Who | Can |
|------|-----|-----|
| Admin | Tophe | Everything, plus reader profiles and settings |
| Reader | Family members | Browse and search their bookshelves, add and edit ouvrages, manage own reading state and wishlist |

Accounts live in the household's existing Authelia instance; Libris creates a
reader profile on a user's first visit. No public sign-up, no passwords in Libris.

## 3. Domain model

The words below are the ones code, contract and documents use. Screens say
the French word given after *On screen*; it is a label for the family, not a
translation of the code name. The words under *Avoid* were considered and
rejected: do not reintroduce them.

### Glossary

**Edition**:
One published version of a work, the thing an ISBN-13 identifies. Two editions
of the same novel are two editions. Has a kind.
*On screen*: ouvrage (kind-neutral); livre, BD, manga by kind.
_Avoid_: item, book (as the entity), publication, work, volume.

**Kind**:
What an edition is: `BOOK` (any book that is neither of the other two), `BD`,
`MANGA`. Decides the words on screen (livre, album, tome) and the author roles
the form offers.
_Avoid_: type, category, genre, format.

**Series**:
A named, ordered set of editions (*One Piece*, *Astérix*). Singular and plural.
*On screen*: série.
_Avoid_: collection, saga, cycle, serie.

**Volume number**:
An edition's position in its series.
*On screen*: tome.
_Avoid_: volume (alone), issue, number.

**Collection**:
The publisher's collection printed on the spine (*Folio*, *Shonen manga*). A
text on the edition, not a series.
*On screen*: collection.
_Avoid_: series, imprint, label.

**Author**:
A person who took part in making an edition, whatever their role. One display
name, *René Goscinny*, never split.
*On screen*: auteur.
_Avoid_: contributor, creator, person, artist (as the entity).

**Role**:
What an author did on an edition: `WRITER`, `ARTIST`, `COLOURIST`,
`TRANSLATOR`.
*On screen*: auteur or scénariste, dessinateur, coloriste, traducteur.
_Avoid_: function, credit, job.

**Tag**:
A free label a reader puts on an edition (*policier*, *shōnen*, *jeunesse*).
There is no closed list and no separate notion of genre.
*On screen*: tag.
_Avoid_: genre, category, subject, keyword, label.

**Reader**:
A person of the household, created on first visit from the Authelia account.
*On screen*: lecteur.
_Avoid_: user, account, member (as the entity), profile.

**Bookshelf**:
A named place where copies sit, with members. *Chambre de Léa*, *salon*,
*cave*.
*On screen*: bibliothèque.
_Avoid_: library, shelf, location, collection.

**Member**:
A reader's part in a bookshelf, with a role: `OWNER` or `VIEWER`.
*On screen*: propriétaire, invité.
_Avoid_: user, participant, guest (in code), share.

**Copy**:
One physical instance of an edition, sitting on one bookshelf.
*On screen*: exemplaire.
_Avoid_: item, instance, book, copie.

Shaped with their own feature, later: **Reading state** (a link between a
reader and a copy: to read, reading, finished, abandoned, with rating and
notes),
**Loan** (a copy temporarily out of the house, or borrowed from outside),
**Wish** (a reader wants an edition; a fact of its own, not "zero copies").

### Relationships and rules

- An Edition has a kind, at most one series with a volume number, zero or
  more authors each with a role, zero or more tags, and optionally a
  collection, a cover URL and every descriptive field the lookup sources give
  (subtitle, publisher, publication year, language, page count, summary).
- An Edition may have one ISBN, stored as the thirteen digits of its ISBN-13,
  which is what the EAN-13 barcode on the book carries. An ISBN-10 typed by
  hand is converted on entry. Old or self-published books have none.
- Editions are shared by the household and matched by ISBN: scanning a known
  ISBN reaches the existing edition, it never creates a second one.
- Series, Author and Tag are matched by name: a name typed or returned by a
  lookup lands on the existing one, whatever its capitalisation.
- An Author is linked to an edition once per role; an author who wrote and
  drew has two links. When a lookup gives no role, the role is `WRITER`.
- A Copy belongs to exactly one Edition and sits on exactly one Bookshelf. It
  has no owner of its own: it is the bookshelf's.
- A Bookshelf has at least one owner at all times and is visible only to its
  members. Owners add, edit and remove its copies and manage its members;
  viewers see them.
- A reader sees and searches the copies of every bookshelf they belong to:
  that is their catalogue.
- Adding an ouvrage from a scan creates the edition when its ISBN is unknown,
  and a copy on the bookshelf the reader chose.
- Nobody deletes an edition. A member removes a copy from a bookshelf; when
  the last copy goes, the edition goes with it. An edition exists only while a
  copy or, later, a wish refers to it.

## 4. Functional requirements

Priorities: **P1** = needed before the family uses it, **P2** = soon after,
**P3** = nice to have.

### 4.1 Catalogue (P1)
- A reader's catalogue is the copies on every bookshelf they belong to, shown
  by edition.
- Add an ouvrage to a bookshelf: the edition is created if its ISBN is
  unknown, with kind, title, subtitle, series and volume number, authors by
  role, publisher, collection, publication year, ISBN-13, language, page
  count, summary, cover URL and tags; the copy is created on the chosen
  bookshelf.
- View and edit an edition; remove a copy from a bookshelf, which deletes the
  edition when it was the last copy.
- List the catalogue with filters (kind, series, bookshelf, tag; reading
  state once it exists) and sort (title, series and volume, added date).
- Edition page showing its copies with their bookshelves, and later the
  reading states and loans.

### 4.2 Search (P1)
- One search box over the reader's catalogue that finds editions by title,
  series, author, ISBN, tag, with accent- and case-insensitive matching
  (`Asterix` finds *Astérix*) and typo tolerance for short queries.
- Results ranked by relevance, grouped or sortable by series.
- Backed by PostgreSQL full-text search; no separate search service.

### 4.3 Bookshelves and copies (P1)
- A reader creates bookshelves and is their owner; an owner invites other
  readers as owner or viewer, and removes them. A bookshelf is visible only to
  its members and keeps at least one owner.
- A copy sits on one bookshelf and may be moved to another the reader owns.
  It has a condition and an acquisition date.
- Mark a copy as lent (to whom, since when) and as returned.

### 4.4 Reading (P1)
- Each reader sets their reading state per copy, a 1–5 rating, and notes.
- A reader's "currently reading" and "to read" lists.

### 4.5 Series tracking (P2)
- For a series, show owned volumes and gaps (e.g. "missing 4, 7").
- Optionally record the total number of published volumes to show completion.

### 4.6 Wishlist (P2)
- A reader adds a wish for an edition they want. A wish keeps a copy-less
  edition alive. Turning a wish into a copy on a bookshelf is one action.

### 4.7 Fast entry (P1, the first feature)
- Scan the EAN-13 barcode with the phone camera, or type an ISBN, and prefill
  the form from public metadata sources; then add the ouvrage to a bookshelf
  in one step.
- Sources arrive one at a time, each degrading gracefully when it fails or
  knows nothing: Open Library and the BnF SRU first (the BnF is the one that
  gives roles, series and collection for French titles), Google Books if an
  API key is configured, the cover by ISBN from Open Library. Sites without an
  API (Bedetheque, BDGest, Babelio) are candidates for later, subject to their
  terms of use.

### 4.8 PWA behaviour (P1 for install, P2 for offline)
- Installable on Android home screens, with icons and a splash screen.
- Works offline for browsing the last synced catalogue (read-only); edits made
  offline are rejected with a clear message in v1 (no sync queue).

### 4.9 Import / export (P3)
- Export the catalogue as CSV; import from CSV with a preview and error report.

### 4.10 Administration (P1)
- Accounts, passwords, second factor and deactivation are managed in Authelia,
  outside Libris. Libris creates a reader profile on first visit, from the name
  Authelia provides; the admin can edit profiles and hide departed readers.
- Backups are done by the server's existing Gordien solution (hourly `pg_dump`
  and restic); Libris only has to be registered there and document how to
  restore from a dump.

## 5. Non-functional requirements

- **Privacy**: private to the household; every request passes through the
  household's Authelia login (Traefik forward auth).
- **Simplicity**: one server, one database, no external SaaS dependency for
  core use. Metadata lookups are optional enhancements that degrade gracefully.
- **Performance**: search results under 300 ms for a catalogue of 10 000 editions.
- **Mobile first**: usable one-handed on a phone; desktop is a bonus.
- **Language**: UI in French. Code, commits, docs in English.
- **Operability**: deployable with `docker compose up -d` behind Traefik;
  logs to stdout; health endpoint.
- **Testability**: every feature ships with automated tests; the API is
  described by an OpenAPI document that is verified in CI.

## 6. Out of scope (v1)

- Public sharing, social features, recommendations.
- E-book reading, file storage for e-books.
- Multi-household tenancy.
- Sync queue for offline edits.

## 7. Open questions for Tophe

1. Should digital copies (e-books, PDFs) be modelled as copies with a
   `format` field, or excluded from v1?
2. ~~Cover images~~ — resolved 2026-09-11: a cover URL on the edition, filled
   by the lookup or by hand; storing our own images is a later feature.
3. Is a barcode-scan lookup source for BD needed beyond BnF? (Bedetheque has
   no public API.)
4. ~~Authentication~~ — resolved 2026-09-07: delegated to the existing Authelia
   (see `docs/ARCHITECTURE.md` D06).
5. ~~Deleting an item that still has copies~~ — resolved 2026-09-11: nobody
   deletes an edition; removing the last copy deletes it (§3).
6. Google Books refuses anonymous requests (daily quota of zero). Configure an
   API key on the server, or leave it out?
