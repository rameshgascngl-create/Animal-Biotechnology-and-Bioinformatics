# Phase 0 figure audit (no figure edited)

Source: `tools/diagrams/audit_geometry.py` (geometric check on the rendered SVG, real fonts, 360 dp wide, 3x), `tools/diagrams/contact_sheet.py`.
Per-figure table: `figure-audit-phase0.csv` / `.json`. Visual verdicts are mine, from viewing every contact sheet; they are not CI results.

## Unverified / failed first
- Nothing was run on a device; no emulator was used for this audit. It is a render-time geometric check plus my viewing of the PNGs.
- The detector understates damage and also misses defects: `dna_library_genomic_cdna-d01` has 0 detector defects but arrows run through step titles when viewed.
- The five hand-art figures (gel_electrophoresis-d01, gene_cloning_steps_selection-d05, vectors_genetic_engineering-d01, hybridoma_monoclonal_antibodies-d01, gene_transfer_methods_rdna_host_cells-d04) contain no text, so the geometric check does not apply; they were viewed only as the single shared EN/TA image.
- Tamil strings missing from the translation table in 96 figures were not re-counted in this pass.

## Headline numbers (140 rendered figures)
- Rendered text below 11 sp at 360 dp: EN 133/140 figures (median minimum 5.8 sp, worst 3.9); TA 140/140 (median minimum 3.5 sp, worst 1.7).
- Structural defects (outside, text/text, text/stroke, edge, covered, shape overlap, stroke cross): EN 60/140, TA 67/140.
- My visual verdict, EN: ok 71, minor 26, moderate 20, major 18, severe 5. TA: ok 43, minor 44, moderate 20, major 28, severe 5.
- The 5 severe (both languages): vectors_genetic_engineering-d04, sanger_dna_sequencing-d01, screening_selection_recombinants-d01, -d02, -d04.

## Accuracy queries found while viewing
- sanger d03: chromatogram reads 5'-ATGCATCGA-3'; the d02 template (3' TACGTAGCAT 5') predicts ATGCATCGTA. Last base inconsistent across figures.
- screening d03: blue product labelled "5-bromo-4-chloro-indigo"; the blue dimer is 5,5'-dibromo-4,4'-dichloro-indigo.
- amp-r / tet-s superscripts render tiny or blank in screening d01/d02 (resistance vs sensitivity is the point of the figure).
- Colour-only cues: blue/white colonies are labelled in screening d03 (fine); the hand-art plate (gene_cloning d05) and plasmid ring (vectors d01) carry no labels at all.
- Wide 5-step strips (molecular_markers d01-d03, protein_databases d03, gene_therapy d05) render at about 4 sp: they need a vertical layout.
- Dark-canvas figures (animal_cell_culture d01-d07 etc.) need checking against the opaque-background rule in Phase 1.

## Re-ranked shortlist (viewed)
1. screening d02 insertional inactivation (severe, EN+TA) - was #5
2. screening d01 direct selection (severe) - was #6, a layout defect, not just Batch A
3. sanger d01 dNTP vs ddNTP (severe) - was #7
4. vectors d04 M13 (severe, new)
5. screening d04 colony hybridization (severe, new)
6. SV40 advanced_vectors d03 (major) - was #1
7. dna_library d04 oligo-dT (major) - was #4
8. vectors d02 Ti plasmid (moderate EN / major TA) - was #8
9. gene_transfer d02 calcium phosphate (moderate) - was #2
10. atlas3 d02 transgenic mouse flow (moderate EN / major TA) - was #3
- gene_cloning d05 (old #10) is the hand-art plate and is fine structurally; it is not a fix.
- dna_library d01 (old #9): moderate, arrows through titles, undetected by the checker.
