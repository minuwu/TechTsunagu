#!/usr/bin/env bash
# Live smoke test of the Chat routes (read-only): threads, the message poll with ETag and the 304, follow-ups.
# Server address and every path are read from ApiConfig.kt. The PIN comes from the environment and is never
# printed. The token is redacted in the output.
#
#   WAKATTA_PIN=<pin> ./live-smoke.sh                       # signs in as the first Foreman of SASEBO01
#   WAKATTA_PIN=<pin> WAKATTA_ROLE=worker ./live-smoke.sh   # signs in as a Worker (follow-ups then expect 403)
#
# Optional: WAKATTA_SHOP (default SASEBO01), WAKATTA_USER_ID (default: first account of the role in the roster),
#           WAKATTA_BASE_URL (override).
# Creates nothing: sending a message, answers, Nudge and Re-explanation are NOT called.
set -u

: "${WAKATTA_PIN:?set WAKATTA_PIN to the PIN of the account to sign in with}"

here="$(cd "$(dirname "$0")" && pwd)"
cfg="$here/../../../core/network/src/main/kotlin/com/techtsunagu/wakatta/core/network/ApiConfig.kt"
const() { grep -E "const val $1 =" "$cfg" | head -1 | cut -d'"' -f2; }

base="${WAKATTA_BASE_URL:-$(const BASE_URL)}"
shop="${WAKATTA_SHOP:-SASEBO01}"
role="${WAKATTA_ROLE:-foreman}"
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

call "roster ($role, $shop)" 200 -G "$base$(const ROSTER)" --data-urlencode "code=$shop" --data-urlencode "role=$role"
user_id="${WAKATTA_USER_ID:-$(printf '%s' "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)}"
[ -n "$user_id" ] || { echo "FAIL no user id in roster"; exit 1; }

call "login (user $user_id)" 200 -H 'Content-Type: application/json' \
  -d "{\"userId\":\"$user_id\",\"pin\":\"$WAKATTA_PIN\"}" "$base$(const LOGIN)"
token="$(printf '%s' "$body" | grep -o '"token":"[^"]*"' | head -1 | cut -d'"' -f4)"
[ -n "$token" ] || { echo "FAIL no token in login response"; exit 1; }
auth=(-H "Authorization: Bearer $token")

call "threads" 200 "${auth[@]}" "$base$(const THREADS)"
tid="$(printf '%s' "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)"
if [ -n "$tid" ]; then
  murl="$base$(const THREAD_MESSAGES)"; murl="${murl//\{thread_id\}/$tid}"
  call "messages of thread $tid" 200 "${auth[@]}" "$murl"
  first_etag="$etag"
  echo "     ETag: ${first_etag:-<none>}"
  if [ -n "$first_etag" ]; then
    call "messages with If-None-Match (expect 304)" 304 "${auth[@]}" -H "If-None-Match: $first_etag" "$murl"
  fi
else
  echo "SKIP messages: this account has no thread"
fi

if [ "$role" = "foreman" ]; then
  call "follow-ups (Foreman)" 200 "${auth[@]}" "$base$(const FOLLOW_UPS)?limit=50"
else
  call "follow-ups (Worker: 403, the app shows an empty list, XREQ-020)" 403 "${auth[@]}" "$base$(const FOLLOW_UPS)?limit=50"
fi

call "logout" 200 -X POST "${auth[@]}" "$base$(const LOGOUT)"

[ "$failed" = 0 ] && echo "SMOKE PASSED" || echo "SMOKE FAILED"
exit "$failed"
