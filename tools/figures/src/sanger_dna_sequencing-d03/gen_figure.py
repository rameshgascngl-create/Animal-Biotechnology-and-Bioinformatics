#!/usr/bin/env python3
"""Geometry for sanger_dna_sequencing-d03 (gel and chromatogram). Run: python3 gen_figure.py > figure.svg  (also writes strings.json)
Data: ../../data/sanger_sequence.json (shared with d02). Portrait: gel above, chromatogram below, each at full width."""
import os, sys; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))
from svgkit import T, svg
from sangerkit import READ, INK, Strings
S = Strings({
 'gel_h': ("Denaturing polyacrylamide gel", "இயல்பிழப்பு பாலிஅக்ரிலமைடு ஜெல்"),
 'gel_s': ("Original radioactive method, 4 lanes", "அசல் கதிரியக்க முறை, 4 தடங்கள்"),
 'dir': ("Read from the bottom up: the smallest fragment comes first.", "கீழிருந்து மேலாக வாசி: சிறிய துண்டு முதலில் வரும்."),
 'ch_h': ("Fluorescent chromatogram", "ஒளிர்வு நிறவரைவு"),
 'ch_s': ("Modern capillary electrophoresis output", "நவீன நுண்குழல் மின்னாற்பிரித்தல் வெளியீடு"),
 'time': ("Time / fragment size →", "நேரம் / துண்டு அளவு →"),
 'n1': ("Each peak is one base; read left → right.", "ஒவ்வொரு உச்சமும் ஒரு காரம்; இடமிருந்து வலமாக வாசி."),
 'n2': ("Colour identifies the base; peak position gives the fragment size.", "நிறம் காரத்தைக் குறிக்கிறது; உச்ச நிலை துண்டின் அளவைக் குறிக்கிறது."),
})
OFF = 56
B = [f'<g transform="translate(0,{OFF})">']
B.append(T('gel_h', 28, 14, 'b', 'start', 304, '#0b2a40', True)); B.append(T('gel_s', 28, 58, 's', 'start', 304, '#2b4a5a'))
gy = 104
B.append(f'<rect x="28" y="{gy}" width="304" height="296" rx="10" fill="#1b1b1b" stroke="#444" stroke-width="3"/>')
lanes = ['A', 'T', 'G', 'C']; cx = {b: 70 + 60 * i for i, b in enumerate(lanes)}
for b in lanes: B.append(T(S.lit('dd' + b), cx[b], gy + 26, 's', 'middle', 52, INK[b][1], True))
n = len(READ)
for r in range(n):
    pos = n - 1 - r                      # top row = longest fragment = last base
    b = READ[pos]; y = gy + 48 + r * 27
    B.append(f'<rect x="{cx[b]-22}" y="{y}" width="44" height="10" rx="3" fill="{INK[b][1]}"/>')
    B.append(T(S.lit(b), 312, y + 11, 's', 'middle', 16, INK[b][1], True))
B.append(T('dir', 28, 424, 's', 'start', 304, '#2b4a5a'))
cy = 482
B.append(T('ch_h', 28, cy, 'b', 'start', 304, '#0b2a40', True)); B.append(T('ch_s', 28, cy + 22, 's', 'start', 304, '#2b4a5a'))
py = cy + 66; base = py + 160
B.append(f'<rect x="28" y="{py}" width="304" height="218" rx="10" fill="#fafafa" stroke="#c9c9c9" stroke-width="3"/>')
B.append(f'<line x1="36" y1="{base}" x2="324" y2="{base}" stroke="#666" stroke-width="2.5"/>')
hs = [96, 84, 92, 80, 90, 78, 82, 94, 88]
for i, b in enumerate(READ):
    c = 28 + 20 + i * 33.5; h = hs[i]
    B.append(f'<path d="M{c-15:.1f},{base} C{c-8:.1f},{base} {c-8:.1f},{base-h} {c:.1f},{base-h} C{c+8:.1f},{base-h} {c+8:.1f},{base} {c+15:.1f},{base} Z" fill="{INK[b][1]}" stroke="{INK[b][0]}" stroke-width="2.5"/>')
    B.append(T(S.lit(b), c, base + 22, 'b', 'middle', 24, INK[b][0], True))
B.append(T('time', 180, base + 42, 's', 'middle', 280, '#555'))
sy = py + 218 + 34
B.append(T(S.lit('5′– ' + ' '.join(READ) + ' –3′'), 180, sy, 'b', 'middle', 300, '#0b2a40', True))
B.append(T('n1', 28, sy + 28, 's', 'start', 304, '#2b4a5a')); B.append(T('n2', 28, sy + 64, 's', 'start', 304, '#2b4a5a'))
B.append('</g>')
S.alt("Two views of the same Sanger sequence. Top: a denaturing polyacrylamide gel with four lanes, ddA, ddT, ddG and ddC. Reading from the bottom up, the bands spell A T G C A T C G A. Bottom: a fluorescent chromatogram with nine coloured peaks, A green, T red, G blue, C purple, reading left to right as 5′ A T G C A T C G A 3′.",
      "ஒரே சேங்கர் வரிசையின் இரு காட்சிகள். மேலே: ddA, ddT, ddG, ddC என நான்கு தடங்கள் கொண்ட இயல்பிழப்பு பாலிஅக்ரிலமைடு ஜெல்; கீழிருந்து மேலாக வாசித்தால் பட்டைகள் A T G C A T C G A எனத் தெரிவிக்கின்றன. கீழே: ஒன்பது நிறமுள்ள உச்சங்கள் கொண்ட ஒளிர்வு நிறவரைவு (A பச்சை, T சிவப்பு, G நீலம், C ஊதா); இடமிருந்து வலமாக 5′ A T G C A T C G A 3′.")
S.write(os.path.join(here, 'strings.json'))
print(svg(360, int(sy + 64 + 40 + 2 * OFF - 20), B))
