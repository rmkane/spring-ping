#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# shellcheck disable=SC1091
source "$SCRIPT_DIR/source-env.sh"

if [[ -z "${DEBUG_HEADERS_ACCESS_TOKEN:-}" ]]; then
  echo "Set DEBUG_HEADERS_ACCESS_TOKEN to the same value as app.security.debug-headers-access-token (env-only secret)." >&2
  exit 1
fi

curl -v \
    -H "Accept: application/json" \
    -H "Authorization: Bearer test" \
    -H "X-Debug-Token: ${DEBUG_HEADERS_ACCESS_TOKEN}" \
    -H "X-Forwarded-For: 127.0.0.1" \
    -H "X-Echo-Request: test" \
    http://localhost:8080/api/debug/headers
