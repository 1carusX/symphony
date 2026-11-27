package io.github.zyrouge.symphony.ui.components

import io.github.zyrouge.symphony.i18n.Strings
import kotlin.math.roundToInt

/**
 * Long durations in the tiles (prototype fmtLong): "1 h 23 min", "45 min".
 * With [t], in the app language; without it (tests), in English.
 */
fun formatBentoDurationMinutes(totalMs: Long, t: Strings? = null): String {
    val totalSeconds = totalMs / 1000
    val minutes = (totalMs / 60_000.0).roundToInt()
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        totalSeconds < 60 -> t?.DurationXSeconds(totalSeconds.toString()) ?: "$totalSeconds s"
        hours >= 1 -> t?.DurationXHoursYMinutes(hours.toString(), rest.toString()) ?: "$hours h $rest min"
        else -> t?.DurationXMinutes(minutes.toString()) ?: "$minutes min"
    }
}
