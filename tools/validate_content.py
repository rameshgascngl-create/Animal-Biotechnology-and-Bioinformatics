"""Validate Tamil coverage and shape of chapter JSON (spec 6, plan Task 6).
Usage: validate_content.py --chapters [--dir app/src/main/assets/content] [--units u1,u2] ; exits 1 on any failure.
"""
import argparse
import json
import re
import sys
import unicodedata
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
from ta_io import walk  # noqa: E402

TAMIL = re.compile("[஀-௿]")
STATUSES = {"draft", "reviewed", "bracketed"}


def check_chapter(chapter, name="chapter"):
    errs = []
    for path, d in walk(chapter):
        where = f"{name}:{path}"
        ta = d.get("ta", "")
        if not d.get("en", "").strip() and "ta" in d:
            continue  # empty source cell (e.g. table corner): nothing to translate
        if not ta.strip():
            errs.append(f"{where}: empty ta")
            continue
        if d.get("ta_status") not in STATUSES:
            errs.append(f"{where}: invalid ta_status {d.get('ta_status')!r}")
        if unicodedata.normalize("NFC", ta) != ta:
            errs.append(f"{where}: ta is not NFC")
        if not TAMIL.search(ta) and ta.strip() != d["en"].strip():
            errs.append(f"{where}: ta has no Tamil script and differs from en")
    return errs


def main(argv=None):
    ap = argparse.ArgumentParser()
    ap.add_argument("--chapters", action="store_true")
    ap.add_argument("--dir", default="app/src/main/assets/content")
    ap.add_argument("--units", default="u1")
    a = ap.parse_args(argv)
    errs, n = [], 0
    for u in a.units.split(","):
        for f in sorted((Path(a.dir) / u).glob("*.json")):
            c = json.loads(f.read_text(encoding="utf-8"))
            n += sum(1 for _ in walk(c))
            errs += check_chapter(c, f.name)
    for e in errs[:50]:
        print(e)
    print(f"{n} strings checked; {len(errs)} problems")
    return 1 if errs else 0


if __name__ == "__main__":
    sys.exit(main())
