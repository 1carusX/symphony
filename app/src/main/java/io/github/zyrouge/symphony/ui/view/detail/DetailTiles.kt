package io.github.zyrouge.symphony.ui.view.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.components.BentoTile
import io.github.zyrouge.symphony.ui.components.BentoTileGeometry
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.LocalBentoTileContentColors
import io.github.zyrouge.symphony.ui.components.SongDropdownMenu
import io.github.zyrouge.symphony.ui.components.formatBentoDurationMinutes
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.view.AlbumMoreLikeThisTile
import io.github.zyrouge.symphony.ui.view.AlbumViewRoute
import io.github.zyrouge.symphony.ui.adapters.toUiSong
import io.github.zyrouge.symphony.utils.DurationUtils

/**
 * Tiles of the detail pages (artist, album artist, genre, playlist, folder),
 * which reuse the album frame (DESIGN.md 5.4) with the prototype's detail
 * tiles: covers, info, a songs list and the side tiles.
 */

/** A cover tile filled with an image (the artist avatar). */
@Composable
fun DetailImageCoverTile(
    request: ImageRequest,
    contentDescription: String?,
    geometry: BentoTileGeometry,
) {
    DetailImageCoverTile(geometry) { modifier ->
        AsyncImage(
            request,
            contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )
    }
}

/** The playlist cover tile: a 2 x 2 mosaic of the first songs' artwork. */
@Composable
fun DetailMosaicCoverTile(
    context: ViewContext,
    songIds: List<String>,
    geometry: BentoTileGeometry,
) {
    val firstIds = songIds.take(4)
    val requests = remember(firstIds) {
        (0 until 4).map { index ->
            firstIds.getOrNull(index)
                ?.let { context.symphony.groove.song.get(it) }
                ?.createArtworkImageRequest(context.symphony)?.build()
                ?: Assets.createPlaceholderImageRequest(context.symphony).build()
        }
    }
    DetailMosaicCoverTile(
        geometry = geometry,
        artworks = requests.map { request ->
            { modifier ->
                AsyncImage(
                    request,
                    null,
                    contentScale = ContentScale.Crop,
                    modifier = modifier,
                )
            }
        },
    )
}

/** A song row inside a list tile, with the Android artwork and song menu. */
@Composable
fun DetailSongRow(
    context: ViewContext,
    song: Song,
    secondary: String,
    playing: Boolean,
    isPlaying: Boolean,
    isFavorite: Boolean,
    menuExtra: (@Composable ColumnScope.(Song, () -> Unit) -> Unit)?,
    onClick: () -> Unit,
) {
    DetailSongRow(
        song = song.toUiSong(),
        secondary = secondary,
        playing = playing,
        isPlaying = isPlaying,
        durationText = DurationUtils.formatMs(song.duration),
        moreContentDescription = context.symphony.t.More,
        artwork = { modifier ->
            AsyncImage(
                song.createArtworkImageRequest(context.symphony).build(),
                null,
                contentScale = ContentScale.Crop,
                modifier = modifier,
            )
        },
        menu = { expanded, dismiss ->
            SongDropdownMenu(
                context,
                song,
                isFavorite = isFavorite,
                trailingContent = menuExtra?.let { extra ->
                    { onDismiss -> extra(song, onDismiss) }
                },
                expanded = expanded,
                onDismissRequest = dismiss,
            )
        },
        onClick = onClick,
    )
}

/**
 * The albums side tile: one row per album (cover, title, year - songs -
 * length) for up to three albums, otherwise the album page's cover grid.
 */
@Composable
fun DetailAlbumsTile(
    context: ViewContext,
    label: String,
    albums: List<Album>,
    geometry: BentoTileGeometry,
) {
    if (albums.size > 3) {
        AlbumMoreLikeThisTile(context = context, geometry = geometry, label = label, albums = albums)
        return
    }
    val tileColors = LocalBentoTileContentColors.current
    val rowHeight = ((geometry.height - 62f) / albums.size.coerceAtLeast(1)).coerceIn(56f, 96f)
    val coverSize = minOf(rowHeight - 16f, (geometry.width - 32f) * 0.3f)
    val chevron = geometry.width >= 240f

    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        DetailTileHeading(
            label = label,
            end = context.symphony.t.XAlbums(albums.size.toString()),
        )
        Column {
            albums.forEach { album ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rowHeight.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { context.navController.navigate(AlbumViewRoute(album.id)) },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    AsyncImage(
                        album.createArtworkImageRequest(context.symphony).build(),
                        album.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(coverSize.dp)
                            .clip(RoundedCornerShape(12.dp)),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            album.name,
                            style = BentoTypography.rowTitle.copy(fontSize = 14.sp),
                            color = tileColors.fg,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            listOfNotNull(
                                (album.startYear ?: album.endYear)?.toString(),
                                context.symphony.t.XSongs(album.numberOfTracks.toString()),
                                formatBentoDurationMinutes(album.duration.inWholeMilliseconds, context.symphony.t),
                            ).joinToString(" · "),
                            style = BentoTypography.label,
                            color = tileColors.sub,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (chevron) {
                        Icon(
                            Icons.Rounded.ChevronRight,
                            null,
                            tint = tileColors.sub,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}
