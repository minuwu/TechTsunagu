# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

- **Worker**: a shop-floor employee, often a foreign national (Bangladeshi in the demo), on their own phone. At a machine they scan its QR sticker, read the Note in Bengali, English or やさしい日本語, pass a Check, and the Machine Panel unlocks. At home they read local Notices (garbage rules, drills, festivals) as plain steps with dates, and ask questions.
- **Foreman** (班長): the one employer-side role, on a PC. Records spoken know-how per Machine, approves AI drafts against a back-translation, assigns Workers, reads the Skill Matrix, heatmap, near-misses and Confusion alerts.
- **Machine Panel**: a tablet beside each Machine showing Lock or Ready for the Worker on it.
- **Community Poster** (自治会, city or employer): publishes a Notice from a flyer photo, text or voice.
- **Senpai**: a Japanese local helper who opens a private link, no app, no login.
- **Judges**: Japanese hackathon judges who watch the whole flow in a pitch.

## Product Purpose

Understood at work, understood at home. A foreman's spoken know-how is pinned to a machine, turned into plain Japanese, Bengali and English, and a worker must prove they understood (pass a Check) before the machine unlocks for them. The same worker gets community notices as plain steps and a senpai to ask. Success: no worker runs a machine they did not understand, and the foreman can see where understanding breaks (Confusion per Glossary Term).

## Positioning

Comprehension is checked, not assumed: a Sign-off per Machine per Note Version gates a (software) lock, and every wrong answer becomes a Confusion signal tied to a shop word. Work and Life live in one app for the same worker.

## Operating Context

- Shop floor: noise, gloves, glare, one hand, short attention; phones of varying quality; QR stickers on machines; a tablet panel beside each machine.
- Foreman office PC: dense boards, CSV exports, morning briefing.
- Home: evening reading of notices, calendar (.ics) export.
- Pitch: one live demo store shared across tabs; two tabs side by side; reset before each run. Demo PINs: Foreman 1234, Community 5678.

## Capabilities and Constraints

- Stack: TanStack Start + React + TypeScript + Vite + Tailwind v4, mock data behind `VITE_USE_MOCK` (backend not live).
- Replaceable UI: each look is a kit in `frontend/src/ui/<version>/`; logic in `features/`, copy in `i18n/` (ja, en, bn). See docs/FRONTEND_ARCHITECTURE.md.
- Japan-facing UI rules F-01…F-53 (docs/FRONTEND_REGULATIONS.md).
- Safety colours carry meaning only (JIS Z 9103): red = Lock / Confusion / error, green = Ready / valid Sign-off, amber = expiring / caution, blue = action.
- Terminology is fixed by GLOSSARY.md (Worker, Foreman, Machine, Note, Check, Sign-off, Confusion, Lock / Ready…).

## Brand Commitments

Name "Wakatta?" and tagline "Understood, at work and in town". No logo, colours or fonts are fixed; the UI kit sets the look.

## Evidence on Hand

Seeded demo data only (`frontend/src/services/mocks/seed.ts`): four machines, four workers, sample notes, notices and senpai links. No real customers, testimonials or metrics exist; do not invent them.

## Product Principles

1. Understanding is proven, not assumed.
2. The worker's language first; Japanese is always one tap away for the foreman.
3. Safety states are unmistakable at a glance, from a distance, in any language.
4. One worker, one app, at work and at home.
5. The demo never hangs: every AI step has a deterministic fallback.

## Accessibility & Inclusion

WCAG 2.1 AA via axe on every screen; low-literacy and second-language readers (やさしい日本語, icons beside text); Bengali and Japanese scripts at a readable size; large touch targets for gloved hands; reduced motion respected.
