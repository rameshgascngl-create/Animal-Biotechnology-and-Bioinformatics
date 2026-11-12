#!/usr/bin/env python3
"""Geometry for dna_library_genomic_cdna-d05 (Step 2: oligo-dT primer anneals to the poly-A tail). Run: python3 gen_figure.py > figure.svg
Antiparallel: mRNA 5' left to 3' right (poly-A at the 3' end); the primer's 3'-OH end points left, so synthesis extends leftwards.
Content sits inside translate(0,52)."""
import os, sys; sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..'))
from svgkit import T, svg, arrow
ORANGE, BLUE, PINK, DARK, GRAY, TEAL = '#d97706', '#0e8fb0', '#be123c', '#0b2a40', '#496570', '#0b6f85'
B = ['<g transform="translate(0,52)">']
B.append(T('t1', 180, 22, 'h', 'middle', 330, DARK))
# mRNA
B.append(f'<rect x="28" y="96" width="206" height="40" rx="20" fill="#fde68a" stroke="{ORANGE}" stroke-width="3.5"/>')
B.append(T('mrna', 131, 122, 'b', 'middle', 170, '#7a3b0a', True))
B.append(f'<rect x="234" y="96" width="98" height="40" rx="20" fill="#fecdd3" stroke="{PINK}" stroke-width="3.5"/>')
B.append(T('polya', 283, 122, 'b', 'middle', 84, '#7f1d2d', True))
B.append(T('e5', 28, 84, 'b', 'start', 40, ORANGE, True)); B.append(T('e3', 332, 84, 'b', 'end', 40, ORANGE, True))
# base-pair ticks (between poly-A and primer)
for x in range(252, 320, 12): B.append(f'<line x1="{x}" y1="140" x2="{x}" y2="160" stroke="{DARK}" stroke-width="2.5"/>')
# primer
B.append(f'<rect x="234" y="164" width="98" height="40" rx="20" fill="#b6ecf5" stroke="{BLUE}" stroke-width="3.5"/>')
B.append(T('dt', 283, 190, 'b', 'middle', 84, '#0b4a5a', True))
B.append(T('p3', 224, 224, 'b', 'end', 40, TEAL, True)); B.append(T('p5', 332, 222, 'b', 'end', 40, TEAL, True))
B.append(f'<circle cx="221" cy="184" r="9" fill="{ORANGE}" stroke="#ffffff" stroke-width="2.5"/>')
# labels with arrows
B.append(T('l_polya', 283, 64, 's', 'middle', 100, PINK, True))
B.append(arrow(130, 262, 130, 214, ORANGE)); B.append(T('l_oh', 150, 262, 's', 'start', 190, '#9a4a14', True))
B.append(T('t2', 180, 386, 'h', 'middle', 330, DARK))
B.append(T('s1', 28, 450, 's', 'start', 304, '#071e2e'))
y = 590
for col, k, kind in (('#fde68a', 'k_mrna', 'r'), ('#fecdd3', 'k_polya', 'r'), ('#b6ecf5', 'k_primer', 'r'), (ORANGE, 'k_oh', 'c')):
    if kind == 'c': B.append(f'<circle cx="35" cy="{y-5}" r="8" fill="{col}"/>')
    else: B.append(f'<rect x="28" y="{y-12}" width="14" height="14" rx="3" fill="{col}" stroke="#7b8a92" stroke-width="1.5"/>')
    B.append(T(k, 52, y, 's', 'start', 284, '#071e2e')); y += 48
B.append('</g>')
print(svg(360, int(y + 52 + 10), B))
