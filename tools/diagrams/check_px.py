#!/usr/bin/env python3
"""Check rendered SVG text bounds in BOTH languages using the renderer's fonts.

Reports text/text overlaps and viewport clipping in CSS pixels. Bounding boxes
are conservative candidates, not proof of glyph intersection or scientific QA.
Exit 1 on candidates; never suppress a language, diagram or missing source.
"""
import argparse
import json
import os
import shutil
from pathlib import Path
from playwright.sync_api import sync_playwright
from render import ROOT, inv, SHOW, TRANS, ta, FONT, FONTB

MEASURE = r"""(i) => {
 const s=document.querySelectorAll('svg')[i], outer=s.getBoundingClientRect();
 const labels=[...s.querySelectorAll('text')].map((t,index)=>{
   const r=t.getBoundingClientRect(), c=getComputedStyle(t);
   return {index,text:t.textContent.trim(),x:r.x-outer.x,y:r.y-outer.y,w:r.width,h:r.height,
     visible:c.display!=='none' && c.visibility!=='hidden' && +c.opacity!==0};
 }).filter(t=>t.visible && t.text && t.w>0 && t.h>0);
 return {width:outer.width,height:outer.height,labels};
}"""

def defects(m, tolerance=1):
    out=[]
    for a in m['labels']:
        if a['x'] < -tolerance or a['y'] < -tolerance or a['x']+a['w'] > m['width']+tolerance or a['y']+a['h'] > m['height']+tolerance:
            out.append({'kind':'clipped','a':a['index'],'text':a['text']})
    for j,a in enumerate(m['labels']):
        for b in m['labels'][j+1:]:
            dx=min(a['x']+a['w'],b['x']+b['w'])-max(a['x'],b['x'])
            dy=min(a['y']+a['h'],b['y']+b['h'])-max(a['y'],b['y'])
            if dx>tolerance and dy>tolerance:
                out.append({'kind':'overlap','a':a['index'],'b':b['index'],'text':a['text'],'other':b['text'],'intersection':[round(dx,2),round(dy,2)]})
    return out

def main():
    ap=argparse.ArgumentParser();ap.add_argument('--out',default='/tmp/diagram-check.json');args=ap.parse_args()
    report={'method':'text bounding boxes, >1 CSS px intersection; not pixel glyph or shape/leader QA','languages':{}}
    with sync_playwright() as p:
        browser=p.chromium.launch(executable_path=os.environ.get('CHROME_BIN') or shutil.which('google-chrome') or shutil.which('chromium'),args=['--no-sandbox'])
        page=browser.new_page(viewport={'width':960,'height':900},device_scale_factor=2)
        for lang in ['en','ta']:
            rows=[]
            for fn in sorted(inv):
                for i in range(len(inv[fn])):
                    page.goto(Path(ROOT,'html-suite',fn).resolve().as_uri());page.wait_for_timeout(60)
                    did=f'{fn[:-5]}-d{i+1:02d}'
                    count=page.locator('svg').count()
                    if i>=count:
                        rows.append({'id':did,'defects':[{'kind':'missing-svg'}]});continue
                    page.evaluate(SHOW,i)
                    if lang=='ta':
                        page.evaluate('(i)=>window.__i=i',i);page.evaluate(TRANS,[ta,FONT,FONTB])
                    page.evaluate('()=>document.fonts.ready')
                    m=page.evaluate(MEASURE,i)
                    rows.append({'id':did,'defects':defects(m),'measurement':m})
            report['languages'][lang]={'diagrams':len(rows),'affected':sum(bool(r['defects']) for r in rows),'defects':sum(len(r['defects']) for r in rows),'rows':rows}
            print(lang,{k:v for k,v in report['languages'][lang].items() if k!='rows'},flush=True)
        browser.close()
    Path(args.out).write_text(json.dumps(report,ensure_ascii=False,indent=2))
    return int(any(v['defects'] for v in report['languages'].values()))

if __name__=='__main__': raise SystemExit(main())
