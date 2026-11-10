# Held edition edit

**Why:** PRD §4.1. A reader corrects an ouvrage the house already holds,
from its page or from the card of a scan that finds none of their copies:
a title stored as the BnF spelled it, a series or an author left out, a
name misspelled, and the card of every copy shows the correction. Now,
because the form corrects only an ouvrage the house lacks: an edition
stored wrong stays wrong on every card, even once its last copy is gone
and its ISBN scanned again.
**Status:** draft

## Scenarios

Any reader edits any edition the house holds, the edition being the
household's: an edition has no owner, and no change waits for anyone's
approval. The form is the add's, the form of `specs/edition-form.md` with
the authors of `specs/authors-edit.md`: the same fields, the authors
included, the ISBN and the stored cover shown and never edited. The pencil
*Modifier* opens it from two places: the card of the ouvrage's page, where
*Enregistrer* saves it, and the card of the lookup screen when it says
*Dans aucune de vos bibliothèques*, the house lacking the ISBN or holding
it with none of the reader's copies, where *Ajouter à ma bibliothèque*
adds it as edited. The card of an ISBN of which the reader holds a copy
carries no pencil: the ouvrage is edited from its page, and an edit there
never adds a second copy.

The last save wins, with no word of a save made in between; the edits not
saved are lost when the reader leaves the screen, with no question. The
form says nothing of what a save changes beyond the ouvrage: a name
respelled for the house (S5) is respelled with no warning.

Not in this spec: changing the cover; removing the names no edition
carries any more, a page of the administration (PRD §4.10) for later; a
change waiting for approval; two saves crossing, told apart; the history
of an edition's changes; an ouvrage with no ISBN.

**S1 A held ISBN with none of the reader's copies shows the pencil** · frontend

```gherkin
Given an edition the house holds, with no copy on a bookshelf the reader
      belongs to

When the reader asks for its ISBN

Then its card shows, with Dans aucune de vos bibliothèques
	And the card carries the pencil Modifier
```

The card of an edition of which the reader holds a copy shows that copy
on its bookshelf, and no pencil. Edition-form S4 is narrowed to that card,
and its test moves here. Proof: frontend scenario test over the fakes, two
cases: the lookup answering an edition the house holds with no copy, its
card carrying the pencil; the lookup answering `ONE_PIECE_2_OWNED`, its
card carrying none.

**S2 A held ISBN is added as edited** · frontend, backend

```gherkin
Given the card of an edition the house holds, with no copy on a bookshelf
      the reader belongs to

When the reader taps the pencil, changes its fields and adds it

Then the edition holds the fields the reader sent
	And a copy of it sits on the reader's default bookshelf
	And the card shows the fields sent and that copy on that bookshelf
```

The form opens as edition-form S12 says, each field holding what the
house holds, the authors as rows; the cover is the house's and stays it,
the add's cover source being ignored for an edition the house holds. The
add writes the fields sent over the house's: an add of an ISBN the house
holds no longer ignores them, and PRD §4.1 gains it, the edition created
when its ISBN is unknown, else written with the fields sent. An add from a
card with no form opened sends what the lookup answered and changes
nothing, unless a save came in between, the last write winning. The names
follow S5. The card that confirms the add shows the fields as sent, as
edition-form S5 says; a later lookup of the ISBN shows the house's
spelling. Proof: backend scenario test over HTTP, the house holding an
edition with a stored cover and copies on another reader's bookshelf
alone, the add of its ISBN sending another title: the lookup of the ISBN
answering the sent title and the stored cover as before; frontend scenario
test over the fakes, the lookup answering an edition the house holds with
no copy, the form holding its fields, the add's body carrying the edited
title, the card showing it and the copy on the default bookshelf.

**S3 The pencil opens the form on the ouvrage's page** · frontend

```gherkin
Given the ouvrage's page

When the reader taps the pencil of its card

Then the form shows in place of the card
	And each field holds what Libris answered, the authors as rows
	And the bookshelf rows show as on the card, without their trash
	And the pencil has become the cross Annuler les modifications
	And the button Enregistrer shows under the form
```

The card carries the pencil whatever the reader's role on the bookshelves
holding a copy: a guest edits as an owner does. The cover is the stored
one or the stand-in, as on the page's card, and the ISBN shows under its
label as on the lookup's form; neither is edited. The page's card has no
button: *Enregistrer* shows while the form does. Proof: frontend scenario
test over the fakes, the ouvrage answering an Astérix album by two
authors, on a bookshelf the reader owns: its fields, its two rows, its
bookshelf row without trash, the cross, *Enregistrer*.

**S4 The ouvrage is saved as edited** · frontend, backend

```gherkin
Given the form of the ouvrage's page

When the reader changes its fields and taps Enregistrer

Then Libris answers the edition with the fields the reader sent
	And the card shows the edition as Libris answered it, with its copies
	And the form and the button are gone
```

Each text field is sent without the spaces at its ends, a field of spaces
alone as no value, and the authors as authors-edit S5 and S9 say; the
ISBN and the cover are not sent. The card shows what Libris answered, not
the fields as sent: a series typed *tintin* that lands on *Tintin* (S5)
reads *Tintin* at once. Any reader saves any edition the house holds, one
with no copy on their bookshelves included, which Libris answers with no
copy. Proof: contract example `200_EDIT_ONE_PIECE_2` verified by
Contracteer on both sides; backend scenario test over HTTP, the save
sending another title, a subtitle, a series and an author, then the
ouvrage's page answering them; a second case, the edition's copies on
another reader's bookshelf alone: Libris answering the saved edition with
no copy, and the lookup of its ISBN the fields sent; frontend scenario
test over the fakes, the save's body carrying the edited fields without
the spaces typed at their ends, the fake answering *Tintin* to a series
sent *tintin*, the card reading *Tintin*.

**S5 Its own name is respelled for the house** · backend

```gherkin
Given an edition, its series and its authors, and other names of the house

When a reader saves it with one of its own names in other capitals,
     other accents or spaces at the ends

Then that name takes the spelling sent, for every edition carrying it
	And the house holds no second name of it

When a reader saves it with another name of the house so written

Then the edition carries that name under the house's spelling
	And that name keeps its spelling
```

Libris matches names as PRD §3 says, ignoring case, accents and the spaces
at their ends. Matching the edition's own name, its series or one of its
authors before the save, the name sent respells it for the house: every
edition of that series, every edition of that author, reads the spelling
sent. Any other name lands on the house's name it matches, kept, or
creates one, as an add does (edition-form S5, authors-edit S5). An
author's own name is matched whatever roles the save gives it. The add of
a held ISBN (S2) follows the same rule; the add of an ISBN the house
lacks, having no names of its own, never respells. No two series and no
two authors of the house match so, so a respelling never merges two
names. `docs/ARCHITECTURE.md` D12 gains this exception to "there is no
renaming", for the series and for the authors; authors-edit's "an
author's spelling changed for the whole house, which no add ever does" and
its S5's "never rewritten by an add" then hold of an ISBN the house lacks.
Proof: backend scenario test over HTTP, one per case: the house holding
two albums of *Astérix*, one saved with *asterix*: the lookup of the other
answering *asterix*, and the series' names *asterix* alone; the house
holding *Tintin* too, an album of *Astérix* saved with *tintin* followed
by a space: the album in *Tintin*, *Astérix* and *Tintin* spelled as
before; an album's own author *Hergé* saved as *herge*: another album of
his answering *herge*; an author added as *rene goscinny*, the house
holding *René Goscinny* on another edition alone: the edition answering
*René Goscinny*; the add of a held ISBN sending *asterix* as its series:
the series spelled *asterix*.

**S6 A name no edition carries is not suggested** · backend

```gherkin
Given an edition alone in its series, and an author on it alone

When a reader saves it in another series, without that author

Then the series' names no longer answer that series
	And the authors' names no longer answer that author
```

The house keeps the name: a later edition that names it lands on it,
under its spelling. A name typed that matches it reads *Nouvelle*, or
*Nouveau*, and lands on it all the same, the two differing only by their
capitals, accents and spaces. Proof: the series' and the authors' names
verified by Contracteer on their shape on both sides, as before; backend
scenario test over HTTP, an album alone in *Astérix*, by *René Goscinny*
alone among the house's editions, saved in *Tintin* without him: neither
name answered.

**S7 An edition Libris does not know is not saved** · backend

```gherkin
Given an id that names no edition of the house

When a reader saves an edition under it

Then Libris answers that there is no such edition
	And nothing is stored
```

Editions are never deleted, so only an address made by hand reaches it;
the screen reads it as a failed save (S9). An id that is not one is the
validation problem. Proof: contract example `404_UNKNOWN_BOOK` verified by
Contracteer on both sides, the id that is not one its generated case;
backend scenario test over HTTP, the save sending a series the house
lacks: the not-found problem, and the series' names without it.

**S8 The cross drops the edits** · frontend

```gherkin
Given the form of the ouvrage's page, its fields changed

When the reader taps the cross

Then the card shows again as Libris answered it, with its pencil
	And the button Enregistrer is gone
	And a tap on the pencil shows the form as Libris answered it
```

No confirmation, as edition-form S13. Proof: frontend scenario test over
the fakes, the title changed then dropped.

**S9 A refused or failed save** · frontend

```gherkin
Given the form of the ouvrage's page

When the reader taps Enregistrer and Libris refuses the save or does not
     answer

Then the screen says to try again later
	And the form stays as the reader left it, open to edits
```

The words are *Erreur lors de l'enregistrement, veuillez réessayer plus
tard.* Before any request, the form checks its fields and its authors on
the tap of *Enregistrer* as on the add's, and refuses as edition-form S10
and authors-edit S8 and S9 say. Proof: frontend scenario test over the
fakes, one per case: the save answering the validation problem, the save
answering the not-found problem, the save failing; and the title emptied:
*Titre manquant* under it, the field focused, no save sent.

## Screen

Mockups: <https://claude.ai/artifact/9RMhN9rnNpAUkvZv6eJmgc>, one artboard
per state below, exported as `specs/held-edition-edit/<n>-<state>.jpg` in
the same order. No route of
its own: the ouvrage's page of `specs/edition-page.md`, route
`/catalogue/ouvrages/{id}`, and the lookup screen of
`specs/edition-form.md`, route `/isbn`.

On the ouvrage's page, the card carries the pencil, the icon button
(U04) *Modifier* of the lookup's card, in the top right corner of its top
part. A tap shows the form card (U06) in its place, the pencil now the
cross *Annuler les modifications*, and the primary button *Enregistrer*
under it, docked above the tab bar while the form runs under it (U03).
Top to bottom inside the block:

- Top part, side by side: the stored cover or its stand-in, as on the
  page's card; at its right the label *ISBN* and the ISBN-13 under it; in
  the top right corner, the cross.
- One row per bookshelf of the reader holding a copy, as on the card,
  without the trash.
- The fields of the lookup's form, in its order, the kind first and the
  authors' rows between *Tome* (or *Album*) and *Collection*, filled with
  what Libris answered.

While the save runs, the button reads *Enregistrement en cours…* with its
spinner, and the form accepts nothing, its cross included (U04). The
message of a failed save goes 8 under the button, docked with it. A saved
edition shows its card again, as Libris answered it, with its pencil and
no button: the card is the confirmation.

On the lookup screen, the card of an edition the house holds with none of
the reader's copies carries the pencil, as the card of an ISBN the house
lacks does; its form is the lookup's form, with the house's cover as on
the card, the row *Dans aucune de vos bibliothèques* and the button
*Ajouter à ma bibliothèque*.

While the form shows on the ouvrage's page, the screen's `h1` stays the
ouvrage's title as Libris answered it, visually hidden: nothing moves when
the form opens, and the screen's name follows no keystroke. The document
title is unchanged.

`docs/DESIGN.md` changes with this spec: in U02, while a form shows in
place of an ouvrage's card, the screen's `h1` is the ouvrage's title as
answered, visually hidden; in U04, the primary button *Enregistrer* for
the form of an ouvrage the house holds, opened from its page, present
while the form is, *Enregistrement en cours…* while it runs; in U06, the
bookshelf rows of a form card carry no trash.

States:

- **Lookup card** (S1): *Le Lotus bleu*, an edition the house holds with
  none of the reader's copies, its stored cover without dots, *Dans
  aucune de vos bibliothèques*, the pencil, the button *Ajouter à ma
  bibliothèque*.
- **Lookup form** (S2): the same edition's form, the house's cover, the
  cross, the button *Ajouter à ma bibliothèque*.
- **Page card** (S3): *Astérix le Gaulois* on its page, stored with no
  series, the card with its pencil, the reader's bookshelf row with its
  trash, a guest's without.
- **Page form** (S3): its form, its bookshelf rows without trash, its two
  authors' rows, the cross, *Enregistrer* docked above the tab bar.
- **Saving** (S4): *asterix* typed in *Série*, *1* in *Album*;
  *Enregistrement en cours…* with its spinner, the form accepting nothing.
- **Saved** (S4, S5): the card as Libris answered it, the series typed
  *asterix* reading *Astérix*, the house's spelling, the pencil back, no
  button.
- **Not saved** (S9): *Erreur lors de l'enregistrement, veuillez
  réessayer plus tard.* under the button, the form as left.

## Contract

To release with Tophe: `v0.18.0` of `camory/libris-api`, one operation
added, two descriptions reworded, no shape changed but the split below:

- `PUT /api/v1/books/{id}` → request: `EditionDetails`, the fields of
  `Edition` but `isbn13`, and no `coverSource`, split from `Edition`,
  which composes over it adding `isbn13`; `200` `Book`, the edition as
  saved with its copies on the reader's bookshelves, none included; `400`
  `Problem` `/problems/validation`, the body refused or the id not one;
  `404` `Problem` `/problems/not-found` when no edition has that id. Keys
  `200_EDIT_ONE_PIECE_2`, `404_UNKNOWN_BOOK`.
- `POST /api/v1/bookshelves/{id}/books`: the book is created when its
  ISBN is unknown, else written with the fields sent; `coverSource` still
  ignored when the house holds the edition.
- `GET /api/v1/series-names`, `GET /api/v1/author-names`: the names some
  edition of the house carries.

## Done

On the Pixel, from the installed app on staging: in *Catalogue*, open an
ouvrage and tap the pencil; read its form filled, its authors as rows,
its bookshelf row without trash, *Enregistrer* docked above the tab bar.
Change its title, tap the cross, read the card as it was. Tap the pencil,
change its title, retype its series in other capitals, tap *Enregistrer*:
the card shows the new title and the series as typed; in *Séries*, the
series reads the new spelling. Open an ouvrage of another series, type the
name of a series of the house in small letters, save: the card reads it
under the house's spelling. Remove the last copy of an ouvrage from its
page and scan its ISBN: the card says *Dans aucune de vos bibliothèques*
and carries the pencil; tap it, change its title, add it: the card shows
the new title and *Dans Bibliothèque de Christophe*. Scan an ouvrage you
hold: its card has no pencil.

## Tasks

- T153 — S7, the backend on `v0.18.0` — backend
- T154 — S4, S5 another series and another author of the house — backend
- T155 — S6 — backend
- T156 — S5 its series — backend
- T157 — S5 its author — backend
- T158 — S2, S5 the add of a held ISBN — backend
- T159 — S1, S2, the frontend on `v0.18.0` — frontend
- T160 — S3 — frontend
- T161 — S8 — frontend
- T162 — S4 — frontend
- T163 — S9 — frontend
