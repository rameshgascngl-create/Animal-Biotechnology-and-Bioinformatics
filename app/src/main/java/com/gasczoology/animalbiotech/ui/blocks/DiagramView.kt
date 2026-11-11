package com.gasczoology.animalbiotech.ui.blocks

import android.graphics.BitmapFactory
import androidx.compose.foundation.clickable
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

private class DiagramAsset(val image: androidx.compose.ui.graphics.ImageBitmap, val spec: DiagramSpec?)

private fun decode(ctx: android.content.Context, path: String): android.graphics.Bitmap? = try {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    ctx.assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
    val opts = BitmapFactory.Options().apply { inSampleSize = if (bounds.outWidth > 1500) 2 else 1 }
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
    if (bmp != null) DiagramAsset(bmp.asImageBitmap(), spec?.takeIf { it.markers.isNotEmpty() }) else null
} catch (e: Exception) { null }

/**
 * Inline figure. Marker-style art (numbered badges + bilingual legend) follows the language chips through the legend;
 * browser-rendered figures carry English labels in the image and are shown unchanged in both languages.
 */
@Composable
fun DiagramView(block: Diagram, lang: Lang, scale: Float) {
    val ctx = LocalContext.current
    val en = remember(block.id) { loadAsset(ctx, block.id, "") }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFB8C4D4)),
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            when {
                en == null -> Text("Figure: ${block.id}", fontSize = (11 * scale).sp, color = Muted)
                en.spec != null -> MarkerFigure(en, block, lang)
                // Owner decision: Tamil mode also shows the English-labelled figure; only the caption is Tamil.
                else -> FlatImage(en, block.caption.en, lang)
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
    var expanded by remember(a) { mutableStateOf(false) }
    if (expanded) {
        Dialog(onDismissRequest = { expanded = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            var zoom by remember { mutableStateOf(1f) }
            var pan by remember { mutableStateOf(Offset.Zero) }
            Column(Modifier.fillMaxSize().background(Color.White)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { zoom = 1f; pan = Offset.Zero }) { Text(tr(lang, "Reset", "மீட்டமை")) }
                    TextButton(onClick = { expanded = false }) { Text(tr(lang, "Close", "மூடு")) }
                }
                Box(Modifier.weight(1f).fillMaxWidth().clip(androidx.compose.ui.graphics.RectangleShape)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, delta, factor, _ ->
                            zoom = (zoom * factor).coerceIn(1f, 5f)
                            val proposed = pan + delta
                            val maxX = size.width * (zoom - 1f) / 2f
                            val maxY = size.height * (zoom - 1f) / 2f
                            pan = Offset(proposed.x.coerceIn(-maxX, maxX), proposed.y.coerceIn(-maxY, maxY))
                        }
                    }, contentAlignment = Alignment.Center) {
                    Image(a.image, description, Modifier.fillMaxSize().graphicsLayer(
                        scaleX = zoom, scaleY = zoom, translationX = pan.x, translationY = pan.y), contentScale = ContentScale.Fit)
                }
                Text(tr(lang, "Pinch to zoom • Drag to move", "விரல்களால் பெரிதாக்கி நகர்த்தவும்"), Modifier.padding(12.dp))
            }
        }
    }
    Image(
        bitmap = a.image, contentDescription = "Diagram: $description", contentScale = ContentScale.FillWidth,
        modifier = Modifier.fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
            .clickable(onClickLabel = tr(lang, "Enlarge diagram", "படத்தைப் பெரிதாக்கு")) { expanded = true },
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
