#!/usr/bin/env python3
"""Geometry for gene_transfer_methods_rdna_host_cells-d02 (calcium phosphate transfection, 4 steps). Run: python3 gen_figure.py > figure.svg
One step per row: heading across the top, drawing left (cell), explanation right. Content sits in translate(0,OFF)."""
import os, sys, math; sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..'))
from svgkit import T, svg, arrow
ORANGE, TEAL, GRAY, DARK, GREEN, GOLD = '#d97706', '#0e8fb0', '#8a949b', '#0b2a40', '#047857', '#fde68a'
OFF = 56; PITCH = 176
B = [f'<g transform="translate(0,{OFF})">']
def head(i, y0, k):
    B.append(f'<circle cx="36" cy="{y0+12}" r="13" fill="{DARK}"/>'); B.append(f'<text data-num="{i}" x="36" y="{y0+17}" text-anchor="middle" class="b" font-weight="700" fill="#ffffff"/>')
    B.append(T(k, 58, y0 + 17, 'b', 'start', 282, DARK, True))
def cell(cx, cy, r=44):
    B.append(f'<ellipse cx="{cx}" cy="{cy}" rx="{r+2}" ry="{r}" fill="#fff1de" stroke="{ORANGE}" stroke-width="4"/>')
    B.append(f'<circle cx="{cx}" cy="{cy}" r="{r*0.38:.0f}" fill="{GOLD}" stroke="{ORANGE}" stroke-width="3"/>')
def dot(x, y, r=7, col='#b9c0c5'): B.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{r}" fill="{col}" fill-opacity="0.75"/>')
def desc(k, y0, w=164, col='#071e2e', bold=False, dy=0): B.append(T(k, 174, y0 + 74 + dy, 's', 'start', w, col, bold))
# 1 form the precipitate: DNA chip + CaCl2/phosphate, mixed into fine particles
y = 0; head(1, y, 't1')
B.append(f'<rect x="28" y="{y+50}" width="136" height="38" rx="19" fill="#b6ecf5" stroke="{TEAL}" stroke-width="3"/>')
B.append(T('dna', 96, y + 74, 's', 'middle', 128, '#0b4a5a', False))
B.append(T('ca', 28, y + 108, 's', 'start', 136, ORANGE, True))
B.append(arrow(96, y + 124, 96, y + 146, ORANGE))
for cx, cy in ((54, 160), (74, 156), (96, 162), (116, 157)): dot(cx, y + cy + 4, 6)
desc('d1', y)
# 2 precipitate settles on the cells
y = PITCH; head(2, y, 't2')
cell(80, y + 104)
import math as _m
for a in (-60, -30, 0, 30, 60): dot(80 + 52 * _m.sin(_m.radians(a)), y + 104 - 52 * _m.cos(_m.radians(a)), 6)
B.append(T('cap', 80, y + 160, 's', 'middle', 100, GRAY)); desc('d2', y, dy=-20)
# 3 endocytosis towards the nucleus
y = 2 * PITCH; head(3, y, 't3')
cell(80, y + 104)
B.append(f'<ellipse cx="52" cy="{y+82}" rx="10" ry="8" fill="#e3e7ea" stroke="{GRAY}" stroke-width="2.5"/>')
B.append(f'<path d="M 61 {y+88} C 70 {y+92}, 72 {y+98}, 76 {y+102}" fill="none" stroke="{ORANGE}" stroke-width="3.5" marker-end="url(#ah)"/>')
desc('d3', y, w=170, dy=-6, col='#9a4a14', bold=True)
# 4 gene expression: nucleus -> mRNA -> protein
y = 3 * PITCH; head(4, y, 't4')
cell(80, y + 104)
B.append(f'<path d="M 100 {y+104} q 10 -14 20 0 t 20 0 t 20 0" fill="none" stroke="{GREEN}" stroke-width="3.5"/>')
B.append(f'<circle cx="150" cy="{y+104}" r="0" fill="none"/>')
desc('d4a', y, col=GREEN, bold=True, dy=-20); desc('d4b', y, dy=36)
B.append('</g>')
print(svg(360, int(4 * PITCH + 2 * OFF + 10), B))
