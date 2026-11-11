# Figure legibility spec (options a-d)

Status: for owner review. Nothing in this document has been implemented except the pilot (screening-d02) described at the end.
Data: `tools/diagrams/audit_geometry.py --width 300` over the 140 rendered figures (280 language states), 0 errors.
Per-figure numbers: `docs/audit/figure-audit-phase0.csv` (360 dp run) and the 300 dp re-run summarised below.

## 1. The measurement that matters: real inline width

In the reader a figure is not 360 dp wide. `LessonScreen` content padding is 20 dp each side and `DiagramView` adds a 10 dp card padding:

| screen | inline figure width |
|---|---|
| 360 dp phone | 360 - 2x20 - 2x10 = **300 dp** (not 360) |
| 411 dp phone | 411 - 60 = **351 dp** |

All "sp" figures below are rendered text size at that width (image text is fixed in dp; it does not follow the system font scale).
The earlier "133/140 English below 11 sp" was measured at 360 dp and flattered the position.

| shipped, inline, at 300 dp | pass at 9 sp | pass at 10 sp | pass at 11 sp | median smallest text | worst |
|---|---|---|---|---|---|
| English (140 figures) | 8 | 6 | **0** | 4.9 sp | 3.2 sp |
| Tamil (140 figures) | 0 | 0 | **0** | 2.95 sp | 1.4 sp |
| English at 351 dp (411 dp phone) | 10 | 8 | 7 | | |
| Tamil at 351 dp | 0 | 0 | 0 | | |

Shape of the problem: median figure height/width is 0.39 (90 of 140 are below 0.5), i.e. they are landscape slides squeezed into a portrait column.
Zoom needed to reach 11 sp from the inline size: English median 2.2x (p90 3.1x, max 3.4x); Tamil median 3.7x (p90 5.0x, max 7.8x).
Tamil is worse because the renderer shrinks Tamil strings to fit the English boxes (floor 0.55 of the font size), so Tamil is typically at about 60% of English size.
Median 15 labels per figure (p90 31, max 113); 90 figures have more than 12 labels.

Two facts about the current viewer that affect option (c):
- `DiagramView.decode` halves any bitmap wider than 1500 px (`inSampleSize = 2`). 212 of the ~285 PNGs are 1600-1800 px wide, so they are shown at ~800 px and look soft as soon as the user zooms. Any zoom feature needs this removed.
- A tap already opens a full-screen dialog with pinch-zoom (1-5x), but it is portrait-fit: a landscape figure is only 360 dp wide there (1.2x). It does not rotate.

## 2. Options, with numbers

Assumptions for (c): phone held landscape, usable area 760 x 330 dp; the figure is fitted inside it; scale factor = min(760/300, 330/figure height). Raster limit: PNGs are 1600 px wide, so up to about 530 dp at 3x density is 1:1; the viewer would need the bitmap decoded at full size (and new figures rendered at 1440 px).

### (a) Portrait-first canvases (vertical flows, minimum font relative to canvas width, fewer words)
- Canvas 360 units wide mapped to 300 dp, minimum font 13.2 units = 11 sp (10.8 units = 9 sp). Tall canvases (360 x 500-700).
- Pass at 9 / 10 / 11 sp: 100% of redrawn figures, enforced by a build gate (min text size, no collisions) - in both languages, Tamil laid out as the worst case. Unredrawn figures: unchanged (table in section 1).
- Text-volume check (estimate, 11 sp, text = 30% of canvas area): English fits a <= 600 dp canvas for 139/140 figures; Tamil for 126/140 (137 within 800 dp). The other 14 Tamil figures cannot hold their text at 11 sp without cutting words or using callouts (screening d01/d03/d04, sanger d01/d02, advanced_vectors d04, animal_cell_culture d07, atlas2 d02/d04, atlas3 d01/d02/d05, atlas5 d02, dna_library d02).
- Effort per figure: full redraw (geometry + EN/TA string table + Tamil terminology + before/after review). Roughly 1 working session per complex figure; the five wide 5-step strips (molecular_markers d01-d03, protein_databases d03, gene_therapy d05) are one template and cost about one figure in total.
- Tamil: best that an image can do (real font, measured widths), but still baked text, non-selectable.
- Rule impact: for redrawn figures both languages are regenerated (this is the exception you already allowed). No change for others.

### (b) Numbered callouts on a language-neutral image, legend as native Compose Text
- In-image text is numerals and Latin symbols only; legend lines are Compose `Text` (correct Tamil shaping, selectable, follows system font scale).
- Pass at 9/10/11 sp: all (numerals, badge size is a free choice; legend is body text >= 14 sp).
- Cost: legends get long (median 15 labels per figure; 29 figures have more than 20). Reading becomes figure -> legend -> figure; the image alone is no longer self-explanatory. `MarkerFigure` + bilingual legend already exist in the app (used for the 5 hand-art figures), so no new UI is needed.
- Effort per figure: redraw to a neutral geometry plus a legend table (similar to (a)).
- Tamil: best (no baked Tamil, no shrink).
- **Rule impact (plain statement):** applying (b) to an existing figure replaces its Tamil-labelled PNG with a neutral image plus a Tamil legend. For figures redrawn under your exception that is covered; applied to the other ~125 it would remove existing Tamil-labelled images from Tamil mode, which breaks "existing Tamil-labelled images stay in Tamil mode". I will not do that without your decision.

### (c) Tap-to-open full-screen landscape viewer with pinch-zoom
- Cost: one-time code (rotate to sensor-landscape while the viewer is open, no bitmap downsampling, zoom up to 6x, double-tap, overlay close button). The manifest already declares `configChanges` for orientation, so the reader does not restart.
- Scale factor over inline: median 2.53x (min 0.9x for tall figures, 104 of 140 get at least 2x; 8 figures would get less than 1x, i.e. are better off portrait-fit).

| (c) alone | pass at 9 sp | pass at 10 sp | pass at 11 sp |
|---|---|---|---|
| English | 101 | 76 | 52 |
| Tamil | 6 | 5 | 3 |
| portrait-fit viewer as today (1.2x) | EN 10 / TA 0 | EN 9 / TA 0 | EN 7 / TA 0 |

- Tamil: only helps with pinch (median further 1.7x needed after the landscape fit). It does not fix Tamil on its own.
- Effort per figure: none. Rule impact: none (images unchanged).

### (d) Combination: (c) for every figure + (a) for redrawn figures + (b) only where the Tamil text volume does not fit
- 15 figures redrawn now (the re-ranked ten and the five strips): pass by construction.
- Counts over all 140 (redrawn 15 counted as passing, the other 125 through the viewer only):

| (d) | pass at 9 sp | pass at 10 sp | pass at 11 sp |
|---|---|---|---|
| English | 114 | 90 | 66 |
| Tamil | 21 | 20 | 18 |

- For the 125 untouched figures the viewer alone leaves Tamil far short (3 of 125 at 11 sp; extra pinch zoom median 1.7x, p90 2.2x). I am not hiding this: Tamil legibility of the untouched figures is not solved by (c); it needs either a triage-based redraw or a change to the shrink policy (Tamil images re-rendered with wrapping instead of shrinking), which is per-figure layout work.

## 3. Recommendation: (d)

1. (c) now, for all figures: it is the only option with no per-figure cost and no rule impact, and it takes English from 0 to 66 of 140 at 11 sp (with pinch, nearly all).
2. (a) as the convention for every redrawn or new figure: portrait-first canvas, minimum font 13.2 units on a 360-unit canvas, Tamil as the layout worst case, build gate on measured size and collisions (the pilot implements this).
3. (b) only for the 14 figures whose Tamil text does not fit at 11 sp, decided one by one with you.
4. Triage of the other ~125 (after this spec): rank by severity x lesson importance using the audit, keep (c) as the baseline, and decide separately whether to relax the Tamil shrink floor.

Pilot convention = (a) + (c).

## 4. Pilot: screening-d02 (insertional inactivation)

One-line claim for your check: cloning into the BamHI site inside the tetracycline-resistance gene of pBR322 disrupts that gene, so recombinant colonies stay ampicillin-resistant (AmpR) but become tetracycline-sensitive (TetS), and show up as colonies present on the amp master plate but absent on the tet replica; non-recombinants grow on both.

Geometry: `tools/figures/src/screening_selection_recombinants-d02/`, text in `strings.json` (en, ta); both images come from `tools/figures/build_figure.py`.
Colour is not the only cue: absent colonies are dashed hollow circles with a cross and the words "no growth"; plates are labelled.
