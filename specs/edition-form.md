# Edition form

**Why:** PRD §4.7 and §4.1. A reader who scans an ouvrage the house lacks
reads what the sources know as a form, corrects or completes it, a title
the BnF spells oddly, a series it leaves out, a summary it lacks, and adds
the ouvrage as they wrote it; an ISBN no source knows is no longer a dead
end. Now, because the sources' answer is stored as it comes and nothing
lets the family fix it before it lands on every copy's card.
**Status:** draft

## Scenarios

The form replaces the card of an ISBN the house lacks, on the lookup
screen, and nowhere else: an ISBN the house holds shows its card as
today, and editing an edition the house already holds is a later spec,
since one edit changes the card of every copy. The authors are shown, not
edited: editing them is a spec of its own. An ouvrage with no ISBN, a
summary from Open Library and a link to a bookshop are not in this spec.

**S1 A new ISBN opens the form** · frontend

```gherkin
Given an ISBN the house lacks and a source knows

When the reader asks for it

Then the form shows in place of the card
	And each field holds what Libris answered
	And the kind Libris answered is chosen
	And the covers on offer show as on the card
```

Proof: frontend scenario test over the fakes, the lookup answering
`ONE_PIECE_1`.

The frontend scenario tests of other specs that read the card of an ISBN
the house lacks move with this one: fast-entry S1 to S3, kind S1 and
summary S2 on the lookup ask for an ISBN the house holds, whose card is
unchanged; bookshelf S2 and S5, bookshelves S7 and covers S1 to S3 wait
for the button *Ajouter à ma bibliothèque*, which the card and the form
both show.

**S2 An unknown ISBN opens an empty form** · frontend

```gherkin
Given an ISBN no source knows

When the reader asks for it

Then the form shows with its ISBN and every field empty
	And the kind Livre is chosen
	And the cover is the stand-in
```

No message: the empty form says that nothing is known. The frontend half
of fast-entry S4, the message *ISBN inconnu*, retires with this
scenario; the backend still answers the not-found problem. Proof: frontend
scenario test over the fakes, the lookup answering the not-found problem.

**S3 Every source down shows no form** · frontend

```gherkin
Given every source failing or not answering in time

When the reader asks for an ISBN

Then the screen says that the sources are unavailable, to try again later
	And no form shows
```

As fast-entry S7: the reader tries again later and gets the form filled,
rather than typing what the BnF would have given. Proof: frontend scenario
test over the fakes, the lookup answering the sources-unavailable problem.

**S4 A held ISBN shows its card** · frontend

```gherkin
Given an edition the house holds

When the reader asks for its ISBN

Then its card shows, as bookshelf S4 says, and no form
```

Proof: frontend scenario test over the fakes, the lookup answering
`ONE_PIECE_2_OWNED`.

**S5 The ouvrage is added as edited** · frontend, backend

```gherkin
Given the form of an ISBN the house lacks

When the reader changes its fields and adds it

Then an edition exists with the fields the reader sent, not the sources'
	And a copy of it sits on the reader's default bookshelf
	And the card shows the fields sent and that copy on that bookshelf
```

The series is matched by name as PRD §3 says: a name the house holds,
whatever its capitalisation and its accents, lands on that series, any
other creates one. Each text field is sent without the spaces at its
ends, and a field of spaces alone is empty, sent as no value. Proof: backend
scenario test over HTTP, the BnF stubbed with the recorded record of the
ISBN, the add sending a title and a series the record does not carry, then
the lookup of the same ISBN answering the edition as the house holds it,
with the sent fields; a second case, the house holding *Astérix*, the add
sending *asterix* followed by a space as the series: the edition lands on
*Astérix*, and the house holds no second series; frontend scenario test
over the fakes, the add's body carrying the edited fields without the
spaces typed at their ends, and the card showing them.

**S6 The kind changes the words** · frontend

```gherkin
Given the form of a Livre

When the reader chooses the kind BD

Then the volume number's field reads Album
	And the authors' roles read in the words of a BD
```

And back to *Tome* for Livre and Manga. Proof: frontend component test of
the form.

**S7 The series of the house are suggested** · frontend, backend

```gherkin
Given series held by the house, on the reader's bookshelves and on others

When the reader types in the series' field

Then up to five series of the house whose name contains the text, ignoring
     case, accents and the spaces at its ends, show under the field
	And a tap on one puts its name in the field

When the field holds the name of a series of the house, ignoring case,
     accents and the spaces at its ends

Then the field says Existante

When it holds another name

Then it says Nouvelle
```

The names are the whole house's, as the series an edition lands on are:
a series on a bookshelf the reader does not belong to is suggested too.
What the reader types stays as typed until they tap a suggestion.
The suggestions show while the field has the focus and holds text; a tap
on one, or leaving the field, closes them. The suggestions are sorted
by name, ignoring case and accents, the API promising no order. When the
field takes the focus, it is brought to the top of what the keyboard
leaves visible, so that its suggestions show above the keyboard. Proof: the
series names verified by Contracteer on their shape on both sides;
backend scenario test over HTTP, series on two bookshelves, one of them
not the reader's, a name shared by two editions answered once; unit test
of the matching rule (contains, ignoring case, accents and the spaces at
the text's ends, sorted by name, five at most);
frontend scenario test over the fakes; the scroll on focus needs the
layout: checked by hand on the Pixel.

**S8 No suggestions when the names fail** · frontend

```gherkin
Given the series names unavailable

When the reader types in the series' field

Then no suggestion and no word show
	And the field takes the text as without them
```

Proof: frontend scenario test with the names request failing.

**S9 The language by its word** · frontend

```gherkin
Given the form of an ISBN whose language Libris answers

When it shows

Then the language's field holds the French word of the language

When the reader types in it

Then up to five words of a language that contain the text, ignoring case
     and accents, show under the field, as the series' suggestions do
	And the add sends the language's code
```

The words are the browser's French names of the ISO 639-1 languages,
*japonais* for `ja`; the card of an ouvrage says its language with the
same words, which leave the `fr` catalogue. Proof: unit test of the
language rule, word to code and code to word, ignoring case and accents;
frontend component test of the field; frontend component test of the card
fed a language code.

**S10 A refused form** · frontend

```gherkin
Given the form with a field the add refuses

When the reader adds it

Then each refused field takes the danger outline and its message under it
	And the first refused field is scrolled into view and focused
	And no request leaves
```

The fields are checked on the add's tap, and not before. Cases: a title
blank or empty, *Titre manquant*; a volume number, a year or a page count
that is not a whole number from 1, *Nombre invalide*; a volume number with
no series, *Tome sans série*, *Album sans série* for a BD; a language word
that names no language, *Langue inconnue*. Proof: unit test of each rule;
frontend scenario test over the fakes, one per case, its field outlined,
its message, the focus on it, no add sent; the scroll needs the layout:
checked by hand on the Pixel.

**S11 Libris refuses or fails the add** · frontend

```gherkin
Given the form

When the reader adds it and Libris refuses it or does not answer

Then the screen says to try again later, as bookshelf S5 does
	And the form stays as the reader left it, open to edits
```

Two cases: the add answers the validation problem, the add fails. Proof:
frontend scenario test over the fakes, one per case.

## Screen

Mockups: <https://claude.ai/artifact/Lemu6WMwey6CpSPkZE2MvC>, one artboard
per state below, exported as `specs/edition-form/<n>-<state>.jpg` in the
same order. No route of its own: the lookup screen of
`specs/fast-entry.md`, route `/isbn`; while the lookup runs, the card's
skeleton shows as today, whether a card or a form follows.

The form is one block shaped like a card (U06), under the ISBN field,
and the button *Ajouter à ma bibliothèque* under it as under the card.
While the form runs under it, the button stays docked above the tab bar,
on `bg`, a `border` hairline over it; at the end of the form it sits back
in its place, without the hairline; the keyboard covers it. Top to bottom
inside the block:

- Top part, side by side: the cover at the left, with its dots and its
  source's name as on the card; at its right the label *ISBN* and the
  ISBN-13 under it.
- The row *Dans aucune de vos bibliothèques*, as on the card.
- Then the fields, 14 apart, each with its label (U04) and no
  placeholder, so that an empty field reads as empty:
  - The kind, a segmented choice *Livre* · *BD* · *Manga*, with no label.
  - *Titre*.
  - *Sous-titre*.
  - *Série*; inside its box, at the right, once it holds text, the word
    *Existante* after a check in `accent`, or *Nouvelle* in `muted`, both
    in `body` semibold. While it has the focus and holds text, its
    suggestions in a block of their own 4 under the box, on `surface`, a
    1.5 `border` outline, radius 12: one row per name, 44 tall, in the
    `field` step, the whole row a button, a `border` hairline between two
    rows, inset 14 each side; the part of the name that matches the text
    in semibold, the rest regular.
  - *Tome*, or *Album* for a BD.
  - *Auteurs*, read only: the authors grouped as the card groups them, in
    the words of the chosen kind; absent when there is none.
  - *Collection*.
  - *Éditeur*.
  - *Année*.
  - *Langue*, and its suggestions under it as the series', with no word
    in its box.
  - *Pages*.
  - *Résumé*, a field of several lines in the `field` step, three lines
    tall when empty, growing with its text; it never folds, unlike the
    card's summary, and keeps the line breaks typed.

*Tome*, *Album*, *Année* and *Pages* take the numeric keyboard, full width
each. A new lookup replaces the form, its edits lost, as it replaces the
card. No field is marked as required: the checks of the add say what is
missing. A refused field keeps its `danger` outline while it has the
focus. While the add runs, the button alone changes (U04): the fields
look the same and accept nothing. The message of a failed add goes 8
under the button, docked with it.

`docs/DESIGN.md` changes with this spec: in U04, the segmented choice also
chooses a value in a form, in as many parts as values, without addresses;
the fields of a form shaped like a card have no placeholder;
a field of several lines, growing; a word inside a field's box at the
right; suggestions under a field, a block of up to five rows, the matched
part in semibold; a refused field keeps its `danger` outline while
focused; in U03, the button of a form stays docked above the tab bar
while the form runs under it; in U06, the language's word comes from the
browser, not the `fr` catalogue; a form shaped like a card, its fields 14
apart.

States:

- **Filled** (S1): *One Piece* 1, every field filled, Manga chosen,
  *Existante* in *Série*.
- **Empty** (S2): an unknown ISBN, Livre chosen, the stand-in, every
  field empty, no *Auteurs*.
- **BD** (S6): the form with BD chosen, *Album*, the roles in BD words.
- **Series suggested** (S7): *pie* typed in *Série*, *Nouvelle* in its
  box and the suggestion rows under it.
- **Language suggested** (S9): *jap* typed in *Langue*, its rows.
- **Refused** (S10): *Titre manquant* under an empty *Titre*, *Tome sans
  série* under *Tome*, the title focused.
- **Adding** (S5): the button reads *Ajout en cours…* with its spinner,
  and the form accepts nothing.
- **Added** (S5): the card of the fields sent, with *Dans Bibliothèque de
  Léa*, the form and the button gone. The card is the confirmation.
- **Not added** (S11): *Erreur lors de l'ajout, veuillez réessayer plus
  tard.* under the button, which is back, the form as left.

## Contract

Released `v0.15.0` of `camory/libris-api`, one read-only operation; the add
is unchanged, the form submitting the same `NewBook`, `language` its ISO
639-1 code:

- `GET /api/v1/series-names` → `200` an array of strings: the names of
  the series the house holds, each once and none blank, in no promised
  order; verified on its shape, no example.

## Done

On the Pixel, from the installed app on staging: scan a book the house
lacks and read its form, filled; change its title, type the first letters
of a series the house holds and tap its suggestion, read *Existante*;
add it, the card shows the new title and *Dans Bibliothèque de
Christophe*; scan it again, the card, with the new title. Type an ISBN no
source knows, read the empty form, add it with no title, read *Titre
manquant* on a focused field scrolled into view; type a volume number
alone, read *Tome sans série*. Type *jap* in *Langue* and tap *japonais*.
Type a summary of several lines and watch the field grow. Scroll the
form: the button stays docked above the tab bar, and the keyboard covers
it; tap *Série*: it comes to the top, its suggestions above the keyboard.

## Tasks

- T119 — S7, the backend on `v0.15.0` — backend
- T120 — S5 — backend
- T121 — S2, the frontend on `v0.15.0` — frontend
- T122 — S1, S9 the word of a code, S3 and S4 kept — frontend
- T123 — S5, S11, S9 the code of a word — frontend
- T124 — S10 field by field, S9 an unknown word — frontend
- T125 — S6, S10 a tome without a series — frontend
- T126 — S7 the suggestions, S8 — frontend
- T127 — S7 the word in the box, S9 the suggestions — frontend
