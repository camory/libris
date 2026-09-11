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

**S2 <title>** · backend

```gherkin
…
```

## Contract

The operations, schemas and problems the contract (`camory/libris-api`) gains for this
feature, written and released in the same session as the spec, before planning;
name the release. One line
per operation: method, path, request and response schemas, problems.

## Done

The feature-level check Tophe makes on a release, from the phone.

## Tasks

Filled by the planner: `T###` — scenarios it realises — PR.
