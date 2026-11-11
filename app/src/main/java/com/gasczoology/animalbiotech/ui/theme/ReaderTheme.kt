package com.gasczoology.animalbiotech.ui.theme

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily

/** Reading surfaces for the book reader. */
enum class ReaderTheme { PAPER, SEPIA, NIGHT }

data class ReaderPalette(val bg: Color, val text: Color, val heading: Color, val muted: Color, val bar: Color, val dark: Boolean)

fun readerPalette(t: ReaderTheme): ReaderPalette = when (t) {
    ReaderTheme.PAPER -> ReaderPalette(Color(0xFFFAF7F0), Color(0xFF1F2933), Color(0xFF0F2744), Color(0xFF5B6B7B), Color(0xFFD8D2C2), false)
    ReaderTheme.SEPIA -> ReaderPalette(Color(0xFFF4ECD8), Color(0xFF433422), Color(0xFF5B3A1A), Color(0xFF7A6850), Color(0xFFD9CBA8), false)
    ReaderTheme.NIGHT -> ReaderPalette(Color(0xFF121820), Color(0xFFD6DCE4), Color(0xFFF2F5F9), Color(0xFF9AA7B5), Color(0xFF2C3744), true)
}

fun parseReaderTheme(s: String?): ReaderTheme = ReaderTheme.entries.firstOrNull { it.name == s } ?: ReaderTheme.PAPER

val LocalReaderPalette = compositionLocalOf { readerPalette(ReaderTheme.PAPER) }

/** Serif body face for English prose inside the reader only; null means the default face. */
val LocalBodyFont = compositionLocalOf<FontFamily?> { null }
