# Generators (reference)

The Python scripts that produced the Brand kit's Lottie files, icons, scenes and wordmarks, one folder per group. Use them to change or extend an asset by editing a script and re-running it, instead of hand-editing JSON or path data.

They are **not turnkey**: they were written in a scratch folder and still point at it (look for `AppData`, `scratchpad` or `work` near the top of each file; point those at your checkout, `frontend/public/brand/` and the tools venv). Pipeline:

1. Lottie scripts import `docs/brand/tools/nekko_lottie.py` and write with `nekko_lottie.save()`, so the cobalt colour stays exact.
2. Check with `tools/lottie-check.cjs`; build contact sheets with `tools/sheet.py`.
3. After any change run `tools/recolor.py frontend/public/brand --render` (Icon-blue twins), then `tools/gallery.py`.

Rules for changes: [../../SPEC.md](../../SPEC.md). Hand-editing an SVG is fine too; keep one blue (`#1351AA`) so the recolour script can build the twin.
