#!/usr/bin/env bash
# PreToolUse hook: last line of defence inside the sandbox.
# Denies Bash commands that would push main, rewrite history, or wipe the tree.
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

exit 0
