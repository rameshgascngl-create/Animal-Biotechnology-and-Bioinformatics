#!/usr/bin/env python3
"""Geometry for atlas3_transgenic_medical-d02 (transgenic mouse, 8 steps). Run: python3 gen_figure.py > figure.svg
Vertical step list: numbered node, bold action, plain detail line; arrows join the nodes. Row height is set from the (conservative) wrapped line counts."""
import os, sys; sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..'))
from svgkit import T, svg, arrow
DARK, TEAL = '#0b2a40', '#0e8fb0'
OFF = 56
# (title key, title lines, detail key or None, detail lines) -- line counts sized for the longer (Tamil) wrap
ROWS = [('t1', 2, 'd1', 1), ('t2', 2, None, 0), ('t3', 3, 'd3', 1), ('t4', 2, 'd4', 1), ('t5', 2, 'd5', 2),
        ('t6', 1, 'd6', 2), ('t7', 1, 'd7', 2), ('t8', 2, 'd8', 1)]
B = [f'<g transform="translate(0,{OFF})">']
ys = []; y = 0
for tk, tl, dk, dl in ROWS:
    ys.append(y)
    h = 17 + (tl - 1) * 19 + (22 + (dl - 1) * 17 if dk else 0) + 22
    y += max(h, 58)
for i, ((tk, tl, dk, dl), y0) in enumerate(zip(ROWS, ys), 1):
    B.append(f'<circle cx="36" cy="{y0+12}" r="13" fill="{DARK}"/>')
    B.append(f'<text data-num="{i}" x="36" y="{y0+17}" text-anchor="middle" class="b" font-weight="700" fill="#ffffff"/>')
    B.append(T(tk, 62, y0 + 17, 'b', 'start', 272, DARK, True))
    if dk: B.append(T(dk, 62, y0 + 17 + (tl - 1) * 19 + 22, 's', 'start', 272, '#2b4a5a'))
    if i < len(ROWS): B.append(arrow(36, y0 + 29, 36, ys[i] - 6, TEAL, 2.5))
B.append('</g>')
print(svg(360, int(y + 2 * OFF - 30), B))
