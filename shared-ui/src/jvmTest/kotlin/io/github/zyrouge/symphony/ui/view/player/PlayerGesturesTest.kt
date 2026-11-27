package io.github.zyrouge.symphony.ui.view.player

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PlayerGesturesTest {
    @Test
    fun `an upward flick opens the medium level`() {
        assertEquals(
            BentoPlayerLevel.MEDIUM,
            resolveBentoPhonePlayerRelease(
                currentTopDp = 700f,
                velocityDpPerMs = -0.6f,
                startedAt = BentoPlayerLevel.SMALL,
                mediumTopDp = 80f,
                smallTopDp = 700f,
                closedTopDp = 900f,
            ),
        )
    }

    @Test
    fun `a downward flick from medium stops at small when near it`() {
        assertEquals(
            BentoPlayerLevel.SMALL,
            resolveBentoPhonePlayerRelease(
                currentTopDp = 660f,
                velocityDpPerMs = 0.7f,
                startedAt = BentoPlayerLevel.MEDIUM,
                mediumTopDp = 80f,
                smallTopDp = 700f,
                closedTopDp = 900f,
            ),
        )
    }

    @Test
    fun `a downward flick away from small closes the sheet`() {
        assertNull(
            resolveBentoPhonePlayerRelease(
                currentTopDp = 800f,
                velocityDpPerMs = 0.7f,
                startedAt = BentoPlayerLevel.MEDIUM,
                mediumTopDp = 80f,
                smallTopDp = 700f,
                closedTopDp = 900f,
            ),
        )
    }

    @Test
    fun `a slow release snaps to the nearest stop`() {
        assertEquals(
            BentoPlayerLevel.MEDIUM,
            resolveBentoPhonePlayerRelease(
                currentTopDp = 120f,
                velocityDpPerMs = 0.1f,
                startedAt = BentoPlayerLevel.SMALL,
                mediumTopDp = 80f,
                smallTopDp = 700f,
                closedTopDp = 900f,
            ),
        )
        assertEquals(
            BentoPlayerLevel.SMALL,
            resolveBentoPhonePlayerRelease(
                currentTopDp = 600f,
                velocityDpPerMs = -0.1f,
                startedAt = BentoPlayerLevel.SMALL,
                mediumTopDp = 80f,
                smallTopDp = 700f,
                closedTopDp = 900f,
            ),
        )
        assertNull(
            resolveBentoPhonePlayerRelease(
                currentTopDp = 880f,
                velocityDpPerMs = 0.1f,
                startedAt = BentoPlayerLevel.SMALL,
                mediumTopDp = 80f,
                smallTopDp = 700f,
                closedTopDp = 900f,
            ),
        )
    }

    @Test
    fun `the sleep timer cycles off to fifteen, thirty, sixty and back`() {
        assertEquals(15, nextBentoSleepTimerMinutes(null))
        assertEquals(30, nextBentoSleepTimerMinutes(15L * 60_000L))
        assertEquals(60, nextBentoSleepTimerMinutes(30L * 60_000L))
        assertNull(nextBentoSleepTimerMinutes(60L * 60_000L))
    }
}
