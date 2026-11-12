#!/usr/bin/env python3
"""Geometry for sanger_dna_sequencing-d01 (dNTP vs ddNTP). Run: python3 gen_figure.py > figure.svg
Portrait: the two nucleotides are stacked instead of side by side, so each keeps the full 360-unit width.
The audit margin is 4% of the canvas height; content sits inside translate(0,48)."""
import os, sys; sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..'))
from svgkit import T, svg, arrow
TEAL, RED, GREEN, GOLD = '#0f766e', '#be123c', '#047857', '#d97706'
B = ['<g transform="translate(0,48)">']
def panel(y0, p, col, fill, sugarfill, ok):
    B.append(T(p + '_h', 180, y0 + 18, 'h', 'middle', 330, col))
    B.append(T(p + '_sub', 180, y0 + 40, 's', 'middle', 330, col))
    B.append(T(p + '_sug', 180, y0 + 62, 's', 'middle', 330, col))
    B.append(f'<rect x="146" y="{y0+90}" width="88" height="46" rx="9" fill="#d1fae5" stroke="{GREEN}" stroke-width="3"/>')
    B.append(T('base', 190, y0 + 109, 's', 'middle', 84, '#065f46')); B.append(T('base2', 190, y0 + 128, 's', 'middle', 84, '#065f46'))
    B.append(f'<line x1="190" y1="{y0+136}" x2="190" y2="{y0+146}" stroke="{GREEN}" stroke-width="3"/>')
    B.append(f'<polygon points="190,{y0+146} 238,{y0+174} 220,{y0+222} 160,{y0+222} 142,{y0+174}" fill="{sugarfill}" stroke="{col}" stroke-width="4"/>')
    B.append(T('sugar', 190, y0 + 190, 's', 'middle', 70, col))
    for i, k in enumerate(('p_g', 'p_b', 'p_a')):
        x = 34 + i * 36
        B.append(f'<circle cx="{x}" cy="{y0+180}" r="16" fill="#fde68a" stroke="{GOLD}" stroke-width="3"/>'); B.append(T(k, x, y0 + 185, 's', 'middle', 30, '#78350f'))
    B.append(f'<line x1="{34+2*36+16}" y1="{y0+180}" x2="144" y2="{y0+180}" stroke="#0f3d4a" stroke-width="3"/>')
    B.append(T('h2', 250, y0 + 174, 's', 'start', 80, '#6b6b5a'))
    B.append(f'<line x1="190" y1="{y0+222}" x2="190" y2="{y0+232}" stroke="{col}" stroke-width="3"/>')
    B.append(f'<circle cx="190" cy="{y0+250}" r="18" fill="{fill}" stroke="{col}" stroke-width="3.5"/>')
    B.append(T(p + '_3', 160, y0 + 255, 's', 'end', 90, col, True))
    B.append(T(p + '_3c', 190, y0 + 255, 's', 'middle', 30, col, True))
    if not ok:
        B.append(T('no_oh', 222, y0 + 255, 's', 'start', 100, col))
    B.append(arrow(190, y0 + 274, 190, y0 + 300, col))
    B.append(f'<rect x="20" y="{y0+306}" width="320" height="124" rx="12" fill="{"#d1fae5" if ok else "#ffe4e6"}" stroke="{col}" stroke-width="3.5"/>')
    B.append(T(p + '_res', 180, y0 + 332, 'b', 'middle', 296, col, True))
    B.append(T(p + '_res2', 180, y0 + 360, 's', 'middle', 296, '#1f3a44'))
panel(0, 'd', TEAL, '#d1fae5', '#b6ecf5', True)
B.append('<line x1="24" y1="458" x2="146" y2="458" stroke="#b8c7cd" stroke-width="2" stroke-dasharray="8 6"/><line x1="214" y1="458" x2="336" y2="458" stroke="#b8c7cd" stroke-width="2" stroke-dasharray="8 6"/>')
B.append(T('vs', 180, 464, 's', 'middle', 40, '#7b8a91', True))
panel(486, 'dd', RED, '#ffe4e6', '#ffe4e6', False)
B.append('<rect x="14" y="946" width="332" height="112" rx="12" fill="#fff3c4" stroke="#d97706" stroke-width="3"/>')
B.append(T('cd_h', 180, 974, 'b', 'middle', 310, '#78350f', True))
B.append(T('cd_t', 180, 1002, 's', 'middle', 310, '#78350f'))
B.append('</g>')
print(svg(360, 1160, B))
