"""Build the Icon-blue twin of every Site-cobalt file in the Brand kit.

usage: python recolor.py <folder> [--render]   (run with the brand-tools venv python)

- <stem>.svg   containing #1351AA          -> <stem>.iconblue.svg   (#246DD4)
- <stem>.json  Lottie with the cobalt float -> <stem>.iconblue.json
- with --render: every <stem>-<width>.png|webp that has a sibling <stem>.svg is re-rendered from
  <stem>.iconblue.svg as <stem>-<width>.iconblue.<ext> (needs NODE_PATH, see tools/README.md).
Files already named *.iconblue.* are skipped. .ico files are not touched: build them per blue by hand.
"""
import json
import os
import re
import subprocess
import sys
from pathlib import Path

COBALT_HEX = '#1351AA'
ICON_HEX = '#246DD4'
COBALT_F = (0x13 / 255, 0x51 / 255, 0xAA / 255)
ICON_F = (0x24 / 255, 0x6D / 255, 0xD4 / 255)
TOL = 0.002
HERE = Path(__file__).resolve().parent


def is_cobalt(a):
    return (
        isinstance(a, list)
        and len(a) in (3, 4)
        and all(isinstance(x, (int, float)) for x in a)
        and all(abs(a[i] - COBALT_F[i]) < TOL for i in range(3))
    )


def walk(o):
    """Replace cobalt colour arrays in place; return the number replaced."""
    n = 0
    if isinstance(o, dict):
        for k, v in o.items():
            if is_cobalt(v):
                o[k] = [*ICON_F, *v[3:]]
                n += 1
            else:
                n += walk(v)
    elif isinstance(o, list):
        for i, v in enumerate(o):
            if is_cobalt(v):
                o[i] = [*ICON_F, *v[3:]]
                n += 1
            else:
                n += walk(v)
    return n


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(2)
    root = Path(sys.argv[1])
    render = '--render' in sys.argv
    svgs = jsons = rasters = 0
    for p in sorted(root.rglob('*')):
        if not p.is_file() or '.iconblue.' in p.name:
            continue
        if p.suffix == '.svg':
            text = p.read_text(encoding='utf-8')
            if re.search(re.escape(COBALT_HEX), text, re.I):
                out = p.with_name(p.stem + '.iconblue.svg')
                out.write_text(re.sub(re.escape(COBALT_HEX), ICON_HEX, text, flags=re.I), encoding='utf-8')
                svgs += 1
        elif p.suffix == '.json':
            try:
                data = json.loads(p.read_text(encoding='utf-8').replace('﻿', ''))
            except ValueError:
                continue
            if isinstance(data, dict) and 'layers' in data and walk(data):
                out = p.with_name(p.stem + '.iconblue.json')
                out.write_text(json.dumps(data, separators=(',', ':')), encoding='utf-8')
                jsons += 1
    if render:
        for p in sorted(root.rglob('*')):
            if not p.is_file() or '.iconblue.' in p.name or p.suffix not in ('.png', '.webp'):
                continue
            m = re.match(r'^(.*)-(\d+)$', p.stem)
            if not m:
                continue
            src = p.with_name(m.group(1) + '.iconblue.svg')
            if not src.exists():
                continue
            out = p.with_name(f'{m.group(1)}-{m.group(2)}.iconblue{p.suffix}')
            subprocess.run(
                ['node', str(HERE / 'render.cjs'), str(src), str(out), m.group(2), 'none'],
                check=True,
                env=os.environ,
                shell=(os.name == 'nt'),
            )
            rasters += 1
    print(f'recoloured: {svgs} svg, {jsons} lottie, {rasters} raster')


main()
