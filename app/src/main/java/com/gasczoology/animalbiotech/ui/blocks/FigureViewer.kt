package com.gasczoology.animalbiotech.ui.blocks

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** What the viewer shows: an asset path (decoded by the viewer itself), the spoken description and the language. All strings, so it can be saved. */
internal data class ViewerRequest(val path: String, val description: String, val lang: String)

internal class FigureViewerHost(val state: MutableState<ViewerRequest?>) {
    var request: ViewerRequest?
        get() = state.value
        set(v) { state.value = v }
}

internal val LocalFigureViewerHost = compositionLocalOf<FigureViewerHost?> { null }

private val RequestSaver = listSaver<MutableState<ViewerRequest?>, String>(
    save = { s -> s.value?.let { listOf(it.path, it.description, it.lang) } ?: emptyList() },
    restore = { l -> mutableStateOf(if (l.size == 3) ViewerRequest(l[0], l[1], l[2]) else null) },
)

/**
 * Provide once per screen that lists figures. The viewer is composed here, above the lazy list, so it does not depend on the figure's
 * list item staying composed (rotation re-lays the list out and can drop the item). The request is saved, so recreation keeps it open.
 */
@Composable
fun ProvideFigureViewer(content: @Composable () -> Unit) {
    val state = rememberSaveable(saver = RequestSaver) { mutableStateOf<ViewerRequest?>(null) }
    val host = remember(state) { FigureViewerHost(state) }
    CompositionLocalProvider(LocalFigureViewerHost provides host) {
        content()
        host.request?.let { r -> FigureViewer(r) { host.request = null } }
    }
}

/** Largest zoom at which one bitmap pixel is still at least one screen pixel (fit-to-width is [viewW] screen pixels wide); never below 1x. */
internal fun crispMaxZoom(bitmapW: Int, viewW: Float): Float = maxOf(1f, bitmapW / viewW)

private val ZoomIn = "Zoom in" to "பெரிதாக்கு"
private val ZoomOut = "Zoom out" to "சிறிதாக்கு"

/**
 * Full-screen figure viewer (spec: docs/figures/viewer-spec.md). Follows the user's orientation; never rotates the screen.
 * Initial view and Reset are fit-to-width. Pinch zooms about the fingers, one finger pans, double tap toggles, buttons zoom in 1.5x steps.
 * Zoom is capped at the crisp factor: native bitmap pixels / on-screen pixels at fit-to-width (never below 1x).
 * The full-resolution bitmap is decoded here, off the main thread, and dropped when the viewer leaves composition.
 */
@Composable
internal fun FigureViewer(request: ViewerRequest, onClose: () -> Unit) {
    val ctx = LocalContext.current
    val lang = if (request.lang == "TA") Lang.TA else Lang.EN
    val bitmap: Bitmap? by produceState<Bitmap?>(null, request.path) {
        value = withContext(Dispatchers.IO) { decodeFigure(ctx, request.path, targetWidthPx = null) }
    }
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        var scale by rememberSaveable { mutableStateOf(1f) }
        var fx by rememberSaveable { mutableStateOf(0.5f) }
        var fy by rememberSaveable { mutableStateOf(0f) }
        val density = LocalDensity.current
        val zoomIn = tr(lang, ZoomIn.first, ZoomIn.second)
        val zoomOut = tr(lang, ZoomOut.first, ZoomOut.second)
        val reset = tr(lang, "Reset", "மீட்டமை")
        val close = tr(lang, "Close", "மூடு")

        BoxWithConstraints(Modifier.fillMaxSize().background(Color.White)) {
            val viewW = constraints.maxWidth.toFloat()
            val bmp = bitmap
            // Fit-to-width on-screen width is viewW; crisp when one bitmap pixel is one screen pixel.
            val maxZoom = if (bmp == null) 1f else crispMaxZoom(bmp.width, viewW)
            fun clampScale(s: Float) = s.coerceIn(1f, maxZoom)

            Column(Modifier.fillMaxSize().semantics { isTraversalGroup = true }) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.End) {
                    ViewerButton("+", zoomIn, 1f) { scale = clampScale(scale * 1.5f) }
                    ViewerButton("−", zoomOut, 2f) { scale = clampScale(scale / 1.5f) }
                    ViewerButton(reset, reset, 3f) { scale = 1f; fx = 0.5f; fy = 0f }
                    ViewerButton(close, close, 4f, onClose)
                }
                BoxWithConstraints(Modifier.weight(1f).fillMaxWidth().clipToBounds()) {
                    val viewH = constraints.maxHeight.toFloat()
                    if (bmp == null) return@BoxWithConstraints
                    val contentW = viewW
                    val contentH = contentW * bmp.height / bmp.width
                    val s0 = clampScale(scale)

                    fun offsetOf(f: Float, view: Float, content: Float, s: Float): Float {
                        val size = content * s
                        return if (size <= view) (view - size) / 2f else -f * (size - view)
                    }
                    fun fractionOf(o: Float, view: Float, content: Float, s: Float): Float {
                        val range = content * s - view
                        return if (range <= 0f) 0.5f else (-o / range).coerceIn(0f, 1f)
                    }
                    /** Zooms to [newScale] keeping the content point under [focus] fixed, then pans by [pan]; the result is clamped. */
                    fun transform(focus: Offset, pan: Offset, newScale: Float) {
                        val s = clampScale(newScale)
                        val ox = offsetOf(fx, viewW, contentW, s0)
                        val oy = offsetOf(fy, viewH, contentH, s0)
                        val k = s / s0
                        val nx = focus.x - (focus.x - ox) * k + pan.x
                        val ny = focus.y - (focus.y - oy) * k + pan.y
                        scale = s
                        fx = fractionOf(nx, viewW, contentW, s)
                        fy = fractionOf(ny, viewH, contentH, s)
                    }

                    val ox = offsetOf(fx, viewW, contentW, s0)
                    val oy = offsetOf(fy, viewH, contentH, s0)
                    val actions = listOf(
                        CustomAccessibilityAction(zoomIn) { transform(Offset(viewW / 2f, viewH / 2f), Offset.Zero, s0 * 1.5f); true },
                        CustomAccessibilityAction(zoomOut) { transform(Offset(viewW / 2f, viewH / 2f), Offset.Zero, s0 / 1.5f); true },
                    )
                    Box(
                        Modifier.fillMaxSize()
                            .pointerInput(viewW, viewH, maxZoom) {
                                detectTransformGestures { centroid, pan, zoom, _ -> transform(centroid, pan, s0 * zoom) }
                            }
                            .pointerInput(viewW, viewH, maxZoom) {
                                detectTapGestures(onDoubleTap = { p -> transform(p, Offset.Zero, if (s0 > 1f) 1f else minOf(2f, maxZoom)) })
                            },
                    ) {
                        Image(
                            bitmap = bmp.asImageBitmap(), contentDescription = null, contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .wrapContentSize(Alignment.TopStart, unbounded = true)
                                .size(with(density) { contentW.toDp() }, with(density) { contentH.toDp() })
                                .graphicsLayer {
                                    transformOrigin = TransformOrigin(0f, 0f)
                                    scaleX = s0; scaleY = s0; translationX = ox; translationY = oy
                                }
                                .semantics { contentDescription = request.description; traversalIndex = 0f; customActions = actions },
                        )
                    }
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
