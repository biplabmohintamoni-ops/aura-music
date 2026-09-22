package com.example.auramusic.model

import androidx.annotation.DrawableRes

data class LyricLine(
    val timestampMs: Long,
    val text: String
)

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val audioUrl: String = "",
    val previewUrl: String? = null,
    val youtubeVideoId: String? = null,
    val candidateVideoIds: List<String> = emptyList(),
    val coverUrl: String? = null,
    @DrawableRes val coverRes: Int? = null,
    val genre: String = "Music",
    val isPlayable: Boolean = true,
    val isLiked: Boolean = false,
    val isDownloaded: Boolean = false,
    val lyrics: List<LyricLine> = emptyList(),
    val releaseYear: Int = 2024,
    val artistId: String? = null,
    val collectionId: String? = null,
    val providerId: String = id,
    val channelTitle: String? = null,
    val isExplicit: Boolean = false,
    val primaryAuraColor: Long = 0xFF1DB954,
    val secondaryAuraColor: Long = 0xFFE4E4E7
) {
    val url: String get() = audioUrl
    val thumbnailUrl: String get() = coverUrl ?: ""
}

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val coverUrl: String? = null,
    val releaseYear: Int = 2024,
    val trackCount: Int = 10,
    val providerId: String = id
)

data class Artist(
    val id: String,
    val name: String,
    val genre: String = "",
    val monthlyListeners: String = "",
    val avatarUrl: String = "",
    val bio: String = "",
    val providerId: String = id
)

data class Playlist(
    val id: String,
    val title: String,
    val description: String,
    @DrawableRes val coverRes: Int? = null,
    val coverUrl: String? = null,
    val songIds: List<String> = emptyList(),
    val isUserCreated: Boolean = false
)

data class MusicSearchResult(
    val query: String,
    val topResult: Song? = null,
    val songs: List<Song> = emptyList(),
    val artists: List<Artist> = emptyList(),
    val albums: List<Album> = emptyList(),
    val playlists: List<Playlist> = emptyList()
)

data class GoogleUser(
    val name: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val isSignedIn: Boolean = false
)

enum class RepeatMode {
    OFF,
    ALL,
    ONE
}

data class EqualizerPreset(
    val name: String,
    val bands: List<Float>,
    val bassBoost: Float
)

data class EqualizerSettings(
    val currentPreset: String = "Flat",
    val bands: List<Float> = listOf(0f, 0f, 0f, 0f, 0f),
    val bassBoost: Float = 20f,
    val surroundSound: Float = 0f,
    val isEnabled: Boolean = true
)

data class MoodCategory(
    val id: String,
    val title: String,
    val searchKeyword: String
)

data class GenreCategory(
    val id: String,
    val title: String,
    val searchKeyword: String
)
