from lib import *


def passbox(x, y, s):
    tk, _ = tick(x + s * 0.2, y + s * 0.26, s * 0.6, COB, t=0.28)
    return rect(x, y, s, s, WH) + tk


def desktop():
    W, H = 1920, 1080
    b = [grid(W, H)]
    b.append(rect(960, 0, 960, 860, JET))
    for k in (7, 8, 9, 10, 11):
        b.append(rect(k * 160 - 1, 0, 2, 860, WH, 0.10))
    size = 240
    xs = (1040, 1340, 1640)
    for x, n in zip(xs, ('helmet', 'forklift', 'exit')):
        b.append(safety(n, x, 100, size))
        b.append(passbox(x + (size - 64) / 2, 396, 64))
    # understood block
    b.append(rect(1010, 560, 250, 300, WH))
    tk, _ = tick(1050, 610, 170, COB, t=0.24)
    b.append(tk + rect(1010, 560, 250, 14, COB))
    b.append(nekko_bottom('happy', 1290, 1000, 0.6))
    b.append(rect(960, 860, 960, 220, COB))
    return svg(W, H, ''.join(b))


def mobile():
    W, H = 1080, 1350
    b = [grid(W, H, 180)]
    b.append(rect(0, 570, W, 620, JET))
    for k in range(1, 6):
        b.append(rect(k * 180 - 1, 570, 2, 620, WH, 0.10))
    size = 290
    for x, n in zip((40, 395, 750), ('helmet', 'forklift', 'exit')):
        b.append(safety(n, x, 600, size))
        b.append(passbox(x + (size - 52) / 2, 898, 52))
    b.append(rect(60, 985, 240, 205, WH))
    tk, _ = tick(100, 1012, 150, COB, t=0.24)
    b.append(tk + rect(60, 985, 240, 14, COB))
    b.append(nekko_bottom('happy', 580, 1310, 0.4))
    b.append(rect(0, 1190, W, 160, COB))
    return svg(W, H, ''.join(b))
