package com.example.auramusic.data

import com.example.auramusic.data.provider.MusicProvider
import com.example.auramusic.data.provider.OnlineMusicProvider
import com.example.auramusic.model.Album
import com.example.auramusic.model.Artist
import com.example.auramusic.model.EqualizerPreset
import com.example.auramusic.model.EqualizerSettings
import com.example.auramusic.model.GenreCategory
import com.example.auramusic.model.GoogleUser
import com.example.auramusic.model.LyricLine
import com.example.auramusic.model.MoodCategory
import com.example.auramusic.model.MusicSearchResult
import com.example.auramusic.model.Playlist
import com.example.auramusic.model.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class MusicRepository(
    val musicProvider: MusicProvider = OnlineMusicProvider()
) {
    // Real recommendations & shelves
    private val _recommendedSongs = MutableStateFlow<List<Song>>(emptyList())
    val recommendedSongs: StateFlow<List<Song>> = _recommendedSongs.asStateFlow()

    private val _trendingSongs = MutableStateFlow<List<Song>>(emptyList())
    val trendingSongs: StateFlow<List<Song>> = _trendingSongs.asStateFlow()

    private val _quickPicks = MutableStateFlow<List<Song>>(emptyList())
    val quickPicks: StateFlow<List<Song>> = _quickPicks.asStateFlow()

    private val _madeForYou = MutableStateFlow<List<Song>>(emptyList())
    val madeForYou: StateFlow<List<Song>> = _madeForYou.asStateFlow()

    // Home category chips
    val homeCategories = listOf("Podcasts", "Romance", "Feel good", "Workout", "Chill", "Gaming", "Party")
    private val _selectedHomeCategory = MutableStateFlow("Feel good")
    val selectedHomeCategory: StateFlow<String> = _selectedHomeCategory.asStateFlow()

    // Service loading and error states
    private val _isLoadingHome = MutableStateFlow(false)
    val isLoadingHome: StateFlow<Boolean> = _isLoadingHome.asStateFlow()

    private val _serviceError = MutableStateFlow<String?>(null)
    val serviceError: StateFlow<String?> = _serviceError.asStateFlow()

    // Search
    private val _searchResults = MutableStateFlow(MusicSearchResult(query = ""))
    val searchResults: StateFlow<MusicSearchResult> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    // Moods & Moments (matching screenshot subahbjhegm.png)
    val moodsAndMoments = listOf(
        MoodCategory("chill", "Chill", "Chill"),
        MoodCategory("commute", "Commute", "Commute"),
        MoodCategory("energize", "Energize", "High Energy"),
        MoodCategory("feel_good", "Feel good", "Feel Good"),
        MoodCategory("focus", "Focus", "Focus Beats"),
        MoodCategory("gaming", "Gaming", "Gaming"),
        MoodCategory("party", "Party", "Party Hits"),
        MoodCategory("romance", "Romance", "Romantic"),
        MoodCategory("sad", "Sad", "Sad Songs"),
        MoodCategory("sleep", "Sleep", "Sleep Music"),
        MoodCategory("workout", "Workout", "Workout Motivation")
    )

    // Genres (matching screenshot subahbjhegm.png)
    val genres = listOf(
        GenreCategory("african", "African", "Afrobeats"),
        GenreCategory("arabic", "Arabic", "Arabic Music"),
        GenreCategory("bengali", "Bengali", "Bengali Hits"),
        GenreCategory("bollywood", "Bollywood", "Bollywood Hindi"),
        GenreCategory("pop", "Pop", "Top Pop Hits"),
        GenreCategory("rock", "Rock", "Rock Anthems"),
        GenreCategory("hiphop", "Hip-Hop", "Hip Hop"),
        GenreCategory("electronic", "Electronic", "Electronic EDM"),
        GenreCategory("classical", "Classical", "Classical Masterpieces")
    )

    // User Library - Liked songs
    private val _likedSongs = MutableStateFlow<List<Song>>(emptyList())
    val likedSongs: StateFlow<List<Song>> = _likedSongs.asStateFlow()

    // User Playlists
    private val _playlists = MutableStateFlow<List<Playlist>>(
        listOf(
            Playlist(
                id = "pl_liked",
                title = "Liked",
                description = "Your favorite tracks",
                songIds = emptyList()
            ),
            Playlist(
                id = "pl_summer",
                title = "Hello, Summer! ☀️🍉",
                description = "Seasonal soundtrack",
                songIds = emptyList()
            ),
            Playlist(
                id = "pl_top50",
                title = "My top 50",
                description = "Most listened tracks",
                songIds = emptyList()
            )
        )
    )
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    // Recently Played History
    private val _recentlyPlayed = MutableStateFlow<List<Song>>(emptyList())
    val recentlyPlayed: StateFlow<List<Song>> = _recentlyPlayed.asStateFlow()

    // Google User Account
    private val _googleUser = MutableStateFlow(
        GoogleUser(
            name = "",
            email = "",
            photoUrl = null,
            isSignedIn = false
        )
    )
    val googleUser: StateFlow<GoogleUser> = _googleUser.asStateFlow()

    // First Launch Welcome Screen state
    private val _isWelcomeDialogVisible = MutableStateFlow(true)
    val isWelcomeDialogVisible: StateFlow<Boolean> = _isWelcomeDialogVisible.asStateFlow()

    // Music Recognition state (Echo Find)
    private val _isRecognizing = MutableStateFlow(false)
    val isRecognizing: StateFlow<Boolean> = _isRecognizing.asStateFlow()

    private val _recognizedSong = MutableStateFlow<Song?>(null)
    val recognizedSong: StateFlow<Song?> = _recognizedSong.asStateFlow()

    // Equalizer Presets
    val equalizerPresets = listOf(
        EqualizerPreset("Flat", listOf(0f, 0f, 0f, 0f, 0f), 0f),
        EqualizerPreset("Bass Boost", listOf(6f, 4f, 1f, 0f, -1f), 60f),
        EqualizerPreset("Vocal Clarity", listOf(-2f, 0f, 4f, 5f, 2f), 15f),
        EqualizerPreset("Acoustic", listOf(3f, 2f, 1f, 2f, 4f), 20f),
        EqualizerPreset("Electronic", listOf(5f, 3f, 0f, 2f, 5f), 50f),
        EqualizerPreset("Rock", listOf(4f, 2f, -1f, 3f, 4f), 35f)
    )

    private val _equalizerSettings = MutableStateFlow(
        EqualizerSettings(
            currentPreset = "Flat",
            bands = listOf(0f, 0f, 0f, 0f, 0f),
            bassBoost = 15f,
            surroundSound = 0f,
            isEnabled = true
        )
    )
    val equalizerSettings: StateFlow<EqualizerSettings> = _equalizerSettings.asStateFlow()

    suspend fun loadHomeData() {
        _isLoadingHome.value = true
        _serviceError.value = null

        val result = musicProvider.getRecommendations()
        result.onSuccess { songs ->
            _recommendedSongs.value = songs
            _trendingSongs.value = songs.shuffled()
            _quickPicks.value = songs.take(6)
            _madeForYou.value = songs.reversed()
            _isLoadingHome.value = false

            // Pre-populate liked with a favorite track if empty
            if (_likedSongs.value.isEmpty() && songs.isNotEmpty()) {
                val first = songs.first().copy(isLiked = true)
                _likedSongs.value = listOf(first)
            }
        }.onFailure { err ->
            _serviceError.value = "Music service is currently unavailable."
            _isLoadingHome.value = false
        }
    }

    fun selectHomeCategory(category: String) {
        _selectedHomeCategory.value = category
    }

    suspend fun searchMusic(query: String) {
        if (query.isBlank()) {
            _searchResults.value = MusicSearchResult(query = "")
            _isSearching.value = false
            _searchError.value = null
            return
        }

        _isSearching.value = true
        _searchError.value = null

        val result = musicProvider.search(query)
        result.onSuccess { searchData ->
            _searchResults.value = searchData
            _isSearching.value = false
        }.onFailure { err ->
            _searchError.value = "Music service is currently unavailable."
            _isSearching.value = false
        }
    }

    suspend fun getLyricsForSong(song: Song): Result<List<LyricLine>> {
        return musicProvider.getLyrics(
            songId = song.id,
            title = song.title,
            artist = song.artist,
            durationSec = (song.durationMs / 1000).toInt()
        )
    }

    fun toggleLikeSong(song: Song) {
        val currentLiked = _likedSongs.value.toMutableList()
        val index = currentLiked.indexOfFirst { it.id == song.id }
        if (index >= 0) {
            currentLiked.removeAt(index)
        } else {
            currentLiked.add(song.copy(isLiked = true))
        }
        _likedSongs.value = currentLiked

        _playlists.update { list ->
            list.map { pl ->
                if (pl.id == "pl_liked") {
                    pl.copy(songIds = currentLiked.map { it.id })
                } else pl
            }
        }
    }

    fun isSongLiked(songId: String): Boolean {
        return _likedSongs.value.any { it.id == songId }
    }

    fun recordRecentlyPlayed(song: Song) {
        val updated = _recentlyPlayed.value.filterNot { it.id == song.id }.toMutableList()
        updated.add(0, song)
        _recentlyPlayed.value = updated.take(25)
    }

    fun createPlaylist(title: String, description: String = "") {
        val newId = "pl_${System.currentTimeMillis()}"
        val newPl = Playlist(
            id = newId,
            title = title,
            description = description,
            isUserCreated = true
        )
        _playlists.value = _playlists.value + newPl
    }

    fun deletePlaylist(playlistId: String) {
        _playlists.value = _playlists.value.filterNot { it.id == playlistId }
    }

    fun addSongToPlaylist(playlistId: String, songId: String) {
        _playlists.update { list ->
            list.map { pl ->
                if (pl.id == playlistId && !pl.songIds.contains(songId)) {
                    pl.copy(songIds = pl.songIds + songId)
                } else pl
            }
        }
    }

    fun removeSongFromPlaylist(playlistId: String, songId: String) {
        _playlists.update { list ->
            list.map { pl ->
                if (pl.id == playlistId) {
                    pl.copy(songIds = pl.songIds - songId)
                } else pl
            }
        }
    }

    // Authentic Google Sign-In
    fun signInWithGoogle(name: String, email: String, photoUrl: String? = null) {
        _googleUser.value = GoogleUser(
            name = name,
            email = email,
            photoUrl = photoUrl,
            isSignedIn = true
        )
    }

    fun signOutGoogle() {
        _googleUser.value = GoogleUser(
            name = "",
            email = "",
            photoUrl = null,
            isSignedIn = false
        )
    }

    // Welcome Dialog
    fun dismissWelcomeDialog() {
        _isWelcomeDialogVisible.value = false
    }

    fun showWelcomeDialog() {
        _isWelcomeDialogVisible.value = true
    }

    // Music Recognition
    fun startRecognizing() {
        _isRecognizing.value = true
        _recognizedSong.value = null
    }

    fun finishRecognizing(song: Song?) {
        _isRecognizing.value = false
        _recognizedSong.value = song
    }

    // Equalizer
    fun setEqualizerPreset(preset: EqualizerPreset) {
        _equalizerSettings.value = _equalizerSettings.value.copy(
            currentPreset = preset.name,
            bands = preset.bands,
            bassBoost = preset.bassBoost
        )
    }

    fun updateEqualizerBand(index: Int, gainDb: Float) {
        val bands = _equalizerSettings.value.bands.toMutableList()
        if (index in bands.indices) {
            bands[index] = gainDb
            _equalizerSettings.value = _equalizerSettings.value.copy(
                bands = bands,
                currentPreset = "Custom"
            )
        }
    }

    fun updateBassBoost(boost: Float) {
        _equalizerSettings.value = _equalizerSettings.value.copy(bassBoost = boost)
    }

    fun updateSurroundSound(surround: Float) {
        _equalizerSettings.value = _equalizerSettings.value.copy(surroundSound = surround)
    }

    fun toggleEqualizer(enabled: Boolean) {
        _equalizerSettings.value = _equalizerSettings.value.copy(isEnabled = enabled)
    }
}
