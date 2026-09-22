package com.example.auramusic.data.provider

import com.example.auramusic.model.Album
import com.example.auramusic.model.Artist
import com.example.auramusic.model.MusicSearchResult
import com.example.auramusic.model.Song

interface MusicCatalogProvider {
    suspend fun search(query: String): Result<MusicSearchResult>
    suspend fun getRecommendations(songId: String): Result<List<Song>>
    suspend fun getArtistDetails(artistId: String): Result<Artist>
    suspend fun getAlbumDetails(albumId: String): Result<Album>
}
