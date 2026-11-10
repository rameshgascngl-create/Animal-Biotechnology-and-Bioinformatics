#!/usr/bin/env python3
"""Extract bilingual-ready chapter JSON (English only) from the HTML chapter files.

Spec: docs/superpowers/specs/2026-11-10-native-bilingual-reader-design.md (4.2, 6.4)

Principles
- Never lose content silently: anything not understood becomes an `unparsed` block that keeps its
  plain text; the run exits 1 if any exist, if any source text is missing from the output
  (text_coverage), or if prose hidden in JavaScript is not accounted for (script_orphans).
- Templates differ per file, so the walk is by tag semantics. A small per-file profile
  (tools/extract/profiles/<stem>.json) only supplies class-name hints.

Run (from the repo root):
  python3 -I tools/extract/extract.py --unit I --out app/src/main/assets/content/u1 \
      --report docs/audit/extract-report.md --diagrams docs/audit/diagrams-unit1.json
"""
import argparse
import json
import re
import sys
import unicodedata
from pathlib import Path

from bs4 import BeautifulSoup, Comment, NavigableString, Tag

VERSION = "1"

DROP_TAGS = {"script", "style", "noscript", "button", "head", "template", "link", "meta", "title"}
UNPARSED_TAGS = {"input", "select", "textarea", "canvas", "iframe", "video", "audio", "object",
                 "embed", "marquee", "form", "map", "applet", "frame", "frameset"}
INLINE_TAGS = {"span", "a", "b", "strong", "i", "em", "u", "small", "sub", "sup", "mark", "abbr",
               "code", "kbd", "s", "del", "ins", "label", "cite", "q", "time", "var", "wbr", "br",
               "font", "big", "tt", "bdi", "bdo", "dfn", "samp"}
CONTAINER_TAGS = {"div", "section", "article", "main", "aside", "header", "footer", "nav", "body",
                  "html", "blockquote", "details", "summary", "figure", "figcaption", "center",
                  "pre", "address", "fieldset", "legend", "dl", "dt", "dd", "li", "hgroup", "tbody",
                  "thead", "tfoot", "tr", "td", "th", "caption", "col", "colgroup"}
BLOCK_TAGS = CONTAINER_TAGS | {"p", "ul", "ol", "table", "h1", "h2", "h3", "h4", "h5", "h6", "svg", "hr"}
KNOWN_TAGS = INLINE_TAGS | BLOCK_TAGS | DROP_TAGS
SHAPES = ["rect", "circle", "ellipse", "line", "polyline", "polygon", "path", "text", "image", "use"]
IGNORED_KEYS = {"type", "kind", "id", "source", "selector", "icon", "skipped", "extractor", "unit"}


# --------------------------------------------------------------------------- text helpers

def _collapse(s):
    """Collapse whitespace but keep explicit <br> newlines (encoded as \x00)."""
    s = re.sub(r"[ \t\r\n\f\v ]+", " ", s)
    s = re.sub(r" ?\x00 ?", "\n", s)
    return s.strip()


def _norm(s):
    """Normalisation used by coverage checks: drop markers, emoji/symbols, collapse space."""
    s = unicodedata.normalize("NFC", s).replace("*", "").replace("_", "")
    s = "".join(ch for ch in s if unicodedata.category(ch) not in ("So", "Sk", "Cf", "Cn") and ch != "️")
    return re.sub(r"\s+", " ", s.replace(" ", " ")).strip()


def _has_alnum(s):
    return any(ch.isalnum() for ch in s)


def _split_icon(text):
    m = re.match(r"^([^\w(\[\"'“‘]+)(.*)$", text, re.S)
    if m and _has_alnum(m.group(2)):
        return m.group(1).strip(), m.group(2).strip()
    return "", text


def _classes(el):
    return set(el.get("class", [])) if isinstance(el, Tag) else set()


def _selector(el):
    cl = ".".join(sorted(_classes(el)))
    return el.name + (("." + cl) if cl else "")


# --------------------------------------------------------------------------- JS helpers

def _read_js_string(s, i):
    """Read a JS string literal starting at s[i] (a quote char). Returns (value, index_after)."""
    q = s[i]
    out, j = [], i + 1
    while j < len(s):
        c = s[j]
        if c == "\\" and j + 1 < len(s):
            n = s[j + 1]
            if n == "u" and re.match(r"[0-9a-fA-F]{4}", s[j + 2:j + 6]):
                out.append(chr(int(s[j + 2:j + 6], 16)))
                j += 6
                continue
            out.append({"n": "\n", "t": "\t", "r": "", "/": "/"}.get(n, n))
            j += 2
            continue
        if c == q:
            return "".join(out), j + 1
        out.append(c)
        j += 1
    return "".join(out), j


def _js_array_objects(code, var):
    """Parse `var NAME = [ {k: 'v', ...}, ... ]` into a list of dicts of string fields."""
    m = re.search(r"(?:var|let|const)\s+" + re.escape(var) + r"\s*=\s*\[", code)
    if not m:
        return []
    i, depth, objs = m.end(), 1, []
    while i < len(code) and depth > 0:
        c = code[i]
        if c in "'\"`":
            _, i = _read_js_string(code, i)
            continue
        if c == "[":
            depth += 1
        elif c == "]":
            depth -= 1
        elif c == "{" and depth == 1:
            obj, i = _js_object(code, i)
            objs.append(obj)
            continue
        i += 1
    return objs


def _js_object(code, i):
    assert code[i] == "{"
    i += 1
    obj = {}
    while i < len(code):
        while i < len(code) and code[i] in " \t\r\n,":
            i += 1
        if i >= len(code) or code[i] == "}":
            return obj, i + 1
        km = re.match(r"['\"]?(\w+)['\"]?\s*:\s*", code[i:])
        if not km:
            i += 1
            continue
        key = km.group(1)
        i += km.end()
        if i < len(code) and code[i] in "'\"`":
            val, i = _read_js_string(code, i)
            obj[key] = val
        else:  # non-string value: skip to the next top-level comma or closing brace
            depth = 0
            while i < len(code):
                c = code[i]
                if c in "'\"`":
                    _, i = _read_js_string(code, i)
                    continue
                if c in "[{(":
                    depth += 1
                elif c in "]})":
                    if depth == 0:
                        break
                    depth -= 1
                elif c == "," and depth == 0:
                    break
                i += 1
    return obj, i


def _svg_info(svg_markup):
    sv = BeautifulSoup(svg_markup, "lxml").find("svg")
    if sv is None:
        return {"labels": [], "elements": 0, "viewbox": ""}
    return _svg_facts(sv)


def _svg_facts(sv):
    labels = [_collapse(t.get_text(" ")) for t in sv.find_all("text")]
    return {
        "labels": [x for x in labels if x],
        "elements": len(sv.find_all(SHAPES)),
        "viewbox": sv.get("viewbox") or sv.get("viewBox") or "",
    }


def _is_decorative(facts, sv_attrs, where):
    if where != "content":
        return True
    small = False
    for k in ("width", "height"):
        m = re.match(r"^\s*(\d+(?:\.\d+)?)\s*(?:px)?\s*$", str(sv_attrs.get(k, "")))
        if m and float(m.group(1)) <= 32:
            small = True
    return small or (not facts["labels"] and facts["elements"] <= 3)


# --------------------------------------------------------------------------- the walker

class _Walk:
    def __init__(self, soup, source, profile, svg_meta):
        self.soup, self.source, self.prof = soup, source, profile
        self.svg_meta = svg_meta            # id(svg element) -> record dict
        self.sections, self.cur = [], None
        self.skipped = []
        self.steps_run = []
        self.script_done = False

    # --- structure
    def section(self, title, icon=""):
        self.flush_steps()
        self.cur = {"id": f"s{len(self.sections) + 1:02d}", "title": {"en": title}, "blocks": []}
        if icon:
            self.cur["icon"] = icon
        self.sections.append(self.cur)

    def add(self, block):
        if self.cur is None:
            self.section("Overview")
        self.cur["blocks"].append(block)

    def p(self, text):
        text = _collapse(text)
        if text:
            self.add({"type": "p", "text": {"en": text}})

    # --- inline
    def inline(self, el):
        if isinstance(el, Comment):
            return ""
        if isinstance(el, NavigableString):
            return str(el)
        if not isinstance(el, Tag) or el.name in DROP_TAGS or el.name == "svg":
            return ""
        if el.name == "br":
            return "\x00"
        inner = "".join(self.inline(c) for c in el.children)
        if el.name in ("b", "strong") and inner.strip():
            lead, trail = inner[: len(inner) - len(inner.lstrip())], inner[len(inner.rstrip()):]
            return f"{lead}**{inner.strip().strip('*')}**{trail}"
        if el.name in ("i", "em") and inner.strip():
            lead, trail = inner[: len(inner) - len(inner.lstrip())], inner[len(inner.rstrip()):]
            return f"{lead}_{inner.strip().strip('_')}_{trail}"
        return inner

    def flat(self, el):
        """Flatten a container to text lines (used for callouts, steps and reveal sides)."""
        lines = []

        def rec(node):
            buf = []
            for ch in node.children:
                if isinstance(ch, Comment):
                    continue
                if isinstance(ch, NavigableString):
                    buf.append(str(ch))
                elif isinstance(ch, Tag):
                    if ch.name in DROP_TAGS or ch.name == "svg":
                        continue
                    if ch.name in INLINE_TAGS and not self.has_block(ch):
                        buf.append(self.inline(ch))
                    elif ch.name in ("ul", "ol"):
                        flush(buf)
                        for li in ch.find_all("li", recursive=False):
                            t = _collapse(self.inline_li(li))
                            if t:
                                lines.append("• " + t)
                    elif ch.name in ("h1", "h2", "h3", "h4", "h5", "h6"):
                        flush(buf)
                        t = _collapse(self.inline(ch))
                        if t:
                            lines.append(f"**{t.strip('*')}**")
                    else:
                        flush(buf)
                        rec(ch)
            flush(buf)

        def flush(buf):
            t = _collapse("".join(buf))
            buf.clear()
            if t:
                lines.append(t)

        rec(el)
        return "\n".join(lines)

    def inline_li(self, li):
        return "".join(self.inline(c) for c in li.children if not (isinstance(c, Tag) and c.name in ("ul", "ol")))

    @staticmethod
    def has_block(el):
        return any(isinstance(d, Tag) and d.name in BLOCK_TAGS for d in el.descendants)

    # --- steps grouping
    def flush_steps(self):
        if self.steps_run:
            items = [{"en": t} for t in self.steps_run if t]
            self.steps_run = []
            if items and self.cur is not None:
                self.cur["blocks"].append({"type": "steps", "items": items})

    # --- main walk
    def children(self, el):
        buf = []
        for ch in el.children:
            if isinstance(ch, Comment):
                continue
            if isinstance(ch, NavigableString):
                buf.append(str(ch))
                continue
            if not isinstance(ch, Tag):
                continue
            if ch.name in INLINE_TAGS and not self.has_block(ch):
                buf.append(self.inline(ch))
                continue
            self.flush_inline(buf)
            self.block(ch)
        self.flush_inline(buf)

    def flush_inline(self, buf):
        text = _collapse("".join(buf))
        buf.clear()
        if text and _has_alnum(text):
            self.flush_steps()
            self.p(text)

    def block(self, el):
        p, name, cls = self.prof, el.name, _classes(el)
        if name in DROP_TAGS:
            return
        eid = el.get("id")
        if eid and eid in p.get("skip_ids", []):
            self.skipped.append(eid)
            return
        if name in UNPARSED_TAGS or name not in KNOWN_TAGS:
            self.flush_steps()
            self.add({"type": "unparsed", "text": _collapse(el.get_text(" ")),
                      "source": self.source, "selector": _selector(el)})
            return
        if name == "svg":
            self.flush_steps()
            return self.diagram(el, caption="")
        step_items = set(p.get("steps_item_classes", []))
        if step_items & cls:
            self.steps_run.append(_collapse(self.flat(el)))
            return
        self.flush_steps()
        if name in ("h1", "h2") or cls & set(p.get("heading_classes", [])):
            icon, title = _split_icon(_collapse(self.inline(el)).replace("**", ""))
            self.section(title, icon)
            return
        if name in ("h3", "h4", "h5", "h6"):
            t = _collapse(self.inline(el)).replace("**", "")
            if t:
                self.p(f"**{t}**")
            return
        if name == "dt":
            t = _collapse(self.inline(el)).replace("**", "")
            if t:
                self.p(f"**{t}**")
            return
        if name == "p":
            self.p(self.inline_children(el))
            return
        if name in ("ul", "ol"):
            self.bullets(el)
            return
        if name == "table":
            self.table(el)
            return
        if name == "hr":
            return
        if name == "figure" or cls & set(p.get("figure_classes", [])):
            return self.figure(el)
        if cls & set(p.get("callout_classes", [])):
            kind = "info"
            if cls & {"amber", "warn", "warning", "red", "danger"}:
                kind = "warn"
            elif cls & {"exam", "exam-tip"}:
                kind = "exam"
            elif cls & {"tip", "green", "success"}:
                kind = "tip"
            text = self.flat(el)
            if text:
                self.add({"type": "callout", "kind": kind, "text": {"en": text}})
            return
        g = p.get("glossary")
        if g and cls & set(g["container"]):
            term = el.find(class_=lambda c: c and set(c.split()) & set(g["term"]))
            definition = el.find(class_=lambda c: c and set(c.split()) & set(g["def"]))
            if term is not None and definition is not None:
                self.add({"type": "glossary", "term": {"en": _collapse(self.inline(term))},
                          "definition": {"en": _collapse(self.inline(definition))}})
                return
        r = p.get("reveal")
        if r and cls & set(r["container"]):
            back = el.find(class_=lambda c: c and set(c.split()) & set(r["back"]))
            if back is not None:
                front_parts = []
                for ch in el.children:
                    if isinstance(ch, Tag) and ch is not back and ch.name not in DROP_TAGS:
                        t = self.flat(ch) if ch.name != "svg" else ""
                        if t:
                            front_parts.append(t)
                self.add({"type": "reveal", "question": {"en": "\n".join(front_parts)},
                          "answer": {"en": self.flat(back)}})
                return
        if cls & set(p.get("steps_container_classes", [])):
            items = [{"en": _collapse(self.flat(c))} for c in el.children
                     if isinstance(c, Tag) and c.name not in DROP_TAGS and _collapse(self.flat(c))]
            if items:
                self.add({"type": "steps", "items": items})
            return
        # default: a container; descend
        self.children(el)
        ss = p.get("script_steps")
        if ss and eid == ss["section"] and not self.script_done:
            self.script_done = True
            self.script_steps(ss)

    def inline_children(self, el):
        return "".join(self.inline(c) for c in el.children)

    def bullets(self, el):
        items = []

        def rec(lst):
            for li in lst.find_all("li", recursive=False):
                t = _collapse(self.inline_li(li))
                if t:
                    items.append({"en": t})
                for sub in li.find_all(["ul", "ol"], recursive=False):
                    rec(sub)

        rec(el)
        if items:
            self.add({"type": "bullets", "ordered": el.name == "ol", "items": items})

    def table(self, el):
        rows = []
        for tr in el.find_all("tr"):
            if tr.find_parent("table") is not el:
                continue
            cells = [_collapse(self.flat(c)) for c in tr.find_all(["th", "td"], recursive=False)]
            if cells:
                rows.append((tr, cells))
        if not rows:
            return
        header_row = next((i for i, (tr, _) in enumerate(rows) if tr.find("th")), 0)
        header = rows[header_row][1]
        body = [c for i, (_, c) in enumerate(rows) if i != header_row]
        self.add({"type": "table", "header": [{"en": h} for h in header],
                  "rows": [[{"en": c} for c in r] for r in body]})

    # --- diagrams
    def caption_for(self, container):
        if container is None:
            return ""
        if container.name == "figure":
            fc = container.find("figcaption")
            return _collapse(self.inline(fc)) if fc else ""
        cap = container.find(class_=lambda c: c and set(c.split()) & set(self.prof.get("caption_classes", [])))
        return _collapse(self.inline(cap)) if cap else ""

    def diagram(self, svg, caption):
        meta = self.svg_meta.get(id(svg))
        if meta is None:
            return
        sec = self.cur["title"]["en"] if self.cur else ""
        meta["section"] = sec
        if not caption:
            t = svg.find("title")
            caption = _collapse(t.get_text(" ")) if t else (svg.get("aria-label") or "")
        self.add({"type": "diagram", "id": meta["id"], "caption": {"en": caption}, "labels": meta["labels"]})

    def figure(self, el):
        caption = self.caption_for(el)
        svgs = el.find_all("svg")
        imgs = el.find_all("img")
        for i, sv in enumerate(svgs):
            self.diagram(sv, caption if i == 0 else "")
        if not svgs and imgs:
            if caption:
                self.p(f"_{caption}_")
        for ch in el.children:  # any non-figure content inside the wrapper must not vanish
            if isinstance(ch, Tag) and ch.name not in ("svg", "img", "figcaption") and ch.name not in DROP_TAGS:
                if not (_classes(ch) & set(self.prof.get("caption_classes", []))) and not ch.find("svg"):
                    self.block(ch)

    def script_steps(self, ss):
        steps = []
        for sc in self.soup.find_all("script"):
            steps = _js_array_objects(sc.get_text(), ss["var"])
            if steps:
                break
        for obj in steps:
            title = _collapse(obj.get(ss.get("title", "title"), ""))
            desc = _collapse(BeautifulSoup(obj.get(ss.get("desc", "desc"), ""), "lxml").get_text(" "))
            if title:
                self.p(f"**{title}**")
            if desc:
                self.p(desc)
            svg = obj.get(ss.get("svg", "svg"))
            if svg:
                facts = _svg_info(svg)
                meta = self.script_svg_meta.pop(0)
                meta.update({"section": self.cur["title"]["en"] if self.cur else "", "caption": title})
                self.add({"type": "diagram", "id": meta["id"], "caption": {"en": title}, "labels": facts["labels"]})


# --------------------------------------------------------------------------- core API

def _prepare(html, source, profile):
    """Parse, enumerate every svg in document order (before chrome is removed) and drop chrome."""
    stem = Path(source).stem
    soup = BeautifulSoup(html, "lxml")
    skip_ids = set(profile.get("skip_ids", []))
    records, n = [], 0
    chrome = set()
    for sel in profile.get("drop_selectors", []):
        for el in soup.select(sel):
            chrome.add(id(el))
    svg_meta = {}
    for sv in soup.find_all("svg"):
        n += 1
        where = "content"
        for anc in sv.parents:
            if id(anc) in chrome or (isinstance(anc, Tag) and anc.name in DROP_TAGS):
                where = "chrome"
                break
            if isinstance(anc, Tag) and anc.get("id") in skip_ids:
                where = f"skipped:{anc.get('id')}"
                break
        facts = _svg_facts(sv)
        fig = sv.find_parent("figure")
        if fig is None:
            fig = sv.find_parent(class_=lambda c: c and set(c.split()) & set(profile.get("figure_classes", [])))
        rec = {
            "file": Path(source).name, "index": n, "kind": "svg", "id": f"{stem}-d{n:02d}",
            "caption": "", "source": "dom", "elements": facts["elements"], "labels": facts["labels"],
            "viewbox": facts["viewbox"], "in_figure": fig is not None, "where": where, "section": "",
            "class": "decorative" if _is_decorative(facts, sv.attrs, where) else "content",
        }
        # caption lookup needs the walker's helper; replicate its rules here
        w = _Walk(soup, source, profile, {})
        if fig is not None:
            rec["caption"] = w.caption_for(fig)
        if not rec["caption"]:
            t = sv.find("title")
            rec["caption"] = _collapse(t.get_text(" ")) if t else (sv.get("aria-label") or "")
        records.append(rec)
        svg_meta[id(sv)] = rec
    # figures that hold only raster images
    for fig in soup.find_all("figure"):
        if fig.find("img") and not fig.find("svg"):
            n += 1
            w = _Walk(soup, source, profile, {})
            records.append({
                "file": Path(source).name, "index": n, "kind": "figure-img", "id": f"{stem}-f{n:02d}",
                "caption": w.caption_for(fig), "source": "dom", "elements": 0, "labels": [], "viewbox": "",
                "in_figure": True, "where": "content", "section": "", "class": "content"})
    # svgs built inside scripts (profile.script_steps)
    script_records = []
    ss = profile.get("script_steps")
    if ss:
        for sc in soup.find_all("script"):
            for obj in _js_array_objects(sc.get_text(), ss["var"]):
                svg = obj.get(ss.get("svg", "svg"))
                if not svg:
                    continue
                facts = _svg_info(svg)
                n += 1
                rec = {"file": Path(source).name, "index": n, "kind": "svg", "id": f"{stem}-d{n:02d}",
                       "caption": "", "source": "script", "elements": facts["elements"],
                       "labels": facts["labels"], "viewbox": facts["viewbox"], "in_figure": False,
                       "where": "content", "section": "", "class": "content"}
                script_records.append(rec)
                records.append(rec)
    for sel in profile.get("drop_selectors", []):
        for el in soup.select(sel):
            el.decompose()
    return soup, records, svg_meta, script_records


def _run(html, source, profile):
    soup, records, svg_meta, script_records = _prepare(html, source, profile)
    walk = _Walk(soup, source, profile, svg_meta)
    walk.script_svg_meta = list(script_records)
    body = soup.body or soup
    walk.children(body)
    walk.flush_steps()
    title_tag = soup.find("title")
    raw_title = _collapse(title_tag.get_text(" ")) if title_tag else Path(source).stem
    title = re.sub(r"\s*[|–—-]\s*(Interactive[^|–—]*|Animal Biotechnology[^|–—]*|[^|–—]*App)$", "", raw_title)
    sections = [s for s in walk.sections if s["blocks"]]
    chapter = {"id": Path(source).stem, "unit": profile.get("unit", ""), "title": {"en": title},
               "sections": sections, "skipped": walk.skipped, "extractor": VERSION}
    return chapter, records


def extract_chapter(html, source, profile):
    return _run(html, source, profile)[0]


def diagram_inventory(html, source, profile):
    chapter, records = _run(html, source, profile)
    return records


def count_unparsed(chapter):
    return sum(1 for s in chapter["sections"] for b in s["blocks"] if b["type"] == "unparsed")


def _strings(node, out):
    if isinstance(node, str):
        out.append(node)
    elif isinstance(node, list):
        for x in node:
            _strings(x, out)
    elif isinstance(node, dict):
        for k, v in node.items():
            if k not in IGNORED_KEYS:
                _strings(v, out)
    return out


def _corpus(chapter):
    return _norm("\n".join(_strings(chapter["sections"], [])))


def text_coverage(html, chapter, profile):
    """Source text nodes (outside chrome, skipped sections and scripts) missing from the chapter."""
    soup = BeautifulSoup(html, "lxml")
    for sel in profile.get("drop_selectors", []):
        for el in soup.select(sel):
            el.decompose()
    for sid in profile.get("skip_ids", []):
        for el in soup.find_all(id=sid):
            el.decompose()
    corpus = _corpus(chapter)
    missing, seen = [], set()
    root = soup.body or soup
    for node in root.find_all(string=True):
        if isinstance(node, Comment):
            continue
        if any(isinstance(a, Tag) and a.name in DROP_TAGS for a in node.parents):
            continue
        raw = _collapse(str(node))
        unit = _norm(raw)
        if not unit or not _has_alnum(unit) or unit in seen:
            continue
        seen.add(unit)
        if unit not in corpus:
            missing.append(raw)
    return missing


_LIT = re.compile(r"""("(?:[^"\\\n]|\\.)*"|'(?:[^'\\\n]|\\.)*'|`[^`]*`)""", re.S)


def script_orphans(html, chapter, quiz_texts, profile):
    """Prose-like string literals in scripts that are in neither the chapter nor the quiz bank."""
    soup = BeautifulSoup(html, "lxml")
    corpus = _corpus(chapter)
    quiz = _norm("\n".join(quiz_texts))
    ignore = profile.get("ignore_script_literals", [])
    orphans = []
    for sc in soup.find_all("script"):
        code = sc.get_text()
        for m in _LIT.finditer(code):
            lit, _ = _read_js_string(m.group(1), 0)
            pieces = BeautifulSoup(lit, "lxml").get_text("\n").split("\n") if "<" in lit else [lit]
            for piece in pieces:
                piece = _collapse(piece)
                if len(piece.split()) < 6 or any(s in piece for s in ignore):
                    continue
                n = _norm(piece)
                if n in corpus or n in quiz:
                    continue
                if piece not in orphans:
                    orphans.append(piece)
    return orphans


# --------------------------------------------------------------------------- profiles and CLI

def load_profile(stem, profile_dir):
    base = json.loads((profile_dir / "_default.json").read_text(encoding="utf-8"))
    f = profile_dir / f"{stem}.json"
    if f.exists():
        base.update(json.loads(f.read_text(encoding="utf-8")))
    return base


def unit_files(syllabus_path, unit):
    data = json.loads(Path(syllabus_path).read_text(encoding="utf-8"))
    files = []
    for u in data["units"]:
        if u["roman"] == unit:
            for t in u["topics"]:
                if t["file"] not in files:
                    files.append(t["file"])
    return files


def quiz_texts(quizbank_path, file):
    data = json.loads(Path(quizbank_path).read_text(encoding="utf-8"))
    out = []
    for q in data.get(file, []):
        out += [q.get("q", ""), q.get("e", "")] + list(q.get("o", []))
    return out


def build_report(results, unit):
    L = [f"# Extract report, Unit {unit}", "",
         "Generated by `tools/extract/extract.py`. Do not edit by hand.", ""]
    L += ["## Per-file results", "",
          "| File | Sections | Blocks | Unparsed | Coverage gaps | Script orphans | Skipped by design | Quiz-bank questions |",
          "|---|---|---|---|---|---|---|---|"]
    for r in results:
        nb = sum(len(s["blocks"]) for s in r["chapter"]["sections"])
        L.append(f"| `{r['file']}` | {len(r['chapter']['sections'])} | {nb} | {r['unparsed']} | "
                 f"{len(r['coverage'])} | {len(r['orphans'])} | {', '.join(r['chapter']['skipped']) or '-'} | "
                 f"{r['quiz_n']} |")
    L.append("")
    for r in results:
        if r["unparsed"] or r["coverage"] or r["orphans"]:
            L += [f"### Problems in `{r['file']}`", ""]
            for b in [b for s in r["chapter"]["sections"] for b in s["blocks"] if b["type"] == "unparsed"]:
                L.append(f"- unparsed `{b['selector']}`: {b['text'][:120]}")
            L += [f"- coverage gap: {t[:120]}" for t in r["coverage"]]
            L += [f"- script orphan: {t[:120]}" for t in r["orphans"]]
            L.append("")
    # diagram inventory
    inv = [d for r in results for d in r["inventory"]]
    L += ["## Diagram inventory", ""]
    dom = [d for d in inv if d["source"] == "dom" and d["kind"] == "svg"]
    scr = [d for d in inv if d["source"] == "script"]
    imgs = [d for d in inv if d["kind"] == "figure-img"]
    content = [d for d in inv if d["class"] == "content" and d["kind"] == "svg"]
    deco = [d for d in inv if d["class"] == "decorative"]
    raw = sum(r["raw_svg"] for r in results)
    L += ["| File | Raw `<svg` strings (regex) | In DOM | Built in scripts | In `<figure>`/figure class | Content (rebuild) | Chrome/decorative |",
          "|---|---|---|---|---|---|---|"]
    for r in results:
        i = r["inventory"]
        L.append(f"| `{r['file']}` | {r['raw_svg']} | {sum(1 for d in i if d['source']=='dom' and d['kind']=='svg')} | "
                 f"{sum(1 for d in i if d['source']=='script')} | {sum(1 for d in i if d['in_figure'])} | "
                 f"{sum(1 for d in i if d['class']=='content' and d['kind']=='svg')} | "
                 f"{sum(1 for d in i if d['class']=='decorative')} |")
    L.append(f"| **Total** | **{raw}** | **{len(dom)}** | **{len(scr)}** | **{sum(1 for d in inv if d['in_figure'])}** | "
             f"**{len(content)}** | **{len(deco)}** |")
    L += ["", "### Reconciliation with the spec's regex figure", "",
          f"- Raw regex count of `<svg` over the pilot files: **{raw}** (spec/plan: 32).",
          f"- Of these, **{len(dom)}** are real elements in the page and **{len(scr)}** are SVG markup held in "
          f"JavaScript template strings (the cDNA stepper), so a DOM-only count under-reports content diagrams.",
          f"- Diagrams to rebuild in M5 (class `content`): **{len(content)}**. Chrome/decorative (not rebuilt): **{len(deco)}**.",
          f"- SVGs nested inside a figure wrapper: {sum(1 for d in inv if d['in_figure'])}; raster-only figures: {len(imgs)}.",
          "- Classification rule: decorative if it sits in dropped chrome or a skipped section, declares width/height "
          "of 32 or less, or has no `<text>` label and 3 or fewer shape elements.", ""]
    L += ["### Every diagram", "", "| id | source | section | caption | elements | labels | class |", "|---|---|---|---|---|---|---|"]
    for d in inv:
        L.append(f"| `{d['id']}` | {d['source']} | {d['section'] or d['where']} | {d['caption'][:60]} | "
                 f"{d['elements']} | {len(d['labels'])} | {d['class']} |")
    L.append("")
    return "\n".join(L)


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("--unit", required=True)
    ap.add_argument("--html-dir", default="html-suite")
    ap.add_argument("--syllabus", default="app/src/main/assets/syllabus.json")
    ap.add_argument("--quizbank", default="app/src/main/assets/quizbank.json")
    ap.add_argument("--profiles", default=str(Path(__file__).resolve().parent / "profiles"))
    ap.add_argument("--out", required=True)
    ap.add_argument("--report", required=True)
    ap.add_argument("--diagrams", required=True)
    a = ap.parse_args(argv)
    out = Path(a.out)
    out.mkdir(parents=True, exist_ok=True)
    results, failed = [], False
    for f in unit_files(a.syllabus, a.unit):
        html = (Path(a.html_dir) / f).read_text(encoding="utf-8")
        prof = {**load_profile(Path(f).stem, Path(a.profiles)), "unit": a.unit}
        chapter, inv = _run(html, f, prof)
        qt = quiz_texts(a.quizbank, f)
        r = {"file": f, "chapter": chapter, "inventory": inv, "unparsed": count_unparsed(chapter),
             "coverage": text_coverage(html, chapter, prof),
             "orphans": script_orphans(html, chapter, qt, prof),
             "quiz_n": len(json.loads(Path(a.quizbank).read_text(encoding="utf-8")).get(f, [])),
             "raw_svg": html.count("<svg")}
        results.append(r)
        (out / (Path(f).stem + ".json")).write_text(
            json.dumps(chapter, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
        failed |= bool(r["unparsed"] or r["coverage"] or r["orphans"])
    Path(a.report).parent.mkdir(parents=True, exist_ok=True)
    Path(a.report).write_text(build_report(results, a.unit), encoding="utf-8")
    Path(a.diagrams).write_text(
        json.dumps([d for r in results for d in r["inventory"]], ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    print(f"extracted {len(results)} files; failures: {'YES' if failed else 'none'}")
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main())
