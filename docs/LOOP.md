# The agentic loop, explained

This is the "journey" document: what the loop is, why each piece exists, and
how to read what it does. Keep it honest; update it when the loop changes.

## The idea in one sentence

Run an AI coding agent **many times with a clean head**, each time on **one
small, testable task**, against **files it can trust** (spec, decisions,
backlog, progress log), inside a **sandbox**, ending each run with something a
human can review in a few minutes (a pull request).

Everything else is plumbing around that sentence.

## Anatomy

Three AI roles, each a separate `claude -p` run with a fresh context and its
own prompt file, chained by a deterministic script. Every run happens in the
sandbox. State passes only through files in the repository and the pull
request.

```
docs/PRD.md ──► PLANNER (backlog) ──► agent/TASKS.md proposal ──► human approves the list
                                              │ first unchecked task
                                              ▼
                                      PLANNER (brief) ──► concrete brief for that task
                                              ▼
                                        IMPLEMENTER ──► branch, commits, PR, PROGRESS entry
                                              │ PR number
                                              ▼
                                         REVIEWER ──► review comment + verdict on the PR
                                              ▼
                              human: merge, request changes, or close
                                              │ merged
                                              └──► next task
```

| Role | What it is | When it runs | Decided 2026-09-06 |
|------|------------|--------------|--------------------|
| Orchestrator | `agent/loop.sh`, no AI | every iteration | deterministic script, readable end to end |
| Planner | fresh run, its own prompt | once up front and on demand for the backlog; before every task for a brief | "both" |
| Implementer | fresh run, its own prompt | once per task | one task, one PR |
| Reviewer | fresh run, its own prompt | once per PR | advisory: findings + verdict as a PR comment, human decides |
| Guard | hook + sandbox + GitHub rules, no AI | always | last line of defence |

### Why a fresh context per iteration
Long sessions drift: the agent forgets early decisions and accumulates wrong
assumptions. A fresh run must re-read the files, so **the files are the
memory**. This is the single most important property of the loop. It also
makes cost predictable: one task ≈ one bounded conversation.

### Why the files, and which files
Nothing important lives outside these files. Decided 2026-09-06 (step 2).

| File | Holds | Planner | Implementer | Reviewer | Human |
|------|-------|---------|-------------|----------|-------|
| `CLAUDE.md` | standing orders for every run | reads | reads | reads | writes |
| `docs/PRD.md` | what the product must do | reads | reads parts | reads parts | writes |
| `docs/ARCHITECTURE.md` | binding technical decisions | reads | reads | judges against | writes |
| `agent/TASKS.md` | ordered backlog with checkboxes | proposes via a `plan/<date>` PR | ticks one line | reads | approves by merging |
| `agent/briefs/T###.md` | concrete plan for one task | writes, on the task branch | reads, follows | judges against | reads in the PR |
| `agent/PROGRESS.md` | append-only diary | reads | appends one entry per task | reads | reads |
| the pull request | diff, description, review, verdict | | opens | comments | decides |

Two rules sit behind the table. The reviewer judges against written criteria
(brief, architecture), not taste, so its verdicts are checkable. The planner
never touches code and the implementer never rewrites the plan, so when
something goes wrong you can tell which role failed.

### Why small tasks
The agent's success rate falls sharply with task size. A task should fit in
one PR a human reads in ten minutes and should have a mechanical pass/fail
(tests, build, a curl). If a task fails twice, split it, do not retry harder.

### Why tests are the oracle
The loop has no human in it while it runs. Only automated verification lets
the agent know it is done. This is why the backend is contract-verified and
why "tests pass" is part of the definition of done, not a suggestion.

### Why a sandbox
Inside the container the agent runs with permission prompts disabled — it
would otherwise stall on the first prompt with nobody to answer. The container
is what makes that acceptable: it sees only the repository bind mount, a
throwaway database, and two scoped tokens. No Docker socket, no host home,
no SSH keys.

### Why a PR and a wait
`main` is protected on GitHub; the agent's token can push branches and open
PRs but cannot merge. The human review is the approval gate, exactly as in
the Contracteer runbook. The loop pauses until the PR is merged or closed:
sequential, boring, safe. Parallel tasks and auto-merge on green CI are
later upgrades, not defaults.

## Guardrails, from the outside in

1. **GitHub**: branch protection on `main`; fine-grained token limited to the
   `libris` repo, contents + pull requests only, no workflow permission.
2. **Container**: non-root user, no Docker socket, only the repo mounted,
   capped at 3 CPUs / 6 GB, and fenced off the LAN and the host by a firewall
   rule (`agent/host/sandbox-firewall.sh`): internet yes, home network no.
3. **CLI limits**: `--max-turns` and `--max-budget-usd` per run; the driver
   also caps iterations per invocation.
4. **Hook**: `agent/hooks/guard-git.sh` denies `git push` to `main`, force
   pushes, and `rm -rf` outside the workspace, even in skip-permissions mode.
5. **Prompt**: one task only, stop and report `blocked` rather than improvise
   around a missing decision.

## Reading a run

Each role run writes `agent/logs/<timestamp>-<task>-<role>.json` (the CLI's
JSON result: cost, turns, session id, and the structured report) plus a
`.stderr` with the agent's progress messages. `loop.sh` prints one line per
role with status, cost and turns, and `loop.sh status` shows the derived
phase at any time. The PR body and the reviewer's comment are the
human-readable versions.

## When the loop gets stuck

- **Same task fails twice** → split it in `TASKS.md`, or add the missing
  decision to `ARCHITECTURE.md`. Do not just re-run.
- **`blocked` report** → read the reason; usually a decision or a credential
  is missing.
- **PR closed without merge** → the loop stops; edit the task and rerun.
- **Costs climb** → lower `MAX_TURNS`, make tasks smaller, check that tests
  are not flaky (flaky tests burn iterations).

## Upgrade path (later, one at a time)

1. A `review` mode: the loop reads PR review comments and pushes fixes.
2. Auto-merge when CI is green for tasks labelled `low-risk`.
3. Independent tasks in parallel via git worktrees.
4. A scheduled trigger (systemd timer) so runs happen overnight.

## Decision log

Decisions taken while designing the loop, newest last.

- 2026-09-06 · step 1 · Roles: planner (backlog and per-task brief), implementer, reviewer as separate fresh runs; reviewer is advisory.
- 2026-09-06 · step 2 · Briefs are committed files in the task PR; the backlog is approved as a pull request; only the implementer writes the diary.
- 2026-09-06 · step 3 · Backlog mode may rewrite any unchecked task; brief mode splits an oversized task via a plan PR and stops; the planner creates the task branch and commits the brief.
- 2026-09-07 · step 4 · Implementer may deviate locally and must record it; self-reviews its diff before the PR; gives up after three failed fixes of the same error and reports blocked.
- 2026-09-07 · step 5 · Reviewer checks out the branch and reruns the tests; verdict as a PR comment with a fixed header plus a `review:*` label; only blocking findings (criterion unmet, test not proving its claim, decision violated, security or data loss, contradicted verification) flip it to request changes.
- 2026-09-07 · step 6 · One orchestrator script with subcommands (plan, next, run N, review, status); stateless, phase derived from git and GitHub; after a split it waits for the plan PR; the reviewer's verdict never stops the loop.
- 2026-09-07 · step 7 · Sandbox capped at 3 CPUs / 6 GB; sandbox network pinned to 172.30.0.0/24 and fenced off the LAN and the host by a DOCKER-USER/INPUT rule set; Playwright left out of the image until a task needs it.
- 2026-09-07 · step 7, applied · Fence verified from inside the sandbox with `agent/host/sandbox-firewall.sh verify`. Lesson: a published port is DNAT'ed into the owning container's network before the firewall sees it, and one stack here uses 172.1.1.0/24 (outside the private ranges), so other Docker networks are dropped by output interface (`br-+`, `docker0`), not by subnet.
- 2026-09-07 · step 8, part 1 · Initial commit pushed; repository made public because branch protection is unavailable on private repos under the free plan; `main` requires a PR and the three CI checks with admin bypass disabled (verified: a direct push is refused with GH006, and the first CI run passed).
