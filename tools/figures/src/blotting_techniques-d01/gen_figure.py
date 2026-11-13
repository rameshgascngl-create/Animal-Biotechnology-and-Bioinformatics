#!/usr/bin/env python3
"""Southern capillary-blot stack, top to bottom, as one panel of stacked layers. Run: python3 gen_figure.py > figure.svg"""
import os, sys; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))
from steplist import cardlist
P = [('h', '#0b6f85', [('l1', None, '#ede7f6', '#5e35b1'), ('l2', None, '#e3f2fd', '#1565c0'),
                       ('l3', 'l3d', '#fff3e0', '#e65100'), ('l4', None, '#e8f5e9', '#2e7d32'),
                       ('l5', 'l5d', '#ffebee', '#c62828'), ('l6', 'l6d', '#e0f2f1', '#00796b'),
                       ('l7', 'l7d', '#e8f5e9', '#2e7d32'), ('l8', None, '#fff3e0', '#e65100'),
                       ('l9', None, '#e3f2fd', '#1565c0')])]
print(cardlist(here, P, [('f', 'b', '#0b2a40', True)]))
