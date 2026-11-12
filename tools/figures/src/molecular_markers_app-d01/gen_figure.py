#!/usr/bin/env python3
"""Geometry for molecular_markers_app-d01: vertical step list (see ../../steplist.py). Run: python3 gen_figure.py > figure.svg"""
import os, sys; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))
from steplist import steplist
ROWS = [('t1','d1'),('t2','d2'),('t3','d3'),('t4','d4'),('t5','d5')]
print(steplist(here, ROWS, None))
