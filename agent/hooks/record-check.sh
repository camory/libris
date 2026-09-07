#!/usr/bin/env bash
# PostToolUse / PostToolUseFailure hook: records the result of a gate run (D07:
# `./gradlew check`, `npm test`) together with a content hash of the side it
# proves, so that guard-git.sh can refuse a push or a pull request without a
# green run on the current tree. Green only for a PostToolUse event whose
# exit code, when reported, is 0; anything else is red. Active only inside the
# sandbox (LIBRIS_SANDBOX=1).
set -euo pipefail
[[ "${LIBRIS_SANDBOX:-0}" == "1" ]] || exit 0

input=$(cat)
[[ "$(jq -r '.tool_name // ""' <<<"$input")" == "Bash" ]] || exit 0
cmd=$(jq -r '.tool_input.command // ""' <<<"$input")
event=$(jq -r '.hook_event_name // ""' <<<"$input")
exit_code=$(jq -r '.tool_response.exit_code // empty' <<<"$input")

# shellcheck source=proof-lib.sh
source "$(dirname "${BASH_SOURCE[0]}")/proof-lib.sh"
repo="${CLAUDE_PROJECT_DIR:-.}"

sides=$(gate_sides "$cmd")
[[ -n "$sides" ]] || exit 0
[[ $(wc -l <<<"$sides") -eq 1 ]] || exit 0   # two gates in one command prove nothing (the guard refuses it)
gate_is_last "$cmd" || exit 0                 # piped or chained: the exit status is not the gate's

status=green
[[ "$event" == "PostToolUse" ]] || status=red
if [[ -n "$exit_code" && "$exit_code" != "0" ]]; then status=red; fi

tree=$(work_tree "$repo")
mkdir -p "$(proof_dir)"
printf '%s %s %s %s\n' "$(side_hash "$repo" "$tree" "$sides")" "$status" "$(date -u +%FT%TZ)" "$cmd" \
  > "$(proof_dir)/$sides"
exit 0
