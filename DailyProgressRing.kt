package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DailyProgressRing(
    progressPct: Float, // 0.0f to 1.0f
    completedCount: Int,
    totalCount: Int,
    completedHours: Float,
    targetHours: Float,
    modifier: Modifier = Modifier,
    size: Dp = 148.dp,
    strokeWidth: Dp = 12.dp
) {
    val clampedProgress = progressPct.coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = clampedProgress,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "progress_ring_anim"
    )

    // Subtle parallax float animation for dynamic interaction
    val infiniteTransition = rememberInfiniteTransition(label = "progress_ring_float")
    val floatOffsetY by infiniteTransition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_float_offset_y"
    )
    val ambientGlowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.20f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ring_ambient_glow"
    )
    val sweepRotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring_sweep_rotation"
    )

    val isComplete = clampedProgress >= 0.999f && totalCount > 0

    val primaryRingColor by animateColorAsState(
        targetValue = if (isComplete) Color(0xFF00C853) else Color(0xFF0070F3),
        animationSpec = tween(durationMillis = 400),
        label = "ring_color_primary"
    )

    val secondaryRingColor by animateColorAsState(
        targetValue = if (isComplete) Color(0xFF00E676) else Color(0xFF00A3FF),
        animationSpec = tween(durationMillis = 400),
        label = "ring_color_secondary"
    )

    val trackColor = Color(0xFF1E293B).copy(alpha = 0.6f)

    Box(
        modifier = modifier
            .offset(y = floatOffsetY.dp)
            .graphicsLayer {
                rotationZ = floatOffsetY * 0.35f
            }
            .size(size),
        contentAlignment = Alignment.Center
    ) {
        // Soft aura glow beneath the ring for depth
        Box(
            modifier = Modifier
                .size(size * 0.85f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            primaryRingColor.copy(alpha = ambientGlowAlpha),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidthPx = strokeWidth.toPx()
            val arcSize = Size(size.toPx() - strokeWidthPx, size.toPx() - strokeWidthPx)
            val topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)

            // 1. Background full track circle
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
            )

            // 2. Foreground animated progress arc with rotating gradient sweep
            if (animatedProgress > 0f) {
                val gradientBrush = Brush.sweepGradient(
                    colors = listOf(primaryRingColor, secondaryRingColor, primaryRingColor),
                    center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                )
                drawArc(
                    brush = gradientBrush,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                )
            }
        }

        // Center Content (with counter-float parallax for 3D multi-plane depth)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .offset(y = (-floatOffsetY * 0.3f).dp)
                .padding(10.dp)
        ) {
            val percentageInt = (animatedProgress * 100).toInt()

            if (isComplete) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Goals Completed",
                    tint = Color(0xFF00875A),
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = "$percentageInt%",
                fontSize = if (isComplete) 24.sp else 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isComplete) Color(0xFF00875A) else MaterialTheme.colorScheme.onSurface,
                lineHeight = 28.sp
            )

            Text(
                text = "$completedCount of $totalCount Done",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (targetHours > 0f) {
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isComplete) Color(0xFFC4EED0) else Color(0xFF6574FF).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = String.format("%.0fh / %.0fh", completedHours, targetHours),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isComplete) Color(0xFF004D20) else Color(0xFF5266CF),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
