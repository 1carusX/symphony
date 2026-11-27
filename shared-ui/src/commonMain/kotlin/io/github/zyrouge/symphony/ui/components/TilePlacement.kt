package io.github.zyrouge.symphony.ui.components

import androidx.compose.runtime.Immutable

/**
 * One entry of a bento placement table (DESIGN.md, Appendix A): a tile's grid
 * position. Columns and rows are counted from 1; spans count cells.
 */
@Immutable
data class BentoTilePlacement(
    val tileId: String,
    val column: Int,
    val row: Int,
    val columnSpan: Int,
    val rowSpan: Int,
)
