"""Nekko master trace: docs/brand/source/nekko-original.jpg -> frontend/public/brand/master/*.

Run (repo root as working directory):

    $s = "C:\\Users\\minha\\AppData\\Local\\Temp\\claude\\e6e9e47624cef00c\\6b9adb8f-cbed-4977-8a20-a1870f863912\\scratchpad"
    & "$s\\brand-tools\\venv\\Scripts\\python.exe" docs\\brand\\tools\\trace.py

Needs numpy, pillow, opencv-python-headless, potracer (import potrace). Writes only inside
frontend/public/brand/master/ and (debug) $s\\work\\foundation\\. Stages:

  1. crop the icon (blue-pixel bbox), resample to a 2048 raster (2x the 1024 canvas), binary white/blue mask
  2. split: silhouette S, face/hand strokes (eyes, brow, nose, mouth, hand, arm-line), whiskers
  3. potrace every part (smooth curves), emit SVG paths in 1024 units
  4. write the SVG set, expressions, face mark, Lottie shapes, MANIFEST numbers
"""
import json
import math
import os
import re
import subprocess
import time
import xml.etree.ElementTree as ET

import cv2
import numpy as np
import potrace

HERE = os.path.dirname(os.path.abspath(__file__))
ROOT = os.path.abspath(os.path.join(HERE, '..', '..', '..'))
SRC = os.path.join(ROOT, 'docs', 'brand', 'source', 'nekko-original.jpg')
OUT = os.path.join(ROOT, 'frontend', 'public', 'brand', 'master')
SCRATCH = os.environ.get(
    'BRAND_SCRATCH',
    r'C:\Users\minha\AppData\Local\Temp\claude\e6e9e47624cef00c\6b9adb8f-cbed-4977-8a20-a1870f863912\scratchpad',
)
DBG = os.path.join(SCRATCH, 'work', 'foundation')

EXT_MAX = 0  # longest straight-down body extension in K px (set by load_mask)
K = 2048  # working raster; the master canvas is 1024 units, so 1 unit = 2 px
CANVAS = 1024


# ---------------------------------------------------------------- stage 1: mask
def load_mask():
    """Return (white, src_crop_1024). white: bool KxK, True = white (cat + whiskers), False = blue.

    Box = blue pixels (R<150, B>150) in x/y plus the cat's white down to the icon's bottom edge (the
    blue hull ends a few px above it because the bottom corners are rounded). Not within exactly square
    (1857x1847): the 0.5% difference is absorbed by the resample. The squircle is convex, so its mask is
    the convex hull of icon pixels; hull edge pixels mix with the grey backdrop, so the hull is eroded and
    that band counts as blue. Columns whose last pixel inside the hull is white are extended straight down
    to the canvas bottom (the rounded corners cut the body short there).
    """
    im = cv2.imread(SRC)
    rgb = im[:, :, ::-1].astype(np.float32)
    R, G, B = rgb[..., 0], rgb[..., 1], rgb[..., 2]
    blue = (R < 150) & (B > 150)
    cw = ((R >= 248) & (G >= 248) & (B >= 248)).astype(np.uint8)
    n, lab, st, _ = cv2.connectedComponentsWithStats(cw, connectivity=4)
    h, w = cw.shape
    cat = np.zeros_like(cw)
    for i in range(1, n):  # big white blobs not touching the image border = the cat (not the backdrop)
        x, y, bw, bh, a = st[i]
        if a > 20000 and x > 0 and y > 0 and x + bw < w and y + bh < h:
            cat |= (lab == i)
    by, bx = np.where(blue)
    cy, cx = np.where(cat > 0)
    x0, x1 = bx.min(), bx.max() + 1
    y0, y1 = by.min(), cy.max() + 1
    pts = np.concatenate([np.stack([bx, by], 1), np.stack([cx, cy], 1)]).astype(np.int32)
    hull = np.zeros((h, w), np.uint8)
    cv2.fillConvexPoly(hull, cv2.convexHull(pts), 1)

    def to_k(a):
        return cv2.resize(a, (K, K), interpolation=cv2.INTER_CUBIC)

    crop = im[y0:y1, x0:x1]
    lum = cv2.cvtColor(crop, cv2.COLOR_BGR2GRAY).astype(np.float32)
    lum = cv2.GaussianBlur(to_k(lum), (0, 0), 1.3)
    hull_k = cv2.resize(hull[y0:y1, x0:x1], (K, K), interpolation=cv2.INTER_NEAREST)
    hull_k = cv2.erode(hull_k, cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (11, 11)))
    blue_lum = float(np.median(lum[(hull_k > 0)][lum[(hull_k > 0)] < 140]))
    thr = (blue_lum + 255.0) / 2.0
    white = (lum > thr) & (hull_k > 0)
    # extend the body straight down where the rounded corner cut it
    last = K - 1 - np.argmax(hull_k[::-1, :] > 0, axis=0)  # last hull row per column
    global EXT_MAX
    EXT_MAX = 0
    for x in range(K):
        if hull_k[:, x].any() and white[last[x], x]:
            EXT_MAX = max(EXT_MAX, K - 1 - last[x])
            white[last[x]:, x] = True
    # speck removal (JPEG noise): small white and small blue islands
    white = drop_small(white, 60)
    white = ~drop_small(~white, 60)
    return white, cv2.resize(crop, (CANVAS, CANVAS), interpolation=cv2.INTER_AREA), thr, hull_k


def drop_small(mask, min_area, conn=8):
    """Remove connected components of `mask` smaller than min_area."""
    m = mask.astype(np.uint8)
    n, lab, st, _ = cv2.connectedComponentsWithStats(m, connectivity=conn)
    big = np.zeros(n, bool)
    big[1:] = st[1:, cv2.CC_STAT_AREA] >= min_area
    return big[lab]


# ---------------------------------------------------------------- stage 2: parts
R_INLET = 24  # px at K: blue strokes thinner than 2*R_INLET+1 that reach the edge are linework, not background


def disk(r):
    return cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (2 * r + 1, 2 * r + 1))


def ccs(mask, conn=4):
    n, lab, st, cen = cv2.connectedComponentsWithStats(mask.astype(np.uint8), connectivity=conn)
    return n, lab, st, cen


def split_parts(white):
    """White mask -> dict of K x K bool masks.

    whiskers_out : the thin white strokes lying on the blue background (CCs of white, 1500..12000 px)
    sil          : solid silhouette: everything that is not background. Background = blue that survives an
                   opening by a disk wider than the linework, so strokes reaching the edge (hand, arm, whisker
                   roots) are not background. Holes (eyes, face) are inside it.
    lines        : sil minus white = every blue stroke inside the body (eyes, brow, nose, mouth, hand, ...)
    """
    n, lab, st, _ = ccs(white, 4)
    whisk = np.zeros_like(white)
    for i in range(1, n):
        if 1500 <= st[i, cv2.CC_STAT_AREA] < 12000:
            whisk |= lab == i
    body_w = white & ~whisk
    bgm = (~body_w).astype(np.uint8)
    bgo = cv2.morphologyEx(bgm, cv2.MORPH_OPEN, disk(R_INLET))
    n, lab, _, _ = ccs(bgo, 4)
    border = set(np.unique(np.concatenate([lab[0], lab[-1], lab[:, 0], lab[:, -1]]))) - {0}
    sil = ~np.isin(lab, list(border))
    n, lab, st, _ = ccs(sil, 4)
    sil = lab == 1 + np.argmax(st[1:, cv2.CC_STAT_AREA])
    lines = drop_small(sil & ~body_w, 200, 4)
    sil = sil & (body_w | lines)  # specks go back to white... or out of the silhouette, whichever they were
    return dict(white=white, sil=sil, lines=lines, body_w=body_w, whisk=whisk)


def ridge_width(mask):
    """Stroke width in K px: 2*median(distance) along the ridge (skeleton-ish) of the mask."""
    dt = cv2.distanceTransform(mask.astype(np.uint8), cv2.DIST_L2, 5)
    ridge = (dt >= cv2.dilate(dt, np.ones((7, 7), np.uint8))) & (dt > 4)
    return float(2 * np.median(dt[ridge]) - 1)


def grow(part, parent, r=5):
    """Dilate a cut-out part by r px inside its parent so neighbouring parts overlap (no hairline at the cut)."""
    return part | (cv2.dilate(part.astype(np.uint8), disk(r)).astype(bool) & parent)


def split_lines(P):
    """Split P['lines'] into named parts (K x K bool masks); positions are in K px of the 1024 canvas x2.

    Joined shapes are cut by hand-placed rules (documented in MANIFEST):
      nose | mouth : cut along y=1150 between the two junction notches (x 960..1150): the loop above is nose
      hand | arm-line | whisker-l-in : cut at the fist/arm junction (x>=538 and y>=1468 = arm-line) and at the
                                       divider line (x>=441 and y<1040 = the left whisker behind the thumb)
    """
    n, lab, st, cen = ccs(P['lines'], 4)
    out = {}
    yy, xx = np.mgrid[0:K, 0:K]
    for i in range(1, n):
        a, (cx, cy) = st[i, cv2.CC_STAT_AREA], cen[i]
        m = lab == i
        if a < 1000:
            continue
        if a > 70000 and cy < 900:
            out['brow'] = m
        elif a > 70000 or (cx < 700 and a > 40000):
            hand = m
        elif 7000 < a < 10000 and cx < 1500:
            out['eye_l' if cx < 1000 else 'eye_r'] = m
        elif a > 40000:  # nose + mouth
            nose = m & (yy < 1150) & (xx > 960) & (xx < 1150)
            nose_mouth = m
            out['nose'], out['mouth'] = nose, m & ~nose
        elif cx > 1500:
            out['wl_r1' if cy < 1170 else 'wl_r2'] = m
    arm = hand & (xx >= 538) & (yy >= 1468)
    wl = hand & (xx >= 441) & (yy < 1040) & ~arm
    out['hand'] = hand & ~arm & ~wl
    out['arm'], out['wl_l'] = grow(arm, hand), grow(wl, hand)
    out['nose'] = grow(out['nose'], nose_mouth)
    return out


# ---------------------------------------------------------------- stage 3: tracing and geometry
U = K // CANVAS  # raster px per canvas unit
COBALT, WHITE = '#1351AA', '#FFFFFF'
COLORS = {'cobalt': COBALT, 'white': WHITE}
LOTTIE_COLORS = {'cobalt': [0.0745098, 0.3176471, 0.6666667, 1], 'white': [1, 1, 1, 1]}
Z = (0.0, 0.0)
LADDER = [(1.0, 0.2), (1.0, 0.4), (1.0, 1.0), (1.1, 2.0), (1.2, 3.0), (1.3, 4.5), (1.3, 7.0), (1.3, 10.0)]  # (alphamax, opttolerance)


def trace_mask(mask, alphamax=1.0, opttol=0.4, turd=4, pad=4):
    """potrace one K x K bool mask -> closed contours in canvas units.

    Contour = {'v': [(x,y)], 'i': [(dx,dy)], 'o': [(dx,dy)], 'c': True}: Lottie convention, tangents are
    RELATIVE to their vertex. Segment k runs v[k] -> v[k+1] with controls v[k]+o[k] and v[k+1]+i[k+1].
    potracer treats falsy pixels as ink, so the mask is inverted; y is the row index (down).
    """
    ys, xs = np.nonzero(mask)
    if not len(ys):
        return []
    y0, y1, x0, x1 = ys.min(), ys.max() + 1, xs.min(), xs.max() + 1
    sub = np.zeros((y1 - y0 + 2 * pad, x1 - x0 + 2 * pad), bool)
    sub[pad:-pad, pad:-pad] = mask[y0:y1, x0:x1]
    plist = potrace.Bitmap(~sub).trace(turdsize=turd, alphamax=alphamax, opticurve=True, opttolerance=opttol)
    ox, oy = x0 - pad, y0 - pad

    def pt(p):
        return ((p.x + ox) / U, (p.y + oy) / U)

    out = []
    for curve in plist:
        v, i, o = [pt(curve.start_point)], [Z], [Z]
        for sg in curve.segments:
            e = pt(sg.end_point)
            if sg.is_corner:  # two straight edges through the corner point
                for q in (pt(sg.c), e):
                    v.append(q)
                    i.append(Z)
                    o.append(Z)
            else:
                c1, c2 = pt(sg.c1), pt(sg.c2)
                o[-1] = (c1[0] - v[-1][0], c1[1] - v[-1][1])
                v.append(e)
                i.append((c2[0] - e[0], c2[1] - e[1]))
                o.append(Z)
        i[0] = i[-1]  # last vertex == start: merge
        v.pop(), i.pop(), o.pop()
        out.append({'v': v, 'i': i, 'o': o, 'c': True})
    return out


def sample(c, steps=10):
    v, i, o = np.array(c['v']), np.array(c['i']), np.array(c['o'])
    n = len(v)
    t = np.linspace(0, 1, steps, endpoint=False)[:, None]
    pts = []
    for k in range(n if c['c'] else n - 1):
        j = (k + 1) % n
        p0, p1, p2, p3 = v[k], v[k] + o[k], v[j] + i[j], v[j]
        pts.append((1 - t) ** 3 * p0 + 3 * (1 - t) ** 2 * t * p1 + 3 * (1 - t) * t ** 2 * p2 + t ** 3 * p3)
    return np.concatenate(pts)


def raster(contours):
    """Even-odd fill of closed contours (canvas units) -> K x K bool."""
    acc = np.zeros((K, K), np.uint8)
    for c in contours:
        t = np.zeros_like(acc)
        pts = np.round(sample(c) * U * 4).astype(np.int32)
        cv2.fillPoly(t, [pts], 1, lineType=cv2.LINE_8, shift=2)
        acc ^= t
    return acc.astype(bool)


def iou(a, b):
    return float((a & b).sum()) / max(1, float((a | b).sum()))


def simplify(mask, ref, maxv=118, min_iou=0.99, amax=1.3):
    """Lottie-grade trace: of the ladder steps with every path <= maxv vertices and IoU(vs fine trace) >= min_iou,
    the one with the fewest vertices; if none qualifies, the highest IoU within maxv."""
    ok, best = [], None
    for am, ot in [s for s in LADDER if s[0] <= amax]:
        cs = trace_mask(mask, am, ot)
        nv, q = max(len(c['v']) for c in cs), iou(raster(cs), ref)
        if nv > maxv:
            continue
        cand = (sum(len(c['v']) for c in cs), -q, cs, q, (am, ot))
        if q >= min_iou:
            ok.append(cand)
        if best is None or q > best[3]:
            best = cand
    pick = min(ok, key=lambda c: c[:2]) if ok else best
    return pick[2], pick[3], pick[4]


BOTTOM_PARTS = ('silhouette', 'outline')  # parts that end on the flat cut; traced with corners kept (alphamax <= 1.0)


def clamp_bottom(cs, ymax=CANVAS):
    """Curve fits may bulge a fraction of a unit past the cut: pin vertices and handles to y <= ymax."""
    out = []
    for c in cs:
        v = [(x, min(y, ymax)) for x, y in c['v']]
        i = [(dx, min(dy, ymax - y)) for (dx, dy), (_, y) in zip(c['i'], v)]
        o = [(dx, min(dy, ymax - y)) for (dx, dy), (_, y) in zip(c['o'], v)]
        out.append({**c, 'v': v, 'i': i, 'o': o})
    return out


def bottom_run(cs, ymax):
    """x-range of the sampled outline on the cut (y within 0.3 of ymax)."""
    p = np.concatenate([sample(c, 40) for c in cs])
    p = p[p[:, 1] > ymax - 0.3]
    return (round(float(p[:, 0].min()), 1), round(float(p[:, 0].max()), 1))


def shift(cs, dx, dy):
    return [{**c, 'v': [(x + dx, y + dy) for x, y in c['v']]} for c in cs]


def fmt(x):
    s = f'{x:.2f}'.rstrip('0').rstrip('.')
    return '0' if s in ('-0', '') else s


def path_d(cs):
    out = []
    for c in cs:
        v, i, o, n = c['v'], c['i'], c['o'], len(c['v'])
        d = f'M{fmt(v[0][0])} {fmt(v[0][1])}'
        for k in range(n if c['c'] else n - 1):
            j = (k + 1) % n
            if max(abs(o[k][0]), abs(o[k][1]), abs(i[j][0]), abs(i[j][1])) < 1e-6:
                d += f'L{fmt(v[j][0])} {fmt(v[j][1])}'
            else:
                d += (f'C{fmt(v[k][0] + o[k][0])} {fmt(v[k][1] + o[k][1])} {fmt(v[j][0] + i[j][0])} '
                      f'{fmt(v[j][1] + i[j][1])} {fmt(v[j][0])} {fmt(v[j][1])}')
        out.append(d + ('Z' if c['c'] else ''))
    return ''.join(out)


def rnd(cs, nd=1):
    f = lambda p: [round(float(p[0]), nd), round(float(p[1]), nd)]
    return [{'i': [f(p) for p in c['i']], 'o': [f(p) for p in c['o']], 'v': [f(p) for p in c['v']], 'c': c['c']} for c in cs]


def outline_mask(sil, d_px):
    """Solid dilation of the silhouette. The bottom edge is replicated downwards first so the flat cut gets NO outline."""
    big = cv2.copyMakeBorder(sil.astype(np.uint8), 0, 3 * d_px, 0, 0, cv2.BORDER_REPLICATE)
    return cv2.dilate(big, disk(d_px))[:K] > 0


def centroid(mask):
    ys, xs = np.nonzero(mask)
    return ((xs.mean() + 0.5) / U, (ys.mean() + 0.5) / U)


def bbox(mask):
    ys, xs = np.nonzero(mask)
    return (xs.min() / U, ys.min() / U, (xs.max() + 1) / U, (ys.max() + 1) / U)


# ---------------------------------------------------------------- stage 4: new strokes (expressions)
def build(start, segs, closed=False):
    """Contour from absolute cubic segments [(c1, c2, end), ...]."""
    v, i, o = [start], [Z], []
    for c1, c2, e in segs:
        o.append((c1[0] - v[-1][0], c1[1] - v[-1][1]))
        v.append(e)
        i.append((c2[0] - e[0], c2[1] - e[1]))
    o.append(Z)
    if closed and abs(v[-1][0] - v[0][0]) + abs(v[-1][1] - v[0][1]) < 1e-9:
        i[0] = i[-1]
        v.pop(), i.pop(), o.pop()
    return {'v': v, 'i': i, 'o': o, 'c': closed}


def ellipse(cx, cy, rx, ry, T=lambda p: p):
    k = 0.5523
    p = [(cx, cy - ry), (cx + rx, cy), (cx, cy + ry), (cx - rx, cy)]
    hs = [((cx + k * rx, cy - ry), (cx + rx, cy - k * ry)), ((cx + rx, cy + k * ry), (cx + k * rx, cy + ry)),
          ((cx - k * rx, cy + ry), (cx - rx, cy + k * ry)), ((cx - rx, cy - k * ry), (cx - k * rx, cy - ry))]
    segs = [(T(a), T(b), T(p[(n + 1) % 4])) for n, (a, b) in enumerate(hs)]
    return build(T(p[0]), segs, True)


def smooth(pts, T=lambda p: p):
    """Open Catmull-Rom curve through pts (local coords), mapped by T."""
    q = [pts[0]] + list(pts) + [pts[-1]]
    segs = []
    for n in range(1, len(q) - 2):
        p0, p1, p2, p3 = q[n - 1], q[n], q[n + 1], q[n + 2]
        c1 = (p1[0] + (p2[0] - p0[0]) / 6, p1[1] + (p2[1] - p0[1]) / 6)
        c2 = (p2[0] - (p3[0] - p1[0]) / 6, p2[1] - (p3[1] - p1[1]) / 6)
        segs.append((T(c1), T(c2), T(p2)))
    return build(T(pts[0]), segs, False)


def poly(pts, T=lambda p: p):
    return build(T(pts[0]), [(T(a), T(b), T(b)) for a, b in zip(pts[:-1], pts[1:])], False)


def item(name, kind, paths, color='cobalt', width=None):
    return {'name': name, 'kind': kind, 'paths': paths, 'color': color, 'width': width}


def expression_faces(A, eye_items, mouth_item):
    """Eyes/mouth per expression, drawn in the face's own tilted frame. Returns {name: {'eyes': [...], 'mouth': [...]}}.

    A: anchors in standalone coordinates. The face is rotated A['face_tilt_deg'] (the traced eyes, brow bar and
    mouth all share it), so every new stroke is defined in that frame and rotated into place. Mouths are
    defined relative to A['mouth_join'], the centre of the nose's flat base, and overlap it so nose and mouth stay joined.
    """
    th = math.radians(A['face_tilt_deg'])
    cs, sn = math.cos(th), math.sin(th)

    def at(origin):
        return lambda p: (origin[0] + cs * p[0] - sn * p[1], origin[1] + sn * p[0] + cs * p[1])

    El, Er, J = at(A['eye_l']), at(A['eye_r']), at(A['mouth_join'])
    wf, wl = A['weight_face'], A['weight_line']
    out = {'neutral': {'eyes': eye_items, 'mouth': [mouth_item]}}

    def both(fn):
        return [fn('eye-l', El, -1), fn('eye-r', Er, 1)]

    # happy: ^ ^ eyes, open smile
    arc = lambda name, T, s: item(name, 'stroke', [build(T((-28, 14)), [(T((-16, -22)), T((16, -22)), T((28, 14)))])], width=wf)
    out['happy'] = {
        'eyes': both(arc),
        'mouth': [item('mouth', 'fill', [build(J((-72, -10)), [
            (J((-72, -10)), J((72, -10)), J((72, -10))),
            (J((72, 24)), J((40, 52)), J((0, 52))),
            (J((-40, 52)), J((-72, 24)), J((-72, -10)))], True)])]}
    # worried: neutral dots dropped 16 units, slanted brows (line weight: the forehead mark leaves no room for 34), wavy mouth
    down = lambda c: [{**p, 'v': [(x - sn * 16, y + cs * 16) for x, y in p['v']]} for p in c]
    dots = [{**e, 'paths': down(e['paths'])} for e in eye_items]
    brow = lambda name, T, s: item(name.replace('eye', 'eyebrow'), 'stroke', [poly([(s * 38, -30), (s * -30, -52)], T)], width=wl)
    out['worried'] = {
        'eyes': dots + both(brow),
        'mouth': [item('mouth', 'stroke', [smooth([(-70, 34), (-35, 6), (0, 14), (35, 6), (70, 34)], J)], width=wf)]}
    # surprised: ringed eyes with a pupil, small round mouth
    ring = lambda name, T, s: item(name, 'stroke', [ellipse(0, 0, 30, 30, T)], width=wl)
    pupils = [item(f'pupil-{k}', 'fill', [ellipse(0, 0, 11, 11, T)]) for k, T in (('l', El), ('r', Er))]
    out['surprised'] = {'eyes': both(ring) + pupils, 'mouth': [item('mouth', 'fill', [ellipse(0, 22, 30, 28, J)])]}
    # sleepy: closed lids, small relaxed smile
    lid = lambda name, T, s: item(name, 'stroke', [build(T((-30, -4)), [(T((-18, 17)), T((18, 17)), T((30, -4)))])], width=wf)
    out['sleepy'] = {
        'eyes': both(lid),
        'mouth': [item('mouth', 'stroke', [build(J((-56, -4)), [(J((-30, 20)), J((30, 20)), J((56, -4)))])], width=wf)]}
    # error: x eyes, wavy mouth
    cross = lambda name, T, s: item(name, 'stroke', [poly([(-22, -22), (22, 22)], T), poly([(22, -22), (-22, 22)], T)], width=wf)
    out['error'] = {
        'eyes': both(cross),
        'mouth': [item('mouth', 'stroke', [smooth([(-76, 6), (-50, -2), (-25, 14), (0, 2), (25, 14), (50, -2), (76, 6)], J)], width=wf)]}
    return out


# ---------------------------------------------------------------- stage 5: SVG assembly
def path_el(it, ids=False):
    col = COLORS[it['color']]
    d = path_d(it['paths'])
    idattr = f' id="{it["id"]}"' if ids and it.get('id') else ''
    if it['kind'] == 'stroke':
        return (f'<path{idattr} d="{d}" fill="none" stroke="{col}" stroke-width="{fmt(it["width"])}" '
                f'stroke-linecap="round" stroke-linejoin="round"/>')
    rule = ' fill-rule="evenodd"' if len(it['paths']) > 1 else ''
    return f'<path{idattr} d="{d}" fill="{col}"{rule}/>'


def group_el(g, ids):
    inner = ''.join(group_el(c, ids) if 'children' in c else path_el(c, ids) for c in g['children'])
    idattr = f' id="{g["id"]}"' if ids else ''
    return f'<g{idattr}>{inner}</g>'


def svg_doc(w, h, body, defs=''):
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">'
            f'{defs}{body}</svg>\n')


def write(path, text):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf8', newline='\n') as f:
        f.write(text)


FACE_MARK = (
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 64 64" width="64" height="64">'
    '<g fill="#1351AA" stroke="#1351AA" stroke-width="12" stroke-linejoin="round">'
    '<path d="M8 38a24 18 0 1 0 48 0a24 18 0 1 0 -48 0Z"/>'
    '<path d="M10 33L12 9L30 22Z"/><path d="M54 33L52 9L34 22Z"/></g>'
    '<g fill="#FFFFFF"><path d="M8 38a24 18 0 1 0 48 0a24 18 0 1 0 -48 0Z"/>'
    '<path d="M10 33L12 9L30 22Z"/><path d="M54 33L52 9L34 22Z"/></g>'
    '<circle cx="22" cy="35.5" r="5.2" fill="#1351AA"/><circle cx="42" cy="35.5" r="5.2" fill="#1351AA"/>'
    '<path d="M25 47.5Q32 54.5 39 47.5" fill="none" stroke="#1351AA" stroke-width="4.4" stroke-linecap="round"/>'
    '</svg>\n')


def render(svg, png, width, bg='none'):
    env = dict(os.environ, NODE_PATH=os.path.join(SCRATCH, 'brand-tools', 'node_modules'))
    r = subprocess.run(['node', os.path.join(HERE, 'render.cjs'), svg, png, str(width), bg],
                       env=env, capture_output=True, text=True)
    if r.returncode:
        raise RuntimeError(r.stderr)


def verify_svgs(paths):
    """XML parses, has viewBox, no image/text/filter/gradient/pattern/use, only #1351AA blue (+ white)."""
    bad = {'image', 'text', 'filter', 'lineargradient', 'radialgradient', 'pattern', 'use', 'foreignobject', 'style', 'script'}
    problems = []
    for p in paths:
        raw = open(p, encoding='utf8').read()
        root = ET.fromstring(raw)
        if not root.get('viewBox'):
            problems.append(f'{p}: no viewBox')
        tags = {e.tag.split('}')[-1].lower() for e in root.iter()}
        if tags & bad:
            problems.append(f'{p}: forbidden element {tags & bad}')
        hexes = {h.upper() for h in re.findall(r'#[0-9a-fA-F]{6}\b', raw)} - {COBALT, WHITE}
        if hexes:
            problems.append(f'{p}: stray colours {hexes}')
        ids = re.findall(r'\bid="([^"]+)"', raw)
        if len(ids) != len(set(ids)):
            problems.append(f'{p}: duplicate ids')
    return problems


# ---------------------------------------------------------------- stage 6: main
def lottie_item(it):
    d = {'name': it['name'], 'kind': it['kind'], 'color': it['color'], 'paths': rnd(it['paths'])}
    if it['kind'] == 'stroke':
        d.update(width=round(it['width'], 2), cap='round', join='round')
    else:
        d['fillRule'] = 'evenodd' if len(it['paths']) > 1 else 'nonzero'
    return d


def py(o):
    """numpy scalars -> plain Python numbers, recursively (clean JSON and clean MANIFEST text)."""
    if isinstance(o, dict):
        return {k: py(v) for k, v in o.items()}
    if isinstance(o, (list, tuple)):
        return [py(v) for v in o]
    return o.item() if isinstance(o, np.generic) else o


def manifest(R, A):
    vw, vh = R['viewBox'][2], R['viewBox'][3]
    vx, vy = R['origin_in_icon_canvas']
    sz = R['sizes_kb']
    rows = ''.join(f"| `{k}` | {v['fine']} | {v['lottie']} | {v['iou']:.4f} |\n" for k, v in R['counts'].items())
    return f'''# Nekko master set

Traced from `docs/brand/source/nekko-original.jpg` by `docs/brand/tools/trace.py` (rerun it to regenerate every file here, this MANIFEST included). Only `#1351AA` (Site cobalt) and `#FFFFFF` appear; the Icon-blue twin is made later by script. Contract: `docs/brand/SPEC.md` section 5.

## Files

| File | Purpose | viewBox | KB |
| :--- | :--- | :--- | ---: |
| `nekko-icon-full-bleed.svg` | iOS 1024 master: cobalt square, white bust to the bottom edge, whiskers, linework. Opaque, no mask, no shadow. One white even-odd path (the traced white mask; eyes, face and hand lines are holes in it) over a rect. | 0 0 1024 1024 | {sz['nekko-icon-full-bleed.svg']} |
| `nekko-icon-squircle.svg` | Same drawing under a rounded-square clip (rx 229.4 = 22.4%). Preview only. | 0 0 1024 1024 | {sz['nekko-icon-squircle.svg']} |
| `nekko-standalone.svg` | Transparent. Cobalt outline, white body, cobalt linework, cobalt whiskers. Bust cut flat at the viewBox bottom, no outline on the cut. For cream and white. | 0 0 {vw} {vh} | {sz['nekko-standalone.svg']} |
| `nekko-rig.svg` | Same drawing, named groups (below). | 0 0 {vw} {vh} | {sz['nekko-rig.svg']} |
| `nekko-expressions/nekko-{{neutral,happy,worried,surprised,sleepy,error}}.svg` | Standalone with eyes and mouth swapped. neutral = the traced original. Body, brow mark, nose, hand and whiskers unchanged. | 0 0 {vw} {vh} | {sz['nekko-expressions/nekko-neutral.svg']} each |
| `nekko-face-mark.svg` | Hand-drawn head mark (two ears, two dot eyes, smile) for 16-48 px favicons. Checked at 16, 32 and 48 px enlarged 8x. | 0 0 64 64 | {sz['nekko-face-mark.svg']} |
| `nekko-lottie-shapes.json` | Parts as Lottie bezier paths, expression items, anchors. Use through `docs/brand/tools/nekko_lottie.py`. | {vw} x {vh} | {sz['nekko-lottie-shapes.json']} |

## Coordinates

- Icon files use the 1024 canvas (source crop resampled to 2048, units = px / 2).
- Standalone, rig, expressions and the Lottie JSON share one frame: `viewBox="0 0 {vw} {vh}"`. It is the icon canvas window x {vx}..{vx + vw}, y {vy}..1024, shifted so the origin is 0,0: standalone = canvas - ({vx}, {vy}). Margin is 16 units on top, left and right (measured to the outline and the whisker tips); 0 at the bottom, where the bust is cut flush with the viewBox edge. Bottom-centre pivot: ({vw / 2:g}, {vh}).
- Left/right (`eye-l`, `whisker-l`) are the VIEWER's left and right: `eye-l` is on the thumb side.

## Measured weights (units)

- Face linework (brow, nose, mouth): {A['weight_face']}. Outline thickness: {A['outline_weight']} (the silhouette dilated by this much). New expression strokes use {A['weight_face']}.
- Hand and arm lines: {A['weight_line']}. Whiskers: {A['weight_whisker']} drawn blue on white, {R['weights']['whisker_out']} drawn white on blue; the outer whiskers are grown by 1.5 units per side in cobalt so the weight matches where they cross the edge.
- Eye dot radius {A['eye_radius']}. Face tilt {A['face_tilt_deg']} degrees (brow bar, eyes and mouth share it; expression strokes follow it).

## Rig groups (`nekko-rig.svg`, bottom to top)

`outline` (solid cobalt, silhouette dilated), `silhouette` (solid white, no holes), `arm-line`, `hand` (thumb, fist and divider lines), `brow` (bar, tick, two strokes), `eyes` (`eye-l`, `eye-r`), `nose` (loop plus its white island, even-odd), `mouth`, `whiskers` (`whisker-l`, `whisker-r1`, `whisker-r2`; each holds the cobalt line inside the body and the stroke outside it). Every linework part is its own cobalt fill on top of the white silhouette (nothing is a hole), so expressions and blinks swap parts cleanly.

Hand-placed cuts where strokes were one blob in the source: nose | mouth along y = {1150 / 2 - vy:g} (standalone) between the two junction notches; arm-line | hand at the fist/arm junction; the left whisker's inner segment | hand at the divider line. Neighbouring parts overlap by 2.5 units so no hairline shows.

## Expressions

All six keep body, brow mark, nose, hand and whiskers. neutral is the trace. happy: arc eyes, open smile. worried: dot eyes dropped 16 units, slanted brows, wavy down mouth. surprised: ringed eyes with pupils, small round mouth. sleepy: closed lids, small smile. error: x eyes, wavy mouth. New strokes have round caps and the face weight; the worried brows and surprised rings use the hand-line weight ({A['weight_line']}) because the forehead bar leaves about 40 units above the eyes. Mouths overlap the nose's flat base (`mouth_join` anchor) so nose and mouth stay joined.

## Vertex counts and fidelity

Fine trace: potrace alphamax 1.0, opttolerance 0.2 (the SVG files). Lottie trace: coarsest alphamax/opttolerance step of a ladder with at most 118 vertices per path and IoU >= 0.99 against the fine trace.

| Part | Fine vertices | Lottie vertices (max per path) | Lottie IoU vs fine |
| :--- | ---: | ---: | ---: |
{rows}
Full-bleed white path: {R['fine_white_vertices']} vertices. Full-bleed render vs source (`docs/brand/source/trace-check.png`: source crop | render | difference): IoU **{R['iou_full_bleed_raw']}** against the raw JPEG crop (resized to 1024, thresholded at the blue/white midpoint, inside the icon hull eroded 5 px) and **{R['iou_full_bleed']}** against the cleaned mask that was traced.

## Anchors (standalone coordinates, in `nekko-lottie-shapes.json` under `anchors`)

eye_l {A['eye_l']}, eye_r {A['eye_r']}, mouth_center {A['mouth_center']}, mouth_join {A['mouth_join']}, nose_center {A['nose_center']}, face_center {A['face_center']}, brow_bbox {A['brow_bbox']}, hand_center {A['hand_center']}, head_bbox {A['head_bbox']} (approximate: down to the cheek line, thumb excluded), silhouette_bbox {A['silhouette_bbox']}, body_bottom_y {A['body_bottom_y']}, whisker_bboxes, weights.

## Known limits

- The source box is 1857 x 1847 px, resampled to a square: 0.5% non-uniform stretch (inside the 3% allowance).
- The rounded bottom corners cut the body short; it is extended straight down by up to {R['ext_max_units']} units (the arm's left edge is vertical over that stretch instead of continuing its slant).
- The silhouette is smoothed where strokes reach the edge (thumb notch, whisker roots): notches narrower than about 24 units are filled, and the cobalt strokes overrun the edge by 1.5 units under the outline.
- The brief mentions two white whiskers on the left. The source has one: the white stroke outside the thumb plus its blue continuation to the right of the thumb. The right side has two. The left outer whisker stops about 15 units short of the thumb in the source; in the standalone it joins the outline.
- Whiskers are 12-15 units, hand lines 16, face 34: the outline (34) is bold next to the hand lines. It is the measured face weight, as specified.
- The face mark is hand-drawn, not traced; it leaves out the forehead mark and whiskers (invisible at 16 px).
- `nekko-lottie-shapes.json` rounds to 0.1 unit. A full Nekko layer from `nekko_lottie.py` is about 18 KB of Lottie JSON (60 KB cap per file): leave out parts you do not animate if a scene needs the budget.
- The raw-crop IoU leaves out the hull's edge band, the filled rounded corners and the bottom extension (the raw crop holds grey backdrop and shadow there that would read as white). The traced-mask IoU covers the whole canvas.
- The bust cut: `silhouette` and `outline` are traced with corners kept (alphamax 1.0) and pinned to y <= {vh} so the cut stays flat and flush; the run on the cut is x {R['counts']['silhouette']['cut_run']['fine']} (fine) / {R['counts']['silhouette']['cut_run']['lottie']} (Lottie) for the silhouette.
'''


def main():
    t0 = time.time()
    os.makedirs(DBG, exist_ok=True)
    white, crop, thr, hull = load_mask()
    P = split_parts(white)
    Lp = split_lines(P)
    sil = P['sil']
    assert not (P['lines'] & ~np.any([m for m in Lp.values()], axis=0)).any(), 'unassigned stroke pixels'

    # whiskers on the background: left, right 1, right 2
    n, lab, st, cen = ccs(P['whisk'], 4)
    wo = {}
    for i in range(1, n):
        wo['wo_l' if cen[i][0] < 700 else ('wo_r1' if cen[i][1] < 1250 else 'wo_r2')] = lab == i
    # measured linework (canvas units): face = brow/nose/mouth, line = hand/arm, whisker in (blue) vs out (white)
    w_face = round(float(np.median([ridge_width(Lp[k]) for k in ('brow', 'nose', 'mouth')])) / U)
    w_line = round(float(np.median([ridge_width(Lp[k]) for k in ('hand', 'arm')])) / U)
    w_wh_in = float(np.median([ridge_width(Lp[k]) for k in ('wl_l', 'wl_r1', 'wl_r2')])) / U
    w_wh_out = float(np.median([ridge_width(m) for m in wo.values()])) / U
    grow = int(round((w_wh_in - w_wh_out) * U / 2))  # white-on-blue strokes read thinner; match them in cobalt
    print(f'weights: face {w_face}, line {w_line}, whisker in {w_wh_in:.1f} / out {w_wh_out:.1f} (+{grow}px each side)')

    ext = lambda m: m | (cv2.dilate(m.astype(np.uint8), disk(3)).astype(bool) & ~sil)  # let strokes overrun the silhouette edge
    d_out = w_face * U
    masks = {
        'outline': outline_mask(sil, d_out), 'silhouette': sil,
        'arm-line': ext(Lp['arm']), 'hand': ext(Lp['hand']), 'brow': ext(Lp['brow']),
        'eye-l': Lp['eye_l'], 'eye-r': Lp['eye_r'], 'nose': Lp['nose'], 'mouth': Lp['mouth'],
        'whisker-l-in': ext(Lp['wl_l']), 'whisker-r1-in': ext(Lp['wl_r1']), 'whisker-r2-in': ext(Lp['wl_r2']),
        'whisker-l-out': cv2.dilate(wo['wo_l'].astype(np.uint8), disk(grow)).astype(bool),
        'whisker-r1-out': cv2.dilate(wo['wo_r1'].astype(np.uint8), disk(grow)).astype(bool),
        'whisker-r2-out': cv2.dilate(wo['wo_r2'].astype(np.uint8), disk(grow)).astype(bool),
    }
    FINE = (1.0, 0.2)
    fine = {k: trace_mask(m, *FINE) for k, m in masks.items()}
    fine_white = clamp_bottom(trace_mask(white, *FINE))
    for k in BOTTOM_PARTS:
        fine[k] = clamp_bottom(fine[k])
    print(f'traced fine in {time.time() - t0:.0f}s')

    # standalone window: 16 units margin top/left/right around outline + whiskers, 0 at the bottom (the cut)
    ext_boxes = [bbox(masks[k]) for k in ('outline', 'whisker-l-out', 'whisker-r1-out', 'whisker-r2-out')]
    vx = int(math.floor(min(b[0] for b in ext_boxes) - 16))
    vy = int(math.floor(min(b[1] for b in ext_boxes) - 16))
    vr = int(math.ceil(max(b[2] for b in ext_boxes) + 16))
    vw, vh = vr - vx, CANVAS - vy
    sh = lambda cs: shift(cs, -vx, -vy)
    sa = {k: sh(v) for k, v in fine.items()}

    # anchors (standalone coordinates)
    ctr = lambda k: (centroid(masks[k])[0] - vx, centroid(masks[k])[1] - vy)
    eye_l, eye_r = ctr('eye-l'), ctr('eye-r')
    nys, nxs = np.nonzero(masks['nose'])
    base_rows = nys >= nys.max() - 5
    mouth_join = ((nxs[base_rows].mean() + 0.5) / U - vx, (nys.max() + 1) / U - vy)
    b = lambda k: tuple(round(c, 2) for c in (bbox(masks[k])[0] - vx, bbox(masks[k])[1] - vy, bbox(masks[k])[2] - vx, bbox(masks[k])[3] - vy))
    n2, lab2, st2, cen2 = ccs(P['body_w'], 4)
    hand_white = max((i for i in range(1, n2) if cen2[i][0] < 700 and st2[i, cv2.CC_STAT_AREA] > 20000), key=lambda i: st2[i, cv2.CC_STAT_AREA])
    top_row = np.nonzero(sil.any(axis=1))[0].min()
    head_x0 = np.nonzero(sil[top_row:800].any(axis=0))[0].min()
    head_x1 = np.nonzero(sil[top_row:1240].any(axis=0))[0].max() + 1
    mb = bbox(masks['mouth'])
    A = {
        'eye_l': [round(eye_l[0], 2), round(eye_l[1], 2)], 'eye_r': [round(eye_r[0], 2), round(eye_r[1], 2)],
        'eye_radius': round(math.sqrt(masks['eye-l'].sum() / math.pi) / U, 2),
        'face_tilt_deg': round(math.degrees(math.atan2(eye_r[1] - eye_l[1], eye_r[0] - eye_l[0])), 2),
        'mouth_center': [round((mb[0] + mb[2]) / 2 - vx, 2), round((mb[1] + mb[3]) / 2 - vy, 2)],
        'mouth_join': [round(mouth_join[0], 2), round(mouth_join[1], 2)],
        'nose_center': [round(centroid(masks['nose'])[0] - vx, 2), round(centroid(masks['nose'])[1] - vy, 2)],
        'brow_bbox': list(b('brow')),
        'hand_center': [round((cen2[hand_white][0] + 0.5) / U - vx, 2), round((cen2[hand_white][1] + 0.5) / U - vy, 2)],
        'hand_bbox': list(b('hand')),
        'head_bbox': [round(head_x0 / U - vx, 2), round(top_row / U - vy, 2), round(head_x1 / U - vx, 2), round(1240 / U - vy, 2)],
        'silhouette_bbox': list(b('silhouette')),
        'body_bottom_y': vh,
        'weight_face': w_face, 'weight_line': w_line, 'weight_whisker': round(w_wh_in, 1), 'outline_weight': w_face,
        'whisker_bboxes': {k: list(b(k)) for k in ('whisker-l-out', 'whisker-r1-out', 'whisker-r2-out')},
    }
    A['face_center'] = [round((eye_l[0] + eye_r[0] + A['mouth_center'][0]) / 3, 2), round((eye_l[1] + eye_r[1] + A['mouth_center'][1]) / 3, 2)]
    A['bottom_center'] = [vw / 2, vh]
    A = py(A)

    # groups (rig order = draw order, bottom to top)
    fi = lambda name, key, color='cobalt', **kw: {**item(name, 'fill', sa[key], color), **kw}
    eye_items = [fi('eye-l', 'eye-l', id='eye-l'), fi('eye-r', 'eye-r', id='eye-r')]
    mouth_item = fi('mouth', 'mouth')
    G = {
        'outline': {'id': 'outline', 'children': [fi('outline', 'outline')]},
        'silhouette': {'id': 'silhouette', 'children': [fi('silhouette', 'silhouette', 'white')]},
        'arm-line': {'id': 'arm-line', 'children': [fi('arm-line', 'arm-line')]},
        'hand': {'id': 'hand', 'children': [fi('hand', 'hand')]},
        'brow': {'id': 'brow', 'children': [fi('brow', 'brow')]},
        'eyes': {'id': 'eyes', 'children': eye_items},
        'nose': {'id': 'nose', 'children': [fi('nose', 'nose')]},
        'mouth': {'id': 'mouth', 'children': [mouth_item]},
        'whiskers': {'id': 'whiskers', 'children': [
            {'id': f'whisker-{s}', 'children': [fi(f'whisker-{s}-in', f'whisker-{s}-in'), fi(f'whisker-{s}-out', f'whisker-{s}-out')]}
            for s in ('l', 'r1', 'r2')]},
    }
    ORDER = ['outline', 'silhouette', 'arm-line', 'hand', 'brow', 'eyes', 'nose', 'mouth', 'whiskers']
    body = lambda groups, ids: ''.join(group_el(groups[k], ids) for k in ORDER)

    # --- files
    write(os.path.join(OUT, 'nekko-icon-full-bleed.svg'), svg_doc(
        CANVAS, CANVAS, f'<rect width="{CANVAS}" height="{CANVAS}" fill="{COBALT}"/>'
        f'<path fill="{WHITE}" fill-rule="evenodd" d="{path_d(fine_white)}"/>'))
    clip = f'<clipPath id="squircle"><rect width="{CANVAS}" height="{CANVAS}" rx="229.4" ry="229.4"/></clipPath>'
    write(os.path.join(OUT, 'nekko-icon-squircle.svg'), svg_doc(
        CANVAS, CANVAS, f'<g clip-path="url(#squircle)"><rect width="{CANVAS}" height="{CANVAS}" fill="{COBALT}"/>'
        f'<path fill="{WHITE}" fill-rule="evenodd" d="{path_d(fine_white)}"/></g>', f'<defs>{clip}</defs>'))
    write(os.path.join(OUT, 'nekko-standalone.svg'), svg_doc(vw, vh, body(G, False)))
    write(os.path.join(OUT, 'nekko-rig.svg'), svg_doc(vw, vh, body(G, True)))
    faces = expression_faces(A, eye_items, mouth_item)
    for name, f in faces.items():
        g2 = dict(G)
        g2['eyes'] = {'id': 'eyes', 'children': f['eyes']}
        g2['mouth'] = {'id': 'mouth', 'children': f['mouth']}
        write(os.path.join(OUT, 'nekko-expressions', f'nekko-{name}.svg'), svg_doc(vw, vh, body(g2, False)))
    write(os.path.join(OUT, 'nekko-face-mark.svg'), FACE_MARK)

    # --- Lottie shapes (simplified, canvas units -> standalone coordinates)
    parts, counts = {}, {}
    for k, m in masks.items():
        cs, q, params = simplify(m, raster(fine[k]), amax=1.0 if k in BOTTOM_PARTS else 1.3)
        if k in BOTTOM_PARTS:
            cs = clamp_bottom(cs)
        parts[k] = {'color': 'white' if k == 'silhouette' else 'cobalt', 'fillRule': 'evenodd' if len(cs) > 1 else 'nonzero',
                    'paths': rnd(shift(cs, -vx, -vy))}
        counts[k] = dict(fine=sum(len(c['v']) for c in fine[k]), lottie=max(len(c['v']) for c in cs), iou=round(q, 4), params=params)
        if k in BOTTOM_PARTS:
            counts[k]['cut_run'] = {'fine': [round(x - vx, 1) for x in bottom_run(fine[k], CANVAS)],
                                    'lottie': [round(x - vx, 1) for x in bottom_run(cs, CANVAS)]}
        print(f'{k:16s} fine {counts[k]["fine"]:4d}  lottie {counts[k]["lottie"]:4d}  IoU {q:.4f}  {params}')
    lottie = {
        'name': 'nekko', 'width': vw, 'height': vh, 'origin_in_icon_canvas': [vx, vy], 'units': 'px; y down; standalone coordinate system',
        'colors': LOTTIE_COLORS, 'fill_rule_note': 'nose has a hole (its white island): use Lottie fill "r":2 (even-odd). Other parts have one path.',
        'order_bottom_to_top': ['outline', 'silhouette', 'arm-line', 'hand', 'brow', 'eyes', 'nose', 'mouth', 'whiskers'],
        'groups': {'eyes': ['eye-l', 'eye-r'], 'whiskers': [f'whisker-{s}-{p}' for s in ('l', 'r1', 'r2') for p in ('in', 'out')]},
        'handedness': 'left/right/l/r = the VIEWER\'s left/right (screen left is eye-l, whisker-l, the thumb side).',
        'parts': parts,
        'expressions': {nm: {'eyes': [lottie_item(i) for i in f['eyes']], 'mouth': [lottie_item(i) for i in f['mouth']]}
                        for nm, f in faces.items() if nm != 'neutral'},
        'expressions_note': 'neutral = parts eye-l, eye-r, mouth. Other expressions replace the whole eyes and mouth groups with these items (strokes: round cap and join).',
        'anchors': A,
    }
    write(os.path.join(OUT, 'nekko-lottie-shapes.json'), json.dumps(lottie, separators=(',', ':')) + '\n')

    # --- verification: IoU of the full-bleed render vs the source mask
    svg = os.path.join(OUT, 'nekko-icon-full-bleed.svg')
    png = os.path.join(DBG, 'full-bleed-1024.png')
    render(svg, png, CANVAS)
    r = cv2.imread(png)
    rlum = cv2.cvtColor(r, cv2.COLOR_BGR2GRAY)
    rmask = rlum > (float(rlum.min()) + 255) / 2
    smask = cv2.resize(white.astype(np.uint8) * 255, (CANVAS, CANVAS), interpolation=cv2.INTER_AREA) > 127
    q = iou(rmask, smask)
    diff = np.full((CANVAS, CANVAS, 3), 255, np.uint8)
    diff[rmask & smask] = (235, 235, 235)
    diff[rmask & ~smask] = (0, 0, 255)  # render has white where the source is blue (red)
    diff[~rmask & smask] = (255, 120, 0)  # source white lost in the render (blue)
    sheet = np.concatenate([crop, r, diff], axis=1)
    cv2.imwrite(os.path.join(ROOT, 'docs', 'brand', 'source', 'trace-check.png'), sheet)
    hull1024 = cv2.erode(cv2.resize(hull.astype(np.uint8), (CANVAS, CANVAS), interpolation=cv2.INTER_NEAREST), disk(3)) > 0
    raw = cv2.cvtColor(crop, cv2.COLOR_BGR2GRAY) > thr  # the source crop resized to 1024, thresholded at the blue/white midpoint
    q_raw = iou(rmask & hull1024, raw & hull1024)
    print(f'white-mask IoU full-bleed render vs traced mask: {q:.4f}; vs raw crop threshold inside the hull: {q_raw:.4f}')

    files = [os.path.join(OUT, f) for f in ('nekko-icon-full-bleed.svg', 'nekko-icon-squircle.svg', 'nekko-standalone.svg', 'nekko-rig.svg', 'nekko-face-mark.svg')]
    files += [os.path.join(OUT, 'nekko-expressions', f'nekko-{n}.svg') for n in faces]
    probs = verify_svgs(files)
    print('SVG problems:', probs or 'none')
    report = {'ext_max_units': round(EXT_MAX / U, 1), 'iou_full_bleed': round(q, 4), 'iou_full_bleed_raw': round(q_raw, 4), 'viewBox': [0, 0, vw, vh], 'origin_in_icon_canvas': [vx, vy], 'counts': counts,
              'weights': {'face': w_face, 'line': w_line, 'whisker_in': round(w_wh_in, 1), 'whisker_out': round(w_wh_out, 1)},
              'sizes_kb': {os.path.relpath(f, OUT).replace(os.sep, '/'): round(os.path.getsize(f) / 1024, 1) for f in files + [os.path.join(OUT, 'nekko-lottie-shapes.json')]},
              'fine_white_vertices': sum(len(c['v']) for c in fine_white), 'problems': probs}
    write(os.path.join(DBG, 'report.json'), json.dumps(report, indent=1))
    write(os.path.join(OUT, 'MANIFEST.md'), manifest(report, A))
    print(f'done in {time.time() - t0:.0f}s')
    return report


if __name__ == '__main__':
    main()
