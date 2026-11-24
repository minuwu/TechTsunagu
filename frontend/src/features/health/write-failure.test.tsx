import type { ReactNode } from 'react'
import { describe, expect, it } from 'vitest'
import {
  QueryClient,
  QueryClientProvider,
  useMutation,
} from '@tanstack/react-query'
import { act, renderHook, waitFor } from '@testing-library/react'
import { ApiError } from '@/services/api'
import { useWriteFailure, writeFailure } from '.'

describe('writeFailure', () => {
  it('reads a per-minute 429 as busy and a daily one as the limit', () => {
    expect(writeFailure(new ApiError(429, 'RATE_LIMITED', '', 60), true)).toBe(
      'busy',
    )
    expect(
      writeFailure(new ApiError(429, 'RATE_LIMITED', '', 3600), true),
    ).toBe('limit')
  })

  it('reads a network error or a lost connection as offline', () => {
    expect(writeFailure(new TypeError('Failed to fetch'), true)).toBe('offline')
    expect(writeFailure(new ApiError(500, 'INTERNAL_ERROR', ''), false)).toBe(
      'offline',
    )
  })

  it('reads anything else as failed', () => {
    expect(writeFailure(new ApiError(500, 'INTERNAL_ERROR', ''), true)).toBe(
      'failed',
    )
  })
})

// Tested without rendering any kit (A-08)
describe('useWriteFailure', () => {
  it('reports each failed write once, with a new id', async () => {
    const client = new QueryClient()
    const wrapper = ({ children }: { children: ReactNode }) => (
      <QueryClientProvider client={client}>{children}</QueryClientProvider>
    )
    const { result } = renderHook(
      () => ({
        failure: useWriteFailure(),
        save: useMutation({
          mutationFn: () =>
            Promise.reject(new ApiError(429, 'RATE_LIMITED', '', 60)),
        }),
      }),
      { wrapper },
    )
    expect(result.current.failure).toBeNull()

    act(() => result.current.save.mutate())
    await waitFor(() => expect(result.current.failure?.kind).toBe('busy'))
    const first = result.current.failure?.id

    act(() => result.current.save.mutate())
    await waitFor(() => expect(result.current.failure?.id).not.toBe(first))
  })
})
