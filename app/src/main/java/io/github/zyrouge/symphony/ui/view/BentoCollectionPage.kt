package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.services.Settings
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.adapters.toUiSong
import io.github.zyrouge.symphony.ui.components.SongDropdownMenu
import io.github.zyrouge.symphony.ui.components.bentoBottomInset
import io.github.zyrouge.symphony.ui.components.rememberBentoPlayingSong
import io.github.zyrouge.symphony.ui.helpers.LocalGrooveVersion
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.components.BentoTileGeometry
import io.github.zyrouge.symphony.utils.DurationUtils

/**
 * Android wrapper of the shared collection page: it collects the song sort
 * settings, maps the repository songs to [io.github.zyrouge.symphony.ui.model.UiSong]
 * and supplies Coil artwork and the song menu.
 */
@Composable
fun BentoCollectionPage(
    context: ViewContext,
    label: String,
    title: String,
    songIds: List<String>,
    onBack: () -> Unit,
    emptyMessage: String,
    cover: @Composable (BentoTileGeometry) -> Unit,
    modifier: Modifier = Modifier,
    metaPrefix: String? = null,
    trailingMenu: (@Composable () -> Unit)? = null,
    sideTiles: List<DetailSideTile> = emptyList(),
    onEditSongs: (() -> Unit)? = null,
    rowSecondary: (Song) -> String = ::defaultDetailRowSecondary,
    rowMenuExtra: (@Composable ColumnScope.(Song, () -> Unit) -> Unit)? = null,
    sortByEntry: Settings.EnumEntry<SongRepository.SortBy> = context.symphony.settings.lastUsedSongsSortBy,
    sortReverseEntry: Settings.BooleanEntry = context.symphony.settings.lastUsedSongsSortReverse,
    sortByOverride: SongRepository.SortBy? = null,
    sortReverseOverride: Boolean? = null,
    info: (@Composable (BentoTileGeometry) -> Unit)? = null,
    rowLeading: (@Composable (Song, Boolean, Boolean) -> Unit)? = null,
    tracksLabel: String? = null,
) {
    val grooveVersion = LocalGrooveVersion.current
    val sortBy by sortByEntry.flow.collectAsState()
    val sortReverse by sortReverseEntry.flow.collectAsState()
    val activeSortBy = sortByOverride ?: sortBy
    val activeSortReverse = sortReverseOverride ?: sortReverse
    val songs by remember(songIds, activeSortBy, activeSortReverse, grooveVersion) {
        derivedStateOf {
            context.symphony.groove.song.get(
                context.symphony.groove.song.sort(songIds, activeSortBy, activeSortReverse),
            )
        }
    }
    val uiSongs = remember(songs) { songs.map { it.toUiSong() } }
    val songsById = remember(songs) { songs.associateBy { it.id } }
    val queueIds = remember(songs) { songs.map { it.id } }
    val playingSong = rememberBentoPlayingSong(context)
    val favorites by context.symphony.groove.playlist.favorites.collectAsState()
    val favoriteIds = remember(favorites) { favorites.toSet() }

    BentoCollectionPage(
        label = label,
        title = title,
        songs = uiSongs,
        t = context.symphony.t,
        onBack = onBack,
        emptyMessage = emptyMessage,
        cover = cover,
        playingSongId = playingSong.songId,
        isPlaying = playingSong.isPlaying,
        onPlayAll = {
            if (queueIds.isNotEmpty()) {
                context.symphony.radio.shorty.playQueue(queueIds, source = title)
            }
        },
        onShuffle = {
            if (queueIds.isNotEmpty()) {
                context.symphony.radio.shorty.playQueue(queueIds, shuffle = true, source = title)
            }
        },
        onQueue = { context.symphony.radio.queue.add(queueIds) },
        onSongClick = { song ->
            context.symphony.radio.shorty.playQueue(
                queueIds,
                options = Radio.PlayOptions(
                    index = songs.indexOfFirst { it.id == song.id }.coerceAtLeast(0),
                ),
                source = title,
            )
        },
        modifier = modifier,
        metaPrefix = metaPrefix,
        trailingMenu = trailingMenu,
        sideTiles = sideTiles,
        onEditSongs = onEditSongs,
        rowSecondary = { song -> songsById[song.id]?.let(rowSecondary) ?: "" },
        rowDurationText = { DurationUtils.formatMs(it.durationMillis) },
        rowArtwork = { song, artworkModifier ->
            songsById[song.id]?.let { original ->
                AsyncImage(
                    original.createArtworkImageRequest(context.symphony).build(),
                    null,
                    contentScale = ContentScale.Crop,
                    modifier = artworkModifier,
                )
            }
        },
        rowMenu = { song, expanded, onDismissRequest ->
            songsById[song.id]?.let { original ->
                SongDropdownMenu(
                    context,
                    original,
                    isFavorite = favoriteIds.contains(original.id),
                    trailingContent = rowMenuExtra?.let { extra ->
                        { onDismiss -> extra(original, onDismiss) }
                    },
                    expanded = expanded,
                    onDismissRequest = onDismissRequest,
                )
            }
        },
        bottomInset = bentoBottomInset(16.dp),
        info = info,
        rowLeading = rowLeading?.let { slot ->
            { song, playing, isPlaying ->
                songsById[song.id]?.let { original -> slot(original, playing, isPlaying) }
            }
        },
        tracksLabel = tracksLabel ?: context.symphony.t.Songs,
    )
}

private fun defaultDetailRowSecondary(song: Song) = listOfNotNull(
    song.artists.joinToString().takeIf { it.isNotEmpty() },
    song.album,
).joinToString(" · ")
