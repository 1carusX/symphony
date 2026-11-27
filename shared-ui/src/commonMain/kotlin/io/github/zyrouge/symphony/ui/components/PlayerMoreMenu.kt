package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.i18n.Strings
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * The player's More menu content: the song header and the player actions.
 * The menu shell and the Android-only actions stay with the platform.
 */
@Composable
fun BentoPlayerMoreMenuContent(
    strings: Strings,
    title: String,
    formatLine: String?,
    artwork: @Composable (Modifier) -> Unit,
    speedPitchLabel: String,
    speedPitchValue: String,
    sleepTimerActive: Boolean,
    pauseOnSongEnd: Boolean,
    showEqualizer: Boolean,
    albumLabel: String?,
    artistNames: List<String>,
    onSpeedPitch: () -> Unit,
    onSleepTimer: () -> Unit,
    onTogglePauseOnSongEnd: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onViewAlbum: () -> Unit,
    onViewArtist: (String) -> Unit,
    onEqualizer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current
    val menuScroll = rememberScrollState()
    Column(
        modifier = modifier
            .heightIn(max = 520.dp)
            .bentoScrollbar(menuScroll, colors.muted, BentoRadii.MENU.dp)
            .verticalScroll(menuScroll),
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            artwork(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(BentoRadii.SONG_ART_44.dp)),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = BentoTypography.rowTitle,
                    color = colors.fg,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                formatLine?.let {
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
        PlayerMenuRow(
            icon = { Icon(Icons.Outlined.Speed, null) },
            label = speedPitchLabel,
            value = speedPitchValue,
            onClick = onSpeedPitch,
        )
        PlayerMenuRow(
            icon = { Icon(Icons.Outlined.Timer, null) },
            label = strings.SleepTimer,
            value = if (sleepTimerActive) strings.On else strings.Off,
            onClick = onSleepTimer,
        )
        PlayerMenuRow(
            icon = { Icon(Icons.Filled.MotionPhotosPaused, null) },
            label = strings.PauseOnCurrentSongEnd,
            value = if (pauseOnSongEnd) strings.On else strings.Off,
            onClick = onTogglePauseOnSongEnd,
        )
        PlayerMenuRow(
            icon = { Icon(Icons.AutoMirrored.Filled.PlaylistAdd, null) },
            label = strings.AddToPlaylist,
            onClick = onAddToPlaylist,
        )
        albumLabel?.let {
            PlayerMenuRow(
                icon = { Icon(Icons.Filled.Album, null) },
                label = it,
                onClick = onViewAlbum,
            )
        }
        artistNames.forEach { artist ->
            PlayerMenuRow(
                icon = { Icon(Icons.Filled.Person, null) },
                label = "${strings.ViewArtist}: $artist",
                onClick = { onViewArtist(artist) },
            )
        }
        if (showEqualizer) {
            PlayerMenuRow(
                icon = { Icon(Icons.Filled.GraphicEq, null) },
                label = strings.Equalizer,
                onClick = onEqualizer,
            )
        }
    }
}

@Composable
private fun PlayerMenuRow(
    icon: @Composable () -> Unit,
    label: String,
    onClick: () -> Unit,
    value: String? = null,
) {
    val colors = LocalBentoColors.current
    MenuRow(
        leading = {
            Box(modifier = Modifier.size(22.dp), contentAlignment = Alignment.Center) {
                CompositionLocalProvider(LocalContentColor provides colors.sub) {
                    icon()
                }
            }
        },
        label = label,
        value = value,
        onClick = onClick,
    )
}
