#!/usr/bin/env bash
# Host-side firewall for the Libris sandbox network (172.30.0.0/24, see agent/compose.yaml).
#
#   sudo agent/host/sandbox-firewall.sh          apply (idempotent)
#   sudo agent/host/sandbox-firewall.sh remove   undo
#   agent/host/sandbox-firewall.sh --dry-run     print the commands only
#   agent/host/sandbox-firewall.sh verify        probe from inside the sandbox (no sudo)
#
# Policy: the sandbox may reach the public internet (registries, GitHub, the
# Anthropic API) but not the LAN, other Docker networks, or services on this
# host. Its own subnet stays reachable so the agent can talk to the PostgreSQL
# sidecar. Other Docker networks are matched by output interface (br-+,
# docker0), not by subnet: a compose file may use any range (one here uses
# 172.1.1.0/24, which is public space), and a published port is DNAT'ed into
# the owning network before our chain sees it. Rules live in a dedicated chain hooked from DOCKER-USER (traffic
# forwarded by the host) and one INPUT rule (traffic to the host itself).
# Docker re-creates DOCKER-USER on start; the systemd unit next to this script
# re-applies the rules after docker.service.
set -euo pipefail

SUBNET="172.30.0.0/24"
CHAIN="LIBRIS-SANDBOX"
PRIVATE=("10.0.0.0/8" "172.16.0.0/12" "192.168.0.0/16" "169.254.0.0/16" "100.64.0.0/10")
IPT="${IPTABLES:-$(command -v iptables || echo /usr/sbin/iptables)}"

run() { if [[ "${DRY_RUN:-0}" == 1 ]]; then echo "$IPT $*"; else "$IPT" "$@"; fi; }
have_rule() { [[ "${DRY_RUN:-0}" == 1 ]] && return 1; "$IPT" -C "$@" 2>/dev/null; }

apply() {
  # Docker owns DOCKER-USER and normally creates it; -N is harmless if it already exists.
  run -N DOCKER-USER 2>/dev/null || true
  run -N "$CHAIN" 2>/dev/null || true
  run -F "$CHAIN"
  run -A "$CHAIN" -d "$SUBNET" -j RETURN                       # sidecar traffic stays allowed
  run -A "$CHAIN" -o 'br-+' -j DROP                            # any other Docker network, whatever its subnet
  run -A "$CHAIN" -o docker0 -j DROP                           # (a published port is DNAT'ed into one of them)
  for net in "${PRIVATE[@]}"; do run -A "$CHAIN" -d "$net" -j DROP; done   # the LAN, via the host's uplink
  run -A "$CHAIN" -j RETURN                                    # everything else: internet
  have_rule DOCKER-USER -s "$SUBNET" -j "$CHAIN" || run -I DOCKER-USER 1 -s "$SUBNET" -j "$CHAIN"
  # New connections from the sandbox to this host (published ports of other stacks, SSH, Grafana...)
  have_rule INPUT -s "$SUBNET" -m conntrack --ctstate NEW -j DROP || run -I INPUT 1 -s "$SUBNET" -m conntrack --ctstate NEW -j DROP
  echo "sandbox firewall applied for $SUBNET"
}

remove() {
  have_rule DOCKER-USER -s "$SUBNET" -j "$CHAIN" && run -D DOCKER-USER -s "$SUBNET" -j "$CHAIN"
  have_rule INPUT -s "$SUBNET" -m conntrack --ctstate NEW -j DROP && run -D INPUT -s "$SUBNET" -m conntrack --ctstate NEW -j DROP
  "$IPT" -L "$CHAIN" -n >/dev/null 2>&1 && { run -F "$CHAIN"; run -X "$CHAIN"; }
  echo "sandbox firewall removed"
}

verify() {
  local compose="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/compose.yaml"
  local lan_gw host_ports sandbox_gw
  lan_gw=$(ip route show default | awk '{print $3; exit}')
  sandbox_gw="${SUBNET%.*}.1"                     # first address of the pinned subnet = this host
  host_ports=$(docker ps --format '{{.Ports}}' | grep -oE '0\.0\.0\.0:[0-9]+' | cut -d: -f2 | sort -un | head -4 | tr '\n' ' ')
  echo "probing from inside the sandbox (LAN gateway $lan_gw, host ports: ${host_ports:-none})"
  docker compose -f "$compose" run --rm -T -e LAN_GW="$lan_gw" -e SANDBOX_GW="$sandbox_gw" -e HOST_PORTS="$host_ports" agent 'bash -s' <<'PROBE' 2>&1 | grep -vE "Creating|Created|Starting|Started|Running|Healthy|Waiting|^\s*$"
fail=0
expect() { # expect <open|closed> <label> <host> <port>
  [[ -n "$3" && -n "$4" ]] || { echo "FAIL  $2: empty target"; fail=1; return; }
  timeout 3 bash -c "echo > /dev/tcp/$3/$4" 2>/dev/null; local rc=$?
  local state=closed; [[ $rc -eq 0 ]] && state=open
  local mark=PASS; [[ "$state" == "$1" ]] || { mark=FAIL; fail=1; }
  printf "%-5s %-28s %-22s %s\n" "$mark" "$2" "$3:$4" "$state"
}
getent hosts registry.npmjs.org >/dev/null && echo "PASS  DNS" || { echo "FAIL  DNS"; fail=1; }
expect open   "internet (github)"       api.github.com 443
expect open   "sidecar postgres"        postgres 5432
expect closed "host ssh via gateway"    "$SANDBOX_GW" 22
for p in $HOST_PORTS; do expect closed "host published port" "$SANDBOX_GW" "$p"; done
expect closed "LAN gateway"             "$LAN_GW" 80
exit $fail
PROBE
  local rc=${PIPESTATUS[0]}
  docker compose -f "$compose" down >/dev/null 2>&1 || true
  (( rc == 0 )) && echo "sandbox fence: all probes as expected" || echo "sandbox fence: FAILED probes above — re-apply with: sudo $0"
  return "$rc"
}

case "${1:-apply}" in
  apply) apply ;;
  remove) remove ;;
  --dry-run) DRY_RUN=1 apply ;;
  verify) verify ;;
  *) echo "usage: $0 [apply|remove|--dry-run|verify]"; exit 1 ;;
esac
