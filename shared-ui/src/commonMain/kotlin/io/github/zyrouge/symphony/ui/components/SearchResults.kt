package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.i18n.Strings
import io.github.zyrouge.symphony.ui.model.BentoSearchFilter
import io.github.zyrouge.symphony.ui.model.SearchHit
import io.github.zyrouge.symphony.ui.model.UiSong
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * The results tile (DESIGN.md 5.5): artist and album hits for the query
 * above a header with the song count and Play / Shuffle buttons, then the
 * song rows. Artwork, the row menu and the actions stay with the platform.
 */
@Composable
fun BentoSearchResultsTile(
    songs: List<UiSong>,
    ready: Boolean,
    hits: List<SearchHit>,
    selectedFilter: BentoSearchFilter?,
    narrowed: Boolean,
    playingSongId: String?,
    isPlaying: Boolean,
    nowMs: Long,
    t: Strings,
    durationText: (UiSong) -> String,
    moreContentDescription: String,
    artwork: @Composable (UiSong, Modifier) -> Unit,
    hitArtwork: @Composable (SearchHit, Modifier) -> Unit,
    menu: (@Composable (UiSong, Boolean, () -> Unit) -> Unit)? = null,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onSongClick: (UiSong) -> Unit,
    onHitClick: (SearchHit) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val colors = LocalBentoColors.current

    if (!ready) {
        BentoSearchResultsLoading(t = t, modifier = modifier.fillMaxWidth())
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BentoRadii.TILE.dp))
            .background(colors.surface),
    ) {
        BentoSearchResultsHits(hits = hits, hitArtwork = hitArtwork, onHitClick = onHitClick)
        BentoSearchResultsHeader(
            songCount = songs.size,
            narrowed = narrowed,
            t = t,
            onPlayAll = onPlayAll,
            onShuffleAll = onShuffleAll,
        )
        if (songs.isEmpty()) {
            BentoSearchResultsEmpty(t = t)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = contentPadding,
            ) {
                itemsIndexed(
                    songs,
                    key = { _, song -> song.id },
                ) { _, song ->
                    BentoSearchResultRow(
                        song = song,
                        selectedFilter = selectedFilter,
                        playingSongId = playingSongId,
                        isPlaying = isPlaying,
                        nowMs = nowMs,
                        t = t,
                        durationText = durationText(song),
                        moreContentDescription = moreContentDescription,
                        artwork = { contentModifier -> artwork(song, contentModifier) },
                        menu = menu?.let { slot ->
                            { expanded, dismiss -> slot(song, expanded, dismiss) }
                        },
                        onClick = { onSongClick(song) },
                    )
                }
            }
        }
    }
}

/**
 * The results tile as items of an enclosing list, so the filters above it
 * scroll away with the songs (compact and short windows). Each item paints
 * its slice of the `surface` tile: the first item carries the top corners
 * and a closing 8 dp strip the bottom ones. [modifier] sets the page gutter.
 */
@OptIn(ExperimentalFoundationApi::class)
fun LazyListScope.bentoSearchResultsTileItems(
    songs: List<UiSong>,
    ready: Boolean,
    hits: List<SearchHit>,
    selectedFilter: BentoSearchFilter?,
    narrowed: Boolean,
    playingSongId: String?,
    isPlaying: Boolean,
    nowMs: Long,
    t: Strings,
    durationText: (UiSong) -> String,
    moreContentDescription: String,
    artwork: @Composable (UiSong, Modifier) -> Unit,
    hitArtwork: @Composable (SearchHit, Modifier) -> Unit,
    menu: (@Composable (UiSong, Boolean, () -> Unit) -> Unit)? = null,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
    onSongClick: (UiSong) -> Unit,
    onHitClick: (SearchHit) -> Unit,
    modifier: Modifier = Modifier,
) {
    val radius = BentoRadii.TILE.dp
    val topShape = RoundedCornerShape(topStart = radius, topEnd = radius)
    val bottomShape = RoundedCornerShape(bottomStart = radius, bottomEnd = radius)

    if (!ready) {
        item(key = "results-loading") {
            BentoSearchResultsLoading(
                t = t,
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 36.dp),
            )
        }
        return
    }

    item(key = "results-head") {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .clip(topShape)
                .background(LocalBentoColors.current.surface),
        ) {
            BentoSearchResultsHits(hits = hits, hitArtwork = hitArtwork, onHitClick = onHitClick)
            BentoSearchResultsHeader(
                songCount = songs.size,
                narrowed = narrowed,
                t = t,
                onPlayAll = onPlayAll,
                onShuffleAll = onShuffleAll,
            )
        }
    }
    if (songs.isEmpty()) {
        item(key = "results-empty") {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(bottomShape)
                    .background(LocalBentoColors.current.surface),
            ) {
                BentoSearchResultsEmpty(t = t)
            }
        }
    } else {
        itemsIndexed(
            songs,
            key = { _, song -> song.id },
        ) { _, song ->
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .background(LocalBentoColors.current.surface),
            ) {
                BentoSearchResultRow(
                    song = song,
                    selectedFilter = selectedFilter,
                    playingSongId = playingSongId,
                    isPlaying = isPlaying,
                    nowMs = nowMs,
                    t = t,
                    durationText = durationText(song),
                    moreContentDescription = moreContentDescription,
                    artwork = { contentModifier -> artwork(song, contentModifier) },
                    menu = menu?.let { slot ->
                        { expanded, dismiss -> slot(song, expanded, dismiss) }
                    },
                    onClick = { onSongClick(song) },
                )
            }
        }
        item(key = "results-end") {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(bottomShape)
                    .background(LocalBentoColors.current.surface),
            )
        }
    }
}

/** Shown until the first query pass has finished. */
@Composable
private fun BentoSearchResultsLoading(t: Strings, modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        IconTextBody(
            icon = { iconModifier ->
                Icon(
                    Icons.Filled.Search,
                    null,
                    modifier = iconModifier,
                )
            },
            content = { Text(t.FilteringResults) },
        )
    }
}

/** The artist and album hits for the query, in a horizontally scrolling row. */
@Composable
private fun BentoSearchResultsHits(
    hits: List<SearchHit>,
    hitArtwork: @Composable (SearchHit, Modifier) -> Unit,
    onHitClick: (SearchHit) -> Unit,
) {
    if (hits.isEmpty()) {
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        hits.forEach { hit ->
            BentoSearchHitChip(
                title = hit.title,
                subtitle = hit.subtitle,
                round = hit.round,
                artwork = { contentModifier -> hitArtwork(hit, contentModifier) },
                onClick = { onHitClick(hit) },
            )
        }
    }
}

/** The song count ("All songs" when nothing narrows the list) with Play and Shuffle. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BentoSearchResultsHeader(
    songCount: Int,
    narrowed: Boolean,
    t: Strings,
    onPlayAll: () -> Unit,
    onShuffleAll: () -> Unit,
) {
    val colors = LocalBentoColors.current
    // The Play and Shuffle buttons move under the title when they crowd it.
    BentoAdaptiveRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 4.dp),
        gap = 8.dp,
        leading = null,
        trailing = if (songCount == 0) null else {
            {
                // Wraps onto a second line when even the full width cannot hold both.
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BentoMiniButton(
                        icon = Icons.Filled.PlayArrow,
                        label = t.Play,
                        tone = BentoTileTone.ACCENT,
                        onClick = onPlayAll,
                    )
                    BentoMiniButton(
                        icon = Icons.Filled.Shuffle,
                        label = t.ShufflePlay,
                        tone = BentoTileTone.TONAL,
                        onClick = onShuffleAll,
                    )
                }
            }
        },
    ) {
        Text(
            when {
                narrowed -> t.XSongs(songCount.toString())
                else -> t.AllSongs
            },
            style = BentoTypography.rowTitle,
            color = colors.fg,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** The message shown when nothing matches. */
@Composable
private fun BentoSearchResultsEmpty(t: Strings) {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 36.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            t.NoSongsMatchSearch,
            style = BentoTypography.rowTitle.copy(fontWeight = FontWeight.Normal),
            color = colors.muted,
            textAlign = TextAlign.Center,
        )
    }
}

/** One song of the results; tapping it plays the results from that song. */
@Composable
private fun BentoSearchResultRow(
    song: UiSong,
    selectedFilter: BentoSearchFilter?,
    playingSongId: String?,
    isPlaying: Boolean,
    nowMs: Long,
    t: Strings,
    durationText: String,
    moreContentDescription: String,
    artwork: @Composable (Modifier) -> Unit,
    menu: (@Composable (Boolean, () -> Unit) -> Unit)?,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val badge = when (selectedFilter) {
        BentoSearchFilter.RECENTLY_ADDED ->
            formatBentoDaysAgo(song.dateModifiedMillis, nowMs, t)

        else -> null
    }
    SongRow(
        song = song,
        playing = song.id == playingSongId,
        isPlaying = isPlaying,
        durationText = durationText,
        moreContentDescription = moreContentDescription,
        artwork = artwork,
        trailing = badge?.let { text ->
            {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.tonal)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(
                        text,
                        style = bentoTabularStyle(BentoTypography.label),
                        color = colors.onTonal,
                        maxLines = 1,
                    )
                }
            }
        },
        menu = menu,
        onClick = onClick,
    )
}
