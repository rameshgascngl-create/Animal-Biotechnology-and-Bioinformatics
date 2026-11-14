"""Text-conservation check shared by every automated figure transform (strip, flow, column).
Nothing the geometry gate can see: it proves no original text was lost, duplicated or invented, and that the step structure was kept.
  1. Word multiset: every word of every original text node (EN) equals the words of all output strings (EN), whitespace-normalised, excluding only bare numeral/arrow
     glyph nodes the transform drops (listed). Same for Tamil, using the legacy map per node (symbol-only nodes carry over unchanged; the old vector term is the token).
  2. Each output key is built from its recorded fragments and every fragment is used exactly once (so no node appears twice).
  3. Step count equals the original box count; original arrow count must equal steps-1 (arrow order is the reading order); otherwise the figure is refused.
Returns '' when conserved, else the reason (the figure then goes to the manual track)."""
import re
from collections import Counter
W = lambda s: re.sub(r'\s+', ' ', s).strip().split(' ') if s.strip() else []
def conservation(orig_nodes, ta_map, old_vec, S, FR, steps, orig_arrows, dropped_nodes, allow_arrows=None):
    keys = [k for k in S if k != '_alt']
    used = Counter(m for k in keys for m in FR.get(k, []))
    expect = Counter(orig_nodes); expect.subtract(Counter(dropped_nodes))
    expect = +expect
    if used != expect: return 'fragment multiset differs: ' + str(list((expect - used).items())[:2] + list((used - expect).items())[:2])[:80]
    for k in keys:
        if S[k]['en'] != ' '.join(FR[k]): return f'{k}: EN string is not the joined fragments'
    en_out = Counter(w for k in keys for w in W(S[k]['en']))
    en_in = Counter(w for m in orig_nodes if m not in Counter(dropped_nodes) for w in W(m))
    if en_out != Counter(w for m in (expect.elements()) for w in W(m)): return 'EN word multiset differs'
    def ta_of(m):
        t = ta_map.get(m)
        if t is None: t = m
        return t.replace(old_vec, '⟦vec⟧')
    ta_in = Counter(w for m in expect.elements() for w in W(ta_of(m)))
    ta_out = Counter(w for k in keys for w in W(S[k]['ta']))
    if ta_in != ta_out: return 'TA word multiset differs: ' + str(list(((ta_in - ta_out) + (ta_out - ta_in)).items())[:2])[:80]
    want = steps - 1 if allow_arrows is None else allow_arrows
    if orig_arrows != want: return f'arrow count {orig_arrows} != {want} (topology differs)'
    return ''
