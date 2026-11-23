# Lottie moments

Folder `frontend/public/brand/lottie/moments/`. 30 fps, track R, shape layers only, flat fills, cobalt float triple `[0.0745098, 0.3176471, 0.6666667, 1]`. All four pass `docs/brand/tools/lottie-check.cjs` (OK).

| File | Canvas | Duration | KB | Notes |
| :--- | :--- | ---: | ---: | :--- |
| `logo-reveal.json` | 512x512 | 2.2 s (66 f) | 22.1 | One-shot. Cobalt square grows from the centre (corners round to square), Nekko rises behind the bottom edge with overshoot and squash, whiskers flick, cream sparkle pops and fades. Holds the full-bleed icon state (same placement as `nekko-icon-full-bleed.svg`). Mark only, no wordmark. |
| `welcome.json` | 512x512 | 1.8 s (54 f) | 21.2 | One-shot, then rests. Nekko (happy) bounces in from below, lands with squash, small second bounce, happy arc eyes blink once (shape morph), thumb gives a small jiggle. Transparent background; holds the happy pose. |
| `celebrate.json` | 512x512 | 2.2 s (66 f) | 37.1 | One-shot, transparent. 16 flat confetti pieces (squares, triangles, bars; cobalt, white with jet edge, jet, cream) burst up behind a happy Nekko doing two hops, then spin and fall off-canvas with different easing. Last frame: Nekko alone. |
| `page-wipe.json` | 1280x720 | 1.13 s (34 f) | 19.8 | One-shot. Cobalt block sweeps left to right with Nekko (rotated 90 degrees, head forward, bust cut on the leading edge) peeking ahead of it, short hold fully covered (f14-f19), block exits right. First and last frames fully transparent. |

## Known limits

- `welcome`, `celebrate`, `page-wipe` have no background; show them over cream or white. Cream confetti pieces are invisible on a cream page.
- Thumb jiggle is limited to about 3.5 degrees: larger angles open seams between the hand lines and the body outline.
- `page-wipe`: Nekko is visible only during the entry sweep (the leading edge leaves the screen at the hold).
- Confetti moves in two straight segments (burst, fall) with easing, not true arcs.
- `logo-reveal` sparkle is cream (`#E3E2DE`), as asked.
