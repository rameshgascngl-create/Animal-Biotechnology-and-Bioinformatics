package com.gasczoology.animalbiotech.ui.blocks

import android.graphics.BitmapFactory
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

private class DiagramAsset(val image: androidx.compose.ui.graphics.ImageBitmap, val spec: DiagramSpec)

/** Loads <id>_photo.(png|jpg) if present (a real photograph supplied by the author), otherwise <id>.png. */
private fun loadAsset(ctx: android.content.Context, id: String): DiagramAsset? = try {
    val dir = "diagrams/"
    val names = ctx.assets.list("diagrams")?.toSet() ?: emptySet()
    val file = listOf("${id}_photo.png", "${id}_photo.jpg", "$id.png").firstOrNull { it in names }
    val specName = if ("${id}_photo.json" in names) "${id}_photo.json" else "$id.json"
    val spec = if (file != null && specName in names)
        parseDiagramSpec(ctx.assets.open(dir + specName).bufferedReader(Charsets.UTF_8).use { it.readText() }) else null
    val bmp = if (file != null) ctx.assets.open(dir + file).use { BitmapFactory.decodeStream(it) } else null
    if (bmp != null && spec != null) DiagramAsset(bmp.asImageBitmap(), spec) else null
} catch (e: Exception) { null }

/** Inline figure: image with numbered badges, caption, and a bilingual legend. Falls back to a caption card. */
@Composable
fun DiagramView(block: Diagram, lang: Lang, scale: Float) {
    val ctx = LocalContext.current
    val asset = remember(block.id) { loadAsset(ctx, block.id) }
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFB8C4D4)),
    ) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (asset != null) {
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    val w = maxWidth
                    val h = w / asset.spec.aspect
                    Image(
                        bitmap = asset.image, contentDescription = block.caption.en,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)),
                    )
                    asset.spec.markers.forEach { m ->
                        Box(
                            Modifier.offset(w * m.x - 11.dp, h * m.y - 11.dp).size(22.dp)
                                .clip(CircleShape).background(Color(0xFF0B2A4A)),
                            contentAlignment = Alignment.Center,
                        ) { Text("${m.n}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                    }
                }
            }
            BiText(block.caption, lang, scale, base = MaterialTheme.typography.bodyMedium, weight = FontWeight.SemiBold)
            if (asset != null) {
                asset.spec.markers.forEach { m ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            Modifier.size(20.dp).clip(CircleShape).background(Color(0xFF0B2A4A)),
                            contentAlignment = Alignment.Center,
                        ) { Text("${m.n}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                        BiText(m.label, lang, scale, Modifier.weight(1f), base = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                Text("Figure: ${block.id}", fontSize = (11 * scale).sp, color = Muted)
            }
        }
    }
}
