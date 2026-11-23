import type { EnvelopeHealth } from '../generated'

// Mirrors the example in docs/openapi.yaml GET /api/health
export const mockHealth: EnvelopeHealth['data'] = { status: 'ok' }
