package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.services.groove.repositories.SongRepository
import io.github.zyrouge.symphony.ui.components.AlbumDropdownMenu
import io.github.zyrouge.symphony.ui.components.BentoEqualizerGlyph
import io.github.zyrouge.symphony.ui.components.bentoAlbumTracksRowSpan
import io.github.zyrouge.symphony.ui.components.bentoTabularStyle
import io.github.zyrouge.symphony.ui.helpers.LocalGrooveVersion
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoGridClass
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlinx.serialization.Serializable

@Serializable
data class AlbumViewRoute(val albumId: String)

/**
 * The Album detail page (DESIGN.md 5.4) on the shared collection frame:
 * cover, info, Play / Shuffle / Queue action tiles, the tracks and
 * "More like this".
 */
@Composable
fun AlbumView(context: ViewContext, route: AlbumViewRoute) {
    val t = context.symphony.t
    val allAlbumIds by context.symphony.groove.album.all.collectAsState()
    val album by remember(allAlbumIds) {
        derivedStateOf { context.symphony.groove.album.get(route.albumId) }
    }
    val isViable by remember(allAlbumIds) {
        derivedStateOf { allAlbumIds.contains(route.albumId) }
    }
    val current = album
    if (!isViable || current == null) {
        BentoCollectionUnknownPage(
            label = t.Album,
            message = t.UnknownAlbumX(route.albumId),
            icon = Icons.Filled.Album,
            onBack = { context.navController.popBackStack() },
        )
        return
    }
    AlbumPage(context, current)
}

@Composable
private fun AlbumPage(context: ViewContext, album: Album) {
    val t = context.symphony.t
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val grooveVersion = LocalGrooveVersion.current
    val songIds = remember(album.id, grooveVersion) {
        album.getSongIds(context.symphony)
    }
    val moreAlbums by remember(album.id, grooveVersion) {
        derivedStateOf {
            album.artists
                .flatMap { artistName -> context.symphony.groove.artist.getAlbumIds(artistName) }
                .filterNot { it == album.id }
                .mapNotNull { context.symphony.groove.album.get(it) }
        }
    }
    var showOptionsMenu by remember { mutableStateOf(false) }

    val sideTiles = if (moreAlbums.isEmpty()) {
        emptyList()
    } else {
        listOf(
            DetailSideTile(
                id = "more",
                rowSpan = { gridClass ->
                    when (gridClass) {
                        BentoGridClass.C6 -> maxOf(
                            2,
                            bentoAlbumTracksRowSpan(songIds.size, gridClass),
                        )

                        else -> 2
                    }
                },
            ) { geometry ->
                AlbumMoreLikeThisTile(
                    context = context,
                    geometry = geometry,
                    label = t.MoreLikeThis,
                    albums = moreAlbums,
                )
            },
        )
    }

    BentoCollectionPage(
        context = context,
        label = t.Album,
        title = album.name,
        songIds = songIds,
        onBack = { context.navController.popBackStack() },
        emptyMessage = t.DamnThisIsSoEmpty,
        cover = { geometry -> AlbumCoverTile(context, album, geometry) },
        trailingMenu = {
            Box {
                IconButton(
                    content = {
                        Icon(
                            Icons.Rounded.MoreVert,
                            t.Details,
                            tint = LocalBentoColors.current.fg,
                        )
                    },
                    onClick = { showOptionsMenu = !showOptionsMenu },
                )
                AlbumDropdownMenu(
                    context,
                    album,
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false },
                )
            }
        },
        sideTiles = sideTiles,
        sortByOverride = SongRepository.SortBy.TRACK_NUMBER,
        sortReverseOverride = false,
        info = { geometry ->
            AlbumInfoTile(
                context = context,
                album = album,
                geometry = geometry,
                appFontFamily = appFontFamily,
            )
        },
        rowLeading = { song, playing, isPlaying ->
            Box(modifier = Modifier.width(22.dp), contentAlignment = Alignment.Center) {
                val colors = LocalBentoColors.current
                if (playing) {
                    BentoEqualizerGlyph(color = colors.accent, playing = isPlaying)
                } else {
                    Text(
                        song.trackNumber?.toString() ?: t.UnknownSymbol,
                        style = bentoTabularStyle(BentoTypography.secondary),
                        color = colors.sub,
                        maxLines = 1,
                    )
                }
            }
        },
        rowSecondary = { song -> song.artists.joinToString() },
        tracksLabel = t.Tracks,
    )
}
