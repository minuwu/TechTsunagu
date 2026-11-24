# TechTsunagu Documentation Index

This directory contains the system architecture, frontend and backend specifications, Japan-facing regulations, quality guidelines, and pedagogical blueprints for TechTsunagu.

---

## Architecture & Specifications Directory

| Document | Purpose & Scope |
| :--- | :--- |
| **[ROVERVILLA_COURSE_PLATFORM_SPEC.md](ROVERVILLA_COURSE_PLATFORM_SPEC.md)** | **Interactive Work Safety Course Platform Specification**<br>Reverse engineering and audit of NASA Space Apps Winner *Rover Villa* (`https://rovervilla.space/`), the "One Safety Skill, Four Doors" pedagogical architecture (Manga KYT vs JIS Handbook, 60s micro-videos, 9 safety simulation drills, 3D digital twin equipment inspection), cross-platform web/mobile flows, `rv:course_progress:v1` offline cache, XREQ-compliant API contracts, and headless UI component blueprints. |
| **[FRONTEND_ARCHITECTURE.md](FRONTEND_ARCHITECTURE.md)** | Replaceable-UI architecture (`A-01`…`A-09`): swappable UI kits on one shared headless core. |
| **[FRONTEND_REGULATIONS.md](FRONTEND_REGULATIONS.md)** | Japan-facing UI rules (`F-01`…`F-53`): typography, dates, furigana, accessibility, and pre-demo checklist. |
| **[design-system.md](design-system.md)** | The default kit's look, "Nagasaki porcelain": colour tokens, type, spacing, components, breakpoints, motion. |
| **[FRONTEND_STACK.md](FRONTEND_STACK.md)** | Frontend technical stack: TanStack Start, React 19, TypeScript, Vite, Tailwind CSS, and state management rules. |
| **[BACKEND_ARCHITECTURE.md](BACKEND_ARCHITECTURE.md)** | Backend architecture: FastAPI, SQLModel/PostgreSQL, service layers, multi-tenant shop/district scopes, and auth. |
| **[BACKEND_PLAN.md](BACKEND_PLAN.md)** | Backend delivery milestones and work-stream breakdown. |
| **[ENGINEERING_GUIDELINES.md](ENGINEERING_GUIDELINES.md)** | Project-wide quality checklist (`G-01`…`G-53`): deterministic checks, verification proof, zero weeds. |
| **[API_SPEC.md](API_SPEC.md)** | REST API contract definitions, response envelopes, error structures, and endpoint inventory. |
| **[openapi.yaml](openapi.yaml)** | OpenAPI 3.1 machine-readable specification for backend routes and schemas. |

---

## Subdirectories

- **[adr/](adr/)**: Architectural Decision Records (ADR 0001 through ADR 0017).
- **[diagrams/](diagrams/)**: Architecture and data flow diagrams.
- **[research/](research/)**: Domain research (analog pain points in Japanese manufacturing and shipbuilding, foreigner support regulations).
- **[superpowers/](superpowers/)**: Engineering process artifacts and plans.
