package com.gasczoology.animalbiotech
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import com.gasczoology.animalbiotech.i18n.Lang

class DataBilingualTest {
    private fun question(extra: String = "") = JSONObject("""{"q":"Vector?","o":["A","B"],"a":1$extra}""")
    @Test fun readsTamilWithoutChangingAnswerOrId() {
        val english = parseMcq("a", question())
        val tamil = parseMcq("a", question(""", "q_ta":"கடத்தி?", "o_ta":["அ","ஆ"], "e_ta":"விளக்கம்" """))
        assertEquals(english.id, tamil.id)
        assertEquals(1, tamil.answer)
        assertEquals("ஆ", tamil.optionsTa[tamil.answer])
        assertEquals("கடத்தி?", tamil.qTa)
        assertEquals("விளக்கம்", tamil.explanationTa)
    }
    @Test fun malformedTamilOptionsFallBackWithoutReindexing() {
        assertTrue(parseMcq("a", question(""", "o_ta":["அ"]""")).optionsTa.isEmpty())
        assertTrue(parseMcq("a", question()).qTa.isEmpty())
        assertTrue(pick("Vector", "", Lang.TA).contains("தமிழ் விரைவில்"))
        assertEquals("கடத்தி", pick("Vector", "கடத்தி", Lang.TA))
        assertEquals("Vector", pick("Vector", "கடத்தி", Lang.EN))
    }
    @Test(expected = IllegalArgumentException::class) fun rejectsInvalidAnswer() {
        parseMcq("a", question().put("a", 2))
    }
}
