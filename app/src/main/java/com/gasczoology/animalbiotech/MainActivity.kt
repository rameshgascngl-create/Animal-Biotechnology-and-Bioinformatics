package com.gasczoology.animalbiotech

import android.os.Bundle
import com.gasczoology.animalbiotech.content.ChapterRepo
import com.gasczoology.animalbiotech.ui.LessonScreen
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
    var lang by remember { mutableStateOf(store.getLang()) }
    var lastId by remember { mutableStateOf(store.getString("last")) }
    var bests by remember { mutableStateOf(quizzes.associate { it.file to store.best(it.file) }) }

    BackHandler(enabled = screen != null) { screen = null }
    val ready = if (screen == null) readyCount(cards, store) else 0

    when (val s = screen) {
        is Screen.Reader -> {
            val chapter = remember(s.topic.id) { ChapterRepo.load(ctx, s.topic.file, s.topic.unitRoman)?.chapter }
            if (chapter != null) {
                val quiz = quizzes.firstOrNull { it.file == s.topic.file }
                LessonScreen(
                    chapter = chapter,
                    lang = lang,
                    onLang = { lang = it; store.putLang(it) },
                    zoom = zoom,
                    studied = s.topic.id in studied,
                    onStudied = { studied = store.toggle("studied", s.topic.id) },
                    saved = s.topic.id in saved,
                    onSaved = { saved = store.toggle("saved", s.topic.id) },
                    onBack = { screen = null },
                    onQuiz = quiz?.let { q -> { screen = Screen.QuizRun(q.title, q.file, q.items, 0) } },
                )
            } else {
                val quiz = quizzes.firstOrNull { it.file == s.topic.file }
                ChapterUnavailableScreen(
                    topic = s.topic, lang = lang, onBack = { screen = null },
                    onQuiz = quiz?.let { q -> { screen = Screen.QuizRun(q.title, q.file, q.items, 0) } },
                )
            }
        }
        is Screen.QuizRun -> QuizScreen(
            title = s.title,
            items = s.items,
            lang = lang,
            onLang = { lang = it; store.putLang(it) },
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
            lang = lang,
            onLang = { lang = it; store.putLang(it) },
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
