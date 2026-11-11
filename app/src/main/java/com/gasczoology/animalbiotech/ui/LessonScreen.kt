package com.gasczoology.animalbiotech.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gasczoology.animalbiotech.content.Chapter
import com.gasczoology.animalbiotech.i18n.Lang
import com.gasczoology.animalbiotech.i18n.shown
import com.gasczoology.animalbiotech.i18n.tr
import com.gasczoology.animalbiotech.ui.blocks.BiText
import com.gasczoology.animalbiotech.ui.blocks.BlockView
import com.gasczoology.animalbiotech.ui.blocks.TamilFont

private val Navy = Color(0xFF0F2744)

/** Native chapter reader. [zoom] is the existing text-size setting in percent (100 = normal). */
@OptIn(ExperimentalMaterial3Api::class)
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
) {
    val scale = zoom / 100f
    val title = chapter.title.shown(lang).firstOrNull { it.text.isNotEmpty() }?.text ?: chapter.title.en
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis, style = MaterialTheme.typography.titleSmall, fontFamily = if (lang == Lang.TA) TamilFont else null) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    IconButton(onClick = onSaved) {
                        Icon(Icons.Default.Star, contentDescription = "Save topic", tint = if (saved) Color(0xFFF5B301) else Color.White.copy(alpha = 0.85f))
                    }
                    IconButton(onClick = onStudied) {
                        Icon(Icons.Default.Check, contentDescription = "Mark as studied", tint = if (studied) Color(0xFF5EE6D5) else Color.White.copy(alpha = 0.85f))
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
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(inner),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item(key = "lang") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = lang == Lang.EN, onClick = { onLang(Lang.EN) }, label = { Text("English") })
                    FilterChip(selected = lang == Lang.TA, onClick = { onLang(Lang.TA) }, label = { Text("தமிழ்", fontFamily = TamilFont) })
                }
            }
            item(key = "title") {
                BiText(chapter.title, lang, scale, base = MaterialTheme.typography.headlineSmall, weight = FontWeight.Bold, color = Navy)
            }
            chapter.sections.forEachIndexed { si, section ->
                item(key = "sh-$si-${section.id}") {
                    BiText(section.title, lang, scale, Modifier.padding(top = 8.dp), MaterialTheme.typography.titleLarge, FontWeight.Bold, Navy)
                }
                items(section.blocks.size, key = { "b-$si-${section.id}-$it" }) { bi ->
                    BlockView(section.blocks[bi], lang, scale)
                }
            }
            if (onQuiz != null) {
                item(key = "quiz") {
                    Button(onClick = onQuiz, modifier = Modifier.fillMaxWidth()) { Text(tr(lang, "Practise MCQs", "பயிற்சி வினாக்கள்"), fontFamily = TamilFont) }
                }
            }
            item(key = "credit") {
                Column(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 24.dp)) {
                    Text("Department of Zoology, GASC, Nagercoil", fontWeight = FontWeight.SemiBold, color = Navy, style = MaterialTheme.typography.bodyMedium)
                    Text("Created by R. Ramesh", style = MaterialTheme.typography.labelSmall, color = Color(0xFF3B4F66))
                }
            }
        }
    }
}
