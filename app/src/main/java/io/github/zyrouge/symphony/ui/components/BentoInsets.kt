package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Bottom padding that clears the system gesture bar. MIUI reports the gesture
 * navigation inset as zero on some devices even with edge-to-edge enabled, so
 * the larger of the navigation-bar and gesture insets is used, plus a small
 * margin.
 */
@Composable
fun Modifier.bentoGestureBarPadding(extra: Dp = 8.dp): Modifier {
    val navigation = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val gestures = WindowInsets.systemGestures.asPaddingValues().calculateBottomPadding()
    return padding(bottom = maxOf(navigation, gestures) + extra)
}
