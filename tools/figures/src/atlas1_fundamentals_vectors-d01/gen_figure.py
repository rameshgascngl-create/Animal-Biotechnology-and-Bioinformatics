#!/usr/bin/env python3
"""atlas1_fundamentals_vectors-d01. Run: python3 gen_figure.py > figure.svg"""
import os, sys; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))
from steplist import steplist, cardlist
print(cardlist(here, [('h1','#0b2a40',[('c1','e1','#e8f5e9','#2e7d32'),('c2','e2','#e0f2f1','#00796b'),('c3','e3','#e3f2fd','#1565c0'),('c4','e4','#ede7f6','#5e35b1'),('c5','e5','#fff3e0','#e65100')])]))
