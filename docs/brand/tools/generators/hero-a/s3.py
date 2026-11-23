from lib import *

NL = chr(10)


def house(x, base, w, h, k=1.0):
    o = ['<g id="house">']
    o.append(rect(x, base - h, w, h, JET))
    o.append(poly([(x - 20, base - h), (x + w + 20, base - h), (x + w / 2, base - h - w * 0.45)], COB))
    ws = w * 0.34
    o.append(rect(x + w * 0.14, base - h + h * 0.2, ws, ws, WHITE))  # lit window
    o.append(rect(x + w * 0.14 + ws / 2 - 3, base - h + h * 0.2, 6, ws, JET))
    o.append(rect(x + w * 0.14, base - h + h * 0.2 + ws / 2 - 3, ws, 6, JET))
    o.append(rect(x + w * 0.58, base - h * 0.42, w * 0.28, h * 0.42, COB))  # door
    o.append('</g>')
    return o


def pair(cx, base, s):
    """Two figures flank a low table with one cup."""
    o = ['<g id="senpai-and-worker">']
    o.append(figure(cx - 58 * s, base, 56 * s, 150 * s, JET, JET, 24 * s))
    o.append(figure(cx + 58 * s, base, 56 * s, 130 * s, COB, COB, 24 * s))
    o.append(rect(cx - 22 * s, base - 62 * s, 44 * s, 8 * s, JET))  # table top
    o.append(rect(cx - 4 * s, base - 54 * s, 8 * s, 54 * s, JET))
    o.append(rect(cx - 18 * s, base - 106 * s, 36 * s, 44 * s, WHITE))  # cup
    o.append(rect(cx + 18 * s, base - 98 * s, 12 * s, 7 * s, WHITE))
    o.append(rect(cx + 25 * s, base - 98 * s, 5 * s, 26 * s, WHITE))
    o.append(rect(cx + 18 * s, base - 76 * s, 12 * s, 5 * s, WHITE))
    o.append('</g>')
    return o


def desktop():
    o = [ground(1920, 1080, 160)]
    o.append(nekko(1678, 860, 0.55, 'neutral'))
    o.append('<g id="megane-bridge">' + bridge(880, 1920, 760, 860, 965, 100, [1310, 1610], 1080) + '</g>')
    o += house(910, 860, 200, 330)
    o += pair(1310, 760, 0.85)
    o.append(tick(1560, 330, 110, 230, 46, gid='tick'))
    return svg(1920, 1080, 'Senpai next door', NL.join(o))


def mobile():
    o = [ground(1080, 1350, 180)]
    o.append(nekko(830, 1130, 0.5, 'neutral'))
    o.append('<g id="megane-bridge">' + bridge(0, 1080, 1030, 1130, 1240, 105, [250, 800], 1350) + '</g>')
    o += house(390, 1130, 170, 290)
    o += pair(250, 1030, 0.85)
    o.append(tick(900, 700, 70, 150, 30, gid='tick'))
    return svg(1080, 1350, 'Senpai next door', NL.join(o))
