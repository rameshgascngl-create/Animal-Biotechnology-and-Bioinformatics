#!/usr/bin/env python3
"""Reject desktop pointer instructions in Android reader and Search copy."""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "app/src/main/assets"
EN = re.compile(
    r"\b(?:click(?:s|ed|ing|able)?|hover(?:s|ed|ing)?|mouseover|"
    r"mouse[\s-]+(?:over|button|wheel|pointer|cursor))\b",
    re.I,
)
TA = re.compile(r"சொடுக்|சுட்டியை|சுட்டியால்")
TEXT_KEYS = {"en", "ta", "text", "title", "caption", "question", "answer"}


def reader_strings(value, *, heading_arrays=False, key=None):
    if isinstance(value, dict):
        for name, child in value.items():
            yield from reader_strings(child, heading_arrays=heading_arrays, key=name)
    elif isinstance(value, list):
        for child in value:
            yield from reader_strings(child, heading_arrays=heading_arrays, key=key)
    elif isinstance(value, str) and (heading_arrays or key in TEXT_KEYS):
        yield key, value


def pointer_instructions(value, *, heading_arrays=False):
    for key, text in reader_strings(value, heading_arrays=heading_arrays):
        if (TA if key == "ta" else EN).search(text):
            yield text


def main():
    files = sorted(ASSETS.rglob("*.json")) + sorted(ROOT.glob("tools/figures/src/*/strings.json"))
    violations = set()
    for path in files:
        try:
            value = json.loads(path.read_text(encoding="utf-8"))
        except (OSError, ValueError) as exc:
            print(f"Unreadable {path.relative_to(ROOT)}: {exc}")
            return 1
        for text in pointer_instructions(value, heading_arrays=path == ASSETS / "headings.json"):
            violations.add((path.relative_to(ROOT).as_posix(), text))
    print(f"Reader pointer instructions: {len(violations)} (tolerated: 0)")
    for path, text in sorted(violations):
        print(f"  {path}: {text[:100].replace(chr(10), ' ')}")
    return int(bool(violations))


if __name__ == "__main__":
    raise SystemExit(main())
