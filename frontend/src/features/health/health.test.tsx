import type { ReactNode } from 'react'
import { describe, expect, it, vi } from 'vitest'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { renderHook, waitFor } from '@testing-library/react'
import { useHealth } from '.'

const getHealth = vi.hoisted(() => vi.fn())
vi.mock('@/services/api', () => ({ USE_MOCK: true, getHealth }))

function wrapper({ children }: { children: ReactNode }) {
  const client = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  return <QueryClientProvider client={client}>{children}</QueryClientProvider>
}

// Tested without rendering any kit (A-08)
describe('useHealth', () => {
  it('goes from loading to up', async () => {
    getHealth.mockResolvedValue({ status: 'ok' })
    const { result } = renderHook(() => useHealth(), { wrapper })
    expect(result.current.state).toBe('loading')
    await waitFor(() =>
      expect(result.current).toEqual({ state: 'up', isMock: true }),
    )
  })

  it('reports down with the status the API sent', async () => {
    getHealth.mockResolvedValue({ status: 'degraded' })
    const { result } = renderHook(() => useHealth(), { wrapper })
    await waitFor(() =>
      expect(result.current).toEqual({
        state: 'down',
        isMock: true,
        status: 'degraded',
      }),
    )
  })

  it('reports error and recovers after retry', async () => {
    getHealth
      .mockRejectedValueOnce(new Error('network'))
      .mockResolvedValue({ status: 'ok' })
    const { result } = renderHook(() => useHealth(), { wrapper })
    await waitFor(() => expect(result.current.state).toBe('error'))
    if (result.current.state === 'error') result.current.retry()
    await waitFor(() => expect(result.current.state).toBe('up'))
  })
})
