#!/usr/bin/env python3
"""advanced_vectors_cosmid_yac_sv40_transposons-d04. Run: python3 gen_figure.py > figure.svg"""
import os, sys; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))
from steplist import steplist, cardlist
print(steplist(here, [('t1','d1'),('t2','d2'),('t3','d3'),('t4','d4')], {1: '#a15c00',2: '#2e7d32',3: '#00796b',4: '#1565c0'}))
