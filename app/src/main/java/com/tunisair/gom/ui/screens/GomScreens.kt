package com.tunisair.gom.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tunisair.gom.data.GomContent
import com.tunisair.gom.data.model.Chapter
import com.tunisair.gom.data.model.Section
import com.tunisair.gom.ui.Destination
import com.tunisair.gom.ui.GomViewModel

private fun chapterColor(id: Int): Color = Color(GomContent.chapters.firstOrNull { it.id == id }?.color ?: 0xFF1565C0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GomApp(viewModel: GomViewModel) {
    val destination = viewModel.destination
    val chapter = viewModel.openChapter?.let { GomContent.chapterById(it) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = chapter?.let { "CH${it.number} · ${it.title}" } ?: destination.label,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (chapter != null || viewModel.openSection != null) {
                        IconButton(onClick = { viewModel.back() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    } else {
                        Icon(
                            Icons.Filled.Flight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                },
                actions = {
                    Text(
                        text = "REV ${GomContent.MANUAL_REVISION}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val items = listOf(
                    Destination.Home to Icons.Filled.Home,
                    Destination.Chapters to Icons.Filled.List,
                    Destination.Search to Icons.Filled.Search,
                    Destination.Bookmarks to Icons.Filled.Bookmark,
                    Destination.Calculator to Icons.Filled.Calculate
                )
                items.forEach { (dest, icon) ->
                    NavigationBarItem(
                        selected = destination == dest,
                        onClick = { viewModel.goTo(dest) },
                        icon = { Icon(icon, contentDescription = dest.label) },
                        label = { Text(dest.label, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                chapter != null -> ChapterScreen(chapter, viewModel)
                else -> when (destination) {
                    Destination.Home -> HomeScreen(viewModel)
                    Destination.Chapters -> ChapterListScreen(viewModel)
                    Destination.Search -> SearchScreen(viewModel)
                    Destination.Bookmarks -> BookmarksScreen(viewModel)
                    Destination.Calculator -> CalculatorScreen()
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(viewModel: GomViewModel) {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            HeroCard(context)
        }
        item {
            Text("Jump to chapter", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        }
        items(GomContent.chapters, key = { it.id }) { ch ->
            ChapterRow(ch, onClick = { viewModel.openChapter(ch.id) })
        }
        item {
            Spacer(Modifier.height(8.dp))
            NoteCard(context)
        }
    }
}

@Composable
private fun HeroCard(context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Flight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "TUNISAIR",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Text(
                "Ground Operations Manual",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                "Revision ${GomContent.MANUAL_REVISION} · Effective ${GomContent.EFFECT_DATE}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Text(
                "Post Holder Ground Operations: ${GomContent.POSTHOLDER}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
            )
            Text(
                "Quality Manager: ${GomContent.QUALITY_MANAGER}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun NoteCard(context: Context) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Text("Reference only", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Text(
                "The English version of the manual is the only official version (GOM 0.4). " +
                    "This app condenses the manual for on-device reference; the official manual on " +
                    "the Tunisair intranet remains the controlling document.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Amendment contact: ${GomContent.NORMAS_EMAIL}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    val clip = ClipData.newPlainText("email", GomContent.NORMAS_EMAIL)
                    (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                    Toast.makeText(context, "E-mail copied", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
private fun ChapterRow(ch: Chapter, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 44.dp)
                    .background(chapterColor(ch.id), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    ch.number,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    ch.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    ch.titleArabic,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "${ch.sections.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChapterListScreen(viewModel: GomViewModel) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(GomContent.chapters, key = { it.id }) { ch ->
            ChapterRow(ch, onClick = { viewModel.openChapter(ch.id) })
        }
    }
}

@Composable
private fun ChapterScreen(chapter: Chapter, viewModel: GomViewModel) {
    val selected = viewModel.openSection
    if (selected != null) {
        val section = chapter.sections.firstOrNull { it.id == selected }
        SectionDetailScreen(chapter, section, viewModel)
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Column(Modifier.padding(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    chapter.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    chapter.titleArabic,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(chapter.sections, key = { it.id }) { sec ->
            SectionRow(
                section = sec,
                bookmarked = viewModel.isBookmarked(chapter.id, sec.id),
                onClick = { viewModel.toggleSection(sec.id) },
                onBookmark = {
                    viewModel.toggleBookmark(chapter.id, sec.id, "${sec.id} ${sec.title}", viewModel.excerptOf(chapter.id, sec.id))
                }
            )
        }
    }
}

@Composable
private fun SectionRow(section: Section, bookmarked: Boolean, onClick: () -> Unit, onBookmark: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f).padding(vertical = 8.dp)) {
                Text(
                    section.id,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    section.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            IconButton(onClick = onBookmark) {
                Icon(
                    if (bookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = if (bookmarked) "Remove bookmark" else "Bookmark section",
                    tint = if (bookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SectionDetailScreen(chapter: Chapter, section: Section?, viewModel: GomViewModel) {
    val context = LocalContext.current
    if (section == null) return

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    "CH${chapter.number} · ${section.id}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    section.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
            IconButton(onClick = {
                viewModel.toggleBookmark(
                    chapter.id,
                    section.id,
                    "${section.id} ${section.title}",
                    viewModel.excerptOf(chapter.id, section.id)
                )
            }) {
                Icon(
                    if (viewModel.isBookmarked(chapter.id, section.id)) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = {
                val text = "CH${chapter.number} ${section.id} ${section.title}\n\n${section.content}"
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("gom-section", text))
                Toast.makeText(context, "Section copied", Toast.LENGTH_SHORT).show()
            }) {
                Icon(Icons.Filled.Share, contentDescription = "Copy section")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
        ) {
            Text(
                section.content,
                style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                color = MaterialTheme.colorScheme.onBackground
            )
            section.subsections.forEach { sub ->
                Spacer(Modifier.height(12.dp))
                Text(
                    sub.id,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(sub.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(sub.content, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp))
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SearchScreen(viewModel: GomViewModel) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        OutlinedTextField(
            value = viewModel.query,
            onValueChange = viewModel::setQuery,
            label = { Text("Search the manual") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        if (viewModel.query.isBlank()) {
            SearchHints()
        } else {
            val results = viewModel.results
            if (results.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "No section matches \"${viewModel.query}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    "${results.size} result(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(results, key = { "${it.chapterId}-${it.sectionId}-${it.snippet.hashCode()}" }) { hit ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                viewModel.openChapter(hit.chapterId, hit.sectionId)
                            },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    "CH${hit.chapterNumber} · ${hit.sectionId} ${hit.sectionTitle}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                hit.snippet?.let {
                                    Text(
                                        it,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 3,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchHints() {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Try searching for a procedure or keyword",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        listOf(
            "check-in" to "Passenger acceptance procedures",
            "baggage" to "Acceptance, labelling and irregularities",
            "lithium" to "Dangerous goods in passenger baggage",
            "loadsheet" to "Load control and LMC entry",
            "NOTOC" to "Notification to captain",
            "PRM" to "Passengers with reduced mobility",
            "UM" to "Unaccompanied minors",
            "dry ice" to "Restricted articles",
            "fuelling" to "Ramp handling",
            "delay codes" to "Punctuality reporting"
        ).forEach { (term, hint) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(term, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarksScreen(viewModel: GomViewModel) {
    if (viewModel.bookmarks.isEmpty()) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Filled.BookmarkBorder,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "No bookmarks yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Tap the bookmark icon on any section to save it here",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(viewModel.bookmarks, key = { "${it.chapterId}-${it.sectionId}" }) { bm ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable {
                    viewModel.openChapter(bm.chapterId, bm.sectionId)
                },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f).padding(vertical = 6.dp)) {
                        Text(
                            bm.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            bm.excerpt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = { viewModel.removeBookmark(bm.chapterId, bm.sectionId) }) {
                        Icon(
                            Icons.Filled.Bookmark,
                            contentDescription = "Remove bookmark",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculatorScreen() {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "Weights and measures",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            "Conversion factors from GOM 0.8",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        var weight by remember { mutableStateOf("") }
        var length by remember { mutableStateOf("") }
        var volume by remember { mutableStateOf("") }
        var tempC by remember { mutableStateOf("") }

        ConversionCard(
            title = "Weight",
            input = weight,
            onInput = { weight = it },
            rows = listOf(
                "Kilograms → Pounds" to { v: Double -> v * 2.2046 },
                "Pounds → Kilograms" to { v: Double -> v * 0.4536 },
                "Ounces → Grams" to { v: Double -> v * 28.3495 },
                "Tons (metric) → Pounds" to { v: Double -> v * 2205.0 },
                "Tons (short) → Kilograms" to { v: Double -> v * 907.1849 }
            )
        )

        ConversionCard(
            title = "Length",
            input = length,
            onInput = { length = it },
            rows = listOf(
                "Centimetres → Inches" to { v: Double -> v * 0.3937 },
                "Inches → Centimetres" to { v: Double -> v * 2.54 },
                "Metres → Feet" to { v: Double -> v * 3.2808 },
                "Feet → Metres" to { v: Double -> v * 0.3048 },
                "Feet → Inches" to { v: Double -> v * 12.0 }
            )
        )

        ConversionCard(
            title = "Volume",
            input = volume,
            onInput = { volume = it },
            rows = listOf(
                "Cubic metres → Cubic feet" to { v: Double -> v * 35.31 },
                "Cubic feet → Litres" to { v: Double -> v * 28.32 },
                "Litres → U.S. gallons" to { v: Double -> v * 0.2642 },
                "Litres → Imperial gallons" to { v: Double -> v * 0.22 },
                "U.S. gallons → Litres" to { v: Double -> v * 3.7853 }
            )
        )

        ConversionCard(
            title = "Temperature",
            input = tempC,
            onInput = { tempC = it },
            rows = listOf(
                "Celsius → Fahrenheit" to { v: Double -> v * 9 / 5 + 32 },
                "Fahrenheit → Celsius" to { v: Double -> (v - 32) * 5 / 9 }
            )
        )
    }
}

@Composable
private fun ConversionCard(
    title: String,
    input: String,
    onInput: (String) -> Unit,
    rows: List<Pair<String, (Double) -> Double>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = input,
                onValueChange = onInput,
                label = { Text("Enter $title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            val value = input.toDoubleOrNull()
            rows.forEach { (label, fn) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = if (value == null) "—" else format(fn(value)),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun format(value: Double): String {
    val abs = kotlin.math.abs(value)
    return if (abs >= 1000) String.format("%.0f", value)
    else if (abs >= 10) String.format("%.2f", value)
    else String.format("%.4f", value)
}
