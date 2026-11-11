#!/usr/bin/env python3
"""Headless collision check over every diagram SVG in html-suite.
 text-text : two <text> boxes overlap by >15% of the smaller box
 text-shape: a <text> box partly overlaps a non-text shape (line/path/rect/circle/ellipse/polygon) without being inside it
 Usage: check.py [--ta] [--json out.json]   (--ta substitutes the Tamil labels first, like render.py)"""
import json, glob, os, sys, collections
from playwright.sync_api import sync_playwright
HERE = os.path.dirname(os.path.abspath(__file__)); ROOT = os.path.join(HERE, '..', '..')
ta_mode = '--ta' in sys.argv
out = sys.argv[sys.argv.index('--json') + 1] if '--json' in sys.argv else None
inv = collections.defaultdict(list)
for f in glob.glob(os.path.join(ROOT, 'docs/audit/diagrams-*.json')):
    j = json.load(open(f, encoding='utf8'))
    for x in (j if isinstance(j, list) else j.get('diagrams', j)): inv[x['file']].append(x)
TA = json.load(open(os.path.join(HERE, 'ta_strings.json'), encoding='utf8')) if ta_mode else {}
FONT = 'file://' + os.path.abspath(os.path.join(ROOT, 'app/src/main/res/font/notosanstamil_regular.ttf'))
JS = """([i, ta, font]) => { const s=document.querySelectorAll('svg')[i]; if(!s) return null;
 let e=s; while(e&&e!==document.documentElement){const cs=getComputedStyle(e); if(cs.display==='none')e.style.setProperty('display','block','important'); e=e.parentElement;}
 if(ta){ const st=document.createElement('style'); st.textContent=`@font-face{font-family:TamilD;src:url(${font})}`; document.head.appendChild(st);
   const w=document.createTreeWalker(s,NodeFilter.SHOW_TEXT); let n; while(n=w.nextNode()){const t=n.nodeValue.replace(/\\s+/g,' ').trim(); if(ta[t]){n.nodeValue=n.nodeValue.replace(t,ta[t]); const x=n.parentNode.closest('text'); if(x)x.style.setProperty('font-family','TamilD,sans-serif','important');}} }
 const R=el=>{const r=el.getBoundingClientRect(); return {x:r.left,y:r.top,w:r.width,h:r.height,r:r.right,b:r.bottom}};
 const texts=[...s.querySelectorAll('text')].filter(t=>t.textContent.trim().length>2&&R(t).w>0).map(t=>({t:t.textContent.trim().slice(0,40),...R(t)}));
 const shapes=[...s.querySelectorAll('line,path,rect,circle,ellipse,polygon,polyline')].filter(x=>!x.closest('defs,marker,clipPath,pattern')).map(x=>({k:x.tagName,...R(x)})).filter(r=>r.w>0||r.h>0);
 const inter=(a,b)=>Math.max(0,Math.min(a.r,b.r)-Math.max(a.x,b.x))*Math.max(0,Math.min(a.b,b.b)-Math.max(a.y,b.y));
 const out=[]; 
 for(let a=0;a<texts.length;a++)for(let b=a+1;b<texts.length;b++){const A=texts[a],B=texts[b]; const o=inter(A,B); const m=Math.min(A.w*A.h,B.w*B.h); if(m>0&&o/m>0.15)out.push(['text-text',A.t,B.t]);}
 for(const A0 of texts){ const mx=A0.w*0.12,my=A0.h*0.22; const A={...A0,x:A0.x+mx,y:A0.y+my,w:A0.w-2*mx,h:A0.h-2*my,r:A0.r-mx,b:A0.b-my}; const aa=A.w*A.h; for(const S of shapes){ const o=inter(A,S); if(o<=0)continue; const sa=S.w*S.h;
   const inside=o/aa>0.9; // text inside the shape (a label on a box)
   if(inside) continue;
   if(sa>aa*6 && o/aa<0.2) continue; // glancing contact with a large panel
   if(sa>aa*6) continue; // text sits on a big panel/background
   if(o/Math.min(aa,sa||1)>0.12 || (S.w<3||S.h<3)&&o>0) out.push(['text-shape',A0.t,S.k]); } }
 return out; }"""
res = {}
with sync_playwright() as p:
    b = p.chromium.launch(executable_path='/usr/bin/google-chrome', args=['--no-sandbox'])
    pg = b.new_page(viewport={'width': 960, 'height': 900})
    for fn in sorted(inv):
        pg.goto('file://' + os.path.abspath(os.path.join(ROOT, 'html-suite', fn))); pg.wait_for_timeout(300)
        for i in range(len(inv[fn])):
            r = pg.evaluate(JS, [i, TA, FONT]) or []
            if ta_mode: pg.reload(); pg.wait_for_timeout(200)
            res[f"{fn[:-5]}-d{i+1:02d}"] = r
    b.close()
tot = sum(len(v) for v in res.values()); bad = [k for k, v in res.items() if v]
print(('TA' if ta_mode else 'EN'), 'collisions:', tot, 'in', len(bad), 'of', len(res), 'diagrams')
tt = sum(1 for v in res.values() for c in v if c[0] == 'text-text'); print(' text-text', tt, ' text-shape', tot - tt)
if out: json.dump(res, open(out, 'w', encoding='utf8'), ensure_ascii=False, indent=0)
