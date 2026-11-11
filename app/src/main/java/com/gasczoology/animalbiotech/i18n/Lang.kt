package com.gasczoology.animalbiotech.i18n

import com.gasczoology.animalbiotech.content.Bi

/** Reader language setting: English only, Tamil only, or both. */
enum class Lang { EN, TA, BOTH }

/** Unknown, empty or corrupt stored values fall back to English. */
fun parseLang(raw: String?): Lang = Lang.entries.firstOrNull { it.name == raw } ?: Lang.EN

/**
 * One line of text to display. [pending] is true when Tamil was requested but not authored,
 * so the UI can show a visible "Tamil pending" marker instead of silently showing English.
 */
data class Shown(val text: String, val isTamil: Boolean, val pending: Boolean)

fun Bi.shown(lang: Lang): List<Shown> {
    val hasTa = ta.isNotBlank()
    return when (lang) {
        Lang.EN -> listOf(Shown(en, false, false))
        Lang.TA -> if (hasTa) listOf(Shown(ta, true, false)) else listOf(Shown(en, false, true))
        Lang.BOTH -> listOf(
            Shown(en, false, false),
            if (hasTa) Shown(ta, true, false) else Shown("", true, true),
        )
    }
}
