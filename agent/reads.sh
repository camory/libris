#!/usr/bin/env bash
# Lists what a run read, in order, from its stream log: agent/reads.sh agent/logs/<run>.jsonl
set -euo pipefail
jq -r '
  select(.type == "assistant") | .message.content[] | select(.type == "tool_use") |
  if .name == "Read" then "Read  \(.input.file_path)\(if .input.limit then " (\(.input.offset // 0)+\(.input.limit) lines)" else "" end)"
  elif .name == "Skill" then "Skill \(.input.skill)"
  elif .name == "Bash" then (.input.command | scan("(?:cat|head|tail|sed|less|grep) [^|;&]*")) | "Bash  \(.)"
  else empty end
' "$1"
