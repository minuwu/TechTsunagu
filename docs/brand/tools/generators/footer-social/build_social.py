from lib import *


def chain(cx_list, wl, r):
    return ''.join(ring(c, wl, r) for c in cx_list)


def vlines(xs, y0, y1):
    return ''.join(hl(x - 1, y0, 2, y1 - y0) for x in xs)


# ---------------------------------------------------------------- og-default 1200x630
def og_default():
    W, H = 1200, 630
    u, wl = 9, 558
    x0 = 502
    b = []
    b.append(vlines([100, 400, 700, 1000], 90, wl))
    b.append(hl(50, 90, 1100))
    b.append(quarter(1200, 0, 270, 'bl', COB))
    b.append(chain([250, 430, 970], wl, 8 * u))
    b.append(hl(0, wl - 1, W))
    b.append(tick(192, 430, 56, 130, 300))
    b.append(nekko(x0 + 198, wl, 430, 'happy'))
    b.append(bridge(x0, wl, u, 2))
    write(OUT_SOCIAL, 'og-default', W, H, ''.join(b))


# ---------------------------------------------------------------- og-square 1200x1200
def og_square():
    W, H = 1200, 1200
    u, wl = 12, 1104
    x0 = 356
    b = []
    b.append(vlines([200, 500, 800, 1100], 120, wl))
    b.append(hl(80, 120, 1040))
    b.append(quarter(1200, 0, 400, 'bl', COB))
    b.append(chain([260, 980], wl, 8 * u))
    b.append(hl(0, wl - 1, W))
    b.append(tick(214, 456, 84, 190, 420))
    b.append(nekko(x0 + 264, wl, 600, 'happy'))
    b.append(bridge(x0, wl, u, 2))
    write(OUT_SOCIAL, 'og-square', W, H, ''.join(b))


# ---------------------------------------------------------------- twitter-card 1200x600
def twitter_card():
    W, H = 1200, 600
    u, wl = 8, 536
    x0 = 274
    b = []
    b.append(quarter(0, 0, 250, 'br', JET))
    b.append(vlines([700, 860, 1020, 1180], 80, wl))
    b.append(chain([50, 210, 690, 850, 1010], wl, 8 * u))
    b.append(hl(0, wl - 1, W))
    b.append(tick(859, 410, 64, 140, 320))
    b.append(nekko(x0 + 176, wl, 400, 'happy'))
    b.append(bridge(x0, wl, u, 2))
    write(OUT_SOCIAL, 'twitter-card', W, H, ''.join(b))


# ---------------------------------------------------------------- line-share 1040x1040
def line_share():
    W, H = 1040, 1040
    u, wl = 10, 960
    x0 = 300
    b = []
    b.append(rect(640, 0, 400, 400, COB))
    for i in range(4):
        b.append(rect(80 + 80 * i, 0, 80, 120 + 80 * i, JET))
    b.append(chain([220, 820], wl, 8 * u))
    b.append(hl(0, wl - 1, W))
    b.append(nekko(x0 + 220, wl, 520, 'happy'))
    b.append(bridge(x0, wl, u, 2))
    write(OUT_SOCIAL, 'line-share', W, H, ''.join(b))


# ---------------------------------------------------------------- demo-cover 1920x1080
def demo_cover():
    W, H = 1920, 1080
    u, wl = 18, 936
    x0 = 1128
    cx = 1380
    b = []
    b.append(vlines([1080, 1240, 1400, 1560, 1720], 80, wl))
    b.append(hl(1080, 80, 840))
    b.append(quarter(1920, 0, 320, 'bl', COB))
    b.append(chain([264, 624, 984], wl, 8 * u))
    b.append(hl(0, wl - 1, W))
    b.append(tick(151, 330, 48, 100, 220))
    b.append(nekko(cx, wl, 700, 'happy'))
    b.append(bridge(cx - 396, wl, u, 2))
    write(OUT_SOCIAL, 'demo-cover', W, H, ''.join(b))


# ---------------------------------------------------------------- slide-bg 1920x1080
def slide_bg():
    W, H = 1920, 1080
    u, wl = 8, 1016
    b = []
    b.append(vlines([80 + 160 * k for k in range(12)], 80, 900))
    b.append(hl(80, 80, 1760))
    b.append(rect(80, 80, 160, 8, COB))
    b.append(chain([1040, 1520, 1680, 1840], wl, 8 * u))
    b.append(hl(0, wl - 1, W))
    b.append(bridge(1104, wl, u, 2))
    write(OUT_SOCIAL, 'slide-bg', W, H, ''.join(b))


og_default()
og_square()
twitter_card()
line_share()
demo_cover()
slide_bg()
print('ok')
