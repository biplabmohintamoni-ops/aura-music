package com.example.auramusic.engine

import android.util.Log
import com.example.auramusic.data.provider.AudioSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object PipedExtractionService {

    private const val TAG = "PipedExtractionService"
    const val SPOOF_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
    
    // Dual-routing architecture: Vercel primary endpoints + Piped API fallback instances
    private val VERCEL_API_BASES = listOf(
        "https://jiosaavn-api-v3.vercel.app",
        "https://saavn.dev/api",
        "https://jiosaavn-api-self-beta.vercel.app"
    )

    private val PIPED_INSTANCES = listOf(
        "https://pipedapi.kavin.rocks",
        "https://api.piped.privacy.com.de",
        "https://pipedapi.tokhmi.xyz",
        "https://pa.il.ax"
    )

    suspend fun extractAudioStream(videoId: String): Result<AudioSource> = withContext(Dispatchers.IO) {
        Log.i(TAG, "EXTRACTION_START | Video ID / Track: $videoId")

        if (videoId.startsWith("http://") || videoId.startsWith("https://")) {
            return@withContext Result.success(
                AudioSource(
                    url = videoId,
                    userAgent = SPOOF_USER_AGENT,
                    format = "audio/mp4",
                    bitrateKbps = 320,
                    isLocal = false
                )
            )
        }

        val rawClean = videoId.trim()
        val encoded = URLEncoder.encode(rawClean, "UTF-8")

        // 1. Direct Song Lookup if videoId looks like an ID
        if (!rawClean.contains(" ") && rawClean.length < 24) {
            for (base in VERCEL_API_BASES) {
                try {
                    val songUrl = if (base.contains("v3")) "$base/song?id=$encoded" else "$base/songs?id=$encoded"
                    val conn = URL(songUrl).openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.connectTimeout = 4000
                    conn.readTimeout = 4000
                    conn.useCaches = false
                    conn.setRequestProperty("User-Agent", SPOOF_USER_AGENT)
                    conn.setRequestProperty("Accept", "application/json")

                    if (conn.responseCode in 200..299) {
                        val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                        conn.disconnect()
                        val json = JSONObject(responseText)
                        val mediaUrls = json.optJSONObject("media_urls")
                        var streamUrl = mediaUrls?.optString("320_KBPS", mediaUrls.optString("160_KBPS", "")) ?: ""
                        if (streamUrl.isBlank()) {
                            streamUrl = json.optString("media_url", "")
                        }
                        if (streamUrl.isBlank()) {
                            val moreInfo = json.optJSONObject("more_info")
                            streamUrl = moreInfo?.optString("vlink", "") ?: ""
                        }

                        if (streamUrl.isNotBlank() && !streamUrl.contains("youtube.com")) {
                            Log.i(TAG, "DIRECT_SONG_EXTRACTION_SUCCESS | Live direct URL: $streamUrl")
                            return@withContext Result.success(
                                AudioSource(
                                    url = streamUrl,
                                    userAgent = SPOOF_USER_AGENT,
                                    format = "audio/mp4",
                                    bitrateKbps = 320,
                                    isLocal = false
                                )
                            )
                        }
                    } else {
                        conn.disconnect()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Direct song lookup $base failed: ${e.message}")
                }
            }
        }

        // 2. Primary Search Attempt on Vercel Mirrors
        val searchTerms = listOf(
            rawClean,
            rawClean.replace(Regex("""\s*[\(\[].*?[\)\]]"""), "").trim(),
            rawClean.split(" ").firstOrNull() ?: rawClean
        ).distinct()

        for (queryTerm in searchTerms) {
            val qEncoded = URLEncoder.encode(queryTerm, "UTF-8")
            for (base in VERCEL_API_BASES) {
                try {
                    val searchUrl = if (base.contains("v3")) "$base/search?query=$qEncoded" else "$base/search/songs?query=$qEncoded"
                    val conn = URL(searchUrl).openConnection() as HttpURLConnection
                    conn.requestMethod = "GET"
                    conn.connectTimeout = 4000
                    conn.readTimeout = 4000
                    conn.useCaches = false
                    conn.setRequestProperty("User-Agent", SPOOF_USER_AGENT)
                    conn.setRequestProperty("Accept", "application/json")

                    if (conn.responseCode in 200..299) {
                        val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                        conn.disconnect()

                        val json = JSONObject(responseText)
                        val dataObj = json.opt("data")
                        val resultsArray: JSONArray? = when (dataObj) {
                            is JSONObject -> dataObj.optJSONArray("results")
                            is JSONArray -> dataObj
                            else -> json.optJSONArray("results")
                        }

                        if (resultsArray != null && resultsArray.length() > 0) {
                            val firstItem = resultsArray.getJSONObject(0)
                            val moreInfo = firstItem.optJSONObject("more_info")
                            var streamUrl = moreInfo?.optString("vlink", "") ?: ""

                            if (streamUrl.isBlank()) {
                                val mediaUrls = firstItem.optJSONObject("media_urls")
                                streamUrl = mediaUrls?.optString("320_KBPS", mediaUrls.optString("160_KBPS", "")) ?: ""
                            }

                            if (streamUrl.isBlank()) {
                                val downloadArray = firstItem.optJSONArray("downloadUrl")
                                if (downloadArray != null && downloadArray.length() > 0) {
                                    streamUrl = downloadArray.getJSONObject(downloadArray.length() - 1).optString("url", "")
                                }
                            }

                            if (streamUrl.isBlank()) {
                                streamUrl = firstItem.optString("url", firstItem.optString("media_url", ""))
                            }

                            if (streamUrl.isNotBlank() && !streamUrl.contains("youtube.com")) {
                                Log.i(TAG, "SEARCH_EXTRACTION_SUCCESS | Live direct URL: $streamUrl")
                                return@withContext Result.success(
                                    AudioSource(
                                        url = streamUrl,
                                        userAgent = SPOOF_USER_AGENT,
                                        format = "audio/mp4",
                                        bitrateKbps = 320,
                                        isLocal = false
                                    )
                                )
                            }
                        }
                    } else {
                        conn.disconnect()
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Search extraction mirror $base failed: ${e.message}")
                }
            }
        }

        // 3. Fallback Piped API Proxy: Decrypt link from 'audioStreams' array
        Log.i(TAG, "FALLBACK_PIPED_PROXY_START | Attempting Piped API instances for $videoId")
        for (pipedBase in PIPED_INSTANCES) {
            try {
                val pipedUrl = "$pipedBase/streams/$encoded"
                val conn = URL(pipedUrl).openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000
                conn.useCaches = false
                conn.setRequestProperty("User-Agent", SPOOF_USER_AGENT)
                conn.setRequestProperty("Accept", "application/json")

                if (conn.responseCode in 200..299) {
                    val resp = conn.inputStream.bufferedReader().use { it.readText() }
                    conn.disconnect()
                    val pJson = JSONObject(resp)
                    val audioStreams = pJson.optJSONArray("audioStreams")
                    if (audioStreams != null && audioStreams.length() > 0) {
                        var bestUrl = ""
                        var maxBitrate = 0
                        for (i in 0 until audioStreams.length()) {
                            val streamObj = audioStreams.getJSONObject(i)
                            val streamCandidate = streamObj.optString("url", "")
                            val bitrate = streamObj.optInt("bitrate", 128)
                            if (streamCandidate.isNotBlank() && bitrate >= maxBitrate) {
                                bestUrl = streamCandidate
                                maxBitrate = bitrate
                            }
                        }

                        if (bestUrl.isNotBlank()) {
                            Log.i(TAG, "PIPED_FALLBACK_SUCCESS | Decrypted audioStream URL from $pipedBase")
                            return@withContext Result.success(
                                AudioSource(
                                    url = bestUrl,
                                    userAgent = SPOOF_USER_AGENT,
                                    format = "audio/mp4",
                                    bitrateKbps = if (maxBitrate > 0) maxBitrate / 1000 else 320,
                                    isLocal = false
                                )
                            )
                        }
                    }
                } else {
                    conn.disconnect()
                }
            } catch (pe: Exception) {
                Log.w(TAG, "Piped instance $pipedBase error: ${pe.message}")
            }
        }

        Log.e(TAG, "EXTRACTION_FAILED | All extraction mirrors and Piped proxies exhausted for: $videoId")
        Result.failure(Exception("All extraction mirrors and fallback proxies failed."))
    }
}
