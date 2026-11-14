#!/usr/bin/env python3
"""Tier 3: the three representative figures for the viewer lifecycle test, as id:LANG pairs.
tall = highest height/width among figures that appear in a chapter; wide = lowest; Tamil = the redrawn figure with the most Tamil text (Tamil mode)."""
import glob, json, os, struct
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..')); A = os.path.join(ROOT, 'app/src/main/assets')
used = set()
for f in glob.glob(A + '/content/u*/*.json'):
    for s in json.load(open(f, encoding='utf8'))['sections']:
        for b in s['blocks']:
            if b.get('type') == 'diagram': used.add(b['id'])
def ratio(i):
    p = os.path.join(A, 'diagrams', i + '.png')
    if not os.path.exists(p): return None
    w, h = struct.unpack('>II', open(p, 'rb').read(24)[16:24]); return h / w
r = {i: ratio(i) for i in used}; r = {i: v for i, v in r.items() if v}
tall = max(r, key=r.get); wide = min(r, key=r.get)
best, bi = -1, None
for g in glob.glob(os.path.join(ROOT, 'docs/audit/pilot/*-geometry.json')):
    i = os.path.basename(g)[:-len('-geometry.json')]
    if i in r and i not in (tall, wide):
        t = json.load(open(g)).get('ta', {}).get('texts') or 0
        if t > best: best, bi = t, i
print(f'{tall}:EN,{wide}:EN,{bi or tall}:TA')
