# Figure viewer spec (for review before merge)

Status: proposal. Implemented in the commit after this one so it can be reverted alone (`FigureViewer.kt` plus the call site in `DiagramView.kt`).
Related: `docs/figures/legibility-spec.md` (option c).

## Behaviour
| # | Requirement | How |
|---|---|---|
| 1 | Open | Tap an inline figure (existing `clickable`, label "Enlarge diagram" / "படத்தைப் பெரிதாக்கு"). Full-screen `Dialog`. The orientation is NOT forced: the viewer follows the user's own orientation (a forced landscape showed only a third of a portrait figure). |
| 2 | Pinch-zoom | Yes, real pinch: `detectTransformGestures`, zoom about the fingers, from fit-to-width (1x) up to the crisp factor (below). Not replaced by buttons; buttons and double tap are additions. |
| 3 | Pan to every edge | One-finger drag. The initial view is fit-to-width; Reset returns to it. The figure is laid out at the dialog width and translated with a clamp, so at any zoom (including 1x for tall figures) all four edges can be reached. If the scaled figure is smaller than the viewport on an axis it is centred on that axis. |
| 4 | Double tap | Toggles 1x / 2x about the tap point. |
| 5 | Touch targets | Zoom in, Zoom out, Reset, Close: each at least 48 x 48 dp. |
| 6 | Back | System Back / back gesture closes the viewer (Dialog default). Tested. |
| 7 | Rotation | The viewer is hosted above the lesson list (`ProvideFigureViewer` in `LessonScreen`), not inside the figure's list item: turning to landscape re-lays the list out and can drop the figure's item, which would take an item-owned viewer with it (this is the bug the first version had, see the milestone report). The open request is a saveable value (`ViewerRequest`: path, description, language) and zoom and position are scale plus fractions of the pannable range (`rememberSaveable`), so the viewer survives rotation and activity recreation. Not covered: process death while the viewer is open. |
| 8 | Language | Visible labels and `contentDescription`s come from `tr(lang, en, ta)`. The figure's description is the figure's alt text in the shown language (`<id>.alt.json`, else the caption). |
| 9 | TalkBack order | Group traversal: figure (alt text) first, then Zoom in, Zoom out, Reset, Close. The figure also exposes "Zoom in" and "Zoom out" as custom actions so a TalkBack user needs no pinch. |

## Bitmap handling
- Only the active language is decoded (the other is never opened). Inline images are decoded off the main thread at display width (power-of-two sub-sampling, never below the display width); the viewer decodes the full image on demand. Both are released when they leave composition.
- Maximum zoom is the crisp factor `max(1, bitmap px / viewer px)`, not a nominal 4x: beyond it a bitmap pixel is larger than a screen pixel and the figure only gets softer. On a 411 dp phone in portrait this is about 1.33x for a 1440 px figure, so for dense figures the viewer cannot make text sharper than the bitmap; that is the raster limit, and the reason for the vector spike.
- Texture limit: minSdk 26 devices guarantee a 4096 px texture at least. The decoder sub-samples any bitmap with a side above 4096 and a JVM test (`FigureBitmapTest`) fails if a shipped PNG has a side above 4096. No tiling was built; nothing currently needs it. The real GPU limit on a device is untested.

## Acceptance tests (instrumented, all fatal in CI)
1. Open and stay open: tap inline figure, the viewer is still open 2 s later, a node with the figure description exists inside the viewer and the "Close" button exists; the enlarged capture is not byte-identical to the inline capture.
2. Back closes: after `pressBack` the "Close" node is gone.
3. Close button closes.
4. Pinch: a two-finger gesture (UiAutomator `pinchOpen`) changes the figure's on-screen bounds or the screenshot differs from the unzoomed one.
5. Same checks in Tamil with the Tamil labels.
6. Regression (`viewerSurvivesRotationAndRecreationOnDeepFigure`): a figure deep in a long lesson, reached by scrolling; open the viewer, rotate left, right and back, recreate the activity, the viewer is still open each time; Back closes it.
