"""Generator for frontend/public/brand/lottie/core/*.json (agent lottie-core). Run with the brand-tools venv python."""
import math
import os
import sys

REPO = r'c:\Users\minha\Desktop\TechTsunagu'
sys.path.insert(0, os.path.join(REPO, 'docs', 'brand', 'tools'))
import nekko_lottie as nl  # noqa: E402

D = nl.load()
A = D['anchors']
OUT = os.path.join(REPO, 'frontend', 'public', 'brand', 'lottie', 'core')
os.makedirs(OUT, exist_ok=True)

COB = nl.COBALT
WHITE = nl.WHITE
GREEN = [0.1176471, 0.4196078, 0.2274510, 1]   # #1E6B3A
RED = [0.7019608, 0.1490196, 0.1176471, 1]     # #B3261E
AMBER = [0.5411765, 0.3254902, 0.0, 1]         # #8A5300

# easing: (out-handle, in-handle) of the segment that starts at a key
LIN = ((0.0, 0.0), (1.0, 1.0))
EO = ((0.22, 0.61), (0.36, 1.0))      # ease-out cubic: entrances
EI = ((0.55, 0.06), (0.68, 0.19))     # ease-in cubic: exits, falls
EIO = ((0.65, 0.05), (0.36, 1.0))     # ease-in-out cubic
SINE = ((0.37, 0.0), (0.63, 1.0))     # sine in-out: loops


# ------------------------------------------------------------------ helpers
def prop(v):
    return v if isinstance(v, dict) else nl.static(v)


def kf(keys, ease=EO):
    return nl.kf(keys, ease)


def hold(keys):
    """Stepped keyframes [(frame, value), ...] (opacity swaps)."""
    out = []
    for n, (t, v) in enumerate(keys):
        d = {'t': t, 's': [v] if not isinstance(v, (list, tuple)) else list(v)}
        if n < len(keys) - 1:
            d['h'] = 1
        out.append(d)
    return {'a': 1, 'k': out}


def sample(fn, t0, t1, step=1):
    ts = list(range(t0, t1 + 1, step))
    if ts[-1] != t1:
        ts.append(t1)
    return [(t, fn(t), LIN) for t in ts]


def tr(p=(0, 0), a=(0, 0), s=(100, 100), r=0, o=100):
    return {'ty': 'tr', 'p': prop(list(p) if not isinstance(p, dict) else p), 'a': prop(list(a)),
            's': prop(list(s) if not isinstance(s, dict) else s), 'r': prop(r), 'o': prop(o),
            'sk': nl.static(0), 'sa': nl.static(0), 'nm': 'Transform'}


def gr(name, items, **kw):
    return {'ty': 'gr', 'nm': name, 'it': list(items) + [tr(**kw)]}


def fill(color, op=100):
    return {'ty': 'fl', 'nm': 'fill', 'c': nl.static(list(color)), 'o': nl.static(op), 'r': 1}


def stroke(color, w, op=100):
    return {'ty': 'st', 'nm': 'stroke', 'c': nl.static(list(color)), 'o': nl.static(op), 'w': nl.static(w),
            'lc': 2, 'lj': 2, 'ml': 4}


def ellipse(name, c, r):
    return {'ty': 'el', 'nm': name, 'p': nl.static(list(c)), 's': nl.static([2 * r, 2 * r]), 'd': 1}


def rect(name, c, size, r):
    return {'ty': 'rc', 'nm': name, 'p': nl.static(list(c)), 's': nl.static(list(size)), 'r': nl.static(r), 'd': 1}


def poly(name, pts, closed=False):
    z = [[0, 0] for _ in pts]
    return {'ty': 'sh', 'nm': name, 'ks': nl.static({'i': z, 'o': [list(x) for x in z],
                                                     'v': [list(p) for p in pts], 'c': closed})}


def circle(name, c, r):
    """Closed circle that starts at 12 o'clock and runs clockwise (so a trim from 0 draws clockwise from the top)."""
    k = 0.5523 * r
    cx, cy = c
    v = [[cx, cy - r], [cx + r, cy], [cx, cy + r], [cx - r, cy]]
    i = [[-k, 0], [0, -k], [k, 0], [0, k]]
    o = [[k, 0], [0, k], [-k, 0], [0, -k]]
    return {'ty': 'sh', 'nm': name, 'ks': nl.static({'i': i, 'o': o, 'v': v, 'c': True})}


def trim(s=0, e=100):
    return {'ty': 'tm', 'nm': 'Trim Paths', 's': prop(s), 'e': prop(e), 'o': nl.static(0), 'm': 1}


def layer(name, shapes, ind, op, pos=(0, 0), anchor=(0, 0), scale=None, rot=0, opacity=100):
    p = pos if isinstance(pos, dict) else nl.static([pos[0], pos[1], 0])
    return {'ddd': 0, 'ind': ind, 'ty': 4, 'nm': name, 'sr': 1, 'ao': 0, 'ip': 0, 'op': op, 'st': 0, 'bm': 0,
            'ks': {'o': prop(opacity), 'r': prop(rot), 'p': p, 'a': nl.static([anchor[0], anchor[1], 0]),
                   's': prop(scale or [100, 100, 100])},
            'shapes': shapes}


def finish(name, layers, op):
    for n, l in enumerate(layers):      # layers arrive top first; give them ascending ind
        l['ind'] = n + 1
        l['op'] = op
    return {'v': '5.7.0', 'fr': 30, 'ip': 0, 'op': op, 'w': 512, 'h': 512, 'nm': name, 'ddd': 0, 'assets': [],
            'layers': layers}


def p3(x, y):
    return [x, y, 0]


def s3(a, b=None):
    return [a, a if b is None else b, 100]


# ------------------------------------------------------------------ Nekko layer
def nekko(face='neutral', s=0.4, x=256, y=520, op=60, name='Nekko', pos_x=None, scale=None, rot=0, hop=None,
          blink=None, extra_face=None, extra_opacity=None, base_opacity=None):
    """Nekko bust, bottom-centre pivot at (x, y). pos_x: kf of x. hop: kf of [0, dy] on an inner group.
    blink: list of (frame, [sx, sy]) keys for the neutral eyes. extra_face: second face swapped in by hold keys."""
    g = nl.nekko_groups(D, face, s)
    if blink:
        c = [(A['eye_l'][0] + A['eye_r'][0]) / 2 * s, (A['eye_l'][1] + A['eye_r'][1]) / 2 * s]
        t = g['eyes']['it'][-1]
        t['a'] = nl.static(c)
        t['p'] = nl.static(c)
        t['s'] = kf(blink, EO)
    shapes = nl.top_first(g)
    if extra_face:
        g2 = nl.nekko_groups(D, extra_face, s)
        for k in ('eyes', 'mouth'):
            g[k]['it'][-1]['o'] = hold(base_opacity)
            g2[k]['it'][-1]['o'] = hold(extra_opacity)
        shapes = [g2['eyes'], g2['mouth']] + shapes
    inner = gr('Nekko-hop', shapes, p=hop if hop else (0, 0))
    pos = pos_x if pos_x else (x, y)
    return layer(name, [inner], 1, op, pos=pos, anchor=nl.bottom_center(D, s), scale=scale, rot=rot)


def save(anim, fname):
    path = os.path.join(OUT, fname)
    nl.save(anim, path)
    return path


# ------------------------------------------------------------------ 1. loading (36 frames, loop)
def loading():
    N = 36
    s = 0.40
    cx0, dot_y, r = 256, 118, 17
    dots = []
    for i in range(3):
        cx = cx0 + (i - 1) * 66
        b = 3 + 7 * i                         # leap frame
        base = dot_y + r                      # pivot at the dot's bottom
        pos = [(0, [cx, base], LIN), (b, [cx, base], EO), (b + 7, [cx, base - 46], EI), (b + 14, [cx, base], LIN),
               (N, [cx, base])]
        sq = [(0, [100, 100], LIN), (b - 3, [100, 100], EO), (b, [118, 84], EO), (b + 3, [88, 114], EIO),
              (b + 7, [100, 100], LIN), (b + 14, [100, 100], EO), (b + 16, [124, 78], EO), (b + 20, [100, 100], LIN),
              (N, [100, 100])]
        grp = gr(f'dot-{i + 1}', [ellipse('dot', (cx, dot_y), r), fill(COB)], p=kf(pos), a=(cx, base), s=kf(sq))
        dots.append(grp)
    bob = kf([(0, [0, 0], SINE), (9, [0, -12], SINE), (18, [0, 0], SINE), (27, [0, -12], SINE), (36, [0, 0])])
    rot = kf([(0, 0, SINE), (9, 2.2, SINE), (18, 0, SINE), (27, -2.2, SINE), (36, 0)])
    blink = [(0, [100, 100]), (21, [100, 100]), (24, [100, 6]), (27, [100, 100]), (36, [100, 100])]
    nk = nekko('neutral', s, 256, 536, N, hop=bob, rot=rot, blink=blink)
    dl = layer('Dots', list(reversed(dots)), 1, N)
    return finish('loading', [dl, nk], N)


# ------------------------------------------------------------------ badge parts
def ring_group(c, r, w, color, e):
    return gr('ring', [circle('ring-path', c, r), trim(0, e), stroke(color, w)])


def check_group(c, r, w, color, e):
    cx, cy = c
    pts = [(cx - 0.40 * r, cy + 0.02 * r), (cx - 0.10 * r, cy + 0.33 * r), (cx + 0.42 * r, cy - 0.28 * r)]
    return gr('check', [poly('check-path', pts), trim(0, e), stroke(color, w)])


def x_group(c, r, w, color):
    cx, cy = c
    return gr('x', [poly('x-a', [(cx - r, cy - r), (cx + r, cy + r)]), poly('x-b', [(cx + r, cy - r), (cx - r, cy + r)]),
                    stroke(color, w)])


BADGE = (418, 122)


# ------------------------------------------------------------------ 2. success (40 frames, one-shot)
def success():
    N = 40
    s = 0.40
    r, w = 64, 18
    pop = kf([(0, s3(0), EO), (10, s3(108), EIO), (14, s3(97), EIO), (18, s3(100))])
    wob = kf([(0, 0, LIN), (12, 0, SINE), (19, -3, SINE), (26, 2, SINE), (33, 0)])
    nk = nekko('happy', s, 236, 520, N, scale=pop, rot=wob)
    bpop = kf([(0, s3(0), LIN), (8, s3(0), EO), (15, s3(112), EIO), (19, s3(96), EIO), (23, s3(100), LIN),
               (32, s3(100), EO), (35, s3(108), EIO), (39, s3(100))])
    ring = ring_group(BADGE, r, w, GREEN, kf([(10, 0), (27, 100)], EIO))
    chk = check_group(BADGE, r, 20, GREEN, kf([(25, 0), (33, 100)], EO))
    badge = layer('Badge', [chk, ring], 1, N, pos=BADGE, anchor=BADGE, scale=bpop)
    return finish('success', [badge, nk], N)


# ------------------------------------------------------------------ 3. fail (36 frames, one-shot)
def fail():
    N = 36
    s = 0.40
    amp = [(6, 0), (8, -16), (11, 15), (14, -11), (17, 8), (20, -5), (23, 3), (26, 0)]
    posx = kf([(0, p3(236, 520), LIN)] + [(t, p3(236 + v, 520), SINE) for t, v in amp[:-1]] + [(26, p3(236, 520))], SINE)
    rot = kf([(6, 0, SINE)] + [(t, v * 0.15, SINE) for t, v in amp[1:-1]] + [(26, 0)], SINE)
    pop = kf([(0, s3(0), EO), (6, s3(106), EIO), (10, s3(100))])
    nk = nekko('error', s, 236, 520, N, pos_x=posx, scale=pop, rot=rot)
    xr = 36
    xpop = kf([(0, s3(0), LIN), (12, s3(0), EO), (17, s3(128), EIO), (21, s3(94), EIO), (24, s3(100))])
    xrot = kf([(12, -40, EO), (24, 0)])
    xl = layer('X', [x_group(BADGE, xr * 0.55, 26, RED)], 1, N, pos=BADGE, anchor=BADGE, scale=xpop, rot=xrot)
    return finish('fail', [xl, nk], N)


# ------------------------------------------------------------------ 4. warning (48 frames, loop)
def warning():
    N = 48
    s = 0.40
    c = (402, BADGE[1])
    cx, cy = c
    tri = gr('triangle', [poly('tri-path', [(cx, cy - 66), (cx - 70, cy + 54), (cx + 70, cy + 54)], True),
                          stroke(AMBER, 28), fill(AMBER)])
    bar = gr('exclamation-bar', [rect('bar', (cx, cy - 2), (16, 48), 8), fill(WHITE)])
    dot = gr('exclamation-dot', [ellipse('dot', (cx, cy + 36), 9.5), fill(WHITE)])
    pulse = kf([(0, s3(100), EO), (7, s3(115), EIO), (17, s3(100), LIN), (24, s3(100), EO), (31, s3(115), EIO),
                (41, s3(100), LIN), (48, s3(100))])
    tl = layer('Warning-triangle', [dot, bar, tri], 1, N, pos=(cx, cy + 10), anchor=(cx, cy + 10), scale=pulse)
    bob = kf([(0, [0, 0], SINE), (24, [0, -6], SINE), (48, [0, 0])])
    # tremble: +-1 degree, three cycles per loop
    trem = kf([(t, 1.0 if (t // 8) % 2 == 0 else -1.0) for t in range(0, 49, 8)], SINE)
    nk = nekko('worried', s, 236, 524, N, hop=bob, rot=trem)
    return finish('warning', [tl, nk], N)


# ------------------------------------------------------------------ 5. saving (48 frames, loop)
def saving():
    N = 48
    s = 0.30
    rc, rr = (256, 330), 170
    track = gr('ring-track', [circle('track', rc, rr), stroke(COB, 22, 16)])
    arc = gr('ring-arc', [circle('arc', rc, rr), trim(0, kf([(0, 30, SINE), (24, 72, SINE), (48, 30)], SINE)),
                          stroke(COB, 22)],
             p=rc, a=rc, r=kf([(0, 0, LIN), (48, 360)], LIN))
    rl = layer('Ring', [arc, track], 1, N)
    bob = kf([(0, [0, 0], SINE), (12, [0, -4], SINE), (24, [0, 0], SINE), (36, [0, -4], SINE), (48, [0, 0])])
    blink = [(0, [100, 100]), (30, [100, 100]), (33, [100, 6]), (36, [100, 100]), (48, [100, 100])]
    nk = nekko('neutral', s, 256, 522, N, hop=bob, blink=blink)
    return finish('saving', [nk, rl], N)


# ------------------------------------------------------------------ upload parts
UP_S = 0.27
UP_C = 343                 # track centre y
TRK_L, TRK_R = 40, 472
SEG_L, SEG_R = 60, 452
NW = 986 * UP_S
N0, N1 = NW / 2 + 8, 512 - NW / 2 - 8


def seg_pct(nx):
    """Nekko centre x -> position on the bar centreline in percent."""
    p = SEG_L + (nx - N0) / (N1 - N0) * (SEG_R - SEG_L)
    return (p - SEG_L) / (SEG_R - SEG_L) * 100


def track_layer():
    trk = gr('track', [rect('track', (256, UP_C), (TRK_R - TRK_L, 44), 22), stroke(COB, 10), fill(WHITE)])
    return layer('Track', [trk], 1, 0)


def seg_layer(s_prop, e_prop):
    line = poly('bar-path', [(SEG_L, UP_C), (SEG_R, UP_C)])
    g = gr('bar', [line, trim(s_prop, e_prop), stroke(COB, 22)])
    return layer('Bar', [g], 1, 0)


# ------------------------------------------------------------------ 6. upload-progress (60 frames, loop)
def upload_progress():
    N = 60
    y = UP_C + 18
    nx = kf([(0, p3(N0, y), SINE), (30, p3(N1, y), SINE), (60, p3(N0, y))], SINE)

    def Nf(t):
        return N0 + (N1 - N0) * 0.5 * (1 - math.cos(2 * math.pi * t / 60))
    lag = 9
    sp, ep = [], []
    for t in range(0, N + 1, 2):
        a, b = seg_pct(Nf(t)), seg_pct(Nf(t - lag))
        sp.append((t, min(a, b), LIN))
        ep.append((t, max(a, b), LIN))
    hops = kf([(12 * i + k, [0, v]) for i in range(5) for k, v in ((0, 0), (6, -7))] + [(60, [0, 0])], SINE)
    rot = kf([(0, 0, SINE), (15, 4, SINE), (30, 0, SINE), (45, -4, SINE), (60, 0)], SINE)
    blink = [(0, [100, 100]), (40, [100, 100]), (43, [100, 6]), (46, [100, 100]), (60, [100, 100])]
    nk = nekko('neutral', UP_S, 0, 0, N, pos_x=nx, rot=rot, hop=hops, blink=blink)
    return finish('upload-progress', [seg_layer(kf(sp, LIN), kf(ep, LIN)), track_layer(), nk], N)


# ------------------------------------------------------------------ 7. upload-done (64 frames, one-shot)
def upload_done():
    N = 64
    y = UP_C + 18
    xc = 256
    nx = kf([(0, p3(N0, y), EIO), (26, p3(N1, y), LIN), (30, p3(N1, y), EIO), (41, p3(xc, y))], EIO)
    ep = kf([(0, 1.0, EIO), (26, 100)], EIO)
    ep['k'].append({'t': 63, 's': [100]})
    hop = kf([(0, [0, 0], LIN), (30, [0, 0], EO), (36, [0, -40], EI), (41, [0, 0])], EO)
    scl = kf([(0, s3(100), LIN), (26, s3(100), EO), (29, s3(110, 86), EO), (31, s3(94, 110), EIO), (35, s3(100), LIN),
              (41, s3(100), EO), (44, s3(116, 82), EO), (48, s3(97, 104), EIO), (52, s3(100))], EO)
    blink = [(0, [100, 100]), (14, [100, 100]), (17, [100, 6]), (20, [100, 100]), (63, [100, 100])]
    nk = nekko('neutral', UP_S, 0, 0, N, pos_x=nx, scale=scl, hop=hop, blink=blink, extra_face='happy',
               base_opacity=[(0, 100), (35, 0)], extra_opacity=[(0, 0), (35, 100)])
    r, w = 48, 14
    c = (404, 118)
    bpop = kf([(0, s3(0), LIN), (40, s3(0), EO), (47, s3(112), EIO), (51, s3(96), EIO), (55, s3(100))], EO)
    ring = ring_group(c, r, w, GREEN, kf([(42, 0), (54, 100)], EIO))
    chk = check_group(c, r, 15, GREEN, kf([(52, 0), (60, 100)], EO))
    badge = layer('Badge', [chk, ring], 1, N, pos=c, anchor=c, scale=bpop)
    return finish('upload-done', [badge, seg_layer(0, ep), track_layer(), nk], N)


BUILD = {'loading': loading, 'success': success, 'fail': fail, 'warning': warning, 'saving': saving,
         'upload-progress': upload_progress, 'upload-done': upload_done}

if __name__ == '__main__':
    names = sys.argv[1:] or list(BUILD)
    for n in names:
        p = save(BUILD[n](), n + '.json')
        print(n, round(os.path.getsize(p) / 1024, 1), 'KB')
