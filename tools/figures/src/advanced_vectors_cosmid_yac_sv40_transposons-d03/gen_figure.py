#!/usr/bin/env python3
"""Geometry for advanced_vectors_cosmid_yac_sv40_transposons-d03 (SV40 genome map and virion). Run: python3 gen_figure.py > figure.svg
Portrait: circular map above with colour-keyed rows instead of leader lines; virion below. Arc sizes are schematic, not to scale.
Content sits inside translate(0,72)."""
import math, os, sys; sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..'))
from svgkit import T, svg
RED, TEAL, GREEN, ORANGE, BLUE, DARK, GRAY = '#be3b57', '#0e7490', '#3fa37c', '#d9712e', '#2579a8', '#0b2a40', '#496570'
B = ['<g transform="translate(0,72)">']
cx, cy, r = 180, 100, 76
def pt(a, rr=r): t = math.radians(a); return cx + rr * math.sin(t), cy - rr * math.cos(t)
def arc(a0, a1, col, sw=18):
    x0, y0 = pt(a0); x1, y1 = pt(a1); large = 1 if (a1 - a0) % 360 > 180 else 0
    return f'<path d="M {x0:.1f} {y0:.1f} A {r} {r} 0 {large} 1 {x1:.1f} {y1:.1f}" fill="none" stroke="{col}" stroke-width="{sw}" stroke-linecap="round"/>'
B.append(f'<circle cx="{cx}" cy="{cy}" r="{r-17}" fill="none" stroke="{DARK}" stroke-width="3"/>')
B.append(arc(10, 168, RED)); B.append(arc(190, 336, TEAL)); B.append(arc(344, 358, GREEN, 18))
ox, oy = pt(0); B.append(f'<circle cx="{ox:.1f}" cy="{oy:.1f}" r="9" fill="{ORANGE}" stroke="#ffffff" stroke-width="2.5"/>')
B.append(T('c_sv', cx, cy - 4, 'b', 'middle', 84, DARK, True)); B.append(T('c_bp', cx, cy + 14, 's', 'middle', 84, GRAY)); B.append(T('c_ds', cx, cy + 32, 's', 'middle', 84, GRAY))
y = 222
rows = ((RED, 'r_early', 'd_early', 104), (TEAL, 'r_late', 'd_late', 68), (GREEN, 'r_reg', 'd_reg', 68), (ORANGE, 'r_ori', 'd_ori', 68))
for col, k, d, pitch in rows:
    if col == ORANGE: B.append(f'<circle cx="35" cy="{y-5}" r="8" fill="{col}"/>')
    else: B.append(f'<rect x="28" y="{y-12}" width="14" height="14" rx="3" fill="{col}"/>')
    B.append(T(k, 52, y, 's', 'start', 290, col if col != ORANGE else '#9a4a14', True)); B.append(T(d, 52, y + 20, 's', 'start', 290, '#071e2e'))
    y += pitch
B.append(T('note', 180, y + 4, 's', 'middle', 320, GRAY, False, True))
y += 46
B.append(f'<line x1="24" y1="{y}" x2="336" y2="{y}" stroke="#c9d6dc" stroke-width="1.5"/>')
B.append(T('t2', 180, y + 40, 'h', 'middle', 330, '#0b4a5a'))
# virion
vy = y + 150
pts = ' '.join(f'{180 + 78 * math.cos(math.radians(22.5 + 45 * i)):.1f},{vy + 78 * math.sin(math.radians(22.5 + 45 * i)):.1f}' for i in range(8))
B.append(f'<polygon points="{pts}" fill="#dbeafe" stroke="{BLUE}" stroke-width="4"/>')
B.append(f'<circle cx="180" cy="{vy}" r="46" fill="#ecfeff" stroke="{TEAL}" stroke-width="3"/>')
for i in range(8):
    a = math.radians(45 * i); B.append(f'<circle cx="{180 + 30 * math.cos(a):.1f}" cy="{vy + 30 * math.sin(a):.1f}" r="8.5" fill="{RED}" opacity="0.9"/>')
B.append(f'<circle cx="180" cy="{vy}" r="30" fill="none" stroke="{DARK}" stroke-width="2.5" stroke-dasharray="4 3"/>')
y = vy + 112
for col, k in ((BLUE, 'v_cap'), (BLUE, 'v_vp1'), (TEAL, 'v_mini'), (RED, 'v_hist')):
    if col == RED: B.append(f'<circle cx="35" cy="{y-5}" r="8" fill="{col}"/>')
    else: B.append(f'<rect x="28" y="{y-12}" width="14" height="14" rx="3" fill="{col}"/>')
    B.append(T(k, 52, y, 's', 'start', 290, '#071e2e', col == TEAL)); y += 56
B.append('</g>')
print(svg(360, int(y + 72 + 36), B))
