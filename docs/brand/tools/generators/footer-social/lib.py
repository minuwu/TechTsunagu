"""Shared helpers for the footer-social brand images (vector only, flat colour)."""
import os
import re

ROOT = r'c:\Users\minha\Desktop\TechTsunagu'
MASTER = os.path.join(ROOT, 'frontend', 'public', 'brand', 'master')
OUT_FOOTER = os.path.join(ROOT, 'frontend', 'public', 'brand', 'images', 'footer')
OUT_SOCIAL = os.path.join(ROOT, 'frontend', 'public', 'brand', 'images', 'social')

CREAM = '#E3E2DE'
JET = '#141414'
COB = '#1351AA'
WHITE = '#FFFFFF'
HAIR = '#C7C7C7'

_cache = {}


def _inner(path):
    if path not in _cache:
        s = open(path, encoding='utf-8').read()
        _cache[path] = re.search(r'<svg[^>]*>(.*)</svg>', s, re.S).group(1).strip()
    return _cache[path]


def n(v):
    s = f'{v:.2f}'.rstrip('0').rstrip('.')
    return s if s not in ('-0', '') else '0'


def svg(w, h, body):
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">'
            f'{body}</svg>\n')


def rect(x, y, w, h, fill):
    return f'<rect x="{n(x)}" y="{n(y)}" width="{n(w)}" height="{n(h)}" fill="{fill}"/>'


def hl(x, y, w, h=2, fill=HAIR):
    """hairline rectangle (horizontal if w>h)"""
    return (f'<rect x="{n(x)}" y="{n(y)}" width="{n(w)}" height="{n(h)}" fill="{fill}" '
            f'shape-rendering="crispEdges"/>')


def ring(cx, cy, r, color=HAIR, w=2):
    return (f'<circle cx="{n(cx)}" cy="{n(cy)}" r="{n(r)}" fill="none" stroke="{color}" '
            f'stroke-width="{n(w)}"/>')


def disc(cx, cy, r, fill):
    return f'<circle cx="{n(cx)}" cy="{n(cy)}" r="{n(r)}" fill="{fill}"/>'


def quarter(cx, cy, r, corner, fill):
    """quarter disc centred on (cx,cy) filling the given quadrant: tl, tr, bl, br."""
    sx = -1 if corner[1] == 'l' else 1
    sy = -1 if corner[0] == 't' else 1
    p = (f'M{n(cx)} {n(cy)}L{n(cx + sx * r)} {n(cy)}'
         f'A{n(r)} {n(r)} 0 0 {1 if sx * sy > 0 else 0} {n(cx)} {n(cy + sy * r)}Z')
    return f'<path d="{p}" fill="{fill}"/>'


def tri(pts, fill):
    d = 'M' + 'L'.join(f'{n(x)} {n(y)}' for x, y in pts) + 'Z'
    return f'<path d="{d}" fill="{fill}"/>'


def tick(vx, vy, t, a, b, fill=COB):
    """Signature device: a check mark built from two rectangles (an L turned -45 deg),
    emitted as one outline so no seam shows. (vx,vy) = bottom vertex; t thickness,
    a short arm, b long arm."""
    d = f'M0 0H{n(b)}V{n(-t)}H{n(t)}V{n(-a)}H0Z'
    return f'<path transform="translate({n(vx)} {n(vy)}) rotate(-45)" d="{d}" fill="{fill}"/>'


def nekko(cx, by, h, expr='standalone'):
    """Nekko bust, bottom-centre at (cx,by), height h (frame 986x860)."""
    path = (os.path.join(MASTER, 'nekko-standalone.svg') if expr == 'standalone'
            else os.path.join(MASTER, 'nekko-expressions', f'nekko-{expr}.svg'))
    s = h / 860
    w = 986 * s
    return (f'<g transform="translate({n(cx - w / 2)} {n(by - h)}) scale({s:.5f})">'
            f'{_inner(path)}</g>')


def face_mark(cx, top, size):
    """tiny Nekko face mark; (cx, top) = top-centre of the 64-unit frame scaled to size."""
    s = size / 64
    p = os.path.join(MASTER, 'nekko-face-mark.svg')
    return (f'<g transform="translate({n(cx - size / 2)} {n(top)}) scale({s:.5f})">'
            f'{_inner(p)}</g>')


def bridge_width(u, k=2):
    return (20 * k + 4) * u


def bridge(x0, wl, u, k=2, body=JET, holes=CREAM, refl=(COB,), flat=False):
    """Megane Bridge from Brand.tsx: k stepped arches (radius 8u), solid body above the
    waterline y=wl, arch openings, and the reflection half discs below the waterline.
    x0 = left edge. refl = fill per arch (None = hairline ring half), cycled.
    Mark geometry: end piers 4u, arches 16u, piers 4u; steps y=-6u over arches, -10u... see below."""
    # relative x (units): arches span [4+20i, 20+20i]
    pts = [(0, 0), (0, -6), (4, -6)]
    for i in range(k):
        a0, a1 = 4 + 20 * i, 20 + 20 * i
        if flat and i < k - 1:
            pts += [(a0, -10), (a1 + 4, -10)]
        else:
            pts += [(a0, -10), (a1, -10), (a1, -6), (a1 + 4, -6)]
    pts += [(20 * k + 4, 0)]
    d = 'M' + 'L'.join(f'{n(x0 + x * u)} {n(wl + y * u)}' for x, y in pts) + 'Z'
    out = [f'<path d="{d}" fill="{body}"/>']
    r = 8 * u
    for i in range(k):
        xl = x0 + (4 + 20 * i) * u
        xr = xl + 16 * u
        out.append(f'<path d="M{n(xl)} {n(wl)}A{n(r)} {n(r)} 0 0 1 {n(xr)} {n(wl)}Z" fill="{holes}"/>')
    for i in range(k):
        xl = x0 + (4 + 20 * i) * u
        xr = xl + 16 * u
        f = refl[i % len(refl)]
        arc = f'M{n(xl)} {n(wl)}A{n(r)} {n(r)} 0 0 0 {n(xr)} {n(wl)}Z'
        if f is None:
            out.append(f'<path d="{arc}" fill="none" stroke="{HAIR}" stroke-width="2"/>')
        else:
            out.append(f'<path d="{arc}" fill="{f}"/>')
    return ''.join(out)


def write(folder, stem, w, h, body):
    os.makedirs(folder, exist_ok=True)
    p = os.path.join(folder, stem + '.svg')
    with open(p, 'w', encoding='utf-8', newline='\n') as f:
        f.write(svg(w, h, rect(0, 0, w, h, CREAM) + body))
    return p
