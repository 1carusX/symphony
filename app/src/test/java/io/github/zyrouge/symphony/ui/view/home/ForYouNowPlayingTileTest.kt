package io.github.zyrouge.symphony.ui.view.home

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ForYouNowPlayingTileTest {
    private val window = 2000f
    private val tile = 400f

    @Test
    fun `a fully shown tile is fully visible`() {
        assertEquals(1f, forYouTileVisibleFraction(200f, 600f, window, tile))
    }

    @Test
    fun `a tile scrolled mostly out is measured against its full height`() {
        // 100 px of the 400 px tile remain below the top of the scroll viewport, which
        // clips its window bounds to that strip.
        assertEquals(0.25f, forYouTileVisibleFraction(300f, 400f, window, tile))
    }

    @Test
    fun `the part below the window does not count`() {
        assertEquals(0.5f, forYouTileVisibleFraction(1800f, 2200f, window, tile))
    }

    @Test
    fun `a tile above the window is not visible`() {
        assertEquals(0f, forYouTileVisibleFraction(-400f, 0f, window, tile))
    }

    @Test
    fun `a fully clipped tile is not visible`() {
        assertEquals(0f, forYouTileVisibleFraction(0f, 0f, window, tile))
    }

    @Test
    fun `an unmeasured tile is not visible`() {
        assertEquals(0f, forYouTileVisibleFraction(0f, 0f, window, 0f))
    }
}
