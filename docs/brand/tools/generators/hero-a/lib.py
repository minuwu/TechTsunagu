"""Poster-grammar helpers for hero-a slides (scratch only)."""
import math
import random
from pathlib import Path

MASTER = Path(r'c:\Users\minha\Desktop\TechTsunagu\frontend\public\brand\master')
CREAM, JET, COB, WHITE, HAIR = '#E3E2DE', '#141414', '#1351AA', '#FFFFFF', '#C7C7C7'


def f(n):
    s = f'{n:.2f}'.rstrip('0').rstrip('.')
    return s if s not in ('-0', '') else '0'


def rect(x, y, w, h, fill):
    return f'<rect x="{f(x)}" y="{f(y)}" width="{f(w)}" height="{f(h)}" fill="{fill}"/>'


def poly(pts, fill):
    return '<polygon points="%s" fill="%s"/>' % (' '.join(f'{f(x)},{f(y)}' for x, y in pts), fill)


def circ(cx, cy, r, fill):
    return f'<circle cx="{f(cx)}" cy="{f(cy)}" r="{f(r)}" fill="{fill}"/>'


def path(d, fill=None, stroke=None, sw=None, extra=''):
    a = f'fill="{fill}"' if fill else 'fill="none"'
    if stroke:
        a += f' stroke="{stroke}" stroke-width="{f(sw)}" stroke-linecap="butt" stroke-linejoin="miter"'
    return f'<path d="{d}" {a}{extra}/>'


def ground(w, h, step, vertical=True, horizontal_ys=()):
    """Cream ground and faint hairline column grid."""
    out = [rect(0, 0, w, h, CREAM)]
    d = ''
    x = step
    while x < w:
        d += f'M{x} 0V{h}'
        x += step
    for y in horizontal_ys:
        d += f'M0 {y}H{w}'
    out.append(path(d, stroke=HAIR, sw=2))
    return '\n'.join(out)


def nekko(cx, bottom, scale, expr='neutral', gid='nekko'):
    """Nekko bust: centred on cx, flat bottom at y=bottom."""
    s = (MASTER / 'nekko-expressions' / f'nekko-{expr}.svg').read_text(encoding='utf-8')
    inner = s[s.index('>', s.index('<svg')) + 1: s.rindex('</svg>')]
    tx, ty = cx - 493 * scale, bottom - 860 * scale
    return f'<g id="{gid}" transform="translate({f(tx)} {f(ty)}) scale({scale})">{inner}</g>'


def tick(cx, cy, S, L, t, fill=COB, gid=None):
    """Two-rectangle tick. (cx,cy) = outer corner at the bottom of the V."""
    i = f' id="{gid}"' if gid else ''
    return (f'<g{i} transform="translate({f(cx)} {f(cy)}) rotate(-45)" fill="{fill}">'
            f'<rect x="0" y="{f(-S)}" width="{f(t)}" height="{f(S)}"/>'
            f'<rect x="0" y="{f(-t)}" width="{f(L)}" height="{f(t)}"/></g>')


def arch_body(cx, base, w, h, fill):
    r = w / 2
    return path(f'M{f(cx - r)} {f(base)}V{f(base - h + r)}A{f(r)} {f(r)} 0 0 1 {f(cx + r)} {f(base - h + r)}V{f(base)}Z', fill=fill)


def figure(cx, base, w, h, body, head, r, gap=12):
    return arch_body(cx, base, w, h, body) + circ(cx, base - h - gap - r, r, head)


def qr(x, y, size, seed=7, n=11, fg=JET, bg=WHITE):
    """Text-free QR-like sticker: n x n modules, three finder squares."""
    m = size / n
    out = [rect(x, y, size, size, bg)]
    rnd = random.Random(seed)
    d = ''
    fin = [(0, 0), (n - 3, 0), (0, n - 3)]

    def infin(i, j):
        return any(a <= i < a + 3 and b <= j < b + 3 for a, b in fin)
    for j in range(n):
        for i in range(n):
            if infin(i, j):
                continue
            # leave a one-module quiet gap around finders
            if any(a - 1 <= i < a + 4 and b - 1 <= j < b + 4 for a, b in fin):
                continue
            if rnd.random() < 0.52:
                d += f'M{f(x + i * m)} {f(y + j * m)}h{f(m)}v{f(m)}h{f(-m)}z'
    for a, b in fin:
        d += f'M{f(x + a * m)} {f(y + b * m)}h{f(3 * m)}v{f(3 * m)}h{f(-3 * m)}z'
        # hollow centre (even-odd inside same path)
        d += f'M{f(x + (a + 1) * m)} {f(y + (b + 1) * m)}h{f(m)}v{f(m)}h{f(-m)}z'
    out.append(path(d, fill=fg, extra=' fill-rule="evenodd"'))
    return '\n'.join(out)


def padlock(cx, top, bw, bh, shackle_t, lift, fill=COB, hole=WHITE, open_=True):
    """Padlock: body rect with keyhole, J-shaped shackle lifted when open. top = body top y."""
    out = []
    r = (bw * 0.62 - shackle_t) / 2  # centre-line radius
    xl, xr = cx - r, cx + r
    ya = top - lift - r  # arch centre y
    if open_:
        d = f'M{f(xl)} {f(top + 30)}V{f(ya)}A{f(r)} {f(r)} 0 0 1 {f(xr)} {f(ya)}V{f(ya + lift * 0.45)}'
    else:
        d = f'M{f(xl)} {f(top + 30)}V{f(ya)}A{f(r)} {f(r)} 0 0 1 {f(xr)} {f(ya)}V{f(top + 30)}'
    out.append(path(d, stroke=fill, sw=shackle_t))
    out.append(rect(cx - bw / 2, top, bw, bh, fill))
    # keyhole: circle + stem
    kr = bw * 0.09
    out.append(circ(cx, top + bh * 0.40, kr, hole))
    out.append(poly([(cx - kr * 0.55, top + bh * 0.40), (cx + kr * 0.55, top + bh * 0.40), (cx + kr * 0.8, top + bh * 0.80), (cx - kr * 0.8, top + bh * 0.80)], hole))
    return '\n'.join(out)


def bridge(x0, x1, y_raised, y_pier, y_spring, R, centres, bottom, jet=JET, cob=COB):
    """Megane Bridge: jet body with raised blocks over arches, cobalt water band with reflection holes."""
    d = f'M{f(x0)} {f(y_pier)}'
    cs = sorted(centres)
    for c in cs:
        d += f'H{f(c - R)}V{f(y_raised)}H{f(c + R)}V{f(y_pier)}'
    d += f'H{f(x1)}V{f(y_spring)}'
    for c in reversed(cs):
        d += f'H{f(c + R)}A{f(R)} {f(R)} 0 0 0 {f(c - R)} {f(y_spring)}'
    d += f'H{f(x0)}Z'
    body = path(d, fill=jet)
    wd = f'M{f(x0)} {f(y_spring)}H{f(x1)}V{f(bottom)}H{f(x0)}Z'
    for c in cs:
        wd += f'M{f(c - R)} {f(y_spring)}A{f(R)} {f(R)} 0 0 0 {f(c + R)} {f(y_spring)}Z'
    water = path(wd, fill=cob, extra=' fill-rule="evenodd"')
    return body + '\n' + water


def svg(w, h, title, body):
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}" role="img" '
            f'aria-label="{title}"><title>{title}</title>\n{body}\n</svg>\n')
