package com.gasczoology.animalbiotech.ui.blocks

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/** A run of text; [bold] is true between a pair of `**` markers. */
data class Span(val text: String, val bold: Boolean)

/**
 * Splits `**bold**` markers into spans. The markers are never shown. An unpaired `**`
 * (or an empty `****`) is left as literal text so no content is ever lost.
 */
fun parseInline(s: String): List<Span> {
    val out = ArrayList<Span>()
    var i = 0
    val plain = StringBuilder()
    fun flush() { if (plain.isNotEmpty()) { out.add(Span(plain.toString(), false)); plain.clear() } }
    while (i < s.length) {
        if (s.startsWith("**", i)) {
            val close = s.indexOf("**", i + 2)
            if (close > i + 2) {
                flush()
                out.add(Span(s.substring(i + 2, close), true))
                i = close + 2
                continue
            }
        }
        plain.append(s[i]); i++
    }
    flush()
    return out
}

fun inlineText(s: String): AnnotatedString = buildAnnotatedString {
    for (sp in parseInline(s)) {
        if (sp.bold) withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(sp.text) } else append(sp.text)
    }
}
