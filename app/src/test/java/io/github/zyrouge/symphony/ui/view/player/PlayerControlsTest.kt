package io.github.zyrouge.symphony.ui.view.player

import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PlayerControlsTest {
    private fun formatLine(path: String, samplingRateK: Float?, bitrateK: Long?) =
        bentoAudioFormatLine(
            path = path,
            samplingRateK = samplingRateK,
            bitrateK = bitrateK,
            kHz = { "$it kHz" },
            kbps = { "$it kbps" },
        )

    @Test
    fun `format line lists the type, sampling rate and bitrate`() {
        assertEquals(
            "FLAC · 44.1 kHz · 920 kbps",
            formatLine("/storage/Music/Album/01 Song.flac", 44.1f, 920L),
        )
    }

    @Test
    fun `format line drops a trailing zero from whole sampling rates`() {
        assertEquals("MP3 · 48 kHz · 320 kbps", formatLine("Music/song.mp3", 48f, 320L))
    }

    @Test
    fun `format line skips the parts that are missing`() {
        assertEquals("OGG · 160 kbps", formatLine("Music/song.ogg", null, 160L))
        assertEquals("44.1 kHz", formatLine("Music/song", 44.1f, null))
        assertEquals("M4A", formatLine("Music/v1.2/song.m4a", null, null))
    }

    @Test
    fun `format line is hidden when nothing is known`() {
        assertNull(formatLine("Music/v1.2/song", null, null))
        assertNull(formatLine("", null, null))
    }

    @Test
    fun `transport keeps its full size when it fits`() {
        assertEquals(1f, bentoTransportScale(320f, BentoSizes.PLAY_BUTTON_LG, seekControls = false))
        assertEquals(1f, bentoTransportScale(400f, BentoSizes.PLAY_BUTTON_LG, seekControls = true))
    }

    @Test
    fun `transport with seek buttons shrinks to fit a 360 dp phone`() {
        // 360 dp window minus the 20 dp content inset on each side.
        val scale = bentoTransportScale(320f, BentoSizes.PLAY_BUTTON_LG, seekControls = true)
        val width = (4 * BentoSizes.ICON_BUTTON + 2 * BentoSizes.PREV_NEXT_BUTTON + BentoSizes.PLAY_BUTTON_LG) * scale
        assertEquals(320f, width, 0.01f)
    }

    @Test
    fun `transport with seek buttons shrinks to fit the floating window`() {
        // 400 dp floating window minus the 20 dp content inset on each side.
        val scale = bentoTransportScale(360f, BentoSizes.PLAY_BUTTON_LG, seekControls = true)
        val width = (4 * BentoSizes.ICON_BUTTON + 2 * BentoSizes.PREV_NEXT_BUTTON + BentoSizes.PLAY_BUTTON_LG) * scale
        assertEquals(360f, width, 0.01f)
    }
}
