package com.example.auramusic.data.repository

import android.util.Log
import com.example.auramusic.data.provider.OnlineMusicProvider
import com.example.auramusic.model.Album
import com.example.auramusic.model.Artist
import com.example.auramusic.model.MusicSearchResult
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

class SearchRepository(
    private val onlineProvider: OnlineMusicProvider = OnlineMusicProvider()
) {

    companion object {
        private const val TAG = "SearchRepository"
        const val BROWSER_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36"
        private val VERCEL_API_BASES = listOf(
            "https://saavn.dev/api",
            "https://jiosaavn-api-self-beta.vercel.app"
        )
    }

    suspend fun search(rawQuery: String): Result<MusicSearchResult> = withContext(Dispatchers.IO) {
        val query = rawQuery.trim()
        if (query.isBlank()) {
            return@withContext Result.success(MusicSearchResult(query = rawQuery))
        }

        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            var vercelSongs: List<Song> = emptyList()

            for (baseUrl in VERCEL_API_BASES) {
                val urlString = if (baseUrl.endsWith("/api")) {
                    "$baseUrl/search/songs?query=$encodedQuery"
                } else {
                    "$baseUrl/search/songs?query=$encodedQuery"
                }
                Log.i(TAG, "Search URL: $urlString")
                val fetched = fetchVercelSongs(urlString)
                if (fetched.isNotEmpty()) {
                    vercelSongs = fetched
                    break
                }
            }

            val finalSongs = if (vercelSongs.isNotEmpty()) {
                sortSongs(vercelSongs)
            } else {
                val fallbackResult = onlineProvider.search(query)
                fallbackResult.getOrNull()?.songs ?: emptyList()
            }

            val finalResult = MusicSearchResult(
                query = rawQuery,
                topResult = finalSongs.firstOrNull(),
                songs = finalSongs,
                artists = finalSongs.map { Artist(id = it.artist, name = it.artist, providerId = it.artist) }.distinctBy { it.name },
                albums = finalSongs.map { Album(id = it.album, title = it.album, artist = it.artist, providerId = it.album) }.distinctBy { it.title }
            )

            Result.success(finalResult)
        } catch (e: Exception) {
            Log.e(TAG, "Search error: ${e.message}", e)
            onlineProvider.search(query)
        }
    }

    private fun fetchVercelSongs(urlString: String): List<Song> {
        val songs = mutableListOf<Song>()
        try {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 6000
            conn.readTimeout = 6000
            conn.useCaches = false
            conn.defaultUseCaches = false
            conn.setRequestProperty("User-Agent", BROWSER_USER_AGENT)
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode == 200) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }

                val root = JSONObject(responseText)
                val dataArray = when {
                    root.has("data") && root.get("data") is JSONObject -> {
                        root.getJSONObject("data").optJSONArray("results") ?: JSONArray()
                    }
                    root.has("data") && root.get("data") is JSONArray -> {
                        root.getJSONArray("data")
                    }
                    root.has("results") -> {
                        root.getJSONArray("results")
                    }
                    else -> JSONArray()
                }

                for (i in 0 until dataArray.length()) {
                    val item = dataArray.getJSONObject(i)
                    val song = parseSongItem(item)
                    if (song != null) {
                        songs.add(song)
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Vercel fetch error: ${e.message}")
        }
        return songs
    }

    private fun parseSongItem(item: JSONObject): Song? {
        try {
            val id = item.optString("id", item.optString("song_id", ""))
            if (id.isBlank()) return null

            val title = item.optString("name", item.optString("title", "Unknown Track"))
            var artist = item.optString("primaryArtists", item.optString("artist", ""))
            if (artist.isBlank() && item.has("artists")) {
                val artistsObj = item.optJSONObject("artists")
                val primaryArray = artistsObj?.optJSONArray("primary")
                if (primaryArray != null && primaryArray.length() > 0) {
                    artist = primaryArray.getJSONObject(0).optString("name", "")
                }
            }
            if (artist.isBlank()) artist = "Unknown Artist"

            val albumObj = item.optJSONObject("album")
            val albumName = albumObj?.optString("name") ?: item.optString("album", "Single")

            var coverUrl: String? = null
            if (item.has("image") && item.get("image") is JSONArray) {
                val images = item.getJSONArray("image")
                coverUrl = images.optJSONObject(images.length() - 1)?.optString("url")
                    ?: images.optJSONObject(images.length() - 1)?.optString("link")
            } else if (item.has("image") && item.get("image") is String) {
                coverUrl = item.getString("image")
            }

            val durationSec = item.optLong("duration", 240L)
            val durationMs = if (durationSec < 10000) durationSec * 1000L else durationSec

            var audioUrl = ""
            if (item.has("downloadUrl") && item.get("downloadUrl") is JSONArray) {
                val downloads = item.getJSONArray("downloadUrl")
                audioUrl = downloads.optJSONObject(downloads.length() - 1)?.optString("url", "")
                    ?: downloads.optJSONObject(downloads.length() - 1)?.optString("link", "") ?: ""
            } else if (item.has("url")) {
                audioUrl = item.optString("url", "")
            }

            return Song(
                id = id,
                title = OnlineMusicProvider.cleanTitle(title),
                artist = OnlineMusicProvider.cleanArtist(artist),
                album = albumName,
                durationMs = durationMs,
                audioUrl = audioUrl,
                coverUrl = coverUrl,
                youtubeVideoId = null
            )
        } catch (e: Exception) {
            return null
        }
    }

    private fun sortSongs(songs: List<Song>): List<Song> {
        return songs.sortedWith(
            compareByDescending<Song> { song ->
                val titleLower = song.title.lowercase()
                when {
                    titleLower.contains("official") -> 4
                    titleLower.contains("full") -> 3
                    else -> 1
                }
            }
        )
    }
}
