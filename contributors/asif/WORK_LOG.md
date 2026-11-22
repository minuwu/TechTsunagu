# Asif's Work Log

Owner: Asif (`MdAsif-Hossain`) — Role: Backend (lead) / Services / Database

Update after each work session. Newest entry first. Bangladesh time (UTC+6). Link commits/PRs/XREQs.

## 2026-11-22

### Completed
- Moved to the recreated repo (same URL, `minuwu/TechTsunagu`, fresh history from Minhaz's `init`). Dropped my old local history and merged my copy into the new `main`; the only change carried over is this log entry. Commit hashes in the 2026-11-21 entry refer to the old, deleted history.

- Reviewed the Wakatta? plan (backend focus). Team decision: keep every feature, 5 people, backend first, deploy on Azure after.
- Wrote the backend design: [docs/BACKEND_ARCHITECTURE.md](../../docs/BACKEND_ARCHITECTURE.md) and ADRs [0005](../../docs/adr/0005-bearer-session-tokens.md) (Bearer tokens), [0006](../../docs/adr/0006-code-first-openapi.md) (OpenAPI generated from FastAPI), [0007](../../docs/adr/0007-sync-fastapi-sqlmodel.md) (sync stack, inline AI), [0008](../../docs/adr/0008-ai-client-seam-and-fake.md) (AI seam + fake). Added **Presence** to GLOSSARY.md; updated the plan's auth, panel and data-model lines to match; ticked my Team review row.
- Plan review answers:
  1. I own **WS0** (foundation) and **WS2** (Notes, Checks, Sign-offs, Lock), and the Azure deploy in WS9.
  2. What makes a judge say no: a demo that breaks on a slow or rate-limited model (fixed: fallback ladder, fake AI by default, cache), a login cookie that fails on an iPhone (fixed: Bearer tokens), and a compliance claim we cannot source (the 特定技能 support wording stays in "Facts to verify").
  3. Nothing is cut. Risk control instead: WS0 lands all tables and typed route stubs first, so five people never edit the same migration or contract file.
  4. Fact I verify: Azure for Students covers PostgreSQL Flexible Server B1ms free for 12 months and Container Apps has a monthly free grant (Microsoft's Azure for Students and PostgreSQL pages); still to check: allowed regions for our subscription.

- Evaluated the alternative idea **Matte** (bank-transfer fraud circuit breaker): checked 18 claims of spec v2 against sources, judged it winnable, and wrote a system and team plan: [MATTE_EVALUATION_AND_PLAN.md](MATTE_EVALUATION_AND_PLAN.md).
- **Team decision: we stay with Wakatta?.** Matte is shelved; its plan stays in my folder for reference only.
- **Backend design v2, built for many shops and many users:** [docs/BACKEND_ARCHITECTURE.md](../../docs/BACKEND_ARCHITECTURE.md) rewritten (v1 replaced; nothing was migrated yet). Decided with a four-voice review (architect, skeptic, pragmatist, critic):
  - Two scopes: Work data per **shop**, Life data per **district** (a 自治会 serves Workers of several factories). Isolation in one repository layer + composite foreign keys; no RLS ([ADR 0009](../../docs/adr/0009-shop-and-district-scopes.md)).
  - Personal accounts: name + PIN → Bearer session; the Worker comes from the token, so nobody can forge a Sign-off; Panel device tokens; Senpai links hashed and revocable ([ADR 0010](../../docs/adr/0010-personal-accounts-and-pins.md), amends 0005).
  - Wire format matches Minhaz's frontend: camelCase, string ids, cursor lists ([ADR 0011](../../docs/adr/0011-camelcase-string-ids-cursor-lists.md)); `docs/API_SPEC.md` §1 updated.
  - Full data model (§6), transactions, idempotency and capacity (§8), every endpoint mapped to the frontend's paths (§9), build order with a check per step (§16).
  - Galib's 7 Gemini points accepted and folded into §7.4 and §10 (global 4/min limit, daily budget in Postgres, no retry on daily-quota 429, `reading` match for the Glossary lock, simple schemas, Notice text as data, model split).
- Opened **XREQ-001** for Minhaz in [AGENTS.md](../../AGENTS.md): Bearer token instead of cookie and the forgeable `'1'` PIN flag, roster + login, no `workerId` in bodies, cursor lists, `Idempotency-Key`.

- Completed the backend plan:
  - Account lifecycle for Workers and Foremen ([§3.4](../../docs/BACKEND_ARCHITECTURE.md#34-account-lifecycle-workers-and-foremen)): Foremen create accounts, first-login PIN change, reset revokes sessions (lost phone), deactivate keeps history, last Foreman can't be removed.
  - Added the plan's missing routes: Notice reach, expanded Events, Support Log date range.
  - Synced the team plan with v2: personal accounts, table map, route groups, WS0 owner and env vars (`8f2834c`).
  - XREQ-001 now includes the first-login PIN screen and the Foreman account screens.
- **Founder skill pack** ([founder-skill](https://github.com/Jakeschincariol/founder-skill) v1.0.0, MIT): read every file before installing (standard-library Python only, no network, no hooks), installed as a Claude Code plugin on the E drive, and its own 29 tests pass. It tests a *business* (board, competitors, buyer panel, pricing, CFO), not code, so the backend design itself was tested by the council above. Inputs used: free for employers and Workers, paid by 登録支援機関; buyer = factory owner / plant manager. Files: [founder/](founder/).
- **Founder board result** ([board.md](founder/board.md)): **3 × FUND IF, average 4.3/10** (Offers 4, Monopoly 4, Product 5).
  - Risks more than one lens raised: (1) nobody has shown the payer (登録支援機関) will pay, or that the Support Log matches what they must file; (2) the software LOCK may not survive a real factory (busy shifts, a worker walking past it); (3) the Foreman carries the effort and every re-approval voided all Sign-offs; (4) the AI is a commodity, its error rate on shop terms is unmeasured and the free tier can't run a factory; (5) incumbents (tebiki, Teachme Biz, カミナシ) could copy a machine gate.
  - Product lens: it is two products in one; the strongest version is a compliance-and-safety record a support organisation buys per Worker, filled daily by the machine gate.
  - Can't be met by Nov 24: letters of intent, a factory pilot. Can: a one-sentence pitch, honest payer/duty wording, a bypass answer, measured AI numbers.
- **Filled the board's gaps in the design** (`2771c6d`, BACKEND_ARCHITECTURE.md D5):
  - **Panel Override**: a Foreman lets a Worker run a Machine without a Sign-off for ≤ 8 h with a reason; amber on the Panel, on the board, audited, in the Support Log (answers "the lock stops production" and "what if someone walks past LOCK").
  - **Safety vs wording change**: only a Safety Change sends everyone back to the Check (`machines.recheck_version_no`); GLOSSARY.md and the plan rule updated.
  - **Per-shop languages** (`shops.languages`: bn, en, vi, id, ne, my, tl, zh); the demo stays bn/en and the wire shape stays `Tri`.
  - **Effort metrics** (`GET /metrics/effort`: time to approve, edits per draft, clean AI drafts, first-time passes) and an **AI eval** (`python -m app.ai.eval` → `docs/ai-eval.md`, Galib) plus a cost-per-factory formula.
  - **Performance budgets** (§8.5): Panel poll p95 50 ms with `ETag`/`304`, board 200 ms, `statement_timeout` 5 s, GZip, load test `tests/load/poll.py` (30 Panels + 5 boards + 2 AI drafts).
  - XREQ items 12–15 added to §17 (override state, approve choice, languages, `If-None-Match`).
- **Founder competitor map** ([competitors.md](founder/competitors.md), `c68a7ec`): 22 sourced competitors in Japan (7 direct, 10 indirect, 5 substitutes).
  - **Gap holds:** no product gates a specific machine on a passed quiz, alerts the foreman with the exact misunderstood term, or writes it into the 登録支援機関's record. No public review asks for it either, so demand is unproven.
  - **Corrections to our claim:** tebiki has translation and a skill map but no test feature found; カミナシ already has tests in 18 languages and Bengali in its worker app. Bengali and quizzes are not our difference; the gate, the term alert and the support record are. Plan line fixed.
  - **Biggest threat:** カミナシ (tests + per-learner results + shared tablets + equipment records + Bengali); linking them is a feature release for them.
  - Prices: most direct tools are quote-only; 3 public prices for factory manual SaaS, ¥34,000–¥89,800 per month.
- **Backend plan mapped to the hackathon guide** ([docs/BACKEND_PLAN.md](../../docs/BACKEND_PLAN.md), `c30145d`), written from the Participant Guide:
  - The guide's rules turned into backend rules: claim only what runs ("what works now vs planned" is a required proposal item, and reviewers check the repo against it); code freeze Tue 22:30 BST because commits after 23:59 are not evaluated; an AI-use statement in the proposal; the API stays up for the 6-minute presentation on Nov 26.
  - 15 open questions closed in one table (accounts, Bearer, many shops, `/scan`, camelCase, WS7/WS8 kept as P2 instead of cut, "Chat" prototype out of backend scope, `argon2-cffi`, demo languages bn + en, AI model split, 1 replica, software lock, what is original (gate + term alert + support record, not Bengali or quizzes), who pays).
  - Backend work mapped to each of the 5 evaluation points; P0 / P1 / P2 tiers; proposed owners (Asif WS0/WS2/WS9, Anim WS10/WS11, Galib WS1 + AI parts, Minhaz XREQ-001, Member 5 WS5–WS8); BST timeline with checkpoints (WS0 by Mon 03:00, P0 on Azure with the real frontend by Mon 18:00, P1 by Tue 06:00, freeze Tue 10:00, submit 23:30); definition of done; proposal checklist.
  - Pulled Galib's Rover Villa UX ideas (proposal, not agreed): listed as an open item, no backend work before P0 is green.
- Running when this entry was written: a sourced research pass (`founder/research.md`: 特定技能 support duties and the periodic report, Nagasaki worker numbers and nationalities, 登録支援機関 counts and fees, Gemini paid prices). The 20-buyer panel is prepared (`founder/customer.json`, `founder/pitch.md`, all guesses) and waits for my check of the buyer profile.

### Next
- Read the competitor and research results; map the Support Log columns to the real support-record fields; put the Gemini prices into the cost model; then run the quick buyer panel.
- Team reads BACKEND_ARCHITECTURE.md v2 (§0 first). OK needed on the `argon2-cffi` dependency.
- WS0 on `asif/ws0-foundation`, steps 1–8 of §16 (target Nov 23 02:00).
- Team reads BACKEND_ARCHITECTURE.md; then WS0 on `asif/ws0-foundation`: skeleton, all tables in the first migration, typed route stubs, generated openapi.yaml, AI seam with fake, seed, CI.
- Hello-world API on Azure Container Apps today.
- XREQ to Minhaz: Bearer token in `src/services/api.ts`, call `/machines/{id}/presence` after the name pick.

### Blockers / handoff
- For the team: re-clone the repo and run `scripts/setup.sh` (or `scripts\setup.ps1`) again; don't push from clones made before 2026-11-22.

## 2026-11-21

### Completed
- Cloned the repo, ran `scripts/setup.sh` (pre-push sync hook enabled via `core.hooksPath=.githooks`), started this work log (`7917a1b`).
- `.gitignore`: added local agent state (`.claude/settings.local.json`, `.claude/worktrees/`, `CLAUDE.local.md`, `.claude-mem/`) and Python tool caches (`.pyright/`, `.hypothesis/`, `.tox/`) (`34fbdb2`).
- Shared my backend docs for the team (`6a70f27`):
  - [CLAUDE.md](CLAUDE.md) — backend engineering standards (layering, API design, OWASP checks, migrations, testing) and the Definition of Done.
  - [claude-code-backend-setup.md](claude-code-backend-setup.md) — the dev tooling setup (review, security, TDD, Postgres best practices, Python LSP) and which command to use at each stage.
- Set up that tooling on my machine; verified every plugin shows enabled and pyright 1.1.414 runs.

### Next
- Hour-0: scaffold `backend/` (FastAPI expected) with CORS + `GET /api/health`, deploy hello-world to Render/Railway.
- Draft endpoints in `docs/API_SPEC.md` at kickoff using the §1 envelope.

### Blockers / handoff
- None. XREQ queue is empty.
- For Anim: if you want the same setup, follow Part A of [claude-code-backend-setup.md](claude-code-backend-setup.md); Part B runs once per project.

### Related issues / commits / PRs
- `7917a1b`, `34fbdb2`, `6a70f27`
