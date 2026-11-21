package io.github.zyrouge.symphony.ui.components

import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import io.github.zyrouge.symphony.ui.theme.bento.resolveBentoGridClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class BentoGridTest {

    @ParameterizedTest(name = "[{index}] width={0} height={1} expected={2}")
    @MethodSource("tileSizeClassCases")
    fun `tile size class resolves in the documented order`(
        widthDp: Float,
        heightDp: Float,
        expected: BentoTileSizeClass,
    ) {
        assertEquals(expected, resolveBentoTileSizeClass(widthDp, heightDp))
    }

    @Test
    fun `grid measure computes cell math from the grid class`() {
        val placements = listOf(
            BentoTilePlacement("a", column = 1, row = 1, columnSpan = 2, rowSpan = 2),
            BentoTilePlacement("b", column = 3, row = 2, columnSpan = 2, rowSpan = 1),
        )
        val measure = measureBentoGrid(
            placements = placements,
            gridClass = BentoGridClass.C4,
            innerWidthDp = 358f,
        )
        // c4: 4 columns, 84 dp rows, 12 dp gaps -> column width (358 - 36) / 4.
        val columnWidth = (358f - 3f * 12f) / 4f
        assertEquals(2, measure.tiles.size)
        val (placementA, geometryA) = measure.tiles.getValue("a")
        assertEquals(2 * columnWidth + 12f, geometryA.width)
        assertEquals(2 * 84f + 12f, geometryA.height)
        assertEquals(0f, geometryA.offsetX)
        assertEquals(0f, geometryA.offsetY)
        // 180 dp tall reaches the l class before the m width check.
        assertEquals(BentoTileSizeClass.L, geometryA.sizeClass)
        // The map keys the geometry with the placement it resolved from.
        assertEquals("a", placementA.tileId)
        val geometryB = measure.tiles.getValue("b").second
        assertEquals(2 * (columnWidth + 12f), geometryB.offsetX)
        assertEquals(84f + 12f, geometryB.offsetY)
    }

    @Test
    fun `grid measure sizes and places a full-width tile`() {
        val placements = listOf(
            BentoTilePlacement("np", column = 1, row = 1, columnSpan = 4, rowSpan = 2),
            BentoTilePlacement("last", column = 1, row = 12, columnSpan = 2, rowSpan = 1),
        )
        val measure = measureBentoGrid(placements, BentoGridClass.C4, innerWidthDp = 358f)
        assertEquals(358f, measure.tiles.getValue("np").second.width)
        assertEquals(2 * 84f + 12f, measure.tiles.getValue("np").second.height)
        // The grid is as tall as its bottom-most tile: 12 rows of 84 + 11 gaps.
        assertEquals(12 * 84f + 11 * 12f, measure.totalHeight)
    }

    @Test
    fun `grid measure of an empty table collapses to zero height`() {
        val measure = measureBentoGrid(emptyList(), BentoGridClass.C8, innerWidthDp = 1000f)
        assertEquals(0, measure.tiles.size)
        assertEquals(0f, measure.totalHeight)
    }

    @Test
    fun `grid measure resolves size classes per tile size`() {
        val placements = listOf(
            BentoTilePlacement("tall", 1, 1, 4, 4),
            BentoTilePlacement("wide", 1, 5, 4, 1),
            BentoTilePlacement("narrow", 1, 6, 1, 1),
        )
        val measure = measureBentoGrid(placements, BentoGridClass.C4, innerWidthDp = 358f)
        assertEquals(BentoTileSizeClass.XL, measure.tiles.getValue("tall").second.sizeClass)
        // A full-width c4 row tile is 358 x 84: m by width.
        assertEquals(BentoTileSizeClass.M, measure.tiles.getValue("wide").second.sizeClass)
        // A single c4 column at this width is 80.5 dp: xs below 92.
        assertEquals(BentoTileSizeClass.XS, measure.tiles.getValue("narrow").second.sizeClass)
    }

    @Test
    fun `cover grid fit prefers fewer larger covers`() {
        // For you c4 "Recently added": 326 x 118 dp of covers with 9 albums.
        val fit = fitBentoCoverGrid(
            tileWidthDp = 326f,
            tileHeightDp = 118f,
            itemCount = 9,
        )
        assertEquals(3, fit.columns)
        assertEquals(1, fit.rows)
        assertEquals(102f, fit.coverSize, EPSILON)
        assertEquals(false, fit.captioned)
    }

    @Test
    fun `cover grid fit takes a caption when covers stay at least 100 dp`() {
        val fit = fitBentoCoverGrid(
            tileWidthDp = 358f,
            tileHeightDp = 400f,
            itemCount = 9,
        )
        // Four large captioned covers (2 x 2) beat six smaller ones.
        assertEquals(2, fit.columns)
        assertEquals(2, fit.rows)
        val expected = minOf((358f - 10f) / 2f, (400f - 10f) / 2f - 22f)
        assertEquals(expected, fit.coverSize, EPSILON)
        assertEquals(true, fit.captioned)
    }

    @Test
    fun `cover grid fit falls back to one small cover on tiny tiles`() {
        val fit = fitBentoCoverGrid(tileWidthDp = 30f, tileHeightDp = 30f, itemCount = 5)
        assertEquals(1, fit.columns)
        assertEquals(1, fit.rows)
        assertEquals(30f, fit.coverSize, EPSILON)
        assertEquals(false, fit.captioned)
    }

    @Test
    fun `cover grid fit honors custom gaps and minimums for avatar grids`() {
        // People tiles call the prototype's fit(aw, ah, N, 12, 44, 24, 44):
        // in a tall area, four big named avatars beat six smaller ones.
        val fit = fitBentoCoverGrid(
            tileWidthDp = 358f,
            tileHeightDp = 400f,
            itemCount = 6,
            gapDp = 12f,
            minSizeDp = 44f,
            captionHeightDp = 24f,
            captionMinSizeDp = 44f,
        )
        assertEquals(2, fit.columns)
        assertEquals(2, fit.rows)
        val expected = minOf((358f - 12f) / 2f, (400f - 12f) / 2f - 24f)
        assertEquals(expected, fit.coverSize, EPSILON)
        assertEquals(true, fit.captioned)
    }

    @Test
    fun `the short grid class only applies to short-height windows`() {
        // The short grid class exists only for short-height windows at
        // 600 dp and wider main columns.
        assertEquals(BentoGridClass.SHORT, resolveBentoGridClass(700, 400))
        assertEquals(BentoGridClass.C6, resolveBentoGridClass(700, 800))
    }

    companion object {
        private const val EPSILON = 0.001f

        @JvmStatic
        fun tileSizeClassCases(): Stream<Arguments> = Stream.of(
            // height >= 300 wins first.
            Arguments.of(600f, 300f, BentoTileSizeClass.XL),
            Arguments.of(80f, 300f, BentoTileSizeClass.XL),
            Arguments.of(600f, 299.9f, BentoTileSizeClass.L),
            // height >= 170 second.
            Arguments.of(80f, 170f, BentoTileSizeClass.L),
            Arguments.of(600f, 169.9f, BentoTileSizeClass.M),
            // width >= 200 third.
            Arguments.of(200f, 100f, BentoTileSizeClass.M),
            Arguments.of(199.9f, 100f, BentoTileSizeClass.S),
            // width < 92 last.
            Arguments.of(91.9f, 100f, BentoTileSizeClass.XS),
            Arguments.of(92f, 100f, BentoTileSizeClass.S),
            Arguments.of(120f, 84f, BentoTileSizeClass.S),
        )
    }
}
