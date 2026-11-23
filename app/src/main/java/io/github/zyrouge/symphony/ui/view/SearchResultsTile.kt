package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.components.BentoMiniButton
import io.github.zyrouge.symphony.ui.components.BentoSearchHitChip
import io.github.zyrouge.symphony.ui.components.BentoSongRow
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.bentoTabularStyle
import io.github.zyrouge.symphony.ui.components.formatBentoDaysAgo
import io.github.zyrouge.symphony.ui.components.label
import io.github.zyrouge.symphony.ui.helpers.ViewContext
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
    playing: Boolean,
    nowMs: Long,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current

    if (results == null) {
        Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
        return
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BentoRadii.TILE.dp))
            .background(colors.surface),
    ) {
        if (artistNames.isNotEmpty() || albumIds.isNotEmpty()) {
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 60.dp)
                .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                when {
                    narrowed -> context.symphony.t.XSongs(resultSongs.size.toString())
                    else -> "All songs"
                },
                style = BentoTypography.rowTitle,
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (resultSongs.isNotEmpty()) {
                BentoMiniButton(
                    icon = Icons.Filled.PlayArrow,
                    label = context.symphony.t.Play,
                    tone = BentoTileTone.ACCENT,
                    onClick = {
                        context.symphony.radio.shorty.playQueue(
                            resultSongs.map { it.id },
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
                        )
                    },
                )
            }
        }
        if (resultSongs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 36.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "No songs match. Try another word or filter.",
                    style = BentoTypography.rowTitle.copy(fontWeight = FontWeight.Normal),
                    color = colors.muted,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(
                    resultSongs,
                    key = { _, song -> song.id },
                ) { index, song ->
                    val playing = song.id == playingSongId
                    val badge = when (selectedFilter) {
                        BentoSearchFilter.RECENTLY_ADDED ->
                            formatBentoDaysAgo(song.dateModified, nowMs)

                        else -> null
                    }
                    BentoSongRow(
                        context = context,
                        song = song,
                        playing = playing,
                        isPlaying = playing,
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
                        onClick = {
                            context.symphony.radio.shorty.playQueue(
                                resultSongs.map { it.id },
                                Radio.PlayOptions(index = index),
                            )
                        },
                    )
                }
            }
        }
    }
}
