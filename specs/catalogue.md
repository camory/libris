# Catalogue

**Why:** PRD §4.1 and §1. From the sofa, a reader sees what the family owns:
every edition with a copy on a bookshelf they belong to, and where each copy
sits. Now, because the bookshelf spec put the first copies on shelves and
nothing shows them but the card of their own ISBN.
**Status:** draft

## Scenarios

**S1 The catalogue lists the house's editions** · frontend, backend

```gherkin
Given editions the house holds, with copies on bookshelves the reader
      belongs to and on one they do not

When the reader opens their catalogue

Then Libris answers every edition with a copy on one of their bookshelves,
     once each, with its copies on those bookshelves and none of the other
	And in the order a shelf reads, by series name, or title when there is
	    none, ignoring case and accents, then by tome, then by title
	And the screen lists them in that order
```

Proof: contract example verified by Contracteer on both sides; backend
scenario test over HTTP; unit test of the order; frontend scenario test over
the fakes.

**S2 The catalogue comes in pages** · frontend, backend

```gherkin
Given a catalogue of more editions than a page holds

When the reader opens their catalogue

Then Libris answers the first page, in the order of S1, and names the next
	And the screen lists it
	And reaching the end of the list asks for the next page, which
	    follows the first without a gap or a repeat
	And the last page names no next
```

A page holds fifty editions, fixed by Libris; an edition added between two
pages lands in the order of S1 without shifting them. Proof: contract
examples verified by Contracteer on both sides, a page that names a next and
a last page; backend scenario test over HTTP with one edition more than a
page; frontend scenario test over the fakes with two pages.

**S3 The catalogue is empty** · frontend, backend

```gherkin
Given a reader whose bookshelves hold no copy

When they open their catalogue

Then Libris answers an empty page that names no next
	And the screen says what will fill it
```

Proof: contract example verified by Contracteer on both sides; backend
scenario test over HTTP; frontend scenario test over the fakes.

**S4 Libris unavailable** · frontend

```gherkin
Given a reader opening their catalogue

When Libris does not answer

Then the screen says to try again later
	And lists nothing
```

Two cases: the first page, which lists nothing; the next page, after which
the rows already listed stay and the sentence sits under them. Proof:
frontend scenario test with the API failing, one per case.
