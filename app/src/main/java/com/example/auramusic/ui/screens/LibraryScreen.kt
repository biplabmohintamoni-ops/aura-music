package com.example.auramusic.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.OfflinePin
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.auramusic.model.GoogleUser
import com.example.auramusic.model.Playlist
import com.example.auramusic.model.Song
import com.example.auramusic.ui.theme.AuraAccentPill
import com.example.auramusic.ui.theme.AuraBackground
import com.example.auramusic.ui.theme.AuraBorder
import com.example.auramusic.ui.theme.AuraLikeHeart
import com.example.auramusic.ui.theme.AuraPrimary
import com.example.auramusic.ui.theme.AuraSurfaceElevated
import com.example.auramusic.ui.theme.AuraSurfaceVariant
import com.example.auramusic.ui.theme.AuraTextMuted
import com.example.auramusic.ui.theme.AuraTextPrimary
import com.example.auramusic.ui.theme.AuraTextSecondary

@Composable
fun LibraryScreen(
    playlists: List<Playlist>,
    likedSongs: List<Song>,
    googleUser: GoogleUser,
    onOpenLikedSongs: () -> Unit,
    onSelectPlaylist: (Playlist) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenHistory: () -> Unit,
    onCreatePlaylist: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("Playlists") }
    val tabs = listOf("Playlists", "Songs", "Albums", "Artists", "Local")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraBackground)
            .testTag("library_screen")
    ) {
        // Header matching subahbjhegm.png
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Library",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AuraTextPrimary
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenHistory,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = AuraTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { /* Refresh */ },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        tint = AuraTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onOpenProfile,
                    modifier = Modifier.size(36.dp)
                ) {
                    if (googleUser.isSignedIn && !googleUser.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = googleUser.photoUrl,
                            contentDescription = "Profile",
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = AuraTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = AuraTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Sub-tabs: Playlists, Songs, Albums, Artists, Local
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tabs) { tab ->
                val isSelected = tab == selectedTab
                val bgAnim by animateColorAsState(
                    targetValue = if (isSelected) AuraAccentPill else AuraSurfaceVariant,
                    label = "tab_bg"
                )
                val textAnim by animateColorAsState(
                    targetValue = if (isSelected) Color.Black else AuraTextPrimary,
                    label = "tab_text"
                )

                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(bgAnim)
                        .clickable { selectedTab = tab }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tab,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = textAnim
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Sort Row: Date added ^
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { /* Sort toggle */ }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = null,
                    tint = AuraTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Date added",
                    fontSize = 13.sp,
                    color = AuraTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            // 2-Column Quick Action Grid (matching subahbjhegm.png)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LibraryQuickCard(
                            title = "Liked",
                            subtitle = "${likedSongs.size} songs",
                            icon = Icons.Default.Favorite,
                            iconTint = AuraLikeHeart,
                            onClick = onOpenLikedSongs,
                            modifier = Modifier.weight(1f)
                        )
                        LibraryQuickCard(
                            title = "Downloaded",
                            subtitle = "0 songs",
                            icon = Icons.Default.OfflinePin,
                            iconTint = AuraPrimary,
                            onClick = { /* Downloaded */ },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LibraryQuickCard(
                            title = "Cached",
                            subtitle = "0 songs",
                            icon = Icons.Default.Sync,
                            iconTint = AuraTextSecondary,
                            onClick = { /* Cached */ },
                            modifier = Modifier.weight(1f)
                        )
                        LibraryQuickCard(
                            title = "Uploaded",
                            subtitle = "0 songs",
                            icon = Icons.Default.CloudUpload,
                            iconTint = AuraTextSecondary,
                            onClick = { /* Uploaded */ },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LibraryQuickCard(
                            title = "My top 50",
                            subtitle = "50 songs",
                            icon = Icons.Default.Leaderboard,
                            iconTint = AuraTextSecondary,
                            onClick = { /* Top 50 */ },
                            modifier = Modifier.weight(1f)
                        )
                        LibraryQuickCard(
                            title = "Local",
                            subtitle = "Device audio",
                            icon = Icons.Default.Folder,
                            iconTint = AuraTextSecondary,
                            onClick = { /* Local */ },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Section: Playlists
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Playlists",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraTextPrimary
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onCreatePlaylist() }
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add",
                            tint = AuraPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "New playlist",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AuraPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            items(playlists) { playlist ->
                PlaylistItemRow(
                    playlist = playlist,
                    onClick = { onSelectPlaylist(playlist) }
                )
            }
        }
    }
}

@Composable
private fun LibraryQuickCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(AuraSurfaceElevated)
            .border(1.dp, AuraBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AuraTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = AuraTextMuted
                )
            }
        }
    }
}

@Composable
private fun PlaylistItemRow(
    playlist: Playlist,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AuraSurfaceElevated)
                .border(1.dp, AuraBorder, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.QueueMusic,
                contentDescription = null,
                tint = AuraPrimary,
                modifier = Modifier.size(26.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = playlist.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = AuraTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Playlist • ${playlist.songIds.size} songs",
                fontSize = 12.sp,
                color = AuraTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
