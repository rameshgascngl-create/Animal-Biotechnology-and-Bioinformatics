#!/usr/bin/env python3
"""Expand an SVG viewBox in the HTML source; does not scale or move its anatomy.
Usage: grow.py file.html --svg 1 --left 10 --right 10 --top 10 --bottom 10
"""
import argparse
import re
from pathlib import Path
from nudge import edit_svg

def grow(svg,left=0,right=0,top=0,bottom=0):
    m=re.search(r'viewBox=[\"\']([^\"\']*)[\"\']',svg,re.I)
    if not m: raise ValueError('SVG has no viewBox')
    x,y,w,h=map(float,re.split(r'[ ,]+',m.group(1)))
    if min(left,right,top,bottom)<0: raise ValueError('Expansion must not crop content')
    box=f'{x-left:g} {y-top:g} {w+left+right:g} {h+top+bottom:g}'
    return svg[:m.start(1)]+box+svg[m.end(1):]

if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('file',type=Path);p.add_argument('--svg',type=int,required=True)
    for side in ['left','right','top','bottom']:p.add_argument('--'+side,type=float,default=0)
    a=p.parse_args();a.file.write_text(edit_svg(a.file.read_text(),a.svg,lambda s:grow(s,a.left,a.right,a.top,a.bottom)))
