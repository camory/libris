# Authors edit

**Why:** PRD §4.1 and §4.7. A reader who adds an ouvrage the house lacks
corrects its authors in its form as they correct its other fields: they
add one the BnF leaves out, remove one it gets wrong, respell a name, give
each author the roles they had, and the authors the house already holds
are suggested and matched, whatever their capitals and accents. Now,
because the edition form shows the authors without editing them, and a
wrong author lands on the card of every copy.
**Status:** done 2026-11-09

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
	And each row holds the author's name
	And under it the author's roles, in the words of the chosen kind
```

An author with several roles is one row, its roles together under its
name. Proof: frontend scenario test over the fakes, one per case: the
lookup answering `ONE_PIECE_1`, one row, *Eiichirō Oda*, scénario and
dessin chosen; the lookup answering an Astérix album by René Goscinny,
scénario, and Albert Uderzo, dessin, two rows in that order.

The frontend scenario test of edition-form S12 moves with this one: it
reads *Eiichirō Oda* as the name of its row, no longer in the text of the
form.

**S2 An author is added** · frontend

```gherkin
Given the form of an ISBN no source knows, Livre chosen

When it shows

Then its authors show no row, and the button Ajouter un auteur

When the reader taps the button

Then a row shows, its name empty and focused
	And the role texte chosen

When the reader types a name in it and adds the ouvrage

Then the add sends that name with the role WRITER
```

A row added to others comes after the last, and nothing moves: the rows
keep the order they opened in, whatever the reader changes in them, and
no row is moved by hand. A new row starts with the first role, texte for
a Livre, scénario for a BD or a Manga, the role PRD §3 gives an author
when none is known. Proof: frontend scenario test over the fakes, the
lookup answering the not-found problem; a second case, the Astérix album
of S1, the new row after *Albert Uderzo*, scénario chosen.

**S3 An author is removed** · frontend

```gherkin
Given the form of an ouvrage with two authors

When the reader taps the trash of one of them

Then its row goes, with no question
	And the focus is on the button Ajouter un auteur

When the reader adds the ouvrage

Then the add sends the other author alone
```

No question: nothing is stored until the add, and on a form opened from a
card the cross Annuler les modifications brings every author back. The
same trash asks first on a bookshelf row, where the copy is stored. Proof:
frontend scenario test over
the fakes, the Astérix album of S1, *Albert Uderzo* removed.

**S4 A name and its roles are changed** · frontend

```gherkin
Given the form of an ouvrage with an author

When the reader changes its name
	And, in the list of its roles, chooses one and releases another

Then the add sends the name as typed, with the roles chosen
```

Proof: frontend scenario test over the fakes, the lookup answering
`ONE_PIECE_1`, *Eiichirō Oda* respelled *Eiichiro Oda*, dessin released
and traduction chosen: the add's body carrying *Eiichiro Oda* as
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

When the reader types an author's name

Then up to five authors of the house whose name contains the text,
     ignoring case, accents and the spaces at its ends, show under its row
	And a tap on one puts it in place of the text

When the name is that of an author of the house, ignoring case, accents
     and the spaces at its ends

Then the word Existant shows beside it

When it is another name

Then the word is Nouveau
```

Point for point as the series of edition-form S7. The names are the whole
house's, as the authors an edition is linked to are: an author on a
bookshelf the reader does not belong to is suggested too. What the reader
types stays as typed until they tap a suggestion. The suggestions show
while the name has the focus and holds text; a tap on one, or leaving the
name, closes them. They are sorted by name, ignoring case and accents, the
API promising no order. When a name takes the focus, a new row's
included, its row is brought to the top of what the keyboard leaves
visible, so that its suggestions show above the keyboard. Proof: the authors' names verified by
Contracteer on their shape on both sides; backend scenario test over
HTTP, authors on two bookshelves, one of them not the reader's, a name
shared by two editions answered once; frontend scenario test over the
fakes, the series' matching rule and its unit test serving unchanged; the
scroll on focus needs the layout: checked by hand on the Pixel.

**S7 No suggestions when the names fail** · frontend

```gherkin
Given the authors' names unavailable

When the reader types an author's name

Then no suggestion and no word show
	And the name takes the text as without them
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
S10, and not before; the first refused of all takes the focus. A refused
row takes the danger outline on the whole of it. Cases: a row with a name
and no role chosen, *Rôle manquant* under it, its roles focused; two rows
whose names match, ignoring case, accents and the spaces at their ends,
whatever their roles, *Auteur en double* under the second, its name
focused. A row with an empty name is never refused (S9). Proof: unit test of each rule;
frontend scenario test over the fakes, one per case, its message, the
focus on it, no add sent; the scroll needs the layout: checked by hand on
the Pixel.

**S9 An empty row is dropped** · frontend

```gherkin
Given the form with a row whose name is empty or holds spaces alone

When the reader adds the ouvrage

Then the add sends no author for that row, whatever its roles
	And no message shows
```

Proof: frontend scenario test over the fakes, the add's body.

## Screen

Mockups: <https://claude.ai/artifact/DhXMpjauFsxeRWHzpqp6Hv>, one artboard
per state below, exported as `specs/authors-edit/<n>-<state>.jpg` in the
same order. No route of its own: the form of `specs/edition-form.md`, on
the lookup screen, route `/isbn`.

The read-only *Auteurs* of the form gives way to its authors, in the same
place, between *Tome* (or *Album*) and *Collection*, shown whatever the
ouvrage, even with no author. The card is unchanged: its authors' line is
absent when there is none. Top to bottom:

- The label *Auteurs* (U04), once for every row.
- One row per author, 14 apart, each one box the full width of the form:
  `surface`, a 1.5 `border` outline, radius 12, taking the `accent`
  outline while its name has the focus or its roles' list is open.
  - Its first line, 48 tall, 14 of left padding and 3 of right: the name,
    typed as in a field (U04), in the field step, with no label of its
    own, `aria-label` *Nom de l'auteur*; at its right, once it holds text,
    the word *Existant* after a check in `accent`, or *Nouveau* in
    `muted`, as *Série*'s; 8 after the word, the trash, an icon button
    (U04), Lucide `Trash2`, `aria-label` *Retirer* followed by the name,
    *Retirer l'auteur* while the name is empty.
  - Under a `border` hairline inset 14, its second line, the roles, a
    button at least 44 tall, `aria-expanded`: the roles chosen in body, in
    the words and the order of the card, *scénario, dessin*, empty when
    none is chosen, its `aria-label` *Rôles* followed by them, *Rôles
    scénario, dessin*, *Rôles* alone when none is chosen; at its right the
    chevron in `muted`, under the trash, turned up while the list is open.
    Four roles take a second line, the box growing with it.
  - While the name has the focus and holds text, its suggestions 4 under
    the box, as *Série*'s.
  - A tap on the roles line opens their list 4 under the box, a list of
    several choices (U04): one row per role, in the order of the card,
    scénario, dessin, couleurs, traduction for a BD or a Manga; texte,
    illustration, couleurs, traduction for a Livre; the rows a group named
    *Rôles*, each a toggle. The four show whatever the kind; a change of
    kind changes their words and keeps them chosen, as edition-form S6
    says.
- 8 under the last row, or under the label when there is none, the text
  button *Ajouter un auteur* (U04), its word on the boxes' left edge.

*Rôle manquant* and *Auteur en double* go 8 under the box of their row,
each a message (U05), the box keeping the `danger` outline even with the
focus. While the add runs, the names, the roles, the trashes and *Ajouter
un auteur* accept nothing, as the fields do.

`docs/DESIGN.md` changes with this spec. In U01 and U04: a field takes the
`accent` outline while it has the focus, every field of the app, the
form's other fields too. In U06: the fields of a form card come each with
its label or under one that names several. In U07: `ChevronDown`, at 22,
as the check of a list of several choices. In U04 too: a field of a form may go
without a label of its own when one label names several of them, its
`aria-label` naming it; the word in the box is *Existant* or *Nouveau* for
an author; one box may hold the lines of one thing, an author's name and
roles, a hairline between them, a refused one taking the `danger` outline
whole, its message under it; a list of several choices, a line of a box
that reads the values chosen with a chevron at its right and opens under
the box a block of rows shaped as the suggestions, each a toggle,
`aria-pressed`, the check in `accent` at its right when chosen, staying
open while the reader chooses, closed by a tap on its line or anywhere
else; the text button also lives under the rows of a form, *Ajouter un
auteur*; the trash removes a row from where it is, and asks first only
when what it removes is stored: a row of a form not yet sent goes at
once, the focus then going to the text button that adds a row.

States:

- **Filled** (S1): *One Piece* 1, Manga chosen, one row *Eiichirō Oda*,
  *Existant* beside the name, *scénario, dessin* under it.
- **Two authors** (S1): an Astérix album, BD chosen, *René Goscinny* on
  scénario, then *Albert Uderzo* on dessin.
- **Empty** (S2): an unknown ISBN, Livre chosen, the label *Auteurs* and
  the button *Ajouter un auteur*, no row.
- **Author suggested** (S6): an unknown ISBN, Livre chosen, *gosc* typed
  as a new row's name, *Nouveau* beside it, *texte* under it, the
  suggestion *René Goscinny* under the row, *Gosc* in semibold.
- **Refused** (S8): the Astérix album, *Rôle manquant* under *Albert
  Uderzo*, no role chosen, its roles focused; *Auteur en double* under a
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
house lacks and tap the pencil; read two rows, *René Goscinny* on
scénario, then *Albert Uderzo* on dessin. Tap *Ajouter un auteur*: a row
at the end, scénario under its name, the name focused and brought to the
top; type *gosc*, read *Nouveau* and the suggestion *René Goscinny* above
the keyboard; tap it, read *Existant*. Tap *Ajouter à ma bibliothèque*,
read *Auteur en double* under that row, its name focused and scrolled
into view; type a name of your own instead, open its roles, release
scénario, add, read *Rôle manquant* under the row; choose couleurs.
Respell the first row *rene goscinny*. Tap *Ajouter un auteur* twice,
remove one of the two new rows with its trash, no question asked, and
leave the other empty. Add: the card shows *rene goscinny*, your
colourist, *Albert Uderzo* and *Dans Bibliothèque de Christophe*, and no
empty author. Scan it again: the card says *René Goscinny*, the house's
spelling.
Checked by Tophe on staging, recorded on 2026-11-09, on *Astérix chez les
Helvètes*, whose BnF record gives Albert Uderzo the function code of an
author: the form opened him on scénario.

## Tasks

- T142 — S6, the backend on `v0.17.0` — backend
- T143 — S5 — backend
- T144 — S1, the frontend on `v0.17.0` — frontend
- T145 — S2 — frontend
- T146 — S3 — frontend
- T147 — S4 — frontend
- T148 — S5, S9 — frontend
- T149 — S6 the suggestions, S7 — frontend
- T150 — S6 the word — frontend
- T151 — S8 Rôle manquant — frontend
- T152 — S8 Auteur en double — frontend
