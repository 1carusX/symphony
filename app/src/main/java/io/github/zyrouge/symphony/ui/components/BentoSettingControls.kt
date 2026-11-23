package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.LocalStrings
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlin.math.roundToInt

/**
 * A settings group inside one bento tile: a title, then tappable rows whose
 * values open their own popup.
 */
@Composable
fun BentoSettingGroup(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val strings = LocalStrings.current
    BentoTile(
        tone = BentoTileTone.PLAIN,
        sizeClass = BentoTileSizeClass.L,
        modifier = modifier,
    ) {
        val tileColors = LocalBentoTileContentColors.current
        val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
        if (title != null) {
            Text(
                title,
                style = BentoTypography.title.copy(fontFamily = appFontFamily),
                color = tileColors.fg,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}

@Composable
fun BentoSettingRow(
    label: String,
    value: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = LocalBentoColors.current
    val scheme = LocalBentoColorScheme.current
    val shape = RoundedCornerShape(BentoRadii.TILE_SMALL.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(shape)
            .then(
                when {
                    scheme == BentoColorScheme.CONTRAST ->
                        Modifier.border(1.dp, colors.line, shape)

                    else -> Modifier
                },
            )
            .background(colors.raised)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            label,
            style = BentoTypography.rowTitle,
            color = colors.fg,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (value != null) {
            Text(
                value,
                style = BentoTypography.secondary,
                color = colors.sub,
                maxLines = 1,
            )
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                null,
                tint = colors.muted,
            )
        }
    }
}

@Composable
fun BentoToggleDialog(
    title: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalBentoColors.current
    BentoDialog(title = title, onDismiss = onDismiss) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                title,
                style = BentoTypography.rowTitle,
                color = colors.fg,
                modifier = Modifier.weight(1f),
            )
            BentoSwitch(
                checked = checked,
                onCheckedChange = onChange,
            )
        }
    }
}

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
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        .clickable { onSelect(index) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        label,
                        style = BentoTypography.rowTitle,
                        color = if (selected) colors.onTonal else colors.fg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

fun formatSettingSecs(value: Float): String {
    val rounded = (value * 2).roundToInt() / 2f
    return if (rounded % 1f == 0f) "${rounded.toInt()} s" else "$rounded s"
}
