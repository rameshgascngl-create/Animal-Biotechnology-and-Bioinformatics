# Do the audit renders match what ships? (item 3)

Method: `tools/diagrams/render_vs_shipped.py`. Each of the 140 audit renders (360 dp wide at 3x, English and Tamil, from the HTML source SVGs) is compared with the shipped PNG in `assets/diagrams`, composited on white and resized to the render. Metrics: SSIM (grayscale), mean absolute difference, aspect-ratio error. Per-figure results: `docs/audit/render-vs-shipped.csv` (280 rows). Resolutions differ (1080 px vs 1440-1800 px), so SSIM is lowered by resampling alone; it is a screen, not proof of identity.

## Result
| verdict | EN | TA |
|---|---|---|
| match (SSIM >= 0.90) | 77 | 73 |
| minor (0.75-0.90) | 58 | 62 |
| differs (< 0.75) | 5 | 5 |
Median SSIM 0.904. Aspect error above 2% only for 2 figures.

The 5 that differ (same figures in both languages):
- screening_selection_recombinants-d02: expected; the shipped file is the redrawn pilot (1440x3232), the audit render is the old source.
- atlas1_fundamentals_vectors-d08, atlas2_techniques-d05, atlas5_bioinformatics-d15: viewed side by side for atlas1-d08: same content and layout; the low score is a 3-4 px vertical offset. Not a content difference.
- **atlas3_transgenic_medical-d06: a real difference.** The shipped PNG is cropped on the right (aspect error 2.4%): the fourth box is cut, "Patent allowed" and "Diamond v. Chakrabarty" are clipped, and a pale strip is left beyond the box. The audit render shows the whole box. The audit therefore did not describe what ships for this figure. In both renders the box text also overlaps the incoming arrowhead (a collision the audit does flag).
I did not view all 114 "minor" pairs; I viewed 2 figures (4 states). "Match" therefore means a high structural similarity, not a human check of every pixel.

## Shipped Tamil images with English fallback words
I re-derived the list from the Tamil-state scan (`/tmp/ta_latin.json`, produced by `tamil_fallback_check.py`) with a word filter: it finds **30** figures, not the 32 I reported earlier; I cannot yet say which two I counted differently (the earlier list was hand-judged). The 30:
atlas1 d04, d07, d12, d14; atlas2 d04, d05, d08; atlas3 d08, d14; atlas5 d06, d12; bioinformatics_databases d03; crispr d02; dna_library d09; gel d03; gene_cloning d02, d03, d04; gene_therapy d03, d04; molecular_markers d04, d05, d06; pcr d01; protein_databases d04; sanger d01, d02; screening d01, d02 (replaced by the pilot), d03.

Viewed in the shipped Tamil PNGs (6 of the 30): gene_cloning d02 (BLUE, WHITE), pcr d01 (DENATURATION, ANNEALING, EXTENSION), gel d03 (WELLS), gene_therapy d03 (PATIENT, LABORATORY), screening d03 (NON-RECOMBINANT -> BLUE, RECOMBINANT -> WHITE, INSERT in MCS, vs, emoji circles). **All six ship mixed-language.** The other 24 I did not open; their Tamil renders match the shipped files by SSIM (apart from the cropped atlas3-d06, which is not in the list), so I infer they ship mixed-language too, but that is inference. In my crop of molecular_markers d04 the Ind1-3 labels were not visible, so I do not confirm those from the image.
Also seen: gene_cloning/screening d03 spells the X-gal product "5-bromo-4-chloro-indigo", which is the error already approved for correction in its batch.
