import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import validate_content as v  # noqa: E402
import ta_io  # noqa: E402


def chap(**leaf):
    return {"sections": [{"blocks": [{"type": "p", "text": {"en": "Enzymes", **leaf}}]}]}


def test_empty_ta_reported_with_path():
    assert "sections/0/blocks/0/text: empty ta" in v.check_chapter(chap(), "x")[0]


def test_invalid_status_rejected():
    assert any("invalid ta_status" in e for e in v.check_chapter(chap(ta="நொதிகள்", ta_status="ok")))


def test_valid_leaf_passes_and_latin_only_differs_fails():
    assert v.check_chapter(chap(ta="நொதிகள்", ta_status="draft")) == []
    assert any("no Tamil script" in e for e in v.check_chapter(chap(ta="Enzymez", ta_status="draft")))


def test_identical_symbols_allowed():
    c = {"sections": [{"blocks": [{"type": "p", "text": {"en": "EcoRI", "ta": "EcoRI", "ta_status": "draft"}}]}]}
    assert v.check_chapter(c) == []


def test_non_nfc_rejected():
    import unicodedata
    ta = unicodedata.normalize("NFD", "கொ")
    assert any("NFC" in e for e in v.check_chapter(chap(ta=ta, ta_status="draft")))


def test_merge_never_changes_en_and_rejects_unknown_path():
    c = chap()
    ta_io.merge(c, {"sections/0/blocks/0/text": "நொதிகள்"})
    leaf = c["sections"][0]["blocks"][0]["text"]
    assert leaf["en"] == "Enzymes" and leaf["ta_status"] == "draft"
    import pytest
    with pytest.raises(KeyError):
        ta_io.merge(c, {"nope": "x"})
