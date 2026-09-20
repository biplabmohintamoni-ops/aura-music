package com.example.auramusic.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloseFullscreen
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.auramusic.model.LyricLine
import com.example.auramusic.model.Song
import com.example.auramusic.ui.theme.AuraBackground
import com.example.auramusic.ui.theme.AuraPrimary
import com.example.auramusic.ui.theme.AuraSurfaceElevated
import com.example.auramusic.ui.theme.AuraTextMuted
import com.example.auramusic.ui.theme.AuraTextPrimary
import com.example.auramusic.ui.theme.AuraTextSecondary

@Composable
fun LyricsSheet(
    song: Song,
    lyrics: List<LyricLine>,
    isLoadingLyrics: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onSeekTo: (Long) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    val activeIndex = lyrics.indexOfLast { it.timestampMs <= currentPositionMs }
        .coerceAtLeast(0)

    LaunchedEffect(activeIndex) {
        if (lyrics.isNotEmpty() && activeIndex in lyrics.indices) {
            val targetScroll = (activeIndex - 2).coerceAtLeast(0)
            listState.animateScrollToItem(targetScroll)
        }
    }

    val progress = if (durationMs > 0) {
        (currentPositionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraBackground)
            .testTag("lyrics_sheet")
    ) {
        // Top Bar: Collapse, Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Close Lyrics",
                    tint = AuraTextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "LYRICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuraTextMuted,
                    letterSpacing = 1.5.sp
                )
                Text(
                    text = song.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AuraTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.size(40.dp))
        }

        // Lyrics Content Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (isLoadingLyrics) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AuraPrimary)
                }
            } else if (lyrics.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Lyrics unavailable for this track.",
                        color = AuraTextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 60.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    itemsIndexed(lyrics) { index, line ->
                        val isCurrent = index == activeIndex
                        val textColor by animateColorAsState(
                            targetValue = if (isCurrent) AuraTextPrimary else Color(0xFF6B6E5B),
                            label = "lyric_color"
                        )
                        val fontSize = if (isCurrent) 24.sp else 18.sp
                        val fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium

                        Text(
                            text = line.text,
                            fontSize = fontSize,
                            fontWeight = fontWeight,
                            color = textColor,
                            lineHeight = 32.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSeekTo(line.timestampMs) }
                                .padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Bottom Docked Track Bar (matching makabm.png right screenshot)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AuraSurfaceElevated)
        ) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp),
                color = AuraPrimary,
                trackColor = Color.Transparent
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AuraBackground),
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
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AuraTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        fontSize = 11.sp,
                        color = AuraTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloseFullscreen,
                        contentDescription = "Collapse",
                        tint = AuraTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = { /* More */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = AuraTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
