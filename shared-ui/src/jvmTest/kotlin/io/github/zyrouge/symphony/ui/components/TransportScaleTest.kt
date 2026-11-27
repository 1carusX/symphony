package io.github.zyrouge.symphony.ui.components

import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TransportScaleTest {
    @Test
    fun `transport keeps its full size when it fits`() {
        assertEquals(1f, bentoTransportScale(320f, BentoSizes.PLAY_BUTTON_LG, seekControls = false))
        assertEquals(1f, bentoTransportScale(400f, BentoSizes.PLAY_BUTTON_LG, seekControls = true))
    }

    @Test
    fun `transport with seek buttons shrinks to fit a 360 dp phone`() {
        // 360 dp window minus the 20 dp content inset on each side.
        val scale = bentoTransportScale(320f, BentoSizes.PLAY_BUTTON_LG, seekControls = true)
        val width =
            (4 * BentoSizes.ICON_BUTTON + 2 * BentoSizes.PREV_NEXT_BUTTON +
                BentoSizes.PLAY_BUTTON_LG) * scale
        assertEquals(320f, width, 0.01f)
    }

    @Test
    fun `transport with seek buttons shrinks to fit the floating window`() {
        // 400 dp floating window minus the 20 dp content inset on each side.
        val scale = bentoTransportScale(360f, BentoSizes.PLAY_BUTTON_LG, seekControls = true)
        val width =
            (4 * BentoSizes.ICON_BUTTON + 2 * BentoSizes.PREV_NEXT_BUTTON +
                BentoSizes.PLAY_BUTTON_LG) * scale
        assertEquals(360f, width, 0.01f)
    }
}
