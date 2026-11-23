import { useEffect, useRef } from 'react'
import type { VantaEffect } from 'vanta/dist/vanta.waves.min'
import { reducedMotion, saveData } from '@/ui/v3/lib/motion'
import type { Theme } from '@/ui/v3/lib/theme'

// Nagasaki harbour under the Megane bridge: slow, low swell in the kit's navy and cobalt.
const BASE = {
  mouseControls: true, // the camera leans a little toward the pointer
  touchControls: false, // never fight a thumb that is scrolling
  gyroControls: false,
  minHeight: 200,
  minWidth: 200,
  scale: 1,
  scaleMobile: 1,
  waveHeight: 13,
  waveSpeed: 0.55,
  zoom: 0.9,
}
const LOOK: Record<Theme, { color: number; shininess: number }> = {
  dark: { color: 0x15284f, shininess: 32 }, // harbour night: navy just above the --tile, crests catch cobalt
  light: { color: 0xc9d6ea, shininess: 50 }, // porcelain day: pale cobalt water
}

const SAMPLE_MS = 2000
const MIN_FPS = 24

/** Vanta WAVES behind the landing hero. three.js and Vanta load only here (own chunk, F-40), and never
 *  under reduced motion (F-38) or Data Saver: the still `harbour-still` background stays instead. */
export function HarbourWaves({ theme }: { theme: Theme }) {
  const ref = useRef<HTMLDivElement>(null)
  const effect = useRef<VantaEffect | null>(null)

  useEffect(() => {
    const el = ref.current
    if (!el || reducedMotion() || saveData()) return
    let cancelled = false
    let raf = 0
    void Promise.all([
      import('three'),
      import('vanta/dist/vanta.waves.min'),
    ]).then(([THREE, mod]) => {
      if (cancelled) return
      // Vanta's UMD build exports webpack's `{ default: WAVES }`; bundled, that object becomes `default`
      const exported = mod.default
      const WAVES = typeof exported === 'function' ? exported : exported.default
      const start: Theme =
        el.closest('[data-theme]')?.getAttribute('data-theme') === 'dark'
          ? 'dark'
          : 'light'
      try {
        effect.current = WAVES({ el, THREE, ...BASE, ...LOOK[start] })
      } catch (err) {
        // No WebGL (old tablet, blocked GPU): the still background is already there
        console.warn('Vanta waves did not start', err)
        return
      }
      // Software WebGL or a weak GPU: if the page drops under MIN_FPS, a stuttering hero is worse than a
      // still one, so stop the waves and keep the still sea (e.g. a judge's laptop with the GPU blocklisted)
      const t0 = performance.now()
      let frames = 0
      const sample = () => {
        frames++
        if (performance.now() - t0 < SAMPLE_MS) {
          raf = requestAnimationFrame(sample)
        } else if ((frames * 1000) / SAMPLE_MS < MIN_FPS) {
          effect.current?.destroy()
          effect.current = null
        }
      }
      raf = requestAnimationFrame(sample)
    })
    return () => {
      cancelled = true
      cancelAnimationFrame(raf)
      effect.current?.destroy()
      effect.current = null
    }
  }, [])

  useEffect(() => {
    effect.current?.setOptions(LOOK[theme])
  }, [theme])

  return (
    <div aria-hidden="true" className="harbour-still absolute inset-0 -z-10">
      <div ref={ref} className="absolute inset-0" />
      <div className="harbour-veil absolute inset-0" />
    </div>
  )
}
