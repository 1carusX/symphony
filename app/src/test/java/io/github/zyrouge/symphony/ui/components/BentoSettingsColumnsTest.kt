package io.github.zyrouge.symphony.ui.components

import androidx.compose.ui.unit.dp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BentoSettingsColumnsTest {
    @Test
    fun `column count follows the prototype breakpoints`() {
        assertEquals(1, bentoSettingsColumnCount(411.dp))
        assertEquals(1, bentoSettingsColumnCount(639.dp))
        assertEquals(2, bentoSettingsColumnCount(640.dp))
        assertEquals(2, bentoSettingsColumnCount(1099.dp))
        assertEquals(3, bentoSettingsColumnCount(1100.dp))
    }

    @Test
    fun `one column keeps every group in it`() {
        assertEquals(listOf(0, 0, 0), bentoSettingsColumnRuns(listOf(100, 200, 50), 1, 16))
    }

    @Test
    fun `never more columns than groups`() {
        assertEquals(listOf(0), bentoSettingsColumnRuns(listOf(120), 3, 16))
        assertEquals(listOf(0, 1), bentoSettingsColumnRuns(listOf(120, 80), 3, 16))
    }

    @Test
    fun `groups keep their order and balance the tallest column`() {
        // 300 | 100 + 100 + 100 (332 with gaps) beats 300 + 100 | 100 + 100.
        assertEquals(listOf(0, 1, 1, 1), bentoSettingsColumnRuns(listOf(300, 100, 100, 100), 2, 16))
        // Even heights split down the middle.
        assertEquals(listOf(0, 0, 1, 1), bentoSettingsColumnRuns(listOf(100, 100, 100, 100), 2, 16))
    }

    @Test
    fun `empty pages have no runs`() {
        assertEquals(emptyList<Int>(), bentoSettingsColumnRuns(emptyList(), 2, 16))
    }
}
