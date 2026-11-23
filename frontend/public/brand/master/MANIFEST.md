# Nekko master set

Traced from `docs/brand/source/nekko-original.jpg` by `docs/brand/tools/trace.py` (rerun it to regenerate every file here, this MANIFEST included). Only `#1351AA` (Site cobalt) and `#FFFFFF` appear; the Icon-blue twin is made later by script. Contract: `docs/brand/SPEC.md` section 5.

## Files

| File | Purpose | viewBox | KB |
| :--- | :--- | :--- | ---: |
| `nekko-icon-full-bleed.svg` | iOS 1024 master: cobalt square, white bust to the bottom edge, whiskers, linework. Opaque, no mask, no shadow. One white even-odd path (the traced white mask; eyes, face and hand lines are holes in it) over a rect. | 0 0 1024 1024 | 11.3 |
| `nekko-icon-squircle.svg` | Same drawing under a rounded-square clip (rx 229.4 = 22.4%). Preview only. | 0 0 1024 1024 | 11.4 |
| `nekko-standalone.svg` | Transparent. Cobalt outline, white body, cobalt linework, cobalt whiskers. Bust cut flat at the viewBox bottom, no outline on the cut. For cream and white. | 0 0 986 860 | 16.0 |
| `nekko-rig.svg` | Same drawing, named groups (below). | 0 0 986 860 | 16.2 |
| `nekko-expressions/nekko-{neutral,happy,worried,surprised,sleepy,error}.svg` | Standalone with eyes and mouth swapped. neutral = the traced original. Body, brow mark, nose, hand and whiskers unchanged. | 0 0 986 860 | 16.0 each |
| `nekko-face-mark.svg` | Hand-drawn head mark (two ears, two dot eyes, smile) for 16-48 px favicons. Checked at 16, 32 and 48 px enlarged 8x. | 0 0 64 64 | 0.6 |
| `nekko-lottie-shapes.json` | Parts as Lottie bezier paths, expression items, anchors. Use through `docs/brand/tools/nekko_lottie.py`. | 986 x 860 | 18.2 |

## Coordinates

- Icon files use the 1024 canvas (source crop resampled to 2048, units = px / 2).
- Standalone, rig, expressions and the Lottie JSON share one frame: `viewBox="0 0 986 860"`. It is the icon canvas window x 31..1017, y 164..1024, shifted so the origin is 0,0: standalone = canvas - (31, 164). Margin is 16 units on top, left and right (measured to the outline and the whisker tips); 0 at the bottom, where the bust is cut flush with the viewBox edge. Bottom-centre pivot: (493, 860).
- Left/right (`eye-l`, `whisker-l`) are the VIEWER's left and right: `eye-l` is on the thumb side.

## Measured weights (units)

- Face linework (brow, nose, mouth): 34. Outline thickness: 34 (the silhouette dilated by this much). New expression strokes use 34.
- Hand and arm lines: 16. Whiskers: 14.9 drawn blue on white, 12.1 drawn white on blue; the outer whiskers are grown by 1.5 units per side in cobalt so the weight matches where they cross the edge.
- Eye dot radius 25.81. Face tilt 5.31 degrees (brow bar, eyes and mouth share it; expression strokes follow it).

## Rig groups (`nekko-rig.svg`, bottom to top)

`outline` (solid cobalt, silhouette dilated), `silhouette` (solid white, no holes), `arm-line`, `hand` (thumb, fist and divider lines), `brow` (bar, tick, two strokes), `eyes` (`eye-l`, `eye-r`), `nose` (loop plus its white island, even-odd), `mouth`, `whiskers` (`whisker-l`, `whisker-r1`, `whisker-r2`; each holds the cobalt line inside the body and the stroke outside it). Every linework part is its own cobalt fill on top of the white silhouette (nothing is a hole), so expressions and blinks swap parts cleanly.

Hand-placed cuts where strokes were one blob in the source: nose | mouth along y = 411 (standalone) between the two junction notches; arm-line | hand at the fist/arm junction; the left whisker's inner segment | hand at the divider line. Neighbouring parts overlap by 2.5 units so no hairline shows.

## Expressions

All six keep body, brow mark, nose, hand and whiskers. neutral is the trace. happy: arc eyes, open smile. worried: dot eyes dropped 16 units, slanted brows, wavy down mouth. surprised: ringed eyes with pupils, small round mouth. sleepy: closed lids, small smile. error: x eyes, wavy mouth. New strokes have round caps and the face weight; the worried brows and surprised rings use the hand-line weight (16) because the forehead bar leaves about 40 units above the eyes. Mouths overlap the nose's flat base (`mouth_join` anchor) so nose and mouth stay joined.

## Vertex counts and fidelity

Fine trace: potrace alphamax 1.0, opttolerance 0.2 (the SVG files). Lottie trace: coarsest alphamax/opttolerance step of a ladder with at most 118 vertices per path and IoU >= 0.99 against the fine trace.

| Part | Fine vertices | Lottie vertices (max per path) | Lottie IoU vs fine |
| :--- | ---: | ---: | ---: |
| `outline` | 66 | 49 | 0.9995 |
| `silhouette` | 79 | 49 | 0.9995 |
| `arm-line` | 24 | 21 | 0.9922 |
| `hand` | 64 | 50 | 0.9927 |
| `brow` | 49 | 30 | 0.9941 |
| `eye-l` | 8 | 5 | 0.9914 |
| `eye-r` | 8 | 3 | 0.9909 |
| `nose` | 14 | 6 | 0.9907 |
| `mouth` | 27 | 11 | 0.9911 |
| `whisker-l-in` | 11 | 9 | 0.9931 |
| `whisker-r1-in` | 11 | 10 | 0.9984 |
| `whisker-r2-in` | 14 | 14 | 1.0000 |
| `whisker-l-out` | 11 | 11 | 1.0000 |
| `whisker-r1-out` | 10 | 9 | 0.9997 |
| `whisker-r2-out` | 11 | 7 | 0.9952 |

Full-bleed white path: 286 vertices. Full-bleed render vs source (`docs/brand/source/trace-check.png`: source crop | render | difference): IoU **0.9983** against the raw JPEG crop (resized to 1024, thresholded at the blue/white midpoint, inside the icon hull eroded 5 px) and **0.9973** against the cleaned mask that was traced.

## Anchors (standalone coordinates, in `nekko-lottie-shapes.json` under `anchors`)

eye_l [373.56, 293.89], eye_r [635.22, 318.19], mouth_center [492.75, 425.0], mouth_join [492.99, 413.5], nose_center [497.87, 377.03], face_center [500.51, 345.69], brow_bbox [319.5, 166.0, 703.0, 338.0], hand_center [149.3, 456.01], head_bbox [195.0, 50.0, 865.5, 456.0] (approximate: down to the cheek line, thumb excluded), silhouette_bbox [50.0, 50.0, 866.0, 860.0], body_bottom_y 860, whisker_bboxes, weights.

## Known limits

- The source box is 1857 x 1847 px, resampled to a square: 0.5% non-uniform stretch (inside the 3% allowance).
- The rounded bottom corners cut the body short; it is extended straight down by up to 6.5 units (the arm's left edge is vertical over that stretch instead of continuing its slant).
- The silhouette is smoothed where strokes reach the edge (thumb notch, whisker roots): notches narrower than about 24 units are filled, and the cobalt strokes overrun the edge by 1.5 units under the outline.
- The brief mentions two white whiskers on the left. The source has one: the white stroke outside the thumb plus its blue continuation to the right of the thumb. The right side has two. The left outer whisker stops about 15 units short of the thumb in the source; in the standalone it joins the outline.
- Whiskers are 12-15 units, hand lines 16, face 34: the outline (34) is bold next to the hand lines. It is the measured face weight, as specified.
- The face mark is hand-drawn, not traced; it leaves out the forehead mark and whiskers (invisible at 16 px).
- `nekko-lottie-shapes.json` rounds to 0.1 unit. A full Nekko layer from `nekko_lottie.py` is about 18 KB of Lottie JSON (60 KB cap per file): leave out parts you do not animate if a scene needs the budget.
- The raw-crop IoU leaves out the hull's edge band, the filled rounded corners and the bottom extension (the raw crop holds grey backdrop and shadow there that would read as white). The traced-mask IoU covers the whole canvas.
- The bust cut: `silhouette` and `outline` are traced with corners kept (alphamax 1.0) and pinned to y <= 860 so the cut stays flat and flush; the run on the cut is x [166.0, 748.0] (fine) / [166.0, 748.0] (Lottie) for the silhouette.
