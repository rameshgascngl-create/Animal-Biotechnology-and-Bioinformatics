package com.gasczoology.animalbiotech

import android.content.Context
import android.util.Log
import android.util.Xml
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.BySelector
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.gasczoology.animalbiotech.i18n.Lang
import java.io.ByteArrayOutputStream
import java.util.regex.Pattern
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser

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
                val reviseScreen = By.text("அனைத்திலும் தேடுக")
                val back = By.desc("பின்செல்")
                val searchField = By.clazz("android.widget.EditText")
                tap(device, By.text("மீளாய்வு"), "Revise tab", reviseScreen)
                val chapter = By.text(tamilFirst.title)
                if (!device.wait(Until.hasObject(chapter), waitMillis)) {
                    device.swipe(device.displayWidth / 2, device.displayHeight * 3 / 4,
                        device.displayWidth / 2, device.displayHeight / 3, 30)
                }
                tap(device, chapter, "first revision chapter", By.text("முக்கியக் கருத்துகள்"))
                val tamilPoint = tamilFirst.keyPoints.first()
                assertNotEquals(first.keyPoints.first(), tamilPoint)
                assertTamilText(device, listOf(tamilPoint), "Revise key point")
                tap(device, back, "close revision chapter", reviseScreen)

                tap(device, By.text("கலைச்சொற்கள்: அனைத்தும்"), "global glossary", searchField)
                val culture = revisions.single { it.file == "animal_cell_culture.html" }
                val termIndex = culture.glossary.indices.single { culture.glossary[it].term == "Cryopreservation" }
                val definition = culture.inLanguage(Lang.TA).glossary[termIndex].def
                assertNotEquals(culture.glossary[termIndex].def, definition)
                val search = requireNotNull(device.wait(Until.findObject(searchField), waitMillis)) {
                    "Global glossary search field did not appear"
                }
                search.text = "Cryopreservation"
                assertTamilText(device, listOf(definition), "Glossary definition")
                tap(device, back, "close global glossary", reviseScreen)

                tap(device, By.text("நினைவட்டைகள்"), "Flashcards", back)
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

    private fun tap(device: UiDevice, selector: BySelector, description: String, destination: BySelector) {
        check(!device.hasObject(destination)) {
            tapFailure(device, "Destination for $description was already visible before the tap")
        }
        val node = requireNotNull(device.wait(Until.findObject(selector), waitMillis)) {
            tapFailure(device, "$description did not appear")
        }
        val bounds = node.visibleBounds
        val clicked = device.click(bounds.centerX(), bounds.centerY())
        assertTrue(if (clicked) "Could not open $description" else tapFailure(device, "Could not open $description"), clicked)
        check(device.wait(Until.hasObject(destination), waitMillis)) {
            tapFailure(device, "$description did not reach its destination after the tap")
        }
    }

    private fun tapFailure(device: UiDevice, message: String): String {
        val screen = runCatching {
            val xml = ByteArrayOutputStream().also { device.dumpWindowHierarchy(it) }
            val parser = Xml.newPullParser().apply { setInput(xml.toByteArray().inputStream(), "UTF-8") }
            buildString {
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.eventType != XmlPullParser.START_TAG || parser.name != "node") continue
                    val text = parser.getAttributeValue(null, "text").orEmpty()
                    val description = parser.getAttributeValue(null, "content-desc").orEmpty()
                    if (text.isNotBlank() || description.isNotBlank()) {
                        appendLine("text=${text.replace("\n", "\\n")} | content-desc=${description.replace("\n", "\\n")}")
                    }
                }
            }.ifBlank { "(no text or content descriptions exposed)" }
        }.getOrElse { "(hierarchy dump failed: ${it.javaClass.simpleName}: ${it.message})" }
        val diagnostic = "CITEST $message\n$screen"
        diagnostic.chunked(900).forEach { Log.e("CITEST", it) }
        // CI keeps logcat as an artifact. Include the same text in the failure shown in the job log.
        return "$message\n$diagnostic"
    }

    private fun assertTamilText(device: UiDevice, candidates: List<String>, description: String) {
        assertTrue("$description has no Tamil test data", candidates.isNotEmpty() && candidates.all(tamilScript::containsMatchIn))
        val textPattern = Pattern.compile(candidates.joinToString("|", transform = Pattern::quote))
        assertTrue("$description did not display Tamil study text",
            device.wait(Until.hasObject(By.text(textPattern)), waitMillis))
    }
}
