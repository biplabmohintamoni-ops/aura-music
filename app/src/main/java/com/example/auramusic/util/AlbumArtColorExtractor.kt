package com.example.auramusic.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

object AlbumArtColorExtractor {

    // In-memory LRU cache for extracted accent colors
    private val paletteCache = LruCache<String, Color>(50)

    private val DEFAULT_ACCENT = Color(0xFF9C27B0) // Elegant purple default matching settings reference

    suspend fun extractAccentColor(context: Context, imageUrl: String?): Color = withContext(Dispatchers.IO) {
        if (imageUrl.isNullOrBlank()) {
            return@withContext DEFAULT_ACCENT
        }

        // Check cache first
        paletteCache.get(imageUrl)?.let { return@withContext it }

        try {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 3500
            connection.readTimeout = 3500
            connection.doInput = true
            connection.connect()

            val inputStream = connection.inputStream
            // Downsample image to 48x48 to process in milliseconds
            val options = BitmapFactory.Options().apply {
                inSampleSize = 8
            }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()
            connection.disconnect()

            if (bitmap != null) {
                val extracted = findVibrantColor(bitmap)
                bitmap.recycle()
                paletteCache.put(imageUrl, extracted)
                return@withContext extracted
            }
        } catch (e: Exception) {
            // Network or decoding failed, use hash-based appealing color or default
            val fallback = generateFallbackColor(imageUrl)
            paletteCache.put(imageUrl, fallback)
            return@withContext fallback
        }

        return@withContext DEFAULT_ACCENT
    }

    private fun findVibrantColor(bitmap: Bitmap): Color {
        val width = bitmap.width
        val height = bitmap.height
        val step = maxOf(1, width / 24)

        var bestColor = DEFAULT_ACCENT
        var bestScore = -1f

        val hsv = FloatArray(3)

        for (x in 0 until width step step) {
            for (y in 0 until height step step) {
                val pixel = bitmap.getPixel(x, y)
                val alpha = android.graphics.Color.alpha(pixel)
                if (alpha < 128) continue

                android.graphics.Color.colorToHSV(pixel, hsv)
                val saturation = hsv[1]
                val value = hsv[2]

                // Avoid near-black and near-white
                if (value < 0.20f || value > 0.95f) continue
                if (saturation < 0.25f) continue

                // Score based on saturation and balanced lightness
                val score = saturation * 2f + (1f - kotlin.math.abs(value - 0.7f))
                if (score > bestScore) {
                    bestScore = score
                    // Boost saturation and clamp value for stunning UI accent
                    hsv[1] = saturation.coerceIn(0.55f, 0.90f)
                    hsv[2] = value.coerceIn(0.70f, 0.95f)
                    val vibrantRgb = android.graphics.Color.HSVToColor(hsv)
                    bestColor = Color(vibrantRgb)
                }
            }
        }

        return bestColor
    }

    private fun generateFallbackColor(seed: String): Color {
        val hash = kotlin.math.abs(seed.hashCode())
        val hues = floatArrayOf(270f, 330f, 210f, 160f, 25f, 190f)
        val hue = hues[hash % hues.size]
        val rgb = android.graphics.Color.HSVToColor(floatArrayOf(hue, 0.75f, 0.85f))
        return Color(rgb)
    }
}
