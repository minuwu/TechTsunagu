# Frontend Architecture — Replaceable UI

The UI of this project will change drastically, and several UI versions may exist side by side (for example `v1` for the demo, `v2` as a redesign, a simplified version for a specific audience). The frontend is therefore split so that **a redesign replaces presentation only**. Logic, data access, copy, routes and Japan-facing rules are written once and never rewritten for a new look.

Items are numbered `A-xx` so reviews and PRs can cite them, like `F-xx` in [FRONTEND_REGULATIONS.md](FRONTEND_REGULATIONS.md) and `G-xx` in [ENGINEERING_GUIDELINES.md](ENGINEERING_GUIDELINES.md). All items are MUST.

**The deletion test.** Delete `src/ui/<version>/` and remove it from the registry. Everything else must still compile, and every non-e2e test must still pass. If it does not, logic has leaked into the UI.

The rules are stack-neutral (Vite + React or Next.js). Stack-specific notes are marked.

---

## 1. Layers

```text
frontend/src/
├── shell/              # Composition root: entry, providers, route table, UI-kit registry
├── contract/
│   └── ui-kit.ts       # UiKit type: the layout and screens every UI version must supply
├── ui/                 # Presentation only. One folder per UI version ("kit")
│   ├── v1/
│   │   ├── index.ts    # export default { Layout, screens } satisfies UiKit
│   │   ├── tokens.css  # This kit's colours, spacing, radius, shadows
│   │   ├── screens/    # One folder per ScreenId; screens never import each other
│   │   └── components/ # This kit's buttons, cards, modals, layouts
│   └── v2/             # Same shape, completely different look
├── features/<feature>/ # Headless behaviour: view-model hooks. No JSX markup, no styles, no copy
├── services/           # The only code that talks to the network (api.ts, USE_MOCK, mocks)
├── domain/             # Types, schemas, pure rules, formatters (date, ¥). No React
├── lib/                # Product-agnostic helpers (IME-safe Enter, NFKC normalise, reduced motion)
├── i18n/               # ja.json, en.json. Shared by every kit
└── theme/
    └── base.css        # Japan typography floor (F-09–F-13, F-38). Imported by every kit
```

**Next.js:** do not name the composition root `src/app/` or `src/pages/`; those are routing folders. Next.js route files under `app/` stay thin: each one renders the active kit's screen for its `ScreenId`, and `shell/` holds everything else.

## 2. Rules

- **A-01 Presentation lives only in `ui/<version>/`.** JSX layout, styles, class names, icons, images and animation. Screens render what a feature hook returns. No fetch, no validation, no business rules, no data shaping beyond display formatting. Screens never import each other; shared pieces go in the kit's `components/`. *Enforced by: lint* (`boundaries/dependencies`; `no-restricted-globals` bans `fetch` outside `services/`).
- **A-02 Behaviour lives in headless feature hooks.** Each screen's state, data loading, validation, step flow (`入力 → 確認 → 完了`, F-25/F-26), submit and error mapping live in `features/<feature>/`, exported through its `index.ts`. Hooks return plain data and handlers. Messages are returned as i18n keys, and the kit renders `t(key)`. Features never import each other; logic two features share moves to `domain/` or `lib/`.

  ```ts
  // features/checkout/index.ts — what every kit's checkout screen consumes
  export interface CheckoutViewModel {
    step: 'input' | 'confirm' | 'done';
    values: CheckoutForm;            // from domain/
    errors: Partial<Record<keyof CheckoutForm, MessageKey>>;
    isSubmitting: boolean;
    setField: <K extends keyof CheckoutForm>(key: K, value: CheckoutForm[K]) => void;
    next: () => void;
    back: () => void;
    submit: () => Promise<void>;
  }
  export function useCheckout(): CheckoutViewModel { /* ... */ }
  ```

- **A-03 One UI contract.** `contract/ui-kit.ts` lists the screens and the layout. Each kit default-exports an object that `satisfies UiKit`, so TypeScript fails until a kit implements every screen.

  ```ts
  import type { ComponentType, ReactNode } from 'react';

  export type ScreenId = 'home' | 'checkout'; // one per route; extend when a screen is added
  export interface UiKit {
    Layout: ComponentType<{ children: ReactNode }>;
    screens: Record<ScreenId, ComponentType>;
  }
  ```

  Adding a screen: add the `ScreenId`, add the route in `shell/`, add the feature hook, then implement the screen in every registered kit.

- **A-04 Kits never import each other.** If two kits need the same visual component, copy it. Kits are meant to diverge, so this is the one accepted exception to G-05. If the shared part is logic, move it to `features/`, `domain/` or `lib/` instead. Start a new version with `cp -r src/ui/v1 src/ui/v2`, register it (A-05), then rewrite it freely. *Enforced by: lint* (`boundaries/dependencies`).
- **A-05 One registry picks the kit.** `shell/ui-registry.ts` is the only file that references `ui/*`, and only through dynamic imports, so an unused kit never ships in the active kit's bundle (F-40, F-41).

  ```ts
  const kits = {
    v1: () => import('../ui/v1'),
    v2: () => import('../ui/v2'),
  } satisfies Record<string, () => Promise<{ default: UiKit }>>;

  type KitId = keyof typeof kits;
  const DEFAULT_KIT: KitId = 'v1';

  const isKitId = (id: string | null | undefined): id is KitId =>
    id != null && Object.hasOwn(kits, id);

  // ?ui= wins over the build default; unknown values fall back, never a blank page (F-42)
  export function pickKitId(search: string, buildDefault?: string): KitId {
    const fromUrl = new URLSearchParams(search).get('ui');
    if (isKitId(fromUrl)) return fromUrl;
    if (isKitId(buildDefault)) return buildDefault;
    return DEFAULT_KIT;
  }

  export const loadKit = (id: KitId) => kits[id]();
  ```

  The build default comes from `VITE_UI_VERSION` (Next.js: `NEXT_PUBLIC_UI_VERSION`), listed in `.env.example`. `?ui=` is read once at startup, for side-by-side comparison. A deployed demo sets the env var, not the query parameter. Load the kit with `React.lazy` (Vite) or `next/dynamic` (Next.js).
- **A-06 Shared things are owned once, outside the kits.**

  | Concern | Owner | Why it is not per kit |
  | :--- | :--- | :--- |
  | URLs and the route table | `shell/` | Links, QR codes (F-46) and e2e specs survive a redesign |
  | Copy | `i18n/ja.json`, `en.json` | Keys name the meaning (`checkout.confirm.submit`), never the layout (`v1.blueBox.title`). A kit may add keys; it never forks the wording of an existing one (F-01, F-02) |
  | Types, schemas, formatters | `domain/` | One date and ¥ format everywhere (G-05, F-07) |
  | Network, mocks, `USE_MOCK` | `services/` | AGENTS.md §4.1 |
  | Typography floor | `theme/base.css` | F-09–F-13 cannot be redesigned away |

  A kit owns colour, spacing, radius, shadows, layout and components, defined as CSS custom properties in its `tokens.css`. Components use the tokens, never raw hex or pixel values (Tailwind: map the theme to these variables).
- **A-07 Every `F-xx` applies to every kit.** `theme/base.css` sets the font stack, 16px body, line heights and `prefers-reduced-motion` once (F-09–F-13, F-38). A kit becomes the default only after it passes the pre-demo checklist in [FRONTEND_REGULATIONS.md](FRONTEND_REGULATIONS.md).
- **A-08 Tests follow the layers.** Logic tests (Vitest, G-24) target feature hooks, `domain/` and `lib/`, so they survive any redesign. Playwright specs select by role, label and i18n text, never by CSS class or DOM shape, so one journey spec runs against any kit with `?ui=<id>`. Run e2e against the default kit at minimum. **SHOULD:** one Playwright project per registered kit.
- **A-09 Import directions are enforced by lint (G-12).** *Enforced by: lint* (`frontend/eslint.config.js`).

  | Layer | May import |
  | :--- | :--- |
  | `shell/` | everything; `ui/*` only through the registry (A-05) |
  | `ui/<v>/` | the same `ui/<v>/`, `contract/`, `features/`, `domain/`, `lib/`, `i18n/`, `theme/` |
  | `ui/<v>/screens/<s>/` | the same as `ui/<v>/`, but not another screen |
  | `features/<f>/` | the same feature, `services/`, `domain/`, `lib/`, `i18n/` (key types only) |
  | `services/` | `domain/` |
  | `contract/`, `domain/`, `lib/` | nothing outside themselves (`contract/` uses React types only) |

## 3. Enforcement (G-01, G-12)

Wired in `frontend/eslint.config.js` with `eslint-plugin-boundaries` v7 (rule `boundaries/dependencies`). Proved on 2026-11-21: a temporary `ui/v2 → ui/v1` import, a `ui/* → services/` import and a `fetch` in `features/` each failed `npm run lint`; after removing them, lint passed. `src/routes/`, `src/router.tsx` and `src/server.ts` count as `shell/`.

```js
// eslint.config.js (excerpt)
import boundaries from 'eslint-plugin-boundaries';

const sameVersion = { version: '{{from.element.captured.version}}' };
const core = ['contract', 'feature', 'domain', 'lib', 'i18n', 'theme'];

export default [
  {
    files: ['src/**/*.{ts,tsx}'],
    plugins: { boundaries },
    settings: {
      'import/resolver': { typescript: { alwaysTryTypes: true } }, // without it, aliased imports are not checked
      'boundaries/elements': [ // most specific first
        { type: 'screen', pattern: 'ui/*/screens/*', capture: ['version', 'screen'] },
        { type: 'ui', pattern: 'ui/*', capture: ['version'] },
        { type: 'feature', pattern: 'features/*', capture: ['feature'] },
        { type: 'shell', pattern: 'shell' },
        { type: 'contract', pattern: 'contract' },
        { type: 'services', pattern: 'services' },
        { type: 'domain', pattern: 'domain' },
        { type: 'lib', pattern: 'lib' },
        { type: 'i18n', pattern: 'i18n' },
        { type: 'theme', pattern: 'theme' },
      ],
    },
    rules: {
      ...boundaries.configs.recommended.rules,
      'boundaries/dependencies': [2, {
        default: 'disallow',
        policies: [
          { allow: { dependency: { relationship: { to: 'internal' } } } },
          { from: { element: { type: 'shell' } },
            allow: { to: { element: { types: ['ui', ...core, 'services'] } } } },
          { from: { element: { types: ['ui', 'screen'] } },
            allow: { to: { element: { types: core } } } },
          { from: { element: { types: ['ui', 'screen'] } },
            allow: { to: { element: { type: 'ui', captured: sameVersion } } } },
          { from: { element: { type: 'ui' } },
            allow: { to: { element: { type: 'screen', captured: sameVersion } } } },
          { from: { element: { type: 'feature' } },
            allow: { to: { element: { types: ['services', 'domain', 'lib', 'i18n'] } } } },
          { from: { element: { type: 'services' } },
            allow: { to: { element: { type: 'domain' } } } },
        ],
      }],
    },
  },
];
```

Needs `eslint-plugin-boundaries`, `@typescript-eslint/parser` and `eslint-import-resolver-typescript` as dev dependencies. Docs: https://www.jsboundaries.dev/docs/quick-start/ , https://www.jsboundaries.dev/docs/guides/typescript-support/ , https://www.jsboundaries.dev/docs/policies/

## 4. Review checklist

- [ ] No fetch, validation or business rule inside `ui/` (A-01)
- [ ] New behaviour is a hook in `features/`, tested without rendering a kit (A-02, A-08)
- [ ] New screen added to `ScreenId` and implemented in every registered kit (A-03)
- [ ] No import between kits, and no `ui/ → services/` import (A-04, A-09; lint passes)
- [ ] New copy is an i18n key named by meaning; no raw hex or px in kit components (A-06)
- [ ] The kit that is the default passes the pre-demo checklist (A-07)
