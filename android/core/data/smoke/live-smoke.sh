#!/usr/bin/env bash
# Live smoke test of the shared data routes (read-only): machines, machine detail, passport, sign-offs,
# workers (Foreman), glossary. The server address and every path are read from ApiConfig.kt.
# The PIN comes from the environment and is never printed. The token is redacted in the output.
#
#   WAKATTA_PIN=<pin> ./live-smoke.sh
#
# Optional: WAKATTA_SHOP (default SASEBO01), WAKATTA_ROLE (worker|foreman, default worker),
#           WAKATTA_USER_ID (default: first name in the roster), WAKATTA_BASE_URL (override).
# Creates nothing: POST /checks, /receipts, /glossary and /machines/{id}/scan are not called.
set -u

: "${WAKATTA_PIN:?set WAKATTA_PIN to the PIN of the account to sign in with}"

here="$(cd "$(dirname "$0")" && pwd)"
cfg="$here/../../network/src/main/kotlin/com/techtsunagu/wakatta/core/network/ApiConfig.kt"
const() { grep -E "const val $1 =" "$cfg" | head -1 | cut -d'"' -f2; }

base="${WAKATTA_BASE_URL:-$(const BASE_URL)}"
shop="${WAKATTA_SHOP:-SASEBO01}"
role="${WAKATTA_ROLE:-worker}"
failed=0

redact() { sed -E 's/"token":"[^"]*"/"token":"<redacted>"/g'; }

# call <label> <expected status> <curl args...>; prints the redacted body, sets $body.
call() {
  local label="$1" want="$2"; shift 2
  local out status
  out="$(curl -sS --max-time 30 -w '\n%{http_code}' "$@")" || { echo "FAIL $label: curl error"; failed=1; body=""; return; }
  status="${out##*$'\n'}"
  body="${out%$'\n'*}"
  if [ "$status" = "$want" ]; then echo "OK   $label -> $status"; else echo "FAIL $label -> $status (wanted $want)"; failed=1; fi
  echo "     $(printf '%s' "$body" | redact | cut -c1-300)"
}

call "roster ($role, $shop)" 200 -G "$base$(const ROSTER)" --data-urlencode "code=$shop" --data-urlencode "role=$role"
user_id="${WAKATTA_USER_ID:-$(printf '%s' "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)}"
[ -n "$user_id" ] || { echo "FAIL no user id in roster"; exit 1; }

call "login (user $user_id)" 200 -H 'Content-Type: application/json' \
  -d "{\"userId\":\"$user_id\",\"pin\":\"$WAKATTA_PIN\"}" "$base$(const LOGIN)"
token="$(printf '%s' "$body" | grep -o '"token":"[^"]*"' | head -1 | cut -d'"' -f4)"
[ -n "$token" ] || { echo "FAIL no token in login response"; exit 1; }
auth=(-H "Authorization: Bearer $token")

call "machines" 200 "${auth[@]}" "$base$(const MACHINES)?limit=200"
machine_id="$(printf '%s' "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)"
if [ -n "$machine_id" ]; then
  path="$(const MACHINE)"; call "machine $machine_id" 200 "${auth[@]}" "$base${path//\{machine_id\}/$machine_id}"
fi
path="$(const WORKER_PASSPORT)"; call "passport (self)" 200 "${auth[@]}" "$base${path//\{worker_id\}/$user_id}"
call "sign-offs" 200 "${auth[@]}" "$base$(const SIGN_OFFS)"
call "glossary" 200 "${auth[@]}" "$base$(const GLOSSARY)"
if [ "$role" = "foreman" ]; then
  call "workers" 200 "${auth[@]}" "$base$(const WORKERS)"
else
  call "workers as a Worker (expect 403)" 403 "${auth[@]}" "$base$(const WORKERS)"
fi

call "logout" 200 -X POST "${auth[@]}" "$base$(const LOGOUT)"

[ "$failed" = 0 ] && echo "SMOKE PASSED" || echo "SMOKE FAILED"
exit "$failed"
