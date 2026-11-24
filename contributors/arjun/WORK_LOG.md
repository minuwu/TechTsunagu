# Arjun's Work Log

Owner: Arjun Saha (`sillyfellow21`) — Role: Frontend design

Update after each work session. Newest entry first. Bangladesh time (UTC+6). Link commits/PRs/XREQs.

## 2026-11-24 (from Asif: R4 is done)

- R4 is on `main` with tests. R1 to R4 are closed; yours are R5, R6, R7. Push them without a JDK if you must and tell me: I run the Android check for you.

## 2026-11-24 (from Asif: R3 is done)

- R3 is on `main` with tests: nothing left for you there. R4 is next (me). Yours: R5, R6, R7.

## 2026-11-24 (from Asif: your Android work is checked, and I take R3 and R4)

- **R1 and App Links compile and pass** (`789b9a4`: 886 Android tests, 0 failed, app built), so nobody needs to run them again.
- **R2 is done** (Minhaz). **I am doing R3 and R4**: please do not start them. R5, R6 and R7 are yours.
- Task 2 needs nothing from you: my answer is in my log.

## 2026-11-24 (v4: Fuji blue through the UI)

### Done
- Neutrals take on the photo's blue. Ink moves from near-black `#0a1c26` to Fuji blue `#0f2f47`, with a bluer steel action colour, labels, cards, mist and dividers, and the same shift at night.
  - Contrast against the worst photo pixel: day ink 7.2, muted 4.8, primary 4.7, label 4.6:1; night ink 9.7, primary 5.5, label 5.4:1.
- The Wakatta mark in the header is the sky steel instead of an ink square, so it blends into the misty ground.
- The course and Inspect drawings use the same blue values. Defect red and the JIS pictograms are unchanged.

## 2026-11-24 (v4 dark mode)

### Done
- Dark mode for the v4 kit: it follows the device setting (CSS `color-scheme`, so nothing flashes), and the header's sun/moon button pins a choice per device. It uses the `wakatta.theme` key, shared with v3.
  - Every colour is `light-dark(day, night)`. The night palette comes from the photo's lake: the photo sits under an 84% lake-navy scrim, text is snow, and the action colour is a lightened steel.
  - Contrast is checked against the brightest photo pixel: 5.5:1 or better for every text token. Status chips are 6.2 to 7.7:1.
  - The Fuji hero layers and the bridge drawing switch to light lines. The splash goes dark at night. Course drawings keep their light poster panels.
- Phone header: the extra button fits by tightening gaps, a one-size-smaller wordmark below `sm`, and a group that never shrinks. The full wordmark shows from 360 px; at 320 px it truncates with no sideways scroll.
- e2e: on a dark device the landing and worker pages render dark and pass axe; the button pins light and keeps it after a reload.

### Known limit
- A pinned theme that differs from the device setting applies after hydration, so it can flash once on load. A theme cookie read on the server would fix that.

## 2026-11-24 (UI: overlaps and alignment, v4)

### Done
- `4823748` Overlap and alignment fixes, found with a sweep of 18 routes at 375, 768, 1024 and 1440 px plus scrolled screenshots:
  - Phones: the presenter's demo bar no longer floats over the screens (it shows from 48rem; phones use `?demo=`), and pages pad past the tab bar and Ask Wakatta.
  - Tablet: the hero headline no longer splits "Understoo|d" at 768 px.
  - Desktop: the Nagasaki card shows its fact; it was hidden in a fixed-height scroll box.
  - Foreman tabs line up with the page edge, and the Reset demo row wraps.
  - New e2e test: on a phone, no fixed bar covers the last control. It fails on the old layout and passes on the new one.
- `53c59db` The course and Inspect drawings use the Fuji palette. Defect red and the JIS pictograms are unchanged.
- Checks: e2e on a v4 production build passes 15 + 1, the new test passes 3 of 3 runs, lint, typecheck and the impeccable detector are clean.

## 2026-11-24 (small fixes)

### Done
- `c9d3f76` On phones, the demo-state switcher was hidden under "Ask Wakatta" (axe target-size on `/me/w-rahim`). It now sits above that row. Checked: axe clean at 375 px on `/`, `/me/w-rahim`, `/me/w-rahim/life`.
- `7455256` R1 (ANDROID_FIX_PLAN): the read timeout is 60 s, with `ReadTimeoutTest`. Not compiled here (no JDK); Minhaz's Gradle run checks it.
- `d0cb894` The journey map shows only Japan and Nagasaki: no 47-prefecture picker. It glides into Nagasaki, with two buttons for All Japan and Nagasaki.
- Sweep: every demo route at 1440 and 375 px is axe clean with no console errors and no sideways scroll; unit tests 109/109 (run from a `D:\` path).

## 2026-11-24 (Android App Links switched on, with Minhaz's OK)

### Done
- `AndroidManifest.xml`: the App Links filter is enabled on the API host (XREQ-026) with the contract's path prefixes. `MainActivity.openFromAppLink` opens the linked screen after sign-in. `AppLinkHostTest` keeps the manifest host equal to `ApiConfig.BASE_URL`.

### Next
- Minhaz: compile and run the Android unit tests (no JDK or SDK on my machine), and send the signing fingerprints to Anim.

## 2026-11-24 (v4 on a photo of Fuji at Lake Motosu, Fuji layers in the hero)

### Done
- **Ground and palette:** the photo is the background of every v4 page under a mist scrim. The palette is sampled from it (mist, lake navy, steel sky) and each text token passes 4.5:1 on the darkest photo pixel. Axe clean at 1440 and 375 px; e2e 15/15 on a v4 build.
- **Hero Fuji:** three silhouettes behind the Landing hero (`HeroFuji.tsx`). Staggered fade-in after the splash, parallax on desktop, a CSS float, and a hover tint and glow from the two hero buttons. All of it stops under reduced motion. Design decisions are in [frontend/README.md](../../frontend/README.md).

### Next
- Check the hero layers in a browser (not run yet): desktop, 768 and 375 px, with motion on and off.
- Course illustrations (`src/ui/v4/art/courses`, `inspect`) still use the old cream, cobalt and black; recolour them, keeping the JIS safety colours.
- Locally, `.env` picks another kit: open `/?ui=v4`.

## 2026-11-24 (Android App Links, web side: B8 checked)

### Done
- Checked my Android item, B8 in [MOBILE_PLATFORM_CONTRACT.md](../../docs/MOBILE_PLATFORM_CONTRACT.md) §5 (with Minhaz): `assetlinks.json` and the "open in the app" page are already live on the API host (Anim, XREQ-026, `backend/app/routers/links.py`), the App Links host until the web app is hosted. Nothing to build on the web for those. Status written into the B8 row.

### Blocked
- `?s={shopCode}` on Machine QR stickers (XREQ-001 item 10): the web app has no shop code until real sign-in (XREQ-001, Minhaz); mock Machine ids (`m-press`) are not numeric, so the app rejects them anyway; stickers print `VITE_PUBLIC_URL`, not the App Links host. Build it with XREQ-001: take `shopCode` from the login response, and decide with Minhaz which host stickers print. The web `/m/$machineId` route already accepts `?s=`.

## 2026-11-24 (v4: Journey to Nagasaki, and the v3 work ported to v4)

### Done
- **Journey to Nagasaki** (`3d4657f`), a landing section below the hero: a 3D Earth (three.js r134, NASA Blue Marble, lazy chunk about 153 KB gzipped) turns and flies to Nagasaki, then hands over to a map of the 47 prefectures (d3-geo/zoom). A panel shows each prefecture's names, famous place, a CC0/PD photo, a description and a fact in ja and en. Reduced motion, no WebGL or a failed texture go straight to the map. `@types/three@0.134.0` added (dev only).
- **Ported from v3 to v4** (presentation only, A-01…A-09; v4 keeps its flat, square look):
  - Lenis smooth scroll and GSAP landing motion: hero entrance, scroll reveals (opacity only), count-up on the sourced 46.2%, gliding `#demo` / `#how` links that move focus. `data-lenis-prevent` on the chatbot list, the confirm dialog and the journey panel.
  - States: the connection band (offline / error, with Try again), the demo switcher (`?demo=loading|empty|error|live`, mock only), Panel UNKNOWN, Board and Home empty states.
  - Lottie moments: the Panel's `machine-unlock` on a live Locked→Ready change, `understood-stamp` / `check-wrong` after a Check.
  - Foreman desktop: a sticky sidebar from 1024 px, Board machines left with flags and Confusions right (≥ 1280 px), Assign in two columns, the Glossary form as a sticky column, Near-misses in a grid, 44 px link targets on the Board.
- **Fixed:** v4 Home showed a loading skeleton forever when a list was empty. New e2e "demo states show the empty message and the connection band" (failed before the fix; axe runs on the error state).
- Check: typecheck, lint, unit 104/104, e2e 15/15 with axe in both languages; screenshots at 1440 and 390 px with no sideways scroll and no console errors.

### Next
- The Brand spot illustrations (Nekko, role and state art) are not in v4: they are blue brand art and v4 has its own poster art. Ask Minhaz whether v4 wants them.
- The WSTG checklist (tanprathan/owasp-testing-checklist) has not been run. Frontend gaps: no Content Security Policy or `frame-ancestors`, and no dependency or secret scanning in CI. A full run needs the hosted app (XREQ-013).

## 2026-11-23 (frontend ↔ backend connection check)

### Done
- `npm run check:api` (frontend): health, database readiness, CORS for `http://localhost:3000`, deployed contract vs `docs/openapi.yaml`, demo sign-in round trip (roster, login, Bearer `me`, `{items}` list, logout). Production: all pass, 74 paths in step with the repo (`46b62e9`, `dbd897d`).
- Real browser on `localhost:3000` → hosted API: Foreman sign-in, Board, ETag readable from JS, `If-None-Match` → 304, logout. All work.
- Links recorded: `frontend/.env.example` (hosted API URL, commented), `docs/FRONTEND_API_HANDOVER.md` (the check command) (`4a8124a`).
- Kept the frontend's generated API types in step with the contract twice (Anim's Interviews / Today's Word / app config / devices, then the Glossary PATCH), so frontend CI's contract step stays green.
- `main` did not typecheck after the v4 push (v4 `LocaleSwitcher` still had `bn`): fixed (`b548cdd`). `.env.example` now selects v4, the new default kit; the Japan-map e2e test opens `/?ui=v3` (the map lives in v3).
- Workflow from now on: merge `origin/main` into local `main`, push to `origin main`; no feature branches.

### Open (others)
- **Minhaz:** with v4 as default, 6 of 12 e2e tests fail (axe on `/` and `/me/w-rahim`, home headline, flyer Publish state): written in his work log; frontend CI stays red on `main` until fixed.

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
