import type { HealthEnvelope } from '../generated'

// Mirrors the example in docs/openapi.yaml GET /api/health
export const mockHealth: HealthEnvelope['data'] = { status: 'ok' }
