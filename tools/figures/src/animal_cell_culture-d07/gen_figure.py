#!/usr/bin/env python3
"""Natural vs tissue-engineered skin as two stacked card panels. Run: python3 gen_figure.py > figure.svg"""
import os, sys; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))
from steplist import cardlist
P = [('h1', '#b45309', [('n1', None, '#e8f5e9', '#2e7d32'), ('n2', None, '#e0f2f1', '#00796b'), ('n3', None, '#e3f2fd', '#1565c0'),
                        ('n4', 'n4d', '#ede7f6', '#5e35b1'), ('n5', None, '#f3e5f5', '#7b1fa2')]),
     ('h2', '#c2410c', [('e1', None, '#fff3e0', '#e65100'), ('e2', 'e2d', '#ffe0b2', '#e65100'), ('e3', None, '#e3f2fd', '#1565c0'),
                        ('e4', 'e4d', '#ede7f6', '#5e35b1'), ('e5', None, '#f3e5f5', '#7b1fa2')])]
print(cardlist(here, P, [('w', 'b', '#b71c1c', True), ('g', 'b', '#0b2a40', True)]))
