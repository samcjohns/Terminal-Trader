# Terminal Trader SSH/Server Migration Handoff

## Date and Context
- Date: 2026-09-24
- Repository: Terminal-Trader
- Branch: main
- Goal: Move runtime model away from local user installation toward SSH gameplay in a server/container setup, with per-user process execution.

## User Intent and Agreed Direction
The requested direction was:
- Users should be able to SSH into the game endpoint and immediately play.
- No shell access should be granted.
- Identity should come from SSH public key context, with fallback to guest.
- Audio should be removed for headless SSH terminal usage.
- Assume Docker runtime with mounted volumes for persistence.
- Add practical testing setup so SSH user experience can be tested locally.

## Clarification Outcomes Captured During Conversation
The following decisions/assumptions were established through Q&A and follow-up:
- Launch behavior: immediate game launch post-authentication.
- Access control: no interactive shell for users.
- Identity: based on SSH key info; guest fallback if identity is unavailable.
- Runtime dependencies: Java-only runtime assumed for the app.
- Initial delivery preference shifted from docs-first to execute-now implementation.
- Later requirement: provide end-to-end local Docker + SSH testing workflow.

## Discovery Findings from Existing Codebase
Before implementation, runtime assumptions were audited:
- Path resolution was OS-install oriented (Windows/macOS/Linux local install paths).
- Save profile flow required manual load/new-account prompts.
- Audio startup was wired into game startup flow.
- Debug logging path used APPDATA semantics and was not server/Linux-container safe.

Key pre-change hotspots identified:
- src/main/java/tetrad/Main.java
- src/main/java/tetrad/Game.java
- src/main/java/tetrad/SoundPlayer.java
- src/main/java/tetrad/Mutil.java
- README.md (installer-centric and not server-centric)

## Implemented Changes

### 1) Runtime and Identity Refactor (Server/Container-Oriented)
Updated src/main/java/tetrad/Main.java with the following:
- Added environment-variable based runtime model:
  - TT_APP_ROOT
  - TT_DATA_ROOT
  - TT_IDENTITY
  - TT_SSH_KEY_FP
  - TT_SSH_PUBLIC_KEY
- Added runtime initialization that creates required directories on startup.
- Added identity resolution with fallback chain and sanitization.
- Added per-identity path layout under TT_DATA_ROOT/players/<identity>/.
- Replaced OS-specific path resolver with source mapping for:
  - saves
  - gen
  - logs
  - assets
  - wav
- Simplified main execution loop to a single session lifecycle:
  - startup
  - create Game
  - startGame
  - play
  - endGame
  - exit

### 2) Auto Profile Flow for SSH Sessions
Updated src/main/java/tetrad/Game.java:
- Removed startup dependence on menu-driven profile selection for runtime entry.
- startGame now auto-resolves username from Main.getIdentity().
- startGame now attempts load, then auto-creates save on first run.
- Main menu banner changed from song display to SSH session identity display.
- Removed runtime audio startup/stop integration from game session flow.

Note: SoundPlayer class remains in repository, but gameplay startup is no longer coupled to it.

### 3) Server-Safe Logging Path
Updated src/main/java/tetrad/Mutil.java:
- DB_LOG now writes to Main.getSource("logs") + err.txt
- This ensures logging works in container/Linux server runtime and is identity-scoped.

### 4) Documentation Rewrite for Server Runtime
Reworked README.md to be server-first rather than installer-first:
- Documented runtime model and identity resolution order.
- Documented mounted filesystem expectations and environment variables.
- Added local Docker run examples.
- Added SSH test harness usage instructions.

### 5) Docker Runtime Setup
Added/updated container artifacts:

Dockerfile changes:
- Introduced build pipeline stages:
  - test (runs mvn test)
  - build (packages jar)
  - app-runtime (JRE runtime)
  - ssh-runtime (OpenSSH-enabled runtime)
- ssh-runtime installs and configures openssh-server.
- Added tt user setup for SSH operation.
- Copied SSH config and launcher/entrypoint scripts.
- Exposed port 2222 and set SSH entrypoint.

Added docker-compose.ssh.yml:
- Builds target ssh-runtime.
- Maps host port 2222 -> container 2222.
- Mounts persistent data volume (./tt-data -> /data).
- Mounts public key directory (./ssh-test/keys -> /keys:ro).

### 6) SSH Forced-Command Harness
Added new files:
- docker/ssh/sshd_config
- docker/ssh/tt-entrypoint.sh
- docker/ssh/tt-launch.sh

Behavior implemented:
- On container startup:
  - Reads /keys/*.pub
  - Derives identity from pub filename
  - Creates authorized_keys entries with forced command:
    - command="TT_IDENTITY=<identity> /usr/local/bin/tt-launch"
  - Applies restrictions in authorized_keys to disable forwarding/X11 capabilities.
- SSH login executes game launcher directly.
- No shell workflow is offered as part of normal authenticated flow.

### 7) Local Test Automation Scripts
Added scripts:
- scripts/setup-ssh-test.sh
  - Generates local ed25519 test identities (alice, bob)
  - Places private keys in ssh-test/client
  - Places public keys in ssh-test/keys for container mounting
- scripts/smoke-ssh.sh
  - Non-interactive SSH smoke path
  - Connects using alice key
  - Sends input sequence to enter and exit
  - Validates basic session lifecycle path

### 8) Ignore Rules
Updated .gitignore to include runtime/test artifacts:
- /data/
- /tt-data/
- /ssh-test/

## Validation Performed

### Static/Editor Diagnostics
- No diagnostics errors were reported in edited files after changes.

### Build/Test/Tooling
- Early in session, local mvn command was not available in host environment.
- Later, Maven-based test/build were validated in Docker build stages.

### Docker + SSH End-to-End Validation
Executed and validated:
- docker compose configuration rendering
- ssh-runtime image build
- container startup
- SSH key authentication acceptance
- forced command launch behavior
- persistence of per-identity files under mounted tt-data

Observed persisted files included per-identity structure under:
- tt-data/players/alice/gen/
- tt-data/players/alice/saves/

## Runtime Issues Encountered and Fixes Applied

1) SSH auth denied due to locked account
- Symptom: User tt not allowed because account is locked
- Fix: Adjusted user creation in Dockerfile to unlock account context for pubkey auth while still disallowing password auth in sshd config.

2) Java not found from SSH forced command context
- Symptom: launcher could not resolve java binary under sshd session environment.
- Fix: Updated docker/ssh/tt-launch.sh to use absolute Java path:
  - /opt/java/openjdk/bin/java

3) Smoke test reliability with terminal output volume
- Symptom: interactive/TTY mode produced noisy output and unstable automation behavior.
- Fix: Updated scripts/smoke-ssh.sh to non-PTY mode and quiet output redirection for deterministic smoke checks.

4) First-run onboarding consumed initial command input
- Symptom: early smoke sequence could stall due to first-run prompt behavior.
- Fix: Updated smoke input to send an initial Enter before exit selection.

## Current Behavior Summary
With current setup:
- SSH key user connects to localhost:2222
- Authenticated session directly launches Terminal Trader
- Identity is sourced from forced command TT_IDENTITY (derived from key filename)
- Session data persists to mounted tt-data per identity
- No normal shell workflow is exposed to authenticated users in this harness path

## Files Created During This Conversation
- Dockerfile (created, then iteratively updated)
- docker-compose.ssh.yml
- docker/ssh/sshd_config
- docker/ssh/tt-entrypoint.sh
- docker/ssh/tt-launch.sh
- scripts/setup-ssh-test.sh
- scripts/smoke-ssh.sh
- handoff.md (this file)

## Files Modified During This Conversation
- src/main/java/tetrad/Main.java
- src/main/java/tetrad/Game.java
- src/main/java/tetrad/Mutil.java
- README.md
- .gitignore
- Dockerfile (further updates after initial creation)
- scripts/smoke-ssh.sh (iterative updates)
- docker/ssh/tt-launch.sh (iterative update)

## Important Repo State Note
During work, unrelated deletions were observed in working tree at one point:
- Terminal-Trader-1.1.1-Installer.exe
- tetrad-installer-setup.iss

These were explicitly left untouched per user direction.

## Suggested Next Steps (Post-Handoff)
1) Add per-identity session lock files
- Prevent simultaneous writes from concurrent sessions using same identity.

2) Add CI smoke checks
- Build ssh-runtime target and run scripts/smoke-ssh.sh in CI.

3) Harden production SSH deployment profile
- Move from local harness to host-level SSH policy for tt.rat.fm.
- Add rate limiting/fail2ban/network controls as needed.

4) Optional identity strategy expansion
- Add mapping layer for key fingerprint/comment -> friendly profile names.

## Quick Start Commands for Future Operators
1) Generate local test keys:
- bash scripts/setup-ssh-test.sh

2) Start SSH container:
- docker compose -f docker-compose.ssh.yml up --build -d

3) Interactive login test:
- ssh -i ssh-test/client/alice -p 2222 tt@localhost

4) Non-interactive smoke test:
- bash scripts/smoke-ssh.sh

5) Inspect persisted data:
- find tt-data -maxdepth 4 -type f | sort

6) Shutdown harness:
- docker compose -f docker-compose.ssh.yml down

## Handoff Completion
This document captures the architectural decisions, code changes, scripts, test harness setup, debugging trail, and verified outcomes completed in this conversation.