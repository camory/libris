#!/usr/bin/env bash
# Behavioural tests for guard-git.sh: feed PreToolUse JSON to the hook, check
# the decision. Needs git, jq and gitleaks on the PATH. Run from anywhere:
#   bash agent/hooks/test-guard-git.sh
set -euo pipefail
here=$(cd "$(dirname "$0")" && pwd)
hook="$here/guard-git.sh"
tmp=$(mktemp -d); trap 'rm -rf "$tmp"' EXIT
fail=0

decision() { # $1 = command, $2 = project dir → "allow" or "deny: <reason>"
  local out
  out=$(jq -cn --arg c "$1" '{tool_name:"Bash",tool_input:{command:$c}}' \
        | LIBRIS_SANDBOX=1 CLAUDE_PROJECT_DIR="$2" bash "$hook")
  if [[ -z "$out" ]]; then echo allow
  else echo "deny: $(jq -r '.hookSpecificOutput.permissionDecisionReason' <<<"$out")"; fi
}
expect() { # $1 = allow|deny, $2 = label, $3 = command, $4 = project dir
  local got; got=$(decision "$3" "$4")
  if [[ "$got" == "$1"* ]]; then echo "ok   $2"
  else echo "FAIL $2 — expected $1, got: $got"; fail=1; fi
}

# A repository with an origin, standing on a task branch, clean.
origin="$tmp/origin.git"; repo="$tmp/repo"
git init -q --bare "$origin"
git init -q -b main "$repo"
git -C "$repo" config user.email test@example.com
git -C "$repo" config user.name test
echo "# libris" > "$repo/README.md"
git -C "$repo" add README.md && git -C "$repo" commit -q -m init
git -C "$repo" remote add origin "$origin" && git -C "$repo" push -q -u origin main
git -C "$repo" switch -q -c task/T000-test

expect deny  "push to main by name"            "git push origin main"                     "$repo"
expect deny  "force push"                      "git push --force origin task/T000-test"   "$repo"
expect deny  "merge is the human's job"        "gh pr merge 1"                            "$repo"
expect allow "clean commit"                    "git commit -m 'docs: note'"               "$repo"
expect allow "clean push of a task branch"     "git push -u origin task/T000-test"        "$repo"

# A fake GitHub token, random and assembled at run time: a literal would sit in
# this repository (push protection and the CI scan would rightly refuse it), and
# gitleaks allowlists obviously sequential values.
prefix="ghp"; body=$(head -c 18 /dev/urandom | od -An -tx1 | tr -d " \n")
token="${prefix}_${body}"

printf 'GITHUB_TOKEN=%s\n' "$token" > "$repo/config.txt"
git -C "$repo" add config.txt
expect deny  "staged secret blocks commit"     "git commit -m 'add config'"               "$repo"
git -C "$repo" commit -q -m "add config"       # what a `git add && git commit` one-liner would achieve
expect deny  "committed secret blocks push"    "git push -u origin task/T000-test"        "$repo"
git -C "$repo" rm -q config.txt && git -C "$repo" commit -q -m "remove config"
expect deny  "secret still in history blocks push" "git push -u origin task/T000-test"    "$repo"

git -C "$repo" switch -q -c task/T001-unstaged main
printf 'token=%s\n' "$token" >> "$repo/README.md"   # tracked file, change not staged
expect deny  "unstaged secret blocks commit -a" "git commit -am 'update readme'"          "$repo"
git -C "$repo" checkout -q README.md
expect allow "clean again after revert"        "git commit -am 'update readme'"           "$repo"

exit "$fail"
