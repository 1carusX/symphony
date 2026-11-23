package io.github.zyrouge.symphony.ui.components

import io.github.zyrouge.symphony.i18n.Strings
import kotlin.math.roundToInt

/** Seconds rounded to a half, "1.5 s"; with [t] in the app language. */
fun formatSettingSecs(value: Float, t: Strings? = null): String {
    val rounded = (value * 2).roundToInt() / 2f
    val number = if (rounded % 1f == 0f) rounded.toInt().toString() else rounded.toString()
    return t?.DurationXSeconds(number) ?: "$number s"
}
