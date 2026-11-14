#!/usr/bin/env python3
"""Contact sheets from Tier 2 captures: one column per figure, EN above TA, 12 figures per sheet (3 per row, 4 rows).
Usage: contact_sheet.py <captures dir> <size tag e.g. 360x800> <out prefix> <comma-separated ids>"""
import glob, os, sys
from PIL import Image
d, size, out, ids = sys.argv[1], sys.argv[2], sys.argv[3], [i for i in sys.argv[4].split(',') if i]
def load(i, l):
    f = glob.glob(f'{d}/**/20-batch-{size}-{i}-{l}-inline.png', recursive=True)
    if not f: sys.exit(f'capture missing: {i}/{l} at {size}')
    return Image.open(f[0]).convert('RGB')
W = 300
def rs(x): return x.resize((W, int(x.height * W / x.width)))
cols = [(rs(load(i, 'EN')), rs(load(i, 'TA'))) for i in ids]
for n, k in enumerate(range(0, len(cols), 12)):
    chunk = cols[k:k + 12]; rows = [chunk[r:r + 3] for r in range(0, len(chunk), 3)]
    hs = [max(a.height + b.height for a, b in r) + 10 for r in rows]
    c = Image.new('RGB', (3 * (W + 14), sum(h + 14 for h in hs)), 'white'); y = 0
    for r, h in zip(rows, hs):
        for j, (a, b) in enumerate(r): c.paste(a, (j * (W + 14), y)); c.paste(b, (j * (W + 14), y + a.height + 10))
        y += h + 14
    p = f'{out}-{n + 1}.png'; c.save(p); print(p, c.size)
