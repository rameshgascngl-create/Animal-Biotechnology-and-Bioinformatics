#!/usr/bin/env python3
"""Lists, for every figure in its shipped Tamil state, the text nodes that still contain Latin letters, plus how many
text nodes had no Tamil string at all (TRANS 'miss'). Permitted Latin (gene/organism/enzyme names, symbols, units,
acronyms) is judged by a person from the token list; this script only collects the evidence.
Usage: tamil_fallback_check.py [--out FILE]"""
import argparse, json, os, re, shutil, sys
from playwright.sync_api import sync_playwright
HERE = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, HERE)
from render import ROOT, inv, SHOW, TRANS, ta, FONT, FONTB, ART
COLLECT = """(i)=>{ const s=document.querySelectorAll('svg')[i]; const out=[];
  for(const t of s.querySelectorAll('text')){ const x=t.textContent.replace(/\\s+/g,' ').trim(); if(x && /[A-Za-z]{2,}/.test(x)) out.push(x); }
  const all=[...s.querySelectorAll('text')].map(t=>t.textContent.replace(/\\s+/g,' ').trim()).filter(Boolean).length; return [out, all]; }"""
def main():
    ap = argparse.ArgumentParser(); ap.add_argument('--out', default='/tmp/ta_latin.json'); a = ap.parse_args(); rows = []
    with sync_playwright() as p:
        b = p.chromium.launch(executable_path=os.environ.get('CHROME_BIN') or shutil.which('google-chrome') or shutil.which('chromium'), args=['--no-sandbox'])
        pg = b.new_page(viewport={'width': 1200, 'height': 900}, device_scale_factor=1)
        for fn in sorted(inv):
            stem = fn[:-5]; ids = [f"{stem}-d{i:02d}" for i in range(1, len(inv[fn]) + 1)]
            pg.goto('file://' + os.path.abspath(os.path.join(ROOT, 'html-suite', fn))); pg.wait_for_timeout(300)
            for i, did in enumerate(ids):
                if did in ART: continue
                pg.evaluate(SHOW, i); pg.evaluate("(i)=>{window.__i=i}", i)
                hit, miss = pg.evaluate(TRANS, [ta, FONT, FONTB]); latin, total = pg.evaluate(COLLECT, i)
                rows.append({'id': did, 'hit': hit, 'miss': miss, 'text_nodes': total, 'latin': latin})
                pg.reload(); pg.wait_for_timeout(200)
        b.close()
    json.dump(rows, open(a.out, 'w'), ensure_ascii=False, indent=1); print('figures', len(rows))
if __name__ == '__main__': main()
