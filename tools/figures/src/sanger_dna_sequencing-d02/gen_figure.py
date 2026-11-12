#!/usr/bin/env python3
"""Geometry for sanger_dna_sequencing-d02 (chain-termination fragments). Run: python3 gen_figure.py > figure.svg  (also writes strings.json)
Data: ../../data/sanger_sequence.json (template 3'->5' TACGTAGCT; new strand ATGCATCGA). The primer binds upstream (3' flank) of the template shown,
so all nine read bases are newly synthesised; fragment k ends at base k with a labelled ddNTP."""
import os, sys; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))
from svgkit import T, svg
from sangerkit import TPL, READ, INK, Strings
S = Strings({
 'tpl': ("Template strand, 3′ → 5′", "வார்ப்புரு இழை, 3′ → 5′"),
 'how': ("The primer binds at the 3′ end of the template. The new strand grows 5′ → 3′ until a ddNTP stops it.",
         "முதன்மை இழை வார்ப்புருவின் 3′ முனையில் இணைகிறது. ddNTP நிறுத்தும் வரை புதிய இழை 5′ → 3′ திசையில் வளர்கிறது."),
 'frag': ("Fragments, shortest to longest (cyan block = primer)", "துண்டுகள், சிறியது முதல் பெரியது வரை (நீலப்பச்சைத் தொகுதி = முதன்மை இழை)"),
 'rd': ("New strand, 5′ → 3′", "புதிய இழை, 5′ → 3′"),
 'rd2': ("Each stop (*) gives one base. The dyes: A green, T red, G blue, C purple.", "ஒவ்வொரு நிறுத்தமும் (*) ஒரு காரத்தைத் தருகிறது. சாயங்கள்: A பச்சை, T சிவப்பு, G நீலம், C ஊதா."),
})
OFF, X0, CW = 56, 84, 27
B = [f'<g transform="translate(0,{OFF})">']
B.append(T('tpl', 28, 14, 'b', 'start', 300, '#0b2a40', True))
y = 26
B.append(T(S.lit('3′'), X0 - 4, y + 19, 's', 'end', 24, '#0b2a40', True)); B.append(T(S.lit('5′'), X0 + CW * 9 + 4, y + 19, 's', 'start', 24, '#0b2a40', True))
for i, b in enumerate(TPL):
    B.append(f'<rect x="{X0+CW*i}" y="{y}" width="{CW-2}" height="26" rx="4" fill="#e2e8ec" stroke="#8aa0ab" stroke-width="1.5"/>')
    B.append(T(S.lit(b), X0 + CW * i + (CW - 2) / 2, y + 19, 'b', 'middle', 24, '#37474f', True))
B.append(T('how', 28, 90, 's', 'start', 304, '#2b4a5a'))
y0 = 226
B.append(T('frag', 28, y0 - 54, 'b', 'start', 304, '#0b2a40', True))
for k in range(1, 10):
    y = y0 + (k - 1) * 31
    B.append(f'<rect x="40" y="{y}" width="40" height="26" rx="4" fill="#b2ebf2" stroke="#0e8fb0" stroke-width="2"/>')
    for j in range(k):
        b = READ[j]; x = X0 + CW * j; last = (j == k - 1)
        if last:
            B.append(f'<rect x="{x}" y="{y}" width="{CW-2}" height="26" rx="4" fill="{INK[b][1]}" stroke="{INK[b][0]}" stroke-width="3"/>')
            B.append(T(S.lit(b + '*'), x + (CW - 2) / 2, y + 19, 'b', 'middle', 24, INK[b][0], True))
        else:
            B.append(T(S.lit(b), x + (CW - 2) / 2, y + 19, 's', 'middle', 24, '#37474f'))
yb = y0 + 9 * 31 + 24
B.append(T('rd', 28, yb, 'b', 'start', 304, '#0b2a40', True))
for j, b in enumerate(READ):
    x = X0 + CW * j
    B.append(f'<rect x="{x}" y="{yb+10}" width="{CW-2}" height="26" rx="4" fill="{INK[b][1]}" stroke="{INK[b][0]}" stroke-width="2.5"/>')
    B.append(T(S.lit(b), x + (CW - 2) / 2, yb + 29, 'b', 'middle', 24, INK[b][0], True))
B.append(T('rd2', 28, yb + 62, 's', 'start', 304, '#2b4a5a'))
B.append('</g>')
S.alt("Diagram of the chain-termination fragments in Sanger sequencing. The template, read 3′ to 5′, is T A C G T A G C T. A primer binds at its 3′ end. Nine fragments are shown, each stopped at one more base by a labelled ddNTP, giving the new strand A T G C A T C G A read 5′ to 3′. Dyes: A green, T red, G blue, C purple.",
      "சேங்கர் வரிசைப்படுத்தலில் சங்கிலி முடிவுறுத்தல் துண்டுகளின் படம். வார்ப்புரு 3′ இலிருந்து 5′ ஆக T A C G T A G C T. அதன் 3′ முனையில் முதன்மை இழை இணைகிறது. ஒவ்வொரு துண்டும் ஒரு கூடுதல் காரத்தில் ddNTP-யால் நிறுத்தப்பட்டு, புதிய இழை 5′ இலிருந்து 3′ ஆக A T G C A T C G A எனப் பெறப்படுகிறது. சாயங்கள்: A பச்சை, T சிவப்பு, G நீலம், C ஊதா.")
S.write(os.path.join(here, 'strings.json'))
print(svg(360, yb + 62 + 40 + 2 * OFF - 20, B))
