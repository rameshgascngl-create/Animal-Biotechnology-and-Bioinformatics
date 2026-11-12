#!/usr/bin/env python3
"""Geometry for screening_selection_recombinants-d04 (colony hybridization, 7 steps + key elements). Run: python3 gen_figure.py > figure.svg
Portrait: one step per row (drawing left, text right), arrows between rows, key elements as term/description pairs.
Content sits in translate(0,72); the 4% audit margin is a fraction of the height."""
import os, sys; sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..'))
from svgkit import T, svg, arrow
GREEN, BLUE, ORANGE, RED, GRAY, PURPLE, DARK = '#047857', '#0e8fb0', '#d97706', '#be123c', '#496570', '#6d28d9', '#0b2a40'
PITCH = 162
B = ['<g transform="translate(0,72)">']
def star(cx, cy, r, fill):
    import math
    pts = []
    for i in range(10):
        a = math.radians(-90 + 36 * i); rr = r if i % 2 == 0 else r * 0.42
        pts.append(f'{cx + rr * math.cos(a):.1f},{cy + rr * math.sin(a):.1f}')
    return f'<polygon points="{" ".join(pts)}" fill="{fill}"/>'
def head(i, y0, k):
    B.append(f'<circle cx="36" cy="{y0+12}" r="13" fill="{DARK}"/>'); B.append(f'<text data-num="{i}" x="36" y="{y0+17}" text-anchor="middle" class="b" font-weight="700" fill="#ffffff"/>')
    B.append(T(k, 58, y0 + 17, 'b', 'start', 282, DARK, True))
def desc(k, y0, dy=0, col='#071e2e', bold=False, w=184):
    B.append(T(k, 152, y0 + 54 + dy, 's', 'start', w, col, bold))
def link(y0, k=None):
    y = y0 + 126
    B.append(arrow(80, y, 80, y + 24))
    if k: B.append(T(k, 98, y + 17, 's', 'start', 240, '#0b6f85'))
cols = [(34, 20, GREEN), (66, 34, GREEN), (98, 22, GREEN), (52, 52, GREEN), (88, 56, GREEN), (112, 44, GREEN)]
# 1 master plate
y = 0; head(1, y, 't1')
B.append(f'<ellipse cx="80" cy="{y+82}" rx="56" ry="38" fill="#fff3c4" stroke="{ORANGE}" stroke-width="3.5"/>')
for cx, cy in ((46, 82), (114, 82), (62, 106), (98, 106), (66, 60), (96, 60)): B.append(f'<circle cx="{cx}" cy="{y+cy}" r="8" fill="{GREEN}"/>')
B.append(f'<circle cx="78" cy="{y+84}" r="10" fill="{GREEN}" stroke="{ORANGE}" stroke-width="3"/>'); B.append(star(78, y + 84, 6, '#ffffff'))
desc('d1', y, 0, ORANGE, True); link(y, 'a12')
# 2 nylon membrane
y = PITCH; head(2, y, 't2')
B.append(f'<rect x="24" y="{y+42}" width="112" height="82" rx="6" fill="#eef2f6" stroke="{GRAY}" stroke-width="3"/>')
for cx, cy in ((50, 66), (80, 60), (110, 72), (58, 96), (96, 100)): B.append(f'<circle cx="{cx}" cy="{y+cy+8}" r="7" fill="#7fb6a6"/>')
B.append(f'<circle cx="78" cy="{y+86}" r="9" fill="#7fb6a6" stroke="{BLUE}" stroke-width="3"/>'); B.append(star(78, y + 86, 5.5, '#ffffff'))
desc('d2', y); link(y, 'a23')
# 3 DNA fixed
y = 2 * PITCH; head(3, y, 't3')
B.append(f'<rect x="24" y="{y+42}" width="112" height="82" rx="6" fill="#fffbe6" stroke="{ORANGE}" stroke-width="3"/>')
for cx, cy in ((46, 66), (86, 64), (62, 88), (100, 94)): B.append(f'<path d="M {cx-16} {y+cy+8} q 8 -12 16 0 t 16 0" fill="none" stroke="{BLUE}" stroke-width="3.5"/>')
B.append(f'<path d="M 66 {y+78} q 8 -12 16 0 t 16 0" fill="none" stroke="{ORANGE}" stroke-width="3.5"/>'); B.append(star(82, y + 68, 5.5, ORANGE))
desc('d3', y); desc('d3b', y, 70, BLUE, True); link(y)
# 4 hybridization
y = 3 * PITCH; head(4, y, 't4')
B.append(f'<rect x="24" y="{y+42}" width="112" height="82" rx="6" fill="#f3efff" stroke="{PURPLE}" stroke-width="3"/>')
B.append(f'<path d="M 38 {y+96} q 8 -12 16 0 t 16 0 t 16 0 t 16 0" fill="none" stroke="{BLUE}" stroke-width="3.5"/>')
for cx in (46, 62, 78, 94): B.append(f'<rect x="{cx-4}" y="{y+70}" width="8" height="16" rx="3" fill="{PURPLE}"/>')
B.append(star(110, y + 70, 7, PURPLE))
desc('d4a', y, 0, PURPLE, True); desc('d4b', y, 50); link(y)
# 5 wash
y = 4 * PITCH; head(5, y, 't5')
B.append(f'<rect x="24" y="{y+42}" width="112" height="82" rx="6" fill="#f4f6f8" stroke="{GRAY}" stroke-width="3"/>')
for cx in (50, 80, 110): B.append(f'<path d="M {cx} {y+58} q -9 14 0 20 q 9 -6 0 -20" fill="#9fd4ea" stroke="{BLUE}" stroke-width="2.5"/>')
B.append(f'<path d="M 44 {y+104} q 8 -10 16 0 t 16 0" fill="none" stroke="{BLUE}" stroke-width="3.5"/>'); B.append(star(104, y + 104, 6, PURPLE))
desc('d5', y); link(y)
# 6 autoradiograph
y = 5 * PITCH; head(6, y, 't6')
B.append(f'<rect x="24" y="{y+42}" width="112" height="82" rx="6" fill="#111827" stroke="{GRAY}" stroke-width="3"/>')
for cx, cy in ((46, 66), (110, 62), (60, 108), (116, 106)): B.append(f'<circle cx="{cx}" cy="{y+cy}" r="4" fill="#374151"/>')
B.append(f'<circle cx="80" cy="{y+84}" r="12" fill="#f9fafb"/>'); B.append(star(80, y + 84, 7, '#111827'))
desc('d6', y, 16, DARK, True); link(y)
# 7 pick
y = 6 * PITCH; head(7, y, 't7')
B.append(f'<ellipse cx="80" cy="{y+82}" rx="56" ry="38" fill="#fff3c4" stroke="{ORANGE}" stroke-width="3.5"/>')
for cx, cy in ((46, 82), (114, 82), (62, 106), (98, 106), (66, 60), (96, 60)): B.append(f'<circle cx="{cx}" cy="{y+cy}" r="8" fill="{GREEN}"/>')
B.append(f'<circle cx="78" cy="{y+84}" r="15" fill="none" stroke="{RED}" stroke-width="3.5" stroke-dasharray="5 4"/>'); B.append(f'<circle cx="78" cy="{y+84}" r="10" fill="{GREEN}"/>'); B.append(star(78, y + 84, 6, '#ffffff'))
desc('d7', y, 18, GREEN, True)
# key elements
ky = 7 * PITCH + 74
B.append(f'<line x1="24" y1="{ky-34}" x2="336" y2="{ky-34}" stroke="#c9d6dc" stroke-width="1.5"/>')
B.append(T('kt', 180, ky, 'h', 'middle', 330, DARK))
yy = ky + 38
for i in range(1, 7):
    B.append(f'<rect x="24" y="{yy-13}" width="6" height="50" rx="3" fill="{BLUE}"/>')
    B.append(T(f'k{i}', 42, yy, 's', 'start', 294, '#0b4a5a', True)); B.append(T(f'k{i}d', 42, yy + 20, 's', 'start', 294, '#071e2e')); yy += 66
B.append('</g>')
H = int(yy + 72 + 62)
print(svg(360, H, B))
