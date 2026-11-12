package com.gasczoology.animalbiotech.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gasczoology.animalbiotech.content.Block
import com.gasczoology.animalbiotech.content.Chapter
import com.gasczoology.animalbiotech.i18n.Lang
import com.gasczoology.animalbiotech.i18n.shown
import com.gasczoology.animalbiotech.i18n.tr
import com.gasczoology.animalbiotech.ui.blocks.BiText
import com.gasczoology.animalbiotech.ui.blocks.BlockView
import com.gasczoology.animalbiotech.ui.blocks.TamilFont
import com.gasczoology.animalbiotech.ui.blocks.ProvideFigureViewer
import com.gasczoology.animalbiotech.ui.theme.LocalBodyFont
import com.gasczoology.animalbiotech.ui.theme.LocalReaderPalette
import com.gasczoology.animalbiotech.ui.theme.ReaderTheme
import com.gasczoology.animalbiotech.ui.theme.readerPalette
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

private sealed interface Entry {
    data class Header(val si: Int) : Entry
    data class Body(val si: Int, val block: Block, val key: String) : Entry
}

private fun entriesOf(c: Chapter): List<Entry> = buildList {
    c.sections.forEachIndexed { si, s ->
        add(Entry.Header(si))
        s.blocks.forEachIndexed { bi, b -> add(Entry.Body(si, b, "b-$si-${s.id}-$bi")) }
    }
}

private fun first(b: com.gasczoology.animalbiotech.content.Bi, lang: Lang) =
    b.shown(lang).firstOrNull { it.text.isNotEmpty() }?.text ?: b.en

/**
 * Book-style chapter reader: title page, flowing sections, tap-to-toggle bars, contents list,
 * reading themes, resume position and previous/next chapter. [zoom] is the text size in percent.
 */
@OptIn(FlowPreview::class, androidx.compose.ui.ExperimentalComposeUiApi::class)
@Composable
fun LessonScreen(
    chapter: Chapter,
    lang: Lang,
    onLang: (Lang) -> Unit,
    zoom: Int,
    studied: Boolean,
    onStudied: () -> Unit,
    saved: Boolean,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    onQuiz: (() -> Unit)?,
    onZoom: (Int) -> Unit = {},
    theme: ReaderTheme = ReaderTheme.PAPER,
    onTheme: (ReaderTheme) -> Unit = {},
    startIndex: Int = 0,
    onPosition: (Int) -> Unit = {},
    onPrev: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    eyebrow: String = "",
) {
    val pal = readerPalette(theme)
    val scale = zoom / 100f
    val entries = remember(chapter) { entriesOf(chapter) }
    val total = entries.size + 1
    val state = rememberLazyListState(initialFirstVisibleItemIndex = startIndex.coerceIn(0, total - 1))
    val scope = rememberCoroutineScope()
    var bars by remember { mutableStateOf(true) }
    var showContents by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    val title = first(chapter.title, lang)
    val tamil = if (lang == Lang.TA) TamilFont else null
    val progress by remember(state) {
        derivedStateOf {
            val last = state.layoutInfo.totalItemsCount.coerceAtLeast(1)
            ((state.firstVisibleItemIndex + 1).toFloat() / last).coerceIn(0f, 1f)
        }
    }
    LaunchedEffect(state) {
        snapshotFlow { state.firstVisibleItemIndex }.debounce(400).collect { onPosition(it) }
    }
    val body = if (lang == Lang.EN) FontFamily.Serif else null

    CompositionLocalProvider(LocalReaderPalette provides pal, LocalBodyFont provides body) {
      ProvideFigureViewer {
        Column(Modifier.fillMaxSize().background(pal.bg).statusBarsPadding().navigationBarsPadding().semantics { testTagsAsResourceId = true }) {
            if (bars) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr(lang, "Back", "பின்செல்"), tint = pal.heading) }
                    Text(
                        title, Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall, color = pal.muted, fontFamily = tamil,
                    )
                    IconButton(onClick = onSaved) {
                        Icon(Icons.Default.Star, contentDescription = tr(lang, "Save topic", "தலைப்பைச் சேமி"), tint = if (saved) Color(0xFFF5B301) else pal.muted)
                    }
                    IconButton(onClick = onStudied) {
                        Icon(Icons.Default.Check, contentDescription = tr(lang, "Mark as studied", "படித்ததாகக் குறி"), tint = if (studied) Color(0xFF1F9D8F) else pal.muted)
                    }
                    TextButton(onClick = { showSettings = true }) { Text("Aa", color = pal.heading, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                }
            }
            LinearProgressIndicator(
                progress = { progress }, modifier = Modifier.fillMaxWidth().height(2.dp),
                color = Color(0xFF2F6FB5), trackColor = pal.bar,
            )
            LazyColumn(
                state = state,
                modifier = Modifier.weight(1f).fillMaxWidth().clickable(
                    interactionSource = remember { MutableInteractionSource() }, indication = null,
                ) { bars = !bars },
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item(key = "title") { TitlePage(chapter, lang, scale, eyebrow, onLang) }
                entries.forEach { e ->
                    when (e) {
                        is Entry.Header -> item(key = "sh-${e.si}") {
                            BiText(
                                chapter.sections[e.si].title, lang, scale, Modifier.padding(top = 14.dp),
                                MaterialTheme.typography.titleLarge, FontWeight.Bold, pal.heading,
                            )
                        }
                        is Entry.Body -> item(key = e.key) { BlockView(e.block, lang, scale) }
                    }
                }
                item(key = "end") {
                    Column(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (onQuiz != null) {
                            Button(onClick = onQuiz, modifier = Modifier.fillMaxWidth()) {
                                Text(tr(lang, "Practise MCQs", "பயிற்சி வினாக்கள்"), fontFamily = TamilFont)
                            }
                        }
                        if (onNext != null) {
                            TextButton(onClick = onNext, modifier = Modifier.fillMaxWidth()) {
                                Text(tr(lang, "Next chapter", "அடுத்த அத்தியாயம்"), fontFamily = TamilFont, color = pal.heading)
                            }
                        }
                        Text("Department of Zoology, GASC, Nagercoil", fontWeight = FontWeight.SemiBold, color = pal.heading, style = MaterialTheme.typography.bodyMedium)
                        Text("Created by R. Ramesh", style = MaterialTheme.typography.labelSmall, color = pal.muted)
                    }
                }
            }
            if (bars) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { onPrev?.invoke() }, enabled = onPrev != null) {
                        Text("‹", fontSize = 26.sp, color = if (onPrev != null) pal.heading else pal.muted)
                    }
                    TextButton(onClick = { showContents = true }, modifier = Modifier.weight(1f)) {
                        Text(
                            tr(lang, "Contents", "பொருளடக்கம்") + " · ${(progress * 100).toInt()}%",
                            fontFamily = TamilFont, color = pal.heading, textAlign = TextAlign.Center, maxLines = 1,
                        )
                    }
                    TextButton(onClick = { onNext?.invoke() }, enabled = onNext != null) {
                        Text("›", fontSize = 26.sp, color = if (onNext != null) pal.heading else pal.muted)
                    }
                }
            }
        }
      }
    }

    if (showContents) {
        val starts = remember(chapter) { sectionStartIndices(chapter) }
        // entries index = starts[si] - 1 + 1 (title page occupies LazyColumn item 0)
        AlertDialog(
            onDismissRequest = { showContents = false },
            confirmButton = { TextButton(onClick = { showContents = false }) { Text(tr(lang, "Close", "மூடு"), fontFamily = TamilFont) } },
            title = { Text(tr(lang, "Contents", "பொருளடக்கம்"), fontFamily = TamilFont) },
            text = {
                Column {
                    chapter.sections.forEachIndexed { si, s ->
                        Text(
                            first(s.title, lang), fontFamily = tamil,
                            modifier = Modifier.fillMaxWidth().clickable {
                                showContents = false
                                scope.launch { state.scrollToItem(starts[si]) }
                            }.padding(vertical = 10.dp),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            },
        )
    }
    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            confirmButton = { TextButton(onClick = { showSettings = false }) { Text(tr(lang, "Done", "முடிந்தது"), fontFamily = TamilFont) } },
            title = { Text(tr(lang, "Reading", "வாசிப்பு"), fontFamily = TamilFont) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = lang == Lang.EN, onClick = { onLang(Lang.EN) }, label = { Text("English") })
                        FilterChip(selected = lang == Lang.TA, onClick = { onLang(Lang.TA) }, label = { Text("தமிழ்", fontFamily = TamilFont) })
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ReaderTheme.entries.forEach { t ->
                            FilterChip(
                                selected = theme == t, onClick = { onTheme(t) },
                                label = { Text(when (t) {
                                    ReaderTheme.PAPER -> tr(lang, "Paper", "காகிதம்")
                                    ReaderTheme.SEPIA -> tr(lang, "Sepia", "செபியா")
                                    ReaderTheme.NIGHT -> tr(lang, "Night", "இரவு")
                                }, fontFamily = TamilFont) },
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(100, 115, 130, 150).forEach { z ->
                            FilterChip(selected = zoom == z, onClick = { onZoom(z) }, label = { Text("$z%") })
                        }
                    }
                }
            },
        )
    }
}

@Composable
private fun TitlePage(chapter: Chapter, lang: Lang, scale: Float, eyebrow: String, onLang: (Lang) -> Unit) {
    val pal = LocalReaderPalette.current
    Column(Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (eyebrow.isNotEmpty()) {
            Text(eyebrow.uppercase(), style = MaterialTheme.typography.labelMedium, color = Color(0xFF2F6FB5), fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
        }
        BiText(chapter.title, lang, scale, base = MaterialTheme.typography.headlineSmall, weight = FontWeight.Bold, color = pal.heading)
        Box(Modifier.fillMaxWidth(0.18f).height(3.dp).background(Color(0xFF2F6FB5)))
        Text(
            tr(lang, "${readingMinutes(chapter)} min read · ${chapter.sections.size} sections",
                "${readingMinutes(chapter)} நிமிட வாசிப்பு · ${chapter.sections.size} பிரிவுகள்"),
            style = MaterialTheme.typography.bodySmall, color = pal.muted, fontFamily = TamilFont,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = lang == Lang.EN, onClick = { onLang(Lang.EN) }, label = { Text("English") })
            FilterChip(selected = lang == Lang.TA, onClick = { onLang(Lang.TA) }, label = { Text("தமிழ்", fontFamily = TamilFont) })
        }
    }
}
