#!/usr/bin/env bash
# Live smoke test of Support routes (Foreman role, read-only):
# - Support Log CSV (support-log.csv)
# - Interviews due (interviews/due)
# - Worker past interviews (workers/{worker_id}/interviews)
# - Today's Word (today-word)
#
# Server address and paths are read from ApiConfig.kt. The PIN comes from WAKATTA_PIN
# and is never printed. The auth token is redacted.
#
#   WAKATTA_ROLE=foreman WAKATTA_PIN=<pin> ./live-smoke.sh
set -u

: "${WAKATTA_PIN:?set WAKATTA_PIN to the PIN of the Foreman account}"

here="$(cd "$(dirname "$0")" && pwd)"
cfg="$here/../../../core/network/src/main/kotlin/com/techtsunagu/wakatta/core/network/ApiConfig.kt"
const() { grep -E "const val $1 =" "$cfg" | head -1 | cut -d'"' -f2; }

base="${WAKATTA_BASE_URL:-$(const BASE_URL)}"
shop="${WAKATTA_SHOP:-SASEBO01}"
role="${WAKATTA_ROLE:-foreman}"
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

# 1. Support Log CSV
call "support-log.csv" 200 "${auth[@]}" "$base$(const SUPPORT_LOG_CSV)?from=2026-11-01&to=2026-11-30"

# 2. Interviews due
call "interviews/due" 200 "${auth[@]}" "$base$(const INTERVIEWS_DUE)"

# Find a worker from worker roster for past interviews check
worker_out="$(curl -sS --max-time 30 -G "$base$(const ROSTER)" --data-urlencode "code=$shop" --data-urlencode "role=worker")"
worker_id="$(printf '%s' "$worker_out" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)"

# 3. Worker past interviews
if [ -n "$worker_id" ]; then
  worker_iv_path="$(const WORKER_INTERVIEWS | sed "s/{worker_id}/$worker_id/")"
  call "worker interviews (worker $worker_id)" 200 "${auth[@]}" "$base$worker_iv_path"
fi

# 4. Today's Word
call "today-word" 200 "${auth[@]}" "$base$(const TODAY_WORD)"

call "logout" 200 -X POST "${auth[@]}" "$base$(const LOGOUT)"

[ "$failed" = 0 ] && echo "SUPPORT SMOKE PASSED" || echo "SUPPORT SMOKE FAILED"
exit "$failed"
