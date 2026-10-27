package com.example.ui.components

import android.os.Build
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Reusable Glassmorphism Container with:
 * - Semi-transparent background (frosted glass)
 * - Optional hardware blur effect for Android 12+ (Build.VERSION_CODES.S)
 * - Soft border glow / highlight gradient simulating refracted light at the rim
 * - Subtle gradient overlay reflecting directional light
 * - Soft depth border shadow
 */
@Composable
fun GlassmorphicContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(22.dp),
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    rimHighlightColor: Color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
    rimShadowColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
    glowBorder: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.20f),
    borderWidth: Dp = 1.dp,
    elevation: Dp = 2.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = elevation,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = 0.04f),
                spotColor = Color.Black.copy(alpha = 0.08f)
            )
            .clip(shape)
            // Frosted background blur layer (supported smoothly on Android S+)
            .then(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Modifier.blur(0.5.dp)
                } else Modifier
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = (backgroundColor.alpha * 0.96f).coerceIn(0f, 1f))
                    )
                )
            )
            // Subtle directional lighting gradient overlay for that authentic translucent sheen
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.05f),
                        Color.Transparent,
                        Color(0xFF0070F3).copy(alpha = 0.03f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            // Soft refracted rim border: lighter on top-left, subtly dark on bottom-right
            .border(
                BorderStroke(
                    borderWidth,
                    Brush.linearGradient(
                        colors = listOf(
                            rimHighlightColor,
                            glowBorder,
                            rimShadowColor
                        )
                    )
                ),
                shape = shape
            )
    ) {
        content()
    }
}

/**
 * Tactile Neumorphic Container for task cards & interactive items.
 */
@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    baseColor: Color = MaterialTheme.colorScheme.surface,
    lightColor: Color = MaterialTheme.colorScheme.outlineVariant,
    shadowColor: Color = Color(0xFF050811),
    isSunken: Boolean = false,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        Modifier.clickable { onClick() }
    } else Modifier

    Box(
        modifier = modifier
            // Outer elevated drop shadow for extruded look
            .then(
                if (!isSunken) {
                    Modifier.shadow(
                        elevation = 3.dp,
                        shape = shape,
                        ambientColor = Color.Black.copy(alpha = 0.04f),
                        spotColor = Color.Black.copy(alpha = 0.08f)
                    )
                } else {
                    Modifier.shadow(
                        elevation = 0.5.dp,
                        shape = shape,
                        ambientColor = Color.Transparent,
                        spotColor = Color.Transparent
                    )
                }
            )
            .clip(shape)
            // Surface bevel gradient simulating convex vs concave neumorphic lighting
            .background(
                brush = if (!isSunken) {
                    Brush.linearGradient(
                        colors = listOf(
                            baseColor,
                            baseColor.copy(alpha = 0.97f)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                } else {
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF2F2F7),
                            baseColor
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                }
            )
            // Dual-tone border
            .border(
                BorderStroke(
                    1.dp,
                    if (!isSunken) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE5E5EA),
                                Color(0xFFE5E5EA).copy(alpha = 0.7f)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0071E3).copy(alpha = 0.4f),
                                Color(0xFFE5E5EA)
                            ),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    }
                ),
                shape = shape
            )
            .then(clickableModifier)
    ) {
        content()
    }
}
