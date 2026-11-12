package com.gasczoology.animalbiotech

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import com.gasczoology.animalbiotech.i18n.Lang
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith

/**
 * RC language gate on the REAL activity: in English mode no visible text or content description may contain Tamil script; in Tamil mode
 * no visible text may contain a Latin word other than symbols/abbreviations (digits, capitals, up to two letters), gene/organism/unit names in the
 * allowlist below, and the institutional credit line. All offenders from all screens are collected and reported together, then the test fails.
 */
@RunWith(AndroidJUnit4::class)
class LanguageSampleTest {
    /** Leave no Tamil or quiz state behind: the other capture tests expect a fresh English install. */
    @After fun resetPrefs() {
        InstrumentationRegistry.getInstrumentation().targetContext.getSharedPreferences("animal_biotech", Context.MODE_PRIVATE).edit().clear().commit()
    }

    /** Genus-style scientific names (Pseudomonas, Bacillus, Escherichia...) stay in Latin in Tamil text, as in the textbooks. */
    private val taxon = Regex("[A-Z][a-z]{3,}(us|um|ia|ae|ii|monas|coccus|bacter|bacterium|ella|ium)")
    private val tamil = Regex("[஀-௿]")
    private val word = Regex("[A-Za-z][A-Za-z'’\\-]*")
    private val allow = setOf("english", "gasc", "nagercoil", "zoology", "department", "created", "by", "ramesh", "mcq", "mcqs", "version", "dna", "rna", "pcr", "blast",
        "crispr", "cas", "mrna", "trna", "rrna", "cdna", "ssdna", "dsdna", "poly", "oligo", "taq", "ori", "lac", "amp", "tet", "kan", "gfp", "yac", "bac", "pac", "sv", "kb", "bp", "kda", "mm", "ml", "nm", "pam", "hat", "swiss-prot", "swiss", "prot", "pdb", "ncbi", "embl", "fasta", "clustal", "rasmol")
    private fun symbolic(w: String) = w.length <= 2 || w.any { it.isDigit() } || w.all { it.isUpperCase() || !it.isLetter() } ||
        Regex("[a-z][A-Z]").containsMatchIn(w) || taxon.matches(w) || w.lowercase() in allow || w.split('-').all { it.lowercase() in allow || it.length <= 2 }

    /** Taps by screen coordinates from a fresh lookup (a held UiObject2 goes stale while the Compose tree recomposes). */
    private fun tap(device: UiDevice, sel: androidx.test.uiautomator.BySelector, wait: Long = 0): Boolean {
        repeat(4) {
            try {
                val o = (if (wait > 0) device.wait(Until.findObject(sel), wait) else device.findObject(sel)) ?: return false
                val b = o.visibleBounds; device.click(b.centerX(), b.centerY()); return true
            } catch (e: androidx.test.uiautomator.StaleObjectException) { Thread.sleep(400) }
        }
        return false
    }

    private fun visible(device: UiDevice): List<String> {
        val bo = java.io.ByteArrayOutputStream(); device.dumpWindowHierarchy(bo)
        val xml = bo.toString("UTF-8")
        // Only nodes of this app: the status bar, navigation bar and notifications belong to Android, not to the app.
        val pkg = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        return Regex("<node [^>]*>").findAll(xml).map { it.value }.filter { it.contains("package=\"$pkg\"") }.flatMap { n ->
            Regex("(?:text|content-desc)=\"([^\"]*)\"").findAll(n).map { it.groupValues[1].replace("&#10;", " ").replace("&amp;", "&") }
        }.filter { it.isNotBlank() }.toList()
    }

    @Test fun screensAreEnglishOnlyOrTamilOnly() {
        val inst = InstrumentationRegistry.getInstrumentation(); val device = UiDevice.getInstance(inst); val ctx = inst.targetContext
        val offenders = ArrayList<String>(); var screens = 0
        for (lang in Lang.entries) {
            ctx.getSharedPreferences("animal_biotech", Context.MODE_PRIVATE).edit().clear().putString("lang", lang.name).commit()
            fun check(name: String) {
                device.waitForIdle(); Thread.sleep(700); screens++
                for (t in visible(device)) {
                    if (t == "தமிழ்" || t == "English") continue   // the language selector shows each language in its own script, by design
                    if (lang == Lang.EN && tamil.containsMatchIn(t)) offenders.add("EN/$name has Tamil: ${t.take(70)}")
                    if (lang == Lang.TA) word.findAll(t).filter { !symbolic(it.value) }.forEach { offenders.add("TA/$name Latin word '${it.value}' in: ${t.take(70)}") }
                }
            }
            ActivityScenario.launch(MainActivity::class.java).use {
                val tab = if (lang == Lang.TA) arrayOf("பாடங்கள்", "மீளாய்வு", "வினாக்கள்", "சேமிப்பு", "பற்றி") else arrayOf("Syllabus", "Revise", "Quiz", "Saved", "About")
                assertTrue("home did not appear", device.wait(Until.hasObject(By.text(tab[0])), 20000))
                for ((i, label) in tab.withIndex()) {
                    tap(device, By.text(label)); check("tab-${tab[i]}")
                }
                // Quiz: exam mode, answer every question, then look at the result screen.
                tap(device, By.text(tab[2])); device.waitForIdle()
                val exam = if (lang == Lang.TA) "தேர்வு முறை" else "Exam mode"
                assertTrue("exam-mode row not found ($lang)", tap(device, By.textStartsWith(exam), 8000))
                check("quiz-question")
                val next = if (lang == Lang.TA) "அடுத்த வினா" else "Next question"; val score = if (lang == Lang.TA) "மதிப்பெண் காண்க" else "See score"
                var n = 0
                while (n < 25 && !device.hasObject(By.text(if (lang == Lang.TA) "வினாடி வினாவை மீண்டும் எழுது" else "Retake quiz"))) {
                    tap(device, By.textStartsWith("A.")); device.waitForIdle()
                    if (n == 0) check("quiz-feedback")
                    if (!tap(device, By.text(score))) tap(device, By.text(next)); device.waitForIdle(); n++
                }
                check("quiz-result")
            }
        }
        android.util.Log.e("CITEST", "LANGUAGE sample: $screens screens, ${offenders.size} offenders")
        offenders.distinct().take(250).forEach { android.util.Log.e("CITEST", "LANGUAGE offender: $it") }
        assertTrue("language rule violated on sampled screens (${offenders.size}):\n" + offenders.distinct().take(25).joinToString("\n"), offenders.isEmpty())
    }
}
