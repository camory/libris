# <Feature name>

**Why:** PRD §4.x. Two sentences: what a reader can do once this exists, and
why now.
**Status:** draft | planned | done <date>

## Scenarios

One block per scenario, Gherkin keywords, one clause per line, a blank line
between Given, When and Then, `And` for each further clause. A scenario for
both sides uses words that hold on both, "the reader asks", "Libris answers";
the proof line says what each side checks. IDs are stable: task lines,
briefs, reviews and the scenario tests cite them, and a scenario test bears
the scenario's exact title, which therefore has no punctuation a method name
refuses; a scenario with several cases has one test per case, the title
followed by a comma and the case. The side line says which side realises it.

**S1 <title>** · frontend, backend

```gherkin
Given …

When …

Then …

And …
```

Proof: contract example `<name>` verified by Contracteer on both sides |
backend scenario test over stubbed sources | frontend scenario test against
`contracteer mock` | unit test of `<rule>` | checked by hand on the Pixel.
A scenario test is written with the spec, committed skipped, and un-skipped
by the task that realises it.

**S2 <title>** · backend

```gherkin
…
```

## Screen

For a feature with a frontend side: the mockups, drawn with Tophe before
planning, linked and exported under `specs/<feature>/<n>-<state>.jpg`; the
route; the layout top to bottom on a phone, with the words on screen in
French; then one bullet per state, named after the scenario it shows. The
text is what the implementer builds from; the pictures show it. The section
assumes `docs/DESIGN.md` and repeats nothing from it: it says what is on the
screen, not how a field or a card looks.

## Contract

The operations, schemas and problems the contract (`camory/libris-api`) gains for this
feature, written and released in the same session as the spec, before planning;
name the release. One line
per operation: method, path, request and response schemas, problems.

## Done

The feature-level check Tophe makes on a release, from the phone.

## Tasks

Filled by the planner: `T###` — scenarios it realises — PR.
