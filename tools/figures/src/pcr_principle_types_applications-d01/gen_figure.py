#!/usr/bin/env python3
"""PCR thermal profile (one cycle) with three numbered phase cards. Run: python3 gen_figure.py > figure.svg"""
import os, sys, json; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))
from svgkit import T, svg
from steplist import wrap_lines
S = json.load(open(os.path.join(here, 'strings.json')))
OFF = 56
def nl(k, w, bold, px): return max(wrap_lines(S[k]['en'], w, bold, px), wrap_lines(S[k]['ta'], w, bold, px))
B = [f'<g transform="translate(0,{OFF})">']
# plot
y95, y72, y58, yb = 44, 104, 134, 200
B.append('<path d="M62,%d L62,%d C90,%d 92,%d 108,%d L150,%d C166,%d 168,%d 186,%d L210,%d C226,%d 228,%d 244,%d L292,%d C308,%d 310,%d 326,%d L326,%d Z" fill="#ffffff" stroke="none"/>' % ((yb, y58) + (y58, y95, y95, y95, y95, y58, y58, y58, y58, y72, y72, y72, y72, y58, y58, yb)))
for x0, x1, c in ((62, 186, '#ffcdd2'), (186, 244, '#bbdefb'), (244, 326, '#c8e6c9')):
    B.append(f'<rect x="{x0}" y="{y95-4}" width="{x1-x0}" height="{yb-y95+4}" fill="{c}" fill-opacity="0.45"/>')
B.append(f'<path d="M62,{y58} C88,{y58} 92,{y95} 112,{y95} L150,{y95} C170,{y95} 172,{y58} 192,{y58} L214,{y58} C232,{y58} 234,{y72} 252,{y72} L290,{y72} C308,{y72} 312,{y58} 326,{y58}" fill="none" stroke="#0b6f85" stroke-width="4" stroke-linecap="round"/>')
B.append(f'<line x1="56" y1="{yb}" x2="344" y2="{yb}" stroke="#444" stroke-width="3"/><line x1="56" y1="{y95-14}" x2="56" y2="{yb}" stroke="#444" stroke-width="3"/>')
for k, y, c in (('t95', y95, '#b71c1c'), ('t72', y72, '#2e7d32'), ('t58', y58, '#1565c0')):
    B.append(T(k, 50, y + 5, 'b', 'end', 36, c, True))
B.append(T('yu', 56, y95 - 24, 'b', 'middle', 40, '#444', True))
B.append(T('xt', 330, yb + 22, 'b', 'end', 80, '#444', True))
for n, x, y in ((1, 131, y95 - 18), (2, 203, y58 - 20), (3, 271, y72 - 20)):
    B.append(f'<circle cx="{x}" cy="{y}" r="10" fill="#0b2a40"/><text data-num="{n}" x="{x}" y="{y+5}" text-anchor="middle" class="b" font-weight="700" fill="#ffffff"/>')
y = yb + 22 + 26
# cards
cards = [('p1', 'c1', 'w1', '#ffebee', '#c62828', 1), ('p2', 'c2', 'w2', '#e3f2fd', '#1565c0', 2), ('p3', 'c3', 'w3', '#e8f5e9', '#2e7d32', 3)]
for tk, ck, wk, fill, edge, n in cards:
    h = 14 + nl(tk, 262, True, 14.5) * 19 + (nl(ck, 262, False, 13.5) + nl(wk, 262, False, 13.5)) * 17 + 12
    B.append(f'<rect x="14" y="{y}" width="332" height="{h}" rx="12" fill="{fill}" stroke="{edge}" stroke-width="3"/>')
    B.append(f'<circle cx="40" cy="{y+24}" r="11" fill="{edge}"/><text data-num="{n}" x="40" y="{y+29}" text-anchor="middle" class="b" font-weight="700" fill="#ffffff"/>')
    ty = y + 25
    B.append(T(tk, 62, ty, 'b', 'start', 262, edge, True)); ty += nl(tk, 262, True, 14.5) * 19 + 4
    B.append(T(ck, 62, ty, 's', 'start', 262)); ty += nl(ck, 262, False, 13.5) * 17
    B.append(T(wk, 62, ty, 's', 'start', 262))
    y += h + 10
B.append(T('f', 180, y + 14, 's', 'middle', 320, '#0b2a40', True)); y += 14 + nl('f', 320, True, 13.5) * 17 + 8
B.append('</g>')
print(svg(360, int(y + 2 * OFF - 10), B))
