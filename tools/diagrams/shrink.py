#!/usr/bin/env python3
"""Palette-quantise the flat (browser-rendered) diagram PNGs: ~35 MB -> ~9 MB. Hand-art ids are left untouched."""
import glob, os, sys
from PIL import Image
sys.path.insert(0, os.path.dirname(__file__)); from art import DIAGRAMS
D = os.path.join(os.path.dirname(__file__), '..', '..', 'app/src/main/assets/diagrams')
for f in glob.glob(D + '/*.png'):
    if os.path.basename(f)[:-4].replace('_ta', '') in DIAGRAMS: continue
    im = Image.open(f)
    if im.mode == 'P': continue
    im.convert('RGB').quantize(colors=128, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE).save(f, optimize=True)
