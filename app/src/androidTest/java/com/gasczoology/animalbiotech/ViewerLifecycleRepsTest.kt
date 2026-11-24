package com.gasczoology.animalbiotech

import android.os.Build
import android.util.Log
import android.util.Xml
import android.view.View
import android.view.ViewGroup
import android.view.inspector.WindowInspector
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.children
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.gasczoology.animalbiotech.i18n.Lang
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser
import java.io.ByteArrayOutputStream

/**
 * Tier 3: viewer lifecycle and memory budget on a few representative figures (CI passes `reps` as id:LANG pairs: the tallest, the widest and a Tamil one).
 * The viewer's behaviour does not depend on the individual figure, so this is not run per figure.
 * For each: open the viewer, rotate left/right/natural, recreate the activity, press Back; PSS must stay within 150 MB inline and 200 MB with the viewer open.
 */
@RunWith(AndroidJUnit4::class)
class ViewerLifecycleRepsTest {
    private fun assertPssWithinBudget(what: String, limitMb: Int) {
        System.gc(); Thread.sleep(800); System.gc(); Thread.sleep(200)
        val out = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).executeShellCommand("dumpsys meminfo com.gasczoology.animalbiotech")
        val kb = Regex("TOTAL PSS:\\s+(\\d+)").find(out)?.groupValues?.get(1)?.toInt() ?: Regex("TOTAL\\s+(\\d+)").find(out)?.groupValues?.get(1)?.toInt() ?: error("no PSS in meminfo")
        android.util.Log.i("CITEST", "BUDGET $what PSS=${kb / 1024} MB (limit $limitMb)")
        assertTrue("PSS budget exceeded at $what: ${kb / 1024} MB > $limitMb MB", kb <= limitMb * 1024)
    }

    private fun rotationMessage(device: UiDevice, rotation: String, message: String, open: Boolean): String {
        if (open) return message

        val display = "display=${device.displayWidth}x${device.displayHeight}px displayRotation=${device.displayRotation}"
        val rootPresent = figureViewerRootPresent()
        val screen = runCatching {
            val xml = ByteArrayOutputStream().also { device.dumpWindowHierarchy(it) }
            val parser = Xml.newPullParser().apply { setInput(xml.toByteArray().inputStream(), "UTF-8") }
            buildString {
                while (parser.next() != XmlPullParser.END_DOCUMENT) {
                    if (parser.eventType != XmlPullParser.START_TAG || parser.name != "node") continue
                    val text = parser.getAttributeValue(null, "text").orEmpty()
                    val description = parser.getAttributeValue(null, "content-desc").orEmpty()
                    if (text.isBlank() && description.isBlank()) continue
                    val bounds = parser.getAttributeValue(null, "bounds")
                    appendLine("bounds=$bounds | text=${text.replace("\n", "\\n")} | content-desc=${description.replace("\n", "\\n")}")
                }
            }.ifBlank { "(no visible text or content descriptions exposed)" }
        }.getOrElse { "(hierarchy dump failed: ${it.javaClass.simpleName}: ${it.message})" }
        val diagnostic = "$message\nrotation=$rotation $display figureViewerRootPresent=$rootPresent\n$screen"
        diagnostic.chunked(900).forEach { Log.e("CITEST", it) }
        return diagnostic
    }

    // The figure viewer is the only Compose dialog opened by this test. Check its attached
    // window root independently of whether the Close control is exposed to UI Automator.
    private fun figureViewerRootPresent(): String {
        if (Build.VERSION.SDK_INT < 29) return "unknown (WindowInspector requires API 29)"
        var present = "unknown"
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            fun View.hasDialogRoot(): Boolean =
                (this is DialogWindowProvider && isAttachedToWindow) ||
                    (this is ViewGroup && children.any { it.hasDialogRoot() })
            present = runCatching {
                WindowInspector.getGlobalWindowViews().any { it.hasDialogRoot() }.toString()
            }.getOrElse { "unknown (${it.javaClass.simpleName}: ${it.message})" }
        }
        return present
    }

    @Test fun viewerLifecycleAndMemoryOnRepresentativeFigures() {
        val reps = FigureNav.listArg("reps").map { val (id, l) = it.split(':'); id to Lang.valueOf(l) }
        val inst = InstrumentationRegistry.getInstrumentation()
        val device = UiDevice.getInstance(inst)
        val dm = inst.targetContext.resources.displayMetrics
        val tag = "${(dm.widthPixels / dm.density).toInt()}x${(dm.heightPixels / dm.density).toInt()}"
        fun capture(name: String) { device.waitForIdle(); Thread.sleep(900); device.executeShellCommand("screencap -p /sdcard/Pictures/$name.png") }
        try {
            ActivityScenario.launch(MainActivity::class.java).use { scenario ->
                assertTrue(device.wait(Until.hasObject(By.text("Animal Biotechnology")), 15000))
                for ((id, lang) in reps) {
                    val close = if (lang == Lang.EN) "Close" else "மூடு"
                    fun viewerOpen() = device.wait(Until.hasObject(By.text(close)), 6000) || device.wait(Until.hasObject(By.desc(close)), 1500)
                    scenario.onActivity { FigureNav.show(it, id, lang) }
                    FigureNav.reach(device, id, tag, lang, ::capture)
                    FigureNav.alignTop(device, id)
                    assertPssWithinBudget("inline $id/${lang.name}", 150)
                    FigureNav.tap(device, id)
                    assertTrue("$id/${lang.name}: viewer did not open", viewerOpen())
                    Thread.sleep(1200)
                    assertPssWithinBudget("viewer $id/${lang.name}", 200)
                    device.setOrientationLeft(); device.waitForIdle()
                    val openAfterLeft = viewerOpen()
                    assertTrue(rotationMessage(device, "left", "$id/${lang.name}: viewer lost after rotating left", openAfterLeft), openAfterLeft)
                    device.setOrientationRight(); device.waitForIdle()
                    val openAfterRight = viewerOpen()
                    assertTrue(rotationMessage(device, "right", "$id/${lang.name}: viewer lost after rotating right", openAfterRight), openAfterRight)
                    device.setOrientationNatural(); device.waitForIdle()
                    val openAfterNatural = viewerOpen()
                    assertTrue(rotationMessage(device, "natural", "$id/${lang.name}: viewer lost after returning to natural orientation", openAfterNatural), openAfterNatural)
                    scenario.recreate()
                    scenario.onActivity { FigureNav.show(it, id, lang) }
                    assertTrue("$id/${lang.name}: viewer not restored after activity recreation", viewerOpen())
                    capture("22-lifecycle-$tag-$id-${lang.name}")
                    device.pressBack()
                    assertTrue("$id/${lang.name}: Back did not close the viewer", device.wait(Until.gone(By.text(close)), 5000))
                }
            }
        } finally { device.setOrientationNatural(); device.unfreezeRotation() }
    }
}
