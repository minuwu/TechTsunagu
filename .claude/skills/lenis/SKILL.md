---
name: lenis
description: Smooth wheel scrolling with Lenis (darkroomengineering/lenis) in the Wakatta? frontend. Use when adding, tuning or debugging smooth scroll, in-page scroll links, scroll-linked animation (with GSAP ScrollTrigger), or when something scrolls wrongly inside a modal, table or nested scroll area.
---

# Lenis smooth scroll (Wakatta? v3 kit)

Source: https://github.com/darkroomengineering/lenis (README read 2026-11-23, `lenis@1.3.27`).

## Where it lives
- `frontend/src/ui/v3/lib/motion.ts`: `startSmoothScroll()` (one instance per page) and `scrollToSection(id)`.
- Started once in `frontend/src/ui/v3/Layout.tsx`: `useEffect(() => startSmoothScroll(), [])`.
- Lenis CSS is copied into `frontend/src/ui/v3/tokens.css` (the kit loads one stylesheet; do not `import 'lenis/dist/lenis.css'` in a component).
- Kit-local only (A-01, A-04): v1 and v2 do not use it. Another kit copies the helper, never imports v3's.

## Settings and why
```ts
new Lenis({ lerp: 0.12, wheelMultiplier: 0.9, anchors: false })
```
- `lerp: 0.12`: a little firmer than the 0.1 default. This is a work tool, not a showreel (F-34).
- **Driven by GSAP's ticker**, not `autoRaf`: `gsap.ticker.add(t => lenis.raf(t * 1000))`, `gsap.ticker.lagSmoothing(0)`, `lenis.on('scroll', ScrollTrigger.update)`. ScrollTrigger then reads the same frame.
- Touch stays native (`syncTouch` off, the default). Never smooth a Worker's thumb.
- `respectReducedMotion` is on by default: under `prefers-reduced-motion: reduce` smoothing stops and `scrollTo` jumps (F-38). Keep it.

## Rules
1. **`anchors: false`.** Lenis's anchor handling calls `preventDefault`, so the skip link ("Skip to main content") would scroll without moving focus. Native anchors keep keyboard users working. For a smooth in-page link, use `scrollToSection(id)`: it scrolls with Lenis (offset -96 for the sticky header, same as `scroll-mt-24`), updates the hash, then focuses the target (adds `tabindex="-1"` if needed). Keep `href="#id"` on the link for no-JS.
2. **Modals and nested scroll areas get `data-lenis-prevent`.** The v3 `ConfirmDialog` has it; any new `<dialog>`, drawer or scrollable panel needs it too, or the page scrolls underneath.
3. Sticky elements (header, Foreman sidebar) work unchanged: Lenis moves the native scroll position.
4. Don't create a second Lenis instance in a screen. Read or scroll through `motion.ts`.

## Checking it works
- With motion on, `document.documentElement.classList.contains('lenis')` is true after load.
- e2e: `tests/e2e/home.spec.ts` "landing with motion on" scrolls with the mouse wheel, which goes through Lenis.
