package com.example.auramusic.data.provider

import com.example.auramusic.model.Song

class DemoPlaybackSourceProvider : PlaybackSourceProvider {
    override val providerId: String = "demo_provider"
    override val name: String = "High-Quality Public Domain Streams"

    // High reliability, fast public domain audio streams for verifying Media3 playback pipeline
    private val testAudioUrls = listOf(
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3",
        "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-8.mp3"
    )

    override suspend fun resolve(song: Song): Result<AudioSource> {
        val hash = kotlin.math.abs(song.id.hashCode())
        val selectedUrl = testAudioUrls[hash % testAudioUrls.size]
        return Result.success(
            AudioSource(
                url = selectedUrl,
                userAgent = "AuraMusic/1.0 (Android)",
                format = "audio/mp3",
                bitrateKbps = 320,
                isLocal = false
            )
        )
    }
}
