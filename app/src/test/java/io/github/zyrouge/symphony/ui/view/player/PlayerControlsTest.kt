package io.github.zyrouge.symphony.ui.view.player

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
}
