#!/usr/bin/env bash
# Behavioural tests of context-signal.sh (PostToolUse). Needs jq.
#   bash agent/hooks/test-context-signal.sh
set -euo pipefail
here=$(cd "$(dirname "$0")" && pwd)
hook="$here/context-signal.sh"
tmp=$(mktemp -d); trap 'rm -rf "$tmp"' EXIT
fail=0
repo="$tmp/repo"; mkdir -p "$repo"
log="$repo/agent/logs/context-signal.log"

transcript() { # $@ context sizes of the assistant messages, oldest first → path of a transcript
  local file="$tmp/transcript.jsonl" size
  printf '%s\n' '{"type":"user","message":{"role":"user","content":"go"}}' > "$file"
  for size in "$@"; do
    jq -cn --argjson s "$size" '{type:"assistant", message:{usage:
      {input_tokens:10, cache_creation_input_tokens:1000, cache_read_input_tokens:($s - 1010), output_tokens:50}}}' >> "$file"
    printf '%s\n' '{"type":"user","message":{"role":"user","content":[{"type":"tool_result","content":"ok"}]}}' >> "$file"
  done
  echo "$file"
}
signal() { # $1 transcript path, $2 threshold ("" for none), $3 sandbox flag → the hook's output
  jq -cn --arg t "$1" '{hook_event_name:"PostToolUse", session_id:"s-1", tool_name:"Bash",
                        tool_input:{command:"echo one"}, transcript_path:$t}' \
    | LIBRIS_SANDBOX="${3:-1}" LIBRIS_HANDOFF_AT="$2" CLAUDE_PROJECT_DIR="$repo" bash "$hook"
}
expect_silent() { # $1 label, $2 the hook's output
  if [[ -z "$2" ]]; then echo "ok   $1"
  else echo "FAIL $1 — expected no output, got: $2"; fail=1; fi
}
expect_signal() { # $1 label, $2 the hook's output, $3 substring of the message
  local event message
  event=$(jq -r '.hookSpecificOutput.hookEventName' <<<"$2" 2>/dev/null || true)
  message=$(jq -r '.hookSpecificOutput.additionalContext' <<<"$2" 2>/dev/null || true)
  if [[ "$event" == "PostToolUse" && "$message" == *"$3"* ]]; then echo "ok   $1"
  else echo "FAIL $1 — expected a signal mentioning '$3', got: $2"; fail=1; fi
}

expect_silent "below the threshold: no signal"             "$(signal "$(transcript 20000 119999)" 120000)"
expect_signal "at the threshold: a signal"                 "$(signal "$(transcript 20000 120000)" 120000)" "120000"
expect_signal "the signal names the context it measured"   "$(signal "$(transcript 20000 131500)" 120000)" "131500"
expect_silent "the last message counts, not the largest"   "$(signal "$(transcript 130000 40000)" 120000)"
expect_silent "no threshold: no signal"                    "$(signal "$(transcript 130000)" "")"
expect_silent "outside the sandbox: no signal"             "$(signal "$(transcript 130000)" 120000 0)"
expect_silent "a transcript that cannot be read: no signal" "$(signal "$tmp/absent.jsonl" 120000)"
printf '%s\n' '{"type":"user","message":{"role":"user","content":"go"}}' > "$tmp/empty.jsonl"
expect_silent "a transcript with no usage yet: no signal"  "$(signal "$tmp/empty.jsonl" 120000)"

rm -f "$log"
signal "$(transcript 119000)" 120000 >/dev/null
if [[ -e "$log" ]]; then echo "FAIL a run below the threshold wrote a log line"; fail=1
else echo "ok   below the threshold: no log line"; fi
signal "$(transcript 125000)" 120000 >/dev/null
if grep -Eq '^[0-9T:Z-]+ s-1 125000 120000$' "$log" 2>/dev/null; then echo "ok   a signal writes its log line"
else echo "FAIL expected '<utc time> s-1 125000 120000' in the log, got: $(cat "$log" 2>/dev/null)"; fail=1; fi

exit "$fail"
