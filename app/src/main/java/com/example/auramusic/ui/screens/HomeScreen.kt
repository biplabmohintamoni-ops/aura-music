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
        // App Header Section Layout Layout
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
                IconButton(onClick = onOpenHistory, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Default.History, contentDescription = "History", tint = AuraTextPrimary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onOpenEqualizer, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Default.Equalizer, contentDescription = "Equalizer", tint = AuraTextPrimary, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onOpenProfile, modifier = Modifier.size(36.dp)) {
                    if (googleUser.isSignedIn && !googleUser.photoUrl.isNullOrBlank()) {
                        AsyncImage(model = googleUser.photoUrl, contentDescription = "Profile", modifier = Modifier.size(24.dp).clip(CircleShape))
                    } else {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "Profile", tint = AuraTextPrimary, modifier = Modifier.size(20.dp))
                    }
                }
                IconButton(onClick = onOpenSettings, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", tint = AuraTextPrimary, modifier = Modifier.size(20.dp))
                }
            }
        }

        // Horizontal Category Row Configuration
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { category ->
                val isSelected = category == selectedCategory
                val bgAnim by animateColorAsState(targetValue = if (isSelected) AuraAccentPill else AuraSurfaceVariant, label = "chip_bg")
                val textAnim by animateColorAsState(targetValue = if (isSelected) Color.Black else AuraTextPrimary, label = "chip_text")

                Box(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(bgAnim)
                        .clickable { onSelectCategory(category) }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = category, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = textAnim)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AuraAccentPill)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f), contentPadding = PaddingValues(bottom = 80.dp)) {
                // Production Feature Seasonal Gradient Banner Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .height(140.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.horizontalGradient(colors = listOf(Color(0xFF1E3A8A), Color(0xFF0D9488))))
                            .clickable { if (trendingSongs.isNotEmpty()) onPlaySong(trendingSongs.first(), trendingSongs) }
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.Center) {
                            Text(text = "Trending Mix 🌧️🎵", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Rainy season top soundtracks", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }

                // Horizontal Music Items Shelf Grid
                if (trendingSongs.isNotEmpty()) {
                    item {
                        Text(text = "Trending Mix Tracks", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AuraTextPrimary, modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp))
                        LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            items(trendingSongs) { song ->
                                Column(modifier = Modifier.width(130.dp).clickable { onPlaySong(song, trendingSongs) }) {
                                    Box(modifier = Modifier.size(130.dp).clip(RoundedCornerShape(16.dp)).background(AuraSurfaceElevated)) {
                                        if (!song.coverUrl.isNullOrBlank()) {
                                            AsyncImage(model = song.coverUrl, contentDescription = song.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        } else {
                                            Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = AuraTextMuted, modifier = Modifier.size(40.dp).align(Alignment.Center))
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
Text(
    text = song.title,
    fontSize = 14.sp,
    fontWeight = FontWeight.Medium,
    color = AuraTextPrimary,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis
)
Text(
    text = song.artist,
    fontSize = 12.sp,
    color = AuraTextSecondary,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis
)
}
}
}
}
}

// Vertical Listing Rows Block: Quick Picks
if (quickPicks.isNotEmpty()) {
    item {
        Text(
            text = "Quick Picks",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = AuraTextPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )
    }
    items(quickPicks.take(5)) { song ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onPlaySong(song, quickPicks) }
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(AuraSurfaceElevated)
                ) {
                    if (!song.coverUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = song.coverUrl,
                            contentDescription = song.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = AuraTextMuted,
                            modifier = Modifier
                                .size(20.dp)
                                .align(Alignment.Center)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = song.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AuraTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        fontSize = 12.sp,
                        color = AuraTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = { onSongMenu(song) }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Menu",
                    tint = AuraTextSecondary
                )
            }
        }
    }
}
}
}
}