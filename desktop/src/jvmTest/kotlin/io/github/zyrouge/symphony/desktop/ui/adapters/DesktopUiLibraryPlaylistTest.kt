package io.github.zyrouge.symphony.desktop.ui.adapters

import io.github.zyrouge.symphony.ui.model.UiPlaylist
import java.util.concurrent.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DesktopUiLibraryPlaylistTest {
    @Test
    fun `playlists are loaded and every change is persisted`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val persisted = mutableListOf<List<UiPlaylist>>()
            val library = DesktopUiLibraryController(
                scope = scope,
                initialPlaylists = listOf(
                    UiPlaylist("local-0", "Saved", listOf("old"), isLocal = false),
                ),
                onPlaylistsChanged = { persisted += it },
            )
            assertEquals("Saved", library.playlists.value.single().title)

            library.createPlaylist("Roadtrip")
            val created = library.playlists.value.last()
            assertEquals("Roadtrip", created.title)
            assertTrue(created.trackIds.isEmpty())

            library.updatePlaylistSongs(created.id, listOf("song-1", "song-2"))
            assertEquals(
                listOf("song-1", "song-2"),
                library.playlists.value.last().trackIds,
            )

            library.deletePlaylist(created.id)
            assertEquals(listOf("Saved"), library.playlists.value.map { it.title })
            assertEquals(3, persisted.size)
        } catch (error: CancellationException) {
            throw error
        } finally {
            scope.cancel()
        }
    }

    @Test
    fun `playlists can be created updated and deleted`() {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        try {
            val library = DesktopUiLibraryController(scope)

            library.createPlaylist("Roadtrip")
            val created = library.playlists.value.single()
            assertEquals("Roadtrip", created.title)
            assertTrue(created.trackIds.isEmpty())

            library.updatePlaylistSongs(created.id, listOf("song-1", "song-2"))
            assertEquals(
                listOf("song-1", "song-2"),
                library.playlists.value.single().trackIds,
            )

            library.deletePlaylist(created.id)
            assertTrue(library.playlists.value.isEmpty())
        } catch (error: CancellationException) {
            throw error
        } finally {
            scope.cancel()
        }
    }
}
