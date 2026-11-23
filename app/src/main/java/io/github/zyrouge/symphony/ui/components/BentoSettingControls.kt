package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.LocalStrings
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlin.math.roundToInt

@Composable
fun BentoSliderDialog(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValue: (Float) -> Float,
    label: (Float) -> String,
    onChange: (Float) -> Unit,
    onReset: (() -> Unit)? = null,
    onDismiss: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val strings = LocalStrings.current
    BentoDialog(
        title = title,
        onDismiss = onDismiss,
        actions = if (onReset != null) {
            { BentoButton(label = strings.Reset, onClick = onReset) }
        } else {
            null
        },
    ) {
        Slider(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            value = value.coerceIn(range.start, range.endInclusive),
            range = range,
            label = { raw ->
                Text(
                    text = label(onValue(raw)),
                    style = BentoTypography.secondary,
                    color = colors.sub,
                )
            },
            onChange = { onChange(it) },
        )
    }
}

@Composable
fun BentoOptionDialog(
    title: String,
    options: List<Pair<String, Boolean>>,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val scheme = LocalBentoColorScheme.current
    BentoDialog(title = title, onDismiss = onDismiss) {
        // Long lists (19 languages) scroll inside the popup so every option and the Done
        // button stay reachable on short screens.
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .verticalScroll(rememberScrollState())
                .selectableGroup(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEachIndexed { index, (label, selected) ->
                val shape = RoundedCornerShape(BentoRadii.TILE_SMALL.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 46.dp)
                        .clip(shape)
                        .then(
                            when {
                                scheme == BentoColorScheme.CONTRAST ->
                                    Modifier.border(1.dp, colors.line, shape)

                                else -> Modifier
                            },
                        )
                        .background(if (selected) colors.tonal else colors.raised)
                        .selectable(
                            selected = selected,
                            role = Role.RadioButton,
                            onClick = { onSelect(index) },
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        label,
                        style = BentoTypography.rowTitle,
                        color = if (selected) colors.onTonal else colors.fg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    if (selected) {
                        Icon(Icons.Rounded.Check, null, tint = colors.onTonal)
                    }
                }
            }
        }
    }
}

fun formatSettingSecs(value: Float): String {
    val rounded = (value * 2).roundToInt() / 2f
    return if (rounded % 1f == 0f) "${rounded.toInt()} s" else "$rounded s"
}
