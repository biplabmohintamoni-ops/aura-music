package com.example.auramusic.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auramusic.model.Song
import com.example.auramusic.ui.theme.AuraAccentPill
import com.example.auramusic.ui.theme.AuraBackground
import com.example.auramusic.ui.theme.AuraBorder
import com.example.auramusic.ui.theme.AuraSurfaceElevated
import com.example.auramusic.ui.theme.AuraTextPrimary
import com.example.auramusic.ui.theme.AuraTextSecondary

@Composable
fun MusicRecognitionScreen(
    isRecognizing: Boolean,
    recognizedSong: Song?,
    onStartRecognize: () -> Unit,
    onPlayRecognizedSong: (Song) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraBackground)
            .testTag("recognition_screen")
    ) {
        // Top Header matching subahbjhegm.png
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AuraTextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Recognize Music",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraTextPrimary
                )
            }

            IconButton(
                onClick = { /* History */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Recognition History",
                    tint = AuraTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // Center Recognition Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Large Circular Button
                Box(
                    modifier = Modifier
                        .size(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRecognizing) {
                        // Outer animated pulse ring
                        Box(
                            modifier = Modifier
                                .size(180.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(Color(0x33B8A7E0))
                        )
                    }

                    // Main circular button
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .clip(CircleShape)
                            .background(AuraAccentPill)
                            .clickable(enabled = !isRecognizing) { onStartRecognize() }
                            .testTag("recognize_mic_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Microphone",
                            tint = Color.Black,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = if (isRecognizing) "Listening for music..." else "Tap to recognize",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AuraTextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isRecognizing) "Hold device near the sound source" else "Identify any song playing around you",
                    fontSize = 13.sp,
                    color = AuraTextSecondary
                )

                // If match found
                if (recognizedSong != null && !isRecognizing) {
                    Spacer(modifier = Modifier.height(36.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AuraSurfaceElevated)
                            .border(1.dp, AuraBorder, RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "FOUND MATCH",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AuraTextSecondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = recognizedSong.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = AuraTextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = recognizedSong.artist,
                                fontSize = 13.sp,
                                color = AuraTextSecondary
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { onPlayRecognizedSong(recognizedSong) },
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = AuraTextPrimary,
                                    contentColor = Color.Black
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Play in Aura Music")
                            }
                        }
                    }
                }
            }
        }
    }
}
