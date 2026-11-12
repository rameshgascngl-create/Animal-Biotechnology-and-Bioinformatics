#!/usr/bin/env python3
"""Geometry for screening_selection_recombinants-d01 (direct selection on ampicillin). Run: python3 gen_figure.py > figure.svg
The audit margin is 4% of the canvas height, so the content sits inside translate(0,44) with matching padding below."""
import os, sys; sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..'))
from svgkit import T, svg, arrow
GREEN, BLUE, ORANGE, RED, GRAY = '#047857', '#0e8fb0', '#d97706', '#be123c', '#496570'
B = ['<g transform="translate(0,44)">']
B.append(T('t1', 180, 22, 'h'))
for cx, col, fill, k in ((70, BLUE, '#b6ecf5', 'm_rdna'), (180, ORANGE, '#fef3c7', 'm_vec'), (290, RED, '#ffe4e6', 'm_none')):
    B.append(f'<circle cx="{cx}" cy="64" r="24" fill="{fill}" stroke="{col}" stroke-width="4"/>')
    B.append(T(k, cx, 108, 's', 'middle', 124 if cx == 180 else 96, '#071e2e', True))
B.append(arrow(180, 124, 180, 154)); B.append(T('s_plate', 196, 144, 's', 'start', 150, '#0b6f85'))
def plate(cy, fill, edge, dots):
    B.append(f'<ellipse cx="180" cy="{cy}" rx="116" ry="40" fill="{fill}" stroke="{edge}" stroke-width="4"/>')
    for x, y, c in dots: B.append(f'<circle cx="{x}" cy="{cy+y}" r="12" fill="{c}"/>')
B.append(T('t2', 180, 186, 'h', 'middle', 330))
plate(246, '#fff3c4', ORANGE, ((124, -12, GREEN), (160, -22, GREEN), (200, -14, GREEN), (236, -6, GREEN), (140, 10, BLUE), (214, 12, BLUE), (178, 2, GREEN)))
for x, y in ((92, 238), (98, 262), (268, 238), (262, 262)): B.append(f'<text x="{x}" y="{y}" text-anchor="middle" class="h" fill="{RED}">×</text>')
B.append(T('a_die', 180, 312, 's', 'middle', 330, RED, True))
B.append(T('a_surv', 180, 336, 's', 'middle', 330, GREEN, True))
B.append(T('a_both', 180, 372, 's', 'middle', 330, ORANGE, True))
B.append(T('t3', 180, 442, 'h', 'middle', 330))
plate(512, '#d1fae5', GREEN, ((126, -12, GREEN), (160, -22, GREEN), (204, -14, GREEN), (238, -4, GREEN), (140, 10, BLUE), (178, 8, ORANGE), (214, 12, BLUE), (254, 20, ORANGE)))
B.append(T('b_all', 180, 580, 's', 'middle', 330, GREEN, True))
B.append(T('b_ctrl', 180, 616, 's', 'middle', 330, GRAY))
B.append('<line x1="24" y1="668" x2="336" y2="668" stroke="#c9d6dc" stroke-width="1.5"/>')
for i, (c, k) in enumerate(((GREEN, 'g_trans'), (BLUE, 'g_rec'), (ORANGE, 'g_non'))):
    y = 698 + i * 42
    B.append(f'<circle cx="38" cy="{y-5}" r="10" fill="{c}"/>'); B.append(T(k, 58, y, 's', 'start', 282, '#071e2e'))
B.append(f'<text x="38" y="{698+3*42-1}" text-anchor="middle" class="b" font-weight="700" fill="{RED}">×</text>'); B.append(T('g_x', 58, 698 + 3 * 42, 's', 'start', 282, '#071e2e'))
B.append('<rect x="14" y="858" width="332" height="118" rx="10" fill="#fff7ed" stroke="#d97706" stroke-width="2"/>')
B.append(T('note', 28, 884, 's', 'start', 304, '#92400e'))
B.append('</g>')
print(svg(360, 1046, B))
