package com.gasczoology.animalbiotech

import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** M7 guard: the shipped app must stay fully native. Unit tests run with the module directory as cwd. */
class NoWebViewTest {
    @Test fun noWebkitImportsInSources() {
        val offenders = File("src/main/java").walkTopDown().filter { it.extension == "kt" && it.readText().contains("android.webkit") }.map { it.name }.toList()
        assertTrue("android.webkit used in: $offenders", offenders.isEmpty())
    }
    @Test fun noHtmlModulesInAssets() {
        assertFalse(File("src/main/assets/modules").exists())
    }
}
