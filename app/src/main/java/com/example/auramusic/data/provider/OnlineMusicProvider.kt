package com.example.auramusic.data.provider

import android.util.Log
import com.example.auramusic.model.Album
import com.example.auramusic.model.Artist
import com.example.auramusic.model.LyricLine
import com.example.auramusic.model.MusicSearchResult
import com.example.auramusic.model.Playlist
import com.example.auramusic.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class OnlineMusicProvider : MusicProvider {

    companion object {
        private const val TAG = "OnlineMusicProvider"
        private const val CONNECT_TIMEOUT_MS = 10000
        private const val READ_TIMEOUT_MS = 10000
        private const val VERCEL_BASE_URL = "https://vercel.app"

        fun fetchHttpGet(urlString: String): String {
            var conn: HttpURLConnection? = null
            return try {
                val url = URL(urlString)
                conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = CONNECT_TIMEOUT_MS
                conn.readTimeout = READ_TIMEOUT_MS
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                conn.setRequestProperty("Accept", "application/json")
                val responseCode = conn.responseCode
                if (responseCode in 200..299) {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } else {
                    ""
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network stream fetch error: ${e.message}")
                ""
            } finally {
                conn?.disconnect()
            }
        }

        fun cleanTitle(title: String): String {
            return title
                .replace(
                    Regex(
                        """\s*[\(\[].*?(feat|ft|official|video|audio|remix|from).*?[\)\]]""",
                        RegexOption.IGNORE_CASE
                    ),
                    ""
                )
                .trim()
        }

        fun cleanArtist(artist: String): String {
            return if (artist.isBlank() || artist.equals("null", ignoreCase = true)) {
                "Various Artists"
            } else {
                artist
            }
        }
    }

    override suspend fun search(query: String): Result<MusicSearchResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        val targetQuery = if (trimmed.equals("trending", ignoreCase = true) || trimmed.isBlank()) {
            "New Releases"
        } else {
            trimmed
        }

        val parsedSongs = mutableListOf<Song>()

        try {
            val searchUrl = "$VERCEL_BASE_URL/search/songs?query=${URLEncoder.encode(targetQuery, "UTF-8")}"
            val jsonResponse = fetchHttpGet(searchUrl)

            if (jsonResponse.isNotBlank()) {
                val json = JSONObject(jsonResponse)
                val success = json.optBoolean("success", false)
                val dataObj = json.optJSONObject("data")

                if (success && dataObj != null) {
                    val resultsArray = dataObj.optJSONArray("results")
                    if (resultsArray != null) {
                        for (i in 0 until resultsArray.length()) {
                            val item = resultsArray.getJSONObject(i)
                            val id = item.optString("id", "")
                            val title = item.optString("name", "Unknown Track")
                            val artist = item.optString("primaryArtists", "Various Artists")
                            val durationSeconds = item.optLong("duration", 180L)

                            val imageArray = item.optJSONArray("image")
                            var imageUrl = ""
                            if (imageArray != null && imageArray.length() > 0) {
                                imageUrl = imageArray
                                    .getJSONObject(imageArray.length() - 1)
                                    .optString("url", "")
                            }

                            val downloadArray = item.optJSONArray("downloadUrl")
                            var fullStreamUrl = ""
                            if (downloadArray != null && downloadArray.length() > 0) {
                                for (j in downloadArray.length() - 1 downTo 0) {
                                    val dObj = downloadArray.optJSONObject(j) ?: continue
                                    val candidate = dObj.optString("url", dObj.optString("link", ""))
                                    if (candidate.isNotBlank() && !candidate.contains("preview")) {
                                        fullStreamUrl = candidate
                                        break
                                    }
                                }
                            }

                            if (id.isNotBlank() && fullStreamUrl.isNotBlank() && !fullStreamUrl.contains("preview")) {
                                parsedSongs.add(
                                    Song(
                                        id = id,
                                        title = cleanTitle(title),
                                        artist = cleanArtist(artist),
                                        album = item.optString("album", "Aura Album"),
                                        durationMs = durationSeconds * 1000L,
                                        audioUrl = fullStreamUrl,
                                        coverUrl = imageUrl.ifBlank { null },
                                        youtubeVideoId = null
                                    )
                                )
                            }
                        }
                    }
                }
            }

            val finalResult = MusicSearchResult(
                query = trimmed,
                topResult = parsedSongs.firstOrNull(),
                songs = parsedSongs,
                artists = parsedSongs
                    .map { Artist(id = it.artist, name = it.artist, providerId = it.artist) }
                    .distinctBy { it.name },
                albums = parsedSongs
                    .map { Album(id = it.album, title = it.album, artist = it.artist, providerId = it.album) }
                    .distinctBy { it.title }
            )
            return@withContext Result.success(finalResult)
        } catch (e: Exception) {
            return@withContext Result.success(MusicSearchResult(query = trimmed))
        }
    }

    override suspend fun getSong(id: String): Result<Song> =
        Result.failure(Exception("Not implemented"))

    override suspend fun getRecommendations(id: String): Result<List<Song>> =
        Result.success(emptyList())

    override suspend fun getArtist(id: String): Result<Artist> =
        Result.failure(Exception("Not implemented"))

    override suspend fun getAlbum(id: String): Result<Album> =
        Result.failure(Exception("Not implemented"))

    override suspend fun getPlaylist(id: String): Result<Playlist> =
        Result.failure(Exception("Not implemented"))

    override suspend fun getLyrics(id: String): Result<List<LyricLine>> =
        Result.success(emptyList())
}