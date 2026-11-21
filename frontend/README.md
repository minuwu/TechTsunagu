# frontend/

TanStack Start + React + TypeScript + Vite, talking to the FastAPI backend ([docs/FRONTEND_STACK.md](../docs/FRONTEND_STACK.md)). The Hour-0 skeleton is in place: one screen (API status) runs end to end on mock data.

Node 24 LTS (`.nvmrc`; jsdom needs `^22.22.2 || ^24.15.0 || >=26`).

```bash
cp .env.example .env   # USE_MOCK is on by default
npm install            # also generates src/i18n/paraglide
npm run dev            # http://localhost:3000
npm run typecheck && npm run lint && npm test && npm run build
npx playwright install chromium && npm run test:e2e   # smoke + axe on the production build
npm run gen:api        # after docs/openapi.yaml changes; commit src/services/generated
```

On Windows, run tests from a shell whose path starts with an uppercase `C:\`. A lowercase `c:\` (some VS Code terminals) makes Vitest load itself twice and fail with "Vitest failed to find the runner".

Conventions (AGENTS.md §4.1):

- `src/services/` is the only place that talks to the network (`api.ts` with the `USE_MOCK` toggle, default ON).
- The UI is replaceable: each UI version is a kit in `src/ui/<version>/`; behaviour lives in headless hooks in `src/features/`; kits never import each other ([docs/FRONTEND_ARCHITECTURE.md](../docs/FRONTEND_ARCHITECTURE.md), `A-01`…`A-09`).
- Mocks mirror [docs/API_SPEC.md](../docs/API_SPEC.md) exactly.
- Every async view handles loading, empty and error.
- Commit `.env.example`; never `.env`.
- Stack, versions, folder layout and state-management rules: [docs/FRONTEND_STACK.md](../docs/FRONTEND_STACK.md) (`S-01`…`S-35`). TanStack file routes in `src/routes/` count as `shell/` and stay thin (S-08).
- Japan-facing UI rules: [docs/FRONTEND_REGULATIONS.md](../docs/FRONTEND_REGULATIONS.md) (`F-01`…`F-53`, pre-demo checklist).
