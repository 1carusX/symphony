package io.github.zyrouge.symphony.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density

/**
 * The density the app lays out with: the window's, scaled by the font and
 * content scale settings (SymphonyTheme). Dialog, sheet and popup windows reset
 * LocalDensity to their own window's, so their content restores this one with
 * [BentoAppDensity].
 */
val LocalBentoAppDensity = compositionLocalOf<Density?> { null }

/** Lays [content] out at the app's scale; use it at the root of a dialog or popup window. */
@Composable
fun BentoAppDensity(content: @Composable () -> Unit) {
    val density = LocalBentoAppDensity.current ?: LocalDensity.current
    CompositionLocalProvider(LocalDensity provides density, content = content)
}

/**
 * The content scale setting as a factor, for dp sizes handed to a window
 * component that measures them in its own window (a menu's fixed width).
 */
@Composable
fun bentoAppScale(): Float {
    val density = LocalBentoAppDensity.current ?: return 1f
    return density.density / LocalContext.current.resources.displayMetrics.density
}
