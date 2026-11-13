#!/usr/bin/env python3
"""Purifying poly-A mRNA from tissue, four steps. Run: python3 gen_figure.py > figure.svg"""
import os, sys; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))
from steplist import steplist
print(steplist(here, [('t1','d1'),('t2','d2'),('t3','d3'),('t4','d4')],
  {1: '#a15c00', 2: '#00796b', 3: '#1565c0', 4: '#2e7d32'}))
