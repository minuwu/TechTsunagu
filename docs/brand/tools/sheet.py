"""Contact sheet: tile PNG/WebP files with their file names so you can eyeball a batch.

usage: python sheet.py <out.png> <cols> <cell_px> <bg-hex> <img1> <img2> ...
Each image is fitted inside a square cell on the chosen background (transparent PNGs show the bg).
"""
import sys

from PIL import Image, ImageDraw


def main() -> None:
    out, cols, cell, bg, *files = sys.argv[1:]
    cols, cell = int(cols), int(cell)
    rows = (len(files) + cols - 1) // cols
    label_h = 22
    sheet = Image.new('RGB', (cols * cell, rows * (cell + label_h)), bg)
    draw = ImageDraw.Draw(sheet)
    for i, f in enumerate(files):
        im = Image.open(f).convert('RGBA')
        im.thumbnail((cell - 12, cell - 12))
        x = (i % cols) * cell + (cell - im.width) // 2
        y = (i // cols) * (cell + label_h) + (cell - im.height) // 2
        sheet.paste(im, (x, y), im)
        name = f.replace('\\', '/').split('/')[-1][:44]
        draw.text(((i % cols) * cell + 6, (i // cols) * (cell + label_h) + cell + 4), name, fill='#888888')
    sheet.save(out)
    print(out, sheet.size)


main()
