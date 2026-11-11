#!/usr/bin/env python3
"""Open every source page in Chrome and harvest the text strings in each diagram SVG -> tools/diagrams/strings.json"""
import json, glob, os, collections, sys
from playwright.sync_api import sync_playwright
ROOT = os.path.join(os.path.dirname(__file__), '..', '..')
inv = collections.defaultdict(list)
for f in glob.glob(os.path.join(ROOT, 'docs/audit/diagrams-*.json')):
    j = json.load(open(f, encoding='utf8')); items = j if isinstance(j, list) else j.get('diagrams', j)
    for x in items: inv[x['file']].append(x)
JS = """() => [...document.querySelectorAll('svg')].map(s => {
  const out=[]; const w=document.createTreeWalker(s, NodeFilter.SHOW_TEXT);
  let n; while(n=w.nextNode()){ const t=n.nodeValue.replace(/\\s+/g,' ').trim(); if(t && !['STYLE','SCRIPT'].includes(n.parentNode.tagName)) out.push(t); }
  return out; })"""
res = {}
with sync_playwright() as p:
    b = p.chromium.launch(executable_path='/usr/bin/google-chrome', args=['--no-sandbox'])
    pg = b.new_page()
    for fn in sorted(inv):
        pg.goto('file://' + os.path.abspath(os.path.join(ROOT, 'html-suite', fn))); pg.wait_for_timeout(300)
        for i, texts in enumerate(pg.evaluate(JS), 1):
            res[f"{fn[:-5]}-d{i:02d}"] = texts
    b.close()
json.dump(res, open(os.path.join(os.path.dirname(__file__), 'strings.json'), 'w', encoding='utf8'), ensure_ascii=False, indent=0)
u = collections.Counter(t for v in res.values() for t in v)
print(len(res), 'diagrams', sum(len(v) for v in res.values()), 'strings', len(u), 'unique')
import re
need = [t for t in u if re.search('[A-Za-z]{3,}', t)]
print(len(need), 'unique strings with words')
