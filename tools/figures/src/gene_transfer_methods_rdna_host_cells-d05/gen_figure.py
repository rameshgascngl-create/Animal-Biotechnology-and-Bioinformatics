#!/usr/bin/env python3
"""gene_transfer_methods_rdna_host_cells-d05 as a vertical step list. Run: python3 gen_figure.py > figure.svg"""
import os, sys; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))
from steplist import steplist
print(steplist(here, [('t1','d1'),('t2','d2'),('t3','d3')], {1: '#a15c00',2: '#2e7d32',3: '#c62828'}))
