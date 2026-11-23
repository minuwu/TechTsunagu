"""Build the five empty-state Lottie files. Run: python build.py  (repo root not required)."""
import os

from lib import *  # noqa: F401,F403

OUT = os.path.join(REPO, 'frontend', 'public', 'brand', 'lottie', 'empty')
data = nl.load()
sin = lambda t, T, ph=0: math.sin(TAU * (t - ph) / T)
cos = lambda t, T, ph=0: math.cos(TAU * (t - ph) / T)
breathe = lambda amp, T=45: (lambda t: 100 + amp * (1 - cos(t, T)) / 2 * 2)  # 100 .. 100 + 2*amp


def ease_out(x):
    return 1 - (1 - min(1.0, max(0.0, x))) ** 3


# ------------------------------------------------------------------ 1. empty-list
def empty_list():
    s, yb = 0.34, 448
    nk = nekko(data, 'sleepy', s, 256, yb, breath=breathe(0.55), sway=lambda t: 1.2 * sin(t, 90))
    box = layer('Empty tray', [
        grp('tape slot', [poly([(216, 408), (296, 408)]), stroke(CO, 10)]),
        grp('tray rim', [rect(352, 32, 14, 256, 366), stroke(CO, 10), fill(CO)]),
        grp('tray front', [rect(328, 100, 20, 256, 416), stroke(CO, 10), fill(WH)]),
    ])
    z = [grp('Z', [poly([(-18, -18), (18, -18), (-18, 18), (18, 18)]), stroke(CO, 9)])]

    def zf(u):
        return dict(p=(368 + 38 * u + 7 * math.sin(TAU * 1.25 * u), 198 - 132 * u),
                    s=55 + 60 * ease_out(u / 0.35) + 10 * u, r=-8 + 16 * u,
                    o=100 * sm(u / 0.2) * (1 - sm((u - 0.7) / 0.3)))

    zs = [l for ph in (0, 30, 60) for l in cycle_layers('Z floating up', z, zf, phase=ph)]
    return file('empty-list', zs + [box, nk])


# ------------------------------------------------------------------ 2. no-results
def qmark():
    p = Path().move(-24, -26).curve((0, -17), (-13, 0), 0, -50).curve((13, 0), (0, -14), 24, -28)
    p.curve((0, 14), (0, -10), 0, 6).line(0, 22)
    return [grp('question dot', [disc(7.5, 0, 46), fill(CO)]), grp('question hook', [p.item(), stroke(CO, 12)])]


def no_results():
    s = 0.38
    nk = nekko(data, 'worried', s, 256, 512, breath=breathe(0.5), blink=64, gaze=lambda t: [6 * sin(t, 90), 0])
    a = math.radians(48)
    h0, h1 = (46 * math.cos(a), 46 * math.sin(a)), (112 * math.cos(a), 112 * math.sin(a))
    lens = layer('Magnifying glass', [grp('lens ring', [disc(46), stroke(CO, 10)]),
                                     grp('handle', [poly([h0, h1]), stroke(CO, 17)])],
                 p=anim(lambda t: [256 + 80 * sin(t, 90), 322 + 7 * sin(t, 45, 11), 0], 0, L, 6),
                 r=anim(lambda t: 8 * sin(t, 90), 0, L, 6))
    q1 = layer('Question mark', qmark(), p=anim(lambda t: [388, 100 + 6 * sin(t, 45), 0], 0, L, 6),
               r=anim(lambda t: 7 * sin(t, 90, 20), 0, L, 6))
    q2 = layer('Question mark small', qmark(), p=anim(lambda t: [122, 142 + 5 * sin(t, 45, 22), 0], 0, L, 6),
               s=[58, 58, 100], r=anim(lambda t: -12 + 6 * sin(t, 90, 55), 0, L, 6))
    return file('no-results', [lens, q1, q2, nk])


# ------------------------------------------------------------------ 3. offline
def offline():
    s = 0.34
    nk = nekko(data, 'sleepy', s, 232, 524, breath=breathe(0.55), sway=lambda t: 1.0 * sin(t, 90))
    wx, wy = 306, 204
    arcs = []
    for k, r in enumerate((34, 62, 90)):
        arcs.append(grp(f'wifi arc {k + 1}', [Path().arc(0, 0, r, -135, -45).item(), stroke(CO, 12)],
                        o=anim(lambda t, k=k: 32 + 68 * (1 + cos(t, 45, 7 * k)) / 2, 0, L, 6)))
    wifi = layer('Wi-Fi icon', arcs[::-1] + [grp('wifi dot', [disc(9), fill(CO)])], p=(wx, wy))
    cx, cy = -4, -42
    slash = layer('Slash', [grp('slash halo', [poly([(-72 - cx, -90 - cy), (64 - cx, 8 - cy)]), stroke(WH, 27)]),
                            grp('slash', [poly([(-72 - cx, -90 - cy), (64 - cx, 8 - cy)]), stroke(DANGER, 13)])][::-1],
                  p=(wx + cx, wy + cy), s=anim(lambda t: [100 + 9 * (1 - cos(t, 45)) / 2] * 2 + [100], 0, L, 6))
    outlet = layer('Wall socket', [
        grp('slot l', [rect(10, 26, 5, 440, 420), fill(JET)]), grp('slot r', [rect(10, 26, 5, 472, 420), fill(JET)]),
        grp('socket plate', [rect(74, 74, 18, 456, 420), stroke(CO, 10), fill(WH)])])
    low = grp('cable lower', [
        grp('plug', [grp('plug body', [rect(44, 52, 12, 0, 264), stroke(CO, 10), fill(WH)]),
                     grp('prong l', [rect(9, 28, 4.5, -11, 302), fill(JET)]),
                     grp('prong r', [rect(9, 28, 4.5, 11, 302), fill(JET)])][::-1]),
        grp('cable end', [Path().move(0, 150).curve((8, 30), (0, -30), 0, 238).item(), stroke(JET, 9)])],
        a=(0, 150), p=(0, 150), r=anim(lambda t: 5 * sin(t, 90, 9), 0, L, 6))
    up = grp('cable upper', [Path().move(0, 0).curve((14, 45), (-14, -45), 0, 150).item(), stroke(JET, 9)])
    cable = layer('Unplugged cable', [low, up], p=(456, -6), r=anim(lambda t: 4.5 * sin(t, 90), 0, L, 6))
    return file('offline', [slash, cable, outlet, wifi, nk])


# ------------------------------------------------------------------ 4. error-404
def error_404():
    s = 0.34
    nk = nekko(data, 'worried', s, 210, 526, breath=breathe(0.45), blink=62,
               sway=lambda t: -2.6 * (1 - cos(t, 90)), gaze=lambda t: [5 + 2 * sin(t, 90), -6])
    half = Path().move(0, -25).line(-54, -25).arc(-54, 0, 25, -90, -270).line(0, 25).item()
    d = lambda t: 17 + 16 * (1 - cos(t, 90)) / 2
    ticks = [((0, -40), (0, -57)), ((-17, -36), (-28, -49)), ((17, -36), (28, -49))]
    sparks = [poly([a, b]) for a, b in ticks] + [poly([(a[0], -a[1]), (b[0], -b[1])]) for a, b in ticks]
    chain = layer('Broken chain link', [
        grp('sparks', [grp('spark', [p, stroke(DANGER, 8)]) for p in sparks],
            o=anim(lambda t: 100 * sm((d(t) - 23) / 8), 0, L, 6)),
        grp('link left', [half, stroke(CO, 12)], p=anim(lambda t: [-d(t), 4], 0, L, 6)),
        grp('link right', [half, stroke(CO, 12)], p=anim(lambda t: [d(t), -4], 0, L, 6), s=[-100, 100])],
        p=anim(lambda t: [372, 150 + 5 * sin(t, 45), 0], 0, L, 6), s=[92, 92, 100],
        r=anim(lambda t: -30 + 3 * sin(t, 90), 0, L, 6))
    return file('error-404', [chain, nk])


# ------------------------------------------------------------------ 5. maintenance
def gear_item(teeth=8, ro=62, ri=48, hw=13, tw=7.5):
    p = Path().move(ri * math.cos(math.radians(-hw)), ri * math.sin(math.radians(-hw)))
    step = 360 / teeth
    for k in range(teeth):
        th = k * step
        for ang, rad in ((th - tw, ro), (th + tw, ro), (th + hw, ri)):
            p.line(rad * math.cos(math.radians(ang)), rad * math.sin(math.radians(ang)))
        p.arc(0, 0, ri, th + hw, th + step - hw)
    return p.item(True)


def wrench_item():
    j = math.degrees(math.atan2(-math.sqrt(900 - 169), -13))   # jaw tip angle, -13 px off centre
    h = math.degrees(math.atan2(math.sqrt(900 - 144), -12))
    p = Path().move(-13, -math.sqrt(900 - 169)).arc(0, 0, 30, j + 360, h)
    p.line(-12, 118).arc(0, 118, 12, 180, 0).line(12, math.sqrt(900 - 144))
    p.arc(0, 0, 30, 180 - h, 180 - j - 360 + 360 - 360).line(13, -6).line(-13, -6)
    return p.item(True)


def hat():
    dome = Path().move(290, 168).curve((0, -120), (-90, 0), 530, -22).curve((90, 0), (0, -120), 770, 168).item(True)
    glint = Path().move(352, 118).curve((0, -40), (-20, 20), 410, 30).item()
    return grp('hard hat', [
        grp('brim', [rect(560, 44, 22, 530, 158), fill(CO), stroke(CO, 16)]),
        grp('ridge', [rect(70, 158, 26, 530, 71), fill(WH)]),
        grp('glint', [glint, stroke(WH, 14)]),
        grp('dome', [dome, fill(CO), stroke(CO, 24)])], s=[34, 34])


def maintenance():
    s = 0.34
    nk = nekko(data, 'neutral', s, 196, 512, breath=breathe(0.5), blink=50, extra=[hat()])
    hit = [30, 75]
    gx, gy = 392, 418
    bump = nl.kf([(0, [100, 100, 100], nl.EASE_OUT)] + sum(([(h - 1, [100, 100, 100], nl.EASE_OUT), (h + 1, [105, 105, 100], nl.EASE_IN_OUT),
                                                          (h + 7, [100, 100, 100], nl.EASE_OUT)] for h in hit), []) + [(L, [100, 100, 100])])
    gear = layer('Gear', [grp('gear hub', [disc(19), stroke(CO, 10), fill(WH)]),
                          grp('gear body', [gear_item(), stroke(CO, 10), fill(WH)])],
                 p=(gx, gy), s=bump, r=anim(lambda t: 135 * t / L, 0, L, 15))
    # wrench: handle end is the pivot; swings the head down onto the gear (hit), bounces, lifts, repeats every 45 frames
    base, hitr, lift = 180, 35, 36
    E_IO, E_IN, E_OUT = nl.EASE_IN_OUT, nl.EASE_IN, nl.EASE_OUT
    keys = []
    for o in (0, 45):
        keys += [(o, base + hitr - lift, E_IO), (o + 21, base + hitr - lift, E_IO), (o + 26, base + hitr - lift - 7, E_IO),
                 (o + 30, base + hitr, E_IN), (o + 34, base + hitr - 9, E_OUT), (o + 37, base + hitr, E_IN),
                 (o + 40, base + hitr - 2, E_OUT)]
    keys[-1] = (L - 0, base + hitr - lift)
    keys = keys[:-1] + [(L, base + hitr - lift)]
    wrench = layer('Wrench', [grp('wrench', [wrench_item(), fill(JET), stroke(JET, 8)])], p=(460, 225), a=(0, 118),
                   r=nl.kf(keys))
    ticks = [((-26, -4), (-40, -16)), ((0, -8), (0, -26)), ((26, -4), (40, -16))]
    spark_op = nl.kf([(0, 0, E_OUT)] + sum(([(h, 0, E_OUT), (h + 1, 100, E_OUT), (h + 9, 0, E_OUT)] for h in hit), [])[:-1] + [(L, 0)])
    sparks = layer('Sparks', [grp('spark', [poly([a, b]), stroke(WARN, 8)]) for a, b in ticks], p=(gx, 354), o=spark_op)
    return file('maintenance', [sparks, wrench, gear, nk])


if __name__ == '__main__':
    os.makedirs(OUT, exist_ok=True)
    for name, fn in [('empty-list', empty_list), ('no-results', no_results), ('offline', offline),
                     ('error-404', error_404), ('maintenance', maintenance)]:
        path = os.path.join(OUT, name + '.json')
        nl.save(fn(), path)
        print(name, round(os.path.getsize(path) / 1024, 1), 'KB')
