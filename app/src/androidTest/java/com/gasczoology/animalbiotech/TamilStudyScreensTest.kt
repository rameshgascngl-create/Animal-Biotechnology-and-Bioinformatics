package com.gasczoology.animalbiotech

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.gasczoology.animalbiotech.i18n.Lang
import java.util.regex.Pattern
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TamilStudyScreensTest {
    private val tamilScript = Regex("[அ-ஹ]")
    private val waitMillis = 10_000L

    @Test fun reviseGlossaryAndFlashcardsShowTamilStudyContent() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val device = UiDevice.getInstance(instrumentation)
        val preferences = context.getSharedPreferences("animal_biotech", Context.MODE_PRIVATE)
        val previousLanguage = preferences.getString("lang", null)
        val revisions = Repo.revisions(context)
        val first = revisions.first()
        val tamilFirst = first.inLanguage(Lang.TA)

        assertTrue(preferences.edit().putString("lang", Lang.TA.name).commit())
        try {
            ActivityScenario.launch(MainActivity::class.java).use {
                tap(device, By.desc("மீளாய்வு"), "Revise tab")
                val chapter = By.text(tamilFirst.title)
                if (!device.wait(Until.hasObject(chapter), waitMillis)) {
                    device.swipe(device.displayWidth / 2, device.displayHeight * 3 / 4,
                        device.displayWidth / 2, device.displayHeight / 3, 30)
                }
                tap(device, chapter, "first revision chapter")
                val tamilPoint = tamilFirst.keyPoints.first()
                assertNotEquals(first.keyPoints.first(), tamilPoint)
                assertTamilText(device, listOf(tamilPoint), "Revise key point")
                tap(device, By.desc("பின்செல்"), "close revision chapter")

                tap(device, By.text("கலைச்சொற்கள்: அனைத்தும்"), "global glossary")
                val culture = revisions.single { it.file == "animal_cell_culture.html" }
                val termIndex = culture.glossary.indices.single { culture.glossary[it].term == "Cryopreservation" }
                val definition = culture.inLanguage(Lang.TA).glossary[termIndex].def
                assertNotEquals(culture.glossary[termIndex].def, definition)
                val search = requireNotNull(device.wait(Until.findObject(By.clazz("android.widget.EditText")), waitMillis)) {
                    "Global glossary search field did not appear"
                }
                search.text = "Cryopreservation"
                assertTamilText(device, listOf(definition), "Glossary definition")
                tap(device, By.desc("பின்செல்"), "close global glossary")

                tap(device, By.text("நினைவட்டைகள்"), "Flashcards")
                val englishCards = buildCards(revisions, Lang.EN).associateBy { it.id }
                val tamilFronts = buildCards(revisions, Lang.TA).filter {
                    tamilScript.containsMatchIn(it.front) && it.front != englishCards.getValue(it.id).front
                }.map { it.front }.distinct()
                assertTamilText(device, tamilFronts, "Flashcard front")
            }
        } finally {
            val edit = preferences.edit()
            if (previousLanguage == null) edit.remove("lang") else edit.putString("lang", previousLanguage)
            assertTrue(edit.commit())
        }
    }

    private fun tap(device: UiDevice, selector: BySelector, description: String) {
        val node = requireNotNull(device.wait(Until.findObject(selector), waitMillis)) {
            "$description did not appear"
        }
        val bounds = node.visibleBounds
        assertTrue("Could not open $description", device.click(bounds.centerX(), bounds.centerY()))
    }

    private fun assertTamilText(device: UiDevice, candidates: List<String>, description: String) {
        assertTrue("$description has no Tamil test data", candidates.isNotEmpty() && candidates.all(tamilScript::containsMatchIn))
        val textPattern = Pattern.compile(candidates.joinToString("|", transform = Pattern::quote))
        assertTrue("$description did not display Tamil study text",
            device.wait(Until.hasObject(By.text(textPattern)), waitMillis))
    }
}
