#!/usr/bin/env python3
"""Gene cloning workflow as a vertical step list. Run: python3 gen_figure.py > figure.svg"""
import os, sys; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))
from steplist import steplist
print(steplist(here, [('t1','d1'),('t2','d2'),('t3','d3'),('t4','d4'),('t5','d5')],
  {1: '#a15c00', 2: '#c62828', 3: '#2e7d32', 4: '#1565c0', 5: '#5e35b1'}))
