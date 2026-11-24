package io.github.zyrouge.symphony.ui.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import coil3.request.ImageRequest
import io.github.zyrouge.symphony.ui.helpers.LocalGrooveVersion
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.detail.DetailAlbumsTile
import io.github.zyrouge.symphony.ui.view.detail.DetailChip
import io.github.zyrouge.symphony.ui.view.detail.DetailChipsTile
import io.github.zyrouge.symphony.ui.view.detail.DetailImageCoverTile
import io.github.zyrouge.symphony.ui.view.detail.DetailPeopleTile
import io.github.zyrouge.symphony.ui.view.detail.DetailPerson
import io.github.zyrouge.symphony.ui.view.detail.detailAlbumsRowSpan
import io.github.zyrouge.symphony.ui.view.detail.detailGenresOf
import io.github.zyrouge.symphony.ui.view.detail.detailSimilarArtistsOf
import kotlinx.serialization.Serializable

@Serializable
data class ArtistViewRoute(val artistName: String)

@Composable
fun ArtistView(context: ViewContext, route: ArtistViewRoute) {
    val allArtistNames by context.symphony.groove.artist.all.collectAsState()
    val grooveVersion = LocalGrooveVersion.current
    val artist by remember(allArtistNames, grooveVersion) {
        derivedStateOf { context.symphony.groove.artist.get(route.artistName) }
    }
    val songIds by remember(artist, grooveVersion) {
        derivedStateOf { artist?.getSongIds(context.symphony) ?: listOf() }
    }
    val albumIds by remember(artist, grooveVersion) {
        derivedStateOf { artist?.getAlbumIds(context.symphony) ?: listOf() }
    }

    when (val value = artist) {
        null -> BentoCollectionUnknownPage(
            label = context.symphony.t.Artist,
            message = context.symphony.t.UnknownArtistX(route.artistName),
            icon = Icons.Filled.Person,
            onBack = { context.navController.popBackStack() },
        )

        else -> ArtistCollectionPage(
            context = context,
            label = context.symphony.t.Artist,
            name = value.name,
            songIds = songIds,
            albumIds = albumIds,
            albumArtist = false,
        )
    }
}

/**
 * The artist and album artist detail page: the avatar, the songs (with the
 * album as the secondary line), and the albums, genres and similar artists
 * as side tiles.
 */
@Composable
internal fun ArtistCollectionPage(
    context: ViewContext,
    label: String,
    name: String,
    songIds: List<String>,
    albumIds: List<String>,
    albumArtist: Boolean,
    trailingMenu: (@Composable () -> Unit)? = null,
) {
    val grooveVersion = LocalGrooveVersion.current
    val albums = remember(albumIds, grooveVersion) {
        context.symphony.groove.album.get(albumIds).sortedWith(
            compareBy({ it.startYear ?: it.endYear ?: Int.MAX_VALUE }, { it.name }),
        )
    }
    val genres = remember(songIds, grooveVersion) { detailGenresOf(context, songIds) }
    val similar = remember(name, genres, grooveVersion) {
        detailSimilarArtistsOf(context, genres, exclude = name, albumArtists = albumArtist).take(12)
    }
    val artworkOf: (String) -> ImageRequest = { artistName ->
        when {
            albumArtist -> context.symphony.groove.albumArtist.createArtworkImageRequest(artistName)
            else -> context.symphony.groove.artist.createArtworkImageRequest(artistName)
        }.build()
    }
    val sideTiles = buildList {
        if (albums.isNotEmpty()) {
            add(
                DetailSideTile(
                    id = "albums",
                    leading = true,
                    rowSpan = { detailAlbumsRowSpan(it, albums.size) },
                ) { geometry ->
                    DetailAlbumsTile(context, context.symphony.t.Albums, albums, geometry)
                },
            )
        }
        if (genres.isNotEmpty()) {
            add(
                DetailSideTile(id = "genres", rowSpan = { 1 }) { geometry ->
                    DetailChipsTile(
                        label = context.symphony.t.Genres,
                        chips = genres.map { genre ->
                            DetailChip(genre) {
                                context.navController.navigate(GenreViewRoute(genre))
                            }
                        },
                        geometry = geometry,
                    )
                },
            )
        }
        if (similar.isNotEmpty()) {
            add(
                DetailSideTile(id = "similar", rowSpan = { 1 }) { geometry ->
                    DetailPeopleTile(
                        label = context.symphony.t.SuggestedArtists,
                        people = similar.map { other ->
                            DetailPerson(other, artworkOf(other)) {
                                context.navController.navigate(
                                    when {
                                        albumArtist -> AlbumArtistViewRoute(other)
                                        else -> ArtistViewRoute(other)
                                    },
                                )
                            }
                        },
                        geometry = geometry,
                    )
                },
            )
        }
    }

    BentoCollectionPage(
        context = context,
        label = label,
        title = name,
        songIds = songIds,
        onBack = { context.navController.popBackStack() },
        emptyMessage = context.symphony.t.DamnThisIsSoEmpty,
        cover = { geometry ->
            val request = remember(name, albumArtist) { artworkOf(name) }
            DetailImageCoverTile(request, name, geometry)
        },
        metaPrefix = context.symphony.t.XAlbums(albums.size.toString()),
        trailingMenu = trailingMenu,
        sideTiles = sideTiles,
        rowSecondary = { song -> song.album ?: song.artists.joinToString() },
    )
}
