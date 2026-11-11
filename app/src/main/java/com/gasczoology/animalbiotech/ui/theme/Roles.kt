package com.gasczoology.animalbiotech.ui.theme

import androidx.compose.ui.graphics.Color
import com.gasczoology.animalbiotech.content.CalloutKind
import com.gasczoology.animalbiotech.i18n.Lang

/** What a block *is*, so each kind of teaching content gets its own shade. */
enum class Role { DEFINITION, EXPLANATION, QUESTION, ANSWER, EXAM, WARNING, TIP, FOCUS, NOTE, SUMMARY }

data class RoleStyle(val bg: Color, val edge: Color, val en: String, val ta: String) {
    fun label(lang: Lang) = if (lang == Lang.TA) ta else en
}

fun roleStyle(r: Role): RoleStyle = when (r) {
    Role.DEFINITION -> RoleStyle(Color(0xFFE6F0FF), Color(0xFF2F6FB5), "Definition", "வரையறை")
    Role.EXPLANATION -> RoleStyle(Color(0xFFE7F6EC), Color(0xFF2E8B57), "Explanation", "விளக்கம்")
    Role.QUESTION -> RoleStyle(Color(0xFFF0EAFF), Color(0xFF6B46C1), "Question", "வினா")
    Role.ANSWER -> RoleStyle(Color(0xFFE7F6EC), Color(0xFF2E8B57), "Answer", "விடை")
    Role.EXAM -> RoleStyle(Color(0xFFFFF4D6), Color(0xFFB8860B), "Exam point", "தேர்வுக் குறிப்பு")
    Role.WARNING -> RoleStyle(Color(0xFFFFEBE8), Color(0xFFC0392B), "Caution", "எச்சரிக்கை")
    Role.TIP -> RoleStyle(Color(0xFFE0F5F3), Color(0xFF0E8A7D), "Tip", "உதவிக்குறிப்பு")
    Role.FOCUS -> RoleStyle(Color(0xFFFCE8F3), Color(0xFFB83280), "PG focus", "முதுநிலைக் கவனம்")
    Role.NOTE -> RoleStyle(Color(0xFFEDF1F6), Color(0xFF5B7391), "Note", "குறிப்பு")
    Role.SUMMARY -> RoleStyle(Color(0xFFE3F3FA), Color(0xFF0B7A9E), "Summary", "சுருக்கம்")
}

private fun lead(textEn: String): String =
    textEn.trimStart { !it.isLetter() }.lowercase()

/**
 * Chooses the shade for a callout from its stored kind and the label the author wrote at the start of the text
 * ("Definition", "Exam tip", "PG focus" ...). Unlabelled notes stay neutral.
 */
fun calloutRole(kind: CalloutKind, textEn: String): Role {
    val s = lead(textEn)
    return when {
        s.startsWith("definition") || s.startsWith("full form") -> Role.DEFINITION
        s.startsWith("exam") || kind == CalloutKind.EXAM -> Role.EXAM
        s.startsWith("pg focus") -> Role.FOCUS
        kind == CalloutKind.WARN -> Role.WARNING
        s.startsWith("key insight") || s.startsWith("core concept") || s.startsWith("key significance") -> Role.EXPLANATION
        kind == CalloutKind.TIP || s.startsWith("remember") || s.startsWith("memory aid") -> Role.TIP
        else -> Role.NOTE
    }
}

/** Header colour pairs for the five units (deep, pale). */
val UnitAccents = listOf(
    Color(0xFF1F4E8C) to Color(0xFFE6EEFA),
    Color(0xFF0E8A7D) to Color(0xFFE0F5F3),
    Color(0xFF7A4FB8) to Color(0xFFF0EAFB),
    Color(0xFFC0702A) to Color(0xFFFFF0E0),
    Color(0xFFB83280) to Color(0xFFFCE8F3),
)
