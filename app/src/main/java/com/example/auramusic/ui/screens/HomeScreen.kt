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
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.auramusic.data.provider.OwnerConfig
import com.example.auramusic.model.GoogleUser
import com.example.auramusic.model.Song
import com.example.auramusic.ui.theme.AuraAccentPill
import com.example.auramusic.ui.theme.AuraBackground
import com.example.auramusic.ui.theme.AuraBorder
import com.example.auramusic.ui.theme.AuraPrimary
import com.example.auramusic.ui.theme.AuraSurface
import com.example.auramusic.ui.theme.AuraSurfaceElevated
import com.example.auramusic.ui.theme.AuraSurfaceVariant
import com.example.auramusic.ui.theme.AuraTextMuted
import com.example.auramusic.ui.theme.AuraTextPrimary
import com.example.auramusic.ui.theme.AuraTextSecondary

@Composable
fun HomeScreen(
    recommendedSongs: List<Song>,
    trendingSongs: List<Song>,
    quickPicks: List<Song>,
    categories: List<String>,
    selectedCategory: String,
    isLoading: Boolean,
    googleUser: GoogleUser,
    onSelectCategory: (String) -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    onOpenEqualizer: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenProfile: () -> Unit,
    onSongMenu: (Song) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraBackground)
            .testTag("home_screen")
    ) {
        // Top Header matching makabm.png
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = OwnerConfig.APP_NAME,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AuraTextPrimary,
                letterSpacing = 0.5.sp
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
                    onClick = onOpenEqualizer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Equalizer,
                        contentDescription = "Equalizer",
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

        // Horizontal Category Chips
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                val bgAnim by animateColorAsState(
                    targetValue = if (isSelected) AuraAccentPill else AuraSurfaceVariant,
                    label = "chip_bg"
                )
                val textAnim by animateColorAsState(
                    targetValue = if (isSelected) Color.Black else AuraTextPrimary,
                    label = "chip_text"
                )

                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(bgAnim)
                        .clickable { onSelectCategory(category) }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = textAnim
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AuraPrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Featured Portrait Banner Card Carousel (matching makabm.png)
                item {
                    val featuredSong = recommendedSongs.firstOrNull()
                    if (featuredSong != null) {
                        FeaturedPortraitCard(
                            song = featuredSong,
                            onClick = { onPlaySong(featuredSong, recommendedSongs) }
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Section: PLAYLISTS FOR THE SEASON
                item {
                    Text(
                        text = "PLAYLISTS FOR THE SEASON",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraTextMuted,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    Text(
                        text = "Hello, Summer! ☀️🍉",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraTextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(recommendedSongs.take(8)) { song ->
                            SmallSquareSongCard(
                                song = song,
                                onClick = { onPlaySong(song, recommendedSongs) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Section: Quick Picks (Vertical list items)
                item {
                    Text(
                        text = "Quick Picks",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraTextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                items(quickPicks) { song ->
                    CompactSongListItem(
                        song = song,
                        onClick = { onPlaySong(song, quickPicks) },
                        onMenuClick = { onSongMenu(song) }
                    )
                }

                // Section: Trending Now
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Trending Now",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraTextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(trendingSongs.take(8)) { song ->
                            SmallSquareSongCard(
                                song = song,
                                onClick = { onPlaySong(song, trendingSongs) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedPortraitCard(
    song: Song,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AuraSurfaceElevated)
            .border(1.dp, AuraBorder, RoundedCornerShape(20.dp))
            .clickable { onClick() }
    ) {
        if (!song.coverUrl.isNullOrBlank()) {
            AsyncImage(
                model = song.coverUrl,
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Dark gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC0A0A0C), Color(0xF00A0A0C)),
                        startY = 50f
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(18.dp)
        ) {
            Text(
                text = "FEATURED TRACK",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AuraPrimary,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = song.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AuraTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = song.artist,
                fontSize = 13.sp,
                color = AuraTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Play Button circle on top right
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(18.dp)
                .size(42.dp)
                .clip(CircleShape)
                .background(AuraTextPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SmallSquareSongCard(
    song: Song,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(135.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(135.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(AuraSurfaceElevated)
                .border(1.dp, AuraBorder, RoundedCornerShape(14.dp)),
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
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = song.title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = AuraTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = song.artist,
            fontSize = 11.sp,
            color = AuraTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CompactSongListItem(
    song: Song,
    onClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AuraSurfaceElevated),
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
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
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

        IconButton(
            onClick = onMenuClick,
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
