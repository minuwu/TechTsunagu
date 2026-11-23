"""Build every app-icon SVG from the Nekko master rig. Run with the brand-tools venv python."""
import math
import re
from pathlib import Path

REPO = Path(r'c:\Users\minha\Desktop\TechTsunagu')
M = REPO / 'frontend/public/brand/master'
OUT = REPO / 'frontend/public/brand/icons'
OUT.mkdir(parents=True, exist_ok=True)
CO, WH, JET, CREAM = '#1351AA', '#FFFFFF', '#141414', '#E3E2DE'


def group(svg, gid):
    i = svg.index(f'<g id="{gid}"')
    j = svg.index('>', i) + 1
    depth, k = 1, j
    while depth:
        a, b = svg.find('<g', k), svg.find('</g>', k)
        if a != -1 and a < b:
            depth += 1
            k = a + 2
        else:
            depth -= 1
            k = b + 4
    return svg[j:k - 4]


def paths(block):
    return re.findall(r'<path[^>]*/>', block)


def top_groups(svg):
    out, k = [], 0
    while True:
        i = svg.find('<g', k)
        if i == -1:
            return out
        j = svg.index('>', i) + 1
        depth, k = 1, j
        while depth:
            a, b = svg.find('<g', k), svg.find('</g>', k)
            if a != -1 and a < b:
                depth += 1
                k = a + 2
            else:
                depth -= 1
                k = b + 4
        out.append(svg[j:k - 4])


def eyes_of(n):
    return top_groups(EXPR[n])[5]


def mouth_of(n):
    return top_groups(EXPR[n])[7]


rig = (M / 'nekko-rig.svg').read_text(encoding='utf-8')
EXPR = {n: (M / f'nekko-expressions/nekko-{n}.svg').read_text(encoding='utf-8') for n in ('neutral', 'happy', 'sleepy', 'worried')}


def d_of(g):
    return re.search(r' d="([^"]+)"', paths(group(rig, g))[0]).group(1)


OUTLINE, SIL = d_of('outline'), d_of('silhouette')
ARM, HAND, BROW, NOSE_RAW, MOUTH_RAW = (group(rig, g) for g in ('arm-line', 'hand', 'brow', 'nose', 'mouth'))
WL, WR1, WR2 = (paths(group(rig, g)) for g in ('whisker-l', 'whisker-r1', 'whisker-r2'))
WH_IN = [WL[0], WR1[0], WR2[0]]
WH_OUT = [WL[1], WR1[1], WR2[1]]


def col(s, c):
    return s.replace(CO, c)


def nekko(body=WH, line=CO, wout=None, outline=None, ext=0, expr='neutral', whin=None):
    """Nekko in standalone coordinates (986x860). ext = white body extension below the flat cut."""
    wout, whin = wout or line, whin or line
    s = ''
    if outline:
        s += f'<path d="{OUTLINE}" fill="{outline}"/>'
    s += f'<path d="{SIL}" fill="{body}"/>'
    if ext:
        s += f'<rect x="166" y="855" width="582" height="{ext}" fill="{body}"/>'
    e = EXPR[expr]
    s += col(ARM + HAND + BROW + NOSE_RAW + eyes_of(expr) + mouth_of(expr), line)
    s += col(''.join(WH_IN), whin) + col(''.join(WH_OUT), wout)
    return s


def tf(s, tx, ty):
    return f'translate({tx:g} {ty:g}) scale({s:g})'


def win(C, x0, y0, W):
    s = C / W
    return tf(s, -x0 * s, -y0 * s)


def svg(C, body, title=None, h=None):
    h = h or C
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {C} {h}" width="{C}" height="{h}">'
            f'{body}</svg>\n')


def save(stem, text):
    (OUT / f'{stem}.svg').write_text(text, encoding='utf-8')


P0 = tf(1, 31, 164)  # master placement on the 1024 canvas
BG = lambda C, c: f'<rect width="{C}" height="{C}" fill="{c}"/>'


def on_blue(C, T, ground=CO, **kw):
    return BG(C, ground) + f'<g transform="{T}">' + nekko(wout=WH, **kw) + '</g>'


# ---- adaptive geometry (432 canvas): content centre (493,330) -> (216,216), s .26
S_AD = 0.245
T_AD = tf(S_AD, 216 - 493 * S_AD, 216 - 330 * S_AD)
S_MK = 0.385
T_MK = tf(S_MK, 256 - 493 * S_MK, 256 - 330 * S_MK)
S_CI = 0.50
T_CI = tf(S_CI, 256 - 493 * S_CI, 256 - 330 * S_CI + 6)

# ---- required set
save('app-icon-ios', svg(1024, on_blue(1024, P0)))
save('app-icon-android-adaptive-foreground',
     svg(432, f'<g transform="{T_AD}">' + nekko(wout=WH, ext=900) + '</g>'))
save('app-icon-android-adaptive-background', svg(432, BG(432, CO)))
save('app-icon-android-playstore', svg(512, on_blue(512, tf(0.5, 15.5, 82))))
save('app-icon-maskable', svg(512, on_blue(512, T_MK, ext=900)))
save('app-icon-circle',
     svg(512, '<defs><clipPath id="c"><circle cx="256" cy="256" r="256"/></clipPath></defs>'
         '<g clip-path="url(#c)">' + on_blue(512, T_CI, ext=900) + '</g>'))
save('app-icon-squircle',
     svg(512, '<defs><clipPath id="c"><rect width="512" height="512" rx="114.7"/></clipPath></defs>'
         '<g clip-path="url(#c)">' + on_blue(512, tf(0.5, 15.5, 82)) + '</g>'))


def mono(C, T, colr, ext=0, mid='m'):
    """Single colour: silhouette + outer whiskers filled, every linework part knocked out (mask)."""
    holes = '<g transform="%s">' % T + col(ARM + HAND + BROW + NOSE_RAW + eyes_of('neutral')
                                          + mouth_of('neutral') + ''.join(WH_IN), '#000000') + '</g>'
    mask = (f'<mask id="{mid}" maskUnits="userSpaceOnUse" x="0" y="0" width="{C}" height="{C}">'
            f'<rect width="{C}" height="{C}" fill="#fff"/>{holes}</mask>')
    fill = f'<path d="{SIL}" fill="{colr}"/>'
    if ext:
        fill += f'<rect x="166" y="855" width="582" height="{ext}" fill="{colr}"/>'
    fill += col(''.join(WH_OUT), colr)
    return f'<defs>{mask}</defs><g mask="url(#{mid})"><g transform="{T}">{fill}</g></g>'


save('app-icon-android-adaptive-monochrome', svg(432, mono(432, T_AD, '#000000', ext=900)))
save('app-icon-mono-white', svg(1024, mono(1024, P0, WH)))
save('app-icon-mono-black', svg(1024, mono(1024, P0, JET)))

save('app-icon-dark', svg(1024, BG(1024, JET) + f'<g transform="{P0}">' + nekko(outline=CO, wout=WH) + '</g>'))
save('app-icon-light', svg(1024, BG(1024, CREAM) + f'<g transform="{P0}">' + nekko(outline=CO) + '</g>'))
save('app-icon-tinted', svg(1024, BG(1024, JET) + f'<g transform="{P0}">' + nekko(line=JET, wout=WH) + '</g>'))

# ---- close-ups
face_clip = '<clipPath id="h"><path d="M205 -200H905V265A337.5 215 0 0 1 205 265Z"/></clipPath>'
save('app-icon-face-closeup',
     svg(1024, BG(1024, CO) + f'<defs>{face_clip}</defs><g transform="{win(1024, 150, -135, 800)}"><g clip-path="url(#h)">'
         + nekko(wout=WH, ext=900) + '</g></g>'))
save('app-icon-thumb-closeup',
     svg(1024, BG(1024, CO) + f'<g transform="{win(1024, -14, 255, 350)}">' + nekko(wout=WH, ext=900) + '</g>'))

# outline only: ring = outline minus silhouette, white lines on cobalt, no white fill
ring = f'<path d="{OUTLINE} {SIL}" fill="{WH}" fill-rule="evenodd"/>'
lines = col(ARM + HAND + BROW + NOSE_RAW + eyes_of('neutral') + mouth_of('neutral')
            + ''.join(WH_IN) + ''.join(WH_OUT), WH)
save('app-icon-outline-only', svg(1024, BG(1024, CO) + f'<g transform="{P0}">{ring}{lines}</g>'))


# ---- badge question
def qbadge(cx, cy, r):
    k = r / 145
    return (f'<g transform="translate({cx} {cy}) scale({k:g})"><circle r="145" fill="{WH}"/>'
            f'<path d="M-46 -36C-46 -86 -10 -104 22 -102C60 -100 84 -76 82 -42C80 -10 54 6 32 22C14 36 10 48 10 62"'
            f' fill="none" stroke="{CO}" stroke-width="36" stroke-linecap="round" stroke-linejoin="round"/>'
            f'<circle cx="10" cy="106" r="21" fill="{CO}"/></g>')


T_B = tf(0.8, 118, 336 + 0)
save('app-icon-badge-question', svg(1024, on_blue(1024, T_B) + qbadge(830, 200, 150)))

# ---- sticker (die-cut white border by wide round-join strokes behind the art)
border = (f'<g fill="{WH}" stroke="{WH}" stroke-width="70" stroke-linejoin="round" stroke-linecap="round">'
          f'<path d="{OUTLINE}"/>' + ''.join(col(p, WH).replace('fill="#FFFFFF"', '') for p in WH_OUT) + '</g>')
bottom = f'<rect x="130" y="840" width="652" height="40" fill="#FFFFFF"/><rect x="130" y="826" width="652" height="34" fill="{CO}"/>'
save('app-icon-sticker',
     svg(1024, f'<g transform="{tf(0.95, 43, 60)}">{border}{bottom}{nekko(outline=CO)}</g>'))


# ---- seasonal
def blossom(cx, cy, r, rot, col_, dot=WH):
    pet = (f'M0 0C{-.55*r:g} {-.35*r:g} {-.5*r:g} {-.95*r:g} {-.12*r:g} {-r:g}L0 {-.84*r:g}'
           f'L{.12*r:g} {-r:g}C{.5*r:g} {-.95*r:g} {.55*r:g} {-.35*r:g} 0 0Z')
    s = f'<g transform="translate({cx} {cy}) rotate({rot})" fill="{col_}">'
    s += ''.join(f'<path d="{pet}" transform="rotate({72*i})"/>' for i in range(5))
    s += f'<circle r="{.17*r:g}" fill="{dot}"/></g>'
    return s


PINK = '#F29BB5'
save('app-icon-seasonal-sakura',
     svg(1024, on_blue(1024, P0)
         + blossom(808, 268, 78, 10, PINK) + blossom(150, 190, 66, -20, PINK) + blossom(905, 700, 50, 30, PINK)
         + blossom(120, 760, 40, 8, PINK)
         + f'<g fill="{PINK}">' + ''.join(
             f'<path transform="translate({x} {y}) rotate({r})" d="M0 0C-16 -14 -14 -40 -3 -48L0 -40L3 -48C14 -40 16 -14 0 0Z"/>'
             for x, y, r in ((300, 90, 30), (560, 120, -25), (940, 330, 60), (70, 520, -40), (950, 880, 15))) + '</g>'))

RED = '#E0412F'


def lantern(cx, top, w, h):
    rx = w / 2
    s = f'<g transform="translate({cx} {top})"><path d="M0 -150V0" stroke="{WH}" stroke-width="8" fill="none"/>'
    s += f'<rect x="{-rx*.45:g}" y="-6" width="{rx*.9:g}" height="22" rx="4" fill="{JET}"/>'
    s += f'<ellipse cx="0" cy="{h/2+10:g}" rx="{rx:g}" ry="{h/2:g}" fill="{RED}"/>'
    s += f'<path d="M{-rx*.6:g} {h*.15+10:g}H{rx*.6:g}M{-rx*.9:g} {h/2+10:g}H{rx*.9:g}M{-rx*.6:g} {h*.85+10:g}H{rx*.6:g}"' \
         f' stroke="{WH}" stroke-width="10" stroke-linecap="round" fill="none"/>'
    s += f'<rect x="{-rx*.45:g}" y="{h+4:g}" width="{rx*.9:g}" height="22" rx="4" fill="{JET}"/>'
    s += f'<path d="M0 {h+26:g}V{h+70:g}" stroke="{WH}" stroke-width="8" stroke-linecap="round"/></g>'
    return s


def burst(cx, cy, r0, r1, n, c, w=14):
    s = f'<g stroke="{c}" stroke-width="{w}" stroke-linecap="round">'
    for i in range(n):
        a = 2 * math.pi * i / n
        s += f'<path d="M{cx + r0*math.cos(a):.1f} {cy + r0*math.sin(a):.1f}L{cx + r1*math.cos(a):.1f} {cy + r1*math.sin(a):.1f}"/>'
    return s + '</g>'


save('app-icon-seasonal-matsuri',
     svg(1024, on_blue(1024, P0) + lantern(140, 150, 118, 150)
         + burst(880, 150, 36, 92, 10, WH, 14) + burst(880, 150, 8, 22, 6, RED, 14)
         + f'<circle cx="880" cy="150" r="16" fill="{RED}"/>'
         + f'<circle cx="760" cy="60" r="12" fill="{WH}"/><circle cx="990" cy="250" r="10" fill="{WH}"/>'
         + lantern(930, 560, 70, 90).replace('M0 -150V0', 'M0 -560V0')))


def flake(cx, cy, r, c=WH, w=9):
    s = f'<g transform="translate({cx} {cy})" stroke="{c}" stroke-width="{w}" stroke-linecap="round" fill="none">'
    for i in range(6):
        s += (f'<g transform="rotate({60*i})"><path d="M0 0V{-r}"/><path d="M0 {-r*.55:g}L{r*.22:g} {-r*.78:g}'
              f'M0 {-r*.55:g}L{-r*.22:g} {-r*.78:g}"/></g>')
    return s + '</g>'


SNOW = '#D8372F'
hat = (f'<g transform="translate(535 118) rotate(-7)" stroke="{CO}" stroke-width="12" stroke-linejoin="round">'
       f'<path d="M-95 6C-90 -70 -30 -125 30 -135C60 -100 95 -50 100 6Z" fill="{SNOW}"/>'
       f'<rect x="-112" y="-14" width="226" height="40" rx="20" fill="{SNOW}"/>'
       f'<circle cx="34" cy="-142" r="26" fill="{WH}"/></g>')
save('app-icon-seasonal-snow',
     svg(1024, on_blue(1024, P0) + f'<g transform="{P0}">{hat}</g>'
         + flake(150, 190, 52) + flake(900, 160, 40) + flake(960, 610, 46) + flake(90, 800, 36)
         + ''.join(f'<circle cx="{x}" cy="{y}" r="{r}" fill="{WH}"/>' for x, y, r in
                   ((300, 120, 9), (640, 80, 8), (60, 450, 8), (990, 430, 9), (930, 880, 10), (230, 930, 8)))))

# ---- alt variations
# hard hat (jet shell, white ridge)
helmet = (f'<g transform="translate(540 120) rotate(-6)">'
          f'<path d="M-150 16C-150 -100 -60 -150 0 -150C70 -150 150 -100 150 16Z" fill="{JET}"/>'
          f'<path d="M-24 -150H24V16H-24Z" fill="{WH}" opacity="0.9"/>'
          f'<rect x="-178" y="2" width="356" height="34" rx="17" fill="{JET}"/>'
          f'<path d="M-118 -30C-104 -70 -78 -96 -48 -110" fill="none" stroke="{WH}" stroke-width="10" stroke-linecap="round"/></g>')
save('app-icon-alt-hardhat', svg(1024, on_blue(1024, P0) + f'<g transform="{P0}">{helmet}</g>'))

# speech bubble: cream ground, cobalt bubble, Nekko clipped inside
bub = 'M230 70H794A150 150 0 0 1 944 220V670A150 150 0 0 1 794 820H330L170 960L210 820H230A150 150 0 0 1 80 670V220A150 150 0 0 1 230 70Z'
bub_rect = '<rect x="80" y="70" width="864" height="750" rx="150"/>'
save('app-icon-alt-speech-bubble',
     svg(1024, BG(1024, CREAM) + f'<path d="{bub}" fill="{CO}"/>'
         f'<defs><clipPath id="b">{bub_rect}</clipPath></defs>'
         f'<g clip-path="url(#b)"><g transform="{tf(0.8, 118, 132)}">' + nekko(wout=WH, ext=60) + '</g></g>'))

# Megane Bridge: jet bridge with two round arches (+ reflections) under the bust
bridge = ('M0 1024V800H80V760H452V800H572V760H944V800H1024V1024Z')
save('app-icon-alt-megane-bridge',
     svg(1024, BG(1024, CO) + f'<g transform="{tf(0.85, 93, 69)}">' + nekko(wout=WH, ext=300) + '</g>'
         + f'<path d="{bridge}" fill="{JET}"/>'
         + ''.join(f'<path d="M{x-104} 898A104 104 0 0 1 {x+104} 898Z" fill="{CO}"/>'
                   f'<path d="M{x-104} 898A104 104 0 0 0 {x+104} 898Z" fill="{CREAM}"/>' for x in (266, 758))))

# understood tick badge, happy face
def tick_badge(cx, cy, r):
    k = r / 145
    return (f'<g transform="translate({cx} {cy}) scale({k:g})"><circle r="145" fill="{WH}"/>'
            f'<path d="M-62 4L-16 52L66 -52" fill="none" stroke="{CO}" stroke-width="40" stroke-linecap="round" stroke-linejoin="round"/></g>')


save('app-icon-alt-understood', svg(1024, on_blue(1024, T_B, expr='happy') + tick_badge(830, 200, 150)))
print('ok')
