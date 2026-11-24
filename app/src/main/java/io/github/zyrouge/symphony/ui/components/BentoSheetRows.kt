package io.github.zyrouge.symphony.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.LocalStrings
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlin.math.roundToInt

/**
 * The pages of a [BentoSheetDialog]: [page] null is the root, anything else a
 * page pushed over it. Pages slide in from the end and back out to it, and
 * the sheet animates to the new height, so a setting with its own list opens
 * in the same sheet instead of a dialog on top. Pair it with the sheet's
 * `onBack` to show the back arrow and take the Back gesture.
 */
@Composable
fun <T : Any> BentoSheetPages(
    page: T?,
    content: @Composable ColumnScope.(page: T?) -> Unit,
) {
    AnimatedContent(
        targetState = page,
        transitionSpec = {
            val forward = targetState != null
            val enter = slideInHorizontally(tween(220)) { width -> if (forward) width / 4 else -width / 4 } +
                fadeIn(tween(220))
            val exit = slideOutHorizontally(tween(180)) { width -> if (forward) -width / 4 else width / 4 } +
                fadeOut(tween(120))
            (enter togetherWith exit).using(SizeTransform(clip = false))
        },
        label = "bento-sheet-pages",
    ) { current ->
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content(current)
        }
    }
}

/**
 * A row that opens a page or picker: the [value] sits at the end in a
 * `tonal` pill before a chevron, so the subtitle is free for an explanation.
 */
@Composable
fun BentoSheetValueRow(
    title: String,
    value: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors = LocalBentoColors.current
    BentoSheetRowLayout(
        title = title,
        subtitle = subtitle,
        leading = null,
        modifier = modifier
            .bentoSheetRowShape()
            .clickable(role = Role.Button, onClick = onClick),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (value != null) {
                BentoSheetValuePill(value)
            }
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                null,
                tint = colors.muted,
            )
        }
    }
}

/** A short value in a `tonal` pill (prototype `so-v`). */
@Composable
fun BentoSheetValuePill(value: String) {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier
            .widthIn(max = 240.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.tonal)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            value,
            style = BentoTypography.secondary.copy(
                fontWeight = FontWeight.SemiBold,
                fontFeatureSettings = "tnum",
            ),
            color = colors.onTonal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** One choice of a single-choice page; the selected one is `tonal` with a check. */
@Composable
fun BentoSheetOptionRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val colors = LocalBentoColors.current
    BentoSheetRowLayout(
        title = title,
        subtitle = subtitle,
        leading = null,
        modifier = modifier
            .bentoSheetRowShape()
            .background(if (selected) colors.tonal else Color.Transparent)
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    ) {
        if (selected) {
            Icon(Icons.Rounded.Check, null, tint = colors.onTonal)
        }
    }
}

/**
 * A small set of choices shown in place (prototype `seg`): the [title], then
 * one chip per option with the selected one `tonal`. For two to six short
 * options; longer lists get a page of [BentoSheetOptionRow]s.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BentoSheetChoiceRow(
    title: String,
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    optionFontFamily: ((index: Int) -> FontFamily?)? = null,
) {
    val colors = LocalBentoColors.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(title, style = BentoTypography.rowTitle, color = colors.fg)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            options.forEachIndexed { index, option ->
                BentoSheetChip(
                    label = option,
                    selected = index == selectedIndex,
                    onClick = { onSelect(index) },
                    fontFamily = optionFontFamily?.invoke(index),
                )
            }
        }
    }
}

/**
 * A value set in place with a slider (prototype `slider` rows): the [title]
 * with the current value at the end, then the slider. [step] rounds what the
 * slider reports; [onReset] adds a reset button while the value is not
 * [defaultValue]. With [stops], the slider snaps to those values only, sits
 * them evenly along a notched track and labels each one, as Speed & Pitch.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoSheetSliderRow(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    label: (Float) -> String,
    onChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    defaultValue: Float? = null,
    onReset: (() -> Unit)? = null,
    stops: List<Float>? = null,
) {
    val colors = LocalBentoColors.current
    val lastStop = (stops?.size ?: 1) - 1
    val stopIndex = stops?.let { bentoNearestStop(it, value) }
    val shown = stopIndex?.toFloat() ?: value.coerceIn(range)
    val fraction = when (stopIndex) {
        null -> (shown - range.start) / (range.endInclusive - range.start)
        else -> stopIndex.toFloat() / lastStop
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            modifier = Modifier.heightIn(min = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                title,
                style = BentoTypography.rowTitle,
                color = colors.fg,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (onReset != null && defaultValue != null && value != defaultValue) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onReset),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.Restore,
                        "${LocalStrings.current.Reset} $title",
                        tint = colors.sub,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
            BentoSheetValuePill(label(value))
        }
        Slider(
            value = shown,
            onValueChange = { raw ->
                when (stops) {
                    null -> {
                        val stepped = range.start + ((raw - range.start) / step).roundToInt() * step
                        onChange(stepped.coerceIn(range))
                    }

                    else -> onChange(stops[raw.roundToInt().coerceIn(0, lastStop)])
                }
            },
            valueRange = if (stops == null) range else 0f..lastStop.toFloat(),
            steps = if (stops == null) 0 else lastStop - 1,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    contentDescription = title
                    stateDescription = label(value)
                },
            thumb = {
                // The Speed & Pitch handle: a 6 x 28 bar ringed in the sheet color.
                Box(
                    modifier = Modifier
                        .size(width = 14.dp, height = 36.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(colors.`float`),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 6.dp, height = 28.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(colors.accent),
                    )
                }
            },
            track = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(colors.track),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(fraction.coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(colors.accent),
                    )
                    if (stops != null) {
                        // One dot per stop, so the track reads as notched rather than smooth.
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val inset = size.height / 2f
                            for (stop in 0..lastStop) {
                                val x = inset + (size.width - inset * 2f) * stop / lastStop
                                drawCircle(
                                    color = if (stop <= (stopIndex ?: -1)) colors.onAccent else colors.sub,
                                    radius = 2.dp.toPx(),
                                    center = Offset(x, size.height / 2f),
                                )
                            }
                        }
                    }
                }
            },
        )
        if (stops != null) {
            BentoStopLabels(stops.map(label), current = stopIndex ?: -1)
        }
    }
}

/** Index of the stop closest to [value], for values set before a slider snapped. */
fun bentoNearestStop(stops: List<Float>, value: Float): Int =
    stops.indices.minBy { kotlin.math.abs(stops[it] - value) }

/**
 * Stop labels under a slider, each centred on its stop: the Material slider
 * insets its track by half the 14 dp thumb on both sides.
 */
@Composable
private fun BentoStopLabels(labels: List<String>, current: Int) {
    val colors = LocalBentoColors.current
    val last = labels.lastIndex
    Layout(
        modifier = Modifier.fillMaxWidth(),
        content = {
            labels.forEachIndexed { index, text ->
                Text(
                    text,
                    style = BentoTypography.label.copy(fontFeatureSettings = "tnum"),
                    color = if (index == current) colors.accent else colors.sub,
                    maxLines = 1,
                )
            }
        },
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0)) }
        val width = constraints.maxWidth
        val inset = 7.dp.roundToPx()
        val height = placeables.maxOfOrNull { it.height } ?: 0
        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                val center = if (last == 0) width / 2 else inset + (width - inset * 2) * index / last
                placeable.place((center - placeable.width / 2).coerceIn(0, maxOf(0, width - placeable.width)), 0)
            }
        }
    }
}
