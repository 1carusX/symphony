package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

/**
 * The body of a settings sub-page (prototype `settingsPage`): each child is
 * one settings group tile, flowed into 1, 2 or 3 columns by width (2 from
 * 640 dp, 3 from 1100 dp, never more than there are groups). Groups keep
 * their order down each column, like CSS columns. The side gutter is 16 dp
 * on phones and 24 dp from 600 dp.
 */
@Composable
fun BentoSettingsColumns(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val wide = maxWidth >= 600.dp
        val gutter = if (wide) 24.dp else 16.dp
        val gap = if (wide) 16.dp else 12.dp
        val maxColumns = bentoSettingsColumnCount(maxWidth)
        BentoSettingsColumnsLayout(
            maxColumns = maxColumns,
            gap = gap,
            modifier = Modifier.padding(horizontal = gutter),
            content = content,
        )
    }
}

/** Column count for a settings page of [width] (prototype: 3 from 1100, 2 from 640). */
fun bentoSettingsColumnCount(width: Dp): Int = when {
    width >= 1100.dp -> 3
    width >= 640.dp -> 2
    else -> 1
}

/**
 * Splits [heights] into `min(columns, heights.size)` non-empty runs that keep
 * their order, making the tallest run as short as possible. Returns the run
 * index of every height. Settings pages have a handful of groups, so the
 * exact partition is cheap.
 */
fun bentoSettingsColumnRuns(heights: List<Int>, columns: Int, gap: Int): List<Int> {
    val n = heights.size
    if (n == 0) return emptyList()
    val k = columns.coerceIn(1, n)
    fun runHeight(from: Int, to: Int) =
        (from until to).sumOf { heights[it] } + gap * (to - from - 1)

    // best[j][i]: the lowest tallest run when the first i heights fill j runs.
    val best = Array(k + 1) { IntArray(n + 1) { Int.MAX_VALUE } }
    val cut = Array(k + 1) { IntArray(n + 1) }
    best[0][0] = 0
    for (j in 1..k) {
        for (i in j..n) {
            for (p in (j - 1) until i) {
                if (best[j - 1][p] == Int.MAX_VALUE) continue
                val cost = max(best[j - 1][p], runHeight(p, i))
                if (cost < best[j][i]) {
                    best[j][i] = cost
                    cut[j][i] = p
                }
            }
        }
    }
    val runs = IntArray(n)
    var end = n
    for (j in k downTo 1) {
        val start = cut[j][end]
        for (index in start until end) runs[index] = j - 1
        end = start
    }
    return runs.toList()
}

@Composable
private fun BentoSettingsColumnsLayout(
    maxColumns: Int,
    gap: Dp,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content = content, modifier = modifier) { measurables, constraints ->
        val gapPx = gap.roundToPx()
        val columns = maxColumns.coerceIn(1, max(1, measurables.size))
        val columnWidth = (constraints.maxWidth - gapPx * (columns - 1)) / columns
        val childConstraints = constraints.copy(
            minWidth = columnWidth,
            maxWidth = columnWidth,
            minHeight = 0,
        )
        val placeables = measurables.map { it.measure(childConstraints) }
        val runs = bentoSettingsColumnRuns(placeables.map { it.height }, columns, gapPx)
        val columnHeights = IntArray(columns)
        val positions = placeables.mapIndexed { index, placeable ->
            val column = runs[index]
            val y = columnHeights[column] + if (columnHeights[column] > 0) gapPx else 0
            columnHeights[column] = y + placeable.height
            column to y
        }
        val height = columnHeights.maxOrNull() ?: 0
        layout(constraints.maxWidth, height) {
            placeables.forEachIndexed { index, placeable ->
                val (column, y) = positions[index]
                placeable.place(column * (columnWidth + gapPx), y)
            }
        }
    }
}
