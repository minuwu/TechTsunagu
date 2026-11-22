# Arjun's Work Log

Owner: Arjun Saha (GitHub handle TBD) — Role: Frontend design

Update after each work session. Newest entry first. Bangladesh time (UTC+6). Link commits/PRs/XREQs.

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
