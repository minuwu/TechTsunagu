# Lottie empty states

Folder `frontend/public/brand/lottie/empty/`. 512x512, 30 fps, transparent, 90 frames (3.00 s), seamless loops (frame 0 = last state). Shape layers only, track R, cobalt float triple exact, state colours danger `#B3261E` and warning `#8A5300`, jet `#141414`. All pass `lottie-check.cjs`.

| File | Duration | KB | Notes |
| :--- | ---: | ---: | :--- |
| `empty-list.json` | 3.0 s | 48.1 | Sleepy Nekko resting behind an empty tray (bust cut hidden by the tray). Three drawn Z paths float up, fade in and out, staggered 30 frames. Breathing plus slight sway. |
| `no-results.json` | 3.0 s | 36.0 | Worried Nekko, eyes follow a magnifying glass that sweeps left to right. Two drawn question marks bob. One blink. |
| `offline.json` | 3.0 s | 36.1 | Sleepy Nekko. Wi-Fi arcs pulse in sequence under a danger-red slash that swells. Unplugged cable sways above a wall socket (cable runs off the top edge). |
| `error-404.json` | 3.0 s | 36.0 | Worried Nekko tilts its head, puzzled. A chain link breaks: halves drift apart, red sparks flash, halves return. No digits. |
| `maintenance.json` | 3.0 s | 30.5 | Neutral Nekko in a cobalt/white hard hat, gear rotates (135 deg per loop, bumps on hits), wrench taps the gear twice per loop with warning-colour sparks. |

Generator: scratchpad `work\lottie-empty\build.py` and `lib.py` (not in repo).

Known limits: hard hat sits on the neutral head by position only (no rig attach), so the head is not tilted; the magnifying glass lens is unfilled and overlaps the face; offline and error-404 push the bust cut 12-14 px below the frame so sway does not show a wedge; Nekko is not mirrored, so the thumb stays on the viewer's left.
