"""Shared data and helpers for the two Sanger figures (d02 fragments, d03 gel and chromatogram)."""
import json, os
HERE = os.path.dirname(os.path.abspath(__file__))
D = json.load(open(os.path.join(HERE, 'data', 'sanger_sequence.json')))
TPL, READ = D['template_3to5'], D['read_5to3']
COMP = {'A': 'T', 'T': 'A', 'G': 'C', 'C': 'G'}
assert ''.join(COMP[b] for b in TPL) == READ, 'new strand must be the complement of the template (template 3->5 pairs with new strand 5->3)'
INK = {'A': ('#2e7d32', '#c8e6c9'), 'T': ('#c62828', '#ffcdd2'), 'G': ('#1565c0', '#bbdefb'), 'C': ('#6a1b9a', '#e1bee7')}
class Strings:
    """Collects strings; language-neutral letters are registered once as keys with identical en/ta."""
    def __init__(self, prose): self.d = {k: {'en': a, 'ta': b} for k, (a, b) in prose.items()}
    def lit(self, txt):
        k = 'lit_' + ''.join(f'{ord(c):x}_' for c in txt)
        self.d.setdefault(k, {'en': txt, 'ta': txt}); return k
    def alt(self, en, ta): self.d['_alt'] = {'en': en, 'ta': ta}
    def write(self, path): json.dump(self.d, open(path, 'w'), ensure_ascii=False, indent=1)
