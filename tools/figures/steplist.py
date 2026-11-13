"""Shared geometry for vertical step lists: numbered node, bold action, plain detail, arrows between nodes.
Row heights are sized from the longer of the English and Tamil strings (conservative chars-per-line), so one SVG serves both languages."""
import json, math, os
from svgkit import T, svg, arrow
DARK, TEAL = '#0b2a40', '#0e8fb0'
OFF = 56
from PIL import ImageFont
_ROOT = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..'))
_F = {}
def _font(kind, bold, px):
    k = (kind, bold, px)
    if k not in _F:
        if kind == 'en': p = '/usr/share/fonts/opentype/inter/Inter-%s.otf' % ('Bold' if bold else 'Regular')
        else: p = os.path.join(_ROOT, 'app/src/main/res/font/notosanstamil_%s.ttf' % ('bold' if bold else 'regular'))
        _F[k] = ImageFont.truetype(p, px * 4, layout_engine=ImageFont.Layout.RAQM)
    return _F[k]
def _w(txt, bold, px):
    """Measured width in canvas units, shaped (Raqm): runs of Tamil script with Noto Sans Tamil, other runs with Inter, as the renderer falls back."""
    import re
    tot = 0.0
    for run in re.findall(r'[\u0b80-\u0bff\u200c\u200d]+|[^\u0b80-\u0bff\u200c\u200d]+', txt):
        kind = 'ta' if '\u0b80' <= run[0] <= '\u0bff' else 'en'
        tot += _font(kind, bold, px).getlength(run) / 4
    return tot * 1.02
def wrap_lines(txt, width, bold, px):
    """Number of lines the renderer will produce for txt at this width (greedy word wrap, as build_figure does)."""
    cur = ''; n = 1
    for w in txt.split(' '):
        nxt = (cur + ' ' + w) if cur else w
        if cur and _w(nxt, bold, px) > width: n += 1; cur = w
        else: cur = nxt
    return n
def _lines(s, cpl): return max(1, math.ceil(len(s) / cpl))
def steplist(figdir, rows, accents=None):
    S = json.load(open(os.path.join(figdir, 'strings.json')))
    def n(k, width, bold, px): return 0 if k is None else max(wrap_lines(S[k]['en'], width, bold, px), wrap_lines(S[k]['ta'], width, bold, px))
    B = [f'<g transform="translate(0,{OFF})">']; ys = []; y = 0; spec = []
    for tk, dk in rows:
        tl = n(tk, 272, True, 14.5); dl = n(dk, 272, False, 13.5)
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

def cardlist(figdir, panels, footer=()):
    """Stacked panels of cards. panels = [(heading_key, accent, [(title_key, detail_key|None, fill, edge), ...]), ...]; footer = [(key, cls, colour, bold), ...].
    Heights follow the longer of EN/TA (conservative chars-per-line); one SVG serves both languages."""
    S = json.load(open(os.path.join(figdir, 'strings.json')))
    def n(k, width, bold, px): return 0 if k is None else max(wrap_lines(S[k]['en'], width, bold, px), wrap_lines(S[k]['ta'], width, bold, px))
    B = [f'<g transform="translate(0,{OFF})">']; y = 0
    for hk, accent, rows in panels:
        hl = n(hk, 330, True, 17)
        B.append(T(hk, 180, y + 16, 'h', 'middle', 330, accent, True)); y += 16 + (hl - 1) * 22 + 14
        for tk, dk, fill, edge in rows:
            tl = n(tk, 314, True, 14.5); dl = n(dk, 314, False, 13.5)
            h = 10 + 18 + (tl - 1) * 19 + (4 + 17 + (dl - 1) * 17 if dk else 0) + 8
            B.append(f'<rect x="14" y="{y}" width="332" height="{h}" rx="9" fill="{fill}" stroke="{edge}" stroke-width="2.5"/>')
            B.append(T(tk, 180, y + 24, 'b', 'middle', 314, edge, True))
            if dk: B.append(T(dk, 180, y + 24 + (tl - 1) * 19 + 19, 's', 'middle', 314, '#37474f'))
            y += h + 6
        y += 10
    for k, cls, col, bold in footer:
        fl = n(k, 300, bold, 14.5 if cls != 's' else 13.5); B.append(T(k, 180, y + 14, cls, 'middle', 300, col, bold)); y += 14 + fl * (19 if cls != 's' else 17) + 8
    B.append('</g>')
    return svg(360, int(y + 2 * OFF - 20), B)
