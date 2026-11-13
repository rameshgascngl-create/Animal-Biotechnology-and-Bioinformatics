#!/usr/bin/env python3
"""Organ culture on a stainless-steel grid, side view, with a numbered key. Run: python3 gen_figure.py > figure.svg"""
import os, sys, json; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))
from svgkit import T, svg
from steplist import wrap_lines
S = json.load(open(os.path.join(here, 'strings.json')))
OFF = 56
def nl(k, w, bold): return max(wrap_lines(S[k]['en'], w, bold, 14.5), wrap_lines(S[k]['ta'], w, bold, 14.5))
B = [f'<g transform="translate(0,{OFF})">']
B.append(T('atm', 180, 16, 'b', 'middle', 320, '#0b6f85', True))
top = 16 + (nl('atm', 320, True) - 1) * 19 + 16
B.append(f'<rect x="14" y="{top}" width="332" height="215" rx="16" fill="#e6f4f8" fill-opacity="0.6" stroke="#7fb3c6" stroke-width="2.5" stroke-dasharray="7 5"/>')
dy = top + 50                                           # dish top
B.append(f'<rect x="40" y="{dy}" width="280" height="100" rx="12" fill="#eceff1" stroke="#546e7a" stroke-width="3"/>')
B.append(f'<rect x="44" y="{dy+34}" width="272" height="62" rx="8" fill="#ffb74d" fill-opacity="0.55"/>')          # culture medium
for lx in (122, 176, 232): B.append(f'<rect x="{lx}" y="{dy+43}" width="6" height="53" fill="#607d8b"/>')            # grid legs
B.append(f'<rect x="104" y="{dy+34}" width="152" height="9" rx="2" fill="#78909c"/>')                              # grid
B.append(f'<ellipse cx="180" cy="{dy+22}" rx="42" ry="12" fill="#a5d6a7" stroke="#2e7d32" stroke-width="3"/>')    # tissue
for i, (x, y) in enumerate([(180, dy + 22), (80, dy + 38), (292, dy + 66), (66, dy + 14)], 1):
    pass
marks = [(1, 180, dy + 22), (2, 84, dy + 16), (3, 270, dy + 70), (4, 180, dy + 126)]
B.append(f'<line x1="90" y1="{dy+22}" x2="106" y2="{dy+36}" stroke="#0b2a40" stroke-width="2"/>')
B.append(f'<line x1="180" y1="{dy+103}" x2="180" y2="{dy+116}" stroke="#0b2a40" stroke-width="2"/>')
for n, x, y in marks:
    B.append(f'<circle cx="{x}" cy="{y}" r="9.5" fill="#0b2a40"/>')
    B.append(f'<text data-num="{n}" x="{x}" y="{y+5}" text-anchor="middle" class="b" font-weight="700" fill="#ffffff"/>')
y = top + 215 + 24
keys = [('k1', 1), ('k2', 2), ('k3', 3), ('k4', 4)]
for k, n in keys:
    ln = nl(k, 272, True)
    B.append(f'<circle cx="36" cy="{y-5}" r="11" fill="#0b2a40"/>')
    B.append(f'<text data-num="{n}" x="36" y="{y}" text-anchor="middle" class="b" font-weight="700" fill="#ffffff"/>')
    B.append(T(k, 62, y, 'b', 'start', 272, '#0b2a40', True)); y += max(32, ln * 19 + 12)
B.append(T('note', 180, y + 8, 's', 'middle', 320, '#2b4a5a')); y += 8 + nl('note', 320, False) * 17 + 10
B.append('</g>')
print(svg(360, int(y + 2 * OFF - 30), B))
