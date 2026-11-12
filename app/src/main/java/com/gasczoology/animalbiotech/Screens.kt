package com.gasczoology.animalbiotech

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.gasczoology.animalbiotech.i18n.Lang
import com.gasczoology.animalbiotech.content.Bi
import com.gasczoology.animalbiotech.ui.blocks.BiText
import androidx.compose.material3.FilterChip
import kotlinx.coroutines.delay

val Navy = Color(0xFF0F2744)
val Teal = Color(0xFF0E8A7D)
val TealPale = Color(0xFFE3F5F2)
val RosePale = Color(0xFFFCE8E8)
val Rose = Color(0xFFB3261E)

/** Shown only if a topic's chapter JSON is missing or unreadable: never a dead end, never a web view. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChapterUnavailableScreen(topic: Topic, lang: Lang, onBack: () -> Unit, onQuiz: (() -> Unit)?) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topic.title, maxLines = 1) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = pick("Back", "பின்செல்", lang)) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy, titleContentColor = Color.White, navigationIconContentColor = Color.White),
            )
        },
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner).padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                when (lang) {
                    Lang.TA -> "இந்தப் பாடத்தின் உரை ஏற்றப்படவில்லை. மீண்டும் முயலவும்."
                    Lang.EN -> "Chapter text could not be loaded."
                },
            )
            if (onQuiz != null) Button(onClick = onQuiz) { Text(if (lang == Lang.TA) "வினாடி வினா" else "Take the quiz") }
        }
    }
}

/** Native one-question-at-a-time MCQ practice with feedback, explanation and a final score. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    title: String,
    items: List<Mcq>,
    timeLimitSec: Int = 0,
    lang: Lang = Lang.EN,
    onLang: (Lang) -> Unit = {},
    onAnswer: (Mcq, Boolean) -> Unit = { _, _ -> },
    onFinish: (Int) -> Unit,
    onClose: () -> Unit,
) {
    var idx by remember { mutableIntStateOf(0) }
    var chosen by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var done by remember { mutableStateOf(false) }
    var round by remember { mutableIntStateOf(0) }   // bumps on retry
    var remaining by remember(round) { mutableIntStateOf(timeLimitSec) }
    if (timeLimitSec > 0 && !done) {
        LaunchedEffect(round, done) {
            while (remaining > 0 && !done) {
                delay(1000)
                remaining--
            }
            if (remaining <= 0 && !done) {
                done = true
                onFinish(score)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = pick("Close quiz", "வினாடி வினாவை மூடு", lang))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                ),
            )
        },
    ) { inner ->
        if (done || items.isEmpty()) {
            val pct = if (items.isEmpty()) 0 else score * 100 / items.size
            val msg = when {
                pct >= 90 -> pick("Excellent. Exam-ready on this topic.", "சிறப்பு. இந்தத் தலைப்பில் தேர்வுக்குத் தயார்.", lang)
                pct >= 70 -> pick("Good. Revise the questions you missed.", "நன்று. தவறிய வினாக்களை மீள்பார்க்கவும்.", lang)
                pct >= 50 -> pick("Fair. Re-read the chapters and try again.", "பரவாயில்லை. அத்தியாயங்களை மீண்டும் படித்து முயலவும்.", lang)
                else -> pick("Needs work. Study the module, then retake the quiz.", "இன்னும் முயற்சி தேவை. பாடத்தைப் படித்து வினாடி வினாவை மீண்டும் எழுதவும்.", lang)
            }
            Column(
                Modifier.fillMaxSize().padding(inner).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("$score / ${items.size}", style = MaterialTheme.typography.displayMedium, color = Navy, fontWeight = FontWeight.Bold)
                Text("$pct%", style = MaterialTheme.typography.titleLarge, color = Teal)
                Spacer(Modifier.height(12.dp))
                Text(msg, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(24.dp))
                Button(onClick = {
                    idx = 0; chosen = null; score = 0; done = false; round++
                }) { Text(pick("Retake quiz", "வினாடி வினாவை மீண்டும் எழுது", lang)) }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onClose) { Text(pick("Back to quizzes", "வினாடி வினாக்களுக்குத் திரும்பு", lang)) }
            }
        } else {
            val q = items[idx]
            // shuffle option order each time so the correct answer is not predictable by position or length
            val order = remember(round, idx) { items[idx].options.indices.toList().shuffled() }
            LazyColumn(
                Modifier.fillMaxSize().padding(inner),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item(key = "language") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Lang.entries.forEach { value ->
                            FilterChip(selected = lang == value, onClick = { onLang(value) },
                                label = { Text(when (value) { Lang.EN -> "English"; Lang.TA -> "தமிழ்" }) })
                        }
                    }
                }
                item(key = "head$round-$idx") {
                    val clock = if (timeLimitSec > 0) (if (lang == Lang.TA) "   ·   எஞ்சிய நேரம் %d:%02d" else "   ·   %d:%02d left").format(remaining / 60, remaining % 60) else ""
                    Text(pick("Question ${idx + 1} of ${items.size}   ·   Score $score", "வினா ${idx + 1} / ${items.size}   ·   மதிப்பெண் $score", lang) + clock, style = MaterialTheme.typography.labelLarge, color = Teal)
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (idx + (if (chosen != null) 1 else 0)).toFloat() / items.size },
                        modifier = Modifier.fillMaxWidth(),
                        color = Teal,
                    )
                    Spacer(Modifier.height(14.dp))
                    BiText(Bi(q.q, q.qTa), lang, 1f, base = MaterialTheme.typography.titleMedium, weight = FontWeight.SemiBold)
                }
                items(order.indices.toList(), key = { "o$round-$idx-$it" }) { pos ->
                    val i = order[pos]
                    val sel = chosen
                    val bg = when {
                        sel == null -> Color.White
                        i == q.answer -> TealPale
                        i == sel -> RosePale
                        else -> Color.White
                    }
                    val border = when {
                        sel == null -> Color(0xFFD5E1EC)
                        i == q.answer -> Teal
                        i == sel -> Rose
                        else -> Color(0xFFD5E1EC)
                    }
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable(enabled = sel == null) {
                            chosen = i
                            if (i == q.answer) score++
                            onAnswer(q, i == q.answer)
                        },
                        colors = CardDefaults.cardColors(containerColor = bg),
                        border = BorderStroke(1.5.dp, border),
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            Text("${'A' + pos}.  ", fontWeight = FontWeight.Bold, color = Navy)
                            BiText(Bi(q.options[i], q.optionsTa.getOrElse(i) { "" }), lang, 1f, Modifier.weight(1f))
                        }
                    }
                }
                if (chosen != null) {
                    item(key = "fb$round-$idx") {
                        val ok = chosen == q.answer
                        Card(
                            colors = CardDefaults.cardColors(containerColor = if (ok) TealPale else RosePale),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column(Modifier.padding(14.dp)) {
                                Text(pick(if (ok) "Correct." else "Not quite.", if (ok) "சரி." else "தவறான விடை.", lang), fontWeight = FontWeight.Bold, color = if (ok) Teal else Rose)
                                if (q.explanation.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    BiText(Bi(q.explanation, q.explanationTa), lang, 1f, base = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (idx == items.size - 1) {
                                    done = true
                                    onFinish(score)
                                } else {
                                    idx++
                                    chosen = null
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                        ) { Text(pick(if (idx == items.size - 1) "See score" else "Next question", if (idx == items.size - 1) "மதிப்பெண் காண்க" else "அடுத்த வினா", lang)) }
                    }
                }
            }
        }
    }
}
