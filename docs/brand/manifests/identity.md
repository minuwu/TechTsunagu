# Identity manifest

Folder `frontend/public/brand/identity/` (track R, vector only, only `#1351AA`; Icon-blue twins come from `recolor.py`). 11 SVG, 27 PNG (transparent, `<stem>-<width>.png`), `brand-sheet.html`, `tokens.css`, `tokens.json`.

| File | Purpose | viewBox | PNG widths | KB |
| :--- | :--- | :--- | :--- | ---: |
| `logo-mark.svg` | Nekko standalone (master path data, verbatim) | 0 0 986 860 | 400, 800 | 17 |
| `logo-wordmark.svg` | "Wakatta?" outlined, jet, cobalt "?" | 0 0 915.3 153.5 | 120, 600, 1200 | 4 |
| `logo-wordmark-ja.svg` | "わかった？" outlined, jet; the "?" is the Latin brand "?" in cobalt | 0 0 448.8 88 | 600, 1200 | 6 |
| `logo-full.svg` | Nekko + wordmark, horizontal, bust flush on the wordmark baseline | auto | 120, 600, 1200 | 20 |
| `logo-icon.svg` | Master squircle app icon (cobalt, white Nekko) | 0 0 1024 1024 | 180, 512, 1024 | 12 |
| `logo-favicon.svg` | Master face mark (ears, eyes, smile) | 0 0 64 64 | 16, 32, 48, 512 | 1 |
| `logo-white.svg` | Full logo, all white, for dark grounds | auto | 120, 1200 | 20 |
| `logo-black.svg` | Full logo, all jet, for light grounds | auto | 120, 1200 | 20 |
| `logo-lockup-stacked.svg` | Nekko over a jet rule over the wordmark (the rule is Nekko's "counter", so the cut is never floating) | auto | 500, 1000 | 20 |
| `logo-lockup-ja.svg` | Nekko + わかった？ | auto | 600, 1200 | 22 |
| `pattern-nekko.svg` | 960 x 960 seamless transparent tile: five expressions (neutral, happy, sleepy, worried, error) peeking over jet blocks, cobalt "?" between | 0 0 960 960 | 512, 1024 | 128 |
| `brand-sheet.html` | Static page: both blues, palette, logos, drawn clear space, minimum sizes, 3 do and 3 do-not, six expressions (links `../master/nekko-expressions/`), tracks P and R, pattern | | | |
| `tokens.css`, `tokens.json` | `--brand`, `--brand-alt`, cream, jet, white, hairline, state colours, radius tokens (P: 0; R: 8, 16, 24 px, icon 22.4%) | | | |

## Type

- Latin: Aileron Heavy (weight 800) from `@fontsource/aileron` 5.3.0. Licence CC0 1.0 (no rights reserved).
- Kana: Noto Sans JP Black (weight 900) from `@fontsource/noto-sans-jp` 5.3.1, installed in the scratch `brand-tools`, not in the repo. Licence SIL OFL 1.1.
- Both outlined with fontTools. No `<text>`, no font loaded by any file. Wordmark kerning is hand-set in font units (W-a -95, t-t -63, others small); the "?" is scaled so its top meets the "k" ascender.

## Clear space and sizes

Clear space x = half the wordmark cap height (the "W"), on all sides. Minimum width: full / lockups 120 px, wordmark 80 px (ja 96), mark 40 px, favicon 16 px. The full logo was checked at 120 px wide.

## Known limits

- `logo-white` and `logo-black` draw Nekko as an open outline plus linework (no body fill): white Nekko needs a dark ground to read, so the white file must go on dark.
- Nekko's bust is cut flat; in `logo-full` and `logo-lockup-ja` the cut sits on the wordmark baseline, in the stacked lockup on the rule.
- `logo-icon` and `logo-favicon` copy the master files, so the icon shows the master's small bottom-right curve.
- `surprised` has the master's mouth-over-nose flaw; it shows in `brand-sheet.html` but is not used in the pattern.
- The Japanese "？" is the Latin "?" glyph on purpose (brand link to "Wakatta?"), not the full-width Japanese question mark.
- Brand sheet shows `#246DD4` as a swatch only; no SVG contains it.
- `pattern-nekko.svg` is 128 KB (limit 150).
