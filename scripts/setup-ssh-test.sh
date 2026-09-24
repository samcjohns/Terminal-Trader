#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
CLIENT_DIR="$ROOT_DIR/ssh-test/client"
KEYS_DIR="$ROOT_DIR/ssh-test/keys"

mkdir -p "$CLIENT_DIR" "$KEYS_DIR"

create_key() {
  local identity="$1"
  local key_path="$CLIENT_DIR/$identity"

  if [[ ! -f "$key_path" ]]; then
    ssh-keygen -t ed25519 -N "" -f "$key_path" -C "$identity@terminal-trader-local" >/dev/null
  fi

  cp "$key_path.pub" "$KEYS_DIR/$identity.pub"
}

create_key "alice"
create_key "bob"

echo "Created local test identities: alice, bob"
echo "Public keys copied to: $KEYS_DIR"
echo "Private keys stored in: $CLIENT_DIR"
echo ""
echo "Next steps:"
echo "1) docker compose -f docker-compose.ssh.yml up --build"
echo "2) ssh -i ssh-test/client/alice -p 2222 tt@localhost"
