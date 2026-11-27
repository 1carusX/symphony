package io.github.zyrouge.symphony.ui.components

import androidx.compose.ui.graphics.Color

/** [this] color with each channel at 55 %, for cover gradients. */
fun Color.darkened(): Color = Color(red * 0.55f, green * 0.55f, blue * 0.55f, alpha)
