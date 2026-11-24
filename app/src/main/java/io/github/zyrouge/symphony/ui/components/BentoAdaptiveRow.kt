package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

/**
 * A row of [leading], [text] and [trailing], laid out like a Row whose text takes
 * the remaining width, everything centred vertically. When [trailing] leaves the
 * text too little room for its longest word (a large font or content scale), it
 * moves under the text, at the end, so the text wraps between words instead of
 * splitting them or cutting off.
 */
@Composable
internal fun BentoAdaptiveRow(
    modifier: Modifier,
    gap: Dp,
    leading: (@Composable () -> Unit)?,
    trailing: (@Composable () -> Unit)?,
    text: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = {
            Box { leading?.invoke() }
            Box { text() }
            Box { trailing?.invoke() }
        },
    ) { measurables, constraints ->
        val gapPx = gap.roundToPx()
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val leadingPlaceable = measurables[0].measure(loose)
        val trailingPlaceable = measurables[2].measure(loose)
        val leadingSpace = if (leadingPlaceable.width > 0) leadingPlaceable.width + gapPx else 0
        val trailingSpace = if (trailingPlaceable.width > 0) trailingPlaceable.width + gapPx else 0
        val width = when {
            constraints.hasBoundedWidth -> constraints.maxWidth
            else -> leadingSpace + measurables[1].maxIntrinsicWidth(Constraints.Infinity) + trailingSpace
        }
        val besideWidth = (width - leadingSpace - trailingSpace).coerceAtLeast(0)
        val stacked = trailingPlaceable.width > 0 &&
            measurables[1].minIntrinsicWidth(Constraints.Infinity) > besideWidth
        val textWidth = (if (stacked) width - leadingSpace else besideWidth).coerceAtLeast(0)
        val textPlaceable = measurables[1].measure(Constraints.fixedWidth(textWidth))
        val stackGap = 6.dp.roundToPx()
        val contentHeight = when {
            stacked -> textPlaceable.height + stackGap + trailingPlaceable.height
            else -> maxOf(textPlaceable.height, trailingPlaceable.height)
        }
        val height = maxOf(leadingPlaceable.height, contentHeight, constraints.minHeight)
        layout(width, height) {
            leadingPlaceable.placeRelative(0, (height - leadingPlaceable.height) / 2)
            if (stacked) {
                val top = (height - contentHeight) / 2
                textPlaceable.placeRelative(leadingSpace, top)
                trailingPlaceable.placeRelative(
                    width - trailingPlaceable.width,
                    top + textPlaceable.height + stackGap,
                )
            } else {
                textPlaceable.placeRelative(leadingSpace, (height - textPlaceable.height) / 2)
                trailingPlaceable.placeRelative(
                    width - trailingPlaceable.width,
                    (height - trailingPlaceable.height) / 2,
                )
            }
        }
    }
}

/**
 * [text] with a short [meta] (a duration) at its end, both centred vertically.
 * The meta is left out when keeping it would leave the text less than seven
 * [em]s, as with a large font or content scale, so a song title keeps a
 * readable stretch instead of shrinking to an ellipsis.
 */
@Composable
internal fun BentoTextWithMeta(
    modifier: Modifier,
    gap: Dp,
    em: TextUnit,
    meta: @Composable () -> Unit,
    text: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = {
            Box { text() }
            Box { meta() }
        },
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val metaPlaceable = measurables[1].measure(loose)
        val width = constraints.maxWidth
        val besideMeta = width - metaPlaceable.width - gap.roundToPx()
        val showMeta = metaPlaceable.width > 0 && besideMeta >= em.toPx() * 7
        val textWidth = (if (showMeta) besideMeta else width).coerceAtLeast(0)
        val textPlaceable = measurables[0].measure(loose.copy(minWidth = textWidth, maxWidth = textWidth))
        val height = maxOf(
            textPlaceable.height,
            if (showMeta) metaPlaceable.height else 0,
            constraints.minHeight,
        )
        layout(width, height) {
            textPlaceable.placeRelative(0, (height - textPlaceable.height) / 2)
            if (showMeta) {
                metaPlaceable.placeRelative(width - metaPlaceable.width, (height - metaPlaceable.height) / 2)
            }
        }
    }
}
