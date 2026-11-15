#!/usr/bin/env python3
"""Logo du mod (src/main/resources/starchitect_logo.png) et avatar CurseForge (curseforge/avatar.png)."""
import os
from PIL import Image, ImageDraw

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
PAPER, INK, GOLD, ROOF, WALL, GLASS, DOOR, LEAF, MONO = (
    (243, 235, 217), (43, 37, 32), (224, 168, 40), (120, 72, 40), (196, 150, 94), (150, 205, 230), (92, 59, 30),
    (70, 140, 60), (107, 45, 92))

# une maison à pignon sous trois étoiles, en pixels (32×32)
ART = [
    "................................",
    "....*...........*...........*...",
    "...***.........***.........***..",
    "..*****.......*****.......*****.",
    "...***.........***.........***..",
    "...*.*.........*.*.........*.*..",
    "................................",
    "...............RR...............",
    ".............RRRRRR.............",
    "...........RRRRRRRRRR...........",
    ".........RRRRRRRRRRRRRR.........",
    ".......RRRRRRRRRRRRRRRRRR.......",
    ".....RRRRRRRRRRRRRRRRRRRRRR.....",
    "...RRRRRRRRRRRRRRRRRRRRRRRRRR...",
    "......WWWWWWWWWWWWWWWWWWWW......",
    "......WWWWWWWWWWWWWWWWWWWW......",
    "......WWGGGGWWWWWWWWGGGGWW......",
    "......WWGGGGWWWWWWWWGGGGWW......",
    "......WWGGGGWWWDDWWWGGGGWW......",
    "......WWGGGGWWDDDDWWGGGGWW......",
    "......WWWWWWWWDDDDWWWWWWWW......",
    "......WWWWWWWWDDDDWWWWWWWW......",
    "....LLWWWWWWWWDDDDWWWWWWWWLL....",
    "...LLLWWWWWWWWDDDDWWWWWWWWLLL...",
    "..IIIIIIIIIIIIIIIIIIIIIIIIIIII..",
    "................................",
]
COL = {'*': GOLD, 'R': ROOF, 'W': WALL, 'G': GLASS, 'D': DOOR, 'L': LEAF, 'I': INK}


def art(size):
    img = Image.new('RGBA', (32, 32), PAPER + (255,))
    for y, row in enumerate(ART):
        for x, c in enumerate(row):
            if c in COL:
                img.putpixel((x, y + 3), COL[c] + (255,))
    return img.resize((size, size), Image.NEAREST)


def main():
    # avatar 400×400 : papier, cadre double, maison
    a = Image.new('RGBA', (400, 400), PAPER + (255,))
    a.paste(art(352), (24, 14))
    d = ImageDraw.Draw(a)
    d.rectangle([4, 4, 395, 395], outline=INK, width=6)
    d.rectangle([16, 16, 383, 383], outline=MONO, width=3)
    a.save(os.path.join(ROOT, 'curseforge', 'avatar.png'))
    # logo de la liste des mods : bandeau 2:1
    l = Image.new('RGBA', (256, 128), PAPER + (255,))
    l.paste(art(128), (64, 0))
    ImageDraw.Draw(l).rectangle([0, 0, 255, 127], outline=INK, width=3)
    l.save(os.path.join(ROOT, 'src', 'main', 'resources', 'starchitect_logo.png'))
    print('ok')


if __name__ == '__main__':
    main()
