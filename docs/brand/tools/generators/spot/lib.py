import re, math
from pathlib import Path

REPO = Path(r'c:\Users\minha\Desktop\TechTsunagu')
EXPR = REPO / 'frontend/public/brand/master/nekko-expressions'
OUT = REPO / 'frontend/public/brand/images/spot'

C = '#1351AA'   # cobalt (the one blue)
J = '#141414'   # jet
W = '#FFFFFF'
CR = '#E3E2DE'  # cream
BK = '#D8D7D3'  # backing
OK = '#1E6B3A'
BAD = '#B3261E'
WARN = '#8A5300'

CARD = (40, 40, 720, 720, 100)
LEDGE_Y = 704
S = 0.52  # nekko scale, same in every scene so line weights match
SW = round(34 * S)   # prop main stroke = Nekko outline/face weight (18)
SD = 10              # prop detail stroke (Nekko hand line 16*S = 8, bumped for 200 px legibility)


def nekko_inner(expr):
    t = (EXPR / f'nekko-{expr}.svg').read_text(encoding='utf-8')
    m = re.search(r'<svg[^>]*>(.*)</svg>', t, re.S)
    return m.group(1)


def f(v):
    s = f'{v:.1f}'
    return s[:-2] if s.endswith('.0') else s


def nekko(expr, tx, ty, s=S):
    assert ty + 860 * s >= LEDGE_Y - 0.5, f'bust cut floats: {ty + 860 * s}'
    return f'<g transform="translate({f(tx)} {f(ty)}) scale({s})">{nekko_inner(expr)}</g>'


def style(fill='none', stroke=None, sw=SW, extra=''):
    a = f'fill="{fill}"'
    if stroke:
        a += f' stroke="{stroke}" stroke-width="{sw}" stroke-linecap="round" stroke-linejoin="round"'
    return a + (' ' + extra if extra else '')


def rect(x, y, w, h, rx=0, fill='none', stroke=None, sw=SW, extra=''):
    return f'<rect x="{f(x)}" y="{f(y)}" width="{f(w)}" height="{f(h)}" rx="{f(rx)}" {style(fill, stroke, sw, extra)}/>'


def circle(cx, cy, r, fill='none', stroke=None, sw=SW, extra=''):
    return f'<circle cx="{f(cx)}" cy="{f(cy)}" r="{f(r)}" {style(fill, stroke, sw, extra)}/>'


def path(d, fill='none', stroke=None, sw=SW, extra=''):
    return f'<path d="{d}" {style(fill, stroke, sw, extra)}/>'


def line(x1, y1, x2, y2, stroke=C, sw=SD):
    return path(f'M{f(x1)} {f(y1)}L{f(x2)} {f(y2)}', 'none', stroke, sw)


def g(inner, transform=None, extra=''):
    t = f' transform="{transform}"' if transform else ''
    return f'<g{t}{(" " + extra) if extra else ""}>{inner}</g>'


def at(x, y, rot=0, sc=1):
    parts = [f'translate({f(x)} {f(y)})']
    if rot:
        parts.append(f'rotate({f(rot)})')
    if sc != 1:
        parts.append(f'scale({sc})')
    return ' '.join(parts)


# ---------------------------------------------------------------- scene frame


def scene(stem, under='', nek='', over='', ledge=True, ledge_fill=C, card_fill=BK, debug=False):
    cid = f'{stem}-clip'
    x, y, w, h, r = CARD
    body = rect(x, y, w, h, r, card_fill)
    body += under + nek
    if ledge:
        body += rect(x, LEDGE_Y, w, y + h - LEDGE_Y, 0, ledge_fill)
    body += over
    if debug:
        for i in range(0, 801, 50):
            body += f'<line x1="{i}" y1="0" x2="{i}" y2="800" stroke="#f0f" stroke-width="1"/>'
            body += f'<line x1="0" y1="{i}" x2="800" y2="{i}" stroke="#f0f" stroke-width="1"/>'
    return (
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 800 800" width="800" height="800">'
        f'<defs><clipPath id="{cid}"><rect x="{x}" y="{y}" width="{w}" height="{h}" rx="{r}"/></clipPath></defs>'
        f'<g clip-path="url(#{cid})">{body}</g></svg>\n'
    )


# ---------------------------------------------------------------- props
def padlock(cx, cy, is_open=False, body=W, key=J, sc=1.0, sw=SW):
    """cx,cy = centre of the padlock body. Width 120, body height 92."""
    sh = ''
    if is_open:
        sh = path('M-38 -46V-96A38 38 0 0 1 38 -96V-78', 'none', body, sw)
    else:
        sh = path('M-38 -44V-66A38 38 0 0 1 38 -66V-44', 'none', body, sw)
    b = rect(-60, -46, 120, 92, 20, body)
    k = circle(0, -8, 11, key) + path('M0 -8V20', 'none', key, 12)
    return g(sh + b + k, at(cx, cy, 0, sc))


def tick(cx, cy, size=1.0, stroke=W, sw=SW):
    return path(f'M{f(cx-34*size)} {f(cy+2*size)}L{f(cx-8*size)} {f(cy+28*size)}L{f(cx+36*size)} {f(cy-26*size)}', 'none', stroke, sw)


def gear(cx, cy, r=60, teeth=8, fill=C, hole=W, sw=SW):
    ro, ri = r, r * 0.78
    tw = math.pi / teeth * 0.52
    tt = math.pi / teeth * 0.33
    d = ''
    for k in range(teeth):
        a = 2 * math.pi * k / teeth
        seg = [(ri, a - tw), (ro, a - tt), (ro, a + tt), (ri, a + tw)]
        for j, (rr, aa) in enumerate(seg):
            x, y = cx + rr * math.cos(aa), cy + rr * math.sin(aa)
            d += ('M' if (k == 0 and j == 0) else 'L') + f'{f(x)} {f(y)}'
        a2 = 2 * math.pi * (k + 1) / teeth - tw
        x, y = cx + ri * math.cos(a2), cy + ri * math.sin(a2)
        d += f'A{f(ri)} {f(ri)} 0 0 1 {f(x)} {f(y)}'
    d += 'Z'
    return path(d, fill, fill, 10) + circle(cx, cy, r * 0.32, hole)


QR_ROWS = [
    '11111011011',
    '10001010100',
    '10101001011',
    '10101010110',
    '11111001001',
    '00000000110',
    '10110100101',
    '01001010010',
    '10101101110',
    '01010010101',
    '11011100011',
]


def qr(x, y, cell=14, fg=J, bg=W, pad=12, rx=14):
    """QR-like sticker made of blocks; x,y = top-left of the white sticker. 11x11 cells."""
    n = 11
    size = n * cell
    out = rect(x, y, size + 2 * pad, size + 2 * pad, rx, bg)
    ox, oy = x + pad, y + pad
    d = ''

    def blk(cx_, cy_, w=1, h=1):
        return f'M{f(ox+cx_*cell)} {f(oy+cy_*cell)}h{f(w*cell)}v{f(h*cell)}h{f(-w*cell)}z'

    # finder patterns 5x5: ring, gap, dot
    for (fx, fy) in [(0, 0), (6, 0), (0, 6)]:
        d += blk(fx, fy, 5, 1) + blk(fx, fy + 4, 5, 1) + blk(fx, fy + 1, 1, 3) + blk(fx + 4, fy + 1, 1, 3)
        d += blk(fx + 2, fy + 2, 1, 1)
    # data blocks: everything not in a finder region
    for r_, row in enumerate(QR_ROWS):
        for c_, ch in enumerate(row):
            if ch != '1':
                continue
            infinder = (c_ < 5 and r_ < 5) or (c_ > 5 and r_ < 5) or (c_ < 5 and r_ > 5)
            if infinder:
                continue
            d += blk(c_, r_)
    out += f'<path d="{d}" fill="{fg}"/>'
    return out, size + 2 * pad


def sparkle(cx, cy, r=14, fill=C):
    d = f'M{f(cx)} {f(cy-r)}Q{f(cx)} {f(cy)} {f(cx+r)} {f(cy)}Q{f(cx)} {f(cy)} {f(cx)} {f(cy+r)}Q{f(cx)} {f(cy)} {f(cx-r)} {f(cy)}Q{f(cx)} {f(cy)} {f(cx)} {f(cy-r)}Z'
    return path(d, fill, fill, 6)


def dots_progress(done, total=4, cx=400, y=100, r=15, gap=54):
    out = ''
    x0 = cx - gap * (total - 1) / 2
    for i in range(total):
        x = x0 + gap * i
        if i < done:
            out += circle(x, y, r, C)
        else:
            out += circle(x, y, r - 3, 'none', C, 6, '')
    return out
