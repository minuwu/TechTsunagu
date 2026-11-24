# frontend/

TanStack Start + React + TypeScript + Vite, talking to the FastAPI backend ([docs/FRONTEND_STACK.md](../docs/FRONTEND_STACK.md)). The Hour-0 skeleton is in place: one screen (API status) runs end to end on mock data.

Node 24 LTS (`.nvmrc`; jsdom needs `^22.22.2 || ^24.15.0 || >=26`).

```bash
cp .env.example .env   # USE_MOCK is on by default
npm install            # also generates src/i18n/paraglide
npm run dev            # http://localhost:3000
npm run typecheck && npm run lint && npm test && npm run build
npx playwright install chromium && npm run test:e2e   # smoke + axe on the production build
npm run gen:api        # after docs/openapi.yaml changes; commit src/services/generated
```

On Windows, run tests from a shell whose path starts with an uppercase `C:\`. A lowercase `c:\` (some VS Code terminals) makes Vitest load itself twice and fail with "Vitest failed to find the runner".

Conventions (AGENTS.md §4.1):

- `src/services/` is the only place that talks to the network (`api.ts` with the `USE_MOCK` toggle, default ON).
- The UI is replaceable: each UI version is a kit in `src/ui/<version>/`; behaviour lives in headless hooks in `src/features/`; kits never import each other ([docs/FRONTEND_ARCHITECTURE.md](../docs/FRONTEND_ARCHITECTURE.md), `A-01`…`A-09`).
- Mocks mirror [docs/API_SPEC.md](../docs/API_SPEC.md) exactly.
- Every async view handles loading, empty and error.
- Commit `.env.example`; never `.env`.
- Stack, versions, folder layout and state-management rules: [docs/FRONTEND_STACK.md](../docs/FRONTEND_STACK.md) (`S-01`…`S-35`). TanStack file routes in `src/routes/` count as `shell/` and stay thin (S-08).
- Japan-facing UI rules: [docs/FRONTEND_REGULATIONS.md](../docs/FRONTEND_REGULATIONS.md) (`F-01`…`F-53`, pre-demo checklist).

## v4: Mount Fuji, design decisions

- **Ground:** every v4 page sits on `src/ui/v4/art/fuji-motosu.avif` (Fuji over Lake Motosu, 26 KB) under a mist scrim, 72% over the sky and 80% from the ridge down (`.v4-ground::before` in `src/ui/v4/tokens.css`). It is a fixed layer, not `background-attachment: fixed`, which iOS Safari ignores. The palette is sampled from the photo; each text token passes 4.5:1 against the darkest photo pixel under the scrim.
- **Hero layers:** `src/ui/v4/components/HeroFuji.tsx`, three polygon silhouettes (back, mid, front) at 8 / 10 / 15% opacity in the bottom-right of the Landing hero, `aria-hidden`, no pointer events. They stay out from under the grey lead text: there a dark layer would push it below 4.5:1.
- **Motion and why:**
  - Fade-in front to back, 0.8 s each, 150 ms apart, after the splash: GSAP `power1.out`, the same curve as `cubic-bezier(0.25, 0.46, 0.45, 0.94)`.
  - Parallax across the hero, desktop (≥ 64rem) only: the layers lift 5 / 8 / 12 px per 100 px of scroll, so the farthest moves most.
  - Float: CSS, transform only, 3 px over 5 s (1.5 px over 7 s on phones).
  - Hover on the demo button or "How it works": fill and glow shift over 300 ms, on pointer devices only.
  - Under `prefers-reduced-motion` the layers are still and at full strength at once. The hover change is instant.
- **CSS variables** (on `.fuji`): `--fuji-front` `#0a1c26`, `--fuji-mid` `#214c69`, `--fuji-back` `#4c7b98`, `--fuji-float`, `--fuji-float-time`. Sizes are in the `.fuji` rules: phones `min(15%, 7rem)`, tablets 25%, desktop 30% of the hero height.
- **Browser notes:** the hover rules use `:has()` and `color-mix()` (Chrome 111+, Safari 16.4+, Firefox 121+). Older browsers keep the still layers and skip the hover shift.
