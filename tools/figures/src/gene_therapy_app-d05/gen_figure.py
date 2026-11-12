#!/usr/bin/env python3
"""Geometry for gene_therapy_app-d05: vertical step list (see ../../steplist.py). Run: python3 gen_figure.py > figure.svg"""
import os, sys; here = os.path.dirname(os.path.abspath(__file__)); sys.path.insert(0, os.path.join(here, '..', '..'))
from steplist import steplist
ROWS = [('t1','d1'),('t2','d2'),('t3','d3'),('t4','d4')]
print(steplist(here, ROWS, {1: '#7f1d1d', 2: '#1d5fa0', 3: '#1b7a4b', 4: '#0b5d6b'}))
