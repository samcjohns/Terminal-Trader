#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
KEY="$ROOT_DIR/ssh-test/client/alice"

if [[ ! -f "$KEY" ]]; then
  echo "Missing key: $KEY"
  echo "Run scripts/setup-ssh-test.sh first."
  exit 1
fi

mkdir -p "$ROOT_DIR/ssh-test"

printf '6\n' | ssh \
  -i "$KEY" \
  -p 2222 \
  -o StrictHostKeyChecking=no \
  -o UserKnownHostsFile="$ROOT_DIR/ssh-test/known_hosts" \
  -tt tt@localhost || true

echo "Smoke test complete."
