package com.example.auramusic.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.auramusic.model.RepeatMode
import com.example.auramusic.model.Song
import com.example.auramusic.ui.theme.AuraBackground
import com.example.auramusic.ui.theme.AuraBorder
import com.example.auramusic.ui.theme.AuraLikeHeart
import com.example.auramusic.ui.theme.AuraPrimary
import com.example.auramusic.ui.theme.AuraSurfaceElevated
import com.example.auramusic.ui.theme.AuraSurfaceVariant
import com.example.auramusic.ui.theme.AuraTextMuted
import com.example.auramusic.ui.theme.AuraTextPrimary
import com.example.auramusic.ui.theme.AuraTextSecondary
import com.example.auramusic.ui.theme.LocalAuraTheme
import com.example.auramusic.ui.theme.LocalDynamicAccent

@Composable
fun NowPlayingSheet(
    song: Song,
    isPlaying: Boolean,
    isBuffering: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    playbackError: String?,
    isShuffle: Boolean,
    repeatMode: RepeatMode,
    isLiked: Boolean,
    onTogglePlayPause: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    onToggleLike: () -> Unit,
    onOpenLyrics: () -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSleepTimer: () -> Unit,
    onSongMenu: () -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isDraggingSlider by remember { mutableStateOf(false) }
    var draggedPositionMs by remember { mutableFloatStateOf(0f) }

    val effectivePosition = if (isDraggingSlider) draggedPositionMs.toLong() else currentPositionMs
    val effectiveDuration = if (durationMs > 0) durationMs else 210000L

    val theme = LocalAuraTheme.current
    val dynamicAccent = LocalDynamicAccent.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        dynamicAccent.copy(alpha = 0.18f),
                        theme.background,
                        theme.background
                    )
                )
            )
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("now_playing_sheet"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header: Collapse, Now Playing Title, Cast icon
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onCollapse,
                modifier = Modifier
                    .size(40.dp)
                    .testTag("now_playing_collapse")
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Collapse",
                    tint = AuraTextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "PLAYING FROM SEARCH",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraTextMuted,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = song.album.ifBlank { "YouTube Music" },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AuraTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = { /* Cast / Device */ },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cast,
                    contentDescription = "Devices",
                    tint = AuraTextPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Large Album Artwork (matching makabm.png)
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(AuraSurfaceElevated)
                .border(1.dp, AuraBorder, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (!song.coverUrl.isNullOrBlank()) {
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = song.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = AuraTextMuted,
                    modifier = Modifier.size(64.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Title, Artist, and Action Buttons (Download & Like)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = song.artist,
                    fontSize = 14.sp,
                    color = AuraTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Download button box
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AuraSurfaceElevated)
                        .border(1.dp, AuraBorder, RoundedCornerShape(12.dp))
                        .clickable { /* Download */ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileDownload,
                        contentDescription = "Download",
                        tint = AuraTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Like heart button box
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AuraSurfaceElevated)
                        .border(1.dp, AuraBorder, RoundedCornerShape(12.dp))
                        .clickable { onToggleLike() }
                        .testTag("now_playing_like_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) AuraLikeHeart else AuraTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Playback Error Notification if present
        if (!playbackError.isNullOrBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0x33FF3B30))
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = playbackError,
                    color = Color(0xFFFF6B6B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Scrubber / Seekbar
        Slider(
            value = effectivePosition.toFloat(),
            onValueChange = {
                isDraggingSlider = true
                draggedPositionMs = it
            },
            onValueChangeFinished = {
                isDraggingSlider = false
                onSeekTo(draggedPositionMs.toLong())
            },
            valueRange = 0f..effectiveDuration.toFloat(),
            colors = SliderDefaults.colors(
                thumbColor = dynamicAccent,
                activeTrackColor = dynamicAccent,
                inactiveTrackColor = theme.surfaceVariant
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("now_playing_scrubber")
        )

        // Time labels: Current and Duration
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = formatDuration(effectivePosition),
                fontSize = 12.sp,
                color = AuraTextSecondary
            )
            Text(
                text = formatDuration(effectiveDuration),
                fontSize = 12.sp,
                color = AuraTextSecondary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Big Controls: Previous, Pill Play/Pause, Next (matching makabm.png)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Previous button in rounded box
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AuraSurfaceElevated)
                    .border(1.dp, AuraBorder, RoundedCornerShape(16.dp))
                    .clickable { onPrevious() }
                    .testTag("now_playing_prev"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    tint = AuraTextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Large Pill Play/Pause button
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(58.dp)
                    .clip(RoundedCornerShape(29.dp))
                    .background(AuraTextPrimary)
                    .clickable { onTogglePlayPause() }
                    .testTag("now_playing_play_pause"),
                contentAlignment = Alignment.Center
            ) {
                if (isBuffering) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.Black,
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            // Next button in rounded box
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(AuraSurfaceElevated)
                    .border(1.dp, AuraBorder, RoundedCornerShape(16.dp))
                    .clickable { onNext() }
                    .testTag("now_playing_next"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    tint = AuraTextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Bottom Toolbar: Lyrics, Moon (Sleep), Equalizer, Shuffle, Repeat, 3-dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onOpenLyrics) {
                Icon(
                    imageVector = Icons.Default.Subtitles,
                    contentDescription = "Lyrics",
                    tint = AuraTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(onClick = onOpenSleepTimer) {
                Icon(
                    imageVector = Icons.Default.NightsStay,
                    contentDescription = "Sleep Timer",
                    tint = AuraTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(onClick = onOpenEqualizer) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = "Equalizer",
                    tint = AuraTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(onClick = onToggleShuffle) {
                Icon(
                    imageVector = Icons.Default.Shuffle,
                    contentDescription = "Shuffle",
                    tint = if (isShuffle) AuraPrimary else AuraTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(onClick = onCycleRepeat) {
                Icon(
                    imageVector = when (repeatMode) {
                        RepeatMode.OFF -> Icons.Default.Repeat
                        RepeatMode.ALL -> Icons.Default.Repeat
                        RepeatMode.ONE -> Icons.Default.RepeatOne
                    },
                    contentDescription = "Repeat",
                    tint = if (repeatMode != RepeatMode.OFF) AuraPrimary else AuraTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(onClick = onSongMenu) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More Options",
                    tint = AuraTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format("%d:%02d", min, sec)
}
