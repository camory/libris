# Libris — Product Requirements (DRAFT v0.1, 2026-09-06)

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
| Admin | Tophe | Everything, plus manage accounts and settings |
| Member | Family members | Browse, search, add/edit items, manage own reading status and wishlist |

Accounts are created by the admin (invite-only). No public sign-up.

## 3. Domain glossary

- **Item**: a published work we track. Has a `type`: `BOOK`, `MANGA`, or `BD`.
- **Series**: an ordered set of items (e.g. *One Piece*, *Astérix*). An item may
  have a series and a volume number.
- **Copy**: a physical (or digital) instance of an item that someone in the
  household owns. An item may have zero copies (wishlist) or several.
- **Reading state**: per member and per item: to read, reading, finished,
  abandoned, with optional rating and notes.
- **Loan**: a copy temporarily out of the house (lent to someone) or borrowed
  from outside.
- **BD** (bande dessinée): Franco-Belgian comics. Metadata differs from books:
  scenario writer, artist, colourist; album numbers; publisher collections.

## 4. Functional requirements

Priorities: **P1** = needed before the family uses it, **P2** = soon after,
**P3** = nice to have.

### 4.1 Catalogue (P1)
- Create, view, edit, delete items with: type, title, subtitle, series and
  volume number, authors by role (writer, artist, translator, ...), publisher,
  publication year, ISBN-13 / EAN, language, page count, summary, cover image,
  genres and free tags.
- List items with filters (type, series, owner, reading state, tag) and sort
  (title, series/volume, added date).
- Item detail page showing copies, reading states of all members, loans.

### 4.2 Search (P1)
- One search box that finds items by title, series, author, ISBN, tag, with
  accent- and case-insensitive matching (`Asterix` finds *Astérix*) and typo
  tolerance for short queries.
- Results ranked by relevance, grouped or sortable by series.
- Backed by PostgreSQL full-text search; no separate search service.

### 4.3 Copies, ownership and location (P1)
- Each copy has an owner (member), a location (free text such as "salon,
  étagère 3"), a condition, and an acquisition date.
- Mark a copy as lent (to whom, since when) and as returned.

### 4.4 Reading (P1)
- Each member sets their reading state per item, a 1–5 rating, and notes.
- A member's "currently reading" and "to read" lists.

### 4.5 Series tracking (P2)
- For a series, show owned volumes and gaps (e.g. "missing 4, 7").
- Optionally record the total number of published volumes to show completion.

### 4.6 Wishlist (P2)
- Members add items they want; wishlist items are items with no copy and a
  wish marker per member. Turning a wish into a copy is one action.

### 4.7 Fast entry (P2)
- Scan a barcode (EAN-13/ISBN) with the phone camera and prefill the form from
  public metadata sources (Google Books, Open Library, BnF for French titles).
- Manual ISBN entry does the same lookup.

### 4.8 PWA behaviour (P1 for install, P2 for offline)
- Installable on iOS and Android home screens, with icons and a splash screen.
- Works offline for browsing the last synced catalogue (read-only); edits made
  offline are rejected with a clear message in v1 (no sync queue).

### 4.9 Import / export (P3)
- Export the catalogue as CSV; import from CSV with a preview and error report.

### 4.10 Administration (P1)
- Admin creates members, resets passwords, deactivates accounts.
- Nightly database backup to a file the admin can restore from.

## 5. Non-functional requirements

- **Privacy**: private to the household; every page requires login.
- **Simplicity**: one server, one database, no external SaaS dependency for
  core use. Metadata lookups are optional enhancements that degrade gracefully.
- **Performance**: search results under 300 ms for a catalogue of 10 000 items.
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
2. Cover images: store our own copies (upload / fetched from lookup) or only
   link to external URLs? (Draft assumes: store our own, on a Docker volume.)
3. Is a barcode-scan lookup source for BD needed beyond BnF? (Bedetheque has
   no public API.)
4. Authentication: plain username + password managed by the admin is assumed.
   Passkeys or a Traefik-level SSO (Authelia) could replace it later.
