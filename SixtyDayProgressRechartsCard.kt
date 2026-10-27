package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.SixtyDayProgressData
import com.example.data.model.SixtyDayProgressPoint
import java.util.Locale
import kotlin.math.roundToInt

enum class SixtyDayChartMode(val label: String) {
    DUAL_METRICS("Dual Metrics"),
    STUDY_HOURS("Study Hours"),
    SYLLABUS_COVERAGE("Syllabus %"),
    CUMULATIVE_HOURS("Cumulative")
}

enum class SixtyDayRange(val label: String, val startDay: Int, val endDay: Int) {
    ALL_60("All 60 Days", 1, 60),
    FIRST_30("Days 1–30", 1, 30),
    SECOND_30("Days 31–60", 31, 60),
    LAST_14("Last 14 Days", 47, 60)
}

enum class ChartEngine(val label: String) {
    D3_RECHARTS("D3 / Recharts (Web)"),
    NATIVE_CANVAS("Native Compose")
}

/**
 * 60-Day Interactive Progress Visualizer
 * Powered by D3.js / Recharts architecture:
 * - Dual-Axis: Left (Study Hours 0-18h), Right (Syllabus Coverage % 0-100%)
 * - Area Gradient Fills & Monotone Cubic Splines
 * - Target Reference Lines (12h/15h) & 7-Day Rolling Moving Average
 * - Interactive Touch Crosshair Tooltip with Scrubbing
 * - Range Filtering (60 Days / 30 Days / 14 Days)
 * - Switchable D3/Recharts SVG Engine & Native High-Performance Canvas
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SixtyDayProgressRechartsCard(
    progressData: SixtyDayProgressData,
    onSelectDay: (dayNumber: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDayNumber by remember { mutableIntStateOf(1) }
    var chartMode by remember { mutableStateOf(SixtyDayChartMode.DUAL_METRICS) }
    var selectedRange by remember { mutableStateOf(SixtyDayRange.ALL_60) }
    var engine by remember { mutableStateOf(ChartEngine.NATIVE_CANVAS) }

    val filteredPoints = remember(progressData.points, selectedRange) {
        progressData.points.filter { it.dayNumber in selectedRange.startDay..selectedRange.endDay }
    }

    val selectedPoint = remember(progressData.points, selectedDayNumber) {
        progressData.points.find { it.dayNumber == selectedDayNumber } ?: progressData.points.firstOrNull()
    }

    GlassmorphicContainer(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sixty_day_progress_recharts_card"),
        shape = RoundedCornerShape(24.dp),
        backgroundColor = MaterialTheme.colorScheme.surface,
        rimHighlightColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        rimShadowColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
        glowBorder = Color(0xFF0070F3).copy(alpha = 0.18f),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // 1. Header with Recharts / D3 Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0070F3).copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = "60-Day Charts",
                                tint = Color(0xFF0070F3),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "60-Day Progress Visualizer",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0070F3).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "D3 / RECHARTS",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0070F3),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Study hours & syllabus coverage over 60-day marathon",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. High-Level 60-Day Metric Summary Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    title = "Logged Hours",
                    value = "${String.format(Locale.ENGLISH, "%.1f", progressData.summary.totalLoggedHours)}h",
                    subtitle = "/ ${progressData.summary.totalTargetHours.toInt()}h target",
                    color = Color(0xFF0070F3),
                    modifier = Modifier.weight(1f)
                )

                MetricChip(
                    title = "Syllabus Covered",
                    value = "${String.format(Locale.ENGLISH, "%.1f", progressData.summary.currentCoveragePct)}%",
                    subtitle = "${progressData.points.getOrNull(selectedDayNumber - 1)?.chaptersCompleted ?: 0} chapters done",
                    color = Color(0xFF00C853),
                    modifier = Modifier.weight(1f)
                )

                MetricChip(
                    title = "Daily Average",
                    value = "${String.format(Locale.ENGLISH, "%.1f", progressData.summary.averageDailyHours)}h",
                    subtitle = progressData.summary.paceStatus.split(" ").firstOrNull() ?: "Active",
                    color = Color(progressData.summary.paceColorHex),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Engine & Mode Toggle Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Range selector
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SixtyDayRange.values().forEach { range ->
                        val isSelected = selectedRange == range
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF0070F3) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier
                                .clickable { selectedRange = range }
                                .testTag("range_tab_${range.name.lowercase()}")
                        ) {
                            Text(
                                text = range.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Engine toggle (D3 Web vs Native Compose)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(modifier = Modifier.padding(2.dp)) {
                        ChartEngine.values().forEach { eng ->
                            val isAct = engine == eng
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isAct) MaterialTheme.colorScheme.surface else Color.Transparent)
                                    .clickable { engine = eng }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (eng == ChartEngine.D3_RECHARTS) "D3 (Web)" else "Native",
                                    fontSize = 10.sp,
                                    fontWeight = if (isAct) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isAct) Color(0xFF0070F3) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chart Series Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                SixtyDayChartMode.values().forEach { mode ->
                    val isSel = chartMode == mode
                    FilterChip(
                        selected = isSel,
                        onClick = { chartMode = mode },
                        label = {
                            Text(
                                text = mode.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0070F3).copy(alpha = 0.15f),
                            selectedLabelColor = Color(0xFF0070F3)
                        ),
                        modifier = Modifier.height(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Recharts / D3 Legend Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendItem(label = "Study Hours (0-18h)", color = Color(0xFF0070F3), isArea = true)
                    LegendItem(label = "Syllabus % (0-100%)", color = Color(0xFF00C853), isLine = true)
                    LegendItem(label = "Target (12h)", color = Color(0xFFFF9800), isDashed = true)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. Chart Visualization Canvas / WebView
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            ) {
                when (engine) {
                    ChartEngine.NATIVE_CANVAS -> {
                        NativeRechartsCanvas(
                            points = filteredPoints,
                            mode = chartMode,
                            selectedDay = selectedDayNumber,
                            onDayHover = { day ->
                                selectedDayNumber = day
                            },
                            onDayClick = { day ->
                                selectedDayNumber = day
                                onSelectDay(day)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    ChartEngine.D3_RECHARTS -> {
                        D3RechartsWebView(
                            points = filteredPoints,
                            mode = chartMode,
                            selectedDay = selectedDayNumber,
                            onDaySelected = { day ->
                                selectedDayNumber = day
                                onSelectDay(day)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Interactive Floating Tooltip / Selected Day Detail Card
            if (selectedPoint != null) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.35f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF0070F3)
                                ) {
                                    Text(
                                        text = "DAY ${selectedPoint.dayNumber}",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${selectedPoint.dayOfWeek}, ${selectedPoint.dateLabel}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (selectedPoint.isToday) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF00C853).copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "TODAY",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF00C853),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Focused Hours",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${String.format(Locale.ENGLISH, "%.1f", selectedPoint.studyHours)}h / ${selectedPoint.targetHours.toInt()}h",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedPoint.isTargetMet) Color(0xFF00C853) else Color(0xFF0070F3)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "Syllabus Coverage",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${String.format(Locale.ENGLISH, "%.1f", selectedPoint.syllabusCoveragePct)}%",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00C853)
                                    )
                                }

                                Column {
                                    Text(
                                        text = "7-Day Moving Avg",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${String.format(Locale.ENGLISH, "%.1f", selectedPoint.movingAverageHours)}h/d",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8A2BE2)
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0070F3).copy(alpha = 0.12f),
                            modifier = Modifier
                                .clickable { onSelectDay(selectedPoint.dayNumber) }
                                .padding(2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Plan Day",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0070F3)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Go",
                                    tint = Color(0xFF0070F3),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7. Recharts-style Milestone Trackers (25%, 50%, 75%, 100%)
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Syllabus Milestones Progress",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MilestoneStep(label = "25% Foundation", pct = 25f, current = progressData.summary.currentCoveragePct, modifier = Modifier.weight(1f))
                    MilestoneStep(label = "50% Core", pct = 50f, current = progressData.summary.currentCoveragePct, modifier = Modifier.weight(1f))
                    MilestoneStep(label = "75% Advanced", pct = 75f, current = progressData.summary.currentCoveragePct, modifier = Modifier.weight(1f))
                    MilestoneStep(label = "100% Ranker", pct = 100f, current = progressData.summary.currentCoveragePct, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun MetricChip(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun LegendItem(
    label: String,
    color: Color,
    isArea: Boolean = false,
    isLine: Boolean = false,
    isDashed: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 14.dp, height = if (isArea) 10.dp else 3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MilestoneStep(
    label: String,
    pct: Float,
    current: Float,
    modifier: Modifier = Modifier
) {
    val reached = current >= pct
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (reached) Color(0xFF00C853).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, if (reached) Color(0xFF00C853).copy(alpha = 0.4f) else Color.Transparent),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = if (reached) Icons.Default.CheckCircle else Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = if (reached) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 8.5.sp,
                fontWeight = if (reached) FontWeight.Bold else FontWeight.Normal,
                color = if (reached) Color(0xFF00C853) else MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

/**
 * High-Performance Native Jetpack Compose Canvas Implementation of Recharts ComposedChart
 */
@Composable
private fun NativeRechartsCanvas(
    points: List<SixtyDayProgressPoint>,
    mode: SixtyDayChartMode,
    selectedDay: Int,
    onDayHover: (Int) -> Unit,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return

    val density = LocalDensity.current
    var touchX by remember { mutableStateOf<Float?>(null) }

    Canvas(
        modifier = modifier
            .pointerInput(points) {
                detectTapGestures { offset ->
                    val paddingLeft = with(density) { 36.dp.toPx() }
                    val paddingRight = with(density) { 36.dp.toPx() }
                    val plotWidth = size.width - paddingLeft - paddingRight
                    val stepX = plotWidth / (points.size - 1).coerceAtLeast(1)
                    val relX = (offset.x - paddingLeft).coerceIn(0f, plotWidth)
                    val idx = (relX / stepX).roundToInt().coerceIn(0, points.size - 1)
                    val pt = points[idx]
                    onDayClick(pt.dayNumber)
                }
            }
            .pointerInput(points) {
                detectDragGestures(
                    onDragStart = { offset ->
                        touchX = offset.x
                        val paddingLeft = with(density) { 36.dp.toPx() }
                        val paddingRight = with(density) { 36.dp.toPx() }
                        val plotWidth = size.width - paddingLeft - paddingRight
                        val stepX = plotWidth / (points.size - 1).coerceAtLeast(1)
                        val relX = (offset.x - paddingLeft).coerceIn(0f, plotWidth)
                        val idx = (relX / stepX).roundToInt().coerceIn(0, points.size - 1)
                        onDayHover(points[idx].dayNumber)
                    },
                    onDrag = { change, _ ->
                        touchX = change.position.x
                        val paddingLeft = with(density) { 36.dp.toPx() }
                        val paddingRight = with(density) { 36.dp.toPx() }
                        val plotWidth = size.width - paddingLeft - paddingRight
                        val stepX = plotWidth / (points.size - 1).coerceAtLeast(1)
                        val relX = (change.position.x - paddingLeft).coerceIn(0f, plotWidth)
                        val idx = (relX / stepX).roundToInt().coerceIn(0, points.size - 1)
                        onDayHover(points[idx].dayNumber)
                    },
                    onDragEnd = {
                        touchX = null
                    },
                    onDragCancel = {
                        touchX = null
                    }
                )
            }
    ) {
        val paddingLeft = 36.dp.toPx()
        val paddingRight = 36.dp.toPx()
        val paddingTop = 16.dp.toPx()
        val paddingBottom = 26.dp.toPx()

        val plotWidth = size.width - paddingLeft - paddingRight
        val plotHeight = size.height - paddingTop - paddingBottom
        if (plotWidth <= 0f || plotHeight <= 0f) return@Canvas

        val maxHours = 18f
        val maxPct = 100f
        val n = points.size
        val stepX = plotWidth / (n - 1).coerceAtLeast(1)

        // Draw Horizontal Gridlines & Y-Axis Labels
        val gridLines = 4
        val hoursPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 10.sp.toPx()
            textAlign = android.graphics.Paint.Align.RIGHT
            isAntiAlias = true
        }
        val pctPaint = android.graphics.Paint().apply {
            color = 0xFF00C853.toInt()
            textSize = 10.sp.toPx()
            textAlign = android.graphics.Paint.Align.LEFT
            isAntiAlias = true
        }

        for (i in 0..gridLines) {
            val frac = i.toFloat() / gridLines
            val y = paddingTop + plotHeight * (1f - frac)
            drawLine(
                color = Color.LightGray.copy(alpha = 0.35f),
                start = Offset(paddingLeft, y),
                end = Offset(size.width - paddingRight, y),
                strokeWidth = 1.dp.toPx()
            )

            // Left axis: Study Hours
            val hVal = (frac * maxHours).toInt()
            drawContext.canvas.nativeCanvas.drawText("${hVal}h", paddingLeft - 6.dp.toPx(), y + 4.dp.toPx(), hoursPaint)

            // Right axis: Syllabus %
            val pctVal = (frac * maxPct).toInt()
            drawContext.canvas.nativeCanvas.drawText("$pctVal%", size.width - paddingRight + 6.dp.toPx(), y + 4.dp.toPx(), pctPaint)
        }

        // Draw Target 12h reference dashed line (<ReferenceLine y={12} />)
        val targetY = paddingTop + plotHeight * (1f - 12f / maxHours)
        drawLine(
            color = Color(0xFFFF9800),
            start = Offset(paddingLeft, targetY),
            end = Offset(size.width - paddingRight, targetY),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )

        // Coordinates mapping
        fun getX(idx: Int): Float = paddingLeft + idx * stepX
        fun getYHours(h: Float): Float = paddingTop + plotHeight * (1f - (h / maxHours).coerceIn(0f, 1f))
        fun getYPct(p: Float): Float = paddingTop + plotHeight * (1f - (p / maxPct).coerceIn(0f, 1f))

        // 1. Draw Study Hours Area & Spline (if mode includes Study Hours or Dual)
        if (mode == SixtyDayChartMode.DUAL_METRICS || mode == SixtyDayChartMode.STUDY_HOURS) {
            val hoursPath = Path()
            val areaPath = Path()

            val firstX = getX(0)
            val firstY = getYHours(points.first().studyHours)

            hoursPath.moveTo(firstX, firstY)
            areaPath.moveTo(firstX, paddingTop + plotHeight)
            areaPath.lineTo(firstX, firstY)

            for (i in 0 until n - 1) {
                val x0 = getX(i)
                val y0 = getYHours(points[i].studyHours)
                val x1 = getX(i + 1)
                val y1 = getYHours(points[i + 1].studyHours)

                val cx1 = x0 + (x1 - x0) / 2f
                val cy1 = y0
                val cx2 = x0 + (x1 - x0) / 2f
                val cy2 = y1

                hoursPath.cubicTo(cx1, cy1, cx2, cy2, x1, y1)
                areaPath.cubicTo(cx1, cy1, cx2, cy2, x1, y1)
            }

            areaPath.lineTo(getX(n - 1), paddingTop + plotHeight)
            areaPath.close()

            // Area Gradient Fill
            drawPath(
                path = areaPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0070F3).copy(alpha = 0.35f),
                        Color(0xFF0070F3).copy(alpha = 0.03f)
                    ),
                    startY = paddingTop,
                    endY = paddingTop + plotHeight
                )
            )

            // Hours Stroke
            drawPath(
                path = hoursPath,
                color = Color(0xFF0070F3),
                style = Stroke(
                    width = 2.5.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // 2. Draw Syllabus Coverage Line (Emerald Green)
        if (mode == SixtyDayChartMode.DUAL_METRICS || mode == SixtyDayChartMode.SYLLABUS_COVERAGE) {
            val pctPath = Path()
            val firstX = getX(0)
            val firstY = getYPct(points.first().syllabusCoveragePct)
            pctPath.moveTo(firstX, firstY)

            for (i in 0 until n - 1) {
                val x0 = getX(i)
                val y0 = getYPct(points[i].syllabusCoveragePct)
                val x1 = getX(i + 1)
                val y1 = getYPct(points[i + 1].syllabusCoveragePct)

                val cx1 = x0 + (x1 - x0) / 2f
                val cy1 = y0
                val cx2 = x0 + (x1 - x0) / 2f
                val cy2 = y1

                pctPath.cubicTo(cx1, cy1, cx2, cy2, x1, y1)
            }

            drawPath(
                path = pctPath,
                color = Color(0xFF00C853),
                style = Stroke(
                    width = 3.dp.toPx(),
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )

            // Draw milestone dots at 25%, 50%, 75%, 100%
            points.forEachIndexed { i, pt ->
                val p = pt.syllabusCoveragePct
                if (p in 24f..26f || p in 49f..51f || p in 74f..76f || p >= 99f) {
                    val px = getX(i)
                    val py = getYPct(p)
                    drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(px, py))
                    drawCircle(color = Color(0xFF00C853), radius = 3.5.dp.toPx(), center = Offset(px, py))
                }
            }
        }

        // 3. Draw 7-Day Rolling Moving Average (Purple Dotted Line)
        if (mode == SixtyDayChartMode.DUAL_METRICS || mode == SixtyDayChartMode.STUDY_HOURS) {
            val avgPath = Path()
            avgPath.moveTo(getX(0), getYHours(points.first().movingAverageHours))
            for (i in 1 until n) {
                avgPath.lineTo(getX(i), getYHours(points[i].movingAverageHours))
            }
            drawPath(
                path = avgPath,
                color = Color(0xFF8A2BE2),
                style = Stroke(
                    width = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )
            )
        }

        // 4. Highlight Selected Day Indicator & Vertical Crosshair
        val selectedIdx = points.indexOfFirst { it.dayNumber == selectedDay }
        if (selectedIdx >= 0) {
            val selX = getX(selectedIdx)
            // Vertical cursor line
            drawLine(
                color = Color(0xFF0070F3).copy(alpha = 0.7f),
                start = Offset(selX, paddingTop),
                end = Offset(selX, paddingTop + plotHeight),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )

            // Dot on hours
            val selYHours = getYHours(points[selectedIdx].studyHours)
            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(selX, selYHours))
            drawCircle(color = Color(0xFF0070F3), radius = 4.dp.toPx(), center = Offset(selX, selYHours))

            // Dot on syllabus %
            val selYPct = getYPct(points[selectedIdx].syllabusCoveragePct)
            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(selX, selYPct))
            drawCircle(color = Color(0xFF00C853), radius = 4.dp.toPx(), center = Offset(selX, selYPct))
        }

        // 5. X-Axis Ticks & Day Labels
        val xLabelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.DKGRAY
            textSize = 9.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }

        val stepLabel = when {
            n <= 14 -> 2
            n <= 30 -> 5
            else -> 10
        }

        for (i in points.indices) {
            val pt = points[i]
            if (pt.dayNumber == 1 || pt.dayNumber % stepLabel == 0 || pt.dayNumber == 60) {
                val x = getX(i)
                drawContext.canvas.nativeCanvas.drawText("D${pt.dayNumber}", x, size.height - 6.dp.toPx(), xLabelPaint)
            }
        }
    }
}

/**
 * Web-Based D3.js / Recharts Interactive Engine running within an optimized Android WebView
 * Renders an interactive D3 SVG with tooltip, animations, and crosshair interaction.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun D3RechartsWebView(
    points: List<SixtyDayProgressPoint>,
    mode: SixtyDayChartMode,
    selectedDay: Int,
    onDaySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val jsBridge = remember {
        object {
            @JavascriptInterface
            fun onSelectDayFromWeb(day: Int) {
                onDaySelected(day)
            }
        }
    }

    val htmlContent = remember(points, mode, selectedDay) {
        generateD3Html(points, mode, selectedDay)
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                addJavascriptInterface(jsBridge, "AndroidBridge")
                webViewClient = WebViewClient()
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}

/**
 * Generates lightweight, fully-offline interactive D3 / Recharts HTML + SVG string
 */
private fun generateD3Html(
    points: List<SixtyDayProgressPoint>,
    mode: SixtyDayChartMode,
    selectedDay: Int
): String {
    val jsonPoints = buildString {
        append("[")
        points.forEachIndexed { idx, pt ->
            append("{\"d\":${pt.dayNumber},\"h\":${String.format(Locale.ENGLISH, "%.1f", pt.studyHours)},\"t\":${pt.targetHours},\"c\":${String.format(Locale.ENGLISH, "%.1f", pt.syllabusCoveragePct)},\"date\":\"${pt.dateLabel}\"}")
            if (idx < points.size - 1) append(",")
        }
        append("]")
    }

    return """
    <!DOCTYPE html>
    <html>
    <head>
      <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
      <style>
        * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; user-select: none; }
        body { background: transparent; overflow: hidden; width: 100vw; height: 100vh; display: flex; align-items: center; justify-content: center; }
        svg { width: 100%; height: 100%; overflow: visible; }
        .grid-line { stroke: rgba(180, 190, 205, 0.3); stroke-width: 1; }
        .target-line { stroke: #FFA000; stroke-width: 1.5; stroke-dasharray: 4,4; }
        .axis-label { font-size: 10px; fill: #6B7280; }
        .pct-label { font-size: 10px; fill: #00C853; font-weight: bold; }
        .cursor-line { stroke: #0070F3; stroke-width: 1.5; stroke-dasharray: 4,4; }
        .tooltip-box { fill: #111827; rx: 6; }
        .tooltip-text { fill: #ffffff; font-size: 11px; font-weight: bold; }
      </style>
    </head>
    <body>
      <div id="chart-container" style="width: 100%; height: 100%;"></div>
      <script>
        const data = $jsonPoints;
        const container = document.getElementById('chart-container');
        const W = container.clientWidth || 360;
        const H = container.clientHeight || 260;
        const padL = 36, padR = 36, padT = 16, padB = 26;
        const plotW = W - padL - padR;
        const plotH = H - padT - padB;

        const maxH = 18;
        const maxPct = 100;
        const n = data.length;

        function xCoord(i) { return padL + (i / Math.max(1, n - 1)) * plotW; }
        function yHours(h) { return padT + plotH * (1 - Math.min(1, Math.max(0, h / maxH))); }
        function yPct(c) { return padT + plotH * (1 - Math.min(1, Math.max(0, c / maxPct))); }

        let svg = `<svg viewBox="0 0 ${'$'}{W} ${'$'}{H}">`;
        svg += `<defs>
          <linearGradient id="hoursGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#0070F3" stop-opacity="0.4"/>
            <stop offset="100%" stop-color="#0070F3" stop-opacity="0.02"/>
          </linearGradient>
        </defs>`;

        // Horizontal Grid
        for (let i = 0; i <= 4; i++) {
          const frac = i / 4;
          const y = padT + plotH * (1 - frac);
          svg += `<line x1="${'$'}{padL}" y1="${'$'}{y}" x2="${'$'}{W - padR}" y2="${'$'}{y}" class="grid-line" />`;
          svg += `<text x="${'$'}{padL - 6}" y="${'$'}{y + 3}" text-anchor="end" class="axis-label">${'$'}{Math.round(frac * maxH)}h</text>`;
          svg += `<text x="${'$'}{W - padR + 6}" y="${'$'}{y + 3}" text-anchor="start" class="pct-label">${'$'}{Math.round(frac * maxPct)}%</text>`;
        }

        // Target Line (12h)
        const targetY = yHours(12);
        svg += `<line x1="${'$'}{padL}" y1="${'$'}{targetY}" x2="${'$'}{W - padR}" y2="${'$'}{targetY}" class="target-line" />`;

        // Hours Area Path
        let areaD = `M ${'$'}{xCoord(0)} ${'$'}{padT + plotH} L ${'$'}{xCoord(0)} ${'$'}{yHours(data[0].h)}`;
        let lineD = `M ${'$'}{xCoord(0)} ${'$'}{yHours(data[0].h)}`;
        for (let i = 0; i < n - 1; i++) {
          const x0 = xCoord(i), y0 = yHours(data[i].h);
          const x1 = xCoord(i + 1), y1 = yHours(data[i + 1].h);
          const cx = (x0 + x1) / 2;
          lineD += ` C ${'$'}{cx} ${'$'}{y0}, ${'$'}{cx} ${'$'}{y1}, ${'$'}{x1} ${'$'}{y1}`;
          areaD += ` C ${'$'}{cx} ${'$'}{y0}, ${'$'}{cx} ${'$'}{y1}, ${'$'}{x1} ${'$'}{y1}`;
        }
        areaD += ` L ${'$'}{xCoord(n - 1)} ${'$'}{padT + plotH} Z`;

        svg += `<path d="${'$'}{areaD}" fill="url(#hoursGrad)" />`;
        svg += `<path d="${'$'}{lineD}" fill="none" stroke="#0070F3" stroke-width="2.5" stroke-linecap="round" />`;

        // Syllabus % Line Path
        let pctD = `M ${'$'}{xCoord(0)} ${'$'}{yPct(data[0].c)}`;
        for (let i = 0; i < n - 1; i++) {
          const x0 = xCoord(i), y0 = yPct(data[i].c);
          const x1 = xCoord(i + 1), y1 = yPct(data[i + 1].c);
          const cx = (x0 + x1) / 2;
          pctD += ` C ${'$'}{cx} ${'$'}{y0}, ${'$'}{cx} ${'$'}{y1}, ${'$'}{x1} ${'$'}{y1}`;
        }
        svg += `<path d="${'$'}{pctD}" fill="none" stroke="#00C853" stroke-width="3" stroke-linecap="round" />`;

        // Milestone circles
        for (let i = 0; i < n; i++) {
          const c = data[i].c;
          if ((c >= 24 && c <= 26) || (c >= 49 && c <= 51) || (c >= 74 && c <= 76) || c >= 99) {
            svg += `<circle cx="${'$'}{xCoord(i)}" cy="${'$'}{yPct(c)}" r="5" fill="#ffffff" stroke="#00C853" stroke-width="2" />`;
          }
        }

        // X-Axis day labels
        const step = n <= 14 ? 2 : (n <= 30 ? 5 : 10);
        for (let i = 0; i < n; i++) {
          const d = data[i].d;
          if (d === 1 || d % step === 0 || d === 60) {
            svg += `<text x="${'$'}{xCoord(i)}" y="${'$'}{H - 6}" text-anchor="middle" class="axis-label">D${'$'}{d}</text>`;
          }
        }

        svg += `</svg>`;
        container.innerHTML = svg;

        // Add touch / click listener
        container.addEventListener('click', function(e) {
          const rect = container.getBoundingClientRect();
          const relX = e.clientX - rect.left - padL;
          const idx = Math.max(0, Math.min(n - 1, Math.round((relX / plotW) * (n - 1))));
          if (window.AndroidBridge && window.AndroidBridge.onSelectDayFromWeb) {
            window.AndroidBridge.onSelectDayFromWeb(data[idx].d);
          }
        });
      </script>
    </body>
    </html>
    """.trimIndent()
}
