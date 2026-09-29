---
name: grill-me
description: A relentless interview to sharpen a feature design with Tophe before its spec is written. Interactive sessions only, never a headless run.
disable-model-invocation: true
---

# Grill me

Interview Tophe until the two of you share one understanding of the
feature. Map it as a **design tree**: every decision branches into the
decisions that hang off it. The root is the PRD section the feature comes
from; the leaves are what a spec needs: scenarios, the words on screen,
the contract delta, the proof of each scenario, what is out of scope.

## Rounds

Work the tree in rounds. The **frontier** is every decision whose
prerequisites are settled: the questions you can ask now without guessing
at answers you have not heard. A question whose answer depends on another
question still open belongs to a later round.

Keep a round small: three questions at most, the one the others hang on
first. Tophe steers the order and may answer one and leave the rest; a
full draft is the last step, never the first. Number each question and
give your recommended answer, so a nod is an answer.

Format a round like so:

```
❓ **Q1** - **<question title>**: <question body, choices when there are
some>

➡️ <your recommended answer, and why in one sentence>

---

❓ **Q2** - **<question title>**: …

➡️ …
```

Each round of answers reshapes the tree: settled decisions push the
frontier outward. Recompute it and ask the next round.

## Facts and decisions

Finding facts is your job, never Tophe's. A fact lives in `docs/PRD.md`
(what the product must do, the words of §3), `docs/ARCHITECTURE.md` (the
decisions D01…), `docs/DESIGN.md` (the screen rules U01…), the specs
already written under `specs/`, the code of both sides, the contract
`camory/libris-api`, staging, or a source's API. Look it up before asking,
with a probe when the fact is a behaviour (a source's answer, a limit), and
bring the evidence into the question. Do not block a round on a lookup:
ask the questions that do not depend on it.

The decisions are Tophe's. A decision no document makes is put to him,
never assumed; a decision a document already makes is cited, not reopened,
unless the feature gives a reason to amend it, in which case the amendment
is one of the questions.

## Done

The session is done when the frontier is empty: every branch visited,
nothing left silently assumed. Say so, list the decisions in the order
they were taken, and stop. The spec comes next, with `to-spec`, on Tophe's
go; nothing is written to the repository by this skill.
