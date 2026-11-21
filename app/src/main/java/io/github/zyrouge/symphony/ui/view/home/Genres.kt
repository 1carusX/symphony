package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.zyrouge.symphony.services.groove.Genre
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.components.BentoGroupGenreCover
import io.github.zyrouge.symphony.ui.components.BentoGroupTile
import io.github.zyrouge.symphony.ui.components.BentoGroupedTileColumns
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.bentoGroupTileHeightDp
import io.github.zyrouge.symphony.ui.components.bentoNamesGroupComparator
import io.github.zyrouge.symphony.ui.components.formatBentoDurationMinutes
import io.github.zyrouge.symphony.ui.components.rememberBentoPlayingSong
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.GenreViewRoute

/**
 * The Genres page (DESIGN.md 5.3): one grouped tile per genre, in name
 * order, with a gradient swatch cover and the genre's songs as rows
 * showing the artist. Tapping the cover or name opens the genre; the play
 * button plays the genre's songs in the current Songs-page sort order.
 */
@Composable
fun GenresView(context: ViewContext) {
    val isUpdating by context.symphony.groove.genre.isUpdating.collectAsState()
    val genreNames by context.symphony.groove.genre.all.collectAsState()
    val caseSensitive by context.symphony.settings.caseSensitiveSorting.flow.collectAsState()
    val playingSong = rememberBentoPlayingSong(context)
    val genres by remember(genreNames) {
        derivedStateOf {
            genreNames.mapNotNull { context.symphony.groove.genre.get(it) }
        }
    }
    val sortedGenres by remember(genres, caseSensitive) {
        derivedStateOf {
            genres.sortedWith(
                bentoNamesGroupComparator(
                    nameOf = { it.name },
                    caseSensitive = caseSensitive,
                ),
            )
        }
    }

    LoaderScaffold(context, isLoading = isUpdating) {
        when {
            genres.isEmpty() -> IconTextBody(
                icon = { modifier ->
                    Icon(
                        Icons.Filled.MusicNote,
                        null,
                        modifier = modifier,
                    )
                },
                content = { Text(context.symphony.t.DamnThisIsSoEmpty) },
            )

            else -> BentoGroupedTileColumns(
                entries = sortedGenres,
                entryHeightDp = { bentoGroupTileHeightDp(it.numberOfTracks) },
                modifier = Modifier.fillMaxSize(),
            ) { genre, tileWidth ->
                GenreGroupTile(
                    context = context,
                    genre = genre,
                    playingSongId = playingSong.songId,
                    playing = playingSong.isPlaying,
                    narrow = tileWidth < 340f,
                )
            }
        }
    }
}

@Composable
private fun GenreGroupTile(
    context: ViewContext,
    genre: Genre,
    playingSongId: String?,
    playing: Boolean,
    narrow: Boolean,
) {
    val sortBy by context.symphony.settings.lastUsedSongsSortBy.flow.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedSongsSortReverse.flow.collectAsState()
    val songIds by remember(genre.name, sortBy, sortReverse) {
        derivedStateOf {
            context.symphony.groove.song.sort(
                genre.getSongIds(context.symphony),
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
        title = genre.name,
        subtitle = context.symphony.t.XSongs(genre.numberOfTracks.toString()) +
            " · " +
            formatBentoDurationMinutes(songs.sumOf { it.duration }),
        songs = songs,
        cover = { BentoGroupGenreCover(genre.name.hashCode()) },
        onOpen = { context.navController.navigate(GenreViewRoute(genre.name)) },
        onPlay = {
            context.symphony.radio.shorty.playQueue(songIds)
        },
        onPlayAt = { index ->
            context.symphony.radio.shorty.playQueue(
                songIds,
                Radio.PlayOptions(index = index),
            )
        },
        rowSecondaryOf = { it.artists.joinToString() },
        playingSongId = playingSongId,
        playing = playing,
        narrow = narrow,
        showAllLabel = "Show all " +
            context.symphony.t.XSongs(genre.numberOfTracks.toString()),
        onShowAll = { context.navController.navigate(GenreViewRoute(genre.name)) },
    )
}
