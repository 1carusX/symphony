package io.github.zyrouge.symphony.ui.components

import io.github.zyrouge.symphony.ui.contracts.UiLoopMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PlayerModeTest {
    @Test
    fun `play mode cycles repeat all, repeat one, shuffle`() {
        assertEquals(PlayerMode.REPEAT_ALL, nextPlayerMode(PlayerMode.REPEAT_OFF))
        assertEquals(PlayerMode.REPEAT_ONE, nextPlayerMode(PlayerMode.REPEAT_ALL))
        assertEquals(PlayerMode.SHUFFLE, nextPlayerMode(PlayerMode.REPEAT_ONE))
        assertEquals(PlayerMode.REPEAT_ALL, nextPlayerMode(PlayerMode.SHUFFLE))
    }

    @Test
    fun `shuffle keeps repeating the queue`() {
        assertEquals(UiLoopMode.QUEUE, PlayerMode.SHUFFLE.toQueueLoopMode())
        assertTrue(PlayerMode.SHUFFLE.isShuffle())
    }

    @Test
    fun `every mode the button sets is shown as that mode`() {
        PlayerMode.entries.forEach { mode ->
            val next = nextPlayerMode(mode)
            assertEquals(next, resolvePlayerMode(next.toQueueLoopMode(), next.isShuffle()))
        }
    }

    @Test
    fun `shuffle shows as shuffle whatever the loop mode`() {
        UiLoopMode.entries.forEach { loopMode ->
            assertEquals(PlayerMode.SHUFFLE, resolvePlayerMode(loopMode, shuffle = true))
        }
        assertEquals(PlayerMode.REPEAT_OFF, resolvePlayerMode(UiLoopMode.NONE, shuffle = false))
    }
}
