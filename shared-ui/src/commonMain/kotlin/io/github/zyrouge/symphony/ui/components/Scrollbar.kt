package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A thin thumb along the right edge while the content does not fit, so a cut-off
 * list reads as scrollable. Put it before `verticalScroll(state)` in the chain,
 * so it draws over the visible area rather than scrolling with the content. A
 * container with rounded corners passes their [cornerRadius], so the thumb's
 * ends stay clear of the curve.
 */
fun Modifier.bentoScrollbar(
    state: ScrollState,
    color: Color,
    cornerRadius: Dp = 0.dp,
): Modifier = drawWithContent {
    drawContent()
    val max = state.maxValue
    if (max <= 0 || max == Int.MAX_VALUE) return@drawWithContent
    drawBentoScrollThumb(
        visibleFraction = size.height / (size.height + max),
        scrolledFraction = state.value.toFloat() / max,
        color = color,
        cornerRadius = cornerRadius,
    )
}

/** [bentoScrollbar] for a lazy list, estimated from the sizes of the visible items. */
fun Modifier.bentoScrollbar(state: LazyListState, color: Color): Modifier = drawWithContent {
    drawContent()
    if (!state.canScrollForward && !state.canScrollBackward) return@drawWithContent
    val info = state.layoutInfo
    val visible = info.visibleItemsInfo
    if (visible.isEmpty()) return@drawWithContent
    val viewport = (info.viewportEndOffset - info.viewportStartOffset).toFloat()
    val itemSize = (visible.sumOf { it.size + info.mainAxisItemSpacing }).toFloat() / visible.size
    val contentSize = itemSize * info.totalItemsCount + info.beforeContentPadding + info.afterContentPadding
    val maxScroll = (contentSize - viewport).coerceAtLeast(1f)
    val scrolled = state.firstVisibleItemIndex * itemSize + state.firstVisibleItemScrollOffset
    drawBentoScrollThumb(
        visibleFraction = viewport / contentSize.coerceAtLeast(viewport),
        scrolledFraction = scrolled / maxScroll,
        color = color,
        cornerRadius = 0.dp,
    )
}

private fun ContentDrawScope.drawBentoScrollThumb(
    visibleFraction: Float,
    scrolledFraction: Float,
    color: Color,
    cornerRadius: Dp,
) {
    val width = 3.dp.toPx()
    val inset = 3.dp.toPx()
    // Half the radius keeps a thumb 3 dp from the side inside the corner's curve.
    val endInset = inset + cornerRadius.toPx() / 2f
    val track = size.height - endInset * 2
    if (track <= 0f) return
    val thumb = (track * visibleFraction).coerceIn(24.dp.toPx().coerceAtMost(track), track)
    val top = endInset + (track - thumb) * scrolledFraction.coerceIn(0f, 1f)
    drawRoundRect(
        color = color,
        topLeft = Offset(size.width - inset - width, top),
        size = Size(width, thumb),
        cornerRadius = CornerRadius(width / 2f),
    )
}
