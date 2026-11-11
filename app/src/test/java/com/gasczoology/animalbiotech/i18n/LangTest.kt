package com.gasczoology.animalbiotech.i18n

import com.gasczoology.animalbiotech.content.Bi
import org.junit.Assert.assertEquals
import org.junit.Test

class LangTest {
    private val vector = Bi("Vector", "காவி")

    @Test fun parseLang_known_values() {
        assertEquals(Lang.TA, parseLang("TA"))
        assertEquals(Lang.BOTH, parseLang("BOTH"))
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

    @Test fun both_shows_english_first() {
        assertEquals(
            listOf(Shown("Vector", false, false), Shown("காவி", true, false)),
            vector.shown(Lang.BOTH),
        )
    }

    @Test fun missing_tamil_falls_back_to_english_with_pending_marker() {
        assertEquals(listOf(Shown("Vector", false, true)), Bi("Vector").shown(Lang.TA))
    }

    @Test fun missing_tamil_in_both_keeps_english_and_adds_pending_tamil_slot() {
        assertEquals(
            listOf(Shown("Vector", false, false), Shown("", true, true)),
            Bi("Vector").shown(Lang.BOTH),
        )
    }
}
