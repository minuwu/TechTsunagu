"""Builds the four moments animations into frontend/public/brand/lottie/moments/.
Run from the repo root:  <venv python> <scratch>\\work\\lottie-moments\\build.py [name ...]
"""
import os
import random
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from lm import *  # noqa: E402,F401,F403
import lm  # noqa: E402

OUT = r'C:\Users\minha\Desktop\TechTsunagu\frontend\public\brand\lottie\moments'
os.makedirs(OUT, exist_ok=True)


def p3(x, y):
    return [x, y, 0]


# ===================================================================== logo-reveal
def logo_reveal():
    N = 66
    s = 0.5                                   # same placement as nekko-icon-full-bleed.svg at 512 px
    base = (262.0, 512.0)                     # bust bottom-centre in the icon (canvas x 31..1017 -> 15.5 + 246.5)
    g = nekko('neutral', s, extend=True, outer_whiskers=WHITE)
    whisker_flick(g, s, 38)
    rise = kf([(12, [base[0], base[1] + 540]), (29, [base[0], base[1] - 26], OUT_CUBIC),
               (38, [base[0], base[1]], IN_OUT_SOFT)], OUT_CUBIC)
    squash = kf([(12, [90, 112, 100]), (29, [96, 105, 100], IN_OUT_SOFT), (33, [104, 96, 100], IN_OUT_SOFT),
                 (39, [100, 100, 100])], IN_OUT_SOFT)
    nek = nekko_layer(g, s, base, name='Nekko', ind=2, op=N, scale_prop=squash)
    nek['ks']['p'] = rise

    # cobalt square: grows from the centre, corners round to square as it reaches the frame
    size = kf([(2, [0, 0]), (20, [520, 520])], OUT_EXPO)
    rad = kf([(2, 150), (20, 0)], OUT_EXPO)
    sq = blank_layer([group([rect_item(size, size, rad, 'square'), fill_item(COBALT)], 'Cobalt square')],
                     'Cobalt square', 3, N, pos=(256, 256))
    sq['shapes'][0]['it'][0]['s'] = size
    sq['shapes'][0]['it'][0]['r'] = rad

    # cream sparkle: pops, turns a quarter, shrinks away
    sp_s = kf([(42, [0, 0]), (47, [118, 118], OUT_CUBIC), (50, [100, 100], IN_OUT_SOFT), (56, [100, 100], IN_CUBIC),
               (62, [0, 0])], OUT_CUBIC)
    sp_r = kf([(42, -30), (62, 60)], IN_OUT_SOFT)
    sp = group([group([path_item(star4(26)), fill_item(CREAM), stroke_item(CREAM, 6)], 'star')], 'Sparkle')
    set_tr(sp, p=[408, 118], s=sp_s, r=sp_r)
    spl = blank_layer([sp], 'Sparkle', 1, N)
    return file([spl, nek, sq], 512, 512, N, 'logo-reveal')


# ===================================================================== welcome
def welcome():
    N = 54
    s = fit(512, 512, 0.9)
    base = (256.0, 512.0)
    g = nekko('happy', s, extend=True)
    blink(g, s, 34)
    # thumb: hand lines and arm line swing about the fist
    hand_pv = [150 * s, 520 * s]
    for key, amp in (('hand', 0.4),):
        t = 42
        set_tr(g[key], a=hand_pv, p=hand_pv, r=kf([(t, 0), (t + 2, -9 * amp, OUT_CUBIC), (t + 4, 7 * amp, IN_OUT_SOFT),
                                                   (t + 6, -5 * amp, IN_OUT_SOFT), (t + 8, 3 * amp, IN_OUT_SOFT),
                                                   (t + 10, 0)], IN_OUT_SOFT))
    pos = kf([(0, [base[0], base[1] + 540]), (10, [base[0], base[1] - 36], OUT_CUBIC), (16, [base[0], base[1]], IN_QUAD),
              (19, [base[0], base[1]], OUT_QUAD), (23, [base[0], base[1] - 15], IN_QUAD), (27, [base[0], base[1]], OUT_QUAD),
              (31, [base[0], base[1]])], OUT_CUBIC)
    sc = kf([(0, [88, 114, 100]), (10, [95, 107, 100], IN_OUT_SOFT), (16, [100, 100, 100], OUT_QUAD),
             (19, [110, 88, 100], OUT_QUAD), (23, [97, 105, 100], IN_OUT_SOFT), (27, [103, 95, 100], IN_OUT_SOFT),
             (31, [100, 100, 100])], IN_OUT_SOFT)
    nek = nekko_layer(g, s, base, ind=1, op=N, scale_prop=sc)
    nek['ks']['p'] = pos
    return file([nek], 512, 512, N, 'welcome')


# ===================================================================== celebrate
def celebrate():
    N = 66
    s = fit(512, 512, 0.62)
    base = (256.0, 512.0)
    g = nekko('happy', s, extend=True)
    hop = kf([(2, [base[0], base[1]]), (8, [base[0], base[1] + 8], IN_QUAD), (16, [base[0], base[1] - 56], OUT_CUBIC),
              (22, [base[0], base[1]], IN_QUAD), (26, [base[0], base[1] - 20], OUT_QUAD), (30, [base[0], base[1]], IN_QUAD),
              (34, [base[0], base[1]])], OUT_QUAD)
    sc = kf([(2, [100, 100, 100]), (8, [108, 90, 100], OUT_QUAD), (16, [94, 108, 100], IN_OUT_SOFT),
             (22, [108, 90, 100], OUT_QUAD), (26, [98, 104, 100], IN_OUT_SOFT), (30, [103, 96, 100], IN_OUT_SOFT),
             (34, [100, 100, 100])], IN_OUT_SOFT)
    nek = nekko_layer(g, s, base, ind=1, op=N, scale_prop=sc)
    nek['ks']['p'] = hop

    sq = lambda r: poly([(-r, -r), (r, -r), (r, r), (-r, r)])
    tri = lambda r: poly([(0, -r * 1.1), (r * 1.05, r * 0.8), (-r * 1.05, r * 0.8)])
    bar = lambda r: poly([(-r * 1.7, -r * 0.45), (r * 1.7, -r * 0.45), (r * 1.7, r * 0.45), (-r * 1.7, r * 0.45)])
    shape = {'sq': sq, 'tri': tri, 'bar': bar}
    # (shape, size, colour, burst dx, apex y, end x, start frame, apex frame, end frame, spin, ease of fall)
    C, W_, J, K = COBALT, WHITE, JET, CREAM
    P = [('sq', 11, C, -150, 60, -230, 2, 13, 52, 540, IN_QUAD), ('tri', 12, W_, -95, 40, -120, 3, 14, 56, -420, IN_CUBIC),
         ('bar', 9, J, -190, 120, -300, 2, 12, 50, 360, IN_QUAD), ('sq', 9, K, -60, 25, -20, 4, 15, 58, -600, IN_QUAD),
         ('tri', 11, C, -30, 15, 60, 3, 13, 54, 480, IN_CUBIC), ('bar', 10, W_, -120, 85, -170, 5, 15, 60, 300, IN_QUAD),
         ('sq', 12, J, 20, 10, 130, 2, 14, 55, -480, IN_CUBIC), ('tri', 10, K, 70, 40, 160, 4, 14, 57, 420, IN_QUAD),
         ('bar', 11, C, 150, 70, 250, 3, 13, 52, -360, IN_QUAD), ('sq', 10, W_, 110, 30, 150, 5, 16, 62, 540, IN_CUBIC),
         ('tri', 12, J, 190, 110, 300, 2, 12, 50, -450, IN_QUAD), ('bar', 9, K, 45, 55, 70, 6, 16, 60, 330, IN_CUBIC),
         ('sq', 8, C, -210, 160, -330, 4, 13, 53, 500, IN_QUAD), ('tri', 9, W_, 215, 150, 330, 3, 13, 52, -540, IN_QUAD),
         ('bar', 10, J, -70, 20, -80, 7, 17, 62, -300, IN_CUBIC), ('sq', 9, K, 160, 130, 240, 6, 15, 58, 400, IN_QUAD)]
    items = []
    for n, (sh, r, col, dx, ay, ex, t0, t1, t2, spin, fall) in enumerate(P):
        x0, y0 = 256 + dx * 0.15, 300
        ax = 256 + dx
        a_y = ay                                   # apex y: small = high on the canvas
        pos = kf([(t0, [x0, y0], OUT_QUAD), (t1, [ax, a_y], fall), (t2, [256 + ex, 600 + r * 3])], OUT_QUAD)
        rot = kf([(t0, 0), (t2, spin)], OUT_CUBIC if n % 2 else IN_OUT_SOFT)
        scl = kf([(t0, [0, 0]), (t0 + 3, [100, 100], OUT_CUBIC), (t2, [100, 100])], OUT_CUBIC)
        paint = [fill_item(col)]
        paint.append(stroke_item(JET, 5) if col is W_ else stroke_item(col, 5))
        items.append(set_tr(group([path_item(shape[sh](r))] + paint, f'piece {n + 1} {sh}'), p=pos, r=rot, s=scl))
    conf = blank_layer(list(reversed(items)), 'Confetti', 2, N)
    return file([nek, conf], 512, 512, N, 'celebrate')


# ===================================================================== page-wipe
def page_wipe():
    W_, H_, N = 1280, 720, 34
    s = 0.7
    BW = 1300                                  # block width: covers the 1280 frame while the edge is in 1280..1300
    g = nekko('happy', s, extend=True)
    edge = kf([(0, [-640, 360]), (14, [1285, 360], OUT_CUBIC), (19, [1295, 360], IN_OUT_SOFT), (33, [2600, 360])], IN_CUBIC)
    nek = nekko_layer(g, s, (-640, 360), ind=2, op=N, rotation=90)
    nek['ks']['p'] = edge
    blk_pos = kf([(0, [-640 - BW / 2, 360]), (14, [1285 - BW / 2, 360], OUT_CUBIC), (19, [1295 - BW / 2, 360], IN_OUT_SOFT),
                  (33, [2600 - BW / 2, 360])], IN_CUBIC)
    blk = blank_layer([group([rect_item(BW, H_ + 40, 0, 'block'), fill_item(COBALT)], 'Cobalt block')], 'Cobalt block', 1, N)
    blk['ks']['p'] = blk_pos
    return file([blk, nek], W_, H_, N, 'page-wipe')


BUILDERS = {'logo-reveal': logo_reveal, 'welcome': welcome, 'celebrate': celebrate, 'page-wipe': page_wipe}

if __name__ == '__main__':
    names = sys.argv[1:] or list(BUILDERS)
    for n in names:
        nl.save(BUILDERS[n](), os.path.join(OUT, n + '.json'))
        print(n, round(os.path.getsize(os.path.join(OUT, n + '.json')) / 1024, 1), 'KB')
