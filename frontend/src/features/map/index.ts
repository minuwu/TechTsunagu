import { useQuery } from '@tanstack/react-query'
import { getHighlights, getNeighbours, getPrefectures } from '@/services/geo'

export type {
  Bilingual,
  PrefectureHighlight,
  PrefectureProps,
} from '@/services/geo'

/** The demo shop's prefecture (the fictional 佐世保鋼材加工 is in Sasebo, Nagasaki) and its pin. */
export const HOME_PREFECTURE = 'JP-42'
export const SASEBO: [number, number] = [129.7239, 33.1799]
/** Nagasaki city, where the journey across the Earth ends (lon, lat). */
export const NAGASAKI: [number, number] = [129.8737, 32.7503]

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

const once = { staleTime: Infinity, gcTime: Infinity, retry: 1 } as const

/** The Nagasaki journey's map data: prefectures, neighbouring countries and each prefecture's highlight,
 *  loaded once per session. `failed` means the map cannot be drawn (the section then says so). */
export function useJourneyMap() {
  const prefectures = useQuery({
    queryKey: ['geo', 'prefectures'],
    queryFn: getPrefectures,
    ...once,
  })
  const neighbours = useQuery({
    queryKey: ['geo', 'neighbours'],
    queryFn: getNeighbours,
    ...once,
  })
  const highlights = useQuery({
    queryKey: ['geo', 'highlights'],
    queryFn: getHighlights,
    ...once,
  })
  return {
    prefectures: prefectures.data?.features ?? [],
    neighbours: neighbours.data?.features ?? [],
    highlights: highlights.data ?? [],
    ready: prefectures.isSuccess && highlights.isSuccess,
    failed: prefectures.isError || highlights.isError,
  }
}
