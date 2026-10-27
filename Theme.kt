package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = UsoDarkPrimary,
    onPrimary = UsoDarkOnPrimary,
    primaryContainer = UsoDarkPrimaryContainer,
    onPrimaryContainer = UsoDarkOnPrimaryContainer,
    secondary = UsoDarkSecondary,
    onSecondary = UsoDarkOnSecondary,
    secondaryContainer = UsoDarkSecondaryContainer,
    onSecondaryContainer = UsoDarkOnSecondaryContainer,
    tertiary = UsoDarkTertiary,
    onTertiary = UsoDarkOnTertiary,
    tertiaryContainer = UsoDarkTertiaryContainer,
    onTertiaryContainer = UsoDarkOnTertiaryContainer,
    background = UsoDarkBackground,
    onBackground = UsoDarkOnBackground,
    surface = UsoDarkSurface,
    onSurface = UsoDarkOnSurface,
    surfaceVariant = UsoDarkSurfaceVariant,
    onSurfaceVariant = UsoDarkOnSurfaceVariant,
    outline = UsoDarkOutline,
    outlineVariant = UsoDarkOutline
)

private val LightColorScheme = lightColorScheme(
    primary = UsoLightPrimary,
    onPrimary = UsoLightOnPrimary,
    primaryContainer = UsoLightPrimaryContainer,
    onPrimaryContainer = UsoLightOnPrimaryContainer,
    secondary = UsoLightSecondary,
    onSecondary = UsoLightOnSecondary,
    secondaryContainer = UsoLightSecondaryContainer,
    onSecondaryContainer = UsoLightOnSecondaryContainer,
    tertiary = UsoLightTertiary,
    onTertiary = UsoLightOnTertiary,
    tertiaryContainer = UsoLightTertiaryContainer,
    onTertiaryContainer = UsoLightOnTertiaryContainer,
    background = UsoLightBackground,
    onBackground = UsoLightOnBackground,
    surface = UsoLightSurface,
    onSurface = UsoLightOnSurface,
    surfaceVariant = UsoLightSurfaceVariant,
    onSurfaceVariant = UsoLightOnSurfaceVariant,
    outline = UsoLightOutline,
    outlineVariant = UsoLightOutline
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
