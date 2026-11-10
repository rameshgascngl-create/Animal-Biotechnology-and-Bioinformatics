from tools.tamil_terms import normalise_terms

def test_electrophoresis_terms_and_idempotence():
    value=normalise_terms('ஜெல் மின்னாற்பகுப்பால் DNA பிரிகிறது')
    assert value=='இழும மின்னாற்பிரித்தலால் DNA பிரிகிறது'
    assert normalise_terms(value)==value
    assert normalise_terms('DNA, PCR, electrolysis')=='DNA, PCR, electrolysis'
