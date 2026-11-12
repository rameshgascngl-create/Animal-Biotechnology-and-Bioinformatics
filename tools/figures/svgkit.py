"""Tiny helpers for writing a figure's geometry. Text is never written here: every label is a <text data-k> whose strings live in strings.json."""
def T(k, x, y, cls='s', anchor='middle', w=None, fill='#071e2e', bold=False, italic=False):
    a = f' data-w="{w}"' if w else ''
    return (f'<text data-k="{k}"{a} x="{x}" y="{y}" text-anchor="{anchor}" class="{cls}"'
            + (' font-weight="700"' if bold else '') + (' font-style="italic"' if italic else '') + f' fill="{fill}"/>')
def svg(w, h, body):
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}">\n<rect width="{w}" height="{h}" fill="#ffffff"/>\n'
            '<defs><marker id="ah" markerWidth="5" markerHeight="5" refX="2.5" refY="2.5" orient="auto" markerUnits="strokeWidth"><path d="M0,0 L5,2.5 L0,5 z" fill="#0b6f85"/></marker></defs>\n'
            + '\n'.join(body) + '\n</svg>\n')
def arrow(x1, y1, x2, y2, color='#0b6f85', sw=2.5):
    return f'<line x1="{x1}" y1="{y1}" x2="{x2}" y2="{y2}" stroke="{color}" stroke-width="{sw}" marker-end="url(#ah)"/>'
