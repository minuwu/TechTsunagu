import {
  useEffect,
  useMemo,
  useRef,
  useState,
  useSyncExternalStore,
} from 'react'
import {
  geoCentroid,
  geoOrthographic,
  type GeoPermissibleObjects,
} from 'd3-geo'
import { HOME_PREFECTURE, NAGASAKI, useJourneyMap } from '@/features/map'
import { m } from '@/i18n/paraglide/messages'
import { uiLang } from '@/ui/v4/lib/format'
import type { Earth, View } from './Earth'
import { JapanMap } from './JapanMap'
import { PrefecturePanel } from './PrefecturePanel'

type Phase = 'earth' | 'flying' | 'map'

const OKINAWA = 'JP-47' // far south-west: fitting it would shrink the main islands
const START_DELAY = 1200 // let the Earth turn a moment before the flight
const LAND_DELAY = 500 // a beat on all of Japan before gliding into Nagasaki

const CALM = '(prefers-reduced-motion: reduce)'
function useReducedMotion() {
  return useSyncExternalStore(
    (onChange) => {
      const q = window.matchMedia(CALM)
      q.addEventListener('change', onChange)
      return () => q.removeEventListener('change', onChange)
    },
    () => window.matchMedia(CALM).matches,
    () => true, // server: the calm version; the client takes over after hydrating
  )
}

/** "Journey to Nagasaki" (landing section): a 3D Earth turns toward Japan and stops on Nagasaki, then
 *  hands over to a map of Japan that glides into Nagasaki, the story's peak (peak-end rule); the panel
 *  is Nagasaki's card. Reduced motion, no WebGL or a failed texture skip straight to the map. */
export function Journey() {
  const { prefectures, neighbours, highlights, ready, failed } = useJourneyMap()
  const calm = useReducedMotion()
  const lang = uiLang()
  const stageRef = useRef<HTMLDivElement>(null)
  const canvasRef = useRef<HTMLCanvasElement>(null)
  const earth = useRef<Earth | null>(null)
  const [size, setSize] = useState({ w: 0, h: 0 })
  const [near, setNear] = useState(false)
  const [visible, setVisible] = useState(false)
  const [phase, setPhase] = useState<Phase>('earth')
  const [earthState, setEarthState] = useState<'loading' | 'ready' | 'failed'>(
    'loading',
  )
  const [zoomed, setZoomed] = useState(false)

  const shown: Phase = calm || earthState === 'failed' ? 'map' : phase

  // Once the map is up, glide into Nagasaki; "All Japan" afterwards stays where the reader put it
  useEffect(() => {
    if (shown !== 'map') return
    const t = window.setTimeout(() => setZoomed(true), calm ? 0 : LAND_DELAY)
    return () => window.clearTimeout(t)
  }, [shown, calm])

  useEffect(() => {
    const el = stageRef.current
    if (!el) return
    const ro = new ResizeObserver(([e]) => {
      if (e)
        setSize({
          w: Math.round(e.contentRect.width),
          h: Math.round(e.contentRect.height),
        })
    })
    // fetch three.js a little before the section arrives; fly only once most of it is on screen
    const nearIo = new IntersectionObserver(
      ([e]) => e?.isIntersecting && setNear(true),
      { rootMargin: '600px 0px' },
    )
    const seenIo = new IntersectionObserver(
      ([e]) => e && setVisible(e.intersectionRatio >= 0.4),
      { threshold: [0, 0.4] },
    )
    ro.observe(el)
    nearIo.observe(el)
    seenIo.observe(el)
    return () => {
      ro.disconnect()
      nearIo.disconnect()
      seenIo.disconnect()
    }
  }, [])

  // The view that frames the main islands; the Earth ends on it and the map starts on it
  const view = useMemo<View | null>(() => {
    if (!prefectures.length || !size.w) return null
    const main = {
      type: 'FeatureCollection',
      features: prefectures.filter((p) => p.properties.id !== OKINAWA),
    } as GeoPermissibleObjects
    const [lon, lat] = geoCentroid(main)
    const pad = Math.min(size.w, size.h) * 0.07
    const fit = geoOrthographic()
      .rotate([-lon, -lat])
      .fitExtent(
        [
          [pad, pad],
          [size.w - pad, size.h - pad],
        ],
        main,
      )
    // centre on whatever the fit put in the middle of the box, so both views share one centre
    const [cLon, cLat] = fit.invert?.([size.w / 2, size.h / 2]) ?? [lon, lat]
    return { lon: cLon, lat: cLat, scale: fit.scale() }
  }, [prefectures, size])

  // Load three.js and build the Earth once the section is near (never under reduced motion)
  useEffect(() => {
    if (calm || !near) return
    let alive = true
    let made: Earth | null = null
    import('./Earth')
      .then(({ createEarth }) => {
        const canvas = canvasRef.current
        if (!alive || !canvas) return
        try {
          made = createEarth(canvas, {
            home: NAGASAKI,
            onReady: () => alive && setEarthState('ready'),
            onError: () => alive && setEarthState('failed'),
          })
          earth.current = made
        } catch {
          setEarthState('failed') // no WebGL: the map alone
        }
      })
      .catch(() => alive && setEarthState('failed'))
    return () => {
      alive = false
      made?.dispose()
      earth.current = null
    }
  }, [calm, near])

  useEffect(() => {
    earth.current?.resize(size.w, size.h)
  }, [size, earthState])

  useEffect(() => {
    earth.current?.setActive(
      visible && shown !== 'map' && earthState === 'ready',
    )
  }, [visible, shown, earthState])

  // Fly when the Earth is ready and on screen (again after "Fly again")
  useEffect(() => {
    if (earthState !== 'ready' || !visible || phase !== 'earth' || !view) return
    const t = window.setTimeout(() => {
      setPhase('flying')
      earth.current?.fly(view, () => setPhase('map'))
    }, START_DELAY)
    return () => window.clearTimeout(t)
  }, [earthState, visible, phase, view])

  const toMap = () => {
    earth.current?.reset()
    setPhase('map')
  }
  const replay = () => {
    setZoomed(false)
    earth.current?.reset()
    setPhase('earth')
  }

  const homePref = prefectures.find(
    (p) => p.properties.id === HOME_PREFECTURE,
  )?.properties
  const highlight = highlights.find((h) => h.id === HOME_PREFECTURE)
  const homeName = homePref ? (lang === 'ja' ? homePref.ja : homePref.en) : ''

  return (
    <section
      id="journey"
      className="poster-grid journey"
      aria-labelledby="journey-h"
    >
      <div className="poster-side journey-side">
        <p className="journey-label">{m.journey_label()}</p>
        <p className="journey-vertical" lang="ja" aria-hidden="true">
          {m.journey_vertical()}
        </p>
      </div>
      <div className="poster-main journey-main">
        <h2 id="journey-h" className="poster-h2 journey-h">
          {m.journey_h()}
        </h2>
        <p className="journey-lead">{m.journey_lead()}</p>
      </div>
      <div className="journey-body">
        <div ref={stageRef} className="journey-stage" data-phase={shown}>
          {!calm && (
            <canvas
              ref={canvasRef}
              className="journey-earth"
              role="img"
              aria-label={m.journey_earth_label()}
              aria-hidden={shown === 'map'}
            />
          )}
          {ready && view && (
            <div className="journey-map" inert={shown !== 'map'}>
              <JapanMap
                prefectures={prefectures}
                neighbours={neighbours}
                view={view}
                size={size}
                zoomed={zoomed}
                onZoom={setZoomed}
                homeName={homeName}
              />
            </div>
          )}

          <p className="journey-status" aria-live="polite">
            {failed
              ? m.journey_error()
              : shown === 'map'
                ? ''
                : earthState === 'loading'
                  ? m.journey_loading()
                  : phase === 'flying'
                    ? m.journey_flying()
                    : ''}
          </p>
          {shown !== 'map' && !failed && (
            <button
              type="button"
              className="journey-action journey-skip"
              onClick={toMap}
            >
              {m.journey_skip()}
            </button>
          )}
          {shown === 'map' && !calm && earthState === 'ready' && (
            <button
              type="button"
              className="journey-action journey-replay"
              onClick={replay}
            >
              {m.journey_replay()}
            </button>
          )}
        </div>

        {ready && homePref && highlight && (
          <PrefecturePanel pref={homePref} highlight={highlight} lang={lang} />
        )}
      </div>
    </section>
  )
}
