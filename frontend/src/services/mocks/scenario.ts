import { ApiError } from '../api'

/** Demo scenarios for the mock store (USE_MOCK only): show loading, empty and error states on cue,
 *  deterministically, without touching the real API path. Per browser tab (sessionStorage). */
export const SCENARIOS = ['live', 'loading', 'empty', 'error'] as const
export type Scenario = (typeof SCENARIOS)[number]

const KEY = 'wakatta.demoScenario'
const SHOWN = 'wakatta.demoControls'
const listeners = new Set<() => void>()

export const isScenario = (v: unknown): v is Scenario =>
  SCENARIOS.includes(v as Scenario)

function read(key: string): string | null {
  try {
    return typeof window === 'undefined' ? null : sessionStorage.getItem(key)
  } catch {
    return null // storage blocked: behave as live
  }
}

export function getScenario(): Scenario {
  const v = read(KEY)
  return isScenario(v) ? v : 'live'
}

/** The switcher stays visible for the tab once a presenter has used `?demo=` (or in development). */
export function controlsShown(): boolean {
  return read(SHOWN) === '1'
}

export function setScenario(next: Scenario): void {
  try {
    sessionStorage.setItem(KEY, next)
    sessionStorage.setItem(SHOWN, '1')
  } catch {
    // storage blocked: the scenario cannot persist, live data stays
  }
  listeners.forEach((fn) => fn())
}

export function subscribeScenario(fn: () => void): () => void {
  listeners.add(fn)
  return () => listeners.delete(fn)
}

/** Applied to every mock read: the data as stored, or the scenario's stand-in. */
export function applyScenario<T extends unknown[]>(data: T): Promise<T> {
  switch (getScenario()) {
    case 'loading':
      return new Promise<T>(() => {}) // never settles: the screen stays in its loading state
    case 'empty':
      return Promise.resolve([] as unknown as T)
    case 'error':
      return Promise.reject(
        new ApiError(
          503,
          'DEMO_UNAVAILABLE',
          'Demo scenario: server unavailable',
        ),
      )
    default:
      return Promise.resolve(data)
  }
}
