#!/usr/bin/env bash
# Live smoke test of the Machines routes (read-only), signed in as a Foreman: machine detail, panel with
# ETag and the 304, versions, shift assignments. Server address and every path are read from ApiConfig.kt.
# The PIN comes from the environment and is never printed. The token is redacted in the output.
#
#   WAKATTA_PIN=<foreman pin> ./live-smoke.sh
#
# Optional: WAKATTA_SHOP (default SASEBO01), WAKATTA_USER_ID (default: first Foreman in the roster),
#           WAKATTA_BASE_URL (override).
# Creates nothing: overrides, PUT shift-assignments, notes and panel-token are not called.
set -u

: "${WAKATTA_PIN:?set WAKATTA_PIN to the PIN of the Foreman account to sign in with}"

here="$(cd "$(dirname "$0")" && pwd)"
cfg="$here/../../../core/network/src/main/kotlin/com/techtsunagu/wakatta/core/network/ApiConfig.kt"
const() { grep -E "const val $1 =" "$cfg" | head -1 | cut -d'"' -f2; }

base="${WAKATTA_BASE_URL:-$(const BASE_URL)}"
shop="${WAKATTA_SHOP:-SASEBO01}"
failed=0

redact() { sed -E 's/"token":"[^"]*"/"token":"<redacted>"/g'; }

# call <label> <expected status> <curl args...>; prints the redacted body, sets $body and $etag.
call() {
  local label="$1" want="$2"; shift 2
  local out status headers
  headers="$(mktemp)"
  out="$(curl -sS --max-time 30 -D "$headers" -w '\n%{http_code}' "$@")" || { echo "FAIL $label: curl error"; failed=1; body=""; rm -f "$headers"; return; }
  status="${out##*$'\n'}"
  body="${out%$'\n'*}"
  etag="$(grep -i '^etag:' "$headers" | head -1 | cut -d' ' -f2- | tr -d '\r')"
  rm -f "$headers"
  if [ "$status" = "$want" ]; then echo "OK   $label -> $status"; else echo "FAIL $label -> $status (wanted $want)"; failed=1; fi
  echo "     $(printf '%s' "$body" | redact | cut -c1-300)"
}

call "roster (foreman, $shop)" 200 -G "$base$(const ROSTER)" --data-urlencode "code=$shop" --data-urlencode "role=foreman"
user_id="${WAKATTA_USER_ID:-$(printf '%s' "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)}"
[ -n "$user_id" ] || { echo "FAIL no user id in roster"; exit 1; }

call "login (user $user_id)" 200 -H 'Content-Type: application/json' \
  -d "{\"userId\":\"$user_id\",\"pin\":\"$WAKATTA_PIN\"}" "$base$(const LOGIN)"
token="$(printf '%s' "$body" | grep -o '"token":"[^"]*"' | head -1 | cut -d'"' -f4)"
[ -n "$token" ] || { echo "FAIL no token in login response"; exit 1; }
auth=(-H "Authorization: Bearer $token")

call "machines" 200 "${auth[@]}" "$base$(const MACHINES)?limit=200"
mid="$(printf '%s' "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)"
[ -n "$mid" ] || { echo "FAIL no machine in the list"; exit 1; }
fill() { local p; p="$(const "$1")"; echo "$base${p//\{machine_id\}/$mid}"; }

call "machine detail" 200 "${auth[@]}" "$(fill MACHINE)"
call "panel" 200 "${auth[@]}" "$(fill MACHINE_PANEL)"
first_etag="$etag"
echo "     ETag: ${first_etag:-<none>}"
if [ -n "$first_etag" ]; then
  call "panel with If-None-Match (expect 304)" 304 "${auth[@]}" -H "If-None-Match: $first_etag" "$(fill MACHINE_PANEL)"
fi
call "versions (Foreman)" 200 "${auth[@]}" "$(fill MACHINE_VERSIONS)"
call "shift assignments (today)" 200 "${auth[@]}" "$base$(const SHIFT_ASSIGNMENTS)"
call "panel as a Worker would see it (no token: expect 401)" 401 "$(fill MACHINE_PANEL)"

call "logout" 200 -X POST "${auth[@]}" "$base$(const LOGOUT)"

[ "$failed" = 0 ] && echo "SMOKE PASSED" || echo "SMOKE FAILED"
exit "$failed"
