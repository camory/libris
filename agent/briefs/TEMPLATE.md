# T### — Title

**Backlog line:** copy of the task as written in `agent/TASKS.md`
**Serves:** PRD §4.x …; decisions Dxx …

## Goal
Two sentences: what exists when this task is done, and why it matters now.

## Acceptance criteria
- [ ] Each line checkable by a test, a command, or an HTTP call
- [ ] …

## Test plan
Which tests to write first and what each proves. Name the test classes or
files. State which suite must be green (`./gradlew check`, `npm test`).
When a step rewrites an existing test, say whether it adopts the current
convention.

## Files and modules
What is created or changed, grouped by module. Existing code to read first.
Every file the task's specs share, fixtures included, is listed here; a rule
that defers a file to "a later spec" must not fire inside the task.

## Out of scope
What a tempted implementer must not do in this task, and where it belongs.

## Risks and decisions
Open points, each with the decision made for this task and the reason.
