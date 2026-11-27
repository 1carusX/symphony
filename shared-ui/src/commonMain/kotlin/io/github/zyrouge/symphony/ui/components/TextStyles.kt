package io.github.zyrouge.symphony.ui.components

import androidx.compose.ui.text.TextStyle

/** Durations, times, counts and badges use tabular figures (DESIGN.md 3.2). */
fun bentoTabularStyle(style: TextStyle) = style.copy(fontFeatureSettings = "tnum")
