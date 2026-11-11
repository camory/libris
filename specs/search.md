# Search

**Why:** PRD §4.2. From the catalogue, a reader finds an ouvrage by a word
of its title, its series, one of its authors, its tome or its ISBN, as
they type, misspelled or not, without scrolling to it. Now, because the
catalogue lists every ouvrage of the house one by one, fifty at a time,
and finding one means scrolling until it shows.
**Status:** draft

## Scenarios

The search narrows the catalogue, never widens it: the editions with a
copy on a bookshelf the reader belongs to (catalogue S1), with their
copies on those bookshelves, in the catalogue's rows and in its order. An
edition the house holds on no bookshelf of the reader's is never found.
The box sits on the ouvrages of the catalogue alone; the series view is
unchanged.

A query is made of words, every one required, in any field and in any
order. A word is found in an edition when it begins a word of its title,
its subtitle, its series' name or one of its authors' names, ignoring
case and accents: *ast* is found in *Astérix*, *uderzo serpe* in *La
Serpe d'or* by *Albert Uderzo*. A word of digits alone is also found in
the edition whose tome is that number, exactly: *one piece 2* finds tome
2, neither tome 12 nor tome 20. *tome* and *album* are words like any
other, found only where a field holds them.

The results stand in the order a shelf reads, the catalogue's, a series'
editions together, whether the words or the near matches found them: no
ranking by relevance. PRD §4.2 changes with this spec: the search finds
editions by title, subtitle, series, tome, author and ISBN, tolerates
typos when the words find nothing, and answers in the order a shelf
reads, in place of "ranked by relevance, grouped or sortable by series".
`docs/ARCHITECTURE.md` D03 changes with it: one query in that order, not
ranked; one text search configuration, `unaccent` then plain tokens,
French stemming dropped, the near matches covering what it caught (a
plural where the title has the singular); the trigram index serving only
when the words find nothing; the tome read beside `search_text` for a
word of digits.

Not in this spec: tags, which Libris does not hold yet; the publisher and
the collection; the filters and sorts of PRD §4.1; a search of the series
view or of a bookshelf; the house beyond the reader's catalogue; the
words found highlighted in the rows; a history of searches and
suggestions; a search offline; a ranking by relevance.

**S1 Words find the editions holding them** · frontend, backend

```gherkin
Given editions the house holds, with copies on bookshelves the reader
      belongs to and on one they do not

When the reader searches their catalogue for words

Then Libris answers the editions of their catalogue in which every word
     is found, once each, with their copies on those bookshelves
	And in the order of the catalogue
	And the screen lists them in that order
```

The screen sends the box's text without the spaces at its ends; Libris
reads the words. A query of one character is a search like any other for
Libris: the screen sends none (S6). An empty or blank query is no search:
Libris answers the catalogue. Proof: the shape of the answer, `near`
included, verified by Contracteer on both sides; backend scenario test
over HTTP, one per case: *asterix* answering two *Astérix* albums in the
order of the catalogue; *ast* answering them; *uderzo serpe* answering
*La Serpe d'or* and not another album of *Albert Uderzo*; *one piece 2*
answering tome 2 and neither tome 12 nor tome 20; an edition whose copies
sit on another reader's bookshelf alone not answered to its title;
frontend scenario test over the catalogue's fake, the rows the answer's,
in its order.

**S2 A whole ISBN finds its edition** · backend

```gherkin
Given editions of the reader's catalogue

When the reader searches it for the ISBN of one of them

Then Libris answers that edition alone
```

An ISBN is the whole query, its hyphens and spaces ignored, read as an
ISBN typed by hand is read: thirteen digits, or an ISBN-10 converted (PRD
§3). It finds its edition or nothing, never a near match (S3). Fewer
digits are words, as S1 says, found in no ISBN: twelve digits of an ISBN
do not find its edition. The screen sends the ISBN as typed; reading it
is Libris's. Proof: backend scenario test over HTTP, one per case: the
thirteen digits, the same with hyphens, the ISBN-10 of the same edition,
each answering it alone; its first twelve digits answering none.

**S3 Near matches when the words find nothing** · frontend, backend

```gherkin
Given editions of the reader's catalogue

When the reader searches it for words no edition holds

Then Libris answers the editions nearest to them, said to be near
	And the screen says that no ouvrage matches, then lists the nearest

When no edition is near either

Then Libris answers none, not said to be near
	And the screen says that no ouvrage matches
```

Near is the typo tolerance of PRD §4.2: a word misspelled, *asterik* for
*Astérix*, a letter missing or doubled. Libris looks for near matches
only when the words find nothing, so an edition the words find never
stands among near ones; how near is near is Libris's setting, no promise
of the contract. An answer with no edition is never said to be near. The
words are *Aucun ouvrage ne correspond à « asterik ».*, the query between
guillemets as sent, then *Les plus proches :* above the near rows. Proof:
backend scenario test over HTTP, two cases: *asterik* answering the
*Astérix* albums, said to be near; *gaston* answering none, not said to
be near; frontend scenario test over the catalogue's fake, two cases: the
fake answering near rows to *asterik*, the sentence, *Les plus proches :*
and the rows; the fake answering none to *gaston*, the sentence alone.

**S4 Results come in pages** · frontend, backend

```gherkin
Given more editions in which the words are found than a page holds

When the reader searches their catalogue for them

Then Libris answers the first page, in the order of the catalogue, and
     names the next
	And reaching the end of the list asks for the next page of the same
	    words, which follows the first without a gap or a repeat
	And the last page names no next
```

The pages are the catalogue's (catalogue S2): fifty editions, and an
`after` naming the last one received. Near matches come in pages too,
each said to be near. Proof: backend scenario test over HTTP with
fifty-one editions in which the words are found, the first page naming a
next and the second none; frontend scenario test over the catalogue's
fake with two pages of a search, the second asked with the same query
and the first page's next.

**S5 An edition is found by what it holds now** · backend

```gherkin
Given an edition added through Libris
	And an edition whose title a reader changed

When the reader searches their catalogue for the words of each

Then Libris answers the added edition
	And the changed edition for its new title, never for its old one
```

Libris keeps what an edition is searched by with the edition, rewritten
each time it is added or saved; its tome is read as stored. A name
respelled for the house (held-edition-edit S5) changes only capitals,
accents and the spaces at its ends, which the search ignores: the other
editions carrying it are found as before. The editions the house held
before this spec are found from its release on, checked by hand on
staging (Done). Proof: backend scenario test over HTTP, two cases: an
edition added, answered to its title; an edition saved with another
title, answered to the new and not to the old.

**S6 The search runs when the reader pauses** · frontend

```gherkin
Given the reader's catalogue

When the reader types words in the search box and pauses

Then the screen asks Libris for them
	And lists the answer in place of the rows
```

The pause is 300 milliseconds, a keystroke within it starting it again:
one request for a word typed at speed. The search key of the keyboard
asks at once and closes the keyboard. Fewer than two characters, the
spaces at the ends not counted, ask nothing: the whole catalogue shows,
as without a search. The cross empties the box and shows the whole
catalogue at once. While Libris answers, the rows shown stay, with no
skeleton, until the answer replaces them. Proof: frontend scenario test
over the catalogue's fake with the clock faked, one per case: a word
typed at speed then a pause, one request; the search key, a request with
no pause; one character, the whole catalogue; the cross, the whole
catalogue.

**S7 The search survives a visit** · frontend

```gherkin
Given a search the screen lists

When the reader opens one of its ouvrages and comes back

Then the box holds the words
	And the screen lists their answer
```

The address carries the query of the list shown, `/catalogue?q=goscinny`,
and none with fewer than two characters; each search replaces the
address in the history instead of adding one, so going back leaves the
catalogue and never steps through the queries typed. An address with a
query opens on its search, the box holding it. *Ouvrages* of the switch
and the *Catalogue* tab open the catalogue with no query: going to
*Séries* and back empties the box. Proof: frontend scenario test over the
router and the fakes, one per case: back from an ouvrage, the box and the
rows of the search; two searches one after the other, one entry in the
history; *Séries* then *Ouvrages*, the box empty and the whole catalogue.

**S8 Only the latest search counts** · frontend

```gherkin
Given a search asked, and another asked before the first is answered

When the answer to the first arrives after the answer to the second

Then the screen lists the answer to the second
	And never the answer to the first
```

The next page of an earlier search, arriving late, is dropped too.
Proof: frontend scenario test over a fake answering out of order.

**S9 Libris unavailable** · frontend

```gherkin
Given a search asked

When Libris does not answer

Then the screen says to try again later
	And lists nothing
```

The words are *Erreur lors de la recherche, veuillez réessayer plus
tard.* The box keeps its text and its outline: nothing in it is wrong.
Two cases: the first page, which lists nothing; the next page, after
which the rows already listed stay and the sentence sits under them.
Proof: frontend scenario test with the API failing, one per case.

**S10 No search box over an empty catalogue** · frontend

```gherkin
Given a reader whose bookshelves hold no copy

When they open their catalogue

Then the screen says what will fill it
	And shows no search box
```

The box shows from the arrival with the loading rows, and leaves when the
catalogue asked with no query answers an empty first page; an empty
answer to a query is S3's. Proof: frontend scenario test over the
catalogue's fake.

## Screen

Mockups: <https://claude.ai/artifact/1GndU1S8Yntbga8ZH9Wan5>, one artboard
per state marked *drawn* below, exported as `specs/search/<n>-<state>.jpg`
in the same order. No route of its own: the ouvrages of the catalogue,
route `/catalogue`, of `specs/catalogue.md` and `specs/series-list.md`,
the query in `?q=`.

Top to bottom on a phone: the header of the catalogue, unchanged; the
switch; the search box; the list, or what stands in its place; the tab
bar. The box is not sticky: it scrolls away with the list.

The search box is a field (U04) with no label above it: its
`aria-label` *Rechercher dans le catalogue*, its placeholder *Titre,
série, auteur, ISBN*, the glyph `Search` at its left inside the box in
`muted`, as decoration; once the box holds text, the cross `X`
*Effacer la recherche* at its right, an icon button, and no clear button
of the browser's own. After a tap on the cross, the focus is in the box
and the keyboard open. It is a search input whose keyboard key reads
search. No primary button: the search runs as the reader types (S6).

Under the box, the rows of the catalogue, as `specs/catalogue.md` draws
them, for the editions Libris answered, in its order; a row leads to its
ouvrage's page. On arrival with a query, the catalogue's loading rows
stand under the box holding it, until the answer.

When the words find nothing, the sentence of S3 stands in the list's
place, in `lead`, `muted`, left-aligned, with no icon; with near matches,
*Les plus proches :* labels the block of their rows under it (U03).

`docs/DESIGN.md` changes with this spec: U04 gains a *Search field*
paragraph, a field with no label, its placeholder naming what it searches
in place of an example value, the glyph at its left, the cross at its
right once it holds text, the focus kept in the box after the cross, and
no primary button; U05 gains a *No match* paragraph, the sentence of a
search that finds nothing, in `lead`, `muted`, left-aligned, with no
icon, which says that nothing matches rather than what will fill the
space; U07 counts the glyph of a search field among its icons at 22.

States:

- **Box** (S6), drawn: the catalogue listed under the empty box, its
  placeholder and its glyph.
- **Found** (S1), drawn: *goscinny* in the box with its cross, the rows
  of his ouvrages in the order of the catalogue.
- **Searching** (S6, S8): the rows shown stay under the box until the
  answer replaces them, no skeleton.
- **Near** (S3), drawn: *asterik* in the box; *Aucun ouvrage ne
  correspond à « asterik ».*, then *Les plus proches :* above the rows of
  the *Astérix* albums.
- **Nothing** (S3), drawn: *gaston* in the box; *Aucun ouvrage ne
  correspond à « gaston ».* in place of the list.
- **Loading more** (S4): the catalogue's two skeleton rows under the
  last row while the next page comes.
- **Unavailable** (S9), drawn: *Erreur lors de la recherche, veuillez
  réessayer plus tard.* with the alert icon in place of the list, the box
  keeping its text and its outline; when the next page is the one that
  does not come, the rows already listed stay and the sentence sits under
  them.
- **Empty catalogue** (S10): the catalogue's empty state with no box,
  `specs/catalogue/4-empty.jpg`, not drawn again.

## Contract

To release with Tophe: `v0.19.0` of `camory/libris-api`, after
`v0.18.0`; one parameter and one property added, nothing else changed:

- `GET /api/v1/books` gains the query parameter `q`, optional, a string,
  the words of the search; absent, empty or blank, the catalogue as
  before → `200` `BookPage`, the books of the reader's catalogue the
  words find, or the nearest (S3), in the order of the catalogue, fifty
  at a time, `after` as before. No new problem: any text is a query;
  `400` `Problem` `/problems/validation` as before when `after` is not an
  id.
- `BookPage` gains `near`, a boolean, required: true when its books are
  the nearest to words that find none, false otherwise, on an empty page
  and with no `q`.

Contracteer verifies the answer's shape, `near` included, with no
example: its generated cases always carry a random `q` beside `after`,
never a request without one, and its mock answers any `q` with a page
(measured on Contracteer 4.1.1 at the release). The words, the ISBN, the
near matches and the pages of a search are the tests' to show.

## Done

On the Pixel, from the installed app on staging: in *Catalogue*, read the
box under the switch. Type *goscinny*: after a pause, the rows of his
ouvrages in the order of the catalogue. Open one and come back: the box
and the rows as left. Type a series and the number of a tome you hold:
that tome alone. Type *asterik*: the sentence, *Les plus proches :* and
the *Astérix* albums. Type *gaston*: the sentence alone. Type the ISBN of
an ouvrage with its hyphens: that ouvrage alone. Type the title of an
ouvrage stored before the release: it is found. Tap the cross: the whole
catalogue, the keyboard open.

## Tasks

Filled by the planner.
