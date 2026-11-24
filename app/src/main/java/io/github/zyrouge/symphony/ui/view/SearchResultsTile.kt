package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.components.BentoAdaptiveRow
import io.github.zyrouge.symphony.ui.components.BentoMiniButton
import io.github.zyrouge.symphony.ui.components.BentoSearchHitChip
import io.github.zyrouge.symphony.ui.components.BentoSongRow
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.SongDropdownMenu
import io.github.zyrouge.symphony.ui.components.bentoBottomInset
import io.github.zyrouge.symphony.ui.components.bentoTabularStyle
import io.github.zyrouge.symphony.ui.components.formatBentoDaysAgo
import io.github.zyrouge.symphony.ui.components.label
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.LocalStrings
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * The results tile (DESIGN.md 5.5): artist and album hits for the query
 * above a header with the song count and Play / Shuffle buttons, then the
 * song rows.
 */
@Composable
internal fun SearchResultsTile(
    context: ViewContext,
    results: BentoSearchResults?,
    resultSongs: List<Song>,
    artistNames: List<String>,
    albumIds: List<String>,
    selectedFilter: BentoSearchFilter?,
    narrowed: Boolean,
    playingSongId: String?,
    isPlaying: Boolean,
    nowMs: Long,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current
    val favoriteSongIds by context.symphony.groove.playlist.favorites.collectAsState()

    if (results == null) {
        SearchResultsLoading(context = context, modifier = modifier.fillMaxWidth())
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BentoRadii.TILE.dp))
            .background(colors.surface),
    ) {
        SearchResultsHits(context = context, artistNames = artistNames, albumIds = albumIds)
        SearchResultsHeader(context = context, resultSongs = resultSongs, narrowed = narrowed)
        if (resultSongs.isEmpty()) {
            SearchResultsEmpty()
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                // Clear the floating capsule at the end of the results.
                contentPadding = PaddingValues(bottom = bentoBottomInset()),
            ) {
                itemsIndexed(
                    resultSongs,
                    key = { _, song -> song.id },
                ) { index, song ->
                    SearchResultRow(
                        context = context,
                        song = song,
                        index = index,
                        resultSongs = resultSongs,
                        selectedFilter = selectedFilter,
                        playingSongId = playingSongId,
                        isPlaying = isPlaying,
                        isFavorite = favoriteSongIds.contains(song.id),
                        nowMs = nowMs,
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
internal fun LazyListScope.searchResultsTileItems(
    context: ViewContext,
    results: BentoSearchResults?,
    resultSongs: List<Song>,
    artistNames: List<String>,
    albumIds: List<String>,
    selectedFilter: BentoSearchFilter?,
    narrowed: Boolean,
    playingSongId: String?,
    isPlaying: Boolean,
    favoriteSongIds: List<String>,
    nowMs: Long,
    modifier: Modifier = Modifier,
) {
    val radius = BentoRadii.TILE.dp
    val topShape = RoundedCornerShape(topStart = radius, topEnd = radius)
    val bottomShape = RoundedCornerShape(bottomStart = radius, bottomEnd = radius)

    if (results == null) {
        item(key = "results-loading") {
            SearchResultsLoading(
                context = context,
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
            SearchResultsHits(context = context, artistNames = artistNames, albumIds = albumIds)
            SearchResultsHeader(context = context, resultSongs = resultSongs, narrowed = narrowed)
        }
    }
    if (resultSongs.isEmpty()) {
        item(key = "results-empty") {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .clip(bottomShape)
                    .background(LocalBentoColors.current.surface),
            ) {
                SearchResultsEmpty()
            }
        }
    } else {
        itemsIndexed(
            resultSongs,
            key = { _, song -> song.id },
        ) { index, song ->
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .background(LocalBentoColors.current.surface),
            ) {
                SearchResultRow(
                    context = context,
                    song = song,
                    index = index,
                    resultSongs = resultSongs,
                    selectedFilter = selectedFilter,
                    playingSongId = playingSongId,
                    isPlaying = isPlaying,
                    isFavorite = favoriteSongIds.contains(song.id),
                    nowMs = nowMs,
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
private fun SearchResultsLoading(context: ViewContext, modifier: Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        IconTextBody(
            icon = { iconModifier ->
                Icon(
                    Icons.Filled.Search,
                    null,
                    modifier = iconModifier,
                )
            },
            content = { Text(context.symphony.t.FilteringResults) },
        )
    }
}

/** The artist and album hits for the query, in a horizontally scrolling row. */
@Composable
private fun SearchResultsHits(
    context: ViewContext,
    artistNames: List<String>,
    albumIds: List<String>,
) {
    if (artistNames.isEmpty() && albumIds.isEmpty()) {
        return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(start = 16.dp, top = 16.dp, bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        artistNames.forEach { artistName ->
            BentoSearchHitChip(
                request = context.symphony.groove.artist
                    .createArtworkImageRequest(artistName)
                    .build(),
                title = artistName,
                subtitle = context.symphony.t.Artist,
                round = true,
                onClick = {
                    context.navController.navigate(ArtistViewRoute(artistName))
                },
            )
        }
        albumIds.forEach { albumId ->
            context.symphony.groove.album.get(albumId)?.let { album ->
                BentoSearchHitChip(
                    request = album.createArtworkImageRequest(context.symphony).build(),
                    title = album.name,
                    subtitle = context.symphony.t.Album,
                    round = false,
                    onClick = {
                        context.navController.navigate(AlbumViewRoute(album.id))
                    },
                )
            }
        }
    }
}

/** The song count ("All songs" when nothing narrows the list) with Play and Shuffle. */
@Composable
private fun SearchResultsHeader(
    context: ViewContext,
    resultSongs: List<Song>,
    narrowed: Boolean,
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
        trailing = if (resultSongs.isEmpty()) null else {
            {
                // Wraps onto a second line when even the full width cannot hold both.
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    BentoMiniButton(
                        icon = Icons.Filled.PlayArrow,
                        label = context.symphony.t.Play,
                        tone = BentoTileTone.ACCENT,
                        onClick = {
                            context.symphony.radio.shorty.playQueue(
                                resultSongs.map { it.id },
                                source = context.symphony.t.SearchResults,
                            )
                        },
                    )
                    BentoMiniButton(
                        icon = Icons.Filled.Shuffle,
                        label = context.symphony.t.ShufflePlay,
                        tone = BentoTileTone.TONAL,
                        onClick = {
                            context.symphony.radio.shorty.playQueue(
                                resultSongs.map { it.id },
                                shuffle = true,
                                source = context.symphony.t.SearchResults,
                            )
                        },
                    )
                }
            }
        },
    ) {
        Text(
            when {
                narrowed -> context.symphony.t.XSongs(resultSongs.size.toString())
                else -> context.symphony.t.AllSongs
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
private fun SearchResultsEmpty() {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 36.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            LocalStrings.current.NoSongsMatchSearch,
            style = BentoTypography.rowTitle.copy(fontWeight = FontWeight.Normal),
            color = colors.muted,
            textAlign = TextAlign.Center,
        )
    }
}

/** One song of the results; tapping it plays the results from that song. */
@Composable
private fun SearchResultRow(
    context: ViewContext,
    song: Song,
    index: Int,
    resultSongs: List<Song>,
    selectedFilter: BentoSearchFilter?,
    playingSongId: String?,
    isPlaying: Boolean,
    isFavorite: Boolean,
    nowMs: Long,
) {
    val colors = LocalBentoColors.current
    val playing = song.id == playingSongId
    val badge = when (selectedFilter) {
        BentoSearchFilter.RECENTLY_ADDED ->
            formatBentoDaysAgo(song.dateModified, nowMs, context.symphony.t)

        else -> null
    }
    BentoSongRow(
        context = context,
        song = song,
        playing = playing,
        isPlaying = isPlaying,
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
        menu = { expanded, onDismissRequest ->
            SongDropdownMenu(
                context = context,
                song = song,
                isFavorite = isFavorite,
                expanded = expanded,
                onDismissRequest = onDismissRequest,
            )
        },
        onClick = {
            context.symphony.radio.shorty.playQueue(
                resultSongs.map { it.id },
                Radio.PlayOptions(index = index),
                source = context.symphony.t.SearchResults,
            )
        },
    )
}
