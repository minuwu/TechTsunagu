from lib import *


def footer(stem, W, H, u, wl, x0, k, cx_n, nek_h, ghosts, flat=True):
    """Megane Bridge along the bottom, Nekko peeking over the bridge top."""
    b = [hl(0, 0, W, 2), hl(0, wl - 1, W, 2)]
    for g in ghosts:
        b.append(ring(g, wl, 8 * u))
    b.append(nekko(cx_n, wl, nek_h))
    b.append(bridge(x0, wl, u, k, refl=(COB,), flat=flat))
    return write(OUT_FOOTER, stem, W, H, ''.join(b))


# desktop 1920x320: u=8, 8 arches span x 208..1520, Nekko 256 tall (176 visible above the bridge)
footer('footer-strip', 1920, 320, 8, 272, 208, 8, 1100, 256, ghosts=[144, 1584, 1744, 1904])
# mobile 1080x420: u=9, R=72, pitch 180, arches at 640/820
footer('footer-strip-mobile', 1080, 420, 9, 348, 640 - 12 * 9, 2, 640 - 12 * 9 + 22 * 9, 262,
       ghosts=[100, 280, 460, 1000], flat=False)


def dividers():
    H = 120
    # divider-bridge: Megane pair repeated, waterline hairline through
    u, wl = 4, 64
    b = [hl(0, wl - 1, 1920)]
    for k in range(6):
        b.append(bridge(80 + 320 * k, wl, u, 2, refl=(COB,)))
    write(OUT_FOOTER, 'divider-bridge', 1920, H, ''.join(b))

    # divider-rule: hairline with ruler ticks, jet block with a tiny face peek, cobalt marker
    y = 72
    b = [hl(0, y - 1, 1920)]
    for x in range(80, 1841, 20):
        major = (x - 80) % 160 == 0
        if major:
            b.append(rect(x - 1, y + 1, 2, 28, JET))
        else:
            b.append(hl(x - 1, y + 1, 2, 12))
    b.append(rect(80, y - 4, 160, 8, COB))
    # face peek: block top at y; face frame scaled 0.8 -> eyes visible, mouth hidden
    cx = 1520
    b.append(face_mark(cx, y - 37, 51))
    b.append(rect(cx - 80, y - 1, 160, 14, JET))
    write(OUT_FOOTER, 'divider-rule', 1920, H, ''.join(b))

    # divider-steps: rising steps, then a low skyline of blocks
    base = 100
    b = []
    for i in range(12):
        h = 8 * (i + 1)
        b.append(rect(80 + 40 * i, base - h, 40, h, COB if i == 11 else JET))
    for k, x in enumerate(range(640, 1841, 160)):
        h = (24, 16, 40, 16, 24, 16, 32, 16)[k % 8]
        b.append(rect(x, base - h, 80, h, JET))
    b.append(hl(0, base, 1920))
    write(OUT_FOOTER, 'divider-steps', 1920, H, ''.join(b))


dividers()
print('ok')
