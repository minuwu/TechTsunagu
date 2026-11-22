import {
  queryOptions,
  useQuery,
  type UseQueryResult,
} from '@tanstack/react-query'
import { list, type Collection, type Db } from '../wakatta'

/** One query per collection; `poll` in ms (Machine Panel 2 s, boards 5 s). */
export const listQuery = <K extends Collection>(key: K, poll?: number) =>
  queryOptions({
    queryKey: ['wakatta', key],
    queryFn: (): Promise<Db[K]> => list(key),
    staleTime: 0,
    refetchInterval: poll ?? false,
  })

/** The collection, or an empty list while it loads. */
export function useList<K extends Collection>(key: K, poll?: number): Db[K] {
  return useQuery(listQuery(key, poll)).data ?? ([] as unknown as Db[K])
}

/** The query itself, for loading and not-found states. */
export function useListQuery<K extends Collection>(key: K, poll?: number) {
  return useQuery(listQuery(key, poll)) as UseQueryResult<Db[K]>
}
