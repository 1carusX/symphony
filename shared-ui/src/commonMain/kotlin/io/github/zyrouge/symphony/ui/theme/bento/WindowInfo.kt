package io.github.zyrouge.symphony.ui.theme.bento

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalWindowInfo
import kotlin.math.roundToInt

/**
 * Resolves the current [BentoWindowClass] from the window container size
 * (`LocalWindowInfo.containerDpSize`, the window bounds in dp, including
 * system bars). Rounded to the nearest dp so a window measuring 599.9 dp
 * stays compact while 600.1 dp is medium.
 */
@Composable
fun rememberBentoWindowClass(): BentoWindowClass {
    val windowInfo = LocalWindowInfo.current
    val widthDp = windowInfo.containerDpSize.width.value
    val heightDp = windowInfo.containerDpSize.height.value
    return remember(widthDp, heightDp) {
        resolveBentoWindowClass(widthDp.roundToInt(), heightDp.roundToInt())
    }
}

/**
 * Resolves the current [BentoGridClass] from the main column width (window
 * minus navigation) and the window height.
 */
@Composable
fun rememberBentoGridClass(mainColumnWidthDp: Int): BentoGridClass {
    val windowInfo = LocalWindowInfo.current
    val heightDp = windowInfo.containerDpSize.height.value
    return remember(mainColumnWidthDp, heightDp) {
        resolveBentoGridClass(mainColumnWidthDp, heightDp.roundToInt())
    }
}
