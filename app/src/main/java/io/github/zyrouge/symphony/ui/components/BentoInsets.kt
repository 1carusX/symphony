package io.github.zyrouge.symphony.ui.components

import android.os.Build
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.mandatorySystemGestures
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.view.player.LocalBentoCapsuleSpace

/**
 * Height of the system's bottom bar or gesture area. MIUI and HyperOS report
 * no systemGestures inset at all (checked on a MIX Fold 2, HyperOS 3), and
 * some versions report a zero navigation bar in full-screen gesture mode, so
 * the mandatory gesture area, which the home gesture always reserves, is
 * considered too.
 */
@Composable
fun bentoSystemBottomInset(): Dp = resolveBentoSystemBottomInset(
    reported = maxOf(
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
        WindowInsets.mandatorySystemGestures.asPaddingValues().calculateBottomPadding(),
        WindowInsets.systemGestures.asPaddingValues().calculateBottomPadding(),
    ),
    sdkInt = Build.VERSION.SDK_INT,
)

/** Bottom space kept on Android 9 when the system reports none (see [resolveBentoSystemBottomInset]). */
val BentoLegacyGestureFallback = 16.dp

/**
 * Android 9 has no gesture insets, and the full-screen gestures vendors built
 * for it (MIUI 10/11, EMUI 9, ColorOS 6) report no navigation bar either, so a
 * reported 0 there gets a small fallback that clears their swipe area. From
 * Android 10 on, a reported 0 is trusted.
 */
fun resolveBentoSystemBottomInset(reported: Dp, sdkInt: Int): Dp = when {
    reported == 0.dp && sdkInt < Build.VERSION_CODES.Q -> BentoLegacyGestureFallback
    else -> reported
}

/**
 * Bottom space scroll content needs: it clears the system gesture bar and,
 * while a song is loaded, the floating capsule, plus a small margin.
 */
@Composable
fun bentoBottomInset(extra: Dp = 8.dp): Dp =
    bentoSystemBottomInset() + extra + LocalBentoCapsuleSpace.current

/** Bottom padding of [bentoBottomInset], for the last child of scroll content. */
@Composable
fun Modifier.bentoGestureBarPadding(extra: Dp = 8.dp): Modifier =
    padding(bottom = bentoBottomInset(extra))
