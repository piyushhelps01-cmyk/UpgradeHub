package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private enum class ConfettiShape {
    CIRCLE, RIBBON, STAR, DIAMOND
}

private data class Particle(
    val initialXRatio: Float, // 0.0 to 1.0 relative width
    val initialYRatio: Float, // relative start height
    val velocityX: Float,     // horizontal speed
    val velocityY: Float,     // initial upward burst speed
    val gravity: Float,
    val size: Float,
    val color: Color,
    val shape: ConfettiShape,
    val initialRotation: Float,
    val rotationSpeed: Float,
    val swayFrequency: Float,
    val swayAmplitude: Float
)

private val CELEBRATION_COLORS = listOf(
    Color(0xFF00E676), // Emerald Green
    Color(0xFF00C853), // Deep Mint
    Color(0xFFFFD700), // Rich Gold
    Color(0xFFFFAB00), // Amber Gold
    Color(0xFF6574FF), // Electric Indigo
    Color(0xFF00E5FF), // Vivid Cyan
    Color(0xFFFF4081), // Coral Pink
    Color(0xFFB388FF)  // Soft Violet
)

@Composable
fun ConfettiAnimation(
    triggerKey: Any?,
    modifier: Modifier = Modifier,
    particleCount: Int = 48,
    durationMillis: Int = 2400
) {
    val progressAnim = remember { Animatable(0f) }

    val particles = remember(triggerKey) {
        val rand = Random(System.currentTimeMillis())
        List(particleCount) {
            val shape = when (rand.nextInt(4)) {
                0 -> ConfettiShape.RIBBON
                1 -> ConfettiShape.STAR
                2 -> ConfettiShape.DIAMOND
                else -> ConfettiShape.CIRCLE
            }
            Particle(
                initialXRatio = rand.nextFloat() * 0.8f + 0.1f, // spread across 10%..90% width
                initialYRatio = rand.nextFloat() * 0.25f + 0.05f, // start near top
                velocityX = (rand.nextFloat() - 0.5f) * 450f,
                velocityY = -rand.nextFloat() * 320f - 80f, // initial pop upwards
                gravity = rand.nextFloat() * 700f + 600f,
                size = rand.nextFloat() * 10f + 8f,
                color = CELEBRATION_COLORS[rand.nextInt(CELEBRATION_COLORS.size)],
                shape = shape,
                initialRotation = rand.nextFloat() * 360f,
                rotationSpeed = (rand.nextFloat() - 0.5f) * 720f,
                swayFrequency = rand.nextFloat() * 4f + 2f,
                swayAmplitude = rand.nextFloat() * 30f + 15f
            )
        }
    }

    LaunchedEffect(triggerKey) {
        if (triggerKey != null && triggerKey != false) {
            progressAnim.snapTo(0f)
            progressAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = durationMillis, easing = LinearEasing)
            )
        }
    }

    val progress = progressAnim.value
    if (progress > 0f && progress < 1f) {
        // Fade out in last 25% of animation
        val alpha = if (progress > 0.75f) {
            ((1f - progress) / 0.25f).coerceIn(0f, 1f)
        } else {
            1f
        }

        Canvas(modifier = modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val t = progress * (durationMillis / 1000f) // elapsed seconds

            particles.forEach { p ->
                val startX = p.initialXRatio * width
                val startY = p.initialYRatio * height

                // Kinematic equations
                val sway = sin(t * p.swayFrequency) * p.swayAmplitude
                val posX = startX + p.velocityX * t * 0.6f + sway
                val posY = startY + p.velocityY * t + 0.5f * p.gravity * t * t

                // Only draw if within bounds + margins
                if (posX in -40f..(width + 40f) && posY in -40f..(height + 40f)) {
                    val currentRotation = p.initialRotation + p.rotationSpeed * t
                    // Flutter tilt effect
                    val flipScaleY = cos(t * p.rotationSpeed * 0.05f)

                    val particleColor = p.color.copy(alpha = alpha * 0.92f)

                    rotate(degrees = currentRotation, pivot = Offset(posX, posY)) {
                        when (p.shape) {
                            ConfettiShape.CIRCLE -> {
                                drawCircle(
                                    color = particleColor,
                                    radius = p.size * 0.5f,
                                    center = Offset(posX, posY)
                                )
                            }
                            ConfettiShape.RIBBON -> {
                                val ribbonHeight = (p.size * 1.8f) * flipScaleY.coerceIn(-1f, 1f)
                                drawRect(
                                    color = particleColor,
                                    topLeft = Offset(posX - p.size * 0.4f, posY - ribbonHeight * 0.5f),
                                    size = Size(p.size * 0.8f, kotlin.math.abs(ribbonHeight).coerceAtLeast(2f))
                                )
                            }
                            ConfettiShape.DIAMOND -> {
                                val diamondPath = Path().apply {
                                    moveTo(posX, posY - p.size * 0.7f)
                                    lineTo(posX + p.size * 0.5f, posY)
                                    lineTo(posX, posY + p.size * 0.7f)
                                    lineTo(posX - p.size * 0.5f, posY)
                                    close()
                                }
                                drawPath(path = diamondPath, color = particleColor)
                            }
                            ConfettiShape.STAR -> {
                                drawStar(
                                    center = Offset(posX, posY),
                                    radius = p.size * 0.6f,
                                    color = particleColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawStar(center: Offset, radius: Float, color: Color) {
    val path = Path()
    val points = 5
    val innerRadius = radius * 0.45f
    var angle = -Math.PI / 2.0
    val step = Math.PI / points

    for (i in 0 until (points * 2)) {
        val r = if (i % 2 == 0) radius else innerRadius
        val x = center.x + (r * cos(angle)).toFloat()
        val y = center.y + (r * sin(angle)).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        angle += step
    }
    path.close()
    drawPath(path = path, color = color)
}
