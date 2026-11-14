#!/usr/bin/env python3
"""Geometry for protein_databases_app-d02: vertical step list converted from a horizontal strip by strip_to_steplist.py. Run: python3 gen_figure.py > figure.svg"""
import os, sys; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))
from steplist import steplist
ROWS = [('t1', 'd1'), ('t2', 'd2'), ('t3', 'd3'), ('t4', 'd4')]
print(steplist(here, ROWS, None, head=None, foot=[]))
