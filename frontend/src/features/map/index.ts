import { useQuery } from '@tanstack/react-query'
import { getCountries, getHighlights, getPrefectures } from '@/services/geo'

export type { PrefectureHighlight, PrefectureProps } from '@/services/geo'

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

const once = { staleTime: Infinity, gcTime: Infinity, retry: 1 } as const

/** The globe map: countries, prefectures and each prefecture's highlight, loaded once per session.
 *  Empty while loading or offline; the map is decorative there and the page still works. */
export function useGlobeMap() {
  const countries = useQuery({
    queryKey: ['geo', 'countries'],
    queryFn: getCountries,
    ...once,
  })
  const prefectures = useQuery({
    queryKey: ['geo', 'prefectures'],
    queryFn: getPrefectures,
    ...once,
  })
  const highlights = useQuery({
    queryKey: ['geo', 'highlights'],
    queryFn: getHighlights,
    ...once,
  })
  return {
    countries: countries.data?.features ?? [],
    prefectures: prefectures.data?.features ?? [],
    highlights: highlights.data ?? [],
    ready: countries.isSuccess && prefectures.isSuccess,
    failed: countries.isError || prefectures.isError,
  }
}
