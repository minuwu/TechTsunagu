# Engineering Guidelines: Turning Slop into Gold

Mandatory for every contributor and every coding agent, on frontend, backend and AI code. Where this file and a skill disagree, this file wins; where this file and [AGENTS.md](../AGENTS.md) disagree, AGENTS.md wins.

The job is to raise productivity without lowering quality, and the way there is **trust you have earned by investing in the agent's environment**, not blind trust.

## How to use this file

- **Authors (human or agent):** before opening a PR, walk the checklist for the areas you touched. A box you cannot tick needs a one-line reason in the PR body.
- **Reviewers:** review against this list. Review verification artifacts first (tests, screenshots, recordings), then code.
- **Audits:** each item has a stable ID (`G-xx`). Report violations by ID with `file:line`, a priority, and a fix.
- **Scope tags:** `[ALL]` every area, `[FE]` frontend, `[BE]` backend, `[AI]` AI/LLM code, `[DOC]` docs and process.
- **Enforced by:** `lint`, `types`, `test`, `ci` (deterministic, strongest), `runtime` (verification), `prose` (judgment, weakest). Prefer the strongest form that exists.

## The trust pyramid

Spend effort top-down. Each layer constrains the one below it.

1. **Deterministic, in code:** API design, architecture, refactoring, lint rules, type systems, compilers, tests.
2. **Verification:** running the real app, property-based tests, state charts, formal checks of invariants.
3. **Prose:** rules, skills, AGENTS.md. Only for what needs judgment.

> **Golden rule (G-01):** every time you correct an agent (or a teammate), turn the correction into a deterministic check, or remove the problem by better API design or architecture. Adding another sentence to a rules file is the last resort.

---

## 1. The codebase is the biggest prompt (hygiene)

Agents copy what they see. A workaround, an anti-pattern or deprecated code left in the repo becomes the "established pattern" the next agent copies. Keep the garden pruned.

- [ ] **G-02 `[ALL]` No weeds.** No workarounds, hacks, deprecated code, dead code, commented-out code, unused exports, stale mocks or leftover scratch files. Delete them; do not leave them as "reference".
- [ ] **G-03 `[ALL]` Fix the root cause.** Never patch over an architectural clash with a timer, a retry loop, a `try/except: pass`, a `!` non-null assertion or an `any`/`# type: ignore`. If the fix is hard, say so in the PR; do not hide it.
- [ ] **G-04 `[ALL]` Clean up old patterns when you add a rule.** A new lint rule or convention lands together with the cleanup of existing violations (or a tracked, dated exception list that only shrinks).
- [ ] **G-05 `[ALL]` One way to do each thing.** One API client, one error type, one response envelope, one date/money formatter, one place per concern. Duplicates are merged, not tolerated.
- [ ] **G-06 `[ALL]` Small, cohesive files.** Aim for 200–400 lines, hard ceiling about 800 (generated files, tests and seed data excepted). Functions under about 50 lines, nesting at most 4 levels, early returns.
- [ ] **G-07 `[ALL]` Names carry the meaning.** Descriptive names; booleans read as claims (`isOverdue`, `has_documents`); no magic numbers (named constants for thresholds, delays, limits such as a confidence threshold).
- [ ] **G-08 `[ALL]` Immutable by default.** Return new objects instead of mutating inputs (React state, store updates, Pydantic models, service functions).

## 2. Make the right thing the only easy thing (constraints in code)

- [ ] **G-09 `[FE]` Ban nuanced footguns in product code, enforce with lint.** Anything that needs expert nuance to use well is banned outside a small set of wrapper APIs. Start with: `useEffect` for data fetching or derived state (use a data hook or event handler), raw `setTimeout` / `setInterval` / `requestAnimationFrame` / `requestIdleCallback` for "fix the timing" (use a declarative hook or CSS), `dangerouslySetInnerHTML`, `any`, `@ts-ignore`. Enforced by: `lint`.
- [ ] **G-10 `[FE]` Frontend has lint and typecheck in CI.** ESLint (typescript-eslint, react-hooks, jsx-a11y) with `no-restricted-syntax` / `no-restricted-imports` for the bans above, strict `tsc`, `npm run lint` and `npm run build` required to pass. Enforced by: `ci`.
- [ ] **G-11 `[BE]` Backend has lint, format and types in CI.** Ruff (widen beyond `E4,E7,E9,F`: add `I`, `B`, `UP`, `S`, `ASYNC`, `T20`, `ERA`), a type checker (mypy or pyright) on `app/`, plus `pytest` with coverage. Enforced by: `ci`.
- [ ] **G-12 `[ALL]` Boundaries are directories, and imports across them are banned by tooling.** Examples to enforce: `frontend/src/pages` never imports another page; only `frontend/src/services` talks to the network; backend `routers` call `services`, never SQL or models directly; `services/ai_*` never import routers; the frontend never imports from `backend/` or `docs/` at build time. Use `import-linter` (Python) and `eslint-plugin-boundaries` or `dependency-cruiser` (TS). Enforced by: `lint`.
- [ ] **G-13 `[ALL]` The structure tells you where code goes.** A new file's location is obvious from its name and the folder conventions in AGENTS.md; do not invent new top-level folders.
- [ ] **G-14 `[ALL]` Lint errors teach.** Custom or restricted-syntax rules carry an error message that names the root-cause fix ("use `useItemQuery` instead of fetching in `useEffect`"), not just "not allowed".
- [ ] **G-15 `[ALL]` Types come from the contract, not from guesses.** Frontend types and mocks mirror `docs/openapi.yaml` (generate them if possible); backend responses are validated against it in tests. A contract change updates `docs/openapi.yaml`, `docs/API_SPEC.md`, the mocks and the tests in the same change. Enforced by: `test`.

## 3. Comments and prose

Comments go stale, humans skim them, agents use them to justify not doing the right thing, and nothing enforces them.

- [ ] **G-16 `[ALL]` No "why this hack is OK" comments.** If you feel the urge to justify a shortcut, fix the cause (G-03) or file a tracked issue instead.
- [ ] **G-17 `[ALL]` Comments are rare and earn their place.** Allowed: a public API docstring, a regulatory or business rule that code cannot express (for example a compliance rule), a non-obvious external constraint with a link. Not allowed: restating the code, change history, TODOs without an owner and an issue, commented-out code.
- [ ] **G-18 `[ALL]` Turn the comment into a check.** "Do not call X here" becomes a lint rule, a type, a test or an architecture rule (G-12), not a comment.
- [ ] **G-19 `[DOC]` Do not over-rotate on one correction.** A one-off fix is not a permanent hard rule. A rule enters AGENTS.md or a skill only if it is general and cannot be a check.
- [ ] **G-20 `[DOC]` Docs match the code.** README, AGENTS.md, DEPLOY docs, `.env.example` and walkthroughs are updated in the same change as the behaviour they describe. No doc claims a route, flag or env var that does not exist.

## 4. Verification beats review

Review shrinks to looking at **verification artifacts**. Spend most of your effort (and tokens) proving the change works.

- [ ] **G-21 `[ALL]` No change is "done" without evidence.** Evidence is a passing test run, a curl/Swagger response, a screenshot or a short recording, pasted or linked in the PR or XREQ proof block. "It should work" is not evidence.
- [ ] **G-22 `[ALL]` Test first (RED, GREEN, IMPROVE).** New behaviour and every bug fix start with a failing test. Target 80% coverage on changed code. Tests are unit, integration and one end-to-end path for each critical flow.
- [ ] **G-23 `[BE]` Run the contract tests.** `uv run pytest tests/api` validates responses against `docs/openapi.yaml`. Every new route has a contract test and a permission test (wrong role, wrong tenant, no token).
- [ ] **G-24 `[FE]` Frontend has automated tests.** Component or hook tests (Vitest and Testing Library) for stateful logic, and Playwright e2e for the critical journeys: sign-up and login, plus every journey shown in the demo. Run them against the mock layer and, before a release, the live API.
- [ ] **G-25 `[FE]` Runtime verification with a feature map.** Keep a feature map (a site map of every user-visible feature, with the shared vocabulary for each screen) in `frontend/walkthrough/`. Agents drive the real app (browser automation, DevTools) against it instead of guessing what "the thing in the left sidebar" means.
- [ ] **G-26 `[ALL]` Reusable drivers, not ad-hoc scripts.** Repeated verification lives in a committed, tested script or CLI (for example a smoke script or a Playwright suite), so agents reuse it and spend fewer tokens.
- [ ] **G-27 `[ALL]` Verify invariants, not just examples.** Tenant isolation (cross-tenant access is `404`), human-in-the-loop rules (AI output never changes a record without a person accepting it), optimistic locking (`409` on a stale version) and every lifecycle state machine have tests that would fail if the invariant broke. Prefer property-based tests (Hypothesis on the backend, fast-check on the frontend) for state machines and parsers.
- [ ] **G-28 `[ALL]` Model user-visible state machines.** Every multi-step lifecycle (for example an order or review flow) has a written state chart (a Mermaid `stateDiagram` in `docs/diagrams/`) that the code and tests match. Review the chart alongside the screenshots.
- [ ] **G-29 `[FE]` Every async view handles loading, empty and error.** Skeleton, empty state and error with retry. Verified by a test or a screenshot of each state.
- [ ] **G-30 `[ALL]` Fix flaky tests, do not retry them.** A flaky test is a bug in the test or the code; quarantine with an issue, never silently re-run.
- [ ] **G-31 `[ALL]` Verification artifacts are attached.** PRs touching UI include before/after screenshots at desktop and mobile width; PRs touching an endpoint include the request/response proof.

## 5. AI and LLM code `[AI]`

- [ ] **G-32 `[AI]` Every AI call has a deterministic fallback.** The demo never hangs or crashes when a model is slow, rate-limited or offline (rule-based fallback, recorded results). The fallback has its own test.
- [ ] **G-33 `[AI]` The AI only advises.** Model output lands in a `pending_review` state; a person must accept it before it changes a record. Enforced by `test`.
- [ ] **G-34 `[AI]` Prompts are code.** Prompts live in `backend/app/prompts/`, versioned in git, never inline in route handlers. Prompt changes ship with an eval or a fixture test showing the effect.
- [ ] **G-35 `[AI]` Quotas and timeouts are explicit.** `AI_TIMEOUT_SECONDS`, `AI_MAX_CONCURRENT` and per-provider fallbacks are configured, documented in `.env.example`, and tested. Eval scripts never run on judging day.
- [ ] **G-36 `[AI]` Validate model output at the boundary.** Parse model output with a schema; reject or fall back on malformed output; never trust it as already-valid data.

## 6. Security and configuration `[ALL]`

- [ ] **G-37 No secrets in git, ever.** No keys, tokens, passwords or database URLs in code, docs, commits or chat. `.env` is ignored; `.env.example` lists every variable with a dummy value. A secret scanner (for example gitleaks) runs in CI.
- [ ] **G-38 Validate at every boundary.** All user input is validated with a schema (Pydantic on the server, a schema or typed parser on the client). Errors are explicit, never swallowed, and never leak internals to the user.
- [ ] **G-39 Authorization is enforced on the server and in the database.** Role checks on every protected route, tenant isolation in the database (for example Postgres RLS), and 404 (not 403) for another tenant's data. The frontend hiding a button is never the control.
- [ ] **G-40 Standard envelope everywhere.** Every response, including errors, uses the envelope in `docs/API_SPEC.md` §1 (`ErrorEnvelope` on failure). No bare strings or framework-default error shapes.
- [ ] **G-41 Handle errors loudly.** No bare `except:`/`except Exception: pass`, no empty `catch {}`. Log full context on the server; show a friendly message in the UI.
- [ ] **G-42 Rate limits, upload limits and CORS are deliberate.** Documented, tested, and consistent between code and DEPLOY docs.
- [ ] **G-43 `[FE]` Accessible and responsive.** Keyboard reachable, visible focus, labelled controls, sufficient contrast (WCAG 2.2 AA), works at phone width, no horizontal page scroll. Checked by `jsx-a11y` lint and an axe check in e2e.
- [ ] **G-44 `[FE]` Performance budget.** No avoidable long tasks or jank: no timer storms, no unbounded re-renders, lists are virtualised or paginated when large, route-level code splitting, bundle size reported in CI.
- [ ] **G-45 `[BE]` Database changes are migrations.** Alembic only, reversible where practical, with RLS policies and an index for every new foreign key or filter column; no schema edits by hand.

## 7. Process and workflow `[ALL]`

- [ ] **G-46 Foundations before automation.** Do not automate a loop that produces slop. First make the check exist, then clean the old code, then make verification strong, then automate (cloud agents, auto-merge, scheduled fuzz runs).
- [ ] **G-47 Step 0, alignment, graphify, in that order.** Read the XREQ queue, align with the user, query `graphify` before grepping, run `graphify update .` after code changes (AGENTS.md §3).
- [ ] **G-48 Small PRs, Conventional Commits, no attribution trailers.** Under about 400 lines where possible; `feat:`, `fix:`, `docs:`, `chore:`; pull `main` before pushing.
- [ ] **G-49 Contract-first.** Anything another team consumes is logged in an XREQ with the exact JSON before it is built. Mocks match the contract exactly. A breaking change needs a `P0` XREQ.
- [ ] **G-50 Track quality over time.** Revert rate, flaky-test count, lint-exception count and coverage are tracked (a simple table in `docs/` updated at each release). Goal: revert rate stays low while volume rises.
- [ ] **G-51 Fuzz and smoke before release.** Before each release, run the smoke script and the e2e suite against the deployed build; every bug found becomes a failing test first, then a fix.
- [ ] **G-52 Agents extend this file, not around it.** When an agent finds a recurring mistake, it proposes a new `G-xx` item together with the check that enforces it (G-01), in the same PR.
- [ ] **G-53 Smoke test like a first-time user.** Human and agent smoke tests run on the live site with a fresh account and an inbox the tester owns (Gmail `+alias` is fine). Never use seed accounts or demo passwords: they are pre-verified and pre-filled, so tests pass while real users are blocked (for example, verification mail silently not sent). Every flow that sends mail is proven by receiving the mail.

---

## Priority guide for audits

| Priority | Meaning | Examples |
| :--- | :--- | :--- |
| **P0 (fix now)** | Security, data isolation, demo-breaking, contract-breaking | G-27, G-33, G-37, G-39, G-40, G-32, G-53 |
| **P1 (this sprint)** | Missing enforcement that lets slop in; missing verification of critical flows | G-09, G-10, G-11, G-12, G-21, G-23, G-24 |
| **P2 (soon)** | Hygiene, structure, docs drift | G-02, G-05, G-06, G-15, G-20, G-29, G-43 |
| **P3 (nice to have)** | Metrics, polish, advanced verification | G-25, G-28, G-44, G-50 |

## Quick pre-PR checklist

- [ ] Touched areas walked against this file; unticked boxes explained in the PR.
- [ ] Lint, types and tests pass locally (`npm run build` / `uv run ruff check && uv run pytest`).
- [ ] No new comments that justify a shortcut, no dead code, no secrets.
- [ ] Evidence attached (test output, screenshots, curl proof).
- [ ] Contract, mocks, docs and `.env.example` updated if behaviour changed.
- [ ] `graphify update .` run.
