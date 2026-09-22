package com.example.auramusic.data.provider

import android.util.Log
import com.example.auramusic.model.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class LrclibLyricsProvider : LyricsProvider {

    override suspend fun getLyrics(song: Song): Result<Lyrics> = withContext(Dispatchers.IO) {
        try {
            val titleEncoded = URLEncoder.encode(song.title, "UTF-8")
            val artistEncoded = URLEncoder.encode(song.artist, "UTF-8")
            val albumEncoded = URLEncoder.encode(song.album, "UTF-8")
            val durationSec = song.durationMs / 1000

            val urlString = "https://lrclib.net/api/get?track_name=$titleEncoded&artist_name=$artistEncoded&album_name=$albumEncoded&duration=$durationSec"
            Log.i("LrclibLyricsProvider", "Fetching lyrics for track '${song.title}' from LRCLIB...")

            val response = fetchHttpGet(urlString)
            val json = JSONObject(response)

            val syncedText = json.optString("syncedLyrics", "")
            val plainText = json.optString("plainLyrics", "")

            val parsedSynced = if (syncedText.isNotBlank()) parseLrc(syncedText) else emptyList()
            
            val lyrics = Lyrics(
                plainLyrics = if (plainText.isNotBlank()) plainText else syncedText,
                syncedLyrics = parsedSynced
            )

            Result.success(lyrics)
        } catch (e: Exception) {
            Log.w("LrclibLyricsProvider", "Failed to fetch lyrics for '${song.title}': ${e.message}")
            Result.failure(e)
        }
    }

    private fun parseLrc(lrcContent: String): List<LyricLine> {
        val lines = mutableListOf<LyricLine>()
        val lrcRegex = Regex("""\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)""")

        for (line in lrcContent.lineSequence()) {
            val match = lrcRegex.find(line.trim())
            if (match != null) {
                val min = match.groupValues[1].toLongOrNull() ?: 0L
                val sec = match.groupValues[2].toLongOrNull() ?: 0L
                val msPart = match.groupValues[3].padEnd(3, '0').take(3).toLongOrNull() ?: 0L
                val timeMs = (min * 60 + sec) * 1000 + msPart
                val text = match.groupValues[4].trim()
                lines.add(LyricLine(timeMs, text))
            }
        }
        return lines.sortedBy { it.timeMs }
    }

    private fun fetchHttpGet(urlString: String): String {
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "GET"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.setRequestProperty("User-Agent", "AuraMusic/1.0 (Android)")
            conn.setRequestProperty("Accept", "application/json")

            if (conn.responseCode !in 200..299) {
                throw Exception("HTTP Error ${conn.responseCode}")
            }

            return conn.inputStream.bufferedReader().use { it.readText() }
        } finally {
            conn.disconnect()
        }
    }
}
