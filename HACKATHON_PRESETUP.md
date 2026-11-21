# Hackathon Pre-Setup & Parallel Workflow Guide

This guide establishes the team workflow, repository architecture, and parallel development strategy to maximize speed and minimize integration blockers during the hackathon.

---

## 1. Core Rule: Contract-First Development

Frontend and backend teams **never block each other**. If frontend has to wait for backend endpoints to be built, development time is cut in half.

```
       [ Hour 0 - 1: Team Whiteboard / Spec ]
        Agree on Screens & JSON Data Contract (API_SPEC.md)
                         │
         ┌───────────────┴───────────────┐
         ▼                               ▼
 [ Frontend Team ]               [ Backend Team ]
 1. Scaffold UI pages            1. Setup DB / Models
 2. Wire components with         2. Implement endpoints
    Mock JSON Data               3. Test via Swagger / Postman
 3. Polish UX / responsive       4. Integrate LLM / business logic
         │                               │
         └───────────────┬───────────────┘
                         ▼
        [ Integration (1-2 Hours Before End) ]
     Switch API Base URL from mock to live backend
```

---

## 2. Recommended Tech Stacks

Choose based on team expertise:

| Stack | Best For | Key Advantage |
| :--- | :--- | :--- |
| **A. Next.js + Tailwind + shadcn/ui** *(Full-Stack TS)* | Web apps, dashboards, SaaS MVPs | Frontend & Backend API routes in one unified codebase; instant Vercel deploy. |
| **B. Vite + React + FastAPI (Python)** *(Decoupled)* | AI/LLM, data science, ML pipelines | Python ecosystem for AI; **FastAPI provides auto-generated Swagger UI (`/docs`)** for instant frontend testing. |
| **C. Supabase / Firebase + React/Next.js** *(BaaS)* | CRUD, real-time apps, quick Auth | Skips 80% of backend boilerplate (instant Auth, Postgres DB, storage, and auto REST APIs). |

---

## 3. Recommended Monorepo Structure

Keep all code in a single repository so commits, branches, and shared schemas are visible to the entire team.

```text
<repo>/
├── frontend/             # Vite + React or Next.js
│   ├── src/
│   │   ├── shell/        # Entry, routes, UI-kit registry
│   │   ├── ui/<version>/ # Swappable UI kits: screens, components, tokens
│   │   ├── features/     # Headless view-model hooks shared by every kit
│   │   ├── services/     # api.ts (handles mock vs live fetch)
│   │   └── domain/       # Types, schemas, formatters
│   │                     # Full layout: docs/FRONTEND_ARCHITECTURE.md
├── backend/              # FastAPI / Express / Flask
│   ├── app/
│   │   ├── routers/      # API endpoints
│   │   ├── services/     # Business logic / AI models / external APIs
│   │   └── models/       # Data schemas (Pydantic / Prisma)
├── docs/
│   ├── API_SPEC.md       # Shared API contract (Source of Truth)
│   └── openapi.yaml      # Machine-readable twin of API_SPEC.md
├── contributors/<name>/  # Per-person WORK_LOG.md
├── graphify-out/         # Local knowledge graph (untracked, query before grep)
├── AGENTS.md             # Agent + team protocol
├── .gitignore
└── README.md
```

---

## 4. Step-by-Step Parallel Execution Plan

### Step 1: Hour 0 to 1 — The API Contract (`docs/API_SPEC.md`)
Before writing code, the entire team meets for 30–45 minutes:
1. Wireframe core user journeys/screens on paper or Excalidraw.
2. Define the exact endpoints, request payloads, and response JSON.

**Example `API_SPEC.md`:**
```markdown
### POST /api/analyze
Request:
{
  "documentText": "sample text",
  "filters": ["summary", "sentiment"]
}

Response (200 OK):
{
  "id": "job_123",
  "summary": "This is a quick summary...",
  "sentiment": "positive",
  "score": 0.94
}
```

### Step 2: Hours 1 to N-2 — Independent Parallel Work

#### Frontend Playbook
- Create an API client layer with a toggle for mock data:
  ```ts
  // frontend/src/services/api.ts
  // Env-driven; defaults ON so the UI works before the backend exists.
  const USE_MOCK = (import.meta.env.VITE_USE_MOCK ?? 'true') === 'true';
  const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8000';

  export async function analyzeDocument(payload: AnalyzeRequest): Promise<AnalyzeResponse> {
    if (USE_MOCK) {
      return {
        id: "job_123",
        summary: "Mock analysis summary for immediate UI testing...",
        sentiment: "positive",
        score: 0.95,
      };
    }
    const res = await fetch(`${API_BASE_URL}/api/analyze`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    return res.json();
  }
  ```
- Build all UI components, forms, error states, and loading spinners using mock data.
- **Frontend can be 100% finished and polished before the backend server is even running.**

#### Backend Playbook
- Implement endpoints strictly adhering to `docs/API_SPEC.md`.
- **Enable CORS immediately** on the backend:
  ```python
  # FastAPI Example
  from fastapi.middleware.cors import CORSMiddleware

  app.add_middleware(
      CORSMiddleware,
      allow_origins=["*"],
      allow_credentials=True,
      allow_methods=["*"],
      allow_headers=["*"],
  )
  ```
- Use FastAPI's interactive documentation (`http://localhost:8000/docs`) or Postman/Thunder Client to test routes independently.
- Connect database, AI prompts, and external integrations.

### Step 3: Final Integration (Plug and Play)
- Set `VITE_USE_MOCK=false` (or `NEXT_PUBLIC_USE_MOCK=false`) in the frontend env.
- Since both teams adhered to `API_SPEC.md`, integration typically takes under 30 minutes.

---

## 5. Git & Team Rules for Speed

1. **Simple Branch Strategy**:
   - Do not use GitFlow. Short-lived branches named `<name>/<topic>`:
     - `minhaz/dashboard-ui`
     - `asif/ai-pipeline`
   - PR to `main` for cross-team or contract changes. Small changes may go straight to `main`, but only after `git pull origin main`; the pre-push hook rejects stale pushes and syncs `origin/main` into feature branches for you.
2. **Deploy on Hour 2 (Not Hour 23)**:
   - Connect the repository to **Vercel** (frontend) and **Render / Railway** (backend) right away with dummy code.
   - Catch CORS issues, build errors, and missing environment variables early.
3. **Environment Variables**:
   - Maintain a `.env.example` in both `frontend/` and `backend/` so teammates don't waste time diagnosing missing keys.
4. **Agents Follow the Protocol**:
   - Every coding agent reads [AGENTS.md](AGENTS.md): XREQ queue first, align with the user, check current docs, `graphify query` before grep.
5. **Automated Pre-Push Hook (Mandatory for All Contributors)**:
   - Run `sh scripts/setup.sh` (or `.\scripts\setup.ps1` on Windows) to enable the pre-push sync hook (`git config core.hooksPath .githooks`).
   - Every `git push` automatically fetches and merges latest `origin/main` into your working branch so nobody pushes stale code or breaks teammates.
