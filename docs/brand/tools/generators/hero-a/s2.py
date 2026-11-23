from lib import *

NL = chr(10)


def lines(x, y, w, specs, h, gap, fill):
    """Abstract text lines: specs = fractions of w."""
    return ''.join(rect(x, y + i * (h + gap), w * fr, h, fill) for i, fr in enumerate(specs))


def mic(x, y, k, fg, bg):
    return (f'<g id="mic" transform="translate({f(x)} {f(y)}) scale({k})">'
            + path('M24 36V18A8 8 0 0 1 40 18V36A8 8 0 0 1 24 36Z', fill=fg)
            + path('M16 30V34A16 16 0 0 0 48 34V30', stroke=fg, sw=4)
            + rect(30, 50, 4, 7, fg) + rect(21, 56, 22, 4, fg)
            + rect(27, 20, 10, 2.4, bg) + rect(27, 26, 10, 2.4, bg) + rect(27, 32, 10, 2.4, bg)
            + '</g>')


def blocks(ax, ay, bx, by, bw, bh, ah):
    """Block A jet (tail bottom-left) and block B cobalt (tail bottom-left), overlap white."""
    o = ['<g id="speech-blocks">']
    o.append(rect(ax, ay, bw, ah, JET))
    o.append(poly([(ax, ay + ah), (ax + 90, ay + ah), (ax, ay + ah + 80)], JET))
    o.append(rect(bx, by, bw, bh, COB))
    o.append(poly([(bx, by + bh), (bx + 90, by + bh), (bx, by + bh + 80)], COB))
    ox0, oy0 = bx, by
    ox1, oy1 = ax + bw, ay + ah
    o.append(rect(ox0, oy0, ox1 - ox0, oy1 - oy0, WHITE))
    o.append('</g>')
    return o, (ox0, oy0, ox1, oy1)


def desktop():
    o = [ground(1920, 1080, 160)]
    ax, ay, bw, ah = 960, 110, 600, 270
    bx, by, bh = 1260, 290, 250
    b, (ox0, oy0, ox1, oy1) = blocks(ax, ay, bx, by, bw, bh, ah)
    o += b
    o.append('<g id="lines-a">' + lines(ax + 50, ay + 50, 330, [1, 0.75, 0.9, 0.5], 16, 32, CREAM) + '</g>')
    o.append('<g id="lines-b">' + lines(bx + 270, by + 115, 270, [1, 0.8, 0.55], 16, 32, WHITE) + '</g>')
    o.append('<g id="lines-overlap">' + lines(ox0 + 24, oy0 + 24, ox1 - ox0 - 48, [1, 0.6], 14, 24, JET) + '</g>')
    # mic square
    o.append(rect(960, 700, 240, 240, COB))
    o.append(mic(1000, 735, 2.55, WHITE, COB))
    o.append(tick(1255, 985, 66, 140, 28, gid='tick'))
    o.append(nekko(1625, 1080, 0.53, 'happy'))
    return svg(1920, 1080, 'Ask in your language', NL.join(o))


def mobile():
    o = [ground(1080, 1350, 180)]
    ax, ay, bw, ah = 100, 570, 560, 220
    bx, by, bh = 380, 690, 210
    b, (ox0, oy0, ox1, oy1) = blocks(ax, ay, bx, by, bw, bh, ah)
    o += b
    o.append('<g id="lines-a">' + lines(ax + 40, ay + 40, 260, [1, 0.75, 0.9, 0.5], 14, 28, CREAM) + '</g>')
    o.append('<g id="lines-b">' + lines(bx + 300, by + 100, 220, [1, 0.8, 0.55], 14, 28, WHITE) + '</g>')
    o.append('<g id="lines-overlap">' + lines(ox0 + 20, oy0 + 20, ox1 - ox0 - 40, [1, 0.6], 12, 22, JET) + '</g>')
    o.append(rect(100, 1060, 220, 220, COB))
    o.append(mic(137, 1092, 2.3, WHITE, COB))
    o.append(tick(480, 1250, 60, 130, 26, gid='tick'))
    o.append(nekko(800, 1350, 0.46, 'happy'))
    return svg(1080, 1350, 'Ask in your language', NL.join(o))
