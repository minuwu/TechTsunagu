# Brand kit: scene and animation list

What each agent builds. Names are final; rename nothing. Product context: **Wakatta?** helps foreign workers in Nagasaki, Japan, understand shop-floor instructions (a machine stays locked until the worker passes a short check) and local life notices (garbage rules, drills, festivals), with a Japanese neighbour (the Senpai) connecting them. Roles: Worker, Foreman, Poster (posts notices), Senpai. Style tracks P and R are defined in [SPEC.md](SPEC.md) section 4. Text-free everywhere except the wordmark files in `identity/`.

## Icons (icons agent) - `frontend/public/brand/icons/` - track R

Variations of the app icon, all from the master. Each as SVG plus PNG. Required:
`ios-1024` (opaque, no alpha), `android-adaptive-foreground`, `android-adaptive-background`, `android-adaptive-monochrome`, `android-playstore-512`, `maskable-512`, `circle-512`, `squircle-512`, `dark` (jet ground, cobalt-outlined white Nekko), `light` (cream ground), `mono-white` and `mono-black` (single colour silhouette plus linework), `tinted` (grayscale-safe for iOS tinted mode), `face-closeup` (head only, tight), `thumb-closeup` (hand only), `outline-only` (no fill), `badge-question` (Nekko with a cobalt question-mark badge), `sticker` (die-cut white border), `seasonal-sakura`, `seasonal-matsuri`, `seasonal-snow` (small flat accessory shapes only). Plus `icons-preview.html`: not text-dependent, a page showing the set under circle, squircle and adaptive masks.

## Favicons (favicon agent) - `frontend/public/brand/favicon/` - track R

`favicon.svg` (with `prefers-color-scheme` dark variant inside the SVG), `favicon.ico` (16, 32, 48) in both blues (`favicon.iconblue.ico`), `favicon-16`, `favicon-32`, `favicon-48`, `apple-touch-icon-180`, `icon-192`, `icon-512`, `icon-maskable-192`, `icon-maskable-512`, `safari-pinned-tab.svg` (single colour), `mstile-150`, `site.webmanifest` (name "Wakatta?", short_name "Wakatta?", theme_color `#1351AA`, background_color `#E3E2DE`, display standalone, icons incl. maskable), `browserconfig.xml`, and `head-snippet.html` (the exact `<link>`/`<meta>` tags, copy-paste, paths under `/brand/favicon/`).

## Identity (identity agent) - `frontend/public/brand/identity/` - track R

Wordmark "Wakatta?" and "わかった？" outlined to paths (Aileron from `frontend/node_modules/@fontsource/aileron`, or another bold geometric sans found on disk; convert with fontTools, never `<text>`). Logo-designer's seven variants: `mark` (Nekko), `wordmark`, `full` (mark + wordmark horizontal), `icon`, `favicon`, `white`, `black`; plus `stacked` lockup and a Japanese lockup. `brand-sheet.html` (static, no scripts): palette with both blues, clear space, minimum sizes, do and don't, the 8 expressions. `tokens.css` (CSS variables) and `tokens.json`.

## Lottie (4 agents) - `frontend/public/brand/lottie/<group>/<name>.json` - 512x512, 30 fps - track R

| Group | Files |
| :--- | :--- |
| `core/` | `loading` (loop), `success`, `fail`, `warning`, `saving` (loop), `upload-progress` (loop), `upload-done` (one-shot) |
| `flow/` | `machine-unlock` (padlock opens, machine tile lights up), `check-correct`, `check-wrong` (shake, worried, gentle), `understood-stamp`, `voice-recording` (loop), `nudge-bell` |
| `empty/` | `empty-list`, `no-results`, `offline`, `error-404`, `maintenance` |
| `moments/` | `logo-reveal` (mark only), `celebrate` (confetti, one-shot), `welcome` (Nekko bounce and happy blink), `page-wipe` (cobalt sweep with Nekko peek) |

## Hero carousel (2 image agents) - `frontend/public/brand/images/hero/` - track P

Each slide as `hero-N-<slug>.svg` (1920x1080) and `hero-N-<slug>-mobile.svg` (1080x1350, recomposed) plus PNG and WebP renders (`-1920`, `-1080`).
1. `hero-1-machine-gate`: a big jet press-machine block with a cobalt padlock opening, a QR-sticker square, a tick. Nekko behind a counter block, thumbs-up.
2. `hero-2-ask-in-your-language`: two rectangular speech blocks (0 px corners, notched tails) overlapping, abstract lines instead of text, a microphone pictogram. Nekko in the corner.
3. `hero-3-senpai-next-door`: Megane Bridge arches, two simple geometric figures sharing a cup, a house block with a lit window. Nekko peeking.
4. `hero-4-notices-that-make-sense`: pinned notice sheets, a calendar grid with one cell highlighted, bin and bag pictograms, a bell. Nekko peeking.
5. `hero-5-safety-you-can-pass`: JIS safety pictograms (helmet, forklift, evacuation), an "understood" tick block. Nekko thumbs-up.
Text-safe zones per SPEC section 4.

## Footer, banners, social (footer agent) - `images/footer/`, `images/social/` - track P

`footer-strip` (1920x320) and `footer-strip-mobile` (1080x420): Megane Bridge motif along the bottom, Nekko peeking over the top edge. `divider-bridge`, `divider-rule`, `divider-steps` (1920x120). `cta-banner` (1920x480) and `cta-banner-mobile` (1080x600): cobalt block, Nekko at the right edge. Social (text-free): `og-default` (1200x630), `og-square` (1200x1200), `twitter-card` (1200x600), `line-share` (1040x1040), `demo-cover` (1920x1080), `slide-bg` (1920x1080, quiet).

## Spot illustrations (spot agent) - `images/spot/` - 800x800, track R

Roles: `role-worker` (hard hat), `role-foreman` (clipboard), `role-poster` (notice sheet and pin), `role-senpai` (tea cup and house). Onboarding: `step-1-scan-qr`, `step-2-take-the-check`, `step-3-machine-unlocked`, `step-4-ready`. States: `state-empty`, `state-no-results`, `state-offline`, `state-404`, `state-error`, `state-success`. Props are drawn flat in the same line weight and sit on Nekko's standalone/expression artwork.
