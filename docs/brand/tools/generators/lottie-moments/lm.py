"""Helpers for the moments animations (builds on docs/brand/tools/nekko_lottie.py)."""
import math
import sys

sys.path.insert(0, r'C:\Users\minha\Desktop\TechTsunagu\docs\brand\tools')
import nekko_lottie as nl  # noqa: E402
from nekko_lottie import kf, static, COBALT, WHITE  # noqa: E402

CREAM = [0.8901961, 0.8862745, 0.8705882, 1]   # #E3E2DE
JET = [0.0784314, 0.0784314, 0.0784314, 1]     # #141414

# cubic-bezier easings as (out-handle, in-handle) pairs, the shape nl.kf takes
OUT_EXPO = ((0.16, 1.0), (0.3, 1.0))
OUT_CUBIC = ((0.215, 0.61), (0.355, 1.0))
OUT_QUAD = ((0.25, 0.46), (0.45, 0.94))
IN_QUAD = ((0.55, 0.085), (0.68, 0.53))
IN_CUBIC = ((0.55, 0.055), (0.675, 0.19))
IN_OUT = ((0.65, 0.0), (0.35, 1.0))
IN_OUT_SOFT = ((0.45, 0.0), (0.55, 1.0))
LINEAR = ((0.0, 0.0), (1.0, 1.0))

DATA = nl.load()
W, H = DATA['width'], DATA['height']


def fit(w, h, fill):
    return fill * min(w / W, h / H)


# ------------------------------------------------------------------ shape helpers
def poly(pts, closed=True):
    n = len(pts)
    return {'i': [[0, 0]] * n, 'o': [[0, 0]] * n, 'v': [list(p) for p in pts], 'c': closed}


def path_item(bez, nm='path'):
    return {'ty': 'sh', 'nm': nm, 'ks': static(bez)}


def fill_item(color, nm='fill'):
    return {'ty': 'fl', 'nm': nm, 'c': static(list(color)), 'o': static(100), 'r': 1}


def stroke_item(color, w, nm='stroke'):
    return {'ty': 'st', 'nm': nm, 'c': static(list(color)), 'o': static(100), 'w': static(w), 'lc': 2, 'lj': 2, 'ml': 4}


def rect_item(w, h, r=0, nm='rect', pos=(0, 0)):
    """Lottie rectangle. w, h, r, pos may be numbers/lists or ready kf() properties."""
    prop = lambda v: v if isinstance(v, dict) else static(v)
    return {'ty': 'rc', 'nm': nm, 'd': 1, 's': prop([w, h]) if not isinstance(w, dict) else w,
            'p': prop(list(pos)), 'r': prop(r)}


def group(items, nm, tr=None):
    return {'ty': 'gr', 'nm': nm, 'it': list(items) + [tr or nl._tr()]}


def set_tr(g, **kw):
    """Replace transform properties of a group's last item: p, a, s, r, o take a number/list or kf property."""
    tr = g['it'][-1]
    for k, v in kw.items():
        tr[k] = v if isinstance(v, dict) else static(v)
    return g


def find(g, nm):
    """First descendant group named nm."""
    for it in g.get('it', []):
        if it.get('nm') == nm and it.get('ty') == 'gr':
            return it
        if it.get('ty') == 'gr':
            r = find(it, nm)
            if r:
                return r
    return None


# ------------------------------------------------------------------ Nekko
def _area(vs):
    return sum(vs[i][0] * vs[(i + 1) % len(vs)][1] - vs[(i + 1) % len(vs)][0] * vs[i][1] for i in range(len(vs))) / 2


def extend_body(groups, scale, depth=330):
    """Grow the white body and its cobalt outline straight down below the bust cut, so Nekko can lift off the frame
    edge (overshoot, hop) without the flat cut floating. Rects sit under the existing paths, same winding."""
    runs = {'outline': (130.0, 782.0), 'silhouette': (166.0, 748.0)}
    for key, (x0, x1) in runs.items():
        g = groups[key]
        main = g['it'][0]['ks']['k']['v']
        pts = [(x0, 852), (x1, 852), (x1, 860 + depth), (x0, 860 + depth)]
        if (_area(pts) > 0) != (_area(main) > 0):
            pts = pts[::-1]
        pts = [(x * scale, y * scale) for x, y in pts]
        g['it'].insert(0, path_item(poly(pts), 'body extension'))


def nekko(face='happy', scale=0.45, extend=True, outer_whiskers=None, outline=None):
    g = nl.nekko_groups(DATA, face, scale, outline=outline)
    if outer_whiskers is not None:  # whisker strokes outside the body take this colour
        for s in ('l', 'r1', 'r2'):
            out = find(g['whiskers'], f'whisker-{s}-out')
            out['it'][-2]['c'] = static(list(outer_whiskers))
    if extend:
        extend_body(g, scale)
    return g


def nekko_layer(g, scale, pos, name='Nekko', ind=1, op=60, scale_prop=None, rotation=0):
    return nl.layer(nl.top_first(g), name=name, position=pos, anchor=nl.bottom_center(DATA, scale),
                    scale=scale_prop, rotation=rotation, ind=ind, op=op)


def pivot_of(part, which):
    """Inner end of a whisker: the vertex of its in-part with min x ('min') or max x ('max')."""
    vs = DATA['parts'][part]['paths'][0]['v']
    return min(vs, key=lambda v: v[0]) if which == 'min' else max(vs, key=lambda v: v[0])


def whisker_flick(g, scale, t0, amp=1.0):
    """Each whisker swings about its root: viewer-left one outward-up first, right ones follow 2 frames apart."""
    spec = {'l': ('whisker-l-in', 'max', 0, +1), 'r1': ('whisker-r1-in', 'min', 2, -1), 'r2': ('whisker-r2-in', 'min', 4, -1)}
    for s, (part, which, delay, sign) in spec.items():
        pv = pivot_of(part, which)
        pv = [pv[0] * scale, pv[1] * scale]
        a = sign * 9 * amp
        t = t0 + delay
        gr = find(g['whiskers'], f'whisker-{s}')
        set_tr(gr, a=pv, p=pv, r=kf([(t, 0), (t + 3, a, OUT_CUBIC), (t + 7, -a * 0.45, IN_OUT_SOFT),
                                     (t + 11, a * 0.18, IN_OUT_SOFT), (t + 15, 0)], IN_OUT_SOFT))


def blink(g, scale, t0, close=3, hold=2, open_=4, lift=11.0):
    """Happy arc eyes squeeze flat (shape morph of the stroke path) and reopen."""
    for it in g['eyes']['it']:
        if it.get('ty') != 'gr':
            continue
        sh = it['it'][0]
        arc = sh['ks']['k']
        flat = {'i': [[0, 0], [0, 0]], 'o': [[0, 0], [0, 0]],
                'v': [[v[0], v[1] - lift * scale] for v in arc['v']], 'c': False}
        sh['ks'] = kf([(t0, arc, IN_OUT_SOFT), (t0 + close, flat, LINEAR), (t0 + close + hold, flat, IN_OUT_SOFT),
                       (t0 + close + hold + open_, arc)])


# ------------------------------------------------------------------ layers and files
def blank_layer(shapes, name, ind, op, pos=(0, 0), opacity=100):
    return nl.layer(shapes, name=name, position=pos, anchor=(0, 0), ind=ind, op=op, opacity=opacity)


def file(layers, w, h, frames, name):
    return {'v': '5.7.0', 'fr': 30, 'ip': 0, 'op': frames, 'w': w, 'h': h, 'nm': name, 'ddd': 0, 'assets': [],
            'layers': layers}


def star4(r, nm='sparkle'):
    """Four-point sparkle with concave sides; a same-colour stroke rounds the tips (track R)."""
    tips = [(0, -r), (r, 0), (0, r), (-r, 0)]
    # both handles of every tip point at the centre, so each side bows inward
    o = [[-t[0] * 0.72, -t[1] * 0.72] for t in tips]
    i = [[-t[0] * 0.72, -t[1] * 0.72] for t in tips]
    return {'i': i, 'o': o, 'v': [list(t) for t in tips], 'c': True}
