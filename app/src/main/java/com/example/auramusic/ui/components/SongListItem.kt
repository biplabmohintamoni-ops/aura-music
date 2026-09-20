package com.example.auramusic.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.auramusic.model.Song
import com.example.auramusic.ui.theme.AuraLikeHeart
import com.example.auramusic.ui.theme.AuraPrimary
import com.example.auramusic.ui.theme.AuraSurfaceElevated
import com.example.auramusic.ui.theme.AuraSurfaceVariant
import com.example.auramusic.ui.theme.AuraTextMuted
import com.example.auramusic.ui.theme.AuraTextPrimary
import com.example.auramusic.ui.theme.AuraTextSecondary

@Composable
fun SongListItem(
    song: Song,
    isCurrentSong: Boolean,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onLikeToggle: () -> Unit,
    onAddToPlaylist: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (isCurrentSong) AuraSurfaceVariant else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("song_item_${song.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album Cover Thumbnail via Coil AsyncImage
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(AuraSurfaceElevated),
            contentAlignment = Alignment.Center
        ) {
            if (song.coverUrl != null) {
                AsyncImage(
                    model = song.coverUrl,
                    contentDescription = "${song.title} artwork",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.MusicNote,
                    contentDescription = null,
                    tint = AuraTextMuted,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Small visualizer overlay if current song
            if (isCurrentSong) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    AuraVisualizer(
                        isPlaying = isPlaying,
                        barColor = AuraPrimary,
                        barCount = 3,
                        height = 16.dp,
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Title and Artist
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = song.title,
                color = if (isCurrentSong) AuraPrimary else AuraTextPrimary,
                fontSize = 15.sp,
                fontWeight = if (isCurrentSong) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(
                    text = song.artist,
                    color = AuraTextSecondary,
                    fontSize = 13.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = " • ${formatDuration(song.durationMs)}",
                    color = AuraTextMuted,
                    fontSize = 12.sp
                )
            }
        }

        // Like Button
        IconButton(
            onClick = onLikeToggle,
            modifier = Modifier
                .size(40.dp)
                .testTag("like_button_${song.id}")
        ) {
            Icon(
                imageVector = if (song.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (song.isLiked) "Unlike" else "Like",
                tint = if (song.isLiked) AuraLikeHeart else AuraTextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        // Context Menu
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("more_menu_${song.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = AuraTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(AuraSurfaceElevated)
            ) {
                DropdownMenuItem(
                    text = { Text("Play", color = AuraTextPrimary) },
                    onClick = {
                        showMenu = false
                        onClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Add to Playlist", color = AuraTextPrimary) },
                    onClick = {
                        showMenu = false
                        onAddToPlaylist()
                    }
                )
                DropdownMenuItem(
                    text = {
                        Text(
                            text = if (song.isLiked) "Remove from Liked" else "Save to Liked Songs",
                            color = AuraTextPrimary
                        )
                    },
                    onClick = {
                        showMenu = false
                        onLikeToggle()
                    }
                )
            }
        }
    }
}

fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}
