package com.example.auramusic.ui.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.auramusic.model.Song
import com.example.auramusic.ui.theme.AuraBackground
import com.example.auramusic.ui.theme.AuraBorder
import com.example.auramusic.ui.theme.AuraLikeHeart
import com.example.auramusic.ui.theme.AuraPrimary
import com.example.auramusic.ui.theme.AuraSurfaceElevated
import com.example.auramusic.ui.theme.AuraTextMuted
import com.example.auramusic.ui.theme.AuraTextPrimary
import com.example.auramusic.ui.theme.AuraTextSecondary

@Composable
fun SongContextMenuSheet(
    song: Song,
    isLiked: Boolean,
    onPlay: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onToggleLike: () -> Unit,
    onViewLyrics: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(AuraSurfaceElevated)
                .border(1.dp, AuraBorder, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .testTag("song_context_menu")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Header with Song Info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(10.dp))
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
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = song.artist,
                            fontSize = 12.sp,
                            color = AuraTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Options List
                ContextMenuRow(
                    title = "Play",
                    icon = Icons.Default.PlayArrow,
                    onClick = {
                        onDismiss()
                        onPlay()
                    }
                )

                ContextMenuRow(
                    title = "Play next",
                    icon = Icons.Default.QueueMusic,
                    onClick = {
                        onDismiss()
                        onPlayNext()
                    }
                )

                ContextMenuRow(
                    title = "Add to queue",
                    icon = Icons.Default.PlaylistAdd,
                    onClick = {
                        onDismiss()
                        onAddToQueue()
                    }
                )

                ContextMenuRow(
                    title = if (isLiked) "Remove from Liked" else "Add to Liked",
                    icon = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    iconTint = if (isLiked) AuraLikeHeart else AuraTextPrimary,
                    onClick = {
                        onDismiss()
                        onToggleLike()
                    }
                )

                ContextMenuRow(
                    title = "Lyrics",
                    icon = Icons.Default.Subtitles,
                    onClick = {
                        onDismiss()
                        onViewLyrics()
                    }
                )

                ContextMenuRow(
                    title = "Share",
                    icon = Icons.Default.Share,
                    onClick = { onDismiss() }
                )
            }
        }
    }
}

@Composable
private fun ContextMenuRow(
    title: String,
    icon: ImageVector,
    iconTint: androidx.compose.ui.graphics.Color = AuraTextPrimary,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = AuraTextPrimary
        )
    }
}
