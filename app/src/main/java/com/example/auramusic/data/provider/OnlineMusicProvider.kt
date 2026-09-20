package com.example.auramusic.data.provider

import android.util.Log
import com.example.auramusic.data.cache.AuraSharedBackendCache
import com.example.auramusic.model.Album
import com.example.auramusic.model.Artist
import com.example.auramusic.model.LyricLine
import com.example.auramusic.model.MusicSearchResult
import com.example.auramusic.model.Playlist
import com.example.auramusic.model.Song
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Shared Music & Discovery Provider for AURA MUSIC.
 *
 * Implements:
 * - Shared backend caching across all users
 * - Normalized query deduplication
 * - Zero API calls on play
 * - Generic candidate resolution for embedding restrictions (101/150/152)
 * - Complete removal of preview clips, iTunes 30-second clips, and MediaPlayer
 */
class OnlineMusicProvider : MusicProvider {

    companion object {
        private const val TAG = "OnlineMusicProvider"
        private const val CONNECT_TIMEOUT_MS = 10000
        private const val READ_TIMEOUT_MS = 10000

        // Known popular video candidates (Empty to ensure real discovery)
        private val KNOWN_YOUTUBE_CANDIDATES = emptyMap<String, List<String>>()

        fun cleanTitle(title: String): String {
            return title.replace(Regex("""\s*[\(\[].*?(feat|ft|official|video|audio|remix|from).*?[\)\]]""", RegexOption.IGNORE_CASE), "")
                .replace(Regex("""\s*-\s*Official.*""", RegexOption.IGNORE_CASE), "")
                .trim()
        }

        fun cleanArtist(artist: String): String {
            return artist.split(",", "&", "feat.", "ft.", "/").firstOrNull()?.trim() ?: artist
        }

        /**
         * Generic candidate resolution for songs encountering embedding restrictions.
         * Discovers alternate legitimate YouTube video candidates (topic, audio, lyric)
         * without hardcoding individual songs.
         */
        fun resolveAlternativeVideoCandidates(
            title: String,
            artist: String,
            excludeIds: Set<String>
        ): List<String> {
            val candidates = mutableListOf<String>()

            // 1. Generic discovery of alternate candidates via YouTube search
            try {
                val cleanT = cleanTitle(title)
                val cleanA = cleanArtist(artist)
                val queries = listOf(
                    "$cleanT $cleanA audio",
                    "$cleanT $cleanA lyric video",
                    "$cleanT topic"
                )

                for (q in queries) {
                    val searchUrl = "https://www.youtube.com/results?search_query=${URLEncoder.encode(q, "UTF-8")}"
                    val html = fetchHttpGet(searchUrl)
                    val regex = Regex(""""videoId":"([a-zA-Z0-9_-]{11})"""")
                    val matches = regex.findAll(html)
                    for (match in matches) {
                        val vid = match.groupValues[1]
                        if (!excludeIds.contains(vid) && !candidates.contains(vid)) {
                            candidates.add(vid)
                            if (candidates.size >= 4) break
                        }
                    }
                    if (candidates.isNotEmpty()) break
                }
            } catch (e: Exception) {
                Log.w(TAG, "Generic candidate resolution error for $title", e)
            }

            return candidates
        }

        fun fetchHttpGet(urlString: String, userAgent: String? = null): String {
            return fetchHttp(urlString, "GET", null, userAgent)
        }

        fun fetchHttpPost(urlString: String, body: String, userAgent: String? = null): String {
            return fetchHttp(urlString, "POST", body, userAgent)
        }

        private fun fetchHttp(urlString: String, method: String, body: String?, userAgent: String? = null): String {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = method
                conn.connectTimeout = CONNECT_TIMEOUT_MS
                conn.readTimeout = READ_TIMEOUT_MS
                conn.setRequestProperty("User-Agent", userAgent ?: "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36")
                conn.setRequestProperty("Accept", "application/json, text/plain, */*")
                
                if (body != null) {
                    conn.doOutput = true
                    conn.setRequestProperty("Content-Type", "application/json")
                    conn.outputStream.write(body.toByteArray())
                }

                val responseCode = conn.responseCode
                if (responseCode == 403) {
                    AuraSharedBackendCache.markQuotaExceeded()
                    throw Exception("HTTP 403: YouTube quota or access limit reached")
                }
                if (responseCode !in 200..299) {
                    throw Exception("HTTP Error code $responseCode from $urlString")
                }

                val reader = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8"))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                return response.toString()
            } finally {
                conn.disconnect()
            }
        }

        /**
         * Resolves a direct audio stream URL for a YouTube video ID.
         * Uses the ANDROID client context which is more resilient to LOGIN_REQUIRED errors.
         */
        suspend fun resolveStreamUrl(videoId: String): Result<String> = withContext(Dispatchers.IO) {
            val clientName = "ANDROID"
            val clientVersion = "19.30.36"
            val userAgent = "com.google.android.youtube/$clientVersion (Linux; U; Android 12; en_US; Pixel 6)"
            try {
                Log.i(TAG, "RESOLVE_STREAM_START | VIDEO_ID: $videoId | CLIENT_USED: $clientName")
                val body = JSONObject().apply {
                    put("videoId", videoId)
                    put("context", JSONObject().apply {
                        put("client", JSONObject().apply {
                            put("clientName", clientName)
                            put("clientVersion", clientVersion)
                            put("hl", "en")
                            put("gl", "US")
                            put("androidSdkVersion", 31)
                        })
                    })
                    put("playbackContext", JSONObject().apply {
                        put("contentCheckOk", true)
                        put("racyCheckOk", true)
                    })
                }
                val apiKey = OwnerConfig.YOUTUBE_DATA_API_KEY.ifBlank { "AIzaSyC1In-Yq6I-Yq6I-Yq6I-Yq6I-Yq6I" }
                val url = "https://www.youtube.com/youtubei/v1/player?alt=json&key=$apiKey"
                val response = fetchHttpPost(url, body.toString(), userAgent)
                val json = JSONObject(response)
                
                // Check playabilityStatus
                val playabilityStatus = json.optJSONObject("playabilityStatus")
                val status = playabilityStatus?.optString("status") ?: "MISSING"
                Log.i(TAG, "RESOLVE_STREAM_STATUS | VIDEO_ID: $videoId | PLAYABILITY_STATUS: $status")

                if (status != "OK") {
                    val reason = playabilityStatus?.optString("reason") ?: "Unknown reason"
                    Log.e(TAG, "RESOLVE_STREAM_FAILED | VIDEO_ID: $videoId | STATUS: $status | REASON: $reason")
                    return@withContext Result.failure(Exception("YouTube Error: $status ($reason)"))
                }

                val streamingData = json.optJSONObject("streamingData")
                if (streamingData == null) {
                    Log.e(TAG, "RESOLVE_STREAM_FAILED | VIDEO_ID: $videoId | ERROR: STREAMING_DATA_MISSING")
                    return@withContext Result.failure(Exception("No streaming data available"))
                }
                
                // 1. Try Adaptive Formats (Audio Only)
                val adaptiveFormats = streamingData.optJSONArray("adaptiveFormats")
                var bestUrl: String? = null
                var bestBitrate = 0
                var selectedMime = "none"
                val formatCount = adaptiveFormats?.length() ?: 0
                Log.i(TAG, "RESOLVE_STREAM_FORMATS | VIDEO_ID: $videoId | AVAILABLE_FORMAT_COUNT: $formatCount")
                
                if (adaptiveFormats != null) {
                    for (i in 0 until adaptiveFormats.length()) {
                        val format = adaptiveFormats.getJSONObject(i)
                        val mimeType = format.optString("mimeType", "")
                        
                        if (mimeType.contains("audio/")) {
                            val streamUrl = format.optString("url")
                            // If url is missing, it's likely a cipher-protected format we can't play without deciphering
                            if (streamUrl.isNotBlank()) {
                                val bitrate = format.optInt("bitrate", 0)
                                if (bitrate > bestBitrate) {
                                    bestBitrate = bitrate
                                    bestUrl = streamUrl
                                    selectedMime = mimeType
                                }
                            }
                        }
                    }
                }
                
                // 2. Try HLS Manifest (Often more reliable for direct playback)
                val hlsUrl = streamingData.optString("hlsManifestUrl")
                if (bestUrl == null && hlsUrl.isNotBlank()) {
                    Log.i(TAG, "RESOLVE_STREAM_SUCCESS | VIDEO_ID: $videoId | SOURCE: HLS_MANIFEST")
                    return@withContext Result.success(hlsUrl)
                }
                
                // 3. Try Basic Formats
                if (bestUrl == null) {
                    val basicFormats = streamingData.optJSONArray("formats")
                    if (basicFormats != null && basicFormats.length() > 0) {
                        for (i in 0 until basicFormats.length()) {
                            val format = basicFormats.getJSONObject(i)
                            val streamUrl = format.optString("url")
                            if (streamUrl.isNotBlank()) {
                                bestUrl = streamUrl
                                selectedMime = format.optString("mimeType", "audio/mp4")
                                break
                            }
                        }
                    }
                }
                
                if (bestUrl != null) {
                    Log.i(TAG, "RESOLVE_STREAM_SUCCESS | VIDEO_ID: $videoId | SELECTED_FORMAT_MIME: $selectedMime | SELECTED_FORMAT_BITRATE: $bestBitrate")
                    Result.success(bestUrl)
                } else {
                    Log.e(TAG, "RESOLVE_STREAM_FAILED | VIDEO_ID: $videoId | ERROR: SOURCE_PROTECTED_OR_UNAVAILABLE")
                    Result.failure(Exception("This track is protected by YouTube signature cipher and cannot be played directly."))
                }
            } catch (e: Exception) {
                Log.e(TAG, "RESOLVE_STREAM_ERROR | VIDEO_ID: $videoId | ERROR: ${e.message}")
                Result.failure(e)
            }
        }
    }

    override suspend fun search(query: String): Result<MusicSearchResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return@withContext Result.success(MusicSearchResult(query = query))
        }

        try {
            Log.i(TAG, "YOUTUBE SEARCH START | query: $trimmed")
            
            // 1. Check Shared Search Cache (Optimization only)
            val cached = AuraSharedBackendCache.getCachedSearch(trimmed)
            if (cached != null && cached.isNotEmpty()) {
                Log.i(TAG, "SEARCH CACHE HIT | query: $trimmed")
                return@withContext Result.success(
                    MusicSearchResult(
                        query = trimmed,
                        topResult = cached.firstOrNull(),
                        songs = cached,
                        artists = cached.map { Artist(id = it.artist, name = it.artist, providerId = it.artist) }.distinctBy { it.name },
                        albums = cached.map { Album(id = it.album, title = it.album, artist = it.artist, providerId = it.album) }.distinctBy { it.title }
                    )
                )
            }

            // 2. YouTube Music Internal Client Search (Primary Discovery)
            Log.i(TAG, "AURA YTM SEARCH START | query: $trimmed")
            val songs = fetchFromYouTubeMusic(trimmed)
            
            if (songs.isNotEmpty()) {
                Log.i(TAG, "AURA YTM SEARCH SUCCESS | results: ${songs.size}")
                AuraSharedBackendCache.putCachedSearch(trimmed, songs)
                return@withContext Result.success(createSearchResult(trimmed, songs))
            } else {
                return@withContext Result.success(MusicSearchResult(query = trimmed))
            }
        } catch (e: Exception) {
            Log.e(TAG, "AURA YTM Search failure for query: $trimmed", e)
            Result.failure(Exception("Search failed: ${e.message}"))
        }
    }

    private suspend fun fetchFromYouTubeMusic(query: String): List<Song> {
        return try {
            val apiKey = OwnerConfig.YOUTUBE_DATA_API_KEY.ifBlank { "AIzaSyC1In-Yq6I-Yq6I-Yq6I-Yq6I-Yq6I" }
            val url = "https://music.youtube.com/youtubei/v1/search?alt=json&key=$apiKey"
            
            val body = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "WEB_REMIX")
                        put("clientVersion", "1.20240101.01.00")
                        put("hl", "en")
                        put("gl", "US")
                    })
                })
                put("query", query)
                // Filter for Songs specifically to prioritize music results
                put("params", "EgWKAQIIAWoKEAkQBRAKEAMQBA%3D%3D") 
            }

            val response = fetchHttpPost(url, body.toString())
            parseYouTubeMusicResults(response)
        } catch (e: Exception) {
            Log.e(TAG, "YTM Fetch Error", e)
            emptyList()
        }
    }

    private fun parseYouTubeMusicResults(jsonString: String): List<Song> {
        val songs = mutableListOf<Song>()
        try {
            val root = JSONObject(jsonString)
            val contents = root.optJSONObject("contents") ?: return emptyList()
            
            // Search result structure for YTM is complex, traverse common paths
            val sectionList = contents.optJSONObject("tabbedSearchResultsRenderer")
                ?.optJSONArray("tabs")?.optJSONObject(0)
                ?.optJSONObject("tabRenderer")?.optJSONObject("content")
                ?.optJSONObject("sectionListRenderer")?.optJSONArray("contents")
                ?: contents.optJSONObject("sectionListRenderer")?.optJSONArray("contents")
                ?: return emptyList()

            for (i in 0 until sectionList.length()) {
                val shelf = sectionList.getJSONObject(i).optJSONObject("musicShelfRenderer") ?: continue
                val items = shelf.optJSONArray("contents") ?: continue
                
                for (j in 0 until items.length()) {
                    val item = items.getJSONObject(j).optJSONObject("musicResponsiveListItemRenderer") ?: continue
                    val song = parseMusicItem(item)
                    if (song != null) songs.add(song)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing YTM results", e)
        }
        return songs
    }

    private fun parseMusicItem(item: JSONObject): Song? {
        try {
            var title = "Unknown"
            var artist = "Unknown Artist"
            var album = "YouTube Music"
            var durationStr = ""
            var videoId: String? = null
            var thumbUrl: String? = null

            if (item.has("flexColumns")) {
                // musicResponsiveListItemRenderer
                val flexColumns = item.optJSONArray("flexColumns") ?: return null
                
                // Column 0: Title
                val titleColumn = flexColumns.optJSONObject(0)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                title = titleColumn?.optJSONObject("text")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text") ?: "Unknown"
                
                // Column 1: Metadata (Artist, Album, Duration)
                val metaColumn = flexColumns.optJSONObject(1)?.optJSONObject("musicResponsiveListItemFlexColumnRenderer")
                val metaRuns = metaColumn?.optJSONObject("text")?.optJSONArray("runs")
                
                if (metaRuns != null) {
                    val filteredRuns = mutableListOf<String>()
                    for (i in 0 until metaRuns.length()) {
                        val run = metaRuns.getJSONObject(i)
                        val text = run.optString("text")
                        if (text != " • " && text != "•") {
                            filteredRuns.add(text)
                        }
                    }
                    
                    if (filteredRuns.size >= 1) artist = filteredRuns[0]
                    if (filteredRuns.size >= 2) album = filteredRuns[1]
                    if (filteredRuns.size >= 3) durationStr = filteredRuns[2]
                }
                
                // Video ID extraction
                val overlay = item.optJSONObject("overlay")
                videoId = overlay?.optJSONObject("musicItemThumbnailOverlayRenderer")
                    ?.optJSONObject("content")?.optJSONObject("musicPlayButtonRenderer")
                    ?.optJSONObject("playNavigationEndpoint")?.optJSONObject("watchEndpoint")
                    ?.optString("videoId")
                    ?: item.optJSONObject("navigationEndpoint")?.optJSONObject("watchEndpoint")?.optString("videoId")

                // Thumbnail
                val thumbnails = item.optJSONObject("thumbnail")?.optJSONObject("musicThumbnailRenderer")
                    ?.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                thumbUrl = thumbnails?.optJSONObject(thumbnails.length() - 1)?.optString("url")
            } else {
                // musicTwoColumnItemRenderer or musicCarouselShelfRenderer items
                title = item.optJSONObject("title")?.optJSONArray("runs")?.optJSONObject(0)?.optString("text") ?: "Unknown"
                val subRuns = item.optJSONObject("subtitle")?.optJSONArray("runs")
                if (subRuns != null) {
                    artist = subRuns.optJSONObject(0)?.optString("text") ?: "Unknown Artist"
                }
                
                videoId = item.optJSONObject("navigationEndpoint")?.optJSONObject("watchEndpoint")?.optString("videoId")
                
                val thumbnails = item.optJSONObject("thumbnail")?.optJSONObject("musicThumbnailRenderer")
                    ?.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                    ?: item.optJSONObject("thumbnail")?.optJSONArray("thumbnails")
                thumbUrl = thumbnails?.optJSONObject(thumbnails.length() - 1)?.optString("url")
            }

            if (videoId == null) return null
            if (thumbUrl == null) thumbUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"

            return Song(
                id = videoId,
                title = cleanTitle(title),
                artist = artist,
                album = album,
                durationMs = parseDuration(durationStr),
                youtubeVideoId = videoId,
                candidateVideoIds = listOf(videoId),
                coverUrl = thumbUrl,
                isPlayable = true,
                providerId = videoId
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun parseDuration(durationStr: String): Long {
        if (durationStr.isBlank()) return 240000L
        val parts = durationStr.split(":")
        return try {
            when (parts.size) {
                2 -> (parts[0].toLong() * 60 + parts[1].toLong()) * 1000
                3 -> (parts[0].toLong() * 3600 + parts[1].toLong() * 60 + parts[2].toLong()) * 1000
                else -> 240000L
            }
        } catch (e: Exception) {
            240000L
        }
    }

    private fun createSearchResult(query: String, songs: List<Song>): MusicSearchResult {
        return MusicSearchResult(
            query = query,
            topResult = songs.firstOrNull(),
            songs = songs,
            artists = songs.map { Artist(id = it.artist, name = it.artist, providerId = it.artist) }.distinctBy { it.name },
            albums = songs.map { Album(id = it.album, title = it.album, artist = it.artist, providerId = it.album) }.distinctBy { it.title }
        )
    }

    private fun parseJsonItems(jsonString: String, outSongs: MutableList<Song>) {
        // ... (Keep for potential secondary backend support)
    }

    override suspend fun getSong(id: String): Result<Song> = withContext(Dispatchers.IO) {
        // Check cache first
        val cached = AuraSharedBackendCache.getCachedMetadata(id)
        if (cached != null) {
            return@withContext Result.success(
                Song(
                    id = cached.videoId,
                    title = cached.title,
                    artist = cached.artist,
                    album = "YouTube Music",
                    durationMs = cached.durationMs,
                    youtubeVideoId = cached.videoId,
                    candidateVideoIds = cached.candidateVideoIds,
                    coverUrl = cached.coverUrl,
                    isPlayable = true,
                    providerId = cached.videoId
                )
            )
        }

        // Return a basic item if not cached, resolution will find the stream
        Result.success(
            Song(
                id = id,
                title = "YouTube Track",
                artist = "Artist",
                album = "YouTube Music",
                durationMs = 240000L,
                youtubeVideoId = id,
                candidateVideoIds = listOf(id),
                coverUrl = "https://img.youtube.com/vi/$id/hqdefault.jpg",
                isPlayable = true,
                providerId = id
            )
        )
    }

    override suspend fun getRecommendations(): Result<List<Song>> = withContext(Dispatchers.IO) {
        try {
            val apiKey = OwnerConfig.YOUTUBE_DATA_API_KEY.ifBlank { "AIzaSyC1In-Yq6I-Yq6I-Yq6I-Yq6I-Yq6I" }
            val url = "https://music.youtube.com/youtubei/v1/browse?alt=json&key=$apiKey"
            
            val body = JSONObject().apply {
                put("context", JSONObject().apply {
                    put("client", JSONObject().apply {
                        put("clientName", "WEB_REMIX")
                        put("clientVersion", "1.20240101.01.00")
                        put("hl", "en")
                        put("gl", "US")
                    })
                })
                put("browseId", "FEmusic_home")
            }

            val response = fetchHttpPost(url, body.toString())
            val songs = parseBrowseResults(response)
            
            if (songs.isNotEmpty()) {
                Result.success(songs)
            } else {
                // Fallback to a popular search if home browse fails or is empty
                val fallbackResult = fetchFromYouTubeMusic("Top Hits 2024")
                Result.success(fallbackResult)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Home Data Fetch Error", e)
            Result.failure(e)
        }
    }

    private fun parseBrowseResults(jsonString: String): List<Song> {
        val songs = mutableListOf<Song>()
        try {
            val root = JSONObject(jsonString)
            val contents = root.optJSONObject("contents") ?: return emptyList()
            val sectionList = contents.optJSONObject("singleColumnBrowseResultsRenderer")
                ?.optJSONArray("tabs")?.optJSONObject(0)
                ?.optJSONObject("tabRenderer")?.optJSONObject("content")
                ?.optJSONObject("sectionListRenderer")?.optJSONArray("contents")
                ?: return emptyList()

            for (i in 0 until sectionList.length()) {
                val section = sectionList.getJSONObject(i)
                val shelf = section.optJSONObject("musicShelfRenderer") 
                    ?: section.optJSONObject("musicCarouselShelfRenderer")
                    ?: continue
                
                val items = shelf.optJSONArray("contents") ?: continue
                for (j in 0 until items.length()) {
                    val itemWrapper = items.getJSONObject(j)
                    val item = itemWrapper.optJSONObject("musicResponsiveListItemRenderer")
                        ?: itemWrapper.optJSONObject("musicTwoColumnItemRenderer")
                        ?: continue
                    
                    val song = parseMusicItem(item)
                    if (song != null && !songs.any { it.id == song.id }) {
                        songs.add(song)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing browse results", e)
        }
        return songs
    }

    override suspend fun getArtist(id: String): Result<Artist> = withContext(Dispatchers.IO) {
        Result.success(Artist(id = id, name = id, providerId = id))
    }

    override suspend fun getAlbum(id: String): Result<Album> = withContext(Dispatchers.IO) {
        Result.success(Album(id = id, title = "Album", artist = "Artist", providerId = id))
    }

    override suspend fun getPlaylist(id: String): Result<Playlist> = withContext(Dispatchers.IO) {
        Result.success(Playlist(id = id, title = "Playlist", description = "", songIds = emptyList()))
    }

    override suspend fun getLyrics(
        songId: String,
        title: String,
        artist: String,
        durationSec: Int
    ): Result<List<LyricLine>> = withContext(Dispatchers.IO) {
        try {
            val cleanSongTitle = cleanTitle(title)
            val cleanArtistName = cleanArtist(artist)
            val encodedTitle = URLEncoder.encode(cleanSongTitle, "UTF-8")
            val encodedArtist = URLEncoder.encode(cleanArtistName, "UTF-8")

            val urlString = "${OwnerConfig.LYRICS_BASE_URL}/get?track_name=$encodedTitle&artist_name=$encodedArtist${if (durationSec > 0) "&duration=$durationSec" else ""}"
            val response = fetchHttpGet(urlString)
            val json = JSONObject(response)

            val syncedLyrics = json.optString("syncedLyrics", "")
            val plainLyrics = json.optString("plainLyrics", "")

            if (syncedLyrics.isNotBlank()) {
                val lines = parseSyncedLyrics(syncedLyrics)
                if (lines.isNotEmpty()) return@withContext Result.success(lines)
            }

            if (plainLyrics.isNotBlank()) {
                val plainLines = plainLyrics.split("\n")
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                val stepMs = if (plainLines.isNotEmpty() && durationSec > 0) (durationSec * 1000L) / plainLines.size else 4000L
                val lyricLines = plainLines.mapIndexed { index, text ->
                    LyricLine(timestampMs = index * stepMs, text = text)
                }
                return@withContext Result.success(lyricLines)
            }

            Result.failure(Exception("Lyrics unavailable for this track."))
        } catch (e: Exception) {
            Result.failure(Exception("Lyrics unavailable for this track."))
        }
    }

    private fun parseSyncedLyrics(lrcContent: String): List<LyricLine> {
        val result = mutableListOf<LyricLine>()
        val regex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)""")

        lrcContent.lines().forEach { line ->
            val match = regex.find(line.trim())
            if (match != null) {
                val min = match.groupValues[1].toLongOrNull() ?: 0L
                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                val millisRaw = match.groupValues[3]
                val ms = if (millisRaw.length == 2) (millisRaw.toLongOrNull() ?: 0L) * 10 else (millisRaw.toLongOrNull() ?: 0L)
                val text = match.groupValues[4].trim()

                if (text.isNotBlank()) {
                    val totalMs = (min * 60 * 1000) + (sec * 1000) + ms
                    result.add(LyricLine(timestampMs = totalMs, text = text))
                }
            }
        }
        return result.sortedBy { it.timestampMs }
    }
}
