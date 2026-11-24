package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * A floating surface (DESIGN.md 3.4): both layers of the scheme's token
 * shadow with their blur, spread and offset, the [fill], and an always-on
 * 1 dp `edge` ring. The capsule, the floating player, menus and dialogs share
 * it, so they read as one family.
 */
@Composable
fun Modifier.bentoFloatingSurface(
    shape: Shape,
    fill: Color = LocalBentoColors.current.high,
): Modifier {
    val colors = LocalBentoColors.current
    return colors.shadow.layers
        .fold(this) { modifier, layer ->
            modifier.dropShadow(
                shape = shape,
                shadow = Shadow(
                    radius = layer.blur.dp,
                    color = layer.color,
                    spread = layer.spread.dp,
                    offset = DpOffset(layer.offsetX.dp, layer.offsetY.dp),
                ),
            )
        }
        .clip(shape)
        .background(fill)
        .border(1.dp, colors.edge, shape)
}
