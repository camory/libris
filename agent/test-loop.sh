#!/usr/bin/env bash
# Behavioural tests of loop.sh's state derivation against a fake gh. Run by CI.
set -euo pipefail
here="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
tmp="$(mktemp -d)"; trap 'rm -rf "$tmp"' EXIT

mkdir -p "$tmp/bin"
cat >"$tmp/bin/gh" <<'FAKE'
#!/usr/bin/env bash
# Fake gh: answers any `gh pr list ... --jq <expr>` from $GH_PRS (a JSON array).
printf '%s' "${GH_PRS:-[]}" | jq -r "${@: -1}"
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

exit "$fail"
