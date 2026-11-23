package io.github.zyrouge.symphony.ui.view.player

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SpeedPitchDialogTest {
    @Test
    fun `playback rates drop trailing zeros`() {
        assertEquals("1×", formatPlaybackRate(1f))
        assertEquals("0.5×", formatPlaybackRate(0.5f))
        assertEquals("1.25×", formatPlaybackRate(1.25f))
        assertEquals("2×", formatPlaybackRate(2f))
    }

    @Test
    fun `slider values round to two decimals`() {
        assertEquals("1.05×", formatPlaybackRate(1.0500001f))
        assertEquals("0.95×", formatPlaybackRate(0.94999998f))
    }
}
