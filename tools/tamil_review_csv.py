"""Write the Tamil review CSV for every chapter that has Tamil: columns file,path,en,ta,status,reviewer_note.
Usage: tamil_review_csv.py [--dir app/src/main/assets/content] [--out docs/audit/tamil-review-chapters.csv]"""
import argparse, csv, json, sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parent))
from ta_io import walk

ap = argparse.ArgumentParser()
ap.add_argument("--dir", default="app/src/main/assets/content")
ap.add_argument("--out", default="docs/audit/tamil-review-chapters.csv")
a = ap.parse_args()
n = 0
with open(a.out, "w", newline="", encoding="utf-8-sig") as o:
    w = csv.writer(o)
    w.writerow(["file", "path", "en", "ta", "status", "reviewer_note"])
    for f in sorted(Path(a.dir).glob("u*/*.json")):
        for p, d in walk(json.loads(f.read_text(encoding="utf-8"))):
            if "ta" in d:
                w.writerow([f.name, p, d["en"], d["ta"], d.get("ta_status", ""), ""]); n += 1
print(f"{n} rows")
