# Brand kit

Mascot **Nekko**, app **Wakatta?**. Everything ships in one folder: [`frontend/public/brand/`](../../frontend/public/brand/). Open [`index.html`](../../frontend/public/brand/index.html) there (double-click, no server) to skim every asset, play every Lottie, and switch between the two blues and three backgrounds.

Nothing here is wired into `frontend/src` yet (decision: assets only, see [ADR 0013](../adr/0013-brand-kit-mascot-two-blues-vector-only.md)). Vite serves `frontend/public/` at `/`, so `frontend/public/brand/x.svg` is `/brand/x.svg`.

## Folder map

| Folder | What | Count |
| :--- | :--- | :--- |
| `master/` | Traced Nekko: icon, standalone, rig, 6 expressions, face mark, Lottie shapes | 24 |
| `icons/` | App icon variations (iOS, Android adaptive, maskable, circle, dark, mono, seasonal, alts) | 93 |
| `favicon/` | favicon.svg/.ico, apple-touch, PWA icons, manifest, `head-snippet.html` | 35 |
| `identity/` | Wordmarks, logo variants, lockups, pattern, `brand-sheet.html`, `tokens.css/json` | 73 |
| `lottie/` | `core/` 7, `flow/` 6, `empty/` 5, `moments/` 4 animations | 44 |
| `images/` | `hero/` 5 slides, `footer/` strips, dividers and banners, `spot/` 14 illustrations, `social/` cards | 176 |

Every file has an Icon-blue twin named `<name>.iconblue.<ext>` next to it (Site cobalt `#1351AA` is the primary; Icon blue `#246DD4` is the original icon's blue). Per-folder notes and known limits: [manifests/](manifests/).

## Wire it up (when you are ready)

Favicons: paste [`favicon/head-snippet.html`](../../frontend/public/brand/favicon/head-snippet.html) into the root route head.

Hero slide (desktop 16:9, recomposed mobile 4:5; leave the left 45% / top 40% for HTML text):

```html
<picture>
  <source media="(max-width: 767px)" srcset="/brand/images/hero/hero-1-machine-gate-mobile-1080.webp">
  <img src="/brand/images/hero/hero-1-machine-gate-1920.webp" alt="" width="1920" height="1080">
</picture>
```

Lottie (22 files, shape layers only, 30 fps): `npm i lottie-web` (or `@lottiefiles/dotlottie-react`), then

```ts
import lottie from 'lottie-web'
lottie.loadAnimation({ container: el, path: '/brand/lottie/core/loading.json', loop: true, autoplay: true })
```

One-shots (`success`, `fail`, `machine-unlock`, ...) use `loop: false`. Honour `prefers-reduced-motion` by showing a static frame.

Social cards: `<meta property="og:image" content="/brand/images/social/og-default-1200.png">`.

## Rebuild

Spec and rules: [SPEC.md](SPEC.md), scene list: [SCENES.md](SCENES.md), tools: [tools/README.md](tools/README.md), generator scripts for the Lottie, icons and scenes: [tools/generators/](tools/generators/README.md). After any asset change run `tools/recolor.py frontend/public/brand --render` then `tools/gallery.py`. Prompts for photo-style alternatives from an external generator: [prompts/image-prompts.md](prompts/image-prompts.md).

## Known limits

- Nekko's source is one JPEG, traced; whiskers and linework are faithful (mask IoU 0.998) but not hand-redrawn.
- The `surprised` expression has its mouth overlapping the nose; it is used only in `nudge-bell`.
- `logo-black` / `logo-white` draw Nekko as an outline only; `logo-white` needs a dark ground.
- Raster favicons keep the cobalt outline, low contrast on dark tab bars (only `favicon.svg` switches in dark mode).
- Hero slide 2 and 3 use less cobalt than the 15-25% poster target; mobile Nekko is smaller than on desktop.
- Seasonal and badge icons use the top corners, so a circle mask clips them (squircle is fine).
- No generated photos: no image model was available. All images are vector scenes.
