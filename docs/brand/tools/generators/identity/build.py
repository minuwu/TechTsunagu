import os, sys, json
from wm import latin, japanese
from nk import *
OUT = r"c:/Users/minha/Desktop/TechTsunagu/frontend/public/brand/identity/"
os.makedirs(OUT, exist_ok=True)
f2 = lambda v: ('%.2f' % v).rstrip('0').rstrip('.')

def svg(vb, body, title, extra=''):
    return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="{vb}"{extra} role="img"><title>{title}</title>{body}</svg>\n'

def word_body(wd, ink, q, dx=0, dy=0):
    g = ''.join(f'<path d="{d}" fill="{q if r=="q" else ink}"/>' for d, r in wd['items'])
    return f'<g transform="translate({f2(dx)} {f2(dy)})">{g}</g>' if (dx or dy) else f'<g>{g}</g>'

def nekko(mode, k, x, y):
    inn = STANDALONE if mode == 'color' else mono(mode)
    return f'<g transform="translate({f2(x)} {f2(y)}) scale({k:.5f})">{inn}</g>'

def colors(variant):
    return {'color': (JET, COB), 'white': (WHITE, WHITE), 'black': (JET, JET)}[variant]

LAT = latin(); JA = japanese()

def horizontal(wd, variant, hn_ratio, gap, align='base'):
    ink, q = colors(variant); nmode = 'color' if variant == 'color' else (WHITE if variant == 'white' else JET)
    hn = hn_ratio * wd['h']; k = hn / NH; nw = NW * k
    wx = nw + gap
    if align == 'base':
        wy = hn - wd['h']
    else:
        wy = (hn - wd['h']) / 2
    W = wx + wd['w']; H = max(hn, wy + wd['h'])
    body = nekko(nmode, k, 0, 0) + word_body(wd, ink, q, wx, wy)
    return f'0 0 {f2(W)} {f2(H)}', body

def stacked(wd, variant, nw_ratio, gap):
    ink, q = colors(variant); nmode = 'color' if variant == 'color' else (WHITE if variant == 'white' else JET)
    nwid = nw_ratio * wd['w']; k = nwid / NW; hn = NH * k
    W = wd['w']
    body = nekko(nmode, k, (W - nwid) / 2, 0) + word_body(wd, ink, q, 0, hn + gap)
    return f'0 0 {f2(W)} {f2(hn + gap + wd["h"])}', body

if __name__ == '__main__':
    cfg = dict(hn=1.9, gap=40, align='base')
    for a in ('base', 'mid'):
        vb, body = horizontal(LAT, 'color', 1.9, 42, a)
        open(f'tt-full-{a}.svg', 'w', encoding='utf-8').write(svg(vb, body, 'x'))
    vb, body = stacked(LAT, 'color', 0.5, 60)
    open('tt-stacked.svg', 'w', encoding='utf-8').write(svg(vb, body, 'x'))
    vb, body = horizontal(JA, 'color', 1.9, 36, 'base')
    open('tt-ja.svg', 'w', encoding='utf-8').write(svg(vb, body, 'x'))
    for v in ('white', 'black'):
        vb, body = horizontal(LAT, v, 1.9, 42, 'base')
        open(f'tt-{v}.svg', 'w', encoding='utf-8').write(svg(vb, body, 'x'))
