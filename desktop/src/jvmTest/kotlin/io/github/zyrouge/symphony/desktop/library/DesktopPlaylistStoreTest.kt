package io.github.zyrouge.symphony.desktop.library

import io.github.zyrouge.symphony.ui.model.UiPlaylist
import java.nio.file.Files
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DesktopPlaylistStoreTest {
    @Test
    fun `playlists round trip through the store`() {
        val directory = Files.createTempDirectory("symphony-playlists")
        try {
            val store = DesktopPlaylistStore(directory.resolve("playlists.properties"))
            val playlists = listOf(
                UiPlaylist(
                    id = "local-0",
                    title = "Roadtrip",
                    trackIds = listOf("/music/a.mp3", "/music/b.mp3"),
                    isLocal = false,
                ),
                UiPlaylist(
                    id = "local-1",
                    title = "夜间",
                    trackIds = emptyList(),
                    isLocal = false,
                ),
            )

            store.save(playlists)

            assertEquals(playlists, store.load())
        } finally {
            directory.toFile().deleteRecursively()
        }
    }

    @Test
    fun `a missing file loads as an empty list`() {
        val directory = Files.createTempDirectory("symphony-playlists")
        try {
            val store = DesktopPlaylistStore(directory.resolve("missing.properties"))
            assertTrue(store.load().isEmpty())
        } finally {
            directory.toFile().deleteRecursively()
        }
    }
}
