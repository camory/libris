# Libris — Product Requirements (v0.5, 2026-11-14)

> The source of truth for *what* Libris does. It changes deliberately, with
> Tophe; a change to built behaviour arrives through a spec.

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
In the API, an `Edition` is what an edition says of itself, with its ISBN-13;
the lookup answers an `IsbnLookup`, with no id since nothing is stored yet;
a stored edition, addressed by id, is a `Book`.
_Avoid_: item, book (as the domain entity), publication, work, volume.

**Kind**:
What an edition is: `BOOK` (any book that is neither of the other two), `BD`,
`MANGA`. Decides the words on screen (livre, album, tome) and the author roles
the form offers. Prefilled by the lookup like the other fields.
_Avoid_: type, category, genre, format.

**Series**:
A named, ordered set of editions (*One Piece*, *Astérix*). Singular and plural.
*On screen*: série.
_Avoid_: collection, saga, cycle, serie.

**Volume number**:
An edition's position in its series.
*On screen*: tome; album for a BD, as the kind decides.
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
*On screen*: auteur; the role as the activity, scénario, dessin, couleurs,
traduction.
_Avoid_: function, credit, job.

**Tag**:
A free label a reader puts on an edition (*policier*, *shōnen*, *jeunesse*).
There is no closed list and no separate notion of genre.
Not in Libris yet: tags arrive with their own feature.
*On screen*: tag.
_Avoid_: genre, category, subject, keyword, label.

**Reader**:
A person of the household, created on first visit from the Authelia account,
with a default bookshelf.
*On screen*: lecteur.
_Avoid_: user, account, member (as the entity), profile.

**Bookshelf**:
A named place where copies sit, with members. *Chambre de Léa*, *salon*,
*cave*. A reader's first one is created with them, named after them
(*Bibliothèque de Léa*), and is their default until they choose another
they own.
*On screen*: bibliothèque.
_Avoid_: library, shelf, location, collection.

**Membership**:
A reader's part in a bookshelf, with a role: `OWNER` or `VIEWER`; the reader
is then a member of the bookshelf.
*On screen*: propriétaire, invité.
_Avoid_: user, participant, guest (in code), share.

**Copy**:
One physical instance of an edition, sitting on one bookshelf. Digital copies
are not modelled in v1.
*On screen*: exemplaire.
_Avoid_: item, instance, book, copie, format.

Shaped with their own feature, later: **Reading state** (a link between a
reader and a copy: to read, reading, finished, abandoned, with rating and
notes),
**Loan** (a copy temporarily out of the house, or borrowed from outside),
**Wish** (a reader wants an edition; a fact of its own, not "zero copies").

### Relationships and rules

- An Edition has a kind, at most one series with a volume number, zero or
  more authors each with a role, zero or more tags once Libris holds them, and
  optionally a collection, a cover and every descriptive field the lookup
  sources give (subtitle, publisher, publication year, language, page count,
  summary).
- An edition's cover is a picture Libris keeps: the reader picks one among
  the sources' pictures when adding the ouvrage, and Libris fetches it and
  serves it from then on.
- An Edition may have one ISBN, stored as the thirteen digits of its ISBN-13,
  which is what the EAN-13 barcode on the book carries. An ISBN-10 typed by
  hand is converted on entry. Old or self-published books have none.
- Editions are shared by the household and matched by ISBN: scanning a known
  ISBN reaches the existing edition, it never creates a second one.
- Series, Author and Tag are matched by name: a name typed or returned by a
  lookup lands on the existing one, whatever its capitalisation and its
  accents.
- An Author is linked to an edition once per role; an author who wrote and
  drew has two links. When a lookup gives no role, the role is `WRITER`.
- The kind is prefilled by the lookup like the other fields. The BnF record
  says whether a book is a comic strip and which language it was translated
  from: a comic strip translated from Japanese, Korean or Chinese is a
  `MANGA`, any other comic strip a `BD`, everything else a `BOOK`. When no
  source says, the form proposes `BOOK`. No kind comes from scraping a site.
- A Copy belongs to exactly one Edition and sits on exactly one Bookshelf. It
  has no owner of its own: it is the bookshelf's.
- A Bookshelf has at least one owner at all times and is visible only to its
  members. Owners add, edit and remove its copies and manage its members;
  viewers see them.
- A Reader has one default bookshelf, which they own: at first the one
  created with them on their first visit, named after them, then whichever
  bookshelf they own they choose. Adding an ouvrage in one step puts the
  copy there.
- A reader sees and searches the copies of every bookshelf they belong to:
  that is their catalogue.
- Adding an ouvrage from a scan creates the edition when its ISBN is unknown,
  and a copy on the reader's default bookshelf, or on another bookshelf they
  own when they choose one.
- Nobody deletes an edition. An owner removes a copy from a bookshelf; when
  the last copy goes, the edition stays the house's record, and a later scan
  of its ISBN reaches it.

## 4. Functional requirements

Priorities: **P1** = the family misses it now, **P2** = soon,
**P3** = nice to have.

Each section names the specs that are done and lists what is still to spec;
when a spec is written with Tophe, the lines it covers leave *Still to spec*
and the spec joins *Done*.

### 4.1 Catalogue (P1)
What the family owns: the catalogue, an ouvrage's page, adding, correcting
and removing.

Done: `specs/catalogue.md`, `specs/edition-page.md`, `specs/bookshelf.md`,
`specs/edition-form.md`, `specs/held-edition-edit.md`,
`specs/authors-edit.md`, `specs/remove-copy.md`, `specs/covers.md`,
`specs/summary.md`.

Still to spec:
- Add an ouvrage to a bookshelf the reader owns other than their default,
  chosen at the add (P2).
- Filter the catalogue by kind (P2), and by tag and reading state once they
  exist.
- The edition page shows the reading states and the loans, with §4.4 and
  §4.11.

### 4.2 Search (P1)
Finding an ouvrage of the catalogue as the reader types.

Done: `specs/search.md`.

Still to spec:
- Search by tag once Libris holds tags.

### 4.3 Bookshelves and copies (P1)
Where the copies sit and who shares them.

Done: `specs/bookshelf.md`, `specs/bookshelves.md`.

Still to spec:
- An owner removes a member; a bookshelf keeps at least one owner (P2).
- Move a copy to another bookshelf the reader owns (P2).

### 4.4 Reading (P1)
Who reads what, and what they thought of it.

Still to spec:
- Each reader sets their reading state per copy, a 1–5 rating, and notes.
- A reader's "currently reading" and "to read" lists.

### 4.5 Series tracking (P2)
What the family has of a series, and what is missing.

Done: `specs/series-list.md`, `specs/series-page.md`.

Still to spec:
- Show the number of volumes published, the ones missing after the highest
  owned, and the completion. The number comes from the sources (the BnF
  knows the highest volume published); a reader types it when no source
  knows it.

### 4.6 Wishlist (P2)
What the family wants next.

Still to spec:
- A reader adds a wish for an edition they want. Turning a wish into a copy
  on a bookshelf is one action.

### 4.7 Fast entry (P1)
An ouvrage scanned or typed by its ISBN, prefilled from public sources,
added in one step.

Done: `specs/fast-entry.md`, `specs/bookshelf.md`, `specs/kind.md`,
`specs/covers.md`, `specs/edition-form.md`, `specs/authors-edit.md`,
`specs/summary.md`, `specs/scan-beep.md`.

### 4.8 PWA behaviour (P1)
Libris on the phone, as an app.

Done: installable on Android home screens; `specs/update.md`.

Still to spec:
- Libris's own icon, drawn for the shapes Android crops it to, on the home
  screen and the splash screen (P1).
- Browse the last synced catalogue offline, read-only; an edit made offline
  is refused with a clear message, with no sync queue (P2).

### 4.10 Administration (P1)
Who the readers are. Accounts, passwords, second factor and deactivation are
managed in Authelia, outside Libris.

Done: a reader's profile created on their first visit, from the name
Authelia provides (`specs/bookshelf.md`).

Still to spec:
- The admin edits a reader's profile and hides a departed reader (P3).

### 4.11 Loans (P2)
Which copies are out of the house, and which books are in it on loan.

Still to spec:
- Mark a copy as lent (to whom, since when) and as returned.
- Record an ouvrage borrowed from outside the house (from whom, since when)
  and its return.

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
- **Backups**: the database is backed up by the server's existing solution,
  hourly and before each deployment; how to restore lives on the server.
- **Testability**: every feature ships with automated tests; the API is
  described by an OpenAPI document that is verified in CI.

## 6. Out of scope (v1)

- Public sharing, social features, recommendations.
- E-book reading, file storage for e-books.
- Multi-household tenancy.
- Sync queue for offline edits.

## 7. Open questions for Tophe

1. Which name does an author get: the one printed on the book, the BnF's
   authorised heading, or a mix? The lookup reads the heading today, so a
   pen name printed on the cover shows as the name the BnF files the author
   under, and accents or name order may differ from the print.
