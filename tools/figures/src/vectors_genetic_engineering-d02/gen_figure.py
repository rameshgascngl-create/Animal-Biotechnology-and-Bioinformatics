#!/usr/bin/env python3
"""Geometry for vectors_genetic_engineering-d02 (Ti plasmid map + key). Run: python3 gen_figure.py > figure.svg
Ring map with no text on the ring; names sit in a colour-keyed key below. Angles run clockwise from the top.
T-DNA lies between RB (0 deg) and LB (240 deg, going round the left); onc genes lie inside the T-DNA; vir genes lie outside it."""
import os, sys, math; sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..'))
from svgkit import T, svg
DARK, TEAL, ONC, GREEN, BLUE, RED, GRAY = '#0b2a40', '#0ea5b7', '#0b5d6b', '#4caf82', '#2b7bb0', '#c24560', '#496570'
OFF = 56; CX, CY, R = 180, 138, 84
B = [f'<g transform="translate(0,{OFF})">']
def pt(a, r): a = math.radians(a); return CX + r * math.sin(a), CY - r * math.cos(a)
def arc(a1, a2, r, col, sw):
    x1, y1 = pt(a1, r); x2, y2 = pt(a2, r); large = 1 if (a2 - a1) % 360 > 180 else 0
    return f'<path d="M {x1:.1f} {y1:.1f} A {r} {r} 0 {large} 1 {x2:.1f} {y2:.1f}" fill="none" stroke="{col}" stroke-width="{sw}" stroke-linecap="butt"/>'
B.append(f'<circle cx="{CX}" cy="{CY}" r="{R}" fill="none" stroke="#d3dde3" stroke-width="16"/>')
B.append(arc(240, 360, R, TEAL, 16)); B.append(arc(262, 340, R - 17, ONC, 8))
B.append(arc(20, 85, R, GREEN, 16)); B.append(arc(150, 222, R, RED, 16))
ox, oy = pt(110, R); B.append(f'<circle cx="{ox:.1f}" cy="{oy:.1f}" r="12" fill="{BLUE}" stroke="#ffffff" stroke-width="3"/>')
for ang, k, dx in ((0, 'rb', 0), (240, 'lb', 0)):
    x1, y1 = pt(ang, R - 12); x2, y2 = pt(ang, R + 26)
    B.append(f'<line x1="{x1:.1f}" y1="{y1:.1f}" x2="{x2:.1f}" y2="{y2:.1f}" stroke="{DARK}" stroke-width="4"/>')
x, y = pt(0, R + 26); B.append(T('rb', x, y - 8, 'b', 'middle', 50, DARK, True))
x, y = pt(240, R + 26); B.append(T('lb', x - 6, y + 14, 'b', 'end', 50, DARK, True))
B.append(T('ti', CX, CY - 12, 'h', 'middle', 112, DARK)); B.append(T('kb', CX, CY + 46, 's', 'middle', 90, GRAY))
rows = [('t_tdna', 'd_tdna', TEAL, 3), ('t_onc', 'd_onc', ONC, 2), ('t_bord', 'd_bord', DARK, 3), ('t_vir', 'd_vir', RED, 3), ('t_opine', 'd_opine', GREEN, 2), ('t_ori', 'd_ori', BLUE, 1)]
y = CY + R + 56
for tk, dk, col, lines in rows:
    B.append(f'<rect x="24" y="{y-14}" width="16" height="16" rx="3" fill="{col}"/>')
    B.append(T(tk, 52, y, 'b', 'start', 284, col if col != GREEN else '#1f7a52', True))
    B.append(T(dk, 52, y + 24, 's', 'start', 284, '#071e2e')); y += 30 + lines * 22
B.append(T('note', 180, y + 6, 's', 'middle', 300, GRAY, False, True))
B.append('</g>')
print(svg(360, int(y + 6 + 2 * OFF + 14), B))
