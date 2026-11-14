#!/usr/bin/env python3
"""Tier 1 geometry gate over EVERY figure source: regenerate the SVG from gen_figure.py, then run build_figure's gate (writes only to a scratch dir).
Fails (exit 1) if any figure fails or if a regenerated SVG differs from the committed figure.svg (stale source).  Usage: gate_all.py [--out DIR] [id...]"""
import argparse, concurrent.futures as cf, json, os, subprocess, sys, tempfile
HERE = os.path.dirname(os.path.abspath(__file__)); ROOT = os.path.abspath(os.path.join(HERE, '..', '..')); SRC = os.path.join(HERE, 'src')
def one(i, out):
    d = os.path.join(SRC, i); g = os.path.join(d, 'gen_figure.py'); stale = None
    if os.path.exists(g):  # hand-authored static SVGs have no generator and are gated as they are
        new = subprocess.run([sys.executable, g], cwd=d, capture_output=True, text=True)
        if new.returncode: return i, 'gen_figure failed: ' + new.stderr[-300:], None
        cur = open(os.path.join(d, 'figure.svg'), encoding='utf8').read() if os.path.exists(os.path.join(d, 'figure.svg')) else ''
        if cur != new.stdout: open(os.path.join(d, 'figure.svg'), 'w', encoding='utf8').write(new.stdout); stale = 'svg regenerated (differed from committed)'
    a = os.path.join(out, 'assets'); u = os.path.join(out, 'audit'); os.makedirs(a, exist_ok=True); os.makedirs(u, exist_ok=True)
    r = subprocess.run([sys.executable, os.path.join(HERE, 'build_figure.py'), i, '--assets', a, '--audit', u], capture_output=True, text=True)
    return i, ('PASS' if r.returncode == 0 else 'FAIL ' + (r.stderr.strip().splitlines() or [''])[-1][:160]), stale
if __name__ == '__main__':
    ap = argparse.ArgumentParser(); ap.add_argument('ids', nargs='*'); ap.add_argument('--out', default=tempfile.mkdtemp(prefix='gateall_')); a = ap.parse_args()
    ids = a.ids or sorted(x for x in os.listdir(SRC) if os.path.isdir(os.path.join(SRC, x)))
    bad = 0; res = {}
    with cf.ThreadPoolExecutor(4) as ex:
        for i, v, stale in ex.map(lambda i: one(i, a.out), ids):
            res[i] = {'result': v, 'stale': stale}; print(f'{i}: {v}' + (f'  [{stale}]' if stale else ''))
            if not v.startswith('PASS'): bad += 1
            if stale: bad += 1
    json.dump(res, open(os.path.join(a.out, 'gate_all.json'), 'w'), indent=1)
    print(f'{len(ids)-bad} clean, {bad} problem(s); out={a.out}'); sys.exit(1 if bad else 0)
