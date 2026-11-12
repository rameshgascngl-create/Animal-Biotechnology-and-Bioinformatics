"""Shared geometry for vertical step lists: numbered node, bold action, plain detail, arrows between nodes.
Row heights are sized from the longer of the English and Tamil strings (conservative chars-per-line), so one SVG serves both languages."""
import json, math, os
from svgkit import T, svg, arrow
DARK, TEAL = '#0b2a40', '#0e8fb0'
OFF = 56
def _lines(s, cpl): return max(1, math.ceil(len(s) / cpl))
def steplist(figdir, rows, accents=None):
    S = json.load(open(os.path.join(figdir, 'strings.json')))
    def n(k, cpl_en, cpl_ta): return 0 if k is None else max(_lines(S[k]['en'], cpl_en), _lines(S[k]['ta'], cpl_ta))
    B = [f'<g transform="translate(0,{OFF})">']; ys = []; y = 0; spec = []
    for tk, dk in rows:
        tl = n(tk, 30, 22); dl = n(dk, 36, 27)
        ys.append(y); spec.append((tk, tl, dk, dl))
        y += max(17 + (tl - 1) * 19 + (22 + (dl - 1) * 17 if dk else 0) + 24, 58)
    for i, ((tk, tl, dk, dl), y0) in enumerate(zip(spec, ys), 1):
        col = (accents or {}).get(i, DARK)
        B.append(f'<circle cx="36" cy="{y0+12}" r="13" fill="{col}"/>')
        B.append(f'<text data-num="{i}" x="36" y="{y0+17}" text-anchor="middle" class="b" font-weight="700" fill="#ffffff"/>')
        B.append(T(tk, 62, y0 + 17, 'b', 'start', 272, DARK, True))
        if dk: B.append(T(dk, 62, y0 + 17 + (tl - 1) * 19 + 22, 's', 'start', 272, '#2b4a5a'))
        if i < len(spec): B.append(arrow(36, y0 + 29, 36, ys[i] - 6, TEAL, 2.5))
    B.append('</g>')
    return svg(360, int(y + 2 * OFF - 30), B)
