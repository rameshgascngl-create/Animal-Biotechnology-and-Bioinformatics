"""Realistic-style diagram art (no text in the images). Labels are numbered markers drawn natively by the app."""
import math, random

DEFS = """
<defs>
 <filter id="shadow" filterUnits="userSpaceOnUse" x="-200" y="-200" width="1400" height="1400"><feGaussianBlur in="SourceAlpha" stdDeviation="6"/><feOffset dy="7"/><feComponentTransfer><feFuncA type="linear" slope="0.35"/></feComponentTransfer><feMerge><feMergeNode/><feMergeNode in="SourceGraphic"/></feMerge></filter>
 <filter id="soft" filterUnits="userSpaceOnUse" x="-200" y="-200" width="1400" height="1400"><feGaussianBlur stdDeviation="2.2"/></filter>
 <filter id="glow" filterUnits="userSpaceOnUse" x="-200" y="-200" width="1400" height="1400"><feGaussianBlur stdDeviation="3"/></filter>
 <linearGradient id="bgg" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#f4f8fc"/><stop offset="1" stop-color="#dfe8f1"/></linearGradient>
 <linearGradient id="glass" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#ffffff" stop-opacity=".75"/><stop offset=".5" stop-color="#dbe9f5" stop-opacity=".35"/><stop offset="1" stop-color="#ffffff" stop-opacity=".6"/></linearGradient>
 <linearGradient id="buffer" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#9fd3ee"/><stop offset="1" stop-color="#4f9cc9"/></linearGradient>
 <linearGradient id="gel" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#f1f7e8" stop-opacity=".95"/><stop offset="1" stop-color="#cfe0b8" stop-opacity=".95"/></linearGradient>
 <linearGradient id="metal" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#4a5666"/><stop offset=".5" stop-color="#2a323d"/><stop offset="1" stop-color="#171c23"/></linearGradient>
 <linearGradient id="agar" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="#f6dc8b"/><stop offset="1" stop-color="#d9a93f"/></linearGradient>
 <linearGradient id="pipette" x1="0" y1="0" x2="0" y2="1"><stop offset="0" stop-color="#ffffff" stop-opacity=".9"/><stop offset=".5" stop-color="#cfe3f3" stop-opacity=".55"/><stop offset="1" stop-color="#9fbdd6" stop-opacity=".8"/></linearGradient>
 <radialGradient id="blueCol" cx=".35" cy=".3" r=".8"><stop offset="0" stop-color="#8fb8ff"/><stop offset=".55" stop-color="#2f5fd0"/><stop offset="1" stop-color="#1a3a95"/></radialGradient>
 <radialGradient id="whiteCol" cx=".35" cy=".3" r=".8"><stop offset="0" stop-color="#ffffff"/><stop offset=".6" stop-color="#f3ecd8"/><stop offset="1" stop-color="#cfc29a"/></radialGradient>
 <radialGradient id="cyto" cx=".4" cy=".35" r=".8"><stop offset="0" stop-color="#fff4dc"/><stop offset="1" stop-color="#e8c98c"/></radialGradient>
 <radialGradient id="nucleus" cx=".4" cy=".35" r=".8"><stop offset="0" stop-color="#e9f3ff"/><stop offset="1" stop-color="#9bbbe0"/></radialGradient>
 <radialGradient id="bead" cx=".35" cy=".3" r=".8"><stop offset="0" stop-color="#ffffff"/><stop offset=".25" stop-color="#ffd37a"/><stop offset="1" stop-color="#d97b00"/></radialGradient>
</defs>"""

def wrap(w, h, body):
    return (f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {w} {h}" width="{w}" height="{h}">{DEFS}'
            f'<rect width="{w}" height="{h}" fill="url(#bgg)"/>{body}</svg>')

def tube(path, w, base, mid, hi, shadow=True):
    """A glossy tube: dark outline, body, lighter core, thin highlight."""
    f = ' filter="url(#shadow)"' if shadow else ''
    return (f'<g{f}><path d="{path}" fill="none" stroke="{base}" stroke-width="{w}" stroke-linecap="round" stroke-linejoin="round"/></g>'
            f'<path d="{path}" fill="none" stroke="{mid}" stroke-width="{w*0.78:.1f}" stroke-linecap="round" stroke-linejoin="round"/>'
            f'<path d="{path}" fill="none" stroke="{hi}" stroke-width="{w*0.28:.1f}" stroke-linecap="round" stroke-linejoin="round" opacity=".55" transform="translate(-{w*0.12:.1f},-{w*0.12:.1f})"/>')

# ---------------------------------------------------------------- gel tank
def gel_tank():
    W, H = 800, 520
    b = []
    b.append('<ellipse cx="400" cy="470" rx="350" ry="26" fill="#000" opacity=".16" filter="url(#glow)"/>')
    # power supply
    b.append('<g filter="url(#shadow)"><rect x="300" y="24" width="200" height="86" rx="10" fill="url(#metal)"/></g>')
    b.append('<rect x="316" y="38" width="86" height="34" rx="4" fill="#06240f"/><rect x="320" y="42" width="78" height="26" rx="3" fill="#2de06a" opacity=".85"/>')
    for i, x in enumerate((428, 466)):
        b.append(f'<circle cx="{x}" cy="56" r="14" fill="#8a929d"/><circle cx="{x-3}" cy="52" r="6" fill="#d7dce2" opacity=".7"/>')
    b.append('<circle cx="338" cy="95" r="8" fill="#111"/><circle cx="338" cy="95" r="4" fill="#555"/>')
    b.append('<circle cx="462" cy="95" r="8" fill="#b3201a"/><circle cx="462" cy="95" r="4" fill="#ff6a5f"/>')
    # tank walls (oblique box)
    b.append('<polygon points="90,250 130,200 750,200 710,250" fill="#ffffff" opacity=".55" stroke="#9fb4c8" stroke-width="2"/>')
    b.append('<polygon points="710,250 750,200 750,390 710,440" fill="#cfe0ee" opacity=".6" stroke="#9fb4c8" stroke-width="2"/>')
    b.append('<rect x="90" y="250" width="620" height="190" rx="10" fill="#e8f2fa" opacity=".5"/>')
    # buffer
    b.append('<rect x="96" y="276" width="608" height="158" rx="6" fill="url(#buffer)" opacity=".85"/>')
    b.append('<polygon points="96,276 136,236 744,236 704,276" fill="#bfe3f5" opacity=".55"/>')
    # electrodes
    b.append('<rect x="124" y="236" width="9" height="180" rx="4" fill="#1b1b1b"/><rect x="126" y="236" width="3" height="180" fill="#666"/>')
    b.append('<rect x="664" y="236" width="9" height="180" rx="4" fill="#b3201a"/><rect x="666" y="236" width="3" height="180" fill="#ff8a80"/>')
    # wires
    b.append('<path d="M338 103 C 338 170, 128 140, 128 236" fill="none" stroke="#111" stroke-width="6" stroke-linecap="round"/>')
    b.append('<path d="M462 103 C 462 170, 668 140, 668 236" fill="none" stroke="#b3201a" stroke-width="6" stroke-linecap="round"/>')
    # gel slab: flat rectangle (tilted slightly), wells on the left, bands aligned in each lane
    b.append('<g filter="url(#shadow)"><rect x="210" y="288" width="400" height="108" rx="3" fill="url(#gel)" stroke="#9db37c" stroke-width="1.5"/></g>')
    b.append('<rect x="210" y="396" width="400" height="12" fill="#b9cf9b" stroke="#9db37c" stroke-width="1.5"/>')
    ladder = [0.12, 0.24, 0.36, 0.50, 0.64, 0.80]
    samples = [[0.30, 0.66], [0.42, 0.74], [0.24, 0.52, 0.80], [0.50], [0.34, 0.70]]
    lanes = 6
    for k in range(lanes):
        y = 300 + k * 17.5
        b.append(f'<rect x="216" y="{y-4.5:.1f}" width="18" height="9" fill="#3d4a2e" opacity=".8"/>')
        pos = ladder if k == 0 else samples[k - 1]
        for p in pos:
            x = 250 + p * 340
            b.append(f'<rect x="{x-9:.1f}" y="{y-4:.1f}" width="18" height="8" rx="2" fill="#ff7a33" opacity=".85" filter="url(#glow)"/>')
            b.append(f'<rect x="{x-8:.1f}" y="{y-2.6:.1f}" width="16" height="5.2" rx="1.5" fill="#ffd2a8"/>')
    # glass sheen
    b.append('<polygon points="96,250 90,440 150,440 190,250" fill="url(#glass)" opacity=".5"/>')
    b.append('<rect x="90" y="250" width="620" height="190" rx="10" fill="none" stroke="#8fa7bd" stroke-width="2.5"/>')
    # migration arrow (soft)
    b.append('<path d="M300 410 L560 410" stroke="#ffffff" stroke-width="5" stroke-linecap="round" opacity=".7"/><polygon points="566,410 548,401 548,419" fill="#ffffff" opacity=".8"/>')
    marks = [
     (1, 400, 70, "Power supply (70–120 V DC)", "மின்வழங்கி (70–120 V DC)"),
     (2, 128, 330, "Cathode (−), wells end", "எதிர்மின்முனை (கேத்தோடு) (−), கிணறுகள் இருக்கும் முனை"),
     (3, 668, 330, "Anode (+)", "நேர்மின்முனை (அனோடு) (+)"),
     (4, 170, 405, "Buffer (TAE/TBE) covers the gel", "ஜெல்லை மூடும் தாங்கல் கரைசல் (TAE/TBE)"),
     (5, 560, 392, "Agarose gel (1%)", "அகரோஸ் ஜெல் (1%)"),
     (6, 225, 300, "Wells", "கிணறுகள்"),
     (7, 290, 300, "DNA ladder lane (L)", "DNA ஏணித் தடம் (L)"),
     (8, 420, 330, "DNA bands: small fragments move farthest towards the anode", "DNA பட்டைகள்: சிறிய துண்டுகள் நேர்மின்முனையை நோக்கித் தொலைவாக நகரும்"),
    ]
    return W, H, wrap(W, H, ''.join(b)), marks

# ---------------------------------------------------------------- petri dish
def petri():
    W, H = 700, 600
    cx, cy, R = 350, 300, 250
    b = ['<ellipse cx="350" cy="560" rx="250" ry="20" fill="#000" opacity=".15" filter="url(#glow)"/>']
    b.append(f'<g filter="url(#shadow)"><circle cx="{cx}" cy="{cy}" r="{R}" fill="#eef5fa" stroke="#9fb4c8" stroke-width="4"/></g>')
    b.append(f'<circle cx="{cx}" cy="{cy}" r="{R-14}" fill="url(#agar)" stroke="#c28f2f" stroke-width="3"/>')
    b.append(f'<circle cx="{cx}" cy="{cy}" r="{R-14}" fill="none" stroke="#fff3c4" stroke-width="10" opacity=".35"/>')
    random.seed(11)
    pts = []
    tries = 0
    while len(pts) < 24 and tries < 5000:
        tries += 1
        a = random.uniform(0, 2 * math.pi); r = random.uniform(0, R - 70)
        x, y = cx + r * math.cos(a), cy + r * math.sin(a)
        if all((x - px) ** 2 + (y - py) ** 2 > 44 ** 2 for px, py, _ in pts):
            pts.append((x, y, 'B' if len(pts) < 15 else 'W'))
    star = None
    for i, (x, y, k) in enumerate(pts):
        s = random.uniform(13, 21)
        fill = 'url(#blueCol)' if k == 'B' else 'url(#whiteCol)'
        edge = '#16307a' if k == 'B' else '#b8a974'
        b.append(f'<ellipse cx="{x+3:.1f}" cy="{y+5:.1f}" rx="{s:.1f}" ry="{s*0.9:.1f}" fill="#7a5410" opacity=".25" filter="url(#soft)"/>')
        b.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{s:.1f}" fill="{fill}" stroke="{edge}" stroke-width="1.4"/>')
        b.append(f'<circle cx="{x-s*0.3:.1f}" cy="{y-s*0.35:.1f}" r="{s*0.25:.1f}" fill="#fff" opacity=".7"/>')
        if k == 'W' and star is None: star = (x, y, s)
    x, y, s = star
    b.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="{s+11:.1f}" fill="none" stroke="#d62828" stroke-width="3.5" stroke-dasharray="6 5"/>')
    b.append(f'<path d="M{cx-190} {cy-150} A 240 240 0 0 1 {cx-40} {cy-238}" fill="none" stroke="#fff" stroke-width="9" stroke-linecap="round" opacity=".55"/>')
    blue = next(p for p in pts if p[2] == 'B')
    marks = [
     (1, blue[0], blue[1], "Blue colony: non-recombinant (lacZ intact, X-gal cleaved)", "நீலக் காலனி: மறுசேர்க்கை அல்லாதது (lacZ சேதமின்றி, X-gal பிளக்கப்பட்டது)"),
     (2, x, y, "White colony: recombinant (insert disrupts lacZ) — pick this", "வெள்ளைக் காலனி: மறுசேர்க்கை (செருகல் lacZ-ஐ சீர்குலைக்கிறது) — இதைத் தேர்ந்தெடு"),
     (3, cx + 120, cy + 150, "LB agar + ampicillin + X-gal + IPTG", "LB அகார் + ஆம்பிசிலின் + X-gal + IPTG"),
     (4, cx + R - 6, cy - 40, "Petri dish", "பெட்ரி தட்டு"),
    ]
    return W, H, wrap(W, H, ''.join(b)), marks

# ---------------------------------------------------------------- pBR322
def pbr322():
    W, H = 700, 640
    cx, cy, R = 350, 330, 205
    def pt(a, r=R):
        t = math.radians(a - 90)
        return cx + r * math.cos(t), cy + r * math.sin(t)
    def arc(a0, a1, r=R):
        x0, y0 = pt(a0, r); x1, y1 = pt(a1, r)
        large = 1 if (a1 - a0) % 360 > 180 else 0
        return f'M{x0:.1f} {y0:.1f} A {r} {r} 0 {large} 1 {x1:.1f} {y1:.1f}'
    b = ['<ellipse cx="350" cy="600" rx="230" ry="16" fill="#000" opacity=".14" filter="url(#glow)"/>']
    b.append(f'<circle cx="{cx}" cy="{cy}" r="{R-34}" fill="#ffffff" opacity=".55"/>')
    b.append(tube(arc(0, 359.5), 38, '#4a5566', '#8793a6', '#e2e8f0'))
    b.append(tube(arc(15, 115), 38, '#1d6b3a', '#37a85f', '#bff0cf'))      # tet
    b.append(tube(arc(218, 338), 38, '#9c2a1a', '#e0533b', '#ffd2c8'))     # amp
    b.append(tube(arc(150, 198), 38, '#1d4f9c', '#3b86e0', '#cfe3ff'))     # ori
    for a, col in ((115, '#1d6b3a'), (338, '#9c2a1a'), (198, '#1d4f9c')):
        x, y = pt(a); t = math.radians(a)
        b.append(f'<circle cx="{x:.1f}" cy="{y:.1f}" r="22" fill="{col}"/>')
    sites = {'EcoRI': 0, 'HindIII': 28, 'SalI': 52, 'BamHI': 88, 'PstI': 272}
    for n, a in sites.items():
        x0, y0 = pt(a, R - 30); x1, y1 = pt(a, R + 34)
        b.append(f'<line x1="{x0:.1f}" y1="{y0:.1f}" x2="{x1:.1f}" y2="{y1:.1f}" stroke="#111" stroke-width="3.5" stroke-linecap="round"/>')
        b.append(f'<circle cx="{x1:.1f}" cy="{y1:.1f}" r="6.5" fill="#f2b705" stroke="#111" stroke-width="2"/>')
    b.append(f'<circle cx="{cx}" cy="{cy}" r="60" fill="none" stroke="#c9d3df" stroke-width="2" stroke-dasharray="3 6"/>')
    def m(n, a, r, en, ta):
        x, y = pt(a, r); return (n, x, y, en, ta)
    marks = [
     m(1, 180, 0, "pBR322 plasmid (4,361 bp; schematic, not to scale)", "pBR322 பிளாஸ்மிட் (4,361 bp; திட்ட வரைபடம், அளவுக்கேற்றதல்ல)"),
     m(2, 175, R, "ori: origin of replication", "ori: இரட்டிப்புத் தொடக்கப்புள்ளி"),
     m(3, 278, R, "amp^r: ampicillin resistance gene", "amp^r: ஆம்பிசிலின் எதிர்ப்பு மரபணு"),
     m(4, 65, R, "tet^r: tetracycline resistance gene", "tet^r: டெட்ராசைக்ளின் எதிர்ப்பு மரபணு"),
     m(5, 0, R + 34, "EcoRI site", "EcoRI இடம்"),
     m(6, 28, R + 34, "HindIII site (inside tet^r)", "HindIII இடம் (tet^r-க்குள்)"),
     m(7, 52, R + 34, "SalI / ClaI site (inside tet^r)", "SalI / ClaI இடம் (tet^r-க்குள்)"),
     m(8, 88, R + 34, "BamHI site (inside tet^r)", "BamHI இடம் (tet^r-க்குள்)"),
     m(9, 272, R + 34, "PstI site (inside amp^r)", "PstI இடம் (amp^r-க்குள்)"),
    ]
    return W, H, wrap(W, H, ''.join(b)), marks

# ---------------------------------------------------------------- IgG
def igg():
    W, H = 700, 560
    b = ['<ellipse cx="350" cy="525" rx="120" ry="14" fill="#000" opacity=".14" filter="url(#glow)"/>']
    heavyL = 'M318 500 L318 360 Q318 330 296 308 L196 160'
    heavyR = 'M382 500 L382 360 Q382 330 404 308 L504 160'
    lightL = 'M165 180 L243 294'
    lightR = 'M535 180 L457 294'
    b.append(tube(heavyL, 44, '#1b3f86', '#3f78d6', '#cfe0ff'))
    b.append(tube(heavyR, 44, '#1b3f86', '#3f78d6', '#cfe0ff'))
    b.append(tube(lightL, 34, '#8c2418', '#e25a45', '#ffd6cd'))
    b.append(tube(lightR, 34, '#8c2418', '#e25a45', '#ffd6cd'))
    # domain notches (segments)
    for (x, y, rot) in ((226, 214, 56), (474, 214, -56), (318, 430, 0), (382, 430, 0)):
        b.append(f'<line x1="{x-24}" y1="{y}" x2="{x+24}" y2="{y}" stroke="#ffffff" stroke-width="2.5" opacity=".55" transform="rotate({rot} {x} {y})"/>')
    # disulphide bonds
    for (x, y) in ((350, 372), (350, 346)):
        b.append(f'<rect x="{x-34}" y="{y-5}" width="68" height="10" rx="5" fill="#f2c200" stroke="#9a7b00" stroke-width="1.5"/>')
    # hinge ring highlight
    b.append('<ellipse cx="350" cy="330" rx="60" ry="22" fill="none" stroke="#f2c200" stroke-width="3" stroke-dasharray="5 5" opacity=".9"/>')
    # antigens
    for (x, y, rot) in ((176, 140, -30), (524, 140, 30)):
        b.append(f'<g transform="translate({x} {y}) rotate({rot})" filter="url(#shadow)"><polygon points="0,-26 22,14 -22,14" fill="#46b86a" stroke="#1f7a3d" stroke-width="2"/></g>')
    # binding site rings
    for (x, y) in ((196, 160), (504, 160)):
        b.append(f'<circle cx="{x}" cy="{y}" r="30" fill="none" stroke="#d62828" stroke-width="3" stroke-dasharray="6 5"/>')
    marks = [
     (1, 196, 160, "Antigen-binding site", "எதிர்ப்பொருள் தூண்டி இணையும் தளம்"),
     (2, 215, 205, "Variable region (V_H + V_L)", "மாறும் பகுதி (V_H + V_L)"),
     (3, 262, 262, "Fab fragment (binds antigen)", "Fab துண்டு (எதிர்ப்பொருள் தூண்டியுடன் இணைகிறது)"),
     (4, 350, 330, "Hinge region", "கீல் பகுதி"),
     (5, 318, 440, "Heavy chain (blue)", "கனமான சங்கிலி (நீலம்)"),
     (6, 188, 252, "Light chain (red)", "இலகு சங்கிலி (சிவப்பு)"),
     (7, 382, 470, "Fc region (constant; effector function)", "Fc பகுதி (மாறாதது; செயல்விளைவுச் செயல்பாடு)"),
     (8, 350, 372, "Disulphide bonds", "டைசல்பைடு பிணைப்புகள்"),
    ]
    return W, H, wrap(W, H, ''.join(b)), marks

# ---------------------------------------------------------------- microinjection
def microinjection():
    W, H = 760, 560
    cx, cy = 400, 290
    b = ['<rect width="760" height="560" fill="#0f2744" opacity=".08"/>']
    # zona pellucida + zygote
    b.append(f'<g filter="url(#shadow)"><circle cx="{cx}" cy="{cy}" r="190" fill="#e8f1f8" stroke="#a9bfd3" stroke-width="3"/></g>')
    b.append(f'<circle cx="{cx}" cy="{cy}" r="172" fill="url(#cyto)" stroke="#caa564" stroke-width="2"/>')
    for i in range(70):
        a = random.Random(i).uniform(0, 6.28); r = random.Random(i + 99).uniform(0, 160)
        b.append(f'<circle cx="{cx+r*math.cos(a):.1f}" cy="{cy+r*math.sin(a):.1f}" r="{random.Random(i+5).uniform(2,5):.1f}" fill="#c9a35d" opacity=".5"/>')
    # polar body
    b.append(f'<circle cx="{cx-198}" cy="{cy-60}" r="11" fill="#f3e3bd" stroke="#b9a273" stroke-width="2"/>')
    # pronuclei
    b.append(f'<circle cx="{cx+60}" cy="{cy+6}" r="62" fill="url(#nucleus)" stroke="#5f84b3" stroke-width="3"/>')
    b.append(f'<circle cx="{cx+72}" cy="{cy-8}" r="12" fill="#5d7db0"/><circle cx="{cx+46}" cy="{cy+28}" r="9" fill="#5d7db0"/>')
    b.append(f'<circle cx="{cx-70}" cy="{cy-10}" r="46" fill="url(#nucleus)" stroke="#5f84b3" stroke-width="3"/>')
    b.append(f'<circle cx="{cx-78}" cy="{cy-18}" r="9" fill="#5d7db0"/>')
    # holding pipette (left)
    b.append(f'<g filter="url(#shadow)"><path d="M60 235 L{cx-196} 248 Q{cx-206} {cy} {cx-196} 332 L60 345 Z" fill="url(#pipette)" stroke="#7f9db8" stroke-width="3"/></g>')
    b.append(f'<path d="M60 262 L{cx-203} 262" stroke="#fff" stroke-width="5" opacity=".6"/>')
    # injection needle (right) into male pronucleus
    b.append(f'<g filter="url(#shadow)"><path d="M720 232 L{cx+98} {cy-2} L{cx+98} {cy+14} L720 262 Z" fill="url(#pipette)" stroke="#7f9db8" stroke-width="3"/></g>')
    b.append(f'<path d="M720 238 L{cx+104} {cy+2}" stroke="#fff" stroke-width="4" opacity=".6"/>')
    # DNA in needle and injected
    for i in range(14):
        x = 690 - i * 15
        b.append(f'<circle cx="{x}" cy="{247 + (720 - x) * 0.2207:.1f}" r="4" fill="#2aa84a"/>')
    for (dx, dy) in ((26, 2), (14, 16), (38, -10), (2, 30), (24, 26)):
        b.append(f'<circle cx="{cx+98-dx+10}" cy="{cy+dy}" r="5" fill="#2aa84a" stroke="#0e5c25" stroke-width="1.4"/>')
    marks = [
     (1, 150, 290, "Holding pipette (gentle suction)", "பிடிக்கும் குழாய் (மென்மையான உறிஞ்சல்)"),
     (2, cx, cy + 120, "Fertilised egg (zygote), ~80–100 µm", "கருவுற்ற முட்டை (கருமுட்டை), ~80–100 µm"),
     (3, cx + 60, cy + 6, "Male pronucleus (larger, injected)", "ஆண் முன்கரு (பெரியது, இங்கு செலுத்தப்படுகிறது)"),
     (4, cx - 70, cy - 10, "Female pronucleus", "பெண் முன்கரு"),
     (5, 600, 252, "Injection needle", "செலுத்தும் ஊசி"),
     (6, cx + 78, cy + 18, "DNA injected directly into the pronucleus", "DNA நேரடியாக முன்கருவுக்குள் செலுத்தப்படுகிறது"),
     (7, cx + 160, cy - 110, "Zona pellucida", "சோனா பெலூசிடா"),
    ]
    return W, H, wrap(W, H, ''.join(b)), marks

DIAGRAMS = {
    'gel_electrophoresis-d01': gel_tank,
    'gene_cloning_steps_selection-d05': petri,
    'vectors_genetic_engineering-d01': pbr322,
    'hybridoma_monoclonal_antibodies-d01': igg,
    'gene_transfer_methods_rdna_host_cells-d04': microinjection,
}
