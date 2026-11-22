# Team TechTsunagu — Hackathon Repository Template

A ready-to-use starting point for a hackathon team. It holds the team protocol, the agent rules, the quality bar and the git safety checks. It holds no app code. Frontend and backend start empty at kickoff.

## What is inside

| Path | Purpose |
| :--- | :--- |
| [AGENTS.md](AGENTS.md) | Single source of truth: XREQ cross-team request queue, agent protocol, git rules, team and contributor directives |
| `CLAUDE.md`, `GEMINI.md`, `.cursor/rules/agents.mdc`, `.github/copilot-instructions.md` | Entry pointers for each coding agent. They only point to AGENTS.md |
| [docs/ENGINEERING_GUIDELINES.md](docs/ENGINEERING_GUIDELINES.md) | Quality checklist `G-01`…`G-53` (lint over prose, verification evidence, no slop) |
| [docs/FRONTEND_ARCHITECTURE.md](docs/FRONTEND_ARCHITECTURE.md) | Replaceable-UI architecture `A-01`…`A-09`: swappable UI kits on one headless core |
| [docs/FRONTEND_REGULATIONS.md](docs/FRONTEND_REGULATIONS.md) | Japan-facing UI rules `F-01`…`F-53` and pre-demo checklist |
| [docs/API_SPEC.md](docs/API_SPEC.md), [docs/openapi.yaml](docs/openapi.yaml) | Contract-first API stubs with the response envelope |
| [docs/ROVERVILLA_COURSE_PLATFORM_SPEC.md](docs/ROVERVILLA_COURSE_PLATFORM_SPEC.md) | Course platform architecture: Rover Villa translation, "One Safety Skill, Four Doors", 9 simulation drills, schemas & Three.js blueprint |
| [TEAM_INSTRUCTIONS.md](TEAM_INSTRUCTIONS.md) | Human onboarding, daily loop, per-person checklist, Hour-0 agenda |
| [HACKATHON_PRESETUP.md](HACKATHON_PRESETUP.md) | Parallel workflow playbook and stack options |
| `.githooks/pre-push` | Rejects stale pushes to `main`; auto-merges `origin/main` into feature branches |
| `.github/workflows/verify-branch-sync.yml` | CI: fails a PR that is behind `main` |
| `scripts/setup.sh`, `scripts/setup.ps1` | One-shot onboarding: enables the hook, checks tools, installs graphify |
| `contributors/` | Per-person work logs |
| `frontend/`, `backend/` | Empty, with folder conventions |
| `.gitignore`, `.gitattributes` | Secrets, build output and line-ending rules |

## Adopt it

```bash
git init && git add . && git commit -m "chore: bootstrap from team template"
sh scripts/setup.sh          # Windows: powershell -ExecutionPolicy Bypass -File scripts\setup.ps1
```

Then follow [TEAM_INSTRUCTIONS.md](TEAM_INSTRUCTIONS.md).
