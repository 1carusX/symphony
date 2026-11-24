package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * Asks to confirm an action: [title], a [description], a `raised` cancel
 * button and an `accent` filled confirm button. A scrim tap or Back counts as
 * cancel. The labels default to No / Yes.
 */
@Composable
fun ConfirmationDialog(
    context: ViewContext,
    title: @Composable () -> Unit,
    description: @Composable () -> Unit,
    onResult: (Boolean) -> Unit,
    confirmLabel: String? = null,
    cancelLabel: String? = null,
) {
    val colors = LocalBentoColors.current
    BentoSheetDialog(
        title = title,
        onDismissRequest = { onResult(false) },
        actions = {
            BentoSheetButton(
                label = cancelLabel ?: context.symphony.t.No,
                onClick = { onResult(false) },
            )
            BentoSheetButton(
                label = confirmLabel ?: context.symphony.t.Yes,
                primary = true,
                onClick = { onResult(true) },
            )
        },
    ) {
        Box(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)) {
            ProvideTextStyle(
                BentoTypography.rowTitle.copy(
                    fontWeight = BentoTypography.body.fontWeight,
                    lineHeight = (15 * 1.45).sp,
                    color = colors.sub,
                ),
            ) {
                description()
            }
        }
    }
}
