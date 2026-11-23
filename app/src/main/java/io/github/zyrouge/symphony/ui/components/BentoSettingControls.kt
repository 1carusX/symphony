package io.github.zyrouge.symphony.ui.components

import kotlin.math.roundToInt

fun formatSettingSecs(value: Float): String {
    val rounded = (value * 2).roundToInt() / 2f
    return if (rounded % 1f == 0f) "${rounded.toInt()} s" else "$rounded s"
}
