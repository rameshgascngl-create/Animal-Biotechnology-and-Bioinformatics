import tools.ta_aux as A


def test_merge_sibling_and_list(tmp_path, monkeypatch):
    import json
    (tmp_path / "quizbank.json").write_text(json.dumps({"f.html": [{"q": "Q?", "o": ["a", "b"], "a": 0, "e": "E"}]}))
    monkeypatch.setattr(A, "ASSETS", tmp_path)
    ex = A.export("quiz:f.html")
    assert list(ex) == ["0/q", "0/o/0", "0/o/1", "0/e"]
    A.merge("quiz:f.html", {"0/q": "வினா?", "0/o/1": "ஆ"})
    m = json.loads((tmp_path / "quizbank.json").read_text())["f.html"][0]
    assert m["q"] == "Q?" and m["q_ta"] == "வினா?" and m["o_ta"] == ["a", "ஆ"]
