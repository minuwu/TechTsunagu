# Design system: "Nagasaki porcelain" (UI kit v3)

The look of the default kit, `frontend/src/ui/v3/`. Tokens live in [`ui/v3/tokens.css`](../frontend/src/ui/v3/tokens.css); Tailwind utilities read them through [`theme/base.css`](../frontend/src/theme/base.css) (A-06). Rules that apply to every kit stay in [FRONTEND_REGULATIONS.md](FRONTEND_REGULATIONS.md) (F-01…F-53) and [FRONTEND_ARCHITECTURE.md](FRONTEND_ARCHITECTURE.md) (A-01…A-09); brand assets and the one brand blue are in [brand/SPEC.md](brand/SPEC.md) and [ADR 0013](adr/0013-brand-kit-mascot-two-blues-vector-only.md).

## Direction

A Japanese technology product from Nagasaki, not a tourism site. By day, **porcelain surfaces with harbour-navy ink and cobalt actions**: the cobalt-on-white of Hasami porcelain (波佐見焼), which is also the brand's Site cobalt `#1351AA`. After dark, **harbour night**. Restraint over decoration: hairline borders, editorial headings, one accent per view.

Avoid: cherry blossoms, torii, flags, purple gradients, heavy glass, floating-card clutter, motion for its own sake.

## Colour

Never type a hex in a component. Use the token (Tailwind: `bg-primary`, `text-muted-foreground`, `border-[var(--rule)]` …).

| Token | Porcelain (default) | Harbour night | Use |
| :--- | :--- | :--- | :--- |
| `--background` | porcelain `oklch(0.972 0.006 80)` | `oklch(0.16 0.03 258)` | page ground |
| `--foreground` | harbour ink `oklch(0.23 0.045 258)` | `oklch(0.96 0.008 80)` | text |
| `--card`, `--tile` | `oklch(0.995 0.002 80)` | `oklch(0.21 0.035 258)` | panels, tiles |
| `--primary` | Site cobalt `#1351AA` | `oklch(0.74 0.12 252)` | **the** action colour, links, focus ring |
| `--accent` | cobalt wash | `oklch(0.29 0.06 255)` | hover, selected row |
| `--muted`, `--muted-foreground` | cream / slate | | secondary text, quiet fills |
| `--shell` | harbour navy | deeper navy | header, splash |
| `--shell-accent` | sea-glass | sea-glass | the bridge mark on navy |
| `--life` | sea-glass teal | | the Life side (notices, Senpai) |
| `--seal` | vermilion | | one small editorial mark; **never** a button |
| `--rule`, `--border` | hairline / 3:1 control edge | | dividers / inputs (F-35) |

**State colours carry meaning (JIS Z 9103) and never change hue between themes:** `--destructive` red = Lock, Confusion, error; `--success` green = Ready, valid Sign-off; `--warning` amber = expiring, caution. Colour is never the only signal: every state also has a word and a mark (F-35).

**Why vermilion is not the action colour:** in a machine-safety product red reads as "stop / locked". Actions are cobalt; vermilion appears only as a seal-like accent.

## Typography

- Stack (all kits, `theme/base.css`, F-09): **Manrope** for Latin letters and digits, then **Noto Sans JP** for every Japanese glyph, then Hiragino and Yu Gothic UI. Languages: Japanese and English only (Bengali dropped 2026-11-23). Manrope's open, slightly geometric Latin gives headings and figures (46.2%, 3.98) an editorial voice; Noto Sans JP keeps Japanese neutral and legible.
- Body 16 px minimum (F-10), line height 1.75, letter spacing 0.04em on Japanese body and 0.01em on English body (F-12).
- `.display` headings: weight 700, `palt` kerning, 0.02em (Latin −0.01em).
- Scale (Tailwind): page title `text-3xl`/`sm:text-4xl`; landing H1 `text-4xl`/`sm:text-6xl`; card title `text-lg`; body `text-base`; meta `text-sm`. Nothing smaller than `text-sm` for content (F-10).

## Space, radius, elevation

- Spacing: Tailwind's 4 px scale. Page padding `px-4` (phone), `lg:px-8`. Card padding `p-4 sm:p-5`; sections `mb-14`.
- Radius: `--radius` 0.75 rem (inputs `rounded-md`, tiles 0.875 rem, cards 1 rem, pills full).
- Elevation: `--shadow-sm` resting, `--shadow-md` hover and raised, `--shadow-lg` dialogs and the floating tab bar. Navy-tinted by day, black by night.

## Components

| Component | Convention |
| :--- | :--- |
| Button (`components/ui/button.tsx`) | `default` cobalt (one per view section), `outline`, `ghost`, `danger` (destructive only, behind a confirm, F-30). 44 px minimum height (F-24). `loading` shows a spinner and sets `aria-busy`; press scales to 0.98. Disabled until prerequisites are met. |
| Input (`inputClass` in `Card.tsx`) | 44 px, 3:1 border, cobalt focus ring; label above, error below with `role="alert"`. |
| Card (`Card.tsx`) | `card-surface`: hairline edge, soft depth. Optional title band; a tone (danger / success / warning / life) tints the band and edge. |
| Tile (`.tile`) | A whole-row link or button: pointer, lift on hover, scale on press. |
| Dialog (`ConfirmDialog`) | native `<dialog>`: focus trap, Esc closes, `data-lenis-prevent`. Destructive confirm repeats the action name on the button. |
| Tabs | App: header tabs (≥ 1024 px) or the floating tab bar (phone). Foreman: sidebar rail (≥ 1024 px) or a hairline tab row. Worker: segmented control. `aria-current` on the active one. |
| Status | `StatusChip` (Sign-off), `StatePlate` (Machine LOCKED / READY / idle: colour + word + mark), toasts (`lib/toast.ts`) for completed actions, inline text for errors. |
| States | `ScreenSkeleton` / `Skeleton` while loading, `EmptyState` (mark + sentence), inline error with a retry action. |

## Breakpoints

Tailwind defaults. Design checks at 375, 430, 768, 1024 and 1440 px.

- `< 640` phone: one column, floating tab bar, full-width primary actions.
- `sm` 640 / `md` 768: two-column card grids.
- `lg` 1024: header tabs replace the tab bar; Foreman console gets its sidebar.
- `xl` 1280: wide layouts (Board alerts column, Glossary form beside the table); content max width 80 rem.

Workers stay phone-first; the Foreman and Poster get the desktop layouts.

## Motion

- UI feedback 120–220 ms, `--ease-out`; springs (`--spring`) only for presses and the tab pill.
- Page enter 320 ms; dialog and toast 220–240 ms.
- Landing only: GSAP entrance and scroll reveals, Lenis smooth wheel scroll, the Vanta harbour hero (skills: `.claude/skills/gsap`, `lenis`, `vanta`).
- `prefers-reduced-motion: reduce` turns all of it off (F-38); every animated reveal ends at full opacity and never hides content from assistive tech.

## States and demo mode

| State | What the user sees | Where it comes from |
| :--- | :--- | :--- |
| Loading | `ScreenSkeleton` / `Skeleton` shaped like the content | the query is pending |
| Empty | `EmptyState`: a mark and one sentence (no Machines, nothing here yet) | the list is empty |
| Error | a caution band in the header, "Can't reach the server…", **Try again** | `useDataStatus()` (`features/health`): a `['wakatta', …]` query failed after its retries |
| Offline | the same band, "You're offline…" | `navigator.onLine` |
| Panel, no data | the **UNKNOWN** plate: "ask your foreman before you start", never a guess | `usePanel().unknown` |
| Success | toast for completed actions; the brand moment for the two that matter (Panel unlock, Check passed) | `lib/toast.ts`, `components/Moment.tsx` |

**Demo mode (mock data only).** Add `?demo=loading`, `?demo=empty`, `?demo=error` or `?demo=live` to any URL. Every mock read then returns that scenario (`services/mocks/scenario.ts`), and a small **Demo state** switcher stays in the bottom-left of that browser tab. It is always visible in development and never with the real API (`VITE_USE_MOCK=false`). A demo outage skips the query retries, so the error shows within about 2 s.

**For teammates wiring the real API:** nothing here changes the API contract. `list()` in `services/wakatta.ts` still calls `request()` when mocks are off; the error band and the Panel's UNKNOWN state work from the real query errors as they are.

