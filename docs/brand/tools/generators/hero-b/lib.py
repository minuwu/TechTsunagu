"""Shared helpers for the hero-b poster SVGs (vector only, flat, 0 px corners)."""
import math, re, os

REPO = r'c:\Users\minha\Desktop\TechTsunagu'
MASTER = REPO + r'\frontend\public\brand\master'
CREAM, JET, COB, WH, HAIR = '#E3E2DE', '#141414', '#1351AA', '#FFFFFF', '#C7C7C7'


def f(v):
    s = ('%.2f' % v).rstrip('0').rstrip('.')
    return s if s != '-0' else '0'


def nekko_inner(expr):
    p = MASTER + (r'\nekko-standalone.svg' if expr == 'standalone' else r'\nekko-expressions\nekko-%s.svg' % expr)
    s = open(p, encoding='utf8').read()
    s = s[s.index('>') + 1:]
    return s[:s.rindex('</svg>')]


def nekko(expr, x, y, s):
    """Place Nekko; (x, y) is the top-left of its 986x860 frame, s the scale."""
    return '<g transform="translate(%s %s) scale(%s)">%s</g>' % (f(x), f(y), f(s), nekko_inner(expr))


def nekko_bottom(expr, x, ybot, s):
    return nekko(expr, x, ybot - 860 * s, s)


def rect(x, y, w, h, fill, op=None, extra=''):
    o = ' fill-opacity="%s"' % op if op is not None else ''
    return '<rect x="%s" y="%s" width="%s" height="%s" fill="%s"%s%s/>' % (f(x), f(y), f(w), f(h), fill, o, extra)


def poly(pts, fill, op=None, extra=''):
    o = ' fill-opacity="%s"' % op if op is not None else ''
    return '<polygon points="%s" fill="%s"%s%s/>' % (' '.join('%s,%s' % (f(a), f(b)) for a, b in pts), fill, o, extra)


def circ(cx, cy, r, fill, op=None):
    o = ' fill-opacity="%s"' % op if op is not None else ''
    return '<circle cx="%s" cy="%s" r="%s" fill="%s"%s/>' % (f(cx), f(cy), f(r), fill, o)


def path(d, fill, op=None, extra=''):
    o = ' fill-opacity="%s"' % op if op is not None else ''
    return '<path d="%s" fill="%s"%s%s/>' % (d, fill, o, extra)


def rot(a, cx, cy, inner):
    return '<g transform="rotate(%s %s %s)">%s</g>' % (f(a), f(cx), f(cy), inner)


def tr(x, y, inner, s=1):
    sc = '' if s == 1 else ' scale(%s)' % f(s)
    return '<g transform="translate(%s %s)%s">%s</g>' % (f(x), f(y), sc, inner)


def grid(W, H, step=160, skip=()):
    out = []
    k = 1
    while k * step < W:
        if k not in skip:
            out.append(rect(k * step - 1, 0, 2, H, HAIR))
        k += 1
    return ''.join(out)


def tick(x, y, size, fill, t=0.22, a=0.42, b=1.0):
    """Check mark made of two rectangles. Fits a box whose longer side is `size`, top-left at (x, y)."""
    r2 = math.sqrt(0.5)
    u1 = (-r2, -r2)  # short arm, from the vertex up-left
    u2 = (r2, -r2)   # long arm, from the vertex up-right

    def arm(u, L):
        n = (-u[1], u[0])
        s0 = (-u[0] * t / 2, -u[1] * t / 2)
        e0 = (u[0] * L, u[1] * L)
        return [(s0[0] + n[0] * t / 2, s0[1] + n[1] * t / 2), (e0[0] + n[0] * t / 2, e0[1] + n[1] * t / 2),
                (e0[0] - n[0] * t / 2, e0[1] - n[1] * t / 2), (s0[0] - n[0] * t / 2, s0[1] - n[1] * t / 2)]
    p1, p2 = arm(u1, a), arm(u2, b)
    xs = [p[0] for p in p1 + p2]
    ys = [p[1] for p in p1 + p2]
    w, h = max(xs) - min(xs), max(ys) - min(ys)
    k = size / max(w, h)
    ox, oy = x - min(xs) * k, y - min(ys) * k
    tp = lambda P: [(ox + p[0] * k, oy + p[1] * k) for p in P]
    return poly(tp(p1), fill) + poly(tp(p2), fill), (w * k, h * k)


def corner_cut_sheet(x, y, w, h, c, fill=WH, cut=None):
    """Paper sheet with the top-right corner cut diagonally (cream triangle shows the fold)."""
    if cut is None:
        cut = 0
    pts = [(x, y), (x + w - c, y), (x + w, y + c), (x + w, y + h), (x, y + h)]
    return poly(pts, fill)


def bars(x, y, w, n, gap=22, h=10, fill=HAIR, last=0.6):
    out = []
    for i in range(n):
        ww = w * (last if i == n - 1 else 1)
        out.append(rect(x, y + i * gap, ww, h, fill))
    return ''.join(out)


def svg(W, H, body, title=None):
    return ('<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 %d %d" width="%d" height="%d">'
            '<rect width="%d" height="%d" fill="%s"/>%s</svg>\n') % (W, H, W, H, W, H, CREAM, body)


# ---- pictograms (copied from frontend/src/ui/v4/art/pictograms.tsx, 64x64 box) ----
SAFETY = {
    'helmet': '<circle cx="32" cy="32" r="29" fill="#005387"/>'
              '<path d="M 19 26 C 19 17 25 14 34 14 C 43 14 47 18 47 26 Z" fill="#FFFFFF"/>'
              '<polygon points="17,26 51,26 47,23 18,23" fill="#FFFFFF"/>'
              '<path d="M 23 28 C 23 28 23 40 30 42 C 34 43 38 43 41 38 C 42 36 43 33 43 28 Z" fill="#FFFFFF"/>'
              '<polygon points="26,41 34,41 35,49 27,49" fill="#FFFFFF"/>'
              '<line x1="18" y1="27" x2="48" y2="27" stroke="#005387" stroke-width="2"/>'
              '<path d="M 25 28 L 32 44 L 37 41" fill="none" stroke="#FFFFFF" stroke-width="2"/>',
    'forklift': '<polygon points="32,5 60,57 4,57" fill="#F9A800" stroke="#141414" stroke-width="4" stroke-linejoin="miter"/>'
                '<polygon points="20,44 34,44 34,34 27,34 22,38 20,38" fill="#141414"/>'
                '<path d="M 21 38 L 22 27 L 31 27 L 32 34" fill="none" stroke="#141414" stroke-width="2.5"/>'
                '<circle cx="27" cy="31" r="2.5" fill="#141414"/>'
                '<rect x="34" y="24" width="3" height="20" fill="#141414"/>'
                '<polygon points="34,44 48,44 48,42 37,42" fill="#141414"/>'
                '<rect x="38" y="32" width="8" height="10" fill="#141414"/>'
                '<circle cx="23" cy="46" r="3.5" fill="#141414"/>'
                '<circle cx="33" cy="46" r="3.5" fill="#141414"/>',
    'exit': '<rect x="3" y="3" width="58" height="58" fill="#237F52"/>'
            '<rect x="38" y="15" width="14" height="34" fill="#FFFFFF"/>'
            '<rect x="41" y="18" width="11" height="31" fill="#237F52"/>'
            '<circle cx="27" cy="22" r="3.5" fill="#FFFFFF"/>'
            '<polygon points="25,26 29,25 32,36 28,37" fill="#FFFFFF"/>'
            '<polygon points="30,28 39,26 38,29 29,31" fill="#FFFFFF"/>'
            '<polygon points="26,29 18,34 19,37 27,32" fill="#FFFFFF"/>'
            '<polygon points="30,36 37,42 37,47 34,47 28,39" fill="#FFFFFF"/>'
            '<polygon points="28,37 20,38 14,46 12,44 19,35" fill="#FFFFFF"/>'
            '<polygon points="12,18 19,18 19,15 24,20 19,25 19,22 12,22" fill="#FFFFFF"/>',
}


def safety(name, x, y, size):
    return tr(x, y, SAFETY[name], size / 64.0)


# ---- own flat pictograms (64x64 box, cream/jet/cobalt) ----
def bin_icon(fg=JET, bg=WH):
    return (rect(24, 6, 16, 6, fg) + rect(10, 12, 44, 6, fg) +
            poly([(14, 21), (50, 21), (46, 58), (18, 58)], fg) +
            rect(23, 27, 4, 25, bg) + rect(30, 27, 4, 25, bg) + rect(37, 27, 4, 25, bg))


def bag_icon(fg=JET, bg=WH, tie=COB):
    return (poly([(18, 4), (46, 4), (36, 17), (28, 17)], fg) +
            rect(25, 17, 14, 5, tie) +
            poly([(26, 22), (38, 22), (51, 34), (55, 52), (50, 60), (14, 60), (9, 52), (13, 34)], fg) +
            poly([(19, 38), (24, 38), (19, 52), (15, 52)], bg))


def bell_icon(fg=WH):
    return (rect(30, 5, 4, 7, fg) +
            path('M 16 46 L 16 31 A 16 16 0 0 1 48 31 L 48 46 Z', fg) +
            rect(10, 46, 44, 6, fg) + circ(32, 58, 5, fg) +
            path('M 6 22 A 22 22 0 0 0 6 44', 'none', extra=' stroke="%s" stroke-width="4"' % fg) +
            path('M 58 22 A 22 22 0 0 1 58 44', 'none', extra=' stroke="%s" stroke-width="4"' % fg))


def calendar(x, y, w, h, hl=(2, 4), events=((0, 2), (1, 5), (2, 1), (3, 3), (4, 6), (3, 0)), tickcol=WH, hdr=JET):
    out = [rect(x, y, w, h, WH)]
    hd = round(h * 0.145)
    out.append(rect(x + 14, y + 14, w - 28, hd, hdr))
    out.append(rect(x + 36, y + 14 + hd / 2 - 7, w * 0.34, 14, WH))
    hd += 14
    m = 18
    gx, gw = x + m, w - 2 * m
    wd = round(h * 0.075)
    cw = gw / 7.0
    gy = y + hd + wd + 10
    ch = (y + h - m - gy) / 5.0
    for c in range(7):
        out.append(rect(gx + c * cw + cw / 2 - 11, y + hd + 18, 22, 8, JET))
    for r in range(6):
        out.append(rect(gx, gy + r * ch - 1, gw, 2, HAIR))
    for c in range(8):
        out.append(rect(gx + c * cw - 1, gy, 2, 5 * ch, HAIR))
    for (r, c) in events:
        if (r, c) != hl:
            out.append(rect(gx + c * cw + 9, gy + r * ch + 9, 13, 13, JET))
    r, c = hl
    cx0, cy0 = gx + c * cw, gy + r * ch
    out.append(rect(cx0, cy0, cw, ch, COB))
    tk, (tw, th) = tick(cx0 + cw * 0.2, cy0 + ch * 0.28, cw * 0.6, tickcol, t=0.26)
    out.append(tk)
    # focus brackets around the highlighted cell
    q, b, e = 9, 22, 6
    L = [(cx0 - q, cy0 - q, 1, 1), (cx0 + cw + q, cy0 - q, -1, 1), (cx0 - q, cy0 + ch + q, 1, -1), (cx0 + cw + q, cy0 + ch + q, -1, -1)]
    for (px, py, sx, sy) in L:
        hx = px if sx == 1 else px - b
        hy = py if sy == 1 else py - e
        out.append(rect(hx, hy, b, e, COB))
        vx = px if sx == 1 else px - e
        vy = py if sy == 1 else py - b
        out.append(rect(vx, vy, e, b, COB))
    return ''.join(out), (gx, gy, cw, ch)


def pin(cx, cy, r=13):
    return circ(cx, cy, r, COB) + circ(cx, cy, r * 0.32, WH)


def write(path_, content):
    os.makedirs(os.path.dirname(path_), exist_ok=True)
    open(path_, 'w', encoding='utf8', newline='\n').write(content)
