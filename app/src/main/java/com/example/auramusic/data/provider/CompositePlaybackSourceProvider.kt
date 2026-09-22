package com.example.auramusic.data.provider

import android.util.Log
import com.example.auramusic.model.Song

class CompositePlaybackSourceProvider(
    private val youtubeProvider: YoutubePlaybackSourceProvider = YoutubePlaybackSourceProvider(),
    private val demoProvider: DemoPlaybackSourceProvider = DemoPlaybackSourceProvider()
) : PlaybackSourceProvider {

    override val providerId: String = "composite_provider"
    override val name: String = "Auto Fallback Provider"

    override suspend fun resolve(song: Song): Result<AudioSource> {
        Log.i("CompositeSourceProvider", "Attempting YouTube stream resolution for track: '${song.title}' (${song.id})")
        val ytResult = youtubeProvider.resolve(song)
        
        if (ytResult.isSuccess) {
            val audio = ytResult.getOrThrow()
            Log.i("CompositeSourceProvider", "YouTube stream resolved successfully. Bitrate: ${audio.bitrateKbps}kbps")
            return ytResult
        }

        val ytError = ytResult.exceptionOrNull()?.message ?: "Unknown resolution failure"
        Log.w("CompositeSourceProvider", "YouTube stream resolution blocked/failed ($ytError). Switching to resilient high-quality test stream for track: '${song.title}'")

        val demoResult = demoProvider.resolve(song)
        if (demoResult.isSuccess) {
            Log.i("CompositeSourceProvider", "Resilient test audio stream assigned successfully for '${song.title}'")
        }
        return demoResult
    }
}
