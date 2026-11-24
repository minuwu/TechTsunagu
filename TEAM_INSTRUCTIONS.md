# Team Instructions — Human Onboarding & Per-Person Checklists

Read this once. Everything an AI agent needs is in [AGENTS.md](AGENTS.md); this file is for the humans driving those agents.

---

## 1. First 10 Minutes (everyone)

```bash
git clone <repo-url>
cd <repo>
sh scripts/setup.sh            # Windows: powershell -ExecutionPolicy Bypass -File scripts\setup.ps1
```

`setup` enables the pre-push sync hook, checks git/node/python/graphify, and verifies your git identity.

Then:
1. Read [AGENTS.md](AGENTS.md) — Section 2 (XREQ queue) and your team's section in 4.
2. Ask your agent to run `/graphify .` once, then open `graphify-out/graph.html` — a 2-minute map of the repo.
3. Create your branch: `git checkout -b <name>/<topic>`.

## 2. Daily Loop (everyone)

1. **Start**: `git pull origin main`. Open AGENTS.md §2. Anything `[PENDING]` for you → do it first.
2. **Work**: tell your agent *"Follow AGENTS.md."* Agents in this repo auto-read `AGENTS.md` / `CLAUDE.md` / `GEMINI.md` / `.cursor/rules` / `.github/copilot-instructions.md`.
3. **Need something from another team?** File an `XREQ` (AGENTS.md §2.3), commit, push, ping the person. Keep working against a mock.
4. **End**: update `contributors/<you>/WORK_LOG.md` (newest first), commit, push. Open a PR to `main` when a slice is demoable.

## 3. Non-negotiables

- Pull before you push to `main`: `git pull origin main`, check, then `git push origin main`. The pre-push hook rejects a push that is missing remote commits. Cross-team, contract or shared-config changes still go `<name>/<topic>` → PR → one approval → merge.
- Never commit `.env`. Keep `.env.example` current.
- Agents align with you before coding and check current docs, not memory (AGENTS.md §3.3).
- Breaking an API field without an `XREQ` to Frontend is a `P0` incident.
- Deploy a hello-world to Vercel (frontend) and Render/Railway (backend) in the first 2 hours, not the last 2.

## 4. Per-Person Checklists

| | Minhaz (`minuwu`) | Asif (`MdAsif-Hossain`) | Anim (`anim710`) | Galib (`Ga-lib`) | New teammate |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Role** | Frontend / Full-Stack / Architecture, repo owner | Backend / Services / Database | Backend / Services / Database (with Asif) | AI / LLM (model features, prompts, fallbacks) | assigned at kickoff |
| **Branch prefix** | `minhaz/*` | `asif/*` | `anim/*` | `galib/*` | `<name>/*` |
| **Work log** | `contributors/minhaz/WORK_LOG.md` | `contributors/asif/WORK_LOG.md` | `contributors/anim/WORK_LOG.md` | `contributors/galib/WORK_LOG.md` | `cp -r contributors/_template contributors/<name>` |
| **XREQs you answer** | `To: Frontend` / `To: Minhaz` | `To: Backend` / `To: Asif` | `To: Backend` / `To: Anim` | `To: AI` / `To: Galib` | `To: <Team>` / `To: <name>` |
| **You own** | `frontend/`, `AGENTS.md`, PR merges | `backend/`, `docs/API_SPEC.md`, Swagger `/docs`, DB schema | `backend/` services agreed with Asif; keep `docs/API_SPEC.md` in sync | `backend/app/prompts/`, AI services in `backend/` agreed with Asif and Anim | agreed at kickoff, recorded in AGENTS.md §5 |
| **Hour-0 task** | Scaffold `frontend/` with mock toggle and the replaceable-UI layout (`shell/ui-registry.ts`, `contract/ui-kit.ts`, `ui/v1/`), wire boundaries lint and prove it with a failing cross-kit import ([FRONTEND_ARCHITECTURE.md](docs/FRONTEND_ARCHITECTURE.md) §3); deploy hello-world to Vercel | Scaffold `backend/` with CORS + `/api/health`; deploy to Render/Railway | Pair with Asif on `backend/` scaffold; claim first endpoint in WORK_LOG | Run `scripts/setup`; pick the model/provider and stub the AI endpoint with a deterministic fallback | run `scripts/setup`, read AGENTS.md, claim first task in WORK_LOG |

Add a column when a teammate joins (AGENTS.md §5.6).

## 5. Hour-0 Kickoff Agenda (45 min)

1. Problem statement + the one demo moment judges will remember (10 min).
2. Screens on paper/Excalidraw (10 min).
3. Endpoints + JSON into [docs/API_SPEC.md](docs/API_SPEC.md) (15 min). Use the envelope in §1 of that file.
4. Stack decision (5 min); the chosen stack is in the [README](README.md#how-it-is-built).
5. Assign Hour-0 tasks from the table above; everyone opens a branch (5 min).

## 6. Keeping the Knowledge Graph Fresh

`graphify-out/` is generated locally (untracked in git) so agents can answer "how does X connect to Y" without reading everything.
- After code changes: `graphify update .`
- After doc changes: ask your agent `/graphify . --update`
- Ask questions: `graphify query "how does the frontend mock toggle work"`
