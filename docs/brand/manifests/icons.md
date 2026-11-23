# Icons manifest

Folder: `frontend/public/brand/icons/`. Track R, vector only. Every file is built from the Nekko master rig (`master/nekko-rig.svg`, neutral face except `alt-understood` = happy) by one script; the only brand blue is `#1351AA`. Rasters are `<stem>-<width>.png` rendered from `<stem>.svg` (`docs/brand/tools/render.cjs`). Open `icons-preview.html` (static, no scripts) for every variation plus square, circle, squircle (22.4%), Android adaptive (visible 66/108) and maskable previews.

Scene: Nekko at master scale on a cobalt ground (bust runs off the bottom edge), white body, cobalt linework, whiskers white outside the body.

| Stem (`app-icon-...`) | Purpose | PNG | Notes |
| :--- | :--- | ---: | :--- |
| `ios` | iOS 1024 master | 1024 | Opaque RGB PNG (no alpha), no pre-rounded corners. Same drawing as `master/nekko-icon-full-bleed.svg`. |
| `android-adaptive-foreground` | adaptive layer | 432 | Transparent. Head, ears, hand and whiskers sit inside the central circle of 66/108; the body runs to the bottom of the canvas (the mask crops it). |
| `android-adaptive-background` | adaptive layer | 432 | Plain cobalt. |
| `android-adaptive-monochrome` | themed icons | 432 | Single colour (jet), transparent; linework knocked out with an SVG mask. Same placement as foreground. |
| `android-playstore` | Play Store | 512 | Opaque, square, full bleed (store applies its mask). |
| `maskable` | PWA maskable | 512 | Full-bleed cobalt; Nekko inside the circle of radius 40%. |
| `circle` | circle icon | 512 | Clipped to a circle, transparent corners. |
| `squircle` | rounded icon | 512 | Clipped to a rounded square, rx 22.4%, transparent corners. |
| `dark` | dark ground | 1024 | Jet `#141414` ground, white Nekko, cobalt outline, white whiskers. Outline is lower contrast than the body; the white body carries readability. |
| `light` | cream ground | 1024 | Cream `#E3E2DE` ground, standalone Nekko with cobalt outline. |
| `mono-white`, `mono-black` | single colour | 512 | Transparent; silhouette plus outer whiskers in white / jet, all linework knocked out. No cobalt. |
| `tinted` | iOS tinted mode | 1024 | Pure greyscale (verified R=G=B): jet ground, white body, jet linework. Opaque. |
| `face-closeup` | head only | 512 | Master drawing clipped to the head; hand and body cut away. |
| `thumb-closeup` | hand only | 512 | Tight crop on the thumbs-up. |
| `outline-only` | no fill | 512 | Cobalt ground, white outline ring and white linework, no white fill. |
| `badge-question` | help / quiz | 512 | Nekko at 80% with a drawn white badge and cobalt question mark (strokes and a dot, no text) top right. |
| `sticker` | die-cut | 512 | Transparent; white round-join border around outline and whiskers; outline closed at the bust cut with a cobalt bar. |
| `seasonal-sakura` | spring | 512 | Pink `#F29BB5` blossoms and petals. |
| `seasonal-matsuri` | festival | 512 | Red `#E0412F` paper lanterns and a firework burst. |
| `seasonal-snow` | winter | 512 | Red `#D8372F` bobble hat, white snowflakes. |
| `alt-hardhat` | work safety | 512 | Jet hard hat with white ridge between the ears. |
| `alt-speech-bubble` | ask in your language | 512 | Cobalt speech bubble on cream, Nekko clipped inside, bust cut on the bubble floor. |
| `alt-megane-bridge` | Nagasaki | 512 | Jet bridge (stepped parapet, two round arches: cobalt arch, cream reflection) under the bust. |
| `alt-understood` | "wakatta" | 512 | Happy face, white tick badge top right. |

Each seasonal file adds exactly one accent hex; no icon adds another blue. Files carry no text, image, filter, gradient or external reference; SVGs are 12-19 KB.

## Mask results

- Circle and squircle: ears, hand and whiskers survive on `circle`, `squircle`, `maskable` (80% circle and 40% safe ring), adaptive foreground (66/108 square and circle).
- `ios` under a circle mask loses the right whisker tips (it is designed for the squircle).

## Known limits

- `badge-question`, `alt-understood` and the seasonal icons use the top corners; under a circle mask the badge is clipped. Squircle-safe only.
- `face-closeup` has a straight cut on the left where the thumb joins the head.
- `thumb-closeup` shows the chest as a white field to the right of the hand.
- Adaptive foreground and monochrome: the body is extended below the master's flat cut so it reaches the canvas bottom; on launchers that show more than 66/108 the body ends at the canvas edge.
- `sticker` shows a faint seam at the bar on the bust cut; the border around thin whiskers is a wide white band.
- `app-icon-android-playstore` is opaque full bleed; check the store's current asset rules before upload.
- Icon-blue twins are not built here; run `recolor.py` (the `-<width>.png` names follow its convention).
