#!/usr/bin/env python3
"""Tier 2 selection: the figures that are NEW in this batch = redrawn figures (they have a <id>.alt.json) whose PNG changed between --base and --head.
An explicit --figures list (workflow_dispatch) overrides the diff. Prints a comma-separated id list (possibly empty) on stdout; the reason goes to stderr.
A base that is unknown or all zeros (first push of a branch) falls back to HEAD~1, so a batch never re-runs earlier ones."""
import argparse, os, re, subprocess, sys
ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..')); D = 'app/src/main/assets/diagrams'
def git(*a): return subprocess.run(['git', *a], cwd=ROOT, capture_output=True, text=True)
ap = argparse.ArgumentParser(); ap.add_argument('--base', default=''); ap.add_argument('--head', default='HEAD'); ap.add_argument('--figures', default=''); a = ap.parse_args()
if a.figures.strip():
    ids = [x.strip() for x in a.figures.split(',') if x.strip()]; print('explicit list', file=sys.stderr)
else:
    base = a.base
    if not base or set(base) == {'0'} or git('cat-file', '-e', base + '^{commit}').returncode: base = a.head + '~1'; print('base unknown, using HEAD~1', file=sys.stderr)
    names = git('diff', '--name-only', base, a.head, '--', D).stdout.split()
    ids = sorted({re.sub(r'(_ta)?\.png$', '', os.path.basename(n)) for n in names if n.endswith('.png')})
    ids = [i for i in ids if os.path.exists(os.path.join(ROOT, D, i + '.alt.json'))]
    print(f'{len(ids)} redrawn figure(s) changed between {base[:10]} and {a.head[:10]}', file=sys.stderr)
print(','.join(ids))
