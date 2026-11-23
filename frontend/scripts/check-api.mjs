// Checks that the frontend can talk to a Wakatta? API (docs/FRONTEND_API_HANDOVER.md).
// usage: npm run check:api [-- <api base url> [<frontend origin>]]
//   defaults: VITE_API_URL from the environment, else the hosted API; origin http://localhost:3000
// It reads only, except one sign-in with the documented demo Worker, which it signs out again.
import { readFileSync } from 'node:fs'

const HOSTED =
  'https://wakatta-api.agreeablestone-840cfb57.japaneast.azurecontainerapps.io'
const API = (process.argv[2] || process.env.VITE_API_URL || HOSTED).replace(
  /\/$/,
  '',
)
const ORIGIN = process.argv[3] || 'http://localhost:3000'
// Demo data only (docs/FRONTEND_API_HANDOVER.md §5); override with CHECK_SHOP / CHECK_PIN
const SHOP = process.env.CHECK_SHOP || 'SASEBO01'
const PIN = process.env.CHECK_PIN || '2468'

let failed = 0
const ok = (name, detail = '') =>
  console.log(`  ok   ${name}${detail ? `: ${detail}` : ''}`)
const bad = (name, detail) => {
  failed++
  console.log(`  FAIL ${name}: ${detail}`)
}
async function call(path, init = {}) {
  const res = await fetch(API + path, {
    ...init,
    signal: AbortSignal.timeout(20_000),
  })
  const text = await res.text()
  let body = null
  try {
    body = JSON.parse(text)
  } catch {
    // not JSON (e.g. a proxy error page)
  }
  return { res, body }
}

console.log(`API ${API}\nfrontend origin ${ORIGIN}\n`)

// 1. Up, and the database behind it
for (const path of ['/api/health', '/api/health/ready']) {
  try {
    const { res, body } = await call(path)
    if (res.ok && body?.success) ok(path, body.data.status)
    else bad(path, `HTTP ${res.status}`)
  } catch (e) {
    bad(path, e.cause?.code || e.message)
  }
}

// 2. CORS: the browser on ORIGIN may call the API with the headers the app sends
try {
  const res = await fetch(API + '/api/auth/me', {
    method: 'OPTIONS',
    headers: {
      Origin: ORIGIN,
      'Access-Control-Request-Method': 'GET',
      'Access-Control-Request-Headers':
        'authorization,content-type,idempotency-key',
    },
  })
  const allowed = res.headers.get('access-control-allow-origin')
  if (allowed === ORIGIN) ok('CORS preflight', `${ORIGIN} allowed`)
  else
    bad(
      'CORS preflight',
      `${ORIGIN} not allowed (HTTP ${res.status}); add it with backend/deploy/set-cors.ps1`,
    )
} catch (e) {
  bad('CORS preflight', e.message)
}

// 3. The live contract is the one the frontend's types were generated from
try {
  const { body: live } = await call('/api/openapi.json')
  const local = JSON.parse(
    readFileSync(new URL('../../docs/openapi.yaml', import.meta.url), 'utf8'),
  )
  const lp = Object.keys(live.paths)
  const rp = Object.keys(local.paths)
  const missing = rp.filter((p) => !lp.includes(p))
  const extra = lp.filter((p) => !rp.includes(p))
  if (!missing.length && !extra.length)
    ok('contract', `${lp.length} paths, same as docs/openapi.yaml`)
  else
    bad(
      'contract',
      `deployed API differs from docs/openapi.yaml: not deployed yet ${JSON.stringify(missing)}, not in the repo ${JSON.stringify(extra)}`,
    )
} catch (e) {
  bad('contract', e.message)
}

// 4. Sign in as the demo Worker, read, sign out (the app's real flow, XREQ-001)
try {
  const roster = await call(`/api/auth/roster?code=${SHOP}&role=worker`)
  const who = roster.body?.data?.[0]
  if (!who) throw new Error(`roster for ${SHOP}: HTTP ${roster.res.status}`)
  ok('roster', `${roster.body.data.length} Workers in ${SHOP}`)
  const login = await call('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId: who.id, pin: PIN }),
  })
  const token = login.body?.data?.token
  if (!token)
    throw new Error(
      `login: HTTP ${login.res.status} ${login.body?.error?.code ?? ''}`,
    )
  ok(
    'login',
    `${login.body.data.user.displayName}, role ${login.body.data.user.role}`,
  )
  const auth = { Authorization: `Bearer ${token}` }
  const me = await call('/api/auth/me', { headers: auth })
  if (me.res.ok) ok('me (Bearer token)')
  else bad('me', `HTTP ${me.res.status}`)
  const notices = await call('/api/notices', { headers: auth })
  if (Array.isArray(notices.body?.data?.items))
    ok(
      'list shape',
      `GET /api/notices → {items: ${notices.body.data.items.length}, nextCursor}`,
    )
  else bad('list shape', `HTTP ${notices.res.status}, no items array`)
  const out = await call('/api/auth/logout', { method: 'POST', headers: auth })
  if (out.res.ok) ok('logout', 'session ended')
  else bad('logout', `HTTP ${out.res.status}`)
} catch (e) {
  bad('sign-in round trip', e.message)
}

console.log(failed ? `\n${failed} check(s) failed` : '\nall checks passed')
process.exit(failed ? 1 : 0)
