"""The cloning-vector term is கடத்தி (TN Class 12 Zoology, Micro Biology and Bio-Zoology).
The earlier coinage தாங்கிக்கடத்தி must not return, and the sandhi must follow the new initial க."""
import glob, json, re
from pathlib import Path

OLD = "தாங்கிக்கடத்தி"
ROOT = Path(__file__).resolve().parents[2]
SOURCES = [Path(p) for p in glob.glob(str(ROOT / "app/src/main/assets/content/u*/*.json"))]
SOURCES += [ROOT / "app/src/main/assets" / n for n in ("quizbank.json", "revision.json", "syllabus.json")]
SOURCES += [ROOT / "tools/diagrams/ta_strings.json"]
TEXT = {p: p.read_text(encoding="utf8") for p in SOURCES}
TA = r"஀-௿"


def hits(rx):
    return sorted({f"{p.name}: {m.group(0)}" for p, t in TEXT.items() for m in re.finditer(rx, t)})


def test_old_vector_term_absent_from_content_and_string_table():
    assert [p.name for p, t in TEXT.items() if OLD in t] == []
    assert hits(r"தாங்கிக்") == []


def test_no_wrong_doubling_before_the_term():
    # A word-final doubling consonant before கடத்தி can only be க் (the old initial த gave த்).
    # த், ப், ச் before கடத்தி, spaced or glued, are wrong: நகலாக்கத் கடத்தி, ...ப் கடத்தி, ...ச் கடத்தி.
    assert hits(rf"[{TA}]*[தபச]்[ \t]*கடத்தி[{TA}]*") == []
    assert hits(r"க்க்[ \t]*கடத்தி") == []


def test_no_repeated_glued_or_old_compound_term():
    assert hits(r"கடத்தி[ \t]+கடத்தி\b") == []
    assert hits(r"கடத்திகடத்தி") == []
    assert hits(r"தனித்[ \t]*கடத்தி") == []  # the compound is தனிக்கடத்தி
    assert hits(r"தனித்தாங்கி") == []


def test_string_table_uses_new_term():
    table = json.loads((ROOT / "tools/diagrams/ta_strings.json").read_text(encoding="utf8"))
    assert table["3. Vector"] == "3. கடத்தி"
