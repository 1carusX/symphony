package io.github.zyrouge.symphony.ui.view.player

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.launch
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.DropdownMenu
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.ui.components.AddToPlaylistDialog
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.BentoAppDensity
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bentoAppScale
import io.github.zyrouge.symphony.ui.view.AlbumViewRoute
import io.github.zyrouge.symphony.ui.view.ArtistViewRoute
import io.github.zyrouge.symphony.utils.Logger
import io.github.zyrouge.symphony.ui.components.BentoPlayerMoreMenuContent as SharedPlayerMoreMenuContent

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
    val t = context.symphony.t
    val albumId = context.symphony.groove.album.getIdFromSong(data.song)
    SharedPlayerMoreMenuContent(
        strings = t,
        title = data.song.title,
        formatLine = bentoPlayerFormatLine(context, data.song),
        artwork = { modifier ->
            AsyncImage(
                remember(data.song.id) {
                    data.song.createArtworkImageRequest(context.symphony).build()
                },
                null,
                contentScale = ContentScale.Crop,
                modifier = modifier,
            )
        },
        speedPitchLabel = speedAndPitchLabel(context),
        speedPitchValue = when (data.currentPitch) {
            1f -> formatPlaybackRate(data.currentSpeed)
            else -> formatPlaybackRate(data.currentSpeed) + " · " +
                formatPlaybackRate(data.currentPitch)
        },
        sleepTimerActive = data.sleepTimer != null,
        pauseOnSongEnd = data.pauseOnCurrentSongEnd,
        showEqualizer = true,
        albumLabel = if (albumId != null) t.ViewAlbum else null,
        artistNames = data.song.artists.toList(),
        onSpeedPitch = {
            onDismissRequest()
            onShowSpeed()
        },
        onSleepTimer = {
            onDismissRequest()
            onShowSleepTimer()
        },
        onTogglePauseOnSongEnd = {
            onDismissRequest()
            context.symphony.radio.setPauseOnCurrentSongEnd(!data.pauseOnCurrentSongEnd)
        },
        onAddToPlaylist = {
            onDismissRequest()
            onAddToPlaylist()
        },
        onViewAlbum = {
            if (albumId != null) {
                onDismissRequest()
                onClosePlayer()
                context.navController.navigate(AlbumViewRoute(albumId))
            }
        },
        onViewArtist = { artist ->
            onDismissRequest()
            onClosePlayer()
            context.navController.navigate(ArtistViewRoute(artist))
        },
        onEqualizer = {
            onDismissRequest()
            onLaunchEqualizer()
        },
    )
}
