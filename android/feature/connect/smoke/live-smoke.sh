#!/usr/bin/env bash
# Live smoke test of Connect routes (Worker role, read-only):
# - My QR (me/qr)
# - Contacts list (contacts)
# - Talk Threads list (talk-threads)
# - Talk Entries list (talk-threads/{thread_id}/entries)
#
# Server address is read from ApiConfig.kt. The PIN comes from WAKATTA_PIN
# and is never printed. The auth token is redacted.
#
#   WAKATTA_PIN=<pin> ./live-smoke.sh
set -u

: "${WAKATTA_PIN:?set WAKATTA_PIN to the PIN of the Worker account}"

here="$(cd "$(dirname "$0")" && pwd)"
cfg="$here/../../../core/network/src/main/kotlin/com/techtsunagu/wakatta/core/network/ApiConfig.kt"
const() { grep -E "const val $1 =" "$cfg" | head -1 | cut -d'"' -f2; }

base="${WAKATTA_BASE_URL:-$(const BASE_URL)}"
shop="${WAKATTA_SHOP:-SASEBO01}"
role="${WAKATTA_ROLE:-worker}"
failed=0

redact() { sed -E 's/"token":"[^"]*"/"token":"<redacted>"/g'; }

call() {
  local label="$1" want="$2"; shift 2
  local out status
  out="$(curl -sS --max-time 30 -w '\n%{http_code}' "$@")" || { echo "FAIL $label: curl error"; failed=1; body=""; return; }
  status="${out##*$'\n'}"
  body="${out%$'\n'*}"
  if [ "$status" = "$want" ]; then echo "OK   $label -> $status"; else echo "FAIL $label -> $status (wanted $want)"; failed=1; fi
  echo "     $(printf '%s' "$body" | redact | head -c 300)"
}

call "roster ($role, $shop)" 200 -G "$base$(const ROSTER)" --data-urlencode "code=$shop" --data-urlencode "role=$role"
user_id="${WAKATTA_USER_ID:-$(printf '%s' "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)}"
[ -n "$user_id" ] || { echo "FAIL no user id in roster"; exit 1; }

call "login (user $user_id)" 200 -H 'Content-Type: application/json' \
  -d "{\"userId\":\"$user_id\",\"pin\":\"$WAKATTA_PIN\"}" "$base$(const LOGIN)"
token="$(printf '%s' "$body" | grep -o '"token":"[^"]*"' | head -1 | cut -d'"' -f4)"
[ -n "$token" ] || { echo "FAIL no token in login response"; exit 1; }
auth=(-H "Authorization: Bearer $token")

# 1. My QR (read/fetch existing without rotating)
call "me/qr" 200 "${auth[@]}" -H 'Content-Type: application/json' \
  -d '{"rotate":false}' "$base$(const ME_QR)"

# 2. Contacts list
call "contacts" 200 "${auth[@]}" "$base$(const CONTACTS)?limit=50"

# 3. Talk Threads list
call "talk-threads" 200 "${auth[@]}" "$base$(const TALK_THREADS)?limit=50"
talk_id="$(printf '%s' "$body" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)"
if [ -n "$talk_id" ]; then
  entries_path="$(const TALK_ENTRIES | sed "s/{thread_id}/$talk_id/")"
  call "talk-entries (thread $talk_id)" 200 "${auth[@]}" "$base$entries_path?limit=50"
fi

call "logout" 200 -X POST "${auth[@]}" "$base$(const LOGOUT)"

[ "$failed" = 0 ] && echo "CONNECT SMOKE PASSED" || echo "CONNECT SMOKE FAILED"
exit "$failed"
