# Minhaz's Work Log

Owner: Minhaz (`minuwu`) — Role: Frontend / Full-Stack / Architecture

Update after each work session. Newest entry first. Bangladesh time (UTC+6). Link commits/PRs/XREQs.

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
