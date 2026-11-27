package io.github.zyrouge.symphony.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.automirrored.rounded.PlaylistPlay
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.view.AlbumViewRoute
import io.github.zyrouge.symphony.utils.DurationUtils
import io.github.zyrouge.symphony.utils.Logger

@Composable
fun SongCard(
    context: ViewContext,
    song: Song,
    highlighted: Boolean = false,
    autoHighlight: Boolean = true,
    disableHeartIcon: Boolean = false,
    leading: @Composable () -> Unit = {},
    thumbnailLabel: (@Composable () -> Unit)? = null,
    thumbnailLabelStyle: SongCardThumbnailLabelStyle = SongCardThumbnailLabelStyle.Default,
    trailingOptionsContent: (@Composable ColumnScope.(() -> Unit) -> Unit)? = null,
    onClick: () -> Unit,
) {
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val isCurrentPlaying by remember(autoHighlight, song, queue) {
        derivedStateOf { autoHighlight && song.id == queue.getOrNull(queueIndex) }
    }
    val favoriteSongIds by context.symphony.groove.playlist.favorites.collectAsState()
    val isFavorite by remember(favoriteSongIds, song) {
        derivedStateOf { favoriteSongIds.contains(song.id) }
    }

    val bentoColors = LocalBentoColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(BentoRadii.TILE_SMALL.dp))
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.padding(12.dp, 12.dp, 4.dp, 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                leading()
                Box {
                    AsyncImage(
                        song.createArtworkImageRequest(context.symphony).build(),
                        null,
                        modifier = Modifier
                            .size(45.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    )
                    thumbnailLabel?.let { it ->
                        val backgroundColor =
                            thumbnailLabelStyle.backgroundColor(MaterialTheme.colorScheme)
                        val contentColor =
                            thumbnailLabelStyle.contentColor(MaterialTheme.colorScheme)

                        Box(
                            modifier = Modifier
                                .offset(y = 8.dp)
                                .align(Alignment.BottomCenter)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        backgroundColor,
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(3.dp, 0.dp)
                            ) {
                                ProvideTextStyle(
                                    MaterialTheme.typography.labelSmall.copy(
                                        color = contentColor
                                    )
                                ) { it() }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        song.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = when {
                                highlighted || isCurrentPlaying -> bentoColors.accent
                                else -> LocalTextStyle.current.color
                            }
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (song.artists.isNotEmpty()) {
                        Text(
                            song.artists.joinToString(),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(15.dp))

                Row {
                    if (!disableHeartIcon && isFavorite) {
                        IconButton(
                            modifier = Modifier.offset(4.dp, 0.dp),
                            onClick = {
                                context.symphony.groove.playlist.unfavorite(song.id)
                            }
                        ) {
                            Icon(
                                Icons.Filled.Favorite,
                                null,
                                modifier = Modifier.size(24.dp),
                                tint = bentoColors.accent,
                            )
                        }
                    }

                    var showOptionsMenu by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { showOptionsMenu = !showOptionsMenu }
                    ) {
                        Icon(
                            Icons.Filled.MoreVert,
                            null,
                            modifier = Modifier.size(24.dp),
                        )
                        SongDropdownMenu(
                            context,
                            song,
                            isFavorite = isFavorite,
                            trailingContent = trailingOptionsContent,
                            expanded = showOptionsMenu,
                            onDismissRequest = {
                                showOptionsMenu = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SongDropdownMenu(
    context: ViewContext,
    song: Song,
    isFavorite: Boolean,
    trailingContent: (@Composable ColumnScope.(() -> Unit) -> Unit)? = null,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
) {
    var showInfoDialog by remember { mutableStateOf(false) }
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    BentoMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        header = {
            BentoMenuHeader(
                title = song.title,
                subtitle = listOfNotNull(
                    song.artists.joinToString().takeIf { it.isNotEmpty() },
                    DurationUtils.formatMs(song.duration),
                ).joinToString(" · "),
                art = {
                    BentoMenuArt(song.createArtworkImageRequest(context.symphony).build())
                },
            )
        },
    ) {
        BentoMenuItem(
            label = when {
                isFavorite -> context.symphony.t.Unfavorite
                else -> context.symphony.t.Favorite
            },
            icon = when {
                isFavorite -> Icons.Filled.Favorite
                else -> Icons.Rounded.FavoriteBorder
            },
            onClick = {
                onDismissRequest()
                context.symphony.groove.playlist.run {
                    when {
                        isFavorite -> unfavorite(song.id)
                        else -> favorite(song.id)
                    }
                }
            }
        )
        BentoMenuItem(
            label = context.symphony.t.PlayNext,
            icon = Icons.Rounded.SkipNext,
            onClick = {
                onDismissRequest()
                context.symphony.radio.queue.add(
                    song.id,
                    context.symphony.radio.queue.currentSongIndex + 1
                )
            }
        )
        BentoMenuItem(
            label = context.symphony.t.AddToQueue,
            icon = Icons.AutoMirrored.Rounded.PlaylistPlay,
            onClick = {
                onDismissRequest()
                context.symphony.radio.queue.add(song.id)
            }
        )
        BentoMenuItem(
            label = context.symphony.t.AddToPlaylist,
            icon = Icons.AutoMirrored.Rounded.PlaylistAdd,
            onClick = {
                onDismissRequest()
                showAddToPlaylistDialog = true
            }
        )
        BentoMenuDivider()
        context.symphony.groove.album.getIdFromSong(song)?.let { albumId ->
            BentoMenuItem(
                label = context.symphony.t.ViewAlbum,
                icon = Icons.Outlined.Album,
                onClick = {
                    onDismissRequest()
                    context.navController.navigate(AlbumViewRoute(albumId))
                }
            )
        }
        BentoGoToArtistItem(
            context = context,
            links = remember(song) { bentoSongArtistLinks(song) },
            onDismissRequest = onDismissRequest,
        )
        BentoMenuItem(
            label = context.symphony.t.ShareSong,
            icon = Icons.Outlined.Share,
            onClick = {
                onDismissRequest()
                try {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        putExtra(Intent.EXTRA_STREAM, song.uri)
                        type = context.activity.contentResolver.getType(song.uri)
                    }
                    context.activity.startActivity(intent)
                } catch (err: Exception) {
                    Logger.error("SongCard", "share failed", err)
                    Toast.makeText(
                        context.activity,
                        context.symphony.t.ShareFailedX(err.localizedMessage ?: err.toString()),
                        Toast.LENGTH_SHORT,
                    ).show()
                }
            }
        )
        BentoMenuItem(
            label = context.symphony.t.Details,
            icon = Icons.Outlined.Info,
            onClick = {
                onDismissRequest()
                showInfoDialog = true
            }
        )
        trailingContent?.let {
            BentoMenuDivider()
            it.invoke(this, onDismissRequest)
        }
    }

    if (showInfoDialog) {
        SongInformationDialog(
            context,
            song = song,
            onDismissRequest = {
                showInfoDialog = false
            }
        )
    }

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            context,
            songIds = listOf(song.id),
            onDismissRequest = {
                showAddToPlaylistDialog = false
            }
        )
    }
}

enum class SongCardThumbnailLabelStyle {
    Default,
    Subtle,
}

private fun SongCardThumbnailLabelStyle.backgroundColor(colorScheme: ColorScheme) = when (this) {
    SongCardThumbnailLabelStyle.Default -> colorScheme.surfaceVariant
    SongCardThumbnailLabelStyle.Subtle -> colorScheme.surfaceVariant
}

private fun SongCardThumbnailLabelStyle.contentColor(colorScheme: ColorScheme) = when (this) {
    SongCardThumbnailLabelStyle.Default -> colorScheme.primary
    SongCardThumbnailLabelStyle.Subtle -> colorScheme.onSurfaceVariant
}
