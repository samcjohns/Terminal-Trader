#!/usr/bin/env bash
set -euo pipefail

mkdir -p /home/tt/.ssh /keys /data
chmod 700 /home/tt/.ssh

authorized_keys="/home/tt/.ssh/authorized_keys"
: > "$authorized_keys"

sanitize_identity() {
  local raw="$1"
  local out
  out=$(printf '%s' "$raw" | tr '[:upper:]' '[:lower:]' | sed -E 's/[^a-z0-9._-]+/_/g; s/_+/_/g; s/^[_-]+|[_-]+$//g')
  if [[ -z "$out" ]]; then
    out="guest"
  fi
  printf '%s' "$out"
}

shopt -s nullglob
for key_file in /keys/*.pub; do
  identity=$(basename "$key_file")
  identity="${identity%.pub}"
  identity=$(sanitize_identity "$identity")
  key_data=$(tr -d '\r' < "$key_file")
  if [[ -n "$key_data" ]]; then
    printf 'command="TT_IDENTITY=%s /usr/local/bin/tt-launch",no-agent-forwarding,no-port-forwarding,no-X11-forwarding %s\n' "$identity" "$key_data" >> "$authorized_keys"
  fi
done
shopt -u nullglob

if [[ ! -s "$authorized_keys" ]]; then
  echo "No SSH keys found in /keys. Mount one or more *.pub files to start." >&2
  exit 1
fi

chown -R tt:tt /home/tt /data
chmod 600 "$authorized_keys"

if [[ ! -f /etc/ssh/ssh_host_ed25519_key ]] || [[ ! -f /etc/ssh/ssh_host_rsa_key ]]; then
  ssh-keygen -A
fi

exec /usr/sbin/sshd -D -e -f /etc/ssh/sshd_config
