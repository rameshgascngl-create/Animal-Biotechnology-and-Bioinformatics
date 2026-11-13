#!/usr/bin/env python3
"""Blue-white screening as two stacked panels. Run: python3 gen_figure.py > figure.svg"""
import os, sys; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))
from steplist import cardlist
P = [('h1', '#1565c0', [('a1', 'a1d', '#e3f2fd', '#1565c0'), ('a2', None, '#e3f2fd', '#1565c0'), ('a3', 'a3d', '#fff3e0', '#e65100'), ('a4', 'a4d', '#bbdefb', '#0d47a1')]),
     ('h2', '#424242', [('b1', 'b1d', '#fce4ec', '#c62828'), ('b2', None, '#f5f5f5', '#9e9e9e'), ('b3', 'b3d', '#f5f5f5', '#757575'), ('b4', 'b4d', '#e8f5e9', '#2e7d32')])]
print(cardlist(here, P, [('m', 'b', '#0b2a40', True)]))
