# Bookshelf

**Why:** PRD §4.3, §4.1 and §4.7. In the bookshop, the card of a scanned
ouvrage tells the reader whether the family already owns it and where; at
home, one tap adds it to their bookshelf. Now, because the lookup shows what
the sources know and stores nothing: this is the first thing Libris keeps.
**Status:** draft

## Scenarios

**S1 The first visit creates the bookshelf** · backend

```gherkin
Given a reader Authelia knows and Libris has never seen

When they visit

Then their profile exists
	And one bookshelf named after them, Bibliothèque de Léa, which they own
	And it is their default bookshelf
```

Proof: backend scenario test over HTTP, the reader's answer naming their
default bookshelf.

**S2 The ouvrage is added** · frontend, backend

```gherkin
Given the card of an ISBN the house lacks and a source knows

When the reader adds it

Then an edition exists with what the card shows, kind included
	And a copy of it sits on the reader's default bookshelf
	And the card shows that copy on that bookshelf
```

Proof: contract example `ADD_ONE_PIECE_1` verified by Contracteer on both
sides; backend scenario test over HTTP; frontend scenario test against
`contracteer mock`.

**S3 A known ISBN reaches the existing edition** · backend

```gherkin
Given an edition the house holds

When a reader adds the ouvrage of its ISBN

Then a copy sits on their default bookshelf
	And the house still holds one edition for that ISBN
```

Two cases: another reader, whose copy is the edition's second on a second
bookshelf; the same reader again, whose second copy sits beside the first.
Proof: backend scenario test over HTTP, one per case.

**S4 The ouvrage is already in a bookshelf** · frontend, backend

```gherkin
Given an edition the house holds, with copies on bookshelves the reader
      belongs to and on one they do not

When the reader asks for its ISBN

Then Libris answers the edition as the house holds it, without asking
     the sources
	And its copies on the reader's bookshelves, each with the name of its
	    bookshelf, and none of the other
	And the card says where the copies are
```

An edition the house holds whose copies are all on bookshelves the reader
does not belong to answers the same, with no copy: the card shows the edition
without a place. Proof: contract example `ONE_PIECE_2_OWNED` verified by
Contracteer on both sides; backend scenario test over HTTP with sources
that must not be asked; frontend scenario test against `contracteer mock`.

**S5 Libris unavailable during the add** · frontend

```gherkin
Given the card of S2

When the reader adds it and Libris does not answer

Then the card says to try again later
	And the card stays as it was, nothing added
```

Proof: frontend scenario test with the API failing.

## Screen

No screen of its own: the lookup screen of `specs/fast-entry.md`, route
`/isbn`, whose card gains a row and a button. The bookshelf of S1 has no
screen; its name shows only in that row.

In the card, between the authors and the field rows: one row per bookshelf
of the reader holding a copy, *Dans Bibliothèque de Léa*, followed by
*· 2 exemplaires* when it holds more than one. No row when the reader's
bookshelves hold none.

Under the card, the button *Ajouter à ma bibliothèque*, full width, primary.

States, on top of those of the lookup:

- **Found** (fast-entry S1): the card without a copies row, the button.
- **Already there** (S4): the card with its copies rows, the button, since a
  second copy is legitimate.
- **Adding** (S2): the button reads *Ajout en cours…* with a spinner and
  accepts nothing.
- **Added** (S2): the copies row shows the new copy, on the reader's default
  bookshelf, and the button is gone. The card is the confirmation; there is
  no message.
- **Not added** (S5): *Erreur lors de l'ajout, veuillez réessayer plus
  tard.* in red under the button, which is back.

A new lookup replaces the card, its rows and the button as the lookup already
replaces the card.

## Contract

Release `v0.6.0` of `camory/libris-api`, after the kind spec's `v0.5.0`,
which gives the lookup answer its `kind`, renames its schemas `IsbnLookup`,
`Author` and `Series`, and folds `ValidationProblem` into one
`Problem` schema with an optional `errors`. Nothing of `v0.6.0` changes the
bytes of an existing answer: two fields are added, one operation.

- `IsbnLookup` gains `copies`, an array of `Copy`, required, empty when the
  reader's bookshelves hold none: the copies of the edition on the
  bookshelves the reader belongs to. `ONE_PIECE_1` answers an empty array; a
  new example `ONE_PIECE_2_OWNED`, *One Piece* tome 2 on its own ISBN,
  answers two copies on two bookshelves, *Bibliothèque de Léa* and *Salon*.
- `CurrentReader` gains `defaultBookshelf`, a `Bookshelf` `{id, name}`,
  required, so the app knows where the add goes from its first request.
- `POST /api/v1/bookshelves/{id}/books`, `id` the bookshelf's uuid; the
  body is a `NewBook`, the book as the reader submits it: the fields of
  `IsbnLookup` without `copies`, and `isbn13` nullable, since a book typed
  by hand may have none → `201` `Copy` `{id, bookshelf}` with the
  bookshelf's `{id, name}`; `400` `Problem` `/problems/validation` with one
  error per refused field, `isbn13` checked as the lookup checks its `isbn`;
  `404` `Problem` `/problems/not-found` when the reader owns no such
  bookshelf, a bookshelf being visible only to its members. Examples
  `ADD_ONE_PIECE_1`, `400_NOT_AN_ISBN` (the body of `ADD_ONE_PIECE_1` with a
  wrong check digit), `404_NOT_MY_BOOKSHELF` (the body of `ADD_ONE_PIECE_1`
  on another bookshelf's id).
- Every request and response body example lives under
  `components/examples`, named after what it holds, `OnePiece1`,
  `NewOnePiece1`, `CopyOfOnePiece1`, `NotMyBookshelf`; the operations keep
  the scenario keys and point at them. A path parameter's value stays on its
  parameter.

`Copy` and `Bookshelf` are the domain's names and appear wherever the
domain does: the same `Copy` in the lookup answer and in the add's answer,
the same `Bookshelf` on the reader and on the copy. `NewBook` is not the
lookup answer sent back: it is what the reader submits, from the card in
this feature, from a form later. The backend matches the edition by
`isbn13` and creates it on the way when unknown, with the series and the
authors matched by name as PRD §3 says. The verifier adds two cases of its
own on the add, a path `id` that is not a uuid and a body of the wrong
types, both answered `400` with a `Problem`.

## Done

On the Pixel, from the installed app on staging: scan *One Piece* 1 and add
it, the card shows *Dans Bibliothèque de Christophe*; scan it again, the row
is there before any tap; add it again, the row reads *· 2 exemplaires*; on a
second account of the family, scan it and read the card without a place.

## Tasks

- T033 — S1 — backend
- T034 — the house's book stored, no scenario of its own — backend
- T035 — S2, S3 — backend
- T036 — S4 — backend
- T037 — S2, S3 and S4 through the API, the backend on `v0.6.0` — backend
- T038 — S4, the frontend on `v0.6.0` — frontend
- T039 — S2, S5 — frontend
