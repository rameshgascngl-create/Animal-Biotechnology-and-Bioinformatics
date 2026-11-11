"""Shared, source-cited terminology corrections for this biotechnology corpus.
These electrophoresis corrections are NOT a general translation of electrolysis.
"""
import json
import unicodedata
from pathlib import Path
GLOSSARY=json.loads((Path(__file__).parent/'i18n/tamil_glossary.json').read_text())

def normalise_terms(text):
    for old,new in GLOSSARY['replacements']:
        text=text.replace(old,new)
    return unicodedata.normalize('NFC',text)

if __name__=='__main__':
    # Retain JSON layout and every English field; replacements contain only Tamil.
    paths=list(Path('app/src/main/assets/content').glob('u*/*.json'))
    paths += [Path('app/src/main/assets')/f for f in ['quizbank.json','revision.json','syllabus.json']]
    paths += [Path('tools/diagrams/ta_strings.json')]
    for path in paths:
        old=path.read_text();new=normalise_terms(old)
        if new!=old:path.write_text(new);print(path)
