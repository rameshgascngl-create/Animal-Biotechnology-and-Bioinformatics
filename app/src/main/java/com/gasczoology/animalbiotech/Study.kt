package com.gasczoology.animalbiotech

import android.content.Context
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gasczoology.animalbiotech.content.Chapter
import com.gasczoology.animalbiotech.content.Paragraph
import com.gasczoology.animalbiotech.content.chapterAssetPath
import com.gasczoology.animalbiotech.content.parseChapter
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import com.gasczoology.animalbiotech.i18n.Lang
import com.gasczoology.animalbiotech.i18n.tr

// ---------------------------------------------------------------- Flashcards (Leitner boxes)

data class FlashCard(val id: String, val front: String, val back: String, val module: String)

private val INTERVAL_DAYS = intArrayOf(0, 1, 2, 4, 8, 16)
private const val NEW_PER_SESSION = 15

fun buildCards(sets: List<RevisionSet>, lang: Lang = Lang.EN): List<FlashCard> =
    sets.flatMap { source ->
        val shown = source.inLanguage(lang)
        source.saqs.zip(shown.saqs).map { (original, text) ->
            FlashCard("${source.file}#q${original.q.hashCode()}", text.q, text.a, shown.title)
        } + source.glossary.zip(shown.glossary).map { (original, text) ->
            val front = tr(lang, "Define: ${text.term}", "${text.term} என்பதன் பொருள் என்ன?")
            FlashCard("${source.file}#g${original.term.hashCode()}", front, text.def, shown.title)
        }
    }.distinctBy { it.id }

fun todayDay(): Long = System.currentTimeMillis() / 86_400_000L

fun dueCards(cards: List<FlashCard>, store: Store, today: Long): List<FlashCard> =
    cards.filter { store.getInt("fc_b_${it.id}", 0) > 0 && store.getLong("fc_d_${it.id}", 0L) <= today }

fun newCards(cards: List<FlashCard>, store: Store): List<FlashCard> =
    cards.filter { store.getInt("fc_b_${it.id}", 0) == 0 }

fun readyCount(cards: List<FlashCard>, store: Store): Int =
    dueCards(cards, store, todayDay()).size + minOf(NEW_PER_SESSION, newCards(cards, store).size)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(cards: List<FlashCard>, store: Store, lang: Lang, onBack: () -> Unit) {
    val today = remember { todayDay() }
    val queue = remember {
        mutableStateListOf<FlashCard>().also {
            it.addAll((dueCards(cards, store, today) + newCards(cards, store).shuffled().take(NEW_PER_SESSION)).shuffled())
        }
    }
    val total = remember { queue.size }
    var shown by remember { mutableStateOf(false) }
    var done by remember { mutableIntStateOf(0) }
    var again by remember { mutableIntStateOf(0) }

    fun rate(good: Boolean) {
        val c = queue.firstOrNull() ?: return
        val box = store.getInt("fc_b_${c.id}", 0)
        if (good) {
            val nb = minOf(box + 1, INTERVAL_DAYS.size - 1)
            store.putInt("fc_b_${c.id}", nb)
            store.putLong("fc_d_${c.id}", today + INTERVAL_DAYS[nb])
            queue.removeAt(0)
            done++
        } else {
            store.putInt("fc_b_${c.id}", 1)
            store.putLong("fc_d_${c.id}", today + 1)
            queue.removeAt(0)
            queue.add(c)
            again++
        }
        shown = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr(lang, "Flashcards", "நினைவட்டைகள்"), style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr(lang, "Back", "பின்செல்")) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy, titleContentColor = Color.White, navigationIconContentColor = Color.White,
                ),
            )
        },
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner).padding(16.dp).verticalScroll(rememberScrollState())) {
            val card = queue.firstOrNull()
            if (card == null) {
                Text(
                    if (total == 0) tr(lang, "Nothing is due today.", "இன்று படிக்க வேண்டிய அட்டைகள் இல்லை.")
                    else tr(lang, "Session complete", "பயிற்சி நிறைவடைந்தது"),
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Navy,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (total == 0) tr(lang, "Come back tomorrow.", "நாளை மீண்டும் பயிற்சி செய்யலாம்.")
                    else tr(lang, "You reviewed $done cards ($again attempts marked Again). Cards return after 1, 2, 4, 8 and 16 days as you remember them.",
                        "$done அட்டைகளைப் படித்தீர்கள். $again முறை மீண்டும் பயிற்சி செய்யத் தேர்ந்தெடுத்தீர்கள். நினைவில் உள்ள அட்டைகள் 1, 2, 4, 8, 16 நாள் இடைவெளிகளில் மீண்டும் வரும்."),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Teal)) { Text(tr(lang, "Done", "முடிந்தது")) }
            } else {
                Text(
                    tr(lang, "${done + 1} of $total · ${card.module}", "${done + 1} / $total · ${card.module}"),
                    style = MaterialTheme.typography.labelLarge, color = Color(0xFF3B4F66),
                )
                Spacer(Modifier.height(10.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = if (shown) TealPale else Color.White),
                    modifier = Modifier.fillMaxWidth().clickable { shown = true },
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text(card.front, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Navy)
                        if (shown) {
                            Spacer(Modifier.height(14.dp))
                            Text(card.back, fontSize = 17.sp, lineHeight = 26.sp)
                        } else {
                            Spacer(Modifier.height(14.dp))
                            Text(tr(lang, "Tap to show the answer", "விடையைக் காண்க"), color = Teal, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                if (shown) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { rate(false) }, modifier = Modifier.weight(1f)) { Text(tr(lang, "Again", "மீண்டும்")) }
                        Button(
                            onClick = { rate(true) }, modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Teal),
                        ) { Text(tr(lang, "Got it", "நினைவில் உள்ளது")) }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Search

data class Hit(val kind: String, val title: String, val text: String, val file: String, val module: String, val key: String, val topic: Topic? = null, val sectionId: String? = null, val aliases: List<String> = emptyList())

data class SearchHeading(val title: String, val topic: Topic, val sectionId: String, val aliases: List<String> = emptyList())

fun loadHeadings(ctx: Context): Map<String, List<String>> = try {
    val o = JSONObject(ctx.assets.open("headings.json").bufferedReader().use { it.readText() })
    o.keys().asSequence().associateWith { k ->
        val a = o.getJSONArray(k)
        (0 until a.length()).map { a.getString(it) }
    }
} catch (e: Exception) {
    emptyMap()
}

fun buildIndex(units: List<UnitInfo>, sets: List<RevisionSet>, headings: List<SearchHeading>, lang: Lang): List<Hit> {
    val out = ArrayList<Hit>()
    for (u in units) for (t in u.topics) {
        val title = tr(lang, t.title, t.titleTa.ifBlank { t.title })
        val note = tr(lang, t.note, t.noteTa.ifBlank { t.note })
        out.add(Hit(tr(lang, "Topic", "பாடம்"), title, note, t.file, "", "t:${t.id}", topic = t))
    }
    headings.forEachIndexed { i, heading ->
        val topic = heading.topic
        val name = tr(lang, topic.title, topic.titleTa.ifBlank { topic.title })
        out.add(Hit(tr(lang, "Section", "பகுதி"), heading.title, "", topic.file, name, "h:$i",
            topic = topic, sectionId = heading.sectionId, aliases = heading.aliases))
    }
    for (source in sets) {
        val rs = source.inLanguage(lang)
        rs.keyPoints.forEachIndexed { i, k -> out.add(Hit(tr(lang, "Key point", "முக்கியக் கருத்து"), k, "", rs.file, rs.title, "k:${rs.file}:$i")) }
        rs.saqs.forEachIndexed { i, s -> out.add(Hit(tr(lang, "Short answer", "குறுவிடை"), s.q, s.a, rs.file, rs.title, "s:${rs.file}:$i")) }
        rs.glossary.forEachIndexed { i, g -> out.add(Hit(tr(lang, "Term", "கலைச்சொல்"), g.term, g.def, rs.file, rs.title, "g:${rs.file}:$i")) }
    }
    return out
}

private val LEADING_DECORATION = Regex("^[\\p{S}\\p{P}\\p{Z}\\s\\uFE0F\\u200D]+")
private val SEARCH_SPACES = Regex("\\s+")

private fun searchForm(text: String) =
    text.trim().removeSurrounding("**").replaceFirst(LEADING_DECORATION, "")
        .trim().replace(SEARCH_SPACES, " ").lowercase()

private val sectionAliases = mapOf(
    "t19/s02" to ("Annotated Animal Cell — Key Organelles" to "விலங்கு செல்லின் முக்கிய நுண்ணுறுப்புகள்"),
    "t19/s03" to ("Sterilization Methods in Cell Culture" to "செல் வளர்ப்பில் கிருமிநீக்க முறைகள்"),
    "t19/s06" to ("Primary Culture → Cell Line → Continuous Line" to "முதல்நிலை வளர்ப்பு செல் வரிசை தொடர் செல் வரிசை"),
    "t19/s07" to ("Stem Cell Potency Hierarchy" to "தண்டு செல்களின் வேறுபாடடையும் திறன் நிலைகள்"),
    "t19/s08" to ("Grid Method — Organ Culture Setup Diagram" to "உறுப்பு வளர்ப்பின் வலைத்தட்டு முறை"),
    "t20/s08" to ("Industrial Ethanol Production — Process Flow" to "தொழிற்சாலை எத்தனால் உற்பத்திப் படிகள்"),
    "t24/s02" to ("Common Confusions" to "பொதுவான கருத்துக் குழப்பங்கள்"),
    "t1/s08" to ("Branches of Biotechnology — Concept Map" to "உயிரித் தொழில்நுட்பத்தின் கிளைகள் கருத்து வரைபடம்"),
    "t10/s10" to ("Sanger Sequencing Advantages and Limitations" to "சாங்கர் வரிசையாக்கத்தின் நன்மைகளும் வரம்புகளும்"),
)

internal fun chapterSearchHeadings(chapter: Chapter, topics: List<Topic>, legacyHeadings: List<String>, lang: Lang): List<SearchHeading> {
    var owner = topics.firstOrNull() ?: return emptyList()
    val topicsBySection = topics.filter { it.sectionId.isNotBlank() }.associateBy { it.sectionId }
    val legacyByTitle = legacyHeadings.associateBy(::searchForm)
    return buildList {
        for (section in chapter.sections) {
            owner = topicsBySection[section.id] ?: owner
            val title = tr(lang, section.title.en, section.title.ta)
            if (title.isBlank()) continue
            val aliases = buildList {
                sectionAliases[owner.id + "/" + section.id]?.let { (english, tamil) ->
                    add(tr(lang, english, tamil))
                }
                for (paragraph in section.blocks.filterIsInstance<Paragraph>()) {
                    val legacy = legacyByTitle[searchForm(paragraph.text.en)] ?: continue
                    val alias = tr(lang, legacy, paragraph.text.ta)
                    if (alias.isNotBlank()) add(alias)
                }
            }.distinctBy(::searchForm)
            add(SearchHeading(title, owner, section.id, aliases))
        }
    }
}

internal fun searchHits(index: List<Hit>, query: String): List<Hit> {
    val normalized = searchForm(query)
    val words = normalized.split(SEARCH_SPACES).filter { it.length > 1 }
    if (words.isEmpty()) return emptyList()
    return index.mapNotNull { hit ->
        val title = searchForm(hit.title)
        val aliases = hit.aliases.map(::searchForm)
        val topic = hit.topic
        val fields = listOf(hit.title, hit.text, hit.module, topic?.title.orEmpty(), topic?.note.orEmpty(),
            topic?.titleTa.orEmpty(), topic?.noteTa.orEmpty())
        val hay = (fields.map(::searchForm) + aliases).joinToString(" ")
        if (!words.all(hay::contains)) return@mapNotNull null
        val rank = when {
            title == normalized -> 0
            normalized in aliases -> 1
            title.contains(words[0]) -> 2
            else -> 3
        }
        hit to rank
    }.sortedBy { it.second }.take(80).map { it.first }
}

private fun loadSearchHeadings(ctx: Context, units: List<UnitInfo>, legacyHeadings: Map<String, List<String>>, lang: Lang): List<SearchHeading> =
    units.flatMap { it.topics }.groupBy { it.file }.flatMap { (file, topics) ->
        val path = chapterAssetPath(file, topics.first().unitRoman)
        val chapter = try {
            val raw = ctx.assets.open(path).bufferedReader().use { it.readText() }
            parseChapter(raw).chapter
        } catch (_: IOException) {
            null
        }
        chapter?.let { chapterSearchHeadings(it, topics, legacyHeadings[file].orEmpty(), lang) }.orEmpty()
    }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(units: List<UnitInfo>, sets: List<RevisionSet>, headings: Map<String, List<String>>, lang: Lang, onOpen: (Topic, String?) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val searchHeadings by produceState<List<SearchHeading>>(emptyList(), context, units, headings, lang) {
        value = withContext(Dispatchers.IO) { loadSearchHeadings(context, units, headings, lang) }
    }
    val index = remember(units, sets, searchHeadings, lang) { buildIndex(units, sets, searchHeadings, lang) }
    val byFile = remember(units) { units.flatMap { it.topics }.groupBy { it.file } }
    var query by remember { mutableStateOf("") }
    val hits = remember(index, query) { searchHits(index, query) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tr(lang, "Search", "தேடல்"), style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = tr(lang, "Back", "பின்செல்")) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy, titleContentColor = Color.White, navigationIconContentColor = Color.White,
                ),
            )
        },
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp), singleLine = true,
                label = { Text(tr(lang, "Search topics and study material", "பாடங்களையும் படிப்புப் பகுதிகளையும் தேடுக")) },
            )
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (query.isNotBlank()) item(key = "count") {
                    Text(
                        if (hits.isEmpty()) tr(lang, "No matches", "முடிவுகள் இல்லை")
                        else {
                            val count = "${hits.size}${if (hits.size == 80) "+" else ""}"
                            tr(lang, "$count matches", "$count முடிவுகள்")
                        },
                        style = MaterialTheme.typography.labelLarge, color = Color(0xFF3B4F66),
                    )
                }
                items(hits, key = { it.key }) { h ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth().clickable {
                            (h.topic ?: byFile[h.file]?.firstOrNull())?.let { onOpen(it, h.sectionId) }
                        },
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(h.kind.uppercase(), style = MaterialTheme.typography.labelSmall, color = Teal, fontWeight = FontWeight.Bold)
                            }
                            Text(h.title, fontWeight = FontWeight.SemiBold, color = Navy, maxLines = 3)
                            if (h.text.isNotEmpty()) Text(h.text, style = MaterialTheme.typography.bodyMedium, maxLines = 3, color = Color(0xFF26394C))
                            if (h.module.isNotEmpty()) Text(h.module, style = MaterialTheme.typography.labelSmall, color = Color(0xFF3B4F66), maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
