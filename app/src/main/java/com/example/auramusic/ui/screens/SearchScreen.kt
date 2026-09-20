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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.auramusic.model.GenreCategory
import com.example.auramusic.model.MoodCategory
import com.example.auramusic.model.MusicSearchResult
import com.example.auramusic.model.Song
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
fun SearchScreen(
    searchQuery: String,
    searchResults: MusicSearchResult,
    isSearching: Boolean,
    searchError: String?,
    moodsAndMoments: List<MoodCategory>,
    genres: List<GenreCategory>,
    onQueryChange: (String) -> Unit,
    onPlaySong: (Song, List<Song>) -> Unit,
    onSongMenu: (Song) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    var selectedFilterTab by remember { mutableStateOf("Explore") }
    val filterTabs = listOf("Explore", "Suggestions", "Album")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AuraBackground)
            .padding(top = 12.dp)
            .testTag("search_screen")
    ) {
        // Search Input Field matching subahbjhegm.png
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(26.dp))
                .background(AuraSurfaceElevated)
                .border(1.dp, AuraBorder, RoundedCornerShape(26.dp))
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = AuraTextSecondary,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                BasicTextField(
                    value = searchQuery,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_input_field"),
                    textStyle = TextStyle(
                        color = AuraTextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(AuraPrimary),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search YouTube Music...",
                                color = AuraTextMuted,
                                fontSize = 15.sp
                            )
                        }
                        innerTextField()
                    }
                )

                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onQueryChange("") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = AuraTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = "Global",
                        tint = AuraTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Filter Pills: Explore, Suggestions, Album
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterTabs) { tab ->
                val isSelected = tab == selectedFilterTab
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
                        .clickable { selectedFilterTab = tab }
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

        // Main Search Content
        if (isSearching) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AuraPrimary)
            }
        } else if (searchError != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = searchError,
                    fontSize = 15.sp,
                    color = AuraTextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AuraSurfaceVariant,
                        contentColor = AuraTextPrimary
                    )
                ) {
                    Text("Retry")
                }
            }
        } else if (searchQuery.isNotBlank()) {
            // Search Results List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Top Result
                searchResults.topResult?.let { topSong ->
                    item {
                        Text(
                            text = "Top result",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraTextPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(AuraSurfaceElevated)
                                .border(1.dp, AuraBorder, RoundedCornerShape(16.dp))
                                .clickable { onPlaySong(topSong, searchResults.songs) }
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AuraBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!topSong.coverUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = topSong.coverUrl,
                                        contentDescription = topSong.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.MusicNote,
                                        contentDescription = null,
                                        tint = AuraTextMuted,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = topSong.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AuraTextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Song • ${topSong.artist}",
                                    fontSize = 13.sp,
                                    color = AuraTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "Songs",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuraTextPrimary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                        )
                    }
                }

                items(searchResults.songs) { song ->
                    SearchResultSongItem(
                        song = song,
                        onClick = { onPlaySong(song, searchResults.songs) },
                        onMenuClick = { onSongMenu(song) }
                    )
                }

                if (searchResults.songs.isEmpty() && searchResults.topResult == null) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No results found for '$searchQuery'",
                                color = AuraTextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        } else {
            // Default Explore state: Moods & Moments (matching subahbjhegm.png)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                item {
                    Text(
                        text = "Moods & moments",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraTextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // 2-Column Grid of Moods & Moments
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        for (i in moodsAndMoments.indices step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                val item1 = moodsAndMoments[i]
                                MoodCard(
                                    mood = item1,
                                    onClick = { onQueryChange(item1.searchKeyword) },
                                    modifier = Modifier.weight(1f)
                                )

                                if (i + 1 < moodsAndMoments.size) {
                                    val item2 = moodsAndMoments[i + 1]
                                    MoodCard(
                                        mood = item2,
                                        onClick = { onQueryChange(item2.searchKeyword) },
                                        modifier = Modifier.weight(1f)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Genres Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Genres",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = AuraTextPrimary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(genres) { genre ->
                            Box(
                                modifier = Modifier
                                    .height(38.dp)
                                    .clip(RoundedCornerShape(19.dp))
                                    .background(AuraSurfaceElevated)
                                    .border(1.dp, AuraBorder, RoundedCornerShape(19.dp))
                                    .clickable { onQueryChange(genre.searchKeyword) }
                                    .padding(horizontal = 18.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = genre.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AuraTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MoodCard(
    mood: MoodCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(AuraSurfaceElevated)
            .border(1.dp, AuraBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = mood.title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AuraTextPrimary
        )
    }
}

@Composable
private fun SearchResultSongItem(
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
