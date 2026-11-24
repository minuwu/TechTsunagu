import { useSyncExternalStore } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { ApiError, USE_MOCK } from '@/services/api'
import { healthQuery } from '@/services/queries/health'

export type HealthViewModel =
  | { state: 'loading'; isMock: boolean }
  | { state: 'error'; isMock: boolean; retry: () => void }
  | { state: 'up'; isMock: boolean }
  | { state: 'down'; isMock: boolean; status: string }

export function useHealth(): HealthViewModel {
  const query = useQuery(healthQuery())
  const isMock = USE_MOCK
  if (query.isPending) return { state: 'loading', isMock }
  if (query.isError)
    return { state: 'error', isMock, retry: () => void query.refetch() }
  const { status } = query.data
  return status === 'ok'
    ? { state: 'up', isMock }
    : { state: 'down', isMock, status }
}

const onOnlineChange = (fn: () => void) => {
  window.addEventListener('online', fn)
  window.addEventListener('offline', fn)
  return () => {
    window.removeEventListener('online', fn)
    window.removeEventListener('offline', fn)
  }
}

export type WriteFailure = 'busy' | 'limit' | 'offline' | 'failed'

// Above this many seconds a 429 is the daily AI allowance, not the per-minute one
const MINUTE_LIMIT = 120

/** Why a write failed, in the words a screen needs: AI busy for a minute, today's AI limit reached,
 *  no connection, or anything else. */
export function writeFailure(error: unknown, online: boolean): WriteFailure {
  if (error instanceof ApiError && error.status === 429)
    return (error.retryAfter ?? 0) > MINUTE_LIMIT ? 'limit' : 'busy'
  // fetch rejects with a TypeError when the network is down
  if (!online || error instanceof TypeError) return 'offline'
  return 'failed'
}

/** The latest failed write (any useMutation), so the kit can say so instead of failing silently.
 *  `id` changes with every new failure; null until something fails. */
export function useWriteFailure(): { id: number; kind: WriteFailure } | null {
  const cache = useQueryClient().getMutationCache()
  const latest = useSyncExternalStore(
    (fn) => cache.subscribe(fn),
    () =>
      cache
        .getAll()
        .filter((mu) => mu.state.status === 'error')
        .at(-1)?.mutationId ?? null,
    () => null,
  )
  if (latest === null) return null
  const failed = cache.getAll().find((mu) => mu.mutationId === latest)
  return {
    id: latest,
    kind: writeFailure(failed?.state.error, navigator.onLine),
  }
}

export type DataStatus = 'ok' | 'error' | 'offline'

/** Whether the shop's data is reachable: offline (the browser says so), error (a read failed after its
 *  retries), or ok. Screens must not present stale or missing data as fact while this is not ok. */
export function useDataStatus(): { status: DataStatus; retry: () => void } {
  const client = useQueryClient()
  const cache = client.getQueryCache()
  const failing = useSyncExternalStore(
    (fn) => cache.subscribe(fn),
    () =>
      cache
        .findAll({ queryKey: ['wakatta'] })
        .some((q) => q.state.status === 'error'),
    () => false,
  )
  const online = useSyncExternalStore(
    onOnlineChange,
    () => navigator.onLine,
    () => true,
  )
  return {
    status: !online ? 'offline' : failing ? 'error' : 'ok',
    retry: () => void client.refetchQueries({ queryKey: ['wakatta'] }),
  }
}
