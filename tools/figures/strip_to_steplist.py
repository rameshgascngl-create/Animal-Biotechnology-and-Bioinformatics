#!/usr/bin/env python3
"""Strip -> step-list transform for legacy horizontal step strips. One small tool, no per-figure code.
Reads the figure's SVG from html-suite (via tools/diagrams/render.py's inventory), orders the boxes left to right, makes the first text in a box the step title
and the rest its detail, texts above the boxes the heading, texts below the strip the footer; Tamil comes from the legacy TA map (the old vector term becomes the
⟦vec⟧ token). Writes tools/figures/src/<id>/{gen_figure.py,strings.json}. REFUSES (needs manual) if any English text has no Tamil, any text is neither placed nor
a bare numeral/arrow glyph, or fewer than 3 boxes. Then build with build_figure.py as usual (its gate is the acceptance test).
Usage: strip_to_steplist.py id [id ...]   (prints WROTE or REFUSED: reason)"""
import json, os, re, shutil, sys
from playwright.sync_api import sync_playwright
HERE = os.path.dirname(os.path.abspath(__file__)); ROOT = os.path.abspath(os.path.join(HERE, '..', '..'))
sys.path.insert(0, os.path.join(ROOT, 'tools', 'diagrams'))
from render import inv, SHOW, ta as TA_MAP
from conservation import conservation
OLD_VEC = 'தாங்கிக்கடத்தி'
NORM = lambda s: re.sub(r'\s+', ' ', s).strip()
JS = """(i) => { const s=document.querySelectorAll('svg')[i]; const live=e=>!e.closest('defs,marker,clipPath,mask,pattern,symbol');
 const R=[...s.querySelectorAll('rect')].filter(live).map(e=>e.getBBox()).filter(b=>b.width>=28&&b.height>=14);
 const vb=(s.getAttribute('viewBox')||'0 0 1 1').split(/[ ,]+/).map(Number);
 const boxes=R.filter(b=>b.width<vb[2]*0.95).map(b=>({x:b.x,y:b.y,w:b.width,h:b.height}));
 const T=[...s.querySelectorAll('text')].filter(live).map(t=>{const b=t.getBBox(); const w=document.createTreeWalker(t,NodeFilter.SHOW_TEXT); let n; const a=[]; while(n=w.nextNode()){const v=n.nodeValue.replace(/\\s+/g,' ').trim(); if(v) a.push(v)}
   return {t:a.join(' '), nodes:a, bold:parseInt(getComputedStyle(t).fontWeight)>=600, cx:b.x+b.width/2, cy:b.y+b.height/2}}).filter(x=>x.t);
 const arrows=[...s.querySelectorAll('[marker-end],[marker-start]')].filter(live).length; return {boxes,T,arrows}; }"""
def extract(pg, fn, i):
    pg.goto('file://' + os.path.join(ROOT, 'html-suite', fn)); pg.evaluate(SHOW, i); return pg.evaluate(JS, i)
def convert(did, pg):
    fn = next(f for f, xs in inv.items() for k in range(len(xs)) if f"{f[:-5]}-d{k+1:02d}" == did); i = int(did.rsplit('-d', 1)[1]) - 1
    d = extract(pg, fn, i); boxes = d['boxes']
    # a strip's steps are the boxes in the one dominant row; ignore enclosing/background rects (those containing other boxes)
    boxes = [b for b in boxes if not any(o is not b and o['x'] >= b['x'] - 1 and o['y'] >= b['y'] - 1 and o['x'] + o['w'] <= b['x'] + b['w'] + 1 and o['y'] + o['h'] <= b['y'] + b['h'] + 1 for o in boxes)]
    if len(boxes) < 3: return None, f'only {len(boxes)} boxes'
    rows = {}
    for b in boxes: rows.setdefault(round((b['y'] + b['h'] / 2) / 20), []).append(b)
    if len(rows) > 3 and any(len(r) > 1 for r in rows.values()): return None, f'{len(rows)} rows with several boxes (not a strip or a single column)'
    steps = [b for r in sorted(rows) for b in sorted(rows[r], key=lambda b: b['x'])]   # reading order: row by row, left to right
    top = min(b['y'] for b in steps); bot = max(b['y'] + b['h'] for b in steps)
    inside = lambda t, b: b['x'] <= t['cx'] <= b['x'] + b['w'] and b['y'] <= t['cy'] <= b['y'] + b['h']
    per = [[] for _ in steps]; head, foot, dropped = [], [], 0; dropped_nodes = []
    for t in sorted(d['T'], key=lambda t: (t['cy'], t['cx'])):
        j = next((j for j, b in enumerate(steps) if inside(t, b)), None)
        if j is not None: per[j].append(t)
        elif re.fullmatch(r'[\d\W_]{1,3}|[→←↑↓▶▼►]+', t['t']): dropped += 1; dropped_nodes += t['nodes']
        elif t['cy'] < top: head.append(t)
        elif t['cy'] > bot: foot.append(t)
        else: return None, f'text outside boxes inside the strip band: {t["t"][:30]}'
    if any(not p for p in per): return None, 'a box with no text'
    if len(head) > 1 or len(foot) > 1: return None, f'{len(head)+len(foot)} unboxed annotations (needs a hand-written layout)'
    S = {}; rowspec = []; seen = 0
    def tr(x):
        nodes = x['nodes'] if isinstance(x, dict) else [x]
        tas = []
        for en in nodes:
            ta = TA_MAP.get(en)
            if ta is None and not re.search(r'[a-z]{3}', en): ta = en   # symbols/abbreviations only: same in Tamil
            if ta is None: raise KeyError(en)
            tas.append(ta.replace(OLD_VEC, '⟦vec⟧'))
        return {'en': ' '.join(nodes), 'ta': ' '.join(tas)}
    try:
        for n, p in enumerate(per, 1):
            nb = 0
            while nb < len(p) and p[nb]['bold']: nb += 1   # leading bold lines are the title; if none, the whole box is one statement
            if nb == 0 or nb == len(p): nb = len(p)
            S[f't{n}'] = {k: ' '.join(tr(x)[k] for x in p[:nb]) for k in ('en', 'ta')}
            if nb < len(p): S[f'd{n}'] = {k: ' '.join(tr(x)[k] for x in p[nb:]) for k in ('en', 'ta')}
            rowspec.append((f't{n}', f'd{n}' if nb < len(p) else None))
        hk = None
        if head: S['h'] = {k: ' '.join(tr(x)[k] for x in head) for k in ('en', 'ta')}; hk = 'h'
        fk = []
        for n, x in enumerate(foot, 1): S[f'f{n}'] = tr(x); fk.append(f'f{n}')
    except KeyError as e: return None, f'no Tamil for: {str(e)[:40]}'
    def alt(l):
        parts = [f"{n}. {S[a][l]}" + (f" — {S[b][l]}" if b else '') for n, (a, b) in enumerate(rowspec, 1)]
        pre = (S['h'][l] + ': ') if hk else ''
        return pre + ('; '.join(parts)) + ('. ' + ' '.join(S[k][l] for k in fk) if fk else '.')
    S['_alt'] = {'en': alt('en'), 'ta': alt('ta')}
    FR = {}
    for n, p in enumerate(per, 1):
        nb = 0
        while nb < len(p) and p[nb]['bold']: nb += 1
        if nb == 0 or nb == len(p): nb = len(p)
        FR[f't{n}'] = [m for x in p[:nb] for m in x['nodes']]
        if nb < len(p): FR[f'd{n}'] = [m for x in p[nb:] for m in x['nodes']]
    for n, x in enumerate(foot, 1): FR[f'f{n}'] = list(x['nodes'])
    if head: FR['h'] = [m for x in head for m in x['nodes']]
    orig = [m for x in d['T'] for m in x['nodes']]
    why = conservation(orig, TA_MAP, OLD_VEC, S, FR, len(steps), d['arrows'], dropped_nodes)
    if why: return None, 'text-conservation: ' + why
    out = os.path.join(HERE, 'src', did); os.makedirs(out, exist_ok=True)
    json.dump(FR, open(os.path.join(out, 'frags.json'), 'w'), ensure_ascii=False, indent=1)
    json.dump(S, open(os.path.join(out, 'strings.json'), 'w'), ensure_ascii=False, indent=1)
    open(os.path.join(out, 'gen_figure.py'), 'w').write(
        f'#!/usr/bin/env python3\n"""Geometry for {did}: vertical step list converted from a horizontal strip by strip_to_steplist.py. Run: python3 gen_figure.py > figure.svg"""\n'
        "import os, sys; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))\nfrom steplist import steplist\n"
        f'ROWS = {rowspec!r}\nprint(steplist(here, ROWS, None, head={hk!r}, foot={fk!r}))\n')
    return out, f'{len(steps)} steps, head={bool(head)}, foot={len(foot)}'
if __name__ == '__main__':
    with sync_playwright() as p:
        b = p.chromium.launch(executable_path=os.environ.get('CHROME_BIN') or shutil.which('google-chrome') or shutil.which('chromium'), args=['--no-sandbox'])
        for did in sys.argv[1:]:
            pg = b.new_page(viewport={'width': 960, 'height': 1400})
            try: o, m = convert(did, pg)
            except Exception as e: o, m = None, f'error {e}'[:120]
            pg.close(); print(did, 'WROTE ' + m if o else 'REFUSED: ' + m)
        b.close()
