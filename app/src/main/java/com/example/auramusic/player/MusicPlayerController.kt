package com.example.auramusic.player

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.auramusic.data.cache.AuraSharedBackendCache
import com.example.auramusic.data.provider.OnlineMusicProvider
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
import kotlinx.coroutines.withContext

/**
 * Persistent Global Music Player Controller for AURA MUSIC.
 *
 * Upgraded Architecture:
 * - Uses Media3 / ExoPlayer for persistent background playback
 * - Proper Music-oriented resolution via YouTube Music (Innertube) API
 * - Elimination of WebView-based IFrame player and associated embedding errors
 * - MediaSession integration for Lock-screen and Notification controls
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

    // Player State
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

    // Queue & Modes
    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue: StateFlow<List<Song>> = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(0)
    val queueIndex: StateFlow<Int> = _queueIndex.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle: StateFlow<Boolean> = _isShuffle.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.OFF)
    val repeatMode: StateFlow<RepeatMode> = _repeatMode.asStateFlow()

    // Volume (0.0 to 1.0) & Mute
    private val _volume = MutableStateFlow(1.0f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private var previousVolume: Float = 1.0f

    // Track failed candidates per song
    private val failedCandidates = mutableSetOf<String>()

    private var pendingPlayback: (() -> Unit)? = null
    private var bufferingTimeoutJob: Job? = null
    private val BUFFERING_TIMEOUT_MS = 20000L // 20 seconds timeout for resolution + buffering

    init {
        initializeMediaController()
    }

    private fun initializeMediaController() {
        val sessionToken = SessionToken(context, android.content.ComponentName(context, MusicService::class.java))
        mediaControllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        mediaControllerFuture?.addListener({
            try {
                mediaController = mediaControllerFuture?.get()
                setupPlayerListener()
                startProgressUpdate()
                
                // Execute any playback that was requested while we were connecting
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
                Log.i(TAG, "MEDIA3_PLAYING_CHANGED | VIDEO_ID: ${_currentSong.value?.id} | IS_PLAYING: $isPlaying")
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val stateName = when (playbackState) {
                    Player.STATE_IDLE -> "STATE_IDLE"
                    Player.STATE_BUFFERING -> "STATE_BUFFERING"
                    Player.STATE_READY -> "STATE_READY"
                    Player.STATE_ENDED -> "STATE_ENDED"
                    else -> "UNKNOWN"
                }
                Log.i(TAG, "MEDIA3_STATE_CHANGED | VIDEO_ID: ${_currentSong.value?.id} | STATE: $stateName")

                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        _isBuffering.value = true
                    }
                    Player.STATE_READY -> {
                        _isBuffering.value = false
                        _durationMs.value = mediaController?.duration ?: 0L
                        bufferingTimeoutJob?.cancel()
                        
                        // Explicitly check if it should be playing
                        if (mediaController?.playWhenReady == true) {
                            Log.i(TAG, "MEDIA3_PLAYBACK_START | VIDEO_ID: ${_currentSong.value?.id}")
                        }
                    }
                    Player.STATE_ENDED -> {
                        _isBuffering.value = false
                        bufferingTimeoutJob?.cancel()
                        handlePlaybackComplete()
                    }
                    Player.STATE_IDLE -> {
                        _isBuffering.value = false
                        bufferingTimeoutJob?.cancel()
                    }
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                val errorMsg = when (error.errorCode) {
                    PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED -> "Network Connection Failed"
                    PlaybackException.ERROR_CODE_IO_BAD_HTTP_STATUS -> "Bad HTTP Status (403/404)"
                    PlaybackException.ERROR_CODE_IO_FILE_NOT_FOUND -> "File Not Found"
                    PlaybackException.ERROR_CODE_IO_UNSPECIFIED -> "IO Unspecified (Possibly 403 or Invalid URL)"
                    else -> error.message ?: "Unknown Media3 Error"
                }
                Log.e(TAG, "MEDIA3_ERROR | VIDEO_ID: ${_currentSong.value?.id} | CODE: ${error.errorCode} | MESSAGE: $errorMsg", error)
                handlePlaybackError(error)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Update current song state when transitioning (e.g. next track in queue)
                mediaItem?.mediaId?.let { id ->
                    val song = _queue.value.find { it.id == id }
                    if (song != null && song.id != _currentSong.value?.id) {
                        _currentSong.value = song
                    }
                }
            }
        })
    }

    private fun startProgressUpdate() {
        progressJob?.cancel()
        progressJob = playerScope.launch {
            while (isActive) {
                mediaController?.let {
                    if (it.isPlaying) {
                        _currentPositionMs.value = it.currentPosition
                        _durationMs.value = it.duration
                    }
                }
                delay(500)
            }
        }
    }

    private fun handlePlaybackError(error: PlaybackException) {
        val cur = _currentSong.value ?: return
        val curVid = cur.youtubeVideoId ?: return
        failedCandidates.add(curVid)

        Log.e(TAG, "Playback Error: ${error.message} | VideoId: $curVid")
        _isBuffering.value = true
        
        // Try next candidate
        val nextFromCandidates = cur.candidateVideoIds.firstOrNull { !failedCandidates.contains(it) }
        if (!nextFromCandidates.isNullOrBlank()) {
            Log.i(TAG, "Trying alternative candidate for track '${cur.title}': $nextFromCandidates")
            playWithVideoId(cur.copy(youtubeVideoId = nextFromCandidates))
            return
        }

        // Generic discovery of alternate candidate
        playerScope.launch(Dispatchers.IO) {
            val candidates = OnlineMusicProvider.resolveAlternativeVideoCandidates(
                title = cur.title,
                artist = cur.artist,
                excludeIds = failedCandidates
            )
            withContext(Dispatchers.Main) {
                val altVid = candidates.firstOrNull { !failedCandidates.contains(it) }
                if (!altVid.isNullOrBlank()) {
                    Log.i(TAG, "Discovered alternative candidate via discovery: $altVid")
                    playWithVideoId(cur.copy(
                        youtubeVideoId = altVid,
                        candidateVideoIds = cur.candidateVideoIds + candidates
                    ))
                } else {
                    _isBuffering.value = false
                    _isPlaying.value = false
                    bufferingTimeoutJob?.cancel()
                    _playbackError.value = "Playback unavailable. ${error.message ?: "Source protected or blocked."}"
                }
            }
        }
    }

    /**
     * Legacy method for UI compatibility - no longer needs WebView.
     */
    fun attachYouTubeWebView(webView: android.webkit.WebView) {
        // No-op: Architecture migrated to Media3
    }

    fun playSong(song: Song, newQueue: List<Song> = emptyList()) {
        _playbackError.value = null
        failedCandidates.clear()

        if (newQueue.isNotEmpty()) {
            _queue.value = newQueue
            val index = newQueue.indexOfFirst { it.id == song.id }
            _queueIndex.value = if (index >= 0) index else 0
        } else if (_queue.value.isEmpty()) {
            _queue.value = listOf(song)
            _queueIndex.value = 0
        } else {
            val index = _queue.value.indexOfFirst { it.id == song.id }
            if (index >= 0) {
                _queueIndex.value = index
            } else {
                _queue.value = _queue.value + song
                _queueIndex.value = _queue.value.size - 1
            }
        }

        _currentSong.value = song
        _isBuffering.value = true
        
        playWithVideoId(song)
    }

    private fun playWithVideoId(song: Song) {
        val videoId = song.youtubeVideoId ?: song.id
        
        // Start buffering timeout
        startBufferingTimeout()
        
        playerScope.launch(Dispatchers.IO) {
            // Step 1: Resolve legitimate playable stream URL
            val result = OnlineMusicProvider.resolveStreamUrl(videoId)
            
            withContext(Dispatchers.Main) {
                result.onSuccess { streamUrl ->
                    if (mediaController != null) {
                        prepareAndPlay(song, streamUrl)
                    } else {
                        Log.i(TAG, "MediaController not ready, queuing playback")
                        pendingPlayback = { prepareAndPlay(song, streamUrl) }
                    }
                }.onFailure { error ->
                    Log.e(TAG, "Stream resolution failed for $videoId: ${error.message}")
                    handlePlaybackError(PlaybackException(error.message, error, PlaybackException.ERROR_CODE_IO_UNSPECIFIED))
                }
            }
        }
    }

    private fun startBufferingTimeout() {
        bufferingTimeoutJob?.cancel()
        bufferingTimeoutJob = playerScope.launch {
            delay(BUFFERING_TIMEOUT_MS)
            if (_isBuffering.value) {
                Log.w(TAG, "Buffering timeout reached for ${_currentSong.value?.title}")
                withContext(Dispatchers.Main) {
                    if (_isBuffering.value) {
                        _playbackError.value = "Playback timed out (Slow Connection or Protected Source)"
                        _isBuffering.value = false
                        mediaController?.stop()
                    }
                }
            }
        }
    }

    private fun prepareAndPlay(song: Song, streamUrl: String) {
        val controller = mediaController ?: return
        
        Log.i(TAG, "MEDIA_SOURCE_CREATED | VIDEO_ID: ${song.id} | URL_LENGTH: ${streamUrl.length}")

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(song.title)
            .setArtist(song.artist)
            .setAlbumTitle(song.album)
            .setArtworkUri(android.net.Uri.parse(song.coverUrl))
            .build()

        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id)
            .setUri(streamUrl)
            .setMediaMetadata(mediaMetadata)
            .build()

        controller.setMediaItem(mediaItem)
        controller.prepare()
        controller.play()
        
        _isPlaying.value = true
        _playbackError.value = null
    }

    fun togglePlayPause() {
        val controller = mediaController ?: return
        if (controller.isPlaying) {
            controller.pause()
        } else {
            controller.play()
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
        _currentPositionMs.value = positionMs
    }

    fun next() {
        val q = _queue.value
        if (q.isEmpty()) return

        val nextIndex = if (_isShuffle.value && q.size > 1) {
            (q.indices).filter { it != _queueIndex.value }.random()
        } else {
            (_queueIndex.value + 1) % q.size
        }

        _queueIndex.value = nextIndex
        playSong(q[nextIndex], q)
    }

    fun previous() {
        val q = _queue.value
        if (q.isEmpty()) return

        if (_currentPositionMs.value > 3000L) {
            seekTo(0L)
            return
        }

        val prevIndex = if (_queueIndex.value - 1 < 0) q.size - 1 else _queueIndex.value - 1
        _queueIndex.value = prevIndex
        playSong(q[prevIndex], q)
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

    fun setRepeatMode(mode: RepeatMode) {
        _repeatMode.value = mode
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
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
        if (_isMuted.value) {
            _isMuted.value = false
            setVolume(if (previousVolume > 0f) previousVolume else 0.8f)
        } else {
            previousVolume = _volume.value
            _isMuted.value = true
            mediaController?.volume = 0f
        }
    }

    fun addToQueue(song: Song) {
        _queue.value = _queue.value + song
    }

    fun removeFromQueue(index: Int) {
        val currentQueue = _queue.value.toMutableList()
        if (index in currentQueue.indices) {
            currentQueue.removeAt(index)
            _queue.value = currentQueue
            if (index < _queueIndex.value) {
                _queueIndex.value -= 1
            } else if (index == _queueIndex.value && currentQueue.isNotEmpty()) {
                val nextIdx = index.coerceAtMost(currentQueue.size - 1)
                _queueIndex.value = nextIdx
                playSong(currentQueue[nextIdx], currentQueue)
            }
        }
    }

    fun moveQueueItem(from: Int, to: Int) {
        val currentQueue = _queue.value.toMutableList()
        if (from in currentQueue.indices && to in currentQueue.indices) {
            val item = currentQueue.removeAt(from)
            currentQueue.add(to, item)
            _queue.value = currentQueue
            if (_queueIndex.value == from) {
                _queueIndex.value = to
            }
        }
    }

    fun clearQueue() {
        _queue.value = emptyList()
        _queueIndex.value = 0
        pause()
        _currentSong.value = null
        _currentPositionMs.value = 0L
    }

    fun release() {
        progressJob?.cancel()
        mediaController?.release()
        mediaControllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
