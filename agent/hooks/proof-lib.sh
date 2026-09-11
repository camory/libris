#!/usr/bin/env bash
# Shared by guard-git.sh and record-check.sh; sourced, never run.
# A "gate" is one of the two commands D07 defines as green: the backend's
# `./gradlew check` and the frontend's `npm test`. A proof is one line per side
# in agent/.proof/<side>: "<side hash> <green|red> <utc time> <command>", where
# the side hash covers the content of <side>/, whether committed or not.

proof_dir() { echo "${CLAUDE_PROJECT_DIR:-.}/agent/.proof"; }

flatten() { # $1 command → one line, newlines as ';', no trailing separators (a here-string would add a newline)
  printf '%s' "$1" | tr '\n' ';' | sed -E 's/[[:space:];]+$//'
}

gate_sides() { # $1 command → the sides whose gate the command runs, one per line (may be empty)
  local c; c=$(flatten "$1")
  grep -Eq 'gradlew[^|;&]*[[:space:]]check([[:space:];&|)]|$)' <<<"$c" && echo backend
  grep -Eq '(^|[[:space:];&|(])npm[[:space:]]+(run[[:space:]]+)?test([[:space:];&|)]|$)' <<<"$c" && echo frontend
  return 0
}

gate_is_last() { # $1 command → 0 if the gate ends the command line: nothing piped or chained after it
  local c; c=$(flatten "$1")
  grep -Eq '(gradlew[^|;&]*[[:space:]]check|npm[[:space:]]+(run[[:space:]]+)?test)([[:space:]][^|;&]*)?$' <<<"$c"
}

gate_command() { # $1 side → the exact command to run
  case "$1" in backend) echo 'cd backend && ./gradlew check' ;; frontend) echo 'cd frontend && npm test' ;; esac
}

work_tree() { # $1 repo → git tree id of the whole working content: tracked (committed or not) and untracked, .gitignore respected
  local repo=$1 idx tmp
  idx=$(git -C "$repo" rev-parse --git-path index); [[ "$idx" == /* ]] || idx="$repo/$idx"
  tmp=$(mktemp); [[ -f "$idx" ]] && cp "$idx" "$tmp"
  GIT_INDEX_FILE="$tmp" git -C "$repo" add -A >/dev/null 2>&1
  GIT_INDEX_FILE="$tmp" git -C "$repo" write-tree
  rm -f "$tmp"
}

side_hash() { # $1 repo, $2 tree id or revision, $3 side → short hash of <side>/ in that tree
  git -C "$1" rev-parse -q --verify "$2:$3" 2>/dev/null || echo absent
}

side_exists() { # $1 repo, $2 tree id, $3 side
  git -C "$1" rev-parse -q --verify "$2:$3" >/dev/null 2>&1
}
