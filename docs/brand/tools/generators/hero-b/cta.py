from lib import *


def base(W, H, step):
    b = [rect(0, 0, W, H, COB)]
    k = 1
    while k * step < W:
        b.append(rect(k * step - 1, 0, 2, H, WH, 0.10))
        k += 1
    return b


def desktop():
    W, H = 1920, 480
    b = base(W, H, 160)
    b.append(rect(1760, 0, 160, H, JET))
    tk, _ = tick(1190, 150, 150, CREAM, t=0.24)
    b.append(tk)
    b.append(nekko_bottom('happy', 1390, 480, 0.5))
    return svg(W, H, ''.join(b))


def mobile():
    W, H = 1080, 600
    b = base(W, H, 180)
    b.append(rect(900, 0, 180, H, JET))
    tk, _ = tick(760, 70, 120, CREAM, t=0.24)
    b.append(tk)
    b.append(nekko_bottom('happy', 590, 600, 0.5))
    return svg(W, H, ''.join(b))
