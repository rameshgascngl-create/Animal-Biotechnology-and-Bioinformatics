package com.gasczoology.animalbiotech

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.viewinterop.AndroidView

val Navy = Color(0xFF0F2744)
val Teal = Color(0xFF0E8A7D)
val TealPale = Color(0xFFE3F5F2)
val RosePale = Color(0xFFFCE8E8)
val Rose = Color(0xFFB3261E)

/** Hosts one offline HTML module from assets/modules (optionally at a chapter via #pN). */
@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ReaderScreen(
    topic: Topic,
    studied: Boolean,
    saved: Boolean,
    onBack: () -> Unit,
    onToggleStudied: () -> Unit,
    onToggleSaved: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topic.title, maxLines = 1, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onToggleSaved) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Save topic",
                            tint = if (saved) Color(0xFFF5B301) else Color.White.copy(alpha = 0.55f),
                        )
                    }
                    IconButton(onClick = onToggleStudied) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Mark as studied",
                            tint = if (studied) Color(0xFF5EE6D5) else Color.White.copy(alpha = 0.55f),
                        )
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
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(inner),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.allowFileAccess = false      // android_asset still works
                    settings.allowContentAccess = false
                    settings.builtInZoomControls = false
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            // keep the reader strictly inside the bundled content
                            return !request.url.toString().startsWith("file:///android_asset/")
                        }
                    }
                    loadUrl(topic.url)
                }
            },
            onRelease = { it.destroy() },
        )
    }
}

/** Native one-question-at-a-time MCQ practice with feedback, explanation and a final score. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    title: String,
    items: List<Mcq>,
    onFinish: (Int) -> Unit,
    onClose: () -> Unit,
) {
    var idx by remember { mutableIntStateOf(0) }
    var chosen by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableIntStateOf(0) }
    var done by remember { mutableStateOf(false) }
    var round by remember { mutableIntStateOf(0) }   // bumps on retry

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Close quiz")
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
        if (done) {
            val pct = if (items.isEmpty()) 0 else score * 100 / items.size
            val msg = when {
                pct >= 90 -> "Excellent. Exam-ready on this topic."
                pct >= 70 -> "Good. Revise the questions you missed."
                pct >= 50 -> "Fair. Re-read the chapters and try again."
                else -> "Needs work. Study the module, then retake the quiz."
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
                }) { Text("Retake quiz") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onClose) { Text("Back to quizzes") }
            }
        } else {
            val q = items[idx]
            LazyColumn(
                Modifier.fillMaxSize().padding(inner),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                item(key = "head$round-$idx") {
                    Text("Question ${idx + 1} of ${items.size}   ·   Score $score", style = MaterialTheme.typography.labelLarge, color = Teal)
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { (idx + (if (chosen != null) 1 else 0)).toFloat() / items.size },
                        modifier = Modifier.fillMaxWidth(),
                        color = Teal,
                    )
                    Spacer(Modifier.height(14.dp))
                    Text(q.q, style = MaterialTheme.typography.titleMedium, color = Navy, fontWeight = FontWeight.SemiBold)
                }
                items(q.options.indices.toList(), key = { "o$round-$idx-$it" }) { i ->
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
                        },
                        colors = CardDefaults.cardColors(containerColor = bg),
                        border = BorderStroke(1.5.dp, border),
                    ) {
                        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            Text("${'A' + i}.  ", fontWeight = FontWeight.Bold, color = Navy)
                            Text(q.options[i])
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
                                Text(if (ok) "Correct." else "Not quite.", fontWeight = FontWeight.Bold, color = if (ok) Teal else Rose)
                                if (q.explanation.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(q.explanation, style = MaterialTheme.typography.bodyMedium)
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
                        ) { Text(if (idx == items.size - 1) "See score" else "Next question") }
                    }
                }
            }
        }
    }
}
