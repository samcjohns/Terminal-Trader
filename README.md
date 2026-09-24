# Terminal-Trader

Terminal Trader is now set up for server-hosted gameplay instead of per-user local installation.

## Server Runtime Model

- The game runs as a single terminal process per SSH session.
- Players do not pick a save profile in-game.
- Profile identity is resolved from environment variables (intended to be set by your SSH wrapper).
- If no identity data is provided, the player is assigned `guest`.
- Save data is written to mounted storage.
- Audio playback is removed from startup flow for headless SSH terminals.

## Identity Resolution

At process startup, identity is resolved in this order:

1. `TT_IDENTITY`
2. `TT_SSH_KEY_FP`
3. `TT_SSH_PUBLIC_KEY` (hashed)
4. `guest`

The resolved identity is sanitized for filesystem-safe storage.

## Filesystem Layout

Use mounted persistent storage at `TT_DATA_ROOT` (default: `./data`).

Per player, the game writes to:

- `TT_DATA_ROOT/players/<identity>/saves/`
- `TT_DATA_ROOT/players/<identity>/gen/`
- `TT_DATA_ROOT/players/<identity>/logs/`

Assets are loaded from `TT_APP_ROOT/assets` (default app root is current working directory).

## Environment Variables

- `TT_APP_ROOT`: Application root containing `assets/` and game files.
- `TT_DATA_ROOT`: Persistent data root for generated files and saves.
- `TT_IDENTITY`: Explicit player identity (recommended if your SSH layer already resolves users).
- `TT_SSH_KEY_FP`: SSH public key fingerprint fallback identity source.
- `TT_SSH_PUBLIC_KEY`: Raw public key fallback (hashed if used).

## Local Development Run

```bash
java -jar target/tetrad-1.1.1.jar
```

Optional local identity override:

```bash
TT_IDENTITY=devuser TT_DATA_ROOT=./data java -jar target/tetrad-1.1.1.jar
```

## Docker Build/Run

Build image:

```bash
docker build -t terminal-trader:latest .
```

Run with mounted data:

```bash
docker run --rm -it \
   -e TT_IDENTITY=player1 \
   -e TT_DATA_ROOT=/data \
   -v $(pwd)/tt-data:/data \
   terminal-trader:latest
```

## Test Setup

This repo includes a local SSH test harness so you can practice the exact
user flow: open terminal, SSH in, and land directly in the game.

### 1) Generate local test keys

```bash
bash scripts/setup-ssh-test.sh
```

This creates:

- `ssh-test/client/alice` and `ssh-test/client/bob` (private keys)
- `ssh-test/keys/alice.pub` and `ssh-test/keys/bob.pub` (mounted in container)

### 2) Start SSH game container

```bash
docker compose -f docker-compose.ssh.yml up --build
```

The container runs OpenSSH on `localhost:2222` and forces game launch for key
holders. No shell is available.

### 3) Login like a player

```bash
ssh -i ssh-test/client/alice -p 2222 tt@localhost
```

Expected behavior:

- SSH login succeeds
- Terminal Trader starts immediately
- Save data writes to `tt-data/players/alice/`
- Exiting the game ends the SSH session

### 4) Optional smoke test

```bash
bash scripts/smoke-ssh.sh
```

This sends `6` after login (save and exit) to validate session lifecycle.

### 5) Verify persisted data

```bash
find tt-data -maxdepth 4 -type f | sort
```

You should see per-identity files under `tt-data/players/<identity>/`.

## SSH Container Details

- Compose file: `docker-compose.ssh.yml`
- SSH target: `ssh-runtime` stage in `Dockerfile`
- SSH entrypoint: `docker/ssh/tt-entrypoint.sh`
- Forced launcher: `docker/ssh/tt-launch.sh`

Public keys are mounted from `./ssh-test/keys`. Each file name sets the in-game
identity (for example `alice.pub` -> identity `alice`).

## SSH Integration Notes

Your SSH layer can launch this container (or the jar directly) as the forced command.
For no-shell gameplay, configure SSH so successful authentication executes Terminal Trader immediately and exits when the game exits.
