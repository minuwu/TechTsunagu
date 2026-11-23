import { useEffect, useMemo, useRef, useState } from 'react'
import {
  geoCentroid,
  geoGraticule10,
  geoOrthographic,
  geoPath,
  type GeoPermissibleObjects,
} from 'd3-geo'
import { select } from 'd3-selection'
import { zoom, zoomIdentity, type ZoomTransform } from 'd3-zoom'
import type { Feature, Geometry } from 'geojson'
import {
  HOME_PREFECTURE,
  SASEBO,
  useGlobeMap,
  type PrefectureProps,
} from '@/features/map'
import { m } from '@/i18n/paraglide/messages'
import { HeroArt } from '@/ui/v4/components/HeroArt'
import { uiLang } from '@/ui/v4/lib/format'
import { cn } from '@/ui/v4/lib/utils'

type Pref = Feature<Geometry, PrefectureProps>
/** Where the globe looks (degrees) and how big it is drawn (projection scale, px). */
type View = { lon: number; lat: number; scale: number }

const DEG = 180 / Math.PI
const INTRO_MS = 2800 // whole globe → Japan
const GLIDE_MS = 1100 // to a prefecture and back
const PREF_ZOOM = 5 // a chosen prefecture at most 5× the all-Japan view, so its neighbours stay in sight
const OKINAWA = 'JP-47' // far south-west: fitting it would shrink the main islands

const ease = (t: number) =>
  t < 0.5 ? 4 * t * t * t : 1 - (-2 * t + 2) ** 3 / 2
const calm = () =>
  typeof window !== 'undefined' &&
  window.matchMedia('(prefers-reduced-motion: reduce)').matches
const clamp = (v: number, lo: number, hi: number) =>
  Math.min(hi, Math.max(lo, v))

/** The landing's hero map (v4): opens on the whole globe and flies into Japan; a prefecture can be clicked
 *  or chosen from the list, and the map glides to it and shows what it is famous for, with a copyright-free
 *  photo. Drawn with d3-geo (orthographic). Zoom: Ctrl/⌘ + scroll, pinch, double-click or the buttons; plain
 *  scroll still scrolls the page; drag turns the globe. Reduced motion: no flight, glides jump. */
export function GlobeMap({ className }: { className?: string }) {
  const { countries, prefectures, highlights, ready, failed } = useGlobeMap()
  const wrap = useRef<HTMLDivElement>(null)
  const svgRef = useRef<SVGSVGElement>(null)
  const [size, setSize] = useState({ w: 0, h: 0 })
  const [view, setView] = useState<View | null>(null)
  const viewRef = useRef<View | null>(null)
  const anim = useRef(0)
  const [selected, setSelected] = useState<string | null>(null)
  const [hover, setHover] = useState<{ p: Pref; x: number; y: number } | null>(
    null,
  )
  const [hint, setHint] = useState(false)
  const hintTimer = useRef(0)
  const lang = uiLang()

  const show = (v: View) => {
    viewRef.current = v
    setView(v)
  }

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

  // The view that fits a feature (or all of Japan) in the box, centred on it
  const fit = useMemo(() => {
    const pad = Math.min(size.w, size.h) * 0.08
    return (f: GeoPermissibleObjects, zoomCap = Infinity): View => {
      const [lon, lat] = geoCentroid(f)
      const p = geoOrthographic()
        .rotate([-lon, -lat])
        .fitExtent(
          [
            [pad, pad],
            [size.w - pad, size.h - pad],
          ],
          f,
        )
      return { lon, lat, scale: Math.min(p.scale(), zoomCap) }
    }
  }, [size])

  const japan = useMemo(
    () =>
      prefectures.length && size.w
        ? fit({
            type: 'FeatureCollection',
            features: prefectures.filter((p) => p.properties.id !== OKINAWA),
          } as GeoPermissibleObjects)
        : null,
    [prefectures, size, fit],
  )
  const globeScale = Math.min(size.w, size.h) * 0.44

  // Glide from the current view to `to`: centre linearly, scale geometrically (feels like a steady zoom)
  const glide = (to: View, ms = GLIDE_MS) => {
    cancelAnimationFrame(anim.current)
    const from = viewRef.current
    if (!from || calm()) return show(to)
    const t0 = performance.now()
    const step = (now: number) => {
      const t = ease(Math.min(1, (now - t0) / ms))
      show({
        lon: from.lon + (to.lon - from.lon) * t,
        lat: from.lat + (to.lat - from.lat) * t,
        scale: from.scale * (to.scale / from.scale) ** t,
      })
      if (t < 1) anim.current = requestAnimationFrame(step)
    }
    anim.current = requestAnimationFrame(step)
  }

  // Intro: the whole globe first, then fly into Japan (once the data and the box size are known)
  // keyed on "can draw", not on `japan`: a resize in the first 500 ms (a scrollbar appearing) must not cancel it
  const japanRef = useRef(japan)
  useEffect(() => {
    japanRef.current = japan // runs before the intro effect below, so it sees the first fit too
  }, [japan])
  const canDraw = !!japan
  useEffect(() => {
    const target = japanRef.current
    if (!target || viewRef.current) return
    const first = requestAnimationFrame(() =>
      show(calm() ? target : { lon: 100, lat: 15, scale: globeScale }),
    )
    const t = window.setTimeout(() => {
      if (japanRef.current) glide(japanRef.current, INTRO_MS)
    }, 500)
    return () => {
      cancelAnimationFrame(first)
      window.clearTimeout(t)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps -- once, when the map can first be drawn
  }, [canDraw])

  // Gestures, read as deltas and applied to the view: pinch / Ctrl+wheel / double-click zoom, drag turns
  useEffect(() => {
    const svg = svgRef.current
    if (!svg) return
    let last: ZoomTransform = zoomIdentity
    const z = zoom<SVGSVGElement, unknown>()
      .filter((e: Event) => {
        if (e.type === 'wheel')
          return (e as WheelEvent).ctrlKey || (e as WheelEvent).metaKey
        if (e.type === 'touchstart') return (e as TouchEvent).touches.length > 1
        return !(e as MouseEvent).button
      })
      .on('start', () => cancelAnimationFrame(anim.current))
      .on('zoom', (e: { transform: ZoomTransform }) => {
        const v = viewRef.current
        if (!v) return
        const t = e.transform
        const scale = clamp(
          v.scale * (t.k / last.k),
          globeScale * 0.8,
          globeScale * 120,
        )
        show({
          lon: v.lon - ((t.x - last.x) / scale) * DEG,
          lat: clamp(v.lat + ((t.y - last.y) / scale) * DEG, -80, 80),
          scale,
        })
        last = t
      })
    select(svg).call(z)
    return () => {
      select(svg).on('.zoom', null)
    }
  }, [globeScale])

  const projection = useMemo(
    () =>
      view && size.w
        ? geoOrthographic()
            .rotate([-view.lon, -view.lat])
            .scale(view.scale)
            .translate([size.w / 2, size.h / 2])
        : null,
    [view, size],
  )
  const path = projection ? geoPath(projection) : null
  const nearJapan = !!japan && !!view && view.scale > japan.scale * 0.3

  const choose = (id: string | null) => {
    setSelected(id)
    const f = id && prefectures.find((p) => p.properties.id === id)
    if (f) glide(fit(f, japan ? japan.scale * PREF_ZOOM : Infinity))
    else if (japan) glide(japan)
  }
  const zoomBy = (k: number) => {
    const v = viewRef.current
    if (v)
      glide(
        { ...v, scale: clamp(v.scale * k, globeScale * 0.8, globeScale * 120) },
        400,
      )
  }

  const name = (p: { ja: string; en: string }) => (lang === 'ja' ? p.ja : p.en)
  const sel = prefectures.find((p) => p.properties.id === selected)
  const hl = highlights.find((h) => h.id === selected)
  const pin = projection?.(SASEBO)
  const sorted = useMemo(
    () =>
      [...prefectures].sort((a, b) =>
        a.properties.id.localeCompare(b.properties.id),
      ),
    [prefectures],
  )

  if (failed) return <HeroArt className={cn(className)} />

  return (
    <div className={cn('globe relative flex flex-col', className)}>
      <div
        ref={wrap}
        className="relative min-h-0 flex-1 overflow-hidden"
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
          aria-label={m.map_globe_label()}
          className="absolute inset-0 block touch-pan-y"
          onPointerLeave={() => setHover(null)}
        >
          {path && (
            <>
              <path
                d={path({ type: 'Sphere' }) ?? undefined}
                className="globe-sea"
                onClick={() => choose(null)}
              />
              <path
                d={path(geoGraticule10()) ?? undefined}
                className="globe-grid"
              />
              {countries.map((c, i) =>
                // near Japan its 47 prefectures draw it: the coarse world outline would show through
                nearJapan && c.properties?.id === 'JPN' ? null : (
                  <path
                    key={i}
                    d={path(c) ?? undefined}
                    className="globe-land"
                  />
                ),
              )}
              {nearJapan &&
                prefectures.map((p) => (
                  <path
                    key={p.properties.id}
                    d={path(p) ?? undefined}
                    className={cn(
                      'globe-pref',
                      p.properties.id === HOME_PREFECTURE && 'is-home',
                      p.properties.id === selected && 'is-selected',
                    )}
                    onPointerMove={(e) => {
                      const r = wrap.current?.getBoundingClientRect()
                      if (r)
                        setHover({
                          p,
                          x: e.clientX - r.left,
                          y: e.clientY - r.top,
                        })
                    }}
                    onClick={() =>
                      choose(
                        p.properties.id === selected ? null : p.properties.id,
                      )
                    }
                  />
                ))}
              {nearJapan && pin && (
                <circle cx={pin[0]} cy={pin[1]} r={5} className="globe-pin" />
              )}
            </>
          )}
        </svg>

        {hover && (
          <span
            aria-hidden="true"
            className="globe-tip"
            style={{ left: hover.x, top: hover.y }}
          >
            {name(hover.p.properties)}
          </span>
        )}
        <p aria-hidden={!hint} className={cn('globe-hint', hint && 'is-shown')}>
          {m.map_hint()}
        </p>
      </div>
      {/* The selected prefecture's famous place, with its copyright-free photo and credit */}
      {/* always mounted, so screen readers announce each choice (a live region added later is often missed) */}
      <p className="sr-only" aria-live="polite">
        {sel && hl ? `${name(sel.properties)}: ${name(hl)}` : ''}
      </p>
      {sel && hl && (
        <figure className="globe-card">
          {hl.image && (
            <img
              key={hl.image}
              src={hl.image}
              alt={name(hl)}
              width={640}
              height={427}
              loading="lazy"
              decoding="async"
            />
          )}
          <figcaption>
            <span className="poster-label">
              {name(sel.properties)} · {m.map_famous()}
            </span>
            <span className="block text-xl font-bold">{name(hl)}</span>
            {hl.artist && hl.license && (
              <span className="block text-sm text-muted-foreground">
                {m.map_credit({ artist: hl.artist, license: hl.license })}
                {hl.page && (
                  <>
                    {' · '}
                    <a
                      href={hl.page}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="underline"
                    >
                      {m.map_source()}
                    </a>
                  </>
                )}
              </span>
            )}
          </figcaption>
        </figure>
      )}
      <div className={cn('globe-controls', !ready && 'invisible')}>
        <label className="sr-only" htmlFor="globe-choose">
          {m.map_choose()}
        </label>
        <select
          id="globe-choose"
          value={selected ?? ''}
          onChange={(e) => choose(e.target.value || null)}
        >
          <option value="">{m.map_choose()}</option>
          {sorted.map((p) => (
            <option key={p.properties.id} value={p.properties.id}>
              {name(p.properties)}
            </option>
          ))}
        </select>
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
        <button
          type="button"
          onClick={() => choose(null)}
          aria-pressed={!selected}
        >
          {m.map_all()}
        </button>
      </div>
    </div>
  )
}
