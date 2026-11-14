# Phase 2 triage proposal (128 figures remaining)

Source: `figure-audit-phase0.csv` (visual grade per language; geometry audit of the shipped renders at the 300 dp inline width). 17 of 145 are already redrawn. Per-figure list: `triage-phase2.csv`.
"Worst visual" = the worse of EN and TA. "Fails threshold" = shipped text below 10 sp (EN) or 11 sp (TA) inline.

| Worst visual | Figures | Fail the legibility threshold | With structural defects |
|---|---|---|---|
| severe | 0 | 0 | 0 |
| major | 17 | 17 | 16 |
| moderate | 20 | 20 | 17 |
| minor | 45 | 44 | 19 |
| ok | 46 | 42 | 2 |
| total | 128 | 123 | 54 |

Median shipped minimum text: 5.9 sp EN, 3.7 sp TA (thresholds 10 / 11).
The audit is Phase 0 data and has not been re-run on the 17 redrawn figures.

## Status at 0.9.0-rc2 (2026-11-14)

| Worst visual | fixed (shipped in rc2) | drawn, ships in rc3 | needs manual | automated pass pending | left as shipped |
|---|---|---|---|---|---|
| severe | 0 | 0 | 0 | 0 | 0 |
| major | 17 | 0 | 0 | 0 | 0 |
| moderate | 1 | 19 | 0 | 0 | 0 |
| minor | 0 | 0 | 19 | 26 | 0 |
| ok | 0 | 0 | 2 | 44 | 0 |

Major figures remaining: 0. Per-figure status is in the `status` column of `triage-phase2.csv`. The automated-pass counts are not final: 10 strip figures are converted but held for the Tamil review.
