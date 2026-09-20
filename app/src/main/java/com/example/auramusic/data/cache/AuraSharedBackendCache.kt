package com.example.auramusic.data.cache

import com.example.auramusic.model.Song
import kotlinx.coroutines.CompletableDeferred
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * Shared Backend Cache for AURA MUSIC.
 *
 * Implements:
 * 1. Query normalization ("Kesariya", "kesariya", "KESARIYA", " Kesariya " -> "kesariya")
 * 2. Shared Search Cache across all users and simulated sessions
 * 3. Video Metadata caching (title, channel, duration, candidates)
 * 4. Popular music / homepage caching to eliminate redundant YouTube API requests
 * 5. Concurrent query deduplication (in-flight request sharing)
 * 6. Internal YouTube quota protection and tracking
 * 7. Candidate list storage for embedding restriction resolution
 */
object AuraSharedBackendCache {

    private const val SEARCH_CACHE_TTL_MS = 24 * 60 * 60 * 1000L // 24 hours
    private const val METADATA_CACHE_TTL_MS = 7 * 24 * 60 * 60 * 1000L // 7 days

    data class CachedSearchResult(
        val normalizedQuery: String,
        val songs: List<Song>,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        val isValid: Boolean
            get() = System.currentTimeMillis() - timestamp < SEARCH_CACHE_TTL_MS
    }

    data class CachedVideoMetadata(
        val videoId: String,
        val title: String,
        val artist: String,
        val durationMs: Long,
        val coverUrl: String?,
        val candidateVideoIds: List<String>,
        val timestamp: Long = System.currentTimeMillis()
    ) {
        val isValid: Boolean
            get() = System.currentTimeMillis() - timestamp < METADATA_CACHE_TTL_MS
    }

    // Shared Search Queries Cache
    private val searchCache = ConcurrentHashMap<String, CachedSearchResult>()

    // Shared Video Metadata Cache
    private val videoMetadataCache = ConcurrentHashMap<String, CachedVideoMetadata>()

    // Popular / Curated catalog cache
    private val popularCatalog = mutableListOf<Song>()
    private var popularCatalogTimestamp: Long = 0L

    // In-flight deduplication
    private val inFlightRequests = ConcurrentHashMap<String, CompletableDeferred<List<Song>>>()

    // Internal quota tracking (YouTube search = 100 units per request, free tier = 10,000 units/day)
    private val dailyQuotaUnitsUsed = AtomicInteger(0)
    private val isQuotaExceeded = AtomicBoolean(false)
    private var quotaResetTimeMs: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)

    init {
        initializeCuratedPopularCache()
    }

    /**
     * Normalizes a search query string.
     * - Trims leading/trailing whitespace
     * - Converts to lowercase
     * - Collapses internal whitespace into single space
     * - Strips redundant suffix noise like "official audio", "official video", "song"
     */
    fun normalizeQuery(query: String): String {
        var norm = query.trim().lowercase().replace(Regex("\\s+"), " ")
        norm = norm.replace(Regex("""\s+(official\s+video|official\s+audio|audio\s+song|full\s+song|lyric\s+video|song)$"""), "")
        return norm.trim()
    }

    /**
     * Retrieves search results from the shared cache if valid.
     */
    fun getCachedSearch(rawQuery: String): List<Song>? {
        val key = normalizeQuery(rawQuery)
        if (key.isBlank()) return null

        val cached = searchCache[key]
        return if (cached != null && cached.isValid) {
            cached.songs
        } else {
            searchCache.remove(key)
            null
        }
    }

    /**
     * Saves search results into the shared cache and populates video metadata.
     */
    fun putCachedSearch(rawQuery: String, songs: List<Song>) {
        val key = normalizeQuery(rawQuery)
        if (key.isBlank() || songs.isEmpty()) return

        searchCache[key] = CachedSearchResult(
            normalizedQuery = key,
            songs = songs
        )

        // Cache metadata and candidates for each song
        songs.forEach { song ->
            val vid = song.youtubeVideoId
            if (!vid.isNullOrBlank()) {
                videoMetadataCache[vid] = CachedVideoMetadata(
                    videoId = vid,
                    title = song.title,
                    artist = song.artist,
                    durationMs = song.durationMs,
                    coverUrl = song.coverUrl,
                    candidateVideoIds = song.candidateVideoIds
                )
            }
        }
    }

    /**
     * Retrieves cached metadata for a specific video ID.
     */
    fun getCachedMetadata(videoId: String): CachedVideoMetadata? {
        val cached = videoMetadataCache[videoId]
        return if (cached != null && cached.isValid) cached else null
    }

    /**
     * Stores cached metadata for a video ID.
     */
    fun putCachedMetadata(metadata: CachedVideoMetadata) {
        videoMetadataCache[metadata.videoId] = metadata
    }

    /**
     * Popular and curated catalog cache.
     */
    fun getPopularCatalog(): List<Song> {
        synchronized(popularCatalog) {
            return popularCatalog.toList()
        }
    }

    fun setPopularCatalog(songs: List<Song>) {
        if (songs.isEmpty()) return
        synchronized(popularCatalog) {
            popularCatalog.clear()
            popularCatalog.addAll(songs)
            popularCatalogTimestamp = System.currentTimeMillis()
        }
        // Also cache all popular songs in metadata cache
        songs.forEach { song ->
            val vid = song.youtubeVideoId
            if (!vid.isNullOrBlank()) {
                videoMetadataCache[vid] = CachedVideoMetadata(
                    videoId = vid,
                    title = song.title,
                    artist = song.artist,
                    durationMs = song.durationMs,
                    coverUrl = song.coverUrl,
                    candidateVideoIds = song.candidateVideoIds
                )
            }
        }
    }

    /**
     * In-flight query deduplication.
     */
    fun getInFlight(rawQuery: String): CompletableDeferred<List<Song>>? {
        val key = normalizeQuery(rawQuery)
        return inFlightRequests[key]
    }

    fun putInFlight(rawQuery: String, deferred: CompletableDeferred<List<Song>>) {
        val key = normalizeQuery(rawQuery)
        inFlightRequests[key] = deferred
    }

    fun removeInFlight(rawQuery: String) {
        val key = normalizeQuery(rawQuery)
        inFlightRequests.remove(key)
    }

    /**
     * Quota tracking methods.
     */
    fun recordQuotaUsed(units: Int = 100) {
        checkQuotaReset()
        dailyQuotaUnitsUsed.addAndGet(units)
    }

    fun markQuotaExceeded() {
        isQuotaExceeded.set(true)
        quotaResetTimeMs = System.currentTimeMillis() + (6 * 60 * 60 * 1000L) // 6h backoff
    }

    fun isQuotaAvailable(): Boolean {
        checkQuotaReset()
        return !isQuotaExceeded.get()
    }

    fun getQuotaUnitsUsed(): Int {
        checkQuotaReset()
        return dailyQuotaUnitsUsed.get()
    }

    private fun checkQuotaReset() {
        val now = System.currentTimeMillis()
        if (now > quotaResetTimeMs) {
            dailyQuotaUnitsUsed.set(0)
            isQuotaExceeded.set(false)
            quotaResetTimeMs = now + (24 * 60 * 60 * 1000L)
        }
    }

    /**
     * Pre-populates the shared popular music catalog.
     * REMOVED: Hardcoded demo tracks are no longer used to ensure real API integration.
     */
    private fun initializeCuratedPopularCache() {
        // No hardcoded songs. The popular catalog will be populated by real API results.
    }
}
