"""Shaped-text measurement shared by the layout tools (steplist/cardlist) and the geometry gate (build_figure).

Widths come from Raqm (HarfBuzz) shaping of the real fonts: a run of Tamil script is measured with Noto Sans Tamil and any other run with Inter,
as the renderer falls back. Strings are measured AFTER the figure-term tokens are expanded, so a token in a Tamil string counts as the Tamil word it becomes.
"""
import json, os, re
from PIL import ImageFont
HERE = os.path.dirname(os.path.abspath(__file__)); ROOT = os.path.abspath(os.path.join(HERE, '..', '..'))
from PIL import features
if not features.check('raqm'): raise SystemExit('Pillow was built without Raqm: shaped-text measurement is unavailable (refusing to fall back to unshaped widths)')
_F = {}
def font(kind, bold, px):
    k = (kind, bold, px)
    if k not in _F:
        if kind == 'en': p = '/usr/share/fonts/opentype/inter/Inter-%s.otf' % ('Bold' if bold else 'Regular')
        else: p = os.path.join(ROOT, 'app/src/main/res/font/notosanstamil_%s.ttf' % ('bold' if bold else 'regular'))
        _F[k] = ImageFont.truetype(p, px * 4, layout_engine=ImageFont.Layout.RAQM)
    return _F[k]
def width(txt, bold, px, safety=1.0):
    """Shaped width in canvas units (1 unit = 1/360 of the figure width)."""
    tot = 0.0
    for run in re.findall(r'[஀-௿‌‍]+|[^஀-௿‌‍]+', txt):
        kind = 'ta' if '஀' <= run[0] <= '௿' else 'en'
        tot += font(kind, bold, px).getlength(run) / 4
    return tot * safety
def wrap_lines(txt, width_units, bold, px):
    """Number of lines the renderer will produce at this width (greedy word wrap, as build_figure does). x1.02 safety against rounding in the renderer."""
    cur = ''; n = 1
    for w in txt.split(' '):
        nxt = (cur + ' ' + w) if cur else w
        if cur and width(nxt, bold, px, 1.02) > width_units: n += 1; cur = w
        else: cur = nxt
    return n
def terms():
    return json.load(open(os.path.join(ROOT, 'tools', 'i18n', 'figure_terms.json'), encoding='utf8'))
def expand(S):
    T = terms()
    def sub(v):
        for k, t in T.items(): v = v.replace('⟦' + k + '⟧', t)
        if '⟦' in v: raise SystemExit('unresolved term token in: ' + v)
        return v
    return {k: {l: sub(t) for l, t in v.items()} for k, v in S.items()}
def load_strings(figdir):
    return expand(json.load(open(os.path.join(figdir, 'strings.json'), encoding='utf8')))
