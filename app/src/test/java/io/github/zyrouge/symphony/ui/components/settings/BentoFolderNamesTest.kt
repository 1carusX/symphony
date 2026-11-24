package io.github.zyrouge.symphony.ui.components.settings

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BentoFolderNamesTest {
    @Test
    fun `internal storage paths read as folders`() {
        assertEquals("Internal storage › Music", bentoReadableFolderPath("primary:Music"))
        assertEquals("Internal storage › Music › Live", bentoReadableFolderPath("primary:Music/Live"))
    }

    @Test
    fun `the volume root alone is the volume name`() {
        assertEquals("Internal storage", bentoReadableFolderPath("primary:"))
    }

    @Test
    fun `other volumes are named by id`() {
        assertEquals("Storage 1A2B-3C4D › Albums", bentoReadableFolderPath("1A2B-3C4D:Albums"))
    }

    @Test
    fun `paths without a volume keep their folders`() {
        assertEquals("Music › Live", bentoReadableFolderPath("Music/Live"))
    }
}
