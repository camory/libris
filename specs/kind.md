# Kind

**Why:** PRD §3 and §4.7. The card of a scanned ouvrage says whether it is a
livre, a BD or a manga, and the words on it follow, without the reader
choosing. Now, because the bookshelf spec stores the edition with its kind,
and the lookup is the only place that can fill it.
**Status:** draft

## Scenarios

The kind is read from the BnF record alone: field 105 says whether the
ouvrage is a comic strip, field 101 which language it was translated from.
Open Library never names a kind: its subjects are free text, differ inside
one series (Murena's first chapter carries none, its fifth *Bandes
dessinées*) and file the Apothicaire light novel under its manga (measured
2026-09-18).

**S1 A manga** · frontend, backend

```gherkin
Given an ISBN whose BnF record marks a comic strip translated from Japanese,
      Korean or Chinese

When the reader asks for it

Then Libris answers the kind MANGA
	And the card uses the words of a manga
```

Proof: contract example `ONE_PIECE_1` verified by Contracteer on both
sides, its answer carrying `kind`; backend scenario test over a recorded
BnF record; frontend scenario test against `contracteer mock`.

**S2 A BD** · frontend, backend

```gherkin
Given an ISBN whose BnF record marks a comic strip

When the reader asks for it

Then Libris answers the kind BD
	And the card uses the words of a BD
```

Murena, a French comic strip whose record names no original language.
An American comic translated from English, Spider-Man, is a BD all the
same: that branch is the rule's unit test. Proof: backend scenario test
over a recorded BnF record; unit test of the kind rule in the implementer's
loop; frontend component test of the card fed the kind.

**S3 A book** · frontend, backend

```gherkin
Given an ISBN whose BnF record marks no comic strip

When the reader asks for it

Then Libris answers the kind BOOK
	And the card uses the words of a livre
```

The Carnets de l'apothicaire light novel, whose record marks a novel where
a comic strip would be marked. Proof: backend scenario test over a recorded
BnF record; frontend component test of the card fed the kind.

**S4 No record says** · backend

```gherkin
Given an ISBN whose BnF record carries no mark, or that Open Library alone
      knows

When the reader asks for it

Then Libris answers the kind BOOK
```

A provisional legal-deposit record of the BnF, which carries no field
105. An ISBN the BnF lacks, answered by Open Library alone, takes the same
path. Proof: backend scenario test over a recorded provisional record; unit
test of the kind rule in the implementer's loop.

## Screen

No screen of its own: the card of the lookup screen of `specs/fast-entry.md`,
route `/isbn`, whose words follow the kind. The kind itself is not written
on the card.

The series line: *One piece · tome 1* for a manga and a livre, *Astérix ·
album 1* for a BD.

The authors: when every author has the same roles, the names alone on one
line, separated by commas, *Eiichirō Oda*, *Erckmann, Chatrian*; otherwise
one line per author with their roles, *René Goscinny · scénario* then
*Albert Uderzo · dessin*. The roles are compared as whole sets: an author
with scénario and dessin beside one with scénario alone are told apart, and
both lines carry their words. The words of a role by kind:

| Role | livre | BD, manga |
|---|---|---|
| WRITER | texte | scénario |
| ARTIST | illustration | dessin |
| COLOURIST | couleurs | couleurs |
| TRANSLATOR | traduction | traduction |

States: the *Found* state of the lookup, three times over, one per kind.

## Contract

Release `v0.5.0` of `camory/libris-api`, before the bookshelf spec's
`v0.6.0`. One field is added; the rest renames what the bookshelf will
reuse, and changes no byte of an answer, since a JSON body carries no
schema name: both sides move to `v0.5.0` in any order.

- `GET /api/v1/isbn/{isbn}` → `200` `Edition`, the schema `Isbn` renamed:
  the same fields, plus `kind`, required, never null, one of
  `BOOK | BD | MANGA`. `IsbnAuthor` becomes `EditionAuthor`, `IsbnSeries`
  `EditionSeries`.
- `ValidationProblem` is folded into `Problem`, which gains an optional
  `errors`, the array of `{field, code}` the validation problem carried;
  `type` stays the slug the client switches on and `status` an integer. The
  `400` of the lookup answers `Problem` with `errors`, its body unchanged.
- The example `ONE_PIECE_1` gains `kind: MANGA`; no other example changes,
  and no example is added: which record gives which kind is the backend's
  rule, proven by its own tests.

The backend answers `BOOK` when no record says, so the field is never
absent.

## Done

On the Pixel, from the installed app on staging: scan *One Piece* 1 and
read *Eiichirō Oda* alone under *One piece · tome 1*; scan a BD with a
writer and an artist, *Astérix*, and read *album* and the two lines with
*scénario* and *dessin*; scan a novel and read its author without a role
word, and *tome* when it belongs to a series.

## Tasks

Filled by the planner.
