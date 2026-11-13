#!/usr/bin/env python3
"""PCR copy-number doubling: table with a log-scale bar (bar length = n). Run: python3 gen_figure.py > figure.svg"""
import os, sys, json; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))
from svgkit import T, svg
from steplist import wrap_lines
S = json.load(open(os.path.join(here, 'strings.json')))
OFF = 56
def nl(k, w, bold, px): return max(wrap_lines(S[k]['en'], w, bold, px), wrap_lines(S[k]['ta'], w, bold, px))
B = [f'<g transform="translate(0,{OFF})">']
y = 18
B.append(T('h', 180, y, 'h', 'middle', 330, '#0b6f85', True)); y += nl('h', 330, True, 17) * 22 + 6
hl = max(nl('hc', 70, True, 14.5), nl('hk', 96, True, 14.5), nl('hb', 148, True, 14.5))
B.append(T('hc', 50, y, 'b', 'middle', 70, '#444', True)); B.append(T('hk', 138, y, 'b', 'middle', 96, '#444', True)); B.append(T('hb', 196, y, 'b', 'start', 148, '#444', True))
y += hl * 19 + 8
B.append(f'<line x1="14" y1="{y-10}" x2="346" y2="{y-10}" stroke="#90a4ae" stroke-width="2"/>')
rows = [(0, 'a0', 'k0'), (1, 'a1', 'k1'), (2, 'a2', 'k2'), (5, 'a5', 'k5'), (10, 'a10', 'k10'), (20, 'a20', 'k20'), (30, 'a30', 'k30')]
for n, a, k in rows:
    c = '#c62828' if n >= 20 else '#0b6f85'
    B.append(T(a, 50, y + 6, 'b', 'middle', 60, '#0b2a40', True)); B.append(T(k, 138, y + 6, 'b', 'middle', 96, c, True))
    B.append(f'<rect x="196" y="{y-10}" width="{max(4, n*4.6)}" height="22" rx="4" fill="{c}"/>')
    y += 34
B.append(T('f', 180, y + 4, 's', 'middle', 322, '#2b4a5a')); y += 4 + nl('f', 322, False, 13.5) * 17
B.append(T('g', 180, y + 14, 'b', 'middle', 322, '#0b2a40', True)); y += 14 + nl('g', 322, True, 14.5) * 19 + 8
B.append('</g>')
print(svg(360, int(y + 2 * OFF - 14), B))
