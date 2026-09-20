package com.example.auramusic.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import com.example.auramusic.data.AuraThemeMode

@Composable
fun AuraMusicTheme(
    themeMode: AuraThemeMode = AuraThemeMode.DARK,
    dynamicAccentColor: Color = AuraAccentPurple,
    blurIntensity: Float = 0.6f,
    content: @Composable () -> Unit
) {
    val animatedAccent by animateColorAsState(
        targetValue = dynamicAccentColor,
        animationSpec = tween(durationMillis = 600),
        label = "theme_accent"
    )

    val themeColors = when (themeMode) {
        AuraThemeMode.DARK -> DarkThemeColors
        AuraThemeMode.LIGHT -> LightThemeColors
        AuraThemeMode.LIQUID_GLASS -> LiquidGlassThemeColors.copy(
            glassAlpha = blurIntensity.coerceIn(0.2f, 0.9f)
        )
    }

    val isDark = themeMode != AuraThemeMode.LIGHT

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = animatedAccent,
            onPrimary = Color.Black,
            primaryContainer = themeColors.surfaceVariant,
            onPrimaryContainer = themeColors.textPrimary,
            secondary = themeColors.textSecondary,
            onSecondary = Color.Black,
            secondaryContainer = themeColors.surfaceElevated,
            onSecondaryContainer = themeColors.textPrimary,
            tertiary = animatedAccent,
            onTertiary = Color.Black,
            background = themeColors.background,
            onBackground = themeColors.textPrimary,
            surface = themeColors.surface,
            onSurface = themeColors.textPrimary,
            surfaceVariant = themeColors.surfaceVariant,
            onSurfaceVariant = themeColors.textSecondary,
            outline = themeColors.border,
            outlineVariant = themeColors.borderLight
        )
    } else {
        lightColorScheme(
            primary = animatedAccent,
            onPrimary = Color.White,
            primaryContainer = themeColors.surfaceVariant,
            onPrimaryContainer = themeColors.textPrimary,
            secondary = themeColors.textSecondary,
            onSecondary = Color.White,
            secondaryContainer = themeColors.surfaceElevated,
            onSecondaryContainer = themeColors.textPrimary,
            tertiary = animatedAccent,
            onTertiary = Color.White,
            background = themeColors.background,
            onBackground = themeColors.textPrimary,
            surface = themeColors.surface,
            onSurface = themeColors.textPrimary,
            surfaceVariant = themeColors.surfaceVariant,
            onSurfaceVariant = themeColors.textSecondary,
            outline = themeColors.border,
            outlineVariant = themeColors.borderLight
        )
    }

    CompositionLocalProvider(
        LocalAuraTheme provides themeColors,
        LocalDynamicAccent provides animatedAccent
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
