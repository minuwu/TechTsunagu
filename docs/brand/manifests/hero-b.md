# hero-b manifest

Poster Modernist (track P). Vector only, cream/jet/cobalt/white, 0 px corners, no text, one blue (`#1351AA`). JIS colours only inside the three safety pictograms (copied from `frontend/src/ui/v4/art/pictograms.tsx`). Nekko pasted in from `master/nekko-expressions` (inline, no refs). Signature device in every file: two-rectangle tick.

| File | Purpose | Size | KB | Notes |
| :--- | :--- | :--- | ---: | :--- |
| `images/hero/hero-4-notices-that-make-sense.svg` (+ `-1920.webp`) | Hero slide 4: pinned notice sheets, garbage-sheet with bin and bag pictograms, calendar with one cell lit and bracketed, bell ribbon, "understood" tick note. Story: many notices, one day that matters, now clear. | 1920x1080 | 24 | Nekko neutral, thumbs-up, behind cobalt ledge. Left 45% flat ground + faint grid. |
| `images/hero/hero-4-notices-that-make-sense-mobile.svg` (+ `-mobile-1080.webp`) | Same slide recomposed 4:5 | 1080x1350 | 23 | Top 40% empty. |
| `images/hero/hero-5-safety-you-can-pass.svg` (+ `-1920.webp`) | Hero slide 5: helmet, forklift, evacuation pictograms each with a passed tick box, big white "understood" tick block. Story: each sign learned and passed. | 1920x1080 | 19 | Nekko happy, thumbs-up, behind cobalt ledge. |
| `images/hero/hero-5-safety-you-can-pass-mobile.svg` (+ `-mobile-1080.webp`) | Same recomposed 4:5 | 1080x1350 | 19 | Top 40% empty. |
| `images/footer/cta-banner.svg` (+ `-1920.webp`) | CTA banner: cobalt block, cream tick, jet column, Nekko happy at right edge | 1920x480 | 16 | Left ~60% empty cobalt (faint white 10% grid) for HTML headline and button. |
| `images/footer/cta-banner-mobile.svg` (+ `-mobile-1080.webp`) | CTA banner mobile | 1080x600 | 16 | Left half empty cobalt; Nekko overlaps the lower right. |

## Known limits
- Nekko outline is cobalt, so on the cobalt ledge and CTA banners the outline merges into the ground (white body still reads).
- Mobile Nekko is smaller (about 345-395 px tall) than the 460-560 desktop rule, to fit the 4:5 layout.
- Pictogram viewBox padding makes the circle and triangle read smaller than the green square at equal box size.
- Dark `#E3E2DE` cream ground is a full-canvas rect in every SVG; the webps are rendered on it.
