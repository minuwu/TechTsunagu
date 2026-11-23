---
name: gsap
description: GSAP animation (greensock/gsap, with @gsap/react and ScrollTrigger) in the Wakatta? frontend. Use when adding entrances, scroll reveals, counters, timelines or any JavaScript animation to a UI kit, and when an animation breaks accessibility checks or leaves content hidden.
---

# GSAP in Wakatta? (v3 kit)

Source: https://github.com/greensock/gsap (`gsap@3.15.1`, `@gsap/react@2.1.2`). GSAP and all its plugins (ScrollTrigger, SplitText, …) are free under the standard "no charge" license.

## Where it lives
- `frontend/src/ui/v3/screens/home/Landing.tsx`, `useLandingMotion()`: hero entrance, scroll reveals (`ScrollTrigger.batch`), count-up on the sourced 46.2% statistic.
- `frontend/src/ui/v3/lib/motion.ts`: `MOTION_OK` media query, GSAP ticker drives Lenis (see the `lenis` skill).

## The pattern (copy this)
```tsx
const scope = useRef<HTMLDivElement>(null)
useGSAP(() => {
  gsap.registerPlugin(ScrollTrigger)          // inside the hook: client only, SSR-safe
  const root = scope.current; if (!root) return
  const mm = gsap.matchMedia()
  mm.add(MOTION_OK, () => {                  // nothing runs under reduced motion (F-38)
    const q = gsap.utils.selector(root)        // scoped selectors, never document-wide strings
    gsap.from(q('[data-hero]'), { y: 24, opacity: 0, duration: 0.8, ease: 'power3.out', stagger: 0.09 })
  })
  return () => mm.revert()
}, { scope })
```

## Motion language (keep it consistent)
- Ease `power3.out`, 0.7–0.8 s, rise 24–28 px, stagger 0.08–0.09 s. Counters `power2.out`, 1.4 s.
- Start after the splash: `document.querySelector('.splash') ? 1.05 : 0.1` s delay.
- Reveals: `data-reveal` on a block. Blocks already on screen at load animate in after the hero; the rest use `ScrollTrigger.batch(..., { start: 'top 88%', once: true })`. Re-measure after web fonts: `document.fonts.ready.then(() => ScrollTrigger.refresh())`. Japanese and Bengali fonts change line breaks.
- Landing and marketing surfaces only. Worker and Foreman task screens stay calm: CSS transitions from `tokens.css`, no scroll choreography (F-34).
- No parallax on text, no scroll-jacking, no pinned sections longer than one screen.

## Accessibility rules
1. **Animate `opacity`, never `autoAlpha`/`visibility`, for reveals.** Hidden-by-visibility content drops out of the accessibility tree, so screen-reader users skip it.
2. Everything must end at `opacity: 1`. The e2e test "landing with motion on" fails if any `[data-reveal]` or `[data-hero]` stays below 1.
3. Counters change only the number's text. The real value is in the server-rendered HTML, so no-JS and reduced-motion users see it.
4. Don't split Japanese text into characters (SplitText): it breaks `word-break: auto-phrase` and line breaking. Animate whole lines or blocks.

## Tests
`playwright.config.ts` runs with `reducedMotion: 'reduce'`, so axe never scans a half-faded element (false contrast failures). A test that checks motion opts in with `test.use({ reducedMotion: 'no-preference' })`.
