package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import kotlin.math.max

/**
 * A tile's size class (DESIGN.md 4.1), derived from its pixel size and
 * controlling padding, type and inner layouts. Checked in this order:
 * height >= 300 -> xl, height >= 170 -> l, width >= 200 -> m, width < 92 -> xs,
 * otherwise -> s.
 */
enum class BentoTileSizeClass {
    XL,
    L,
    M,
    S,
    XS,
}

fun resolveBentoTileSizeClass(widthDp: Float, heightDp: Float) = when {
    heightDp >= 300f -> BentoTileSizeClass.XL
    heightDp >= 170f -> BentoTileSizeClass.L
    widthDp >= 200f -> BentoTileSizeClass.M
    widthDp < 92f -> BentoTileSizeClass.XS
    else -> BentoTileSizeClass.S
}

/**
 * Resolved geometry of one placed tile, in dp relative to the grid's content
 * box (inside the page gutter).
 */
@Immutable
data class BentoTileGeometry(
    val offsetX: Float,
    val offsetY: Float,
    val width: Float,
    val height: Float,
    val sizeClass: BentoTileSizeClass,
)

/**
 * Resolved geometry of a whole grid: each placement with its geometry, keyed
 * by tile id, plus the grid's total height.
 */
@Immutable
data class BentoGridMeasure(
    val tiles: Map<String, Pair<BentoTilePlacement, BentoTileGeometry>>,
    val totalHeight: Float,
)

/** The least a tile shares the bottom edge of a square tile may shrink to, in dp. */
const val BENTO_SQUARE_NEIGHBOUR_MIN_HEIGHT = 72f

/**
 * Resolves tile offsets, sizes and size classes for a placement table on a
 * grid class. Column widths are equal and fill [innerWidthDp]; a tile's pixel
 * size is `span * cell + (span - 1) * gap` per axis.
 *
 * Rows have a fixed height while columns follow the window, so a tile that
 * spans as many rows as columns is rarely square. [squareTileId] makes that
 * tile square (a cover): its height takes its width, tiles below its last
 * row move by the difference, and tiles reaching that row from above grow or
 * shrink with it, so edges stay aligned. Shrinking stops before any of those
 * tiles drops below [BENTO_SQUARE_NEIGHBOUR_MIN_HEIGHT].
 */
fun measureBentoGrid(
    placements: List<BentoTilePlacement>,
    gridClass: BentoGridClass,
    innerWidthDp: Float,
    squareTileId: String? = null,
): BentoGridMeasure {
    val columns = gridClass.columns.toFloat()
    val rowHeight = gridClass.rowHeightDp.toFloat()
    val gap = gridClass.gapDp.toFloat()
    val columnWidth = (innerWidthDp - (columns - 1f) * gap) / columns
    val tiles = placements.associate { placement ->
        val width = placement.columnSpan * columnWidth +
            (placement.columnSpan - 1f) * gap
        val height = placement.rowSpan * rowHeight + (placement.rowSpan - 1f) * gap
        val geometry = BentoTileGeometry(
            offsetX = (placement.column - 1f) * (columnWidth + gap),
            offsetY = (placement.row - 1f) * (rowHeight + gap),
            width = width,
            height = height,
            sizeClass = resolveBentoTileSizeClass(width, height),
        )
        placement.tileId to (placement to geometry)
    }
    val maxRow = placements.maxOfOrNull { it.row + it.rowSpan - 1 } ?: 0
    val totalHeight = maxRow * rowHeight + (maxRow - 1f).coerceAtLeast(0f) * gap
    val measure = BentoGridMeasure(tiles = tiles, totalHeight = totalHeight)
    return squareTileId?.let { squareBentoTile(measure, it) } ?: measure
}

private fun squareBentoTile(measure: BentoGridMeasure, tileId: String): BentoGridMeasure? {
    val (square, squareGeometry) = measure.tiles[tileId] ?: return null
    val lastRow = square.row + square.rowSpan - 1
    // Tiles that end on or below the square's last row but start on or above it.
    val stretched = measure.tiles.values.filter { (placement, _) ->
        placement.tileId != tileId &&
            placement.row <= lastRow &&
            placement.row + placement.rowSpan - 1 >= lastRow
    }
    val floor = stretched.maxOfOrNull { (_, geometry) ->
        BENTO_SQUARE_NEIGHBOUR_MIN_HEIGHT - geometry.height.coerceAtLeast(BENTO_SQUARE_NEIGHBOUR_MIN_HEIGHT)
    } ?: Float.NEGATIVE_INFINITY
    val delta = maxOf(squareGeometry.width - squareGeometry.height, floor)
    if (delta == 0f) return measure
    val tiles = measure.tiles.mapValues { (id, placed) ->
        val (placement, geometry) = placed
        val adjusted = when {
            placement.row > lastRow -> geometry.copy(offsetY = geometry.offsetY + delta)
            id == tileId || placement.row + placement.rowSpan - 1 >= lastRow -> {
                val height = geometry.height + delta
                geometry.copy(
                    height = height,
                    sizeClass = resolveBentoTileSizeClass(geometry.width, height),
                )
            }

            else -> geometry
        }
        placement to adjusted
    }
    return BentoGridMeasure(tiles = tiles, totalHeight = measure.totalHeight + delta)
}

/**
 * Tile padding from the size class (DESIGN.md 3.3): 16, or 12 in s, 8 in xs.
 */
fun bentoTilePaddingDp(sizeClass: BentoTileSizeClass) = when (sizeClass) {
    BentoTileSizeClass.XS -> BentoSpacing.TILE_PADDING_EXTRA_SMALL.dp
    BentoTileSizeClass.S -> BentoSpacing.TILE_PADDING_SMALL.dp
    else -> BentoSpacing.TILE_PADDING.dp
}

/**
 * True when a sub-label should render at this tile size (DESIGN.md 4.1): it
 * hides in xs tiles and in s tiles under 100 dp high.
 */
fun showsTileSubLabel(sizeClass: BentoTileSizeClass, tileHeightDp: Float) = when (sizeClass) {
    BentoTileSizeClass.XS -> false
    BentoTileSizeClass.S -> tileHeightDp >= 100f
    else -> true
}

/**
 * The cover grid (columns x rows, cell size) that fits a tile (DESIGN.md
 * 4.1): at least three covers, as large as fits, counting at most six
 * towards the covered area so a few big covers beat many small ones.
 * Mirrors the prototype's `fit(aw, ah, N, gap, minS, capH, capMin)`; a
 * caption of [captionHeightDp] below a cell is allowed from
 * [captionMinSizeDp] cell sizes up.
 */
fun fitBentoCoverGrid(
    tileWidthDp: Float,
    tileHeightDp: Float,
    itemCount: Int,
    gapDp: Float = 10f,
    minSizeDp: Float = 40f,
    captionHeightDp: Float = 22f,
    captionMinSizeDp: Float = 100f,
): BentoCoverGridFit {
    var best: BentoCoverGridFit? = null
    var bestScore = -1f
    for (rows in 1..4) {
        for (cols in 1..12) {
            val count = cols * rows
            if (count > itemCount) break
            if (count < minOf(3, itemCount)) continue
            val availableWidth = tileWidthDp - (cols - 1f) * gapDp
            val availableHeight = tileHeightDp - (rows - 1f) * gapDp
            var coverSize = minOf(availableWidth / cols, availableHeight / rows)
            var captioned = false
            if (minOf(availableWidth / cols, availableHeight / rows - captionHeightDp) >= captionMinSizeDp) {
                coverSize = minOf(availableWidth / cols, availableHeight / rows - captionHeightDp)
                captioned = true
            }
            if (coverSize < minSizeDp) continue
            val score = minOf(count, 6) * coverSize * coverSize
            if (score > bestScore) {
                bestScore = score
                best = BentoCoverGridFit(
                    columns = cols,
                    rows = rows,
                    coverSize = coverSize,
                    captioned = captioned,
                )
            }
        }
    }
    return best ?: BentoCoverGridFit(
        columns = 1,
        rows = 1,
        coverSize = max(24f, minOf(tileWidthDp, tileHeightDp)),
        captioned = false,
    )
}

/**
 * The cover layout picked by [fitBentoCoverGrid].
 */
@Immutable
data class BentoCoverGridFit(
    val columns: Int,
    val rows: Int,
    val coverSize: Float,
    val captioned: Boolean,
)

/**
 * Stat value type by size class (DESIGN.md 3.2): 40 in l/xl, 22 in s/xs,
 * 28 otherwise.
 */
fun bentoStatValueStyle(sizeClass: BentoTileSizeClass, fontFamily: FontFamily?) = when (sizeClass) {
    BentoTileSizeClass.XL, BentoTileSizeClass.L -> BentoTypography.statLarge
    BentoTileSizeClass.S, BentoTileSizeClass.XS -> BentoTypography.title.copy(fontFamily = fontFamily)
    else -> BentoTypography.display.copy(fontFamily = fontFamily)
}
