package com.gasczoology.animalbiotech

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gasczoology.animalbiotech.i18n.Lang
import com.gasczoology.animalbiotech.i18n.tr
import com.gasczoology.animalbiotech.ui.blocks.TamilFont
import com.gasczoology.animalbiotech.ui.theme.UnitAccents

private val Ink = Color(0xFF1B2B3F)
private val Soft = Color(0xFF3B4F66)

/** Text in the active language with the bundled Tamil font when needed. */
@Composable
private fun T(lang: Lang, en: String, ta: String, modifier: Modifier = Modifier, size: Int = 14, weight: FontWeight? = null, color: Color = Ink, align: TextAlign? = null) {
    Text(tr(lang, en, ta), modifier, fontSize = size.sp, fontWeight = weight, color = color, textAlign = align, fontFamily = if (lang == Lang.TA) TamilFont else null,
        lineHeight = (size * (if (lang == Lang.TA) 1.4f else 1.3f)).sp)
}

/** English | தமிழ் switch, used on every tab. Choosing one language shows only that language everywhere. */
@Composable
fun LangToggle(lang: Lang, onLang: (Lang) -> Unit) {
    Row(Modifier.padding(end = 10.dp).clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.16f)).padding(3.dp)) {
        listOf(Lang.EN to "English", Lang.TA to "தமிழ்").forEach { (l, label) ->
            val on = lang == l
            Text(
                label,
                Modifier.clip(RoundedCornerShape(17.dp)).background(if (on) Color.White else Color.Transparent)
                    .clickable { onLang(l) }.padding(horizontal = 12.dp, vertical = 6.dp),
                color = if (on) Navy else Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp,
                fontFamily = if (l == Lang.TA) TamilFont else null,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScaffold(
    tab: Int,
    onTab: (Int) -> Unit,
    lang: Lang,
    onLang: (Lang) -> Unit,
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
    val titles = listOf("" to "", "Revise" to "மீள்பார்வை", "Practice quizzes" to "பயிற்சி வினாடி வினாக்கள்", "Saved topics" to "சேமித்த பாடங்கள்", "About" to "பற்றி")
    val titleTa = remember(units) { units.flatMap { it.topics }.associate { it.file to it.titleTa } }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { if (tab > 0) T(lang, titles[tab].first, titles[tab].second, size = 20, weight = FontWeight.SemiBold, color = Color.White) },
                actions = { LangToggle(lang, onLang) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy, titleContentColor = Color.White),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                val items = listOf(
                    Triple("Syllabus", "பாடத்திட்டம்", Icons.Default.List), Triple("Revise", "மீள்பார்வை", Icons.Default.Edit),
                    Triple("Quiz", "வினாடி வினா", Icons.Default.CheckCircle), Triple("Saved", "சேமித்தவை", Icons.Default.Star),
                    Triple("About", "பற்றி", Icons.Default.Info),
                )
                items.forEachIndexed { i, (en, ta, icon) ->
                    NavigationBarItem(
                        selected = tab == i, onClick = { onTab(i) },
                        icon = { Icon(icon, contentDescription = tr(lang, en, ta)) },
                        label = { Text(tr(lang, en, ta), fontFamily = if (lang == Lang.TA) TamilFont else null, maxLines = 1, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = Color.White, selectedTextColor = Navy, indicatorColor = Teal),
                    )
                }
            }
        },
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            when (tab) {
                0 -> HomeTab(lang, units, quizzes, revisions, studied, saved, lastId, ready, onOpen, onToggleStudied, onToggleSaved, onTab, onFlash, onGlossary, onSearch)
                1 -> ReviseTab(revisions, onRevision, onGlossary, onFlash, onSearch, ready, lang)
                2 -> QuizTab(lang, quizzes, bests, wrongIds, titleTa, onQuiz)
                3 -> SavedTab(lang, units, studied, saved, onOpen, onToggleStudied, onToggleSaved)
                else -> AboutTab(lang, units, quizzes, zoom, onZoom)
            }
        }
    }
}

@Composable
fun TopicRow(lang: Lang, topic: Topic, studied: Boolean, saved: Boolean, accent: Color, onOpen: (Topic) -> Unit, onToggleStudied: (String) -> Unit, onToggleSaved: (String) -> Unit) {
    val title = if (lang == Lang.TA && topic.titleTa.isNotBlank()) topic.titleTa else topic.title
    Row(Modifier.fillMaxWidth().padding(end = 2.dp), verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = studied, onCheckedChange = { onToggleStudied(topic.id) }, colors = CheckboxDefaults.colors(checkedColor = accent))
        Column(Modifier.weight(1f).clickable { onOpen(topic) }.padding(vertical = 8.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = Navy, fontSize = 15.sp, fontFamily = if (lang == Lang.TA) TamilFont else null,
                lineHeight = if (lang == Lang.TA) 21.sp else 20.sp)
            if (lang == Lang.EN && topic.note.isNotBlank()) Text(topic.note, fontSize = 13.sp, color = Soft)
        }
        IconButton(onClick = { onToggleSaved(topic.id) }) {
            Icon(Icons.Default.Star, contentDescription = tr(lang, if (saved) "Remove from saved" else "Save topic", if (saved) "சேமிப்பிலிருந்து நீக்கு" else "பாடத்தைச் சேமி"),
                tint = if (saved) Color(0xFFF5B301) else Color(0xFFB4C0CE))
        }
    }
}

@Composable
private fun Tile(lang: Lang, emoji: String, en: String, ta: String, pale: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier.clip(RoundedCornerShape(16.dp)).background(pale).clickable(onClick = onClick).padding(vertical = 12.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(emoji, fontSize = 24.sp)
        T(lang, en, ta, size = 12, weight = FontWeight.SemiBold, color = Navy, align = TextAlign.Center)
    }
}

@Composable
fun HomeTab(
    lang: Lang, units: List<UnitInfo>, quizzes: List<QuizSet>, revisions: List<RevisionSet>,
    studied: Set<String>, saved: Set<String>, lastId: String, ready: Int,
    onOpen: (Topic) -> Unit, onToggleStudied: (String) -> Unit, onToggleSaved: (String) -> Unit,
    onTab: (Int) -> Unit, onFlash: () -> Unit, onGlossary: () -> Unit, onSearch: () -> Unit,
) {
    val total = units.sumOf { it.topics.size }
    val done = units.sumOf { u -> u.topics.count { it.id in studied } }
    val questions = quizzes.sumOf { it.items.size }
    val last = units.flatMap { it.topics }.firstOrNull { it.id == lastId }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        item(key = "hero") {
            Box(Modifier.fillMaxWidth().background(Brush.verticalGradient(listOf(Navy, Color(0xFF14506B)))).padding(start = 20.dp, end = 20.dp, bottom = 22.dp, top = 4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        T(lang, "Animal Biotechnology", "விலங்கு உயிரித் தொழில்நுட்பவியல்", size = 26, weight = FontWeight.Bold, color = Color.White)
                        T(lang, "$total chapters · $questions questions · 5 units", "$total பாடங்கள் · $questions வினாக்கள் · 5 அலகுகள்", size = 13, color = Color.White.copy(alpha = 0.85f))
                        T(lang, "Department of Zoology, GASC, Nagercoil", "விலங்கியல் துறை, அரசு கலை அறிவியல் கல்லூரி, நாகர்கோவில்", size = 11, color = Color.White.copy(alpha = 0.65f))
                    }
                    Spacer(Modifier.width(12.dp))
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                        CircularProgressIndicator(progress = { if (total == 0) 0f else done.toFloat() / total }, modifier = Modifier.fillMaxSize(),
                            color = Color(0xFF5EE6D5), trackColor = Color.White.copy(alpha = 0.2f), strokeWidth = 7.dp)
                        Text("$done/$total", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }
        }
        if (last != null) item(key = "continue") {
            Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp).clickable { onOpen(last) }, colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFD5DEEA))) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("📖", fontSize = 28.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        T(lang, "Continue reading", "தொடர்ந்து படிக்க", size = 12, weight = FontWeight.SemiBold, color = Teal)
                        Text(if (lang == Lang.TA && last.titleTa.isNotBlank()) last.titleTa else last.title, fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp,
                            fontFamily = if (lang == Lang.TA) TamilFont else null, maxLines = 2)
                    }
                    Text("›", fontSize = 28.sp, color = Teal)
                }
            }
        }
        item(key = "tiles") {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Tile(lang, "📝", "Quiz", "வினாடி வினா", UnitAccents[0].second, Modifier.weight(1f)) { onTab(2) }
                Tile(lang, "📖", "Revise", "மீள்பார்வை", UnitAccents[1].second, Modifier.weight(1f)) { onTab(1) }
                Tile(lang, "🗂", "Flashcards", "நினைவட்டைகள்", UnitAccents[2].second, Modifier.weight(1f)) { onFlash() }
                Tile(lang, "🔤", "Glossary", "கலைச்சொற்கள்", UnitAccents[3].second, Modifier.weight(1f)) { onGlossary() }
                Tile(lang, "🔍", "Search", "தேடல்", UnitAccents[4].second, Modifier.weight(1f)) { onSearch() }
            }
        }
        units.forEachIndexed { idx, u ->
            item(key = "u" + u.roman) {
                val (deep, pale) = UnitAccents[idx % UnitAccents.size]
                var open by rememberSaveable(u.roman) { mutableStateOf(u.roman == "I" || u.topics.any { it.id == lastId }) }
                val k = u.topics.count { it.id in studied }
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp), colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, deep.copy(alpha = 0.25f)), elevation = CardDefaults.cardElevation(2.dp)) {
                    Column {
                        Row(Modifier.fillMaxWidth().background(pale).clickable { open = !open }.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(42.dp).clip(CircleShape).background(deep), contentAlignment = Alignment.Center) {
                                Text(u.roman, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                T(lang, "UNIT ${u.roman}", "அலகு ${u.roman}", size = 11, weight = FontWeight.Bold, color = deep)
                                Text(if (lang == Lang.TA && u.titleTa.isNotBlank()) u.titleTa else u.title, fontWeight = FontWeight.Bold, color = Navy, fontSize = 16.sp,
                                    fontFamily = if (lang == Lang.TA) TamilFont else null)
                                T(lang, "$k of ${u.topics.size} studied", "$k / ${u.topics.size} படிக்கப்பட்டன", size = 12, color = Soft)
                            }
                            Text(if (open) "▾" else "▸", fontSize = 20.sp, color = deep)
                        }
                        LinearProgressIndicator(progress = { if (u.topics.isEmpty()) 0f else k.toFloat() / u.topics.size }, Modifier.fillMaxWidth().height(3.dp), color = deep, trackColor = pale)
                        if (open) u.topics.forEach { t ->
                            TopicRow(lang, t, t.id in studied, t.id in saved, deep, onOpen, onToggleStudied, onToggleSaved)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuizRow(title: String, sub: String, badge: String?, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick), colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE1E8F0))) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = Navy, fontFamily = TamilFont)
                Text(sub, style = MaterialTheme.typography.bodyMedium, color = Soft, fontFamily = TamilFont)
            }
            if (badge != null) Text(badge, color = Teal, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge, fontFamily = TamilFont)
        }
    }
}

@Composable
private fun SectionLabel(lang: Lang, en: String, ta: String) =
    T(lang, en, ta, Modifier.padding(top = 10.dp), size = 14, weight = FontWeight.Bold, color = Teal)

@Composable
fun QuizTab(lang: Lang, quizzes: List<QuizSet>, bests: Map<String, Int>, wrongIds: Set<String>, titleTa: Map<String, String>, onQuiz: (String, String, List<Mcq>, Int) -> Unit) {
    val all = remember(quizzes) { quizzes.flatMap { it.items } }
    val unitList = listOf("I", "II", "III", "IV", "V")
    val inBank = tr(lang, "${all.size} questions in the bank", "வங்கியில் ${all.size} வினாக்கள்")
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        item(key = "mixhead") { SectionLabel(lang, "Mixed practice", "கலவைப் பயிற்சி") }
        item(key = "exam") {
            QuizRow(tr(lang, "Exam mode: 20 questions, 20 minutes", "தேர்வு முறை: 20 வினாக்கள், 20 நிமிடங்கள்"),
                tr(lang, "Timed, random from the whole syllabus", "நேரக்கட்டுப்பாட்டுடன், முழுப் பாடத்திட்டத்திலிருந்து"), null) { onQuiz("Exam mode", "", all.shuffled().take(20), 20 * 60) }
        }
        item(key = "mixall") {
            QuizRow(tr(lang, "Random 20 from the whole syllabus", "முழுப் பாடத்திட்டத்திலிருந்து 20 வினாக்கள்"), inBank, null) { onQuiz("Random 20", "", all.shuffled().take(20), 0) }
        }
        val mistakes = all.filter { it.id in wrongIds }
        if (mistakes.isNotEmpty()) item(key = "mistakes") {
            QuizRow(tr(lang, "Review my mistakes", "என் தவறுகளை மீள்பார்க்க"),
                tr(lang, "${mistakes.size} questions you got wrong; answer correctly to clear them", "${mistakes.size} வினாக்களில் தவறினீர்கள்; சரியாகப் பதிலளித்தால் நீங்கும்"), null) { onQuiz("My mistakes", "", mistakes.shuffled(), 0) }
        }
        items(unitList, key = { "mix$it" }) { u ->
            val pool = quizzes.filter { it.unitRoman == u }.flatMap { it.items }
            if (pool.isNotEmpty()) QuizRow(tr(lang, "Unit $u: random 15", "அலகு $u: 15 வினாக்கள்"),
                tr(lang, "${pool.size} questions in the bank", "வங்கியில் ${pool.size} வினாக்கள்"), null) { onQuiz("Unit $u mixed", "", pool.shuffled().take(15), 0) }
        }
        item(key = "modhead") { SectionLabel(lang, "Module quizzes (10 MCQs each)", "பாட வினாடி வினாக்கள் (ஒவ்வொன்றிலும் 10)") }
        items(quizzes, key = { it.file }) { qs ->
            val b = bests[qs.file] ?: -1
            val title = if (lang == Lang.TA) titleTa[qs.file].orEmpty().ifBlank { qs.title } else qs.title
            QuizRow(title, tr(lang, "Unit ${qs.unitRoman}", "அலகு ${qs.unitRoman}"), if (b >= 0) tr(lang, "Best $b/${qs.items.size}", "சிறந்தது $b/${qs.items.size}") else null) {
                onQuiz(qs.title, qs.file, qs.items, 0)
            }
        }
    }
}

@Composable
fun SavedTab(lang: Lang, units: List<UnitInfo>, studied: Set<String>, saved: Set<String>, onOpen: (Topic) -> Unit, onToggleStudied: (String) -> Unit, onToggleSaved: (String) -> Unit) {
    val list = units.flatMap { it.topics }.filter { it.id in saved }
    if (list.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text("⭐", fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            T(lang, "No saved topics yet.", "சேமித்த பாடங்கள் இன்னும் இல்லை.", weight = FontWeight.Bold, color = Navy, size = 16)
            Spacer(Modifier.height(6.dp))
            T(lang, "Tap the star beside any topic to keep it here for quick revision.", "விரைவு மீள்பார்வைக்காக, ஒரு பாடத்தின் அருகிலுள்ள நட்சத்திரத்தைத் தொடுங்கள்.", align = TextAlign.Center)
        }
    } else {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list, key = { it.id }) { t ->
                Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Color(0xFFE1E8F0))) {
                    TopicRow(lang, t, t.id in studied, true, Teal, onOpen, onToggleStudied, onToggleSaved)
                }
            }
        }
    }
}

@Composable
fun AboutTab(lang: Lang, units: List<UnitInfo>, quizzes: List<QuizSet>, zoom: Int, onZoom: (Int) -> Unit) {
    val ctx = LocalContext.current
    val version = remember { runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName }.getOrNull().orEmpty() }
    val n = units.sumOf { it.topics.size }; val m = quizzes.sumOf { it.items.size }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    T(lang, "Animal Biotechnology", "விலங்கு உயிரித் தொழில்நுட்பவியல்", size = 20, weight = FontWeight.Bold, color = Navy)
                    T(lang,
                        "An offline study companion for the five-unit Animal Biotechnology syllabus: $n chapters, $m practice MCQs, illustrated chapters, exam short-answer revision and glossaries. Everything works without an internet connection; the app does not request network access and collects no data.",
                        "ஐந்து அலகுகள் கொண்ட விலங்கு உயிரித் தொழில்நுட்பவியல் பாடத்திட்டத்துக்கான இணையமில்லாத் துணைக் கருவி: $n பாடங்கள், $m பயிற்சி வினாக்கள், படங்களுடன் கூடிய பாடங்கள், தேர்வுக்கான குறுவிடை மீள்பார்வை, கலைச்சொற்கள். அனைத்தும் இணைய இணைப்பின்றி இயங்கும்; செயலி இணைய அனுமதி கோராது, தரவு எதையும் சேகரிக்காது.")
                }
            }
        }
        item(key = "zoom") {
            Card(colors = CardDefaults.cardColors(containerColor = Color.White), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(14.dp)) {
                    T(lang, "Chapter text size", "பாட எழுத்தின் அளவு", weight = FontWeight.Bold, color = Navy, size = 15)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                        listOf(Triple(100, "Small", "சிறியது"), Triple(115, "Medium", "நடுத்தரம்"), Triple(130, "Large", "பெரியது"), Triple(150, "Extra large", "மிகப் பெரியது")).forEach { (v, en, ta) ->
                            FilterChip(selected = zoom == v, onClick = { onZoom(v) }, label = { Text(tr(lang, en, ta), fontFamily = if (lang == Lang.TA) TamilFont else null, fontSize = 12.sp) })
                        }
                    }
                }
            }
        }
        item {
            Column(Modifier.fillMaxWidth().padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Department of Zoology, GASC, Nagercoil", fontWeight = FontWeight.SemiBold, color = Navy, style = MaterialTheme.typography.bodyMedium)
                Text("Created by R. Ramesh", style = MaterialTheme.typography.labelSmall, color = Soft)
                Text("Version $version", style = MaterialTheme.typography.labelSmall, color = Soft)
            }
        }
    }
}
