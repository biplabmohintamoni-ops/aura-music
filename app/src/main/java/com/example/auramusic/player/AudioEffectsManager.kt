package com.example.auramusic.player

import android.media.audiofx.Equalizer
import android.util.Log

class AudioEffectsManager {

    private var equalizer: Equalizer? = null
    private var activeAudioSessionId: Int = 0

    fun attachAudioSession(sessionId: Int) {
        if (sessionId <= 0 || sessionId == activeAudioSessionId) return
        release()
        try {
            activeAudioSessionId = sessionId
            equalizer = Equalizer(0, sessionId).apply {
                enabled = true
            }
            Log.i("AudioEffectsManager", "Attached System Equalizer to AudioSession ID: $sessionId")
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "Failed to initialize Equalizer for session $sessionId: ${e.message}")
        }
    }

    fun setEnabled(enabled: Boolean) {
        try {
            equalizer?.enabled = enabled
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "Failed to set equalizer state: ${e.message}")
        }
    }

    fun getBandLevels(): ShortArray {
        val eq = equalizer ?: return shortArrayOf()
        val numBands = eq.numberOfBands
        val levels = ShortArray(numBands.toInt())
        for (i in 0 until numBands) {
            levels[i] = eq.getBandLevel(i.toShort())
        }
        return levels
    }

    fun setBandLevel(band: Short, level: Short) {
        try {
            equalizer?.setBandLevel(band, level)
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "Failed to set band level $band: ${e.message}")
        }
    }

    fun release() {
        try {
            equalizer?.release()
            equalizer = null
            activeAudioSessionId = 0
        } catch (e: Exception) {
            Log.w("AudioEffectsManager", "Failed to release Equalizer: ${e.message}")
        }
    }
}
