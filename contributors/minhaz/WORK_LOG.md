# Minhaz's Work Log

Owner: Minhaz (`minuwu`) — Role: Frontend / Full-Stack / Architecture

Update after each work session. Newest entry first. Bangladesh time (UTC+6). Link commits/PRs/XREQs.

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
