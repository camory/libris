#!/usr/bin/env bash
# PostToolUse hook: measures the context of the run from its transcript, the
# input, cache-read and cache-creation tokens of the last assistant message,
# and once it reaches LIBRIS_HANDOFF_AT tells the run to hand off. Each signal
# writes "<utc time> <session id> <context> <threshold>" to
# agent/logs/context-signal.log. Active only inside the sandbox
# (LIBRIS_SANDBOX=1) and only when LIBRIS_HANDOFF_AT is set.
set -euo pipefail
[[ "${LIBRIS_SANDBOX:-0}" == "1" ]] || exit 0
threshold="${LIBRIS_HANDOFF_AT:-}"
[[ -n "$threshold" ]] || exit 0

input=$(cat)
transcript=$(jq -r '.transcript_path // ""' <<<"$input")
[[ -r "$transcript" ]] || exit 0

context=$(jq -r 'select(.type == "assistant") | .message.usage | select(. != null)
                 | (.input_tokens // 0) + (.cache_read_input_tokens // 0) + (.cache_creation_input_tokens // 0)' \
          "$transcript" | tail -n1)
[[ -n "$context" ]] || exit 0
(( context >= threshold )) || exit 0

logs="${CLAUDE_PROJECT_DIR:-.}/agent/logs"
mkdir -p "$logs"
printf '%s %s %s %s\n' "$(date -u +%FT%TZ)" "$(jq -r '.session_id // "?"' <<<"$input")" "$context" "$threshold" \
  >> "$logs/context-signal.log"

jq -cn --arg context "$context" --arg threshold "$threshold" '{hookSpecificOutput: {hookEventName: "PostToolUse",
  additionalContext: ("Context signal: this run holds " + $context + " tokens, past its handoff threshold of "
    + $threshold + ". Finish the cycle in progress and commit it at green, start no other step, "
    + "then hand off as your prompt describes.")}}'
