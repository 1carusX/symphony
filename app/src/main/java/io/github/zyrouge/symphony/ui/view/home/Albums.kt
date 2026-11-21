package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.components.BentoGroupAlbumCover
import io.github.zyrouge.symphony.ui.components.BentoGroupTile
import io.github.zyrouge.symphony.ui.components.BentoGroupedTileColumns
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.bentoAlbumsGroupComparator
import io.github.zyrouge.symphony.ui.components.bentoGroupTileHeightDp
import io.github.zyrouge.symphony.ui.components.rememberBentoPlayingSong
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.AlbumViewRoute

/**
 * The Albums page (DESIGN.md 5.3): one grouped tile per album, ordered by
 * artist then year, with the album's tracks as numbered rows. Tapping the
 * cover or title opens the album; the play button plays the album in track
 * order.
 */
@Composable
fun AlbumsView(context: ViewContext) {
    val isUpdating by context.symphony.groove.album.isUpdating.collectAsState()
    val albumIds by context.symphony.groove.album.all.collectAsState()
    val caseSensitive by context.symphony.settings.caseSensitiveSorting.flow.collectAsState()
    val playingSong = rememberBentoPlayingSong(context)
    val albums by remember(albumIds) {
        derivedStateOf {
            albumIds.mapNotNull { context.symphony.groove.album.get(it) }
        }
    }
    val sortedAlbums by remember(albums, caseSensitive) {
        derivedStateOf {
            albums.sortedWith(
                bentoAlbumsGroupComparator(
                    artistNameOf = { it.artists.firstOrNull() },
                    yearOf = { it.startYear ?: it.endYear },
                    caseSensitive = caseSensitive,
                ),
            )
        }
    }

    LoaderScaffold(context, isLoading = isUpdating) {
        when {
            albums.isEmpty() -> IconTextBody(
                icon = { modifier ->
                    Icon(
                        Icons.Filled.Album,
                        null,
                        modifier = modifier,
                    )
                },
                content = { Text(context.symphony.t.DamnThisIsSoEmpty) },
            )

            else -> BentoGroupedTileColumns(
                entries = sortedAlbums,
                entryHeightDp = { bentoGroupTileHeightDp(it.numberOfTracks) },
                modifier = Modifier.fillMaxSize(),
            ) { album, tileWidth ->
                AlbumGroupTile(
                    context = context,
                    album = album,
                    playingSongId = playingSong.songId,
                    playing = playingSong.isPlaying,
                    narrow = tileWidth < 340f,
                )
            }
        }
    }
}

@Composable
private fun AlbumGroupTile(
    context: ViewContext,
    album: Album,
    playingSongId: String?,
    playing: Boolean,
    narrow: Boolean,
) {
    val trackSongIds by remember(album.id) {
        derivedStateOf {
            context.symphony.groove.song.sort(
                album.getSongIds(context.symphony),
                SongRepository.SortBy.TRACK_NUMBER,
                false,
            )
        }
    }
    val songs by remember(trackSongIds) {
        derivedStateOf {
            trackSongIds.mapNotNull { context.symphony.groove.song.get(it) }
        }
    }
    val year = album.startYear ?: album.endYear
    val subtitle = listOfNotNull(
        album.artists.joinToString().takeIf { it.isNotEmpty() },
        year?.toString(),
    ).joinToString(" · ")

    BentoGroupTile(
        context = context,
        title = album.name,
        subtitle = subtitle,
        songs = songs,
        cover = {
            BentoGroupAlbumCover(
                album.createArtworkImageRequest(context.symphony).build(),
            )
        },
        onOpen = { context.navController.navigate(AlbumViewRoute(album.id)) },
        onPlay = {
            context.symphony.radio.shorty.playQueue(trackSongIds)
        },
        onPlayAt = { index ->
            context.symphony.radio.shorty.playQueue(
                trackSongIds,
                Radio.PlayOptions(index = index),
            )
        },
        trackNumberOf = { _, song -> song.trackNumber },
        playingSongId = playingSongId,
        playing = playing,
        narrow = narrow,
        showAllLabel = "Show all " +
            context.symphony.t.XSongs(album.numberOfTracks.toString()),
        onShowAll = { context.navController.navigate(AlbumViewRoute(album.id)) },
    )
}
