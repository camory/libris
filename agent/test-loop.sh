#!/usr/bin/env bash
# Behavioural tests of loop.sh's state derivation against a fake gh. Run by CI.
set -euo pipefail
here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
tmp="$(mktemp -d)"; trap 'rm -rf "$tmp"' EXIT

mkdir -p "$tmp/bin"
cat >"$tmp/bin/gh" <<'FAKE'
#!/usr/bin/env bash
# Fake gh: answers `gh pr list ... --jq <expr>` from $GH_PRS (a JSON array) and
# `gh pr view <n> ... --jq <expr>` from the entry of $GH_PRS numbered <n>.
if [[ "$1 $2" == "pr view" ]]; then
  printf '%s' "${GH_PRS:-[]}" | jq -r ".[] | select(.number == $3) | ${@: -1}"
else
  head=; for ((i = 1; i <= $#; i++)); do [[ "${!i}" == --head ]] && { i=$((i + 1)); head="${!i}"; }; done
  printf '%s' "${GH_PRS:-[]}" | jq -r --arg head "$head" '[.[] | select($head == "" or .headRefName == $head)]' | jq -r "${@: -1}"
fi
FAKE
chmod +x "$tmp/bin/gh"
export PATH="$tmp/bin:$PATH"

# shellcheck disable=SC1091
source "$here/loop.sh"
have_remote() { return 0; }

fail=0
check() {  # check <name> <expected> <actual>
  if [[ "$3" == "$2" ]]; then echo "ok   $1"; else echo "FAIL $1: expected '$2', got '$3'"; fail=1; fi
}

export GH_PRS='[]'
check "branch_pr: no PR yields nothing" "" "$(branch_pr task/T001-x)"
check "open_plan_pr: no PR yields nothing" "" "$(open_plan_pr)"

export GH_PRS='[{"number":12,"state":"OPEN","headRefName":"task/T001-x"}]'
check "branch_pr: an open PR yields 'number state'" "12 OPEN" "$(branch_pr task/T001-x)"
check "open_plan_pr: a task PR is not a plan PR" "" "$(open_plan_pr)"

export GH_PRS='[{"number":7,"state":"OPEN","headRefName":"plan/2026-09-08"}]'
check "open_plan_pr: a plan/* PR yields its number" "7" "$(open_plan_pr)"

# cmd_status: the next task and its branch come from the functions below, not from the repository
ENV_FILE=/nonexistent
next_task() { echo T001; }
task_branch() { printf '%s' "${TASK_BRANCH:-}"; }
status_line() { cmd_status | grep -m1 "^ *$1:" | sed -E 's/^ *[^:]+: +//'; }

export GH_PRS='[]' TASK_BRANCH=
check "cmd_status: no plan PR shows none" "none" "$(status_line 'open plan PR')"
check "cmd_status: no task branch shows pending" "pending" "$(status_line 'brief phase')"

export GH_PRS='[{"number":7,"state":"OPEN","headRefName":"plan/2026-09-08","url":"https://x/pull/7"},{"number":12,"state":"CLOSED","headRefName":"task/T001-x","url":"https://x/pull/12","labels":[{"name":"review:approve"}]}]'
export TASK_BRANCH=task/T001-x
check "cmd_status: a plan PR shows its number and URL once" "#7 https://x/pull/7" "$(status_line 'open plan PR')"
check "cmd_status: a task branch shows once" "done on task/T001-x" "$(status_line 'brief phase')"
check "cmd_status: a task PR shows once with its state" "PR #12 (CLOSED)" "$(status_line 'implement')"
check "cmd_status: a review label shows" "review:approve" "$(status_line 'review')"

exit "$fail"
