package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.adapters.toUiSong
import io.github.zyrouge.symphony.ui.components.BentoGroupTile as SharedBentoGroupTile
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.utils.DurationUtils

private object BentoGenreSwatch {
    val colors = listOf(
        0xFFEF4444,
        0xFFF97316,
        0xFFF59E0B,
        0xFF16A34A,
        0xFF06B6B4,
        0xFF8B5CF6,
        0xFFD946EF,
        0xFFF43F5E,
        0xFF6366F1,
        0xFFA855F7,
    ).map { Color(it) }

    fun colorAt(index: Int): Color {
        val bounded = ((index % colors.size) + colors.size) % colors.size
        return colors[bounded]
    }
}

/**
 * Android wrapper of the shared grouped tile: it maps songs to UI models,
 * supplies Coil artwork and the default song-list menu.
 */
@Composable
fun BentoGroupTile(
    context: ViewContext,
    title: String,
    subtitle: String,
    songs: List<Song>,
    cover: @Composable () -> Unit,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    onPlayAt: (index: Int) -> Unit,
    trackNumberOf: ((index: Int, song: Song) -> Int?)? = null,
    rowSecondaryOf: ((song: Song) -> String?)? = null,
    playingSongId: String?,
    playing: Boolean,
    narrow: Boolean,
    showAllLabel: String?,
    onShowAll: (() -> Unit)?,
    menu: (@Composable (expanded: Boolean, onDismissRequest: () -> Unit) -> Unit)? = null,
) {
    val uiSongs = remember(songs) { songs.map { it.toUiSong() } }
    val songsById = remember(songs) { songs.associateBy { it.id } }
    val songIds = remember(songs) { songs.map { it.id } }

    SharedBentoGroupTile(
        title = title,
        subtitle = subtitle,
        songs = uiSongs,
        cover = cover,
        onOpen = onOpen,
        onPlay = onPlay,
        onPlayAt = onPlayAt,
        durationText = { DurationUtils.formatMs(it.durationMillis) },
        artwork = { song, modifier ->
            songsById[song.id]?.let { original ->
                AsyncImage(
                    original.createArtworkImageRequest(context.symphony).build(),
                    null,
                    contentScale = ContentScale.Crop,
                    modifier = modifier,
                )
            }
        },
        moreContentDescription = context.symphony.t.More,
        playingSongId = playingSongId,
        playing = playing,
        narrow = narrow,
        showAllLabel = showAllLabel,
        onShowAll = onShowAll,
        menu = { expanded, onDismissRequest ->
            when (val provided = menu) {
                null -> GenericSongListDropdown(
                    context = context,
                    songIds = songIds,
                    expanded = expanded,
                    onDismissRequest = onDismissRequest,
                    source = title,
                    header = {
                        BentoMenuHeader(
                            title = title,
                            subtitle = subtitle,
                            art = { BentoGroupMenuArt(cover) },
                        )
                    },
                )

                else -> provided(expanded, onDismissRequest)
            }
        },
        trackNumberOf = { index, song ->
            songsById[song.id]?.let { trackNumberOf?.invoke(index, it) }
        },
        rowSecondaryOf = { song ->
            songsById[song.id]?.let { rowSecondaryOf?.invoke(it) }
        },
    )
}

/** The tile's 64 dp cover drawn at 44 dp for the menu header. */
@Composable
private fun BentoGroupMenuArt(cover: @Composable () -> Unit) {
    val scale = 44f / 64f
    Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .requiredSize(64.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
        ) {
            cover()
        }
    }
}

/** A 64 dp album-art cover of a grouped tile (radius 16). */
@Composable
fun BentoGroupAlbumCover(request: ImageRequest) {
    AsyncImage(
        request,
        null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(BentoRadii.GROUP_COVER_64.dp)),
    )
}

/** A 64 dp round artist avatar of a grouped tile. */
@Composable
fun BentoGroupArtistCover(request: ImageRequest, artistName: String) {
    AsyncImage(
        request,
        artistName,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape),
    )
}

/** A 64 dp gradient swatch of a grouped genre tile. */
@Composable
fun BentoGroupGenreCover(index: Int) {
    val base = BentoGenreSwatch.colorAt(index)
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(BentoRadii.GROUP_COVER_64.dp))
            .background(
                Brush.linearGradient(listOf(base, base.darkened())),
            ),
    )
}

/** A 64 dp 2 x 2 mosaic of album art for a grouped playlist tile. */
@Composable
fun BentoGroupPlaylistCover(context: ViewContext, songIds: List<String>) {
    val cells = (0 until 4).map { index ->
        songIds.getOrNull(index)
            ?.let { songId ->
                context.symphony.groove.song.get(songId)
                    ?.createArtworkImageRequest(context.symphony)?.build()
            }
            ?: Assets.createPlaceholderImageRequest(context.symphony).build()
    }
    Column(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(BentoRadii.GROUP_COVER_64.dp)),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        cells.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                row.forEach { request ->
                    AsyncImage(
                        request,
                        null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(31.dp)
                            .clip(RoundedCornerShape(2.dp)),
                    )
                }
                if (row.size == 1) {
                    Spacer(Modifier.size(31.dp))
                }
            }
        }
    }
}

/** A 64 dp folder cover of a grouped tile: the folder icon on `tonal`. */
@Composable
fun BentoGroupFolderCover() {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(BentoRadii.GROUP_COVER_64.dp))
            .background(colors.tonal),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.Folder,
            null,
            tint = colors.onTonal,
            modifier = Modifier.size(28.dp),
        )
    }
}
