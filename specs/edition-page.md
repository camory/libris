# Edition page

**Why:** PRD §4.1. From a row of their catalogue or of a series' page, a
reader opens one ouvrage and reads all Libris knows of it and on which of
their bookshelves its copies sit: the edition page of §4.1, the shelf to
look on read from the sofa. Now, because the catalogue and the series'
page list ouvrages that lead nowhere, and editing an ouvrage and removing
a copy start from this page.
**Status:** done 2026-10-23

## Scenarios

An ouvrage is the reader's through a copy on a bookshelf they belong to,
the catalogue's rule, and the page shows those copies alone. Editing the
ouvrage, removing a copy, a link from the ouvrage to its series, its tags,
the facts of each copy (who added it and when, its condition, its
acquisition date), the reading states and the loans are not in this spec.
The page is reached from a row of the catalogue and from a row of a
series' page alone: the lookup's card and a bookshelf's screen lead
nowhere.

**S1 The reader opens an ouvrage** · frontend, backend

```gherkin
Given an edition with copies on bookshelves the reader belongs to,
      and a copy of it on a bookshelf they do not belong to

When the reader taps its row in their catalogue or on a series' page

Then Libris answers the edition with its copies on their bookshelves,
     and no other
	And the screen shows the ouvrage's card, its title as the screen's
	    title
	And one row per bookshelf of theirs holding a copy
```

The card is the lookup's (DESIGN U06) as it shows an edition the house
holds: the stored cover or its stand-in, with neither dots nor a source
name, and no button under it. What the card shows of an edition is the
card's own tests'. Proof: the shape of the answer verified by Contracteer
on both sides; backend scenario test over HTTP; frontend scenario test
over the fakes, two cases: from a row of the catalogue, from a row of a
series' page. Going back with the phone's back button, from the page to
the screen it was opened from, follows from the addresses and is the
browser's: checked by hand on the Pixel.

**S2 No such ouvrage** · frontend, backend

```gherkin
Given an edition none of whose copies sits on a bookshelf the reader
      belongs to

When the reader opens it

Then Libris answers that there is no such ouvrage
	And the screen says the ouvrage cannot be found
```

Two cases: an edition Libris does not know, one whose copies sit only on
bookshelves the reader does not belong to; the edition whose last copy
the reader has just removed comes with removing a copy, which Libris does
not offer yet. An address whose id is not one reads the same way on the
screen. Proof: contract example `404_NOT_IN_MY_CATALOGUE` verified by
Contracteer on both sides, the id that is not one its generated case;
backend scenario test over HTTP, one per case; frontend scenario test
over the fakes, the no such ouvrage and the id that is not one.

**S3 Libris unavailable** · frontend

```gherkin
Given a reader opening an ouvrage

When Libris does not answer

Then the screen says to try again later
	And shows nothing of the ouvrage
```

Proof: frontend scenario test with the API failing.

## Screen

Mockups: <https://claude.ai/artifact/3pkmNQpTD6CSb6mKvZ4XnQ>, one artboard
per state below, exported as `specs/edition-page/<n>-<state>.jpg` in the
same order. Route `/catalogue/ouvrages/{id}`, reached from a row of the
catalogue or of a series' page; the *Catalogue* tab is in `accent`.

`docs/DESIGN.md` gains with this spec: in U03, a screen about one ouvrage
has no header, its card the first block of the content; in U02, the card
title is then the `h1` of the screen, the overline outside it; in U08,
the ouvrage's title as the title of its screen.

The catalogue (`specs/catalogue.md`) and the series' page
(`specs/series-page.md`) change: a row leads to the ouvrage's page, the
whole row a link, with no chevron.

Top to bottom on a phone, everything but the tab bar scrolling together:

- **The card** (U06), 20 under the top of the content, with no header
  above it: the cover or its stand-in; at its right the overline when
  the ouvrage has a series (*Astérix · album 1*), the title, the subtitle
  and the authors; one row per bookshelf of the reader holding a copy
  (*Dans Bibliothèque de Christophe · 2 exemplaires*, *Dans Salon*); the
  field rows; the summary, as long as it is. A field with no value has no
  row and an ouvrage with no summary ends on its last field row.

The screen asks the ouvrage anew on each arrival, so a copy added a
moment ago counts. There is no empty state: an ouvrage with no copy on
the reader's bookshelves is not found.

States:

- **Loading**: no title; the card's skeleton at the card's shape: the top
  part, one bookshelf row and three field rows, until the answer arrives.
- **Shown** (S1): the card. Two artboards: a BD with its series, two
  bookshelves and a summary; a book with neither cover nor series, one
  bookshelf and no summary.
- **Not found** (S2): no title, *Ouvrage introuvable.* with the alert
  icon as the screen's one message.
- **Unavailable** (S3): no title, *Erreur lors du chargement, veuillez
  réessayer plus tard.* with the alert icon.

## Contract

Release `v0.14.0` of `camory/libris-api`, after `v0.13.0`. One operation
is added; no schema changes.

- `GET /api/v1/books/{id}` → `200` `Book`, the edition with its copies on
  the bookshelves the reader belongs to, at least one; `400` `Problem`
  `/problems/validation` for an id that is not one, the screen showing it
  as *Not found*; `404` `Problem` `/problems/not-found` when no copy of the
  edition sits on a bookshelf the reader belongs to. Key
  `404_NOT_IN_MY_CATALOGUE`.

The answer is the catalogue's `Book`, read for one edition: the
bibliography answers the edition, the library its copies on the reader's
bookshelves on top of it (D12).

## Done

On the Pixel, from the installed app on staging: in *Catalogue*, tap *One
Piece* tome 1 and read its card, its title at the top and the bookshelves
holding it; go back with the phone's button and land on the catalogue; in
*Séries*, tap *Tintin*, then tome 20, and read its card. On the second
account of the family, paste the address of tome 20's page and read
*Ouvrage introuvable.* The other cases are the tests' to show.
Checked by Tophe on staging, recorded on 2026-10-23.

## Tasks

- T111 — S2, the backend on `v0.14.0` — backend
- T112 — S1 — backend
- T113 — S2, the frontend on `v0.14.0` — frontend
- T114 — S1 — frontend
- T115 — S3 — frontend
