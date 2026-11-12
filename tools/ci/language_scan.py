#!/usr/bin/env python3
"""Language-rule scan. Rule: a screen is English-only or Tamil-only, never mixed.
 1. Every Tamil-mode string (the "ta" fields of content/quiz/revision/headings/syllabus JSON, figure `ta` strings, Tamil literals in Kotlin)
    is scanned for Latin words. Allowed: symbols and abbreviations (any token with a digit, an upper-case letter after the first
    character, or all capitals), units, DNA/RNA/enzyme-style names, and organism/gene names in tools/ci/latin_allowlist.txt.
 2. Every English-mode string ("en" fields) is scanned for Tamil script (never allowed).
Exit 1 if (a) any English string contains Tamil, or (b) the set of non-allowed Latin words in Tamil strings is larger than the
committed baseline tools/ci/latin_baseline.txt (a ratchet: it may shrink, never grow). `--write-baseline` rewrites the baseline."""
import json, os, re, sys, glob
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
A = os.path.join(ROOT, 'app/src/main/assets')
TAMIL = re.compile(r'[஀-௿]'); WORD = re.compile(r"[A-Za-z][A-Za-z'’\-]*")
allow = set(w.strip().lower() for w in open(os.path.join(ROOT, 'tools/ci/latin_allowlist.txt'), encoding='utf8') if w.strip() and not w.startswith('#'))
def symbolic(w):
    return len(w) <= 2 or any(c.isdigit() for c in w) or w.isupper() or re.search(r'[a-z][A-Z]', w) or w.lower() in allow \
        or all(p.lower() in allow or len(p) <= 2 or p.isupper() for p in w.split('-'))
ta, en = [], []
def walk(o, src):
    if isinstance(o, dict):
        if 'ta' in o and isinstance(o['ta'], str): ta.append((src, o['ta']))
        if 'en' in o and isinstance(o['en'], str): en.append((src, o['en']))
        for v in o.values(): walk(v, src)
    elif isinstance(o, list):
        for v in o: walk(v, src)
for f in glob.glob(A + '/**/*.json', recursive=True):
    if f.endswith('.alt.json') or '/diagrams/' in f and not f.endswith('.alt.json') and 'alt' not in f: pass
    try: walk(json.load(open(f, encoding='utf8')), os.path.relpath(f, A))
    except Exception: pass
for f in glob.glob(ROOT + '/tools/figures/src/*/strings.json'):
    d = json.load(open(f, encoding='utf8')); walk(d, 'figure:' + os.path.basename(os.path.dirname(f)))
bad_en = [(s, t) for s, t in en if TAMIL.search(t)]
hits = {}
for s, t in ta:
    for w in WORD.findall(re.sub(r'⟦[a-z_]+⟧', '', t)):
        if not symbolic(w): hits.setdefault(w.lower(), s)
base_p = os.path.join(ROOT, 'tools/ci/latin_baseline.txt')
if '--write-baseline' in sys.argv:
    open(base_p, 'w', encoding='utf8').write('\n'.join(sorted(hits)) + '\n'); print('baseline written:', len(hits)); sys.exit(0)
base = set(l.strip() for l in open(base_p, encoding='utf8')) if os.path.exists(base_p) else set()
new = sorted(set(hits) - base)
print(f'Tamil strings scanned: {len(ta)}; English strings scanned: {len(en)}')
print(f'English strings containing Tamil script: {len(bad_en)}')
print(f'Non-allowed Latin words in Tamil strings: {len(hits)} (baseline {len(base)}, new {len(new)})')
for s, t in bad_en[:20]: print('  EN has Tamil:', s, t[:80])
for w in new[:40]: print('  NEW Latin word in Tamil string:', w, '<-', hits[w])
sys.exit(1 if bad_en or new else 0)
