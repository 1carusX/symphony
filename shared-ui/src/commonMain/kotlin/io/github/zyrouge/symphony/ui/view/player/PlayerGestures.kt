package io.github.zyrouge.symphony.ui.view.player

import kotlin.math.abs
import kotlin.math.ceil

/** The two non-fullscreen player levels (DESIGN.md 6.2). */
enum class BentoPlayerLevel {
    SMALL,
    MEDIUM,
}

/**
 * Resolves a phone-sheet release (DESIGN.md 6.5). Positions and velocity are
 * in dp and dp/ms; a null result closes the sheet. Slow releases choose the
 * nearest of medium, small and closed, keeping that order on an exact tie.
 */
fun resolveBentoPhonePlayerRelease(
    currentTopDp: Float,
    velocityDpPerMs: Float,
    startedAt: BentoPlayerLevel,
    mediumTopDp: Float,
    smallTopDp: Float,
    closedTopDp: Float,
): BentoPlayerLevel? = when {
    velocityDpPerMs < -0.45f -> BentoPlayerLevel.MEDIUM
    velocityDpPerMs > 0.45f -> when {
        startedAt == BentoPlayerLevel.MEDIUM && currentTopDp <= smallTopDp + 60f ->
            BentoPlayerLevel.SMALL

        else -> null
    }

    else -> {
        val stops = listOf(
            BentoPlayerLevel.MEDIUM to mediumTopDp,
            BentoPlayerLevel.SMALL to smallTopDp,
            null to closedTopDp,
        )
        var target = stops.first()
        stops.drop(1).forEach { stop ->
            if (abs(currentTopDp - stop.second) < abs(currentTopDp - target.second)) {
                target = stop
            }
        }
        target.first
    }
}

/** Sleep-timer transport cycle: off -> 15 -> 30 -> 60 minutes -> off. */
fun nextBentoSleepTimerMinutes(durationMs: Long?): Int? = when (durationMs) {
    null -> 15
    15L * 60_000L -> 30
    30L * 60_000L -> 60
    else -> null
}

/** Whole minutes left, rounded up so a live timer never displays zero early. */
fun bentoSleepTimerMinutesLeft(endsAtMs: Long, nowMs: Long): Int =
    ceil((endsAtMs - nowMs).coerceAtLeast(0L) / 60_000.0).toInt()

/**
 * Milliseconds until [bentoSleepTimerMinutesLeft] next changes, or null once
 * the timer has ended and the value stays at zero.
 */
fun bentoSleepTimerNextMinuteDelayMs(endsAtMs: Long, nowMs: Long): Long? {
    val leftMs = endsAtMs - nowMs
    if (leftMs <= 0L) return null
    return (leftMs - 1L) % 60_000L + 1L
}
