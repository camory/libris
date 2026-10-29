# Summary

**Why:** PRD §4.1 and §4.7. The card of an ouvrage, on the lookup and on
its page, shows the summary the BnF gives, in its paragraphs, folded so
that the card stays short until the reader asks for the rest. Now,
because the contract and the card have carried a summary since the
bookshelf spec and the BnF mapping has never filled it, and because the
edition form that follows this spec shows the summary as a field.
**Status:** draft

## Scenarios

The summary is read from the BnF record alone, its field 330 `$a`: the
publisher's back-cover blurb, or Electre's shorter text. Open Library is
not read for it: its text sits on the work rather than the edition and is
almost always in English. An ISBN whose BnF record carries no 330, or that
Open Library alone knows, has no summary, as today.

The BnF's text comes with markup and loose spacing that Libris cleans, and
with defects in its own bytes that Libris leaves to the reader: a tagline
glued to the text without a separator (*impérial La capitale*), `œ` that
arrives as `S` (*vSu*), `&` for `…`, a lost apostrophe, a split word
(*m ontagnes*). The summary has no length limit, in the answer, in the
database or on the card.

**S1 The BnF gives a summary** · backend

```gherkin
Given an ISBN whose BnF record carries a summary

When the reader asks for it

Then Libris answers the summary cleaned
	And split in lines where the record breaks it
```

Cleaning: the markup is dropped and its entities decoded; a paragraph end
followed by a new paragraph, a line break of the markup or a line feed of
the text is one line break, a run of them one too; a run of spaces is one
space, a lone non-breaking space, the French one before `:` and `?`, is
kept; the text is trimmed; a space is put after `.`, `!`, `?` or `…`
directly followed by a capital or `•` (*pain.DÉCOUVREZ* becomes *pain.
DÉCOUVREZ*, *épi...• LES* becomes *épi... • LES*). A summary that cleans
to nothing is no summary. In the answer, a line feed separates the lines.

Two cases: *Mont Tombe*, 9782344008980, whose summary is in paragraphs
with a bold first line, line breaks and non-breaking spaces; *Le Larousse
du Pain*, 9782035973689, with runs of spaces, sentences glued to the
next and bullets. Proof: backend scenario test over the recorded BnF
record of each case, asserting the exact summary; unit test of each
clause of the cleaning in the implementer's loop.

**S2 A summary in several lines** · frontend

```gherkin
Given an ouvrage whose summary is in several lines

When the reader sees its card

Then each line is a paragraph of its own
```

On the lookup's card and on the ouvrage's page alike: the card is one
component. Proof: frontend component test of the card fed a summary of
three lines.

**S3 A long summary folds** · frontend

```gherkin
Given an ouvrage whose summary takes more than six lines on the card

When the reader sees its card

Then the summary stops after six lines, on an ellipsis
	And Lire la suite stands under it

When the reader taps Lire la suite

Then the whole summary shows
	And Lire la suite is gone
```

Once unfolded, the summary stays whole while the card is on screen: there
is no *Réduire*. A summary of six lines or fewer shows whole, with no
button. Only the card folds: the summary field of the edition form
(`specs/edition-form.md`) never does. Proof: frontend component test of
the card, the tap showing the whole summary and removing the button; the
fold at six lines and its absence for a short summary depend on the
layout, which the tests do not render: checked by hand on the Pixel.

**S4 Stored editions gain their summary** · backend

```gherkin
Given editions stored with an ISBN and no summary
	And editions without an ISBN, or with a summary

When Libris starts on the release of this spec

Then each edition with an ISBN and no summary has the summary the BnF
     gives, cleaned as in S1
	And the others are as they were
	And Libris starts, whatever the BnF answers
```

The editions stored before this spec were stored without the BnF's
summary, in production too. They ask the BnF once, as a migration, the
first that reaches the network: an edition the BnF has no summary for,
or fails on, keeps none, and is not asked again. Three cases: the BnF
gives a summary, the BnF has none, the BnF fails. Proof: a test of the
migration against the PostgreSQL of D08, the schema migrated to the
version before it, the editions inserted, then migrated with the BnF
stubbed by WireMock, one per case; checked on staging with its stored
editions.

## Screen

Mockups: <https://claude.ai/artifact/MUFpaoJy3wQVRf7y7tvKfs>, one artboard
per state below, exported as `specs/summary/<n>-<state>.jpg` in the same
order. No route of its own: the card (U06) of the lookup screen, route `/isbn`,
and of the ouvrage's page, route `/catalogue/ouvrages/{id}`. The summary
stays the card's last part, one paragraph per line, 8 between two
paragraphs; *Lire la suite* right under it, its word on the summary's
left edge.

`docs/DESIGN.md` changes with this spec: in U06, the summary is one paragraph per line, folded at six lines with
an ellipsis and *Lire la suite* under it; in U04, the text button lives
in a banner (U09) and in a card, under its folded summary.

States, each on the ouvrage's page:

- **Folded** (S3): *Mont Tombe*, its first six lines, the sixth ending on
  *…*, then *Lire la suite*.
- **Unfolded** (S2, S3): the same card after the tap, every paragraph,
  no button.
- **Short** (S2): *Ça roule !*, whose summary fits in six lines, whole, no
  button.

## Contract

No release: `IsbnLookup` and `Book` carry `summary`, a string or `null`,
since the bookshelf spec, and no example changes. That a line feed
separates the summary's lines is no promise of the contract, which states
shapes: S1 and S2 prove it on each side.

## Done

On the Pixel, from the installed app on staging: scan *Mont Tombe*,
9782344008980, and read its summary in paragraphs, cut after six lines
on *…*, *Lire la suite* under it; tap it and read the whole summary, the
button gone; scan a book whose summary is short and read it whole, with
no button. Open an ouvrage of the catalogue stored before the release
whose BnF record carries a summary, and read it on its page.

## Tasks
