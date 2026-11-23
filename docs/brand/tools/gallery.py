"""Build frontend/public/brand/index.html: one static page that previews every Brand kit asset.

usage: python gallery.py   (run from the repo root with the brand-tools venv python; run recolor.py first)

- Groups by folder, shows SVG (or the raster when no same-stem SVG exists), skips sized renders of an SVG.
- Lottie files play live; their JSON is embedded, so the page works from file:// with no server.
- Toggle: Site cobalt / Icon blue (swaps to the *.iconblue.* twin when it exists); background: cream / white / dark.
- lottie-web is copied next to the page (brand/_gallery/lottie.min.js) from the brand-tools node_modules (NODE_PATH).
"""
import html
import json
import os
import re
import shutil
from pathlib import Path

ROOT = Path('frontend/public/brand')
ORDER = ['master', 'icons', 'favicon', 'identity', 'lottie', 'images']
SHOW = {'.svg', '.png', '.webp', '.ico'}


def twin(p: Path):
    t = p.with_name(p.stem + '.iconblue' + p.suffix)
    return t if t.exists() else None


def keep(p: Path):
    if '.iconblue.' in p.name or p.name == 'index.html':
        return False
    if '_gallery' in p.parts:
        return False
    if p.suffix not in SHOW and p.suffix != '.json':
        return False
    if p.suffix == '.json':
        try:
            return 'layers' in json.loads(p.read_text(encoding='utf-8').replace('﻿', ''))
        except ValueError:
            return False
    if p.suffix in ('.png', '.webp'):  # sized render of an SVG: show the SVG only
        m = re.match(r'^(.*)-(\d+)$', p.stem)
        if m and p.with_name(m.group(1) + '.svg').exists():
            return False
    return True


def rel(p: Path):
    return p.relative_to(ROOT).as_posix()


def main():
    files = [p for p in sorted(ROOT.rglob('*')) if p.is_file() and keep(p)]
    groups = {}
    for p in files:
        groups.setdefault(p.parent.relative_to(ROOT).as_posix(), []).append(p)
    keys = sorted(groups, key=lambda k: (ORDER.index(k.split('/')[0]) if k.split('/')[0] in ORDER else 99, k))

    lottie = {}
    parts = []
    for k in keys:
        cards = []
        for p in groups[k]:
            kb = p.stat().st_size / 1024
            name = html.escape(p.name)
            t = twin(p)
            if p.suffix == '.json':
                lid = 'L%d' % len(lottie)
                lottie[lid] = {'a': json.loads(p.read_text(encoding='utf-8').replace('﻿', ''))}
                if t:
                    lottie[lid]['b'] = json.loads(t.read_text(encoding='utf-8'))
                media = f'<div class="lot" data-id="{lid}"></div>'
            else:
                alt = f' data-alt="{rel(t)}"' if t else ''
                media = f'<img loading="lazy" src="{rel(p)}" data-src="{rel(p)}"{alt} alt="{name}">'
            cards.append(
                f'<figure><a href="{rel(p)}" target="_blank">{media}</a><figcaption>{name}<span>{kb:.0f} KB</span></figcaption></figure>'
            )
        parts.append(f'<section><h2>{html.escape(k)} <small>{len(cards)}</small></h2><div class="grid">{"".join(cards)}</div></section>')

    gdir = ROOT / '_gallery'
    gdir.mkdir(exist_ok=True)
    lw = Path(os.environ.get('NODE_PATH', '')) / 'lottie-web/build/player/lottie.min.js'
    if lw.exists():
        shutil.copy(lw, gdir / 'lottie.min.js')

    page = f'''<!doctype html><html lang="en"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>Wakatta? Brand kit</title>
<style>
:root{{--bg:#E3E2DE;--fg:#141414}}body{{margin:0;font:14px system-ui,sans-serif;background:var(--bg);color:var(--fg)}}
header{{position:sticky;top:0;z-index:5;background:#141414;color:#E3E2DE;padding:10px 20px;display:flex;gap:16px;align-items:center;flex-wrap:wrap}}
header h1{{font-size:16px;margin:0 12px 0 0}}header button{{background:none;color:inherit;border:1px solid #777;padding:4px 10px;cursor:pointer}}
header button.on{{background:#1351AA;border-color:#1351AA}}main{{padding:20px}}
section{{margin-bottom:36px}}h2{{font-size:15px;border-bottom:2px solid var(--fg);padding-bottom:4px}}h2 small{{opacity:.5}}
.grid{{display:grid;grid-template-columns:repeat(auto-fill,minmax(220px,1fr));gap:14px}}
figure{{margin:0;background:var(--cell,#fff);border:1px solid #c7c7c7}}figure a{{display:flex;align-items:center;justify-content:center;height:200px;padding:8px}}
figure img,.lot{{max-width:100%;max-height:100%;width:100%;height:100%;object-fit:contain}}
figcaption{{font-size:11px;padding:6px 8px;border-top:1px solid #c7c7c7;display:flex;justify-content:space-between;gap:8px;word-break:break-all;background:#fff}}figcaption span{{opacity:.5;white-space:nowrap}}
</style></head><body>
<header><h1>Wakatta? Brand kit</h1>
<span>Blue:</span><button id="b1" class="on">Site cobalt #1351AA</button><button id="b2">Icon blue #246DD4</button>
<span>Background:</span><button data-bg="#fff" class="bg on">White</button><button data-bg="#E3E2DE" class="bg">Cream</button><button data-bg="#141414" class="bg">Dark</button></header>
<main>{"".join(parts)}</main>
<script src="_gallery/lottie.min.js"></script>
<script>
const L={json.dumps(lottie, separators=(',', ':'))};
let alt=false;const anims=[];
document.querySelectorAll('.lot').forEach(el=>{{const d=L[el.dataset.id];el._d=d;el._p=lottie.loadAnimation({{container:el,renderer:'svg',loop:true,autoplay:true,animationData:JSON.parse(JSON.stringify(d.a))}});}});
function setBlue(b){{alt=b;b1.classList.toggle('on',!b);b2.classList.toggle('on',b);
document.querySelectorAll('img[data-alt]').forEach(i=>i.src=b?i.dataset.alt:i.dataset.src);
document.querySelectorAll('.lot').forEach(el=>{{if(!el._d.b)return;el._p.destroy();el._p=lottie.loadAnimation({{container:el,renderer:'svg',loop:true,autoplay:true,animationData:JSON.parse(JSON.stringify(b?el._d.b:el._d.a))}});}});}}
b1.onclick=()=>setBlue(false);b2.onclick=()=>setBlue(true);
document.querySelectorAll('.bg').forEach(b=>b.onclick=()=>{{document.querySelectorAll('.bg').forEach(x=>x.classList.remove('on'));b.classList.add('on');document.querySelectorAll('figure').forEach(f=>f.style.setProperty('--cell',b.dataset.bg));}});
</script></body></html>'''
    (ROOT / 'index.html').write_text(page, encoding='utf-8')
    print(f'gallery: {len(files)} assets in {len(keys)} groups, {len(lottie)} lottie -> {ROOT / "index.html"}')


main()
