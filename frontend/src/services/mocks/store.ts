import { seed, type Db } from './seed'

// In-browser mock database for the demo (USE_MOCK). Persists in localStorage so the
// Machine Panel in another tab sees what the Worker's phone did (they poll).
// shortcut: no locking between tabs; last write wins. Fine for a scripted demo.

const KEY = 'wakatta.demo.v1'
let memory: Db | null = null // server render and private-mode fallback

const hasStorage = () => typeof localStorage !== 'undefined'

export function readDb(): Db {
  if (hasStorage()) {
    const raw = localStorage.getItem(KEY)
    if (raw) return JSON.parse(raw) as Db
  }
  if (!memory) memory = seed(new Date())
  return memory
}

export function writeDb(db: Db) {
  memory = db
  if (hasStorage()) localStorage.setItem(KEY, JSON.stringify(db))
}

/** Read-modify-write in one step. */
export function update<T>(fn: (db: Db) => T): T {
  const db = readDb()
  const out = fn(db)
  writeDb(db)
  return out
}

export function resetDb() {
  memory = null
  if (hasStorage()) localStorage.removeItem(KEY)
  writeDb(seed(new Date()))
}

export const wait = (ms: number) => new Promise((r) => setTimeout(r, ms))
