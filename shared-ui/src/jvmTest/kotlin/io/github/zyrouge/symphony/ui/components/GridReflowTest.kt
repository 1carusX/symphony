package io.github.zyrouge.symphony.ui.components

import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BentoGridReflowTest {
    @Test
    fun `wide tiles take the full width and single-column tiles pair up`() {
        val c4 = listOf(
            BentoTilePlacement("cover", 1, 1, 2, 2),
            BentoTilePlacement("info", 3, 1, 2, 2),
            BentoTilePlacement("play", 1, 3, 2, 1),
            BentoTilePlacement("shuffle", 3, 3, 1, 1),
            BentoTilePlacement("queue", 4, 3, 1, 1),
            BentoTilePlacement("tracks", 1, 4, 4, 3),
        )
        val reflowed = reflowBentoPlacements(c4).associateBy { it.tileId }
        assertEquals(BentoTilePlacement("cover", 1, 1, 2, 2), reflowed["cover"])
        assertEquals(BentoTilePlacement("info", 1, 3, 2, 2), reflowed["info"])
        assertEquals(BentoTilePlacement("play", 1, 5, 2, 1), reflowed["play"])
        assertEquals(BentoTilePlacement("shuffle", 1, 6, 1, 1), reflowed["shuffle"])
        assertEquals(BentoTilePlacement("queue", 2, 6, 1, 1), reflowed["queue"])
        assertEquals(BentoTilePlacement("tracks", 1, 7, 2, 3), reflowed["tracks"])
    }

    @Test
    fun `a single-column tile without a partner keeps its row to itself`() {
        val reflowed = reflowBentoPlacements(
            listOf(
                BentoTilePlacement("a", 1, 1, 1, 2),
                BentoTilePlacement("b", 2, 1, 3, 1),
            ),
        ).associateBy { it.tileId }
        assertEquals(BentoTilePlacement("a", 1, 1, 1, 2), reflowed["a"])
        assertEquals(BentoTilePlacement("b", 1, 3, 2, 1), reflowed["b"])
    }

    @Test
    fun `reflowed tiles span two columns of the inner width and rows scale`() {
        val measure = measureBentoGrid(
            listOf(BentoTilePlacement("a", 1, 1, 4, 1), BentoTilePlacement("b", 1, 2, 1, 1)),
            BentoGridClass.C4,
            innerWidthDp = 300f,
            rowScale = 1.5f,
            reflow = true,
        )
        val (_, a) = measure.tiles.getValue("a")
        val (_, b) = measure.tiles.getValue("b")
        assertEquals(300f, a.width, 0.01f)
        assertEquals(BentoGridClass.C4.rowHeightDp * 1.5f, a.height, 0.01f)
        assertEquals((300f - BentoGridClass.C4.gapDp) / 2f, b.width, 0.01f)
    }

    @Test
    fun `narrow windows count the font scale`() {
        assertFalse(bentoTooNarrowForText(379f, 1f))
        assertTrue(bentoTooNarrowForText(242f, 1f))
        assertTrue(bentoTooNarrowForText(379f, 1.6f))
    }
}
