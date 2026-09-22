package com.example.auramusic.data.provider

import com.example.auramusic.model.Song

data class AudioSource(
    val url: String,
    val userAgent: String = "Mozilla/5.0",
    val format: String = "audio/mp4",
    val bitrateKbps: Int = 128,
    val isLocal: Boolean = false
)

interface PlaybackSourceProvider {
    val providerId: String
    val name: String
    suspend fun resolve(song: Song): Result<AudioSource>
}
