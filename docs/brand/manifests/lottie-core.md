# Lottie core

Folder `frontend/public/brand/lottie/core/`. 512x512, 30 fps, transparent, shape layers only (no text, expressions, effects, assets, audio), track R. Cobalt is the float triple `[0.0745098, 0.3176471, 0.6666667, 1]`; state colours are #1E6B3A, #B3261E, #8A5300. All files pass `docs/brand/tools/lottie-check.cjs`. Built by a generator script on `docs/brand/tools/nekko_lottie.py`.

| File | Duration | Type | KB | Notes |
| :--- | :--- | :--- | ---: | :--- |
| `loading.json` | 1.20 s (36 f) | loop | 24.3 | Neutral Nekko bobs and tilts, one blink; three cobalt dots leap in sequence with squash and stretch. |
| `success.json` | 1.33 s (40 f) | one-shot | 21.1 | Happy Nekko pops in with overshoot and settles with a small wobble; green ring draws (trim path), check strokes in, badge pops. Holds the last pose. |
| `fail.json` | 1.20 s (36 f) | one-shot | 21.5 | Error Nekko pops in, then a horizontal shake with decay; danger-red X pops in with overshoot and a short turn. Holds. |
| `warning.json` | 1.60 s (48 f) | loop | 22.4 | Worried Nekko bobs with a slight tremble; amber triangle pulses twice per loop; the exclamation is a rounded bar plus a dot (shapes, white). |
| `saving.json` | 1.60 s (48 f) | loop | 20.9 | Cobalt trim-path arc, length breathing 30-72%, spins one turn around a Nekko bust over a 16% cobalt track ring. |
| `upload-progress.json` | 2.00 s (60 f) | loop | 26.1 | Indeterminate bar (a cobalt segment trimmed along the track); Nekko rides its leading edge with small hops. Ping-pongs left to right and back, seamless. |
| `upload-done.json` | 2.13 s (64 f) | one-shot | 26.2 | Bar fills while Nekko rides to the right end, squashes, returns to the centre, swaps to the happy face and hops once; green ring and check appear top right. Holds. |

## Known limits

- Nekko is a bust cut flat at the canvas bottom (loading, success, fail, warning, saving) or sits on the bar (upload files). The bust is cut by the frame edge, as the SPEC allows.
- One-shots start from an empty frame (scale 0), which is the pop-in.
- The saving ring passes behind Nekko's lower body, so only the upper arc reads fully.
- Expression swaps use stepped opacity keys (upload-done); other files keep a single face.
- The check, X, ring and exclamation use the fixed state colours; the Icon-blue twin changes only the cobalt.
