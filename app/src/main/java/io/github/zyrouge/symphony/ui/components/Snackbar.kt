package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

@Composable
fun AdaptiveSnackbar(snackbarData: SnackbarData) {
    val colors = LocalBentoColors.current
    Snackbar(
        snackbarData = snackbarData,
        shape = RoundedCornerShape(BentoRadii.TILE_SMALL.dp),
        containerColor = colors.float,
        contentColor = colors.fg,
        actionColor = colors.accent,
        actionContentColor = colors.onAccent,
        dismissActionContentColor = colors.sub,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
