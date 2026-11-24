// Throwaway live smoke (not committed): runs the real adapter against the hosted API with demo accounts.
import { describe, expect, it, vi } from 'vitest'

const API =
  'https://wakatta-api.agreeablestone-840cfb57.japaneast.azurecontainerapps.io'
const KEYS = [
  'workers',
  'machines',
  'glossary',
  'notes',
  'confusions',
  'signOffs',
  'assignments',
  'briefings',
  'receipts',
  'nearMisses',
  'posters',
  'notices',
  'pairs',
  'asks',
  'meetups',
] as const

async function as(
  code: string,
  role: 'foreman' | 'worker' | 'poster',
  name: string,
  pin: string,
) {
  vi.resetModules()
  vi.stubEnv('VITE_USE_MOCK', 'false')
  vi.stubEnv('VITE_API_URL', API)
  localStorage.clear()
  const w = await import('./wakatta')
  const people = await w.roster(code, role)
  const me = people.find((p) => p.displayName.startsWith(name)) ?? people[0]!
  await w.signIn(me.id, pin)
  const out: Record<string, unknown> = {}
  for (const k of KEYS) out[k] = await w.list(k)
  return { w, out }
}

describe.skipIf(!process.env.LIVE)('live adapter', () => {
  it('foreman', async () => {
    const { out, w } = await as(
      'SASEBO01',
      'foreman',
      '山口 隆',
      process.env.F_PIN!,
    )
    for (const k of KEYS)
      console.log(
        'F',
        k,
        (out[k] as unknown[]).length,
        JSON.stringify((out[k] as unknown[])[0])?.slice(0, 160),
      )
    expect((out.workers as unknown[]).length).toBeGreaterThan(0)
    const acc = await w.listAccounts('worker')
    console.log('accounts', acc.length)
    const csv = await w.supportLogCsv('2026-01-01', '2026-11-24')
    console.log('csv', csv.size, (await csv.text()).slice(0, 120))
    const machines = out.machines as { id: string }[]
    console.log('panel', JSON.stringify(await w.machinePanel(machines[0]!.id)))
  }, 120_000)
  it('worker', async () => {
    const { out } = await as('SASEBO01', 'worker', 'Rahim', process.env.W_PIN!)
    for (const k of KEYS)
      console.log(
        'W',
        k,
        (out[k] as unknown[]).length,
        JSON.stringify((out[k] as unknown[])[0])?.slice(0, 160),
      )
    expect((out.notes as unknown[]).length).toBeGreaterThan(0)
  }, 120_000)
  it('poster', async () => {
    const { out, w } = await as('MINATO26', 'poster', '', process.env.P_PIN!)
    for (const k of KEYS)
      console.log(
        'P',
        k,
        (out[k] as unknown[]).length,
        JSON.stringify((out[k] as unknown[])[0])?.slice(0, 160),
      )
    const n = (out.notices as { id: string; status: string }[]).find(
      (x) => x.status === 'published',
    )
    if (n) console.log('reach', JSON.stringify(await w.noticeReach(n.id)))
  }, 120_000)
  it('senpai', async () => {
    vi.resetModules()
    vi.stubEnv('VITE_USE_MOCK', 'false')
    vi.stubEnv('VITE_API_URL', API)
    const w = await import('./wakatta')
    console.log(
      'senpai',
      JSON.stringify(await w.senpaiView('demo-tanaka-7f3a9c')).slice(0, 300),
    )
  }, 60_000)
})
