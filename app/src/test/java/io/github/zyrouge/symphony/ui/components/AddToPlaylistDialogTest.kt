package io.github.zyrouge.symphony.ui.components

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AddToPlaylistDialogTest {
    @Test
    fun `adding a song the playlist holds keeps a single copy`() {
        assertEquals(listOf("a", "b"), togglePlaylistSongs(listOf("a", "b"), listOf("b"), add = true))
    }

    @Test
    fun `adding appends only the missing songs in order`() {
        assertEquals(
            listOf("a", "b", "c", "d"),
            togglePlaylistSongs(listOf("a", "b"), listOf("c", "b", "d", "c"), add = true),
        )
    }

    @Test
    fun `removing drops every occurrence and keeps the rest in order`() {
        assertEquals(
            listOf("a", "c"),
            togglePlaylistSongs(listOf("a", "b", "c", "b"), listOf("b", "x"), add = false),
        )
    }
}
