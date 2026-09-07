#!/usr/bin/env bash
# Behavioural tests of the proof-of-test pair: record-check.sh (PostToolUse /
# PostToolUseFailure) and the push / PR gate in guard-git.sh. Needs git, jq.
#   bash agent/hooks/test-proof.sh
set -euo pipefail
here=$(cd "$(dirname "$0")" && pwd)
guard="$here/guard-git.sh"; recorder="$here/record-check.sh"
tmp=$(mktemp -d); trap 'rm -rf "$tmp"' EXIT
fail=0

decision() { # $1 command → "allow" | "deny: <reason>"
  local out
  out=$(jq -cn --arg c "$1" '{tool_name:"Bash",tool_input:{command:$c}}' \
        | LIBRIS_SANDBOX=1 CLAUDE_PROJECT_DIR="$repo" bash "$guard")
  if [[ -z "$out" ]]; then echo allow
  else echo "deny: $(jq -r '.hookSpecificOutput.permissionDecisionReason' <<<"$out")"; fi
}
record() { # $1 event (PostToolUse | PostToolUseFailure), $2 command, $3 optional exit code
  jq -cn --arg e "$1" --arg c "$2" --arg x "${3:-}" \
     '{hook_event_name:$e, tool_name:"Bash", tool_input:{command:$c},
       tool_response:(if $x=="" then {} else {exit_code:($x|tonumber)} end)}' \
    | LIBRIS_SANDBOX=1 CLAUDE_PROJECT_DIR="$repo" bash "$recorder" >/dev/null
}
expect() { # $1 allow|deny, $2 label, $3 command
  local got; got=$(decision "$3")
  if [[ "$got" == "$1"* ]]; then echo "ok   $2"
  else echo "FAIL $2 — expected $1, got: $got"; fail=1; fi
}
expect_reason() { # $1 substring of the deny reason, $2 label, $3 command
  local got; got=$(decision "$3")
  if [[ "$got" == deny:*"$1"* ]]; then echo "ok   $2"
  else echo "FAIL $2 — expected a denial mentioning '$1', got: $got"; fail=1; fi
}
commit_all() { git -C "$repo" add -A && git -C "$repo" commit -q -m "$1"; }

# A repository with backend/, frontend/ and api/ on main, pushed; a task branch.
origin="$tmp/origin.git"; repo="$tmp/repo"
git init -q --bare "$origin"; git init -q -b main "$repo"
git -C "$repo" config user.email test@example.com; git -C "$repo" config user.name test
mkdir -p "$repo/backend" "$repo/frontend" "$repo/api"
echo "fun main() {}" > "$repo/backend/App.kt"
echo "export {}" > "$repo/frontend/main.ts"
echo "openapi: 3.1.0" > "$repo/api/openapi.yaml"
printf 'agent/.proof/\n' > "$repo/.gitignore"
commit_all init; git -C "$repo" remote add origin "$origin"; git -C "$repo" push -q -u origin main
git -C "$repo" switch -q -c task/T000-proof
push="git push -u origin task/T000-proof"; pr="gh pr create --base main --title t"
BACK="cd backend && ./gradlew check"; FRONT="cd frontend && npm test"

expect allow        "nothing changed: push needs no proof"                    "$push"
echo "fun main() { println(1) }" > "$repo/backend/App.kt"
expect_reason backend "backend changed, no proof: push denied"                "$push"
record PostToolUse "$BACK"
expect allow        "green backend gate on this tree: push allowed"           "$push"
commit_all "backend change"
expect allow        "same content once committed: proof still valid"          "$push"
echo "fun main() { println(2) }" > "$repo/backend/App.kt"
expect_reason "different tree" "backend changed again: proof is stale"        "$push"
record PostToolUseFailure "$BACK"
expect_reason red   "failed gate recorded: push denied"                       "$push"
record PostToolUse "$BACK" 1
expect_reason red   "PostToolUse carrying exit_code 1 counts as red"          "$push"
record PostToolUse "$BACK" 0
expect allow        "green again, exit_code 0: push allowed"                  "$push"
commit_all "backend change 2"
echo "fun main() { println(3) }" > "$repo/backend/App.kt"; commit_all "backend change 3"
record PostToolUse "cd backend && ./gradlew test"
expect_reason backend "a partial run (gradlew test) is not the gate"          "$push"
record PostToolUse "$BACK"
echo "paths: {}" >> "$repo/api/openapi.yaml"; commit_all "contract change"
expect_reason backend "a contract change invalidates the backend proof"       "$push"
record PostToolUse "$BACK"
expect_reason frontend "a contract change also needs the frontend gate"       "$push"
record PostToolUse "$FRONT"
expect allow        "both gates green: push allowed"                          "$push"
expect allow        "gh pr create with valid proofs: allowed"                 "$pr"
echo "export const x = 1" > "$repo/frontend/main.ts"; commit_all "frontend change"
expect_reason frontend "gh pr create with a stale frontend proof: denied"     "$pr"
record PostToolUse "$FRONT"
expect allow        "frontend green again: gh pr create allowed"              "$pr"

# Gate hygiene: a gate ends its command, alone, so that its exit status is what gets recorded.
expect allow        "plain gate with options"                                 "cd backend && ./gradlew check --no-daemon"
expect deny         "gate piped into tail"                                    "cd backend && ./gradlew check 2>&1 | tail -20"
expect deny         "gate followed by another command"                        "cd backend && ./gradlew check; echo done"
expect deny         "gate with || true"                                       "cd frontend && npm test || true"
expect deny         "two gates in one command"                                "cd backend && ./gradlew check && cd ../frontend && npm test"
expect allow        "a non-gate command is not concerned"                     "cd backend && ./gradlew test --tests Foo | tail -5"

# The recorder ignores what is not a gate, or what it cannot vouch for.
rm -rf "$repo/agent/.proof"
record PostToolUse "cd backend && ./gradlew build"
record PostToolUse "cd backend && ./gradlew check | tail -3"
if [[ -e "$repo/agent/.proof" ]]; then echo "FAIL a non-gate or piped command wrote a proof"; fail=1
else echo "ok   non-gate and piped commands leave no proof"; fi

exit "$fail"
