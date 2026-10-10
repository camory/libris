# Bookshelves

**Why:** PRD §4.3 and §3. A reader creates a bookshelf, adds the readers
Libris knows to it as owner or viewer, and chooses which bookshelf they own
takes their one-tap add: two readers of the household fill one bookshelf
together. Now, because each reader has only the bookshelf created with them,
and the household wants one bookshelf for two.
**Status:** draft

## Scenarios

**S1 The reader's bookshelves** · frontend, backend

```gherkin
Given a reader who owns two bookshelves, one of them their default
	And is a viewer of a third
	And another bookshelf they do not belong to

When the reader opens the home page

Then Libris answers the three bookshelves they belong to, each with its
     name and the reader's role on it, and not the fourth
	And the default first, then the others by name, ignoring case and
	    accents
	And the screen lists them in that order, the default marked
```

Proof: the shape of the answer verified by Contracteer on both sides;
backend scenario test over HTTP; unit test of the order; frontend scenario
test over the fakes.

**S2 A reader creates a bookshelf** · frontend, backend

```gherkin
Given a reader

When they create a bookshelf named Bibliothèque familiale

Then the bookshelf exists, with the reader as its one owner
	And it is not their default
	And the screen lists it among their bookshelves
```

Proof: the creation verified by Contracteer on both sides, its `201` case
generated; backend scenario test over HTTP, the reader's bookshelves then
answering it; frontend scenario test over the fakes.

**S3 A name Libris refuses** · frontend, backend

```gherkin
Given a reader

When they create a bookshelf with a name Libris refuses

Then Libris answers that the name is refused, and why
	And no bookshelf is created
	And the screen says why under the name, which keeps what was typed
```

Two cases: a blank name, which the screen never sends, its check absent
until a name is typed; a name taken by a bookshelf the reader belongs to, whatever
their role on it, compared ignoring case and accents. Proof: contract
examples `400_BLANK_NAME` and `400_TAKEN_NAME` verified by Contracteer on
both sides; backend scenario test over HTTP, one per case; frontend
scenario test over the fakes, the taken name.

**S4 A member opens a bookshelf** · frontend, backend

```gherkin
Given a bookshelf with two owners and a viewer

When one of its members opens it

Then Libris answers its name and its members, each with their role
	And the owners first, then the viewers, each by display name, ignoring
	    case and accents
	And the screen shows them in that order
```

A reader who does not belong to the bookshelf is answered that there is no
such bookshelf, as for an id that names none, and the screen says so.
Proof: the shape of the answer and contract example `404_NOT_A_MEMBER`
verified by Contracteer on both sides; backend scenario test over HTTP, one
per case; unit test of the order; frontend scenario test over the fakes,
one per case.

**S5 An owner adds a member** · frontend, backend

```gherkin
Given a bookshelf holding copies, and a reader Libris knows who does not
      belong to it

When an owner of the bookshelf adds that reader with a role

Then the reader is a member of the bookshelf with that role
	And the bookshelf is among their bookshelves
	And its copies are in their catalogue and on the card of their ISBN
	And the screen shows the new member among the members
```

Two cases: added as owner, added as viewer. The screen offers only the
readers Libris knows who are not members yet, by display name, ignoring
case and accents. Proof: the readers' list and the add verified by
Contracteer on both sides, the add's `201` case generated; backend scenario
test over HTTP, one per case, the added reader's bookshelves and catalogue
answering the bookshelf and its copies; frontend scenario test over the
fakes.

**S6 An add Libris refuses** · backend

```gherkin
Given a bookshelf

When a reader adds a member to it and Libris refuses

Then the bookshelf's members stay as they were
```

Three cases: a viewer of the bookshelf adds a reader, answered that there is
no such bookshelf; a reader who does not belong to it does the same, with
the same answer; an owner adds a reader who is already a member, answered
that the reader is refused. Proof: contract example `404_NOT_MY_BOOKSHELF`
verified by Contracteer on both sides; backend scenario test over HTTP, one
per case.

**S7 A reader chooses their default** · frontend, backend

```gherkin
Given a reader who owns a bookshelf other than their default

When they make it their default

Then Libris answers it as their default from then on
	And the home page marks it as the default
	And the reader's next add from the card puts the copy on it
```

The reader makes the change on the home page, from the star of the
bookshelf's row. Proof: the change verified by Contracteer on both
sides, its `204` case generated; backend scenario test over HTTP, the
reader's answer then naming it; frontend scenario test over the fakes,
ending with an add from the card.

**S8 A bookshelf the reader cannot make their default** · frontend, backend

```gherkin
Given a reader and a bookshelf they do not own

When they make it their default

Then Libris answers that there is no such bookshelf
	And their default stays as it was
```

Two cases: a bookshelf they are a viewer of, one they do not belong to. The
home page offers neither: a viewer's row has no star, and the default's
star is a mark, not a button. Proof: contract example
`404_NOT_MY_BOOKSHELF` verified by Contracteer on both sides; backend
scenario test over HTTP, one per case; frontend scenario test over the
fakes, the viewer's row without a star and the default's star not a
button.

**S9 Libris unavailable** · frontend

```gherkin
Given a reader on the home page or on a bookshelf

When they ask Libris something and Libris does not answer

Then the screen says to try again later
	And nothing changes
```

Five cases: the home page's bookshelves; opening a bookshelf; creating a
bookshelf; adding a member; choosing the default. Proof: frontend scenario
test with the API failing, one per case.

## Screen

Mockups: <https://claude.ai/artifact/DgkPZBCjVEXQeU31rmcsC1>, one artboard
per state below, exported as `specs/bookshelves/<n>-<state>.jpg` in the
same order, 1 to 11 the home page, 12 to 21 a bookshelf. The rules of
`docs/DESIGN.md` hold; what they lack is proposed as an amendment with the
spec: in U04, the form in a list, a block's form being the last row of
its list, its fields with no border and no label, a placeholder in
`muted` naming each, a choice among many a native `select` with a
chevron down after its text, at the row's right a check, an icon button
in `accent` drawn at stroke 3, absent until the form can be sent, its
place kept, a spinner in its place while it runs, a message under the
fields inside the row; in U03, the block label, the `label` step naming a
block of the content as it names a field, with at its right one icon
button, a plus in a circle that opens the block's form as the last row of
its list and is gone while the form is there, the form leaving only on its success; in U03 too,
a row that leads to a screen holding one icon button left of its chevron; in U03 again, a header with
no hint, the bookshelf's until its screen shows its ouvrages; in U07,
Lucide as the source of new icons, at stroke 1.8, today's hand-drawn
ones staying until a follow-up moves them, and from it `IconCirclePlus`
(`circle-plus`), `IconCheck` (`check`), `IconChevronRight`
(`chevron-right`), `IconChevronDown` (`chevron-down`), `IconStar`
(`star`) outlined and filled, `IconCrown` (`crown`) outlined and filled,
the check of a form in a list at stroke 3; in U08, the application's name,
*Libris*, as the title of the home page, and the bookshelf's name as
the title of its screen, a proper name in place of an infinitive.

**Home page**, route `/`, the *Accueil* tab. Top to bottom on a phone: the
title *Libris*, the application's name, then the greeting, as today; the link
*Ajouter un ouvrage* leaves the page, the tab bar's *Ajouter* being the way
to the lookup; then the block *Mes bibliothèques*: one row per bookshelf of
the reader, in the order of the answer, the name in the row title step and
under it, in `muted`, the reader's role on it, *propriétaire* or *invité*,
followed by *· par défaut* on the default, a chevron at the right; a row
leads to the bookshelf's screen. On the row of a bookshelf the reader
owns, left of the chevron, a star in `accent`: filled on the default, a
mark and not a button; outlined on the others, an icon button,
*Choisir comme bibliothèque par défaut*, a tap making it the default. A
viewer's row has no star. At the right of the block's label, a plus,
*Nouvelle bibliothèque*: a tap adds a last row to the list, the field
*Nom de la bibliothèque*, that name its placeholder, and at its right,
aligned with the chevrons, the check *Créer*, absent while the name is
blank; while the row is there the plus is gone. Then the revision, as
today.

**Bookshelf**, route `/bibliotheques/{id}`, reached from a row of the home
page, the *Accueil* tab shown. Top to bottom: the bookshelf's name as the
title, no hint under it; the block *Membres*: one row
per member, in the order of the answer, the display name and after a
middle dot, in `muted`, the role (*Léa · propriétaire*), the reader's own
row reading *Vous* in place of their name (*Vous · invité*): the screen
says the reader's role there and nowhere else. For an owner, at the
right of the block's label, a plus, *Ajouter un membre*: a tap adds a last
row to the members, one line: the choice *Lecteur*, placeholder *Choisir
un lecteur*, among the readers who are not members; then a crown in
`accent`, a toggle button *Propriétaire*, outlined and off at first,
filled when on; then the check *Ajouter*, absent until a reader is
chosen. The reader is added as *invité*, or as *propriétaire* with the
crown on; changing a member's role afterwards is not in this feature.
While the row is there the plus is gone.
The readers who are not members are asked with the bookshelf, for an
owner.

States:

- **Loading** (S1, S4): skeleton rows where the rows of the list will land,
  on either screen, under the block's label, and no plus until the rows
  are there; on the bookshelf's screen a skeleton bar where its name will
  be.
- **Listed** (S1): the home page's rows, the default first and marked.
- **New bookshelf** (S2): the field as the list's last row, empty, no
  check, no plus.
- **Creating** (S2): *Créer* is busy, *Création en cours…*; on the answer
  the new row is in the list, the field's row is gone and the plus is back. No message.
- **Name taken** (S3): *Vous avez déjà une bibliothèque de ce nom.* under
  the field, in its row, the field keeping what was typed.
- **Members** (S4): the bookshelf's screen, an owner's with the plus at
  the right of *Membres*, a viewer's with the members only.
- **Not found** (S4): no title, and *Bibliothèque introuvable.* as the
  screen's one message.
- **New member** (S5): the row as the members' last, no reader chosen,
  the crown off, no check, no plus.
- **Adding** (S5): *Ajouter* is busy, *Ajout en cours…*; on the answer the
  new member is among the members, the row is gone and the plus is
  back. No message.
- **Every reader a member** (S5): no plus at the right of *Membres*, and
  *Tous les lecteurs sont déjà membres.* in `muted` under the members.
- **Changing the default** (S7): a spinner in `accent` in place of the
  tapped star, its label *Changement en cours…*, and the stars accept
  nothing; on the answer its star is
  filled, the former default's outlined, *· par défaut* moves with it, and
  the row goes first, in the order of S1. No message.
- **Unavailable** (S9): *Erreur lors du chargement, veuillez réessayer plus
  tard.* in place of the list, with no plus, or of the bookshelf's blocks; *Erreur lors de
  la création, veuillez réessayer plus tard.* under the field, in its row;
  *Erreur lors de l'ajout, veuillez réessayer plus tard.* under the
  member's line, in its row, the reader and the crown kept; *Erreur lors
  du changement, veuillez réessayer plus tard.* in the tapped row, under
  its role, the stars as they were.

## Contract

Release `v0.11.0` of `camory/libris-api`, after `v0.10.0`. Nothing changes
in an existing answer: five operations and their schemas are added.
`MembershipRole` is an enumeration, `OWNER` and `VIEWER`, since the screen
switches on it; `Bookshelf` is the existing `{id, name}`.

- `GET /api/v1/bookshelves`, the bookshelves the reader belongs to → `200`
  an array of `Membership` `{bookshelf: Bookshelf, role: MembershipRole}`,
  in the order of S1.
- `POST /api/v1/bookshelves`, body `NewBookshelf` `{name}` → `201`
  `Bookshelf`, the reader its one owner; `400` `Problem`
  `/problems/validation` on `name`, its codes `blank` and `taken`
  enumerated, the screen telling the two apart. Keys `400_BLANK_NAME`,
  `400_TAKEN_NAME`.
- `GET /api/v1/bookshelves/{id}` → `200` `BookshelfWithMembers`, a
  `Bookshelf` and `members`, an array of `Member` `{reader: Reader, role:
  MembershipRole}` in the order of S4, `Reader` being `{id, displayName}`;
  `404` `Problem` `/problems/not-found` when the reader is not a member.
  Key `404_NOT_A_MEMBER`.
- `GET /api/v1/readers`, the readers Libris knows → `200` an array of
  `Reader`, by display name.
- `POST /api/v1/bookshelves/{id}/members`, body `NewMember` `{readerId,
  role: MembershipRole}` → `201` `Member`; `400` `Problem`
  `/problems/validation` on `readerId` when it names no reader or a
  member, its code not enumerated; `404` `Problem` `/problems/not-found`
  when the reader does not own the bookshelf. Key `404_NOT_MY_BOOKSHELF`.
- `PUT /api/v1/me/default-bookshelf`, body `{id}` → `204`; `404` `Problem`
  `/problems/not-found` when the reader does not own that bookshelf. Key
  `404_NOT_MY_BOOKSHELF`.

## Done

On the Pixel, from the installed app on staging: create *Bibliothèque
familiale*, make it your default, add the second account of the family as
*propriétaire*; on that account, the bookshelf is on the home page, make it
the default, scan an ouvrage and add it; on yours, the card of that ISBN
reads *Dans Bibliothèque familiale*. Then create *Salon* and add the
second account as *invité*: on that account, the row of *Salon* has no
star, and its copies are in the catalogue.

## Tasks
