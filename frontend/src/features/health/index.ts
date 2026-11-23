import { useSyncExternalStore } from 'react'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { USE_MOCK } from '@/services/api'
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
