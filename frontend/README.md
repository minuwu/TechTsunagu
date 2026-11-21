# frontend/

Empty until kickoff. Stack is chosen at Hour 0 ([HACKATHON_PRESETUP.md §2](../HACKATHON_PRESETUP.md#2-recommended-tech-stacks)).

Conventions (AGENTS.md §4.1):
- `src/services/` is the only place that talks to the network (`api.ts` with the `USE_MOCK` toggle, default ON).
- `src/pages/` screens never import each other; shared UI lives in `src/components/`.
- Mocks mirror [docs/API_SPEC.md](../docs/API_SPEC.md) exactly.
- Every async view handles loading, empty and error.
- Commit `.env.example`; never `.env`.
