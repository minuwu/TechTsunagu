# Prompt pack for external image generators

The Brand kit images are vector scenes (see [../SPEC.md](../SPEC.md)). If you also want photo-style or painterly alternatives from a generator (Midjourney, Firefly, Imagen, Flux, Ideogram), paste these prompts. Always attach `frontend/public/brand/master/nekko-icon-full-bleed.png` (render it with `docs/brand/tools/render.cjs`) or `nekko-standalone` as a **character reference**, so the cat stays on model. Generators drift on this mascot, so keep the style block verbatim.

## Style block (paste before every scene)

```
Flat vector illustration, Japanese poster design, cream background (#E3E2DE), jet black (#141414) and one cobalt blue (#1351AA) accent, white fills, thick uniform outlines, no gradients, no shadows, no texture, high contrast, generous empty space on the left for a headline.
Character: Nekko, a chubby white cat bust giving a thumbs-up with a thick cobalt outline, two round dot eyes, a small loop nose, a smile, and a bold cobalt mark on the forehead made of one horizontal bar with short vertical strokes below it; whiskers as thin lines.
```

## Negative prompt (all scenes)

```
text, letters, kana, kanji, numbers, logo, watermark, gradient, drop shadow, 3D render, photorealistic skin, extra fingers, blurry, noisy, cluttered, rounded rectangle UI frames
```

## Hero carousel (16:9, then 4:5 recomposed)

1. `hero-1-machine-gate`: a huge jet-black press machine with a cobalt padlock springing open and a small QR sticker, Nekko behind a counter giving a thumbs-up, bold diagonal composition.
2. `hero-2-ask-in-your-language`: two overlapping rectangular speech blocks in cobalt and jet made of abstract lines, a microphone pictogram, Nekko in the corner listening.
3. `hero-3-senpai-next-door`: Nagasaki Megane Bridge arches with their reflection forming circles, two simple geometric figures sharing a cup of tea, a house with one lit window, Nekko peeking in.
4. `hero-4-notices-that-make-sense`: pinned paper notices, a calendar grid with one highlighted day, a bin and a garbage bag pictogram, a bell, Nekko peeking over a notice board.
5. `hero-5-safety-you-can-pass`: JIS safety pictograms (helmet, forklift, evacuation route) in safety colours on cream, a bold cobalt tick block, Nekko thumbs-up.

## Footer and banners

- `footer-strip`: wide 6:1 strip, Megane Bridge arches along the bottom in jet and cobalt, Nekko peeking over the top edge of the bridge.
- `cta-banner`: wide cobalt block, Nekko giving a thumbs-up at the right edge, empty cobalt on the left.

## Spot illustrations (1:1, playful rounded variant)

Add: `rounded corners, round line caps, soft cream-grey backing shape`.
- Roles: Nekko as a Worker in a hard hat; as a Foreman holding a clipboard; as a Poster pinning a notice with a megaphone; as a Senpai with a tea cup in front of a small house.
- Steps: scanning a QR code on a machine; taking a short quiz card; a padlock turning cobalt and opening; a machine with a green ready light.
- States: sleepy Nekko with an empty box; Nekko with a magnifying glass; Nekko beside an unplugged cable and a Wi-Fi icon with a slash; puzzled Nekko with a broken link; Nekko with crossed-out eyes; Nekko cheering with confetti.

## Social cards

`og-default` 1200x630, `og-square` 1200x1200, `line-share` 1040x1040: centred Nekko with a bold cobalt tick, bridge arches low, keep everything inside the central 60%.

## After generating

Generators will not keep the exact cobalt. Pick the best result, then recolour it to `#1351AA` (or `#246DD4` for the alternate set) and trace it only if you need an SVG; raster output cannot go through `recolor.py`.
