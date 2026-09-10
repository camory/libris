---
name: tdd
description: The red → green → refactor loop for Libris. Use before writing the first test of a task and on every cycle after it, one test at a time.
---

# Test-Driven Development

TDD is the red → green → refactor loop. This skill is the reference that
makes that loop produce tests worth keeping: what a good test is, where
tests go, the anti-patterns, and the rules of the loop. Every section
applies on every cycle: consult them before and during the loop, not after.

Before the first cycle, read the brief's *Test plan* and, in
`docs/ARCHITECTURE.md`, the layering (D02) and what "green" means (D07).
Test names use the vocabulary of `docs/PRD.md`.

## What a good test is

Tests verify behaviour through public interfaces, not implementation
details. Code can change entirely; tests shouldn't. A good test reads like a
specification: `member profile is read from the request headers` tells you
exactly what capability exists, and it survives refactors because it doesn't
care about internal structure.

Expected values come from an independent source of truth: a literal from
the contract, a worked example, the PRD. Never from the code's own way of
computing them.

```kotlin
// GOOD: an independent literal
result.displayName shouldBe "Tophe"

// BAD: recomputes what the code does
result.displayName shouldBe headers["Remote-Name"]
```

## Seams: where tests go

A **seam** is the public boundary you test at: the interface where you
observe behaviour without reaching inside. Tests live at seams, never
against internals. In Libris the seams are the HTTP interface (through the
test client, matching `api/openapi.yaml`), the public functions of `domain`
and `application`, the `infra.persistence` interface against the real
PostgreSQL, and for the frontend a rendered component, a store, or
`infra/api` against the Contracteer mock.

**Test only at pre-agreed seams.** The brief's *Test plan* is that
agreement: it names the test classes or files and what each proves. Do not
write a test at a seam the brief does not name. If the plan does not survive
contact with the code, deviate and record it as the implementer rules say;
if the conflict is structural, report `blocked`.

## Anti-patterns

- **Implementation-coupled**: mocks internal collaborators, tests private
  functions, or verifies through a side channel (querying the database
  instead of using the interface). The tell: the test breaks when you
  refactor but behaviour hasn't changed.
- **Tautological**: the assertion recomputes the expected value the way the
  code does, so it passes by construction and can never disagree with the
  code.
- **Horizontal slicing**: writing all tests first, then all implementation.
  Bulk tests verify _imagined_ behaviour: you test the _shape_ of things
  rather than observable behaviour, the tests go insensitive to real
  changes, and you commit to test structure before understanding the
  implementation. Work in **vertical slices** instead: one test → one
  implementation → repeat, each test a **tracer bullet** that responds to
  what the last cycle taught you.
- **Wiring test**: it exercises Spring, Vite or a library, not Libris code.
  Write one to learn, delete it before the pull request.

## Mocking

Double Libris code only at its layer seams: a port gets a hand-written
fake from the `fixture` package, a use case seen from `infra.web` gets a
stub. On the frontend the seam is the port: a composable is called with a
fake of its port, a view is mounted with the fake provided through its
injection key, never over a mocked module. Never mock a collaborator a
module constructs itself, never mock the database or the API contract: the
database is the real PostgreSQL of the sandbox, and the frontend talks to
`contracteer mock`. A response the contract does not declare is not a seam:
the client does not handle it and no spec exercises it. Mock the boundaries
you do not control: an outside service, time, randomness, a browser API; on
the frontend that is what `vi.mock`, `vi.fn` and `vi.stubGlobal` are for,
and nothing else. A seam worth doubling is injected, not constructed inside
the module.

## Rules of the loop

- **Red before green.** Write one failing test. Run it and read the failure:
  it must fail for the reason the test states. Then write only enough code
  to pass it. Don't anticipate the next test or add speculative behaviour.
- **Refactor on green, bounded.** Tidy only what the tests written so far
  motivate: a duplicate, a misleading name, a shape the last test made
  awkward. The `code-smells` skill is the catalogue of what "tidy" means.
  Never to prepare the next test. Run the tests again.
- **One slice at a time.** One seam, one test, one minimal implementation,
  one bounded refactor per cycle. The next cycle starts from what this one
  taught.
- **One cycle, one commit.** Commit the test with the implementation that
  makes it pass, at green; a refactor is its own `refactor:` commit. A
  commit that adds several tests is horizontal slicing made visible.
- **Commit before you mutate.** A mutation check that ends with
  `git checkout <file>` restores the last commit and takes uncommitted work
  with it; commit the cycle first.
- **Run the one test while cycling, the gate at the end.** A single class or
  file is enough for red and green; the full gate proves the task.

---

Adapted from the `tdd` skill of [mattpocock/skills](https://github.com/mattpocock/skills),
MIT License, Copyright (c) 2026 Matt Pocock. See `LICENSE` beside this file.
