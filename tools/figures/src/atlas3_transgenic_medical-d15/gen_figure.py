#!/usr/bin/env python3
"""Geometry for atlas3_transgenic_medical-d15: vertical step list converted from a horizontal strip by strip_to_steplist.py. Run: python3 gen_figure.py > figure.svg"""
import os, sys; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))
from steplist import steplist
ROWS = [('t1', None), ('t2', None), ('t3', None), ('t4', None), ('t5', None)]
print(steplist(here, ROWS, None, head=None, foot=[]))
