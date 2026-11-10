#!/usr/bin/env python3
"""Render every source diagram to PNG in Chrome: <id>.png (English) and <id>_ta.png (Tamil labels from ta_strings.json).
Usage: render.py [--ids a,b] [--en-only]   Existing hand-art ids (art.py) are skipped."""
import json, glob, os, re, sys, collections
from playwright.sync_api import sync_playwright
HERE = os.path.dirname(os.path.abspath(__file__)); ROOT = os.path.join(HERE, '..', '..')
OUT = os.path.join(ROOT, 'app/src/main/assets/diagrams')
FONT = 'file://' + os.path.abspath(os.path.join(ROOT, 'app/src/main/res/font/notosanstamil_regular.ttf'))
FONTB = 'file://' + os.path.abspath(os.path.join(ROOT, 'app/src/main/res/font/notosanstamil_bold.ttf'))
sys.path.insert(0, HERE)
from art import DIAGRAMS as ART
ta = json.load(open(os.path.join(HERE, 'ta_strings.json'), encoding='utf8')) if os.path.exists(os.path.join(HERE, 'ta_strings.json')) else {}
args = sys.argv[1:]
only = set(args[args.index('--ids') + 1].split(',')) if '--ids' in args else None
en_only = '--en-only' in args
inv = collections.defaultdict(list)
for f in glob.glob(os.path.join(ROOT, 'docs/audit/diagrams-*.json')):
    j = json.load(open(f, encoding='utf8'))
    for x in (j if isinstance(j, list) else j.get('diagrams', j)): inv[x['file']].append(x)

SHOW = """(i) => { const s=document.querySelectorAll('svg')[i]; let e=s;
  while(e && e!==document.documentElement){ const cs=getComputedStyle(e);
    if(cs.display==='none'){e.style.setProperty('display','block','important');}
    if(cs.visibility==='hidden'){e.style.setProperty('visibility','visible','important');}
    if(parseFloat(cs.opacity)===0){e.style.setProperty('opacity','1','important');}
    if(cs.maxHeight==='0px'){e.style.setProperty('max-height','none','important');}
    e=e.parentElement; }
  document.querySelectorAll('*').forEach(x=>{const c=getComputedStyle(x).position; if((c==='fixed'||c==='sticky') && !x.contains(s)) x.style.setProperty('display','none','important');});
  s.scrollIntoView(); const r=s.getBoundingClientRect(); return [r.width,r.height]; }"""
TRANS = """([map, font, fontb]) => { const s = document.querySelectorAll('svg')[window.__i];
  const st=document.createElement('style'); st.textContent=`@font-face{font-family:TamilD;src:url(${font});font-weight:400}@font-face{font-family:TamilD;src:url(${fontb});font-weight:700}`; document.head.appendChild(st);
  const texts=[...s.querySelectorAll('text')]; const ow=new Map(); texts.forEach(t=>ow.set(t,t.getBBox().width));
  const w=document.createTreeWalker(s, NodeFilter.SHOW_TEXT); let n; const nodes=[]; while(n=w.nextNode()) nodes.push(n);
  let hit=0, miss=0; const touched=new Set();
  for(const n of nodes){ const t=n.nodeValue.replace(/\\s+/g,' ').trim(); if(!t||['STYLE','SCRIPT'].includes(n.parentNode.tagName)) continue;
    if(map[t]){ n.nodeValue=n.nodeValue.replace(t,map[t]); const text=n.parentNode.closest('text'); if(text){ text.style.setProperty('font-family','TamilD, sans-serif','important'); touched.add(text);} hit++; } else miss++; }
  touched.forEach(t=>{ const o=ow.get(t)||0; const nw=t.getBBox().width; if(o>0 && nw>o*1.02){ const fs=parseFloat(getComputedStyle(t).fontSize)||12; const f=Math.max(0.6,o/nw); t.style.setProperty('font-size',(fs*f)+'px','important'); } });
  return [hit,miss]; }"""

def main():
    os.makedirs(OUT, exist_ok=True)
    done = 0
    with sync_playwright() as p:
        b = p.chromium.launch(executable_path='/usr/bin/google-chrome', args=['--no-sandbox'])
        pg = b.new_page(viewport={'width': 960, 'height': 900}, device_scale_factor=2)
        for fn in sorted(inv):
            stem = fn[:-5]
            ids = [f"{stem}-d{i:02d}" for i in range(1, len(inv[fn]) + 1)]
            if only and not (only & set(ids)): continue
            pg.goto('file://' + os.path.abspath(os.path.join(ROOT, 'html-suite', fn))); pg.wait_for_timeout(400)
            for i, did in enumerate(ids):
                if did in ART or (only and did not in only): continue
                sz = pg.evaluate(SHOW, i)
                pg.set_viewport_size({'width': 960, 'height': max(900, int(sz[1]) + 80)}); pg.evaluate('(i)=>document.querySelectorAll("svg")[i].scrollIntoView()', i)
                if sz[0] < 20 or sz[1] < 20: print('SKIP tiny', did, sz); continue
                el = pg.locator('svg').nth(i)
                el.screenshot(path=os.path.join(OUT, did + '.png'))
                json.dump({'id': did, 'w': round(sz[0]), 'h': round(sz[1]), 'markers': []}, open(os.path.join(OUT, did + '.json'), 'w'))
                done += 1
                if not en_only and ta:
                    pg.evaluate("(i)=>{window.__i=i}", i)
                    hit, miss = pg.evaluate(TRANS, [ta, FONT, FONTB])
                    pg.wait_for_timeout(150)
                    el.screenshot(path=os.path.join(OUT, did + '_ta.png'))
                    pg.reload(); pg.wait_for_timeout(300)
        b.close()
    print('rendered', done)
main()
