"""Wordmark geometry. Latin: Aileron Heavy (CC0 / no rights reserved). Kana: Noto Sans JP Black (OFL 1.1). Both outlined."""
from fp import *
SC = 0.1                      # Aileron font unit -> lockup unit (cap height 1413 -> 141.3)
Q = 1505 / 1442             # brand "?" scale: its top meets the k ascender line
ADV = lambda f, ch: f['hmtx'][gname(f, ch)][0]

# font-unit adjustments applied after the left glyph's advance
KERN = {('W','a'): -95, ('a','k'): -12, ('k','a'): -28, ('a','t'): -16, ("t","t"): -63, ('t','a'): -14}
Q_GAP = 28                    # a -> ? (in font units, relative to a's advance, before ? left bearing)

def latin(sc=SC, q=Q, kern=KERN, qgap=Q_GAP):
    chars = 'Wakatta?'
    pos = []; x = 0
    for i, ch in enumerate(chars):
        if ch == '?':
            lb = bounds(AIL, '?')[0] * q
            pos.append(x + qgap - lb + bounds(AIL,'a')[0]*0)  # place by ink: left ink edge sits at x+qgap
            break
        pos.append(x)
        nxt = chars[i+1]
        x += ADV(AIL, ch) + (kern.get((ch, nxt), 0) if nxt != '?' else 0)
    # ink extents (font units, y up)
    left = bounds(AIL, 'W')[0]
    qb = bounds(AIL, '?')
    right = pos[-1] + qb[2] * q
    top = max(bounds(AIL, c)[3] for c in 'Wakta'); top = max(top, qb[3] * q)
    bot = min(bounds(AIL, c)[1] for c in 'Wakta'); bot = min(bot, qb[1] * q)
    w = (right - left) * sc; h = (top - bot) * sc
    base = top * sc           # baseline y from top of the box
    items = []
    for ch, px in zip(chars, pos):
        if ch == '?':
            d = path(AIL, ch, sc * q, (px - left) * sc, base) if False else None
            gs = AIL.getGlyphSet()
            sp = SVGPathPen(gs, ntos=lambda v: ('%.2f' % v).rstrip('0').rstrip('.'))
            tp = TransformPen(sp, (sc*q, 0, 0, -sc*q, (px - left) * sc, base)); gs[gname(AIL,'?')].draw(tp)
            items.append((sp.getCommands(), 'q'))
        else:
            items.append((path(AIL, ch, sc, (px - left) * sc, base), 'ink'))
    return dict(w=w, h=h, base=base, items=items, cap=1413*sc, xh=530*sc)

def _qpath(sc, tx, ty):
    gs = AIL.getGlyphSet()
    sp = SVGPathPen(gs, ntos=lambda v: ('%.2f' % v).rstrip('0').rstrip('.'))
    tp = TransformPen(sp, (sc, 0, 0, -sc, tx, ty)); gs[gname(AIL,'?')].draw(tp)
    return sp.getCommands()

JA_EM = 100                   # noto em in lockup units
JA_KERN = {('わ','か'): -10, ('か','っ'): -25, ('っ','た'): -30}  # 1/1000 em
JA_Q_H = 0.86                 # "?" height as fraction of the noto em
def japanese(em=JA_EM, kern=JA_KERN, qgap=0.075, qh=JA_Q_H):
    chars = 'わかった'
    pos = []; x = 0
    for i, ch in enumerate(chars):
        pos.append(x)
        x += ADV(NOTO, ch) + (kern.get((ch, chars[i+1]), 0) if i+1 < len(chars) else 0)
    s = em / 1000
    left = min(bounds(NOTO, 'わ')[0], 0)
    left = bounds(NOTO, 'わ')[0]
    qb = bounds(AIL, '?'); qs = qh * 1000 * s / (qb[3]-qb[1])  # scale so ?-height = qh*em
    # right edge of last kana ink
    last_right = pos[-1] + bounds(NOTO, 'た')[2]
    q_left_ink = last_right + qgap * 1000
    top = max(bounds(NOTO, c)[3] for c in chars); bot = min(bounds(NOTO, c)[1] for c in chars)
    qtop = (qb[3]) * qs / s; qbot = qb[1] * qs / s     # in noto font units
    qoff = bot + 0 - qbot                               # ? bottom aligned to the kana bottom
    top = max(top, qtop + qoff)
    q_x = q_left_ink - qb[0] * qs / s
    right = q_x + qb[2] * qs / s
    w = (right - left) * s; h = (top - bot) * s; base = top * s
    items = [(path(NOTO, ch, s, (px - left) * s, base), 'ink') for ch, px in zip(chars, pos)]
    items.append((_qpath(qs, (q_x - left) * s, base + qoff * s * -1 * -1 if False else base - qoff * s), 'q'))
    return dict(w=w, h=h, base=base, items=items, em=em)
