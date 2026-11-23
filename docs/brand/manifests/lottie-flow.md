# Lottie flow set

Folder `frontend/public/brand/lottie/flow/`. 512x512, 30 fps, transparent, shape layers only, track R. Cobalt is the exact float triple `[0.0745098, 0.3176471, 0.6666667, 1]`; other colours are white, jet `#141414`, success `#1E6B3A` and warning `#8A5300`. Built by a generator script on `docs/brand/tools/nekko_lottie.py` (kept in the scratch folder, not in the repo). All files pass `docs/brand/tools/lottie-check.cjs`.

| File | Duration | Type | KB | Notes |
| :--- | ---: | :--- | ---: | :--- |
| `machine-unlock.json` | 1.8 s (54 f) | one-shot, holds last pose | 26.7 | Jet tile pops in with a padlock; shackle dips, lifts and swings open; tile lights up cobalt, lock fades, white tick draws (trim path), cobalt rays burst; happy Nekko pops up behind the tile and holds. |
| `check-correct.json` | 1.8 s (54 f) | one-shot, holds | 29.2 | Question card (abstract lines, no text). Nekko pops behind it, one answer row turns cobalt, a green tick badge stamps in (squash, overshoot, rays); Nekko swaps neutral to happy on impact. |
| `check-wrong.json` | 2.2 s (66 f) | one-shot, holds | 30.1 | Card nudges sideways with decaying amplitude (about 20 units to 0), Nekko swaps to worried and tilts slightly, an amber (`#8A5300`) hint bubble with a drawn question mark pops in. No red, no shake. |
| `understood-stamp.json` | 1.8 s (54 f) | one-shot, holds | 7.3 | Paper with abstract lines; a cobalt hanko ring with a bold tick lands from 190% scale with squash, 12 impact lines burst, paper dips 6 units, settle. No Nekko. |
| `voice-recording.json` | 1.6 s (48 f) | seamless loop | 35.0 | Cobalt microphone (beats twice per loop) with three staggered rings pulsing outward and fading; neutral Nekko listens at the bottom edge, breathes and blinks once. Every animated property returns to its frame-0 value at frame 48. |
| `nudge-bell.json` | 1.8 s (54 f) | one-shot, holds | 29.3 | Cobalt bell on a jet bracket swings 3 times (peaks +26, -22, +15, -9, +4.5, -2 degrees) with a lagging jet clapper and sound arcs that decay; surprised Nekko rises from the bottom edge and settles to neutral at frame 40. |

## Known limits

- Nekko in `nudge-bell` uses the surprised face: its small round mouth sits tight under the nose (the known master flaw); it reads as an alert face at 512 px. The hold pose is neutral.
- `machine-unlock` and `check-correct` are drawn off-centre upward (content spans about y 90 to 460) so the bust sits behind the tile or card.
- The tile "light up" is an opacity cross-fade of a cobalt layer over the jet one, so the middle frames show a dark blue.
- Nekko is included whole (about 18 KB each), so no file is much under 27 KB except `understood-stamp`.
