package com.example.auramusic.data.provider

/**
 * Application Owner Configuration for AURA MUSIC.
 *
 * Creator: MADE BY K98
 * Instagram: @k98gamer_editz
 * URL: https://www.instagram.com/k98gamer_editz/
 *
 * All external music service endpoints and backend secrets are defined here by the
 * application owner before deployment.
 *
 * NORMAL CONSUMER USERS MUST NEVER BE PROMPTED TO ENTER KEYS, EDIT .env,
 * OR CONFIGURE DEVELOPER SETTINGS.
 */
object OwnerConfig {
    // Creator branding
    const val APP_NAME: String = "AURA MUSIC"
    const val CREATOR_NAME: String = "MADE BY K98"
    const val CREATOR_INSTAGRAM: String = "@k98gamer_editz"
    const val CREATOR_INSTAGRAM_URL: String = "https://www.instagram.com/k98gamer_editz/"

    // Optional owner-side YouTube Data API Key or Backend Proxy
    // AURA MUSIC APP -> AURA BACKEND -> YouTube / Music Provider API
    val YOUTUBE_DATA_API_KEY: String = com.example.auramusic.BuildConfig.YOUTUBE_DATA_API_KEY
    const val OWNER_BACKEND_URL: String = ""

    val hasCustomBackend: Boolean
        get() = OWNER_BACKEND_URL.isNotBlank()

    // YouTube Data API Base
    const val YOUTUBE_API_BASE: String = "https://www.googleapis.com/youtube/v3"

    // Public YouTube Music Search / Suggestion endpoint
    const val YOUTUBE_SUGGEST_BASE: String = "https://suggestqueries.google.com/complete/search"

    // Open lyrics repository endpoint
    const val LYRICS_BASE_URL: String = "https://lrclib.net/api"
}
