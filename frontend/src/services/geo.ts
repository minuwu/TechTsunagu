import type { FeatureCollection, Geometry } from 'geojson'
import { feature } from 'topojson-client'
import type { GeometryCollection, Topology } from 'topojson-specification'

/** A prefecture: ISO 3166-2 code (JP-42), Japanese and English names. */
export interface PrefectureProps {
  id: string
  ja: string
  en: string
}

// Static file served by the frontend itself (public/geo/README.md: Natural Earth, public domain)
const URL = '/geo/japan-prefectures.topo.json'

/** Japan's 47 prefectures as GeoJSON features. */
export async function getPrefectures(): Promise<
  FeatureCollection<Geometry, PrefectureProps>
> {
  const res = await fetch(URL)
  if (!res.ok) throw new Error(`map data: HTTP ${res.status}`)
  const topo = (await res.json()) as Topology<{
    prefectures: GeometryCollection<PrefectureProps>
  }>
  return feature(topo, topo.objects.prefectures) as FeatureCollection<
    Geometry,
    PrefectureProps
  >
}

/** The world's countries (Natural Earth 1:110m, public domain), for the globe the map opens on. */
export async function getCountries(): Promise<FeatureCollection<Geometry>> {
  const res = await fetch('/geo/world-countries-110m.topo.json')
  if (!res.ok) throw new Error(`world data: HTTP ${res.status}`)
  const topo = (await res.json()) as Topology<{ countries: GeometryCollection }>
  return feature(topo, topo.objects.countries) as FeatureCollection<Geometry>
}

/** A prefecture's famous place and its copyright-free photo (public/geo/README.md, public/media/README.md). */
export interface PrefectureHighlight {
  id: string
  ja: string
  en: string
  image: string | null
  license?: string
  artist?: string
  page?: string
}

export async function getHighlights(): Promise<PrefectureHighlight[]> {
  const res = await fetch('/geo/prefecture-highlights.json')
  if (!res.ok) throw new Error(`highlights: HTTP ${res.status}`)
  return (await res.json()) as PrefectureHighlight[]
}
