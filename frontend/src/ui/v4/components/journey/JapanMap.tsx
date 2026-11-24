import { useEffect, useMemo, useRef, useState } from 'react'
import { geoGraticule10, geoOrthographic, geoPath } from 'd3-geo'
import { select } from 'd3-selection'
import 'd3-transition'
import { zoom, zoomIdentity, type ZoomTransform } from 'd3-zoom'
import type { Feature, Geometry } from 'geojson'
import { HOME_PREFECTURE, NAGASAKI, type PrefectureProps } from '@/features/map'
import { m } from '@/i18n/paraglide/messages'
import { cn } from '@/ui/v4/lib/utils'
import type { View } from './Earth'

type Pref = Feature<Geometry, PrefectureProps>

const MAX_ZOOM = 12
const FOCUS_FILL = 0.55 // Nagasaki fills about half the frame: close enough to read its coast
const FOCUS_MS = 1400

const calm = () => window.matchMedia('(prefers-reduced-motion: reduce)').matches

/** Japan on the same orthographic view the Earth ends on, so the hand-off lines up. One story, one choice
 *  (Hick's law): the other prefectures are plain land and only Nagasaki stands out (von Restorff effect).
 *  Nagasaki itself, the sea and two large buttons (Fitts's law) switch between all of Japan and Nagasaki.
 *  Code drives the zoom, so scroll, drag and pinch keep moving the page. */
export function JapanMap({
  prefectures,
  neighbours,
  view,
  size,
  zoomed,
  onZoom,
  homeName,
}: {
  prefectures: Pref[]
  neighbours: Feature<Geometry>[]
  view: View
  size: { w: number; h: number }
  /** true: Nagasaki fills the frame; false: all of Japan. */
  zoomed: boolean
  onZoom: (zoomed: boolean) => void
  homeName: string
}) {
  const svgRef = useRef<SVGSVGElement>(null)
  const zoomRef = useRef<ReturnType<typeof zoom<SVGSVGElement, unknown>>>(null)
  const [t, setT] = useState<ZoomTransform>(zoomIdentity)

  const path = useMemo(
    () =>
      geoPath(
        geoOrthographic()
          .rotate([-view.lon, -view.lat])
          .scale(view.scale)
          .translate([size.w / 2, size.h / 2]),
      ),
    [view, size],
  )
  const home = path.projection() as ReturnType<typeof geoOrthographic>
  const pin = home(NAGASAKI)
  const homePref = prefectures.find((p) => p.properties.id === HOME_PREFECTURE)

  useEffect(() => {
    const svg = svgRef.current
    if (!svg) return
    const z = zoom<SVGSVGElement, unknown>()
      .scaleExtent([1, MAX_ZOOM])
      .filter(() => false)
      .on('zoom', (e: { transform: ZoomTransform }) => setT(e.transform))
    zoomRef.current = z
    select(svg).call(z)
    return () => {
      select(svg).on('.zoom', null)
    }
  }, [])

  // Glide into Nagasaki (d3's smooth zoom path), or back out to all of Japan
  useEffect(() => {
    const svg = svgRef.current
    const z = zoomRef.current
    if (!svg || !z || !size.w) return
    let target = zoomIdentity
    if (zoomed && homePref) {
      const [[x0, y0], [x1, y1]] = path.bounds(homePref)
      const k = Math.min(
        MAX_ZOOM,
        FOCUS_FILL / Math.max((x1 - x0) / size.w, (y1 - y0) / size.h),
      )
      target = zoomIdentity
        .translate(size.w / 2, size.h / 2)
        .scale(Math.max(1, k))
        .translate(-(x0 + x1) / 2, -(y0 + y1) / 2)
    }
    select(svg)
      .transition()
      .duration(calm() ? 0 : FOCUS_MS)
      .call(z.transform, target)
  }, [zoomed, path, homePref, size])

  return (
    <div className="jm">
      <svg
        ref={svgRef}
        width={size.w}
        height={size.h}
        role="img"
        aria-label={m.journey_map_label()}
        className="block touch-pan-y"
      >
        <rect
          width={size.w}
          height={size.h}
          className="jm-sea"
          onClick={() => zoomed && onZoom(false)}
        />
        <g transform={t.toString()}>
          <path d={path(geoGraticule10()) ?? undefined} className="jm-grid" />
          {neighbours.map((c, i) => (
            <path key={i} d={path(c) ?? undefined} className="jm-land" />
          ))}
          {prefectures.map((p) =>
            p.properties.id === HOME_PREFECTURE ? null : (
              <path
                key={p.properties.id}
                d={path(p) ?? undefined}
                className="jm-pref"
              />
            ),
          )}
          {homePref && (
            <path
              d={path(homePref) ?? undefined}
              className={cn('jm-pref is-home', zoomed && 'is-selected')}
              onClick={() => onZoom(!zoomed)}
            />
          )}
        </g>
        {pin && (
          <g
            transform={`translate(${t.applyX(pin[0])} ${t.applyY(pin[1])})`}
            className="jm-pin"
          >
            <circle r={10} className="jm-pin-pulse" />
            <circle r={4} />
            <text x={14} y={5} className="jm-pin-name">
              {homeName}
            </text>
          </g>
        )}
      </svg>

      <div className="jm-zoom">
        <button
          type="button"
          onClick={() => onZoom(false)}
          aria-pressed={!zoomed}
        >
          {m.map_all()}
        </button>
        <button
          type="button"
          onClick={() => onZoom(true)}
          aria-pressed={zoomed}
        >
          {homeName}
        </button>
      </div>
    </div>
  )
}
