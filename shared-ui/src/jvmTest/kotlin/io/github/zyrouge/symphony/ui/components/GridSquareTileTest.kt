package io.github.zyrouge.symphony.ui.components

import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BentoGridSquareTileTest {
    // The c8 detail frame: cover 3x3, info and actions beside it, songs from row 3, a side tile under the cover.
    private val placements = listOf(
        BentoTilePlacement("cover", column = 1, row = 1, columnSpan = 3, rowSpan = 3),
        BentoTilePlacement("info", column = 4, row = 1, columnSpan = 5, rowSpan = 1),
        BentoTilePlacement("play", column = 4, row = 2, columnSpan = 2, rowSpan = 1),
        BentoTilePlacement("songs", column = 4, row = 3, columnSpan = 5, rowSpan = 4),
        BentoTilePlacement("albums", column = 1, row = 4, columnSpan = 3, rowSpan = 2),
    )

    @Test
    fun `a wide cover grows square and pushes the rows below it down`() {
        val plain = measureBentoGrid(placements, BentoGridClass.C8, innerWidthDp = 1132f)
        val square = measureBentoGrid(placements, BentoGridClass.C8, innerWidthDp = 1132f, squareTileId = "cover")
        val cover = square.tiles.getValue("cover").second
        assertEquals(cover.width, cover.height, 0.01f)

        val delta = cover.height - plain.tiles.getValue("cover").second.height
        // Above the cover's last row: untouched.
        assertEquals(plain.tiles.getValue("info").second, square.tiles.getValue("info").second)
        assertEquals(plain.tiles.getValue("play").second, square.tiles.getValue("play").second)
        // Reaching the cover's last row: grows with it, so the bottoms line up.
        val songs = square.tiles.getValue("songs").second
        assertEquals(plain.tiles.getValue("songs").second.height + delta, songs.height, 0.01f)
        // Below it: moved down by the same amount.
        val albums = square.tiles.getValue("albums").second
        assertEquals(plain.tiles.getValue("albums").second.offsetY + delta, albums.offsetY, 0.01f)
        assertEquals(cover.offsetY + cover.height + BentoGridClass.C8.gapDp, albums.offsetY, 0.01f)
        assertEquals(plain.totalHeight + delta, square.totalHeight, 0.01f)
    }

    @Test
    fun `a narrow cover shrinks with the tiles beside it`() {
        // c4 phone: cover and info 2x2 side by side.
        val phone = listOf(
            BentoTilePlacement("cover", column = 1, row = 1, columnSpan = 2, rowSpan = 2),
            BentoTilePlacement("info", column = 3, row = 1, columnSpan = 2, rowSpan = 2),
            BentoTilePlacement("play", column = 1, row = 3, columnSpan = 2, rowSpan = 1),
        )
        val square = measureBentoGrid(phone, BentoGridClass.C4, innerWidthDp = 328f, squareTileId = "cover")
        val cover = square.tiles.getValue("cover").second
        assertEquals(cover.width, cover.height, 0.01f)
        assertEquals(cover.height, square.tiles.getValue("info").second.height, 0.01f)
        assertEquals(cover.height + BentoGridClass.C4.gapDp, square.tiles.getValue("play").second.offsetY, 0.01f)
    }

    @Test
    fun `shrinking stops before a neighbour gets too short`() {
        // c6 at 600 dp: a square cover would squeeze the 110 dp action row to about 47 dp.
        val c6 = listOf(
            BentoTilePlacement("cover", column = 1, row = 1, columnSpan = 2, rowSpan = 2),
            BentoTilePlacement("info", column = 3, row = 1, columnSpan = 4, rowSpan = 1),
            BentoTilePlacement("play", column = 3, row = 2, columnSpan = 2, rowSpan = 1),
        )
        val square = measureBentoGrid(c6, BentoGridClass.C6, innerWidthDp = 552f, squareTileId = "cover")
        assertEquals(BENTO_SQUARE_NEIGHBOUR_MIN_HEIGHT, square.tiles.getValue("play").second.height, 0.01f)
    }

    @Test
    fun `an unknown square tile leaves the grid as it is`() {
        val plain = measureBentoGrid(placements, BentoGridClass.C8, innerWidthDp = 1132f)
        assertEquals(plain, measureBentoGrid(placements, BentoGridClass.C8, innerWidthDp = 1132f, squareTileId = "missing"))
    }
}
