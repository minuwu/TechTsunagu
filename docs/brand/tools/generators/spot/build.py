import sys
from lib import *

TX, TY = 60, 260
COLX = 650


def N(expr, tx=TX, ty=TY):
    return nekko(expr, tx, ty)


def hardhat(cx=335, base=300, fill=C, rib=W):
    dome = path(f'M{cx-80} {base}C{cx-80} {base-68} {cx-43} {base-95} {cx} {base-95}C{cx+43} {base-95} {cx+80} {base-68} {cx+80} {base}Z', fill, fill, 12)
    ribs = rect(cx - 12, base - 95, 24, 90, 0, rib)
    brim = rect(cx - 103, base - 9, 206, 28, 14, fill)
    return dome + ribs + brim


def approve_stamp(cx, cy, r=58):
    return circle(cx, cy, r, C) + tick(cx, cy + 2, 1.0, W, 20)


def clipboard(cx, cy, rot=-6):
    inner = rect(-85, -125, 170, 250, 18, C)
    inner += rect(-66, -96, 132, 205, 8, W)
    inner += rect(-38, -142, 76, 40, 12, J)
    inner += rect(-40, -78, 80, 12, 6, C)
    for i, yy in enumerate((-32, 20, 72)):
        inner += rect(-52, yy - 14, 28, 28, 7, 'none', C, SD)
        if i < 2:
            inner += path(f'M-47 {yy}L-40 {yy+8}L-30 {yy-8}', 'none', C, SD)
        inner += line(-8, yy, 50, yy, C, SD)
    return g(inner, at(cx, cy, rot))


def megaphone(cx, cy, rot=-22):
    inner = rect(-20, -28, 56, 56, 12, W, C, SW)
    inner += path('M30 -30L116 -66Q130 -66 130 -52V52Q130 66 116 66L30 30Z', C, C, 10)
    inner += rect(-6, 28, 28, 46, 8, J)
    inner += path('M152 -34A48 48 0 0 1 152 34', 'none', C, SD)
    inner += path('M172 -62A84 84 0 0 1 172 62', 'none', C, SD)
    return g(inner, at(cx, cy, rot))


def notice_sheet(cx, cy, rot=5):
    inner = rect(-78, -102, 156, 204, 14, W, C, SW)
    inner += rect(-52, -76, 104, 24, 6, C)
    for yy in (-26, -2):
        inner += line(-50, yy, 50, yy, C, SD)
    for r_ in range(2):
        for c_ in range(3):
            x0, y0 = -50 + c_ * 36, 24 + r_ * 34
            inner += rect(x0, y0, 28, 26, 5, C if (r_, c_) == (1, 1) else 'none', C, 6)
    inner += circle(0, -102, 17, J) + circle(-4, -106, 4.5, W)
    return g(inner, at(cx, cy, rot))


def teacup(cx, by):
    inner = path('M-66 -92H66V-48Q66 0 0 0Q-66 0 -66 -48Z', W, C, SW)
    inner += path('M-44 -70H44', 'none', C, SD)
    inner += path('M-28 -118Q-40 -138 -28 -158Q-16 -178 -28 -198', 'none', C, SD)
    inner += path('M10 -118Q-2 -138 10 -158Q22 -178 10 -198', 'none', C, SD)
    inner += path('M48 -118Q36 -138 48 -158Q60 -178 48 -198', 'none', C, SD)
    return g(inner, at(cx, by + 6))


def house(cx, cy, sc=1.0):
    inner = path('M-62 -8V70H62V-8', W, C, SW)
    inner += path('M-86 -2L0 -84L86 -2', C, C, SW)
    inner += rect(-14, 26, 30, 44, 6, C)
    inner += rect(26, 4, 26, 26, 4, 'none', C, 8)
    return g(inner, at(cx, cy, 0, sc))


PX, PY, PW, PH = 520, 96, 220, 360


def panel(state):
    fill = J if state == 'locked' else C
    return rect(PX, PY, PW, PH, 36, fill)


def check_card():
    out = rect(PX, PY, PW, PH, 36, W, C, SW)
    out += circle(PX + 62, PY + 78, 38, C)
    out += path(f'M{PX+50} {PY+68}A12 12 0 1 1 {PX+68} {PY+80}Q{PX+62} {PY+85} {PX+62} {PY+92}', 'none', W, 10)
    out += circle(PX + 62, PY + 106, 6, W)
    out += line(PX + 112, PY + 70, PX + 188, PY + 70, C, SD) + line(PX + 112, PY + 92, PX + 160, PY + 92, C, SD)
    for i in range(3):
        y = PY + 142 + i * 62
        if i == 1:
            out += rect(PX + 26, y, PW - 52, 44, 22, C)
            out += tick(PX + 52, y + 22, 0.42, W, 10)
            out += line(PX + 84, y + 22, PX + PW - 52, y + 22, W, SD)
        else:
            out += rect(PX + 26, y, PW - 52, 44, 22, 'none', C, SD)
            out += circle(PX + 50, y + 22, 8, 'none', C, 6)
            out += line(PX + 84, y + 22, PX + PW - 52, y + 22, C, SD)
    return out


def ledge_dots(done, total=4, cx=400, y=732, r=14, gap=52):
    out = ''
    x0 = cx - gap * (total - 1) / 2
    for i in range(total):
        x = x0 + gap * i
        if i < done:
            out += circle(x, y, r, W)
        else:
            out += circle(x, y, r - 3, 'none', W, 6)
    return out


scenes = {}
scenes['role-worker'] = scene('role-worker', nek=N('happy') + hardhat(),
    under=gear(COLX, 230, 70, 8) + gear(578, 345, 38, 8))
scenes['role-foreman'] = scene('role-foreman', nek=N('neutral'), under=approve_stamp(COLX, 220), over=clipboard(COLX, 575))
scenes['role-poster'] = scene('role-poster', nek=N('happy'), under=notice_sheet(COLX, 235), over=megaphone(520, 590, -20))
scenes['role-senpai'] = scene('role-senpai', nek=N('happy'), under=house(COLX, 215, 1.05), over=teacup(COLX, 704))

lock_closed = padlock(PX + PW / 2, PY + 112, False, W, J, 0.62)
qr_svg, qrs = qr(PX + (PW - 163) / 2, PY + 168, 13, J, W, 10, 12)
scenes['step-1-scan-qr'] = scene('step-1-scan-qr', nek=N('neutral'), over=panel('locked') + lock_closed + qr_svg + ledge_dots(1))
badge = circle(PX + PW - 26, PY + 4, 40, J) + padlock(PX + PW - 26, PY + 6, False, W, J, 0.36)
scenes['step-2-take-the-check'] = scene('step-2-take-the-check', nek=N('neutral'), over=check_card() + badge + ledge_dots(2))
open_lock = padlock(PX + PW / 2, PY + 190, True, W, C, 0.95)
scenes['step-3-machine-unlocked'] = scene('step-3-machine-unlocked', nek=N('happy'), over=panel('open') + open_lock + ledge_dots(3))
scenes['step-4-ready'] = scene('step-4-ready', nek=N('happy') + hardhat(), over=panel('ready') + tick(PX + PW / 2, PY + PH / 2, 2.2, W, 30) + ledge_dots(4))


def tray(cx, by):
    """empty tray resting on the ledge; by = ledge top."""
    inner = path('M-100 -60L-70 -118H70L100 -60V0H-100Z', W, C, SW)
    inner += path('M-100 -60H100', 'none', C, SW)
    inner += path('M-60 -34H60', 'none', C, SD)
    return g(inner, at(cx, by + 4))


def dots_up(x, y):
    return circle(x, y, 9, C) + circle(x + 30, y - 40, 14, C) + circle(x + 70, y - 88, 20, C)


def magnifier(cx, cy):
    inner = path('M44 44L108 108', 'none', J, 26)
    inner += circle(0, 0, 70, W, C, SW)
    inner += line(-28, 0, 28, 0, C, 14)
    return g(inner, at(cx, cy, 0))


def wifi(cx, cy, slash=True, sc=0.8):
    inner = path('M-110 -30A156 156 0 0 1 110 -30', 'none', C, 22)
    inner += path('M-70 12A96 96 0 0 1 70 12', 'none', C, 22)
    inner += path('M-30 52A40 40 0 0 1 30 52', 'none', C, 22)
    inner += circle(0, 92, 17, C)
    if slash:
        inner += path('M-98 -62L98 130', 'none', W, 44)
        inner += path('M-98 -62L98 130', 'none', BAD, 22)
    return g(inner, at(cx, cy, 0, sc))


def broken_link(cx, cy):
    a = rect(-48, -26, 96, 52, 26, 'none', C, SW)
    a += rect(-14, -26, 62, 52, 26, 'none', C, SW) if False else ''
    left = g(rect(-96, -28, 104, 56, 28, 'none', C, SW), at(-18, -4, -32))
    right = g(rect(-8, -28, 104, 56, 28, 'none', C, SW), at(18, 4, -32))
    crack = path('M-8 -18L6 -2L-8 12L6 28', 'none', BAD, SD)
    sparks = line(-86, -60, -70, -44, BAD, SD) + line(-100, -24, -76, -24, BAD, SD) + line(84, 56, 68, 42, BAD, SD) + line(100, 20, 76, 22, BAD, SD)
    return g(left + right + sparks, at(cx, cy, 0, 1.05))


def warn_triangle(cx, cy, sc=0.9):
    inner = path('M0 -108L104 70H-104Z', BAD, BAD, 30)
    inner += rect(-11, -40, 22, 66, 11, W) + circle(0, 52, 13, W)
    return g(inner, at(cx, cy, 0, sc))


def success_badge(cx, cy):
    inner = circle(0, 0, 100, OK)
    inner += tick(0, 4, 1.6, W, 26)
    return g(inner, at(cx, cy))


scenes['state-empty'] = scene('state-empty', nek=N('sleepy'), under=dots_up(520, 380), over=tray(625, 704))
scenes['state-no-results'] = scene('state-no-results', nek=N('worried'), under=magnifier(590, 280))
scenes['state-offline'] = scene('state-offline', nek=N('worried'), under=wifi(640, 250, True, 0.95))
scenes['state-404'] = scene('state-404', nek=N('worried'), under=broken_link(645, 300))
scenes['state-error'] = scene('state-error', nek=N('error'), under=warn_triangle(630, 290))
scenes['state-success'] = scene('state-success', nek=N('happy'), under=success_badge(630, 260) + sparkle(540, 120, 22) + sparkle(740, 380, 16) + sparkle(560, 400, 12) + circle(720, 100, 8, C))


def main():
    only = sys.argv[1:]
    OUT.mkdir(parents=True, exist_ok=True)
    for stem, svg in scenes.items():
        if only and stem not in only:
            continue
        (OUT / f'{stem}.svg').write_text(svg, encoding='utf-8')
    print('wrote', len(only) or len(scenes))


main()
