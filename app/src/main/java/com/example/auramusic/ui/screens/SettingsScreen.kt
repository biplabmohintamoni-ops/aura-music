package com.example.auramusic.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.BlurOn
import androidx.compose.material.icons.filled.CastConnected
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auramusic.data.AuraThemeMode
import com.example.auramusic.data.provider.OwnerConfig
import com.example.auramusic.ui.theme.AuraAccentPurple
import com.example.auramusic.ui.theme.LocalAuraTheme
import com.example.auramusic.ui.theme.LocalDynamicAccent
import com.example.auramusic.viewmodel.MusicViewModel

@Composable
fun SettingsScreen(
    viewModel: MusicViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val theme = LocalAuraTheme.current
    val accent = LocalDynamicAccent.current

    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColorEnabled by viewModel.dynamicColorEnabled.collectAsState()
    val blurIntensity by viewModel.blurIntensity.collectAsState()
    val useAlbumArtColors by viewModel.useAlbumArtColors.collectAsState()
    val audioQuality by viewModel.audioQuality.collectAsState()
    val alwaysResumePlayback by viewModel.alwaysResumePlayback.collectAsState()
    val crossfadeSeconds by viewModel.crossfadeSeconds.collectAsState()
    val backgroundPlayback by viewModel.backgroundPlayback.collectAsState()
    val downloadQuality by viewModel.downloadQuality.collectAsState()
    val downloadWifiOnly by viewModel.downloadWifiOnly.collectAsState()
    val cacheSizeFormatted by viewModel.cacheSizeFormatted.collectAsState()
    val googleUser by viewModel.googleUser.collectAsState()
    val sleepTimerRemaining by viewModel.sleepTimerMinutesRemaining.collectAsState()

    var showAudioQualityDialog by remember { mutableStateOf(false) }
    var showDownloadQualityDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(theme.background)
            .testTag("settings_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.testTag("settings_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = theme.textPrimary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Settings",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
                Text(
                    text = "Customize your AURA MUSIC experience",
                    fontSize = 13.sp,
                    color = theme.textSecondary
                )
            }

            IconButton(onClick = { /* Quick search settings filter */ }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Settings",
                    tint = theme.textSecondary
                )
            }
        }

        // Scrollable Settings Sections
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ==========================================
            // 1. APPEARANCE SECTION
            // ==========================================
            SettingsCard(
                title = "Appearance",
                subtitle = "Themes, colors and visual experience",
                icon = Icons.Default.Palette,
                iconTint = Color(0xFFC084FC),
                iconBg = Color(0xFF581C87)
            ) {
                // Theme Selector Header
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp)) {
                    Text(
                        text = "Theme",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = theme.textPrimary
                    )
                    Text(
                        text = "Choose your preferred theme",
                        fontSize = 12.sp,
                        color = theme.textMuted
                    )
                }

                // Horizontal row of 3 Theme Preview Cards
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ThemePreviewCard(
                        title = "Dark",
                        subtitle = "Sleek & modern",
                        isSelected = themeMode == AuraThemeMode.DARK,
                        accentColor = accent,
                        theme = theme,
                        isBeta = false,
                        previewBg = Color(0xFF0F0F14),
                        onClick = { viewModel.setThemeMode(AuraThemeMode.DARK) },
                        modifier = Modifier.weight(1f),
                        testTag = "theme_dark_card"
                    )

                    ThemePreviewCard(
                        title = "Light",
                        subtitle = "Clean & minimal",
                        isSelected = themeMode == AuraThemeMode.LIGHT,
                        accentColor = accent,
                        theme = theme,
                        isBeta = false,
                        previewBg = Color(0xFFEFEFF5),
                        onClick = { viewModel.setThemeMode(AuraThemeMode.LIGHT) },
                        modifier = Modifier.weight(1f),
                        testTag = "theme_light_card"
                    )

                    ThemePreviewCard(
                        title = "Liquid Glass",
                        subtitle = "iOS 26 inspired",
                        isSelected = themeMode == AuraThemeMode.LIQUID_GLASS,
                        accentColor = accent,
                        theme = theme,
                        isBeta = true,
                        previewBg = Color(0xFF1E1B2E),
                        onClick = { viewModel.setThemeMode(AuraThemeMode.LIQUID_GLASS) },
                        modifier = Modifier.weight(1f),
                        testTag = "theme_glass_card"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Dynamic Color Toggle
                SettingsToggleRow(
                    icon = Icons.Default.Palette,
                    iconBg = Color(0xFF7C3AED),
                    title = "Dynamic Color",
                    subtitle = "UI color changes based on current song",
                    checked = dynamicColorEnabled,
                    accentColor = accent,
                    onCheckedChange = { viewModel.setDynamicColorEnabled(it) },
                    testTag = "toggle_dynamic_color"
                )

                // Blur Intensity Slider
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF3B82F6).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BlurOn,
                                contentDescription = null,
                                tint = Color(0xFF60A5FA),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Blur Intensity",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = theme.textPrimary
                            )
                            Text(
                                text = "Adjust liquid glass blur level (for Liquid Glass theme)",
                                fontSize = 12.sp,
                                color = theme.textMuted
                            )
                        }

                        Text(
                            text = "${(blurIntensity * 100).toInt()}%",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                    }

                    Slider(
                        value = blurIntensity,
                        onValueChange = { viewModel.setBlurIntensity(it) },
                        valueRange = 0.10f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = accent,
                            inactiveTrackColor = theme.borderLight
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .testTag("slider_blur_intensity")
                    )
                }

                // Use Album Art Colors
                SettingsToggleRow(
                    icon = Icons.Default.CropFree,
                    iconBg = Color(0xFFEC4899),
                    title = "Use Album Art Colors",
                    subtitle = "Extract colors from current song artwork",
                    checked = useAlbumArtColors,
                    accentColor = accent,
                    onCheckedChange = { viewModel.setUseAlbumArtColors(it) },
                    testTag = "toggle_album_art_colors"
                )
            }

            // ==========================================
            // 2. PLAYBACK SECTION
            // ==========================================
            SettingsCard(
                title = "Playback",
                subtitle = "Audio quality and playback behavior",
                icon = Icons.Default.PlayArrow,
                iconTint = Color(0xFF2DD4BF),
                iconBg = Color(0xFF134E4A)
            ) {
                // Audio Streaming Quality
                SettingsClickableRow(
                    icon = Icons.Default.Equalizer,
                    iconBg = Color(0xFF0D9488),
                    title = "Audio Quality",
                    subtitle = "Choose streaming quality",
                    value = audioQuality,
                    onClick = { showAudioQualityDialog = true },
                    testTag = "setting_audio_quality"
                )

                // Always Resume Playback
                SettingsToggleRow(
                    icon = Icons.Default.History,
                    iconBg = Color(0xFF0284C7),
                    title = "Always Resume Playback",
                    subtitle = "Continue where you left off",
                    checked = alwaysResumePlayback,
                    accentColor = accent,
                    onCheckedChange = { viewModel.setAlwaysResumePlayback(it) },
                    testTag = "toggle_always_resume"
                )

                // Crossfade Slider
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF8B5CF6).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GraphicEq,
                                contentDescription = null,
                                tint = Color(0xFFA78BFA),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Crossfade",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = theme.textPrimary
                            )
                            Text(
                                text = "Smooth transition between songs",
                                fontSize = 12.sp,
                                color = theme.textMuted
                            )
                        }

                        Text(
                            text = "${crossfadeSeconds}s",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                    }

                    Slider(
                        value = crossfadeSeconds.toFloat(),
                        onValueChange = { viewModel.setCrossfadeSeconds(it.toInt()) },
                        valueRange = 0f..12f,
                        steps = 11,
                        colors = SliderDefaults.colors(
                            thumbColor = Color.White,
                            activeTrackColor = accent,
                            inactiveTrackColor = theme.borderLight
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                            .testTag("slider_crossfade")
                    )
                }

                // Background Playback
                SettingsToggleRow(
                    icon = Icons.Default.CastConnected,
                    iconBg = Color(0xFF10B981),
                    title = "Background Playback",
                    subtitle = "Keep music playing when app is in background",
                    checked = backgroundPlayback,
                    accentColor = accent,
                    onCheckedChange = { viewModel.setBackgroundPlayback(it) },
                    testTag = "toggle_background_playback"
                )
            }

            // ==========================================
            // 3. DOWNLOADS & STORAGE SECTION
            // ==========================================
            SettingsCard(
                title = "Downloads",
                subtitle = "Offline music and storage",
                icon = Icons.Default.FileDownload,
                iconTint = Color(0xFFFBBF24),
                iconBg = Color(0xFF78350F)
            ) {
                // Download Quality
                SettingsClickableRow(
                    icon = Icons.Default.FileDownload,
                    iconBg = Color(0xFFD97706),
                    title = "Download Quality",
                    subtitle = "Quality for offline downloads",
                    value = downloadQuality,
                    onClick = { showDownloadQualityDialog = true },
                    testTag = "setting_download_quality"
                )

                // Download Over Wi-Fi Only
                SettingsToggleRow(
                    icon = Icons.Default.Wifi,
                    iconBg = Color(0xFF2563EB),
                    title = "Download Over Wi-Fi Only",
                    subtitle = "Save mobile data",
                    checked = downloadWifiOnly,
                    accentColor = accent,
                    onCheckedChange = { viewModel.setDownloadWifiOnly(it) },
                    testTag = "toggle_download_wifi"
                )

                // Storage & Cache Clear
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF64748B).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Clear Cache",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = theme.textPrimary
                        )
                        Text(
                            text = "App cache size: $cacheSizeFormatted",
                            fontSize = 12.sp,
                            color = theme.textMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(theme.surfaceVariant)
                            .border(1.dp, theme.border, RoundedCornerShape(14.dp))
                            .clickable {
                                viewModel.clearCache()
                                Toast.makeText(context, "Cache cleared successfully", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("btn_clear_cache")
                    ) {
                        Text(
                            text = "Clear",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = theme.textPrimary
                        )
                    }
                }
            }

            // ==========================================
            // 4. SLEEP TIMER SECTION
            // ==========================================
            SettingsCard(
                title = "Sleep Timer",
                subtitle = if (sleepTimerRemaining != null) "Stopping playback in $sleepTimerRemaining min" else "Turn off music automatically",
                icon = Icons.Default.NightsStay,
                iconTint = Color(0xFFA78BFA),
                iconBg = Color(0xFF4C1D95)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(15, 30, 45, 60).forEach { mins ->
                        val isCurrent = sleepTimerRemaining == mins
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isCurrent) accent else theme.surfaceVariant)
                                .border(1.dp, if (isCurrent) accent else theme.border, RoundedCornerShape(12.dp))
                                .clickable { viewModel.startSleepTimer(mins) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mins}m",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) Color.Black else theme.textPrimary
                            )
                        }
                    }

                    if (sleepTimerRemaining != null) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.2f))
                                .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(12.dp))
                                .clickable { viewModel.cancelSleepTimer() }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }

            // ==========================================
            // 5. ACCOUNT SECTION
            // ==========================================
            SettingsCard(
                title = "Account",
                subtitle = "Profile and cloud synchronization",
                icon = Icons.Default.Person,
                iconTint = Color(0xFF38BDF8),
                iconBg = Color(0xFF075985)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(accent.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (googleUser.name.isNotBlank()) googleUser.name.first().toString() else "U",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = accent
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (googleUser.name.isNotBlank()) googleUser.name else "Biplab Mohinta",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = theme.textPrimary
                        )
                        Text(
                            text = if (googleUser.email.isNotBlank()) googleUser.email else "biplabmohintamoni@gmail.com",
                            fontSize = 12.sp,
                            color = theme.textMuted
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                            .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                            .clickable {
                                if (googleUser.isSignedIn) {
                                    viewModel.signOutGoogle()
                                } else {
                                    viewModel.signInWithGoogle(
                                        name = "Biplab Mohinta",
                                        email = "biplabmohintamoni@gmail.com",
                                        photoUrl = null
                                    )
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (googleUser.isSignedIn) "Sign Out" else "Connect",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFEF4444)
                        )
                    }
                }
            }

            // ==========================================
            // 6. ABOUT & CREATOR SECTION
            // ==========================================
            SettingsCard(
                title = "About",
                subtitle = "App information and developer credits",
                icon = Icons.Default.Info,
                iconTint = Color(0xFF94A3B8),
                iconBg = Color(0xFF334155)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "AURA MUSIC",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = theme.textPrimary
                    )
                    Text(
                        text = "Version 2.4.0 • Build 2026.1",
                        fontSize = 12.sp,
                        color = theme.textMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(theme.surfaceVariant)
                            .clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(OwnerConfig.CREATOR_INSTAGRAM_URL))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Instagram: @k98gamer_editz", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "MADE BY K98",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = accent
                            )
                            Text(
                                text = "Instagram: @k98gamer_editz",
                                fontSize = 12.sp,
                                color = theme.textSecondary
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = "Open Instagram",
                            tint = theme.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Audio Quality Selection Dialog
    if (showAudioQualityDialog) {
        val qualities = listOf(
            "Auto (Recommended)",
            "Extreme (320 kbps)",
            "High (256 kbps)",
            "Medium (192 kbps)",
            "Low (128 kbps)"
        )
        AlertDialog(
            onDismissRequest = { showAudioQualityDialog = false },
            title = { Text("Audio Streaming Quality", color = theme.textPrimary) },
            text = {
                Column {
                    qualities.forEach { q ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setAudioQuality(q)
                                    showAudioQualityDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = q,
                                color = if (q == audioQuality) accent else theme.textPrimary,
                                fontWeight = if (q == audioQuality) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (q == audioQuality) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = accent
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAudioQualityDialog = false }) {
                    Text("Close", color = accent)
                }
            },
            containerColor = theme.surfaceElevated
        )
    }

    // Download Quality Selection Dialog
    if (showDownloadQualityDialog) {
        val downloadQualities = listOf(
            "Extreme (320 kbps)",
            "High (256 kbps)",
            "Standard (128 kbps)"
        )
        AlertDialog(
            onDismissRequest = { showDownloadQualityDialog = false },
            title = { Text("Download Quality", color = theme.textPrimary) },
            text = {
                Column {
                    downloadQualities.forEach { q ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDownloadQuality(q)
                                    showDownloadQualityDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = q,
                                color = if (q == downloadQuality) accent else theme.textPrimary,
                                fontWeight = if (q == downloadQuality) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (q == downloadQuality) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = accent
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDownloadQualityDialog = false }) {
                    Text("Close", color = accent)
                }
            },
            containerColor = theme.surfaceElevated
        )
    }
}

// ==========================================
// Reusable Subcomponents
// ==========================================

@Composable
private fun SettingsCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    content: @Composable () -> Unit
) {
    val theme = LocalAuraTheme.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(theme.surfaceElevated)
            .border(1.dp, theme.border, RoundedCornerShape(20.dp))
            .padding(vertical = 12.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = theme.textPrimary
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = theme.textMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        content()
    }
}

@Composable
private fun ThemePreviewCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    accentColor: Color,
    theme: com.example.auramusic.ui.theme.AuraThemeColors,
    isBeta: Boolean,
    previewBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    val cardBorder = if (isSelected) accentColor else theme.border
    val cardBorderWidth = if (isSelected) 2.dp else 1.dp

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(theme.surfaceVariant)
            .border(cardBorderWidth, cardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(10.dp)
            .testTag(testTag)
    ) {
        // Mini Graphic Visual
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(previewBg)
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                .padding(6.dp)
        ) {
            // Miniature mock cards
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (title == "Light") Color(0xFF6366F1) else accentColor)
                    )
                    if (isBeta) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF8B5CF6))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("BETA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    } else if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Miniature fake track cards
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (title == "Light") Color(0xFFCBD5E1) else Color(0xFF374151))
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (title == "Light") Color(0xFF94A3B8) else Color(0xFF4B5563))
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = theme.textPrimary
        )

        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = theme.textMuted,
            maxLines = 1
        )
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    accentColor: Color,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    val theme = LocalAuraTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconBg,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = theme.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = theme.textMuted
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = accentColor,
                uncheckedThumbColor = theme.textMuted,
                uncheckedTrackColor = theme.surfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    iconBg: Color,
    title: String,
    subtitle: String,
    value: String,
    onClick: () -> Unit,
    testTag: String
) {
    val theme = LocalAuraTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(iconBg.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconBg,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = theme.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = theme.textMuted
            )
        }

        Text(
            text = "$value ›",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = theme.textSecondary
        )
    }
}
