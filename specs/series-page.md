# Series page

**Why:** PRD §4.5. From the series of their catalogue, a reader opens one
series and reads the tomes or albums their bookshelves hold, the ones
missing between them, who made them and who published them: the gaps of
§4.5, "missing 4, 7", read from the sofa. Now, because the series list is
done and leads nowhere, and the series details, the total published and
the completion, start from this page.
**Status:** done 2026-10-20

## Scenarios

A series is the reader's through the editions with a copy on a bookshelf
they belong to, the catalogue's rule, and the page shows those editions
alone. A gap is a number from 1 to the highest tome owned that no owned
edition carries: an edition without a tome fills none, and two editions of
one tome fill it once. The tomes after the highest owned, the total
published and the completion belong to the series details; the edition
page, a link from an ouvrage to its series, the series filter of §4.1 and
renaming a series are not in this spec. The page is reached from the
series list alone.

**S1 The reader opens a series** · frontend, backend

```gherkin
Given a series with editions on bookshelves the reader belongs to,
      and an edition of it on a bookshelf they do not belong to

When the reader taps the series in the series of their catalogue

Then Libris answers the series with its editions on their bookshelves,
     each once, and no other
	And the screen shows the series' name and how many tomes it holds
	And lists its editions in the order a shelf reads
```

The order a shelf reads is catalogue S1's within one series: by tome, an
edition without a tome after the numbered tomes, then by title, ignoring
case and accents. Libris promises no order: the screen sorts. The series'
name, count, kind and cover are those of the series list, S2 and S3 of
`specs/series-list.md`. Proof: the shape of the answer verified by
Contracteer on both sides; backend scenario test over HTTP; frontend unit
test of the order; frontend scenario test over the fakes, from a row of the
series list to the page. Going back with the phone's back button, from the
page to the list, follows from the addresses and is the browser's: checked
by hand on the Pixel.

**S2 The owned tomes and the gaps** · frontend

```gherkin
Given a series of which the reader owns tomes 1 to 25, 36, 37 and 50

When they open the series

Then the screen says they own 1 à 25, 36, 37, 50
	And that 26 à 35, 38 à 49 are missing
	And lists a row for each run of missing tomes, in its place among
	    the tomes
```

A run of three tomes or more reads as its ends (*26 à 35*), a run of two
as both (*4, 5*), a tome alone as itself (*4*). Words in albums when the
series' kind is BD, in tomes otherwise, as series-list S2 counts. Proof:
frontend unit test of the runs and the gaps, one per case: a tome alone, a
run of two, a run of three or more, two editions of one tome, an edition
without a tome, no gap; frontend scenario test over the fakes, three cases:
the tomes of the Given, a BD series in albums, a series without a gap
showing no missing tomes.

**S3 The authors and the publishers** · frontend

```gherkin
Given a series whose editions have authors and publishers, some shared,
      one author only a translator

When the reader opens the series

Then the screen names each of its authors once, translators left out
	And each of its publishers once
```

The authors in the order of the card (DESIGN U06), by their first role
then by name, the publishers by name, both ignoring case and accents; an
author is the same one by name, as PRD §3 matches them. Proof: frontend
unit test of the authors and of the publishers; frontend scenario test
over the fakes, one case.

**S4 The band of a series** · frontend

```gherkin
Given a series

When the reader opens it

Then the screen shows its cover as a band above its name
	And no band when the series has no cover or the cover does not load
```

The cover is the series' own, series-list S3's: the cover of its first
edition in the order a shelf reads. Proof: frontend scenario test over the
fakes, three cases: a cover, none, a cover that fails to load. The crop
and the height are checked by hand on the Pixel.

**S5 No such series** · frontend, backend

```gherkin
Given a series none of whose editions sits on a bookshelf the reader
      belongs to

When the reader opens it

Then Libris answers that there is no such series
	And the screen says the series cannot be found
```

Two cases: a series Libris does not know, one only on bookshelves the
reader does not belong to; the series whose last copy the reader has just
removed comes with removing a copy, which Libris does not offer yet. An
address whose id is not one reads the same way on the screen.
Proof: contract example `404_NOT_IN_MY_CATALOGUE` verified by Contracteer
on both sides, the id that is not one its generated case; backend scenario
test over HTTP, one per case; frontend scenario test over the fakes, the
no such series and the id that is not one.

**S6 Libris unavailable** · frontend

```gherkin
Given a reader opening a series

When Libris does not answer

Then the screen says to try again later
	And shows nothing of the series
```

Proof: frontend scenario test with the API failing.

## Screen

Mockups: <https://claude.ai/artifact/TqcQHTCC7APD8HQXPGPjjF>, one
artboard per state below, exported as `specs/series-page/<n>-<state>.jpg`
in the same order; the listed state is option D of the layouts weighed,
<https://claude.ai/artifact/86pggv3YHgDGQv6HBYLKXH>. Route
`/catalogue/series/{id}`, reached from a row of the series list; the
*Catalogue* tab is in `accent`.

`docs/DESIGN.md` gains with this spec: in U03, the band above the header
and the spacing under it, and a row that leads to a screen with no
chevron, so the bookshelf rows of the home page lose theirs; in U07,
`ChevronRight` gone; in U08, the series' name as the title of its screen.

The series list (`specs/series-list.md`) changes: a row leads to the
series' page, the whole row a link, with no chevron.

Top to bottom on a phone, everything but the tab bar scrolling together:

- **The band** (U03): the series' cover, named *Couverture de* and the
  series' name.
- **The header**: the series' name as the title; under it the hint, the
  series' count followed by *dans vos bibliothèques.* (*28 tomes dans vos
  bibliothèques.*, *1 album dans vos bibliothèques.*), the words of
  series-list S2.
- **The card**: a card (U06) of field rows alone, the label at the left
  and the value at the right:
  - *Tomes possédés*, or *Albums possédés* for a BD: the runs owned (*1 à
    25, 36, 37, 50*);
  - *Tomes manquants*, or *Albums manquants*: the runs missing; no row
    when none is missing;
  - *Auteur*, or *Auteurs* for more than one: the names alone, separated
    by commas; no row when no author is left;
  - *Éditeur*, or *Éditeurs* for more than one: the names, separated by
    commas; no row when no edition has one.

  The kind is not shown. The series details will add *Tomes parus* there.
- **The list**: the catalogue's block, with no label, one row per edition
  in the order of S1, an edition without a tome after the numbered ones.
  A row is the catalogue's row (`specs/catalogue.md`) with the overline
  *Tome 23*, or *Album 23* for a BD, and no overline for an edition
  without a tome; the title, the authors and the bookshelves as there. A
  row leads to its ouvrage's page (`specs/edition-page.md`), the whole
  row a link, with no chevron.
  Among the rows, in its place, one row per run of missing tomes, on
  `bg`: a dashed outline in `muted` where the cover stands, empty, and at
  its right the run in the row title step, in `muted`, *Tomes 26 à 35* or
  *Tome 4* (*Albums*, *Album*), over *Manquants*, or *Manquant* for one,
  in `body`, `muted`.

The screen asks the series anew on each arrival, so an ouvrage added or
removed a moment ago counts. There is no empty state: a series with no
edition on the reader's bookshelves is not found.

States:

- **Loading**: no band; a skeleton bar for the title and one for the hint,
  the card's skeleton with two rows, and five skeleton rows, until the
  answer arrives.
- **Listed** (S1, S2, S3, S4): the band, the header, the card with its
  four rows, and the list, a run of missing tomes among the rows.
- **Not found** (S5): no band, no title, and *Série introuvable.* as the
  screen's one message.
- **Unavailable** (S6): no band, no title, *Erreur lors du chargement,
  veuillez réessayer plus tard.* with the alert icon.

## Contract

Release `v0.13.0` of `camory/libris-api`, after `v0.12.1`. An existing
answer gains a field, which a client reading the document ignores until
it needs it: one operation and one schema are added.

- `Series` gains `id`, a uuid, required: the series' own, the row its name
  has in persistence. The series list answers it; the page is asked by it.
- `SeriesWithBooks`: `Series` plus `books`, an array of `Book`, at least
  one, the editions of the series on a bookshelf the reader belongs to,
  each with its copies on those bookshelves, in no promised order, the
  screen sorting them (S1). Not paged: a series holds a few dozen tomes.
- `GET /api/v1/series/{id}` → `200` `SeriesWithBooks`; `400` `Problem`
  `/problems/validation` for an id that is not one, the screen showing it
  as *Not found*; `404` `Problem` `/problems/not-found` when no edition of
  the series sits on a bookshelf the reader belongs to. Key
  `404_NOT_IN_MY_CATALOGUE`.

The series is an aggregate of the bibliography (D12): the id is its
`SeriesId`, and the books are read across the editions that refer to it,
the series holding no list of them. The runs, the gaps, the authors and
the publishers are the screen's, derived from the books.

## Done

On the Pixel, from the installed app on staging: in *Catalogue*, *Séries*,
tap *Tintin*, owning tomes 20 and 21; read *Tomes manquants 1 à 19*, the
run of missing tomes as the first row, and the cover of tome 20 as the
band; go back with the phone's button and land on the series list; tap
*Dragon Quest*, owning tome 30, and read *1 à 29* missing. On the second
account of the family, paste the address of *Tintin*'s page and read
*Série introuvable.* The other cases are the tests' to show.

Checked by Tophe on the Pixel, recorded on 2026-10-20.

## Tasks

- T101 — S5, the backend on `v0.13.0` — backend
- T102 — S1 — backend
- T103 — S5, the frontend on `v0.13.0` — frontend
- T104 — S1 — frontend
- T105 — S2 — frontend
- T106 — S3 — frontend
- T107 — S4 — frontend
- T108 — S6 — frontend
