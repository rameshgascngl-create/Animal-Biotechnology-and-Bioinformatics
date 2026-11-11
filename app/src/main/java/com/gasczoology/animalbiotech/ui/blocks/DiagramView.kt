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
import com.gasczoology.animalbiotech.content.Diagram
import com.gasczoology.animalbiotech.content.DiagramSpec
import com.gasczoology.animalbiotech.content.parseDiagramSpec
import com.gasczoology.animalbiotech.i18n.Lang
import com.gasczoology.animalbiotech.i18n.tr

private class DiagramAsset(val image: androidx.compose.ui.graphics.ImageBitmap, val spec: DiagramSpec?, val altEn: String? = null, val altTa: String? = null)

private fun decode(ctx: android.content.Context, path: String): android.graphics.Bitmap? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    ctx.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
    val opts = BitmapFactory.Options().apply { inSampleSize = if (bounds.outWidth > 2400) 2 else 1 }
    ctx.assets.open(path).use { BitmapFactory.decodeStream(it, null, opts) }
} catch (e: Exception) { null }

/**
 * Loads one figure variant. [suffix] is "" (English art) or "_ta" (Tamil-labelled render).
 * A real photograph supplied as <id>_photo.png|jpg replaces the illustration (and may carry <id>_photo.json markers).
 */
private fun loadAsset(ctx: android.content.Context, id: String, suffix: String): DiagramAsset? = try {
    val names = ctx.assets.list("diagrams")?.toSet() ?: emptySet()
    val file = listOf("${id}_photo.png", "${id}_photo.jpg", "$id$suffix.png").firstOrNull { it in names }
    val specName = if ("${id}_photo.json" in names) "${id}_photo.json" else "$id.json"
    val spec = if (specName in names)
        parseDiagramSpec(ctx.assets.open("diagrams/$specName").bufferedReader(Charsets.UTF_8).use { it.readText() }) else null
    val bmp = if (file != null) decode(ctx, "diagrams/$file") else null
    val alt = if ("$id.alt.json" in names) runCatching {
        org.json.JSONObject(ctx.assets.open("diagrams/$id.alt.json").bufferedReader(Charsets.UTF_8).use { it.readText() })
    }.getOrNull() else null
    if (bmp != null) DiagramAsset(bmp.asImageBitmap(), spec?.takeIf { it.markers.isNotEmpty() },
        alt?.optString("en")?.takeIf { it.isNotBlank() }, alt?.optString("ta")?.takeIf { it.isNotBlank() }) else null
} catch (e: Exception) { null }

/**
 * Inline figure. Marker-style art (numbered badges + bilingual legend) follows the language chips through the legend;
 * browser-rendered figures carry their labels in the image, so English or Tamil picks the matching PNG.
 */
@Composable
fun DiagramView(block: Diagram, lang: Lang, scale: Float) {
    val ctx = LocalContext.current
    val en = remember(block.id) { loadAsset(ctx, block.id, "") }
    val ta = remember(block.id) { loadAsset(ctx, block.id, "_ta") }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFB8C4D4)),
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when {
                en == null -> Text("Figure: ${block.id}", fontSize = (11 * scale).sp, color = Muted)
                en.spec != null -> MarkerFigure(en, block, lang)
                else -> {
                    val showEn = lang == Lang.EN || ta == null
                    val showTa = lang == Lang.TA && ta != null
                    if (showEn) FlatImage(en, en.altEn ?: block.caption.en, lang)
                    if (showTa) FlatImage(ta!!, ta!!.altTa ?: en.altTa ?: block.caption.ta.ifBlank { block.caption.en }, lang)
                }
            }
            BiText(block.caption, lang, scale, base = MaterialTheme.typography.bodyMedium, weight = FontWeight.SemiBold)
            val legend = en?.spec?.markers
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
private fun FlatImage(a: DiagramAsset, description: String, lang: Lang) {
    val host = LocalFigureViewerHost.current
    var expanded by remember { mutableStateOf(false) }
    if (host == null && expanded) FigureViewer(a.image, description, lang) { expanded = false }
    // Alt text is already a full sentence for rebuilt figures; older figures only have the caption.
    val label = if (description.startsWith("Diagram") || description.startsWith("படம்")) description
        else tr(lang, "Diagram: ", "படம்: ") + description
    Image(
        bitmap = a.image, contentDescription = label, contentScale = ContentScale.FillWidth,
        modifier = Modifier.fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .clickable(onClickLabel = tr(lang, "Enlarge diagram", "படத்தைப் பெரிதாக்கு")) { if (host != null) host.request = ViewerRequest(a.image, description, lang) else expanded = true },
    )
}

@Composable
private fun MarkerFigure(a: DiagramAsset, block: Diagram, lang: Lang) {
    val spec = a.spec ?: return
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val w = maxWidth
        val h = w / spec.aspect
        FlatImage(a, block.caption.en, lang)
        spec.markers.forEach { m ->
            Box(
                Modifier.offset(w * m.x - 11.dp, h * m.y - 11.dp).size(22.dp).clip(CircleShape).background(Color(0xFF0B2A4A)),
                contentAlignment = Alignment.Center,
            ) { Text("${m.n}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }
    }
}
