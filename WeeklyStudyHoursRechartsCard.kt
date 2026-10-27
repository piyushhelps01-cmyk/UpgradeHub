package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.DailyStudyHoursPoint
import com.example.data.model.SevenDayTrendData
import java.util.Locale
import kotlin.math.roundToInt

enum class WeeklyRechartsStyle(val label: String) {
    COMPOSED_BAR_LINE("Composed Bar"),
    MONOTONE_AREA("Area Spline"),
    RECHARTS_WEB_SVG("Recharts SVG")
}

/**
 * Weekly Study Hours Summary Chart (Recharts Architecture) for the Main Dashboard.
 * Visualizes productivity trends over the last 7 days with:
 * - Recharts <ComposedChart> (Bar + Monotone Target Line), <AreaChart> (Monotone Cubic Spline + Gradient),
 *   and Interactive Recharts SVG Web Engine
 * - 7-Day Productivity KPI Summary (Weekly Total, Daily Average, Target Met Days, Consistency Score)
 * - Interactive Day Tooltip with Physics/Chemistry/Maths subject split & instant study hour logging
 */
@Composable
fun WeeklyStudyHoursRechartsCard(
    trendData: SevenDayTrendData,
    onSelectDay: (dayNumber: Int) -> Unit,
    onUpdateDayHours: (dayNumber: Int, newHours: Float) -> Unit,
    onOpenFullAnalytics: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }
    var chartStyle by remember { mutableStateOf(WeeklyRechartsStyle.COMPOSED_BAR_LINE) }

    LaunchedEffect(trendData.points) {
        if (selectedIndex == -1 || selectedIndex >= trendData.points.size) {
            val todayIdx = trendData.points.indexOfFirst { it.isToday }
            selectedIndex = if (todayIdx >= 0) todayIdx else (trendData.points.size - 1).coerceAtLeast(0)
        }
    }

    val selectedPoint = trendData.points.getOrNull(selectedIndex)
    val summary = trendData.consistencySummary

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_study_hours_recharts_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // 1. Header Row with Recharts Badge & Productivity Trend Pill
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
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF0070F3).copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = "Weekly Study Hours Chart",
                                tint = Color(0xFF0070F3),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Weekly Study Hours",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF0070F3).copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "RECHARTS",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0070F3),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Last 7 Days (Days ${trendData.startDay}–${trendData.endDay}) • Productivity Trend",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Consistency / Productivity Trend Pill
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${summary.consistencyScore}% Pace",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF059669)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. 7-Day Productivity Summary Metrics Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeeklyKpiPill(
                    title = "7-DAY TOTAL",
                    value = "${String.format(Locale.ENGLISH, "%.1f", summary.totalHours)}h",
                    subtitle = "/ ${summary.targetHours.toInt()}h goal",
                    accentColor = Color(0xFF0070F3),
                    modifier = Modifier.weight(1f)
                )
                WeeklyKpiPill(
                    title = "DAILY AVG",
                    value = "${String.format(Locale.ENGLISH, "%.1f", summary.averageHoursPerDay)}h",
                    subtitle = "per day",
                    accentColor = Color(0xFF8B5CF6),
                    modifier = Modifier.weight(1f)
                )
                WeeklyKpiPill(
                    title = "TARGETS MET",
                    value = "${summary.daysCompletedTarget}/${summary.totalDays}",
                    subtitle = "days on pace",
                    accentColor = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                WeeklyKpiPill(
                    title = "PEAK DAY",
                    value = "${String.format(Locale.ENGLISH, "%.1f", summary.bestDayHours)}h",
                    subtitle = "Day ${summary.bestDayNumber}",
                    accentColor = Color(0xFFF59E0B),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Recharts Visualization Mode Switcher & Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WeeklyRechartsStyle.entries.forEach { style ->
                        val active = chartStyle == style
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) Color(0xFF0070F3) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                            modifier = Modifier
                                .clickable { chartStyle = style }
                                .testTag("weekly_chart_style_${style.name.lowercase()}")
                        ) {
                            Text(
                                text = style.label,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Legend
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0070F3))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Logged", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Target", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. Recharts Interactive Plot Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(205.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f))
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .testTag("weekly_recharts_plot_container")
            ) {
                when (chartStyle) {
                    WeeklyRechartsStyle.COMPOSED_BAR_LINE -> {
                        RechartsComposedBarLineCanvas(
                            points = trendData.points,
                            maxScaleHours = trendData.maxScaleHours,
                            selectedIndex = selectedIndex,
                            onSelectIndex = { selectedIndex = it }
                        )
                    }
                    WeeklyRechartsStyle.MONOTONE_AREA -> {
                        RechartsWeeklyAreaSplineCanvas(
                            points = trendData.points,
                            maxScaleHours = trendData.maxScaleHours,
                            selectedIndex = selectedIndex,
                            onSelectIndex = { selectedIndex = it }
                        )
                    }
                    WeeklyRechartsStyle.RECHARTS_WEB_SVG -> {
                        RechartsWeeklyWebView(
                            points = trendData.points,
                            maxScaleHours = trendData.maxScaleHours,
                            selectedIndex = selectedIndex,
                            onSelectIndex = { selectedIndex = it },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 5. X-Axis Interactive 7-Day Selector Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trendData.points.forEachIndexed { idx, pt ->
                    val isSelected = idx == selectedIndex
                    val isMet = pt.isTargetMet

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isSelected) Color(0xFF0070F3).copy(alpha = 0.12f)
                                else Color.Transparent
                            )
                            .clickable { selectedIndex = idx }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = pt.dayOfWeek,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF0070F3) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Surface(
                            shape = CircleShape,
                            color = when {
                                isSelected -> Color(0xFF0070F3)
                                isMet -> Color(0xFF10B981).copy(alpha = 0.18f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            },
                            modifier = Modifier.size(22.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${pt.dayNumber}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isSelected -> Color.White
                                        isMet -> Color(0xFF059669)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${String.format(Locale.ENGLISH, "%.1f", pt.completedHours)}h",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isMet) Color(0xFF059669) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 6. Selected Day Recharts Tooltip & Quick Hour Logger
            if (selectedPoint != null) {
                Spacer(modifier = Modifier.height(12.dp))
                SelectedDayProductivityTooltip(
                    point = selectedPoint,
                    onJumpToDay = { onSelectDay(selectedPoint.dayNumber) },
                    onAdjustHours = { newHours ->
                        onUpdateDayHours(selectedPoint.dayNumber, newHours.coerceIn(0f, 18f))
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7. Footer Action Link to Full Study Hours & Weekly Review
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onOpenFullAnalytics() }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = summary.summaryNarrative,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Full Analytics",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0070F3)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF0070F3),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyKpiPill(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.22f)),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            Text(
                text = title,
                fontSize = 8.5.sp,
                fontWeight = FontWeight.Black,
                color = accentColor,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
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
private fun SelectedDayProductivityTooltip(
    point: DailyStudyHoursPoint,
    onJumpToDay: () -> Unit,
    onAdjustHours: (Float) -> Unit
) {
    val phyHours = (point.completedHours * 0.36f)
    val chemHours = (point.completedHours * 0.32f)
    val mathHours = (point.completedHours * 0.32f)

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(1.dp, Color(0xFF0070F3).copy(alpha = 0.25f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0070F3).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "DAY ${point.dayNumber} • ${point.dayOfWeek.uppercase()}",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0070F3),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = point.dateLabel,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (point.isToday) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF10B981)
                        ) {
                            Text(
                                text = "TODAY",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                // Quick +/- Study Hour Logger Controls
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .size(26.dp)
                            .clickable { onAdjustHours(point.completedHours - 0.5f) }
                            .testTag("weekly_chart_minus_hours")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Remove,
                                contentDescription = "Decrease hours",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    Text(
                        text = "${String.format(Locale.ENGLISH, "%.1f", point.completedHours)}h / ${point.targetHours.toInt()}h",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Black,
                        color = if (point.isTargetMet) Color(0xFF059669) else Color(0xFF0070F3)
                    )

                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0070F3),
                        modifier = Modifier
                            .size(26.dp)
                            .clickable { onAdjustHours(point.completedHours + 0.5f) }
                            .testTag("weekly_chart_plus_hours")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Increase hours",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subject Split Breakdown Row (Physics / Chemistry / Maths) + Open Day Planner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Phy: ${String.format(Locale.ENGLISH, "%.1f", phyHours)}h",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0070F3)
                    )
                    Text(
                        text = "Chem: ${String.format(Locale.ENGLISH, "%.1f", chemHours)}h",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF10B981)
                    )
                    Text(
                        text = "Math: ${String.format(Locale.ENGLISH, "%.1f", mathHours)}h",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8B5CF6)
                    )
                }

                Text(
                    text = "Inspect Day ${point.dayNumber} →",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0070F3),
                    modifier = Modifier.clickable { onJumpToDay() }
                )
            }
        }
    }
}

/**
 * Recharts <ComposedChart> Native Renderer:
 * Draws rounded vertical gradient <Bar dataKey="completedHours" /> +
 * smooth monotone <Line dataKey="targetHours" /> + <ReferenceLine y={12} /> + <Tooltip cursor />
 */
@Composable
private fun RechartsComposedBarLineCanvas(
    points: List<DailyStudyHoursPoint>,
    maxScaleHours: Float,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit
) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(points) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(650, easing = FastOutSlowInEasing))
    }
    val progress = anim.value

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(points) {
                detectTapGestures { offset ->
                    val padL = 36f
                    val padR = 20f
                    val plotW = size.width - padL - padR
                    val slotW = if (points.isNotEmpty()) plotW / points.size else plotW
                    val idx = ((offset.x - padL) / slotW).toInt().coerceIn(0, (points.size - 1).coerceAtLeast(0))
                    onSelectIndex(idx)
                }
            }
            .pointerInput(points) {
                detectDragGestures { change, _ ->
                    val padL = 36f
                    val padR = 20f
                    val plotW = size.width - padL - padR
                    val slotW = if (points.isNotEmpty()) plotW / points.size else plotW
                    val idx = ((change.position.x - padL) / slotW).toInt().coerceIn(0, (points.size - 1).coerceAtLeast(0))
                    onSelectIndex(idx)
                }
            }
    ) {
        val padL = 38f
        val padR = 18f
        val padT = 20f
        val padB = 24f
        val plotW = size.width - padL - padR
        val plotH = size.height - padT - padB
        val scaleMax = maxScaleHours.coerceAtLeast(16f)

        val axisPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#8E8E93")
            textSize = 21f
            isAntiAlias = true
        }

        // 1. CartesianGrid horizontal dashed lines & YAxis labels
        val steps = 4
        for (i in 0..steps) {
            val ratio = i.toFloat() / steps
            val y = padT + plotH * (1f - ratio)
            val labelVal = (scaleMax * ratio).roundToInt()

            drawLine(
                color = Color(0xFFD1D5DB).copy(alpha = 0.45f),
                start = Offset(padL, y),
                end = Offset(size.width - padR, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
            drawContext.canvas.nativeCanvas.drawText("${labelVal}h", 4f, y + 7f, axisPaint)
        }

        if (points.isEmpty()) return@Canvas

        val slotW = plotW / points.size
        val barW = (slotW * 0.48f).coerceIn(14f, 44f)

        // 2. Draw Bars (<Bar dataKey="completedHours" radius={[8, 8, 0, 0]} />)
        val targetCoords = mutableListOf<Offset>()
        val barTopCoords = mutableListOf<Offset>()

        points.forEachIndexed { idx, pt ->
            val centerX = padL + slotW * (idx + 0.5f)
            val barRatio = (pt.completedHours / scaleMax).coerceIn(0f, 1f) * progress
            val barH = plotH * barRatio
            val topY = padT + plotH - barH

            val targetRatio = (pt.targetHours / scaleMax).coerceIn(0f, 1f)
            val targetY = padT + plotH * (1f - targetRatio)
            targetCoords.add(Offset(centerX, targetY))
            barTopCoords.add(Offset(centerX, topY))

            val isSelected = idx == selectedIndex
            if (isSelected) {
                // Recharts active cursor highlight band
                drawRoundRect(
                    color = Color(0xFF0070F3).copy(alpha = 0.08f),
                    topLeft = Offset(centerX - slotW * 0.42f, padT),
                    size = Size(slotW * 0.84f, plotH),
                    cornerRadius = CornerRadius(10f, 10f)
                )
            }

            val barBrush = if (pt.isTargetMet) {
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF10B981), Color(0xFF059669)),
                    startY = topY,
                    endY = padT + plotH
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0070F3), Color(0xFF2563EB)),
                    startY = topY,
                    endY = padT + plotH
                )
            }

            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(centerX - barW / 2f, topY),
                size = Size(barW, barH.coerceAtLeast(4f)),
                cornerRadius = CornerRadius(10f, 10f)
            )

            // Value label above bar
            val valPaint = android.graphics.Paint().apply {
                color = if (isSelected) android.graphics.Color.parseColor("#0070F3")
                else android.graphics.Color.parseColor("#6B7280")
                textSize = if (isSelected) 21f else 18f
                isFakeBoldText = isSelected
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText(
                "${String.format(Locale.ENGLISH, "%.1f", pt.completedHours)}h",
                centerX,
                (topY - 6f).coerceAtLeast(14f),
                valPaint
            )

            // XAxis day label at bottom
            val xPaint = android.graphics.Paint().apply {
                color = if (isSelected) android.graphics.Color.parseColor("#0070F3")
                else android.graphics.Color.parseColor("#6B7280")
                textSize = 20f
                isFakeBoldText = isSelected
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            drawContext.canvas.nativeCanvas.drawText(
                pt.dayOfWeek,
                centerX,
                size.height - 4f,
                xPaint
            )
        }

        // 3. Draw Target Pace Trend Line (<Line type="monotone" dataKey="targetHours" stroke="#F59E0B" />)
        if (targetCoords.size > 1) {
            val targetPath = Path()
            targetPath.moveTo(targetCoords.first().x, targetCoords.first().y)
            for (i in 0 until targetCoords.size - 1) {
                val p0 = targetCoords[i]
                val p1 = targetCoords[i + 1]
                val cx = (p0.x + p1.x) / 2f
                targetPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            }
            drawPath(
                path = targetPath,
                color = Color(0xFFF59E0B),
                style = Stroke(
                    width = 2.2.dp.toPx(),
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                )
            )
            targetCoords.forEach { pt ->
                drawCircle(color = Color.White, radius = 4.5f, center = pt)
                drawCircle(color = Color(0xFFF59E0B), radius = 3f, center = pt)
            }
        }
    }
}

/**
 * Recharts <AreaChart> Monotone Spline Renderer:
 * Smooth cubic interpolation + translucent vertical gradient fill + target reference line
 */
@Composable
private fun RechartsWeeklyAreaSplineCanvas(
    points: List<DailyStudyHoursPoint>,
    maxScaleHours: Float,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit
) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(points) {
        anim.snapTo(0f)
        anim.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
    }
    val progress = anim.value

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(points) {
                detectTapGestures { offset ->
                    val padL = 38f
                    val padR = 20f
                    val plotW = size.width - padL - padR
                    val stepX = if (points.size > 1) plotW / (points.size - 1) else plotW
                    val idx = ((offset.x - padL + stepX / 2f) / stepX).toInt().coerceIn(0, (points.size - 1).coerceAtLeast(0))
                    onSelectIndex(idx)
                }
            }
            .pointerInput(points) {
                detectDragGestures { change, _ ->
                    val padL = 38f
                    val padR = 20f
                    val plotW = size.width - padL - padR
                    val stepX = if (points.size > 1) plotW / (points.size - 1) else plotW
                    val idx = ((change.position.x - padL + stepX / 2f) / stepX).toInt().coerceIn(0, (points.size - 1).coerceAtLeast(0))
                    onSelectIndex(idx)
                }
            }
    ) {
        val padL = 38f
        val padR = 20f
        val padT = 20f
        val padB = 24f
        val plotW = size.width - padL - padR
        val plotH = size.height - padT - padB
        val scaleMax = maxScaleHours.coerceAtLeast(16f)

        val axisPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#8E8E93")
            textSize = 21f
            isAntiAlias = true
        }

        for (i in 0..4) {
            val ratio = i / 4f
            val y = padT + plotH * (1f - ratio)
            val labelVal = (scaleMax * ratio).roundToInt()
            drawLine(
                color = Color(0xFFD1D5DB).copy(alpha = 0.45f),
                start = Offset(padL, y),
                end = Offset(size.width - padR, y),
                strokeWidth = 1f
            )
            drawContext.canvas.nativeCanvas.drawText("${labelVal}h", 4f, y + 7f, axisPaint)
        }

        // ReferenceLine y = 12h
        val targetY = padT + plotH * (1f - 12f / scaleMax)
        drawLine(
            color = Color(0xFFF59E0B),
            start = Offset(padL, targetY),
            end = Offset(size.width - padR, targetY),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
        )

        if (points.isEmpty()) return@Canvas

        val stepX = if (points.size > 1) plotW / (points.size - 1) else plotW
        val coords = points.mapIndexed { idx, pt ->
            val x = padL + idx * stepX
            val yRatio = (pt.completedHours / scaleMax).coerceIn(0f, 1f) * progress
            val y = padT + plotH * (1f - yRatio)
            Offset(x, y)
        }

        val linePath = Path()
        val areaPath = Path()
        linePath.moveTo(coords.first().x, coords.first().y)
        areaPath.moveTo(coords.first().x, padT + plotH)
        areaPath.lineTo(coords.first().x, coords.first().y)

        for (i in 0 until coords.size - 1) {
            val p0 = coords[i]
            val p1 = coords[i + 1]
            val cx = (p0.x + p1.x) / 2f
            linePath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
            areaPath.cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
        }
        areaPath.lineTo(coords.last().x, padT + plotH)
        areaPath.close()

        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0070F3).copy(alpha = 0.32f),
                    Color(0xFF10B981).copy(alpha = 0.10f),
                    Color(0xFF0070F3).copy(alpha = 0.01f)
                ),
                startY = padT,
                endY = padT + plotH
            )
        )

        drawPath(
            path = linePath,
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFF0070F3), Color(0xFF10B981))
            ),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        if (selectedIndex in coords.indices) {
            val sel = coords[selectedIndex]
            drawLine(
                color = Color(0xFF0070F3).copy(alpha = 0.5f),
                start = Offset(sel.x, padT),
                end = Offset(sel.x, padT + plotH),
                strokeWidth = 1.5.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        }

        coords.forEachIndexed { idx, c ->
            val pt = points[idx]
            val isSel = idx == selectedIndex
            val dotColor = if (pt.isTargetMet) Color(0xFF10B981) else Color(0xFF0070F3)
            if (isSel) {
                drawCircle(color = dotColor.copy(alpha = 0.22f), radius = 14f, center = c)
                drawCircle(color = Color.White, radius = 8f, center = c)
                drawCircle(color = dotColor, radius = 5.5f, center = c)
            } else {
                drawCircle(color = Color.White, radius = 5.5f, center = c)
                drawCircle(color = dotColor, radius = 3.8f, center = c)
            }
        }
    }
}

/**
 * Embedded Recharts Web SVG Engine for 7-Day Weekly Study Hours Summary
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun RechartsWeeklyWebView(
    points: List<DailyStudyHoursPoint>,
    maxScaleHours: Float,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val jsBridge = remember {
        object {
            @JavascriptInterface
            fun onSelectIndexFromRecharts(idx: Int) {
                onSelectIndex(idx)
            }
        }
    }

    val html = remember(points, maxScaleHours, selectedIndex) {
        buildRechartsWeeklySvgHtml(points, maxScaleHours, selectedIndex)
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                setBackgroundColor(android.graphics.Color.TRANSPARENT)
                addJavascriptInterface(jsBridge, "RechartsBridge")
                webViewClient = WebViewClient()
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}

private fun buildRechartsWeeklySvgHtml(
    points: List<DailyStudyHoursPoint>,
    maxScaleHours: Float,
    selectedIndex: Int
): String {
    val jsonArray = buildString {
        append("[")
        points.forEachIndexed { idx, pt ->
            append(
                "{\"idx\":$idx,\"day\":${pt.dayNumber},\"dow\":\"${pt.dayOfWeek}\",\"date\":\"${pt.dateLabel}\",\"h\":${
                    String.format(Locale.ENGLISH, "%.1f", pt.completedHours)
                },\"t\":${String.format(Locale.ENGLISH, "%.1f", pt.targetHours)},\"met\":${pt.isTargetMet}}"
            )
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
        body { background: transparent; overflow: hidden; width: 100vw; height: 100vh; }
        svg { width: 100%; height: 100%; overflow: visible; }
        .recharts-cartesian-grid-horizontal line { stroke: rgba(156, 163, 175, 0.35); stroke-dasharray: 3 3; }
        .recharts-text { font-size: 10px; fill: #6B7280; font-weight: 600; }
      </style>
    </head>
    <body>
      <div id="recharts-responsive-container" style="width:100%;height:100%;"></div>
      <script>
        const data = $jsonArray;
        const selIdx = $selectedIndex;
        const maxH = ${maxScaleHours.coerceAtLeast(16f)};
        const root = document.getElementById('recharts-responsive-container');
        const W = root.clientWidth || 340;
        const H = root.clientHeight || 185;
        const padL = 32, padR = 14, padT = 18, padB = 22;
        const plotW = W - padL - padR;
        const plotH = H - padT - padB;
        const n = Math.max(1, data.length);
        const slotW = plotW / n;

        function xCenter(i) { return padL + slotW * (i + 0.5); }
        function yVal(v) { return padT + plotH * (1 - Math.min(1, Math.max(0, v / maxH))); }

        let svg = `<svg class="recharts-surface" viewBox="0 0 ${'$'}{W} ${'$'}{H}">`;
        svg += `<defs>
          <linearGradient id="rechartsAreaGrad" x1="0" y1="0" x2="0" y2="1">
            <stop offset="0%" stop-color="#0070F3" stop-opacity="0.34"/>
            <stop offset="100%" stop-color="#0070F3" stop-opacity="0.02"/>
          </linearGradient>
        </defs>`;

        svg += `<g class="recharts-cartesian-grid-horizontal">`;
        for (let i = 0; i <= 4; i++) {
          const frac = i / 4;
          const y = padT + plotH * (1 - frac);
          svg += `<line x1="${'$'}{padL}" y1="${'$'}{y}" x2="${'$'}{W - padR}" y2="${'$'}{y}" />`;
          svg += `<text x="${'$'}{padL - 5}" y="${'$'}{y + 3}" text-anchor="end" class="recharts-text">${'$'}{Math.round(frac * maxH)}h</text>`;
        }
        svg += `</g>`;

        // Area + Monotone Spline
        let areaD = `M ${'$'}{xCenter(0)} ${'$'}{padT + plotH} L ${'$'}{xCenter(0)} ${'$'}{yVal(data[0].h)}`;
        let lineD = `M ${'$'}{xCenter(0)} ${'$'}{yVal(data[0].h)}`;
        for (let i = 0; i < n - 1; i++) {
          const x0 = xCenter(i), y0 = yVal(data[i].h);
          const x1 = xCenter(i + 1), y1 = yVal(data[i + 1].h);
          const cx = (x0 + x1) / 2;
          lineD += ` C ${'$'}{cx} ${'$'}{y0}, ${'$'}{cx} ${'$'}{y1}, ${'$'}{x1} ${'$'}{y1}`;
          areaD += ` C ${'$'}{cx} ${'$'}{y0}, ${'$'}{cx} ${'$'}{y1}, ${'$'}{x1} ${'$'}{y1}`;
        }
        areaD += ` L ${'$'}{xCenter(n - 1)} ${'$'}{padT + plotH} Z`;
        svg += `<path class="recharts-area-area" d="${'$'}{areaD}" fill="url(#rechartsAreaGrad)" />`;
        svg += `<path class="recharts-area-curve" d="${'$'}{lineD}" fill="none" stroke="#0070F3" stroke-width="2.5" />`;

        // Bars & Dots
        const barW = Math.min(26, slotW * 0.42);
        data.forEach((pt, i) => {
          const cx = xCenter(i);
          const cy = yVal(pt.h);
          const bh = Math.max(4, (padT + plotH) - cy);
          const color = pt.met ? "#10B981" : "#0070F3";
          if (i === selIdx) {
            svg += `<rect x="${'$'}{cx - slotW * 0.4}" y="${'$'}{padT}" width="${'$'}{slotW * 0.8}" height="${'$'}{plotH}" rx="6" fill="rgba(0,112,243,0.08)" />`;
          }
          svg += `<rect class="recharts-bar-rectangle" x="${'$'}{cx - barW / 2}" y="${'$'}{cy}" width="${'$'}{barW}" height="${'$'}{bh}" rx="5" fill="${'$'}{color}" fill-opacity="0.78" />`;
          svg += `<circle cx="${'$'}{cx}" cy="${'$'}{cy}" r="${'$'}{i === selIdx ? 5.5 : 3.5}" fill="#ffffff" stroke="${'$'}{color}" stroke-width="2.5" />`;
          svg += `<text x="${'$'}{cx}" y="${'$'}{H - 5}" text-anchor="middle" class="recharts-text">${'$'}{pt.dow}</text>`;
        });

        svg += `</svg>`;
        root.innerHTML = svg;

        root.addEventListener('click', function(e) {
          const rect = root.getBoundingClientRect();
          const relX = e.clientX - rect.left - padL;
          const idx = Math.max(0, Math.min(n - 1, Math.floor(relX / slotW)));
          if (window.RechartsBridge && window.RechartsBridge.onSelectIndexFromRecharts) {
            window.RechartsBridge.onSelectIndexFromRecharts(idx);
          }
        });
      </script>
    </body>
    </html>
    """.trimIndent()
}
