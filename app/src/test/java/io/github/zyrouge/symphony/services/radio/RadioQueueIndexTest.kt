package io.github.zyrouge.symphony.services.radio

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RadioQueueIndexTest {
    private val original = listOf("a", "b", "c", "d")

    @Test
    fun `unshuffled queues share indices`() {
        assertEquals(2, radioOriginalQueueIndex(original, original, 2, shuffled = false))
    }

    @Test
    fun `shuffled queues find the same song`() {
        val shuffled = listOf("c", "a", "d", "b")
        assertEquals(1, radioOriginalQueueIndex(original, shuffled, 3, shuffled = true))
        assertEquals(2, radioOriginalQueueIndex(original, shuffled, 0, shuffled = true))
    }

    @Test
    fun `repeated songs map copy to copy`() {
        val withRepeats = listOf("a", "b", "a", "c")
        val shuffled = listOf("c", "a", "b", "a")
        assertEquals(0, radioOriginalQueueIndex(withRepeats, shuffled, 1, shuffled = true))
        assertEquals(2, radioOriginalQueueIndex(withRepeats, shuffled, 3, shuffled = true))
    }
}
