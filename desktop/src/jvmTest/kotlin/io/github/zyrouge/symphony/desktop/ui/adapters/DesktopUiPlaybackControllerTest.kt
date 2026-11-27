package io.github.zyrouge.symphony.desktop.ui.adapters

import io.github.zyrouge.symphony.desktop.player.AudioPlayer
import io.github.zyrouge.symphony.desktop.player.PlaybackState
import io.github.zyrouge.symphony.ui.contracts.UiLibraryController
import io.github.zyrouge.symphony.ui.model.UiAlbum
import io.github.zyrouge.symphony.ui.model.UiAlbumArtist
import io.github.zyrouge.symphony.ui.model.UiArtist
import io.github.zyrouge.symphony.ui.model.UiArtwork
import io.github.zyrouge.symphony.ui.model.UiGenre
import io.github.zyrouge.symphony.ui.model.UiMusicFolder
import io.github.zyrouge.symphony.ui.model.UiPlaylist
import io.github.zyrouge.symphony.ui.model.UiSong
import java.nio.file.Path
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DesktopUiPlaybackControllerTest {
    @Test
    fun `play queue follows the selected song and updates queue actions`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val player = FakeAudioPlayer()
            val library = FakeLibrary(
                listOf(
                    song("one", "/music/one.mp3"),
                    song("two", "/music/two.mp3"),
                ),
            )
            val controller = DesktopUiPlaybackController(library, scope, player)

            controller.playQueue(listOf("one", "two"), startIndex = 0)
            assertEquals(listOf("one", "two"), controller.queue.value)
            assertEquals(0, controller.queueIndex.value)
            assertEquals(Path.of("/music/one.mp3"), player.played)
            assertEquals(true, controller.isPlaying.value)

            controller.skipToNext()
            assertEquals(1, controller.queueIndex.value)
            assertEquals(Path.of("/music/two.mp3"), player.played)

            controller.removeQueueItem(1)
            assertEquals(listOf("one"), controller.queue.value)
            assertEquals(0, controller.queueIndex.value)
            assertEquals(Path.of("/music/one.mp3"), player.played)

            controller.clearUpNext()
            assertEquals(listOf("one"), controller.queue.value)
        } finally {
            scope.cancel()
        }
    }

    private fun song(id: String, path: String) = UiSong(
        id = id,
        title = id,
        album = null,
        artists = emptySet(),
        albumArtists = emptySet(),
        genres = emptySet(),
        durationMillis = 0,
        dateModifiedMillis = 0,
        trackNumber = null,
        discNumber = null,
        path = path,
        coverKey = null,
    )

    private class FakeLibrary(songs: List<UiSong>) : UiLibraryController {
        override val isScanning = MutableStateFlow(false)
        override val songs = MutableStateFlow(songs)
        override val albums = MutableStateFlow(emptyList<UiAlbum>())
        override val artists = MutableStateFlow(emptyList<UiArtist>())
        override val albumArtists = MutableStateFlow(emptyList<UiAlbumArtist>())
        override val genres = MutableStateFlow(emptyList<UiGenre>())
        override val playlists = MutableStateFlow(emptyList<UiPlaylist>())
        override val musicFolders = MutableStateFlow(emptyList<UiMusicFolder>())
        override val favorites = MutableStateFlow<Set<String>>(emptySet())

        override fun songArtwork(songId: String) = UiArtwork.Song(songId)
        override fun albumArtwork(albumId: String) = UiArtwork.Album(albumId)
        override fun artistArtwork(name: String) = UiArtwork.Artist(name)
        override fun albumArtistArtwork(name: String) = UiArtwork.AlbumArtist(name)
        override fun playlistArtwork(playlistId: String) = UiArtwork.Playlist(playlistId)
        override fun genreArtwork(name: String) = UiArtwork.Genre(name)

        override fun createPlaylist(title: String, songIds: List<String>) = Unit
        override fun deletePlaylist(playlistId: String) = Unit
        override fun updatePlaylistSongs(playlistId: String, songIds: List<String>) = Unit
    }

    private class FakeAudioPlayer : AudioPlayer {
        override var state: PlaybackState = PlaybackState.Idle
            private set
        var played: Path? = null

        override fun play(path: Path) {
            played = path
            state = PlaybackState.Playing(path)
        }

        override fun pause() {
            val path = (state as? PlaybackState.Playing)?.path ?: return
            state = PlaybackState.Paused(path)
        }

        override fun resume() {
            val path = (state as? PlaybackState.Paused)?.path ?: return
            state = PlaybackState.Playing(path)
        }

        override fun stop() {
            state = PlaybackState.Idle
        }

        override fun seekTo(positionMs: Long) = Unit
        override fun setVolume(percent: Int) = Unit
        override fun positionMs() = 0L
        override fun durationMs() = 0L
        override fun close() {
            state = PlaybackState.Idle
        }
    }
}
