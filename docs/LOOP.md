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
docs/PRD.md ──► specs/<feature>.md (written with the human, contract included)
                        │
                        ▼
              PLANNER (backlog) ──► agent/TASKS.md proposal ──► human approves the list
                                              │ first unchecked task
                                              ▼
                                      PLANNER (brief) ──► concrete brief for that task
                                              ▼
                                        IMPLEMENTER ──► branch, commits, PR, PROGRESS entry
                                              │ PR number
                                              ▼
                                         REVIEWER ──► review comment + verdict on the PR
                                              ▼
                                human: merge, send back, or close
                                      │ merged        │ a review + `rework` label
                                      ▼               ▼
                                  next task    IMPLEMENTER (rework) ──► new commits on the same PR
                                                      │
                                                      └──► REVIEWER again
```

| Role | What it is | When it runs | Decided 2026-09-06 |
|------|------------|--------------|--------------------|
| Orchestrator | `agent/loop.sh`, no AI | every iteration | deterministic script, readable end to end |
| Planner | fresh run, its own prompt | once up front and on demand for the backlog; before every task for a brief | "both" |
| Implementer | fresh run, its own prompt | once per task, and once per rework asked on its PR | one task, one PR |
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
| `docs/PRD.md` | what the product must do, the domain words | reads | reads parts | reads parts | writes |
| `specs/<feature>.md` | one feature's scenarios, contract and done check | derives a phase from it, fills its *Tasks* | reads the cited scenarios | judges against | writes, with Claude, in a session |
| `docs/ARCHITECTURE.md` | binding technical decisions | reads | reads | judges against | writes |
| `docs/DESIGN.md` | binding screen rules | reads | reads | judges against | writes, with Claude, in a session |
| `agent/TASKS.md` | ordered backlog with checkboxes | proposes via a `plan/<date>` PR | ticks one line | reads | approves by merging |
| `agent/briefs/T###.md` | concrete plan for one task | writes, on the task branch | reads, follows | judges against, amended by the human's reviews | reads in the PR, amends by a review |
| `agent/PROGRESS.md` | append-only diary | reads the tail | appends one entry per task and per rework | reads the entry | reads |
| `agent/gotchas/` | what a run must know before it starts, kept true: `every-run.md`, `backend.md`, `frontend.md` | reads every-run and its sides, whole | reads every-run and its sides, whole; adds and corrects | reads every-run and the diff's sides, whole; checks the diff | reads, edits |
| `agent/PROPOSED.md` | follow-ups and ideas, never picked up by a run | | appends | | promotes into a spec or a task |
| the pull request | diff, description, review, verdict | | opens, reworks | comments | merges, sends back with a review, or closes |

Two rules sit behind the table. The reviewer judges against written criteria
(brief, architecture), not taste, so its verdicts are checkable. The planner
never touches code and the implementer never rewrites the plan, so when
something goes wrong you can tell which role failed. A human review on the
PR amends the brief without editing it, the newest review winning.

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
sequential, boring, safe. A brief marked *first of its kind* is where the
human spends that review: the task introduces something the code base had
none of, so it sets conventions no document states yet, and the review
writes them into `docs/ARCHITECTURE.md` before the next task copies the
pattern. Every other task gets the headless review alone. Parallel tasks and auto-merge on green CI are
later upgrades, not defaults.

### Why a PR is sent back, not closed
A headless approval is not a merge. When the human reads the PR and wants a
different shape, closing it throws away what was right and a rerun from the
same brief rebuilds the same shape. So the PR is sent back: the human writes
what must change as a review on the PR and sets the `rework` label.
`loop.sh rework <pr>` runs the implementer on the branch with every review
and comment newer than the PR's last commit, the reviewer's verdict included.
The run adds commits to the same branch and PR (never a rebase, never a
rewrite), records the decision in the diary, clears `rework` and `review:*`
when it pushes, and the reviewer runs again. The brief is not edited: the
human's reviews amend it and the reviewer judges against both; the
reviewer's verdict amends nothing, its blocking findings are the brief
unmet. A rework that needs a decision the review does not make reports
`blocked` like any run.

The reviewer's block is a rework too. When the reviewer requests changes,
`loop.sh next` runs the same rework round itself, the verdict as its input,
and the reviewer runs again; `REWORK_ROUNDS` (one by default) caps the
rounds, since a second block in a row says the brief or the review got
something wrong and a human should read it. A PR left with
`review:changes` can be sent back again by hand with `loop.sh rework <pr>`,
no label to set.

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

1. Auto-merge when CI is green for tasks labelled `low-risk`.
2. Independent tasks in parallel via git worktrees.
3. A scheduled trigger (systemd timer) so runs happen overnight.

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
- 2026-09-07 · step 8, settings · Secret scanning and push protection enabled on the public repository; merged head branches deleted automatically. Rationale: the agent's tokens live only in `agent/.env`, but a public repository needs a server-side refusal of a leaked credential, not just a `.gitignore`. Next loop pieces, in order: gitleaks for generic patterns (CI job + sandbox guard), then a proof-of-test hook refusing `git push` / `gh pr create` without a green `./gradlew check` / `npm test` on the current tree.
- 2026-09-07 · step 8, gitleaks · The guard hook now runs gitleaks (pinned in the image) on every `git commit` (staged and unstaged diff) and every `git push` (all commits not on origin/main), failing closed. CI's `guardrails` job scans the whole history with the same pinned binary and runs the hook's behavioural tests (`agent/hooks/test-guard-git.sh`). Chosen over the gitleaks GitHub Action: one pinned, checksummed binary in both places, no marketplace dependency, and the existing required check covers it.
- 2026-09-07 · step 8, merges · Squash-only merges (merge commits and rebase merges disabled; PR title → commit subject, PR body → commit message) and required linear history on `main`. Rationale: one commit per task on `main` and a readable history; `loop.sh` is unaffected, it detects a merge through the PR state, not git ancestry.
- 2026-09-07 · step 8, proof of test · The sandbox records every plain run of a D07 gate (`./gradlew check`, `npm test`) through PostToolUse / PostToolUseFailure hooks, with a git tree hash of the side's content plus `api/`; the guard refuses `git push` and `gh pr create` unless every side that differs from `origin/main` has a green run on exactly the current content. Gates must end their command alone (nothing piped or chained), one per command, so the recorded exit status is the gate's. Rationale: CI was already the hard gate at merge time; this makes an untested branch impossible to push, so a lazy run cannot cost a review cycle or a false report. Not tamper-proof against a hostile agent (same user as the hook), which is not the threat model.
- 2026-09-08 · backlog review · No human tasks in the backlog: a task that needs a human step states it as *Precondition (human)* on its line, Tophe does it between runs, and the planner's brief mode reports `blocked` while it is missing. Rationale: the loop exists to minimise human action, and a human line in the queue would be a one-shot the orchestrator cannot take. Phase 0 rewritten against D01–D11; it now ends with `/api/v1/me` deployed as release `v0.1.0` (D09 amended: `sha-*` images on every push to `main`, `vX.Y.Z` re-tagged from a git tag, Traefik and Authelia configured by hand outside the repo). A feature-spec layer above tasks was discussed and deferred until then.
- 2026-09-08 · images · `images` job in `ci.yml` builds both Dockerfiles on every PR after the test jobs and pushes `sha-<short>` on `main`; `release.yml` re-tags them on a `v*` tag, no rebuild. Plain docker CLI in shell, no marketplace actions, OCI labels passed from CI so the Dockerfiles only need `ARG VERSION`. Consequence accepted: the app shows its revision, not the release name. Required check `images` added by hand; GHCR package visibility is a T007 runbook item.
- 2026-09-09 · tdd skill · The implementer works one test at a time: an adapted copy of Matt Pocock's `tdd` skill lives in `.claude/skills/tdd/` (seams pre-agreed by the brief's test plan, refactor kept inside the cycle and bounded to what the written tests motivate, one cycle per commit, the wiring test named as an anti-pattern) and the implementer prompt invokes it by name before the first test. The reviewer reads the commit history against it; horizontal slicing is a suggestion, not blocking, because history is never rewritten and the fix is to redo the task. Only that skill is vendored, not the plugin: the plugin would live in the `claude-state` volume, unversioned, with 24 unused skills. Verified with a one-turn `claude -p` probe in the sandbox that invoked the skill.
- 2026-09-09 · code smells · Fowler's twelve smells (chapter 3 of _Refactoring_), adapted from the baseline of Matt Pocock's `code-review` skill, live in `.claude/skills/code-smells/` as the shared vocabulary of "tidy": the implementer's bounded refactor on green tidies only a smell inside code the written tests cover, the reviewer names the smell and the remedy in a suggestion. A smell is always a judgement call, the documents override it, and what detekt or ESLint enforce is not reported. Never blocking, with one exception by scope: a whole file, dependency or parameter nothing needs is the "Only what the task uses" rule, which blocks.
- 2026-09-09 · context size · Every role runs with `--autocompact 150000`: at that context size the CLI summarises the history and continues, the built-in form of a handoff. Measured on the T006 runs (Opus, one-million-token window, no compaction): the planner ended at 140k tokens after 52 turns, the implementer at 105k after about 50, the reviewer at 65k after 26; what fills the context is stale tool output, and accuracy drops with it well before the window is full. A turn is a poor proxy (52 turns cost 140k, 26 cost 65k), so the bound is on tokens. An orchestrated handoff (a PostToolUse hook measuring the transcript, a `handoff` status, a fresh run on a note committed at green) is the next step only if compaction summaries prove lossy, seen as an agent re-reading what it had read or repeating a mistake. Also measured: after a Monitor wake-up the result JSON's `num_turns` and `duration_ms` cover only the last segment (`origin: task-notification`); cost and `duration_api_ms` stay cumulative.
- 2026-09-11 · feature specs · A phase is one feature spec, `specs/<feature>.md`: scenarios with stable IDs (Given, When, Then, and the proof each one gets), the contract delta written in the same session as the spec, the feature's done check, and the task IDs the planner fills in. The planner in backlog mode plans only from specs, the PRD being context and vocabulary; briefs restate the cited scenarios as criteria and the reviewer judges against them. Done phases collapse to one line under *Done* in `agent/TASKS.md`; follow-ups moved to `agent/PROPOSED.md`. Rationale: the first Phase 1 plan (PR #46, closed) showed the planner inventing domain choices from a five-line PRD section; the domain model went into PRD §3 (PR #47) and the behaviour needs a home between the PRD and a task line. Writing the contract per feature, once, replaces one *Precondition (human)* per backend task.
- 2026-09-11 · contract repository · The contract leaves the monorepo for `camory/libris-api`, published as immutable GitHub releases tagged `v<info.version>`; the backend verifier test and the frontend mock pin the raw URL of the tag they implement, and a feature's first backend and frontend tasks bump the pin. Rationale: the Contracteer verifier checks every operation of the document, so a contract merged ahead of its backend turned the backend gate red; a released, pinned contract lets the spec session publish it before any task runs. Immutable releases were chosen over tag rulesets (an admin can drop the ruleset and move the tag) and over jar/npm packaging (a read token per consumer, for one file).
- 2026-09-12 · scenario tests · The human writes one scenario test per scenario and side with the spec, at the edge (real HTTP over WireMock stubs of the sources on the backend, the mounted application over `contracteer mock` on the frontend), committed skipped; a task un-skips the ones it cites and edits nothing else in them; the inside and its unit tests are the run's. Rationale: Tophe wants the loop to merge without him reading code, so the deterministic part grows: the behaviour is pinned by tests he wrote, the reviewer checks the un-skip is the only change, and mutation testing (PIT, Stryker) is the next guard on the run's own tests.
- 2026-09-12 · CI by side · On a pull request the backend and frontend jobs run only when `backend/`, `frontend/` or the workflow changed, decided by a `changes` job diffing against the base; skipped jobs satisfy the required checks. Every job still runs on a push to `main`, so each merge commit has its `sha-` images and can be released. Rationale: the plan and spec PRs are docs-only and were paying two application builds each.
- 2026-09-12 · review of T013 · Reviewed interactively with Tophe after the headless approval; twelve changes followed, one of which the reviewer had found (as a suggestion), three it could have found, and eight that were conventions no document stated — the first outbound client, the first setting, the first domain sub-package, the first shared stubs. Three rules for the prompts: a criterion stated as a property is judged by counterexample, not by the cases its tests cover (blocking when one is found); a PROGRESS claim a future run would act on is checked against the branch; the brief is read against `docs/ARCHITECTURE.md` as well as the code against the brief (T013's brief told the run to copy the stubs D07 sends to `fixture`). And a *First of its kind* line in every brief: the planner names what the task introduces, the reviewer repeats it, Tophe reviews that pull request interactively and amends the architecture with what it settles. Effort is not the lever: the reviewer ran on Opus 5 at `high`; what it missed it was told not to judge.
- 2026-09-12 · design rules · `docs/DESIGN.md` holds the app-wide screen rules (palette, type, page, controls, feedback, cards, icons, words) as numbered rules U01…U08, binding like the architecture and written with Tophe from the lookup mockups; a spec's *Screen* section assumes it and repeats nothing from it. Frontend runs read it, the reviewer judges screens and briefs against it; a change to a rule is proposed in the PR body, never edited silently.
- 2026-09-12 · what a run reads · The documents grew (architecture, PRD, design rules, specs) and nothing showed whether a run read them: the CLI result kept no tool trace. Two changes. The run's stream is kept as the `.jsonl` next to the result (`--output-format stream-json --verbose`), and `agent/reads.sh` lists what a run read, so the question is measured, not guessed. And the brief gets a *Rules in play* section: the planner lists, one line each, the decisions and rules the task touches and what they require here, because a run applies what sits next to the work, the brief, not what it read forty turns earlier; the reviewer uses the list as its checklist and reports a rule the list omits as a note on the brief.
- 2026-09-14 · `agent/GOTCHAS.md` · The diary had reached a thousand lines, 65 to 80 per task, while the planner reads its last 120 lines and the implementer its last 60: a lesson older than two tasks was out of reach unless a role thought to grep. Durable facts and dated narrative were one file. Now they are two: `agent/GOTCHAS.md` holds what a run must know before it starts, one item per fact, rewritten or removed when it stops being true, read whole by every role and checked by the reviewer like a diary claim; `agent/PROGRESS.md` stays the append-only diary of what each task did, decided and left, read by its tail, and can be archived by phase without losing anything a run needs. Distilled from the 23 entries in a session with Tophe.
- 2026-09-27 · rework · A PR the human reads and wants reshaped is sent back, not closed: the human writes the change as a review on the PR and sets the `rework` label; `loop.sh rework <pr>` runs the implementer on the branch with every review and comment newer than the PR's last commit (the reviewer's verdict excepted), which adds commits to the same PR, records the decision in the diary, clears `rework` and `review:*` on push, and the reviewer reruns. The brief is not edited: the human's reviews amend it, newest winning, and the reviewer judges against both. Rationale: T043's PR #143 was approved headless and rejected by Tophe on the shape of the read side (a domain query port where a use case composing aggregates was wanted); closing it would have thrown away the contract cases, the pin and the un-skip that were right, and a rerun from the same brief would have rebuilt the same shape. A label rather than a derived signal, so a passing remark never starts a run; the author cannot tell the human from the agent, both post as the same account, and GitHub refuses "request changes" on a PR opened by that account.
- 2026-09-27 · rework on a block · The reviewer's `REQUEST CHANGES` sends the PR back by itself: `loop.sh next` runs a rework round (implementer-rework with everything written since the last commit, the verdict included, then the reviewer again), capped by `REWORK_ROUNDS`, one by default; `loop.sh rework <pr>` accepts `review:changes` as well as the `rework` label. Before this, only the human could send a PR back and the verdict was filtered out of the rework input.
- 2026-09-29 · design session skills · Two more of Matt Pocock's skills, adapted, live in `.claude/skills/`: `grill-me`, a relentless interview that maps a feature as a design tree and asks it in small rounds, facts looked up by Claude and decisions put to Tophe; `to-spec`, which turns that conversation into `specs/<feature>.md` in the house format, proofs agreed first, with no interview. Both carry `disable-model-invocation`: they serve the sessions with Tophe and never a headless run. Vendored rather than installed as a plugin, as on 2026-09-09: versioned, adapted to `docs/` and `specs/`, and only what the project uses.
- 2026-10-02 · handoff · The implementer no longer compacts: past 120k tokens of context a PostToolUse hook tells it to finish its cycle, commit, write a handoff entry in `agent/PROGRESS.md` and report `handoff`, and the loop launches a fresh implementer on the same branch, four runs at most. Measured on the T050–T052 runs: `--autocompact 150000` is a window and compaction fired at about 118k; an implementer stood at about 85k after its reading and compacted twice per task, working from a summary of the documents for most of its turns, and T052 re-read its brief and a test file after the first compaction, the sign the 2026-09-09 entry named. A fresh run reads the documents themselves again, and the state it needs is already in git, one commit per step. The turn cap is not a context bound: turns per brief step ran from 3 to 7. A handoff pushes only when the gate is green, since the scenarios a task cites stay red until its last step; otherwise the commits wait on the local branch. The other roles keep compaction, at a window of 190000: the brief planner compacted on its reading alone.
- 2026-10-03 · gotchas split · `agent/GOTCHAS.md` becomes `agent/gotchas/every-run.md`, `backend.md` and `frontend.md`, each read whole with the Read tool. At 48 KB the file no longer fit one Bash read: the CLI hands back a Bash output past about 30 KB as a 2 KB preview and a saved file, so every run read it in ranges of its own choosing. T053, T065 and T068 read slices that happened to cover their side; T067's implementer read lines 1 to 200 and never saw *Backend tests*, which began at line 215. A file per side makes "every run and your side, whole" a list of files, not a search for headings, and keeps each file well under the Read tool's limit; the review of the items that came with the split merged the duplicates and dropped what the tree no longer holds.
