package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.zyrouge.symphony.ui.theme.LocalStrings
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.BentoAppDensity
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/** Window options a bento dialog or sheet needs from its platform host. */
@Immutable
data class BentoDialogWindow(
    val dismissOnBackPress: Boolean = true,
    val dismissOnClickOutside: Boolean = true,
    val usePlatformDefaultWidth: Boolean = true,
    /** Full-bleed windows lay out edge to edge and dim through their own scrim. */
    val edgeToEdge: Boolean = false,
)

/** Routes the platform back gesture to the open dialog; a no-op by default. */
val LocalBentoBackHandler = compositionLocalOf<
    @Composable (enabled: Boolean, onBack: () -> Unit) -> Unit,
    > {
    { _, _ -> }
}

/**
 * Creates the window a bento dialog lives in. Android injects its app-density
 * dialog window; other platforms get the default dialog.
 */
val LocalBentoDialogHost = compositionLocalOf<
    @Composable (
        window: BentoDialogWindow,
        onDismissRequest: () -> Unit,
        content: @Composable () -> Unit,
    ) -> Unit,
    > {
    { window, onDismissRequest, content ->
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                dismissOnBackPress = window.dismissOnBackPress,
                dismissOnClickOutside = window.dismissOnClickOutside,
                usePlatformDefaultWidth = window.usePlatformDefaultWidth,
            ),
        ) {
            BentoAppDensity(content)
        }
    }
}

/**
 * Popup used for settings that used to live on their own page: a floating
 * bento surface with a title, the setting content, and a done action. Changes
 * apply immediately, so the popup only closes.
 */
@Composable
fun BentoDialog(
    title: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    actions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val strings = LocalStrings.current
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val dialogHost = LocalBentoDialogHost.current
    dialogHost(BentoDialogWindow(), onDismiss) {
        Column(
            modifier = modifier
                .widthIn(max = 560.dp)
                .bentoFloatingSurface(RoundedCornerShape(BentoRadii.TILE_HOVER.dp), colors.float)
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                title,
                style = BentoTypography.title.copy(fontFamily = appFontFamily),
                color = colors.fg,
            )
            content()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                actions?.invoke(this)
                BentoButton(
                    label = strings.Done,
                    onClick = onDismiss,
                    filled = true,
                )
            }
        }
    }
}
