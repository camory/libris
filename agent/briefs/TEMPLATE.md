# T### — Title

**Backlog line:** copy of the task as written in `agent/TASKS.md`
**Serves:** spec `<feature>` S#, S#; PRD §4.x; decisions Dxx …
**First of its kind:** what this task introduces that the code base has none of yet, or `none`

## Goal
Two sentences: what exists when this task is done, and why it matters now.

## Rules in play
One line per decision of `docs/ARCHITECTURE.md` and rule of `docs/DESIGN.md`
the task touches, numbered, each stating what it requires of this task:
- D07 — the scenario tests named in the backlog line are un-skipped, never
  edited
- U04 — the button is busy while the lookup runs and the field stays editable

## Acceptance criteria
- [ ] Each line checkable by a test, a command, or an HTTP call
- [ ] …

## Test plan
Which tests to write first and what each proves. Name the test classes or
files. State which suite must be green (`./gradlew check`, `npm test`).
When a step rewrites an existing test, say whether it adopts the current
convention. Prescribe what each assertion proves, never the spec body.

## Files and modules
What is created or changed, grouped by module. Existing code to read first.
Every file the task's specs share, fixtures included, is listed here; a rule
that defers a file to "a later spec" must not fire inside the task.

## Out of scope
What a tempted implementer must not do in this task, and where it belongs.

## Risks and decisions
Open points, each with the decision made for this task and the reason.
A point decided here is the planner's; the criteria do not call it a local
choice of the implementer.
