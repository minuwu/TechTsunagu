# Arjun's Work Log

Owner: Arjun Saha (GitHub handle TBD) — Role: Frontend design

Update after each work session. Newest entry first. Bangladesh time (UTC+6). Link commits/PRs/XREQs.

## 2026-11-23 (frontend ↔ backend connection check)

### Done
- `npm run check:api` (frontend): health, database readiness, CORS for `http://localhost:3000`, deployed contract vs `docs/openapi.yaml`, demo sign-in round trip (roster, login, Bearer `me`, `{items}` list, logout). Production: all pass, 67 paths in step with the repo.
- Real browser on `localhost:3000` → hosted API: Foreman sign-in, Board, ETag readable from JS, `If-None-Match` → 304, logout. All work.
- Links recorded: `frontend/.env.example` (hosted API URL, commented), `docs/FRONTEND_API_HANDOVER.md` (the check command).

### Not connected yet (owners)
- The app's screens still use the mock store: the client needs XREQ-001 (Bearer sign-in instead of shared PINs, `{items, nextCursor}` unwrap, path changes, Idempotency-Key). Owner Minhaz; not started (no branch).
- Hosted frontend: XREQ-013 (Asif, Azure Static Web Apps). When it is up, its origin must be added to the API's CORS (`backend/deploy/set-cors.ps1`), then `npm run check:api -- <api> <frontend origin>`.

## 2026-11-23 (interactive Japan map, static demo build)

### Done
- Landing hero: interactive map of Japan's 47 prefectures (d3-geo SVG over the Vanta sea): hover names a prefecture (ja / en), click glides to it (d3-zoom + d3-transition), 全国 / All Japan and Nagasaki buttons, an intro glide to Nagasaki with a pin on Sasebo, a live caption; scroll never zooms; reduced motion jumps. Map data: Natural Earth (public domain); photo shown on Nagasaki: Megane Bridge by Yoshi Canopus, CC0 (Wikimedia Commons). Sources in `frontend/public/geo/README.md` and `frontend/public/media/README.md`.
- `npm run build:static`: the whole demo pre-rendered (39 pages) for a static host; verified with a plain file server, core loop included.
- e2e 12/12 (new map test), axe clean.

### Next
- Live link: asked Asif to host the static build on Azure Static Web Apps (**XREQ-013**); put the URL in the slides and README when it is up.

## 2026-11-23 (Bengali dropped from the frontend; team decision, Minhaz agrees)

### Done
- Frontend is Japanese + English only: `Lang` is `'ja' | 'en'`; `bn` removed from the i18n locales (`bn.json` deleted), the language switchers and the Worker's content toggle in all three kits, the Hind Siliguri font and its `:lang(bn)` rules, all mock data and the 19 course files (2,693 `t()` calls; done with the TypeScript parser, not regex), and every screen (Bengali lines dropped where English already showed; English shown where Bengali was the only second language: notice steps, Near-miss text, QR stickers). Demo Workers Rahim, Sadia, Tanvir now read English. Copy that promised Bengali (landing, record, notices, near-misses) is rewritten in ja and en; the competitor fact about カミナシ supporting Bengali stays.
- e2e: a hydration-safe `resetDemo()`; the flyer and new-version flows now really reset the demo (they never confirmed the dialog before). 11/11 in CI mode with axe.

### Still Bengali (other owners)
- Backend and AI (Galib; Asif/Anim): prompts, `AiTri`, `AI_LANGUAGES`, glossary seed and seed data still produce `bn`. The frontend ignores the extra field.
- Android plan (Minhaz): D-08 bundled Bengali font, D-13 ja/en/bn strings, WP-0C and WP-QA checks.
- Prototype (`prototypes/wakatta`): still has the JA / EN / BN switch.

## 2026-11-23 (Nagasaki porcelain redesign, branch `feat/techtsunagu-ui`)

### Done (each milestone pushed; PR not opened yet)
- M0 tokens: Porcelain by day (default), harbour night after dark, Site cobalt `#1351AA` as the one action colour, sea-glass for Life, vermilion as a seal accent only (red means Lock here). One `useTheme()` hook. [docs/design-system.md](../../docs/design-system.md).
- M1 shell: Manrope for Latin ahead of Noto Sans JP (F-09 note updated), footer, navy header.
- M2 signature moment: the Panel plays the brand `machine-unlock` Lottie on a live Locked to Ready change; the Worker's phone stamps `understood-stamp`; `check-wrong` on a fail (`lottie-web` light player, lazy).
- M3 brand: favicons, manifest, theme colour, share cards; Nekko role art on the hub, PIN gates and 404.
- M4 states: connection band (error / offline) with retry, Panel UNKNOWN, empty states; demo switcher `?demo=loading|empty|error|live` (mock only).
- M5 responsive: no overflow at 375 / 430 / 768 / 1024 / 1440; 44 px targets on the Board links; tablet Foreman tabs wrap.
- M6: axe clean on every screen in both themes and the error states; merged `main` (no conflicts); e2e 11/11.

### Next
- Open the PR from `feat/techtsunagu-ui` (description in the session handoff); Minhaz reviews the default-theme change (v3 night to porcelain) and the brand wiring.
- Native check of the new ja / bn strings (data_error, data_offline, panel_unknown, board_empty, hub_empty, demo_*).

## 2026-11-23 (motion: Lenis, GSAP, Vanta in v3)

### Done
- **Lenis** smooth wheel scroll for the v3 kit, driven by GSAP's ticker (ScrollTrigger in sync); touch native; `anchors: false` so the skip link still moves focus; `scrollToSection()` for smooth in-page links that also move focus; `data-lenis-prevent` on the confirm dialog.
- **Vanta WAVES** behind the landing hero ("Nagasaki harbour at night", dark and light colours follow the theme toggle). three r134 + Vanta lazy-loaded in their own chunk (about 153 KB gzipped); skipped under reduced motion or Data Saver; a frame-rate guard (under 24 fps for 2 s) falls back to the still sea; a veil keeps hero text at panel contrast.
- **GSAP** landing motion: hero entrance after the splash, scroll reveals (opacity only, so nothing leaves the accessibility tree), count-up on the sourced 46.2%. All inside `gsap.matchMedia('(prefers-reduced-motion: no-preference)')`.
- Fixed: the hero's "How it works" link pointed at `#how`, which no element had.
- Skills for agents: `.claude/skills/lenis`, `gsap`, `vanta` (setup, theme options, a11y rules, pitfalls). FRONTEND_STACK S-02 lines added; `three` and `vanta` pinned exactly.
- e2e runs with `reducedMotion: 'reduce'` (axe never sees a half-faded element); new test "landing with motion on" checks every reveal ends at full opacity. 11/11 in CI mode.

### Next
- The brand task below puts a 5-slide hero carousel on the landing page: decide with Minhaz whether the slides replace the Vanta waves or sit on top of them (waves as the slide backdrop).

## 2026-11-23 (brand kit: wire it and make it better, from Minhaz)

The Brand kit is on `main` in [`frontend/public/brand/`](../../frontend/public/brand/): the mascot **Nekko**, app icons, favicons, 22 Lottie animations, hero / footer / spot / social images, logos and tokens, each in two blues (`#1351AA` primary, `.iconblue` twin `#246DD4`). Open `frontend/public/brand/index.html` in a browser to skim everything. Start with [docs/brand/README.md](../../docs/brand/README.md); rules are in [SPEC.md](../../docs/brand/SPEC.md), the decision in [ADR 0013](../../docs/adr/0013-brand-kit-mascot-two-blues-vector-only.md). This comes **before** the entries below, after you finish what you have open.

### Next (assigned)
1. **Wire it in** (v3, the default kit; presentation only, A-01…A-09):
   - Favicons, PWA manifest and theme colour: paste `favicon/head-snippet.html` into the root route head (`src/routes/__root.tsx`); add Open Graph / Twitter image tags (`images/social/og-default-1200.png`).
   - Brand mark: Nekko / `identity/logo-*.svg` in the header, the PIN gate and the landing page.
   - Landing (`Landing.tsx`): the 5 hero slides as a carousel (`images/hero/`, desktop and a separate mobile file; keep the left 45% / top 40% free for HTML text in ja / en / bn, no text is baked into images), the footer strip and CTA banner, the dividers.
   - Spot illustrations: the four roles (`role-*`), the onboarding steps (`step-*`), and the empty / no-results / offline / 404 / error / success states (`state-*`).
   - Lottie: add a player (`lottie-web` or `@lottiefiles/dotlottie-react`) and use `lottie/core/` for loading, success, fail and saving, `lottie/flow/` for the machine unlock, Check results and nudge, `lottie/empty/` for empty and error screens. Honour `prefers-reduced-motion` (show a still frame). Mind the bundle: load Lottie JSON lazily.
2. **Change or enhance the Brand kit as you see fit.** It is a first pass: edit, redraw, replace or delete anything that does not work in the real screens. Keep: one blue per file (`#1351AA`, so `docs/brand/tools/recolor.py` can rebuild the Icon-blue twins), no text baked into images, shape-layer-only Lottie that passes `tools/lottie-check.cjs`, and a short note in `docs/brand/manifests/` when you change a group. `docs/brand/tools/generators/` has the scripts that built the Lottie, icons and scenes (reference, not turnkey). After changes run `recolor.py frontend/public/brand --render` then `gallery.py`. Known weak spots to start with: `logo-white` / `logo-black` (Nekko is an outline only), a faint smile on the 16 px favicon, hero slides 2 and 3 are light on cobalt, the `surprised` expression mouth overlaps the nose, and Nekko is small on mobile hero slides.
3. Check every screen you touch against FRONTEND_REGULATIONS (F-01…F-53) and axe: alt text (`alt=""` for decorative art), contrast, reduced motion, 1280×800 / 1024×768 / 390×844.

### Blockers / handoff
- Minhaz's **v4 "Poster Modernist" kit** (cream / jet / cobalt, `ui/v4/`) is not on `main` yet, so wire into v3. The Brand kit is kit-independent: it moves to v4 later by swapping markup only.
- Ask Minhaz before changing the Nekko master (`brand/master/`): every other asset is built from it.

## 2026-11-23 (desktop layouts, v3)

### Done
- Kit agreed: desktop layouts go into **v3** (the default kit). `.env.example` now says `VITE_UI_VERSION=v3` (it still said v2, so a fresh setup got the old kit).
- From 1024 px: the four app tabs sit in the header and the floating tab bar is phone-only; the Foreman screens get a sticky left sidebar (nav + lock).
- Board: machines left, flags and Confusions in a right column (≥ 1280 px). Assign: machine cards in two columns. Glossary: the add form is a sticky column beside the table (≥ 1280 px), Japanese terms no longer break mid-word. Near-misses: two-column grid. Briefing, Senpai / Support Log and the Poster notice editor already had two columns.
- Presentation only (A-01…A-09): `PinGate` got an optional `frame` prop so the Foreman console can place the lock button; no `features/` change.
- Check: 1280×800, 1024×768, 390×844 with no sideways scroll; ja / en / bn header fits at 1024; focus order matches the visual order. New e2e "Foreman screens fit 1024px"; Playwright (CI mode) 10/10 with axe green on all 16 screens; typecheck and lint clean.
- e2e: PIN unlock helper retries until the PIN sticks (typing before hydration left the field empty and the test timed out).
- Bengali font: **Hind Siliguri** replaces Noto Sans Bengali (web app `base.css` + `__root.tsx`, prototype, F-09). `:lang(bn)` puts it first so Bengali and its Latin match; Japanese still falls back to Noto Sans JP. Fixed a conflict: the kits' `.display` tracking overrode `letter-spacing: 0` on Bengali headings.

### Next
- Chat (Thread list + chat side by side) and Task Board: no `features/` yet (Minhaz, XREQ-005). Lay them out once the hooks land.
- Item 2: public landing page. v3 already has `Landing.tsx` on the home route from Minhaz; check it against BACKEND_PLAN §2 decisions 14 and 15 before building more.

## 2026-11-23 (web frontend, from Minhaz)

Minhaz asks you to take the **web frontend**. It comes **before** the items in the 2026-11-22 entry below; those stay yours, after this.

### Next (assigned)
1. **Desktop web layouts of the React app** (first). Today only `src/ui/v3/tokens.css` has breakpoints; v1 and v2 are phone-only. Workers stay phone-first; the Foreman and Poster get desktop and tablet layouts at ≥ 1024 px:
   - Foreman Board, chat (Thread list + chat side by side), Task Board (Kanban columns side by side), Skill Matrix, Confusion Heatmap, Support Log, Glossary, Assign, Briefing, Near-miss list; Community Poster notice editor.
   - Presentation only, inside the kit (`src/ui/<version>/`): same routes, same `features/` hooks, no logic copied (FRONTEND_ARCHITECTURE A-01…A-09). Data stays mock until XREQ-001/005 land.
   - Check at 1280×800, 1024×768 and 390×844; nothing clipped; keyboard navigation works; axe green; FRONTEND_REGULATIONS F-01…F-53.
2. **Public web site** (second): a landing page for judges and 登録支援機関 at a public route in the same app (no login): the problem, how it works (machine gate, term alert to the Foreman, Support Log), a short demo video slot and links into the demo. In Japanese first, en and bn switchable. Claims match BACKEND_PLAN §2 decisions 14 and 15 (what makes us different, who pays): no price, no unsourced numbers.

### Blockers / handoff
- Agree with Minhaz which kit (v2 or v3) gets the desktop layouts before you start; Minhaz owns the chat logic you lay out.

## 2026-11-22 (proto → production handoff from Minhaz)

Minhaz mapped the prototype (`prototypes/wakatta/`) and the frontend against `main` (`c07a8fb`). Decisions: the prototype's **Chat is in production scope (WS12, P1)**; Course stays P2 and frontend-only. Read [BACKEND_PLAN.md](../../docs/BACKEND_PLAN.md) decision #8 and XREQ-004 and XREQ-005 in [AGENTS.md](../../AGENTS.md) §2.2. Do the items in this order.

### Next (assigned)
1. **Chat screen designs in the v2 kit** (`frontend/src/ui/v2/`), taken from the prototype's Material look: Thread list, chat with voice button and Auto-send Countdown ring, the WK_STAGES progress, Wakatta Status chips (green / orange / red / grey), Follow-up list, Task Board. Presentation only: logic stays in `features/` ([FRONTEND_ARCHITECTURE.md](../../docs/FRONTEND_ARCHITECTURE.md) A-01…A-09).
2. **XREQ-004** UI pieces with Minhaz: the "Dictate instead" button, the degraded-draft message (ja / en / bn), the provenance line.
3. Replace the Unicode pictograms in `src/ui/v1/components/Icon.tsx` with real JIS / ISO 7010 SVGs.
4. Walk the [FRONTEND_REGULATIONS.md](../../docs/FRONTEND_REGULATIONS.md) pre-demo checklist (F-01…F-53) on every new screen; axe must stay green.
5. Proposal design and presentation slides.

### Blockers / handoff
- Run `scripts/setup.ps1` (or `scripts/setup.sh`) once, then add your GitHub handle to the roster in AGENTS.md §5.1.
