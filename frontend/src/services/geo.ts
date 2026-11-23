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
