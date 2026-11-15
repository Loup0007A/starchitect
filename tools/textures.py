#!/usr/bin/env python3
"""Textures de l'interface (papier, sceaux, étoiles, ornements…) -> assets/starchitect/textures/gui/

Usage : python3 tools/textures.py   (Pillow ; résultat déterministe)
"""
import math
import os
import random

from PIL import Image, ImageDraw, ImageFilter

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
OUT = os.path.join(ROOT, 'src', 'main', 'resources', 'assets', 'starchitect', 'textures', 'gui')

PAPER = (243, 235, 217)
INK = (43, 37, 32)
GOLD = (222, 168, 46)
GOLD_D = (150, 98, 22)
GOLD_L = (255, 222, 120)


def save(img, name):
    os.makedirs(OUT, exist_ok=True)
    img.save(os.path.join(OUT, name + '.png'))


def paper(w, h, seed, stain=True):
    """Papier journal vieilli : grain, fibres, bords jaunis, piqûres, tache de café."""
    rnd = random.Random(seed)
    img = Image.new('RGB', (w, h), PAPER)
    px = img.load()
    for y in range(h):
        for x in range(w):
            n = rnd.randint(-7, 7)
            # bords jaunis
            dx = min(x, w - 1 - x) / (w * 0.5)
            dy = min(y, h - 1 - y) / (h * 0.5)
            edge = max(0.0, 1.0 - min(dx, dy) * 7.0)
            r, g, b = PAPER
            r = r + n - int(22 * edge)
            g = g + n - int(30 * edge)
            b = b + n - int(46 * edge)
            px[x, y] = (max(0, r), max(0, g), max(0, b))
    d = ImageDraw.Draw(img, 'RGBA')
    # fibres
    for _ in range(w * h // 900):
        x, y = rnd.randrange(w), rnd.randrange(h)
        a = rnd.uniform(0, math.pi)
        L = rnd.randint(4, 14)
        d.line([(x, y), (x + math.cos(a) * L, y + math.sin(a) * L)], fill=(120, 100, 70, rnd.randint(10, 26)), width=1)
    # piqûres
    for _ in range(w * h // 6000):
        x, y = rnd.randrange(w), rnd.randrange(h)
        r = rnd.uniform(0.6, 2.2)
        d.ellipse([x - r, y - r, x + r, y + r], fill=(150, 110, 60, rnd.randint(20, 55)))
    if stain:
        # tache de café (anneau) dans un coin
        cx, cy, R = int(w * 0.86), int(h * 0.80), int(min(w, h) * 0.16)
        ring = Image.new('L', (w, h), 0)
        rd = ImageDraw.Draw(ring)
        for i in range(3):
            rr = R - i
            rd.ellipse([cx - rr, cy - rr * 0.92, cx + rr, cy + rr * 0.92], outline=60 - i * 15, width=2)
        rd.ellipse([cx - R + 3, cy - R * 0.92 + 3, cx + R - 3, cy + R * 0.92 - 3], fill=12)
        ring = ring.filter(ImageFilter.GaussianBlur(1.2))
        brown = Image.new('RGB', (w, h), (140, 92, 40))
        img = Image.composite(brown, img, ring.point(lambda v: int(v * 0.55)))
    return img


def star(size, filled):
    S = size * 8
    img = Image.new('RGBA', (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    cx = cy = S / 2
    pts = []
    for i in range(10):
        a = -math.pi / 2 + i * math.pi / 5
        r = S * 0.48 if i % 2 == 0 else S * 0.2
        pts.append((cx + math.cos(a) * r, cy + math.sin(a) * r * 1.0 + S * 0.03))
    if filled:
        d.polygon(pts, fill=GOLD_D + (255,))
        inner = [(cx + (x - cx) * 0.78, cy + (y - cy) * 0.78) for x, y in pts]
        d.polygon(inner, fill=GOLD + (255,))
        hl = [(cx + (x - cx) * 0.45 - S * 0.05, cy + (y - cy) * 0.45 - S * 0.06) for x, y in pts]
        d.polygon(hl, fill=GOLD_L + (255,))
    else:
        d.polygon(pts, outline=(150, 128, 96, 255), width=S // 12)
    return img.resize((size, size), Image.LANCZOS)


def rosette(size, stars, base, dark, light, ribbon, center=None):
    """Sceau doré à festons, rubans, et des étoiles au centre."""
    S = size * 4
    img = Image.new('RGBA', (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    cx, cy = S / 2, S * 0.42
    R = S * 0.36
    # rubans
    for side in (-1, 1):
        x0 = cx + side * R * 0.35
        poly = [(x0 - S * 0.09, cy), (x0 + S * 0.09, cy), (x0 + side * S * 0.12 + S * 0.08, S * 0.97),
                (x0 + side * S * 0.12, S * 0.88), (x0 + side * S * 0.12 - S * 0.08, S * 0.97)]
        d.polygon(poly, fill=ribbon)
        d.line(poly + [poly[0]], fill=tuple(int(c * 0.6) for c in ribbon[:3]) + (255,), width=3)
    # festons
    pts = []
    for i in range(72):
        a = i * 2 * math.pi / 72
        r = R * (1.0 if (i // 3) % 2 == 0 else 0.9)
        pts.append((cx + math.cos(a) * r, cy + math.sin(a) * r))
    d.polygon(pts, fill=dark)
    d.ellipse([cx - R * 0.84, cy - R * 0.84, cx + R * 0.84, cy + R * 0.84], fill=base)
    d.ellipse([cx - R * 0.70, cy - R * 0.70, cx + R * 0.70, cy + R * 0.70], outline=dark, width=max(2, S // 70))
    # reflet
    glow = Image.new('RGBA', (S, S), (0, 0, 0, 0))
    ImageDraw.Draw(glow).chord([cx - R * 0.66, cy - R * 0.66, cx + R * 0.66, cy + R * 0.66], 200, 300, fill=light)
    img.alpha_composite(glow)
    if center is not None:
        c = center.resize((int(R * 0.9), int(R * 0.9)), Image.NEAREST)
        img.alpha_composite(c, (int(cx - c.width / 2), int(cy - c.height / 2)))
    else:
        n = max(1, stars)
        st = star(int(R * (0.62 if n == 1 else 0.46)), True)
        # étoiles claires sur fond doré
        st_img = Image.new('RGBA', st.size, (0, 0, 0, 0))
        mask = st.split()[3]
        st_img.paste((255, 248, 225, 255), (0, 0), mask)
        sp = st.width * 0.82
        y = cy - st.height / 2 + (S * 0.03 if n == 3 else 0)
        for i in range(n):
            ox = cx - (n - 1) * sp / 2 + i * sp - st.width / 2
            oy = y - (S * 0.05 if n == 3 and i == 1 else 0)
            img.alpha_composite(st_img, (int(ox), int(oy)))
    return img.resize((size, size), Image.LANCZOS)


def ink_stamp(size, seed):
    """Tampon encreur rouge, légèrement penché, pour les constructions sans étoile."""
    rnd = random.Random(seed)
    S = size * 4
    img = Image.new('RGBA', (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    red = (176, 40, 40, 235)
    c = S / 2
    d.ellipse([S * 0.06, S * 0.06, S * 0.94, S * 0.94], outline=red, width=S // 18)
    d.ellipse([S * 0.16, S * 0.16, S * 0.84, S * 0.84], outline=red, width=S // 40)
    w = S // 10
    d.line([(S * 0.3, S * 0.3), (S * 0.7, S * 0.7)], fill=red, width=w)
    d.line([(S * 0.7, S * 0.3), (S * 0.3, S * 0.7)], fill=red, width=w)
    # encre irrégulière
    px = img.load()
    for y in range(S):
        for x in range(S):
            r, g, b, a = px[x, y]
            if a and rnd.random() < 0.22:
                px[x, y] = (r, g, b, int(a * rnd.uniform(0.15, 0.6)))
    img = img.rotate(-14, resample=Image.BICUBIC)
    return img.resize((size, size), Image.LANCZOS)


def dirt_icon():
    rnd = random.Random(7)
    img = Image.new('RGBA', (8, 8))
    for y in range(8):
        for x in range(8):
            c = rnd.choice([(134, 96, 67), (121, 85, 58), (150, 108, 74), (100, 70, 48)])
            img.putpixel((x, y), c + (255,))
    return img


def divider(w, h):
    S = 4
    img = Image.new('RGBA', (w * S, h * S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    cy = h * S / 2
    col = INK + (255,)
    d.line([(0, cy), (w * S * 0.40, cy)], fill=col, width=S)
    d.line([(w * S * 0.60, cy), (w * S, cy)], fill=col, width=S)
    cx = w * S / 2
    r = h * S * 0.38
    d.polygon([(cx - r, cy), (cx, cy - r), (cx + r, cy), (cx, cy + r)], fill=col)
    for side in (-1, 1):
        x = cx + side * r * 2.2
        d.ellipse([x - r * 0.45, cy - r * 0.45, x + r * 0.45, cy + r * 0.45], fill=col)
        d.arc([cx + side * r * 1.2 - r, cy - r * 1.2, cx + side * r * 1.2 + r, cy + r * 0.2],
              0 if side > 0 else 180, 180 if side > 0 else 360, fill=col, width=S)
    return img.resize((w, h), Image.LANCZOS)


def magnifier(size):
    S = size * 8
    img = Image.new('RGBA', (S, S), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.line([(S * 0.62, S * 0.62), (S * 0.92, S * 0.92)], fill=(92, 59, 30, 255), width=S // 7)
    d.ellipse([S * 0.08, S * 0.08, S * 0.68, S * 0.68], fill=(190, 225, 240, 200), outline=(60, 60, 70, 255), width=S // 12)
    d.chord([S * 0.16, S * 0.16, S * 0.6, S * 0.6], 190, 260, fill=(255, 255, 255, 220))
    return img.resize((size, size), Image.LANCZOS)


def no_photo(w, h):
    """Croquis au crayon d'une maison : l'illustration quand la critique n'a pas de photo."""
    img = paper(w, h, 11, stain=False).convert('RGBA')
    d = ImageDraw.Draw(img)
    pencil = (90, 82, 72, 200)
    bx, by, bw, bh = w * 0.3, h * 0.45, w * 0.4, h * 0.38
    rnd = random.Random(3)

    def sk(a, b):
        for _ in range(2):
            j = lambda p: (p[0] + rnd.uniform(-0.8, 0.8), p[1] + rnd.uniform(-0.8, 0.8))
            d.line([j(a), j(b)], fill=pencil, width=1)
    sk((bx, by), (bx + bw, by)); sk((bx, by), (bx, by + bh)); sk((bx + bw, by), (bx + bw, by + bh))
    sk((bx - 4, by + bh), (bx + bw + 4, by + bh))
    sk((bx - 4, by + 2), (w / 2, h * 0.18)); sk((w / 2, h * 0.18), (bx + bw + 4, by + 2))
    sk((w / 2 - 4, by + bh), (w / 2 - 4, by + bh * 0.45)); sk((w / 2 + 4, by + bh), (w / 2 + 4, by + bh * 0.45))
    sk((w / 2 - 4, by + bh * 0.45), (w / 2 + 4, by + bh * 0.45))
    for x0 in (bx + 5, bx + bw - 15):
        d.rectangle([x0, by + 6, x0 + 10, by + 15], outline=pencil)
    for i in range(14):
        x = bx + rnd.uniform(-20, bw + 20)
        d.line([(x, by + bh + 2), (x + 3, by + bh + 5)], fill=(90, 82, 72, 90))
    return img


def main():
    save(paper(512, 320, 1), 'paper')
    save(paper(256, 64, 2, stain=False), 'paper_strip')
    save(star(16, True), 'star')
    save(star(16, False), 'star_empty')
    gold = dict(base=GOLD + (255,), dark=GOLD_D + (255,), light=GOLD_L + (120,), ribbon=(170, 40, 50, 255))
    for n in (1, 2, 3):
        save(rosette(96, n, **gold), f'seal_{n}')
    save(ink_stamp(96, 5), 'seal_0')
    save(rosette(96, 0, base=(150, 108, 74, 255), dark=(92, 62, 40, 255), light=(190, 150, 110, 120),
                 ribbon=(92, 62, 40, 255), center=dirt_icon()), 'seal_dirt')
    save(divider(96, 8), 'divider')
    save(magnifier(16), 'magnifier')
    save(no_photo(128, 72), 'no_photo')
    print('textures ->', os.path.relpath(OUT, ROOT))


if __name__ == '__main__':
    main()
