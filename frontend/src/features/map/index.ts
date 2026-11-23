import { useQuery } from '@tanstack/react-query'
import { getPrefectures } from '@/services/geo'

export type { PrefectureProps } from '@/services/geo'

/** The demo shop's prefecture (the fictional 佐世保鋼材加工 is in Sasebo, Nagasaki) and its pin. */
export const HOME_PREFECTURE = 'JP-42'
export const SASEBO: [number, number] = [129.7239, 33.1799]

/** Japan's prefectures for the map: loaded once per session; an empty list while loading or offline. */
export function useJapanMap() {
  const q = useQuery({
    queryKey: ['geo', 'prefectures'],
    queryFn: getPrefectures,
    staleTime: Infinity,
    gcTime: Infinity,
    retry: 1,
  })
  return { prefectures: q.data?.features ?? [], ready: q.isSuccess }
}
