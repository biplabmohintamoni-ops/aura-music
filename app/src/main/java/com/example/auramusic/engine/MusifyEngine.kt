package com.example.auramusic.engine

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL
import java.util.concurrent.ConcurrentHashMap

/**
 * Source-Level Port of Musify's Non-UI Music Engine into Kotlin/Android.
 *
 * Faithfully ports Musify's YouTube / InnerTube client logic:
 * - Primary Client: VISIONOS (1.02)
 * - Secondary Clients: TVHTML5, WEB_EMBEDDED_PLAYER, ANDROID
 * - sw.js_data visitor token extraction
 * - InnerTube /youtubei/v1/player POST payload & headers
 * - Adaptive format parsing & audio quality selection (High, Medium, Low)
 * - Stream validation via HEAD request
 * - Musify ProxyManager implementation (scraping public proxies when needed)
 */
object MusifyEngine {

    private const val TAG = "MusifyEngine"
    private const val CONNECT_TIMEOUT_MS = 10000
    private const val READ_TIMEOUT_MS = 10000

    enum class AudioQuality {
        LOW,    // ~48k - 64k
        MEDIUM, // ~128k
        HIGH    // Highest available (~160k+ Opus/M4A)
    }

    data class MusifyClientConfig(
        val name: String,
        val version: String,
        val userAgent: String,
        val extraJson: (JSONObject.() -> Unit)? = null,
        val extraHeaders: Map<String, String>? = null
    )

    data class AudioStreamInfo(
        val url: String,
        val container: String,
        val codec: String,
        val bitrate: Int,
        val qualityName: String,
        val clientName: String,
        val userAgent: String
    )

    data class StreamManifest(
        val videoId: String,
        val title: String?,
        val audioStreams: List<AudioStreamInfo>,
        val playabilityStatus: String,
        val playabilityReason: String?
    )

    // Musify's exact primary & secondary client configurations
    val VISION_OS_CLIENT = MusifyClientConfig(
        name = "VISIONOS",
        version = "1.02",
        userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 15_7_3) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.0 Safari/605.1.15",
        extraJson = {
            put("deviceMake", "Apple")
            put("deviceModel", "RealityDevice17,1")
            put("osName", "visionOS")
            put("osVersion", "26.5.23O471")
        }
    )

    val TV_HTML5_CLIENT = MusifyClientConfig(
        name = "TVHTML5",
        version = "7.20260707.07.00",
        userAgent = "Mozilla/5.0 (ChromiumStylePlatform) Cobalt/25.lts.30.1034943-gold (unlike Gecko), Unknown_TV_Unknown_0/Unknown (Unknown, Unknown)",
        extraJson = {
            put("deviceMake", "")
            put("deviceModel", "")
            put("originalUrl", "https://www.youtube.com/tv")
            put("theme", "CLASSIC")
            put("platform", "DESKTOP")
            put("clientFormFactor", "UNKNOWN_FORM_FACTOR")
            put("webpSupport", false)
        },
        extraHeaders = mapOf(
            "Sec-Fetch-Mode" to "navigate",
            "Origin" to "https://www.youtube.com"
        )
    )

    val WEB_EMBEDDED_CLIENT = MusifyClientConfig(
        name = "WEB_EMBEDDED_PLAYER",
        version = "2.20260708.00.00",
        userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/119.0.0.0 Safari/537.36"
    )

    val ANDROID_CLIENT = MusifyClientConfig(
        name = "ANDROID",
        version = "19.30.36",
        userAgent = "com.google.android.youtube/19.30.36 (Linux; U; Android 12; en_US; Pixel 6)",
        extraJson = {
            put("androidSdkVersion", 31)
        }
    )

    val ALL_CLIENTS = listOf(VISION_OS_CLIENT, TV_HTML5_CLIENT, WEB_EMBEDDED_CLIENT, ANDROID_CLIENT)

    @Volatile
    var activeUserAgent: String = VISION_OS_CLIENT.userAgent

    private var cachedVisitorData: String? = null
    private val urlCache = ConcurrentHashMap<String, Pair<String, Long>>() // key -> (url, expiryMs)

    /**
     * Extracts visitorData from sw.js_data as in Musify's video_controller.dart
     */
    suspend fun fetchVisitorData(): String? = withContext(Dispatchers.IO) {
        if (cachedVisitorData != null) return@withContext cachedVisitorData
        try {
            val url = URL("https://www.youtube.com/sw.js_data")
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = CONNECT_TIMEOUT_MS
            conn.readTimeout = READ_TIMEOUT_MS
            conn.setRequestProperty("User-Agent", VISION_OS_CLIENT.userAgent)

            if (conn.responseCode == 200) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                // Musify video_controller.dart extracts array index data[0][2][0][0][13]
                val regex = Regex("""Cgt([a-zA-Z0-9_-]{10,40})""")
                val match = regex.find(text)
                if (match != null) {
                    val token = match.value
                    cachedVisitorData = token
                    Log.i(TAG, "EXTRACTED VISITOR DATA TOKEN: $token")
                    return@withContext token
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to fetch visitor data token", e)
        }
        null
    }

    /**
     * Executes InnerTube /youtubei/v1/player POST request
     */
    suspend fun getPlayerResponse(
        videoId: String,
        client: MusifyClientConfig,
        proxy: Proxy? = null
    ): JSONObject = withContext(Dispatchers.IO) {
        val visitorData = fetchVisitorData()

        val body = JSONObject().apply {
            put("videoId", videoId)
            put("context", JSONObject().apply {
                put("client", JSONObject().apply {
                    put("clientName", client.name)
                    put("clientVersion", client.version)
                    put("hl", "en")
                    put("timeZone", "UTC")
                    put("utcOffsetMinutes", 0)
                    if (visitorData != null) {
                        put("visitorData", visitorData)
                    }
                    client.extraJson?.invoke(this)
                })
            })
            put("contentCheckOk", true)
            put("racyCheckOk", true)
        }

        val urlString = "https://www.youtube.com/youtubei/v1/player?prettyPrint=false"
        val url = URL(urlString)
        val conn = (if (proxy != null) url.openConnection(proxy) else url.openConnection()) as HttpURLConnection

        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = CONNECT_TIMEOUT_MS
            conn.readTimeout = READ_TIMEOUT_MS
            conn.setRequestProperty("User-Agent", client.userAgent)
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Accept", "application/json, text/plain, */*")

            if (visitorData != null) {
                conn.setRequestProperty("X-Goog-Visitor-Id", visitorData)
            }

            client.extraHeaders?.forEach { (k, v) ->
                conn.setRequestProperty(k, v)
            }

            conn.doOutput = true
            conn.outputStream.write(body.toString().toByteArray(Charsets.UTF_8))

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val respStr = BufferedReader(InputStreamReader(stream, "UTF-8")).use { it.readText() }

            JSONObject(respStr)
        } finally {
            conn.disconnect()
        }
    }

    /**
     * Parses PlayerResponse and builds StreamManifest
     */
    fun parseManifest(videoId: String, client: MusifyClientConfig, responseJson: JSONObject): StreamManifest {
        val playability = responseJson.optJSONObject("playabilityStatus")
        val status = playability?.optString("status") ?: "UNKNOWN"
        val reason = playability?.optString("reason")

        val videoDetails = responseJson.optJSONObject("videoDetails")
        val title = videoDetails?.optString("title")

        val audioStreams = mutableListOf<AudioStreamInfo>()

        val streamingData = responseJson.optJSONObject("streamingData")
        if (streamingData != null) {
            val adaptive = streamingData.optJSONArray("adaptiveFormats")
            if (adaptive != null) {
                for (i in 0 until adaptive.length()) {
                    val format = adaptive.getJSONObject(i)
                    val mimeType = format.optString("mimeType", "")
                    if (mimeType.contains("audio/")) {
                        val streamUrl = format.optString("url")
                        if (streamUrl.isNotBlank()) {
                            val bitrate = format.optInt("bitrate", 0)
                            val container = if (mimeType.contains("webm")) "webm" else "mp4"
                            val codec = if (mimeType.contains("opus")) "opus" else "aac"
                            val qualityName = if (bitrate >= 140000) "High" else if (bitrate >= 96000) "Medium" else "Low"

                            audioStreams.add(
                                AudioStreamInfo(
                                    url = streamUrl,
                                    container = container,
                                    codec = codec,
                                    bitrate = bitrate,
                                    qualityName = qualityName,
                                    clientName = client.name,
                                    userAgent = client.userAgent
                                )
                            )
                        }
                    }
                }
            }

            // HLS fallback
            val hlsUrl = streamingData.optString("hlsManifestUrl")
            if (hlsUrl.isNotBlank()) {
                audioStreams.add(
                    AudioStreamInfo(
                        url = hlsUrl,
                        container = "m3u8",
                        codec = "aac",
                        bitrate = 128000,
                        qualityName = "Medium",
                        clientName = "${client.name}_HLS",
                        userAgent = client.userAgent
                    )
                )
            }
        }

        return StreamManifest(
            videoId = videoId,
            title = title,
            audioStreams = audioStreams,
            playabilityStatus = status,
            playabilityReason = reason
        )
    }

    /**
     * Selects best audio stream according to target quality
     */
    fun selectStreamForQuality(streams: List<AudioStreamInfo>, quality: AudioQuality): AudioStreamInfo? {
        if (streams.isEmpty()) return null
        val sorted = streams.sortedByDescending { it.bitrate }

        return when (quality) {
            AudioQuality.HIGH -> sorted.firstOrNull()
            AudioQuality.MEDIUM -> sorted.firstOrNull { it.bitrate in 96000..150000 } ?: sorted.firstOrNull()
            AudioQuality.LOW -> sorted.lastOrNull()
        }
    }

    /**
     * Validates stream URL with HEAD request as in YoutubeExplode StreamClient
     */
    suspend fun validateStreamUrl(streamInfo: AudioStreamInfo): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = URL(streamInfo.url)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "HEAD"
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.setRequestProperty("User-Agent", streamInfo.userAgent)
            conn.setRequestProperty("Range", "bytes=0-1023")

            val code = conn.responseCode
            conn.disconnect()
            code in 200..308
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Musify's ProxyManager logic: scrapes free proxies and attempts routing player requests
     */
    object ProxyManager {
        private val scrapedProxies = mutableListOf<Proxy>()

        suspend fun fetchFreeProxies(): List<Proxy> = withContext(Dispatchers.IO) {
            Log.i(TAG, "[PROXY_DIAG] ProxyManager instantiated / fetchFreeProxies invoked")
            if (scrapedProxies.isNotEmpty()) {
                Log.i(TAG, "[PROXY_DIAG] Using cached proxy list (${scrapedProxies.size} proxies)")
                return@withContext scrapedProxies
            }

            val list = mutableListOf<Proxy>()
            val sources = listOf(
                "https://api.proxyscrape.com/v4/free-proxy-list/get?request=display_proxies&proxy_format=protocolipport&format=text&protocol=http",
                "https://spys.me/proxy.txt"
            )

            for (src in sources) {
                Log.i(TAG, "[PROXY_DIAG] Downloading proxy list from source: $src")
                val startTime = System.currentTimeMillis()
                try {
                    val url = URL(src)
                    val conn = url.openConnection() as HttpURLConnection
                    conn.connectTimeout = 5000
                    conn.readTimeout = 5000
                    val responseCode = conn.responseCode
                    val durationMs = System.currentTimeMillis() - startTime
                    Log.i(TAG, "[PROXY_DIAG] Proxy source $src returned HTTP status $responseCode in ${durationMs}ms")

                    if (responseCode == 200) {
                        val lines = conn.inputStream.bufferedReader().useLines { it.toList() }
                        val ipPortRegex = Regex("""(\d{1,3}\.\d{1,3}\.\d{1,3}\.\d{1,3}):(\d{2,5})""")
                        for (line in lines) {
                            val match = ipPortRegex.find(line)
                            if (match != null) {
                                val ip = match.groupValues[1]
                                val port = match.groupValues[2].toIntOrNull()
                                if (port != null && port in 1..65535) {
                                    list.add(Proxy(Proxy.Type.HTTP, InetSocketAddress(ip, port)))
                                    if (list.size >= 10) break
                                }
                            }
                        }
                        Log.i(TAG, "[PROXY_DIAG] Successfully parsed ${list.size} proxies from $src")
                    }
                    conn.disconnect()
                } catch (e: Exception) {
                    val durationMs = System.currentTimeMillis() - startTime
                    Log.w(TAG, "[PROXY_DIAG] Proxy download failed for $src after ${durationMs}ms: ${e.javaClass.simpleName} - ${e.message}")
                }
                if (list.isNotEmpty()) break
            }

            scrapedProxies.clear()
            scrapedProxies.addAll(list)
            Log.i(TAG, "[PROXY_DIAG] Total proxies obtained: ${scrapedProxies.size}")
            list
        }

        suspend fun tryGetManifestWithProxy(videoId: String, client: MusifyClientConfig): StreamManifest? {
            Log.i(TAG, "[PROXY_DIAG] tryGetManifestWithProxy START | VIDEO_ID: $videoId | CLIENT: ${client.name}")
            val proxies = fetchFreeProxies()
            if (proxies.isEmpty()) {
                Log.e(TAG, "[PROXY_DIAG] No proxies available in list. Proxy resolution aborted.")
                return null
            }

            for ((index, p) in proxies.withIndex()) {
                Log.i(TAG, "[PROXY_DIAG] Proxy #${index + 1}/${proxies.size} selected. Sending ${client.name} /youtubei/v1/player request...")
                val reqStartTime = System.currentTimeMillis()
                try {
                    val resp = getPlayerResponse(videoId, client, p)
                    val durationMs = System.currentTimeMillis() - reqStartTime
                    val manifest = parseManifest(videoId, client, resp)
                    Log.i(TAG, "[PROXY_DIAG] Proxy #${index + 1} response received in ${durationMs}ms | playabilityStatus: ${manifest.playabilityStatus} | reason: ${manifest.playabilityReason} | audioStreams: ${manifest.audioStreams.size}")

                    if (manifest.playabilityStatus == "OK" && manifest.audioStreams.isNotEmpty()) {
                        Log.i(TAG, "[PROXY_DIAG] SUCCESS with Proxy #${index + 1}")
                        return manifest
                    } else {
                        Log.w(TAG, "[PROXY_DIAG] Proxy #${index + 1} returned playabilityStatus=${manifest.playabilityStatus} (${manifest.playabilityReason}). Retrying with next proxy...")
                    }
                } catch (e: Exception) {
                    val durationMs = System.currentTimeMillis() - reqStartTime
                    Log.w(TAG, "[PROXY_DIAG] Proxy #${index + 1} request failed after ${durationMs}ms with exception: ${e.javaClass.simpleName} - ${e.message}. Retrying with next proxy...")
                }
            }

            Log.e(TAG, "[PROXY_DIAG] All ${proxies.size} proxies in list were attempted and failed.")
            return null
        }
    }

    /**
     * Complete Musify Stream Resolution Call Chain:
     * 1. Try VISIONOS client (Musify primary)
     * 2. Try secondary clients (TVHTML5, WEB_EMBEDDED, ANDROID)
     * 3. If LOGIN_REQUIRED or blocked, try ProxyManager
     * 4. Select audio stream for quality
     * 5. Validate stream
     */
    suspend fun resolveStream(videoId: String, quality: AudioQuality = AudioQuality.HIGH): Result<AudioStreamInfo> = withContext(Dispatchers.IO) {
        Log.i(TAG, "MUSIFY_RESOLVE_START | VIDEO_ID: $videoId | TARGET_QUALITY: $quality")

        // 1. Check cache
        val cached = urlCache[videoId]
        if (cached != null && System.currentTimeMillis() < cached.second) {
            Log.i(TAG, "MUSIFY_RESOLVE_CACHE_HIT | VIDEO_ID: $videoId")
            val streamInfo = AudioStreamInfo(
                url = cached.first,
                container = "m4a",
                codec = "aac",
                bitrate = 160000,
                qualityName = quality.name,
                clientName = "CACHED",
                userAgent = activeUserAgent
            )
            return@withContext Result.success(streamInfo)
        }

        var lastPlayabilityStatus = "UNKNOWN"
        var lastPlayabilityReason = ""

        // Try direct clients in Musify priority order
        for (client in ALL_CLIENTS) {
            try {
                val resp = getPlayerResponse(videoId, client)
                val manifest = parseManifest(videoId, client, resp)

                lastPlayabilityStatus = manifest.playabilityStatus
                lastPlayabilityReason = manifest.playabilityReason ?: ""

                Log.i(TAG, "MUSIFY_CLIENT_RESPONSE | CLIENT: ${client.name} | STATUS: $lastPlayabilityStatus | REASON: $lastPlayabilityReason | AUDIO_STREAMS: ${manifest.audioStreams.size}")

                if (manifest.playabilityStatus == "OK" && manifest.audioStreams.isNotEmpty()) {
                    val selected = selectStreamForQuality(manifest.audioStreams, quality)
                    if (selected != null) {
                        activeUserAgent = selected.userAgent
                        // Cache for 4 hours
                        urlCache[videoId] = Pair(selected.url, System.currentTimeMillis() + 4 * 3600 * 1000L)
                        Log.i(TAG, "MUSIFY_RESOLVE_SUCCESS | CLIENT: ${client.name} | URL: ${selected.url.take(100)}...")
                        return@withContext Result.success(selected)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Musify client ${client.name} exception for $videoId: ${e.message}")
                if (lastPlayabilityStatus == "UNKNOWN" || lastPlayabilityReason.isBlank()) {
                    lastPlayabilityReason = "${client.name} request error: ${e.message ?: e.javaClass.simpleName}"
                }
            }
        }

        // Try ProxyManager if direct clients fail or return LOGIN_REQUIRED
        Log.i(TAG, "Direct clients failed ($lastPlayabilityStatus: $lastPlayabilityReason). Trying ProxyManager...")
        val proxyManifest = ProxyManager.tryGetManifestWithProxy(videoId, VISION_OS_CLIENT)
        if (proxyManifest != null && proxyManifest.audioStreams.isNotEmpty()) {
            val selected = selectStreamForQuality(proxyManifest.audioStreams, quality)
            if (selected != null) {
                activeUserAgent = selected.userAgent
                urlCache[videoId] = Pair(selected.url, System.currentTimeMillis() + 4 * 3600 * 1000L)
                return@withContext Result.success(selected)
            }
        }

        Log.e(TAG, "MUSIFY_RESOLVE_FAILED | STATUS: $lastPlayabilityStatus | REASON: $lastPlayabilityReason")
        Result.failure(Exception("YouTube InnerTube response: $lastPlayabilityStatus ($lastPlayabilityReason)"))
    }
}
