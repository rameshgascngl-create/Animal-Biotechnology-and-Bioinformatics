"""Link each atlas figure to the chapter section whose text it illustrates.

Matching is lexical (IDF-weighted token overlap between the figure's caption, labels and the atlas
text beside it, and the English text of each section of the other chapters in the same unit).
It is a heuristic: every link goes to a review CSV with a margin over the runner-up.
Writes app/src/main/assets/figure_anchors.json and docs/audit/figure-anchors-review.csv.
"""
import csv, glob, json, math, os, re, sys
from collections import Counter

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
CONTENT = os.path.join(ROOT, "app/src/main/assets/content")
STOP = set("the a an of and or in on to for with by is are as at from that this it its be can which into than then their these those not no".split())

GENERIC = re.compile(r"summary|interactive|overview|exam|quiz|practice|glossary|objectives|self.?test|revision|further reading|references", re.I)

def key(t):
    """ASCII letters/digits only, lower-case, first 40: identical in Python and Kotlin (no UTF-16 surprises)."""
    return re.sub(r"[^a-z0-9]", "", t.lower())[:40]

def toks(s):
    return [w for w in re.findall(r"[a-z0-9]+", s.lower()) if w not in STOP and len(w) > 2]

def text(b):
    if isinstance(b, dict) and "en" in b: return b["en"]
    return ""

def block_text(b):
    t = b["type"]
    if t == "p": return text(b["text"])
    if t == "bullets": return " ".join(text(i) for i in b["items"])
    if t == "callout": return text(b["text"])
    return ""

def main():
    chapters = {}
    for f in sorted(glob.glob(CONTENT + "/u*/*.json")):
        unit = os.path.basename(os.path.dirname(f))
        stem = os.path.basename(f)[:-5]
        chapters[(unit, stem)] = json.load(open(f))
    df = Counter(); secs = []
    for (unit, stem), d in chapters.items():
        if stem.startswith("atlas"): continue
        for si, s in enumerate(d["sections"]):
            if GENERIC.search(text(s["title"])): continue
            body = [block_text(b) for b in s["blocks"]]
            tk = toks(text(s["title"])) * 3 + toks(" ".join(body))
            secs.append((unit, stem, si, s, body, Counter(tk)))
            df.update(set(tk))
    N = len(secs)
    idf = lambda w: math.log((N + 1) / (df.get(w, 0) + 1)) + 1
    out, rows = [], []
    for (unit, stem), d in chapters.items():
        if not stem.startswith("atlas"): continue
        for s in d["sections"]:
            blocks = s["blocks"]
            for bi, b in enumerate(blocks):
                if b["type"] != "diagram": continue
                near = " ".join(block_text(x) for x in blocks[max(0, bi - 2):bi])
                q = Counter(toks(text(b["caption"])) * 2 + toks(" ".join(b.get("labels", []))) + toks(near))
                scored = []
                for (u, st, si, sec, body, tf) in secs:
                    if u != unit: continue
                    sc = sum(min(c, tf[w] and 3) * idf(w) for w, c in q.items() if w in tf)
                    sc /= math.sqrt(sum(tf.values()) + 50)
                    scored.append((sc, st, si, sec, body))
                scored.sort(key=lambda x: -x[0])
                sc, st, si, sec, body = scored[0]
                run = scored[1][0] if len(scored) > 1 else 0
                # within the section, place after the block that best matches the figure
                best, bj = -1, None
                for j, t in enumerate(body):
                    if not t: continue
                    if j > 0 and re.search(r"objective|exam tip|pg focus", body[j - 1], re.I): continue
                    v = sum(idf(w) for w in set(toks(t)) if w in q) / math.sqrt(len(toks(t)) + 10)
                    if v > best: best, bj = v, j
                if bj is None: bj = len(body) - 1
                placed = sc >= 1.0 or sc - run >= 0.3
                if placed: out.append({"id": b["id"], "chapter": st, "unit": unit, "section": sec["id"],
                            "after": key(body[bj]), "score": round(sc, 3), "margin": round(sc - run, 3)})
                rows.append([b["id"], text(b["caption"])[:70], f"{st}/{sec['id']}" if placed else "NOT PLACED (low confidence)", text(sec["title"])[:50],
                             round(sc, 3), round(sc - run, 3)])
    json.dump(out, open(os.path.join(ROOT, "app/src/main/assets/figure_anchors.json"), "w"), ensure_ascii=False, indent=1)
    rows.sort(key=lambda r: r[5])
    with open(os.path.join(ROOT, "docs/audit/figure-anchors-review.csv"), "w", newline="") as fh:
        w = csv.writer(fh); w.writerow(["figure", "caption", "placed_in", "section_title", "score", "margin_over_runner_up"]); w.writerows(rows)
    print(len(out), "placed;", len(rows) - len(out), "left in atlas only")

main()
