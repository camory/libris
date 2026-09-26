# Catalogue

**Why:** PRD §4.1 and §1. From the sofa, a reader sees what the family owns:
every edition with a copy on a bookshelf they belong to, and where each copy
sits. Now, because the bookshelf spec put the first copies on shelves and
nothing shows them but the card of their own ISBN.
**Status:** draft

## Scenarios

**S1 The catalogue lists the house's editions** · frontend, backend

```gherkin
Given editions the house holds, with copies on bookshelves the reader
      belongs to and on one they do not

When the reader opens their catalogue

Then Libris answers every edition with a copy on one of their bookshelves,
     once each, with its copies on those bookshelves and none of the other
	And in the order a shelf reads, by series name, or title when there is
	    none, ignoring case and accents, then by tome, then by title
	And the screen lists them in that order
```

Proof: contract example verified by Contracteer on both sides; backend
scenario test over HTTP; unit test of the order; frontend scenario test over
the fakes.

**S2 The catalogue comes in pages** · frontend, backend

```gherkin
Given a catalogue of more editions than a page holds

When the reader opens their catalogue

Then Libris answers the first page, in the order of S1, and names the next
	And the screen lists it
	And reaching the end of the list asks for the next page, which
	    follows the first without a gap or a repeat
	And the last page names no next
```

A page holds fifty editions, fixed by Libris; an edition added between two
pages lands in the order of S1 without shifting them. Proof: contract
examples verified by Contracteer on both sides, a page that names a next and
a last page; backend scenario test over HTTP with one edition more than a
page; frontend scenario test over the fakes with two pages.

**S3 The catalogue is empty** · frontend, backend

```gherkin
Given a reader whose bookshelves hold no copy

When they open their catalogue

Then Libris answers an empty page that names no next
	And the screen says what will fill it
```

Proof: backend scenario test over HTTP; frontend scenario test over the
fakes. No contract example: its request is the first page's, and one
request has one answer under verification.

**S4 Libris unavailable** · frontend

```gherkin
Given a reader opening their catalogue

When Libris does not answer

Then the screen says to try again later
	And lists nothing
```

Two cases: the first page, which lists nothing; the next page, after which
the rows already listed stay and the sentence sits under them. Proof:
frontend scenario test with the API failing, one per case.

## Screen

Mockups: <https://claude.ai/artifact/TVJWSSoqYZR6ncunYQ7zV3>, one
artboard per state below, exported as `specs/catalogue/<n>-<state>.jpg` in
the same order. Route `/catalogue`, reached from the tab bar, whose third
column it claims: three book spines, *Catalogue*.

Top to bottom on a phone: the title *Parcourir le catalogue* and the line
*Les ouvrages de toutes vos bibliothèques.*; the list; at the bottom the tab
bar, the house of *Accueil*, the barcode circle *Ajouter* and the spines of
*Catalogue*, in accent here.

The list is one block on `surface` with a `border` hairline between its
rows, 14 of padding inside a row. A row shows one edition: the cover at the
left, 48 by 74, or its stand-in; at its right, stacked 6 apart, the overline
série · tome when it has a series (*Astérix · tome 1*), the title in the row
title step, the authors as names alone on one line separated by commas,
whatever their roles, in the order of the answer, then in `muted` the
bookshelves of the reader holding a copy, separated by commas, each
followed by *· 2 exemplaires* when it holds more than one (*Bibliothèque de
Christophe · 2 exemplaires, Salon*). The rows are in the order of the
answer; the screen sorts nothing. A row leads nowhere yet: the edition page
comes with its own spec. The screen asks the first page anew on each
arrival, so an ouvrage added a moment ago is there.

States:

- **Loading**: five skeleton rows stand where the rows will land, until the
  first page arrives.
- **Listed** (S1, S2): the rows of the pages received; the list scrolls
  under the tab bar, which stays.
- **Loading more** (S2): when the last row comes into view, two skeleton rows
  stand under it while the next page comes, and its rows take their place.
  Under the last row of the last page, nothing.
- **Empty** (S3): in place of the list, the outlined book icon over *Les
  ouvrages de vos bibliothèques apparaîtront ici.*
- **Unavailable** (S4): in place of the list, *Erreur lors du chargement,
  veuillez réessayer plus tard.* with the alert icon; when the next page is
  the one that does not come, the rows already listed stay and the sentence
  sits under them, where the skeleton rows stood.

## Contract

Release `v0.7.0` of `camory/libris-api`, after the bookshelf spec's
`v0.6.2`. Nothing changes in an existing answer: one operation and two
schemas are added.

- `Book`, the stored edition addressed by id: the fields of `IsbnLookup`,
  `isbn13` nullable as in `NewBook`, plus `id`, a uuid, required; `copies`
  as in `IsbnLookup`, the copies on the bookshelves the reader belongs to.
  `IsbnLookup` stays as it is: an ISBN the house lacks has no id.
- `BookPage`: `books`, an array of `Book`, and `next`, a uuid or null.
- `GET /api/v1/books`, the reader's catalogue: the books with a copy on a
  bookshelf they belong to, in the order of S1, fifty at a time. Query
  parameter `after`, optional, the `id` of the last book received, the
  page then starting after it → `200` `BookPage`, its `next` the `after`
  of the following page, null on the last. `400` `Problem`
  `/problems/validation` when `after` is not an id. Examples
  `CATALOGUE_FIRST_PAGE`, no `after`, three books, *Astérix* 1 and 2 and
  *Le Petit Prince*, `next` the id of the last; `CATALOGUE_LAST_PAGE`,
  `after` that id, one book, *One Piece* 3, `next` null.
- The body examples live under `components/examples`, `CatalogueFirstPage`
  and `CatalogueLastPage`; the operation keeps the scenario keys and points
  at them. The request without `after` is the example `null` on the
  parameter, declared nullable for it.

The empty catalogue and a page of fifty are the tests' to show, not the
document's: a request has one answer under verification. The verifier adds
its case on `after` of the wrong type, answered `400` with a `Problem`.

## Done

On the Pixel, from the installed app on staging: open *Catalogue*; the
ouvrages added in the bookshelf spec's check are there, *One Piece* 1 first
with *Bibliothèque de Christophe · 2 exemplaires*, and the rows read as a
shelf; on the second account of the family, open *Catalogue* and read the
empty sentence. The pages are the tests' to show: the house has fewer than
fifty ouvrages.

## Tasks
