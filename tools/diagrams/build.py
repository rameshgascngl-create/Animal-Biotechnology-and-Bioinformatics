#!/usr/bin/env python3
"""Render diagram art to PNG and write label specs into app assets.  Usage: build.py [id ...] | validate"""
import json, os, re, subprocess, sys, tempfile, unicodedata
sys.path.insert(0, os.path.dirname(__file__))
from art import DIAGRAMS

OUT = os.path.join(os.path.dirname(__file__), '..', '..', 'app', 'src', 'main', 'assets', 'diagrams')
TAMIL = re.compile('[஀-௿]')

def build(did):
    w, h, svg, marks = DIAGRAMS[did]()
    os.makedirs(OUT, exist_ok=True)
    with tempfile.NamedTemporaryFile('w', suffix='.svg', delete=False, encoding='utf8') as f:
        f.write(svg); p = f.name
    png = os.path.join(OUT, did + '.png')
    subprocess.run(['rsvg-convert', '-w', '1200', '-o', png, p], check=True)
    os.unlink(p)
    spec = {'id': did, 'w': w, 'h': h, 'markers': [
        {'n': n, 'x': round(x / w, 4), 'y': round(y / h, 4), 'en': en, 'ta': unicodedata.normalize('NFC', ta), 'ta_status': 'draft'}
        for n, x, y, en, ta in marks]}
    with open(os.path.join(OUT, did + '.json'), 'w', encoding='utf8') as f:
        json.dump(spec, f, ensure_ascii=False, indent=1)
    return spec

def validate():
    bad = 0
    for fn in sorted(os.listdir(OUT)):
        if not fn.endswith('.json'): continue
        did = fn[:-5]
        s = json.load(open(os.path.join(OUT, fn), encoding='utf8'))
        if not os.path.exists(os.path.join(OUT, did + '.png')): print(did, 'missing png'); bad += 1
        nums = [m['n'] for m in s['markers']]
        if nums != list(range(1, len(nums) + 1)): print(did, 'markers not numbered 1..n'); bad += 1
        for m in s['markers']:
            if not (0 <= m['x'] <= 1 and 0 <= m['y'] <= 1): print(did, m['n'], 'marker outside image'); bad += 1
            if not m['en'].strip(): print(did, m['n'], 'empty en'); bad += 1
            if not TAMIL.search(m['ta']) and m['ta'] != m['en']: print(did, m['n'], 'ta has no Tamil script'); bad += 1
    print(f'{len([f for f in os.listdir(OUT) if f.endswith(".json")])} diagrams, {bad} problems')
    return bad

if __name__ == '__main__':
    a = sys.argv[1:]
    if a == ['validate']: sys.exit(1 if validate() else 0)
    for d in (a or DIAGRAMS): build(d); print('built', d)
    sys.exit(1 if validate() else 0)
