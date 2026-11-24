package io.github.zyrouge.symphony.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.zyrouge.symphony.ui.theme.BentoAppDensity

/**
 * A [Dialog] laid out at the app's font and content scale. A dialog window
 * resets LocalDensity to its own, so the content restores the app's. Compose
 * also rebuilds a dialog's window whenever LocalDensity changes where it is
 * called, and the theme scales LocalDensity with those settings, so the window
 * is handed its own unscaled density: moving the Appearance sliders then
 * resizes the open sheet instead of flashing a new window in.
 */
@Composable
fun BentoAppDialog(
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalDensity provides Density(LocalContext.current)) {
        Dialog(onDismissRequest = onDismissRequest, properties = properties) {
            BentoAppDensity(content)
        }
    }
}
