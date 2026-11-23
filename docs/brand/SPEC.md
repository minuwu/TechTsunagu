# Brand kit: build spec

The contract every brand-kit agent follows. The result is the **Brand kit** of the mascot **Nekko** ([GLOSSARY.md](../../GLOSSARY.md), "Brand"). Decisions behind it are settled; do not reopen them.

Settled decisions: both blues ship (Site cobalt `#1351AA` primary, Icon blue `#246DD4` alternate); website images use the Mixed style (below); everything is vector (no image-generation model exists), plus a prompt pack for external generators; assets only: nothing under `frontend/src`, no `package.json` edit, no commits.

## 1. Hard rules for every agent

1. **No git.** Run no `git` command at all. Skills that say "sync the repo first" (logo-designer does): skip that step.
2. **Write only inside your owned folder** (given in your task). Never touch `frontend/src`, `package.json`, `GLOSSARY.md`, `AGENTS.md`, `docs/adr/` or another agent's folder. Scratch work goes to `<scratchpad>\work\<your-name>\`, never into the repo.
3. **No attribution text** anywhere: no "Claude", "Anthropic", "AI-generated" in files, comments, metadata or file names.
4. **Do not spawn sub-agents** and do not start the skills' own sub-agents.
5. **Vector only.** No `<image>` elements, no embedded raster, no fonts, no `<text>` (outline any wordmark), no filters (blur, drop-shadow), no gradients, no external references. Every SVG has a `viewBox`, unique ids, and is valid XML.
6. **One blue.** The only brand-blue hex in any SVG or Lottie file is `#1351AA`. For lighter or darker blue use opacity, never a new hex. A script recolours `#1351AA` to `#246DD4` to make the Icon-blue set, so any other blue would not follow. Safety-sign blue (JIS) is the one exception and only inside safety pictograms.
7. **No baked-in text** in images (the site is ja/en/bn). Only the wordmark assets under `identity/` carry letters. Nekko's forehead mark stays as drawn.
8. **Look at your output.** Render every file to PNG, build a contact sheet, open it with the Read tool, fix what looks wrong, then write `MANIFEST.md` in your folder (file, purpose, size, notes, known limits). The manager reviews contact sheets, not your word.
9. Reading the repo for context is fine; if you search code, query the graph first: `graphify query "<question>"` from `c:\Users\minha\Desktop\TechTsunagu`.

## 2. Tools

```
SCRATCH = C:\Users\minha\AppData\Local\Temp\claude\e6e9e47624cef00c\6b9adb8f-cbed-4977-8a20-a1870f863912\scratchpad
PY      = %SCRATCH%\brand-tools\venv\Scripts\python.exe        (numpy, pillow, opencv, potracer, fonttools, brotli)
NODE_PATH = %SCRATCH%\brand-tools\node_modules                  (sharp, playwright-core, lottie-web)
```

PowerShell example (the repo root is the working directory):

```powershell
$s = "C:\Users\minha\AppData\Local\Temp\claude\e6e9e47624cef00c\6b9adb8f-cbed-4977-8a20-a1870f863912\scratchpad"
$env:NODE_PATH = "$s\brand-tools\node_modules"
node docs\brand\tools\render.cjs in.svg out.png 1024 "#E3E2DE"
& "$s\brand-tools\venv\Scripts\python.exe" docs\brand\tools\sheet.py "$s\work\me\sheet.png" 4 320 "#E3E2DE" a.png b.png
node docs\brand\tools\lottie-check.cjs anim.json "$s\work\me\frames" 6
```

Details in [tools/README.md](tools/README.md). `pip install` into the venv is allowed if you need a library; never into the global Python, never `--break-system-packages`.

## 3. Palette

| Token | Hex | Use |
| :--- | :--- | :--- |
| Site cobalt (`--brand`) | `#1351AA` | the one brand blue in every file |
| Cream | `#E3E2DE` | ground in Poster Modernist work |
| Jet | `#141414` | linework, type-like blocks |
| White | `#FFFFFF` | Nekko's body, highlights |
| Hairline | `#C7C7C7` | 1-2 px rules, only on cream |
| Success / Danger / Warning | `#1E6B3A` / `#B3261E` / `#8A5300` | state meaning in Lottie and empty states only |
| Icon blue | `#246DD4` | produced by script from cobalt; never typed by hand |

## 4. Two style tracks (the "Mixed" decision)

| Track | Applies to | Rules |
| :--- | :--- | :--- |
| **P: Poster Modernist** | hero carousel, footer and banners, social cards | Cream/jet/cobalt/white only. **0 px corners**: no rounded rectangles and no circles used as UI containers (circles and arcs are fine as graphic elements: bridge arches, wheels, eyes, dots). Flat blocks, hairline rules, heavy geometric shapes, asymmetric grid layout, large empty cream space. Nekko (standalone, outlined) is the one friendly element. Safety pictograms may reuse the shapes in `frontend/src/ui/v4/art/pictograms.tsx` (read only; copy path data into your SVG) and keep JIS colours. The Nagasaki Megane Bridge motif is in `frontend/src/ui/v4/components/Brand.tsx`. |
| **R: Playful rounded** | app icons, spot illustrations, empty states, 404, all Lottie | Rounded corners and round caps allowed; uniform thick line weight like Nekko's own linework; flat fills; still no gradients or shadows. Palette cream/white/jet/cobalt plus the three state colours where meaning needs them. |

Both tracks: flat, high contrast, readable at small size, consistent line weight within a scene. The departure from the v4 "all art is cream/black/cobalt, 0 px" rule (ADR 0012) is recorded in ADR 0013.

Hero rules: 16:9 desktop (SVG master `viewBox="0 0 1920 1080"`) and a 4:5 mobile slide **recomposed for 4:5**, not cropped from 16:9 (`viewBox="0 0 1080 1350"`). Keep a **text-safe zone**: the left 45% of the 16:9 slide (and the top 40% of the 4:5 slide) holds only flat ground, no detail, because HTML headline and copy sit there.

## 5. The foundation (built first; everyone depends on it)

Folder `frontend/public/brand/master/`, produced from [source/nekko-original.jpg](source/nekko-original.jpg) by `docs/brand/tools/trace.py`. All files use `#1351AA` only.

| File | What |
| :--- | :--- |
| `nekko-icon-full-bleed.svg` | `viewBox="0 0 1024 1024"`, full-bleed cobalt square, white Nekko bust extending to the bottom edge, whiskers, blue linework. Opaque; this is the iOS 1024 master. No grey surround, no shadow, no rounded mask. |
| `nekko-icon-squircle.svg` | the same drawing under a rounded-square mask: preview variant only. |
| `nekko-standalone.svg` | transparent background: white body, **cobalt outline** around the silhouette, whiskers recoloured cobalt, cobalt face and hand linework. Bust cut flat at the bottom of the viewBox. For use on cream and white. |
| `nekko-rig.svg` | one viewBox, named groups: `outline`, `silhouette`, `arm-line`, `hand`, `brow` (forehead mark), `eyes`, `nose`, `mouth`, `whiskers`. Each group editable and animatable on its own. |
| `nekko-expressions/nekko-{neutral,happy,worried,surprised,sleepy,error}.svg` | standalone Nekko with swapped eyes and mouth, same stroke weight, same viewBox as `nekko-standalone.svg`. Everyone uses these; nobody draws a new face. |
| `nekko-face-mark.svg` | hand-simplified head mark (ears, two eyes, smile) for 16-48 px use; checked at 16 and 32 px. |
| `nekko-lottie-shapes.json` | the parts as simplified Lottie bezier paths (`{i,o,v,c}`), at most about 120 vertices each, with the viewBox size and part names. |
| `docs/brand/tools/nekko_lottie.py` | helpers that turn those shapes into Lottie shape groups. |

Nekko is a **bust**: the body is cut off flat at the bottom. Compose scenes so the cut sits on the frame edge or behind a foreground shape (counter, table, ground block). Never show the cut floating in the middle of a scene.

## 6. Output layout

```
frontend/public/brand/        shipped, served
  master/     foundation (above)
  icons/      app icons                     (icons agent)
  favicon/    favicons, PWA, manifest       (favicon agent)
  identity/   wordmark, lockups, brand sheet (identity agent)
  lottie/     core/ flow/ empty/ moments/   (lottie agents)
  images/     hero/ footer/ spot/ social/   (image agents)
docs/brand/                   not served
  source/ tools/ SPEC.md README.md prompts/
```

Naming: lowercase kebab-case. Rasters are rendered from a same-stem SVG and named `<stem>-<width>.png|webp` (for example `hero-1-machine-gate-1920.webp` from `hero-1-machine-gate.svg`). The manager's `recolor.py` uses this convention to build the Icon-blue twins (`<stem>.iconblue.svg`, `<stem>-<width>.iconblue.png`); do not hand-build Icon-blue files, except binary formats a script cannot recolour (`.ico`), which the owning agent writes in both blues.

Size budgets: SVG at most 150 KB (hero scenes 300 KB); Lottie at most 60 KB each; WebP for large scene rasters, PNG for icons and anything with transparency.

## 7. Lottie rules (the site will play them in lottie-web or dotLottie)

- 30 fps, canvas 512x512 unless the task says otherwise. Shape layers only: no text layers, no expressions, no effects, no image assets, no audio. Fills, strokes, trim paths, transforms, parenting, and shape morphs are fine.
- Loops (`loading`, `recording`, `idle`) must end on their first frame's state. One-shots end in a clean held pose.
- Layer and group names (`nm`) are descriptive. Colours use the cobalt float triple `[0.0745098, 0.3176471, 0.6666667, 1]`, the white/jet/state colours from section 3.
- Use Nekko through `nekko_lottie.py` and `nekko-lottie-shapes.json`, not by pasting the raw trace.
- Motion: ease-out on entrances, ease-in on exits, anticipation then overshoot for pops; 200-400 ms for state changes. Read `C:\Users\minha\.claude\skills\motion-design\SKILL.md` (and `text-to-lottie\SKILL.md` as reference; **do not run its player**, validate with `lottie-check.cjs`, which uses lottie-web).
- Every file must pass `lottie-check.cjs` and the contact sheet of its frames must look right.

## 8. Icon rules

- Test each app icon under circle, squircle (rounded square, radius about 22.4%), and Android adaptive masks (visible area is the central 66/108 = 61% of the canvas; the PWA maskable safe zone is a circle of radius 40% of the icon). Ears, hand and whiskers must survive the mask they are meant for; show the mask previews in the contact sheet.
- iOS: 1024x1024 opaque, no alpha, no pre-rounded corners.
- Favicon sizes (16, 32, 48): use `nekko-face-mark.svg`, never the full drawing; inspect each size enlarged 8x with nearest-neighbour.
