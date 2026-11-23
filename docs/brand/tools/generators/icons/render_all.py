import os, subprocess, sys
from pathlib import Path
from PIL import Image

S = r'C:\Users\minha\AppData\Local\Temp\claude\e6e9e47624cef00c\6b9adb8f-cbed-4977-8a20-a1870f863912\scratchpad'
REPO = Path(r'c:\Users\minha\Desktop\TechTsunagu')
OUT = REPO / 'frontend/public/brand/icons'
env = dict(os.environ, NODE_PATH=S + r'\brand-tools\node_modules')
OPAQUE = {'ios', 'android-playstore', 'dark', 'light', 'tinted', 'face-closeup', 'thumb-closeup', 'outline-only',
          'badge-question', 'seasonal-sakura', 'seasonal-matsuri', 'seasonal-snow', 'alt-hardhat',
          'alt-megane-bridge', 'alt-understood', 'android-adaptive-background', 'alt-speech-bubble'}
WIDTH = {'ios': 1024, 'dark': 1024, 'light': 1024, 'tinted': 1024, 'android-adaptive-foreground': 432,
         'android-adaptive-background': 432, 'android-adaptive-monochrome': 432}
for svg in sorted(OUT.glob('app-icon-*.svg')):
    name = svg.stem[len('app-icon-'):]
    w = WIDTH.get(name, 512 if name not in ('mono-white', 'mono-black', 'sticker') else 512)
    png = OUT / f'{svg.stem}-{w}.png'
    subprocess.run(['node', str(REPO / 'docs/brand/tools/render.cjs'), str(svg), str(png), str(w), 'none'],
                   check=True, env=env, shell=True, stdout=subprocess.DEVNULL)
    if name in OPAQUE:
        im = Image.open(png)
        if name == 'tinted':
            im = im.convert('L').convert('RGB')
        else:
            im = im.convert('RGB')
        im.save(png, optimize=True)
print('rendered')
