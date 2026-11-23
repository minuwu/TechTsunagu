import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from lib import *  # noqa

OUT = os.path.join(REPO, 'frontend', 'public', 'brand', 'lottie', 'flow')
os.makedirs(OUT, exist_ok=True)
DATA = nl.load()
BC = DATA['anchors']
FACE_SURPRISED_OK = True


def nekko_layer(s, faces, swaps, pos, scale=None, rot=0, name='Nekko'):
    shapes, named = nekko_shapes(DATA, s, faces, swaps)
    return lay(name, shapes, p=pos, a=nl.bottom_center(DATA, s), s=scale, r=rot), named


def pop_scale(t0, dur=14, peak=112):
    """Pop with overshoot: 0 -> peak -> 100 (percent)."""
    mid = t0 + round(dur * 0.6)
    return Ks([(t0, 0, 'out'), (mid, peak, 'io'), (t0 + dur, 100)])


def ray_group(name, center, angles, r0, r1, color, w, t_in, t_mid, t_out):
    """Short radial strokes that burst: trim end grows, then trim start follows."""
    paths = []
    for a in angles:
        c, s_ = math.cos(math.radians(a)), math.sin(math.radians(a))
        paths.append(line((center[0] + c * r0, center[1] + s_ * r0), (center[0] + c * r1, center[1] + s_ * r1)))
    # trim end leads, trim start follows
    trim = {'ty': 'tm', 'nm': 'burst', 's': K([(t_mid - 2, 0, 'io'), (t_out, 100)]),
            'e': K([(t_in, 0, 'out'), (t_mid, 100)]), 'o': P(0), 'm': 1}
    return grp(name, paths + [trim, st(color, w)])


def question_mark(color, cy=0, w=12):
    pts = []
    for d in range(170, 401, 15):
        a = math.radians(d)
        pts.append((17 * math.cos(a), -10 + 17 * math.sin(a) + cy))
    pts += [(3, 6 + cy), (0, 11 + cy)]
    return grp('question-mark', [el(14, 14, (0, 31 + cy)), fl(color)]), grp('question-stroke', [sh(pts), st(color, w)])


def tick_path(sc=1.0, dx=0, dy=0):
    return sh([(-44 * sc + dx, 6 * sc + dy), (-14 * sc + dx, 36 * sc + dy), (46 * sc + dx, -30 * sc + dy)])


# ------------------------------------------------------------------ card (shared by check-correct / check-wrong)
ROWS = (-14, 22, 58)


def card_shapes(hl_keys=None):
    """Question card around (0,0): 280x180, abstract lines. hl_keys: opacity keys of the cobalt highlighted row."""
    items = []
    if hl_keys:
        y = ROWS[1]
        items.append(grp('answer-highlight', [
            sh([(-70, y), (60, y)]), st(COBALT, 10),
        ], o=K(hl_keys)))
        items.append(grp('answer-dot', [el(22, 22, (-96, y)), fl(COBALT)], o=K(hl_keys)))
    items.append(grp('question-line', [sh([(-100, -56), (40, -56)]), st(JET, 14)]))
    for y in ROWS:
        items.append(grp('option-ring', [el(22, 22, (-96, y)), st(JET, 6)]))
        items.append(grp('option-line', [sh([(-70, y), (60, y)]), st(JET, 10, 38)]))
    items.append(grp('card', [rc(280, 180, 30), st(JET, 10), fl(WHITE)]))
    return items


# ------------------------------------------------------------------ 1. machine-unlock
def machine_unlock():
    OP = 54
    cx, cy = 256, 352
    pk = [(0, 70, 'out'), (9, 106, 'io'), (13, 100)]
    tile_s = Ks(pk)
    tile_o = K([(0, 0, 'out'), (6, 100)])
    # tile
    lit = K([(20, 0, 'out'), (28, 100)])
    tile = lay('Machine tile', [
        grp('status-dots', [el(16, 16, (-28, 70)), el(16, 16, (0, 70)), el(16, 16, (28, 70)), fl(WHITE, 55)]),
        grp('lit', [rc(230, 200, 38), st(JET, 12), fl(COBALT)], o=lit),
        grp('base', [rc(230, 200, 38), fl(JET)]),
    ], p=(cx, cy), s=tile_s, o=tile_o)
    # padlock
    shk_rot = K([(10, 0, 'out'), (13, -4, 'out'), (22, 38, 'io'), (26, 30)])
    shk_pos = K([(10, [22, -10]), (13, [22, -6], 'out'), (20, [22, -34], 'out'), (26, [22, -28])])
    shackle = grp('shackle', [sh([(-22, -10), (22, -10)], i=[(0, 0), (0, -48)], o=[(0, -48), (0, 0)]), st(WHITE, 15)],
                  a=(22, -10), p=shk_pos, r=shk_rot)
    body = grp('lock-body', [rc(80, 62, 16, (0, 16)), fl(WHITE)])
    keyhole = grp('keyhole', [el(15, 15, (0, 12)), sh([(0, 14), (0, 28)]), fl(JET), st(JET, 8)])
    lock_o = K([(0, 0, 'out'), (6, 100, 'hold'), (28, 100, 'in'), (34, 0)])
    lock_s = Ks([(0, 70, 'out'), (9, 106, 'io'), (13, 100, 'in'), (28, 100, 'in'), (34, 70)])
    lock = lay('Padlock', [keyhole, shackle, body], p=(cx, cy), s=lock_s, o=lock_o)
    # tick
    tick_trim = tm(0, K([(32, 0, 'out'), (42, 100)]))
    tick = lay('Tick', [grp('tick', [tick_path(1.15, 0, 4), tick_trim, st(WHITE, 26)])], p=(cx, cy),
               s=Ks([(32, 100, 'out'), (42, 100, 'io'), (46, 108, 'io'), (50, 100)]))
    rays = lay('Impact rays', [
        ray_group('rays-left', (cx, cy), [160, 180, 200], 138, 170, COBALT, 12, 24, 30, 38),
        ray_group('rays-right', (cx, cy), [-20, 0, 20], 138, 170, COBALT, 12, 24, 30, 38)], p=(0, 0))
    npos = Kp([(22, (256, 372), 'out'), (36, (256, 328), 'io'), (42, (256, 336))])
    nk, _ = nekko_layer(0.29, ['happy'], [], pos=npos,
                        scale=Ks([(22, 0, 'out'), (34, 110, 'io'), (42, 100)]))
    return build('machine-unlock', [rays, tick, lock, tile, nk], OP, os.path.join(OUT, 'machine-unlock.json'))


# ------------------------------------------------------------------ 2. check-correct
def check_correct():
    OP = 54
    cx, cy = 256, 346
    card_o = K([(0, 0, 'out'), (6, 100)])
    card = lay('Question card', card_shapes([(18, 0, 'out'), (24, 100)]), p=(cx, cy),
               s=Ks([(0, 88, 'out'), (10, 102, 'io'), (14, 100)]), o=card_o)
    bx, by = 372, 424
    badge_s = Ks([(22, 240, 'in'), (26, 88, 'out'), (29, 108, 'io'), (33, 100)])
    badge = lay('Stamp badge', [
        grp('badge-tick', [tick_path(0.62), st(WHITE, 15)]),
        grp('badge', [el(92, 92), fl(SUCCESS)]),
    ], p=(bx, by), s=badge_s, o=K([(22, 0, 'hold'), (24, 100)]))
    rays = lay('Stamp impact', [ray_group('stamp-rays', (bx, by), [-90, -45, 0, 45, 90, 135, 180, 225], 58, 82,
                                         SUCCESS, 9, 26, 30, 38)], p=(0, 0))
    nk, named = nekko_layer(0.27, ['neutral', 'happy'], [24], pos=Kp([(6, (cx, 400), 'out'), (18, (cx, 316), 'io'),
                                                                   (24, (cx, 324))]),
                            scale=Ks([(6, 0, 'out'), (16, 108, 'io'), (24, 100)]))
    return build('check-correct', [rays, badge, card, nk], OP, os.path.join(OUT, 'check-correct.json'))


# ------------------------------------------------------------------ 3. check-wrong
def check_wrong():
    OP = 66
    cx, cy = 232, 350
    t0 = 10
    xs = [(0, cx), (t0, cx), (t0 + 4, cx + 20), (t0 + 9, cx - 16), (t0 + 14, cx + 11), (t0 + 19, cx - 7),
          (t0 + 24, cx + 3), (t0 + 29, cx)]
    pos = Kp([(f, (x, cy), 'io') for f, x in xs[:-1]] + [(xs[-1][0], (xs[-1][1], cy))])
    card = lay('Question card', card_shapes(), p=pos, s=Ks([(0, 88, 'out'), (10, 102, 'io'), (14, 100)]),
               o=K([(0, 0, 'out'), (6, 100)]))
    rot = K([(6, 0, 'out'), (16, -4, 'io'), (36, -3, 'io'), (50, -4)])
    nk, _ = nekko_layer(0.27, ['neutral', 'worried'], [t0], pos=Kp([(2, (cx - 18, 410), 'out'), (14, (cx - 18, 326), 'io'),
                                                                   (20, (cx - 18, 332))]),
                        scale=Ks([(2, 0, 'out'), (12, 108, 'io'), (20, 100)]), rot=rot)
    # amber hint bubble, anchored at its tail tip
    q_dot, q_arc = question_mark(WHITE, cy=-4)
    tail = [(-30, 36), (-52, 74), (-4, 44)]
    bub = lay('Hint bubble', [
        q_dot, q_arc,
        grp('bubble-tail', [sh(tail, closed=True), st(WARNING, 10), fl(WARNING)]),
        grp('bubble', [rc(124, 98, 32), fl(WARNING)]),
    ], p=(400 - 52, 112 + 74), a=(-52, 74),
        s=Ks([(24, 0, 'out'), (33, 114, 'io'), (40, 100)]))
    return build('check-wrong', [bub, card, nk], OP, os.path.join(OUT, 'check-wrong.json'))


# ------------------------------------------------------------------ 4. understood-stamp
def understood_stamp():
    OP = 54
    cx, cy = 256, 256
    paper_pos = Kp([(0, (cx, cy), 'out'), (13, (cx, cy), 'out'), (15, (cx, cy + 6), 'out'), (20, (cx, cy - 2), 'io'),
                    (25, (cx, cy))])
    paper = lay('Paper', [
        grp('line-b', [sh([(-110, 144), (10, 144)]), st(JET, 12, 38)]),
        grp('line-a', [sh([(-110, -150), (50, -150)]), st(JET, 14)]),
        grp('line-a2', [sh([(-110, -122), (-10, -122)]), st(JET, 10, 38)]),
        grp('paper', [rc(330, 404, 38), st(JET, 10), fl(WHITE)]),
    ], p=paper_pos, s=Ks([(0, 88, 'out'), (10, 102, 'io'), (14, 100)]), o=K([(0, 0, 'out'), (7, 100)]))
    ix, iy = 256, 270
    imprint = lay('Hanko imprint', [
        grp('tick', [tick_path(1.55, 0, 4), st(COBALT, 30)]),
        grp('ring-inner', [el(150), st(COBALT, 6)]),
        grp('ring-outer', [el(196), st(COBALT, 18)]),
    ], p=(ix, iy), s=Ks([(8, 190, 'in'), (13, [112, 88], 'out'), (17, [95, 104], 'io'), (21, [102, 98], 'io'),
                         (25, 100)]),
        o=K([(8, 0, 'hold'), (9, 100)]))
    rays = lay('Impact lines', [ray_group('impact', (ix, iy), [i * 30 + 15 for i in range(12)], 120, 150, COBALT, 11,
                                         13, 17, 25)], p=(0, 0))
    return build('understood-stamp', [rays, imprint, paper], OP, os.path.join(OUT, 'understood-stamp.json'))


# ------------------------------------------------------------------ 5. voice-recording (loop, 48 frames)
def voice_recording():
    OP = 48
    mx, my = 256, 138
    d0, d1 = 112, 250

    def ring(phase, n):
        sk, ok = [], []
        for f in range(0, 49, 2):
            u = ((f + phase) % 48) / 48.0
            e = 1 - (1 - u) ** 2.2
            d = d0 + (d1 - d0) * e
            o = 100 * min(1, u / 0.1) * (1 - u) ** 1.3 if u > 0 else 0
            if (f + phase) % 48 == 0:
                o = 0
            sk.append((f, [d, d], 'lin'))
            ok.append((f, o, 'lin'))
        return grp(f'pulse-ring-{n}', [el(K(sk)), st(COBALT, 11)], o=K(ok))
    rings = lay('Rings', [ring(0, 1), ring(16, 2), ring(32, 3)], p=(mx, my))
    mic_body = grp('mic-body', [rc(54, 88, 27, (0, -10)), fl(COBALT)])
    grille = grp('mic-grille', [sh([(-12, -22), (12, -22)]), sh([(-12, -6), (12, -6)]), st(WHITE, 7)])
    holder = grp('mic-holder', [sh([(-42, -6), (0, 50), (42, -6)], i=[(0, 0), (-24, 0), (0, 22)],
                                   o=[(0, 22), (24, 0), (0, 0)]), st(COBALT, 12)])
    stem = grp('mic-stem', [sh([(0, 48), (0, 68)]), sh([(-20, 70), (20, 70)]), st(COBALT, 12)])
    beat = Ks([(0, 100, 'io'), (12, 106, 'io'), (24, 100, 'io'), (36, 106, 'io'), (48, 100)])
    mic = lay('Microphone', [grille, mic_body, holder, stem], p=(mx, my), s=beat)
    s = 0.32
    breathe = Ks([(0, [100, 100], 'io'), (24, [100.8, 102], 'io'), (48, [100, 100])])
    nk, named = nekko_layer(s, ['neutral'], [], pos=(256, 512), scale=breathe, name='Nekko listening')
    eyes = named['eyes-neutral']
    blink(eyes, DATA, s, [(0, [100, 100], 'hold'), (27, [100, 100], 'in'), (30, [100, 8], 'out'), (34, [100, 100])])
    return build('voice-recording', [mic, nk, rings], OP, os.path.join(OUT, 'voice-recording.json'))


# ------------------------------------------------------------------ 6. nudge-bell
def nudge_bell(face='surprised'):
    OP = 54
    px, py = 256, 56
    peaks = [(0, 0), (6, 26), (13, -22), (20, 15), (27, -9), (33, 4.5), (38, -2), (43, 0)]
    bell_rot = K([(f, a, 'io') for f, a in peaks[:-1]] + [(peaks[-1][0], peaks[-1][1])])
    lag = [(f + 2, -a * 1.5) for f, a in peaks]
    clap_rot = K([(f, a, 'io') for f, a in lag[:-1]] + [(lag[-1][0], lag[-1][1])])
    body = sh([(0, -64), (58, 12), (84, 52), (-84, 52), (-58, 12)], closed=True,
              i=[(-36, 0), (0, -34), (-10, -12), (10, -12), (0, 16)],
              o=[(36, 0), (0, 16), (0, 0), (0, 0), (0, -34)])
    bell_items = [
        grp('clapper', [el(30, 30, (0, 66)), fl(JET)], a=(0, -20), p=(0, -20), r=clap_rot),
        grp('bell-shine', [sh([(-38, 4), (-34, -22)], i=[(0, 0), (0, 0)], o=[(0, 0), (0, 0)]), st(WHITE, 9, 80)]),
        grp('bell-body', [body, st(COBALT, 10), fl(COBALT)]),
        grp('bell-knob', [el(26, 26, (0, -68)), fl(COBALT)]),
    ]
    bell = lay('Bell', bell_items, p=(px, py), a=(0, -66 + 0), r=bell_rot)
    # keep anchor at hook: layer anchor (0,-66) and position at hook
    bell['ks']['a'] = nl.static([0, -66, 0])
    bracket = lay('Bracket', [grp('hook', [el(16, 16, (0, 0)), fl(JET)]),
                              grp('arm', [sh([(-62, 0), (62, 0)]), st(JET, 16)])], p=(px, py - 66 + 66 - 2))
    arcs = []
    for side in (-1, 1):
        for n, (r, w) in enumerate(((132, 11), (162, 9))):
            a0, a1 = (-34, 34)
            pts = [(px + side * r * math.cos(math.radians(a)) * 1.0, py + 66 + r * math.sin(math.radians(a)))
                   for a in range(a0, a1 + 1, 17)]
            arcs.append(grp(f'sound-{"l" if side < 0 else "r"}{n}', [sh(pts), st(COBALT, w)]))
    pulse = [(0, 0, 'out'), (5, 0, 'out'), (7, 100, 'io'), (12, 35, 'io'), (14, 90, 'io'), (19, 25, 'io'),
             (21, 65, 'io'), (26, 15, 'io'), (28, 40, 'io'), (33, 0)]
    sounds = lay('Sound arcs', arcs, p=(0, 0), o=K(pulse))
    faces = [face] if face == 'neutral' else [face, 'neutral']
    swaps = [] if face == 'neutral' else [40]
    nk, _ = nekko_layer(0.32, faces, swaps, pos=(256, 512),
                        scale=Ks([(4, 70, 'out'), (20, 104, 'io'), (26, 100)]))
    # Nekko slides up from below the frame: position (x, bottom)
    nk['ks']['p'] = Kp([(4, (256, 820), 'out'), (20, (256, 504), 'io'), (26, (256, 512))])
    return build('nudge-bell', [bracket, bell, sounds, nk], OP, os.path.join(OUT, 'nudge-bell.json'))


if __name__ == '__main__':
    only = sys.argv[1:]
    for fn in (machine_unlock, check_correct, check_wrong, understood_stamp, voice_recording, nudge_bell):
        if not only or fn.__name__ in only:
            fn()
            print('built', fn.__name__)
