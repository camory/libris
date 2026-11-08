# Authors edit

**Why:** PRD §4.1 and §4.7. A reader who adds an ouvrage the house lacks
corrects its authors in its form as they correct its other fields: they
add one the BnF leaves out, remove one it gets wrong, respell a name, give
each author the roles they had, and the authors the house already holds
are suggested and matched, whatever their capitals and accents. Now,
because the edition form shows the authors without editing them, and a
wrong author lands on the card of every copy.
**Status:** draft

## Scenarios

The authors are edited in the form of an ISBN the house lacks, the form of
`specs/edition-form.md`, and nowhere else; the card shows them as today.
Not in this spec: the authors of an edition the house holds, edited with
its other fields in a later spec; the colourist of a BnF record, which the
lookup does not read; a role beyond the four of PRD §3; a page or a search
of an author; an author's spelling changed for the whole house, which no
add ever does.

**S1 The authors open as rows** · frontend

```gherkin
Given the card of an ISBN the house lacks, whose sources name authors

When the reader taps the pencil

Then the form shows one row per author, in the order of the card
	And each row holds the author's name in its box
	And the author's roles pressed, in the words of the chosen kind
```

An author with several roles is one row with each of them pressed. Proof:
frontend scenario test over the fakes, one per case: the lookup answering
`ONE_PIECE_1`, one row, *Eiichirō Oda*, scénario and dessin pressed; the
lookup answering an Astérix album by René Goscinny, scénario, and Albert
Uderzo, dessin, two rows in that order.

The frontend scenario test of edition-form S12 moves with this one: it
reads *Eiichirō Oda* in the box of its row, no longer in the text of the
form.

**S2 An author is added** · frontend

```gherkin
Given the form of an ISBN no source knows, Livre chosen

When it shows

Then its authors show no row, and the button Ajouter un auteur

When the reader taps the button

Then a row shows, its box empty and focused
	And the role texte is pressed

When the reader types a name in it and adds the ouvrage

Then the add sends that name with the role WRITER
```

A row added to others comes after the last, and nothing moves: the rows
keep the order they opened in, whatever the reader changes in them, and
no row is moved by hand. A new row starts with the first role, texte for
a Livre, scénario for a BD or a Manga, the role PRD §3 gives an author
when none is known. Proof: frontend scenario test over the fakes, the
lookup answering the not-found problem; a second case, the Astérix album
of S1, the new row after *Albert Uderzo*, scénario pressed.

**S3 An author is removed** · frontend

```gherkin
Given the form of an ouvrage with two authors

When the reader taps the cross of one of them

Then its row goes, with no question
	And the focus is on the button Ajouter un auteur

When the reader adds the ouvrage

Then the add sends the other author alone
```

No question: nothing is stored until the add, and on a form opened from a
card the cross Annuler les modifications brings every author back. Proof:
frontend scenario test over
the fakes, the Astérix album of S1, *Albert Uderzo* removed.

**S4 A name and its roles are changed** · frontend

```gherkin
Given the form of an ouvrage with an author

When the reader changes the name in its box
	And presses a role and releases another

Then the add sends the name as typed, with the roles pressed
```

Proof: frontend scenario test over the fakes, the lookup answering
`ONE_PIECE_1`, *Eiichirō Oda* respelled *Eiichiro Oda*, dessin released
and traduction pressed: the add's body carrying *Eiichiro Oda* as
`WRITER` and as `TRANSLATOR`, and nothing else.

**S5 An author of the house is matched by name** · frontend, backend

```gherkin
Given an author of the house

When the reader adds an ouvrage naming them with other capitals, other
     accents or spaces at the ends

Then the edition is linked to that author, under the house's spelling
	And the house holds no second author of that name
```

As the series of edition-form S5: Libris matches an author's name ignoring
case, accents and the spaces at its ends, and the house's spelling stays,
never rewritten by an add. The card that confirms the add shows the names
as sent, as it shows the series; a later lookup of the ISBN shows the
house's spelling. Each name is sent without the spaces at its ends. Two
authors of one add whose names match so, with one role, make one link:
the form refuses them (S8), and Libris keeps one should they reach it. On
2026-11-08 no two authors of the house matched so, on staging nor in
production, so the new match merges none. Proof: backend scenario test
over HTTP, the BnF stubbed, the house holding an album by *Hergé*, the
add of another sending *herge* followed by a space as its writer: the
lookup of its ISBN answering *Hergé*, and the authors' names answering
*Hergé* alone; unit test of the rule that keeps one contribution per
author and role, two names that match so kept once; frontend scenario
test over the fakes, the add's body carrying the names without the spaces
typed at their ends.

**S6 The authors of the house are suggested** · frontend, backend

```gherkin
Given authors held by the house, on the reader's bookshelves and on others

When the reader types in an author's box

Then up to five authors of the house whose name contains the text,
     ignoring case, accents and the spaces at its ends, show under the box
	And a tap on one puts its name in the box

When the box holds the name of an author of the house, ignoring case,
     accents and the spaces at its ends

Then the box says Existant

When it holds another name

Then it says Nouveau
```

Point for point as the series of edition-form S7. The names are the whole
house's, as the authors an edition is linked to are: an author on a
bookshelf the reader does not belong to is suggested too. What the reader
types stays as typed until they tap a suggestion. The suggestions show
while the box has the focus and holds text; a tap on one, or leaving the
box, closes them. They are sorted by name, ignoring case and accents, the
API promising no order. When a box takes the focus, a new row's included,
it is brought to the top of what the keyboard leaves visible, so that its
suggestions show above the keyboard. Proof: the authors' names verified by
Contracteer on their shape on both sides; backend scenario test over
HTTP, authors on two bookshelves, one of them not the reader's, a name
shared by two editions answered once; frontend scenario test over the
fakes, the series' matching rule and its unit test serving unchanged; the
scroll on focus needs the layout: checked by hand on the Pixel.

**S7 No suggestions when the names fail** · frontend

```gherkin
Given the authors' names unavailable

When the reader types in an author's box

Then no suggestion and no word show
	And the box takes the text as without them
```

Proof: frontend scenario test with the names request failing.

**S8 A refused author** · frontend

```gherkin
Given the form with an author the add refuses

When the reader adds it

Then each refused part takes the danger outline and its message under it
	And the first refused, in the order of the form, is scrolled into view
	    and focused
	And no request leaves
```

The authors are checked on the add's tap, with the fields of edition-form
S10, and not before; the first refused of all takes the focus. Cases: a
row with a name and no role pressed, *Rôle manquant* under its roles, each
of them outlined, the first focused; two rows whose names match, ignoring
case, accents and the spaces at their ends, whatever their roles, *Auteur
en double* under the box of the second, outlined and focused. A row with
an empty box is never refused (S9). Proof: unit test of each rule;
frontend scenario test over the fakes, one per case, its message, the
focus on it, no add sent; the scroll needs the layout: checked by hand on
the Pixel.

**S9 An empty row is dropped** · frontend

```gherkin
Given the form with a row whose box is empty or holds spaces alone

When the reader adds the ouvrage

Then the add sends no author for that row, whatever its roles
	And no message shows
```

Proof: frontend scenario test over the fakes, the add's body.

## Screen

Mockups: to draw with Tophe, one artboard per state below, exported as
`specs/authors-edit/<n>-<state>.jpg` in the same order. No route of its
own: the form of `specs/edition-form.md`, on the lookup screen, route
`/isbn`.

The read-only *Auteurs* of the form gives way to its authors, in the same
place, between *Tome* (or *Album*) and *Collection*, shown whatever the
ouvrage, even with no author. The card is unchanged: its authors' line is
absent when there is none. Top to bottom:

- The label *Auteurs* (U04), once for every row.
- One row per author, 14 apart:
  - The name box, a field (U04) with no label of its own, `aria-label`
    *Nom de l'auteur*; inside it at the right, once it holds text, the
    word *Existant* after a check in `accent`, or *Nouveau* in `muted`, as
    *Série*'s; while it has the focus and holds text, its suggestions
    under it, as *Série*'s.
  - At the box's right, on its line, 8 apart, the cross, an icon button
    (U04), `aria-label` *Retirer* followed by the name in the box,
    *Retirer l'auteur* while the box is empty.
  - 8 under the box, the four roles in a group named *Rôles*, in the order
    of the card, each a toggle, pressed or not, several pressed at once:
    scénario, dessin, couleurs, traduction for a BD or a Manga; texte,
    illustration, couleurs, traduction for a Livre. The four show
    whatever the kind; a change of kind changes their words and leaves
    them pressed as they were, as edition-form S6 says. Whether they form
    one block divided as the segmented choice or four pills apart is
    chosen on the mockups.
- 8 under the last row, or under the label when there is none, the text
  button *Ajouter un auteur* (U04), its word on the boxes' left edge.

*Rôle manquant* goes 8 under the roles of its row, *Auteur en double* 8
under the box of its row, each a message (U05). While the add runs, the
boxes, the roles, the crosses and *Ajouter un auteur* accept nothing, as
the fields do.

`docs/DESIGN.md` changes with this spec: in U04, a field of a form may go
without a label of its own when one label names several of them, its
`aria-label` naming it; the word in the box is *Existant* or *Nouveau* for
an author; a toggle, a button pressed or not, `aria-pressed`, at least 44
tall, several in a group named after what they choose, its look chosen on
the mockups; a refused group of toggles takes the `danger` outline on
each, its message under it; the text button also lives under the rows of
a form, *Ajouter un auteur*; a row of a form goes by its cross, an icon
button on its line, with no question, nothing being stored before the
form is sent, and the focus then goes to the text button that adds a row.

States:

- **Filled** (S1): *One Piece* 1, Manga chosen, one row *Eiichirō Oda*,
  *Existant* in its box, scénario and dessin pressed.
- **Two authors** (S1): an Astérix album, BD chosen, *René Goscinny* with
  scénario pressed, then *Albert Uderzo* with dessin pressed.
- **Empty** (S2): an unknown ISBN, Livre chosen, the label *Auteurs* and
  the button *Ajouter un auteur*, no row.
- **Author suggested** (S6): *gosc* typed in a new row's box, *Nouveau*
  in it, the suggestion *René Goscinny* under it, *Gosc* in semibold.
- **Refused** (S8): *Rôle manquant* under the roles of a row, its roles
  outlined, the first focused; *Auteur en double* under the box of a
  second *René Goscinny*.

## Contract

To release with Tophe: `v0.17.0` of `camory/libris-api`, one read-only
operation; the add is unchanged, the form submitting the same `NewBook`,
one `Author` per name and role:

- `GET /api/v1/author-names` → `200` an array of strings: the names of
  the authors the house holds, each once and none blank, in no promised
  order; verified on its shape, no example.

## Done

On the Pixel, from the installed app on staging: scan an Astérix album the
house lacks and tap the pencil; read two rows, *René Goscinny* with
scénario pressed, then *Albert Uderzo* with dessin pressed. Tap *Ajouter
un auteur*: a row at the end, scénario pressed, its box focused and
brought to the top; type *gosc*, read *Nouveau* and the suggestion *René
Goscinny* above the keyboard; tap it, read *Existant*. Tap *Ajouter à ma
bibliothèque*, read *Auteur en double* under that row's name, focused and
scrolled into view; type a name of your own instead, release scénario,
add, read *Rôle manquant* under its roles; press couleurs. Respell the
first row *rene goscinny*. Tap *Ajouter un auteur* twice, remove one of
the two new rows with its cross, no question asked, and leave the other
empty. Add: the card shows *rene goscinny*, your colourist, *Albert
Uderzo* and *Dans Bibliothèque de Christophe*, and no empty author. Scan
it again: the card says *René Goscinny*, the house's spelling.

## Tasks

Left for the planner.
