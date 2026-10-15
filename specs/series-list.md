# Series list

**Why:** PRD §4.5 and §4.1. From the catalogue, a reader switches to the
series their bookshelves hold, one row per series with its cover and how
many tomes or albums they own: the shelf of BD and manga read as the family
thinks of it. Now, because the catalogue lists every edition one by one,
and the series page and the series details, the next two specs of §4.5,
start from this list.
**Status:** draft

## Scenarios

A series is owned through the editions with a copy on a bookshelf the
reader belongs to, the catalogue's rule. The list leaves to later specs the
series page and anything a row leads to, the gaps, the total number of
published volumes, the series filter of §4.1 and renaming a series. An
edition with no series stays in the ouvrages alone, and the *Catalogue* tab
always opens the ouvrages: no visit remembers the last view.

**S1 The reader's series** · frontend, backend

```gherkin
Given editions in series with copies on bookshelves the reader belongs to,
      an edition of another series on a bookshelf they do not belong to,
      and an edition with no series

When the reader opens the series of their catalogue

Then Libris answers each series with an edition on one of their
     bookshelves, once each, and no other
	And in the order of their names, ignoring case and accents
	And the screen lists them in that order, each with its cover, its name
	    and its count
```

Proof: the shape of the answer verified by Contracteer on both sides;
backend scenario test over HTTP; frontend scenario test over the fakes.

**S2 The count of a series** · frontend, backend

```gherkin
Given a series with editions on the reader's bookshelves, one of them in
      two copies

When the reader opens the series of their catalogue

Then Libris answers the number of its editions, each once however many
     copies it has
	And the kind every edition of the series shares, or none when they
	    differ
	And the screen counts in albums when that kind is BD, in tomes
	    otherwise
```

Three cases: a BD series, a manga series, and a series mixing a BD and a
manga, counted in tomes. The words, singular for one:

| Kind of the series | One | More |
|---|---|---|
| BD | *1 album* | *6 albums* |
| MANGA, BOOK, none | *1 tome* | *3 tomes* |

Proof: backend unit test of the count and of the shared kind, one per case;
frontend scenario test over the fakes, one per case, one of them counting
one.

**S3 The cover of a series** · frontend, backend

```gherkin
Given a series with editions on the reader's bookshelves

When the reader opens the series of their catalogue

Then Libris answers the cover of its first edition in the order a shelf
     reads, or none when that edition has none
	And the screen shows that cover, or the stand-in
```

The order a shelf reads is catalogue S1's: by tome, an edition without a
tome after the numbered tomes, then by title. The cover of another edition
never stands in: the row would show one tome's cover in the place of the
first. Three cases: the first tome has a cover; the first tome has none
and the second has one, answered none; no edition has a tome, the first by
title giving the cover. Proof: backend unit test of the cover rule, one per
case; frontend scenario test over the fakes of the second case, the
stand-in shown.

**S4 The reader switches the view** · frontend

```gherkin
Given a reader whose catalogue holds editions in series

When they open their catalogue from the tab bar
	And choose Séries
	And then Ouvrages

Then the screen shows the ouvrages, then the series, then the ouvrages
	And each view at its own address
	And the switch marks the view shown as the current one
```

Proof: frontend scenario test over the fakes. Going back with the phone's
back button, from the series to the ouvrages, follows from the addresses
and is the browser's: checked by hand on the Pixel.

**S5 No series** · frontend, backend

```gherkin
Given a reader whose bookshelves hold editions, none of them in a series

When they open the series of their catalogue

Then Libris answers no series
	And the screen says what will fill it
```

Proof: backend scenario test over HTTP; frontend scenario test over the
fakes.

**S6 Libris unavailable** · frontend

```gherkin
Given a reader opening the series of their catalogue

When Libris does not answer

Then the screen says to try again later
	And lists nothing
```

Proof: frontend scenario test with the API failing.

## Screen

Mockups: <https://claude.ai/artifact/UpsddVq39DKUFrxwwHiQnc>, one artboard
per state below, exported as `specs/series-list/<n>-<state>.jpg` in the
same order. Routes `/catalogue`, the ouvrages, which the *Catalogue* tab
opens, and `/catalogue/series`, the series; the tab is in `accent` on both.

Top to bottom on a phone: the header of the catalogue, unchanged on both
views, *Parcourir le catalogue* and *Les ouvrages de toutes vos
bibliothèques.*; the switch; the list; the tab bar.

The switch is a segmented choice (U04), which DESIGN gains with this spec:
*Ouvrages* then *Séries*, in a `nav` named *Vue du catalogue*.

On `/catalogue`, nothing changes under the switch: the list of
`specs/catalogue.md`. On `/catalogue/series`, the list is the catalogue's
block, one row per series, in the order of the answer; the screen sorts
nothing. A row shows the cover at the left, 48 by 74, or its stand-in; at
its right, stacked 6 apart, the name in the row title step and, in
`muted`, the count (*6 albums*, *1 tome*). No overline: the name is the
row's subject. No chevron: a row leads nowhere until the series page. The
screen asks the series anew on each arrival, so an ouvrage added a moment
ago counts.

States:

- **Ouvrages** (S4): the catalogue's states, under the switch on
  *Ouvrages*.
- **Loading**: five skeleton rows, a cover and two lines each, stand where
  the rows will land, until the answer arrives.
- **Listed** (S1, S2, S3): the rows of the answer, one of them showing the
  stand-in; the list scrolls under the tab bar, which stays.
- **Empty** (S5): in place of the list, the outlined book icon over *Les
  séries de vos bibliothèques apparaîtront ici.*
- **Unavailable** (S6): in place of the list, *Erreur lors du chargement,
  veuillez réessayer plus tard.* with the alert icon.

## Contract

Release `v0.12.0` of `camory/libris-api`, after `v0.11.0`. Nothing changes
in an existing answer: one operation and two schemas are added, and one
schema is renamed, which changes no byte, since a JSON body carries no
schema name.

- `Kind`, `BOOK | BD | MANGA`, the enum `Edition.kind` held inline, now a
  schema both kinds share.
- `SeriesEntry`: the schema `Series` renamed, an edition's place in a
  series, `name` and `volumeNumber`, unchanged; the backend's domain
  already bears the name.
- `Series`, the PRD's word, a series of the reader's catalogue: `name`, as
  `SeriesEntry`'s; `volumeCount`, an integer, at least 1; `coverUrl`, as
  `Book`'s, nullable; `kind`, a `Kind` or null, the kind every edition of
  the series shares. All four required. No `id`: the series page
  adds it without breaking anything.
- `GET /api/v1/series`, the series of the reader's catalogue, each with an
  edition on a bookshelf they belong to, in the order of S1 → `200`, an
  array of `Series`, empty when there is none. Not paged: a household's
  series are a few dozen.

The operation carries no example: it takes no input for one to fix, and
Contracteer verifies an answer's shape, never its values. The order, the
count, the kind and the cover are the tests' to show.

## Done

On the Pixel, from the installed app on staging: open *Catalogue* and see
*Ouvrages* current; choose *Séries* and read *One Piece* with its count in
tomes and the cover of its first tome, and a BD series counted in albums,
adding one first if staging holds none; go back with the phone's button
and land on the ouvrages; on the second account of the family, choose
*Séries* and read the empty sentence. A series mixing kinds and a first
tome without a cover are the tests' to show.

## Tasks

- T092 — S5, the backend on `v0.12.1` — backend
- T093 — S1, S2 the count — backend
- T094 — S2 the shared kind — backend
- T095 — S3 — backend
- T096 — S5, the frontend on `v0.12.1` — frontend
- T097 — S1, S2, S3 — frontend
- T098 — S6 — frontend
- T099 — S4 — frontend
