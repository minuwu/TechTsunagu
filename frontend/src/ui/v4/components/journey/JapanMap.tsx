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

const MAX_ZOOM = 8
const FOCUS_FILL = 0.38 // a chosen prefecture fills about a third of the frame; its neighbours stay in view
const FOCUS_MS = 1000

const calm = () => window.matchMedia('(prefers-reduced-motion: reduce)').matches

/** All 47 prefectures on the same orthographic view the Earth ends on, so the hand-off lines up.
 *  Click or tap a prefecture to choose it; Ctrl/⌘ + scroll, pinch or the buttons zoom; drag moves.
 *  Plain scroll keeps scrolling the page. Keyboard users choose from the list in the panel. */
export function JapanMap({
  prefectures,
  neighbours,
  view,
  size,
  selected,
  focus,
  onSelect,
  onAll,
  name,
}: {
  prefectures: Pref[]
  neighbours: Feature<Geometry>[]
  view: View
  size: { w: number; h: number }
  selected: string
  /** The prefecture to zoom to; null shows all of Japan. */
  focus: string | null
  onSelect: (id: string) => void
  /** Back to all of Japan. */
  onAll: () => void
  name: (p: PrefectureProps) => string
}) {
  const svgRef = useRef<SVGSVGElement>(null)
  const zoomRef = useRef<ReturnType<typeof zoom<SVGSVGElement, unknown>>>(null)
  const [t, setT] = useState<ZoomTransform>(zoomIdentity)
  const [hover, setHover] = useState<{ p: Pref; x: number; y: number } | null>(
    null,
  )
  const [hint, setHint] = useState(false)
  const hintTimer = useRef(0)

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

  useEffect(() => {
    const svg = svgRef.current
    if (!svg) return
    const z = zoom<SVGSVGElement, unknown>()
      .scaleExtent([0.8, MAX_ZOOM])
      .filter((e: Event) => {
        if (e.type === 'wheel')
          return (e as WheelEvent).ctrlKey || (e as WheelEvent).metaKey
        if (e.type === 'touchstart') return (e as TouchEvent).touches.length > 1
        return !(e as MouseEvent).button
      })
      .on('zoom', (e: { transform: ZoomTransform }) => setT(e.transform))
    zoomRef.current = z
    select(svg).call(z)
    return () => {
      select(svg).on('.zoom', null)
    }
  }, [])

  // Glide to the chosen prefecture (d3's smooth zoom path), or back to all of Japan
  useEffect(() => {
    const svg = svgRef.current
    const z = zoomRef.current
    if (!svg || !z || !size.w) return
    const f = focus ? prefectures.find((p) => p.properties.id === focus) : null
    let target = zoomIdentity
    if (f) {
      const [[x0, y0], [x1, y1]] = path.bounds(f)
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
  }, [focus, path, prefectures, size])

  const zoomBy = (k: number) => {
    const svg = svgRef.current
    const z = zoomRef.current
    if (svg && z)
      select(svg)
        .transition()
        .duration(calm() ? 0 : 350)
        .call(z.scaleBy, k)
  }

  return (
    <div
      className="jm"
      onWheel={(e) => {
        if (e.ctrlKey || e.metaKey) return
        setHint(true)
        window.clearTimeout(hintTimer.current)
        hintTimer.current = window.setTimeout(() => setHint(false), 1600)
      }}
    >
      <svg
        ref={svgRef}
        width={size.w}
        height={size.h}
        role="img"
        aria-label={m.journey_map_label()}
        className="block touch-pan-y"
        onPointerLeave={() => setHover(null)}
      >
        <rect width={size.w} height={size.h} className="jm-sea" />
        <g transform={t.toString()}>
          <path d={path(geoGraticule10()) ?? undefined} className="jm-grid" />
          {neighbours.map((c, i) => (
            <path key={i} d={path(c) ?? undefined} className="jm-land" />
          ))}
          {prefectures.map((p) => {
            const id = p.properties.id
            return (
              <path
                key={id}
                d={path(p) ?? undefined}
                className={cn(
                  'jm-pref',
                  id === HOME_PREFECTURE && 'is-home',
                  id === selected && 'is-selected',
                )}
                onPointerMove={(e) => {
                  const r = svgRef.current?.getBoundingClientRect()
                  if (r)
                    setHover({ p, x: e.clientX - r.left, y: e.clientY - r.top })
                }}
                onClick={() => onSelect(id)}
              />
            )
          })}
        </g>
        {pin && (
          <g
            transform={`translate(${t.applyX(pin[0])} ${t.applyY(pin[1])})`}
            className="jm-pin"
          >
            <circle r={10} className="jm-pin-pulse" />
            <circle r={4} />
          </g>
        )}
      </svg>

      {hover && (
        <span
          aria-hidden="true"
          className="jm-tip"
          style={{ left: hover.x, top: hover.y }}
        >
          {name(hover.p.properties)}
        </span>
      )}
      <p aria-hidden={!hint} className={cn('jm-hint', hint && 'is-shown')}>
        {m.map_hint()}
      </p>
      <div className="jm-zoom">
        <button
          type="button"
          onClick={() => zoomBy(1.6)}
          aria-label={m.map_zoom_in()}
        >
          +
        </button>
        <button
          type="button"
          onClick={() => zoomBy(1 / 1.6)}
          aria-label={m.map_zoom_out()}
        >
          −
        </button>
        <button type="button" onClick={onAll} aria-pressed={!focus}>
          {m.map_all()}
        </button>
      </div>
    </div>
  )
}
