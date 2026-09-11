# Fast entry

**Why:** PRD §4.7. A reader in a bookshop or in front of a shelf scans the
barcode of an ouvrage, or types its ISBN, and reads what the public sources
know about it. Nothing is stored: adding the edition and a copy is another
feature, and this one shows first how good the sources are.
**Status:** draft

## Scenarios

**S1 — Typed ISBN, found**
Given a reader on the lookup screen
When they type an ISBN, with or without hyphens or spaces, thirteen digits or
the old ten, and submit
Then one card shows what the sources know: title, subtitle, authors with their
roles, series and tome, collection, publisher, publication year, language,
page count, summary, cover, the ISBN-13, and which sources answered. No kind
is shown: no source gives one.
Proof: contract example verified by Contracteer on both sides; component test
of the screen against `contracteer mock`.

**S2 — Scanned barcode**
Given a reader on the lookup screen who allowed the camera
When the camera sees an EAN-13 starting with 978 or 979
Then the same lookup runs with it and the card of S1 shows. Any other barcode,
the five-digit price add-on included, is ignored. Scanning uses the browser's
`BarcodeDetector`, present in Chrome and Brave on Android; where it is
absent the screen offers only the text field.
Proof: component test with a stubbed `BarcodeDetector` answering an EAN-13,
then a non-ISBN barcode; checked by hand on the Pixel.

**S3 — Not an ISBN**
Given a reader on the lookup screen
When they submit a text of the wrong length or with a wrong check digit
Then the screen says beside the field that this is not an ISBN, without
calling the API; and the API, given thirteen digits with a wrong check digit,
answers the validation problem naming the field `isbn`.
Proof: unit test of the ISBN rule in the frontend (thirteen digits kept, ten
converted, check digit verified, separators dropped); contract example
`400_NOT_AN_ISBN` verified on both sides.

**S4 — Unknown ISBN**
Given an ISBN no source knows
When the reader submits it
Then the API answers the not-found problem and the screen says no source
knows this ISBN.
Proof: contract example on the 404; use-case test over fake sources that
answer nothing.

**S5 — Merged answer**
Given an ISBN both the BnF and Open Library know
When the reader submits it
Then the card carries the BnF's value for every field the BnF gives, Open
Library's for the fields the BnF leaves empty, Open Library's cover by ISBN,
and lists both sources.
Proof: use-case test over fakes answering different fields.

**S6 — One source down**
Given a source that fails or does not answer in time while the other answers
When the reader submits an ISBN
Then the card shows the answer of the source that replied and lists only that
source.
Proof: use-case test with one failing fake.

**S7 — Every source down**
Given every source failing or not answering in time
When the reader submits an ISBN
Then the API answers the sources-unavailable problem and the screen says to try
again later.
Proof: contract example on the 503; use-case test with failing fakes.

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
key, cover `covers.openlibrary.org/b/isbn/<isbn>-L.jpg?default=false`).
Google Books waits for PRD open question 6.

## Done

On the Pixel, from the installed app: scan a manga and a BD and read both
cards, sources included; type an ISBN-10 by hand and read its card; type a
wrong ISBN and read the message.

## Tasks

