package com.example.auramusic.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.auramusic.data.AuraThemeMode
import com.example.auramusic.data.MusicRepository
import com.example.auramusic.data.SettingsManager
import com.example.auramusic.model.EqualizerPreset
import com.example.auramusic.model.GoogleUser
import com.example.auramusic.model.LyricLine
import com.example.auramusic.model.MusicSearchResult
import com.example.auramusic.model.Playlist
import com.example.auramusic.model.RepeatMode
import com.example.auramusic.model.Song
import com.example.auramusic.player.MusicPlayerController
import com.example.auramusic.ui.theme.AuraAccentPurple
import com.example.auramusic.util.AlbumArtColorExtractor
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MusicViewModel(application: Application) : AndroidViewModel(application) {

    val repository = MusicRepository()
    val playerController = MusicPlayerController(application.applicationContext)
    val settingsManager = SettingsManager(application.applicationContext)

    // Settings flows
    val themeMode: StateFlow<AuraThemeMode> = settingsManager.themeMode
    val dynamicColorEnabled: StateFlow<Boolean> = settingsManager.dynamicColorEnabled
    val blurIntensity: StateFlow<Float> = settingsManager.blurIntensity
    val useAlbumArtColors: StateFlow<Boolean> = settingsManager.useAlbumArtColors
    val audioQuality: StateFlow<String> = settingsManager.audioQuality
    val alwaysResumePlayback: StateFlow<Boolean> = settingsManager.alwaysResumePlayback
    val crossfadeSeconds: StateFlow<Int> = settingsManager.crossfadeSeconds
    val backgroundPlayback: StateFlow<Boolean> = settingsManager.backgroundPlayback
    val downloadQuality: StateFlow<String> = settingsManager.downloadQuality
    val downloadWifiOnly: StateFlow<Boolean> = settingsManager.downloadWifiOnly
    val cacheSizeFormatted: StateFlow<String> = settingsManager.cacheSizeFormatted

    // Dynamic accent color extracted from current track artwork
    private val _dynamicAccentColor = MutableStateFlow(AuraAccentPurple)
    val dynamicAccentColor: StateFlow<Color> = _dynamicAccentColor.asStateFlow()

    // Player state delegated from playerController
    val currentSong: StateFlow<Song?> = playerController.currentSong
    val isPlaying: StateFlow<Boolean> = playerController.isPlaying
    val isBuffering: StateFlow<Boolean> = playerController.isBuffering
    val currentPositionMs: StateFlow<Long> = playerController.currentPositionMs
    val durationMs: StateFlow<Long> = playerController.durationMs
    val queue: StateFlow<List<Song>> = playerController.queue
    val queueIndex: StateFlow<Int> = playerController.queueIndex
    val isShuffle: StateFlow<Boolean> = playerController.isShuffle
    val repeatMode: StateFlow<RepeatMode> = playerController.repeatMode
    val volume: StateFlow<Float> = playerController.volume
    val isMuted: StateFlow<Boolean> = playerController.isMuted
    val playbackError: StateFlow<String?> = playerController.playbackError

    // Sleep Timer
    private val _sleepTimerMinutesRemaining = MutableStateFlow<Int?>(null)
    val sleepTimerMinutesRemaining: StateFlow<Int?> = _sleepTimerMinutesRemaining.asStateFlow()
    private var sleepTimerJob: Job? = null

    // Repository flows
    val recommendedSongs: StateFlow<List<Song>> = repository.recommendedSongs
    val trendingSongs: StateFlow<List<Song>> = repository.trendingSongs
    val quickPicks: StateFlow<List<Song>> = repository.quickPicks
    val madeForYou: StateFlow<List<Song>> = repository.madeForYou
    val homeCategories = repository.homeCategories
    val selectedHomeCategory: StateFlow<String> = repository.selectedHomeCategory
    val isLoadingHome: StateFlow<Boolean> = repository.isLoadingHome
    val serviceError: StateFlow<String?> = repository.serviceError

    val searchResults: StateFlow<MusicSearchResult> = repository.searchResults
    val isSearching: StateFlow<Boolean> = repository.isSearching
    val searchError: StateFlow<String?> = repository.searchError
    val moodsAndMoments = repository.moodsAndMoments
    val genres = repository.genres

    val likedSongs: StateFlow<List<Song>> = repository.likedSongs
    val playlists: StateFlow<List<Playlist>> = repository.playlists
    val recentlyPlayed: StateFlow<List<Song>> = repository.recentlyPlayed
    val googleUser: StateFlow<GoogleUser> = repository.googleUser
    val isWelcomeDialogVisible: StateFlow<Boolean> = repository.isWelcomeDialogVisible
    val isRecognizing: StateFlow<Boolean> = repository.isRecognizing
    val recognizedSong: StateFlow<Song?> = repository.recognizedSong

    val equalizerPresets = repository.equalizerPresets
    val equalizerSettings = repository.equalizerSettings

    // UI state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isNowPlayingExpanded = MutableStateFlow(false)
    val isNowPlayingExpanded: StateFlow<Boolean> = _isNowPlayingExpanded.asStateFlow()

    private val _isLyricsExpanded = MutableStateFlow(false)
    val isLyricsExpanded: StateFlow<Boolean> = _isLyricsExpanded.asStateFlow()

    private val _isRecognizeScreenOpen = MutableStateFlow(false)
    val isRecognizeScreenOpen: StateFlow<Boolean> = _isRecognizeScreenOpen.asStateFlow()

    private val _isEqualizerOpen = MutableStateFlow(false)
    val isEqualizerOpen: StateFlow<Boolean> = _isEqualizerOpen.asStateFlow()

    private val _isSettingsOpen = MutableStateFlow(false)
    val isSettingsOpen: StateFlow<Boolean> = _isSettingsOpen.asStateFlow()

    private val _isQuickMenuOpen = MutableStateFlow(false)
    val isQuickMenuOpen: StateFlow<Boolean> = _isQuickMenuOpen.asStateFlow()

    private val _selectedPlaylist = MutableStateFlow<Playlist?>(null)
    val selectedPlaylist: StateFlow<Playlist?> = _selectedPlaylist.asStateFlow()

    private val _songForContextMenu = MutableStateFlow<Song?>(null)
    val songForContextMenu: StateFlow<Song?> = _songForContextMenu.asStateFlow()

    // Lyrics state
    private val _activeSongLyrics = MutableStateFlow<List<LyricLine>>(emptyList())
    val activeSongLyrics: StateFlow<List<LyricLine>> = _activeSongLyrics.asStateFlow()

    private val _isLoadingLyrics = MutableStateFlow(false)
    val isLoadingLyrics: StateFlow<Boolean> = _isLoadingLyrics.asStateFlow()

    private var searchDebounceJob: Job? = null
    private var lyricsFetchJob: Job? = null

    init {
        loadHomeData()
        observeCurrentSongForDynamicAccent()
    }

    private fun observeCurrentSongForDynamicAccent() {
        viewModelScope.launch {
            currentSong.collect { song ->
                if (dynamicColorEnabled.value || useAlbumArtColors.value) {
                    val accent = AlbumArtColorExtractor.extractAccentColor(
                        getApplication<Application>().applicationContext,
                        song?.coverUrl
                    )
                    _dynamicAccentColor.value = accent
                }
            }
        }
    }

    fun loadHomeData() {
        viewModelScope.launch {
            repository.loadHomeData()
        }
    }

    fun selectHomeCategory(category: String) {
        repository.selectHomeCategory(category)
        updateSearchQuery(category)
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        searchDebounceJob?.cancel()

        if (query.isBlank()) {
            viewModelScope.launch {
                repository.searchMusic("")
            }
            return
        }

        searchDebounceJob = viewModelScope.launch {
            delay(350)
            repository.searchMusic(query)
        }
    }

    fun playSong(song: Song, queue: List<Song> = emptyList()) {
        playerController.playSong(song, queue)
        repository.recordRecentlyPlayed(song)
        fetchLyrics(song)
    }

    fun togglePlayPause() {
        playerController.togglePlayPause()
    }

    fun next() {
        playerController.next()
        playerController.currentSong.value?.let { fetchLyrics(it) }
    }

    fun previous() {
        playerController.previous()
        playerController.currentSong.value?.let { fetchLyrics(it) }
    }

    fun seekTo(positionMs: Long) {
        playerController.seekTo(positionMs)
    }

    fun toggleShuffle() {
        playerController.toggleShuffle()
    }

    fun cycleRepeatMode() {
        playerController.cycleRepeatMode()
    }

    fun setVolume(vol: Float) {
        playerController.setVolume(vol)
    }

    fun toggleMute() {
        playerController.toggleMute()
    }

    fun toggleLikeSong(song: Song) {
        repository.toggleLikeSong(song)
    }

    fun isSongLiked(songId: String): Boolean {
        return repository.isSongLiked(songId)
    }

    private fun fetchLyrics(song: Song) {
        lyricsFetchJob?.cancel()
        _isLoadingLyrics.value = true
        _activeSongLyrics.value = emptyList()

        lyricsFetchJob = viewModelScope.launch {
            val res = repository.getLyricsForSong(song)
            res.onSuccess { lines ->
                _activeSongLyrics.value = lines
                _isLoadingLyrics.value = false
            }.onFailure {
                _activeSongLyrics.value = emptyList()
                _isLoadingLyrics.value = false
            }
        }
    }

    // Sleep Timer
    fun startSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        _sleepTimerMinutesRemaining.value = minutes

        sleepTimerJob = viewModelScope.launch {
            var remaining = minutes
            while (remaining > 0) {
                delay(60000L)
                remaining -= 1
                _sleepTimerMinutesRemaining.value = remaining
            }
            playerController.pause()
            _sleepTimerMinutesRemaining.value = null
        }
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        _sleepTimerMinutesRemaining.value = null
    }

    // Recognition
    fun startRecognition() {
        repository.startRecognizing()
        viewModelScope.launch {
            delay(3500)
            val match = repository.recommendedSongs.value.randomOrNull()
            repository.finishRecognizing(match)
        }
    }

    // Google Sign-In
    fun signInWithGoogle(name: String, email: String, photoUrl: String? = null) {
        repository.signInWithGoogle(name, email, photoUrl)
    }

    fun signOutGoogle() {
        repository.signOutGoogle()
    }

    // Welcome Screen
    fun dismissWelcomeDialog() {
        repository.dismissWelcomeDialog()
    }

    fun showWelcomeDialog() {
        repository.showWelcomeDialog()
    }

    // Equalizer
    fun setEqualizerPreset(preset: EqualizerPreset) {
        repository.setEqualizerPreset(preset)
    }

    fun updateEqualizerBand(index: Int, gainDb: Float) {
        repository.updateEqualizerBand(index, gainDb)
    }

    fun updateBassBoost(boost: Float) {
        repository.updateBassBoost(boost)
    }

    fun updateSurroundSound(surround: Float) {
        repository.updateSurroundSound(surround)
    }

    fun toggleEqualizer(enabled: Boolean) {
        repository.toggleEqualizer(enabled)
    }

    // Navigation & Sheet Toggles
    fun setNowPlayingExpanded(expanded: Boolean) {
        _isNowPlayingExpanded.value = expanded
    }

    fun setLyricsExpanded(expanded: Boolean) {
        _isLyricsExpanded.value = expanded
    }

    fun setRecognizeScreenOpen(open: Boolean) {
        _isRecognizeScreenOpen.value = open
    }

    fun setEqualizerOpen(open: Boolean) {
        _isEqualizerOpen.value = open
    }

    fun setSettingsOpen(open: Boolean) {
        _isSettingsOpen.value = open
    }

    fun setQuickMenuOpen(open: Boolean) {
        _isQuickMenuOpen.value = open
    }

    fun selectPlaylist(playlist: Playlist?) {
        _selectedPlaylist.value = playlist
    }

    fun openSongContextMenu(song: Song) {
        _songForContextMenu.value = song
    }

    fun closeSongContextMenu() {
        _songForContextMenu.value = null
    }

    // Settings Actions
    fun setThemeMode(mode: AuraThemeMode) {
        settingsManager.setThemeMode(mode)
    }

    fun setDynamicColorEnabled(enabled: Boolean) {
        settingsManager.setDynamicColorEnabled(enabled)
        if (!enabled) {
            _dynamicAccentColor.value = AuraAccentPurple
        } else {
            observeCurrentSongForDynamicAccent()
        }
    }

    fun setBlurIntensity(intensity: Float) {
        settingsManager.setBlurIntensity(intensity)
    }

    fun setUseAlbumArtColors(enabled: Boolean) {
        settingsManager.setUseAlbumArtColors(enabled)
    }

    fun setAudioQuality(quality: String) {
        settingsManager.setAudioQuality(quality)
    }

    fun setAlwaysResumePlayback(enabled: Boolean) {
        settingsManager.setAlwaysResumePlayback(enabled)
    }

    fun setCrossfadeSeconds(seconds: Int) {
        settingsManager.setCrossfadeSeconds(seconds)
    }

    fun setBackgroundPlayback(enabled: Boolean) {
        settingsManager.setBackgroundPlayback(enabled)
    }

    fun setDownloadQuality(quality: String) {
        settingsManager.setDownloadQuality(quality)
    }

    fun setDownloadWifiOnly(enabled: Boolean) {
        settingsManager.setDownloadWifiOnly(enabled)
    }

    fun clearCache() {
        settingsManager.clearCache()
    }

    override fun onCleared() {
        super.onCleared()
        playerController.release()
    }
}
