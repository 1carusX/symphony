package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Immutable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.i18n.Strings
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import io.github.zyrouge.symphony.utils.withCase
import kotlin.math.ceil
import kotlin.math.roundToInt

/** A leading "The " is ignored while ordering groups (prototype groupsOf). */
fun bentoStripLeadingThe(value: String): String =
    value.replaceFirst(Regex("^the\\s+", RegexOption.IGNORE_CASE), "")

/**
 * The letter a song files under (DESIGN.md 5.2): the first character of its
 * sort key, uppercased; anything outside A-Z files under "#".
 */
fun resolveBentoLetter(sortKeyValue: String): String {
    val c = sortKeyValue.firstOrNull()?.uppercaseChar() ?: return "#"
    return if (c in 'A'..'Z') c.toString() else "#"
}

/**
 * One run of consecutive entries sharing a letter (DESIGN.md 5.2). A null
 * letter means the sort shows a plain list (Year, Duration and the other
 * non-lettered sorts).
 */
@Immutable
data class BentoLetterGroup<T>(val letter: String?, val entries: List<T>)

/**
 * Groups already-ordered entries into consecutive letter runs. Only lettered
 * sorts (Title, Artist, Album) group; the others return a single plain group.
 */
fun <T> groupBentoEntriesByLetter(
    entries: List<T>,
    lettered: Boolean,
    sortKeyOf: (T) -> String,
): List<BentoLetterGroup<T>> {
    if (!lettered) {
        return listOf(BentoLetterGroup(letter = null, entries = entries))
    }
    val groups = mutableListOf<Pair<String, MutableList<T>>>()
    entries.forEach { entry ->
        val letter = resolveBentoLetter(sortKeyOf(entry))
        val last = groups.lastOrNull()
        if (last != null && last.first == letter) {
            last.second.add(entry)
        } else {
            groups.add(letter to mutableListOf(entry))
        }
    }
    return groups.map { BentoLetterGroup(letter = it.first, entries = it.second) }
}

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

private val BENTO_LOSSLESS_ENCODER_HINTS =
    listOf("flac", "alac", "aiff", "ape", "wv", "pcm", "lossless")

private val BENTO_LOSSLESS_EXTENSIONS =
    listOf(".flac", ".wav", ".aiff", ".aif", ".ape", ".wv", ".alac")

/**
 * Whether a song is lossless for the search filter (DESIGN.md 5.5): the kit
 * does not define the check for the app's metadata, so a lossless encoder
 * name or a lossless file extension qualifies.
 */
fun isBentoLosslessSong(path: String, encoder: String?): Boolean {
    val hint = encoder?.lowercase() ?: ""
    if (BENTO_LOSSLESS_ENCODER_HINTS.any { it in hint }) {
        return true
    }
    val filename = path.lowercase().substringAfterLast('/')
    return BENTO_LOSSLESS_EXTENSIONS.any { filename.endsWith(it) }
}

/**
 * "N d ago" for the Recently added filter rows; "Today" within the first day.
 * With [t], in the app language; without it (tests), in English.
 */
fun formatBentoDaysAgo(dateModifiedMs: Long, nowMs: Long, t: Strings? = null): String {
    val days = ((nowMs - dateModifiedMs) / 86_400_000L).toInt()
    return when {
        days <= 0 -> t?.Today ?: "Today"
        else -> t?.XDaysAgo(days.toString()) ?: "$days d ago"
    }
}

/** The window of the "Recently added" search filter (prototype: 30 days). */
const val BENTO_RECENTLY_ADDED_DAYS = 30

/** Whether a song falls in the "Recently added" search filter. */
fun isBentoRecentlyAddedSong(dateModifiedMs: Long, nowMs: Long): Boolean =
    nowMs - dateModifiedMs <= BENTO_RECENTLY_ADDED_DAYS * 86_400_000L

/**
 * Search matching (DESIGN.md 5.5): the query matches title, artist and
 * album, case-insensitively.
 */
fun bentoSongMatchesQuery(
    title: String,
    artists: Collection<String>,
    album: String?,
    query: String,
): Boolean {
    val q = query.lowercase()
    return title.lowercase().contains(q) ||
        artists.any { it.lowercase().contains(q) } ||
        album?.lowercase()?.contains(q) == true
}

/** Artist hits match the artist name, case-insensitively. */
fun bentoArtistNameMatchesQuery(artistName: String, query: String): Boolean =
    artistName.lowercase().contains(query.lowercase())

/** Album hits match the album title or its artists, case-insensitively. */
fun bentoAlbumMatchesQuery(album: Album, query: String): Boolean {
    val q = query.lowercase()
    return album.name.lowercase().contains(q) ||
        album.artists.any { it.lowercase().contains(q) }
}

/**
 * Long durations in the tiles (prototype fmtLong): "1 h 23 min", "45 min".
 * With [t], in the app language; without it (tests), in English.
 */
fun formatBentoDurationMinutes(totalMs: Long, t: Strings? = null): String {
    val totalSeconds = totalMs / 1000
    val minutes = (totalMs / 60_000.0).roundToInt()
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        totalSeconds < 60 -> t?.DurationXSeconds(totalSeconds.toString()) ?: "$totalSeconds s"
        hours >= 1 -> t?.DurationXHoursYMinutes(hours.toString(), rest.toString()) ?: "$hours h $rest min"
        else -> t?.DurationXMinutes(minutes.toString()) ?: "$minutes min"
    }
}

/** Durations, times, counts and badges use tabular figures (DESIGN.md 3.2). */
fun bentoTabularStyle(style: TextStyle) = style.copy(fontFeatureSettings = "tnum")
