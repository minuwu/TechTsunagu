#!/usr/bin/env bash
# Live smoke test of Insights routes (read-only):
# - Foreman role: understanding, heatmap, effort, skill-matrix, follow-ups, glossary
# - Worker role: workers/me/learning
#
# Server address and paths are read from ApiConfig.kt. The PIN comes from WAKATTA_PIN
# and is never printed. The auth token is redacted.
#
#   WAKATTA_ROLE=foreman WAKATTA_PIN=<pin> ./live-smoke.sh
#   WAKATTA_ROLE=worker WAKATTA_PIN=<pin> ./live-smoke.sh
set -u

: "${WAKATTA_PIN:?set WAKATTA_PIN to the PIN of the account}"

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

if [ "$role" = "foreman" ]; then
  # 1. Understanding analytics
  call "analytics/understanding" 200 "${auth[@]}" "$base$(const ANALYTICS_UNDERSTANDING)?days=30"

  # 2. Confusion heatmap
  call "heatmap" 200 "${auth[@]}" "$base$(const HEATMAP)"

  # 3. Effort metrics
  call "metrics/effort" 200 "${auth[@]}" "$base$(const METRICS_EFFORT)"

  # 4. Skill Matrix
  call "skill-matrix" 200 "${auth[@]}" "$base$(const SKILL_MATRIX)"

  # 5. Follow-ups
  call "follow-ups" 200 "${auth[@]}" "$base$(const FOLLOW_UPS)?limit=200"

  # 6. Glossary
  call "glossary" 200 "${auth[@]}" "$base$(const GLOSSARY)"
elif [ "$role" = "worker" ]; then
  # 1. Worker own learning history
  call "workers/me/learning" 200 "${auth[@]}" "$base$(const MY_LEARNING)?limit=200"
else
  echo "UNKNOWN role: $role"
  failed=1
fi

call "logout" 200 -X POST "${auth[@]}" "$base$(const LOGOUT)"

[ "$failed" = 0 ] && echo "INSIGHTS SMOKE PASSED ($role)" || echo "INSIGHTS SMOKE FAILED ($role)"
exit "$failed"
