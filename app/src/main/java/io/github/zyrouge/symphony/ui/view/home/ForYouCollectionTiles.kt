package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.ui.adapters.toUiAlbum
import io.github.zyrouge.symphony.ui.adapters.toUiSong
import io.github.zyrouge.symphony.ui.components.BentoTileGeometry
import io.github.zyrouge.symphony.ui.components.ForYouAlbumCollectionTile as BentoForYouAlbumCollectionTile
import io.github.zyrouge.symphony.ui.components.ForYouArtistsTile as BentoForYouArtistsTile
import io.github.zyrouge.symphony.ui.components.ForYouLibraryStatTile as BentoForYouLibraryStatTile
import io.github.zyrouge.symphony.ui.components.ForYouUpNextEntry
import io.github.zyrouge.symphony.ui.components.ForYouUpNextTile as BentoForYouUpNextTile
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.helpers.libraryRescanAction
import io.github.zyrouge.symphony.ui.view.AlbumViewRoute
import io.github.zyrouge.symphony.ui.view.ArtistViewRoute
import io.github.zyrouge.symphony.ui.view.HomePage
import io.github.zyrouge.symphony.utils.DurationUtils

@Composable
internal fun ForYouLibraryStatTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
    appFontFamily: FontFamily?,
    totalDurationMs: Long,
    songCount: Int,
    albumCount: Int,
) {
    BentoForYouLibraryStatTile(
        geometry = geometry,
        appFontFamily = appFontFamily,
        totalDurationMs = totalDurationMs,
        songCount = songCount,
        albumCount = albumCount,
        strings = context.symphony.t,
        onRescan = libraryRescanAction(context),
    )
}

@Composable
internal fun ForYouAlbumCollectionTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
    label: String,
    albums: List<Album>,
) {
    val uiAlbums = remember(albums) { albums.map { it.toUiAlbum() } }
    BentoForYouAlbumCollectionTile(
        geometry = geometry,
        label = label,
        seeAllLabel = context.symphony.t.All,
        albums = uiAlbums,
        artwork = { album, modifier ->
            AsyncImage(
                context.symphony.groove.album.createArtworkImageRequest(album.id).build(),
                null,
                contentScale = ContentScale.Crop,
                modifier = modifier,
            )
        },
        onAlbumClick = { album ->
            context.navController.navigate(AlbumViewRoute(album.id))
        },
        onSeeAll = { selectHomeSection(context, HomePage.Albums) },
    )
}

@Composable
internal fun ForYouArtistsTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
    artistNames: List<String>,
) {
    BentoForYouArtistsTile(
        geometry = geometry,
        label = context.symphony.t.Artists,
        seeAllLabel = context.symphony.t.All,
        artistNames = artistNames,
        artwork = { artistName, modifier ->
            AsyncImage(
                context.symphony.groove.artist.createArtworkImageRequest(artistName).build(),
                artistName,
                contentScale = ContentScale.Crop,
                modifier = modifier,
            )
        },
        onArtistClick = { artistName ->
            context.navController.navigate(ArtistViewRoute(artistName))
        },
        onSeeAll = { selectHomeSection(context, HomePage.Artists) },
    )
}

@Composable
internal fun ForYouUpNextTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
) {
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val upNext by remember(queue, queueIndex) {
        derivedStateOf {
            queue.drop(queueIndex + 1).mapIndexed { index, songId ->
                queueIndex + 1 + index to songId
            }
        }
    }
    val entries = upNext.mapNotNull { (position, songId) ->
        context.symphony.groove.song.get(songId)?.let { song ->
            ForYouUpNextEntry(
                position = position,
                song = song.toUiSong(),
                durationText = DurationUtils.formatMs(song.duration),
            )
        }
    }
    BentoForYouUpNextTile(
        geometry = geometry,
        label = context.symphony.t.UpNext,
        actionLabel = when {
            upNext.isEmpty() -> context.symphony.t.EndOfQueue
            else -> context.symphony.t.XSongs(upNext.size.toString())
        },
        entries = entries,
        artwork = { song, modifier ->
            AsyncImage(
                context.symphony.groove.song.createArtworkImageRequest(song.id).build(),
                null,
                contentScale = ContentScale.Crop,
                modifier = modifier,
            )
        },
        onJump = { position -> context.symphony.radio.jumpTo(position) },
    )
}
