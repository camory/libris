# Libris — Agent directives

You are one run of the Libris agentic loop, or an interactive assistant helping
Tophe on the same repository. Either way, the rules below apply.

## Required reading, in order
1. `docs/ARCHITECTURE.md` — binding technical decisions (D01…D11)
2. `docs/PRD.md` — what the product must do
3. `agent/TASKS.md` — the ordered backlog; a run works on exactly one task
4. `agent/briefs/T###.md` — the planner's brief for that task, when one exists;
   its acceptance criteria define "done"
5. `agent/PROGRESS.md` — the last few entries: what was just done and left over

Do not invent product behaviour that the PRD does not describe. If a task
needs a decision that no document makes, stop and report `blocked` with the
question; do not guess.

## How to work
- **One task per run.** Do not start the next task, do not "quickly also"
  fix unrelated things. Note follow-ups under *Proposed* in `agent/TASKS.md`.
- **TDD.** Write the failing test first, then the smallest implementation,
  then refactor. No production code without a test that motivates it.
- **Verify, never assume.** Run the real commands and read the real output.
  Never claim tests pass without having run them in this session. In the
  sandbox the two gates of D07 are recorded when run plainly, and a push or a
  PR is refused without a green run on the current tree for every side
  changed.
- **Small and boring.** Prefer the simplest design that satisfies the task and
  the architecture. No speculative abstractions, no extra dependencies unless
  the task requires them (say why in the PR body).
- **Contract first.** An API change starts in `api/openapi.yaml`, then
  backend (verified by Contracteer), then frontend (regenerated types).
- **Keep documents true.** If you learn something a future run must know
  (a command, a gotcha, a decision you had to make), write it in
  `agent/PROGRESS.md`. If it changes an architectural rule, do not edit
  `docs/ARCHITECTURE.md` silently: propose the change in the PR body.

## Definition of done for a task
- [ ] Every acceptance criterion in the brief (or, failing a brief, in the
      task line) is met and demonstrably verified
- [ ] Tests added or updated; `cd backend && ./gradlew check` and/or
      `cd frontend && npm test` pass (whichever applies)
- [ ] Contract verification passes if the API changed
- [ ] The task line in `agent/TASKS.md` is ticked `- [x]`
- [ ] An entry is appended to `agent/PROGRESS.md`
- [ ] Commits follow Conventional Commits; branch `task/T###-slug`; the PR
      title too, since it becomes the squash commit on `main`
- [ ] A pull request is open against `main` with: what, why, how verified,
      follow-ups, and any decision you had to make

## Git rules
- Never commit to or push `main`. Never force-push. Never rewrite history.
- One branch per task, created from an up-to-date `main`.
- Commit early and in logical steps; the PR may contain several commits.
- Commit trailer: `Co-Authored-By: Claude <noreply@anthropic.com>`.

## Environment
- Sandbox: JDK 25, Node 24, Gradle via wrapper, npm, git, gh, the `contracteer`
  and `gitleaks` CLIs, PostgreSQL reachable through `LIBRIS_DB_URL` / `LIBRIS_DB_USER` /
  `LIBRIS_DB_PASSWORD`. Nothing else is provided (D08).
- No Docker inside the sandbox; no network services other than the database
  and the public internet (package registries, GitHub, metadata APIs).
- `gh` is authenticated with a token limited to this repository.

## Roles
The loop runs three roles as separate headless runs: planner
(`agent/prompts/planner-*.md`), implementer (`agent/prompts/implementer.md`),
reviewer (`agent/prompts/reviewer.md`). Your prompt tells you which one you
are. Do not do another role's work.

## Final report
When running headless, your last message must be the JSON report described by
the schema your prompt names under `agent/schemas/`, and nothing else. Status
must be truthful: `pr_opened` only if `gh pr create` succeeded; `blocked` with
a clear `blocker` otherwise.
