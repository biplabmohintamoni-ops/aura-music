package com.example.auramusic.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// ==========================================
// Base Color Definitions
// ==========================================

// Premium Dark Color Palette (Inspired by Echo Music reference)
val AuraBackground = Color(0xFF0A0A0C)          // Deep obsidian dark background
val AuraSurface = Color(0xFF121216)             // Primary surface for sheets & bars
val AuraSurfaceElevated = Color(0xFF18181E)     // Elevated card background
val AuraSurfaceVariant = Color(0xFF202026)      // Subtle container / input background
val AuraCapsuleNav = Color(0xFF191920)          // Floating bottom capsule navigation bar

// Borders & Dividers
val AuraBorder = Color(0xFF26262E)              // Very subtle border
val AuraBorderLight = Color(0xFF32323C)         // Slightly visible card outline

// Typography
val AuraTextPrimary = Color(0xFFFFFFFF)         // White text
val AuraTextSecondary = Color(0xFF9E9EA8)       // Soft neutral gray
val AuraTextMuted = Color(0xFF666672)           // Muted gray metadata

// Accents & Actions
val AuraPrimary = Color(0xFF1DB954)             // Restrained music green accent
val AuraAccentPurple = Color(0xFFA855F7)        // Purple accent from settings reference
val AuraAccentPill = Color(0xFFE5D5FA)          // Light lavender pill from screenshot
val AuraAccentPillDark = Color(0xFF2C2738)      // Dark lavender container
val AuraLikeHeart = Color(0xFFFF3B5C)           // Red/Pink heart for liked tracks
val AuraBlue = Color(0xFF388BFD)                // Device / Cast blue

// ==========================================
// Theme Data Class & Palettes
// ==========================================

data class AuraThemeColors(
    val background: Color,
    val surface: Color,
    val surfaceElevated: Color,
    val surfaceVariant: Color,
    val capsuleNav: Color,
    val border: Color,
    val borderLight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val isGlass: Boolean = false,
    val glassAlpha: Float = 1.0f
)

val DarkThemeColors = AuraThemeColors(
    background = Color(0xFF0A0A0C),
    surface = Color(0xFF121216),
    surfaceElevated = Color(0xFF18181E),
    surfaceVariant = Color(0xFF202026),
    capsuleNav = Color(0xFF191920),
    border = Color(0xFF26262E),
    borderLight = Color(0xFF32323C),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFF9E9EA8),
    textMuted = Color(0xFF666672),
    isGlass = false,
    glassAlpha = 1.0f
)

val LightThemeColors = AuraThemeColors(
    background = Color(0xFFF6F6F9),
    surface = Color(0xFFFFFFFF),
    surfaceElevated = Color(0xFFF0F0F4),
    surfaceVariant = Color(0xFFE8E8EE),
    capsuleNav = Color(0xFFFFFFFF),
    border = Color(0xFFE2E2EA),
    borderLight = Color(0xFFD4D4DE),
    textPrimary = Color(0xFF111116),
    textSecondary = Color(0xFF555562),
    textMuted = Color(0xFF888896),
    isGlass = false,
    glassAlpha = 1.0f
)

val LiquidGlassThemeColors = AuraThemeColors(
    background = Color(0xFF08080C),
    surface = Color(0x331C1C28),
    surfaceElevated = Color(0x40252536),
    surfaceVariant = Color(0x2E2A2A3C),
    capsuleNav = Color(0x551E1E2C),
    border = Color(0x35FFFFFF),
    borderLight = Color(0x4AFFFFFF),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFD0D0DC),
    textMuted = Color(0xFF9494A8),
    isGlass = true,
    glassAlpha = 0.65f
)

val LocalAuraTheme = compositionLocalOf { DarkThemeColors }
val LocalDynamicAccent = compositionLocalOf { AuraAccentPurple }
