package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass

/**
 * Player state shared by every page. BaseView hosts the capsule and the player
 * above the navigation graph; pages open the player and report what affects
 * the capsule (the For you Now playing tile, a navigation rail).
 */
@Stable
class BentoPlayerHostState(open: Boolean = false) {
    /** Whether the small/medium player is shown. */
    var open by mutableStateOf(open)

    /** The For you Now playing tile is visible, so the capsule hides (DESIGN.md 4.2). */
    var capsuleSuppressed by mutableStateOf(false)

    /** The current page shows a navigation rail, so the capsule centres in the main column. */
    var hasRail by mutableStateOf(false)

    companion object {
        val Saver = Saver<BentoPlayerHostState, Boolean>(
            save = { it.open },
            restore = { BentoPlayerHostState(open = it) },
        )
    }
}

val LocalBentoPlayerHost = staticCompositionLocalOf<BentoPlayerHostState> {
    error("BentoPlayerHostState is not provided; BaseView provides it")
}

/**
 * Space the floating capsule takes above the navigation bar, including the gap
 * above it; 0 while no song is loaded. Scroll content adds it as bottom padding
 * so the last item can scroll clear of the capsule.
 */
val LocalBentoCapsuleSpace = compositionLocalOf { 0.dp }

/** Capsule bottom inset + height + a 12 dp gap, per window class (DESIGN.md 4.2). */
fun bentoCapsuleSpace(windowClass: BentoWindowClass): Dp = when (windowClass) {
    BentoWindowClass.SHORT_HEIGHT ->
        BentoSizes.CAPSULE_BOTTOM_INSET_SHORT + BentoSizes.CAPSULE_HEIGHT_SHORT

    BentoWindowClass.COMPACT -> BentoSizes.CAPSULE_BOTTOM_INSET_PHONE + BentoSizes.CAPSULE_HEIGHT
    else -> BentoSizes.CAPSULE_BOTTOM_INSET_OTHER + BentoSizes.CAPSULE_HEIGHT
}.plus(12).dp
