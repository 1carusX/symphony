package io.github.zyrouge.symphony.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * The content scale setting as a factor, for dp sizes handed to a window
 * component that measures them in its own window (a menu's fixed width).
 */
@Composable
fun bentoAppScale(): Float {
    val density = LocalBentoAppDensity.current ?: return 1f
    return density.density / LocalContext.current.resources.displayMetrics.density
}
