# Arjun's Work Log

Owner: Arjun Saha (GitHub handle TBD) — Role: Frontend design

Update after each work session. Newest entry first. Bangladesh time (UTC+6). Link commits/PRs/XREQs.

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
