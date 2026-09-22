package com.example.auramusic.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auramusic.ui.components.AuraBottomNav
import com.example.auramusic.ui.components.AuraNavDestination
import com.example.auramusic.ui.components.GoogleAccountDialog
import com.example.auramusic.ui.components.MiniPlayer
import com.example.auramusic.ui.components.QuickMenuSheet
import com.example.auramusic.ui.components.SongContextMenuSheet
import com.example.auramusic.ui.components.WelcomeDialog
import com.example.auramusic.ui.screens.EqualizerScreen
import com.example.auramusic.ui.screens.HomeScreen
import com.example.auramusic.ui.screens.LibraryScreen
import com.example.auramusic.ui.screens.MusicRecognitionScreen
import com.example.auramusic.ui.screens.NowPlayingSheet
import com.example.auramusic.ui.screens.LyricsSheet
import com.example.auramusic.ui.screens.SearchScreen
import com.example.auramusic.ui.screens.SettingsDialog
import com.example.auramusic.ui.theme.LocalAuraTheme
import com.example.auramusic.ui.viewmodel.MusicViewModel

@Composable
fun AuraApp(
    viewModel: MusicViewModel = viewModel()
) {
    val theme = LocalAuraTheme.current
    var currentDestination by remember { mutableStateOf(AuraNavDestination.HOME) }
    var isProfileOpen by remember { mutableStateOf(false) }

    val currentSong by viewModel.currentSong.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val isBuffering by viewModel.isBuffering.collectAsState()
    val currentPositionMs by viewModel.currentPositionMs.collectAsState()
    val durationMs by viewModel.durationMs.collectAsState()
    val playbackError by viewModel.playbackError.collectAsState()
    val isShuffle by viewModel.isShuffle.collectAsState()
    val repeatMode by viewModel.repeatMode.collectAsState()

    val recommendedSongs by viewModel.recommendedSongs.collectAsState()
    val trendingSongs by viewModel.trendingSongs.collectAsState()
    val quickPicks by viewModel.quickPicks.collectAsState()
    val selectedHomeCategory by viewModel.selectedHomeCategory.collectAsState()
    val isLoadingHome by viewModel.isLoadingHome.collectAsState()

    val searchResults by viewModel.searchResults.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchError by viewModel.searchError.collectAsState()

    val likedSongs by viewModel.likedSongs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val googleUser by viewModel.googleUser.collectAsState()
    val isWelcomeVisible by viewModel.isWelcomeDialogVisible.collectAsState()

    val isNowPlayingExpanded by viewModel.isNowPlayingExpanded.collectAsState()
    val isLyricsExpanded by viewModel.isLyricsExpanded.collectAsState()
    val isRecognizeOpen by viewModel.isRecognizeScreenOpen.collectAsState()
    val isEqualizerOpen by viewModel.isEqualizerOpen.collectAsState()
    val isSettingsOpen by viewModel.isSettingsOpen.collectAsState()
    val isQuickMenuOpen by viewModel.isQuickMenuOpen.collectAsState()
    val songForContextMenu by viewModel.songForContextMenu.collectAsState()
    val sleepTimerRemaining by viewModel.sleepTimerMinutesRemaining.collectAsState()
    val activeLyrics by viewModel.activeSongLyrics.collectAsState()
    val isLoadingLyrics by viewModel.isLoadingLyrics.collectAsState()
    val equalizerSettings by viewModel.equalizerSettings.collectAsState()
    val isRecognizing by viewModel.isRecognizing.collectAsState()
    val recognizedSong by viewModel.recognizedSong.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.background)
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            containerColor = Color.Transparent,
            bottomBar = {
                if (!isNowPlayingExpanded && !isLyricsExpanded && !isRecognizeOpen && !isEqualizerOpen) {
                    Column(
                        modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Docked MiniPlayer directly above the bottom capsule nav
                        if (currentSong != null) {
                            MiniPlayer(
                                song = currentSong!!,
                                isPlaying = isPlaying,
                                isBuffering = isBuffering,
                                currentPositionMs = currentPositionMs,
                                durationMs = durationMs,
                                onTogglePlayPause = { viewModel.togglePlayPause() },
                                onNext = { viewModel.next() },
                                onPrevious = { viewModel.previous() },
                                onClick = { viewModel.setNowPlayingExpanded(true) }
                            )
                        }

                        // Floating Capsule Bottom Navigation
                        AuraBottomNav(
                            currentDestination = currentDestination,
                            onNavigate = { currentDestination = it },
                            onOpenQuickMenu = { viewModel.setQuickMenuOpen(true) }
                        )
                    }
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when (currentDestination) {
                    AuraNavDestination.HOME -> {
                        HomeScreen(
                            recommendedSongs = recommendedSongs,
                            trendingSongs = trendingSongs,
                            quickPicks = quickPicks,
                            categories = viewModel.homeCategories,
                            selectedCategory = selectedHomeCategory,
                            isLoading = isLoadingHome,
                            googleUser = googleUser,
                            onSelectCategory = { viewModel.selectHomeCategory(it) },
                            onPlaySong = { song, queue -> viewModel.playSong(song, queue) },
                            onOpenEqualizer = { viewModel.setEqualizerOpen(true) },
                            onOpenSettings = { viewModel.setSettingsOpen(true) },
                            onOpenHistory = { /* History */ },
                            onOpenProfile = { isProfileOpen = true },
                            onSongMenu = { viewModel.openSongContextMenu(it) }
                        )
                    }
                    AuraNavDestination.SEARCH -> {
                        SearchScreen(
                            searchQuery = searchQuery,
                            searchResults = searchResults,
                            isSearching = isSearching,
                            searchError = searchError,
                            moodsAndMoments = viewModel.moodsAndMoments,
                            genres = viewModel.genres,
                            onQueryChange = { viewModel.updateSearchQuery(it) },
                            onPlaySong = { song, queue -> viewModel.playSong(song, queue) },
                            onSongMenu = { viewModel.openSongContextMenu(it) },
                            onRetry = { viewModel.updateSearchQuery(searchQuery) }
                        )
                    }
                    AuraNavDestination.LIBRARY -> {
                        LibraryScreen(
                            playlists = playlists,
                            likedSongs = likedSongs,
                            googleUser = googleUser,
                            onOpenLikedSongs = {
                                if (likedSongs.isNotEmpty()) {
                                    viewModel.playSong(likedSongs.first(), likedSongs)
                                }
                            },
                            onSelectPlaylist = { pl ->
                                val playlistSongs = recommendedSongs.filter { pl.songIds.contains(it.id) }
                                if (playlistSongs.isNotEmpty()) {
                                    viewModel.playSong(playlistSongs.first(), playlistSongs)
                                }
                            },
                            onOpenSettings = { viewModel.setSettingsOpen(true) },
                            onOpenProfile = { isProfileOpen = true },
                            onOpenHistory = { /* History */ },
                            onCreatePlaylist = { viewModel.repository.createPlaylist("My New Playlist") }
                        )
                    }
                }
            }
        }

        // Full Screen Overlays & Modals

        // Now Playing Sheet
        AnimatedVisibility(
            visible = isNowPlayingExpanded && currentSong != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            currentSong?.let { song ->
                NowPlayingSheet(
                    song = song,
                    isPlaying = isPlaying,
                    isBuffering = isBuffering,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    playbackError = playbackError,
                    isShuffle = isShuffle,
                    repeatMode = repeatMode,
                    isLiked = viewModel.isSongLiked(song.id),
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onNext = { viewModel.next() },
                    onPrevious = { viewModel.previous() },
                    onSeekTo = { viewModel.seekTo(it) },
                    onToggleShuffle = { viewModel.toggleShuffle() },
                    onCycleRepeat = { viewModel.cycleRepeatMode() },
                    onToggleLike = { viewModel.toggleLikeSong(song) },
                    onOpenLyrics = { viewModel.setLyricsExpanded(true) },
                    onOpenEqualizer = { viewModel.setEqualizerOpen(true) },
                    onOpenSleepTimer = { viewModel.setSettingsOpen(true) },
                    onSongMenu = { viewModel.openSongContextMenu(song) },
                    onCollapse = { viewModel.setNowPlayingExpanded(false) }
                )
            }
        }

        // Synchronized Lyrics Sheet
        AnimatedVisibility(
            visible = isLyricsExpanded && currentSong != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            currentSong?.let { song ->
                LyricsSheet(
                    song = song,
                    lyrics = activeLyrics,
                    isLoadingLyrics = isLoadingLyrics,
                    currentPositionMs = currentPositionMs,
                    durationMs = durationMs,
                    onSeekTo = { viewModel.seekTo(it) },
                    onClose = { viewModel.setLyricsExpanded(false) }
                )
            }
        }

        // Music Recognition Screen (Echo Find)
        AnimatedVisibility(
            visible = isRecognizeOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            MusicRecognitionScreen(
                isRecognizing = isRecognizing,
                recognizedSong = recognizedSong,
                onStartRecognize = { viewModel.startRecognition() },
                onPlayRecognizedSong = {
                    viewModel.playSong(it)
                    viewModel.setRecognizeScreenOpen(false)
                    viewModel.setNowPlayingExpanded(true)
                },
                onClose = { viewModel.setRecognizeScreenOpen(false) }
            )
        }

        // Equalizer Screen
        AnimatedVisibility(
            visible = isEqualizerOpen,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            EqualizerScreen(
                settings = equalizerSettings,
                presets = viewModel.equalizerPresets,
                onSelectPreset = { viewModel.setEqualizerPreset(it) },
                onBandChange = { index, gain -> viewModel.updateEqualizerBand(index, gain) },
                onBassBoostChange = { viewModel.updateBassBoost(it) },
                onSurroundChange = { viewModel.updateSurroundSound(it) },
                onToggleEnabled = { viewModel.toggleEqualizer(it) },
                onClose = { viewModel.setEqualizerOpen(false) }
            )
        }

        // Settings Dialog / Screen
        if (isSettingsOpen) {
            SettingsDialog(
                viewModel = viewModel,
                sleepTimerRemaining = sleepTimerRemaining,
                onSetSleepTimer = { viewModel.startSleepTimer(it) },
                onCancelSleepTimer = { viewModel.cancelSleepTimer() },
                onShowWelcome = { viewModel.showWelcomeDialog() },
                onDismiss = { viewModel.setSettingsOpen(false) }
            )
        }

        // Welcome Dialog (First Launch)
        if (isWelcomeVisible) {
            WelcomeDialog(
                onDismiss = { viewModel.dismissWelcomeDialog() }
            )
        }

        // Quick Menu Sheet
        if (isQuickMenuOpen) {
            QuickMenuSheet(
                onOpenRecognize = { viewModel.setRecognizeScreenOpen(true) },
                onOpenEqualizer = { viewModel.setEqualizerOpen(true) },
                onOpenSleepTimer = { viewModel.setSettingsOpen(true) },
                onOpenProfile = { isProfileOpen = true },
                onOpenSettings = { viewModel.setSettingsOpen(true) },
                onDismiss = { viewModel.setQuickMenuOpen(false) }
            )
        }

        // Song Context Menu Sheet
        songForContextMenu?.let { song ->
            SongContextMenuSheet(
                song = song,
                isLiked = viewModel.isSongLiked(song.id),
                onPlay = { viewModel.playSong(song) },
                onPlayNext = { /* Next in queue */ },
                onAddToQueue = { /* Add to queue */ },
                onToggleLike = { viewModel.toggleLikeSong(song) },
                onViewLyrics = {
                    viewModel.playSong(song)
                    viewModel.setLyricsExpanded(true)
                },
                onDismiss = { viewModel.closeSongContextMenu() }
            )
        }

        // Google Account Dialog
        if (isProfileOpen) {
            GoogleAccountDialog(
                googleUser = googleUser,
                onSignIn = {
                    viewModel.signInWithGoogle(
                        name = "Biplab Mohinta",
                        email = "biplabmohintamoni@gmail.com",
                        photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150&auto=format&fit=crop&q=80"
                    )
                },
                onSignOut = { viewModel.signOutGoogle() },
                onDismiss = { isProfileOpen = false }
            )
        }
    }
}
