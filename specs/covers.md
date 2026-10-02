# Covers

**Why:** PRD §3, §4.1 and §4.7. In the bookshop, the card shows the cover the
sources hold for a scanned ouvrage and the reader picks the one they
recognise; at home, the catalogue shows every edition with its cover, kept by
Libris. Now, because the catalogue's rows load the sources' pictures from the
phone, the BnF refuses its whole catalogue after about seventy pictures in two
minutes, one page and a lookup, and a book the BnF has no picture for hides
the picture Open Library has.
**Status:** draft

## Scenarios

**S1 The lookup offers the sources' covers** · frontend, backend

```gherkin
Given an ISBN the house lacks that inventaire.io, Open Library and the BnF
      know, each with a picture

When the reader asks for it

Then Libris answers the candidates in that order, each named after its
     source
	And the card offers them
```

A source with no record or no picture to offer is absent from the list, which
may be empty. A candidate is handed out unchecked, as the cover is today: the
phone finds out. inventaire.io is asked for its picture alone; no field of the
card comes from it. Proof: contract example `ONE_PIECE_1` verified by
Contracteer on both sides; backend scenario test over HTTP with the three
sources stubbed; frontend scenario test over the fakes.

**S2 The card shows the first cover that loads** · frontend

```gherkin
Given the card of S1

When its candidates load

Then the card shows the first that loads, with the name of its source
     under it
	And one dot per candidate that loaded, the shown one filled
```

Three cases: the reader taps another dot, and the card shows that cover with
its name; a candidate that does not load is not offered, so its dot never
appears; none loads, and the card shows the stand-in, no name, no dot. Proof:
frontend scenario test over the fakes, one per case.

**S3 The add carries the cover's source** · frontend, backend

```gherkin
Given the card of S2 showing a cover

When the reader adds the ouvrage

Then the edition exists with the source of the cover shown
	And Libris answers the copy before asking any source for a picture
	And the card shows the copy, as the bookshelf spec says
```

A card showing the stand-in adds the edition with no source. Proof: the add
operation verified by Contracteer on both sides, its `201` case keyed
`201_ADD_ONE_PIECE_1` and checked on its schema; backend scenario test over HTTP with a source that never answers; frontend
scenario test over the fakes.

**S4 The worker fetches the chosen cover** · backend

```gherkin
Given an edition stored with a chosen source

When the worker runs

Then it asks that source alone for the picture of the edition's ISBN
	And stores it, normalised, under a name made from its bytes
	And the catalogue answers the edition with its cover's address
```

Proof: backend scenario test over HTTP with the source stubbed, the worker
woken by the test as the add wakes it.

**S5 Libris serves a stored cover** · frontend, backend

```gherkin
Given a stored cover

When the screen asks for its address

Then Libris answers the picture, in the format it was stored in
	And says it may be kept for a year, since a name never changes its
	    picture
```

A text that is not a cover name is refused; a well-formed name no stored
cover bears is not found. Proof: the cover operation verified by Contracteer
on the backend, any image for a well-formed name, the refusal of a text that
is not one; backend scenario test over HTTP for the media type saved beside
the picture, in `<name>.type`, the headers and the not-found.

**S6 The picture is normalised** · backend

```gherkin
Given a picture fetched from a source

When it is taller than 600 pixels

Then it is scaled to 600 pixels tall, its proportions kept, and encoded as
     JPEG
```

Two cases: a taller picture, scaled; a picture 600 tall or less, kept as
fetched in its own format. inventaire.io is asked for its picture at 600
tall, so a WebP never needs scaling. Proof: unit test of the rule, one per
case, over generated pictures.

**S7 An edition without a chosen source gets the cascade** · backend

```gherkin
Given an edition with an ISBN and no chosen source

When the worker runs

Then it asks inventaire.io, then Open Library, then the BnF, stopping at
     the first picture
	And stores it as S4 does
	And an edition no source has a picture for stays without
```

Three cases: the first source has it; only the last has it; none has it.
Proof: backend scenario test over HTTP with the sources stubbed, one per
case.

**S8 A failed fetch waits a day** · backend

```gherkin
Given an edition whose source does not answer, answers an error, or
      answers what is not a picture

When the worker runs

Then the edition stays without a picture and the attempt is dated
	And a run within a day passes it by
	And a run a day later tries again
```

A fetch gives up after five seconds or five megabytes. Proof: backend
scenario test over the stubbed sources with the clock controlled, one per
case.

**S9 The worker runs on its own** · backend

```gherkin
Given editions awaiting a picture

When Libris starts, or a day has passed, or an ouvrage is added

Then the worker takes them one at a time, in the order they were added
```

Proof: backend scenario test over the stubbed sources, the run triggered by
the test; the schedule itself is configuration, checked by hand on staging's
log.

**S10 A held edition offers its own cover** · frontend, backend

```gherkin
Given an edition the house holds, its cover stored

When a reader asks for its ISBN

Then Libris answers the edition's id and one candidate, its own
	And the card shows it with no dot and no name under it, the id
	    telling it the choice is over
```

An edition the house holds whose picture is not yet stored answers its id
and no candidate: the stand-in. The candidate's source is a name like any
other; no side reads it. Proof: contract example `ONE_PIECE_2_OWNED`
verified by Contracteer on both sides; backend scenario test over HTTP;
frontend scenario test over the fakes.

**S11 The catalogue shows the covers** · frontend

```gherkin
Given the reader's catalogue, editions with a stored cover and one without

When they open it

Then each row shows its cover, and the stand-in where there is none yet
```

Proof: the shape of the answer verified by Contracteer on both sides;
frontend scenario test over the catalogue's fake.

**S12 The editions stored before this feature** · backend

```gherkin
Given editions stored before covers were kept, each with an ISBN

When Libris starts after the deploy

Then they await a picture with no chosen source, and S7 takes them
```

Proof: S7 over an edition inserted with no source; the migration is proven
by the gate migrating the test database.

## Screen

No screen of its own: the lookup screen of `specs/fast-entry.md`, route
`/isbn`, whose card gains the choice of the cover; and the catalogue screen
of `specs/catalogue.md`, route `/catalogue`, whose rows load their cover
from Libris instead of a source.

In the card's top part, under the cover block: the dots, one per candidate
that loaded, the shown one filled, each a button named after its source,
*Couverture inventaire.io*; then the name of the shown cover's source in
`muted`, *inventaire.io*, *Open Library*, *BnF*. The cover of an edition
the house holds draws neither dots nor name, the choice being over; the
stand-in draws neither. The fields above keep their silence about the
sources: the name says where the picture comes from, nothing else.

States, on top of those of the lookup and the bookshelf specs:

- **Found, with covers** (S1, S2): the card, the first cover that loaded,
  its dots and its source's name.
- **Switched** (S2): the card after a tap on a dot, the other cover and its
  name, that dot filled.
- **Found, no cover** (S2): the card with the stand-in, no dot, no name.
- **Already there** (S10): the card with the house's cover, no dot, no
  name.
- **Added** (S3): as the bookshelf spec's Added; the cover and its name
  stay as they were.
- **Catalogue** (S11): the rows with their covers, the stand-in on a row
  whose picture is not there yet.

Mockups: <https://claude.ai/artifact/CVaFzHpi83fCnxuC8Fv1fX>, one
artboard per state drawn for this feature, *Found, with covers*, *Switched*
and *Already there*, exported as `specs/covers/<n>-<state>.jpg` in that
order; the other states repeat artboards of the lookup, bookshelf and
catalogue specs.

## Contract

Release `v0.8.2` of `camory/libris-api`, after the catalogue's `v0.7.0`.
One field leaves the shared schema, each composition gains its own, one
operation is new; the lookup keeps its old field until the frontend reads
the new one, so each side deploys alone.

- `Edition` loses `coverUrl`: the three schemas composed over it each mean
  something else by a cover.
- `IsbnLookup` gains `id`, required, nullable: the house's edition when it
  holds the ISBN, null when it does not; and `covers`, required, an ordered
  array of `CoverCandidate` `{source, url}`, `source` the name Libris knows
  the source by, to give back on the add, no enumeration so that a source
  comes or goes without a release, `url` the picture's address, empty when
  no source offers one; and keeps `coverUrl`, nullable, deprecated, the
  first candidate's `url` or null. `ONE_PIECE_1` answers no id and three
  candidates in cascade order, `inventaire.io`, `Open Library`, `BnF`;
  `ONE_PIECE_2_OWNED` answers an id and one candidate, `Libris`, at the
  address of the cover operation.
- `NewBook` gains `coverSource`, nullable, a string: the source of the cover
  the card showed, by the name the lookup gave, null when it showed the
  stand-in. The add's request examples send `inventaire.io`. On an ISBN the
  house holds the field is ignored; on one it does not, a name that is not a
  source's, the house's own included, is taken as null: the edition is added
  with no source.
- `Book` gains `coverUrl`, nullable: the relative address of the cover
  operation, null while no picture is stored.
- `GET /api/v1/covers/{name}`, `name` the picture's hash in hexadecimal, a
  `pattern` on the parameter → `200`, the body binary, `image/*`, the media
  type the picture was stored with, with
  `Cache-Control: public, max-age=31536000, immutable`; `400` `Problem`
  `/problems/validation` for a text that is not a cover name; `404`
  `Problem` `/problems/not-found` for a well-formed name no stored cover
  bears. The `200` case generated, the `400` keyed `400_NOT_A_COVER_NAME` on
  the path example `ABC`, the `404` keyed `404_NO_COVER`.

Release `v0.9.0`, after the frontend's deploy: `IsbnLookup` loses
`coverUrl`. Nothing else changes.

Outside the contract, for the PR bodies: the covers live as files in a
directory the backend receives through one environment variable, a second
named volume of the production compose, registered with the server's backup
by the runbook, which amends D09; the sentence of `specs/fast-entry.md`
that says the card never names a source is amended for the cover alone; the
dots and the name under the cover are a rule for U06 of `docs/DESIGN.md`,
proposed with the mockups. The worker is one piece of the backend's
infrastructure behind a port of the application, which the add and the
tests call the same way; the tests give it a temporary directory and stubbed
sources serving real bytes, a small JPEG and a small WebP.

## Done

On the Pixel, from the installed app on staging: scan an ouvrage the house
lacks; the card shows a cover with its source under it; tap a dot, the cover
changes; add it; open the catalogue, the row shows that cover, after a
second visit if the first came too soon; scan it again, the card shows that
cover with nothing under it. The editions from before the deploy
show their covers, save those no source has a picture for.

## Tasks

- T051 — S5 not found, S10 not yet stored, the backend on `v0.8.2` — backend
- T052 — S1 — backend
- T053 — the source of S3 kept by the add, no scenario of its own — backend
- T065 — the cover of S4 stored and answered by the catalogue, no scenario
  of its own — backend
- T066 — S3, S4, inventaire.io chosen — backend
- T054 — S5 stored, Open Library chosen — backend
- T055 — S10 stored — backend
- T056 — S6 — backend
- T057 — S7, S12 — backend
- T058 — S8 — backend
- T059 — S9, and S12 at start — backend
- T060 — S1, S11, the frontend on `v0.8.2` — frontend
- T061 — S2 — frontend
- T062 — S3 — frontend
- T063 — S10 — frontend
- T064 — the backend on `v0.9.0` — backend
