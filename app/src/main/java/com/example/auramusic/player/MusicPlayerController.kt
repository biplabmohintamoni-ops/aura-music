package com.example.auramusic.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.auramusic.model.RepeatMode
import com.example.auramusic.model.Song
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Pure Ad-Free High-Performance Music Player Controller for AURA MUSIC.
 * Fully optimized to stream direct high-quality JioSaavn CDN audio sources seamlessly.
 */
class MusicPlayerController(private val context: Context) {

    companion object {
        private const val TAG = "MusicPlayerController"
    }

    private val playerScope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    // Media3 State
    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    // Player Flows
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong: StateFlow<Song?> = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isBuffering = MutableStateFlow(false)
    val isBuffering: StateFlow<Boolean> = _isBuffering.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(0)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    // Volume State
    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private var pendingPlayback: (() -> Unit)? = null

    init {
        flushPlayerCache()
        initializeMediaController()
    }

    fun flushPlayerCache() {
        _queue.value = emptyList()
        _queueIndex.value = 0
        _currentSong.value = null
        _currentPositionMs.value = 0L
        _durationMs.value = 0L
        _playbackError.value = null
        mediaController?.let {
            it.stop()
            it.clearMediaItems()
        }
    }

    private fun initializeMediaController() {
        val sessionToken = SessionToken(context, android.content.ComponentName(context, MusicService::class.java))
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture?.addListener({
            try {
                mediaController = mediaControllerFuture?.get()
                setupPlayerListener()
                startProgressUpdate()
                
                pendingPlayback?.invoke()
                pendingPlayback = null
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize MediaController", e)
                _isBuffering.value = false
                _playbackError.value = "Player initialization failed."
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setupPlayerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> _isBuffering.value = true
                    Player.STATE_READY -> {
                        _isBuffering.value = false
                        _durationMs.value = mediaController?.duration ?: 0L
                        _playbackError.value = null
                    }
                    Player.STATE_ENDED -> {
                        _isBuffering.value = false
                        handlePlaybackComplete()
                    }
                    Player.STATE_IDLE -> _isBuffering.value = false
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e(TAG, "ExoPlayer Engine Error: ${error.message}", error)
                _playbackError.value = "Playback error: Track stream could not be loaded."
                _isBuffering.value = false
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                mediaItem?.mediaId?.let { id ->
                    val song = _queue.value.find { it.id == id }
                    if (song != null) {
                        _currentSong.value = song
                        _queueIndex.value = _queue.value.indexOf(song)
                    }
                }
            }
        })
    }

    private fun createMediaItem(song: Song): MediaItem {
        val streamUrl = song.audioUrl.ifBlank { song.url }
        val coverUri = if (!song.coverUrl.isNullOrBlank()) Uri.parse(song.coverUrl) else Uri.EMPTY

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(coverUri)
            .build()

        val builder = MediaItem.Builder()
            .setMediaId(song.id)
            .setMediaMetadata(mediaMetadata)

        if (streamUrl.isNotBlank()) {
            builder.setUri(Uri.parse(streamUrl))
        }

        return builder.build()
    }

    /**
     * Instantly plays a clean audio stream link with full explicit metadata.
     */
    fun playSong(song: Song, queueList: List<Song> = emptyList()) {
        val controller = mediaController
        if (controller == null) {
            pendingPlayback = { playSong(song, queueList) }
            return
        }

        playerScope.launch {
            try {
                _isBuffering.value = true
                _playbackError.value = null

                var resolvedSong = song
                var directAudioUrl = song.audioUrl.ifBlank { song.url }

                if (directAudioUrl.isBlank()) {
                    Log.i(TAG, "Resolving stream for '${song.title}' (${song.id})")
                    val videoId = song.youtubeVideoId?.takeIf { it.isNotBlank() }
                        ?: song.id.takeIf { it.isNotBlank() }
                        ?: "${song.title} ${song.artist}"
                    val streamResult = com.example.auramusic.engine.PipedExtractionService.extractAudioStream(videoId)
                    if (streamResult.isSuccess) {
                        directAudioUrl = streamResult.getOrThrow().url
                        resolvedSong = song.copy(audioUrl = directAudioUrl)
                    } else {
                        // Fallback search by title
                        val searchFallback = com.example.auramusic.engine.PipedExtractionService.extractAudioStream(song.title)
                        if (searchFallback.isSuccess) {
                            directAudioUrl = searchFallback.getOrThrow().url
                            resolvedSong = song.copy(audioUrl = directAudioUrl)
                        } else {
                            throw Exception("Audio stream could not be resolved for '${song.title}'")
                        }
                    }
                }

                _currentSong.value = resolvedSong

                val currentQueue = if (queueList.isNotEmpty()) {
                    queueList
                } else if (_queue.value.isNotEmpty() && _queue.value.any { it.id == resolvedSong.id }) {
                    _queue.value
                } else {
                    listOf(resolvedSong)
                }

                val updatedQueue = currentQueue.map { if (it.id == resolvedSong.id) resolvedSong else it }
                _queue.value = updatedQueue
                val targetIndex = updatedQueue.indexOfFirst { it.id == resolvedSong.id }.coerceAtLeast(0)
                _queueIndex.value = targetIndex

                val mediaItems = updatedQueue.map { createMediaItem(it) }
                controller.setMediaItems(mediaItems, targetIndex, 0L)
                controller.prepare()
                controller.play()
                
            } catch (e: Exception) {
                Log.e(TAG, "Playback layout build crashed", e)
                _playbackError.value = "Failed to load audio stream payload."
                _isBuffering.value = false
            }
        }
    }

    fun togglePlayPause() {
        mediaController?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun pause() {
        mediaController?.pause()
    }

    fun resume() {
        mediaController?.play()
    }

    fun seekTo(positionMs: Long) {
        mediaController?.seekTo(positionMs)
    }

    fun next() {
        val q = _queue.value
        if (q.isEmpty()) return
        val nextIndex = if (_isShuffle.value && q.size > 1) {
            q.indices.filter { it != _queueIndex.value }.random()
        } else {
            (_queueIndex.value + 1) % q.size
        }
        val nextSong = q[nextIndex]
        playSong(nextSong, q)
    }

    fun previous() {
        val q = _queue.value
        if (q.isEmpty()) return
        if (_currentPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }
        val prevIndex = if (_queueIndex.value - 1 < 0) q.size - 1 else _queueIndex.value - 1
        val prevSong = q[prevIndex]
        playSong(prevSong, q)
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0.0f, 1.0f)
        _volume.value = clamped
        if (clamped > 0.0f) {
            _isMuted.value = false
        }
        mediaController?.volume = if (_isMuted.value) 0f else clamped
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        mediaController?.volume = if (_isMuted.value) 0f else _volume.value
    }

    fun release() {
        progressJob?.cancel()
        mediaController?.release()
        mediaControllerFuture?.let { MediaController.releaseFuture(it) }
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = playerScope.launch {
            while (isActive) {
                mediaController?.let {
                    if (it.isPlaying) {
                        _currentPositionMs.value = it.currentPosition
                        _durationMs.value = it.duration.coerceAtLeast(0L)
                    }
                }
                delay(500)
            }
        }
    }

    private fun handlePlaybackComplete() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                mediaController?.seekTo(0)
                mediaController?.play()
            }
            RepeatMode.ALL -> {
                next()
            }
            RepeatMode.OFF -> {
                if (_queueIndex.value < _queue.value.size - 1) {
                    next()
                } else {
                    _isPlaying.value = false
                    _currentPositionMs.value = 0L
                }
            }
        }
    }
}
