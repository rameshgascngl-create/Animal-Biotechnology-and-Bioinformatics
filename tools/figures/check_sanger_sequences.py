#!/usr/bin/env python3
"""Evidence script (no figure is changed): compares the two Sanger sequences currently drawn.
d02 template (3'->5') comes from the SVG text in html-suite/sanger_dna_sequencing.html (Fig. 2);
d03 gel/chromatogram strings come from Fig. 3 and from the lesson prose."""
import re, os
ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..')
src = open(os.path.join(ROOT, 'html-suite', 'sanger_dna_sequencing.html'), encoding='utf8').read()
tpl = re.search(r'Template: 3\'</text>\s*<text[^>]*>([^<]+)</text>', src).group(1)
template_3to5 = ''.join(re.findall(r'[ACGT]', tpl))
comp = dict(zip('ACGT', 'TGCA'))
new_5to3 = ''.join(comp[b] for b in template_3to5)          # strand synthesised 5'->3' (primer A included at position 1)
d03 = re.search(r"5'– ([A-Z ]+) –3'", src).group(1).replace(' ', '')
print("d02 template 3'->5' :", template_3to5, len(template_3to5), 'nt')
print("d02 implies new 5'->3':", new_5to3, len(new_5to3), 'nt')
print("d03 chromatogram     :", d03, len(d03), 'nt')
n = min(len(new_5to3), len(d03))
print('common prefix length :', next((i for i in range(n) if new_5to3[i] != d03[i]), n))
print('d02 fragments (terminators after primer A) read:', 'A' + new_5to3[1:6], '(positions 1-5 = T,G,C,A,T)')
