package io.github.zyrouge.symphony.ui.view.player

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MotionPhotosPaused
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.ui.components.AddToPlaylistDialog
import io.github.zyrouge.symphony.ui.components.MenuRow
import io.github.zyrouge.symphony.ui.components.bentoScrollbar
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.BentoAppDensity
import io.github.zyrouge.symphony.ui.theme.bentoAppScale
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.view.AlbumViewRoute
import io.github.zyrouge.symphony.ui.view.ArtistViewRoute
import io.github.zyrouge.symphony.utils.Logger

/** Player More menu: phone action sheet or a 288 dp popover (DESIGN.md 4.5). */
@Composable
internal fun BentoPlayerMoreMenu(
    context: ViewContext,
    data: BentoPlayerData,
    compact: Boolean,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onShowSpeed: () -> Unit,
    // Speed and pitch share one sheet, opened through onShowSpeed.
    @Suppress("UNUSED_PARAMETER") onShowPitch: () -> Unit,
    onShowSleepTimer: () -> Unit,
    onClosePlayer: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val colorScheme = LocalBentoColorScheme.current
    val equalizerActivity = rememberLauncherForActivityResult(
        context.symphony.radio.session.createEqualizerActivityContract(),
    ) {}
    var showAddToPlaylistDialog by remember { mutableStateOf(false) }

    fun launchEqualizer() {
        try {
            equalizerActivity.launch()
        } catch (err: Exception) {
            Logger.error("BentoPlayerMoreMenu", "launching equalizer failed", err)
            Toast.makeText(
                context.activity,
                context.symphony.t.LaunchingEqualizerFailedX(
                    err.localizedMessage ?: err.toString(),
                ),
                Toast.LENGTH_SHORT,
            ).show()
        }
    }

    if (expanded && compact) {
        Popup(
            onDismissRequest = onDismissRequest,
            properties = PopupProperties(focusable = false),
        ) {
            BentoAppDensity {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.scrim)
                        .clickable(onClick = onDismissRequest),
                    contentAlignment = Alignment.BottomCenter,
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp)
                            .widthIn(max = 560.dp)
                            .fillMaxWidth()
                            .shadow(20.dp, RoundedCornerShape(BentoRadii.MENU_SHEET.dp))
                            .clip(RoundedCornerShape(BentoRadii.MENU_SHEET.dp))
                            .background(colors.high)
                            .clickable { }
                            .padding(10.dp),
                    ) {
                        BentoPlayerMoreMenuContent(
                            context = context,
                            data = data,
                            onDismissRequest = onDismissRequest,
                            onShowSpeed = onShowSpeed,
                            onShowSleepTimer = onShowSleepTimer,
                            onClosePlayer = onClosePlayer,
                            onAddToPlaylist = { showAddToPlaylistDialog = true },
                            onLaunchEqualizer = ::launchEqualizer,
                        )
                    }
                }
            }
        }
    } else {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = Modifier.width(BentoSizes.MENU_WIDTH.dp * bentoAppScale()),
            containerColor = colors.high,
            shape = RoundedCornerShape(BentoRadii.MENU.dp),
            shadowElevation = 16.dp,
            border = when (colorScheme) {
                BentoColorScheme.CONTRAST -> BorderStroke(1.dp, colors.line)
                else -> null
            },
        ) {
            BentoAppDensity {
                BentoPlayerMoreMenuContent(
                    context = context,
                    data = data,
                    onDismissRequest = onDismissRequest,
                    onShowSpeed = onShowSpeed,
                    onShowSleepTimer = onShowSleepTimer,
                    onClosePlayer = onClosePlayer,
                    onAddToPlaylist = { showAddToPlaylistDialog = true },
                    onLaunchEqualizer = ::launchEqualizer,
                )
            }
        }
    }

    if (showAddToPlaylistDialog) {
        AddToPlaylistDialog(
            context = context,
            songIds = listOf(data.song.id),
            onDismissRequest = { showAddToPlaylistDialog = false },
        )
    }
}

@Composable
private fun BentoPlayerMoreMenuContent(
    context: ViewContext,
    data: BentoPlayerData,
    onDismissRequest: () -> Unit,
    onShowSpeed: () -> Unit,
    onShowSleepTimer: () -> Unit,
    onClosePlayer: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onLaunchEqualizer: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val menuScroll = rememberScrollState()
    Column(
        modifier = Modifier
            .heightIn(max = 520.dp)
            .bentoScrollbar(menuScroll, colors.muted, BentoRadii.MENU.dp)
            .verticalScroll(menuScroll),
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AsyncImage(
                remember(data.song.id) {
                    data.song.createArtworkImageRequest(context.symphony).build()
                },
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(BentoRadii.SONG_ART_44.dp)),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    data.song.title,
                    style = BentoTypography.rowTitle,
                    color = colors.fg,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                // The same "MP3 · 44.1 kHz · 32 kbps" line as under the player title.
                bentoPlayerFormatLine(context, data.song)?.let {
                    Text(
                        it,
                        style = BentoTypography.secondary,
                        color = colors.sub,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        PlayerMenuItem(
            icon = { Icon(Icons.Outlined.Speed, null) },
            label = speedAndPitchLabel(context),
            value = when (data.currentPitch) {
                1f -> formatPlaybackRate(data.currentSpeed)
                else -> formatPlaybackRate(data.currentSpeed) + " · " +
                    formatPlaybackRate(data.currentPitch)
            },
            onClick = {
                onDismissRequest()
                onShowSpeed()
            },
        )
        PlayerMenuItem(
            icon = { Icon(Icons.Outlined.Timer, null) },
            label = context.symphony.t.SleepTimer,
            value = if (data.sleepTimer != null) context.symphony.t.On else context.symphony.t.Off,
            onClick = {
                onDismissRequest()
                onShowSleepTimer()
            },
        )
        PlayerMenuItem(
            icon = { Icon(Icons.Filled.MotionPhotosPaused, null) },
            label = context.symphony.t.PauseOnCurrentSongEnd,
            value = if (data.pauseOnCurrentSongEnd) context.symphony.t.On else context.symphony.t.Off,
            onClick = {
                onDismissRequest()
                context.symphony.radio.setPauseOnCurrentSongEnd(!data.pauseOnCurrentSongEnd)
            },
        )
        PlayerMenuItem(
            icon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null) },
            label = context.symphony.t.AddToPlaylist,
            onClick = {
                onDismissRequest()
                onAddToPlaylist()
            },
        )
        context.symphony.groove.album.getIdFromSong(data.song)?.let { albumId ->
            PlayerMenuItem(
                icon = { Icon(Icons.Filled.Album, null) },
                label = context.symphony.t.ViewAlbum,
                onClick = {
                    onDismissRequest()
                    onClosePlayer()
                    context.navController.navigate(AlbumViewRoute(albumId))
                },
            )
        }
        data.song.artists.forEach { artist ->
            PlayerMenuItem(
                icon = { Icon(Icons.Filled.Person, null) },
                label = "${context.symphony.t.ViewArtist}: $artist",
                onClick = {
                    onDismissRequest()
                    onClosePlayer()
                    context.navController.navigate(ArtistViewRoute(artist))
                },
            )
        }
        PlayerMenuItem(
            icon = { Icon(Icons.Filled.GraphicEq, null) },
            label = context.symphony.t.Equalizer,
            onClick = {
                onDismissRequest()
                onLaunchEqualizer()
            },
        )
    }
}

@Composable
private fun PlayerMenuItem(
    icon: @Composable () -> Unit,
    label: String,
    value: String? = null,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    MenuRow(
        leading = {
            Box(modifier = Modifier.size(22.dp), contentAlignment = Alignment.Center) {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides colors.sub,
                ) {
                    icon()
                }
            }
        },
        label = label,
        value = value,
        onClick = onClick,
    )
}
