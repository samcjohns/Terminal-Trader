#!/usr/bin/env bash
set -euo pipefail

export TT_APP_ROOT="${TT_APP_ROOT:-/opt/terminal-trader}"
export TT_DATA_ROOT="${TT_DATA_ROOT:-/data}"

exec /opt/java/openjdk/bin/java -jar /opt/terminal-trader/tetrad.jar
