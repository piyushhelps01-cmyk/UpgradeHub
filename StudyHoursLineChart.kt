package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import com.example.data.model.ConsistencySummary
import com.example.data.model.DailyStudyHoursPoint
import com.example.data.model.SevenDayTrendData
import kotlin.math.roundToInt

/**
 * Modern Recharts-inspired Native Compose Line Chart with:
 * - Smooth Monotone Cubic Bézier Spline Curves
 * - Translucent Area Gradient Fading Downward (<Area type="monotone" />)
 * - Dotted Reference Target Line (<ReferenceLine y={12} />)
 * - Interactive Touch Scrubber / Floating Tooltip with Day & Hours Inspection
 * - Glowing Data Dot Highlights
 * - 7-Day Consistency Summary & Metric Dashboard
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyHoursTrendCard(
    trendData: SevenDayTrendData,
    onSelectDay: (dayNumber: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember { mutableIntStateOf(-1) }

    // Auto-select today or latest day initially if none selected
    LaunchedEffect(trendData.points) {
        if (selectedIndex == -1 || selectedIndex >= trendData.points.size) {
            val todayIdx = trendData.points.indexOfFirst { it.isToday }
            selectedIndex = if (todayIdx >= 0) todayIdx else (trendData.points.size - 1).coerceAtLeast(0)
        }
    }

    val selectedPoint = trendData.points.getOrNull(selectedIndex)

    GlassmorphicContainer(
        modifier = modifier
            .fillMaxWidth()
            .testTag("study_hours_trend_card"),
        shape = RoundedCornerShape(24.dp),
        backgroundColor = MaterialTheme.colorScheme.surface,
        rimHighlightColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f),
        rimShadowColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f),
        glowBorder = Color(0xFF0071E3).copy(alpha = 0.15f),
        elevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // 1. Header with Recharts title styling & Quick Consistency Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF0071E3).copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ShowChart,
                                contentDescription = null,
                                tint = Color(0xFF0071E3),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "7-Day Study Hours Trend",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Days ${trendData.startDay}–${trendData.endDay} • Monotone Pace Curve",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Consistency Rating Pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(trendData.consistencySummary.ratingColorHex).copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(
                        width = 1.dp,
                        color = Color(trendData.consistencySummary.ratingColorHex).copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = Color(trendData.consistencySummary.ratingColorHex),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${trendData.consistencySummary.consistencyScore}% Consistent",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(trendData.consistencySummary.ratingColorHex)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Interactive Tooltip Card (Displays active point stats on touch/scrub)
            if (selectedPoint != null) {
                InteractivePointTooltip(
                    point = selectedPoint,
                    onTapJump = { onSelectDay(selectedPoint.dayNumber) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. The Canvas Line Chart (Recharts styled Monotone Bézier Spline + Area Gradient)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f))
                    .border(
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                RechartsMonotoneLineCanvas(
                    points = trendData.points,
                    maxScaleHours = trendData.maxScaleHours,
                    selectedIndex = selectedIndex,
                    onSelectIndex = { selectedIndex = it }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4. X-Axis Day Pills (Touch to select day)
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
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedIndex = idx }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = pt.dayOfWeek,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                            color = if (isSelected) Color(0xFF0071E3) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = CircleShape,
                            color = when {
                                isSelected -> Color(0xFF0071E3)
                                isMet -> Color(0xFF34C759).copy(alpha = 0.2f)
                                else -> Color.Transparent
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${pt.dayNumber}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when {
                                        isSelected -> Color.White
                                        isMet -> Color(0xFF248A3D)
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Consistency Summary Section
            ConsistencySummarySection(
                summary = trendData.consistencySummary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Interactive floating Recharts tooltip displaying inspected day metrics
 */
@Composable
private fun InteractivePointTooltip(
    point: DailyStudyHoursPoint,
    onTapJump: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF6574FF).copy(alpha = 0.25f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTapJump() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF6574FF).copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = "DAY ${point.dayNumber}",
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            color = Color(0xFF5266CF),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "${point.dateLabel} (${point.dayOfWeek})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    if (point.isToday) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF00C853)
                        ) {
                            Text(
                                text = "TODAY",
                                color = Color.White,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${String.format("%.1f", point.completedHours)}h",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = if (point.isTargetMet) Color(0xFF00C853) else Color(0xFF6574FF)
                    )
                    Text(
                        text = " / ${point.targetHours.toInt()}h target",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Pace pill & jump button
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (point.isTargetMet)
                        Color(0xFF00C853).copy(alpha = 0.2f)
                    else if (point.completedHours > 0f)
                        Color(0xFFFFA000).copy(alpha = 0.2f)
                    else
                        Color.Gray.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (point.isTargetMet)
                            "Target Met ✨"
                        else if (point.completedHours > 0f)
                            "${String.format("%.1f", point.targetHours - point.completedHours)}h left"
                        else
                            "Not Started",
                        color = if (point.isTargetMet)
                            Color(0xFF00875A)
                        else if (point.completedHours > 0f)
                            Color(0xFFB26A00)
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Tap to switch to Day ${point.dayNumber} →",
                    fontSize = 10.sp,
                    color = Color(0xFF5266CF),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Recharts Monotone Cubic Bézier Canvas renderer:
 * Plots grid lines, target reference dashed line, gradient filled area,
 * curved spline stroke, data markers, and scrub cursor.
 */
@Composable
private fun RechartsMonotoneLineCanvas(
    points: List<DailyStudyHoursPoint>,
    maxScaleHours: Float,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit
) {
    val progressAnim = remember { Animatable(0f) }

    LaunchedEffect(points) {
        progressAnim.snapTo(0f)
        progressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val animatedProgress = progressAnim.value

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(points) {
                detectTapGestures { offset ->
                    val width = size.width
                    val paddingX = 32f
                    val chartWidth = width - paddingX * 2
                    val stepX = if (points.size > 1) chartWidth / (points.size - 1) else chartWidth

                    val clickedIdx = ((offset.x - paddingX + stepX / 2) / stepX)
                        .toInt()
                        .coerceIn(0, points.size - 1)
                    onSelectIndex(clickedIdx)
                }
            }
            .pointerInput(points) {
                detectDragGestures { change, _ ->
                    val width = size.width
                    val paddingX = 32f
                    val chartWidth = width - paddingX * 2
                    val stepX = if (points.size > 1) chartWidth / (points.size - 1) else chartWidth

                    val draggedIdx = ((change.position.x - paddingX + stepX / 2) / stepX)
                        .toInt()
                        .coerceIn(0, points.size - 1)
                    onSelectIndex(draggedIdx)
                }
            }
    ) {
        val width = size.width
        val height = size.height
        val paddingLeft = 36f
        val paddingRight = 24f
        val paddingTop = 24f
        val paddingBottom = 28f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom
        val scaleMax = maxScaleHours.coerceAtLeast(16f)

        // 1. Horizontal Reference Gridlines & Y-Axis Labels (0h, 4h, 8h, 12h, 16h)
        val gridSteps = 4
        val gridColor = Color(0xFFD1D1D6).copy(alpha = 0.5f)
        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#8E8E93")
            textSize = 22f
            isAntiAlias = true
        }

        for (i in 0..gridSteps) {
            val ratio = i.toFloat() / gridSteps
            val yPos = paddingTop + chartHeight * (1f - ratio)
            val hoursLabel = (scaleMax * ratio).roundToInt()

            // Grid line
            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, yPos),
                end = Offset(width - paddingRight, yPos),
                strokeWidth = 1f
            )

            // Y-axis label text
            drawContext.canvas.nativeCanvas.drawText(
                "${hoursLabel}h",
                8f,
                yPos + 8f,
                textPaint
            )
        }

        // 2. Dashed Reference Line for Target (12h base pace)
        val targetYPos = paddingTop + chartHeight * (1f - (12f / scaleMax))
        val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
        drawLine(
            color = Color(0xFF0071E3).copy(alpha = 0.45f),
            start = Offset(paddingLeft, targetYPos),
            end = Offset(width - paddingRight, targetYPos),
            strokeWidth = 1.5f,
            pathEffect = dashedEffect
        )

        // Reference line label pill in canvas
        val refPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#0071E3")
            textSize = 20f
            isAntiAlias = true
        }
        drawContext.canvas.nativeCanvas.drawText(
            "Target: 12h",
            width - paddingRight - 90f,
            targetYPos - 6f,
            refPaint
        )

        if (points.isEmpty()) return@Canvas

        // 3. Compute Coordinates for each point
        val stepX = if (points.size > 1) chartWidth / (points.size - 1) else chartWidth
        val coordinates = points.mapIndexed { idx, pt ->
            val x = paddingLeft + idx * stepX
            val rawYRatio = (pt.completedHours / scaleMax).coerceIn(0f, 1f)
            val animatedYRatio = rawYRatio * animatedProgress
            val y = paddingTop + chartHeight * (1f - animatedYRatio)
            Offset(x, y)
        }

        // 4. Build Monotone Cubic Bézier Spline Path
        val linePath = Path()
        val areaPath = Path()

        if (coordinates.isNotEmpty()) {
            linePath.moveTo(coordinates[0].x, coordinates[0].y)
            areaPath.moveTo(coordinates[0].x, paddingTop + chartHeight)
            areaPath.lineTo(coordinates[0].x, coordinates[0].y)

            for (i in 0 until coordinates.size - 1) {
                val p0 = if (i > 0) coordinates[i - 1] else coordinates[i]
                val p1 = coordinates[i]
                val p2 = coordinates[i + 1]
                val p3 = if (i + 2 < coordinates.size) coordinates[i + 2] else p2

                // Cubic control points for smooth monotone curve
                val tension = 0.25f
                val control1X = p1.x + (p2.x - p0.x) * tension
                val control1Y = p1.y + (p2.y - p0.y) * tension
                val control2X = p2.x - (p3.x - p1.x) * tension
                val control2Y = p2.y - (p3.y - p1.y) * tension

                linePath.cubicTo(control1X, control1Y, control2X, control2Y, p2.x, p2.y)
                areaPath.cubicTo(control1X, control1Y, control2X, control2Y, p2.x, p2.y)
            }

            areaPath.lineTo(coordinates.last().x, paddingTop + chartHeight)
            areaPath.close()

            // 5. Draw Translucent Gradient Area Fill (<Area fill="url(#gradient)" />)
            val areaBrush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0071E3).copy(alpha = 0.22f),
                    Color(0xFF34C759).copy(alpha = 0.08f),
                    Color(0xFF0071E3).copy(alpha = 0.01f)
                ),
                startY = paddingTop,
                endY = paddingTop + chartHeight
            )
            drawPath(path = areaPath, brush = areaBrush)

            // 6. Draw Smooth Monotone Line Stroke (<Line stroke="#0071E3" strokeWidth={3} />)
            val strokeBrush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF0071E3),
                    Color(0xFF34C759)
                ),
                startX = paddingLeft,
                endX = width - paddingRight
            )
            drawPath(
                path = linePath,
                brush = strokeBrush,
                style = Stroke(
                    width = 3.5f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }

        // 7. Draw Scrubber Vertical Cursor Line at selectedIndex (<Tooltip cursor />)
        if (selectedIndex in coordinates.indices) {
            val selCoord = coordinates[selectedIndex]
            drawLine(
                color = Color(0xFF0071E3).copy(alpha = 0.4f),
                start = Offset(selCoord.x, paddingTop),
                end = Offset(selCoord.x, paddingTop + chartHeight),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        }

        // 8. Draw Data Dots (Recharts Dot & ActiveDot)
        coordinates.forEachIndexed { idx, coord ->
            val pt = points[idx]
            val isSelected = idx == selectedIndex
            val isTargetMet = pt.isTargetMet

            val dotColor = when {
                isTargetMet -> Color(0xFF34C759)
                pt.completedHours > 0f -> Color(0xFF0071E3)
                else -> Color(0xFF8E8E93)
            }

            if (isSelected) {
                // Glowing outer pulse halo
                drawCircle(
                    color = Color(0xFF0071E3).copy(alpha = 0.20f),
                    radius = 16f,
                    center = coord
                )
                // Outer ring
                drawCircle(
                    color = Color.White,
                    radius = 9f,
                    center = coord
                )
                // Core dot
                drawCircle(
                    color = dotColor,
                    radius = 6f,
                    center = coord
                )
            } else {
                // Outer white halo
                drawCircle(
                    color = Color.White.copy(alpha = 0.95f),
                    radius = 5.5f,
                    center = coord
                )
                // Inner colored core
                drawCircle(
                    color = dotColor,
                    radius = 3.5f,
                    center = coord
                )
            }
        }
    }
}

/**
 * Consistency Summary metrics card displaying:
 * - Consistency Score gauge
 * - Average hours/day
 * - Total hours logged
 * - Days target met
 * - Current streak
 * - Actionable consistency narrative
 */
@Composable
private fun ConsistencySummarySection(
    summary: ConsistencySummary,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Insights,
                    contentDescription = null,
                    tint = Color(0xFF5266CF),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CONSISTENCY SUMMARY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF6574FF).copy(alpha = 0.12f)
            ) {
                Text(
                    text = summary.consistencyRating,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF5266CF),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Metrics Grid (4 Key Consistency Indicators)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Metric 1: 7-Day Average
            ConsistencyMetricTile(
                title = "7-DAY AVG",
                value = "${String.format("%.1f", summary.averageHoursPerDay)}h",
                subtitle = "of 12h daily pace",
                icon = Icons.Default.Speed,
                iconTint = Color(0xFF6574FF),
                modifier = Modifier.weight(1f)
            )

            // Metric 2: Total Hours
            ConsistencyMetricTile(
                title = "LOGGED",
                value = "${summary.totalHours.toInt()}h",
                subtitle = "of ${summary.targetHours.toInt()}h total",
                icon = Icons.Default.Schedule,
                iconTint = Color(0xFF00875A),
                modifier = Modifier.weight(1f)
            )

            // Metric 3: Targets Met
            ConsistencyMetricTile(
                title = "ON TARGET",
                value = "${summary.daysCompletedTarget}/7",
                subtitle = "days completed",
                icon = Icons.Default.CheckCircle,
                iconTint = Color(0xFF00639B),
                modifier = Modifier.weight(1f)
            )

            // Metric 4: Current Streak
            ConsistencyMetricTile(
                title = "STREAK",
                value = "${summary.currentStreak}d",
                subtitle = "active run",
                icon = Icons.Default.LocalFireDepartment,
                iconTint = Color(0xFFB26A00),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Narrative Summary Banner
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF5266CF),
                    modifier = Modifier
                        .size(16.dp)
                        .padding(top = 1.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Consistency Analysis",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = summary.summaryNarrative,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ConsistencyMetricTile(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onSurface
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
