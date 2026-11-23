package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.components.BentoCoverGrid
import io.github.zyrouge.symphony.ui.components.BentoEqualizerGlyph
import io.github.zyrouge.symphony.ui.components.BentoStatContent
import io.github.zyrouge.symphony.ui.components.BentoTile
import io.github.zyrouge.symphony.ui.components.BentoTileGeometry
import io.github.zyrouge.symphony.ui.components.BentoTileHeader
import io.github.zyrouge.symphony.ui.components.BentoTileSizeClass
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.LocalBentoTileContentColors
import io.github.zyrouge.symphony.ui.components.bentoStatValueStyle
import io.github.zyrouge.symphony.ui.components.bentoTabularStyle
import io.github.zyrouge.symphony.ui.components.bentoTilePaddingDp
import io.github.zyrouge.symphony.ui.components.fitBentoCoverGrid
import io.github.zyrouge.symphony.ui.components.formatBentoDurationMinutes
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.utils.DurationUtils

/** The album cover tile (DESIGN.md 5.4): the artwork fills the tile. */
@Composable
internal fun AlbumCoverTile(context: ViewContext, album: Album, geometry: BentoTileGeometry) {
    BentoTile(
        tone = BentoTileTone.PLAIN,
        sizeClass = geometry.sizeClass,
        contentPadding = 0.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                album.createArtworkImageRequest(context.symphony).build(),
                album.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(BentoRadii.TILE.dp)),
            )
        }
    }
}

/**
 * The album info tile (DESIGN.md 5.4): the "Album" label, the title (28 sp,
 * 22 in m and narrow tiles), the artist as an accent link and the
 * year - genre - songs - length meta line.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AlbumInfoTile(
    context: ViewContext,
    album: Album,
    geometry: BentoTileGeometry,
    appFontFamily: FontFamily?,
) {
    val colors = LocalBentoColors.current
    val tileColors = LocalBentoTileContentColors.current
    val compact = geometry.height < 170f
    val narrow = geometry.width < 240f
    val titleStyle = when {
        geometry.sizeClass == BentoTileSizeClass.M || narrow -> BentoTypography.title
        else -> BentoTypography.display
    }.copy(fontFamily = appFontFamily)
    val year = album.startYear ?: album.endYear
    val genres by remember(album.id) {
        derivedStateOf { albumGenresOf(context, album) }
    }
    val genre = genres.firstOrNull()

    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                context.symphony.t.Album,
                style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
                color = tileColors.sub,
                maxLines = 1,
            )
            Spacer(Modifier.weight(1f))
            Text(
                album.name,
                style = titleStyle,
                color = tileColors.fg,
                maxLines = when {
                    compact -> 1
                    else -> 2
                },
                overflow = TextOverflow.Ellipsis,
            )
            if (album.artists.isNotEmpty()) {
                FlowRow {
                    album.artists.forEachIndexed { i, artistName ->
                        Text(
                            artistName,
                            style = BentoTypography.body.copy(fontWeight = FontWeight.SemiBold),
                            color = colors.accent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.clickable {
                                context.navController.navigate(ArtistViewRoute(artistName))
                            },
                        )
                        if (i != album.artists.size - 1) {
                            Text(
                                ", ",
                                style = BentoTypography.body.copy(
                                    fontWeight = FontWeight.SemiBold,
                                ),
                                color = colors.accent,
                            )
                        }
                    }
                }
            }
            if (!compact) {
                Text(
                    listOfNotNull(
                        year?.toString(),
                        genre,
                        context.symphony.t.XSongs(album.numberOfTracks.toString()),
                        formatBentoDurationMinutes(album.duration.inWholeMilliseconds),
                    ).joinToString(" · "),
                    style = BentoTypography.secondary,
                    color = tileColors.sub,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun albumGenresOf(context: ViewContext, album: Album): Set<String> {
    val genres = mutableSetOf<String>()
    album.getSongIds(context.symphony).forEach { songId ->
        context.symphony.groove.song.get(songId)?.genres?.let { genres.addAll(it) }
    }
    return genres
}

/** The Length stat tile (DESIGN.md 5.4, wider grids only). */
@Composable
internal fun AlbumLengthStatTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
    appFontFamily: FontFamily?,
    songs: List<Song>,
) {
    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        BentoStatContent(
            label = context.symphony.t.Length,
            value = formatBentoDurationMinutes(songs.sumOf { it.duration }),
            valueStyle = bentoStatValueStyle(geometry.sizeClass, appFontFamily),
            sub = context.symphony.t.XSongs(songs.size.toString()),
            geometry = geometry,
        )
    }
}

/**
 * The Tracks tile (DESIGN.md 5.4): 48 dp rows with track numbers, as tall
 * as all tracks need.
 */
@Composable
internal fun AlbumTracksTile(
    context: ViewContext,
    songs: List<Song>,
    trackSongIds: List<String>,
    geometry: BentoTileGeometry,
    playingSongId: String?,
    playing: Boolean,
) {
    val colors = LocalBentoColors.current
    val tileColors = LocalBentoTileContentColors.current

    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        Column(modifier = Modifier.fillMaxSize()) {
            BentoTileHeader(
                label = context.symphony.t.Tracks,
                actionLabel = formatBentoDurationMinutes(songs.sumOf { it.duration }),
                showAction = true,
                onAction = null,
            )
            songs.forEachIndexed { index, song ->
                val isPlayingSong = song.id == playingSongId
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(BentoSizes.TILE_LIST_ROW.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            context.symphony.radio.shorty.playQueue(
                                trackSongIds,
                                Radio.PlayOptions(index = index),
                            )
                        }
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier.width(22.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isPlayingSong) {
                            BentoEqualizerGlyph(color = colors.accent, playing = playing)
                        } else {
                            Text(
                                song.trackNumber?.toString()
                                    ?: context.symphony.t.UnknownSymbol,
                                style = bentoTabularStyle(BentoTypography.secondary),
                                color = tileColors.sub,
                                maxLines = 1,
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            song.title,
                            style = BentoTypography.rowTitle.copy(fontSize = 14.sp),
                            color = when {
                                isPlayingSong -> colors.accent
                                else -> tileColors.fg
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        song.artists.takeIf { it.isNotEmpty() }?.let {
                            Text(
                                it.joinToString(),
                                style = BentoTypography.label,
                                color = tileColors.sub,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Text(
                        DurationUtils.formatMs(song.duration),
                        style = bentoTabularStyle(BentoTypography.label),
                        color = tileColors.sub,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * The "More like this" tile (DESIGN.md 5.4): albums by the same artist
 * first, then the same genre.
 */
@Composable
internal fun AlbumMoreLikeThisTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
    label: String,
    albums: List<Album>,
) {
    val tileColors = LocalBentoTileContentColors.current
    val padding = bentoTilePaddingDp(geometry.sizeClass)
    val fit = fitBentoCoverGrid(
        tileWidthDp = geometry.width - 2f * padding.value,
        tileHeightDp = geometry.height - 2f * padding.value - 30f,
        itemCount = albums.size,
    )

    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        Column(modifier = Modifier.fillMaxSize()) {
            BentoTileHeader(
                label = label,
                actionLabel = "",
                showAction = false,
                onAction = null,
            )
            Box(modifier = Modifier.weight(1f)) {
                if (albums.isNotEmpty() && fit.rows > 0) {
                    BentoCoverGrid(
                        itemCount = albums.size,
                        fit = fit,
                    ) { index ->
                        val album = albums.getOrNull(index) ?: return@BentoCoverGrid
                        Column {
                            AsyncImage(
                                album.createArtworkImageRequest(context.symphony).build(),
                                album.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(fit.coverSize.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        context.navController.navigate(AlbumViewRoute(album.id))
                                    },
                            )
                            if (fit.captioned) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    album.name,
                                    style = BentoTypography.secondary.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = tileColors.fg,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(fit.coverSize.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun UnknownAlbum(context: ViewContext, albumId: String) {
    IconTextBody(
        icon = { modifier ->
            Icon(Icons.Filled.Album, null, modifier = modifier)
        },
        content = {
            Text(context.symphony.t.UnknownAlbumX(albumId))
        },
    )
}
