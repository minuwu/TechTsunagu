# Backend Plan — Wakatta?

**Owner** Asif (`MdAsif-Hossain`), backend lead · **Updated** 2026-11-22 19:45 BST · **For** the whole team, before writing backend code

This is **what we build, in what order, by whom, and by when**, judged against the [Hackathon Participant Guide](#1-what-the-guide-requires). *How* each piece works (tables, rules, endpoints, security) is in [BACKEND_ARCHITECTURE.md](BACKEND_ARCHITECTURE.md); this file never repeats it. If the two disagree, the architecture wins for design and this file wins for scope and schedule.

Contents: 1 What the guide requires · 2 Decisions (every open question, closed) · 3 What the judges score and how the backend earns it · 4 Scope tiers · 5 Owners · 6 Timeline · 7 Definition of done · 8 Demo and submission · 9 Risks · 10 What is still open

---

## 1. What the guide requires

| Guide says | What it means for the backend |
| :--- | :--- |
| "Software that actually runs and that people can use. An idea or a slide deck alone is not enough." | The demo path runs on a deployed API with a real database, not mocks. |
| Evaluation: problem/theme fit, **technical skill**, **completeness (what actually works)**, originality, proposal clarity | §3: each criterion has backend work that earns it. |
| Proposal item: "**Implementation status: what works now vs. what is planned**" | We build in tiers (§4) and claim only what runs. `backend/README.md` keeps a "Works now / Planned" table that matches the proposal word for word. |
| "**Reviewers will check your repository** to confirm that it matches your proposal." | README, CI, tests and code must back every claim. No stubs presented as features. |
| "Do not commit API keys, passwords, or other secrets." | `.env` ignored, `.env.example` only, gitleaks in CI (already in the design). Demo PINs live in the seed and README, never real secrets. |
| "AI tools may be used. **State in your proposal where and how** you used them." | §8.3 lists where AI was used (the product's Gemini calls, and Claude Code for design, code and review), for the proposal. Commits stay authored by team members. |
| "All development must be done during the development period." / no code written before | Everything is written from 2026-11-22 00:00 BST on; only open-source libraries. |
| "Anything **committed after the deadline will not be evaluated**. We will save a copy of each repository at the deadline." | Code freeze **Tue Nov 24 22:30 BST**; last push by 23:30. Nothing pushed after 23:59 counts. |
| Demo video ≤ 3 min, English, "show your product actually working" | Filmed against the deployed API with the AI cache filled (§8.2). |
| Final presentation Thu Nov 26, 6 minutes, in person | The API and database stay up through Nov 26 17:00 (min 1 replica), seeded and rehearsed. |
| Teams may be shortlisted on the Nov 24 submission | The submission alone (proposal + repo + video) must carry the project. |

---

## 2. Decisions (every open question, closed)

| # | Question | Decision | Why / source |
| :--- | :--- | :--- | :--- |
| 1 | Wakatta? or Matte? | **Wakatta?** | Team decision 2026-11-22; Matte plan kept for reference only. |
| 2 | Shared role PINs or accounts? | **Every Worker, Foreman and Poster has their own account** (name + PIN) | Architecture §3, ADR 0010. Without it anyone can forge a Sign-off. |
| 3 | Cookie or Bearer? | **Bearer token** | ADR 0005; the frontend's cookie and `'1'` PIN flag are replaced (XREQ-001). |
| 4 | One shop or many? | **Many shops and districts in one database**, isolated in the repository layer + composite FKs, no RLS | ADR 0009. The seed has two shops to prove it. |
| 5 | `/presence` or `/scan`? | **`POST /machines/{id}/scan`** | Matches the built frontend. |
| 6 | snake_case or camelCase? page numbers or cursor? | **camelCase, string ids, cursor lists** | ADR 0011; `API_SPEC.md` §1 updated. |
| 7 | Galib's proposal to cut WS7 (Near-miss) and WS8 (Library) | **Not cut: built last (tier P2).** If they are not working by the freeze, the proposal lists them under "Planned". | Keeps the team's "keep every feature" decision and the guide's "what works now vs planned" honesty. |
| 8 | Minhaz's prototype "Chat" terms (Thread, Task, Kanban **Board**, Nudge…) | **Not in the backend scope.** The word **Board** means the Foreman board. | Minhaz confirms whether the prototype is a demo idea; until then no backend work. |
| 9 | PIN hashing library | **`argon2-cffi` (Argon2id)** with a server pepper | The engineering manual requires Argon2id or bcrypt; the standard library has neither. Open-source, allowed by the guide. Approving this plan approves this one new dependency. |
| 10 | Languages | **Backend supports per-shop languages** (bn, en, vi, id, ne, my, tl, zh); **the demo uses bn + en** | Architecture §6.1. More languages are a claim only after one is shown working. |
| 11 | AI model and quota | `AI_MODE=fake` by default; `gemini-3.5-flash-lite` for prompt work, `gemini-3.8-flash` for filmed takes and the live demo, every filmed take cached | Galib's check (20/day vs 500/day); architecture §10.3. |
| 12 | How many API replicas? | **Exactly 1**, min = max = 1 from Nov 24 to Nov 26 | In-memory AI limiter; no cold start on stage. |
| 13 | Is the Lock hardware? | **No, software only**, with a named, time-limited Foreman override | ADR 0003; architecture §7.1. Say so in the proposal. |
| 14 | What makes us different (proposal "Originality") | The **machine gate**, the **term-level alert to the Foreman**, and the **support record** for the 登録支援機関. **Not** Bengali, **not** quizzes (カミナシ has both) | Sourced competitor check of 22 products ([competitors.md](../contributors/asif/founder/competitors.md)). |
| 15 | Who pays? | Free for employers and Workers; **登録支援機関 pay**; price **not set** (say "next step", never a number) | Team answer; founder board flagged it as unproven. |

---

## 3. What the judges score and how the backend earns it

| Criterion | Backend work that earns it | Evidence a judge can see |
| :--- | :--- | :--- |
| **Problem definition and fit with the theme** (TSUNAGU) | One Worker connected at work (Foreman ↔ Worker through the Machine), at home (Community ↔ Worker through Notices), and to a person (Senpai). Data connects too: a wrong answer becomes the Foreman's alert and the support organisation's record. | Demo story; proposal Problem section with the MHLW 46.2% figure (sourced). |
| **Technical skill** | Many shops in one database with tested isolation; personal accounts with lockout and session revocation; the AI fallback ladder (never a 500); idempotent creates; performance budgets with ETag polling; generated OpenAPI contract; CI (lint, types, layer rules, tests, contract diff, secret scan). | README architecture diagram, green CI badge, `tests/` (isolation suite, guard test), `docs/ai-eval.md`, `docs/openapi.yaml`. |
| **Completeness (what actually works)** | Tier P0 end to end on Azure before anything else (§4); P1 next; each tier finished, not half of everything. | The deployed URL; the video shows real requests; README "Works now" matches. |
| **Originality** | Machine gate + Panel Override; term-level Confusion alerts and heatmap; Safety vs Wording change; Support Log for the 登録支援機関. | The demo's LOCK → pass → READY moment and the alert naming 玉掛け. |
| **Structure and clarity of the proposal** | Backend gives the proposal: a one-paragraph architecture, the "works now / planned" table, the AI-use statement, and measured numbers (AI eval pass rate, p95 latencies). | Proposal pages 2–3. |

---

## 4. Scope tiers

Build top to bottom. A tier is **done** only when every item passes §7 and runs on Azure. Nothing from a lower tier starts until the tier above it is green, except by an owner who has nothing left above.

### P0 — the demo path (must work live; everything else is worth less than this)

| Item | Architecture | Workstream |
| :--- | :--- | :--- |
| Skeleton, config, envelope, errors, health, CORS, CI | §4, §13, §14 | WS0 |
| All tables in the first migration, roles and grants | §6 | WS0 |
| Accounts: roster, login, lockout, first-login PIN change, sessions, Foreman creates Workers | §3 | WS0 |
| Repository layer with scope + guard test + two-shop seed | §2, §3.3, §16 | WS0 |
| Every route as a typed stub; generated `docs/openapi.yaml` | §9.4 | WS0 |
| AI seam, fake client, limiter, daily budget, cache, fallback ladder | §10 | WS0 + WS1 |
| Record a note (voice or text) → draft → edit → approve (safety change) | §7, §8.1, §10.2 | WS2 + WS1 |
| Worker scan → Machine page (no answers leaked) → Check → Sign-off / Confusion | §7.1–7.3 | WS2 |
| Machine Panel state with ETag polling | §7.2, §8.5 | WS2 |
| Foreman board + open Confusions + acknowledge | §6.6, §9.3 | WS2 |
| Deploy to Azure, seed, `/api/health/ready` green | §15 | WS9 |

### P1 — the full story (the Life half and the Foreman's tools)

| Item | Architecture | Workstream |
| :--- | :--- | :--- |
| Notices: flyer photo / text / voice → draft with Events → confirm → publish; Worker feed + receipts | §7.5, §9.3 | WS10 |
| Upcoming Events and `.ics` calendar | §9.3 | WS10 |
| Asks grounded in Notices; forward to Senpai; Senpai link view and reply | §7.6, §9.3 | WS11 |
| Senpai pairs (create, revoke) and Support Log CSV | §9.3 | WS11 |
| Skill Matrix, Heatmap, Skill Passport | §6.6 | WS5 |
| Panel Override and Wording Change | §7.1 | WS2 |
| Briefings: record → draft → publish; Worker read + Check; receipts | §9.3 | WS6 |
| Glossary and Shift Assignments management | §9.3 | WS5 |
| AI eval run (`docs/ai-eval.md`) and load test results | §10.6, §8.5 | WS1, WS9 |

### P2 — breadth (proposal "Works now" only if green by the freeze, else "Planned")

| Item | Workstream |
| :--- | :--- |
| Near-miss Reports (voice/text + photo, translated, acknowledge) | WS7 |
| Knowledge Library (version timeline with who recorded) | WS8 |
| Meet-ups logged by Worker or Senpai | WS11 |
| Effort metrics endpoint | WS2 |
| Notice reach counts | WS10 |
| Foreman / Worker deactivate and PIN reset screens wired (endpoints are P0) | WS0 |

### Not built in the hackathon (proposal "Planned")
Self-service shop onboarding; retention and deletion jobs; more than one API replica; a cross-shop verified glossary library and anonymised confusion benchmarks (the moat the founder board asked for); a hardware interlock; languages beyond bn/en shown in the UI; payments.

---

## 5. Owners

Proposed; each person confirms by writing their name in the workstream in the [plan](superpowers/plans/2026-11-22-wakatta.md) and in their WORK_LOG.

| Person | Backend workstreams | Also |
| :--- | :--- | :--- |
| **Asif** (backend lead) | WS0 Foundation, WS2 Notes/Checks/Sign-offs/Lock/Panel/Board, WS9 Deploy | schema owner (one Alembic head), reviews every backend PR, README "works now" table |
| **Anim** (backend) | WS10 Notices + Events, WS11 Asks + Senpai + Support Log + Meet-ups | Life seed data |
| **Galib** (AI) | WS1 AI pipelines, prompts, validators, eval, cost model; AI parts of WS6 (Briefing) and WS7 (Near-miss translation) | Gemini project and key (never in git), cache seeding before filming |
| **Minhaz** (frontend) | XREQ-001 (Bearer, roster/login, PIN change, cursor lists, Idempotency-Key, ETag), wiring screens to the API | video edit, proposal layout |
| **Member 5** | WS5 Skill Matrix / Heatmap / Glossary / Shifts, WS6 Briefing, WS7, WS8 (backend routes on top of WS0) | proposal text, presentation slides |

Shared-file rules (from architecture §16): each workstream edits only its own `routers/`, `services/`, `repositories/`, `schemas/`, `tests/` files; schema changes go through Asif; `docs/openapi.yaml` is regenerated, never edited; `seed.py` and `domain/` changes are small reviewed PRs.

---

## 6. Timeline (BST)

Now: Sun Nov 22, about 19:45. Deadline: Tue Nov 24 23:59. About 52 hours.

| When | Milestone | Done when |
| :--- | :--- | :--- |
| Sun 20:00 → Mon 03:00 | **WS0** on `asif/ws0-foundation`, pushed after each step | Architecture §16 steps 1–7 pass their checks; merged to `main` |
| Mon 03:00 | Everyone starts their workstream on top of WS0 | stubs exist for every route, so frontend wiring starts at once |
| Mon 06:00 | Azure: API + database + seed deployed | public `/api/health/ready` ok |
| Mon 14:00 | **P0 backend complete** | P0 items pass §7 locally and on Azure |
| Mon 18:00 | **P0 end to end with the real frontend** (`VITE_USE_MOCK=false`) | the demo path clicks through on the deployed URL on a phone and a tablet |
| Tue 06:00 | **P1 complete** | P1 items pass §7 on Azure |
| Tue 10:00 | **Feature freeze.** P2 only if P0/P1 are green | from here: fixes, tests, docs only |
| Tue 10:00–14:00 | AI eval run, cache filled for every filmed take, load test, two full rehearsals on Azure | `docs/ai-eval.md` and load-test numbers committed |
| Tue 14:00–18:00 | Film the demo video (≤ 3 min, English) against Azure with the cache | unlisted YouTube link plays in a private window |
| Tue 18:00–22:00 | Proposal (≤ 3 pages, English, PDF) with the backend sections from §8 | every required item present (§8.1 checklist) |
| Tue 22:30 | **Code freeze** | `main` green in CI; README matches the proposal |
| Tue 23:30 | Submit `[TeamName]_Proposal.pdf` to Dropbox | upload confirmed; fallback: email to hcd_inc@bjitgroup.com before 23:59 |
| Wed Nov 25 | No commits (they would not be evaluated). Presentation slides (6 min) and rehearsal against Azure | slides done |
| Thu Nov 26 10:00 | Final presentation | API up (min 1 replica), seeded, cache filled, PINs on a card |

If a milestone slips by more than 3 hours, the backend lead moves the lowest unfinished P1 item to P2 and says so in the WORK_LOG. P0 never moves.

---

## 7. Definition of done (per item)

An item counts as "works now" only when all of these are true:

1. Endpoint(s) implemented as in architecture §9, request and response models typed, no `Any`.
2. Tests: success, validation error (422), the auth guard matrix (401/403/404 incl. the other shop), and the domain rule it depends on. Every bug fix starts with a failing test.
3. Runs against real Postgres (compose locally, the CI service container), `FakeAiClient` for AI.
4. `ruff`, `pyright`, `import-linter`, `pytest` and the `openapi.yaml` diff are green in CI.
5. Reviewed by another backend member (or `/ponytail-review` if nobody is free), and no secrets, debug prints or dead code in the diff.
6. Deployed to Azure and clicked through once from the real frontend.
7. One line in the owner's `WORK_LOG.md`, and the README "Works now" table updated.

---

## 8. Demo and submission

### 8.1 Proposal: what the backend supplies (checklist against the guide's required items)
- [ ] **Solution**: one paragraph and one diagram of the backend (FastAPI, Postgres, Gemini with fallbacks, Azure), from architecture §1.
- [ ] **Key points / Originality**: machine gate, term-level alert, Safety vs Wording change, Panel Override, support record; isolation between shops; AI never blocks the demo.
- [ ] **Implementation status**: the README "Works now / Planned" table, copied exactly.
- [ ] **Numbers we measured**, never estimated: AI eval pass rates (`docs/ai-eval.md`), p95 latencies from the load test, test count and coverage from CI.
- [ ] **AI use statement** (§8.3).
- [ ] GitHub URL is public; a reviewer can run the backend from the README in one command (`docker compose up` + `uv run …`).

### 8.2 Demo video and live demo
- Every Gemini call in the script is cached in `ai_cache` beforehand; `AI_MODE=gemini`; the day's quota checked that morning (it resets at 14:00 BST).
- Seed reset before each take: `python -m app.seed --reset`.
- If the API fails on stage: the frontend's `USE_MOCK=true` build runs the same journey with no backend (already built by Minhaz). Say so if used; never present mock as live.

### 8.3 AI use statement (for the proposal)
- **In the product:** Google Gemini turns Foreman voice notes, flyer photos and questions into drafts (plain Japanese, Bengali, English, quiz questions, calendar events) and translates replies. Every AI output is validated in code (glossary lock, schema, grounding) and approved by a person before Workers see it.
- **In building it:** the team used Claude Code (Anthropic) for system design review, code generation, code review and research, and Gemini in AI Studio for prompt testing. Every change was reviewed and committed by a team member.

---

## 9. Risks

| Risk | Likelihood | What we do |
| :--- | :--- | :--- |
| WS0 takes longer than 7 hours (accounts + scopes are new work) | medium | WS0 order puts stubs and `openapi.yaml` early, so others are never blocked; accounts minimal first (login, PIN change), extras P2 |
| Gemini quota gone before filming | medium | fake by default, budget in Postgres, cache, Flash-Lite for prompt work, separate Google Cloud project |
| Frontend still on mocks at Mon 18:00 | medium | XREQ-001 is small and in one service file; backend pairs with Minhaz at 14:00 |
| Azure region / quota surprise | low | hello-world deploy at Mon 06:00, not on the last day |
| Claiming something that does not run | must not happen | §7 rule 7 and the README table; the proposal copies the table |
| Commits after the deadline | must not happen | code freeze 22:30, submission 23:30 |

---

## 10. What is still open

| Item | Owner | Needed by |
| :--- | :--- | :--- |
| Minhaz confirms XREQ-001 and whether the "Chat" prototype is in scope | Minhaz | Mon 03:00 |
| Member 5 confirms WS5–WS8 | Member 5 | Mon 03:00 |
| Galib's [Rover Villa ideas](../contributors/galib/ROVERVILLA_IDEAS.md) (UX proposal, not agreed) | team | decide after P0 is green; no backend work before then |
| Support Log columns mapped to the official support-record fields (research running: `contributors/asif/founder/research.md`) | Asif, Anim | P1 |
| Gemini paid prices for the cost model (same research) | Galib | Tue 10:00 |
| Azure region allowed by the student subscription; database `max_connections` | Asif | Mon 06:00 |
