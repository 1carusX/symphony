package io.github.zyrouge.symphony.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.zyrouge.symphony.ui.components.GenericSongListDropdown
import io.github.zyrouge.symphony.ui.helpers.LocalGrooveVersion
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.detail.DetailAlbumsTile
import io.github.zyrouge.symphony.ui.view.detail.DetailGenreCoverTile
import io.github.zyrouge.symphony.ui.view.detail.DetailPeopleTile
import io.github.zyrouge.symphony.ui.view.detail.DetailPerson
import io.github.zyrouge.symphony.ui.view.detail.detailAlbumsOf
import io.github.zyrouge.symphony.ui.view.detail.detailAlbumsRowSpan
import io.github.zyrouge.symphony.ui.view.detail.detailArtistsOf
import kotlinx.serialization.Serializable

@Serializable
data class GenreViewRoute(val genreName: String)

/**
 * The genre detail page: the genre swatch, its songs, and the artists and
 * albums in it as side tiles.
 */
@Composable
fun GenreView(context: ViewContext, route: GenreViewRoute) {
    val allGenreNames by context.symphony.groove.genre.all.collectAsState()
    val grooveVersion = LocalGrooveVersion.current
    val genre by remember(allGenreNames, grooveVersion) {
        derivedStateOf { context.symphony.groove.genre.get(route.genreName) }
    }
    val songIds by remember(genre, grooveVersion) {
        derivedStateOf { genre?.getSongIds(context.symphony) ?: listOf() }
    }
    val artists = remember(songIds, grooveVersion) { detailArtistsOf(context, songIds) }
    val albums = remember(songIds, grooveVersion) { detailAlbumsOf(context, songIds) }

    val value = genre
    if (value == null) {
        BentoCollectionUnknownPage(
            label = context.symphony.t.Genre,
            message = context.symphony.t.UnknownGenreX(route.genreName),
            icon = Icons.Filled.Sell,
            onBack = { context.navController.popBackStack() },
        )
        return
    }
    val sideTiles = buildList {
        if (artists.isNotEmpty()) {
            add(
                DetailSideTile(id = "artists", leading = true, rowSpan = { 1 }) { geometry ->
                    DetailPeopleTile(
                        label = context.symphony.t.Artists,
                        people = artists.take(12).map { artistName ->
                            DetailPerson(
                                artistName,
                                context.symphony.groove.artist
                                    .createArtworkImageRequest(artistName)
                                    .build(),
                            ) {
                                context.navController.navigate(ArtistViewRoute(artistName))
                            }
                        },
                        geometry = geometry,
                    )
                },
            )
        }
        if (albums.isNotEmpty()) {
            add(
                DetailSideTile(
                    id = "albums",
                    rowSpan = { detailAlbumsRowSpan(it, albums.size) },
                ) { geometry ->
                    DetailAlbumsTile(context, context.symphony.t.Albums, albums, geometry)
                },
            )
        }
    }

    BentoCollectionPage(
        context = context,
        label = context.symphony.t.Genre,
        title = value.name,
        songIds = songIds,
        onBack = { context.navController.popBackStack() },
        emptyMessage = context.symphony.t.DamnThisIsSoEmpty,
        cover = { geometry -> DetailGenreCoverTile(value.name, geometry) },
        metaPrefix = context.symphony.t.XArtists(artists.size.toString()),
        trailingMenu = {
            var showOptionsMenu by remember { mutableStateOf(false) }
            IconButton(onClick = { showOptionsMenu = !showOptionsMenu }) {
                Icon(Icons.Filled.MoreVert, null)
                GenericSongListDropdown(
                    context,
                    songIds = songIds,
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false },
                )
            }
        },
        sideTiles = sideTiles,
    )
}
