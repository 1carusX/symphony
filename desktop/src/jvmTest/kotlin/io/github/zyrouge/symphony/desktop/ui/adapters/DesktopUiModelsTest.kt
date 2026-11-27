package io.github.zyrouge.symphony.desktop.ui.adapters

import io.github.zyrouge.symphony.ui.model.UiSong
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DesktopUiModelsTest {
    private val first = song(
        id = "one",
        title = "First",
        album = "Album",
        artists = setOf("Artist"),
        albumArtists = setOf("Album Artist"),
        genres = setOf("Rock"),
        durationMillis = 1000,
        path = "/music/album/one.mp3",
    )

    private val second = song(
        id = "two",
        title = "Second",
        album = "Album",
        artists = setOf("Artist", "Guest"),
        albumArtists = setOf("Album Artist"),
        genres = setOf("Rock", "Live"),
        durationMillis = 2000,
        path = "/music/album/two.mp3",
    )

    @Test
    fun `songs group into albums with combined duration and artists`() {
        val albums = listOf(first, second).toUiAlbums()
        assertEquals(1, albums.size)
        assertEquals("Album", albums.single().name)
        assertEquals(2, albums.single().trackCount)
        assertEquals(3000, albums.single().durationMillis)
        assertEquals(setOf("Album Artist"), albums.single().artistNames)
    }

    @Test
    fun `songs group into artists album artists and genres`() {
        val songs = listOf(first, second)
        assertEquals(
            listOf("Artist", "Guest"),
            songs.toUiArtists().map { it.name },
        )
        assertEquals("Album Artist", songs.toUiAlbumArtists().single().name)
        assertEquals(
            listOf("Live", "Rock"),
            songs.toUiGenres().map { it.name },
        )
    }

    @Test
    fun `folders are distinct parent paths`() {
        val folders = listOf(first, second).toUiMusicFolders()
        assertEquals(1, folders.size)
        assertEquals("/music/album", folders.single().path)
        assertEquals("album", folders.single().name)
    }

    private fun song(
        id: String,
        title: String,
        album: String?,
        artists: Set<String>,
        albumArtists: Set<String>,
        genres: Set<String>,
        durationMillis: Long,
        path: String,
    ) = UiSong(
        id = id,
        title = title,
        album = album,
        artists = artists,
        albumArtists = albumArtists,
        genres = genres,
        durationMillis = durationMillis,
        dateModifiedMillis = 0,
        trackNumber = null,
        discNumber = null,
        path = path,
        coverKey = null,
    )
}
