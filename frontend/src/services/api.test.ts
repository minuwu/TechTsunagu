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

describe('services/api (session)', () => {
  beforeEach(() => {
    vi.unstubAllEnvs()
    localStorage.clear()
  })
  const session = {
    token: 'tok',
    expiresAt: '2999-01-01T00:00:00Z',
    mustChangePin: false,
    user: {
      id: '3',
      displayName: 'Rahim',
      role: 'worker' as const,
      language: 'bn',
      shopId: '1',
      districtId: '1',
    },
  }

  it('sends the Bearer token, and clears it when the API answers 401', async () => {
    let auth: string | null = null
    server.use(
      http.get(`${API}/api/health`, ({ request }) => {
        auth = request.headers.get('Authorization')
        return HttpResponse.json(
          { success: false, error: { code: 'UNAUTHORIZED', message: 'no' } },
          { status: 401 },
        )
      }),
    )
    const { getHealth, getSession, setSession } = await liveApi()
    setSession(session)
    await expect(getHealth()).rejects.toMatchObject({ status: 401 })
    expect(auth).toBe('Bearer tok')
    expect(getSession()).toBeNull()
  })

  it('treats an expired session as signed out', async () => {
    const { getSession, setSession } = await liveApi()
    setSession({ ...session, expiresAt: '2000-01-01T00:00:00Z' })
    expect(getSession()).toBeNull()
  })

  it('follows nextCursor until the list ends', async () => {
    server.use(
      http.get(`${API}/api/workers`, ({ request }) => {
        const cursor = new URL(request.url).searchParams.get('cursor')
        return HttpResponse.json({
          success: true,
          data: cursor
            ? { items: [2], nextCursor: null }
            : { items: [1], nextCursor: 'c1' },
        })
      }),
    )
    const { requestAll } = await liveApi()
    await expect(requestAll<number>('/api/workers')).resolves.toEqual([1, 2])
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
