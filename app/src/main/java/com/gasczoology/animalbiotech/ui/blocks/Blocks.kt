package com.gasczoology.animalbiotech.ui.blocks

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gasczoology.animalbiotech.R
import com.gasczoology.animalbiotech.content.Bi
import com.gasczoology.animalbiotech.content.Block
import com.gasczoology.animalbiotech.content.Bullets
import com.gasczoology.animalbiotech.content.Callout
import com.gasczoology.animalbiotech.content.CalloutKind
import com.gasczoology.animalbiotech.content.Diagram
import com.gasczoology.animalbiotech.content.GlossaryEntry
import com.gasczoology.animalbiotech.content.KeyTerm
import com.gasczoology.animalbiotech.content.Paragraph
import com.gasczoology.animalbiotech.content.Reveal
import com.gasczoology.animalbiotech.content.Steps
import com.gasczoology.animalbiotech.content.Summary
import com.gasczoology.animalbiotech.content.Table
import com.gasczoology.animalbiotech.content.Unparsed
import com.gasczoology.animalbiotech.i18n.Lang
import com.gasczoology.animalbiotech.i18n.shown
import com.gasczoology.animalbiotech.ui.padRow
import com.gasczoology.animalbiotech.ui.tableWidth

private val Navy = Color(0xFF0F2744)
private val Muted = Color(0xFF3B4F66)

/** Noto Sans Tamil (SIL OFL 1.1), bundled so Tamil renders the same on every device. */
val TamilFont = FontFamily(
    Font(R.font.notosanstamil_regular, FontWeight.Normal),
    Font(R.font.notosanstamil_medium, FontWeight.Medium),
    Font(R.font.notosanstamil_bold, FontWeight.Bold),
)

/**
 * Renders one bilingual value according to [lang]. Tamil uses the bundled Tamil font with 1.35 line height.
 * When Tamil is requested but missing, a visible "Tamil pending" tag is shown; English is never passed off as Tamil.
 */
@Composable
fun BiText(
    bi: Bi,
    lang: Lang,
    scale: Float,
    modifier: Modifier = Modifier,
    base: TextStyle = MaterialTheme.typography.bodyLarge,
    weight: FontWeight? = null,
    color: Color = Color(0xFF1B2B3F),
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        for (s in bi.shown(lang)) {
            if (s.pending && s.text.isEmpty()) {
                Text("தமிழ் விரைவில் / Tamil pending", fontSize = (11 * scale).sp, color = Color(0xFF9A5B00))
            } else if (s.isTamil) {
                val size = base.fontSize.value.takeIf { !it.isNaN() } ?: 16f
                Text(
                    s.text,
                    fontFamily = TamilFont,
                    fontSize = (size * scale).sp,
                    lineHeight = (size * scale * 1.35f).sp,
                    fontWeight = weight,
                    color = color,
                )
            } else {
                val size = base.fontSize.value.takeIf { !it.isNaN() } ?: 16f
                Text(
                    s.text,
                    fontSize = (size * scale).sp,
                    lineHeight = (size * scale * 1.45f).sp,
                    fontWeight = weight,
                    color = color,
                )
                if (s.pending) {
                    Text("தமிழ் விரைவில் / Tamil pending", fontSize = (11 * scale).sp, color = Color(0xFF9A5B00))
                }
            }
        }
    }
}

@Composable
fun BlockView(block: Block, lang: Lang, scale: Float) {
    when (block) {
        is Paragraph -> BiText(block.text, lang, scale)
        is Bullets -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            block.items.forEachIndexed { i, item ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (block.ordered) "${i + 1}." else "•", color = Navy, fontWeight = FontWeight.Bold)
                    BiText(item, lang, scale, Modifier.weight(1f))
                }
            }
        }
        is Steps -> Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            block.items.forEachIndexed { i, item ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.background(Navy, MaterialTheme.shapes.small).padding(horizontal = 8.dp, vertical = 2.dp)) {
                        Text("${i + 1}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    BiText(item, lang, scale, Modifier.weight(1f))
                }
            }
        }
        is Table -> TableView(block, lang, scale)
        is Callout -> CalloutView(block, lang, scale)
        is KeyTerm -> TermCard(block.term, block.definition, lang, scale)
        is GlossaryEntry -> TermCard(block.term, block.definition, lang, scale)
        is Summary -> Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFEAF3FF))) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Summary / சுருக்கம்", fontWeight = FontWeight.Bold, color = Navy, fontFamily = TamilFont)
                block.items.forEach { item ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("✓", color = Navy, fontWeight = FontWeight.Bold)
                        BiText(item, lang, scale, Modifier.weight(1f))
                    }
                }
            }
        }
        is Reveal -> RevealView(block, lang, scale)
        is Diagram -> DiagramView(block, lang, scale)
        is Unparsed -> Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF4E0)),
            border = BorderStroke(1.dp, Color(0xFFE0A030)),
        ) {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Content not structured (${block.selector})", fontWeight = FontWeight.Bold, color = Color(0xFF8A5200))
                Text(block.text, fontSize = (14 * scale).sp, color = Color(0xFF3B2A10))
            }
        }
    }
}

@Composable
private fun TermCard(term: Bi, definition: Bi, lang: Lang, scale: Float) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FC)), border = BorderStroke(1.dp, Color(0xFFD5DEEA))) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            BiText(term, lang, scale, weight = FontWeight.Bold, color = Navy)
            BiText(definition, lang, scale, base = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CalloutView(c: Callout, lang: Lang, scale: Float) {
    val (bg, edge, label) = when (c.kind) {
        CalloutKind.INFO -> Triple(Color(0xFFEAF3FF), Color(0xFF2F6FB5), "Note / குறிப்பு")
        CalloutKind.WARN -> Triple(Color(0xFFFFF0EE), Color(0xFFC0392B), "Caution / எச்சரிக்கை")
        CalloutKind.TIP -> Triple(Color(0xFFEAF8EE), Color(0xFF2E8B57), "Tip / உதவிக்குறிப்பு")
        CalloutKind.EXAM -> Triple(Color(0xFFFFF7E0), Color(0xFFB8860B), "Exam / தேர்வு")
    }
    Card(colors = CardDefaults.cardColors(containerColor = bg), border = BorderStroke(1.dp, edge)) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(label, fontWeight = FontWeight.Bold, color = edge, fontFamily = TamilFont, fontSize = (13 * scale).sp)
            BiText(c.text, lang, scale)
        }
    }
}

@Composable
private fun RevealView(r: Reveal, lang: Lang, scale: Float) {
    var open by rememberSaveable { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth().clickable { open = !open },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9FC)),
        border = BorderStroke(1.dp, Color(0xFFD5DEEA)),
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            BiText(r.question, lang, scale, weight = FontWeight.SemiBold, color = Navy)
            if (open) BiText(r.answer, lang, scale)
            else Text("Tap to reveal / பதிலைக் காண தொடவும்", fontSize = (12 * scale).sp, color = Muted, fontFamily = TamilFont)
        }
    }
}

@Composable
private fun TableView(t: Table, lang: Lang, scale: Float) {
    val width = tableWidth(t.header, t.rows)
    val cellW = 170.dp
    Box(Modifier.horizontalScroll(rememberScrollState())) {
        Column {
            Row(Modifier.background(Navy)) {
                padRow(t.header, width).forEach { c ->
                    BiText(c, lang, scale, Modifier.width(cellW).padding(8.dp), MaterialTheme.typography.bodyMedium, FontWeight.Bold, Color.White)
                }
            }
            t.rows.forEachIndexed { i, row ->
                Row(Modifier.background(if (i % 2 == 0) Color(0xFFF7F9FC) else Color.White)) {
                    padRow(row, width).forEach { c ->
                        BiText(c, lang, scale, Modifier.width(cellW).padding(8.dp), MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
