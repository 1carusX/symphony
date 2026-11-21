package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.utils.DurationUtils
import io.github.zyrouge.symphony.utils.withCase
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Shared building blocks of the bento library pages (DESIGN.md 4.4 rows,
 * 5.2 Songs, 5.3 grouped tiles, 5.5 search filters) plus the pure layout
 * logic those pages need: letter grouping, masonry distribution, group
 * ordering and the album placement helpers.
 */

// ---------------------------------------------------------------------------
// Pure logic
// ---------------------------------------------------------------------------

/**
 * The number of rows a grouped tile shows (DESIGN.md 5.3): up to six, with a
 * "Show all" row replacing the sixth song when the group is longer.
 */
const val BENTO_GROUP_MAX_ROWS = 6

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
 * A grouped tile's height in dp (prototype gt-tile): 16 of top padding, the
 * 64 dp header, an 8 dp gap, 44 dp rows and 8 dp of bottom padding.
 */
fun bentoGroupTileHeightDp(songCount: Int): Float =
    96f + bentoGroupVisibleRowCount(songCount) * 44f

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

/** "N d ago" for the Recently added filter rows; "Today" within the first day. */
fun formatBentoDaysAgo(dateModifiedMs: Long, nowMs: Long): String {
    val days = ((nowMs - dateModifiedMs) / 86_400_000L).toInt()
    return when {
        days <= 0 -> "Today"
        else -> "$days d ago"
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
fun bentoAlbumMatchesQuery(album: io.github.zyrouge.symphony.services.groove.Album, query: String): Boolean {
    val q = query.lowercase()
    return album.name.lowercase().contains(q) ||
        album.artists.any { it.lowercase().contains(q) }
}

/** Long durations in the tiles (prototype fmtLong): "1 h 23 min", "45 min". */
fun formatBentoDurationMinutes(totalMs: Long): String {
    val minutes = (totalMs / 60_000.0).roundToInt()
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours >= 1 -> "$hours h $rest min"
        else -> "$minutes min"
    }
}

/** Durations, times, counts and badges use tabular figures (DESIGN.md 3.2). */
fun bentoTabularStyle(style: TextStyle) = style.copy(fontFeatureSettings = "tnum")

/** The A-Z letters of the scrubber, in order (DESIGN.md 4.6). */
val BENTO_AZ_LETTERS = ('A'..'Z').map { it.toString() } + "#"

/**
 * Resolves the scrub target while dragging (prototype scrubAt): a letter
 * without songs is skipped forward, then backward; null when no letter has
 * songs at all.
 */
fun resolveBentoScrubLetter(presentLetters: Set<String>, pressedLetter: String): String? {
    if (pressedLetter in presentLetters) {
        return pressedLetter
    }
    val pressedIndex = BENTO_AZ_LETTERS.indexOf(pressedLetter).coerceAtLeast(0)
    for (i in pressedIndex until BENTO_AZ_LETTERS.size) {
        if (BENTO_AZ_LETTERS[i] in presentLetters) return BENTO_AZ_LETTERS[i]
    }
    for (i in pressedIndex downTo 0) {
        if (BENTO_AZ_LETTERS[i] in presentLetters) return BENTO_AZ_LETTERS[i]
    }
    return null
}

// ---------------------------------------------------------------------------
// Playing-song state
// ---------------------------------------------------------------------------

/**
 * The song the radio is on, with its playing state, for row highlighting.
 */
@Immutable
data class BentoPlayingSong(val songId: String?, val isPlaying: Boolean)

@Composable
fun rememberBentoPlayingSong(context: ViewContext): BentoPlayingSong {
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val isPlaying by context.symphony.radio.observatory.isPlaying.collectAsState()
    return remember(queue, queueIndex, isPlaying) {
        BentoPlayingSong(songId = queue.getOrNull(queueIndex), isPlaying = isPlaying)
    }
}

// ---------------------------------------------------------------------------
// Tone surface
// ---------------------------------------------------------------------------

/**
 * A tile-shaped surface in one of the bento tones (DESIGN.md 4.1): tone fill,
 * the 7 % hover/press overlay, the radius growing to 30 on hover, and the
 * 1 dp line ring in the Contrast scheme. Unlike [BentoTile] the padding and
 * inner layout belong to the caller.
 */
@Composable
fun BentoToneSurface(
    modifier: Modifier = Modifier,
    tone: BentoTileTone = BentoTileTone.PLAIN,
    radiusDp: Int = BentoRadii.TILE,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val colors = LocalBentoColors.current
    val colorScheme = LocalBentoColorScheme.current
    val (fill, fg) = when (tone) {
        BentoTileTone.PLAIN -> colors.surface to colors.fg
        BentoTileTone.RAISED -> colors.raised to colors.fg
        BentoTileTone.ACCENT -> colors.accent to colors.onAccent
        BentoTileTone.TONAL -> colors.tonal to colors.onTonal
    }
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val shape = RoundedCornerShape(
        when {
            hovered -> BentoRadii.TILE_HOVER.dp
            else -> radiusDp.dp
        },
    )
    Box(
        modifier = modifier
            .let { m ->
                when (onClick) {
                    null -> m
                    else -> m
                        .hoverable(interactionSource)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = onClick,
                        )
                }
            }
            .clip(shape)
            .then(
                when (colorScheme) {
                    BentoColorScheme.CONTRAST -> Modifier.border(1.dp, colors.line, shape)
                    else -> Modifier
                },
            )
            .background(fill)
            .then(
                when {
                    hovered || pressed -> Modifier.background(fg.copy(alpha = 0.07f))
                    else -> Modifier
                },
            ),
    ) {
        content()
    }
}

// ---------------------------------------------------------------------------
// Action tiles (DESIGN.md 5.2)
// ---------------------------------------------------------------------------

/**
 * An action tile (DESIGN.md 5.2): 72 dp high, an icon well beside the label
 * and sub-label. Below 520 dp inner width the non-main tiles stack the icon
 * over the label and drop their sub-label.
 */
@Composable
fun BentoActionTile(
    icon: ImageVector,
    label: String,
    sub: String?,
    tone: BentoTileTone = BentoTileTone.PLAIN,
    main: Boolean = false,
    narrow: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val (fg, subColor) = when (tone) {
        BentoTileTone.PLAIN -> colors.fg to colors.sub
        BentoTileTone.RAISED -> colors.fg to colors.sub
        BentoTileTone.ACCENT -> colors.onAccent to colors.onAccentSub
        BentoTileTone.TONAL -> colors.onTonal to colors.tonalSub
    }
    val stacked = narrow && !main
    BentoToneSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(BentoSizes.ACTION_TILE.dp),
        tone = tone,
        onClick = onClick,
    ) {
        if (stacked) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(icon, label, tint = fg, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(4.dp))
                Text(
                    label,
                    style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
                    color = fg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(BentoRadii.ICON_WELL_40.dp))
                        .background(fg.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, label, tint = fg, modifier = Modifier.size(22.dp))
                }
                Column {
                    Text(
                        label,
                        style = BentoTypography.rowTitle,
                        color = fg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (sub != null) {
                        Text(
                            sub,
                            style = BentoTypography.secondary,
                            color = subColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Song rows (DESIGN.md 4.4)
// ---------------------------------------------------------------------------

/**
 * A song row (DESIGN.md 4.4): 60 dp (56 in column mode), 44 dp art, the
 * title (accent while playing, with an equalizer glyph over the art), the
 * artist - album line and the duration. Column mode swaps the line for
 * artist and album cells.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoSongRow(
    context: ViewContext,
    song: Song,
    playing: Boolean,
    isPlaying: Boolean,
    columns: Boolean = false,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val durationStyle = bentoTabularStyle(BentoTypography.secondary)
    val artistsLine = song.artists.joinToString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                when {
                    !columns -> Modifier.height(BentoSizes.SONG_ROW.dp)
                    else -> Modifier.height(BentoSizes.SONG_ROW_COLUMNS.dp)
                },
            )
            .hoverable(interactionSource)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick,
            )
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .background(
                when {
                    hovered || pressed -> colors.raised
                    else -> Color.Transparent
                },
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box {
            AsyncImage(
                song.createArtworkImageRequest(context.symphony).build(),
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(BentoRadii.SONG_ART_44.dp)),
            )
            if (playing) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(BentoRadii.SONG_ART_44.dp))
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center,
                ) {
                    BentoEqualizerGlyph(color = Color.White, playing = isPlaying)
                }
            }
        }
        if (columns) {
            Text(
                song.title,
                style = BentoTypography.rowTitle,
                color = when {
                    playing -> colors.accent
                    else -> colors.fg
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(2f),
            )
            Text(
                artistsLine,
                style = BentoTypography.body,
                color = colors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1.3f),
            )
            Text(
                song.album ?: "",
                style = BentoTypography.body,
                color = colors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1.5f),
            )
            Text(
                DurationUtils.formatMs(song.duration),
                style = durationStyle,
                color = colors.muted,
                maxLines = 1,
                textAlign = TextAlign.End,
                modifier = Modifier.width(52.dp),
            )
        } else {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    song.title,
                    style = BentoTypography.rowTitle,
                    color = when {
                        playing -> colors.accent
                        else -> colors.fg
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (artistsLine.isNotEmpty() || !song.album.isNullOrEmpty()) {
                    Text(
                        listOfNotNull(
                            artistsLine.takeIf { it.isNotEmpty() },
                            song.album,
                        ).joinToString(" · "),
                        style = BentoTypography.secondary,
                        color = colors.sub,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            trailing?.invoke(this)
            Text(
                DurationUtils.formatMs(song.duration),
                style = durationStyle,
                color = colors.muted,
                maxLines = 1,
                textAlign = TextAlign.End,
            )
        }
    }
}

/**
 * The sticky column header of the Songs page in column mode (DESIGN.md 4.4):
 * 40 dp, 12/700 uppercase with 0.06em tracking.
 */
@Composable
fun BentoSongColumnHeader(
    title: String,
    artist: String,
    album: String,
    duration: String,
) {
    val colors = LocalBentoColors.current
    val style = BentoTypography.label.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.06.em,
    )
    // The header is uppercase (DESIGN.md 4.4).
    val upperTitle = title.uppercase()
    val upperArtist = artist.uppercase()
    val upperAlbum = album.uppercase()
    val upperDuration = duration.uppercase()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.width(44.dp))
        Text(
            upperTitle,
            style = style,
            color = colors.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(2f),
        )
        Text(
            upperArtist,
            style = style,
            color = colors.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.3f),
        )
        Text(
            upperAlbum,
            style = style,
            color = colors.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.5f),
        )
        Text(
            upperDuration,
            style = style,
            color = colors.muted,
            maxLines = 1,
            textAlign = TextAlign.End,
            modifier = Modifier.width(52.dp),
        )
    }
}

/**
 * A letter header of the Songs page (DESIGN.md 5.2): 34 dp, 13/700 accent.
 */
@Composable
fun BentoLetterHeader(letter: String) {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BentoSizes.LETTER_HEADER.dp)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            letter,
            style = BentoTypography.secondary.copy(fontWeight = FontWeight.Bold),
            color = colors.accent,
            maxLines = 1,
        )
    }
}

// ---------------------------------------------------------------------------
// A-Z scrubber (DESIGN.md 4.6)
// ---------------------------------------------------------------------------

/**
 * The A-Z scrubber (DESIGN.md 4.6): a 24 dp column of letters at the right
 * edge, vertically centered on screen, min(520, window height - 260) tall.
 * Letters with songs use `accent`, the others render dimmed and are skipped
 * while scrubbing. A 56 dp accent bubble shows the current letter while
 * dragging and the caller jumps the list to that letter's header.
 */
@Composable
fun BentoAzScrubber(
    presentLetters: Set<String>,
    onLetter: (String) -> Unit,
) {
    val colors = LocalBentoColors.current
    val density = LocalDensity.current
    val windowInfo = LocalWindowInfo.current
    val height = minOf(
        BentoSizes.AZ_SCRUBBER_MAX_HEIGHT.dp,
        windowInfo.containerDpSize.height - 260.dp,
    ).coerceAtLeast(120.dp)
    var dragY by remember { mutableStateOf<Float?>(null) }
    var bubbleLetter by remember { mutableStateOf<String?>(null) }

    fun scrubAt(yPx: Float, boxHeightPx: Float) {
        with(density) {
            val usable = boxHeightPx - 20.dp.toPx()
            val fraction = ((yPx - 10.dp.toPx()) / usable).coerceIn(0f, 0.9999f)
            val index = (fraction * BENTO_AZ_LETTERS.size).toInt().coerceIn(0, 26)
            val letter = resolveBentoScrubLetter(presentLetters, BENTO_AZ_LETTERS[index])
            if (letter != null) {
                dragY = yPx
                if (bubbleLetter != letter) {
                    bubbleLetter = letter
                    onLetter(letter)
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .width(30.dp)
            .height(height)
            .pointerInput(presentLetters, height) {
                detectVerticalDragGestures(
                    onDragStart = { offset -> scrubAt(offset.y, size.height.toFloat()) },
                    onVerticalDrag = { change, _ ->
                        change.consume()
                        scrubAt(change.position.y, size.height.toFloat())
                    },
                    onDragEnd = {
                        dragY = null
                        bubbleLetter = null
                    },
                    onDragCancel = {
                        dragY = null
                        bubbleLetter = null
                    },
                )
            }
            .pointerInput(presentLetters, height) {
                detectTapGestures { offset ->
                    scrubAt(offset.y, size.height.toFloat())
                    dragY = null
                    bubbleLetter = null
                }
            },
    ) {
        Column(
            modifier = Modifier
                .width(BentoSizes.AZ_SCRUBBER_WIDTH.dp)
                .fillMaxHeight()
                .padding(vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BENTO_AZ_LETTERS.forEach { letter ->
                val present = letter in presentLetters
                Text(
                    letter,
                    style = BentoTypography.index,
                    color = when {
                        present -> colors.accent
                        else -> colors.muted.copy(alpha = 0.45f)
                    },
                    maxLines = 1,
                )
            }
        }
        val y = dragY
        val letter = bubbleLetter
        if (y != null && letter != null) {
            with(density) {
                Box(
                    modifier = Modifier
                        .offset { IntOffset((-60).dp.roundToPx(), (y - 28.dp.toPx()).roundToInt()) }
                        .size(56.dp)
                        .clip(RoundedCornerShape(28.dp, 28.dp, 6.dp, 28.dp))
                        .background(colors.accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        letter,
                        style = BentoTypography.display,
                        color = colors.onAccent,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Grouped tiles (DESIGN.md 5.3)
// ---------------------------------------------------------------------------

/**
 * The genre swatch palette (the previous Genre page colors, reused for the
 * gradient swatch covers).
 */
private object BentoGenreSwatch {
    val colors = listOf(
        0xFFEF4444,
        0xFFF97316,
        0xFFF59E0B,
        0xFF16A34A,
        0xFF06B6B4,
        0xFF8B5CF6,
        0xFFD946EF,
        0xFFF43F5E,
        0xFF6366F1,
        0xFFA855F7,
    ).map { Color(it) }

    fun colorAt(index: Int): Color {
        val bounded = ((index % colors.size) + colors.size) % colors.size
        return colors[bounded]
    }
}

private fun Color.darkened(): Color = Color(red * 0.55f, green * 0.55f, blue * 0.55f, alpha)

/**
 * A grouped tile (DESIGN.md 5.3): a 64 dp cover, the group title and
 * subtitle, a 44 dp accent play button, and up to six song rows; longer
 * groups show five rows and a "Show all" row that opens the group's page.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoGroupTile(
    context: ViewContext,
    title: String,
    subtitle: String,
    songs: List<Song>,
    cover: @Composable () -> Unit,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    onPlayAt: (index: Int) -> Unit,
    trackNumberOf: ((index: Int, song: Song) -> Int?)? = null,
    rowSecondaryOf: ((song: Song) -> String?)? = null,
    playingSongId: String?,
    playing: Boolean,
    narrow: Boolean,
    showAllLabel: String?,
    onShowAll: (() -> Unit)?,
) {
    val colors = LocalBentoColors.current
    val colorScheme = LocalBentoColorScheme.current
    val tileShape = RoundedCornerShape(BentoRadii.TILE.dp)
    val openInteraction = remember { MutableInteractionSource() }
    val openHovered by openInteraction.collectIsHoveredAsState()
    val visibleRows = songs.take(
        when {
            songs.size > BENTO_GROUP_MAX_ROWS -> BENTO_GROUP_MAX_ROWS - 1
            else -> BENTO_GROUP_MAX_ROWS
        },
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(bentoGroupTileHeightDp(songs.size).dp)
            .clip(tileShape)
            .then(
                when (colorScheme) {
                    BentoColorScheme.CONTRAST -> Modifier.border(1.dp, colors.line, tileShape)
                    else -> Modifier
                },
            )
            .background(colors.surface)
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .hoverable(openInteraction)
                    .combinedClickable(
                        interactionSource = openInteraction,
                        indication = null,
                        onClick = onOpen,
                    )
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                cover()
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = BentoTypography.groupTitle,
                        color = when {
                            openHovered -> colors.accent
                            else -> colors.fg
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        subtitle,
                        style = BentoTypography.secondary,
                        color = colors.sub,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.accent)
                    .combinedClickable(onClick = onPlay),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    title,
                    tint = colors.onAccent,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        visibleRows.forEachIndexed { index, song ->
            BentoGroupTileRow(
                context = context,
                song = song,
                trackNumber = trackNumberOf?.invoke(index, song),
                secondary = rowSecondaryOf?.invoke(song),
                playing = song.id == playingSongId,
                isPlaying = playing,
                narrow = narrow,
                onClick = { onPlayAt(index) },
            )
        }
        if (songs.size > BENTO_GROUP_MAX_ROWS && showAllLabel != null && onShowAll != null) {
            BentoGroupShowAllRow(
                label = showAllLabel,
                onClick = onShowAll,
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BentoGroupTileRow(
    context: ViewContext,
    song: Song,
    trackNumber: Int?,
    secondary: String?,
    playing: Boolean,
    isPlaying: Boolean,
    narrow: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(BentoSizes.GROUP_ROW.dp)
            .hoverable(interactionSource)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .background(
                when {
                    hovered || pressed -> colors.raised
                    else -> Color.Transparent
                },
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (trackNumber) {
            null -> Box {
                AsyncImage(
                    song.createArtworkImageRequest(context.symphony).build(),
                    null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(BentoRadii.GROUP_ROW_ART_30.dp)),
                )
                if (playing) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(BentoRadii.GROUP_ROW_ART_30.dp))
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        BentoEqualizerGlyph(color = Color.White, playing = isPlaying)
                    }
                }
            }

            else -> Text(
                trackNumber.toString(),
                style = bentoTabularStyle(BentoTypography.secondary),
                color = when {
                    playing -> colors.accent
                    else -> colors.muted
                },
                maxLines = 1,
                modifier = Modifier.width(22.dp),
                textAlign = TextAlign.Center,
            )
        }
        Text(
            song.title,
            style = BentoTypography.rowTitle.copy(fontWeight = FontWeight.Medium),
            color = when {
                playing -> colors.accent
                else -> colors.fg
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        if (!narrow) {
            secondary?.let {
                Text(
                    it,
                    style = BentoTypography.secondary,
                    color = colors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(0.55f, fill = false),
                )
            }
        }
        Text(
            DurationUtils.formatMs(song.duration),
            style = bentoTabularStyle(BentoTypography.secondary),
            color = colors.muted,
            maxLines = 1,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BentoGroupShowAllRow(
    label: String,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(BentoSizes.GROUP_ROW.dp)
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .combinedClickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            label,
            style = BentoTypography.rowTitle,
            color = colors.accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Rounded.ChevronRight,
            label,
            tint = colors.accent,
            modifier = Modifier.size(18.dp),
        )
    }
}

/** A 64 dp album-art cover of a grouped tile (radius 16). */
@Composable
fun BentoGroupAlbumCover(request: ImageRequest) {
    AsyncImage(
        request,
        null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(BentoRadii.GROUP_COVER_64.dp)),
    )
}

/** A 64 dp round artist avatar of a grouped tile. */
@Composable
fun BentoGroupArtistCover(request: ImageRequest, artistName: String) {
    AsyncImage(
        request,
        artistName,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape),
    )
}

/** A 64 dp gradient swatch of a grouped genre tile. */
@Composable
fun BentoGroupGenreCover(index: Int) {
    val base = BentoGenreSwatch.colorAt(index)
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(BentoRadii.GROUP_COVER_64.dp))
            .background(
                Brush.linearGradient(listOf(base, base.darkened())),
            ),
    )
}

/** A 64 dp 2 x 2 mosaic of album art for a grouped playlist tile. */
@Composable
fun BentoGroupPlaylistCover(context: ViewContext, songIds: List<String>) {
    val cells = (0 until 4).map { index ->
        songIds.getOrNull(index)
            ?.let { songId ->
                context.symphony.groove.song.get(songId)
                    ?.createArtworkImageRequest(context.symphony)?.build()
            }
            ?: Assets.createPlaceholderImageRequest(context.symphony).build()
    }
    Column(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(BentoRadii.GROUP_COVER_64.dp)),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        cells.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                row.forEach { request ->
                    AsyncImage(
                        request,
                        null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(31.dp)
                            .clip(RoundedCornerShape(2.dp)),
                    )
                }
                if (row.size == 1) {
                    Spacer(Modifier.size(31.dp))
                }
            }
        }
    }
}

/** A 64 dp folder cover of a grouped tile: the folder icon on `tonal`. */
@Composable
fun BentoGroupFolderCover() {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(BentoRadii.GROUP_COVER_64.dp))
            .background(colors.tonal),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.Folder,
            null,
            tint = colors.onTonal,
            modifier = Modifier.size(28.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Grouped tile columns (DESIGN.md 5.3)
// ---------------------------------------------------------------------------

/**
 * The grouped-page columns (DESIGN.md 5.3): 1 column below 560 dp inner
 * width, 2 at 560-999, 3 at 1000 and up. With at most 2 x columns groups
 * the tiles form an even grid; with more they flow down masonry columns.
 * Each entry renders through [entry] with its resolved tile width.
 */
@Composable
fun <T> BentoGroupedTileColumns(
    entries: List<T>,
    entryHeightDp: (T) -> Float,
    modifier: Modifier = Modifier,
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
        val contentAtEnd = io.github.zyrouge.symphony.ui.view.LocalHomeContentAtEnd.current
        val contentAtTop = io.github.zyrouge.symphony.ui.view.LocalHomeContentAtTop.current
        androidx.compose.runtime.LaunchedEffect(scrollState) {
            androidx.compose.runtime.snapshotFlow {
                scrollState.maxValue > 0 && scrollState.value >= scrollState.maxValue - 4
            }.collect { contentAtEnd.value = it }
        }
        androidx.compose.runtime.LaunchedEffect(scrollState) {
            androidx.compose.runtime.snapshotFlow { scrollState.value <= 4 }
                .collect { contentAtTop.value = it }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = gutter, end = gutter, bottom = 8.dp),
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

// ---------------------------------------------------------------------------
// Search filters and chips (DESIGN.md 5.5)
// ---------------------------------------------------------------------------

/**
 * A smart filter tile (DESIGN.md 5.5): 64 dp (56 in the side column), an
 * icon well, the filter name and its song count; the active filter turns
 * accent and also orders the results.
 */
@Composable
fun BentoSmartFilterTile(
    icon: ImageVector,
    label: String,
    count: String,
    active: Boolean,
    sideColumn: Boolean = false,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val (fg, subColor, well) = when {
        active -> Triple(colors.onAccent, colors.onAccentSub, colors.onAccent)
        else -> Triple(colors.fg, colors.sub, colors.fg)
    }
    BentoToneSurface(
        modifier = Modifier
            .fillMaxWidth()
            .height(
                when {
                    sideColumn -> BentoSizes.SMART_FILTER_SIDE_COLUMN.dp
                    else -> BentoSizes.SMART_FILTER.dp
                },
            ),
        tone = when {
            active -> BentoTileTone.ACCENT
            else -> BentoTileTone.PLAIN
        },
        onClick = onClick,
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(BentoRadii.ICON_WELL_40.dp))
                    .background(well.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, label, tint = fg, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(
                    label,
                    style = BentoTypography.body.copy(fontWeight = FontWeight.SemiBold),
                    color = fg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    count,
                    style = BentoTypography.secondary,
                    color = subColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * A genre chip (DESIGN.md 5.5): 36 dp, radius 18, `surface` with the
 * selected chip in `tonal`.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoGenreChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    Box(
        modifier = Modifier
            .height(BentoSizes.GENRE_CHIP.dp)
            .hoverable(interactionSource)
            .clip(RoundedCornerShape(BentoRadii.CHIP.dp))
            .background(
                when {
                    selected -> colors.tonal
                    hovered -> colors.raised
                    else -> colors.surface
                },
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = BentoTypography.body.copy(fontWeight = FontWeight.SemiBold),
            color = when {
                selected -> colors.onTonal
                else -> colors.fg
            },
            maxLines = 1,
        )
    }
}

/**
 * An artist or album hit above the song results (DESIGN.md 5.5): 60 dp,
 * `raised`, with a 40 dp avatar or artwork.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoSearchHitChip(
    request: ImageRequest,
    title: String,
    subtitle: String,
    round: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    Row(
        modifier = Modifier
            .height(60.dp)
            .widthIn(max = 240.dp)
            .hoverable(interactionSource)
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .background(
                when {
                    hovered -> colors.high
                    else -> colors.raised
                },
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        AsyncImage(
            request,
            title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(40.dp)
                .clip(
                    when {
                        round -> CircleShape
                        else -> RoundedCornerShape(10.dp)
                    },
                ),
        )
        Column {
            Text(
                title,
                style = BentoTypography.rowTitle.copy(fontSize = 14.sp),
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                subtitle,
                style = BentoTypography.label,
                color = colors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * A mini Play/Shuffle button of the results header (DESIGN.md 4.3): 36 dp,
 * radius 18, accent or tonal.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoMiniButton(
    icon: ImageVector,
    label: String,
    tone: BentoTileTone,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val (bg, fg) = when (tone) {
        BentoTileTone.ACCENT -> colors.accent to colors.onAccent
        BentoTileTone.TONAL -> colors.tonal to colors.onTonal
        else -> colors.surface to colors.fg
    }
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(BentoRadii.CHIP.dp))
            .background(bg)
            .combinedClickable(onClick = onClick)
            .padding(start = 10.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(icon, label, tint = fg, modifier = Modifier.size(18.dp))
        Text(
            label,
            style = BentoTypography.body.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
            maxLines = 1,
        )
    }
}

// ---------------------------------------------------------------------------
// Sort menu (DESIGN.md 4.5)
// ---------------------------------------------------------------------------

/** One sort option of [BentoSortMenu]. */
@Immutable
data class BentoSortOption<T>(val value: T, val label: String)

/**
 * The sort menu (DESIGN.md 4.5): "Sort songs by", one 48 dp row per option
 * with a check mark on the current one, then "Reverse order" with an On/Off
 * value pill. Rendered in a Material dropdown with the bento surface, shape
 * and item styling.
 */
@Composable
fun <T> BentoSortMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    options: List<BentoSortOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    reverse: Boolean,
    reverseLabel: String,
    onReverseChange: (Boolean) -> Unit,
) {
    val colors = LocalBentoColors.current
    val colorScheme = LocalBentoColorScheme.current
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        containerColor = colors.high,
        shape = RoundedCornerShape(BentoRadii.MENU.dp),
        shadowElevation = 16.dp,
        border = when (colorScheme) {
            BentoColorScheme.CONTRAST -> BorderStroke(1.dp, colors.line)
            else -> null
        },
    ) {
        Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Column {
                Text(
                    title,
                    style = BentoTypography.secondary.copy(fontWeight = FontWeight.Bold),
                    color = colors.muted,
                    maxLines = 1,
                    modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 8.dp),
                )
                options.forEach { option ->
                    MenuRow(
                        leading = {
                            if (option.value == selected) {
                                Icon(
                                    Icons.Rounded.Check,
                                    option.label,
                                    tint = colors.sub,
                                    modifier = Modifier.size(22.dp),
                                )
                            } else {
                                Spacer(Modifier.size(22.dp))
                            }
                        },
                        label = option.label,
                        value = null,
                        onClick = {
                            onDismissRequest()
                            onSelect(option.value)
                        },
                    )
                }
                MenuRow(
                    leading = {
                        Icon(
                            Icons.AutoMirrored.Rounded.Sort,
                            reverseLabel,
                            tint = colors.sub,
                            modifier = Modifier.size(22.dp),
                        )
                    },
                    label = reverseLabel,
                    value = if (reverse) "On" else "Off",
                    onClick = {
                        onDismissRequest()
                        onReverseChange(!reverse)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun MenuRow(
    leading: @Composable () -> Unit,
    label: String,
    value: String?,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(BentoSizes.MENU_ITEM.dp)
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .combinedClickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        leading()
        Text(
            label,
            style = BentoTypography.rowTitle.copy(fontWeight = FontWeight.Medium),
            color = colors.fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        value?.let {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.tonal)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    it,
                    style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
                    color = colors.onTonal,
                    maxLines = 1,
                )
            }
        }
    }
}
