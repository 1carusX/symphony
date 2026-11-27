package io.github.zyrouge.symphony.ui.view.player

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class SleepTimerBadgeTest {
    private val endsAt = 10_000_000L

    @Test
    fun `badge rounds the minutes left up`() {
        assertEquals(15, bentoSleepTimerMinutesLeft(endsAt, endsAt - 15 * 60_000L))
        assertEquals(15, bentoSleepTimerMinutesLeft(endsAt, endsAt - 14 * 60_000L - 1L))
        assertEquals(1, bentoSleepTimerMinutesLeft(endsAt, endsAt - 1L))
        assertEquals(0, bentoSleepTimerMinutesLeft(endsAt, endsAt))
    }

    @Test
    fun `next refresh lands on the next minute boundary`() {
        assertEquals(60_000L, bentoSleepTimerNextMinuteDelayMs(endsAt, endsAt - 15 * 60_000L))
        assertEquals(1L, bentoSleepTimerNextMinuteDelayMs(endsAt, endsAt - 14 * 60_000L - 1L))
        assertEquals(25_000L, bentoSleepTimerNextMinuteDelayMs(endsAt, endsAt - 85_000L))
        assertEquals(1L, bentoSleepTimerNextMinuteDelayMs(endsAt, endsAt - 1L))
    }

    @Test
    fun `the shown minute changes exactly after the delay`() {
        var now = endsAt - 30 * 60_000L + 12_345L
        while (true) {
            val shown = bentoSleepTimerMinutesLeft(endsAt, now)
            val delay = bentoSleepTimerNextMinuteDelayMs(endsAt, now) ?: break
            assertEquals(shown, bentoSleepTimerMinutesLeft(endsAt, now + delay - 1L))
            assertEquals(shown - 1, bentoSleepTimerMinutesLeft(endsAt, now + delay))
            now += delay
        }
        assertEquals(endsAt, now)
    }

    @Test
    fun `no refresh after the timer ends`() {
        assertNull(bentoSleepTimerNextMinuteDelayMs(endsAt, endsAt))
        assertNull(bentoSleepTimerNextMinuteDelayMs(endsAt, endsAt + 5_000L))
    }
}
