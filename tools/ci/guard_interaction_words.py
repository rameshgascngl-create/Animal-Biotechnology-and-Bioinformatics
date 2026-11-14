#!/usr/bin/env python3
"""Guard: the app is touch-only, so reader text must not describe interactions it does not have (click, hover, mouse, left/right panel).
"Tap" is fine where the app supports it. Scans every string in the shipped content (en and ta), figure strings and alt text.
Ratchet: strings already in tools/ci/interaction_baseline.txt (found before the sweep) are tolerated until the sweep rewrites them; any NEW offending string fails.
`--write-baseline` rewrites the baseline (the sweep empties it)."""
import glob, hashlib, json, os, re, sys
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..')); A = os.path.join(ROOT, 'app/src/main/assets')
EN = re.compile(r"\b(click(?:s|ed|ing|-to-reveal|-free)?|right-click|hover(?:s|ing)?|mouse|(?:left|right)(?:-hand)? panel|left/right panel)\b", re.I)
TA = re.compile(r'சொடுக்|சுட்டியை|சுட்டியால்')
hits = {}
def walk(o, src):
    if isinstance(o, dict):
        for k, v in o.items():
            if isinstance(v, str) and k in ('en', 'ta', 'text', 'title', 'caption', 'question', 'answer'):
                rx = TA if k == 'ta' else EN
                if rx.search(v): hits[hashlib.sha1((src + '|' + v).encode('utf8')).hexdigest()[:16]] = (src, v)
            else: walk(v, src)
    elif isinstance(o, list):
        for v in o: walk(v, src)
files = glob.glob(A + '/**/*.json', recursive=True) + glob.glob(ROOT + '/tools/figures/src/*/strings.json')
for f in files:
    try: walk(json.load(open(f, encoding='utf8')), os.path.relpath(f, ROOT))
    except Exception as e: print('unreadable', f, e); sys.exit(1)
base_p = os.path.join(ROOT, 'tools/ci/interaction_baseline.txt')
if '--write-baseline' in sys.argv:
    open(base_p, 'w').write('\n'.join(sorted(hits)) + '\n'); print('baseline written:', len(hits)); sys.exit(0)
base = set(l.strip() for l in open(base_p)) if os.path.exists(base_p) else set()
new = [h for h in hits if h not in base]
print(f'strings describing click/hover/mouse/panel interactions: {len(hits)} (tolerated baseline {len(base & set(hits))}, new {len(new)})')
for h in new[:30]: print('  NEW:', hits[h][0], '|', hits[h][1][:100].replace('\n', ' '))
sys.exit(1 if new else 0)
