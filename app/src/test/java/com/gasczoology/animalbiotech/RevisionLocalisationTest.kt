package com.gasczoology.animalbiotech

import com.gasczoology.animalbiotech.i18n.Lang
import org.junit.Assert.assertEquals
import org.junit.Test

class RevisionLocalisationTest {
    private val source = RevisionSet(
        "sample.html", "Chapter", "I",
        listOf("Point", "Second point"),
        listOf(Saq("Question", "Answer", "வினா", "விடை")),
        listOf(Term("Term", "Definition", "Chapter", "சொல்", "விளக்கம்")),
        titleTa = "அத்தியாயம்",
        keyPointsTa = listOf("கருத்து"),
    )

    @Test fun tamil_uses_available_fields_and_falls_back_for_older_data() {
        val tamil = source.inLanguage(Lang.TA)
        assertEquals("அத்தியாயம்", tamil.title)
        assertEquals(listOf("கருத்து", "Second point"), tamil.keyPoints)
        assertEquals("விடை", tamil.saqs.single().a)
        assertEquals("விளக்கம்", tamil.glossary.single().def)
        assertEquals("அத்தியாயம்", tamil.glossary.single().module)
        assertEquals(source, source.inLanguage(Lang.EN))
    }

    @Test fun language_switch_preserves_flashcard_schedule_keys() {
        val english = buildCards(listOf(source), Lang.EN)
        val tamil = buildCards(listOf(source), Lang.TA)
        assertEquals(english.map { it.id }, tamil.map { it.id })
        assertEquals(listOf("விடை", "விளக்கம்"), tamil.map { it.back })
    }
}
