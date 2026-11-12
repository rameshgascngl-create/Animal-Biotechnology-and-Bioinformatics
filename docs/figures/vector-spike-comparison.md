# Native vector vs tiled raster: one-page comparison (item 5; spike only, branch `spike/native-vector-pilot`, not merged)

What was built (time-boxed): `VectorFigure.kt` (166 lines) parses the pilot's own `figure.svg` (the geometry the PNG pipeline uses) plus `strings.json` at runtime and draws it on a Compose `Canvas`: shapes as paths, labels as system text (greedy wrap by measured width), one TalkBack node carrying the alt text. Supports only what the pilot uses (rect, circle, ellipse, line + arrow, M/A/L/Z paths, text). Captures: `vector-spike-captures.jpg` (Tamil, left to right: 300 dp, font scale 1.5, 3x zoom), CI run on the spike branch, 360x640 emulator only (the later sizes did not run because an unrelated test failed first, see below).

| | Tiled/plain raster (what ships) | Native vector (spike) |
|---|---|---|
| Size per figure in the APK | 427 KB EN + 529 KB TA PNG = 956 KB (all 285 PNGs: 10.5 MB) | 6.1 KB SVG + 3.4 KB strings (both languages) = 9.5 KB, about 1% |
| Memory | 18.6 MB decoded per language shown at full size; inline now decoded at display size; viewer full size | no bitmap. PSS at 300 dp inline on the 2 GB AVD: EN 126 MB, TA 118 MB (other fixture, so not comparable to the 112-121 MB raster numbers beyond "same order"). **GPU memory unmeasured** (software GL on the emulator) |
| Zoom crispness | limited to crisp factor (about 1.3-1.5x on a phone); softer beyond | **crisp at 3x** in the capture (the layer is re-rasterised); no crisp cap |
| Tamil text | baked glyphs from Noto Sans Tamil in the PNG, not selectable | **system font**, real shaping, selectable text possible; looked correct at 300 dp and 3x; glyph shapes differ slightly from the PNG's Noto |
| User font scale (sp) | none: text size is fixed in the picture | labels do scale, **but geometry does not reflow**: at font scale 1.5 labels overlap each other and the drawing (visible in the capture). Needs either a cap on figure text scale, per-label growth room, or a layout engine |
| TalkBack | one node with alt text | same one node (spike). **Not exercised on any device or TalkBack.** Per-label nodes would need work |
| Effort per figure | redraw SVG + strings, gated build | the same SVG + strings (already the source of truth), plus renderer coverage for every SVG feature the figure uses; the 166-line renderer covers only this figure. Feature gaps for the other figures (text on a curve, rotated text, gradients, markers elsewhere, clip paths) are unestimated |
| Existing Tamil PNGs | unchanged | **untouched**: the vector path would apply only to redrawn figures; the other ~125 keep their PNGs and the same viewer. The spike changes no existing asset |

## Honest limits of the spike
- One figure, one emulator size, software rendering. No device, no TalkBack, no real GPU, no battery/frame-time measurement of a large `Canvas`.
- The 1.5x font-scale result is a genuine failure of the naive approach, not a rendering bug.
- The spike's text size is derived from the figure width in dp, so labels are about 11 sp at 300 dp, the same as the PNG; they are not larger.
- I did not wire it into the reader or the viewer (no tap-to-enlarge on the vector path).

## Reading
Vector removes the crispness and APK-size problems for redrawn figures and keeps one source for both languages, but it moves the problem to font scaling and to renderer coverage. It does nothing for the ~125 untouched figures. Decision is yours.
