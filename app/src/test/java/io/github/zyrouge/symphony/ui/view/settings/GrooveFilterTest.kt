package io.github.zyrouge.symphony.ui.view.settings

import io.github.zyrouge.symphony.ui.components.bentoNearestStop
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class GrooveFilterTest {
    private fun keeps(path: String, pattern: String) =
        bentoFilterKeeps(path, Regex(pattern, RegexOption.IGNORE_CASE))

    @Test
    fun `everything keeps every song`() {
        assertTrue(keeps("primary:Music/Live/a.mp3", ".*"))
    }

    @Test
    fun `a folder that fails drops the songs under it`() {
        assertFalse(keeps("primary:Recordings/2024/memo.m4a", "^(?!.*Recordings)"))
        assertTrue(keeps("primary:Music/a.mp3", "^(?!.*recordings)"))
    }

    @Test
    fun `only folders are checked, not the file name`() {
        // A file-name pattern fails on the folders, so the scan keeps nothing.
        assertFalse(keeps("primary:Music/a.flac", "\\.flac$"))
        // A song directly under the root only needs the root to match.
        assertTrue(keeps("primary:WhatsApp Audio.mp3", "^(?!.*(WhatsApp|Telegram))"))
    }

    @Test
    fun `stops snap to the nearest value`() {
        val stops = listOf(0.85f, 1f, 1.15f, 1.3f, 1.5f)
        assertEquals(1, bentoNearestStop(stops, 1f))
        assertEquals(2, bentoNearestStop(stops, 1.2f))
        assertEquals(4, bentoNearestStop(stops, 2f))
        assertEquals(0, bentoNearestStop(stops, 0.5f))
    }
}
