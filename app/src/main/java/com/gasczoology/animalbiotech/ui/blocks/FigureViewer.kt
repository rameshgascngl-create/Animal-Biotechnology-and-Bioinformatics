package com.gasczoology.animalbiotech.ui.blocks

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.gasczoology.animalbiotech.i18n.Lang
import com.gasczoology.animalbiotech.i18n.tr

/** What the viewer shows. Lives above the lesson list so the viewer survives its figure leaving the list (rotation re-lays the list out). */
internal class ViewerRequest(val image: ImageBitmap, val description: String, val lang: Lang)
internal class FigureViewerHost { var request by mutableStateOf<ViewerRequest?>(null) }
internal val LocalFigureViewerHost = compositionLocalOf<FigureViewerHost?> { null }

/** Provide once per screen that lists figures; figures open their viewer through it. */
@Composable
fun ProvideFigureViewer(content: @Composable () -> Unit) {
    val host = remember { FigureViewerHost() }
    CompositionLocalProvider(LocalFigureViewerHost provides host) {
        content()
        host.request?.let { r -> FigureViewer(r.image, r.description, r.lang) { host.request = null } }
    }
}

private const val MAX_ZOOM = 4f
private val MAX_CONTENT_WIDTH = 640.dp

/**
 * Full-screen figure viewer (spec: docs/figures/viewer-spec.md).
 * The figure is laid out at the viewer width (capped at 640 dp) and moved with a transform that is clamped so every edge can be reached.
 * Pinch zooms about the fingers, one finger pans, double tap toggles 2x, buttons zoom in 1.5x steps.
 * Zoom and position are saved as scale and fractions of the pannable range, so rotation and recreation keep them.
 */
@Composable
internal fun FigureViewer(image: ImageBitmap, description: String, lang: Lang, onClose: () -> Unit) {
    val ctx = LocalContext.current
    DisposableEffect(Unit) {
        var c: android.content.Context = ctx
        while (c is android.content.ContextWrapper && c !is android.app.Activity) c = c.baseContext
        val act = c as? android.app.Activity
        val before = act?.requestedOrientation
        act?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose { if (act != null && before != null) act.requestedOrientation = before }
    }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        var scale by rememberSaveable { mutableStateOf(1f) }
        var fx by rememberSaveable { mutableStateOf(0.5f) }
        var fy by rememberSaveable { mutableStateOf(0f) }
        val density = LocalDensity.current
        val zoomIn = tr(lang, "Zoom in", "பெரிதாக்கு")
        val zoomOut = tr(lang, "Zoom out", "சிறிதாக்கு")
        val reset = tr(lang, "Reset", "மீட்டமை")
        val close = tr(lang, "Close", "மூடு")

        Column(Modifier.fillMaxSize().background(Color.White).semantics { isTraversalGroup = true }) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End) {
                ViewerButton("+", zoomIn, 1f) { scale = (scale * 1.5f).coerceAtMost(MAX_ZOOM) }
                ViewerButton("−", zoomOut, 2f) { scale = (scale / 1.5f).coerceAtLeast(1f) }
                ViewerButton(reset, reset, 3f) { scale = 1f; fx = 0.5f; fy = 0f }
                ViewerButton(close, close, 4f, onClose)
            }
            BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
                val viewW = constraints.maxWidth.toFloat()
                val viewH = constraints.maxHeight.toFloat()
                val contentW = minOf(viewW, with(density) { MAX_CONTENT_WIDTH.toPx() })
                val contentH = contentW * image.height / image.width

                fun offsetOf(f: Float, view: Float, content: Float, s: Float): Float {
                    val size = content * s
                    return if (size <= view) (view - size) / 2f else -f * (size - view)
                }
                fun fractionOf(o: Float, view: Float, content: Float, s: Float): Float {
                    val range = content * s - view
                    return if (range <= 0f) 0.5f else (-o / range).coerceIn(0f, 1f)
                }
                /** Zooms to [newScale] keeping the content point under [focus] fixed, then pans by [pan]; the result is clamped by [fractionOf]. */
                fun transform(focus: Offset, pan: Offset, newScale: Float) {
                    val s = newScale.coerceIn(1f, MAX_ZOOM)
                    val ox = offsetOf(fx, viewW, contentW, scale)
                    val oy = offsetOf(fy, viewH, contentH, scale)
                    val k = s / scale
                    val nx = focus.x - (focus.x - ox) * k + pan.x
                    val ny = focus.y - (focus.y - oy) * k + pan.y
                    scale = s
                    fx = fractionOf(nx, viewW, contentW, s)
                    fy = fractionOf(ny, viewH, contentH, s)
                }

                val ox = offsetOf(fx, viewW, contentW, scale)
                val oy = offsetOf(fy, viewH, contentH, scale)
                val actions = listOf(
                    CustomAccessibilityAction(zoomIn) { transform(Offset(viewW / 2f, viewH / 2f), Offset.Zero, scale * 1.5f); true },
                    CustomAccessibilityAction(zoomOut) { transform(Offset(viewW / 2f, viewH / 2f), Offset.Zero, scale / 1.5f); true },
                )
                Box(
                    Modifier.fillMaxSize()
                        .pointerInput(viewW, viewH) {
                            detectTransformGestures { centroid, pan, zoom, _ -> transform(centroid, pan, scale * zoom) }
                        }
                        .pointerInput(viewW, viewH) {
                            detectTapGestures(onDoubleTap = { p -> transform(p, Offset.Zero, if (scale > 1f) 1f else 2f) })
                        },
                ) {
                    Image(
                        bitmap = image, contentDescription = null, contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .wrapContentSize(Alignment.TopStart, unbounded = true)
                            .size(with(density) { contentW.toDp() }, with(density) { contentH.toDp() })
                            .graphicsLayer {
                                transformOrigin = TransformOrigin(0f, 0f)
                                scaleX = scale; scaleY = scale; translationX = ox; translationY = oy
                            }
                            .semantics { contentDescription = description; traversalIndex = 0f; customActions = actions },
                    )
                }
            }
        }
    }
}

@Composable
private fun ViewerButton(label: String, description: String, order: Float, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .semantics { contentDescription = description; traversalIndex = order },
    ) { Text(label) }
}
