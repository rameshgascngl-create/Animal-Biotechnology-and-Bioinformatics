"""Export every bilingual string of a chapter JSON to a flat list, and merge Tamil back.

Paths are stable JSON pointers such as `sections/2/blocks/1/items/0`. Merge never touches `en`.
Usage: ta_io.py export CHAPTER.json OUT.json | ta_io.py merge CHAPTER.json TA.json
TA.json: {"<path>": "tamil" | {"ta": "...", "status": "draft|reviewed|bracketed"}}
"""
import json
import sys
import unicodedata


def walk(node, path=()):
    """Yield (path, dict) for every {"en": ...} leaf, in document order."""
    if isinstance(node, dict):
        if isinstance(node.get("en"), str):
            yield "/".join(map(str, path)), node
            return
        for k, v in node.items():
            yield from walk(v, path + (k,))
    elif isinstance(node, list):
        for i, v in enumerate(node):
            yield from walk(v, path + (i,))


def export(chapter):
    return {p: d["en"] for p, d in walk(chapter)}


def merge(chapter, ta):
    by_path = dict(walk(chapter))
    unknown = [p for p in ta if p not in by_path]
    if unknown:
        raise KeyError(f"unknown paths: {unknown[:5]}")
    for p, v in ta.items():
        d = by_path[p]
        text, status = (v, "draft") if isinstance(v, str) else (v["ta"], v.get("status", "draft"))
        d["ta"] = unicodedata.normalize("NFC", text)
        d["ta_status"] = status
    return chapter


if __name__ == "__main__":
    cmd, chap, other = sys.argv[1:4]
    c = json.load(open(chap, encoding="utf-8"))
    if cmd == "export":
        json.dump(export(c), open(other, "w", encoding="utf-8"), ensure_ascii=False, indent=0)
    else:
        merge(c, json.load(open(other, encoding="utf-8")))
        json.dump(c, open(chap, "w", encoding="utf-8"), ensure_ascii=False, indent=1)
        open(chap, "a").write("\n")
