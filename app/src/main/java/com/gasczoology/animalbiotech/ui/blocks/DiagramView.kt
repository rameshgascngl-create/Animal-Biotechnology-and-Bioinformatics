package com.gasczoology.animalbiotech.ui.blocks

import android.graphics.BitmapFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.gasczoology.animalbiotech.content.Diagram
import com.gasczoology.animalbiotech.content.DiagramSpec
import com.gasczoology.animalbiotech.content.parseDiagramSpec
import com.gasczoology.animalbiotech.i18n.Lang
import com.gasczoology.animalbiotech.i18n.tr

/** Which files belong to a figure, plus its marker spec and alt text. No pixels are decoded here. */
private class FigureFiles(val enPath: String?, val taPath: String?, val spec: DiagramSpec?, val altEn: String?, val altTa: String?)

private const val MAX_BITMAP_SIDE = 4096

/** Power-of-two sub-sampling: as far as the result stays at least [targetWidthPx] wide, and always enough to keep both sides within [MAX_BITMAP_SIDE]. */
internal fun sampleSizeFor(outW: Int, outH: Int, targetWidthPx: Int?): Int {
    var sample = 1
    if (targetWidthPx != null && targetWidthPx > 0) while (outW / (sample * 2) >= targetWidthPx) sample *= 2
    while (maxOf(outW, outH) / sample > MAX_BITMAP_SIDE) sample *= 2
    return sample
}

/**
 * Decodes an asset. With [targetWidthPx] the image is sub-sampled (powers of two) as far as it can be while staying at least that wide,
 * so inline figures are decoded at display size, not full size. Whatever the target, no side is allowed above [MAX_BITMAP_SIDE]
 * (texture limit on some GPUs); shipped figures are at most 3232 px, and the unit test AssetSizeTest enforces the 4096 limit.
 */
internal fun decodeFigure(ctx: android.content.Context, path: String, targetWidthPx: Int?): android.graphics.Bitmap? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    ctx.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
    val sample = sampleSizeFor(bounds.outWidth, bounds.outHeight, targetWidthPx)
    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    ctx.assets.open(path).use { BitmapFactory.decodeStream(it, null, opts) }
} catch (e: Exception) { null }

private fun imageSize(ctx: android.content.Context, path: String): Pair<Int, Int>? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    ctx.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
    if (bounds.outWidth > 0 && bounds.outHeight > 0) bounds.outWidth to bounds.outHeight else null
} catch (e: Exception) { null }

/**
 * Finds a figure's files. A real photograph supplied as <id>_photo.png|jpg replaces the illustration in both languages
 * (and may carry <id>_photo.json markers); otherwise English art is <id>.png and the Tamil-labelled render is <id>_ta.png.
 */
private fun loadFiles(ctx: android.content.Context, id: String): FigureFiles? = try {
    val names = ctx.assets.list("diagrams")?.toSet() ?: emptySet()
    fun pick(suffix: String) = listOf("${id}_photo.png", "${id}_photo.jpg", "$id$suffix.png").firstOrNull { it in names }?.let { "diagrams/$it" }
    val specName = if ("${id}_photo.json" in names) "${id}_photo.json" else "$id.json"
    val spec = if (specName in names)
        parseDiagramSpec(ctx.assets.open("diagrams/$specName").bufferedReader(Charsets.UTF_8).use { it.readText() }) else null
    val alt = if ("$id.alt.json" in names) runCatching {
        org.json.JSONObject(ctx.assets.open("diagrams/$id.alt.json").bufferedReader(Charsets.UTF_8).use { it.readText() })
    }.getOrNull() else null
    val en = pick("")
    if (en == null) null else FigureFiles(en, pick("_ta"), spec?.takeIf { it.markers.isNotEmpty() },
        alt?.optString("en")?.takeIf { it.isNotBlank() }, alt?.optString("ta")?.takeIf { it.isNotBlank() })
} catch (e: Exception) { null }

/**
 * Inline figure. Marker-style art (numbered badges + bilingual legend) follows the language chips through the legend;
 * browser-rendered figures carry their labels in the image, so English or Tamil picks the matching PNG.
 */
@Composable
fun DiagramView(block: Diagram, lang: Lang, scale: Float) {
    val ctx = LocalContext.current
    val files = remember(block.id) { loadFiles(ctx, block.id) }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFB8C4D4)),
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when {
                files == null -> Text("Figure: ${block.id}", fontSize = (11 * scale).sp, color = Muted)
                files.spec != null -> MarkerFigure(files, block, lang)
                else -> {
                    // Only the active language's bitmap is ever decoded.
                    val showTa = lang == Lang.TA && files.taPath != null
                    if (showTa) FlatImage(block.id, files.taPath!!, files.altTa ?: block.caption.ta.ifBlank { block.caption.en }, lang)
                    else FlatImage(block.id, files.enPath!!, files.altEn ?: block.caption.en, lang)
                }
            }
            BiText(block.caption, lang, scale, base = MaterialTheme.typography.bodyMedium, weight = FontWeight.SemiBold)
            val legend = files?.spec?.markers
            if (legend != null) {
                legend.forEach { m ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            Modifier.size(20.dp).clip(CircleShape).background(Color(0xFF0B2A4A)),
                            contentAlignment = Alignment.Center,
                        ) { Text("${m.n}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        BiText(m.label, lang, scale, Modifier.weight(1f), base = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun FlatImage(figureId: String, path: String, description: String, lang: Lang) {
    val ctx = LocalContext.current
    val host = LocalFigureViewerHost.current
    var local by remember { mutableStateOf<ViewerRequest?>(null) }
    if (host == null) local?.let { FigureViewer(it) { local = null } }
    // Alt text is already a full sentence for rebuilt figures; older figures only have the caption.
    val label = if (description.startsWith("Diagram") || description.startsWith("படம்")) description
        else tr(lang, "Diagram: ", "படம்: ") + description
    val size = remember(path) { imageSize(ctx, path) }
    val aspect = size?.let { it.first.toFloat() / it.second } ?: 1.5f
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val widthPx = constraints.maxWidth
        // Decoded at (about) display width; the bitmap is dropped when this item leaves composition.
        val bmp by produceState<android.graphics.Bitmap?>(null, path, widthPx) {
            value = withContext(Dispatchers.IO) { decodeFigure(ctx, path, widthPx) }
            value?.let { android.util.Log.i("CITEST", "figure-bitmap id=$figureId path=$path target=$widthPx decoded=${it.width}x${it.height} config=${it.config} bytes=${it.allocationByteCount}") }
        }
        Box(
            Modifier.fillMaxWidth().aspectRatio(aspect).clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                .clickable(onClickLabel = tr(lang, "Enlarge diagram", "படத்தைப் பெரிதாக்கு")) {
                    val r = ViewerRequest(path, description, if (lang == Lang.TA) "TA" else "EN")
                    if (host != null) host.request = r else local = r
                }
                .semantics { contentDescription = label }
                .testTag("figure-$figureId"),
        ) {
            bmp?.let { Image(bitmap = it.asImageBitmap(), contentDescription = null, contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize()) }
        }
    }
}

@Composable
private fun MarkerFigure(f: FigureFiles, block: Diagram, lang: Lang) {
    val spec = f.spec ?: return
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val w = maxWidth
        val h = w / spec.aspect
        FlatImage(f.enPath!!, block.caption.en, lang)
        spec.markers.forEach { m ->
            Box(
                Modifier.offset(w * m.x - 11.dp, h * m.y - 11.dp).size(22.dp).clip(CircleShape).background(Color(0xFF0B2A4A)),
                contentAlignment = Alignment.Center,
            ) { Text("${m.n}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }
    }
}
