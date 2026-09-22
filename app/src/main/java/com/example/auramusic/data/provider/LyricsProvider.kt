package com.example.auramusic.data.provider

import com.example.auramusic.model.Song

data class LyricLine(
    val timeMs: Long,
    val text: String
)

data class Lyrics(
    val plainLyrics: String,
    val syncedLyrics: List<LyricLine> = emptyList()
)

interface LyricsProvider {
    suspend fun getLyrics(song: Song): Result<Lyrics>
}
