package com.gasczoology.animalbiotech.i18n

import com.gasczoology.animalbiotech.content.Bi
import org.junit.Assert.assertEquals
import org.junit.Test

class LangTest {
    private val vector = Bi("Vector", "காவி")

    @Test fun parseLang_known_values() {
        assertEquals(Lang.TA, parseLang("TA"))
        assertEquals(Lang.EN, parseLang("BOTH"))   // legacy stored value falls back to English
        assertEquals(Lang.EN, parseLang("EN"))
    }

    @Test fun parseLang_unknown_or_null_defaults_to_english() {
        assertEquals(Lang.EN, parseLang("garbage"))
        assertEquals(Lang.EN, parseLang(null))
        assertEquals(Lang.EN, parseLang(""))
    }

    @Test fun english_only() {
        assertEquals(listOf(Shown("Vector", false, false)), vector.shown(Lang.EN))
    }

    @Test fun tamil_only() {
        assertEquals(listOf(Shown("காவி", true, false)), vector.shown(Lang.TA))
    }

    @Test fun there_are_exactly_two_languages() {
        assertEquals(listOf(Lang.EN, Lang.TA), Lang.entries)
    }

    @Test fun missing_tamil_falls_back_to_english_with_pending_marker() {
        assertEquals(listOf(Shown("Vector", false, true)), Bi("Vector").shown(Lang.TA))
    }
}
