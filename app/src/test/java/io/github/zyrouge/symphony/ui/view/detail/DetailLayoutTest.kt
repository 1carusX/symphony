package io.github.zyrouge.symphony.ui.view.detail

import io.github.zyrouge.symphony.ui.components.BentoTilePlacement
import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class DetailLayoutTest {
    private val side = listOf(
        DetailSideSlot("albums", 2, leading = true),
        DetailSideSlot("genres", 1),
    )

    private fun List<BentoTilePlacement>.byId(id: String) = firstOrNull { it.tileId == id }

    @Test
    fun `c4 stacks leading side tiles, then the songs, then the rest`() {
        val placements = detailPlacements(BentoGridClass.C4, 5, side, "queue", "stat")
        assertEquals(BentoTilePlacement("albums", 1, 4, 4, 2), placements.byId("albums"))
        assertEquals(BentoTilePlacement("tracks", 1, 6, 4, 5), placements.byId("tracks"))
        assertEquals(BentoTilePlacement("genres", 1, 11, 4, 1), placements.byId("genres"))
        assertEquals(BentoTilePlacement("queue", 4, 3, 1, 1), placements.byId("queue"))
        assertNull(placements.byId("stat"))
    }

    @Test
    fun `c6 puts the side tiles beside the songs`() {
        val placements = detailPlacements(BentoGridClass.C6, 4, side, "queue", "stat")
        assertEquals(BentoTilePlacement("tracks", 1, 3, 4, 4), placements.byId("tracks"))
        assertEquals(BentoTilePlacement("albums", 5, 3, 2, 2), placements.byId("albums"))
        assertEquals(BentoTilePlacement("genres", 5, 5, 2, 1), placements.byId("genres"))
        assertNull(placements.byId("stat"))
    }

    @Test
    fun `c8 and short stack the side tiles under the cover and show both extra actions`() {
        listOf(BentoGridClass.C8, BentoGridClass.SHORT).forEach { gridClass ->
            val placements = detailPlacements(gridClass, 3, side, "edit", "queue")
            assertEquals(BentoTilePlacement("cover", 1, 1, 3, 3), placements.byId("cover"))
            assertEquals(BentoTilePlacement("edit", 7, 2, 1, 1), placements.byId("edit"))
            assertEquals(BentoTilePlacement("queue", 8, 2, 1, 1), placements.byId("queue"))
            assertEquals(BentoTilePlacement("albums", 1, 4, 3, 2), placements.byId("albums"))
            assertEquals(BentoTilePlacement("tracks", 4, 3, 5, 3), placements.byId("tracks"))
        }
    }

    @Test
    fun `c12 follows the album frame`() {
        val placements = detailPlacements(BentoGridClass.C12, 6, emptyList(), "queue", "stat")
        assertEquals(BentoTilePlacement("cover", 1, 1, 4, 3), placements.byId("cover"))
        assertEquals(BentoTilePlacement("stat", 11, 2, 2, 1), placements.byId("stat"))
        assertEquals(BentoTilePlacement("tracks", 5, 3, 8, 6), placements.byId("tracks"))
    }

    @Test
    fun `list tiles need at least two rows`() {
        assertEquals(2, detailListRowSpan(BentoGridClass.C4, 0))
        assertEquals(2, detailListRowSpan(BentoGridClass.C4, 1))
        // 32 + 30 + 10 x 48 + 12 = 554 over 96 per row.
        assertEquals(6, detailListRowSpan(BentoGridClass.C4, 10))
    }

    @Test
    fun `albums take a row each up to three, then a two-row cover grid`() {
        assertEquals(detailListRowSpan(BentoGridClass.C6, 3, 48f), detailAlbumsRowSpan(BentoGridClass.C6, 3))
        assertEquals(2, detailAlbumsRowSpan(BentoGridClass.C6, 12))
    }

    @Test
    fun `the songs tile is capped at the viewport`() {
        // (600 + 16) / (110 + 16) = 4.9 rows on c6.
        assertEquals(4, detailMaxTracksRowSpan(BentoGridClass.C6, 600f))
        assertEquals(3, detailMaxTracksRowSpan(BentoGridClass.C6, 100f))
    }
}
