# Frontend Stack — TanStack Start + React + TypeScript + Vite

The frontend stack, versions, data flow and state-management rules for `frontend/`. Items are numbered `S-xx` so PRs and reviews can cite them, like `F-xx` in [FRONTEND_REGULATIONS.md](FRONTEND_REGULATIONS.md) and `G-xx` in [ENGINEERING_GUIDELINES.md](ENGINEERING_GUIDELINES.md). Where this file and those disagree, AGENTS.md and ENGINEERING_GUIDELINES.md win.

**Goal:** ship fast in a hackathon, but with bones that scale after it: SSR, typed routes, typed API, one place per kind of state, tests from day one.

**Requirement levels:** **MUST** is required for the demo build; **SHOULD** is done unless time runs out. Untagged items are MUST.

**Status (2026-11-21):** `frontend/` is scaffolded (Hour-0 skeleton: one screen, API status, end to end on mock data). `npm install` resolves with no peer conflicts; `typecheck`, `lint`, `build`, `test` (7 unit tests) and `test:e2e` (4 smoke + axe tests) pass; `npm audit --omit=dev` reports 0 vulnerabilities. The table below matches `frontend/package.json`.

---

## 1. The stack at a glance

| Layer | Choice | Why this one |
| :--- | :--- | :--- |
| Framework | **TanStack Start 1.x** (SSR, streaming, server functions) on **Vite 8** | Stable since 2026-10-13. SSR gives fast first paint for judges on phones (F-41) and real `<html lang>` per request (F-09). Stays on Vite, unlike Next.js. |
| UI library | **React 19.3** | Current stable line. |
| Language | **TypeScript 6.0** (strict) | TS 7 (Go compiler) is out, but `typescript-eslint` does not support it yet (§2). |
| Routing | **TanStack Router** (bundled with Start) | File routes, fully typed params and search params, loaders. |
| Server state | **TanStack Query 5** + `@tanstack/react-router-ssr-query` | Cache, dedupe, refetch, SSR hydration handled for us. |
| Client state | `useState` first; **Zustand 5** only when shared (S-20) | Smallest tool that works; no Redux / Jotai. |
| Forms + validation | **TanStack Form 1** + **Zod 4** | Zod works directly through Standard Schema (no resolver package). Same schemas validate route search params. |
| Styling + components | **Tailwind CSS 4** + **shadcn/ui** | Components are copied into the repo, so we own them and can tune them for Japanese typography. |
| API types | **@hey-api/openapi-ts** from `docs/openapi.yaml` | G-15: types come from the contract. Supports TS 6 (openapi-typescript does not yet). |
| i18n | **Paraglide JS 3** (`src/i18n/ja.json`, `src/i18n/en.json`) | Compile-time, typed, tree-shaken; official TanStack Start example; SSR locale drives `<html lang>`. |
| Unit / component tests | **Vitest 5** + **Testing Library** (jsdom) + **MSW 3** | G-24. MSW in tests only (S-31). |
| E2E | **Playwright** + **@axe-core/playwright** | G-24, G-43 / F-35–F-38. |
| Lint / format | **ESLint 9** (typescript-eslint, react-hooks, jsx-a11y, boundaries, i18next) + **Prettier** | Exactly what G-10 / G-12 / F-01 require. |
| Deploy | **Nitro** (`nitro/vite`) → Node server, or a host plugin | One build, many hosts (§7). |

Rejected for now, with the reason, so nobody re-litigates at 3 a.m.:
- **Next.js** — drops Vite; RSC model is heavier than this team needs.
- **Plain Vite SPA** — no SSR, slower first paint on judges' phones, `lang` only set after JS runs.
- **Redux Toolkit / RTK Query, Jotai** — TanStack Query + Zustand cover our needs with less code.
- **React Hook Form** — fine library, but needs `@hookform/resolvers`; TanStack Form matches the rest of the stack.
- **Biome / Oxlint as the linter** — no `jsx-a11y` / `boundaries` equivalents that G-10 / G-12 require.
- **Start RSC (server components)** — experimental in Start 1.x; off.

---

## 2. Versions

- **S-01** Pin these ranges in `frontend/package.json`. Commit `package-lock.json`.

| Package | Range | Latest on npm (2026-11-21) | Why not latest / note |
| :--- | :--- | :--- | :--- |
| Node.js | `.nvmrc` = `24`, `engines.node` = `^22.22.2 \|\| ^24.15.0 \|\| >=26.0.0` | 26 (LTS since Oct 2026) | jsdom 31 (through undici) needs exactly these ranges; on Node 23 Vitest crashes in `undici` (`createZstdDecompress` missing). 24 is LTS with the widest host support; 26 is fine too. Do not use odd versions (23, 25). |
| `react`, `react-dom` | `^19.3.1` | 19.3.1 | |
| `vite` | `^8.3.8` | 8.3.8 | Start requires Vite ≥ 8. |
| `typescript` | `~6.0.3` | 7.0.2 | `typescript-eslint@8.73` peer is `<6.1.0`; `jsx-a11y` lint needs it. Upgrade to 7 when typescript-eslint allows it (S-05). |
| `@tanstack/react-start` | `^1.169.7` | 1.169.7 | 1.169 is the first stable release. It pins `@tanstack/react-router` itself; do not add a different router version. |
| `@tanstack/react-query` | `^5.108.2` | 5.108.2 | |
| `@tanstack/react-router-ssr-query` | `^1.167.3` | 1.167.3 | Needs `react-router >= 1.170.33` (Start ships 1.170.46). |
| `@tanstack/react-form` | `^1.33.5` | 1.33.5 | **Not installed yet**; add with the first form. v2 is alpha; stay on 1. |
| `zod` | `^4.6.7` | 4.6.7 | **Not installed yet**; add with the first form or `validateSearch`. |
| `zustand` | `^5.1.0` | 5.1.0 | Install only when S-20 says so. |
| `tailwindcss`, `@tailwindcss/vite` | `^4.3.5` | 4.3.5 | The TanStack CLI installs both when Tailwind is selected. |
| `shadcn` (CLI, via `npx`) | `4.x` | 4.23.0 | Not a runtime dependency. `frontend/components.json` points it at `src/ui/v1/`. |
| `radix-ui`, `class-variance-authority`, `clsx`, `tailwind-merge` | `^1.8.1`, `^0.7.1`, `^2.1.1`, `^3.0.2` | 1.8.1, 0.7.1, 2.1.1, 3.7.1 | Runtime deps of the shadcn components in `ui/v1`. |
| `@vitejs/plugin-react` | `^6.3.0` | 6.3.0 | |
| `gsap`, `@gsap/react` | `^3.15.1`, `^2.1.2` | 3.15.1, 2.1.2 | v3 kit landing motion (entrance, scroll reveals, counter) and the ticker that drives Lenis. Free "no charge" license, plugins included. Only inside `gsap.matchMedia('(prefers-reduced-motion: no-preference)')` (F-38). Skill: `.claude/skills/gsap`. |
| `lenis` | `^1.3.27` | 1.3.27 | v3 kit smooth wheel scroll; touch stays native; honours reduced motion by default. `anchors: false` so the skip link keeps moving focus. Skill: `.claude/skills/lenis`. |
| `vanta`, `three` | `0.5.24`, `0.134.0` (exact) | 0.5.24, 0.187.0 | v3 landing hero background (WAVES). Vanta's last release targets three r134; newer three removed APIs it uses, so both are pinned. Lazy-loaded in their own chunk (about 153 KB gzipped), skipped under reduced motion or Data Saver. Skill: `.claude/skills/vanta`. |
| `lottie-web` | `5.13.0` (exact) | 5.13.0 | Plays the brand kit's one-shot moments (`public/brand/lottie/`: machine unlock on the Panel, the understood stamp and the kind check-wrong on the Worker's phone). Only the light SVG player (`build/player/lottie_light`), lazy-loaded where a moment plays; final frame, still, under reduced motion. |
| `d3-geo`, `d3-zoom`, `d3-selection`, `d3-transition`, `topojson-client` (+ their `@types/*` as dev) | `^3.1.2`, `^3.0.1`, `^3.0.1`, `^3.0.2`, `^3.1.0` | same | The interactive Japan map on the v3 landing (`ui/v3/components/JapanMap.tsx`): d3-geo draws the prefectures as SVG, d3-zoom + d3-transition glide between views (code-driven only, so the page scroll is never captured), topojson-client decodes `public/geo/japan-prefectures.topo.json` (Natural Earth, public domain). Small ISC modules, not the full `d3` bundle. Loaded by `services/geo.ts` → `features/map`. |
| `@inlang/paraglide-js` | `^3.2.2` | 3.2.2 | |
| `@hey-api/openapi-ts` | `0.99.0` (exact) | 0.99.0 | 0.x: minor bumps can break output, so pin exactly. |
| `vitest` | `^5.1.1` | 5.1.1 | |
| `@testing-library/react` | `^16.3.3` | 16.3.3 | |
| `@testing-library/jest-dom` | `^8.0.0` | 8.0.0 | **Not installed yet**; add with the first component test that needs DOM matchers (`toBeInTheDocument`). |
| `jsdom` | `^31.0.1` | 31.0.1 | Vitest test environment. |
| `msw` | `^3.0.7` | 3.0.7 | ESM-only; import from `msw` entrypoints (`msw/node`). |
| `@playwright/test` | `^1.65.1` | 1.65.1 | |
| `@axe-core/playwright` | `^4.14.0` | 4.14.0 | |
| `eslint`, `@eslint/js` | `^9.39.5` | 10.15.0 | `eslint-plugin-jsx-a11y@6.10.2` peer stops at ESLint 9. Upgrade when it declares 10. |
| `@types/node` | `^24.0.0` | 24.19.2 (in the 24 line) | Matches `.nvmrc`. |
| `js-yaml` (npm `overrides`) | `^4.3.2` | 4.3.2 | Not a direct dependency. Forces a fixed version under `@tanstack/start-plugin-core` → `xmlbuilder2` (high-severity advisory on 4.2.0). Remove when Start ships the fix. |
| `typescript-eslint` | `^8.73.0` | 8.73.0 | |
| `eslint-plugin-react-hooks` | `^7.1.1` | 7.1.1 | v7 includes the React Compiler rules. |
| `eslint-plugin-jsx-a11y` | `^6.10.2` | 6.10.2 | |
| `eslint-plugin-boundaries` | `^7.4.4` | 7.4.4 | Config in FRONTEND_ARCHITECTURE §3. |
| `eslint-import-resolver-typescript` | `^4.4.5` | 4.4.5 | Lets boundaries resolve `@/*` imports. |
| `eslint-plugin-i18next` | `^6.1.5` | 6.1.5 | |
| `prettier` | `^3.10.1` | 3.10.1 | |
| `nitro` | `3.0.261103-rc` (exact) | same | Release candidate; Start docs call the `nitro/vite` plugin "under active development". Pin exactly. |
| `@rolldown/plugin-babel`, `babel-plugin-react-compiler` | `^0.3.1`, `^1.0.0` | 0.3.1, 1.0.0 | **Optional**, only if the React Compiler is turned on (S-29). |

- **S-02** No package is added without a line in this table (name, range, why). Keeps the dependency list small and reviewed.
- **S-03** Security floor: `@tanstack/react-start >= 1.169`. Run `npm audit --omit=dev` before every demo build.
- **S-04** After scaffolding or any upgrade, run `npm install`, `npm run typecheck`, `npm run lint`, `npm run build`, `npm test`, and paste `npm ls --depth=0` into the PR (AGENTS §3.6 evidence).
- **S-05** **SHOULD:** Planned upgrades, each in its own PR: TypeScript 7 (when typescript-eslint supports it; faster `tsc`), ESLint 10 (when jsx-a11y supports it), Nitro 3 stable.

---

## 3. Setup (Hour 0)

Done on 2026-11-21. A fresh clone needs only the commands in [frontend/README.md](../frontend/README.md) (`npm install` also generates the Paraglide runtime through `postinstall`). Use **npm** (G-10 uses `npm run lint` / `npm run build`).

The script names in `frontend/package.json` are fixed so CI and agents can rely on them: `dev`, `build`, `start`, `typecheck`, `lint`, `format`, `test`, `test:e2e`, `gen:api` (options in `openapi-ts.config.ts`), `gen:i18n`. CI runs them in `.github/workflows/frontend-ci.yml`, and fails if `src/services/generated/` is stale against `docs/openapi.yaml`.

To add a shadcn component: `npx shadcn@latest add <name>` (it lands in `src/ui/v1/components/ui/`).

- **S-06** `tsconfig.json`: `"strict": true`, `"noUncheckedIndexedAccess": true`, path alias `@/*` → `src/*` (the CLI sets the alias). **SHOULD:** also `"exactOptionalPropertyTypes": true`; drop it if the first `npm run typecheck` fails inside `ui/*/components/` (shadcn) or `services/generated/`, which we do not hand-edit.
- **S-07** `.env.example` lists every variable with a dummy value. Only `VITE_`-prefixed variables reach the browser; never put a secret in one.

```bash
# frontend/.env.example
VITE_USE_MOCK=true
VITE_API_URL=http://localhost:8000
```

---

## 4. Folder layout

The layers are defined in [FRONTEND_ARCHITECTURE.md](FRONTEND_ARCHITECTURE.md) (replaceable UI kits, `A-01`…`A-09`); that file wins on layout. This section only says where the TanStack Start pieces go inside those layers.

```
frontend/
├── src/
│   ├── routes/               TanStack file routes = part of shell/ (S-08)
│   │   ├── __root.tsx        <html lang>, fonts, providers, kit Layout, error/404 boundaries
│   │   └── checkout.tsx      loader + validateSearch + head; renders kit.screens.checkout
│   ├── router.tsx            getRouter(): per-request QueryClient + SSR query integration (shell)
│   ├── shell/                ui-registry.ts (A-05), providers (QueryClient, Zustand stores)
│   ├── contract/ui-kit.ts    ScreenId + UiKit (A-03)
│   ├── ui/v1/                kit: screens/, components/ (shadcn copied here), tokens.css
│   ├── features/<feature>/   headless hooks: useQuery, TanStack Form, Zustand store factory
│   ├── services/             the ONLY code that talks to the network (G-12)
│   │   ├── api.ts            USE_MOCK toggle + typed functions per endpoint
│   │   ├── queries/          queryOptions per resource (keys + fetchers)
│   │   ├── mocks/            fixtures that mirror docs/openapi.yaml exactly
│   │   └── generated/        hey-api output — never edit by hand
│   ├── domain/               Zod schemas, types, formatters (date, ¥ — F-07)
│   ├── lib/                  IME-safe Enter, NFKC normalise (F-28, F-29)
│   ├── i18n/                 ja.json, en.json (Paraglide message files — S-26)
│   └── theme/base.css        Tailwind import + Japan typography floor (F-09–F-13, F-38)
├── tests/e2e/                Playwright journeys + axe
└── walkthrough/              feature map (G-25)
```

- **S-08** `src/routes/` is where TanStack Start looks for file routes, so it stays there and counts as `shell/`: add `{ type: 'shell', pattern: 'routes' }` and `{ type: 'shell', pattern: 'router.tsx', mode: 'file' }` to the boundaries elements in FRONTEND_ARCHITECTURE §3. Route files stay thin: `loader`, `validateSearch`, `head`, `pendingComponent`, `errorComponent`, and a `component` that renders the active kit's screen for its `ScreenId`. No markup, no business logic.
- **S-09** Data hooks follow A-09: route loaders (shell) prefetch through `services/queries`; feature hooks read the same `queryOptions`; kit screens only call feature hooks and never import `services/`.
- **S-10** shadcn components are copied **into the kit** (`ui/v1/components/`): set the aliases in `components.json` to `@/ui/v1/components` and `@/ui/v1/lib`. A new kit gets its own copy (A-04). Map Tailwind's `@theme` values to the kit's `tokens.css` variables (A-06).
- **S-35** The kit is chosen from the **request** URL, not `window.location`, because the first render happens on the server: call `pickKitId(location.searchStr, import.meta.env.VITE_UI_VERSION)` in the root route, then load it with `React.lazy` inside `<Suspense>`; streaming SSR resolves it on the server. Check the first `npm run build` that only the active kit's chunk is in the initial page (A-05).

---

## 5. Data flow: API, mocks, SSR

With SSR, route loaders run **on the server** for the first request and **in the browser** on client navigations. Everything in this section follows from that.

- **S-11** Base URL is absolute and comes from env (`VITE_API_URL`). A relative URL or the Vite dev proxy does not work for server-side fetches. FastAPI keeps CORS open for the frontend origin (AGENTS §4.2) because client navigations call it directly from the browser.
- **S-12** `src/services/api.ts` owns the `USE_MOCK` toggle (AGENTS §4.1). Mocks are **in-process fixtures**, so they behave the same on server and client:

```ts
// src/services/api.ts
import { mockItemList } from './mocks/items';
import type { ItemList } from './generated/types.gen'; // { items, page, page_size, total } — API_SPEC §1

const USE_MOCK = (import.meta.env.VITE_USE_MOCK ?? 'true') === 'true';
const BASE = import.meta.env.VITE_API_URL;

export class ApiError extends Error {
  constructor(public status: number, public code: string, message: string) {
    super(message);
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${path}`, init);
  const body = await res.json().catch(() => null);                 // a 502 from a proxy may not be JSON
  if (!res.ok || !body?.success) {                                  // envelope from docs/API_SPEC.md §1 (G-40)
    throw new ApiError(res.status, body?.error?.code ?? 'UNKNOWN', body?.error?.message ?? res.statusText);
  }
  return body.data as T;
}

export async function listItems(): Promise<ItemList> {
  if (USE_MOCK) return mockItemList;
  return request<ItemList>('/api/items');
}
```

- **S-13** Types for requests and responses come from `npm run gen:api` (hey-api reads `docs/openapi.yaml`). Hand-written API types are not allowed (G-15). A contract change regenerates, updates `services/mocks/`, and fixes the type errors in the same PR.
- **S-14** One `queryOptions` per read, in `src/services/queries/`. The key and the fetcher live together, so the loader and the component cannot drift:

```ts
// src/services/queries/items.ts
import { queryOptions } from '@tanstack/react-query';
import { listItems } from '../api';

export const itemsQuery = () =>
  queryOptions({ queryKey: ['items'], queryFn: listItems, staleTime: 30_000 });
```

- **S-15** Routes prefetch in the loader; feature hooks read with `useQuery` and return a view model with `loading`, `error` (with `retry`), empty and success states; the kit screen renders each state (A-01, A-02, F-31). The prefetch puts the data in the server-rendered HTML, so there is no loading flash on first paint. `prefetchQuery` never throws, so a failed request reaches the kit as the `error` state instead of a shell error page with markup the kit does not own. Working example: `src/routes/index.tsx`, `src/features/health/`, `src/ui/v1/screens/home/`.

```ts
// src/routes/items.tsx (shell)
export const Route = createFileRoute('/items')({
  loader: ({ context }) => context.queryClient.prefetchQuery(itemsQuery()),
  component: () => <KitScreen screen="items" />,
});
// src/features/items/index.ts
export function useItems() {
  const { data, isPending, isError, refetch } = useQuery(itemsQuery());
  if (isPending) return { state: 'loading' } as const;
  if (isError) return { state: 'error', retry: () => void refetch() } as const;
  return data.total === 0 ? ({ state: 'empty' } as const) : ({ state: 'ready', items: data.items } as const);
}
// src/ui/v1/screens/items/ItemsScreen.tsx renders useItems()
```

  A `useQuery` without a loader prefetch is **not** server-rendered; skip the prefetch only for data that may appear after first paint.
- **S-16** Set `staleTime` (default `30_000` on the QueryClient). With the default `0`, every query refetches right after hydration.
- **S-17** The `QueryClient` is created **inside** `getRouter()` (one per SSR request), never at module scope. A module-level client leaks one user's data into another user's HTML:

```ts
// src/router.tsx
export function getRouter() {
  const queryClient = new QueryClient({ defaultOptions: { queries: { staleTime: 30_000 } } });
  const router = createRouter({ routeTree, context: { queryClient }, scrollRestoration: true });
  setupRouterSsrQueryIntegration({ router, queryClient });
  return router;
}
```

- **S-18** Writes use `useMutation`, then `queryClient.invalidateQueries({ queryKey: [...] })`. Optimistic updates only where the demo needs instant feedback (F-30), and they must roll back on error.
- **S-19** **SHOULD:** Start server functions (`createServerFn`) are for things that must run on the frontend server: reading an httpOnly cookie, hiding a third-party key. Business logic and data stay in FastAPI. If a server function is added, it still calls through `src/services/` so the mock toggle keeps working.

---

## 6. State management

Pick the **first** row that fits. Most state never gets past row 3.

| # | Kind of state | Example | Tool |
| :--- | :--- | :--- | :--- |
| 1 | Data from the API | items, user profile, AI result | TanStack Query (§5) |
| 2 | Anything that should survive reload or be shareable by URL | filters, sort, tab, page, search text, wizard step | Router **search params** with `validateSearch` (Zod) |
| 3 | Form input and validation | sign-up, booking form | TanStack Form + Zod |
| 4 | Local UI in one component or a parent and its children | open/closed, hover, selected row | `useState` / `useReducer` |
| 5 | Client-only state shared by distant components | 入力→確認 draft across routes, theme, toast queue | Zustand (S-20) |

- **S-20** Zustand only when rows 1–4 do not fit **and** two or more components far apart in the tree need the state. With SSR, the store is created per request (`createStore` from `zustand/vanilla`) and provided through React context. Never `export const useStore = create(...)` at module scope:

```ts
// src/features/checkout/draft-store.ts (provider mounted in shell/)
import { createStore } from 'zustand/vanilla';
export type DraftState = { name: string; setName: (v: string) => void; reset: () => void };
export const createDraftStore = () =>
  createStore<DraftState>()((set) => ({
    name: '',
    setName: (name) => set({ name }),
    reset: () => set({ name: '' }),
  }));
// Provide one instance per request with createContext + useRef; read it with useStore(store, selector).
```

- **S-21** Never copy server data into Zustand or `useState`. Read it from the query cache; derive values while rendering.
- **S-22** Never sync state with `useEffect` (state A changes → effect sets state B). Derive B while rendering, or move the code into the event handler.
- **S-23** State is immutable (G-08): return new objects in `set`, reducers and query updaters.
- **S-24** Client-only values (`localStorage`, `window.matchMedia`) are read after mount or with `ClientOnly`, never during SSR render. Otherwise hydration mismatches.
- **S-25** The F-26 flow (入力 → 確認 → 完了) lives in the feature hook (A-02), never in a kit. Keep the draft in one TanStack Form instance when all steps are on one route (step in a search param); use a Zustand draft store (S-20) only when the steps are separate routes. Clear the draft after 完了.

---

## 7. Rendering, routing, deploy

- **S-26** `__root.tsx` sets `<html lang>` from the request locale (Paraglide), loads Noto Sans JP with `display=swap` in `head()`, and sets `notFoundComponent` to the active kit's `notFound` screen, so 404 is translated (F-01, F-09, F-31, F-40). **Not built yet:** a root `errorComponent`; until it exists an unexpected crash shows TanStack's default English message. Paraglide reads the message files from `src/i18n/{locale}.json` (set `pathPattern` in `project.inlang/settings.json`), so A-06's single copy owner still holds. Feature hooks return message keys; kits render them (A-02).
- **S-27** Loading, error (polite message + retry button) and empty states are rendered by the kit screen from the feature hook's view model (S-15, F-31), not by route `pendingComponent`/`errorComponent`, because shell markup cannot follow a kit swap. The root keeps only the 404 screen and, once built, a last-resort `errorComponent` (S-26).
- **S-28** Search params are validated with a Zod schema in `validateSearch`; never read `window.location` directly (S-35 uses the router location too).
- **S-29** **SHOULD:** React Compiler is optional. If the team turns it on, use the established Babel path: install `@rolldown/plugin-babel` + `babel-plugin-react-compiler` and add the Babel plugin **before** `viteReact()` in `vite.config.ts` (in the other order the compiler silently does nothing). The oxc `react({ compiler: true })` option is still experimental. Do not hand-write `useMemo`/`useCallback` "just in case" either way.
- **S-30** Deploy: `nitro()` from `nitro/vite` is in `vite.config.ts` (`plugins: [paraglideVitePlugin(…), nitro(), tailwindcss(), tanstackStart(), viteReact()]`). `npm run build` writes `.output/`; `npm start` runs `node .output/server/index.mjs` on any Node host (Render, Railway, Docker). Vercel and Netlify work through Nitro or their own Start plugins; Cloudflare Workers through `@cloudflare/vite-plugin`. Pick the host at kickoff and record it here.

---

## 8. Testing

- **S-31** Unit and component tests: Vitest (`environment: 'jsdom'`) + Testing Library, colocated as `*.test.tsx`. API calls in tests are mocked with MSW 3 (`setupServer` from `msw/node`) at the HTTP level, so `services/api.ts` itself is exercised. MSW is **not** used in the running app; the app's mock mode is S-12.
- **S-32** Required unit tests: every Zod schema with Japanese edge cases (full-width digits, NFKC — F-28), the IME Enter guard (F-29), formatters (F-07), every Zustand store's actions, and every feature hook (rendered with `renderHook`, no kit — A-08).
- **S-33** Playwright e2e covers every demo journey against `VITE_USE_MOCK=true`, runs axe on each page (G-43), checks a 375px viewport (F-24), and asserts no `undefined`, `NaN` or `lorem` text appears (F-42).
- **S-34** **SHOULD:** Before the final demo, run the e2e suite once with `VITE_USE_MOCK=false` against the live backend.

---

## 9. Rules → checks (G-01)

| Item | Check |
| :--- | :--- |
| S-01, S-02 | PR review: diff of `package.json` must touch this file's table |
| S-04 | CI runs `npm ci && npm run typecheck && npm run lint && npm run build && npm test` (G-10) |
| S-06 | `tsc` with strict flags in `npm run typecheck` |
| S-08, S-09 | `eslint-plugin-boundaries` config from FRONTEND_ARCHITECTURE §3, plus `routes/` and `router.tsx` as `shell` elements (S-08) |
| S-09, S-11 | `no-restricted-globals: ['fetch']` everywhere except `src/services/**`; `no-restricted-imports` bans `axios` |
| S-13 | CI runs `npm run gen:api` and fails if `git diff --exit-code src/services/generated` is not clean |
| S-17, S-20 | `no-restricted-syntax`: ban `new QueryClient` outside `src/router.tsx`; ban `create(` imported from `zustand` (only `createStore` from `zustand/vanilla`) |
| S-22 | `eslint-plugin-react-hooks` v7 recommended rules (includes `set-state-in-effect`) |
| S-28, S-35 | `no-restricted-properties`: ban `window.location` reads outside `src/lib/` |
| S-31–S-33 | `npm test`, `npm run test:e2e` in CI |
| F-01 | `eslint-plugin-i18next` `no-literal-string` on `src/ui/**` |

---

## Sources

- TanStack Start 1.0 announcement (2026-10-13): https://tanstack.com/blog/tanstack-start-1-0
- TanStack Start docs — overview, hosting, server components, migration from RC: https://tanstack.com/start/latest/docs/framework/react/overview , https://tanstack.com/start/latest/docs/framework/react/guide/hosting , https://tanstack.com/start/latest/docs/framework/react/guide/server-components , https://tanstack.com/start/latest/docs/framework/react/migrate-from-release-candidate
- TanStack Start + Query guide (`getRouter`, `setupRouterSsrQueryIntegration`): https://tanstack.com/start/latest/docs/framework/react/guide/tanstack-query ; Router integration: https://tanstack.com/router/latest/docs/integrations/query
- TanStack Router i18n (Paraglide): https://tanstack.com/router/latest/docs/guide/internationalization-i18n ; Paraglide: https://github.com/opral/paraglide-js
- Vite 8 (Rolldown): https://vite.dev/blog/announcing-vite8 ; Rolldown 1.0: https://voidzero.dev/posts/announcing-rolldown-1-0
- React 19.2 / Compiler 1.0: https://react.dev/blog/2025/10/01/react-19-2 , https://react.dev/learn/react-compiler/installation ; plugin-react v6 + Compiler: https://recca0120.github.io/en/2026/04/14/react-compiler-vite-v6/ , https://oxc.rs/blog/2026-08-18-react-compiler-support
- TypeScript 7.0: https://devblogs.microsoft.com/typescript/announcing-typescript-7-0/
- shadcn/ui on TanStack Start: https://ui.shadcn.com/docs/installation/tanstack ; Tailwind on Start: https://tailwindcss.com/docs/installation/framework-guides/tanstack-start
- TanStack Form v1 (Standard Schema): https://tanstack.com/blog/announcing-tanstack-form-v1
- Zustand SSR per-request stores: https://zustand.docs.pmnd.rs/guides/nextjs
- State-management landscape 2026 (secondary): https://www.pkgpulse.com/guides/state-of-react-state-management-2026
- OpenAPI codegen comparison: https://dev.to/nyaomaru/which-openapi-codegen-should-you-choose-openapi-typescript-vs-hey-api-vs-orval-vs-kubb-100p
- Vitest 4 browser mode / Vitest 5 migration: https://voidzero.dev/posts/announcing-vitest-4 , https://main.vitest.dev/guide/migration
- MSW 3.0 release notes: https://newreleases.io/project/github/mswjs/msw/release/v3.0.0
- ESLint 10 / linter comparison: https://infoq.com/news/2026/04/eslint-10-release , https://tech-insider.org/eslint-vs-biome-vs-oxlint-2026/
- Node.js release schedule: https://nodejs.org/about/releases/
- Versions and peer ranges: `npm view <pkg> version peerDependencies`, run 2026-11-21.
