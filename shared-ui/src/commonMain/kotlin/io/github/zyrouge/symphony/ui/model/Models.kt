package io.github.zyrouge.symphony.ui.model

import androidx.compose.runtime.Immutable

enum class UiSongSort {
    CUSTOM,
    TITLE,
    ARTIST,
    ALBUM,
    DURATION,
    DATE_MODIFIED,
    COMPOSER,
    ALBUM_ARTIST,
    YEAR,
    FILENAME,
    TRACK_NUMBER,
}

/** A playback position as the UI needs it: the played and total times and the ratio. */
@Immutable
data class UiPlaybackPosition(
    val playedMillis: Long,
    val durationMillis: Long,
) {
    val ratio: Float
        get() = when {
            durationMillis > 0 -> (playedMillis.toFloat() / durationMillis).coerceIn(0f, 1f)
            else -> 0f
        }
}

@Immutable
data class UiSong(
    val id: String,
    val title: String,
    val album: String?,
    val artists: Set<String>,
    val albumArtists: Set<String>,
    val genres: Set<String>,
    val durationMillis: Long,
    val dateModifiedMillis: Long,
    val trackNumber: Int?,
    val discNumber: Int?,
    val path: String,
    val coverKey: String?,
    val encoder: String? = null,
)

@Immutable
data class UiAlbum(
    val id: String,
    val name: String,
    val artistNames: Set<String>,
    val startYear: Int?,
    val endYear: Int?,
    val trackCount: Int,
    val durationMillis: Long,
)

@Immutable
data class UiArtist(
    val name: String,
    val albumCount: Int,
    val trackCount: Int,
)

@Immutable
data class UiAlbumArtist(
    val name: String,
    val albumCount: Int,
    val trackCount: Int,
)

@Immutable
data class UiGenre(
    val name: String,
    val trackCount: Int,
)

@Immutable
data class UiPlaylist(
    val id: String,
    val title: String,
    val trackIds: List<String>,
    val isLocal: Boolean,
) {
    val trackCount: Int get() = trackIds.size
}

@Immutable
data class UiMusicFolder(
    val id: String,
    val name: String,
    val uri: String?,
    val path: String?,
)

sealed interface UiArtwork {
    val key: String

    @Immutable
    data class Song(val songId: String) : UiArtwork {
        override val key = "song:$songId"
    }

    @Immutable
    data class Album(val albumId: String) : UiArtwork {
        override val key = "album:$albumId"
    }

    @Immutable
    data class Artist(val name: String) : UiArtwork {
        override val key = "artist:$name"
    }

    @Immutable
    data class AlbumArtist(val name: String) : UiArtwork {
        override val key = "album-artist:$name"
    }

    @Immutable
    data class Playlist(val playlistId: String) : UiArtwork {
        override val key = "playlist:$playlistId"
    }

    @Immutable
    data class Genre(val name: String) : UiArtwork {
        override val key = "genre:$name"
    }

    @Immutable
    data class Uri(val uri: String, val fallbackKey: String? = null) : UiArtwork {
        override val key = "uri:$uri"
    }

    @Immutable
    data class Resource(val resourceKey: String) : UiArtwork {
        override val key = "resource:$resourceKey"
    }
}
