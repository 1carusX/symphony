package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import kotlin.math.ceil


// ---------------------------------------------------------------------------
// Song rows (DESIGN.md 4.4)
// ---------------------------------------------------------------------------

/**
 * Assigns tiles to masonry columns (DESIGN.md 5.3): each tile joins the
 * currently shortest column, so many groups flow down balanced columns.
 */
fun <T> distributeBentoMasonry(
    entries: List<T>,
    columns: Int,
    heightOf: (T) -> Float,
): List<List<T>> {
    if (columns <= 1) {
        return listOf(entries)
    }
    val columnHeights = FloatArray(columns)
    val result = List(columns) { mutableListOf<T>() }
    entries.forEach { entry ->
        var shortest = 0
        for (column in 1 until columns) {
            if (columnHeights[column] < columnHeights[shortest]) {
                shortest = column
            }
        }
        columnHeights[shortest] += heightOf(entry)
        result[shortest].add(entry)
    }
    return result
}

/**
 * True when few groups form an even grid instead of masonry (DESIGN.md 5.3):
 * at most 2 x columns groups.
 */
fun isBentoGroupEvenGrid(groupCount: Int, maxColumns: Int): Boolean =
    maxColumns > 1 && groupCount <= maxColumns * 2

/**
 * The column count for grouped tiles (DESIGN.md 5.3): an even grid with few
 * groups uses the column count that leaves the fewest empty cells, and ties
 * keep the wider grid; with many groups every column is used (masonry).
 */
fun resolveBentoGroupColumnCount(groupCount: Int, maxColumns: Int): Int {
    if (maxColumns <= 1) {
        return 1
    }
    if (!isBentoGroupEvenGrid(groupCount, maxColumns)) {
        return maxColumns
    }
    fun waste(columns: Int) = ceil(groupCount / columns.toFloat()).toInt() * columns - groupCount
    var best = maxColumns
    for (columns in maxColumns - 1 downTo 2) {
        if (waste(columns) < waste(best)) {
            best = columns
        }
    }
    return best
}

/**
 * The grouped-page columns (DESIGN.md 5.3): 1 column below 560 dp inner
 * width, 2 at 560-999, 3 at 1000 and up. With at most 2 x columns groups
 * the tiles form an even grid; with more they flow down masonry columns.
 * Each entry renders through [entry] with its resolved tile width.
 * [bottomInset] clears platform chrome, and [contentAtEnd] / [contentAtTop]
 * receive the scroll edges when the caller tracks them.
 */
@Composable
fun <T> BentoGroupedTileColumns(
    entries: List<T>,
    entryHeightDp: (T) -> Float,
    modifier: Modifier = Modifier,
    bottomInset: Dp = 0.dp,
    contentAtEnd: MutableState<Boolean>? = null,
    contentAtTop: MutableState<Boolean>? = null,
    entry: @Composable (T, tileWidthDp: Float) -> Unit,
) {
    val windowInfo = LocalWindowInfo.current
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val compactWindow = windowInfo.containerDpSize.width < 600.dp
        val gutter = when {
            compactWindow -> BentoSpacing.GUTTER_COMPACT.dp
            else -> BentoSpacing.GUTTER_OTHER.dp
        }
        val gap = when {
            compactWindow -> BentoSpacing.GAP_COMPACT.dp
            else -> BentoSpacing.GAP_OTHER.dp
        }
        val innerWidth = maxWidth - gutter * 2
        val maxColumns = when {
            innerWidth >= 1000.dp -> 3
            innerWidth >= 560.dp -> 2
            else -> 1
        }
        val columns = resolveBentoGroupColumnCount(entries.size, maxColumns)
        val evenGrid = isBentoGroupEvenGrid(entries.size, maxColumns)
        val tileWidth = (innerWidth - gap * (columns - 1)) / columns

        val scrollState = rememberScrollState()
        if (contentAtEnd != null) {
            LaunchedEffect(scrollState) {
                snapshotFlow {
                    scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue - 4
                }.collect { contentAtEnd.value = it }
            }
        }
        if (contentAtTop != null) {
            LaunchedEffect(scrollState) {
                snapshotFlow { scrollState.value <= 4 }
                    .collect { contentAtTop.value = it }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = gutter, end = gutter, bottom = bottomInset),
        ) {
            when {
                evenGrid -> entries.chunked(columns).forEach { rowEntries ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = gap),
                        horizontalArrangement = Arrangement.spacedBy(gap),
                    ) {
                        rowEntries.forEach { entryValue ->
                            Box(modifier = Modifier.weight(1f)) {
                                entry(entryValue, tileWidth.value)
                            }
                        }
                        repeat(columns - rowEntries.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }

                else -> {
                    val columnsEntries = distributeBentoMasonry(entries, columns, entryHeightDp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(gap),
                    ) {
                        columnsEntries.forEach { columnEntries ->
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(gap),
                            ) {
                                columnEntries.forEach { entryValue ->
                                    Box {
                                        entry(entryValue, tileWidth.value)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
