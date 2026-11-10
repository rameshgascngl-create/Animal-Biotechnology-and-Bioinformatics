package com.gasczoology.animalbiotech

import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.gasczoology.animalbiotech.content.ChapterRepo
import com.gasczoology.animalbiotech.content.Diagram
import com.gasczoology.animalbiotech.i18n.Lang
import com.gasczoology.animalbiotech.ui.LessonScreen
import org.junit.Test
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith

/** Genuine native screens at 360 dp; fixture opens an actual chapter without scrolling through the syllabus. */
@RunWith(AndroidJUnit4::class)
class ReaderCaptureTest {
    @Test fun captureReaderLanguagesAndEnlargement() {
        val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        fun capture(name: String) {
            device.waitForIdle()
            device.executeShellCommand("screencap -p /sdcard/Pictures/$name.png")
        }
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            assertTrue(device.wait(Until.hasObject(By.text("Animal Biotechnology")), 15000))
            capture("01-home")
            for (lang in Lang.entries) {
                scenario.onActivity { activity ->
                    val chapter = requireNotNull(ChapterRepo.load(activity, "vectors_genetic_engineering.html", "I")?.chapter)
                    // Keep the original section and diagram, removing preceding sections only to bring the figure into view.
                    val section = chapter.sections.first { it.blocks.any { b -> b is Diagram } }
                    val start = section.blocks.indexOfFirst { it is Diagram }
                    val fixture = chapter.copy(sections = listOf(section.copy(blocks = section.blocks.drop(start))))
                    activity.setContent {
                        MaterialTheme { LessonScreen(fixture, lang, {}, 100, false, {}, false, {}, {}, null) }
                    }
                }
                device.waitForIdle()
                assertTrue(device.wait(Until.hasObject(By.descStartsWith("Diagram:")), 10000) ||
                    device.wait(Until.hasObject(By.clazz("android.widget.ImageView")), 10000))
                capture("02-lesson-" + lang.name)
                if (lang == Lang.EN) {
                    val image = device.findObject(By.descStartsWith("Diagram:"))
                        ?: device.findObject(By.clazz("android.widget.ImageView"))
                    requireNotNull(image).click()
                    assertTrue(device.wait(Until.hasObject(By.text("Close / மூடு")), 5000))
                    capture("03-enlarged")
                    device.findObject(By.text("Close / மூடு")).click()
                }
            }
        }
    }
}
