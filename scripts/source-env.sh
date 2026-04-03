#!/usr/bin/env bash
# Load repo-root .env (exports every variable defined in that file).
#
#   source scripts/source-env.sh
#
# Running ./scripts/source-env.sh only affects a subshell — use source (.) instead.

_scripts_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$_scripts_dir/.." && pwd)"
ENV_FILE="$REPO_ROOT/.env"

_sourced=0
[[ "${BASH_SOURCE[0]}" != "$0" ]] && _sourced=1

if [[ ! -f "$ENV_FILE" ]]; then
    echo "Missing $ENV_FILE" >&2
    echo "  cp .env.example .env   # then edit and set DEBUG_HEADERS_ACCESS_TOKEN" >&2
    if ((_sourced)); then
        return 1
    fi
    exit 1
fi

set -a
# shellcheck disable=SC1090
source "$ENV_FILE"
set +a
