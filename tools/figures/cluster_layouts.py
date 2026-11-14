#!/usr/bin/env python3
"""Cluster legacy figures by layout type from SVG structure. Usage: cluster_layouts.py <out.json> id [id ...] (ids as in legibility_pass.py).
Types: strip (>=60% of boxes in one horizontal row), column (>=60% in one vertical column), flow (boxes in >=3 rows and >=2 columns, arrows),
ring (a large circle/ellipse with arcs or >=1 big circle and few boxes), plate_gel (>=5 small circles inside a big one, or >=4 lane/band rects), other."""
import json, os, shutil, sys
from playwright.sync_api import sync_playwright
HERE = os.path.dirname(os.path.abspath(__file__)); ROOT = os.path.abspath(os.path.join(HERE, '..', '..'))
sys.path.insert(0, os.path.join(ROOT, 'tools', 'diagrams'))
from render import inv, SHOW
FEAT = """(i) => { const s=document.querySelectorAll('svg')[i]; const vb=(s.getAttribute('viewBox')||'0 0 1 1').split(/[ ,]+/).map(Number);
 const T=s.querySelectorAll('text').length; const live=e=>!e.closest('defs,marker,clipPath,mask,pattern,symbol');
 const R=[...s.querySelectorAll('rect')].filter(live).map(e=>e.getBBox()).filter(b=>b.width>=28&&b.height>=14&&b.width<vb[2]*0.95);
 const lanes=[...s.querySelectorAll('rect')].filter(live).map(e=>e.getBBox()).filter(b=>b.height<=9&&b.width>=14).length;
 const C=[...s.querySelectorAll('circle,ellipse')].filter(live).map(e=>{const b=e.getBBox();return [b.x+b.width/2,b.y+b.height/2,Math.max(b.width,b.height)/2]});
 const big=C.filter(c=>c[2]>=45); const small=C.filter(c=>c[2]<=14);
 let inside=0; big.forEach(b=>{inside=Math.max(inside,small.filter(c=>Math.hypot(c[0]-b[0],c[1]-b[1])<b[2]).length)});
 const arcs=[...s.querySelectorAll('path')].filter(live).filter(p=>/[Aa]\\s*[\\d.]/.test(p.getAttribute('d')||'')).length;
 const arrows=s.querySelectorAll('[marker-end],[marker-start]').length;
 const rows={}, cols={}; R.forEach(b=>{const y=Math.round((b.y+b.height/2)/20),x=Math.round((b.x+b.width/2)/40); rows[y]=(rows[y]||0)+1; cols[x]=(cols[x]||0)+1});
 const mr=Math.max(0,...Object.values(rows)), mc=Math.max(0,...Object.values(cols));
 return {boxes:R.length,rows:Object.keys(rows).length,cols:Object.keys(cols).length,maxrow:mr,maxcol:mc,lanes,big:big.length,small:small.length,inside,arcs,arrows,T,aspect:vb[2]/vb[3]}; }"""
def classify(f):
    n = f['boxes']
    if f['inside'] >= 5 or f['lanes'] >= 4: return 'plate_gel'
    if f['big'] >= 1 and (f['arcs'] >= 1 or n <= 4): return 'ring'
    if n >= 3 and f['maxrow'] >= 0.6 * n and f['rows'] <= 2: return 'strip'
    if n >= 3 and f['maxcol'] >= 0.6 * n and f['cols'] <= 2: return 'column'
    if n >= 4 and f['rows'] >= 3 and f['cols'] >= 2: return 'flow'
    return 'other'
if __name__ == '__main__':
    out, ids = sys.argv[1], sys.argv[2:]; res = {}
    with sync_playwright() as p:
        b = p.chromium.launch(executable_path=os.environ.get('CHROME_BIN') or shutil.which('google-chrome') or shutil.which('chromium'), args=['--no-sandbox'])
        for did in ids:
            fn = next(f for f, xs in inv.items() for k in range(len(xs)) if f"{f[:-5]}-d{k+1:02d}" == did); i = int(did.rsplit('-d', 1)[1]) - 1
            pg = b.new_page(viewport={'width': 960, 'height': 1400}); pg.goto('file://' + os.path.join(ROOT, 'html-suite', fn)); pg.evaluate(SHOW, i)
            f = pg.evaluate(FEAT, i); f['type'] = classify(f); res[did] = f; pg.close()
        b.close()
    json.dump(res, open(out, 'w'), indent=1)
    from collections import Counter; print(Counter(v['type'] for v in res.values()))
