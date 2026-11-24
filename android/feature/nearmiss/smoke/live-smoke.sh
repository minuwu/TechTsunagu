#!/usr/bin/env bash
# Live smoke test of the Near-miss routes, read-only: list reports (Foreman), check photo route, Worker role check.
# The server address and every path are read from ApiConfig.kt. The PIN comes from the environment and is
# never printed. The token is redacted in the output.
#
#   WAKATTA_PIN=<pin> ./live-smoke.sh
#
# Optional: WAKATTA_SHOP (default SASEBO01), WAKATTA_BASE_URL (override), WAKATTA_FOREMAN_PIN, WAKATTA_WORKER_PIN.
# Creates nothing: POST /near-misses and POST /near-misses/{id}/ack are NOT called.
set -u

: "${WAKATTA_PIN:?set WAKATTA_PIN to the demo PIN}"

here="$(cd "$(dirname "$0")" && pwd)"
cfg="$here/../../../core/network/src/main/kotlin/com/techtsunagu/wakatta/core/network/ApiConfig.kt"
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
  local role="$1"
  local pin="${WAKATTA_PIN}"
  if [ "$role" = "foreman" ] && [ -n "${WAKATTA_FOREMAN_PIN:-}" ]; then
    pin="$WAKATTA_FOREMAN_PIN"
  elif [ "$role" = "worker" ] && [ -n "${WAKATTA_WORKER_PIN:-}" ]; then
    pin="$WAKATTA_WORKER_PIN"
  fi
  call "roster ($role, $shop)" 200 -G "$base$(const ROSTER)" --data-urlencode "code=$shop" --data-urlencode "role=$role"
  local id; id="$(printf '%s' "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)"
  [ -n "$id" ] || { echo "FAIL no $role in roster"; exit 1; }
  call "login ($role $id)" 200 -H 'Content-Type: application/json' -d "{\"userId\":\"$id\",\"pin\":\"$pin\"}" "$base$(const LOGIN)"
  token="$(printf '%s' "$body" | grep -o '"token":"[^"]*"' | head -1 | cut -d'"' -f4)"
  [ -n "$token" ] || { echo "FAIL no token for $role"; exit 1; }
}

signin foreman
call "near-misses (foreman list)" 200 -H "Authorization: Bearer $token" "$base$(const NEAR_MISSES)?limit=5"
rid="$(printf '%s' "$body" | grep -o '"id":"[0-9]*"' | head -1 | cut -d'"' -f4)"
has_photo="$(printf '%s' "$body" | grep -o '"photo":"[^"]*"' | head -1)"
if [ -n "$rid" ] && [ -n "$has_photo" ]; then
  photo_path="$(const NEAR_MISS_PHOTO)"
  call "near-miss photo ($rid)" 200 -H "Authorization: Bearer $token" "$base${photo_path/\{report_id\}/$rid}"
else
  echo "SKIP photo: no report with photo found"
fi
call "logout (foreman)" 200 -X POST -H "Authorization: Bearer $token" "$base$(const LOGOUT)"

signin worker
call "near-misses (worker expects 403)" 403 -H "Authorization: Bearer $token" "$base$(const NEAR_MISSES)?limit=5"
call "logout (worker)" 200 -X POST -H "Authorization: Bearer $token" "$base$(const LOGOUT)"

if [ "$failed" = 0 ]; then echo "SMOKE PASSED"; else echo "SMOKE FAILED"; fi
exit "$failed"
