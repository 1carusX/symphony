package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.zyrouge.symphony.services.groove.AlbumArtist
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.components.AlbumArtistDropdownMenu
import io.github.zyrouge.symphony.ui.components.BentoGroupArtistCover
import io.github.zyrouge.symphony.ui.components.BentoGroupTile
import io.github.zyrouge.symphony.ui.components.BentoGroupedTileColumns
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.bentoGroupTileHeightDp
import io.github.zyrouge.symphony.ui.components.bentoNamesGroupComparator
import io.github.zyrouge.symphony.ui.components.rememberBentoPlayingSong
import io.github.zyrouge.symphony.ui.helpers.LocalGrooveVersion
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.AlbumArtistViewRoute

/**
 * The Artists page (DESIGN.md 5.3): one grouped tile per artist, in name
 * order, with the artist's songs as rows showing the album title. Tapping
 * the cover or name opens the artist; the play button plays the artist's
 * songs in the current Songs-page sort order.
 */
@Composable
fun AlbumArtistsView(context: ViewContext) {
    val isUpdating by context.symphony.groove.albumArtist.isUpdating.collectAsState()
    val artistNames by context.symphony.groove.albumArtist.all.collectAsState()
    val caseSensitive by context.symphony.settings.caseSensitiveSorting.flow.collectAsState()
    val playingSong = rememberBentoPlayingSong(context)
    val artists by remember(artistNames) {
        derivedStateOf {
            artistNames.mapNotNull { context.symphony.groove.albumArtist.get(it) }
        }
    }
    val sortedArtists by remember(artists, caseSensitive) {
        derivedStateOf {
            artists.sortedWith(
                bentoNamesGroupComparator(
                    nameOf = { it.name },
                    caseSensitive = caseSensitive,
                ),
            )
        }
    }

    LoaderScaffold(context, isLoading = isUpdating) {
        when {
            artists.isEmpty() -> IconTextBody(
                icon = { modifier ->
                    Icon(
                        Icons.Filled.Person,
                        null,
                        modifier = modifier,
                    )
                },
                content = { Text(context.symphony.t.DamnThisIsSoEmpty) },
            )

            else -> BentoGroupedTileColumns(
                entries = sortedArtists,
                entryHeightDp = { bentoGroupTileHeightDp(it.numberOfTracks) },
                modifier = Modifier.fillMaxSize(),
            ) { artist, tileWidth ->
                AlbumArtistGroupTile(
                    context = context,
                    artist = artist,
                    playingSongId = playingSong.songId,
                    playing = playingSong.isPlaying,
                    narrow = tileWidth < 340f,
                )
            }
        }
    }
}

@Composable
private fun AlbumArtistGroupTile(
    context: ViewContext,
    artist: AlbumArtist,
    playingSongId: String?,
    playing: Boolean,
    narrow: Boolean,
) {
    val sortBy by context.symphony.settings.lastUsedSongsSortBy.flow.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedSongsSortReverse.flow.collectAsState()
    val grooveVersion = LocalGrooveVersion.current
    val songIds by remember(artist.name, sortBy, sortReverse, grooveVersion) {
        derivedStateOf {
            context.symphony.groove.song.sort(
                artist.getSongIds(context.symphony),
                sortBy,
                sortReverse,
            )
        }
    }
    val songs by remember(songIds) {
        derivedStateOf {
            songIds.mapNotNull { context.symphony.groove.song.get(it) }
        }
    }

    BentoGroupTile(
        context = context,
        title = artist.name,
        subtitle = context.symphony.t.XAlbums(artist.numberOfAlbums.toString()) +
            " · " +
            context.symphony.t.XSongs(artist.numberOfTracks.toString()),
        songs = songs,
        cover = {
            BentoGroupArtistCover(
                context.symphony.groove.albumArtist
                    .createArtworkImageRequest(artist.name)
                    .build(),
                artist.name,
            )
        },
        onOpen = { context.navController.navigate(AlbumArtistViewRoute(artist.name)) },
        onPlay = {
            context.symphony.radio.shorty.playQueue(songIds, source = artist.name)
        },
        onPlayAt = { index ->
            context.symphony.radio.shorty.playQueue(
                songIds,
                Radio.PlayOptions(index = index),
                source = artist.name,
            )
        },
        rowSecondaryOf = { it.album },
        playingSongId = playingSongId,
        playing = playing,
        narrow = narrow,
        showAllLabel = context.symphony.t.ShowAllX(context.symphony.t.XSongs(artist.numberOfTracks.toString())),
        onShowAll = { context.navController.navigate(AlbumArtistViewRoute(artist.name)) },
        menu = { expanded, onDismissRequest ->
            AlbumArtistDropdownMenu(context, artist, expanded, onDismissRequest)
        },
    )
}
