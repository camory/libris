# agent/ — the loop, its sandbox and its state

| Path | What |
|------|------|
| `loop.sh` | The orchestrator: `plan`, `next`, `run N`, `review <pr>`, `status` |
| `test-loop.sh` | Behavioural tests of `loop.sh`'s state derivation against a fake `gh`, run by the CI `guardrails` job |
| `prompts/` | One prompt per role: `planner-backlog.md`, `planner-brief.md`, `implementer.md`, `reviewer.md` (`{{TASK_ID}}`, `{{BRANCH}}` substituted) |
| `schemas/` | The JSON report each role must end with; enforced by `--json-schema`. No `$schema` key: the CLI's validator rejects the 2020-12 meta-schema URL |
| `briefs/` | One brief per task, written by the planner on the task branch; `TEMPLATE.md` |
| `TASKS.md` | Ordered backlog with checkboxes (the loop's queue), one phase per spec in `../specs/` |
| `PROGRESS.md` | Append-only diary written by runs |
| `PROPOSED.md` | Follow-ups and ideas, appended by runs and by Tophe, promoted by a human |
| `Dockerfile`, `compose.yaml` | Sandbox image (JDK 25, Node 24, contracteer, git, gh, claude) + PostgreSQL 18 sidecar |
| `hooks/guard-git.sh` | PreToolUse hook denying pushes to main, force pushes, `rm -rf /`, sudo, and any commit or push carrying a credential (gitleaks) |
| `hooks/test-guard-git.sh` | Behavioural tests of the guard hook, run by the CI `guardrails` job |
| `hooks/record-check.sh`, `hooks/proof-lib.sh` | PostToolUse hooks recording each gate run (`./gradlew check`, `npm test`) with a content hash; the guard refuses `git push` / `gh pr create` without a green run on the current tree |
| `hooks/test-proof.sh` | Behavioural tests of the proof-of-test pair, run by CI |
| `.env.example` | Credentials and limits template → copy to `.env` (git-ignored) |
| `host/` | Host-side pieces: the LAN firewall script and its systemd unit |
| `logs/` | One JSON + stderr per run (git-ignored) |

## One-time setup

1. **GitHub repository** `camory/libris`. It is **public**: branch protection
   is not available on private repositories under a free plan, and the
   protection matters more than privacy for a repo that holds no secrets and
   no family data. `main` requires a pull request (no approval count, since
   one account cannot approve its own PR) and green `backend`, `frontend`,
   `guardrails` and `images` checks (the last added 2026-09-08 with
   `gh api -X PATCH .../protection/required_status_checks`); "do not allow
   bypassing" is on, so even the owner,
   and therefore the agent token acting as the owner, cannot push `main`.
   Applied with `gh api -X PUT repos/camory/libris/branches/main/protection`
   on 2026-09-07.
   Also on: **secret scanning** and **push protection** (a push containing a
   known credential pattern is refused before it lands) and **automatic
   deletion of head branches after merge**. Applied with
   `gh api -X PATCH repos/camory/libris` on `security_and_analysis` and
   `delete_branch_on_merge`, 2026-09-07.
   Merges are **squash only** (merge commits and rebase merges disabled); the
   squash commit takes the PR title as subject and the PR body as message,
   and `main` **requires a linear history**. Applied with the same PATCH and
   the branch-protection PUT, 2026-09-07.
2. **Fine-grained token** for the agent, repository access limited to
   `camory/libris`, permissions *Contents: read/write*, *Pull requests:
   read/write*, *Issues: read/write* (labels), *Metadata: read*. No
   *Workflows*. Put it in `agent/.env` as `GH_TOKEN`.
3. **Labels** the reviewer sets on pull requests:
   ```
   gh label create review:approve --color 0E8A16 --description "Reviewer run found no blocking issue"
   gh label create review:changes --color D93F0B --description "Reviewer run found blocking issues"
   ```
4. **Claude credential**: on the host run `claude setup-token` (subscription)
   and paste the result as `CLAUDE_CODE_OAUTH_TOKEN`, or use an
   `ANTHROPIC_API_KEY`.
5. `cp agent/.env.example agent/.env` and fill it in. `chmod 600 agent/.env`.
6. Build the sandbox: `docker compose -f agent/compose.yaml build agent`.
   The sandbox is capped at 3 CPUs and 6 GB (see `compose.yaml`) so the other
   services on bestheda keep running during builds.
7. **Fence the sandbox off the LAN** (once, needs sudo). The sandbox network is
   pinned to `172.30.0.0/24`; the script drops its traffic to private ranges
   and to this host while leaving the internet open:
   ```
   agent/host/sandbox-firewall.sh --dry-run          # see the rules
   sudo agent/host/sandbox-firewall.sh                # apply now
   sudo cp agent/host/libris-sandbox-firewall.service /etc/systemd/system/
   sudo systemctl daemon-reload && sudo systemctl enable --now libris-sandbox-firewall
   ```
   Then check it from inside the sandbox (no sudo): `agent/host/sandbox-firewall.sh verify`.
   Re-run `verify` whenever a new stack is added to this host.
8. Smoke test:
   ```
   docker compose --env-file agent/.env -f agent/compose.yaml run --rm agent \
     'claude --version && gh auth status && java -version && node --version'
   ```

## Running

```
agent/loop.sh status        # where the loop is, derived from git and GitHub; runs nothing
agent/loop.sh plan          # planner in backlog mode → a plan PR you review and merge
agent/loop.sh next          # one task: brief → implement → review, then exits for your review
agent/loop.sh run 3         # up to three tasks, waiting for each PR to be merged in between
agent/loop.sh review 14     # re-run the reviewer on PR #14, e.g. after you pushed fixes
```

The orchestrator keeps no state file. It derives the phase of the current
task from what exists: a local `task/T###-*` branch means the brief is done,
an open PR on it means the implementation is done, a `review:*` label means
the review is done. Rerunning after a crash or a Ctrl-C resumes at the right
phase.

While a run is in progress:

```
tail -f agent/logs/*.stderr          # the agent's progress messages
docker compose -f agent/compose.yaml ps
```

Ctrl-C removes the sandbox container; a branch already pushed stays on GitHub.

## Exit codes

| Code | Meaning | What to do |
|------|---------|------------|
| 0 | done for now: PR awaiting you, or backlog empty | review and merge |
| 1 | setup problem (env file, dirty tree, credentials, bad argument) | fix and rerun |
| 2 | a PR was closed without merge, or a stale branch/PR blocks the task | follow the message, usually delete the task branch or fix `TASKS.md` |
| 3 | a run reported success but the branch or PR it claims does not exist | inspect the log |
| 4 | the planner or implementer reported `blocked` | answer the blocker in docs, tasks or env |
| 6 | no valid report (crash, budget or turn cap hit) | inspect the log and its `.stderr` |

The reviewer never stops the loop: its verdict is advisory.

## Tuning

- `MAX_TURNS` / `MAX_BUDGET_USD` in `.env` bound one run. The budget is the
  CLI's list-price estimate (`total_cost_usd`); on a subscription nothing is
  billed per run, but the cap still stops a runaway iteration. The sandbox
  smoke test cost $0.41 for 4 turns with `fable`, so a foundation task with
  scaffolding and dependency downloads can legitimately reach $10 or more.
- `MODEL` / `EFFORT`: `fable` + `high` by default. Per-role overrides such as
  `REVIEWER_MODEL=sonnet` or `PLANNER_BRIEF_MAX_TURNS=40` go in `.env`.
- `AUTOCOMPACT`: the context size, in tokens, at which the CLI summarises the
  run's history and continues; 150000 by default, `auto` for the CLI's own
  threshold. A compaction shows in the session transcript kept in the
  `claude-state` volume.
- Each run writes `agent/logs/<timestamp>-<task>-<role>.json` (the CLI result
  with cost, turns and the structured report) and a matching `.stderr`.
- Gradle and npm caches persist in named volumes between runs; the database
  does not (tmpfs), so every run starts from a clean schema.
