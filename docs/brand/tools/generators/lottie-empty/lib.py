"""Small Lottie toolkit on top of nekko_lottie: shapes, path builder, Hermite-keyed periodic motion, layers."""
import math
import os
import sys

REPO = r'c:\Users\minha\Desktop\TechTsunagu'
sys.path.insert(0, os.path.join(REPO, 'docs', 'brand', 'tools'))
import nekko_lottie as nl  # noqa: E402

L = 90  # frames per loop (3 s at 30 fps)
CO, WH = nl.COBALT, nl.WHITE
JET = [0.0784314, 0.0784314, 0.0784314, 1]
DANGER = [0.7019608, 0.1490196, 0.1176471, 1]
WARN = [0.5411765, 0.3254902, 0, 1]
TAU = 2 * math.pi
static = nl.static


def sm(x):
    x = min(1.0, max(0.0, x))
    return x * x * (3 - 2 * x)


def lst(v):
    return list(v) if isinstance(v, (list, tuple)) else [v]


# ---------------------------------------------------------------- keyed motion
def anim(fn, t0=0, t1=L, step=6):
    """Property from fn(t) (number or list) sampled with C1-continuous Hermite easing: exact for smooth periodic motion."""
    ts = sorted(set(list(range(t0, t1, step)) + [t1]))
    h = 0.02

    def val(t):
        return lst(fn(t))

    def der(t):
        a, b = (t, t + h) if t == t0 else ((t - h, t) if t == t1 else (t - h, t + h))
        return [(y - x) / (b - a) for x, y in zip(val(a), val(b))]

    for _ in range(4):  # split segments where the end values match but the curve bulges (peak between two keys)
        add = []
        for a, b in zip(ts, ts[1:]):
            va, vb, da, db = val(a), val(b), der(a), der(b)
            if any(abs(y - x) < 0.02 * abs((p + q) * (b - a) / 2) for x, y, p, q in zip(va, vb, da, db)):
                add.append((a + b) / 2)
        if not add:
            break
        ts = sorted(set(ts + add))
    V = [val(t) for t in ts]
    if all(max(abs(a - b) for a, b in zip(v, V[0])) < 1e-6 for v in V):
        return static(V[0][0] if len(V[0]) == 1 else V[0])
    D = [der(t) for t in ts]
    keys = []
    for k in range(len(ts) - 1):
        dt = ts[k + 1] - ts[k]
        o, i = {'x': [], 'y': []}, {'x': [], 'y': []}
        for j in range(len(V[0])):
            dv = V[k + 1][j] - V[k][j]
            y1, y2 = (1 / 3, 2 / 3) if abs(dv) < 1e-5 else (D[k][j] * dt / 3 / dv, 1 - D[k + 1][j] * dt / 3 / dv)
            o['x'].append(1 / 3), o['y'].append(y1), i['x'].append(2 / 3), i['y'].append(y2)
        keys.append({'t': ts[k], 's': V[k], 'o': o, 'i': i})
    keys.append({'t': ts[-1], 's': V[-1]})
    return {'a': 1, 'k': keys}


def cyc(f, C=L, phase=0):
    """f(u), u in [0,1] the age inside a cycle of C frames that starts at frame `phase`. Returns [(t0, t1, fn)] windows
    that never wrap inside (a phase > 0 gives two), so each window is keyed on its own layer: no jump at the loop seam."""
    cut = phase % C
    if not cut:
        return [(0, L, lambda t: f(t / C))]
    return [(cut, L, lambda t: f((t - cut) / C)), (0, cut, lambda t: f((t - cut + C) / C))]


# ---------------------------------------------------------------- shape items
def P(x):
    return x if isinstance(x, dict) and 'a' in x else static(x)


def tr(p=(0, 0), a=(0, 0), s=(100, 100), r=0, o=100):
    return {'ty': 'tr', 'nm': 'Transform', 'p': P(list(p) if not isinstance(p, dict) else p),
            'a': P(list(a) if not isinstance(a, dict) else a), 's': P(list(s) if not isinstance(s, dict) else s),
            'r': P(r), 'o': P(o), 'sk': static(0), 'sa': static(0)}


def grp(name, items, **k):
    return {'ty': 'gr', 'nm': name, 'it': list(items) + [tr(**k)]}


def fill(c, o=100):
    return {'ty': 'fl', 'nm': 'fill', 'c': static(list(c)), 'o': P(o), 'r': 1}


def stroke(c, w, o=100):
    return {'ty': 'st', 'nm': 'stroke', 'c': static(list(c)), 'o': P(o), 'w': P(w), 'lc': 2, 'lj': 2, 'ml': 4}


def rect(w, h, r, x=0, y=0):
    return {'ty': 'rc', 'nm': 'rect', 'p': static([x, y]), 's': static([w, h]), 'r': static(r)}


def disc(r, x=0, y=0):
    return {'ty': 'el', 'nm': 'ellipse', 'p': static([x, y]), 's': static([2 * r, 2 * r])}


class Path:
    """Cubic path builder: move, line, arc (angles in degrees, y down, so -90 is up), build."""

    def __init__(self):
        self.v, self.i, self.o = [], [], []

    def _add(self, x, y, i=(0, 0), o=(0, 0)):
        self.v.append([x, y]), self.i.append(list(i)), self.o.append(list(o))

    def move(self, x, y):
        self._add(x, y)
        return self

    def line(self, x, y):
        self._add(x, y)
        return self

    def arc(self, cx, cy, r, a0, a1):
        a0, a1 = math.radians(a0), math.radians(a1)
        if not self.v:
            self._add(cx + r * math.cos(a0), cy + r * math.sin(a0))
        n = max(1, math.ceil(abs(a1 - a0) / (math.pi / 2) - 1e-9))
        d = (a1 - a0) / n
        k = 4 / 3 * math.tan(d / 4) * r
        for m in range(n):
            t0, t1 = a0 + m * d, a0 + (m + 1) * d
            self.o[-1] = [-k * math.sin(t0), k * math.cos(t0)]
            self._add(cx + r * math.cos(t1), cy + r * math.sin(t1), i=(k * math.sin(t1), -k * math.cos(t1)))
        return self

    def curve(self, o, i, x, y):
        """Cubic to (x, y); o = out-tangent at the current vertex, i = in-tangent at the new one (both relative)."""
        self.o[-1] = list(o)
        self._add(x, y, i=i)
        return self

    def item(self, closed=False):
        v, i, o = [list(p) for p in self.v], [list(p) for p in self.i], [list(p) for p in self.o]
        if closed and len(v) > 1 and math.dist(v[0], v[-1]) < 1e-6:
            i[0] = i[-1]
            v, i, o = v[:-1], i[:-1], o[:-1]
        return {'ty': 'sh', 'nm': 'path', 'ks': static({'i': i, 'o': o, 'v': v, 'c': closed})}


def poly(pts, closed=False):
    p = Path()
    for n, (x, y) in enumerate(pts):
        p.move(x, y) if n == 0 else p.line(x, y)
    return p.item(closed)


# ---------------------------------------------------------------- layers and file
_ind = [0]


def layer(name, shapes, p=(0, 0), a=(0, 0), s=None, r=0, o=100, ip=0, op=L):
    _ind[0] += 1
    pp = p if isinstance(p, dict) else static([p[0], p[1], 0])
    aa = static([a[0], a[1], 0])
    return {'ddd': 0, 'ind': _ind[0], 'ty': 4, 'nm': name, 'sr': 1, 'ao': 0, 'ip': ip, 'op': op, 'st': 0, 'bm': 0,
            'ks': {'o': P(o), 'r': P(r), 'p': pp, 'a': aa, 's': s if isinstance(s, dict) else static(s or [100, 100, 100])},
            'shapes': shapes}


def cycle_layers(name, shapes, f, C=L, phase=0, step=5):
    """One layer per non-wrapping window of the cycle. f(u) -> dict(p=(x,y), s=pct, r=deg, o=pct)."""
    out = []
    for a, b, fn in cyc(f, C, phase):
        out.append(layer(name, shapes,
                         p=anim(lambda t: [*fn(t)['p'], 0], a, b, step), a=(0, 0),
                         s=anim(lambda t: [fn(t)['s'], fn(t)['s'], 100], a, b, step),
                         r=anim(lambda t: fn(t)['r'], a, b, step), o=anim(lambda t: fn(t)['o'], a, b, step),
                         ip=a, op=b))
    return out


def file(name, layers, frames=L):
    return {'v': '5.7.0', 'fr': 30, 'ip': 0, 'op': frames, 'w': 512, 'h': 512, 'nm': name, 'ddd': 0, 'assets': [],
            'layers': layers}


# ---------------------------------------------------------------- Nekko helpers
def part_center(paths):
    xs = [p[0] for pa in paths for p in pa['v']]
    ys = [p[1] for pa in paths for p in pa['v']]
    return [(min(xs) + max(xs)) / 2, (min(ys) + max(ys)) / 2]


def nekko(data, face, s, x, yb, breath=None, sway=None, blink=None, gaze=None, extra=None, name='Nekko'):
    """Nekko layer. breath(t) -> scale pct about the bust bottom, sway(t) -> degrees, blink = frame of one blink,
    gaze(t) -> [dx, dy] px for the eyes group, extra = shapes (in Nekko units, scaled by s) drawn above Nekko."""
    g = nl.nekko_groups(data, face, s)
    if blink is not None:
        src = (data['expressions'][face]['eyes'] if face != 'neutral'
               else [{'name': n, 'paths': data['parts'][n]['paths']} for n in ('eye-l', 'eye-r')])
        for child in g['eyes']['it'][:-1]:
            if child.get('nm') in ('eye-l', 'eye-r'):
                it = next(e for e in src if e['name'] == child['nm'])
                c = [v * s for v in part_center(it['paths'])]
                t = child['it'][-1]
                t['a'], t['p'] = static(c), static(c)
                t['s'] = nl.kf([(0, [100, 100]), (blink, [100, 100]), (blink + 3, [100, 6]), (blink + 7, [100, 100]),
                                (L, [100, 100])], ease=nl.EASE_IN_OUT)
    if gaze:
        g['eyes']['it'][-1]['p'] = anim(gaze, 0, L, 6)
    shapes = nl.top_first(g)
    if extra:
        shapes = extra + shapes
    sc = anim(lambda t: [100 - 0.3 * (breath(t) - 100), breath(t), 100], 0, L, 6) if breath else None
    return layer(name, shapes, p=(x, yb), a=nl.bottom_center(data, s), s=sc,
                 r=anim(sway, 0, L, 6) if sway else 0)
