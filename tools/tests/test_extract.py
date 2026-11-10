"""Tests for the chapter extractor (spec 4.2, 6.4). Run: python3 -I -m pytest tools/tests -q"""
import json
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "extract"))
import extract as ex  # noqa: E402

BASE = {
    "drop_selectors": ["nav", "footer", "#sidebar"],
    "skip_ids": [],
}


def chap(html, profile=None, source="demo.html"):
    return ex.extract_chapter(html, source, {**BASE, **(profile or {})})


def blocks(c):
    return [b for s in c["sections"] for b in s["blocks"]]


def types(c):
    return [b["type"] for b in blocks(c)]


BASIC = """<html><head><title>Enzymes – App</title><style>p{}</style></head><body>
<nav>Chapter menu</nav>
<h2>🔬 Introduction</h2><p>Restriction enzymes cut DNA.</p>
<ul><li>First point</li><li>Second point</li></ul>
<table><tr><th>Enzyme</th><th>Site</th></tr><tr><td>EcoRI</td><td>GAATTC</td></tr></table>
<footer>Department of Zoology</footer><script>var x = 1;</script></body></html>"""


def test_basic_blocks_and_chrome_dropped():
    c = chap(BASIC)
    assert types(c) == ["p", "bullets", "table"]
    text = json.dumps(c, ensure_ascii=False)
    assert "Chapter menu" not in text and "Department of Zoology" not in text and "var x" not in text
    assert c["title"]["en"] == "Enzymes"
    tbl = blocks(c)[2]
    assert [h["en"] for h in tbl["header"]] == ["Enzyme", "Site"]
    assert tbl["rows"][0][1]["en"] == "GAATTC"


def test_heading_icon_is_split_from_title():
    c = chap(BASIC)
    assert c["sections"][0]["title"]["en"] == "Introduction"
    assert c["sections"][0]["icon"] == "🔬"


def test_inline_markup_bold_and_italic():
    c = chap("<body><h2>T</h2><p>Cut <b>EcoRI</b> from <i>E. coli</i>.</p></body>")
    assert blocks(c)[0]["text"]["en"] == "Cut **EcoRI** from _E. coli_."


def test_h3_becomes_bold_paragraph_per_spec():
    c = chap("<body><h2>T</h2><h3>Sub heading</h3><p>Body.</p></body>")
    assert blocks(c)[0] == {"type": "p", "text": {"en": "**Sub heading**"}}


def test_unknown_structure_becomes_unparsed_and_is_counted():
    c = chap("<body><h2>T</h2><marquee>Hello</marquee></body>")
    u = [b for b in blocks(c) if b["type"] == "unparsed"]
    assert u == [{"type": "unparsed", "text": "Hello", "source": "demo.html", "selector": "marquee"}]
    assert ex.count_unparsed(c) == 1


def test_interactive_controls_are_unparsed_not_silent():
    c = chap('<body><h2>T</h2><canvas id="c">fallback</canvas></body>')
    assert ex.count_unparsed(c) == 1


def test_skip_ids_are_recorded_not_silent():
    html = '<body><h2>T</h2><p>Keep.</p><section id="quiz"><h2>Quiz</h2><p>Q?</p></section></body>'
    c = chap(html, {"skip_ids": ["quiz"]})
    assert "Q?" not in json.dumps(c, ensure_ascii=False)
    assert c["skipped"] == ["quiz"]


def test_coverage_is_empty_then_detects_a_dropped_block():
    c = chap(BASIC)
    assert ex.text_coverage(BASIC, c, {**BASE}) == []
    c["sections"][0]["blocks"].pop(0)  # lose the paragraph
    assert ex.text_coverage(BASIC, c, {**BASE}) == ["Restriction enzymes cut DNA."]


def test_callout_glossary_reveal_by_profile_hints():
    prof = {
        "callout_classes": ["ibox"],
        "glossary": {"container": ["gc"], "term": ["gt"], "def": ["gd"]},
        "reveal": {"container": ["rc"], "front": ["rf"], "back": ["rb"]},
    }
    html = """<body><h2>T</h2>
    <div class="ibox amber"><h4>Note</h4><p>Mind the star activity.</p></div>
    <div class="gc"><span class="gt">Vector</span><span class="gd">A DNA carrier.</span></div>
    <div class="rc"><div class="rf">What cuts DNA?</div><div class="rb">Restriction enzymes.</div></div></body>"""
    c = chap(html, prof)
    b = blocks(c)
    assert [x["type"] for x in b] == ["callout", "glossary", "reveal"]
    assert b[0]["kind"] == "warn" and "star activity" in b[0]["text"]["en"]
    assert b[1]["term"]["en"] == "Vector" and b[1]["definition"]["en"] == "A DNA carrier."
    assert b[2]["question"]["en"] == "What cuts DNA?" and b[2]["answer"]["en"] == "Restriction enzymes."
    assert ex.text_coverage(html, c, {**BASE, **prof}) == []


def test_steps_group_consecutive_items():
    prof = {"steps_item_classes": ["fb"]}
    html = '<body><h2>T</h2><div class="fb">Cut</div><div class="fb">Ligate</div><p>After.</p></body>'
    c = chap(html, prof)
    assert types(c) == ["steps", "p"]
    assert [i["en"] for i in blocks(c)[0]["items"]] == ["Cut", "Ligate"]


DIAGRAMS = """<body><h2>T</h2>
<svg viewBox="0 0 10 10"><rect/><text>Plasmid</text></svg>
<figure><svg><circle/><text>Ori</text></svg><figcaption>Map of pBR322</figcaption></figure>
<div class="figure"><svg><circle/><path/><path/><path/></svg><div class="figcap">Fig 3 caption</div></div>
<figure><img src="x.png"><figcaption>A photo</figcaption></figure></body>"""


def test_diagram_blocks_and_inventory_reconcile_nested_figures():
    prof = {"figure_classes": ["figure"], "caption_classes": ["figcap"]}
    c = chap(DIAGRAMS, prof, source="demo.html")
    d = [b for b in blocks(c) if b["type"] == "diagram"]
    assert len(d) == 3  # three svgs; the img-only figure is inventory only
    assert d[1]["caption"]["en"] == "Map of pBR322" and d[1]["labels"] == ["Ori"]
    assert d[2]["caption"]["en"] == "Fig 3 caption"
    inv = ex.diagram_inventory(DIAGRAMS, "demo.html", {**BASE, **prof})
    assert [r["kind"] for r in inv] == ["svg", "svg", "svg", "figure-img"]
    assert [r["in_figure"] for r in inv] == [False, True, True, True]
    assert inv[0]["labels"] == ["Plasmid"] and inv[0]["source"] == "dom"
    assert inv[2]["elements"] == 4 and inv[2]["class"] == "content"
    assert inv[0]["id"] == d[0]["id"]


def test_decorative_svg_is_classified_as_chrome():
    inv = ex.diagram_inventory('<body><svg width="16" height="16"><path/></svg></body>', "d.html", BASE)
    assert inv[0]["class"] == "decorative"


SCRIPTED = """<body><section id="anim"><h2>Stepper</h2><p>Click Next.</p></section>
<script>var STEPS = [
 {label: '1: Isolate', title: 'Step 1: Isolate mRNA', desc: 'Total RNA is extracted from tissue.',
  svg: `<svg viewBox="0 0 5 5"><rect/><text>Tissue</text></svg>`},
 {label: '2: Prime', title: 'Step 2: Prime', desc: 'Oligo-dT anneals to the tail.',
  svg: `<svg viewBox="0 0 5 5"><rect/><text>Primer</text></svg>`}];</script></body>"""
SCRIPT_PROF = {"script_steps": {"var": "STEPS", "section": "anim", "title": "title", "desc": "desc", "svg": "svg"}}


def test_script_embedded_steps_are_extracted_with_diagrams():
    c = chap(SCRIPTED, SCRIPT_PROF)
    b = blocks(c)
    assert [x["type"] for x in b] == ["p", "p", "p", "diagram", "p", "p", "diagram"]
    assert b[1]["text"]["en"] == "**Step 1: Isolate mRNA**"
    assert b[2]["text"]["en"] == "Total RNA is extracted from tissue."
    assert b[3]["labels"] == ["Tissue"]
    inv = ex.diagram_inventory(SCRIPTED, "demo.html", {**BASE, **SCRIPT_PROF})
    assert [r["source"] for r in inv] == ["script", "script"]
    assert ex.script_orphans(SCRIPTED, c, [], {**BASE, **SCRIPT_PROF}) == []


LONG = "Transposons were discovered by Barbara McClintock in maize during the nineteen forties."


def test_script_prose_not_in_chapter_or_quizbank_is_reported():
    html = f'<body><h2>T</h2><p>Hi.</p><script>var A = "{LONG}"; var B = "Swipe sideways to see the whole diagram";</script></body>'
    c = chap(html)
    prof = {**BASE, "ignore_script_literals": ["Swipe sideways"]}
    assert ex.script_orphans(html, c, [], prof) == [LONG]
    assert ex.script_orphans(html, c, [LONG], prof) == []  # present in quizbank text
