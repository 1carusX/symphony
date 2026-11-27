package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import io.github.zyrouge.symphony.utils.withCase
import kotlin.math.ceil

/** A leading "The " is ignored while ordering groups (prototype groupsOf). */
fun bentoStripLeadingThe(value: String): String =
    value.replaceFirst(Regex("^the\\s+", RegexOption.IGNORE_CASE), "")

/**
 * The rows a grouped tile shows: all songs up to six, or five songs plus the
 * "Show all" row.
 */
fun bentoGroupVisibleRowCount(songCount: Int): Int = minOf(songCount, BENTO_GROUP_MAX_ROWS)

/**
 * A grouped tile's estimated height in dp at the default font scale, used to
 * balance the masonry columns (the tile itself sizes to its content): 16 of
 * top padding, the 76 dp header (64 dp cover plus 6 dp above and below), an
 * 8 dp gap, 44 dp rows and 8 dp of bottom padding.
 */
fun bentoGroupTileHeightDp(songCount: Int): Float =
    108f + bentoGroupVisibleRowCount(songCount) * 44f

/**
 * The Tracks tile's grid row span (DESIGN.md Appendix A, "album"):
 * `ceil((32 + 30 + trackCount x 48 + gap) / (rowHeight + gap))`.
 */
fun bentoAlbumTracksRowSpan(trackCount: Int, gridClass: BentoGridClass): Int {
    val gap = gridClass.gapDp.toFloat()
    val content = 32f + 30f + trackCount * 48f + gap
    return ceil(content / (gridClass.rowHeightDp + gap)).toInt()
}

/**
 * The Albums group order (DESIGN.md 5.3): artist name, then year; a leading
 * "The " is ignored on the artist name and unknown years sort last.
 */
fun <T> bentoAlbumsGroupComparator(
    artistNameOf: (T) -> String?,
    yearOf: (T) -> Int?,
    caseSensitive: Boolean,
): Comparator<T> = compareBy(
    { bentoStripLeadingThe(artistNameOf(it) ?: "").withCase(caseSensitive) },
    { yearOf(it) ?: Int.MAX_VALUE },
)

/**
 * Orders artist, genre and folder names (DESIGN.md 5.3): "name" order with a
 * leading "The " ignored.
 */
fun <T> bentoNamesGroupComparator(
    nameOf: (T) -> String,
    caseSensitive: Boolean,
): Comparator<T> = compareBy { bentoStripLeadingThe(nameOf(it)).withCase(caseSensitive) }

/** Album hits match the album title or its artists, case-insensitively. */
fun bentoAlbumMatchesQuery(album: Album, query: String): Boolean {
    val q = query.lowercase()
    return album.name.lowercase().contains(q) ||
        album.artists.any { it.lowercase().contains(q) }
}
