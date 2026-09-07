#!/usr/bin/env bash
# Libris agentic loop — the orchestrator.
#
# Deterministic and stateless: git and GitHub are the state.
#   local branch task/T###-*        → the brief phase is done
#   an open PR on that branch       → the implementation phase is done
#   a review:* label on that PR     → the review phase is done
# Every invocation re-derives where it is and resumes at the right phase.
#
#   agent/loop.sh plan          planner, backlog mode → plan PR for you to approve
#   agent/loop.sh next          one task: brief → implement → review, then exit
#   agent/loop.sh run [N]       up to N tasks, waiting for each PR to be merged between them
#   agent/loop.sh review <pr>   re-run the reviewer on an existing pull request
#   agent/loop.sh status        show the derived state, run nothing
#
# Read docs/LOOP.md for the why, agent/README.md for setup and exit codes.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"
ENV_FILE="agent/.env"
COMPOSE=(docker compose --env-file "$ENV_FILE" -f agent/compose.yaml)

log() { printf '%s %s\n' "$(date '+%H:%M:%S')" "$*"; }
die() { log "$*"; exit "${2:-1}"; }

usage() { sed -n '2,16p' "$0" | sed 's/^# \{0,1\}//'; exit "${1:-0}"; }

# ---------------------------------------------------------------- environment
load_env() {
  [[ -f "$ENV_FILE" ]] || die "missing $ENV_FILE (copy agent/.env.example and fill it in)" 1
  set -a; # shellcheck disable=SC1090
  source "$ENV_FILE"; set +a
  : "${MODEL:=fable}" "${EFFORT:=high}" "${MAX_TURNS:=120}" "${MAX_BUDGET_USD:=15}" "${PR_POLL_SECONDS:=300}"
  [[ -n "${CLAUDE_CODE_OAUTH_TOKEN:-}${ANTHROPIC_API_KEY:-}" ]] || die "no Claude credential in $ENV_FILE" 1
  [[ -n "${GH_TOKEN:-}" ]] || die "no GH_TOKEN in $ENV_FILE" 1
  mkdir -p agent/logs
}

have_remote() { git remote get-url origin >/dev/null 2>&1; }

# ------------------------------------------------- state derived from git/GitHub
sync_main() {
  git checkout -q main
  have_remote && git pull -q --ff-only origin main
  [[ -z "$(git status --porcelain)" ]] || die "working tree not clean on main; inspect 'git status' and clean up before rerunning" 1
}

next_task() {  # T### of the first unchecked task, or nothing
  grep -m1 -oE '^- \[ \] T[0-9]{3}' agent/TASKS.md | grep -oE 'T[0-9]{3}' || true
}

task_branch() {  # local branch task/T###-*, or nothing
  git branch --list "task/$1-*" --format='%(refname:short)' | head -n1
}

branch_pr() {  # "<number> <state>" of the most recent PR whose head is $1, or nothing
  have_remote || return 0
  gh pr list --state all --head "$1" --limit 1 --json number,state --jq '.[0] | "\(.number) \(.state)"' 2>/dev/null || true
}

open_plan_pr() {  # number of an open plan/* PR, or nothing
  have_remote || return 0
  gh pr list --state open --json number,headRefName \
    --jq '[.[] | select(.headRefName | startswith("plan/"))] | .[0].number // empty' 2>/dev/null || true
}

pr_review_label() {  # review:* label on PR $1, or nothing
  gh pr view "$1" --json labels --jq '.labels[].name' 2>/dev/null | grep -E '^review:' | head -n1 || true
}

pr_state() { gh pr view "$1" --json state --jq .state 2>/dev/null || echo UNKNOWN; }
pr_url()   { gh pr view "$1" --json url   --jq .url   2>/dev/null || echo "#$1"; }

wait_for_pr() {  # blocks until PR $1 is MERGED (returns 0) or CLOSED (returns 1)
  while :; do
    local state; state=$(pr_state "$1")
    case "$state" in
      MERGED) log "PR #$1 merged"; return 0 ;;
      CLOSED) log "PR #$1 closed without merge"; return 1 ;;
      *) log "waiting on PR #$1 (state=$state); next check in ${PR_POLL_SECONDS}s"; sleep "$PR_POLL_SECONDS" ;;
    esac
  done
}

# ---------------------------------------------------------------- running a role
# run_role <role> <tag> [KEY=VALUE ...]
#   role: planner-backlog | planner-brief | implementer | reviewer  (prompt + schema of that name)
#   tag:  goes into the log file name (task id, "plan", ...)
#   KEY=VALUE: replaces {{KEY}} in the prompt
# Sets STATUS and LAST_LOG. Per-role overrides: PLANNER_BRIEF_MODEL, REVIEWER_EFFORT, IMPLEMENTER_MAX_TURNS, ...
run_role() {
  local role="$1" tag="$2"; shift 2
  local prompt; prompt=$(cat "agent/prompts/$role.md")
  local kv; for kv in "$@"; do local ph="{{${kv%%=*}}}"; prompt="${prompt//"$ph"/${kv#*=}}"; done

  local var; var=$(tr 'a-z-' 'A-Z_' <<<"$role")
  local mv="${var}_MODEL" ev="${var}_EFFORT" tv="${var}_MAX_TURNS" bv="${var}_MAX_BUDGET_USD"
  local model="${!mv:-$MODEL}" effort="${!ev:-$EFFORT}" turns="${!tv:-$MAX_TURNS}" budget="${!bv:-$MAX_BUDGET_USD}"

  LAST_LOG="agent/logs/$(date +%Y%m%d-%H%M%S)-${tag}-${role}.json"
  log "▶ $role for $tag — model=$model effort=$effort max_turns=$turns budget=\$$budget"
  set +e
  "${COMPOSE[@]}" run --rm -T agent \
    "claude -p --output-format json --json-schema \"\$(cat agent/schemas/$role.json)\" \
       --model '$model' --effort '$effort' --max-turns '$turns' --max-budget-usd '$budget' \
       --dangerously-skip-permissions --permission-prompts none" \
    <<<"$prompt" >"$LAST_LOG" 2>"${LAST_LOG%.json}.stderr"
  local rc=$?
  set -e
  (( rc == 0 )) || log "  claude exited with code $rc — see ${LAST_LOG%.json}.stderr"

  STATUS=$(jq -r '.structured_output.status // "none"' "$LAST_LOG" 2>/dev/null || echo none)
  local cost turns_used summary
  cost=$(jq -r '(.total_cost_usd // 0) | tostring | .[0:6]' "$LAST_LOG" 2>/dev/null || echo "?")
  turns_used=$(jq -r '.num_turns // "?"' "$LAST_LOG" 2>/dev/null || echo "?")
  summary=$(jq -r '.structured_output.summary // .structured_output.blocker // (.result // "" | .[0:300])' "$LAST_LOG" 2>/dev/null || true)
  log "  status=$STATUS cost=\$$cost turns=$turns_used log=$LAST_LOG"
  [[ -n "$summary" ]] && log "  $summary"
  return 0
}

field() { jq -r ".structured_output.$1 // empty" "$LAST_LOG" 2>/dev/null || true; }

sandbox_up()   { "${COMPOSE[@]}" up -d --wait postgres >/dev/null 2>&1; }
sandbox_down() { "${COMPOSE[@]}" down >/dev/null 2>&1 || true; }

# ------------------------------------------------------------------- phases
# do_task <wait:0|1> — takes the first unchecked task through the phases it still needs.
# Returns 0 when done for now (PR open, or merged when waiting), 10 when the backlog is empty.
do_task() {
  local wait="$1" task branch pr label prinfo

  sync_main
  local plan; plan=$(open_plan_pr)
  if [[ -n "$plan" ]]; then
    if (( wait )); then
      log "plan PR #$plan is open — waiting for your merge before taking a task"
      wait_for_pr "$plan" || die "plan PR closed without merge; fix agent/TASKS.md and rerun" 2
      sync_main
    else
      log "plan PR #$plan is open: $(pr_url "$plan") — merge or close it, then rerun"; return 0
    fi
  fi

  task=$(next_task); [[ -n "$task" ]] || return 10
  log "task $task"

  # Phase 1 — brief (planner)
  branch=$(task_branch "$task")
  if [[ -z "$branch" ]]; then
    run_role planner-brief "$task" TASK_ID="$task"
    case "$STATUS" in
      brief_written) branch=$(field branch); [[ -n "$branch" ]] || branch=$(task_branch "$task")
                     [[ -n "$branch" ]] || die "brief_written but no task branch found" 3
                     log "  brief committed on $branch" ;;
      split) log "  $task split into smaller tasks: $(field pr_url)"
             if (( wait )); then
               local num; num=$(open_plan_pr); [[ -n "$num" ]] || die "split reported but no plan PR found" 3
               wait_for_pr "$num" || die "split PR closed without merge; fix agent/TASKS.md and rerun" 2
               return 0
             fi
             log "  merge the split PR, then rerun"; return 0 ;;
      blocked) die "planner blocked: $(field blocker)" 4 ;;
      *) die "planner produced no valid report — inspect $LAST_LOG and ${LAST_LOG%.json}.stderr" 6 ;;
    esac
  else
    log "  brief phase already done ($branch exists)"
  fi

  # Phase 2 — implement
  prinfo=$(branch_pr "$branch"); pr="${prinfo%% *}"
  case "${prinfo#* }" in
    CLOSED) die "PR #$pr for $branch was closed without merge. To retry from a fresh brief: git branch -D $branch (and delete it on GitHub), then rerun" 2 ;;
    MERGED) die "PR #$pr for $branch is merged but $task is still unchecked — tick it in agent/TASKS.md on main" 2 ;;
  esac
  if [[ -z "$pr" ]]; then
    run_role implementer "$task" TASK_ID="$task" BRANCH="$branch"
    case "$STATUS" in
      pr_opened) prinfo=$(branch_pr "$branch"); pr="${prinfo%% *}"
                 [[ -n "$pr" ]] || die "pr_opened reported but no PR found for $branch" 3
                 log "  PR #$pr opened: $(pr_url "$pr")" ;;
      blocked) die "implementer blocked: $(field blocker)" 4 ;;
      *) die "implementer produced no valid report — inspect $LAST_LOG and ${LAST_LOG%.json}.stderr" 6 ;;
    esac
  else
    log "  implementation phase already done (PR #$pr open)"
  fi

  # Phase 3 — review (advisory: never stops the loop)
  label=$(pr_review_label "$pr")
  if [[ -z "$label" ]]; then
    run_role reviewer "$task" TASK_ID="$task" PR_NUMBER="$pr"
    case "$STATUS" in
      posted) log "  verdict: $(field verdict) (blocking $(field blocking), suggestions $(field suggestions))" ;;
      *) log "  reviewer did not post (status=$STATUS); the PR still awaits your review" ;;
    esac
  else
    log "  review phase already done ($label)"
  fi

  # Phase 4 — the human
  log "$task awaits your review: $(pr_url "$pr")"
  if (( wait )); then
    wait_for_pr "$pr" || die "PR #$pr closed without merge; fix or split $task, delete $branch, and rerun" 2
  fi
  return 0
}

# ------------------------------------------------------------------ commands
cmd_plan() {
  load_env; sync_main
  local existing; existing=$(open_plan_pr)
  [[ -z "$existing" ]] || die "a plan PR is already open: $(pr_url "$existing") — merge or close it first" 2
  sandbox_up; trap sandbox_down EXIT
  run_role planner-backlog plan
  case "$STATUS" in
    pr_opened) log "plan PR: $(field pr_url) — review, edit, merge" ;;
    no_change) log "backlog unchanged" ;;
    blocked)   die "planner blocked: $(field blocker)" 4 ;;
    *)         die "planner produced no valid report — inspect $LAST_LOG" 6 ;;
  esac
}

cmd_next() {
  load_env; sandbox_up; trap sandbox_down EXIT
  do_task 0 || { (( $? == 10 )) && log "backlog empty — nothing to do"; }
}

cmd_run() {
  local n="${1:-1}" i rc
  load_env; sandbox_up; trap sandbox_down EXIT
  for ((i = 1; i <= n; i++)); do
    log "── iteration $i/$n ──"
    rc=0; do_task $(( i < n )) || rc=$?
    (( rc == 10 )) && { log "backlog empty — loop complete"; return 0; }
    (( rc == 0 )) || exit "$rc"
  done
  log "done: $n iteration(s); the last PR awaits your review"
}

cmd_review() {
  local pr="${1:?usage: agent/loop.sh review <pr-number>}"
  load_env
  local head task; head=$(gh pr view "$pr" --json headRefName --jq .headRefName) || die "cannot read PR #$pr" 1
  task=$(grep -oE 'T[0-9]{3}' <<<"$head" | head -n1); [[ -n "$task" ]] || die "PR #$pr head '$head' is not a task branch" 1
  sync_main; sandbox_up; trap sandbox_down EXIT
  run_role reviewer "$task" TASK_ID="$task" PR_NUMBER="$pr"
  [[ "$STATUS" == posted ]] && log "verdict: $(field verdict) (blocking $(field blocking), suggestions $(field suggestions))"
}

cmd_status() {
  [[ -f "$ENV_FILE" ]] && { set -a; source "$ENV_FILE"; set +a; }
  local task branch prinfo pr label plan
  echo "branch (local):  $(git branch --show-current)"
  echo "remote:          $(have_remote && git remote get-url origin || echo "none yet")"
  plan=$(open_plan_pr); echo "open plan PR:    ${plan:+#$plan $(pr_url "$plan")}${plan:-none}"
  task=$(next_task); echo "next task:       ${task:-none (backlog empty)}"
  [[ -n "$task" ]] || return 0
  branch=$(task_branch "$task"); echo "  brief phase:   ${branch:+done on $branch}${branch:-pending}"
  [[ -n "$branch" ]] || return 0
  prinfo=$(branch_pr "$branch"); pr="${prinfo%% *}"
  echo "  implement:     ${pr:+PR #$pr (${prinfo#* })}${pr:-pending}"
  [[ -n "$pr" ]] || return 0
  label=$(pr_review_label "$pr"); echo "  review:        ${label:-pending}"
  echo "  waiting on:    you — $(pr_url "$pr")"
}

case "${1:-}" in
  plan)    cmd_plan ;;
  next)    cmd_next ;;
  run)     cmd_run "${2:-1}" ;;
  review)  cmd_review "${2:-}" ;;
  status)  cmd_status ;;
  -h|--help|help|"") usage 0 ;;
  *) echo "unknown command: $1"; usage 1 ;;
esac
