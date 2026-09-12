# Fast entry

**Why:** PRD §4.7. A reader in a bookshop or in front of a shelf scans the
barcode of an ouvrage, or types its ISBN, and reads what the public sources
know about it. Nothing is stored: adding the edition and a copy is another
feature, and this one shows first how good the sources are.
**Status:** draft

## Scenarios

**S1 Typed ISBN, found** · frontend, backend

```gherkin
Given an ISBN the sources know

When the reader asks for it, typed with or without hyphens or spaces,
     thirteen digits or the old ten

Then Libris answers what the sources know: title, subtitle, authors with their
     roles, series and tome, collection, publisher, publication year, language,
     page count, summary, cover, the ISBN-13, and which sources answered
```

Proof: contract example `ONE_PIECE_1` verified by Contracteer on both sides;
backend scenario test over stubbed sources; frontend scenario test of the
screen against `contracteer mock`.

**S2 Scanned barcode** · frontend

```gherkin
Given a reader on the lookup screen who allowed the camera

When the camera sees an EAN-13 starting with 978 or 979

Then the same lookup runs with it and the card of S1 shows
```

Scanning uses the browser's `BarcodeDetector`, present in Chrome and Brave on
Android; where it is absent the screen offers only the text field.
Proof: frontend scenario test with a stubbed `BarcodeDetector`; checked by
hand on the Pixel.

**S3 Not an ISBN** · frontend

```gherkin
Given a reader on the lookup screen

When they submit a text of the wrong length or with a wrong check digit

Then the screen says beside the field that this is not an ISBN

And no request leaves
```

Proof: unit test of the ISBN rule (thirteen digits kept, ten converted, check
digit verified, separators dropped); frontend scenario test. The API's own
check is the contract example `400_NOT_AN_ISBN`, verified on both sides.

**S4 Unknown ISBN** · frontend, backend

```gherkin
Given an ISBN no source knows

When the reader asks for it

Then Libris answers that no source knows it
```

Proof: backend scenario test over stubbed sources answering nothing, the
not-found problem; frontend scenario test against `contracteer mock` with
`404_UNKNOWN_ISBN`, the message.

**S5 Merged answer** · backend

```gherkin
Given an ISBN both the BnF and Open Library know

When the reader asks for it

Then the answer carries the BnF's value for every field the BnF gives

And Open Library's for the fields the BnF leaves empty

And Open Library's cover by ISBN

And lists both sources
```

Proof: backend scenario test over stubbed sources, one recorded BnF record
with fields blanked that Open Library fills; unit tests of the merge rule,
field by field, in the implementer's loop.

**S6 One source down** · backend

```gherkin
Given a source that fails or does not answer in time while the other answers

When the reader asks for an ISBN

Then the answer carries what the source that replied knows

And lists only that source

And keeps Open Library's cover by ISBN, whichever source is down
```

Proof: backend scenario test with one stub failing, then one stub answering
past the timeout.

**S7 Every source down** · frontend, backend

```gherkin
Given every source failing or not answering in time

When the reader asks for an ISBN

Then Libris answers that the sources are unavailable, to try again later
```

Proof: backend scenario test with both stubs failing, the sources-unavailable
problem; frontend scenario test against `contracteer mock` with
`503_SOURCES_DOWN`, the message.

## Screen

Mockups: <https://claude.ai/code/artifact/900be65c-b8a0-4766-a8b3-23ceb4e19422>,
one artboard per state below, exported as `specs/fast-entry/<n>-<state>.jpg`
in the same order. Route `/isbn`, reached from the home page's link and from
the tab bar.

Top to bottom on a phone: the title *Ajouter un ouvrage* and the line
*Scannez le code-barres ou saisissez l'ISBN.*; the field labelled *ISBN*,
numeric keyboard, placeholder `978-2-7234-8852-5`, the typed text kept as
typed; inside the field at the right, a barcode icon when the browser has a
`BarcodeDetector`, nothing otherwise; under the field the button *Chercher*,
full width; then the state of the lookup; at the bottom the tab bar,
*Accueil* and *Ajouter*, the latter active on this screen.

States:

- **Ready**: nothing under the button.
- **Camera open** (S2): the icon turns into a cross; the camera view, with
  corner brackets, fills the space between the button and the tab bar,
  centred. The first EAN-13 read closes it and runs the lookup; the cross
  closes it.
- **Searching**: the button reads *Recherche en cours…* with a spinner and
  accepts nothing; a grey placeholder of the card takes its place below.
- **Found** (S1): the card. Cover at the left; at its right the series and
  tome (*One piece · tome 1*), the title, the subtitle, the authors each with
  their roles (*Eiichirō Oda · scénario, dessin*); then one row per field,
  *Collection*, *Éditeur*, *Année*, *Langue*, *Pages*, *ISBN*; then
  *Sources* followed by one chip per source, *BnF*, *Open Library*. A field
  the answer leaves empty has no row.
- **Not an ISBN** (S3): the field outlined in red and *ISBN invalide* in red
  under it. No request leaves.
- **Unknown ISBN** (S4): the same, *ISBN inconnu*.
- **Sources unavailable** (S7): *Erreur lors de la recherche, veuillez
  réessayer plus tard.* in red under the field.

A message replaces the previous card or message; the field keeps its text.

## Contract

Release `v0.3.0` of `camory/libris-api`, one read-only operation:

- `GET /api/v1/isbn/{isbn}` (thirteen digits; the client turns the typed text
  or the barcode into the ISBN-13 first) → `200` `Isbn`: `isbn13`, `title`,
  nullable `subtitle`, `authors` as an array of `IsbnAuthor` `{name, role}`
  with `role` in `WRITER | ARTIST | COLOURIST | TRANSLATOR`, nullable
  `series` as `IsbnSeries` `{name, volumeNumber}`, nullable `collection`,
  `publisher`, `publicationYear`, `language`, `pageCount`, `summary`,
  `coverUrl`, and `sources` as an array of `BNF | OPEN_LIBRARY`; `400`
  `ValidationProblem` `/problems/validation` with `errors: [{field: isbn,
  code: not-an-isbn}]`; `404` `Problem` `/problems/not-found`; `503` `Problem`
  `/problems/sources-unavailable`. Examples `ONE_PIECE_1` (9782723488525),
  `400_NOT_AN_ISBN` (9782723488526, wrong check digit), `404_UNKNOWN_ISBN`
  (9782000000006), `503_SOURCES_DOWN` (9791000000008).

Problems carry no wording: the screen picks its text from `type`. A 5xx
without a problem body means Libris itself is unavailable, not the sources.
`contracteer mock` serves a problem only when the request's `Accept` lists
`application/problem+json`, which the client always sends.

Sources in this feature: the BnF SRU (`recordSchema=unimarcxchange`; the
role comes from the author field's function code, mapped against the BnF's
published list) and Open Library (`/isbn/<isbn>.json`, authors fetched by
key, cover `covers.openlibrary.org/b/isbn/<isbn>-L.jpg`).
Google Books waits for PRD open question 6.

## Done

On the Pixel, from the installed app, through the home page's link to the
lookup screen: scan a manga and a BD and read both
cards, sources included; type an ISBN-10 by hand and read its card; type a
wrong ISBN and read the message.

## Tasks

- T013 — the ISBN-13 value type and the Open Library source, no scenario
  of its own — backend
- T020 — S4, S7, the API's own check in S3 — backend
- T014 — the merge rule of S5 and S6 — backend
- T015 — S1, S5, S6 — backend
- T016 — the rule of S3, the client of S1, S4, S7 — frontend
- T017 — S3, S4, S7 — frontend
- T018 — S1 — frontend
- T019 — S2 — frontend
