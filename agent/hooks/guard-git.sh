#!/usr/bin/env bash
# PreToolUse hook: last line of defence inside the sandbox.
# Denies Bash commands that would push main, rewrite history, wipe the tree,
# or commit / push a credential (gitleaks, pinned in the sandbox image).
# Active only when LIBRIS_SANDBOX=1 (set in the image), so humans working on the
# host with the same .claude/settings.json are not affected.
set -euo pipefail

[[ "${LIBRIS_SANDBOX:-0}" == "1" ]] || exit 0

input=$(cat)
tool=$(jq -r '.tool_name // ""' <<<"$input")
[[ "$tool" == "Bash" ]] || exit 0
cmd=$(jq -r '.tool_input.command // ""' <<<"$input")

deny() {
  jq -n --arg reason "$1" '{
    hookSpecificOutput: {
      hookEventName: "PreToolUse",
      permissionDecision: "deny",
      permissionDecisionReason: $reason
    }
  }'
  exit 0
}

# git push to main/master: by name, as a refspec target (HEAD:main), or as refs/heads/main.
if grep -Eq 'git[[:space:]]+push[^|;&]*[[:space:]:/](main|master)([[:space:]]|$)' <<<"$cmd"; then
  deny "Pushing to main is forbidden. Push a task/T### branch and open a PR."
fi
# A bare `git push` while standing on main would push main too.
if grep -Eq '(^|[[:space:];&|])git[[:space:]]+push([[:space:]]|$)' <<<"$cmd"; then
  current=$(git -C "${CLAUDE_PROJECT_DIR:-.}" symbolic-ref --short HEAD 2>/dev/null || echo "")
  if [[ "$current" == "main" || "$current" == "master" ]] && ! grep -Eq 'git[[:space:]]+push[^|;&]*[[:space:]][^-[:space:]][^[:space:]]*[[:space:]]+[^-[:space:]]' <<<"$cmd"; then
    deny "You are on $current. Create a task/T### branch before pushing."
  fi
fi
# Merging is the human's job.
if grep -Eq 'gh[[:space:]]+pr[[:space:]]+merge|gh[[:space:]]+api[^|;&]*/merge' <<<"$cmd"; then
  deny "Merging pull requests is reserved to the human reviewer."
fi
if grep -Eq 'git[[:space:]]+push[^|;&]*(--force|-f([[:space:]]|$)|\+[a-zA-Z])' <<<"$cmd"; then
  deny "Force pushes are forbidden."
fi
if grep -Eq 'git[[:space:]]+(reset[[:space:]]+--hard[[:space:]]+origin/main|rebase[[:space:]]+-i|filter-branch|push[[:space:]]+--delete[^|;&]*main)' <<<"$cmd"; then
  deny "Rewriting shared history is forbidden."
fi
if grep -Eq 'git[[:space:]]+branch[[:space:]]+-[dD][[:space:]]+main' <<<"$cmd"; then
  deny "Deleting main is forbidden."
fi
# Destructive filesystem commands outside the workspace.
if grep -Eq 'rm[[:space:]]+(--?[a-zA-Z]+[[:space:]]+)*(-[a-zA-Z]*r[a-zA-Z]*|--recursive)[[:space:]]+(--?[a-zA-Z]+[[:space:]]+)*(/|~/?|\$HOME/?|/home(/agent)?/?|/work/?)([[:space:]]|$)' <<<"$cmd"; then
  deny "Recursive delete of a root, home or the whole workspace is forbidden."
fi
if grep -Eq '(^|[[:space:];&|])(sudo|su)([[:space:]]|$)' <<<"$cmd"; then
  deny "No privilege escalation in the sandbox."
fi

# Secrets: nothing carrying a credential is committed, and nothing carrying one
# leaves the sandbox. A commit scans the staged and the unstaged diff (so that
# `git add && git commit` and `git commit -a` are both covered); a push scans
# every commit not yet on origin/main, so a secret removed in a later commit
# still blocks. Fails closed: no gitleaks, or a gitleaks error, is a denial.
repo="${CLAUDE_PROJECT_DIR:-.}"
is_commit=0; is_push=0
grep -Eq '(^|[[:space:];&|])git[[:space:]]+commit([[:space:]]|$)' <<<"$cmd" && is_commit=1
grep -Eq '(^|[[:space:];&|])git[[:space:]]+push([[:space:]]|$)' <<<"$cmd" && is_push=1
if [[ "$is_commit" == 1 || "$is_push" == 1 ]]; then
  command -v gitleaks >/dev/null \
    || deny "gitleaks is missing from the sandbox: rebuild the image (agent/Dockerfile) before committing or pushing."
  findings() { # gitleaks findings for the given scan flags, one "rule in file:line" per line; empty = clean
    local report
    report=$(gitleaks git "$repo" --no-banner --redact --exit-code 0 --log-level error -f json -r - "$@" 2>/dev/null) \
      || deny "gitleaks could not scan the repository (flags: $*). Fix the repository state before retrying."
    jq -r '.[]? | "\(.RuleID) in \(.File):\(.StartLine)"' <<<"$report"
  }
  if [[ "$is_commit" == 1 ]]; then
    found=$( { findings --pre-commit --staged; findings --pre-commit; } | sort -u)
    if [[ -n "$found" ]]; then
      deny "gitleaks found a credential in the changes to commit: ${found//$'\n'/; }. Remove it (rotate it if real); configuration comes from the environment, never from files (D09)."
    fi
  fi
  if [[ "$is_push" == 1 ]]; then
    range=""
    git -C "$repo" rev-parse --verify -q origin/main >/dev/null 2>&1 && range="origin/main..HEAD"
    found=$(findings ${range:+--log-opts="$range"})
    if [[ -n "$found" ]]; then
      deny "gitleaks found a credential in the commits to push (${range:-whole history}): ${found//$'\n'/; }. It must leave the history before anything is pushed; rotate it if real."
    fi
  fi
fi

exit 0
