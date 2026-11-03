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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONObject

// ---------------------------------------------------------------- Flashcards (Leitner boxes)

data class FlashCard(val id: String, val front: String, val back: String, val module: String)

private val INTERVAL_DAYS = intArrayOf(0, 1, 2, 4, 8, 16)
private const val NEW_PER_SESSION = 15

fun buildCards(sets: List<RevisionSet>): List<FlashCard> =
    sets.flatMap { rs ->
        rs.saqs.map { FlashCard("${rs.file}#q${it.q.hashCode()}", it.q, it.a, rs.title) } +
            rs.glossary.map { FlashCard("${rs.file}#g${it.term.hashCode()}", "Define: ${it.term}", it.def, rs.title) }
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
fun FlashcardsScreen(cards: List<FlashCard>, store: Store, onBack: () -> Unit) {
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
                title = { Text("Flashcards", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
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
                    if (total == 0) "Nothing is due today." else "Session complete",
                    style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Navy,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    if (total == 0) "Come back tomorrow, or study more chapters to unlock new cards."
                    else "You reviewed $done cards ($again marked Again). Cards return after 1, 2, 4, 8 and 16 days as you remember them.",
                    style = MaterialTheme.typography.bodyLarge,
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Teal)) { Text("Done") }
            } else {
                Text(
                    "${done + 1} of ${total} · ${card.module}",
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
                            Text("Tap to show the answer", color = Teal, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                if (shown) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { rate(false) }, modifier = Modifier.weight(1f)) { Text("Again") }
                        Button(
                            onClick = { rate(true) }, modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Teal),
                        ) { Text("Got it") }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------- Search

data class Hit(val kind: String, val title: String, val text: String, val file: String, val module: String, val key: String)

fun loadHeadings(ctx: Context): Map<String, List<String>> = try {
    val o = JSONObject(ctx.assets.open("headings.json").bufferedReader().use { it.readText() })
    o.keys().asSequence().associateWith { k ->
        val a = o.getJSONArray(k)
        (0 until a.length()).map { a.getString(it) }
    }
} catch (e: Exception) {
    emptyMap()
}

fun buildIndex(units: List<UnitInfo>, sets: List<RevisionSet>, headings: Map<String, List<String>>): List<Hit> {
    val titleOf = HashMap<String, String>()
    for (u in units) for (t in u.topics) titleOf.putIfAbsent(t.file, t.title)
    val out = ArrayList<Hit>()
    for (u in units) for (t in u.topics) out.add(Hit("Topic", t.title, t.note, t.file, t.title, "t:${t.id}"))
    for ((f, hs) in headings) {
        val m = titleOf[f] ?: f
        hs.forEachIndexed { i, h -> out.add(Hit("Section", h, "", f, m, "h:$f:$i")) }
    }
    for (rs in sets) {
        rs.keyPoints.forEachIndexed { i, k -> out.add(Hit("Key point", k, "", rs.file, rs.title, "k:${rs.file}:$i")) }
        rs.saqs.forEachIndexed { i, s -> out.add(Hit("Short answer", s.q, s.a, rs.file, rs.title, "s:${rs.file}:$i")) }
        rs.glossary.forEachIndexed { i, g -> out.add(Hit("Term", g.term, g.def, rs.file, rs.title, "g:${rs.file}:$i")) }
    }
    return out
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(units: List<UnitInfo>, sets: List<RevisionSet>, headings: Map<String, List<String>>, onOpen: (Topic) -> Unit, onBack: () -> Unit) {
    val index = remember(units, sets, headings) { buildIndex(units, sets, headings) }
    val byFile = remember(units) { units.flatMap { it.topics }.groupBy { it.file } }
    var query by remember { mutableStateOf("") }
    val words = query.trim().lowercase().split(Regex("\\s+")).filter { it.length > 1 }
    val hits = if (words.isEmpty()) emptyList() else index.filter { h ->
        val hay = (h.title + " " + h.text + " " + h.module).lowercase()
        words.all { hay.contains(it) }
    }.sortedBy { if (it.title.lowercase().contains(words[0])) 0 else 1 }.take(80)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Search all content", style = MaterialTheme.typography.titleMedium) },
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
                value = query, onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp), singleLine = true,
                placeholder = { Text("e.g. PAM, HAT medium, E-value") },
            )
            LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (words.isNotEmpty()) item(key = "count") {
                    Text(
                        if (hits.isEmpty()) "No matches" else "${hits.size}${if (hits.size == 80) "+" else ""} matches",
                        style = MaterialTheme.typography.labelLarge, color = Color(0xFF3B4F66),
                    )
                }
                items(hits, key = { it.key }) { h ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth().clickable {
                            byFile[h.file]?.firstOrNull()?.let(onOpen)
                        },
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(h.kind.uppercase(), style = MaterialTheme.typography.labelSmall, color = Teal, fontWeight = FontWeight.Bold)
                            }
                            Text(h.title, fontWeight = FontWeight.SemiBold, color = Navy, maxLines = 3)
                            if (h.text.isNotEmpty()) Text(h.text, style = MaterialTheme.typography.bodyMedium, maxLines = 3, color = Color(0xFF26394C))
                            Text(h.module, style = MaterialTheme.typography.labelSmall, color = Color(0xFF3B4F66), maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}
