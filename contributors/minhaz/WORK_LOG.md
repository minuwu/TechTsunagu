# Minhaz's Work Log

Owner: Minhaz (`minuwu`) — Role: Frontend / Full-Stack / Architecture

Update after each work session. Newest entry first. Bangladesh time (UTC+6). Link commits/PRs/XREQs.


> **Message from Asif (2026-11-23), read first:** [CHATBOT_FROM_ASIF.md](CHATBOT_FROM_ASIF.md) explains the chatbot (what it does, the API, the four screen states, "Ask my Senpai" vs "Safety Senpai") and three decisions that are **yours**: where the ask box goes, one bot or two names, and which half grows first. Answer in XREQ-016.

## 2026-11-24 (from Arjun: App Links, B8)

- B8 (web side of App Links): `assetlinks.json` and the fallback page are live on the API host (Anim, XREQ-026), so the web app has nothing to serve for now.
- **Done for you (with your OK):** the App Links filter in `android/app/src/main/AndroidManifest.xml` is enabled on the API host with the `/m/ /c/ /t/ /n/ /b/ /nm/ /i/` prefixes; `MainActivity.openFromAppLink` parses `intent.dataString` with `parseAppLink` against the host of `ApiConfig.BASE_URL` and opens `toDestination()` once signed in (same wait as notifications); new `AppLinkHostTest` keeps the two hosts equal. **Not compiled or run here (no JDK or SDK on my machine): please run `testDebugUnitTest` and `:app:assembleDebug`.**
- **Still yours:** send Anim the SHA-256 fingerprints of the debug and release keys (`keytool -list -v -keystore …`); `assetlinks.json` is `[]` until then, so links do not verify yet. The Machine scanner (`QrPayload.parse`) still accepts any host and requires `?s=`; the contract wants it on `UriDeepLinkMatcher` too (2.10 check 8).
- `?s={shopCode}` on the web QR stickers waits on XREQ-001: the web app gets `shopCode` only from real sign-in. When you do XREQ-001, tell me and I add it, or add it in the same change. We also need to agree which host stickers print: the web `VITE_PUBLIC_URL` opens the web Machine page, the API host opens the app (or its "open in the app" page).

## 2026-11-24 (Shop Registration, Members screen, bilingual sign-in and intro)

- Backend: `POST /api/auth/register` opens a new shop with its first Foreman, a 「全員」 group Thread and a copy of the standard Glossary (ADR 0017). New accounts join every group Thread. `UserOut` carries `shopCode`. Re-explain no longer fails with 503 for a term outside the Glossary.
- Android: sign-in and intro redesigned, Japanese first with English under it; demo chips fill the form; errors show inline. Register a shop from the sign-in screen.
- Android: Members screen for Foremen (Me tab row and Home shortcut). Add a person by name only; the app makes a first PIN and a share card (shop code, name, PIN) for LINE or SMS. Rename, reset PIN, stop or resume an account.
- Checks: backend pytest, ruff, pyright; Android unit tests and `:app:assembleDebug` green.

## 2026-11-24 (Wakatta cards work end to end; Home crash; Japanese and English only)

- Backend chat: the Foreman's message carries `results` (each Worker's status, answers, read, nudged); the Worker's carries `myAnswers`, `answerKey` (only after answering) and `asked`; the answer response returns the key; a repeated ask on the same term sends nothing new. Contract in `docs/MOBILE_API_CONTRACT.md` and `docs/openapi.yaml`; tests green.
- Android: Worker Check card asks one question at a time and shows right and wrong after the last answer (with Retry if the send fails); Foreman card shows the understood count, live steps, the answer key and each Worker's read state, score, Nudge and Re-explain.
- Fixed: Home crashed on scroll (duplicate LazyColumn keys across sections). Foreman Machines no longer calls the Worker-only sign-offs route.
- App icon and splash use the brand Nekko on #246DD4. Bengali removed from the app (ADR 0016).

## 2026-11-24 (Android app on the live backend)

- The native app in `android/` runs on the live API for every screen: Asks and Senpai pairs (Worker and Foreman), Panel Check reminders and override end by id, Chat and Panel ETag bodies kept in Room, pushes that show the server's text and open their thread or Machine.
- Device features wired: dictation, TTS, QR camera, photo, share, offline outbox with banner.
- Checks: `testDebugUnitTest`, `checkModuleGraph`, `:app:assembleDebug` green; installs and starts on the emulator. Plan and open items: [ANDROID_FINISH_PLAN.md](ANDROID_FINISH_PLAN.md), backend asks: [BACKEND_WORK.md](BACKEND_WORK.md).
- Still open: full Worker/Foreman walk on the device, Senpai revoke button, fonts, App Links, baseline profile; backend XREQ-019, 025, 026, 014.

## 2026-11-24 (backend gaps for the Android app: XREQ-021, 023, 024)

- Built in the backend, with tests and one migration (`a445952485bc`): Machine reminders and override retries (XREQ-021), Near-miss severity, photo mark, Worker's own list and Task link (XREQ-023), Asks for the Foreman (XREQ-024, items 2 and 3 not built, see the card).
- Chat: a retried message POST now returns its `taskDrafts` until the Send (stored in `messages.task_drafts`); an edit that lost the model is repaired by the same `PATCH` again.
- `docs/openapi.yaml` and the frontend types are regenerated. New push type `check_due` (`machineId` in the data, channel `safety`).
- Still open for Anim: XREQ-019 (group controls), XREQ-025, XREQ-026, XREQ-014 key.

## 2026-11-23 (chatbot decisions answered)

- Answered Asif's three decisions in XREQ-016 (`AGENTS.md`): pinned "Ask Wakatta" chat-list entry (history on the phone only) plus the Machine-screen button with `machineId`; one "Ask Wakatta" bot, no Safety Senpai / Life Guide names on screen; Life Guide grows first after the hackathon.
- Open: Android chatbot screen (XREQ-016), FCM token upload (XREQ-014), Contacts and My QR on Android (XREQ-017).

## 2026-11-23 (from Arjun: v4 is the default, frontend CI needs you)

- **Fixed for you:** `ui/v4/components/LocaleSwitcher.tsx` still had `bn: 'বাংলা'`, which no longer typechecks (Locale is `ja | en`), so `tsc` failed on `main`. `.env.example` now says `VITE_UI_VERSION=v4`, matching your new default (it still said v3).
- **Still red with v4 as default** (Playwright in CI mode, as CI runs it): axe violations on `/` and `/me/w-rahim`; the home tests expect the h1 `Understood at work. Understood at home.` / `職場でも、くらしでも、わかった。`; the flyer flow expects `Publish` disabled before the dates are confirmed. 6 of 12 fail. These are in your kit, so I did not change them.
- The Japan-map test now opens `/?ui=v3`, so it passes whatever the default kit is. The map lives in v3; say if you want it in v4.

## 2026-11-23 (handoff from Arjun: v3 redesign is on `main`, please review)

Arjun fast-forwarded `feat/techtsunagu-ui` (10 commits, up to `80f34b8`) into `main` **without a PR**, so it still needs your review even though it touches shared files. Details: [docs/design-system.md](../../docs/design-system.md) and [Arjun's WORK_LOG](../arjun/WORK_LOG.md).

### Decisions to review (each is easy to revert)
- **v3 default theme is now light "Porcelain"** (was dark); night is harbour navy instead of purple indigo. Revert: default `'light'` → `'dark'` in `frontend/src/ui/v3/lib/theme.ts`.
- **v3 action colour is the brand's Site cobalt `#1351AA`** (ADR 0013); vermilion only as a small seal accent, never on buttons, because red means Lock (JIS Z 9103). All in `ui/v3/tokens.css`.
- **Manrope for Latin letters and digits in every kit**, ahead of Noto Sans JP (`theme/base.css`; Japanese unchanged; F-09 note updated).
- **New dependencies:** `lottie-web` 5.13.0, and earlier `gsap`, `lenis`, `vanta`, `three` (all in FRONTEND_STACK; three and Vanta load only on the landing page).
- **Mock layer:** in mock mode `list()` in `services/wakatta.ts` goes through a demo scenario (`services/mocks/scenario.ts`); `router.tsx` skips query retries for the demo outage only. The real-API path is unchanged.

### Next (asked of you)
1. **Run `npm run gen:api` in `frontend/` and commit.** `src/services/generated` only knows `/api/health` while the contract has 61 routes, so frontend CI's contract check fails on `main` until then.
2. **Native check of 12 new ja / bn strings:** `data_error`, `data_offline`, `panel_unknown`, `panel_unknown_word`, `board_empty`, `hub_empty`, `demo_*` (6 keys).
3. **Decide the landing hero:** the brand kit's 5-slide carousel either replaces the animated Vanta harbour hero or sits on top of it. Arjun waits for your call.

### For the API wiring (XREQ-001 / XREQ-002)
- New hooks: `useDataStatus()` (`features/health`: error / offline band with Try again), `usePanel().unknown` (Panel shows UNKNOWN, "ask your foreman", instead of "Machine not found"), `useHub().loading` (loading vs empty), `useDemoScenario()` (demo switcher).
- They work from real query errors as-is. The demo switcher (`?demo=loading|empty|error|live`) exists only in mock mode and disappears with `VITE_USE_MOCK=false`.

### For the v4 kit
- Kit-local in v3, to copy into v4 rather than import (A-04): `components/Moment.tsx` (brand Lottie one-shots), `components/Spot.tsx` (brand illustrations), `components/HarbourWaves.tsx` (Vanta hero), `lib/motion.ts` (Lenis), `lib/theme.ts`. Agent skills: `.claude/skills/{lenis,gsap,vanta}`.

### Brand kit task status (Arjun's)
- Done: favicons, manifest, theme colour, link-preview cards; Nekko role art on the demo hub and PIN screens; 404 illustration; Lottie `machine-unlock` (Panel), `understood-stamp` and `check-wrong` (Worker's phone).
- Not done: hero carousel (item 3 above), footer strip and dividers, `step-*` and `state-*` spots in empty states, logo in the header (still the bridge mark), the kit's known weak spots.

### Verified (locally, on the merged code; GitHub CI not seen yet)
- Typecheck and lint clean; Playwright e2e 11/11 on the production build with axe on every screen; separate axe sweep of all screens in the night theme and the error states: clean.
- Playwright now runs with `reducedMotion: 'reduce'` by default; tests that check motion opt in with `test.use({ reducedMotion: 'no-preference' })`.
## 2026-11-23 (Android app plan, backend requests)

### Completed
- Native Android app decided ([ADR 0015](../../docs/adr/0015-native-android-app-in-android-folder.md)): Kotlin, Compose, Material 3, folder `android/`, applicationId `com.techtsunagu.wakatta`, direct signed release APK kept in `android/release/`, mock mode until a backend is hosted.
- Plans and contracts: [ANDROID_ARCHITECTURE.md](../../docs/ANDROID_ARCHITECTURE.md) (30 work packages, performance rules), [ANDROID_RELEASE.md](../../docs/ANDROID_RELEASE.md), [MOBILE_API_CONTRACT.md](../../docs/MOBILE_API_CONTRACT.md) (76 live operations on 64 paths), [MOBILE_PLATFORM_CONTRACT.md](../../docs/MOBILE_PLATFORM_CONTRACT.md).
- Filed **XREQ-008 to XREQ-011** in AGENTS.md for Asif, Anim and Galib (hosted URL, app config and push, personal side, gaps in live routes).
- Decided with the user: Room 2.8.5, primary blue `#1f63b5`, no Poster role in the app for now.
- Prototype: Talk Threads, My QR and Contacts, Periodic Interview, Today's Word (see the entry below).

### Next
- Android scaffold (WP-0A) is being built by a worker; then up to six feature packages in parallel.
- Switch the app to the real API once XREQ-008 answers with a public base URL.

### Blockers / handoff
- **Asif:** XREQ-008 (P0) needs the public HTTPS base URL of `wakatta-api`, a seeded shop code and the PIN rule. Nothing in the repo records the host name.
- **Asif, Anim, Galib:** XREQ-009 to 011 list the routes the app still needs.

### Related issues / commits / PRs
- `f93a044` docs(android): add architecture and release plans, refresh the API contract, file XREQ-008 to 011

## 2026-11-23 (v4 kit art, Brand kit setup, Periodic Interview in the prototype)

### Completed
- v4 UI kit (`frontend/src/ui/v4/`): a restyled copy of v3 in cream `#E3E2DE` / jet black `#141414` / cobalt `#1351AA`, 12-column hairline grid with a sticky label column, 0px corners, no shadows, Aileron 800 headlines, light theme only. Every v3 screen is ported; logic, routes and copy are untouched (A-01…A-09). It is `DEFAULT_KIT` in `shell/ui-registry.ts`; v1, v2 and v3 stay reachable with `?ui=`. Decision in [ADR 0012](../../docs/adr/0012-v4-poster-modernist-kit-is-the-default.md).
- SVG art for v4, flat in the kit palette (the safety-signs course keeps the JIS/ISO safety colours), drafted with Gemini workers:
  - 18 course covers (`art/courses/`, shown by `CourseArt` in the Course screen).
  - 43 equipment drawings for Inspect (`art/inspect/`, three groups, used in `Doors.tsx`) covering 14 of the 18 courses. The other four (safety-signs, shisa-kanko, tamagake, welding) are not drawn yet.
  - 32 pictograms (`art/pictograms.tsx`). No screen imports them yet.
  - A hero poster of Nagasaki's Megane Bridge (`components/HeroArt.tsx`, used on the Landing page).
- Redrew `Icon.tsx` with a bold 2-unit stroke and gave `BridgeMark` square caps and a stepped deck. `PosterRow` now takes `to` and renders a router `Link`.
- Restyled Home, Landing, Course screen and Doors on the new art; added nine landing strings (`lp_*`) in ja / en / bn.
- Brand kit setup (no assets built yet). [ADR 0013](../../docs/adr/0013-brand-kit-mascot-two-blues-vector-only.md): the mascot Nekko, two blues, vector only, a Mixed style (Poster Modernist hero, footer and social; rounded playful icons, spot art and Lottie), a deliberate departure from ADR 0012's "cream, black and cobalt, 0px" rule. `docs/brand/SPEC.md` is the build contract for the agents and `docs/brand/SCENES.md` lists every icon, favicon, wordmark, Lottie, hero, footer and spot file. `docs/brand/tools/` has `render.cjs`, `sheet.py`, `lottie-check.cjs`, `trace.py` and `recolor.py` (heavy dependencies stay outside the repo). The source drawing is `docs/brand/source/nekko-original.jpg`. `frontend/public/brand/master/` exists but is empty, `nekko_lottie.py` and `docs/brand/README.md` (both named in the spec) are not written yet, and nothing is wired into `frontend/src`.
- Prototype (`prototypes/wakatta/`, parts D and E, rebuilt into `app.html`):
  - **Periodic Interview** (定期面談, support item 10, at least every 3 months): a Foreman-only sheet per Worker with the due date, what the app already knows since the last interview (Meet-ups, Notice Checks, Confusions, forwarded Asks), six questions with hints, and a record form (method, consent, note). Three seeded states: Rahim ok, Nguyen due in days, Siti overdue.
  - **Today's Word**: one word of a Worker's language (Bengali, Vietnamese, Indonesian) shown on the Japanese side.
  - The Support Log now counts Periodic Interviews, and the KPI tiles are labelled "demo value".
- [GLOSSARY.md](../../GLOSSARY.md): added **Periodic Interview**, **Today's Word** and the Brand terms **Nekko**, **Brand kit**, **Site cobalt**, **Icon blue**; widened **Support Log** to include Periodic Interviews.
- [AGENTS.md](../../AGENTS.md): filed **XREQ-007** (P1, to Galib for the bake-off and the choice, to Asif and Anim for the seam): pick the Japanese ⇄ English transcription and translation models by a bake-off on our own audio, not by free-tier quota. Deadline Tue 2026-11-24 10:00 (UTC+6), before the 14:00 filming window.
- Checked today: `npm run typecheck` passes, `eslint src/ui/v4 --max-warnings 0` passes, `node --check` passes on prototype `D.js` and `E.js`. I did not open the prototype in a browser, and I did not run the Brand kit tools.

### Next
- Build the Brand kit per `docs/brand/SPEC.md`: the master Nekko files in `frontend/public/brand/master/` first, then icons, favicons, wordmark, Lottie, hero, footer and spot art, then the Icon-blue twins with `recolor.py`. Write `docs/brand/README.md` and `nekko_lottie.py`, then wire the favicon links and a Lottie player into `frontend/src`.
- Draw Inspect art for safety-signs, shisa-kanko, tamagake and welding, and use the 32 pictograms in a screen or drop them.
- Darken the two tokens that fail the accessibility bars, `--label` (`#7A7A7A` on cream, about 3.3:1, needs 4.5:1) and `--rule` (`#C7C7C7`, about 1.3:1, needs 3:1), in `ui/v4/tokens.css`. Until then the A-07 pre-demo checklist does not fully pass for v4.
- Port the Periodic Interview sheet and Today's Word from the prototype to the React kits (they exist only in `prototypes/wakatta/` today).
- `git pull origin main` before pushing: Arjun added Hind Siliguri for Bengali and Foreman desktop layouts in the v3 kit (`a1411c0`, `14e0b14`). Agree with him on the overlap before either of us ports the other's work; v4 is a separate kit, so there is no file clash today. v4 may want the Bengali font too.
- Replace the `// verify:` numbers in the course library with primary sources (carried over from the course library entry).

### Blockers / handoff
- **Galib, Asif, Anim:** XREQ-007 is `[PENDING]`; the bake-off numbers are due Tue 2026-11-24 10:00 (UTC+6).
- `vitest` still fails to start: `vitest.config.ts` lists `./vitest.setup.ts` in `setupFiles` and the file is missing, so v4 has no unit-test coverage.

### Related issues / commits / PRs
- `441e92a` feat(frontend): add the v4 Poster Modernist kit base (tokens, layout, flat primitives) and make it the default
- feat(frontend): add the v4 course and Inspect art, pictograms, hero poster, redrawn icons, restyled Home, Landing, Course and Doors, and ADR 0012.

## 2026-11-23 (brand kit)

### Completed
- Brand kit generated from the app-icon image: mascot **Nekko** traced to vectors (mask IoU 0.998), then app icons (25 variations), favicons and PWA manifest, wordmarks and logo variants, 22 Lottie animations (loading / success / fail / warning, machine unlock, Check right / wrong, understood stamp, empty / offline / 404, logo reveal, confetti), 5 hero slides (desktop + recomposed mobile), footer strips, dividers, CTA banners, 14 spot illustrations and social cards. All vector (no image model); every file has an Icon-blue twin. Assets only: nothing under `frontend/src` changed.
- One folder to skim: `frontend/public/brand/` (open `index.html`). Spec, tools, per-group manifests and a prompt pack for external image generators are in `docs/brand/`. Decision: [ADR 0013](../../docs/adr/0013-brand-kit-mascot-two-blues-vector-only.md) (two blues, vector only, mixed style: a departure from the "all art cream / black / cobalt, 0 px" rule of the v4 design brief). Glossary: Brand section.

### Next
- Arjun wires it into v3 and may change or enhance the kit: see his [WORK_LOG](../arjun/WORK_LOG.md).
- v4 kit (`ui/v4/`, local only) still to push; the Brand kit moves to it by swapping markup.

## 2026-11-23 (course library)

### Completed
- Course library: 18 courses in `frontend/src/domain/courses/` (core 雇入れ時教育 set, trades, Japanese-for-work), each with comic, handbook, reel, drills, inspect items, quiz and glossary in ja / en / bn. Drafted with Gemini workers and spot-checked by hand; numbers marked `// verify:` still need a primary source.
- Library screen, per-course progress (`rv:course_progress:v1:<id>`), quiz Door and glossary in the v2 and v3 kits; v1 shows progress only.
- Fixed `pick()` so furigana braces (`漢字{よみ}`) never show in plain Japanese text.

### Next
- v4 UI kit (Poster Modernist: cream / cobalt / black, 12-column grid, flat, 0px corners) with SVG art: course illustrations, JIS/ISO pictograms, equipment drawings for Inspect, icon set. Presentation only (A-01…A-09). It overlaps Arjun's desktop-layout and `Icon.tsx` SVG items; v4 is a separate kit so no file clash, but agree before either of us ports the other's work.

### Blockers / handoff
- `vitest` fails to start in this checkout (missing `vitest.setup.ts`; the old tests fail the same way), so the course checks ran as a plain Node script.

## 2026-11-22 (proto → production handoff from Minhaz)

Minhaz mapped the prototype (`prototypes/wakatta/`) and the frontend against `main` (`c07a8fb`). Decisions: the prototype's **Chat is in production scope (WS12, P1)**; Course stays P2 and frontend-only. Read [BACKEND_PLAN.md](../../docs/BACKEND_PLAN.md) decision #8 and the new XREQ cards in [AGENTS.md](../../AGENTS.md) §2.2. Do the items in this order.

### Next (assigned)
1. **XREQ-001:** Bearer token in `services/api.ts`, roster → login → first-login PIN screen (`PUT /api/auth/me/pin`, not `/api/auth/pin`), Foreman account screens, no `workerId` in bodies, `{items, nextCursor}`, `Idempotency-Key`.
2. **XREQ-002:** Life contract changes (multipart flyer, 409 publish, `needsEdit`, blob fetch with token, Senpai by token, `toIcs` 75-octet folding + `VTIMEZONE`, backend Support Log CSV, recyclables step).
3. **Chat screens in the React kits** from the prototype (Threads, chat with voice + Auto-send Countdown, Wakatta Status chips, Follow-ups, Re-explanation, Nudge, Task Board, notifications), on mocks that match XREQ-005 exactly, then the real API.
4. **XREQ-004** with Arjun: "Dictate instead", degraded-draft message, provenance line.
5. Prototype UX to port: onboarding, notifications, dark mode and large text, analytics.
6. **Production switch:** e2e against the real API with `VITE_USE_MOCK=false`, then the hosted build Asif deploys (WS9).
7. P2: Course, frontend-only (local content, localStorage progress).

### Blockers / handoff
- Filed **XREQ-005** (Chat API → Anim, Asif) and **XREQ-006** (Chat AI → Galib). Handoffs written in each teammate's WORK_LOG, including Arjun's new log.

## 2026-11-22 (demo frontend)

### Completed
- Built a clickable demo frontend for every part of the plan, on mock data (no backend needed). Start at `/` (demo hub). Foreman PIN `1234`, Community PIN `5678`.
  - Worker: `/m/$machineId` (read → Check → unlock), `/me/$workerId` (shift, Skill Passport, Briefing, Near-miss), `/me/$workerId/life` (Events + .ics, Notices, Ask, Senpai).
  - Machine Panel: `/panel/$machineId` (LOCKED / READY, polls 2 s).
  - Foreman: Board, Record note (AI draft, back-translation, confirm), Assign, Skill Matrix, Heatmap, Glossary, Briefing, Near-misses, Library, QR stickers, Senpai + Support Log CSV.
  - Community Poster: `/community` (flyer photo / text / voice → confirm dates → publish → reach). Senpai link: `/senpai/$token`.
- UI in ja / en / bn (`src/i18n/*.json`). Mic and camera are real; AI answers are canned in `src/services/mocks/ai.ts`. Every service call has the real endpoint path ready for when the backend lands.
- E2E: lock → pass Check → Panel READY, flyer → notice → Ask cites it, and axe on all 18 screens.

### Next
- Team: click through the demo hub and note anything that does not match the plan.
- Swap Unicode pictograms in `src/ui/v1/components/Icon.tsx` for real JIS/ISO 7010 SVGs.
- Wire services to the FastAPI endpoints once WS0 is up (`VITE_USE_MOCK=false`).

## 2026-11-22

### Completed
- Picked the project after two 4-judge review rounds: **Wakatta?** (shop-floor checks that prove understanding + community notices for the same worker). Research behind it is in `research_notes/` and `reports/`.
- Wrote the plan [docs/superpowers/plans/2026-11-22-wakatta.md](../../docs/superpowers/plans/2026-11-22-wakatta.md), the [GLOSSARY.md](../../GLOSSARY.md) and ADRs [0001–0004](../../docs/adr/) (Gemini, FastAPI + Postgres on Azure, software lock, one app for Work + Life). Deleted the old Mimamori Pass plan.
- Pushed to `main` (`afa8480`, merged with `origin/main`).

### Next
- Review my row in the plan's "Team review" table (frontend routes, demo script, mock fixtures).
- Start WS0 foundation with the team once everyone has reviewed.

### Blockers / handoff
- **For everyone (Asif, Anim, Galib) — do this before WS0 starts:**
  1. `git pull origin main`.
  2. Read the plan and GLOSSARY.md in full.
  3. Do your row in the plan's "Team review" table and answer its 4 questions in the plan (or in your own WORK_LOG.md).
  4. Edit the plan directly to fix or improve it. Use GLOSSARY.md terms; add a new ADR in `docs/adr/` for any hard-to-undo change.
  5. Put your name on the workstream(s) you will own (WS0–WS11).
  6. Pick one item from "Facts to verify" and check it against a primary source; note the source next to it.
  7. Log what you did here in your own `contributors/<name>/WORK_LOG.md`, then `git pull origin main` and push to `main`.

### Related issues / commits / PRs
- `afa8480` docs: add Wakatta? plan, glossary, ADRs and idea research

## 2026-11-24 (send bar and Wakatta fold on device)

- Ported the prototype send bar: hint row, live transcript, wave, red pulse, mic and send cross-fade, countdown ring, green check.
- Ported the prototype Wakatta fold: status dots and per-status summary, step progress, step grid, numbered question cards, worker rows with marks, read state, status pill and re-explain or nudge.
- Fixed two send bar bugs found on the emulator: the hidden send circle swallowed mic taps, and the keyboard covered the bar (added `imePadding`).
- Committed the signed 1.0.0 release APK to `android/release/` and moved compileSdk to 37.1.

## 2026-11-24 (chat: voice fallback, optimistic send, unread divider, worker card)

- Voice: device speech no longer forces the offline model (no pack made it fail). When device speech is missing or fails, the app records an m4a and the new `POST /api/transcriptions` (Gemini, Groq Whisper fallback) fills the composer. Nothing is sent.
- Optimistic send: the bubble shows at once and is replaced by the server copy; a failed send puts the text back.
- Chat opens at the first unread with an "N new messages" divider mid-screen, else at the bottom.
- Status pill: after sending it shows "Waiting for answers · 5/6" with a clock, not a spinner.
- Worker Wakatta card restyled to the prototype (header with dots, 52dp option rows, result banner, board chips).
- Bubbles show the original; Workers open "Show translation".
- Chats top bar: QR button opens Connect.

## 2026-11-24 (crash guard for the submission build)

- Release builds no longer crash in a judge's hands: an unexpected error in a screen's coroutine shows "Something went wrong" and the app keeps running (`CrashGuard`, `MviViewModel` shadows `viewModelScope`). Debug builds and tests still throw.
- A crash outside screens reopens the app on Home silently; a second crash within 10 s falls back to the system, so it never loops.
- `safeApiCall` turns any unexpected exception into `AppError.Unknown`.
- Added the 7 path constants teammates' new routes were missing (`OpenApiContractTest` was red).
- Voice layer 3: offline or server down keeps the recording, says so, and retries with backoff (about 10 min); the text joins the composer when it lands. Fixed backend CI (API_SPEC endpoint index lacked `/api/transcriptions`), which had blocked the deploy.
- Chat readability pass on the Redmi: sender name and a per-person avatar colour (the poll's `fromDisplayName`, which the app ignored), the original in the sender's language (`language`), glossary words inline (one Text with link spans), runs by one sender grouped, own bubbles filled primary on the right and wrapped to content (prototype), no "Show translation" on your own words, and the keyboard keeps the newest message in view.
- Backend CI now runs on `docs/API_SPEC.md` changes; `/api/transcriptions` is live.
- Share on My QR sends a PNG of the QR (QrPng, plain-JVM writer) instead of a wk: text file, so chat apps show a scannable image.
- Chat device pass: a poll no longer closes an open translation or fold; Chats list shows 13:38 instead of a raw ISO time for contact/talk threads; "You: " keeps its space (Android trims unquoted trailing spaces). Checked on the Redmi: send, keyboard, glossary dialog, back, no crashes.
- Foreman avatar in chat gets a thin primary ring (1.5dp, 60% alpha, 2.5dp gap); the poll now carries `fromRole` (backend + openapi).

## 2026-11-24 14:15: My QR opens the app; captioned share card; replaced QR said plainly

- My QR (on screen and shared) is the App Link `https://<API host>/c/{token}`. The scanner and deep-link matcher accept it (the matcher now takes the server's 43-char token). The link opens Me > Connect > Scan with the token, which resolves at once. Checked on the phone with `am start -d`.
- The shared PNG is a card: Wakatta? title, the QR, "Scan to chat with <name>", a Japanese line, what scanning does, what is shared.
- Rotating My QR keeps the old hash in `retired_qr_tokens`; resolve/add on it answer 410 `QR_REPLACED`, and the app says the QR was replaced and to ask for the new one (`AppError.Gone`).
- "Make a new QR" button is now "New QR" / 「新しいQR」 (fits one line).
- Fingerprints for `ANDROID_CERT_SHA256` written in AGENTS.md XREQ-026 for Anim (no az CLI here).

## 2026-11-24 14:20: voice waits for a long pause; check verdict only; offline voice packs prompt

- Chat dictation uses `continuous()` with one silent round: a pause restarts the recognizer and the send countdown starts only when a whole listen hears nothing (also asks the recognizer for a 3 s end-of-speech silence). Chat VM tests' fake recognizer now ends a round on Final or Error.
- Worker check result banner shows only the verdict (Understood / Partly understood / Not understood); the "all answers correct" clause and "k of n correct" line are gone; missed questions still listed.
- One-time prompt after sign-in to download the Japanese and English offline speech packs (`SpeechPackPrompt`): Android 13+ checks the on-device recognizer and triggers the download of missing packs; older phones open Google's Offline Speech Recognition screen. Checked on the Redmi (API 29).

## 2026-11-24 — QR scan fixes, exact scan errors

- Camera analyses at 1280x720 (was 640x480), so a link QR shown on another phone reads.
- Scan a QR from a photo or file (system document picker, ML Kit on the image).
- My QR shows its code under the QR and on the share card; the screen goes full brightness while it shows.
- Share sends the link as text with the image, so chat apps make it tappable.
- Scan errors say what is wrong: not a Wakatta? QR, QR replaced by a new one, no one has this QR, your own QR, no QR in the photo.
- Backend: your own QR is 409 `OWN_QR` on resolve and add (was a 403 the app showed as "blocked").

## 2026-11-24 — My QR short code

- My QR code is now `WK-7KQ4M2` (WK- plus 6 characters, no 0/O/1/I/L; about 887 million values, resolve is rate-limited). Stored hashed; older long codes keep working until New QR.
- Server normalizes typed codes (`wk 7kq4m2` is `WK-7KQ4M2`). The app checks the shape before asking.
- Copy icon next to My QR code; paste icon in the scan field. The card prints the code large and bold; share text includes it.
- Needs the backend deploy; until then the live server rejects short codes, and the scan shows "no one has this QR".
