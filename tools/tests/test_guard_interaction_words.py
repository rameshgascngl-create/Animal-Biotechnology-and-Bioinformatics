from tools.ci.guard_interaction_words import pointer_instructions


def test_search_heading_arrays_are_scanned():
    headings = {"lesson.html": ["Mouse over the figure", "Click to open", "Transgenic mouse"]}
    assert list(pointer_instructions(headings, heading_arrays=True)) == [
        "Mouse over the figure",
        "Click to open",
    ]


def test_pointer_phrases_are_blocked_but_zoology_and_touch_are_allowed():
    data = [
        {"en": "The super mouse grew rapidly."},
        {"en": "Use the mouse wheel."},
        {"en": "Tap to reveal."},
        {"ta": "சுட்டியை நகர்த்துக."},
    ]
    assert list(pointer_instructions(data)) == ["Use the mouse wheel.", "சுட்டியை நகர்த்துக."]
