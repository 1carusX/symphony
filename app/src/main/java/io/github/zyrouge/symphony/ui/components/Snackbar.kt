package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass
import io.github.zyrouge.symphony.ui.view.player.LocalBentoPlayerHost

/** A toast (DESIGN.md 4.6): inverted colors, `fg` fill with `bg` text, radius 16. */
@Composable
fun AdaptiveSnackbar(snackbarData: SnackbarData) {
    val colors = LocalBentoColors.current
    Snackbar(
        snackbarData = snackbarData,
        shape = RoundedCornerShape(16.dp),
        containerColor = colors.fg,
        contentColor = colors.bg,
        actionColor = colors.bg,
        actionContentColor = colors.bg,
        dismissActionContentColor = colors.bg,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

/**
 * Places [AdaptiveSnackbar] toasts (DESIGN.md 4.6): 100 dp above the bottom
 * edge, clear of the gesture bar; while the phone player sheet is open they
 * move to the top so they do not cover its controls. Fills its parent.
 */
@Composable
fun BentoSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    val sheetOpen = LocalBentoPlayerHost.current.open &&
        rememberBentoWindowClass() == BentoWindowClass.COMPACT
    val bottomInset = bentoSystemBottomInset()
    Box(modifier = modifier.fillMaxSize()) {
        SnackbarHost(
            hostState,
            modifier = when {
                sheetOpen -> Modifier
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 12.dp)

                else -> Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = bottomInset + 100.dp)
            },
        ) {
            AdaptiveSnackbar(it)
        }
    }
}
