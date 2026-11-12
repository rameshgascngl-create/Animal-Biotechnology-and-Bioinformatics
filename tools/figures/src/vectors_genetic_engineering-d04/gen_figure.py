#!/usr/bin/env python3
"""Geometry for vectors_genetic_engineering-d04 (M13 phage particle and genome). Run: python3 gen_figure.py > figure.svg
Portrait: particle (horizontal rod) above, genome ring below, labels in colour-keyed rows instead of leader lines.
Arc lengths follow the original schematic (not to scale). Content sits inside translate(0,46)."""
import math, os, sys; sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..'))
from svgkit import T, svg
BLUE, GREEN, ORANGE, RED, TEAL, DARK = '#2579a8', '#3fa37c', '#d9712e', '#be3b57', '#12a3b0', '#0b2a40'
B = ['<g transform="translate(0,46)">']
# ---- particle
B.append(T('len', 180, 22, 's', 'middle', 300, '#496570'))
B.append('<line x1="40" y1="36" x2="320" y2="36" stroke="#496570" stroke-width="2" stroke-dasharray="5 4"/><line x1="40" y1="30" x2="40" y2="42" stroke="#496570" stroke-width="2"/><line x1="320" y1="30" x2="320" y2="42" stroke="#496570" stroke-width="2"/>')
B.append('<rect x="40" y="54" width="280" height="44" rx="22" fill="#dcebf7" stroke="#2579a8" stroke-width="3.5"/>')
B.append(f'<rect x="40" y="54" width="48" height="44" rx="22" fill="{RED}" stroke="#7f1d2d" stroke-width="2.5"/>')
B.append(f'<rect x="272" y="54" width="48" height="44" rx="22" fill="{GREEN}" stroke="#1f6b4c" stroke-width="2.5"/>')
for x in range(104, 262, 16): B.append(f'<line x1="{x}" y1="58" x2="{x}" y2="94" stroke="{BLUE}" stroke-width="2.5"/>')
B.append(f'<path d="M 96 76 C 120 56 140 96 164 76 S 208 56 232 76 S 258 90 266 76" fill="none" stroke="{TEAL}" stroke-width="3.5"/>')
B.append(T('tip_a', 40, 122, 's', 'start', 120, RED, True)); B.append(T('tip_b', 320, 122, 's', 'end', 120, GREEN, True))
B.append(T('wide', 180, 122, 's', 'middle', 90, '#496570'))
rows = (('r_tip', RED, 'tip'), ('r_coat', BLUE, 'coat'), ('r_dna', TEAL, 'dna'))
y = 160
for i, (k, c, _) in enumerate(rows):
    B.append(f'<rect x="28" y="{y-12}" width="14" height="14" rx="3" fill="{c}"/>'); B.append(T(k, 52, y, 's', 'start', 290, '#071e2e')); y += 42
# ---- genome
B.append(T('t2', 180, 318, 'h', 'middle', 330, '#0b4a5a'))
cx, cy, r = 180, 440, 78
def pt(a, rr=r): t = math.radians(a); return cx + rr * math.sin(t), cy - rr * math.cos(t)
def arc(a0, a1, col, sw=17):
    x0, y0 = pt(a0); x1, y1 = pt(a1); large = 1 if (a1 - a0) % 360 > 180 else 0
    return f'<path d="M {x0:.1f} {y0:.1f} A {r} {r} 0 {large} 1 {x1:.1f} {y1:.1f}" fill="none" stroke="{col}" stroke-width="{sw}" stroke-linecap="round"/>'
B.append(f'<circle cx="{cx}" cy="{cy}" r="{r-16}" fill="none" stroke="{DARK}" stroke-width="3.5"/>')
B.append(arc(2, 84, GREEN)); B.append(arc(98, 152, ORANGE)); B.append(arc(160, 200, RED)); B.append(arc(208, 352, BLUE))
mx, my = pt(91); B.append(f'<circle cx="{mx:.1f}" cy="{my:.1f}" r="9" fill="{ORANGE}" stroke="#ffffff" stroke-width="2.5"/>')
B.append(T('c_m13', cx, cy - 6, 'b', 'middle', 90, DARK, True)); B.append(T('c_ss', cx, cy + 14, 's', 'middle', 90, '#496570')); B.append(T('c_nt', cx, cy + 34, 's', 'middle', 90, '#496570'))
y = 566
for k1, k2, c in (('g_v', 'g_v2', GREEN), ('g_m', 'g_m2', ORANGE), ('g_2', 'g_22', ORANGE), ('g_36', 'g_362', RED), ('g_8', 'g_82', BLUE)):
    if k1 == 'g_m': B.append(f'<circle cx="35" cy="{y-5}" r="8" fill="{c}"/>')
    else: B.append(f'<rect x="28" y="{y-12}" width="14" height="14" rx="3" fill="{c}"/>')
    B.append(T(k1, 52, y, 's', 'start', 290, c if c != ORANGE else '#9a4a14', True)); B.append(T(k2, 52, y + 19, 's', 'start', 290, '#496570')); y += 50
B.append('</g>')
print(svg(360, 880, B))
