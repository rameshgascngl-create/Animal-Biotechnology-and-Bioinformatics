#!/usr/bin/env python3
"""Build EN|TA contact sheets from audit_geometry.py output.  Usage: contact_sheet.py AUDIT_DIR OUT_DIR [per_sheet=4]"""
import json, os, sys, collections
from PIL import Image, ImageDraw, ImageFont
src, out = sys.argv[1], sys.argv[2]; per = int(sys.argv[3]) if len(sys.argv) > 3 else 4
os.makedirs(out, exist_ok=True)
rows = json.load(open(os.path.join(src, 'audit.json')))
by = collections.defaultdict(dict)
for r in rows: by[r['id']][r['lang']] = r
ids = sorted(by)
STRUCT = ('outside', 'text_text', 'text_stroke', 'text_edge', 'covered', 'shape_overlap', 'stroke_cross')
def tag(r):
    c = collections.Counter(d['kind'] for d in r['defects'])
    return f"struct {sum(c[k] for k in STRUCT)} (out {c['outside']} tt {c['text_text']} str {c['text_stroke']} edge {c['text_edge']} cov {c['covered']} sh {c['shape_overlap']+c['stroke_cross']}) | small {c['small_text']} | margin {c['margin']}"
try: F = ImageFont.truetype('/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf', 15)
except Exception: F = ImageFont.load_default()
W = 560
for s in range(0, len(ids), per):
    chunk = ids[s:s + per]; tiles = []
    for did in chunk:
        pair = []
        for lg in ('en', 'ta'):
            im = Image.open(os.path.join(src, f'{did}_{lg}.png')).convert('RGB'); im = im.resize((W, int(im.height * W / im.width))); pair.append(im)
        h = max(p.height for p in pair) + 44
        t = Image.new('RGB', (2 * W + 10, h), 'white'); d = ImageDraw.Draw(t)
        d.text((4, 2), f"{did}", fill='black', font=F); d.text((4, 22), 'EN ' + tag(by[did]['en']) + '   ||   TA ' + tag(by[did]['ta']), fill=(160, 0, 0), font=F)
        t.paste(pair[0], (0, 44)); t.paste(pair[1], (W + 10, 44)); tiles.append(t)
    sheet = Image.new('RGB', (2 * W + 10, sum(t.height for t in tiles) + 6 * len(tiles)), (200, 200, 200)); y = 0
    for t in tiles: sheet.paste(t, (0, y)); y += t.height + 6
    sheet.save(os.path.join(out, f'sheet_{s // per + 1:02d}.png')); print(f'sheet_{s // per + 1:02d}', ' '.join(chunk))
