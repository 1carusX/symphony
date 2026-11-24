package io.github.zyrouge.symphony.ui.view.detail

import androidx.compose.runtime.Immutable
import io.github.zyrouge.symphony.ui.components.BentoTilePlacement
import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import kotlin.math.ceil
import kotlin.math.floor

/** Tile ids of the detail frame. Side tiles bring their own ids. */
object DetailTileIds {
    const val COVER = "cover"
    const val INFO = "info"
    const val PLAY = "play"
    const val SHUFFLE = "shuf"
    const val QUEUE = "queue"
    const val LENGTH = "stat"
    const val EDIT = "edit"
    const val TRACKS = "tracks"
}

/**
 * A secondary tile of a detail page: its id, the grid rows it needs and
 * whether it sits above the songs when the page stacks on phones.
 */
@Immutable
data class DetailSideSlot(
    val tileId: String,
    val rowSpan: Int,
    val leading: Boolean = false,
)

/**
 * Detail page placements (the prototype's `detailLayout`), the album frame
 * shared by the artist, genre, playlist and folder pages: cover, info, Play,
 * Shuffle and up to two more action tiles on top, then the songs tile beside
 * the cover column, which carries the side tiles. On c4 everything stacks
 * full width: the leading side tiles, the songs, then the other side tiles.
 * [actionB] only fits on c8, short and c12.
 */
fun detailPlacements(
    gridClass: BentoGridClass,
    tracksRowSpan: Int,
    side: List<DetailSideSlot>,
    actionA: String?,
    actionB: String?,
): List<BentoTilePlacement> {
    val out = mutableListOf<BentoTilePlacement>()
    fun place(id: String, column: Int, row: Int, columnSpan: Int, rowSpan: Int) {
        out.add(BentoTilePlacement(id, column, row, columnSpan, rowSpan))
    }

    fun stack(slots: List<DetailSideSlot>, column: Int, startRow: Int, columnSpan: Int) {
        var row = startRow
        slots.forEach {
            place(it.tileId, column, row, columnSpan, it.rowSpan)
            row += it.rowSpan
        }
    }

    when (gridClass) {
        BentoGridClass.C4 -> {
            place(DetailTileIds.COVER, 1, 1, 2, 2)
            place(DetailTileIds.INFO, 3, 1, 2, 2)
            place(DetailTileIds.PLAY, 1, 3, 2, 1)
            place(DetailTileIds.SHUFFLE, 3, 3, 1, 1)
            actionA?.let { place(it, 4, 3, 1, 1) }
            val tracks = DetailSideSlot(DetailTileIds.TRACKS, tracksRowSpan)
            stack(
                side.filter { it.leading } + tracks + side.filterNot { it.leading },
                column = 1,
                startRow = 4,
                columnSpan = 4,
            )
        }

        BentoGridClass.C6 -> {
            place(DetailTileIds.COVER, 1, 1, 2, 2)
            place(DetailTileIds.INFO, 3, 1, 4, 1)
            place(DetailTileIds.PLAY, 3, 2, 2, 1)
            place(DetailTileIds.SHUFFLE, 5, 2, 1, 1)
            actionA?.let { place(it, 6, 2, 1, 1) }
            place(DetailTileIds.TRACKS, 1, 3, 4, tracksRowSpan)
            stack(side, column = 5, startRow = 3, columnSpan = 2)
        }

        BentoGridClass.C12 -> {
            place(DetailTileIds.COVER, 1, 1, 4, 3)
            place(DetailTileIds.INFO, 5, 1, 8, 1)
            place(DetailTileIds.PLAY, 5, 2, 2, 1)
            place(DetailTileIds.SHUFFLE, 7, 2, 2, 1)
            actionA?.let { place(it, 9, 2, 2, 1) }
            actionB?.let { place(it, 11, 2, 2, 1) }
            place(DetailTileIds.TRACKS, 5, 3, 8, tracksRowSpan)
            stack(side, column = 1, startRow = 4, columnSpan = 4)
        }

        BentoGridClass.C8, BentoGridClass.SHORT -> {
            place(DetailTileIds.COVER, 1, 1, 3, 3)
            place(DetailTileIds.INFO, 4, 1, 5, 1)
            place(DetailTileIds.PLAY, 4, 2, 2, 1)
            place(DetailTileIds.SHUFFLE, 6, 2, 1, 1)
            actionA?.let { place(it, 7, 2, 1, 1) }
            actionB?.let { place(it, 8, 2, 1, 1) }
            place(DetailTileIds.TRACKS, 4, 3, 5, tracksRowSpan)
            stack(side, column = 1, startRow = 4, columnSpan = 3)
        }
    }
    return out
}

/**
 * Grid rows a list tile needs for [rowCount] rows of 48 dp below its 22 dp
 * heading, plus [extraDp] (the prototype's `listRows`); at least two.
 */
fun detailListRowSpan(gridClass: BentoGridClass, rowCount: Int, extraDp: Float = 0f): Int {
    val gap = gridClass.gapDp.toFloat()
    val content = 32f + 30f + rowCount * 48f + extraDp + gap
    return maxOf(2, ceil(content / (gridClass.rowHeightDp + gap)).toInt())
}

/**
 * Rows of the albums tile: one row per album (cover, title, year) for up to
 * three albums, otherwise a two-row cover grid.
 */
fun detailAlbumsRowSpan(gridClass: BentoGridClass, albumCount: Int): Int = when {
    albumCount <= 3 -> detailListRowSpan(gridClass, albumCount, albumCount * 16f)
    else -> 2
}

/**
 * The most grid rows a songs tile may span on wide grids: as many as fit in
 * [viewportHeightDp], at least three. Longer lists scroll inside the tile,
 * so only the visible rows are composed.
 */
fun detailMaxTracksRowSpan(gridClass: BentoGridClass, viewportHeightDp: Float): Int {
    val gap = gridClass.gapDp.toFloat()
    val rows = floor((viewportHeightDp + gap) / (gridClass.rowHeightDp + gap)).toInt()
    return maxOf(3, rows)
}
