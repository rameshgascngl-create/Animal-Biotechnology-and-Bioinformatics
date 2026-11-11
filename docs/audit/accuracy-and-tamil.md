# Accuracy queries and Tamil recount

Nothing in this file has been applied to a shipped asset.

## Sanger sequences (answer before changing anything)
`tools/figures/check_sanger_sequences.py` output:
```
d02 template 3'->5' : TACGTAGCAT 10 nt
d02 implies new 5'->3': ATGCATCGTA 10 nt
d03 chromatogram     : ATGCATCGA 9 nt
common prefix length : 8
```
- The two figures agree for 8 nt (ATGCATCG) and then diverge: d03 has `A` where d02 has `TA`. It is not a truncation and not a last-base typo: d03 is missing a T relative to d02.
- Lesson prose in both languages (html-suite line 1096 and `content/u2/sanger_dna_sequencing.json`) says `5'-ATGCATCGA...-3'`, i.e. it agrees with d03. Only the d02 template disagrees with the other four places (gel, label, chromatogram, prose).
- Position numbering also differs: d02 numbers the terminated positions after the primer base (T = pos 1); d03 starts the readout at the primer base (A = pos 1).
- My reading: d02's template is the odd one out. With template 3'-TACGTAGCT...-5' (9th base T, not A) the synthesised strand is ATGCATCGA..., matching d03 and the prose. The alternative is to change d03 and the prose to ATGCATCGTA. Your call; I change nothing until you choose.
- Planned fix after your decision: one data file (`tools/figures/data/sanger_sequence.json`: template 3'->5', primer, read-out numbering), both figures generated from it, complementarity asserted in code.

## X-gal product (confirmed by you)
The blue product is 5,5'-dibromo-4,4'-dichloro-indigo (from 5-bromo-4-chloro-3-hydroxyindole). Current text "5-bromo-4-chloro-indigo" occurs in lesson prose (screening html lines 645, 1433; gene_cloning 1305) and in two figures (screening-d03 label; gene_cloning-d02 "X-gal -> 5-bromo-4-" + Tamil key "blue 5-bromo-4-chloro-"). Because it also sits in the lesson text (CI-checked extraction) and the Tamil table, I have not edited it yet; it is a source-text change in html-suite + ta_strings, and the two figure PNGs would be re-rendered. Blue = non-recombinant, white = recombinant stays as is. Tell me whether to include that edit in the next commit.

## Superscripts (amp-r / tet-s)
- Current method: separate `<text>` elements at font-size 7-8 beside 10-pt labels. At the real inline width that is about 3.5-4 sp: it is the reason the resistance symbols read as blank.
- Renderer: Playwright Chrome supports `tspan baseline-shift="super"` and `dy`, so the method itself would work, but a superscript is about 65% of an already small size. Not acceptable at 11 sp targets.
- Pilot uses plain notation at full size: `AmpR`, `TetR`, `TetS` (R/S as capitals, same size). Note the lesson prose writes amp^r / tet^s (lower-case superscripts); I am following the phenotype notation Amp^R/Tet^S in plain form and flag the case mismatch with the prose for your decision.

## Atlas 3 d13 (new, found while checking colour/accuracy)
The figure shows the guide RNA (red) base-paired with the strand that carries the PAM. By convention the spacer has the same sequence as the PAM-bearing (non-target) strand and pairs with the other strand; and the guide's 5' end should be PAM-distal. As drawn, the spacer is the complement of the PAM strand and its 5' end is next to the PAM. Layout verdict was ok; accuracy is a separate defect. No change made.

## Unlabelled figures
gene_cloning-d05 (plate) and vectors_genetic_engineering-d01 (plasmid ring): untouched; covered under option (b) in the legibility spec.

## Tamil recount (tools/diagrams/tamil_fallback_check.py, shipped Tamil state of all 140 figures)
- 96 of 140 figures have at least one text node with no Tamil string; 861 of 2,540 text nodes (34%) have none. Most are numbers, units, symbols, gene/enzyme/organism names and acronyms (permitted Latin).
- 326 text nodes (238 unique strings) are Latin-only. Of those, **56 unique strings in 32 figures are English words/phrases, not permitted Latin** - this breaks the English-or-Tamil rule in Tamil mode:
  - colony/plate words: BLUE, WHITE, COLONY, RECOMBINANT, INSERT, INSERT in MCS, (DISRUPTED), DIES, SURVIVES AND GROWS, "RECOMBINANT -> WHITE", "NON-RECOMBINANT -> BLUE" (gene_cloning d02-d04, screening d03, atlas2 d04, atlas3 d08);
  - PCR: DENATURATION, ANNEALING, EXTENSION (pcr d01), "... 25-35 cycles ... final extension" (atlas2 d08);
  - people/process: LABORATORY, PATIENT (gene_therapy d03, d04), WELLS, "SMALLEST: FASTEST ->" (gel d03), Ind1/Ind2/Ind3 (molecular_markers d04-d06), "Primer: 5'" (sanger d02), "NO -OH!", vs (sanger d01);
  - connectors: or / to / up to / no / vs ("NHEJ or HDR", "EMBL or DDBJ", "to GenBank", "mRNA to cDNA", "up to 10 kb", "or sgRNA)", "or TTAA (piggyBac)");
  - sentences: "TEL telomere: stability of ends ARS: origin in yeast CEN: segregation" (atlas1 d12), the BLAST E-value formula legend (atlas5 d07), "X-RAY DIFFRACTION; NMR; ELECTRON MICROSCOPY" (atlas5 d12), LYTIC / LYSOGENIC (atlas1 d07).
  - Emoji (blue/white circles, star, folder) in protein_databases d04 and screening d03.
- Literal GenBank/PDB record keywords (LOCUS, DEFINITION, ACCESSION, ORIGIN, FEATURES, ORGANISM, REFERENCE, HEADER, SEQRES, HELIX, SHEET) are shown as they appear in the file format. I list them as permitted Latin unless you rule otherwise.

## Tamil terms I cannot confirm against the Tamil Nadu State Board books (not guessed)
Reused from the earlier table or proposed for the pilot; none is verified.
- insertional inactivation (pilot title): "செருகல் வழிச் செயலிழப்பு" - unconfirmed
- replica plating "நகல் தட்டிடல்", velvet pad "வெல்வெட் பட்டை", master plate "முதன்மைத் தட்டு" - unconfirmed
- resistant / sensitive (AmpR / TetS explained in words): "எதிர்ப்புத்திறன் கொண்டது / உணர்திறன் கொண்டது" - unconfirmed
- recombinant "மறுசேர்க்கை", non-recombinant "மறுசேர்க்கை அல்லாதது", colony "காலனி", plasmid "பிளாஸ்மிட்", vector "தாங்கிக்கடத்தி" - in use project-wide, State Board spelling not confirmed
- "no growth" "வளர்ச்சி இல்லை", "growth" "வளர்ச்சி" - unconfirmed
- Earlier list still open: மீளாய்வு, சேமிப்பு, வினாக்கள், நினைவட்டைகள், கலைச்சொற்கள், பொருளடக்கம், காகிதம், இரவு, செபியா, வாசிப்பு, அடுத்த அத்தியாயம், முதுநிலைக் கவனம், விலங்கு உயிரித் தொழில்நுட்பவியல்.
