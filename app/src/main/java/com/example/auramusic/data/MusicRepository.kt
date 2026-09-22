package com.example.auramusic.data

import android.content.Context
import android.util.Log
import com.example.auramusic.data.auth.AccountState
import com.example.auramusic.data.auth.AuthRepository
import com.example.auramusic.data.db.AuraDatabase
import com.example.auramusic.data.db.FavoriteTrackEntity
import com.example.auramusic.data.db.HistoryTrackEntity
import com.example.auramusic.data.db.PlaylistEntity
import com.example.auramusic.data.provider.AudioSource
import com.example.auramusic.data.provider.CompositePlaybackSourceProvider
import com.example.auramusic.data.provider.LrclibLyricsProvider
import com.example.auramusic.data.provider.Lyrics
import com.example.auramusic.data.provider.LyricsProvider
import com.example.auramusic.data.provider.MusicProvider
import com.example.auramusic.data.provider.OnlineMusicProvider
import com.example.auramusic.data.provider.PlaybackSourceProvider
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MusicRepository(
    context: Context? = null,
    val musicProvider: MusicProvider = OnlineMusicProvider(),
    val sourceProvider: PlaybackSourceProvider = CompositePlaybackSourceProvider(),
    val lyricsProvider: LyricsProvider = LrclibLyricsProvider()
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val db = context?.let { AuraDatabase.getDatabase(it) }
    private val authRepository = context?.let { AuthRepository.getInstance(it) }

    val accountState: StateFlow<AccountState>? = authRepository?.accountState

    private val _recommendedSongs = MutableStateFlow<List<Song>>(emptyList())
    val recommendedSongs: StateFlow<List<Song>> = _recommendedSongs.asStateFlow()

    private val _trendingSongs = MutableStateFlow<List<Song>>(emptyList())
    val trendingSongs: StateFlow<List<Song>> = _trendingSongs.asStateFlow()

    private val _quickPicks = MutableStateFlow<List<Song>>(emptyList())
    val quickPicks: StateFlow<List<Song>> = _quickPicks.asStateFlow()

    private val _madeForYou = MutableStateFlow<List<Song>>(emptyList())
    val madeForYou: StateFlow<List<Song>> = _madeForYou.asStateFlow()

    val homeCategories = listOf("Podcasts", "Romance", "Feel good", "Workout", "Chill", "Gaming", "Party")
    private val _selectedHomeCategory = MutableStateFlow("Feel good")
    val selectedHomeCategory: StateFlow<String> = _selectedHomeCategory.asStateFlow()

    private val _isLoadingHome = MutableStateFlow(false)
    val isLoadingHome: StateFlow<Boolean> = _isLoadingHome.asStateFlow()

    private val _serviceError = MutableStateFlow<String?>(null)
    val serviceError: StateFlow<String?> = _serviceError.asStateFlow()

    private val _searchResults = MutableStateFlow(MusicSearchResult(query = ""))
    val searchResults: StateFlow<MusicSearchResult> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

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

    private val _likedSongs = MutableStateFlow<List<Song>>(emptyList())
    val likedSongs: StateFlow<List<Song>> = _likedSongs.asStateFlow()

    private val _playlists = MutableStateFlow<List<Playlist>>(
        listOf(
            Playlist(
                id = "pl_liked",
                title = "Liked",
                description = "Your favorite tracks",
                songIds = emptyList()
            ),
            Playlist(
                id = "pl_trending",
                title = "Trending Mix 🌧️🎵",
                description = "Rainy season top soundtracks",
                songIds = emptyList()
            )
        )
    )
    val playlists: StateFlow<List<Playlist>> = _playlists.asStateFlow()

    private val _recentlyPlayed = MutableStateFlow<List<Song>>(emptyList())
    val recentlyPlayed: StateFlow<List<Song>> = _recentlyPlayed.asStateFlow()

    private val _googleUser = MutableStateFlow(GoogleUser(name = "", email = "", photoUrl = null, isSignedIn = false))
    val googleUser: StateFlow<GoogleUser> = _googleUser.asStateFlow()

    private val _isWelcomeDialogVisible = MutableStateFlow(true)
    val isWelcomeDialogVisible: StateFlow<Boolean> = _isWelcomeDialogVisible.asStateFlow()

    private val _isRecognizing = MutableStateFlow(false)
    val isRecognizing: StateFlow<Boolean> = _isRecognizing.asStateFlow()

    private val _recognizedSong = MutableStateFlow<Song?>(null)
    val recognizedSong: StateFlow<Song?> = _recognizedSong.asStateFlow()

    val equalizerPresets = listOf(
        EqualizerPreset("Flat", listOf(0f, 0f, 0f, 0f, 0f), 0f),
        EqualizerPreset("Bass Boost", listOf(6f, 4f, 1f, 0f, -1f), 60f),
        EqualizerPreset("Vocal Clarity", listOf(-2f, 0f, 4f, 5f, 2f), 15f),
        EqualizerPreset("Acoustic", listOf(3f, 2f, 1f, 2f, 4f), 20f),
        EqualizerPreset("Electronic", listOf(5f, 3f, 0f, 2f, 5f), 50f),
        EqualizerPreset("Rock", listOf(4f, 2f, -1f, 3f, 4f), 35f)
    )

    private val _equalizerSettings = MutableStateFlow(
        EqualizerSettings(currentPreset = "Flat", bands = listOf(0f, 0f, 0f, 0f, 0f), bassBoost = 15f, surroundSound = 0f, isEnabled = true)
    )
    val equalizerSettings: StateFlow<EqualizerSettings> = _equalizerSettings.asStateFlow()

    init {
        loadHomeData()

        db?.let { database ->
            scope.launch {
                database.favoritesDao().getAllFavorites().collect { entities ->
                    _likedSongs.value = entities.map {
                        Song(
                            id = it.songId,
                            title = it.title,
                            artist = it.artist,
                            album = it.album,
                            durationMs = it.durationMs,
                            coverUrl = it.coverUrl,
                            youtubeVideoId = it.youtubeVideoId,
                            isLiked = true
                        )
                    }
                }
            }

            scope.launch {
                database.historyDao().getRecentHistory().collect { entities ->
                    _recentlyPlayed.value = entities.map {
                        Song(
                            id = it.songId,
                            title = it.title,
                            artist = it.artist,
                            album = it.album,
                            durationMs = it.durationMs,
                            coverUrl = it.coverUrl,
                            youtubeVideoId = it.youtubeVideoId
                        )
                    }
                }
            }
        }
    }

    fun loadHomeData() {
        scope.launch {
            try {
                _isLoadingHome.value = true
                _serviceError.value = null

                val trendingResult = musicProvider.search("trending")
                if (trendingResult.isSuccess) {
                    val songs = trendingResult.getOrThrow().songs
                    if (songs.isNotEmpty()) {
                        _trendingSongs.value = songs
                        _recommendedSongs.value = songs.shuffled()
                    }
                }

                val quickPicksResult = musicProvider.search("Top Hits")
                if (quickPicksResult.isSuccess) {
                    val songs = quickPicksResult.getOrThrow().songs
                    if (songs.isNotEmpty()) {
                        _quickPicks.value = songs
                        _madeForYou.value = songs.shuffled()
                    }
                }

            } catch (e: Exception) {
                Log.e("MusicRepository", "Failed to force load live metadata streams", e)
                _serviceError.value = "Failed to load unblocked cloud shelves."
            } finally {
                _isLoadingHome.value = false
            }
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
            _searchError.value = err.message ?: "Music service is currently unavailable."
            _isSearching.value = false
        }
    }

    suspend fun searchSongs(query: String) = searchMusic(query)

    suspend fun resolveTrackStream(song: Song): Result<AudioSource> {
        if (song.audioUrl.isNotBlank()) {
            return Result.success(
                AudioSource(
                    url = song.audioUrl,
                    userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36",
                    format = "audio/mp4",
                    bitrateKbps = 320,
                    isLocal = false
                )
            )
        }
        return sourceProvider.resolve(song)
    }

    suspend fun getLyricsForSong(song: Song): Result<List<LyricLine>> {
        val result = lyricsProvider.getLyrics(song)
        return result.map { lyrics ->
            lyrics.syncedLyrics.map { LyricLine(timestampMs = it.timeMs, text = it.text) }
        }
    }

    fun toggleLikeSong(song: Song) {
        val currentLiked = _likedSongs.value.toMutableList()
        val index = currentLiked.indexOfFirst { it.id == song.id }
        val isLiked = index >= 0

        if (isLiked) {
            currentLiked.removeAt(index)
            db?.let { database ->
                scope.launch { database.favoritesDao().deleteFavorite(song.id) }
            }
        } else {
            val newLiked = song.copy(isLiked = true)
            currentLiked.add(newLiked)
            db?.let { database ->
                scope.launch {
                    database.favoritesDao().insertFavorite(
                        FavoriteTrackEntity(
                            songId = song.id,
                            title = song.title,
                            artist = song.artist,
                            album = song.album,
                            coverUrl = song.coverUrl ?: "",
                            durationMs = song.durationMs,
                            youtubeVideoId = song.youtubeVideoId
                        )
                    )
                }
            }
        }
        _likedSongs.value = currentLiked
    }

    fun isSongLiked(songId: String): Boolean {
        return _likedSongs.value.any { it.id == songId }
    }

    fun recordRecentlyPlayed(song: Song) {
        val updated = _recentlyPlayed.value.filterNot { it.id == song.id }.toMutableList()
        updated.add(0, song)
        _recentlyPlayed.value = updated.take(25)

        db?.let { database ->
            scope.launch {
                database.historyDao().insertHistory(
                    HistoryTrackEntity(
                        songId = song.id,
                        title = song.title,
                        artist = song.artist,
                        album = song.album,
                        coverUrl = song.coverUrl ?: "",
                        durationMs = song.durationMs,
                        youtubeVideoId = song.youtubeVideoId
                    )
                )
            }
        }
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
        db?.let { database ->
            scope.launch {
                database.playlistDao().insertPlaylist(
                    PlaylistEntity(
                        playlistId = newId,
                        title = title,
                        description = description
                    )
                )
            }
        }
    }

    fun deletePlaylist(playlistId: String) {
        _playlists.value = _playlists.value.filterNot { it.id == playlistId }
        db?.let { database ->
            scope.launch { database.playlistDao().deletePlaylist(playlistId) }
        }
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

    fun signInWithGoogle(name: String, email: String, photoUrl: String? = null) {
        authRepository?.signInWithGoogle("google_token", name, email, photoUrl)
        _googleUser.value = GoogleUser(
            name = name,
            email = email,
            photoUrl = photoUrl,
            isSignedIn = true
        )
    }

    fun signOutGoogle() {
        authRepository?.signOut()
        _googleUser.value = GoogleUser(
            name = "",
            email = "",
            photoUrl = null,
            isSignedIn = false
        )
    }

    fun dismissWelcomeDialog() {
        _isWelcomeDialogVisible.value = false
    }

    fun showWelcomeDialog() {
        _isWelcomeDialogVisible.value = true
    }

    fun startRecognizing() {
        _isRecognizing.value = true
        _recognizedSong.value = null
    }

    fun finishRecognizing(song: Song?) {
        _isRecognizing.value = false
        _recognizedSong.value = song
    }

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
