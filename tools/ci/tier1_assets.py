#!/usr/bin/env python3
"""Tier 1 asset checks (no emulator, seconds). Every check is fatal; all run so one report lists everything.
 1. Every shipped figure PNG has both sides <= 4096 px and a decoded size <= 24 MB (width*height*4).
 2. Every redrawn figure (tools/figures/src/<id>) has a complete string table: every <text data-k> has en and ta, no empty string, no stray key,
    _alt in both languages, no unresolved term token; and its asset trio (<id>.png, <id>_ta.png, <id>.alt.json) exists.
 3. Vector-term guard: the literal term under review (தாங்கிக்கடத்தி) never appears in a redrawn figure's strings; it comes only from the token
    in tools/i18n/figure_terms.json, so the pending decision is a one-line change.
"""
import json, os, re, struct, sys
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
A = os.path.join(ROOT, 'app/src/main/assets/diagrams'); SRC = os.path.join(ROOT, 'tools/figures/src')
VEC_LITERAL = 'தாங்கிக்கடத்தி'; MAX_SIDE = 4096; MAX_BYTES = 24 * 1024 * 1024
fails = []
def fail(m): fails.append(m); print('FAIL', m)
n = 0
for f in sorted(os.listdir(A)):
    if not f.endswith('.png'): continue
    n += 1
    with open(os.path.join(A, f), 'rb') as h: head = h.read(24)
    w, hh = struct.unpack('>II', head[16:24])
    if not (1 <= w <= MAX_SIDE and 1 <= hh <= MAX_SIDE): fail(f'{f} is {w}x{hh} (limit {MAX_SIDE})')
    if w * hh * 4 > MAX_BYTES: fail(f'{f} decodes to {w*hh*4/1048576:.1f} MB (limit 24)')
print(f'PNG dimensions/decoded size checked: {n}')
figs = 0; pending = []
for i in sorted(os.listdir(SRC)):
    d = os.path.join(SRC, i)
    if not os.path.isdir(d): continue
    figs += 1
    try: S = json.load(open(os.path.join(d, 'strings.json'), encoding='utf8'))
    except Exception as e: fail(f'{i}: strings.json unreadable ({e})'); continue
    svg = open(os.path.join(d, 'figure.svg'), encoding='utf8').read() if os.path.exists(os.path.join(d, 'figure.svg')) else ''
    keys = set(re.findall(r'data-k="([^"]+)"', svg))
    for k in keys:
        if k not in S: fail(f'{i}: key {k} used in figure.svg but missing from strings.json')
    for k, v in S.items():
        if k != '_alt' and k not in keys: fail(f'{i}: key {k} in strings.json is not used by figure.svg')
        for l in ('en', 'ta'):
            t = (v or {}).get(l)
            if not isinstance(t, str) or not t.strip(): fail(f'{i}: {k}/{l} is empty or missing'); continue
            if '⟦' in re.sub(r'⟦[a-z_]+⟧', '', t): fail(f'{i}: {k}/{l} has a broken term token')
            if l == 'ta' and VEC_LITERAL in t: fail(f'{i}: {k}/ta contains the literal vector term; use the token')
    if '_alt' not in S: fail(f'{i}: no _alt')
    if os.path.exists(os.path.join(A, i + '.alt.json')):  # shipped: the whole trio must be there
        for suf in ('.png', '_ta.png'):
            if not os.path.exists(os.path.join(A, i + suf)): fail(f'{i}: asset {i}{suf} missing')
    else: pending.append(i)
print(f'redrawn figures with string tables checked: {figs}')
print(f'drawn but not yet shipped (no .alt.json): {len(pending)}' + (' ' + ', '.join(pending) if pending else ''))
print('TIER 1 ASSETS:', 'FAILED (%d)' % len(fails) if fails else 'ok'); sys.exit(1 if fails else 0)
