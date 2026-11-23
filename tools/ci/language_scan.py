#!/usr/bin/env python3
"""Language-rule scan. Rule: a screen is English-only or Tamil-only, never mixed.
 1. Scan JSON "ta" strings and the Tamil "_ta" fields of revision.json for Latin words. Allowed: symbols and abbreviations (any token with a digit, an upper-case letter after the first
    character, or all capitals), units, DNA/RNA/enzyme-style names, and organism/gene names in tools/ci/latin_allowlist.txt.
 2. Every English-mode string ("en" fields) is scanned for Tamil script (never allowed).
Exit 1 if (a) any English string contains Tamil, or (b) the set of non-allowed Latin words in Tamil strings is larger than the
corresponding committed baseline. Revision Tamil fields have their own unreviewed baseline.
`--write-baseline` rewrites only the regular baseline."""
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
def latin_hits(strings):
    hits = {}
    for source, value in strings:
        for word in WORD.findall(re.sub(r'⟦[a-z_]+⟧', '', value)):
            if not symbolic(word):
                hits.setdefault(word.lower(), source)
    return hits


def revision_tamil_strings(value, path='revision.json'):
    if isinstance(value, dict):
        for key, child in value.items():
            field = f'{path}/{key}'
            if key.endswith('_ta'):
                values = child if isinstance(child, list) else [child]
                for index, text in enumerate(values):
                    if isinstance(text, str):
                        source = f'{field}/{index}' if isinstance(child, list) else field
                        yield source, text
            else:
                yield from revision_tamil_strings(child, field)
    elif isinstance(value, list):
        for index, child in enumerate(value):
            yield from revision_tamil_strings(child, f'{path}/{index}')


hits = latin_hits(ta)
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
with open(os.path.join(A, 'revision.json'), encoding='utf8') as revision_file:
    revision_strings = list(revision_tamil_strings(json.load(revision_file)))
revision_hits = latin_hits(revision_strings)
revision_baseline_path = os.path.join(ROOT, 'tools/ci/latin_revision_baseline.txt')
with open(revision_baseline_path, encoding='utf8') as revision_baseline_file:
    revision_baseline = {
        line.strip() for line in revision_baseline_file
        if line.strip() and not line.lstrip().startswith('#')
    }
revision_new = sorted(revision_hits.keys() - revision_baseline)
print(f'revision.json Tamil strings scanned: {len(revision_strings)}')
print(f'revision.json non-allowed Latin words: {len(revision_hits)} '
      f'(unreviewed baseline {len(revision_baseline)}, new {len(revision_new)})')
for word in revision_new:
    print('  NEW Latin word in revision.json Tamil string:', word, '<-', revision_hits[word])
sys.exit(1 if bad_en or new or revision_new else 0)
