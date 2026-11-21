import { useQuery } from '@tanstack/react-query'
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
