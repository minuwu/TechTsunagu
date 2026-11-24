import type { EnvelopeHealth } from './generated'

// Every error answer (backend app/api/envelope.py ErrorEnvelope). Routes do not declare their error
// responses, so this shape is not in docs/openapi.yaml and is written here, once.
export interface ErrorEnvelope {
  success: false
  error: { code: string; message: string; details?: Record<string, unknown> }
}
import { mockHealth } from './mocks/health'

export const USE_MOCK = (import.meta.env.VITE_USE_MOCK ?? 'true') === 'true'
const BASE = import.meta.env.VITE_API_URL

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly code: string,
    message: string,
    /** Seconds from the Retry-After header (a 429 says when to try again) */
    public readonly retryAfter?: number,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

/** The signed-in account (POST /api/auth/login). One per browser: signing in as another role replaces it. */
export interface Session {
  token: string
  expiresAt: string
  mustChangePin: boolean
  user: {
    id: string
    displayName: string
    role: 'foreman' | 'worker' | 'poster'
    language: string
    shopId: string | null
    districtId: string | null
    shopCode?: string | null
  }
}

export const SESSION_KEY = 'wakatta.auth'
// Same event lib/persisted listens to, so every usePersisted(SESSION_KEY) re-renders
const PERSISTED_EVENT = 'persisted-change'

export function getSession(): Session | null {
  if (typeof localStorage === 'undefined') return null
  try {
    const s = JSON.parse(
      localStorage.getItem(SESSION_KEY) ?? 'null',
    ) as Session | null
    return s && Date.parse(s.expiresAt) > Date.now() ? s : null
  } catch {
    return null
  }
}

export function setSession(session: Session | null) {
  if (session) localStorage.setItem(SESSION_KEY, JSON.stringify(session))
  else localStorage.removeItem(SESSION_KEY)
  window.dispatchEvent(new Event(PERSISTED_EVENT))
}

/** Headers every real call carries: the Bearer token, and an Idempotency-Key on writes that need one. */
export function authHeaders(idempotent = false): Record<string, string> {
  const token = getSession()?.token
  return {
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...(idempotent ? { 'Idempotency-Key': crypto.randomUUID() } : {}),
  }
}

export async function request<T>(
  path: string,
  init?: RequestInit,
  auth = true,
): Promise<T> {
  const headers: Record<string, string> = {
    ...authHeaders(),
    ...(init?.headers as Record<string, string> | undefined),
  }
  // Link-token routes (Senpai): the signed-in account's Bearer must not ride along
  if (!auth) delete headers.Authorization
  const res = await fetch(`${BASE}${path}`, { ...init, headers })
  // The token expired or was revoked: drop it so the screen asks to sign in again
  if (res.status === 401 && headers.Authorization) setSession(null)
  // A proxy error page (502/504) may not be JSON
  const body = (await res.json().catch(() => null)) as
    { success: true; data: T } | ErrorEnvelope | null
  if (!res.ok || !body?.success) {
    const error = body && !body.success ? body.error : undefined
    const wait = Number(res.headers.get('Retry-After'))
    throw new ApiError(
      res.status,
      error?.code ?? 'UNKNOWN',
      error?.message ?? res.statusText,
      wait > 0 ? wait : undefined,
    )
  }
  return body.data
}

/** Every page of a list route (`{items, nextCursor}`). */
export async function requestAll<T>(path: string): Promise<T[]> {
  const items: T[] = []
  let cursor: string | null = null
  do {
    const sep = path.includes('?') ? '&' : '?'
    const page: { items: T[]; nextCursor: string | null } = await request(
      `${path}${sep}limit=200${cursor ? `&cursor=${encodeURIComponent(cursor)}` : ''}`,
    )
    items.push(...page.items)
    cursor = page.nextCursor
  } while (cursor)
  return items
}

/** A file behind the Bearer token (`/api/blobs/{id}`, CSV exports): `<img src>` and links cannot send it. */
export async function requestBlob(path: string): Promise<Blob> {
  const res = await fetch(`${BASE}${path}`, { headers: authHeaders() })
  if (!res.ok) throw new ApiError(res.status, 'UNKNOWN', res.statusText)
  return res.blob()
}

export type Health = EnvelopeHealth['data']

export async function getHealth(): Promise<Health> {
  if (USE_MOCK) return mockHealth
  return request<Health>('/api/health')
}
