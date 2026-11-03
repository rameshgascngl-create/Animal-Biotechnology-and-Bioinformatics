package com.gasczoology.animalbiotech

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT)
        )
        val store = Store(applicationContext)
        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Navy,
                    secondary = Teal,
                    background = Color(0xFFF4F8FB),
                    surface = Color.White,
                    surfaceTint = Color.Transparent,
                    onPrimary = Color.White,
                ),
            ) {
                App(store)
            }
        }
    }
}

sealed interface Screen {
    data class Reader(val topic: Topic) : Screen
    data class QuizRun(val title: String, val key: String, val items: List<Mcq>, val timeLimitSec: Int = 0) : Screen
    data class Revision(val set: RevisionSet) : Screen
    data object Glossary : Screen
    data object Flash : Screen
    data object Search : Screen
}

@Composable
fun App(store: Store) {
    val ctx = LocalContext.current
    val units = remember { Repo.units(ctx) }
    val quizzes = remember { Repo.quizzes(ctx) }
    val revisions = remember { Repo.revisions(ctx) }
    val cards = remember { buildCards(revisions) }
    val headings = remember { loadHeadings(ctx) }
    var tab by remember { mutableIntStateOf(0) }
    var screen by remember { mutableStateOf<Screen?>(null) }
    var studied by remember { mutableStateOf(store.getSet("studied")) }
    var saved by remember { mutableStateOf(store.getSet("saved")) }
    var wrong by remember { mutableStateOf(store.getSet("wrong")) }
    var zoom by remember { mutableIntStateOf(store.getInt("zoom", 115)) }
    var lastId by remember { mutableStateOf(store.getString("last")) }
    var bests by remember { mutableStateOf(quizzes.associate { it.file to store.best(it.file) }) }

    BackHandler(enabled = screen != null) { screen = null }
    val ready = if (screen == null) readyCount(cards, store) else 0

    when (val s = screen) {
        is Screen.Reader -> ReaderScreen(
            topic = s.topic,
            studied = s.topic.id in studied,
            saved = s.topic.id in saved,
            zoom = zoom,
            onBack = { screen = null },
            onToggleStudied = { studied = store.toggle("studied", s.topic.id) },
            onToggleSaved = { saved = store.toggle("saved", s.topic.id) },
        )
        is Screen.QuizRun -> QuizScreen(
            title = s.title,
            items = s.items,
            timeLimitSec = s.timeLimitSec,
            onAnswer = { m, ok ->
                wrong = if (ok) store.remove("wrong", m.id) else store.add("wrong", m.id)
            },
            onFinish = { score ->
                if (s.key.isNotEmpty()) {
                    store.saveBest(s.key, score)
                    bests = quizzes.associate { it.file to store.best(it.file) }
                }
            },
            onClose = { screen = null },
        )
        is Screen.Revision -> RevisionScreen(s.set) { screen = null }
        Screen.Glossary -> GlossaryScreen(revisions) { screen = null }
        Screen.Flash -> FlashcardsScreen(cards, store) { screen = null }
        Screen.Search -> SearchScreen(units, revisions, headings, onOpen = { lastId = it.id; store.putString("last", it.id); screen = Screen.Reader(it) }, onBack = { screen = null })
        null -> HomeScaffold(
            tab = tab,
            onTab = { tab = it },
            units = units,
            quizzes = quizzes,
            studied = studied,
            saved = saved,
            bests = bests,
            revisions = revisions,
            wrongIds = wrong,
            zoom = zoom,
            onZoom = { zoom = it; store.putInt("zoom", it) },
            lastId = lastId,
            onOpen = { lastId = it.id; store.putString("last", it.id); screen = Screen.Reader(it) },
            onRevision = { screen = Screen.Revision(it) },
            onGlossary = { screen = Screen.Glossary },
            onFlash = { screen = Screen.Flash },
            onSearch = { screen = Screen.Search },
            ready = ready,
            onToggleStudied = { studied = store.toggle("studied", it) },
            onToggleSaved = { saved = store.toggle("saved", it) },
            onQuiz = { title, key, items, secs -> screen = Screen.QuizRun(title, key, items, secs) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScaffold(
    tab: Int,
    onTab: (Int) -> Unit,
    units: List<UnitInfo>,
    quizzes: List<QuizSet>,
    studied: Set<String>,
    saved: Set<String>,
    bests: Map<String, Int>,
    onOpen: (Topic) -> Unit,
    onToggleStudied: (String) -> Unit,
    onToggleSaved: (String) -> Unit,
    revisions: List<RevisionSet>,
    wrongIds: Set<String>,
    zoom: Int,
    onZoom: (Int) -> Unit,
    lastId: String,
    onRevision: (RevisionSet) -> Unit,
    onGlossary: () -> Unit,
    onFlash: () -> Unit,
    onSearch: () -> Unit,
    ready: Int,
    onQuiz: (String, String, List<Mcq>, Int) -> Unit,
) {
    val titles = listOf("Animal Biotechnology", "Revise", "Practice quizzes", "Saved topics", "About")
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titles[tab], fontWeight = FontWeight.SemiBold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy, titleContentColor = Color.White),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val items = listOf("Syllabus" to Icons.Default.List, "Revise" to Icons.Default.Edit, "Quiz" to Icons.Default.CheckCircle, "Saved" to Icons.Default.Star, "About" to Icons.Default.Info)
                items.forEachIndexed { i, (label, icon) ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { onTab(i) },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Navy,
                            indicatorColor = Teal,
                        ),
                    )
                }
            }
        },
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            when (tab) {
                0 -> SyllabusTab(units, studied, saved, lastId, onOpen, onToggleStudied, onToggleSaved)
                1 -> ReviseTab(revisions, onRevision, onGlossary, onFlash, onSearch, ready)
                2 -> QuizTab(quizzes, bests, wrongIds, onQuiz)
                3 -> SavedTab(units, studied, saved, onOpen, onToggleStudied, onToggleSaved)
                else -> AboutTab(units, quizzes, zoom, onZoom)
            }
        }
    }
}

@Composable
fun TopicCard(
    topic: Topic,
    studied: Boolean,
    saved: Boolean,
    onOpen: (Topic) -> Unit,
    onToggleStudied: (String) -> Unit,
    onToggleSaved: (String) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = if (studied) TealPale else Color.White),
    ) {
        Row(Modifier.fillMaxWidth().padding(end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = studied,
                onCheckedChange = { onToggleStudied(topic.id) },
                colors = CheckboxDefaults.colors(checkedColor = Teal),
            )
            Column(Modifier.weight(1f).clickable { onOpen(topic) }.padding(vertical = 10.dp)) {
                Text(topic.title, fontWeight = FontWeight.SemiBold, color = Navy, style = MaterialTheme.typography.bodyLarge)
                if (topic.note.isNotBlank()) {
                    Text(topic.note, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF3B4F66))
                }
            }
            IconButton(onClick = { onToggleSaved(topic.id) }) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = if (saved) "Remove from saved" else "Save topic",
                    tint = if (saved) Color(0xFFF5B301) else Color(0xFF7D8FA3),
                )
            }
        }
    }
}

@Composable
fun SyllabusTab(
    units: List<UnitInfo>,
    studied: Set<String>,
    saved: Set<String>,
    lastId: String,
    onOpen: (Topic) -> Unit,
    onToggleStudied: (String) -> Unit,
    onToggleSaved: (String) -> Unit,
) {
    var query by remember { mutableStateOf("") }
    val total = units.sumOf { it.topics.size }
    val done = units.sumOf { u -> u.topics.count { it.id in studied } }
    val q = query.trim().lowercase()

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "progress") {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.size(64.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        CircularProgressIndicator(
                            progress = { if (total == 0) 0f else done.toFloat() / total },
                            modifier = Modifier.size(56.dp),
                            color = Teal,
                            strokeWidth = 6.dp,
                        )
                    }
                    Spacer(Modifier.size(14.dp))
                    Column {
                        Text("$done of $total topics studied", fontWeight = FontWeight.Bold, color = Navy)
                        Text("Tick a topic when you have studied it.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        val last = units.flatMap { it.topics }.firstOrNull { it.id == lastId }
        if (last != null) {
            item(key = "continue") {
                QuizRow("Continue: " + last.title, "Pick up where you left off", null) { onOpen(last) }
            }
        }
        item(key = "search") {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("Search topics (e.g. CRISPR, BLAST, IPR)") },
            )
        }
        for (u in units) {
            val list = if (q.isEmpty()) u.topics else u.topics.filter {
                (it.title + " " + it.note + " " + u.title).lowercase().contains(q)
            }
            if (list.isEmpty()) continue
            item(key = "u" + u.roman) {
                Text(
                    "UNIT ${u.roman}  ·  ${u.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Teal,
                    modifier = Modifier.padding(top = 10.dp, bottom = 2.dp),
                )
            }
            items(list, key = { it.id }) { t ->
                TopicCard(t, t.id in studied, t.id in saved, onOpen, onToggleStudied, onToggleSaved)
            }
        }
    }
}

@Composable
fun QuizTab(quizzes: List<QuizSet>, bests: Map<String, Int>, wrongIds: Set<String>, onQuiz: (String, String, List<Mcq>, Int) -> Unit) {
    val all = remember(quizzes) { quizzes.flatMap { it.items } }
    val units = listOf("I", "II", "III", "IV", "V")
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        item(key = "mixhead") {
            Text("Mixed practice", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Teal)
        }
        item(key = "exam") {
            QuizRow("Exam mode: 20 questions, 20 minutes", "Timed, random from the whole syllabus", null) {
                onQuiz("Exam mode", "", all.shuffled().take(20), 20 * 60)
            }
        }
        item(key = "mixall") {
            QuizRow("Random 20 from the whole syllabus", "${all.size} questions in the bank", null) {
                onQuiz("Random 20", "", all.shuffled().take(20), 0)
            }
        }
        val mistakes = all.filter { it.id in wrongIds }
        if (mistakes.isNotEmpty()) {
            item(key = "mistakes") {
                QuizRow("Review my mistakes", "${mistakes.size} questions you got wrong; answer correctly to clear them", null) {
                    onQuiz("My mistakes", "", mistakes.shuffled(), 0)
                }
            }
        }
        items(units, key = { "mix$it" }) { u ->
            val pool = quizzes.filter { it.unitRoman == u }.flatMap { it.items }
            if (pool.isNotEmpty()) {
                QuizRow("Unit $u: random 15", "${pool.size} questions in the bank", null) {
                    onQuiz("Unit $u mixed", "", pool.shuffled().take(15), 0)
                }
            }
        }
        item(key = "modhead") {
            Text(
                "Module quizzes (10 MCQs each)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Teal,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        items(quizzes, key = { it.file }) { qs ->
            val b = bests[qs.file] ?: -1
            QuizRow(qs.title, "Unit ${qs.unitRoman}", if (b >= 0) "Best $b/${qs.items.size}" else null) {
                onQuiz(qs.title, qs.file, qs.items, 0)
            }
        }
    }
}

@Composable
fun QuizRow(title: String, sub: String, badge: String?, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = Navy)
                Text(sub, style = MaterialTheme.typography.bodyMedium, color = Color(0xFF3B4F66))
            }
            if (badge != null) Text(badge, color = Teal, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun SavedTab(
    units: List<UnitInfo>,
    studied: Set<String>,
    saved: Set<String>,
    onOpen: (Topic) -> Unit,
    onToggleStudied: (String) -> Unit,
    onToggleSaved: (String) -> Unit,
) {
    val list = units.flatMap { it.topics }.filter { it.id in saved }
    if (list.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("No saved topics yet.", fontWeight = FontWeight.Bold, color = Navy)
            Spacer(Modifier.height(6.dp))
            Text("Tap the star beside any topic to keep it here for quick revision.", textAlign = TextAlign.Center)
        }
    } else {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.id }) { t ->
                TopicCard(t, t.id in studied, true, onOpen, onToggleStudied, onToggleSaved)
            }
        }
    }
}

@Composable
fun AboutTab(units: List<UnitInfo>, quizzes: List<QuizSet>, zoom: Int, onZoom: (Int) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Animal Biotechnology", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Navy)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "An offline study companion for the five-unit Animal Biotechnology syllabus: " +
                            "${units.sumOf { it.topics.size }} syllabus topics, ${quizzes.sumOf { it.items.size }} practice MCQs, " +
                            "interactive chapters with diagrams, exam short-answer revision and glossaries. " +
                            "Everything works without an internet connection; the app does not request network access and collects no data."
                    )
                }
            }
        }
        item(key = "zoom") {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("Chapter text size", fontWeight = FontWeight.Bold, color = Navy)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        listOf(100 to "Small", 115 to "Medium", 130 to "Large", 150 to "Extra large").forEach { (v, label) ->
                            FilterChip(selected = zoom == v, onClick = { onZoom(v) }, label = { Text(label) })
                        }
                    }
                }
            }
        }
        items(units, key = { "about" + it.roman }) { u ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    Text("Unit ${u.roman}: ${u.title}", fontWeight = FontWeight.Bold, color = Navy)
                    Text("${u.topics.size} topics", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF3B4F66))
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Department of Zoology, GASC, Nagercoil", fontWeight = FontWeight.SemiBold, color = Navy, style = MaterialTheme.typography.bodyMedium)
                Text("Created by R. Ramesh", style = MaterialTheme.typography.labelSmall, color = Color(0xFF3B4F66))
                Text("Version 1.3.0", style = MaterialTheme.typography.labelSmall, color = Color(0xFF3B4F66))
            }
        }
    }
}
