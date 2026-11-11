package com.gasczoology.animalbiotech.ui.blocks

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

/** A run of text. [bold] is between `**` markers, [italic] between `_` markers (gene and organism names). */
data class Span(val text: String, val bold: Boolean, val italic: Boolean = false)

private fun asciiAlnum(c: Char) = c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9'

/** Splits one run into italic and plain parts. Underscores inside words (snake_case) stay literal. */
private fun italics(s: String, bold: Boolean): List<Span> {
    val out = ArrayList<Span>()
    val plain = StringBuilder()
    fun flush() { if (plain.isNotEmpty()) { out.add(Span(plain.toString(), bold)); plain.clear() } }
    var i = 0
    while (i < s.length) {
        if (s[i] == '_' && (i == 0 || !asciiAlnum(s[i - 1]) && s[i - 1] != '_') && i + 1 < s.length && !s[i + 1].isWhitespace() && s[i + 1] != '_') {
            var j = i + 2
            var close = -1
            while (j < s.length && s[j] != '\n') {
                if (s[j] == '_' && !s[j - 1].isWhitespace() && (j + 1 == s.length || !asciiAlnum(s[j + 1]) && s[j + 1] != '_')) { close = j; break }
                j++
            }
            if (close > 0) { flush(); out.add(Span(s.substring(i + 1, close), bold, true)); i = close + 1; continue }
        }
        plain.append(s[i]); i++
    }
    flush()
    return out
}

/**
 * Splits `**bold**` and `_italic_` markers into spans. The markers are never shown. An unpaired marker
 * (or an empty `****`) is left as literal text so no content is ever lost.
 */
fun parseInline(s: String): List<Span> {
    val out = ArrayList<Span>()
    var i = 0
    val plain = StringBuilder()
    fun flush() { if (plain.isNotEmpty()) { out.addAll(italics(plain.toString(), false)); plain.clear() } }
    while (i < s.length) {
        if (s.startsWith("**", i)) {
            val close = s.indexOf("**", i + 2)
            if (close > i + 2) {
                flush()
                out.addAll(italics(s.substring(i + 2, close), true))
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
        val style = SpanStyle(fontWeight = if (sp.bold) FontWeight.Bold else null, fontStyle = if (sp.italic) FontStyle.Italic else null)
        withStyle(style) { append(sp.text) }
    }
}

private val revealPrompts = setOf("click to reveal", "\u0BB5\u0BC6\u0BB3\u0BBF\u0BAA\u0BCD\u0BAA\u0B9F\u0BC1\u0BA4\u0BCD\u0BA4\u0BA4\u0BCD \u0BA4\u0BCA\u0B9F\u0BC1\u0B95")

/** Removes a trailing "Click to reveal" line that the HTML source put inside the question text. */
fun stripRevealPrompt(s: String): String {
    val lines = s.trimEnd().split('\n')
    return if (lines.size > 1 && lines.last().trim().lowercase() in revealPrompts) lines.dropLast(1).joinToString("\n").trimEnd() else s
}
