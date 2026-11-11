# Figure viewer spec (for review before merge)

Status: proposal. Implemented in the commit after this one so it can be reverted alone (`FigureViewer.kt` plus the call site in `DiagramView.kt`).
Related: `docs/figures/legibility-spec.md` (option c).

## Behaviour
| # | Requirement | How |
|---|---|---|
| 1 | Open | Tap an inline figure (existing `clickable`, label "Enlarge diagram" / "படத்தைப் பெரிதாக்கு"). Full-screen `Dialog`; the activity is turned to sensor-landscape while open and restored on close. |
| 2 | Pinch-zoom | Yes, real pinch: `detectTransformGestures`, zoom about the fingers, 1x to 4x. Not replaced by buttons; buttons and double tap are additions. |
| 3 | Pan to every edge | One-finger drag. The figure is laid out at viewer width (max 640 dp) and translated with a clamp, so at any zoom (including 1x for tall figures) all four edges can be reached. If the scaled figure is smaller than the viewport on an axis it is centred on that axis. |
| 4 | Double tap | Toggles 1x / 2x about the tap point. |
| 5 | Touch targets | Zoom in, Zoom out, Reset, Close: each at least 48 x 48 dp. |
| 6 | Back | System Back / back gesture closes the viewer (Dialog default). Tested. |
| 7 | Rotation | Zoom and position are kept as scale plus fractions of the pannable range (`rememberSaveable`), so they survive rotation and activity recreation; the open/closed state of the viewer is also saved. |
| 8 | Language | Visible labels and `contentDescription`s come from `tr(lang, en, ta)`. The figure's description is the figure's alt text in the shown language (`<id>.alt.json`, else the caption). |
| 9 | TalkBack order | Group traversal: figure (alt text) first, then Zoom in, Zoom out, Reset, Close. The figure also exposes "Zoom in" and "Zoom out" as custom actions so a TalkBack user needs no pinch. |

## Deviation and limits (stated, not hidden)
- No deviation from pinch-zoom.
- Maximum zoom is 4x, not 6x. Figures are raster (PNG, 1440 px wide for new figures, 1600-1800 px for the old ones). At 4x on a 420 dpi phone a 1440 px bitmap is shown at about 2.8x its pixels, so text is soft, not crisp. This is a limit of baking text into PNGs; see the image-resource note in the milestone report. The only real fixes are vector figures or a higher-resolution tile source, which I have not built.
- The viewer does not change the stored images and does not remove the existing downsampling for very large bitmaps (`decode` halves only above 2400 px width; it does not tile).

## Acceptance tests (instrumented, all fatal in CI)
1. Open: tap inline figure, a node with the figure description exists inside the viewer and the "Close" button exists; the enlarged capture is not byte-identical to the inline capture.
2. Back closes: after `pressBack` the "Close" node is gone.
3. Close button closes.
4. Pinch: a two-finger gesture (UiAutomator `pinchOpen`) changes the figure's on-screen bounds or the screenshot differs from the unzoomed one.
5. Same checks in Tamil with the Tamil labels.
