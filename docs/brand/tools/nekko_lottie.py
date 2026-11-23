"""Nekko as Lottie shape groups, built from frontend/public/brand/master/nekko-lottie-shapes.json.

Usage (repo root as working directory):

    import sys, json
    sys.path.insert(0, 'docs/brand/tools')
    import nekko_lottie as nl

    data = nl.load()                                   # the shapes JSON (anchors, parts, expressions)
    a = data['anchors']                                # eye_l, eye_r, mouth_center, hand_center, weight_face, ...

    # 1. one finished animation: Nekko pops in with an overshoot (30 fps, 512x512 unless told otherwise)
    anim = nl.animation(face='happy', scale_keys=nl.pop_keys(0, 14))
    nl.save(anim, 'out.json')                          # compact, rounded; check with docs/brand/tools/lottie-check.cjs

    # 2. build your own layer: the shape groups are plain dicts you can edit and animate
    groups = nl.nekko_groups(data, face='worried', scale=0.45)   # {'outline': gr, 'silhouette': gr, ..., 'whiskers': gr}
    groups['eyes']['it'][-1]['p'] = nl.kf([(0, [0, 0]), (10, [0, 12])])   # every group ends in a 'tr' item: animate it
    layer = nl.layer(nl.top_first(groups), name='Nekko', position=[256, 512], anchor=nl.bottom_center(data, 0.45))

Rules this module follows (SPEC section 7): shape layers only, fills and strokes, static or keyframed transforms, no
expressions, no effects. Colours default to the cobalt/white float triples; pass your own to recolour (only the
state colours from the SPEC palette are allowed).

Coordinates. The JSON uses the nekko-standalone.svg coordinate system (zero origin, y down, bust cut at y = height).
`scale` multiplies vertices, tangents and stroke widths (baked in, so layer scale stays free to animate); `offset`
shifts vertices. Nekko's drawing is `data['width']` x `data['height']` units at scale 1.

Draw order. The SVG order is bottom to top: outline, silhouette, arm-line, hand, brow, eyes, nose, mouth, whiskers.
Lottie draws the FIRST item of a `shapes` / `it` list on top, so `top_first()` reverses a groups dict for you; use it
when you put the groups into a layer.

Faces. face = neutral | happy | worried | surprised | sleepy | error. 'neutral' is the traced original; the others
replace the eyes and mouth groups (strokes use round caps and joins). Left and right are the VIEWER's left and right.

Nose. It has a hole (its white island), so its fill uses the even-odd rule (Lottie "r": 2); the helper sets it.
"""
import json
import os

HERE = os.path.dirname(os.path.abspath(__file__))
SHAPES = os.path.join(HERE, '..', '..', '..', 'frontend', 'public', 'brand', 'master', 'nekko-lottie-shapes.json')
COBALT = [0.0745098, 0.3176471, 0.6666667, 1]
WHITE = [1, 1, 1, 1]
ORDER = ['outline', 'silhouette', 'arm-line', 'hand', 'brow', 'eyes', 'nose', 'mouth', 'whiskers']
EASE_OUT = ((0.2, 0.0), (0.4, 1.0))      # (out-handle, in-handle) of a keyframe segment: fast start, soft landing
EASE_IN_OUT = ((0.45, 0.0), (0.55, 1.0))
EASE_IN = ((0.5, 0.0), (0.8, 0.5))


def load(path=None):
    with open(path or SHAPES, encoding='utf8') as f:
        return json.load(f)


# ------------------------------------------------------------------ property helpers
def static(v):
    return {'a': 0, 'k': v}


def kf(keys, ease=EASE_OUT):
    """Animated property from [(frame, value[, ease]), ...]; value is a number or a list. The ease of a key shapes
    the segment that STARTS at that key; the last key needs none."""
    out = []
    for n, key in enumerate(keys):
        t, v = key[0], key[1]
        v = list(v) if isinstance(v, (list, tuple)) else [v]
        d = {'t': t, 's': v}
        if n < len(keys) - 1:
            (ox, oy), (ix, iy) = key[2] if len(key) > 2 else ease
            d['o'] = {'x': [ox] * len(v), 'y': [oy] * len(v)}
            d['i'] = {'x': [ix] * len(v), 'y': [iy] * len(v)}
        out.append(d)
    return {'a': 1, 'k': out}


def pop_keys(t0=0, dur=14, peak=112, end=100):
    """Scale keys for a pop-in with overshoot: 0 -> peak (ease out) -> end (ease in-out). Percent, 3 dims."""
    s = lambda p: [p, p, 100]
    mid = t0 + round(dur * 0.62)
    return [(t0, s(0), EASE_OUT), (mid, s(peak), EASE_IN_OUT), (t0 + dur, s(end))]


# ------------------------------------------------------------------ shape groups
def _tr(pos=(0, 0), anchor=(0, 0), scale=100, rot=0, opacity=100):
    return {'ty': 'tr', 'p': static(list(pos)), 'a': static(list(anchor)), 's': static([scale, scale]),
            'r': static(rot), 'o': static(opacity), 'sk': static(0), 'sa': static(0), 'nm': 'Transform'}


def _path(p, scale, offset, closed=None):
    sc = lambda pts, off: [[x * scale + off[0], y * scale + off[1]] for x, y in pts]
    return {'ty': 'sh', 'nm': 'path', 'ks': static({
        'i': sc(p['i'], (0, 0)), 'o': sc(p['o'], (0, 0)), 'v': sc(p['v'], offset),
        'c': p['c'] if closed is None else closed})}


def _fill(color, rule):
    return {'ty': 'fl', 'nm': 'fill', 'c': static(list(color)), 'o': static(100), 'r': 2 if rule == 'evenodd' else 1}


def _stroke(color, width, scale):
    return {'ty': 'st', 'nm': 'stroke', 'c': static(list(color)), 'o': static(100), 'w': static(width * scale),
            'lc': 2, 'lj': 2, 'ml': 4}


def _col(d, cobalt, white):
    return white if d.get('color') == 'white' else cobalt


def part_group(name, part, scale=1.0, offset=(0, 0), cobalt=COBALT, white=WHITE):
    """Lottie group for one entry of data['parts'] (a filled shape; evenodd when it has a hole)."""
    paths = [_path(p, scale, offset) for p in part['paths']]
    return {'ty': 'gr', 'nm': name, 'it': paths + [_fill(_col(part, cobalt, white), part['fillRule']), _tr()]}


def item_group(it, scale=1.0, offset=(0, 0), cobalt=COBALT, white=WHITE):
    """Lottie group for one expression item (kind 'fill' or 'stroke')."""
    paths = [_path(p, scale, offset) for p in it['paths']]
    col = _col(it, cobalt, white)
    paint = _stroke(col, it['width'], scale) if it['kind'] == 'stroke' else _fill(col, it['fillRule'])
    return {'ty': 'gr', 'nm': it['name'], 'it': paths + [paint, _tr()]}


def _wrap(name, children):
    """Group of groups. Children arrive bottom-to-top; Lottie wants top first."""
    return {'ty': 'gr', 'nm': name, 'it': list(reversed(children)) + [_tr()]}


def nekko_groups(data, face='neutral', scale=1.0, offset=(0, 0), cobalt=COBALT, white=WHITE, outline=None):
    """{'outline': gr, ..., 'whiskers': gr} in SVG order (bottom to top). Every group ends with a 'tr' item
    (position, anchor, scale, rotation, opacity) you can animate. `outline` overrides the outline colour."""
    P = data['parts']
    pg = lambda k, nm=None: part_group(nm or k, P[k], scale, offset, cobalt, white)
    g = {'outline': part_group('outline', P['outline'], scale, offset, outline or cobalt, white),
         'silhouette': pg('silhouette'), 'arm-line': pg('arm-line'), 'hand': pg('hand'), 'brow': pg('brow'),
         'nose': pg('nose')}
    if face == 'neutral':
        g['eyes'] = _wrap('eyes', [pg('eye-l'), pg('eye-r')])
        g['mouth'] = _wrap('mouth', [pg('mouth')])
    else:
        e = data['expressions'][face]
        ig = lambda it: item_group(it, scale, offset, cobalt, white)
        g['eyes'] = _wrap('eyes', [ig(i) for i in e['eyes']])
        g['mouth'] = _wrap('mouth', [ig(i) for i in e['mouth']])
    g['whiskers'] = _wrap('whiskers', [
        _wrap(f'whisker-{s}', [pg(f'whisker-{s}-in'), pg(f'whisker-{s}-out')]) for s in ('l', 'r1', 'r2')])
    return {k: g[k] for k in ORDER}


def top_first(groups):
    """Groups dict (bottom-to-top) -> list for a layer's `shapes` (top item first)."""
    return [groups[k] for k in reversed(ORDER) if k in groups]


def bottom_center(data, scale=1.0):
    """Anchor point at the bust's bottom edge, centre: the natural pivot for pop-in and scale animations."""
    return [data['width'] * scale / 2, data['height'] * scale]


def _round(o, nd):
    if isinstance(o, float):
        return round(o, nd)
    if isinstance(o, list):
        return [_round(x, nd) for x in o]
    if isinstance(o, dict):  # 'c' holds colours (the cobalt triple must stay exact) or a path's closed flag
        return {k: (v if k == 'c' else _round(v, nd)) for k, v in o.items()}
    return o


def save(anim, path, nd=2):
    """Write compact JSON with floats rounded to nd decimals (keeps a Nekko file near 30 KB; Lottie cap is 60 KB)."""
    with open(path, 'w', encoding='utf8') as f:
        json.dump(_round(anim, nd), f, separators=(',', ':'))


# ------------------------------------------------------------------ layer and file
def layer(shapes, name='Nekko', position=(256, 512), anchor=(0, 0), scale=None, rotation=0, opacity=100,
          ind=1, ip=0, op=60):
    """Shape layer. scale is a [x, y, 100] percent list, a kf(...) property, or None (100%)."""
    scale = scale if isinstance(scale, dict) else static(scale or [100, 100, 100])
    return {'ddd': 0, 'ind': ind, 'ty': 4, 'nm': name, 'sr': 1, 'ao': 0, 'ip': ip, 'op': op, 'st': 0, 'bm': 0,
            'ks': {'o': static(opacity), 'r': static(rotation), 'p': static([position[0], position[1], 0]),
                   'a': static([anchor[0], anchor[1], 0]), 's': scale},
            'shapes': shapes}


def animation(data=None, face='neutral', w=512, h=512, fr=30, frames=60, scale_keys=None, fill=0.86,
              name='nekko', cobalt=COBALT, white=WHITE):
    """Minimal full Lottie file: Nekko fitted into the canvas (fill = fraction of the canvas it may occupy), bottom
    edge on the canvas bottom, centred. scale_keys = pop_keys(...) or any list for kf(); None = static."""
    data = data or load()
    s = fill * min(w / data['width'], h / data['height'])
    groups = nekko_groups(data, face, s, cobalt=cobalt, white=white)
    sk = kf(scale_keys) if scale_keys else None
    lay = layer(top_first(groups), name='Nekko', position=[w / 2, h], anchor=bottom_center(data, s),
                scale=sk, op=frames)
    return {'v': '5.7.0', 'fr': fr, 'ip': 0, 'op': frames, 'w': w, 'h': h, 'nm': name, 'ddd': 0, 'assets': [],
            'layers': [lay]}
