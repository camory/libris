# Remove a copy

**Why:** PRD §4.1. An owner removes a copy from one of their bookshelves,
from the ouvrage's page or from the card of the lookup screen, the book
given away, lost, or added twice by mistake; the edition stays the house's
record, found again by a later scan of its ISBN. Now, because a copy added
by mistake has no way out, and a scan of a book already on the shelf is
one tap away from a second copy.
**Status:** done 2026-11-07

## Scenarios

Only an owner of a bookshelf removes its copies (PRD §3); a guest's row
carries no action. One action removes one copy, and which of a
bookshelf's copies goes is not promised: copies have no fact of their own
on screen yet. The removal asks once, inside the row, and there is no undo
after it. The edition is never removed: when the house's last copy goes,
the edition stays, with its fields as the house holds them, and a later
scan of its ISBN shows its card as an ISBN the house holds.

The facts of a copy, choosing which copy goes, moving a copy to another
bookshelf, removing from a bookshelf's screen, editing an edition the house
holds and an undo after the removal are not in this spec.

The card's bookshelf rows grow to 44 tall and say a count on a line of its
own (*Screen*), no scenario of its own: what the card shows is the card's
own tests', and edition-page S1, which reads the row's words, moves with
it.

A copy can now leave between the two reads of the catalogue, its page's
editions and their copies: the planner weighs the race noted in
`agent/PROPOSED.md`.

**S1 The owner removes one copy of two** · frontend, backend

```gherkin
Given an ouvrage with two copies on a bookshelf the reader owns

When the reader taps the trash of its row on the ouvrage's page

Then the row asks Retirer un exemplaire de and the bookshelf's name

When the reader taps Retirer

Then Libris removes one of the two copies
	And the row reads Dans and the bookshelf's name, with no count
```

The ouvrage's page asks the ouvrage anew once the copy is removed. Proof:
contract example `204_REMOVE_ONE_PIECE_1` verified by Contracteer on both
sides; backend scenario test over HTTP, the ouvrage then answered with one
copy fewer on that bookshelf; frontend scenario test over the fakes, the
removal sent with one of the row's copies.

**S2 The reader removes their last copy** · frontend, backend

```gherkin
Given an ouvrage with one copy on the reader's bookshelves, on a bookshelf
      they own

When the reader removes it from the ouvrage's page

Then Libris removes the copy and keeps the edition
	And the screen goes back to the one the page was opened from
	And with no screen to go back to, to the catalogue
```

The ouvrage's page leaves the history as the screen goes back, so the
phone's back button does not return to it. Back on a series' page whose
last tome the reader held, the page reads *Série introuvable.*, as it
does for any series not on their bookshelves. Proof: backend scenario test
over HTTP, two cases: the house's last copy, the ouvrage then not found
and the lookup of its ISBN answering the edition as the house holds it,
with no copy and without asking the sources; a copy left on another
reader's bookshelf, that reader's catalogue still listing the ouvrage;
frontend scenario test over the fakes, two cases: the page opened from
the catalogue, back on the catalogue; the page opened from its address
alone, on the catalogue; the phone's back button after it: checked by
hand on the Pixel.

**S3 A guest's row has no trash** · frontend, backend

```gherkin
Given an ouvrage with copies on a bookshelf the reader owns and on one
      they join as a guest

When the reader opens it

Then Libris answers each copy with the reader's role on its bookshelf
	And the owned row carries the trash, and the guest's row none

When the reader asks Libris to remove a copy they do not own

Then Libris answers that there is no such copy
	And the copy stays
```

The role comes with every copy Libris answers: on the ouvrage's page, the
lookup, the catalogue, the series' page and the add's own answer, so a
copy just added carries its trash with no other request. Proof: the role
verified by Contracteer on its shape on both sides; contract example
`404_NOT_MY_COPY` verified by Contracteer on both sides; backend scenario
test over HTTP, three cases: a copy on a bookshelf the reader joins as a
guest, the ouvrage first answered with each copy's role, `OWNER` on the
reader's and `VIEWER` on the guest's; one on a bookshelf they do not
belong to; an id Libris does not know; each answered not found and the
copy still there; frontend scenario test over the fakes.

**S4 Annuler puts the row back** · frontend

```gherkin
Given a row asking to remove a copy

When the reader taps Annuler

Then the row shows again as it was, with its trash
	And no copy is removed
```

Proof: frontend scenario test over the fakes, no removal sent.

**S5 One question at a time** · frontend

```gherkin
Given an ouvrage on two bookshelves the reader owns, the first row asking

When the reader taps the trash of the second row

Then the second row asks
	And the first row shows again as it was
```

Proof: frontend scenario test over the fakes.

**S6 Libris unavailable during the removal** · frontend

```gherkin
Given a row asking to remove a copy

When the reader taps Retirer and Libris does not answer

Then the row says to try again later, inside it
	And keeps its question, ready for another try
```

Proof: frontend scenario test with the API failing.

**S7 A copy already gone** · frontend

```gherkin
Given a row asking to remove a copy another owner has just removed

When the reader taps Retirer

Then Libris answers that there is no such copy
	And the screen takes the copy as removed, as S1 and S2 say
```

The reader's intent is already true: the ouvrage's page asks the ouvrage
anew, as after a removal. Proof: frontend scenario test over the fakes,
the removal answering the not-found problem.

**S8 Removing from the lookup screen** · frontend

```gherkin
Given the card of an ISBN the house holds, with a copy on a bookshelf
      the reader owns

When the reader removes that copy from its row

Then the card stays and its row shows one copy fewer
	And with no copy left on the reader's bookshelves, the card reads Dans
	    aucune de vos bibliothèques
	And the button Ajouter à ma bibliothèque shows
```

The row asks as on the ouvrage's page, S3 to S6 holding as they say
there; nothing navigates. The card drops the copy on the not-found
problem too, as S7 says. The button gone after an add (bookshelf S2) comes
back once a copy is removed. Proof: frontend scenario test over the fakes,
two cases: an ISBN the house holds with two copies on a bookshelf the
reader owns, one removed, the row then *Dans* and the bookshelf's name;
the card right after *Ajouter à ma bibliothèque*, that copy removed, the
card then *Dans aucune de vos bibliothèques* and the button back.

## Screen

Mockups: <https://claude.ai/artifact/Sq9yQmne2M9LWs9F7kJQ6p>, one artboard
per state below, exported as `specs/remove-copy/<n>-<state>.jpg` in the
same order. No route of its own: the ouvrage's page of
`specs/edition-page.md`, route `/catalogue/ouvrages/{id}`, and the lookup
screen of `specs/fast-entry.md`, route `/isbn`, the card of
`specs/bookshelf.md`.

In the card, every bookshelf row is 44 tall, *Dans aucune de vos
bibliothèques* included, its name 12 under its top; a row of a bookshelf
the reader owns carries at its right the trash, an icon button (U04),
`aria-label` *Retirer un exemplaire*. A guest's row has no action. On
every card, a bookshelf holding more than one copy says its count on a
second line under its name, *2 exemplaires* in `body` regular, `muted`,
with no middle dot; the book icon and the trash centre on the name's first
line. A tap on the trash turns the row into its question, in the same
place, the book icon gone, the row on `bg`, radius 10, its text 8 from its
edges: the sentence *Retirer un exemplaire de Bibliothèque de
Christophe ?* in `body` regular, then the cross, `aria-label` *Annuler*,
in `muted`, then the check, `aria-label` *Retirer*, in `danger`, drawn at
stroke 3, both centred on the sentence's first line. One row asks at a
time: another row's trash moves the question there. While the removal is
sent, a spinner takes the check's place and the row accepts nothing. The
message of a failed removal goes under the question, inside the row (U05):
*Erreur lors du retrait, veuillez réessayer plus tard.*

On the ouvrage's page the screen asks the ouvrage anew after a removal,
or goes back when none of the reader's copies is left; on the lookup
screen the card updates in place.

`docs/DESIGN.md` changes with this spec: in U04, a row action, an icon
button at the right of a card's row that turns the row into its question,
on `bg`, radius 10, the sentence, the cross *Annuler* in `muted` and the
check in `danger` at stroke 3, the spinner in the check's place while
sent, the message inside the row, one question per card; in U01, `danger`
also colours a check that destroys; in U06, every bookshelf row is 44
tall, and the row of a bookshelf the reader owns carries the trash; in
U07, `Trash2` joins the icons, and stroke 3 for the check of a row's
question; in U06, the count of a bookshelf row goes on a second line under
its name, *2 exemplaires*, in `body` regular, `muted`, with no middle dot,
and the row's icons centre on its first line. The rows of the catalogue
keep their count after the middle dot.

States:

- **Owned and guest** (S3): the ouvrage's page of *Astérix le Gaulois*,
  *Dans Bibliothèque de Christophe* over *2 exemplaires* with its trash,
  *Dans Salon* without.
- **Asking** (S1): the first row as its question, the cross and the
  check.
- **Removing** (S1): the spinner in the check's place.
- **Removed** (S1): *Dans Bibliothèque de Christophe* with its trash,
  *Dans Salon*.
- **Not removed** (S6): the question kept, the message under it inside
  the row.
- **Lookup card** (S8): an ISBN the house holds, its row with the trash,
  the button under the card.
- **Lookup card emptied** (S8): *Dans aucune de vos bibliothèques*, the
  button back.

## Contract

Release `v0.16.0` of `camory/libris-api`, after `v0.15.0`; an addition,
so the backend goes first.

- `Copy.bookshelf` becomes a `ReaderBookshelf`, `{id, name, role}`, the
  reader's role on the copy's bookshelf, wherever a `Copy` appears: `Book`
  (the ouvrage, the catalogue's page, the series' editions), `IsbnLookup`
  and the add's `201`.
- `DELETE /api/v1/copies/{id}`, `id` the copy's uuid → `204`, no body;
  `400` `Problem` `/problems/validation` for an id that is not one; `404`
  `Problem` `/problems/not-found` when the reader owns no such copy: an id
  Libris does not know, a copy already removed, a copy on a bookshelf the
  reader joins as a guest or does not belong to. Keys
  `204_REMOVE_ONE_PIECE_1` and `404_NOT_MY_COPY`.

PRD changes with this spec: in §3, *"A member removes a copy"* becomes
*"An owner removes a copy"*, and the edition no longer goes with the last
copy: *"An edition exists only while a copy or, later, a wish refers to
it"* goes; in §4.1, *"which deletes the edition when it was the last
copy"* goes; in §4.6, *"A wish keeps a copy-less edition alive"* goes.

## Done

On the Pixel, from the installed app on staging: scan *One Piece* 1, held
once on Bibliothèque de Christophe, add it again, read *2 exemplaires*
under the bookshelf's name; tap the trash, read the question, tap
*Annuler*, the row is back; tap the trash and *Retirer*, read *Dans
Bibliothèque de Christophe*. In *Catalogue*, open an ouvrage on two of
your bookshelves and remove one copy, the row updates. Remove your last
copy of an ouvrage and land back on the catalogue, the ouvrage gone from
it; the phone's back button does not return to its page. Scan that ISBN
again and read *Dans aucune de vos bibliothèques*, its card as the house
holds it. On the second account of the family, a guest of *Salon*, open an
ouvrage on *Salon* and see no trash on its row.
Checked by Tophe on staging, recorded on 2026-11-07.

## Tasks

- T130 — S3, the backend on `v0.16.0` — backend
- T131 — S1, S2 — backend
- T132 — S3, the frontend on `v0.16.0` — frontend
- T133 — S4, S5, the count on its own line — frontend
- T134 — S1 — frontend
- T135 — S6, S7 — frontend
- T136 — S2 — frontend
- T137 — S8 — frontend
