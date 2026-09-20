package com.example.auramusic.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AuraThemeMode {
    DARK,
    LIGHT,
    LIQUID_GLASS
}

class SettingsManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("aura_music_settings", Context.MODE_PRIVATE)

    // Theme Mode
    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AuraThemeMode> = _themeMode.asStateFlow()

    // Dynamic Color
    private val _dynamicColorEnabled = MutableStateFlow(prefs.getBoolean(KEY_DYNAMIC_COLOR, true))
    val dynamicColorEnabled: StateFlow<Boolean> = _dynamicColorEnabled.asStateFlow()

    // Blur Intensity (0.0f to 1.0f)
    private val _blurIntensity = MutableStateFlow(prefs.getFloat(KEY_BLUR_INTENSITY, 0.60f))
    val blurIntensity: StateFlow<Float> = _blurIntensity.asStateFlow()

    // Use Album Art Colors
    private val _useAlbumArtColors = MutableStateFlow(prefs.getBoolean(KEY_ALBUM_ART_COLORS, true))
    val useAlbumArtColors: StateFlow<Boolean> = _useAlbumArtColors.asStateFlow()

    // Audio Streaming Quality
    private val _audioQuality = MutableStateFlow(prefs.getString(KEY_AUDIO_QUALITY, "Auto (Recommended)") ?: "Auto (Recommended)")
    val audioQuality: StateFlow<String> = _audioQuality.asStateFlow()

    // Always Resume Playback
    private val _alwaysResumePlayback = MutableStateFlow(prefs.getBoolean(KEY_ALWAYS_RESUME, true))
    val alwaysResumePlayback: StateFlow<Boolean> = _alwaysResumePlayback.asStateFlow()

    // Crossfade Seconds (0 to 12)
    private val _crossfadeSeconds = MutableStateFlow(prefs.getInt(KEY_CROSSFADE, 3))
    val crossfadeSeconds: StateFlow<Int> = _crossfadeSeconds.asStateFlow()

    // Background Playback
    private val _backgroundPlayback = MutableStateFlow(prefs.getBoolean(KEY_BACKGROUND_PLAYBACK, true))
    val backgroundPlayback: StateFlow<Boolean> = _backgroundPlayback.asStateFlow()

    // Download Quality
    private val _downloadQuality = MutableStateFlow(prefs.getString(KEY_DOWNLOAD_QUALITY, "High (256 kbps)") ?: "High (256 kbps)")
    val downloadQuality: StateFlow<String> = _downloadQuality.asStateFlow()

    // Download over Wi-Fi only
    private val _downloadWifiOnly = MutableStateFlow(prefs.getBoolean(KEY_DOWNLOAD_WIFI_ONLY, false))
    val downloadWifiOnly: StateFlow<Boolean> = _downloadWifiOnly.asStateFlow()

    // Cache size
    private val _cacheSizeFormatted = MutableStateFlow("48.2 MB")
    val cacheSizeFormatted: StateFlow<String> = _cacheSizeFormatted.asStateFlow()

    private fun loadThemeMode(): AuraThemeMode {
        val saved = prefs.getString(KEY_THEME_MODE, AuraThemeMode.DARK.name) ?: AuraThemeMode.DARK.name
        return try {
            AuraThemeMode.valueOf(saved)
        } catch (e: Exception) {
            AuraThemeMode.DARK
        }
    }

    fun setThemeMode(mode: AuraThemeMode) {
        _themeMode.value = mode
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        _dynamicColorEnabled.value = enabled
        prefs.edit().putBoolean(KEY_DYNAMIC_COLOR, enabled).apply()
    }

    fun setBlurIntensity(intensity: Float) {
        val clamped = intensity.coerceIn(0.10f, 1.0f)
        _blurIntensity.value = clamped
        prefs.edit().putFloat(KEY_BLUR_INTENSITY, clamped).apply()
    }

    fun setUseAlbumArtColors(enabled: Boolean) {
        _useAlbumArtColors.value = enabled
        prefs.edit().putBoolean(KEY_ALBUM_ART_COLORS, enabled).apply()
    }

    fun setAudioQuality(quality: String) {
        _audioQuality.value = quality
        prefs.edit().putString(KEY_AUDIO_QUALITY, quality).apply()
    }

    fun setAlwaysResumePlayback(enabled: Boolean) {
        _alwaysResumePlayback.value = enabled
        prefs.edit().putBoolean(KEY_ALWAYS_RESUME, enabled).apply()
    }

    fun setCrossfadeSeconds(seconds: Int) {
        val clamped = seconds.coerceIn(0, 12)
        _crossfadeSeconds.value = clamped
        prefs.edit().putInt(KEY_CROSSFADE, clamped).apply()
    }

    fun setBackgroundPlayback(enabled: Boolean) {
        _backgroundPlayback.value = enabled
        prefs.edit().putBoolean(KEY_BACKGROUND_PLAYBACK, enabled).apply()
    }

    fun setDownloadQuality(quality: String) {
        _downloadQuality.value = quality
        prefs.edit().putString(KEY_DOWNLOAD_QUALITY, quality).apply()
    }

    fun setDownloadWifiOnly(enabled: Boolean) {
        _downloadWifiOnly.value = enabled
        prefs.edit().putBoolean(KEY_DOWNLOAD_WIFI_ONLY, enabled).apply()
    }

    fun clearCache() {
        _cacheSizeFormatted.value = "0.0 KB"
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_DYNAMIC_COLOR = "dynamic_color"
        private const val KEY_BLUR_INTENSITY = "blur_intensity"
        private const val KEY_ALBUM_ART_COLORS = "album_art_colors"
        private const val KEY_AUDIO_QUALITY = "audio_quality"
        private const val KEY_ALWAYS_RESUME = "always_resume"
        private const val KEY_CROSSFADE = "crossfade"
        private const val KEY_BACKGROUND_PLAYBACK = "background_playback"
        private const val KEY_DOWNLOAD_QUALITY = "download_quality"
        private const val KEY_DOWNLOAD_WIFI_ONLY = "download_wifi_only"
    }
}
