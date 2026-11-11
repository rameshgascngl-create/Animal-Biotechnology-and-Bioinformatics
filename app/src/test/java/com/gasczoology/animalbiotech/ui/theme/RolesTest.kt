package com.gasczoology.animalbiotech.ui.theme

import com.gasczoology.animalbiotech.content.CalloutKind
import com.gasczoology.animalbiotech.i18n.Lang
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class RolesTest {
    @Test fun definition_label_wins_over_info_kind() = assertEquals(Role.DEFINITION, calloutRole(CalloutKind.INFO, "📌 Definition (UN Convention, 1992) ..."))
    @Test fun exam_tip_is_exam() = assertEquals(Role.EXAM, calloutRole(CalloutKind.INFO, "Exam tip: remember the order"))
    @Test fun warn_kind_is_warning() = assertEquals(Role.WARNING, calloutRole(CalloutKind.WARN, "Do not confuse X with Y"))
    @Test fun key_insight_is_explanation() = assertEquals(Role.EXPLANATION, calloutRole(CalloutKind.INFO, "💡 Key Insight: ..."))
    @Test fun unlabelled_info_is_neutral_note() = assertEquals(Role.NOTE, calloutRole(CalloutKind.INFO, "Plain remark"))
    @Test fun exam_kind_without_label_is_exam() = assertEquals(Role.EXAM, calloutRole(CalloutKind.EXAM, "Something"))
    @Test fun every_role_has_distinct_background_for_the_three_teaching_kinds() {
        assertNotEquals(roleStyle(Role.DEFINITION).bg, roleStyle(Role.QUESTION).bg)
        assertNotEquals(roleStyle(Role.QUESTION).bg, roleStyle(Role.EXPLANATION).bg)
        assertNotEquals(roleStyle(Role.DEFINITION).bg, roleStyle(Role.EXPLANATION).bg)
    }
    @Test fun labels_are_single_language() {
        for (r in Role.entries) {
            val st = roleStyle(r)
            assertEquals(false, st.label(Lang.EN).any { it in '஀'..'௿' })
            assertEquals(true, st.label(Lang.TA).any { it in '஀'..'௿' })
        }
    }
}
