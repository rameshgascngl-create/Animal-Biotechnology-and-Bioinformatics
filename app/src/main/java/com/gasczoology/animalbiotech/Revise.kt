package com.gasczoology.animalbiotech

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
) {
    val terms = sets.sumOf { it.glossary.size }
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "fc") {
            QuizRow("Flashcards", if (ready > 0) "$ready cards ready today (spaced repetition)" else "Nothing due today", null, onFlash)
        }
        item(key = "se") {
            QuizRow("Search all content", "Topics, sections, key points, answers and terms", null, onSearch)
        }
        item(key = "gl") {
            QuizRow("Glossary: all terms", "$terms terms, searchable", null, onGlossary)
        }
        item(key = "hd") {
            Text(
                "Key points, exam short answers and glossary by module",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Teal,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        items(sets, key = { it.file }) { rs ->
            val parts = ArrayList<String>()
            if (rs.keyPoints.isNotEmpty()) parts.add("${rs.keyPoints.size} key points")
            if (rs.saqs.isNotEmpty()) parts.add("${rs.saqs.size} short answers")
            if (rs.glossary.isNotEmpty()) parts.add("${rs.glossary.size} terms")
            QuizRow(rs.title, "Unit ${rs.unitRoman}  ·  " + parts.joinToString(", "), null) { onOpen(rs) }
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
