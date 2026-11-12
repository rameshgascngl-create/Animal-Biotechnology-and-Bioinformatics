# Tamil legibility threshold, recomputed for the shipped viewer (item 4)

Data: `docs/audit/figure-audit-phase0.csv` (`TA_min_sp` = smallest rendered Tamil-state text at 360 dp width, 140 figures) and the pixel width of each shipped `_ta.png`. Per-figure table: `docs/audit/tamil-threshold-by-figure.csv`.
Model of the viewer as now built: fit-to-width, no forced rotation, zoom capped at the crisp factor z = bitmap px / screen px (1080 px screens), never below 1. Text size in the viewer = `TA_min_sp` x (phone dp width / 360) x z. Inline (300 dp) = `TA_min_sp` x 300/360.
Caveat: `TA_min_sp` is the smallest text anywhere in the figure, a strict criterion. The CSV holds no distribution, so I cannot say how many figures would pass if footnote-sized text were excluded.

## Tamil pass counts (of 140), (d) = viewer for every figure + 15 planned redraws counted as passing
| threshold | inline 300 dp | viewer, fit-to-width | viewer at crisp cap, all figures | (d), 360 dp phone | (d), 411 dp phone |
|---|---|---|---|---|---|
| 10 sp | 0 | 0 | 1 | 16 | 16 |
| 11 sp | 0 | 0 | 0 (360) / 1 (411) | 15 | 16 |
| 12 sp | 0 | 0 | 0 | 15 | 15 |
The earlier "Tamil 18 of 140 at 11 sp under (d)" assumed unlimited pinch zoom in a forced-landscape viewer. With zoom honestly capped at the crisp factor (median z = 1.49; median smallest Tamil text 3.5 sp), only one untouched figure ever reaches 10 sp (atlas1_fundamentals_vectors-d05, 10.0 sp at 360 dp). The 15 redraws are what carries (d), and 4 of those 15 are on the earlier text-volume failure list (screening d01, d04, sanger d01, atlas3 d02), so they do not pass at 11 sp without callouts: realistic (d) is about 11 of 140 plus at most 1.

## Figures that cannot pass under (d) at any of 10, 11, 12 sp
- 124 untouched figures cannot pass at 10 sp; 124-125 at 11 and 12 (all 125 non-redrawn figures except atlas1-d05 at 10 sp and, on a 411 dp phone, one at 11 sp). They are listed with the exact sp reached in the CSV (`pass10/11/12` = no).
- The 4 planned redraws above fail on text volume, not on size, unless they get callouts or a taller canvas.
- Nothing about the 5 hand-art figures (no text) is counted.

## Rule I would use
1. A Tamil figure passes if its smallest label is at least **11 sp at the largest crisp zoom** (not at an uncapped pinch), and it has zero structural defects. 11 sp is the same bar as English; Tamil conjuncts are wider and have no smaller-size excuse.
2. Footnote text (units, tick values) may be 10 sp if it is under 5% of characters; I cannot count this yet, so it is a proposal needing a per-figure measure.
3. A figure that cannot pass by this rule is not "fixed" by pinch: it must be redrawn at a larger native size, move its Tamil text into callouts or a legend (option b), or be drawn as native vector (the spike, item 5). Which of these, per figure, is your decision after the triage proposal.
