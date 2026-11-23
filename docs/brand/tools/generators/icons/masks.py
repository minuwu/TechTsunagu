from pathlib import Path
from PIL import Image, ImageDraw, ImageChops
S = Path(r'C:\Users\minha\AppData\Local\Temp\claude\e6e9e47624cef00c\6b9adb8f-cbed-4977-8a20-a1870f863912\scratchpad\work\icons')
I = Path(r'c:\Users\minha\Desktop\TechTsunagu\frontend\public\brand\icons')
O = S / 'masks'; O.mkdir(exist_ok=True)
N = 512
def shape(kind, size=N, r=1.0):
    big = Image.new('L', (size*4, size*4), 0); d = ImageDraw.Draw(big); n = size*4
    if kind == 'circle':
        m = (1-r)/2*n; d.ellipse([m, m, n-m, n-m], fill=255)
    else:
        d.rounded_rectangle([0, 0, n-1, n-1], radius=int(.224*n), fill=255)
    return big.resize((size, size), Image.LANCZOS)
def load(name, w):
    return Image.open(I / f'app-icon-{name}-{w}.png').convert('RGBA').resize((N, N), Image.LANCZOS)
def mk(img, kind, out, r=1.0):
    bg = Image.new('RGBA', (N, N), '#E3E2DE'); bg.paste(img, (0, 0), ImageChops.multiply(img.split()[3], shape(kind, N, r)))
    bg.save(O / out)
for name, w in (('ios',1024),('dark',1024),('light',1024),('tinted',1024),('face-closeup',512),('alt-hardhat',512),('badge-question',512),('seasonal-snow',512),('seasonal-sakura',512),('alt-speech-bubble',512),('outline-only',512)):
    im = load(name, w)
    mk(im, 'circle', f'mask-{name}-circle.png'); mk(im, 'squircle', f'mask-{name}-squircle.png')
# maskable: circle at 80% (min safe) and full squircle
im = load('maskable', 512)
mk(im, 'circle', 'mask-maskable-circle80.png', .8)
d = im.copy(); dr = ImageDraw.Draw(d); dr.ellipse([256-204.8, 256-204.8, 256+204.8, 256+204.8], outline='#FF00AA', width=3); d.save(O/'mask-maskable-safezone.png')
# adaptive
bg = Image.open(I/'app-icon-android-adaptive-background-432.png').convert('RGBA')
fg = Image.open(I/'app-icon-android-adaptive-foreground-432.png').convert('RGBA')
comp = Image.alpha_composite(bg, fg)
crop = comp.crop((72, 72, 360, 360)).resize((N, N), Image.LANCZOS)  # visible 66/108 = 264 of 432 -> use 72dp window
vis = comp.crop((84, 84, 348, 348)).resize((N, N), Image.LANCZOS)
mk(vis, 'circle', 'mask-adaptive-circle.png'); mk(vis, 'squircle', 'mask-adaptive-squircle.png')
full = comp.resize((N, N), Image.LANCZOS).copy(); dr = ImageDraw.Draw(full)
a, b = 84/432*N, 348/432*N
dr.rectangle([a, a, b, b], outline='#FF00AA', width=3); dr.ellipse([a, a, b, b], outline='#00AA55', width=3)
full.save(O/'mask-adaptive-canvas-safe.png')
# monochrome under tint
mono = Image.open(I/'app-icon-android-adaptive-monochrome-432.png').convert('RGBA')
t = Image.new('RGBA', mono.size, '#1351AA'); t = Image.composite(Image.new('RGBA', mono.size, '#FFFFFF'), t, mono.split()[3])
t.crop((84,84,348,348)).resize((N,N)).save(O/'mask-adaptive-mono-themed.png')
print(sorted(p.name for p in O.glob('*.png')))
