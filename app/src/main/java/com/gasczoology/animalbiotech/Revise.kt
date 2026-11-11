package com.gasczoology.animalbiotech

import com.gasczoology.animalbiotech.i18n.tr
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Revise tab: list of modules that have native revision content, plus a global glossary. */
@Composable
fun ReviseTab(
    sets: List<RevisionSet>,
    onOpen: (RevisionSet) -> Unit,
    onGlossary: () -> Unit,
    onFlash: () -> Unit,
    onSearch: () -> Unit,
    ready: Int,
    lang: com.gasczoology.animalbiotech.i18n.Lang = com.gasczoology.animalbiotech.i18n.Lang.EN,
) {
    val terms = sets.sumOf { it.glossary.size }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "fc") {
            QuizRow(tr(lang, "Flashcards", "நினைவட்டைகள்"), if (ready > 0) tr(lang, "$ready cards ready today (spaced repetition)", "இன்று $ready அட்டைகள் தயார் (இடைவெளி மீள்பார்வை)") else tr(lang, "Nothing due today", "இன்று எதுவும் இல்லை"), null, onFlash)
        }
        item(key = "se") {
            QuizRow(tr(lang, "Search all content", "அனைத்திலும் தேடுக"), tr(lang, "Topics, sections, key points, answers and terms", "பாடங்கள், பகுதிகள், முக்கியக் கருத்துகள், விடைகள், சொற்கள்"), null, onSearch)
        }
        item(key = "gl") {
            QuizRow(tr(lang, "Glossary: all terms", "கலைச்சொற்கள்: அனைத்தும்"), tr(lang, "$terms terms, searchable", "$terms சொற்கள், தேடலாம்"), null, onGlossary)
        }
        item(key = "hd") {
            Text(
                tr(lang, "Key points, exam short answers and glossary by module", "பாடவாரியாக முக்கியக் கருத்துகள், குறுவிடைகள், கலைச்சொற்கள்"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Teal,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        items(sets, key = { it.file }) { rs ->
            val parts = ArrayList<String>()
            if (rs.keyPoints.isNotEmpty()) parts.add(tr(lang, "${rs.keyPoints.size} key points", "${rs.keyPoints.size} முக்கியக் கருத்துகள்"))
            if (rs.saqs.isNotEmpty()) parts.add(tr(lang, "${rs.saqs.size} short answers", "${rs.saqs.size} குறுவிடைகள்"))
            if (rs.glossary.isNotEmpty()) parts.add(tr(lang, "${rs.glossary.size} terms", "${rs.glossary.size} சொற்கள்"))
            QuizRow(rs.title, tr(lang, "Unit ${rs.unitRoman}", "அலகு ${rs.unitRoman}") + "  ·  " + parts.joinToString(", "), null) { onOpen(rs) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionScreen(rs: RevisionSet, onBack: () -> Unit) {
    val sections = buildList {
        if (rs.keyPoints.isNotEmpty()) add("Key points")
        if (rs.saqs.isNotEmpty()) add("Short answers")
        if (rs.glossary.isNotEmpty()) add("Glossary")
    }
    var sel by remember { mutableIntStateOf(0) }
    var openQ by remember { mutableStateOf<Int?>(null) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(rs.title, maxLines = 1, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy, titleContentColor = Color.White, navigationIconContentColor = Color.White,
                ),
            )
        },
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                sections.forEachIndexed { i, name ->
                    FilterChip(selected = sel == i, onClick = { sel = i }, label = { Text(name) })
                }
            }
            val current = sections.getOrNull(sel)
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (current) {
                    "Key points" -> items(rs.keyPoints.indices.toList(), key = { "k$it" }) { i ->
                        Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                            Row(Modifier.padding(12.dp)) {
                                Text("${i + 1}.  ", fontWeight = FontWeight.Bold, color = Teal)
                                Text(rs.keyPoints[i])
                            }
                        }
                    }
                    "Short answers" -> items(rs.saqs.indices.toList(), key = { "s$it" }) { i ->
                        val open = openQ == i
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (open) TealPale else Color.White),
                            modifier = Modifier.fillMaxWidth().clickable { openQ = if (open) null else i },
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(rs.saqs[i].q, fontWeight = FontWeight.SemiBold, color = Navy)
                                if (open) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(rs.saqs[i].a)
                                } else {
                                    Text("Tap to reveal the model answer", style = MaterialTheme.typography.bodySmall, color = Teal)
                                }
                            }
                        }
                    }
                    "Glossary" -> items(rs.glossary, key = { it.term + it.def.hashCode() }) { t -> TermCard(t, false) }
                    else -> {}
                }
            }
        }
    }
}

@Composable
fun TermCard(t: Term, showModule: Boolean) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(t.term, fontWeight = FontWeight.Bold, color = Navy)
            Text(t.def, style = MaterialTheme.typography.bodyMedium)
            if (showModule) Text(t.module, style = MaterialTheme.typography.labelSmall, color = Teal)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlossaryScreen(sets: List<RevisionSet>, onBack: () -> Unit) {
    val all = remember(sets) { sets.flatMap { it.glossary }.sortedBy { it.term.lowercase() } }
    var query by remember { mutableStateOf("") }
    val q = query.trim().lowercase()
    val list = if (q.isEmpty()) all else all.filter { (it.term + " " + it.def).lowercase().contains(q) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Glossary", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy, titleContentColor = Color.White, navigationIconContentColor = Color.White,
                ),
            )
        },
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                singleLine = true,
                placeholder = { Text("Search ${all.size} terms") },
            )
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(list, key = { it.module + it.term + it.def.hashCode() }) { t -> TermCard(t, true) }
            }
        }
    }
}
