import { gsap } from 'gsap'
import { ScrollTrigger } from 'gsap/ScrollTrigger'
import Lenis from 'lenis'

/** Motion for the v3 kit: Lenis smooth scroll driven by GSAP's ticker, so ScrollTrigger reads the same frame.
 *  Everything here honours prefers-reduced-motion (F-38): Lenis drops its smoothing, GSAP work runs only
 *  inside `gsap.matchMedia(MOTION_OK)`, and the Vanta hero never starts. Touch keeps native scrolling. */

export const MOTION_OK = '(prefers-reduced-motion: no-preference)'
// Sticky header height plus breathing room; the same value as the sections' `scroll-mt-24`
const HEADER_OFFSET = -96

let lenis: Lenis | null = null

export function reducedMotion(): boolean {
  return typeof window !== 'undefined' && !window.matchMedia(MOTION_OK).matches
}

/** Data saver on, or a slow link: skip the WebGL hero (three.js is the biggest download on the page). */
export function saveData(): boolean {
  const c = (
    navigator as Navigator & {
      connection?: { saveData?: boolean; effectiveType?: string }
    }
  ).connection
  return c?.saveData === true || /(^|-)2g$/.test(c?.effectiveType ?? '')
}

/** Starts Lenis once for the page; returns the cleanup for the Layout's effect. */
export function startSmoothScroll(): () => void {
  gsap.registerPlugin(ScrollTrigger)
  const instance = new Lenis({
    lerp: 0.12, // a touch firmer than the 0.1 default: a work tool, not a showreel (F-34)
    wheelMultiplier: 0.9,
    anchors: false, // native anchors keep the skip link moving focus; in-page CTAs call scrollToSection
  })
  const tick = (seconds: number) => instance.raf(seconds * 1000)
  instance.on('scroll', ScrollTrigger.update)
  gsap.ticker.add(tick)
  gsap.ticker.lagSmoothing(0)
  lenis = instance
  return () => {
    gsap.ticker.remove(tick)
    instance.destroy()
    if (lenis === instance) lenis = null
  }
}

/** Smooth scroll to an in-page section, then move focus there so keyboard and screen reader users follow. */
export function scrollToSection(id: string): void {
  const el = document.getElementById(id)
  if (!el) return
  const arrive = () => {
    if (!el.hasAttribute('tabindex')) el.setAttribute('tabindex', '-1')
    el.focus({ preventScroll: true })
  }
  history.replaceState(history.state, '', `#${id}`)
  if (lenis) lenis.scrollTo(el, { offset: HEADER_OFFSET, onComplete: arrive })
  else {
    el.scrollIntoView({ behavior: reducedMotion() ? 'auto' : 'smooth' })
    arrive()
  }
}
