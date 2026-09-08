---
name: code-smells
description: Fowler's code smells as labelled judgement calls for Libris code. Use when tidying on green in the tdd loop, and when reviewing a diff for suggestions.
---

# Code smells

Twelve smells from Fowler's _Refactoring_, chapter 3. They are the shared
vocabulary for what "tidy" means: the implementer uses them to name what a
bounded refactor may touch, the reviewer to name a suggestion. Each smell
reads *what it is* → *how to fix*.

## Rules

- **Always a judgement call.** A smell is a labelled heuristic ("possible
  Feature Envy"), never a hard violation. Name the smell, quote the code,
  point at the remedy.
- **The documents override.** `docs/ARCHITECTURE.md`, `docs/PRD.md` and
  `CLAUDE.md` win: where a document endorses something the baseline would
  flag, there is no smell.
- **Skip what tooling enforces.** detekt (complexity, naming, style,
  formatting) and ESLint with the boundaries rules already raise their own
  findings; those are not smells to report.
- **Implementer, on green:** tidy only a smell inside code the tests written
  so far cover, in its own `refactor:` commit. A smell that would need a new
  seam or a new test is left for the next cycle, or for the pull request's
  *Follow-ups*.
- **Reviewer:** a smell is a *Suggestion*, never a blocking finding. One
  exception by scope, not by smell: a whole file, dependency or parameter
  that nothing in the task needs is the "Only what the task uses" rule of
  `CLAUDE.md`, and that rule is blocking.

## The baseline

- **Mysterious Name**: a function, variable or type whose name doesn't reveal
  what it does or holds. → rename it; if no honest name comes, the design is
  murky.
- **Duplicated Code**: the same logic shape appears in more than one place in
  the change. → extract the shared shape, call it from both.
- **Feature Envy**: a function that reaches into another object's data more
  than its own. → move the function onto the data it envies.
- **Data Clumps**: the same few fields or parameters keep travelling together,
  a type wanting to be born. → bundle them into one type, pass that.
- **Primitive Obsession**: a primitive standing in for a domain concept, a
  `String` for an ISBN or a username. → give the concept its own small value
  type in `domain`.
- **Repeated Switches**: the same `when` or `if` cascade on the same type
  recurs across the change. → a sealed type with the behaviour on each case,
  or one map both sites share.
- **Shotgun Surgery**: one logical change forces scattered edits across many
  files. → gather what changes together into one module.
- **Divergent Change**: one file or module is edited for several unrelated
  reasons. → split it so each module changes for one reason.
- **Speculative Generality**: an abstraction, parameter or hook added for a
  need the brief doesn't have. → delete it; inline until a real need shows.
- **Message Chains**: long `a.b().c().d()` navigation the caller shouldn't
  depend on. → hide the walk behind one function on the first object.
- **Middle Man**: a class or function that mostly delegates onward. → cut it,
  call the real target directly.
- **Refused Bequest**: a subclass or implementer that ignores or overrides
  most of what it inherits. → drop the inheritance, use composition.

---

Adapted from the smell baseline of the `code-review` skill of
[mattpocock/skills](https://github.com/mattpocock/skills), MIT License,
Copyright (c) 2026 Matt Pocock. See `LICENSE` beside this file.
