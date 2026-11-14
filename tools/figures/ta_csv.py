#!/usr/bin/env python3
"""Tamil review CSV for converted figures. export: writes one CSV (figure_id, step, part, key, EN, TA, TA_status, TA_legacy).
Boxes that wrapped over several lines carry a whole-box draft translation (ta_drafts_<set>.json) in TA, marked 'draft, needs RR review'; the rest keep the
current TA ('current'). import: reads the CSV back, writes TA into each figure's strings.json, and refuses rows whose EN Latin tokens (numbers, symbols,
abbreviations) are missing from TA. Re-render with build_figure.py afterwards.
Usage: ta_csv.py export <drafts.json> <out.csv> id [id...] | ta_csv.py import <csv>"""
import csv, json, os, re, sys
HERE = os.path.dirname(os.path.abspath(__file__)); SRC = os.path.join(HERE, 'src')
def LAT(en):
    # technical Latin tokens that must survive translation: contain a digit or >=2 capitals (DNA, ddNTPs, Oct4, ampR, 3'-OH)
    out = set()
    for t in re.split(r'[\s;,:]+', en):
        t = t.strip('()[]')
        if t and re.search(r'[A-Za-z]', t) and (re.search(r'\d', t) or len(re.findall(r'[A-Z]', t)) >= 2): out.add(re.sub(r'-[a-z]+$', '', re.sub(r'(?<=[A-Z])s$', '', t.strip('.'))))
    return out
def rows_of(i):
    S = json.load(open(f'{SRC}/{i}/strings.json', encoding='utf8')); out = []
    for k in S:
        if k == '_alt': continue
        m = re.fullmatch(r'([tdfh])(\d*)', k); part = {'t': 'title', 'd': 'detail', 'f': 'footer', 'h': 'heading'}[m.group(1)]
        out.append((int(m.group(2) or 0) if m.group(1) in 'td' else 0, part, k))
    return S, sorted(out, key=lambda r: (r[0], r[1] != 'title'))
if sys.argv[1] == 'export':
    drafts = json.load(open(sys.argv[2], encoding='utf8')); w = csv.writer(open(sys.argv[3], 'w', encoding='utf-8-sig', newline=''))
    w.writerow(['figure_id', 'step', 'part', 'key', 'EN', 'TA', 'TA_status', 'TA_legacy_joined_fragments'])
    for i in sys.argv[4:]:
        S, rs = rows_of(i); fr = json.load(open(f'{SRC}/{i}/frags.json', encoding='utf8'))
        for step, part, k in rs:
            d = drafts.get(i, {}).get(k)
            w.writerow([i, step or '', part, k, S[k]['en'], d or S[k]['ta'], 'draft, needs RR review' if d else 'current', S[k]['ta'] if d else ''])
else:
    n = 0; bad = []; by = {}
    for r in csv.DictReader(open(sys.argv[2], encoding='utf-8-sig')):
        ta = r['TA'].replace('⟦vec⟧', 'VEC'); miss = {t for t in LAT(r['EN']) if t not in ta}
        if miss: bad.append((r['figure_id'], r['key'], sorted(miss)))
        by.setdefault(r['figure_id'], {})[r['key']] = r['TA'].strip()
    if bad: sys.exit('REFUSED, Latin tokens of EN missing in TA: ' + str(bad[:6]))
    for i, d in by.items():
        p = f'{SRC}/{i}/strings.json'; S = json.load(open(p, encoding='utf8'))
        for k, t in d.items():
            if not t: sys.exit(f'empty TA for {i} {k}')
            if S[k]['ta'] != t: S[k]['ta'] = t; n += 1
        a = S['_alt']; json.dump(S, open(p, 'w', encoding='utf8'), ensure_ascii=False, indent=1)
    print('imported', n, 'changed strings in', len(by), 'figures')
