#!/usr/bin/env python3
"""gene_cloning_steps_selection-d02. Run: python3 gen_figure.py > figure.svg"""
import os, sys; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))
from steplist import steplist, cardlist
print(cardlist(here, [('h1','#0b6f85',[('c1','e1','#e8f5e9','#2e7d32'),('c2','e2','#e0f2f1','#00796b'),('c3','e3','#e3f2fd','#1565c0')])]))
