"""Small Lottie toolkit for the flow animations (builds on docs/brand/tools/nekko_lottie.py)."""
import json
import math
import os
import sys

REPO = r'C:\Users\minha\Desktop\TechTsunagu'
sys.path.insert(0, os.path.join(REPO, 'docs', 'brand', 'tools'))
import nekko_lottie as nl  # noqa: E402

COBALT = nl.COBALT
WHITE = [1, 1, 1, 1]


def hexf(h):
    h = h.lstrip('#')
    return [round(int(h[i:i + 2], 16) / 255, 7) for i in (0, 2, 4)] + [1]


JET = hexf('#141414')
SUCCESS = hexf('#1E6B3A')
DANGER = hexf('#B3261E')
WARNING = hexf('#8A5300')

EASE = {
    'out': ((0.22, 1.0), (0.36, 1.0)),
    'in': ((0.64, 0.0), (0.78, 0.0)),
    'io': ((0.45, 0.0), (0.55, 1.0)),
    'lin': ((0.0, 0.0), (1.0, 1.0)),
}


def K(keys):
    """keys: [(frame, value, ease)]; ease in EASE, a handle pair, or 'hold'. The ease belongs to the segment that
    starts at that key (the last key needs none). Value: number or list."""
    out = []
    for n, key in enumerate(keys):
        t, v = key[0], key[1]
        e = key[2] if len(key) > 2 else 'out'
        v = list(v) if isinstance(v, (list, tuple)) else [v]
        d = {'t': t, 's': v}
        if n < len(keys) - 1:
            if e == 'hold':
                d['h'] = 1
            else:
                (ox, oy), (ix, iy) = EASE[e] if isinstance(e, str) else e
                d['o'] = {'x': [ox] * len(v), 'y': [oy] * len(v)}
                d['i'] = {'x': [ix] * len(v), 'y': [iy] * len(v)}
        out.append(d)
    return {'a': 1, 'k': out}


def Kp(keys):
    """Layer position keys (frame, (x, y), ease) -> 3-value keys."""
    return K([(k[0], [k[1][0], k[1][1], 0]) + tuple(k[2:]) for k in keys])


def Ks(keys):
    """Layer scale keys (frame, scalar | (sx, sy), ease) -> 3-value keys."""
    out = []
    for k in keys:
        v = k[1]
        sx, sy = (v, v) if not isinstance(v, (list, tuple)) else v
        out.append((k[0], [sx, sy, 100]) + tuple(k[2:]))
    return K(out)


def P(v):
    if isinstance(v, dict):
        return v
    if isinstance(v, tuple):
        v = list(v)
    return nl.static(v)


# ------------------------------------------------------------------ shapes
def rc(w, h, r, p=(0, 0)):
    return {'ty': 'rc', 'nm': 'rect', 'd': 1, 'p': P(p), 's': P([w, h]), 'r': P(r)}


def el(w, h=None, p=(0, 0)):
    return {'ty': 'el', 'nm': 'ellipse', 'd': 1, 'p': P(p), 's': P(w if isinstance(w, dict) else [w, h or w])}


def sh(v, closed=False, i=None, o=None, nm='path'):
    n = len(v)
    return {'ty': 'sh', 'nm': nm, 'ks': nl.static({
        'i': [list(x) for x in (i or [(0, 0)] * n)], 'o': [list(x) for x in (o or [(0, 0)] * n)],
        'v': [list(x) for x in v], 'c': closed})}


def line(a, b):
    return sh([a, b])


def fl(color, o=100):
    return {'ty': 'fl', 'nm': 'fill', 'c': P(color), 'o': P(o), 'r': 1}


def st(color, w, o=100):
    return {'ty': 'st', 'nm': 'stroke', 'c': P(color), 'o': P(o), 'w': P(w), 'lc': 2, 'lj': 2, 'ml': 4}


def tm(s=0, e=100, o=0):
    return {'ty': 'tm', 'nm': 'trim', 's': P(s), 'e': P(e), 'o': P(o), 'm': 1}


def tr(p=(0, 0), a=(0, 0), s=(100, 100), r=0, o=100):
    return {'ty': 'tr', 'nm': 'Transform', 'p': P(p), 'a': P(a), 's': P(s), 'r': P(r), 'o': P(o),
            'sk': nl.static(0), 'sa': nl.static(0)}


def grp(name, items, **kw):
    """Group: items in draw order (first = on top), then the transform. Order inside: shapes, trim, paints."""
    return {'ty': 'gr', 'nm': name, 'it': list(items) + [tr(**kw)]}


# ------------------------------------------------------------------ layers
def lay(name, shapes, p=(256, 256), a=(0, 0), s=None, r=0, o=100, ip=0):
    pos = p if isinstance(p, dict) else nl.static([p[0], p[1], 0])
    anc = a if isinstance(a, dict) else nl.static([a[0], a[1], 0])
    return {'ddd': 0, 'ind': 0, 'ty': 4, 'nm': name, 'sr': 1, 'ao': 0, 'ip': ip, 'op': 0, 'st': 0, 'bm': 0,
            'ks': {'o': P(o), 'r': P(r), 'p': pos, 'a': anc, 's': s if isinstance(s, dict) else nl.static(s or [100, 100, 100])},
            'shapes': shapes}


def build(name, layers, op, path):
    """layers are top-first (first drawn on top). Sets ind and op, writes the file with nl.save."""
    for n, l in enumerate(layers):
        l['ind'] = n + 1
        l['op'] = op
    anim = {'v': '5.7.0', 'fr': 30, 'ip': 0, 'op': op, 'w': 512, 'h': 512, 'nm': name, 'ddd': 0, 'assets': [],
            'layers': layers}
    nl.save(anim, path)
    return anim


# ------------------------------------------------------------------ Nekko
def nekko_shapes(data, s, faces, swaps=()):
    """One Nekko with several faces: eyes/mouth groups per face, swapped by hold opacity keys at frames `swaps`
    (len(swaps) == len(faces) - 1). Returns (shapes top-first, {'eyes-<face>': group, 'mouth-<face>': group})."""
    base = nl.nekko_groups(data, faces[0], s)
    seq = [base[k] for k in ('outline', 'silhouette', 'arm-line', 'hand', 'brow')]
    eyes, mouths, named = [], [], {}
    for n, f in enumerate(faces):
        g = base if n == 0 else nl.nekko_groups(data, f, s)
        e, m = g['eyes'], g['mouth']
        e['nm'], m['nm'] = 'eyes-' + f, 'mouth-' + f
        if len(faces) > 1:
            keys = []
            if n > 0:
                keys += [(0, 0, 'hold'), (swaps[n - 1], 100, 'hold')]
            else:
                keys += [(0, 100, 'hold')]
            if n < len(faces) - 1:
                keys += [(swaps[n], 0, 'hold')]
            if len(keys) > 1:
                # the last key of a keyframe list takes no ease; make sure the final key exists once
                keys = _dedupe(keys)
                e['it'][-1]['o'] = K(keys)
                m['it'][-1]['o'] = K(keys)
        eyes.append(e)
        mouths.append(m)
        named['eyes-' + f], named['mouth-' + f] = e, m
    order = seq + eyes + [base['nose']] + mouths + [base['whiskers']]
    return list(reversed(order)), named


def _dedupe(keys):
    out = []
    for k in keys:
        if out and out[-1][0] == k[0]:
            out[-1] = k
        else:
            out.append(k)
    return out


def blink(group, data, s, keys):
    """Blink on an eyes group: scale y about the eye midpoint (anchor == position, no jump)."""
    a = data['anchors']
    mid = [(a['eye_l'][0] + a['eye_r'][0]) / 2 * s, (a['eye_l'][1] + a['eye_r'][1]) / 2 * s]
    t = group['it'][-1]
    t['a'], t['p'] = nl.static(mid), nl.static(list(mid))
    t['s'] = K(keys)


def sample_ease(t, t0, t1, v0, v1):
    u = min(1.0, max(0.0, (t - t0) / (t1 - t0)))
    u = 0.5 - 0.5 * math.cos(math.pi * u)
    return v0 + (v1 - v0) * u
