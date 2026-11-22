"""Merge part fragments into the app skeleton.

usage: python merge.py [--only A|B] [--out FILE]
Reads parts/<P>.css (P in A..F), <P>.screens.html, <P>.tabs.html, <P>.overlays.html, <P>.js, <P>.init.js (missing files are skipped)
and replaces the PART:<P>:<SLOT> markers in skeleton.html. Default output: ../app.html
"""
import argparse
import pathlib

HERE = pathlib.Path(__file__).parent
PARTS = 'ABCDEF'
SLOTS = {  # slot -> (file suffix, marker template)
    'CSS': ('.css', '/* PART:{p}:CSS */'),
    'SCREENS': ('.screens.html', '<!-- PART:{p}:SCREENS -->'),
    'TABS': ('.tabs.html', '<!-- PART:{p}:TABS -->'),
    'OVERLAYS': ('.overlays.html', '<!-- PART:{p}:OVERLAYS -->'),
    'JS': ('.js', '// PART:{p}:JS'),
    'INIT': ('.init.js', '// PART:{p}:INIT'),
}

ap = argparse.ArgumentParser()
ap.add_argument('--only', choices=list(PARTS))
ap.add_argument('--out', default=str(HERE.parent / 'app.html'))
args = ap.parse_args()

s = (HERE / 'skeleton.html').read_text(encoding='utf-8')
for p in PARTS:
    for suffix, marker in SLOTS.values():
        marker = marker.format(p=p)
        assert marker in s, f'skeleton lost marker {marker}'
        f = HERE / f'{p}{suffix}'
        body = f.read_text(encoding='utf-8') if (f.exists() and args.only in (None, p)) else ''
        s = s.replace(marker, body)
pathlib.Path(args.out).write_text(s, encoding='utf-8')
print('wrote', args.out)
