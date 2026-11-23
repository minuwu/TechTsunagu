from lib import *

CRUSH = ('<polygon points="32,5 60,57 4,57" fill="#F9A800" stroke="#141414" stroke-width="4" stroke-linejoin="miter"/>'
         '<circle cx="23" cy="36" r="7" fill="none" stroke="#141414" stroke-width="3"/>'
         '<circle cx="41" cy="36" r="7" fill="none" stroke="#141414" stroke-width="3"/>'
         '<polygon points="30,18 34,18 34,28 30,28" fill="#141414"/>'
         '<polygon points="29,28 35,28 33,40 31,40" fill="#141414"/>'
         '<polygon points="30,28 26,26 27,29 30,31" fill="#141414"/>')


def machine(ox, oy, k):
    """Front-view press. Local frame: x 0..500, y 0..790 (plinth bottom)."""
    o = [f'<g id="machine" transform="translate({f(ox)} {f(oy)}) scale({k})">']
    o.append(rect(20, 0, 460, 160, JET))  # crown
    o.append(rect(20, 150, 90, 512, JET))  # left upright
    o.append(rect(390, 150, 90, 512, JET))  # right upright
    o.append(rect(0, 648, 500, 142, JET))  # plinth
    o.append(rect(100, 150, 300, 94, JET))  # ram block
    o.append(rect(220, 240, 60, 64, JET))  # ram rod
    o.append(rect(180, 298, 140, 32, JET))  # die
    o.append(rect(180, 618, 140, 32, JET))  # bolster
    o.append(f'<g id="warn-crush-hand" transform="translate(44 24) scale(1.72)">{CRUSH}</g>')
    o.append(rect(400, 36, 56, 56, COB))  # lamp
    o.append(padlock(250, 468, 200, 136, 32, 40, fill=COB, hole=CREAM))
    o.append(qr(40, 676, 90, seed=11))
    o.append('</g>')
    return '\n'.join(o)


NL = chr(10)


def desktop():
    o = [ground(1920, 1080, 160)]
    o.append(machine(905, 880 - 790 * 0.9, 0.9))
    o.append(nekko(1617, 900, 0.56, 'neutral'))
    o.append(rect(880, 880, 1040, 200, COB))
    o.append(tick(1640, 385, 130, 270, 54, gid='tick'))
    return svg(1920, 1080, 'Machine gate', NL.join(o))


def mobile():
    o = [ground(1080, 1350, 180)]
    o.append(machine(40, 1230 - 790 * 0.74, 0.74))
    o.append(nekko(744, 1270, 0.6, 'neutral'))
    o.append(rect(0, 1230, 1080, 120, COB))
    o.append(tick(820, 745, 100, 215, 44, gid='tick'))
    return svg(1080, 1350, 'Machine gate', NL.join(o))
