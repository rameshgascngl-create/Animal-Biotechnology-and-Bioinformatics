#!/usr/bin/env python3
"""Perceptual diff of every audit render (/tmp/audit/<id>_{en,ta}.png) against the shipped asset (assets/diagrams/<id>[_ta].png).
Shipped asset is composited on white and resized to the render's size; metrics: SSIM (grayscale, 7px window), mean absolute difference
(0-255), aspect-ratio error. Writes docs/audit/render-vs-shipped.csv. Verdict: match (SSIM>=0.90), minor (0.75-0.90), differs (<0.75)."""
import csv, glob, os, sys
import numpy as np
from PIL import Image
from skimage.metrics import structural_similarity as ssim
ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
AUD = sys.argv[1] if len(sys.argv) > 1 else '/tmp/audit'
def load(p):
    im = Image.open(p).convert('RGBA'); bg = Image.new('RGBA', im.size, (255, 255, 255, 255)); bg.alpha_composite(im); return bg.convert('L')
rows = []
for f in sorted(glob.glob(os.path.join(AUD, '*_en.png')) + glob.glob(os.path.join(AUD, '*_ta.png'))):
    b = os.path.basename(f)[:-4]; did, lang = b.rsplit('_', 1)
    ship = os.path.join(ROOT, 'app/src/main/assets/diagrams', did + ('' if lang == 'en' else '_ta') + '.png')
    if not os.path.exists(ship): rows.append([did, lang.upper(), '', '', '', '', 'MISSING SHIPPED']); continue
    a = load(f); s = load(ship)
    asp = abs((s.width / s.height) / (a.width / a.height) - 1)
    s2 = s.resize(a.size, Image.LANCZOS)
    A = np.asarray(a, float); S = np.asarray(s2, float)
    q = ssim(A, S, data_range=255, win_size=7); mad = float(np.abs(A - S).mean())
    v = 'match' if q >= .90 else ('minor' if q >= .75 else 'differs')
    rows.append([did, lang.upper(), f'{q:.3f}', f'{mad:.2f}', f'{asp:.3f}', f'{a.width}x{a.height} vs {s.width}x{s.height}', v])
os.makedirs(os.path.join(ROOT, 'docs/audit'), exist_ok=True)
with open(os.path.join(ROOT, 'docs/audit/render-vs-shipped.csv'), 'w', newline='') as fh:
    w = csv.writer(fh); w.writerow(['id', 'lang', 'ssim', 'mean_abs_diff', 'aspect_err', 'sizes', 'verdict']); w.writerows(rows)
from collections import Counter
print(Counter((r[1], r[6]) for r in rows))
