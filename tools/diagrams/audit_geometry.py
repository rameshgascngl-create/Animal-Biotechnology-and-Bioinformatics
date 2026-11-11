#!/usr/bin/env python3
"""Phase-0 geometric audit of every source figure, English and Tamil, as the phone shows it.

Each SVG is forced to 360 CSS px wide (= 360 dp) and screenshotted at 3x (1080 px wide). The Tamil state is the
shipped one (renderer.TRANS: Noto Sans Tamil from app/src/main/res/font, including its shrink-to-fit pass).
Checks, all measured in the browser on the rendered DOM (screen px at 360 dp):
  outside      element extends beyond the viewBox
  margin       element inside the 4% safe margin (minor)
  text_text    two text boxes overlap (boxes trimmed to the ink area)
  text_stroke  a line/outline/ring/arrow stroke passes through a text box
  text_edge    text straddles the edge of a filled shape (partly in, partly out)
  covered      an opaque shape drawn later hides part of a text
  shape_overlap two filled shapes overlap without one containing the other
  stroke_cross two outlines cross over a length (>= 6 samples)
  arrow_text   connector, arrowhead or tick (thin/small shape) touches a text box at all
  small_text   rendered text below 11 sp at the chosen width (--width, default 360 dp)
Usage: audit_geometry.py [--out DIR] [--ids a,b]   writes DIR/<id>_en.png, <id>_ta.png and DIR/audit.json
This is a heuristic geometric check. It does not replace looking at the figure.
"""
import argparse, json, os, shutil, sys
from playwright.sync_api import sync_playwright
HERE = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, HERE)
from render import ROOT, inv, SHOW, TRANS, ta, FONT, FONTB, ART

FORCE_T = """(i) => { const s=document.querySelectorAll('svg')[i];
  s.style.setProperty('width','__W__px','important'); s.style.setProperty('height','auto','important');
  s.style.setProperty('max-width','none','important'); s.style.setProperty('min-width','0','important');
  s.style.setProperty('margin','0','important');
  let e=s.parentElement; while(e && e!==document.documentElement){ e.style.setProperty('overflow','visible','important'); e.style.setProperty('max-width','none','important'); e=e.parentElement; }
  s.scrollIntoView(); const r=s.getBoundingClientRect(); return [r.width,r.height]; }"""

MEASURE = r"""(i) => {
 const s=document.querySelectorAll('svg')[i]; const R=s.getBoundingClientRect(); const W=R.width, H=R.height;
 const vb=s.viewBox.baseVal; const k=W/((vb&&vb.width)||W);
 const skip=e=>!!e.closest('defs,marker,clipPath,mask,pattern,symbol');
 const scale=e=>{const m=e.getScreenCTM(); return m?Math.sqrt(Math.abs(m.a*m.d-m.b*m.c)):k;};
 const num=v=>{const x=parseFloat(v); return isNaN(x)?1:x;};
 const texts=[]; for(const t of s.querySelectorAll('text')){
   if(skip(t)) continue; const txt=t.textContent.replace(/\s+/g,' ').trim(); if(!txt) continue;
   const c=getComputedStyle(t); if(c.visibility==='hidden'||c.display==='none'||+c.opacity===0) continue;
   const r=t.getBoundingClientRect(); if(r.width<=0||r.height<=0) continue;
   const fs=(parseFloat(c.fontSize)||12)*scale(t);
   const top=r.top-R.top+r.height*0.08, bot=r.bottom-R.top-r.height*0.12;
   texts.push({el:t,txt,x0:r.left-R.left,x1:r.right-R.left,y0:top,y1:bot,sp:fs,u0:r.left-R.left,u1:r.right-R.left,v0:r.top-R.top,v1:r.bottom-R.top});
 }
 const shapes=[]; for(const e of s.querySelectorAll('rect,circle,ellipse,path,polygon,polyline,line')){
   if(skip(e)) continue; const c=getComputedStyle(e); if(c.visibility==='hidden'||c.display==='none') continue;
   const r=e.getBoundingClientRect(); const sw=c.stroke!=='none'?num(c.strokeWidth)*scale(e):0;
   const fillOn=c.fill!=='none' && num(c.fillOpacity)*num(c.opacity)>0.05;
   const strokeOn=c.stroke!=='none' && sw>0 && num(c.strokeOpacity)*num(c.opacity)>0.05;
   if(!fillOn&&!strokeOn) continue;
   const o={el:e,tag:e.tagName.toLowerCase(),x0:r.left-R.left,x1:r.right-R.left,y0:r.top-R.top,y1:r.bottom-R.top,
     fillOn,strokeOn,sw,opaque:fillOn&&num(c.fillOpacity)*num(c.opacity)>=0.85,
     closed:!['line','polyline'].includes(e.tagName.toLowerCase())};
   o.w=o.x1-o.x0; o.h=o.y1-o.y0; o.area=o.w*o.h;
   o.bg=fillOn&&o.w>=0.95*W&&o.h>=0.95*H; shapes.push(o);
 }
 // stroke sample points (client->svg-relative), step ~1.5 px
 for(const o of shapes){ o.pts=[]; if(!o.strokeOn||typeof o.el.getTotalLength!=='function') continue;
   let L=0; try{L=o.el.getTotalLength();}catch(x){} const m=o.el.getScreenCTM(); if(!m||!L) continue;
   const sc=Math.sqrt(Math.abs(m.a*m.d-m.b*m.c)); const n=Math.min(2500,Math.max(2,Math.ceil(L*sc/1.5)));
   for(let j=0;j<=n;j++){ const p=o.el.getPointAtLength(L*j/n); o.pts.push([m.a*p.x+m.c*p.y+m.e-R.left, m.b*p.x+m.d*p.y+m.f-R.top]); } }
 const sps=texts.map(t=>[+t.sp.toFixed(2),t.txt.length]);
 const defects=[]; const add=(kind,d)=>defects.push(Object.assign({kind},d));
 const tx=0.5, mx=0.04*W, my=0.04*H;
 for(const t of texts){
   if(t.x0<-tx||t.x1>W+tx||t.y0<-tx||t.y1>H+tx) add('outside',{what:'text',text:t.txt.slice(0,40)});
   else if(t.x0<mx||t.x1>W-mx||t.y0<my||t.y1>H-my) add('margin',{what:'text',text:t.txt.slice(0,40)});
   if(t.sp<11) add('small_text',{text:t.txt.slice(0,40),sp:+t.sp.toFixed(1)});
 }
 for(const o of shapes){ if(o.bg) continue; const g=o.sw/2;
   if(o.x0-g<-1.5||o.x1+g>W+1.5||o.y0-g<-1.5||o.y1+g>H+1.5) add('outside',{what:o.tag});
   else if(o.area>25 && (o.x0<mx*0.5||o.x1>W-mx*0.5||o.y0<my*0.5||o.y1>H-my*0.5)) add('margin',{what:o.tag}); }
 for(let a=0;a<texts.length;a++) for(let b=a+1;b<texts.length;b++){ const A=texts[a],B=texts[b];
   const ox=Math.min(A.x1,B.x1)-Math.max(A.x0,B.x0), oy=Math.min(A.y1,B.y1)-Math.max(A.y0,B.y0);
   if(ox>1.5&&oy>0.6) add('text_text',{text:A.txt.slice(0,28),other:B.txt.slice(0,28),ox:+ox.toFixed(1),oy:+oy.toFixed(1)}); }
 for(const t of texts){ const seenStroke=new Set();
   for(const o of shapes){ if(o.bg) continue;
     if(o.x1<t.x0-2||o.x0>t.x1+2||o.y1<t.y0-2||o.y0>t.y1+2) continue;
     if(o.pts.length){ let n=0; for(const p of o.pts) if(p[0]>=t.x0&&p[0]<=t.x1&&p[1]>=t.y0&&p[1]<=t.y1) n++;
       if(n>=2) { add('text_stroke',{text:t.txt.slice(0,32),shape:o.tag,samples:n}); } }
     if(o.fillOn && o.closed && typeof o.el.isPointInFill==='function'){
       const m=o.el.getScreenCTM(); if(!m) continue; const inv=m.inverse(); let hit=0,tot=0;
       for(let ix=0;ix<10;ix++) for(let iy=0;iy<3;iy++){ const x=t.x0+(t.x1-t.x0)*(ix+0.5)/10+R.left, y=t.y0+(t.y1-t.y0)*(iy+0.5)/3+R.top;
         const p=new DOMPoint(x,y).matrixTransform(inv); tot++; if(o.el.isPointInFill(p)) hit++; }
       const f=hit/tot; const later=!!(t.el.compareDocumentPosition(o.el)&Node.DOCUMENT_POSITION_FOLLOWING);
       if(f>0 && later && o.opaque) add('covered',{text:t.txt.slice(0,32),shape:o.tag,frac:+f.toFixed(2)});
       else if(f>0.08 && f<0.92) add('text_edge',{text:t.txt.slice(0,32),shape:o.tag,frac:+f.toFixed(2)});
     } } }
 // markers (arrowheads defined in <defs>) are not part of the element's own box: add each as a virtual shape at the line end
 const virt=[]; for(const o of shapes){ if(!o.pts.length) continue; const c=getComputedStyle(o.el);
   for(const [prop,idx] of [['markerEnd',o.pts.length-1],['markerStart',0]]){ const v=c[prop]; if(!v||v==='none') continue;
     const mm=v.match(/#([^")]+)/); const mk=mm&&s.querySelector('#'+CSS.escape(mm[1])); if(!mk) continue;
     const un=mk.getAttribute('markerUnits')==='userSpaceOnUse'?1:(o.sw||1);
     const mw=(mk.markerWidth.baseVal.value||3)*un, mh=(mk.markerHeight.baseVal.value||3)*un;
     const q=o.pts[idx], r2=idx? o.pts[idx-1] : o.pts[1]; const dx=q[0]-r2[0], dy=q[1]-r2[1], d=Math.hypot(dx,dy)||1;
     const cx=q[0]-(idx? dx/d*mw*0.5 : -dx/d*mw*0.5), cy=q[1]-(idx? dy/d*mw*0.5 : -dy/d*mw*0.5);
     const half=Math.max(mw,mh)/2; virt.push({tag:'marker',x0:cx-half,x1:cx+half,y0:cy-half,y1:cy+half,w:2*half,h:2*half,sw:0,pts:[],fillOn:true,closed:true,bbox:true,el:o.el}); } }
 // arrow_text: connectors, arrowheads and ticks (thin or small shapes) that touch a text's full (untrimmed) box,
 // except shapes that wholly contain the text (badges, labelled circles)
 for(const t of texts){ for(const o of shapes.concat(virt)){ if(o.bg) continue;
     const thin=Math.min(o.w,o.h)<=Math.max(8,o.sw*2.5)||Math.max(o.w,o.h)<=45; if(!thin) continue;
     if(o.x0<=t.u0+1&&o.x1>=t.u1-1&&o.y0<=t.v0+1&&o.y1>=t.v1-1) continue;
     if(o.x1<t.u0||o.x0>t.u1||o.y1<t.v0||o.y0>t.v1) continue;
     let hit=0;
     if(o.pts.length){ for(const p of o.pts) if(p[0]>=t.u0&&p[0]<=t.u1&&p[1]>=t.v0&&p[1]<=t.v1) hit++; }
     if(o.bbox) hit=1;
     if(!hit && o.fillOn && o.closed && typeof o.el.isPointInFill==='function'){ const m=o.el.getScreenCTM(); if(m){ const iv=m.inverse();
       for(let ix=0;ix<16;ix++) for(let iy=0;iy<6;iy++){ const x=t.u0+(t.u1-t.u0)*(ix+0.5)/16+R.left, y=t.v0+(t.v1-t.v0)*(iy+0.5)/6+R.top;
         if(o.el.isPointInFill(new DOMPoint(x,y).matrixTransform(iv))) hit++; } } }
     if(hit>=1 && !defects.some(d=>d.text===t.txt.slice(0,32)&&d.kind==='text_stroke'&&d.shape===o.tag)) add('arrow_text',{text:t.txt.slice(0,32),shape:o.tag,samples:hit}); } }
 const big=shapes.filter(o=>!o.bg&&o.area>60);
 for(let a=0;a<big.length;a++) for(let b=a+1;b<big.length;b++){ const A=big[a],B=big[b];
   const ox=Math.min(A.x1,B.x1)-Math.max(A.x0,B.x0), oy=Math.min(A.y1,B.y1)-Math.max(A.y0,B.y0); if(ox<=2||oy<=2) continue;
   const inter=ox*oy, small=Math.min(A.area,B.area); if(inter<0.08*small) continue;
   const contains=(P,Q)=>P.x0<=Q.x0+2&&P.y0<=Q.y0+2&&P.x1>=Q.x1-2&&P.y1>=Q.y1-2;
   if(contains(A,B)||contains(B,A)) continue;
   if(A.fillOn&&B.fillOn&&A.closed&&B.closed&&A.opaque&&B.opaque) add('shape_overlap',{a:A.tag,b:B.tag,frac:+(inter/small).toFixed(2)});
   else if(A.pts.length&&B.pts.length&&A.closed&&B.closed&&Math.max(A.w,A.h)>40&&Math.max(B.w,B.h)>40){
     let n=0; const g=B.sw/2+1; for(const p of A.pts){ if(p[0]<B.x0-g||p[0]>B.x1+g||p[1]<B.y0-g||p[1]>B.y1+g) continue;
       const m=B.el.getScreenCTM(); const q=new DOMPoint(p[0]+R.left,p[1]+R.top).matrixTransform(m.inverse()); if(B.el.isPointInStroke(q)) n++; }
     if(n>=6) add('stroke_cross',{a:A.tag,b:B.tag,samples:n}); } }
 return {W:+W.toFixed(1),H:+H.toFixed(1),k:+k.toFixed(3),texts:texts.length,shapes:shapes.length,minsp:sps.length?Math.min(...sps.map(x=>x[0])):null,sps,defects};
}"""

def main():
    ap = argparse.ArgumentParser(); ap.add_argument('--out', default='/tmp/audit'); ap.add_argument('--ids')
    ap.add_argument('--width', type=float, default=360, help='rendered figure width in dp (300 = real inline width at a 360 dp screen)')
    a = ap.parse_args(); FORCE = FORCE_T.replace('__W__', str(a.width)); only = set(a.ids.split(',')) if a.ids else None
    os.makedirs(a.out, exist_ok=True); rows = []
    with sync_playwright() as p:
        b = p.chromium.launch(executable_path=os.environ.get('CHROME_BIN') or shutil.which('google-chrome') or shutil.which('chromium'), args=['--no-sandbox'])
        pg = b.new_page(viewport={'width': 1200, 'height': 900}, device_scale_factor=3)
        for fn in sorted(inv):
            stem = fn[:-5]; ids = [f"{stem}-d{i:02d}" for i in range(1, len(inv[fn]) + 1)]
            if only and not (only & set(ids)): continue
            pg.goto('file://' + os.path.abspath(os.path.join(ROOT, 'html-suite', fn))); pg.wait_for_timeout(400)
            for i, did in enumerate(ids):
                if did in ART or (only and did not in only): continue
                try:
                    pg.evaluate(SHOW, i); sz = pg.evaluate(FORCE, i)
                    pg.set_viewport_size({'width': 1200, 'height': max(900, int(sz[1]) + 120)}); pg.evaluate('(i)=>document.querySelectorAll("svg")[i].scrollIntoView()', i)
                    el = pg.locator('svg').nth(i)
                    m = pg.evaluate(MEASURE, i); el.screenshot(path=os.path.join(a.out, did + '_en.png'))
                    rows.append({'id': did, 'lang': 'en', **m})
                    pg.evaluate("(i)=>{window.__i=i}", i); hit, miss = pg.evaluate(TRANS, [ta, FONT, FONTB]); pg.wait_for_timeout(150)
                    m = pg.evaluate(MEASURE, i); el.screenshot(path=os.path.join(a.out, did + '_ta.png'))
                    rows.append({'id': did, 'lang': 'ta', 'tamil_strings_hit': hit, 'tamil_strings_missed': miss, **m})
                except Exception as e:
                    print('FAILED', did, repr(e)[:200]); rows.append({'id': did, 'lang': '?', 'error': repr(e)[:300]})
                pg.reload(); pg.wait_for_timeout(300)
        b.close()
    json.dump(rows, open(os.path.join(a.out, 'audit.json'), 'w'), ensure_ascii=False, indent=1)
    print('audited', len(rows), 'figure-language states;', sum(1 for r in rows if 'error' in r), 'errors')

if __name__ == '__main__': main()
