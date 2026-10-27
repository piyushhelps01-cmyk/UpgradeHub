package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BatchCurriculum

@Composable
fun BatchUrlImportSyncCard(
    urlInput: String,
    selectedBatch: BatchCurriculum,
    parallelBatches: List<BatchCurriculum>,
    targetDays: Int,
    dailyHours: Float,
    isPlanning: Boolean = false,
    onUrlChange: (String) -> Unit,
    onSyncUrl: (String) -> Unit,
    onOpenFullPlannerDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val clipboardManager = LocalClipboardManager.current

    // Saved external batch URLs list so users can save and switch between PW, Unacademy, KGS links
    val savedBatchUrls = remember {
        mutableStateListOf(
            Triple("PW Prayas / Lakshya", "https://www.pw.live/study/batches/prayas-jee-2026", "PW"),
            Triple("Unacademy Nimbus", "https://unacademy.com/batch/nimbus-jee-main-advanced-2026", "Unacademy"),
            Triple("KGS SSC & Group D", "https://khanglobalstudies.com/courses/ssc-cgl-rrb-group-d-foundation-2026", "KGS")
        )
    }

    // Live platform detection from the typed/pasted URL
    val detectedPlatform = remember(urlInput, selectedBatch) {
        val lower = urlInput.lowercase()
        when {
            lower.contains("pw.live") || lower.contains("physicswallah") || lower.contains("prayas") || lower.contains("lakshya") || lower.contains("arjuna") || lower.contains("yakeen") || lower.contains("udaan") || lower.contains("neev") ->
                Triple("PW (Physics Wallah)", Color(0xFF6366F1), "PW")
            lower.contains("unacademy") || lower.contains("nimbus") || lower.contains("phoenix") || lower.contains("conquer") ->
                Triple("Unacademy", Color(0xFF10B981), "Unacademy")
            lower.contains("kgs") || lower.contains("khanglobal") || lower.contains("khan") ->
                Triple("KGS (Khan Global Studies)", Color(0xFFF59E0B), "KGS")
            lower.contains("allen") || lower.contains("enthuse") || lower.contains("nurture") || lower.contains("leader") ->
                Triple("Allen Digital", Color(0xFF0EA5E9), "Allen")
            lower.contains("youtube") || lower.contains("youtu.be") ->
                Triple("YouTube Playlist", Color(0xFFEF4444), "YouTube")
            lower.contains("ncert") || lower.contains("class-8") || lower.contains("class-9") || lower.contains("class-10") ->
                Triple("NCERT / Board Foundation", Color(0xFF8B5CF6), "NCERT")
            urlInput.isNotBlank() ->
                Triple("Custom Batch URL", Color(0xFF0070F3), "Custom")
            else ->
                Triple(selectedBatch.platform, Color(0xFF0070F3), selectedBatch.platform)
        }
    }

    val quickPlatformSamples = remember {
        listOf(
            Triple("PW Batch", "https://www.pw.live/study/batches/prayas-jee-2026", Color(0xFF6366F1)),
            Triple("Unacademy", "https://unacademy.com/batch/nimbus-jee-main-advanced-2026", Color(0xFF10B981)),
            Triple("KGS (Khan Sir)", "https://khanglobalstudies.com/courses/ssc-cgl-rrb-group-d-foundation-2026", Color(0xFFF59E0B)),
            Triple("PW NEET Yakeen", "https://www.pw.live/study/batches/yakeen-neet-2026", Color(0xFFEC4899)),
            Triple("Class 10 Udaan", "https://www.pw.live/study/batches/udaan-class-10-2026", Color(0xFF8B5CF6)),
            Triple("Allen Digital", "https://allen.ac.in/digital/leader-batch-2026", Color(0xFF0EA5E9))
        )
    }

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .testTag("batch_url_import_sync_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0070F3).copy(alpha = 0.10f),
                            Color(0xFF8B5CF6).copy(alpha = 0.06f)
                        )
                    )
                )
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0070F3).copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.35f)),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = "Batch URL Sync",
                                tint = Color(0xFF0070F3),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "EXTERNAL BATCH URL TRACKER (PW • UNACADEMY • KGS)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0070F3),
                            letterSpacing = 0.4.sp
                        )
                        Text(
                            text = "Input & save batch URLs to auto-sync lectures, syllabus & 30–150d planner",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = detectedPlatform.second.copy(alpha = 0.16f),
                    border = BorderStroke(1.dp, detectedPlatform.second.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = detectedPlatform.first,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = detectedPlatform.second,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Quick Platform Preset Chips (1-Tap Fill & Sync for PW, Unacademy, KGS, etc.)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Platforms:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                quickPlatformSamples.forEach { (label, sampleUrl, chipColor) ->
                    val isCurrent = urlInput.equals(sampleUrl, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isCurrent) chipColor else chipColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, chipColor.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clickable {
                                onUrlChange(sampleUrl)
                                onSyncUrl(sampleUrl)
                            }
                            .testTag("platform_preset_${label.lowercase().replace(" ", "_")}")
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCurrent) Color.White else chipColor,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // URL Input Field with Paste from Clipboard & Clear actions
            OutlinedTextField(
                value = urlInput,
                onValueChange = onUrlChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("batch_url_paste_input"),
                placeholder = {
                    Text(
                        text = "Paste PW, Unacademy, KGS, Allen or YouTube batch URL...",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = "Batch URL",
                        tint = Color(0xFF0070F3),
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        if (urlInput.isNotEmpty()) {
                            IconButton(
                                onClick = { onUrlChange("") },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear URL",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF0070F3).copy(alpha = 0.14f),
                            modifier = Modifier
                                .clickable {
                                    val clipText = clipboardManager.getText()?.text?.trim().orEmpty()
                                    if (clipText.isNotEmpty()) {
                                        onUrlChange(clipText)
                                    } else {
                                        onUrlChange("https://www.pw.live/study/batches/prayas-jee-2026")
                                    }
                                }
                                .testTag("batch_url_paste_clipboard_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentPaste,
                                    contentDescription = "Paste URL",
                                    tint = Color(0xFF0070F3),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Paste",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0070F3)
                                )
                            }
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF0070F3),
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Primary Action Row: Save & Sync URL + Configure Parallel Batches
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val trimmed = urlInput.trim()
                        if (trimmed.isNotEmpty()) {
                            val label = "${detectedPlatform.third} Batch (${trimmed.substringAfterLast("/").take(14).ifBlank { "Saved" }})"
                            if (savedBatchUrls.none { it.second.equals(trimmed, ignoreCase = true) }) {
                                savedBatchUrls.add(0, Triple(label, trimmed, detectedPlatform.third))
                            }
                        }
                        onSyncUrl(urlInput)
                    },
                    enabled = !isPlanning,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0070F3)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("batch_url_sync_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.BookmarkAdded,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isPlanning) "Syncing..." else "Save & Sync Batch URL",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                OutlinedButton(
                    onClick = onOpenFullPlannerDialog,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("batch_url_open_planner_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "${parallelBatches.size.coerceIn(1, 3)}/3 Parallel • ${targetDays}d",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Saved Batch URLs Row (Quick switch between user's saved PW, Unacademy, KGS URLs)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved URLs:",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                savedBatchUrls.forEach { (savedTitle, savedLink, platformTag) ->
                    val isSelectedUrl = urlInput.equals(savedLink, ignoreCase = true)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelectedUrl) Color(0xFF10B981).copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(
                            1.dp,
                            if (isSelectedUrl) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.clickable {
                            onUrlChange(savedLink)
                            onSyncUrl(savedLink)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "[$platformTag] $savedTitle",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelectedUrl) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Automated Tracking Status Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "Tracking: ${selectedBatch.title}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${selectedBatch.platform} • ${selectedBatch.examType} • ${selectedBatch.chapters.size} Chapters • ${selectedBatch.totalLectures} Lectures (${dailyHours.toInt()}h/day)",
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
