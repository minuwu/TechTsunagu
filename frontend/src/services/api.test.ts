import {
  afterAll,
  afterEach,
  beforeAll,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from 'vitest'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'

const API = 'http://api.test'
const server = setupServer()

beforeAll(() => server.listen({ onUnhandledFrame: 'error' }))
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

// USE_MOCK and the base URL are read when api.ts loads, so stub env first, then import fresh
async function liveApi() {
  vi.resetModules()
  vi.stubEnv('VITE_USE_MOCK', 'false')
  vi.stubEnv('VITE_API_URL', API)
  return import('./api')
}

describe('services/api (live mode)', () => {
  beforeEach(() => vi.unstubAllEnvs())

  it('returns data from the success envelope', async () => {
    server.use(
      http.get(`${API}/api/health`, () =>
        HttpResponse.json({
          success: true,
          data: { status: 'ok' },
          message: 'ok',
        }),
      ),
    )
    const { getHealth } = await liveApi()
    await expect(getHealth()).resolves.toEqual({ status: 'ok' })
  })

  it('throws ApiError with the code from the error envelope', async () => {
    server.use(
      http.get(`${API}/api/health`, () =>
        HttpResponse.json(
          {
            success: false,
            error: { code: 'SERVICE_DOWN', message: 'DB offline', details: {} },
          },
          { status: 503 },
        ),
      ),
    )
    const { getHealth, ApiError } = await liveApi()
    const error = await getHealth().catch((e: unknown) => e)
    expect(error).toBeInstanceOf(ApiError)
    expect(error).toMatchObject({
      status: 503,
      code: 'SERVICE_DOWN',
      message: 'DB offline',
    })
  })

  it('keeps Retry-After on a 429, so the UI can say when to try again', async () => {
    server.use(
      http.get(`${API}/api/health`, () =>
        HttpResponse.json(
          {
            success: false,
            error: { code: 'RATE_LIMITED', message: 'Slow down', details: {} },
          },
          { status: 429, headers: { 'Retry-After': '60' } },
        ),
      ),
    )
    const { getHealth } = await liveApi()
    await expect(getHealth()).rejects.toMatchObject({
      status: 429,
      code: 'RATE_LIMITED',
      retryAfter: 60,
    })
  })

  it('throws ApiError when a proxy returns a non-JSON error page', async () => {
    server.use(
      http.get(
        `${API}/api/health`,
        () => new HttpResponse('<h1>Bad Gateway</h1>', { status: 502 }),
      ),
    )
    const { getHealth } = await liveApi()
    await expect(getHealth()).rejects.toMatchObject({
      status: 502,
      code: 'UNKNOWN',
    })
  })
})

describe('services/api (mock mode)', () => {
  it('returns the fixture without calling the network', async () => {
    vi.resetModules()
    vi.stubEnv('VITE_USE_MOCK', 'true')
    const { getHealth } = await import('./api')
    await expect(getHealth()).resolves.toEqual({ status: 'ok' })
  })
})
