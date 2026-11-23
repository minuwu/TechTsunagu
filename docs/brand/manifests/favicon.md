# Favicon set manifest

Folder: `frontend/public/brand/favicon/` (served at `/brand/favicon/`). Only `#1351AA` and `#FFFFFF` appear in the SVGs. Rasters are rendered with `docs/brand/tools/render.cjs` from the same-stem SVG.

| File | Purpose | Size | Notes |
| :--- | :--- | :--- | :--- |
| `favicon.svg` | Tab icon (modern browsers) | viewBox 64 | `nekko-face-mark` paths. A `<style>` block holds a `prefers-color-scheme: dark` rule that turns the cobalt outline white, so the head stays visible on a dark tab bar. Checked in Chrome in both schemes. |
| `favicon-16.png`, `favicon-32.png`, `favicon-48.png` | Raster tab icons | 16, 32, 48 | From `favicon.svg` (light rule), transparent. Checked 8x nearest-neighbour on white, cream and `#202124`. |
| `favicon.ico` | Legacy tab icon | 16, 32, 48 frames | Pillow, built from the three PNGs. Frames verified identical to the PNGs. |
| `favicon.iconblue.ico` | Icon-blue twin of the `.ico` | 16, 32, 48 frames | Same build from a scratch copy with `#1351AA` replaced by `#246DD4`. The scratch SVG is not shipped. |
| `apple-touch-icon.svg` / `apple-touch-icon-180.png` | iOS home screen | 1024 / 180 | Copy of the full-bleed master. PNG is opaque (mode RGB, no alpha). |
| `icon.svg` / `icon-192.png`, `icon-512.png` | PWA "any" icon | 1024 / 192, 512 | Copy of the squircle master (rx 22.4%). Corners transparent. |
| `icon-maskable.svg` / `icon-maskable-192.png`, `icon-maskable-512.png` | PWA maskable icon | 1024 / 192, 512 | Full-bleed cobalt ground. Whole Nekko scaled 0.84 so ears, thumb and whiskers sit inside the circle of radius 40% of the canvas; the body bleeds off the bottom. PNG opaque. Checked under circle and squircle masks. |
| `mstile.svg` / `mstile-150.png` | Windows tile | 150 | White face mark on a cobalt square. Opaque. |
| `safari-pinned-tab.svg` | Safari mask icon | viewBox 64 | Single colour (`#000000`), one even-odd path: head silhouette with eyes and smile as holes. Traced from the face mark. Safari applies the colour from `color` in the link tag. |
| `site.webmanifest` | PWA manifest | - | name and short_name "Wakatta?", theme `#1351AA`, background `#E3E2DE`, standalone, start_url `/`, icons 192/512 any plus 192/512 maskable, all under `/brand/favicon/`. |
| `browserconfig.xml` | Windows tile config | - | 150x150 logo `mstile-150.png`, TileColor `#1351AA`. |
| `head-snippet.html` | Copy-paste `<head>` tags | - | Icon svg and ico, apple-touch-icon, manifest, mask-icon, theme-color light (`#1351AA`) and dark (`#141414`), msapplication tags. |
| `favicon-preview.html` | Static preview, no scripts | - | Light and dark tab mocks, 16/32/48 at 1x and enlarged, dark rule, app icons, maskable under masks. |

## Known limits

- At 16 px the cat reads as a cat head with two eyes and a smile; the smile is faint (about 1 px tall). On a dark ground the PNG and `.ico` keep the cobalt outline, which has low contrast against `#202124`; only `favicon.svg` has the white dark rule. The `.ico` cannot switch with the colour scheme.
- Icon-blue twins other than `favicon.iconblue.ico` are left to `recolor.py`. Run it on this folder with `--render`; the `.ico` files are skipped by the script.
- `icon-maskable` shows a slightly smaller Nekko than `icon` because of the 40% safe circle. The cut body bottom is hidden by bleeding the body off the canvas edge.
- `safari-pinned-tab.svg` is a traced path (4 curves, 1.6 KB).
- No favicon for Android adaptive layers here; see the `icons` folder.
