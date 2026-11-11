#!/usr/bin/env python3
"""Pixel-based collision check. Renders each diagram with all text hidden, then asks: is the area under each label a
single flat colour (label on a panel / background = fine) or does a stroke/edge/other shape cross it (collision)?
Also flags labels that fall outside the SVG viewport (clipped).  Usage: check_px.py [--ta] [--json out.json]"""
import json, glob, os, sys, io, collections
from playwright.sync_api import sync_playwright
from PIL import Image
HERE = os.path.dirname(os.path.abspath(__file__)); ROOT = os.path.join(HERE, '..', '..')
ta_mode = '--ta' in sys.argv
out = sys.argv[sys.argv.index('--json') + 1] if '--json' in sys.argv else None
THRESH = 0.045   # share of label-box pixels that differ from the box's modal colour
inv = collections.defaultdict(list)
for f in glob.glob(os.path.join(ROOT, 'docs/audit/diagrams-*.json')):
    j = json.load(open(f, encoding='utf8'))
    for x in (j if isinstance(j, list) else j.get('diagrams', j)): inv[x['file']].append(x)
TA = json.load(open(os.path.join(HERE, 'ta_strings.json'), encoding='utf8')) if ta_mode else {}
FONT = 'file://' + os.path.abspath(os.path.join(ROOT, 'app/src/main/res/font/notosanstamil_regular.ttf'))
PREP = """([i, ta, font]) => { const s=document.querySelectorAll('svg')[i]; let e=s;
 while(e&&e!==document.documentElement){const cs=getComputedStyle(e); if(cs.display==='none')e.style.setProperty('display','block','important'); if(cs.visibility==='hidden')e.style.setProperty('visibility','visible','important'); e=e.parentElement;}
 document.querySelectorAll('*').forEach(x=>{const c=getComputedStyle(x).position; if((c==='fixed'||c==='sticky')&&!x.contains(s))x.style.setProperty('display','none','important');});
 if(ta){ const st=document.createElement('style'); st.textContent=`@font-face{font-family:TamilD;src:url(${font})}`; document.head.appendChild(st);
   const w=document.createTreeWalker(s,NodeFilter.SHOW_TEXT); let n; while(n=w.nextNode()){const t=n.nodeValue.replace(/\\s+/g,' ').trim(); if(ta[t]){n.nodeValue=n.nodeValue.replace(t,ta[t]); const x=n.parentNode.closest('text'); if(x)x.style.setProperty('font-family','TamilD,sans-serif','important');}} }
 s.scrollIntoView(); const sr=s.getBoundingClientRect();
 const items=[...s.querySelectorAll('text')].filter(t=>t.textContent.trim().length>2).map(t=>{const r=t.getBoundingClientRect(); return {t:t.textContent.trim().slice(0,40),x:r.left-sr.left,y:r.top-sr.top,w:r.width,h:r.height}}).filter(r=>r.w>0);
 const tx=[...s.querySelectorAll('text')]; tx.forEach(t=>t.style.setProperty('visibility','hidden','important'));
 return {w:sr.width,h:sr.height,items}; }"""
res = {}
with sync_playwright() as p:
    b = p.chromium.launch(executable_path='/usr/bin/google-chrome', args=['--no-sandbox'])
    pg = b.new_page(viewport={'width': 960, 'height': 900})
    for fn in sorted(inv):
        pg.goto('file://' + os.path.abspath(os.path.join(ROOT, 'html-suite', fn))); pg.wait_for_timeout(300)
        for i in range(len(inv[fn])):
            did = f"{fn[:-5]}-d{i+1:02d}"; hits = []
            info = pg.evaluate(PREP, [i, TA, FONT])
            pg.set_viewport_size({'width': 960, 'height': max(900, int(info['h']) + 80)}); pg.evaluate("(i)=>document.querySelectorAll('svg')[i].scrollIntoView()", i)
            png = pg.locator('svg').nth(i).screenshot()
            im = Image.open(io.BytesIO(png)).convert('RGB'); W, H = im.size
            for it in info['items']:
                if it['x'] < -2 or it['y'] < -2 or it['x'] + it['w'] > info['w'] + 2 or it['y'] + it['h'] > info['h'] + 2:
                    hits.append(['clipped', it['t'], 0]); continue
                mx, my = it['w'] * 0.04, it['h'] * 0.25
                box = (int(max(0, it['x'] + mx)), int(max(0, it['y'] + my)), int(min(W, it['x'] + it['w'] - mx)), int(min(H, it['y'] + it['h'] - my)))
                if box[2] - box[0] < 3 or box[3] - box[1] < 3: continue
                c = im.crop(box).quantize(colors=8).convert('RGB'); px = list(c.getdata())
                mode = collections.Counter(px).most_common(1)[0][1]
                share = 1 - mode / len(px)
                if share > THRESH: hits.append(['crossed', it['t'], round(share, 3)])
            pg.set_viewport_size({'width': 960, 'height': 900}); pg.reload(); pg.wait_for_timeout(150)
            res[did] = hits
    b.close()
tot = sum(len(v) for v in res.values()); bad = [k for k, v in res.items() if v]
cl = sum(1 for v in res.values() for c in v if c[0] == 'clipped')
print(('TA' if ta_mode else 'EN'), 'pixel collisions:', tot, 'in', len(bad), 'of', len(res), 'diagrams;', 'clipped', cl, 'crossed', tot - cl)
if out: json.dump(res, open(out, 'w', encoding='utf8'), ensure_ascii=False, indent=0)
