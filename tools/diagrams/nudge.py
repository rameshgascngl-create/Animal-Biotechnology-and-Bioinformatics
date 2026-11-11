#!/usr/bin/env python3
"""Move one label at its HTML/SVG source, preserving all surrounding HTML.
Usage: nudge.py file.html --svg 1 --text 0 --dx 0 --dy 12
SVG is one-based, text is zero-based (same as check_px.py). Does not guess fixes.
"""
import argparse
import re
from pathlib import Path

SVG = re.compile(r'<svg\b[^>]*>.*?</svg>', re.S | re.I)
TEXT = re.compile(r'<text\b[^>]*>', re.I)

def edit_svg(source, index, edit):
    matches=list(SVG.finditer(source))
    if not 1 <= index <= len(matches): raise ValueError('SVG index out of range')
    m=matches[index-1]
    return source[:m.start()]+edit(m.group())+source[m.end():]

def nudge(svg, index, dx, dy):
    labels=list(TEXT.finditer(svg))
    if not 0 <= index < len(labels): raise ValueError('text index out of range')
    m=labels[index]; tag=m.group()
    # A parent-space translation composes before the existing transform.
    old=re.search(r'\btransform=[\"\']([^\"\']*)[\"\']',tag)
    transform=f'translate({dx:g} {dy:g})'+(' '+old.group(1) if old else '')
    new=f'transform="{transform}"'
    tag=tag[:old.start()]+new+tag[old.end():] if old else tag[:-1]+' '+new+'>'
    return svg[:m.start()]+tag+svg[m.end():]

if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('file',type=Path);p.add_argument('--svg',type=int,required=True);p.add_argument('--text',type=int,required=True);p.add_argument('--dx',type=float,default=0);p.add_argument('--dy',type=float,default=0);a=p.parse_args()
    a.file.write_text(edit_svg(a.file.read_text(),a.svg,lambda s:nudge(s,a.text,a.dx,a.dy)))
