package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

@Composable
fun BentoMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalBentoColors.current
    val scheme = MaterialTheme.colorScheme
    MaterialTheme(
        colorScheme = scheme.copy(
            surface = colors.float,
            surfaceContainer = colors.float,
            surfaceContainerHigh = colors.float,
            primary = colors.accent,
            onPrimary = colors.onAccent,
            onSurface = colors.fg,
        ),
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            shape = RoundedCornerShape(BentoRadii.TILE_SMALL.dp),
        ) {
            content()
        }
    }
}
