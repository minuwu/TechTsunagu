import { useEffect, useMemo, useRef, useState } from 'react'
import { geoMercator, geoPath, type GeoPermissibleObjects } from 'd3-geo'
import { select } from 'd3-selection'
import 'd3-transition' // adds selection.transition()
import { zoom, zoomIdentity, type ZoomBehavior } from 'd3-zoom'
import type { Feature, Geometry } from 'geojson'
import {
  HOME_PREFECTURE as HOME,
  SASEBO,
  useJapanMap,
  type PrefectureProps as Props,
} from '@/features/map'
import { m } from '@/i18n/paraglide/messages'
import { reducedMotion } from '@/ui/v3/lib/motion'
import { uiLang } from '@/ui/v3/lib/format'
import { cn } from '@/ui/v3/lib/utils'

type Pref = Feature<Geometry, Props>

const DURATION = 1100 // ms, a calm glide (motion only; reduced motion jumps)

/** Interactive Japan map, drawn with d3-geo into SVG. Hover names a prefecture, click glides to it
 *  (d3-zoom + d3-transition), the buttons return to all of Japan or go to Nagasaki. Scroll and drag never
 *  zoom, so the page scrolls normally. `focusRight` keeps the map in the right part on wide screens,
 *  where the hero text does not cover it. */
export function JapanMap({
  className,
  focusRight = true,
  intro = true,
}: {
  className?: string
  focusRight?: boolean
  intro?: boolean
}) {
  const wrap = useRef<HTMLDivElement>(null)
  const svgRef = useRef<SVGSVGElement>(null)
  const layer = useRef<SVGGElement>(null)
  const pin = useRef<SVGGElement>(null)
  const zoomer = useRef<ZoomBehavior<SVGSVGElement, unknown> | null>(null)
  const { prefectures: prefs } = useJapanMap()
  const [size, setSize] = useState({ w: 0, h: 0 })
  const [hover, setHover] = useState<{ p: Pref; x: number; y: number } | null>(
    null,
  )
  const [focused, setFocused] = useState<string | null>(null)
  const lang = uiLang()

  // Follow the container's size
  useEffect(() => {
    const el = wrap.current
    if (!el) return
    const ro = new ResizeObserver(([e]) => {
      if (e)
        setSize({
          w: Math.round(e.contentRect.width),
          h: Math.round(e.contentRect.height),
        })
    })
    ro.observe(el)
    return () => ro.disconnect()
  }, [])

  const wide = focusRight && size.w >= 1024
  // The part of the box the map uses: the right 56% on wide screens, everything on narrow ones
  const box = useMemo(() => {
    const { w, h } = size
    const pad = Math.min(w, h) * 0.06
    return wide
      ? { x0: w * 0.4, y0: pad, x1: w - pad, y1: h - pad }
      : { x0: pad, y0: pad, x1: w - pad, y1: h - pad }
  }, [size, wide])

  const path = useMemo(() => {
    if (!prefs.length || !size.w || !size.h) return null
    const projection = geoMercator().fitExtent(
      [
        [box.x0, box.y0],
        [box.x1, box.y1],
      ],
      // Fit the main islands; Okinawa, far to the south-west, would shrink everything else
      {
        type: 'FeatureCollection',
        features: prefs.filter((p) => p.properties.id !== 'JP-47'),
      } as GeoPermissibleObjects,
    )
    return { gen: geoPath(projection), projection }
  }, [prefs, size, box])

  // The zoom behaviour: driven by code only (no wheel, drag or double-click), so it never steals the scroll
  useEffect(() => {
    const svg = svgRef.current
    if (!svg) return
    const z = zoom<SVGSVGElement, unknown>()
      .scaleExtent([1, 14])
      .filter(() => false)
      .on('zoom', (e: { transform: { k: number; toString(): string } }) => {
        layer.current?.setAttribute('transform', e.transform.toString())
        // The pin keeps its size on screen while the land grows
        pin.current?.style.setProperty('--k', String(e.transform.k))
      })
    select(svg).call(z)
    zoomer.current = z
  }, [])

  const glide = (target: string | null) => {
    const svg = svgRef.current
    const z = zoomer.current
    if (!svg || !z || !path) return
    const sel = select(svg)
    const run = reducedMotion() ? sel : sel.transition().duration(DURATION)
    setFocused(target)
    const f = target ? prefs.find((p) => p.properties.id === target) : null
    if (!f) {
      run.call(z.transform as never, zoomIdentity)
      return
    }
    const [[x0, y0], [x1, y1]] = path.gen.bounds(f)
    const bw = box.x1 - box.x0
    const bh = box.y1 - box.y0
    const k = Math.min(8, 0.75 / Math.max((x1 - x0) / bw, (y1 - y0) / bh))
    const cx = (box.x0 + box.x1) / 2
    const cy = (box.y0 + box.y1) / 2
    const t = zoomIdentity
      .translate(cx, cy)
      .scale(k)
      .translate(-(x0 + x1) / 2, -(y0 + y1) / 2)
    run.call(z.transform as never, t)
  }

  // Intro: after the splash, glide from all of Japan to Nagasaki (motion only)
  useEffect(() => {
    if (!intro || !path || reducedMotion()) return
    const t = window.setTimeout(() => glide(HOME), 1600)
    return () => window.clearTimeout(t)
    // eslint-disable-next-line react-hooks/exhaustive-deps -- once, when the map first has a size
  }, [Boolean(path)])

  const name = (p: Pref) => (lang === 'ja' ? p.properties.ja : p.properties.en)
  const focusedPref = prefs.find((p) => p.properties.id === focused)
  const pinAt = path?.projection(SASEBO)

  return (
    <div
      ref={wrap}
      className={cn('japan-map relative overflow-hidden', className)}
    >
      <svg
        ref={svgRef}
        width={size.w}
        height={size.h}
        role="img"
        aria-label={m.map_label()}
        className="absolute inset-0 block"
        onPointerLeave={() => setHover(null)}
        onClick={(e) => {
          if (e.target === e.currentTarget) glide(null) // the sea: back to all of Japan
        }}
      >
        <g ref={layer}>
          {path &&
            prefs.map((p) => (
              <path
                key={p.properties.id}
                d={path.gen(p) ?? undefined}
                className={cn(
                  'japan-pref',
                  p.properties.id === HOME && 'is-home',
                  p.properties.id === focused && 'is-focused',
                )}
                onPointerMove={(e) => {
                  const r = wrap.current?.getBoundingClientRect()
                  if (r)
                    setHover({ p, x: e.clientX - r.left, y: e.clientY - r.top })
                }}
                onClick={() =>
                  glide(focused === p.properties.id ? null : p.properties.id)
                }
              />
            ))}
          {pinAt && (
            <g
              ref={pin}
              className="japan-pin"
              transform={`translate(${pinAt[0]},${pinAt[1]})`}
            >
              <circle className="japan-pin-pulse" r={9} />
              <circle className="japan-pin-dot" r={4.5} />
            </g>
          )}
        </g>
      </svg>

      {hover && (
        <span
          aria-hidden="true"
          className="japan-tip"
          style={{ left: hover.x, top: hover.y }}
        >
          {name(hover.p)}
        </span>
      )}

      {/* Nagasaki: a public-domain photo of the place (public/media/README.md, CC0) */}
      <figure
        aria-hidden={focused !== HOME}
        className={cn('japan-photo', focused === HOME && 'is-shown')}
      >
        <img
          src="/media/megane-bridge-nagasaki-960.webp"
          alt={m.map_photo_alt()}
          width={960}
          height={720}
          loading="lazy"
          decoding="async"
        />
        <figcaption>
          <span className="font-bold">{m.map_photo_caption()}</span>
          <span className="block text-muted-foreground">
            {m.map_photo_credit()}
          </span>
        </figcaption>
      </figure>

      {/* Controls and what is shown: real buttons, so the map works from the keyboard too */}
      <div
        className={cn(
          'absolute right-3 bottom-3 z-10 flex flex-wrap items-center justify-end gap-2',
          !path && 'invisible',
        )}
      >
        <p aria-live="polite" className="japan-chip">
          {focusedPref ? name(focusedPref) : m.map_all()}
          {focused === HOME && (
            <span className="font-normal"> · {m.map_pilot()}</span>
          )}
        </p>
        <button
          type="button"
          className="japan-btn"
          onClick={() => glide(null)}
          aria-pressed={!focused}
        >
          {m.map_all()}
        </button>
        <button
          type="button"
          className="japan-btn"
          onClick={() => glide(HOME)}
          aria-pressed={focused === HOME}
        >
          {lang === 'ja' ? '長崎県' : 'Nagasaki'}
        </button>
      </div>
    </div>
  )
}
