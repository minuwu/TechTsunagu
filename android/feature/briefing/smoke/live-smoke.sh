#!/usr/bin/env bash
# Live smoke test of the Briefing routes, read-only: the Briefing list (Foreman and Worker shapes) and one receipts call.
# The server address and every path are read from ApiConfig.kt. The PIN comes from the environment and is
# never printed. The token is redacted in the output.
#
#   WAKATTA_PIN=<pin> ./live-smoke.sh
#
# Optional: WAKATTA_SHOP (default SASEBO01), WAKATTA_BASE_URL (override). The same PIN signs in the first
# Foreman and the first Worker of the roster (demo accounts share it).
# Creates nothing: POST /briefings/draft and POST /briefings are not called.
set -u

: "${WAKATTA_PIN:?set WAKATTA_PIN to the demo PIN}"

here="$(cd "$(dirname "$0")" && pwd)"
cfg="$here/../../core/network/src/main/kotlin/com/techtsunagu/wakatta/core/network/ApiConfig.kt"
const() { grep -E "const val $1 =" "$cfg" | head -1 | cut -d'"' -f2; }

base="${WAKATTA_BASE_URL:-$(const BASE_URL)}"
shop="${WAKATTA_SHOP:-SASEBO01}"
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

# signin <role>: sets $token for the first account of that role.
signin() {
  call "roster ($1, $shop)" 200 -G "$base$(const ROSTER)" --data-urlencode "code=$shop" --data-urlencode "role=$1"
  local id; id="$(printf '%s' "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)"
  [ -n "$id" ] || { echo "FAIL no $1 in roster"; exit 1; }
  call "login ($1 $id)" 200 -H 'Content-Type: application/json' -d "{\"userId\":\"$id\",\"pin\":\"$WAKATTA_PIN\"}" "$base$(const LOGIN)"
  token="$(printf '%s' "$body" | grep -o '"token":"[^"]*"' | head -1 | cut -d'"' -f4)"
  [ -n "$token" ] || { echo "FAIL no token for $1"; exit 1; }
}

signin foreman
call "briefings (foreman, with answers)" 200 -H "Authorization: Bearer $token" "$base$(const BRIEFINGS)?limit=5"
bid="$(printf '%s' "$body" | grep -o '"id":"[0-9]*"' | head -1 | cut -d'"' -f4)"
if [ -n "$bid" ]; then
  receipts_path="$(const BRIEFING_RECEIPTS)"
  call "receipts ($bid)" 200 -H "Authorization: Bearer $token" "$base${receipts_path/\{briefing_id\}/$bid}"
else
  echo "SKIP receipts: no Briefing yet"
fi
call "logout (foreman)" 200 -X POST -H "Authorization: Bearer $token" "$base$(const LOGOUT)"

signin worker
call "briefings (worker, no correctIndex)" 200 -H "Authorization: Bearer $token" "$base$(const BRIEFINGS)?limit=5"
if printf '%s' "$body" | grep -q correctIndex; then echo "FAIL worker response contains correctIndex"; failed=1; fi
call "logout (worker)" 200 -X POST -H "Authorization: Bearer $token" "$base$(const LOGOUT)"

if [ "$failed" = 0 ]; then echo "SMOKE PASSED"; else echo "SMOKE FAILED"; fi
exit "$failed"
