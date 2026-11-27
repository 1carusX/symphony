package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/** One row of a [BentoSettingsSheet]. */
sealed interface BentoSettingsRow {
    @Immutable
    data class Section(val title: String) : BentoSettingsRow

    @Immutable
    data class Switch(
        val title: String,
        val checked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
        val subtitle: String? = null,
    ) : BentoSettingsRow

    /** Arbitrary content between rows, such as a preview. */
    @Immutable
    data class Content(val content: @Composable () -> Unit) : BentoSettingsRow

    @Immutable
    data class Slider(
        val title: String,
        val value: Float,
        val range: ClosedFloatingPointRange<Float>,
        val step: Float,
        val label: (Float) -> String,
        val onChange: (Float) -> Unit,
        val defaultValue: Float? = null,
        val onReset: (() -> Unit)? = null,
    ) : BentoSettingsRow
}

/**
 * A settings sheet built from [rows]: switch rows, sliders and section
 * headings, with the shared Done action.
 */
@Composable
fun BentoSettingsSheet(
    title: String,
    doneLabel: String,
    onDismissRequest: () -> Unit,
    rows: List<BentoSettingsRow>,
) {
    BentoSheetDialog(
        title = title,
        onDismissRequest = onDismissRequest,
        actions = {
            BentoSheetButton(
                label = doneLabel,
                primary = true,
                onClick = onDismissRequest,
            )
        },
    ) {
        rows.forEach { row ->
            when (row) {
                is BentoSettingsRow.Section -> BentoSettingsSectionLabel(row.title)

                is BentoSettingsRow.Content -> row.content()

                is BentoSettingsRow.Switch -> BentoSheetSwitchRow(
                    title = row.title,
                    subtitle = row.subtitle,
                    checked = row.checked,
                    onCheckedChange = row.onCheckedChange,
                )

                is BentoSettingsRow.Slider -> BentoSheetSliderRow(
                    title = row.title,
                    value = row.value,
                    range = row.range,
                    step = row.step,
                    label = row.label,
                    onChange = row.onChange,
                    defaultValue = row.defaultValue,
                    onReset = row.onReset,
                )
            }
        }
    }
}

@Composable
private fun BentoSettingsSectionLabel(text: String) {
    Text(
        text,
        style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
        color = LocalBentoColors.current.sub,
        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 2.dp),
    )
}
