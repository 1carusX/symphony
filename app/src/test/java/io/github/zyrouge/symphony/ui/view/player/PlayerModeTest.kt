package io.github.zyrouge.symphony.ui.view.player

import io.github.zyrouge.symphony.services.radio.RadioQueue
import org.junit.jupiter.api.Assertions.assertEquals
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
        assertEquals(RadioQueue.LoopMode.Queue, PlayerMode.SHUFFLE.loopMode)
        assertEquals(true, PlayerMode.SHUFFLE.shuffle)
    }

    @Test
    fun `every mode the button sets is shown as that mode`() {
        PlayerMode.entries.forEach { mode ->
            val next = nextPlayerMode(mode)
            assertEquals(next, resolvePlayerMode(next.loopMode, next.shuffle))
        }
    }

    @Test
    fun `shuffle shows as shuffle whatever the loop mode`() {
        RadioQueue.LoopMode.entries.forEach { loopMode ->
            assertEquals(PlayerMode.SHUFFLE, resolvePlayerMode(loopMode, shuffle = true))
        }
        assertEquals(PlayerMode.REPEAT_OFF, resolvePlayerMode(RadioQueue.LoopMode.None, shuffle = false))
    }
}
