"""Tamil `*_ta` siblings for quizbank.json, revision.json and syllabus.json (M3b).

A string field X gets a sibling X_ta; a list of strings (o, kp) gets X_ta, a list of equal length.
Scopes: quiz:<file.html> | rev:<file.html> | syl
Usage: ta_aux.py export SCOPE OUT.json | merge SCOPE TA.json | validate [--strict]
TA.json: {"<flat path>": "tamil"}; paths come from export. English is never touched.
"""
import json
import re
import sys
import unicodedata
from pathlib import Path

ASSETS = Path("app/src/main/assets")
FILES = {"quiz": "quizbank.json", "rev": "revision.json", "syl": "syllabus.json"}
TAMIL = re.compile("[஀-௿]")


def _load(kind):
    return json.loads((ASSETS / FILES[kind]).read_text(encoding="utf-8"))


def _save(kind, data):
    (ASSETS / FILES[kind]).write_text(json.dumps(data, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")


def _targets(kind, data, key=None):
    """Yield (flat_path, holder, field, index_or_None) for every translatable string."""
    if kind == "quiz":
        for i, m in enumerate(data[key]):
            yield f"{i}/q", m, "q", None
            for k in range(len(m["o"])):
                yield f"{i}/o/{k}", m, "o", k
            if m.get("e"):
                yield f"{i}/e", m, "e", None
    elif kind == "rev":
        r = data[key]
        yield "title", r, "title", None
        for i in range(len(r["kp"])):
            yield f"kp/{i}", r, "kp", i
        for i, s in enumerate(r["sa"]):
            yield f"sa/{i}/q", s, "q", None
            yield f"sa/{i}/a", s, "a", None
        for i, g in enumerate(r["gl"]):
            yield f"gl/{i}/t", g, "t", None
            yield f"gl/{i}/d", g, "d", None
    else:
        for ui, u in enumerate(data["units"]):
            yield f"u{ui}/title", u, "title", None
            for ti, t in enumerate(u["topics"]):
                yield f"u{ui}/t{ti}/title", t, "title", None
                if t.get("note"):
                    yield f"u{ui}/t{ti}/note", t, "note", None
        for f, m in data["modules"].items():
            yield f"m/{f}/title", m, "title", None


def _scope(s):
    kind, _, key = s.partition(":")
    return kind, (key or None)


def _value(holder, field, idx):
    v = holder[field]
    return v if idx is None else v[idx]


def export(scope):
    kind, key = _scope(scope)
    d = _load(kind)
    return {p: _value(h, f, i) for p, h, f, i in _targets(kind, d, key)}


def merge(scope, ta):
    kind, key = _scope(scope)
    d = _load(kind)
    by = {p: (h, f, i) for p, h, f, i in _targets(kind, d, key)}
    bad = [p for p in ta if p not in by]
    if bad:
        raise KeyError(f"unknown paths: {bad[:5]}")
    for p, text in ta.items():
        h, f, i = by[p]
        text = unicodedata.normalize("NFC", text)
        if i is None:
            h[f + "_ta"] = text
        else:
            lst = h.setdefault(f + "_ta", list(h[f]))
            lst[i] = text
    _save(kind, d)


def validate():
    errs, total, missing = [], 0, 0
    for kind in FILES:
        d = _load(kind)
        keys = [None] if kind == "syl" else list(d)
        for key in keys:
            for p, h, f, i in _targets(kind, d, key):
                total += 1
                en = _value(h, f, i)
                ta = h.get(f + "_ta")
                ta = ta if i is None or ta is None else ta[i]
                where = f"{kind}:{key}:{p}"
                if ta is None or (i is not None and ta == en and not TAMIL.search(ta) and False):
                    missing += 1
                    continue
                if not ta.strip():
                    errs.append(f"{where}: empty ta")
                elif unicodedata.normalize("NFC", ta) != ta:
                    errs.append(f"{where}: not NFC")
                elif not TAMIL.search(ta) and ta.strip() != en.strip():
                    errs.append(f"{where}: no Tamil script and differs from en")
    return errs, total, missing


if __name__ == "__main__":
    cmd = sys.argv[1]
    if cmd == "export":
        json.dump(export(sys.argv[2]), open(sys.argv[3], "w", encoding="utf-8"), ensure_ascii=False, indent=0)
    elif cmd == "merge":
        merge(sys.argv[2], json.load(open(sys.argv[3], encoding="utf-8")))
    else:
        e, t, m = validate()
        print(f"{t} strings, {m} without Tamil yet, {len(e)} problems")
        for x in e[:20]:
            print(" ", x)
        sys.exit(1 if e or (m and "--strict" in sys.argv) else 0)
