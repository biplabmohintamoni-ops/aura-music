package com.example.auramusic.data.provider

import com.example.auramusic.model.Album
import com.example.auramusic.model.Artist
import com.example.auramusic.model.LyricLine
import com.example.auramusic.model.MusicSearchResult
import com.example.auramusic.model.Playlist
import com.example.auramusic.model.Song

interface MusicProvider {
    suspend fun search(query: String): Result<MusicSearchResult>
    suspend fun getSong(id: String): Result<Song>
    suspend fun getArtist(id: String): Result<Artist>
    suspend fun getAlbum(id: String): Result<Album>
    suspend fun getPlaylist(id: String): Result<Playlist>
    suspend fun getRecommendations(): Result<List<Song>>
    suspend fun getLyrics(songId: String, title: String, artist: String, durationSec: Int): Result<List<LyricLine>>
}
