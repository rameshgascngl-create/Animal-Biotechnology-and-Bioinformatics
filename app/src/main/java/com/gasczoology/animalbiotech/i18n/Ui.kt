package com.gasczoology.animalbiotech.i18n

/** One interface string in the active language only; English and Tamil are never shown together. */
fun tr(lang: Lang, en: String, ta: String): String = if (lang == Lang.TA) ta else en
