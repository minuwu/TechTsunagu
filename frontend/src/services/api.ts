import type { ErrorEnvelope, HealthEnvelope } from './generated'
import { mockHealth } from './mocks/health'

export const USE_MOCK = (import.meta.env.VITE_USE_MOCK ?? 'true') === 'true'
const BASE = import.meta.env.VITE_API_URL

export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly code: string,
    message: string,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${path}`, init)
  // A proxy error page (502/504) may not be JSON
  const body = (await res.json().catch(() => null)) as
    { success: true; data: T } | ErrorEnvelope | null
  if (!res.ok || !body?.success) {
    const error = body && !body.success ? body.error : undefined
    throw new ApiError(
      res.status,
      error?.code ?? 'UNKNOWN',
      error?.message ?? res.statusText,
    )
  }
  return body.data
}

export type Health = HealthEnvelope['data']

export async function getHealth(): Promise<Health> {
  if (USE_MOCK) return mockHealth
  return request<Health>('/api/health')
}
