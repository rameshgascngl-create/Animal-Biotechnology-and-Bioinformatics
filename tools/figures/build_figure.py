#!/usr/bin/env python3
"""One geometry, two languages.

tools/figures/src/<id>/figure.svg      geometry only; every <text data-k="key"> takes its string from strings.json
tools/figures/src/<id>/strings.json    {"key": {"en": "...", "ta": "..."}, "_alt": {"en","ta"}}
Usage: build_figure.py <id> [--assets DIR] [--audit DIR]

For each language the script lays the text out with the real fonts (Inter for Latin, Noto Sans Tamil from the app's
res/font for Tamil; Latin inside Tamil strings also uses Inter), wrapping at data-w units by MEASURED width, then gates:
  - every wrapped line must fit its data-w
  - geometric check at the real inline width (300 dp, 3x): no structural defect, none inside the 4% margin, no text under 11 sp
and only then writes the PNGs (1440 px wide) and the alt-text JSON. A failed gate exits non-zero and writes nothing.
Classes: .h 17, .b 14.5, .s 13.5 units on a 360-unit canvas (13.5 x 300/360 = 11.25 sp).
"""
import argparse, json, os, re, shutil, sys
from playwright.sync_api import sync_playwright
HERE = os.path.dirname(os.path.abspath(__file__)); ROOT = os.path.abspath(os.path.join(HERE, '..', '..'))
sys.path.insert(0, os.path.join(ROOT, 'tools', 'diagrams'))
from audit_geometry import MEASURE, FORCE_T
INTER = '/usr/share/fonts/opentype/inter/Inter-Regular.otf', '/usr/share/fonts/opentype/inter/Inter-Bold.otf'
TAMIL = (os.path.join(ROOT, 'app/src/main/res/font/notosanstamil_regular.ttf'), os.path.join(ROOT, 'app/src/main/res/font/notosanstamil_bold.ttf'))
STRUCT = {'outside', 'text_text', 'text_stroke', 'text_edge', 'covered', 'shape_overlap', 'stroke_cross', 'arrow_text', 'margin'}
MIN_SP = 11.0
MAX_SIDE_PX = 4096
CSS = """@font-face{font-family:FigEN;src:url(file://%s);font-weight:400}@font-face{font-family:FigEN;src:url(file://%s);font-weight:700}
@font-face{font-family:FigTA;src:url(file://%s);font-weight:400}@font-face{font-family:FigTA;src:url(file://%s);font-weight:700}
html,body{margin:0;background:#fff} svg{display:block} svg text{font-family:FigEN,FigTA,sans-serif}
svg .h{font-size:17px;font-weight:700} svg .b{font-size:14.5px} svg .s{font-size:13.5px}"""
LAYOUT = r"""async ([S, lang]) => {
  await document.fonts.load('14px FigEN'); await document.fonts.load('700 14px FigEN');
  await document.fonts.load('14px FigTA'); await document.fonts.load('700 14px FigTA'); await document.fonts.ready;
  const svg = document.querySelector('svg'); const ns = 'http://www.w3.org/2000/svg'; const issues = [];
  for (const t of svg.querySelectorAll('text')) {
    if (t.dataset.num) { t.textContent = t.dataset.num; continue; }
    const k = t.dataset.k; if (!k) continue;
    if (!S[k] || !S[k][lang]) { issues.push('missing string ' + k + '/' + lang); continue; }
    const maxw = parseFloat(t.dataset.w) || 0; const x = t.getAttribute('x');
    const lh = 1.3 * parseFloat(getComputedStyle(t).fontSize); const paras = S[k][lang].split('\n');
    const probe = (s) => { t.textContent = s; return t.getComputedTextLength(); };
    const lines = [];
    for (const para of paras) { let cur = '';
      for (const w of para.split(' ')) { const nxt = cur ? cur + ' ' + w : w; if (maxw && cur && probe(nxt) > maxw) { lines.push(cur); cur = w; } else cur = nxt; }
      lines.push(cur); }
    t.textContent = '';
    lines.forEach((l, i) => { const ts = document.createElementNS(ns, 'tspan'); ts.setAttribute('x', x); if (i) ts.setAttribute('dy', lh); ts.textContent = l; t.appendChild(ts); });
    if (maxw) for (const ts of t.querySelectorAll('tspan')) { const w = ts.getComputedTextLength(); if (w > maxw + 0.5) issues.push(k + ': line ' + Math.round(w) + ' > ' + maxw); }
  }
  return issues; }"""

def main():
    ap = argparse.ArgumentParser(); ap.add_argument('id'); ap.add_argument('--assets', default=os.path.join(ROOT, 'app/src/main/assets/diagrams'))
    ap.add_argument('--audit', default=os.path.join(ROOT, 'docs/audit/pilot')); a = ap.parse_args()
    src = os.path.join(HERE, 'src', a.id); svg = open(os.path.join(src, 'figure.svg'), encoding='utf8').read()
    S = json.load(open(os.path.join(src, 'strings.json'), encoding='utf8'))
    TERMS = json.load(open(os.path.join(ROOT, 'tools', 'i18n', 'figure_terms.json'), encoding='utf8'))  # one place for terms whose wording is still under review
    def sub(v):
        for k, t in TERMS.items(): v = v.replace('\u27e6' + k + '\u27e7', t)
        if '\u27e6' in v: sys.exit('unresolved term token in: ' + v)
        return v
    S = {k: {l: sub(t) for l, t in v.items()} for k, v in S.items()}
    for f in INTER + TAMIL:
        if not os.path.exists(f): sys.exit('missing font ' + f)
    html = '<html><head><meta charset="utf-8"><style>' + CSS % (INTER + TAMIL) + '</style></head><body>' + svg + '</body></html>'
    tmp = os.path.join('/tmp', 'fig_' + a.id + '.html'); open(tmp, 'w', encoding='utf8').write(html)
    vb = [float(v) for v in re.search(r'viewBox="([^"]+)"', svg).group(1).split()]
    scale = min(4.0, MAX_SIDE_PX / (vb[3] * 360.0 / vb[2]))  # 4x unless that would put a side over the 4096 px texture limit (AssetSizeTest)
    scale = int(scale * 100) / 100.0
    os.makedirs(a.audit, exist_ok=True); report = {}; ok = True; out = []
    with sync_playwright() as p:
        b = p.chromium.launch(executable_path=os.environ.get('CHROME_BIN') or shutil.which('google-chrome') or shutil.which('chromium'), args=['--no-sandbox'])
        for lang, suffix in (('en', ''), ('ta', '_ta')):
            pg = b.new_page(viewport={'width': 800, 'height': 1000}, device_scale_factor=3); pg.goto('file://' + tmp)
            issues = pg.evaluate(LAYOUT, [S, lang])
            pg.evaluate(FORCE_T.replace('__W__', '300'), 0); m = pg.evaluate(MEASURE, 0)
            bad = [d for d in m['defects'] if d['kind'] in STRUCT]; small = [d for d in m['defects'] if d['kind'] == 'small_text']
            report[lang] = {'layout_issues': issues, 'structural_defects': bad, 'small_text': small, 'min_sp': m['minsp'], 'texts': m['texts'], 'inline_height_dp': m['H']}
            if issues or bad or (m['minsp'] or 0) < MIN_SP - 0.01: ok = False
            pg.locator('svg').screenshot(path=os.path.join(a.audit, f'{a.id}_after{suffix}_300dp.png'))
            pg.evaluate(FORCE_T.replace('__W__', '360'), 0); pg.close()
            pg = b.new_page(viewport={'width': 800, 'height': 1000}, device_scale_factor=scale); pg.goto('file://' + tmp); pg.evaluate(LAYOUT, [S, lang])
            pg.evaluate(FORCE_T.replace('__W__', '360'), 0); out.append((lang, suffix, pg.locator('svg').screenshot(type='png'))); pg.close()
        b.close()
    json.dump(report, open(os.path.join(a.audit, a.id + '-geometry.json'), 'w'), ensure_ascii=False, indent=1)
    print(json.dumps({k: {x: (v[x] if x in ('min_sp', 'inline_height_dp', 'texts') else len(v[x])) for x in ('layout_issues', 'structural_defects', 'small_text', 'min_sp', 'texts', 'inline_height_dp')} for k, v in report.items()}))
    if not ok: sys.exit('GATE FAILED (see ' + os.path.join(a.audit, a.id + '-geometry.json') + '); no asset written')
    for lang, suffix, png in out: open(os.path.join(a.assets, a.id + suffix + '.png'), 'wb').write(png)
    json.dump({'en': S['_alt']['en'], 'ta': S['_alt']['ta']}, open(os.path.join(a.assets, a.id + '.alt.json'), 'w'), ensure_ascii=False)
    print('wrote assets for', a.id)
if __name__ == '__main__': main()
