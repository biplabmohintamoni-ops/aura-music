package com.example.auramusic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auramusic.model.EqualizerPreset
import com.example.auramusic.model.EqualizerSettings
import com.example.auramusic.ui.theme.AuraAccentPill
import com.example.auramusic.ui.theme.AuraBackground
import com.example.auramusic.ui.theme.AuraBorder
import com.example.auramusic.ui.theme.AuraPrimary
import com.example.auramusic.ui.theme.AuraSurfaceElevated
import com.example.auramusic.ui.theme.AuraSurfaceVariant
import com.example.auramusic.ui.theme.AuraTextMuted
import com.example.auramusic.ui.theme.AuraTextPrimary
import com.example.auramusic.ui.theme.AuraTextSecondary

@Composable
fun EqualizerScreen(
    settings: EqualizerSettings,
    presets: List<EqualizerPreset>,
    onSelectPreset: (EqualizerPreset) -> Unit,
    onBandChange: (Int, Float) -> Unit,
    onBassBoostChange: (Float) -> Unit,
    onSurroundChange: (Float) -> Unit,
    onToggleEnabled: (Boolean) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bandFrequencies = listOf("60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraBackground)
            .testTag("equalizer_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onClose) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AuraTextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Equalizer",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraTextPrimary
                )
            }

            Switch(
                checked = settings.isEnabled,
                onCheckedChange = onToggleEnabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = AuraPrimary,
                    uncheckedThumbColor = AuraTextSecondary,
                    uncheckedTrackColor = AuraSurfaceVariant
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 60.dp)
        ) {
            // Presets
            Text(
                text = "PRESETS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AuraTextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(presets) { preset ->
                    val isSelected = preset.name == settings.currentPreset
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(if (isSelected) AuraAccentPill else AuraSurfaceElevated)
                            .border(1.dp, AuraBorder, RoundedCornerShape(18.dp))
                            .clickable(enabled = settings.isEnabled) { onSelectPreset(preset) }
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = preset.name,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) Color.Black else AuraTextPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 5 Band EQ Sliders
            Text(
                text = "FREQUENCY BANDS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AuraTextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AuraSurfaceElevated)
                    .border(1.dp, AuraBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    settings.bands.forEachIndexed { index, gain ->
                        val freqLabel = bandFrequencies.getOrElse(index) { "Band $index" }
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = freqLabel,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AuraTextPrimary
                                )
                                Text(
                                    text = "${gain.toInt()} dB",
                                    fontSize = 13.sp,
                                    color = AuraTextSecondary
                                )
                            }
                            Slider(
                                value = gain,
                                onValueChange = { onBandChange(index, it) },
                                valueRange = -10f..10f,
                                enabled = settings.isEnabled,
                                colors = SliderDefaults.colors(
                                    thumbColor = AuraTextPrimary,
                                    activeTrackColor = AuraPrimary,
                                    inactiveTrackColor = AuraSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bass Boost & Surround Sound
            Text(
                text = "ENHANCEMENTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AuraTextMuted,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AuraSurfaceElevated)
                    .border(1.dp, AuraBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Bass Boost
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Bass Boost",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = AuraTextPrimary
                            )
                            Text(
                                text = "${settings.bassBoost.toInt()}%",
                                fontSize = 13.sp,
                                color = AuraTextSecondary
                            )
                        }
                        Slider(
                            value = settings.bassBoost,
                            onValueChange = onBassBoostChange,
                            valueRange = 0f..100f,
                            enabled = settings.isEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = AuraTextPrimary,
                                activeTrackColor = AuraPrimary,
                                inactiveTrackColor = AuraSurfaceVariant
                            )
                        )
                    }

                    // Surround Sound
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "3D Surround",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = AuraTextPrimary
                            )
                            Text(
                                text = "${settings.surroundSound.toInt()}%",
                                fontSize = 13.sp,
                                color = AuraTextSecondary
                            )
                        }
                        Slider(
                            value = settings.surroundSound,
                            onValueChange = onSurroundChange,
                            valueRange = 0f..100f,
                            enabled = settings.isEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = AuraTextPrimary,
                                activeTrackColor = AuraPrimary,
                                inactiveTrackColor = AuraSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}
