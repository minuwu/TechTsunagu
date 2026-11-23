---
name: vanta
description: Animated WebGL backgrounds with Vanta.js (tengbao/vanta) in the Wakatta? frontend. Use when adding, re-theming or debugging an animated hero background, or choosing a Vanta effect and its options for the Nagasaki night theme.
---

# Vanta.js backgrounds (Wakatta? v3 kit)

Source: https://github.com/tengbao/vanta (`vanta@0.5.24`, last release 2022). Effects render with **three.js r134**: the version in Vanta's README. Keep `three@0.134.0` pinned; newer three removed APIs Vanta uses.

## Where it lives
- `frontend/src/ui/v3/components/HarbourWaves.tsx`: the landing hero background.
- `frontend/src/ui/v3/lib/vanta.d.ts`: minimal types (Vanta ships none).
- CSS `.harbour-still` (still fallback) and `.harbour-veil` (text-side contrast veil) in `frontend/src/ui/v3/tokens.css`.

## Chosen effect: WAVES, "Nagasaki harbour at night"
The brand mark is the Megane bridge, so the hero sits on slow night water.
```ts
{ waveHeight: 13, waveSpeed: 0.55, zoom: 0.9, mouseControls: true, touchControls: false, gyroControls: false }
dark:  { color: 0x1a2150, shininess: 32 }   // just above the night --tile, crests catch indigo
light: { color: 0xc3cdf0, shininess: 55 }   // daylight: pale indigo water
```
The theme toggle calls `effect.setOptions(LOOK[theme])`; no re-init. Alternatives that also fit: `FOG` (aurora-like, matches v3's glows, shader only), `NET` (technical). Avoid `BIRDS` and `CELLS` for this brand.

## Rules
1. **One hero only.** Never on Worker, Foreman, Panel or form screens. WebGL costs battery on a factory phone.
2. **Load lazily:** `import('three')` and `import('vanta/dist/vanta.waves.min')` inside `useEffect`, so three.js (about 153 KB gzipped) is its own chunk and SSR never touches it (F-40).
3. **Skip when** `prefers-reduced-motion: reduce` (F-38), Data Saver or 2G (`navigator.connection`), or WebGL is missing. The still `.harbour-still` gradient is the designed fallback, not an error state.
4. **Always `destroy()` on unmount**, or the WebGL context leaks on route change.
5. **Frame-rate guard:** for 2 s after start the component counts frames; under 24 fps it destroys the effect and the still background stays. A stuttering hero on a judge's laptop with a blocklisted GPU is worse than a still one. Headless Chrome renders WebGL in software (about 11 fps), so in e2e the waves start and then stop.
6. **Contrast:** text sits on `.harbour-veil` (94% → 78% panel colour on the text side; 82% everywhere on phones). Recheck contrast if you change the wave colour or the veil.
7. Wrapper is `aria-hidden="true"`; the canvas is decoration.

## Pitfall: the default export
Vanta's `dist/*.min.js` is a webpack UMD build whose export is `{ default: WAVES }`. After Vite bundles it, `import()`'s `default` can be that object, not the function:
```ts
const exported = mod.default
const WAVES = typeof exported === 'function' ? exported : exported.default
```
The `catch` logs `console.warn('Vanta waves did not start', err)`. Don't swallow it silently; that hid this bug.

## Checking it works
Run the production build (`npm run build && npm start`): Vite's dev optimizer can stall on these late-discovered UMD deps. In a real browser with a GPU and motion on, `.harbour-still canvas` exists and stays; with reduced motion it never appears; in headless Chrome it appears, then the frame-rate guard removes it.
