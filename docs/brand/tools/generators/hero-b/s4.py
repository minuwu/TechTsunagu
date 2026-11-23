from lib import *


def messy(x, y, w, h, a, kind=0, pinned=True):
    """Plain notice sheet with abstract lines; rotated about its centre."""
    c = 44
    inner = poly([(x, y), (x + w - c, y), (x + w, y + c), (x + w, y + h), (x, y + h)], WH)
    inner += poly([(x + w - c, y), (x + w, y + c), (x + w - c, y + c)], HAIR)
    if kind == 0:
        inner += rect(x + 22, y + 34, w * 0.5, 16, JET) + bars(x + 22, y + 78, w - 44, 7, 26, 10)
        inner += rect(x + 22, y + h - 56, 40, 24, COB)
    elif kind == 1:
        inner += rect(x + 22, y + 34, w - 90, 12, JET) + rect(x + 22, y + 58, w * 0.4, 12, JET)
        inner += rect(x + 22, y + 96, w - 44, h * 0.3, HAIR) + bars(x + 22, y + h * 0.3 + 124, w - 44, 4, 26, 10)
    else:
        inner += bars(x + 22, y + 38, w - 44, 9, 26, 10)
    if pinned:
        inner += pin(x + w / 2 - 14, y + 24)
    return rot(a, x + w / 2, y + h / 2, inner)


def garbage_sheet(x, y, w, h, a):
    c = 56
    inner = poly([(x, y), (x + w - c, y), (x + w, y + c), (x + w, y + h), (x, y + h)], WH)
    inner += poly([(x + w - c, y), (x + w, y + c), (x + w - c, y + c)], HAIR)
    cw = (w - 66) / 2
    for i, ic in enumerate((bin_icon(), bag_icon())):
        cx = x + 22 + i * (cw + 22)
        inner += rect(cx, y + 60, cw, cw, CREAM)
        inner += tr(cx + cw * 0.14, y + 60 + cw * 0.14, ic, cw * 0.72 / 64)
    inner += rect(x + 22, y + 60 + cw + 26, w * 0.55, 14, JET)
    inner += bars(x + 22, y + 60 + cw + 56, w - 44, 3, 24, 10)
    inner += pin(x + w / 2 - 20, y + 28)
    return rot(a, x + w / 2, y + h / 2, inner)


def ribbon(x, y, w, h):
    n = 46
    s = poly([(x, y), (x + w, y), (x + w, y + h), (x + w / 2, y + h - n), (x, y + h)], COB)
    s += tr(x + w * 0.16, y + h * 0.2, bell_icon(), w * 0.68 / 64)
    return s


def desktop():
    W, H = 1920, 1080
    b = [grid(W, H)]
    # jet board bleeds off the top and right; the grid carries on across it
    b.append(rect(1040, 0, 880, 880, JET))
    for k in (7, 8, 9, 10, 11):
        b.append(rect(k * 160 - 1, 0, 2, 880, WH, 0.10))
    # stray notices (many papers = noise)
    b.append(messy(990, 170, 210, 290, -8, 2))
    b.append(messy(1130, 60, 200, 270, 4, 1))
    b.append(ribbon(1240, 0, 150, 250))
    b.append(garbage_sheet(1090, 260, 320, 330, -3))
    # calendar hero, one day lit
    cal, _ = calendar(1440, 40, 430, 400)
    b.append(cal)
    b.append(pin(1655, 30, 15))
    # understood note with the signature tick
    b.append(rot(-3, 1205, 690, rect(1070, 560, 270, 260, WH)))
    tk, _ = tick(1115, 600, 170, COB, t=0.24)
    b.append(rot(-3, 1205, 690, tk + pin(1205, 574, 12)))
    # nekko peeks over the ledge
    b.append(nekko_bottom('neutral', 1372, 960, 0.57))
    b.append(rect(960, 880, 960, 200, COB))
    return svg(W, H, ''.join(b))


def mobile():
    W, H = 1080, 1350
    b = [grid(W, H, 180)]
    b.append(rect(0, 570, W, 620, JET))
    for k in range(1, 6):
        b.append(rect(k * 180 - 1, 570, 2, 620, WH, 0.10))
    b.append(messy(30, 640, 190, 250, -9, 2))
    b.append(garbage_sheet(90, 620, 330, 300, -3))
    cal, _ = calendar(520, 600, 510, 290, hl=(1, 4))
    b.append(cal)
    b.append(pin(775, 588, 14))
    b.append(ribbon(390, 570, 110, 220))
    b.append(rot(-3, 215, 1035, rect(80, 930, 270, 250, WH)))
    tk, _ = tick(125, 965, 160, COB, t=0.24)
    b.append(rot(-3, 215, 1035, tk + pin(215, 944, 12)))
    b.append(nekko_bottom('neutral', 580, 1290, 0.46))
    b.append(rect(0, 1190, W, 160, COB))
    return svg(W, H, ''.join(b))
