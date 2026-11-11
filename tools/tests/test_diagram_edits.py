import importlib.util
import sys
from pathlib import Path
sys.path.insert(0,str(Path(__file__).parents[1]/'diagrams'))
from nudge import edit_svg,nudge
from grow import grow
import pytest

def test_nudge_preserves_text_and_other_svg():
    source='before<svg viewBox="0 0 100 80"><text x="5" transform="rotate(5)">DNA</text></svg>after<svg></svg>'
    result=edit_svg(source,1,lambda s:nudge(s,0,2,12))
    assert 'transform="translate(2 12) rotate(5)"' in result
    assert '>DNA</text>' in result
    assert result.endswith('after<svg></svg>')

def test_grow_preserves_shape_geometry():
    assert grow('<svg viewBox="0 0 100 80"><circle cx="5"/></svg>',10,20,4,6)=='<svg viewBox="-10 -4 130 90"><circle cx="5"/></svg>'
    with pytest.raises(ValueError): grow('<svg viewBox="0 0 1 1"/>',left=-1)
