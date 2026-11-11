# Detector validation (tools/diagrams/audit_geometry.py)

Compared against my own visual verdicts for all 140 rendered figures, English and Tamil (280 states). The 5 hand-art figures contain no text and are outside the detector.
Detector run: `--width 300` (real inline width), now including the new `arrow_text` kind. "Flagged" = at least one structural defect (outside, text_text, text_stroke, text_edge, covered, shape_overlap, stroke_cross, arrow_text); `small_text` and `margin` are not counted here because they say nothing about collisions.

## Verdict levels (one line each)
- **ok** - no collision, clipping or crowding visible; every label can be read apart from its size.
- **minor** - cosmetic only: tight spacing, mixed font sizes, a label touching an edge; nothing can be misread.
- **moderate** - at least one label collides with, overlaps or is clipped by another element, but every label can still be read or inferred; or text is so small (about 5 sp) that it fails on size alone.
- **major** - several labels unreadable, overlapped or clipped, or text cut at the canvas edge, so a step could be missed or misread.
- **severe** - the figure's main message is obscured: collisions on the key structure plus unreadable labels.

## Confusion tables (rows = my verdict, columns = structural defects detected)
English
| verdict | 0 | 1-2 | 3-5 | 6+ | n |
|---|---|---|---|---|---|
| ok | 67 | 2 | 1 | 1 | 71 |
| minor | 5 | 7 | 9 | 5 | 26 |
| moderate | 1 | 2 | 7 | 10 | 20 |
| major | 4 | 2 | 2 | 10 | 18 |
| severe | 0 | 0 | 0 | 5 | 5 |

Tamil
| verdict | 0 | 1-2 | 3-5 | 6+ | n |
|---|---|---|---|---|---|
| ok | 39 | 1 | 2 | 1 | 43 |
| minor | 26 | 6 | 7 | 5 | 44 |
| moderate | 1 | 5 | 6 | 8 | 20 |
| major | 4 | 2 | 3 | 19 | 28 |
| severe | 0 | 0 | 0 | 5 | 5 |

Treating "moderate or worse" as positive and "flagged" as at least 1 structural defect: English recall 0.88 (38 of 43), precision 0.60; Tamil recall 0.91 (48 of 53), precision 0.69.

## False negatives (verdict moderate or worse, 0 structural defects): 10 states, 5 figures
- molecular_markers_app-d02, -d03, protein_databases_app-d03, -d04 (EN and TA): my verdict is major because the text is about 4 sp, not because of collisions. These are legibility failures the `small_text` kind catches; they are not collision false negatives.
- transgenic_animal_technology-d02 (EN and TA): moderate, text about 5 sp, no collisions.
So for collisions the detector missed nothing I judged; for size it relies on `small_text`.

## False positives (verdict ok, 3 or more structural defects): 5 states, 3 figures
- atlas3_transgenic_medical-d13 (EN, TA; 20 each): base-pair tick lines end flush against the sequence letters and are counted as `arrow_text`. Layout is clean; the ticks are meant to touch. (Separate accuracy finding below.)
- gel_electrophoresis-d03 (EN, TA; 3 each) and animal_cell_culture-d03 (TA; 4): thin ticks and connectors touching labels by design.
Precision therefore stays moderate until intentional contact (ticks, leader lines ending at a label) is whitelisted.

## The dna_library_genomic_cdna-d01 case
The old detector reported 0 defects although the arrowheads sit on the "Step 2/3/4" titles and the arrow labels.
Cause: arrowheads are SVG `marker`s inside `<defs>`, which the detector skipped, so they never entered any test.
Fix: markers are now added as virtual shapes at the line end and tested against the full text box by `arrow_text`.
Result: 3 `arrow_text` flags per language (the three arrow labels "Restriction enzyme...", "DNA ligase...", "Transform into E. coli"). The contact of the arrowhead tip with the step titles themselves is about 1-2 px and is still **not** flagged; I am not claiming it is caught.

## Not yet a regression gate
- Tuned on two smoke figures plus these 140; thresholds (trim 8% top / 12% bottom, overlap 1.5 x 0.6 px) are judgement calls.
- Intentional-contact whitelist missing (see false positives).
- Emoji glyphs (not in the fonts) and rotated text are measured by browser boxes only.
- Use it as a gate only together with `minsp >= 11` and a person looking at the severe/major sheets.
