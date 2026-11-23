# Brand kit tools

Scripts that build and check the Brand kit. Heavy dependencies live **outside the repo** (a venv and a `node_modules`), so nothing here adds to `package.json` or the served `frontend/public/`.

## One-time setup (Windows, PowerShell)

```powershell
$bt = "<any folder outside the repo>\brand-tools"
python -m venv "$bt\venv"
& "$bt\venv\Scripts\python.exe" -m pip install numpy pillow opencv-python-headless potracer fonttools brotli
cd $bt; npm init -y; npm install sharp playwright-core lottie-web
```

Before running any `.cjs` script: `$env:NODE_PATH = "$bt\node_modules"`. The Lottie check drives the installed Chrome (`C:/Program Files/Google/Chrome/Application/chrome.exe`, override with `CHROME_PATH`); no browser download.

## Scripts

| Script | Use |
| :--- | :--- |
| `render.cjs` | `node render.cjs in.svg out.png\|webp <width> [bg-hex\|none]`: SVG to PNG/WebP. Outline text first; no fonts are loaded. |
| `sheet.py` | `python sheet.py out.png <cols> <cell-px> <bg-hex> img1 img2 ...`: contact sheet with file names, for eyeballing a batch. |
| `lottie-check.cjs` | `node lottie-check.cjs anim.json out-dir [frames] [bg]`: plays the file in lottie-web, saves frames, fails on text layers, effects, expressions, image assets or runtime errors. |
| `trace.py`, `nekko_lottie.py`, `recolor.py` | Added by the foundation and finishing steps; see [../SPEC.md](../SPEC.md). |
