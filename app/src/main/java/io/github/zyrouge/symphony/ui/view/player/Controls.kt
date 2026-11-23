package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.rounded.QueueMusic
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.radio.RadioQueue
import io.github.zyrouge.symphony.ui.components.BentoSeekWave
import io.github.zyrouge.symphony.ui.components.bentoTabularStyle
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.utils.DurationUtils
import kotlinx.coroutines.delay

@Composable
internal fun BentoPlayerSeek(context: ViewContext, data: BentoPlayerData) {
    val colors = LocalBentoColors.current
    val position = data.playbackPosition
    Column {
        BentoSeekWave(
            context = context,
            position = position,
            playing = data.isPlaying,
            height = BentoSizes.SEEK_HEIGHT.dp,
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                DurationUtils.formatMs(position.played),
                style = bentoTabularStyle(BentoTypography.secondary),
                color = colors.sub,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "-${DurationUtils.formatMs((position.total - position.played).coerceAtLeast(0L))}",
                style = bentoTabularStyle(BentoTypography.secondary),
                color = colors.sub,
            )
        }
    }
}

@Composable
internal fun BentoPlayerTitle(context: ViewContext, data: BentoPlayerData) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val favoriteSongIds by context.symphony.groove.playlist.favorites.collectAsState()
    val isFavorite by remember(data.song.id, favoriteSongIds) {
        derivedStateOf { favoriteSongIds.contains(data.song.id) }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                data.song.title,
                style = BentoTypography.title.copy(fontFamily = appFontFamily),
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                listOfNotNull(
                    data.song.artists.joinToString().takeIf { it.isNotEmpty() },
                    data.song.album,
                ).joinToString(" · "),
                style = BentoTypography.rowTitle.copy(
                    fontWeight = FontWeight.Normal,
                    fontFamily = appFontFamily,
                ),
                color = colors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        PlayerIconButton(
            icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            label = if (isFavorite) context.symphony.t.Unfavorite else context.symphony.t.Favorite,
            size = BentoSizes.ICON_BUTTON.dp,
            iconSize = BentoSizes.ICON.dp,
            tint = if (isFavorite) colors.accent else colors.sub,
            selected = isFavorite,
            onClick = {
                context.symphony.groove.playlist.run {
                    if (isFavorite) unfavorite(data.song.id) else favorite(data.song.id)
                }
            },
        )
    }
}

@Composable
internal fun BentoPlayerTransport(
    context: ViewContext,
    data: BentoPlayerData,
    short: Boolean,
    onShowSleepTimer: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val playMode = resolvePlayerMode(data.currentLoopMode, data.currentShuffleMode)
    val timerMinutes by produceState(
        initialValue = data.sleepTimer?.let {
            bentoSleepTimerMinutesLeft(it.endsAt, System.currentTimeMillis())
        },
        key1 = data.sleepTimer,
    ) {
        val timer = data.sleepTimer
        if (timer != null) {
            while (true) {
                value = bentoSleepTimerMinutesLeft(timer.endsAt, System.currentTimeMillis())
                delay(30_000)
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        PlayerIconButton(
            icon = when (playMode) {
                PlayerMode.REPEAT_ALL, PlayerMode.REPEAT_OFF -> Icons.Filled.Repeat
                PlayerMode.REPEAT_ONE -> Icons.Filled.RepeatOne
                PlayerMode.SHUFFLE -> Icons.Filled.Shuffle
            },
            label = context.symphony.t.PlayModeX(playMode.label),
            size = BentoSizes.ICON_BUTTON.dp,
            iconSize = BentoSizes.ICON.dp,
            tint = if (playMode == PlayerMode.REPEAT_OFF) colors.muted else colors.sub,
            onClick = {
                val nextMode = when (playMode) {
                    PlayerMode.REPEAT_OFF -> PlayerMode.REPEAT_ALL
                    PlayerMode.REPEAT_ALL -> PlayerMode.REPEAT_ONE
                    PlayerMode.REPEAT_ONE -> PlayerMode.SHUFFLE
                    PlayerMode.SHUFFLE -> PlayerMode.REPEAT_ALL
                }
                when (nextMode) {
                    PlayerMode.REPEAT_ALL -> {
                        context.symphony.radio.queue.setShuffleMode(false)
                        context.symphony.radio.queue.setLoopMode(RadioQueue.LoopMode.Queue)
                    }

                    PlayerMode.REPEAT_ONE -> {
                        context.symphony.radio.queue.setShuffleMode(false)
                        context.symphony.radio.queue.setLoopMode(RadioQueue.LoopMode.Song)
                    }

                    PlayerMode.SHUFFLE -> {
                        context.symphony.radio.queue.setLoopMode(RadioQueue.LoopMode.None)
                        context.symphony.radio.queue.setShuffleMode(true)
                    }

                    PlayerMode.REPEAT_OFF -> Unit
                }
            },
        )
        PlayerIconButton(
            icon = Icons.Filled.SkipPrevious,
            label = context.symphony.t.Previous,
            size = BentoSizes.PREV_NEXT_BUTTON.dp,
            iconSize = BentoSizes.PREV_NEXT_ICON.dp,
            tint = colors.fg,
            onClick = { context.symphony.radio.shorty.previous() },
        )
        BentoPlayButton(
            playing = data.isPlaying,
            size = if (short) BentoSizes.PLAY_BUTTON_MD.dp else BentoSizes.PLAY_BUTTON_LG.dp,
            iconSize = if (short) 24.dp else 34.dp,
            label = if (data.isPlaying) "Pause" else "Play",
            onClick = { context.symphony.radio.shorty.playPause() },
        )
        PlayerIconButton(
            icon = Icons.Filled.SkipNext,
            label = context.symphony.t.Next,
            size = BentoSizes.PREV_NEXT_BUTTON.dp,
            iconSize = BentoSizes.PREV_NEXT_ICON.dp,
            tint = colors.fg,
            onClick = { context.symphony.radio.shorty.skip() },
        )
        PlayerIconButton(
            icon = Icons.Outlined.Timer,
            label = timerMinutes?.let { "Sleep timer: $it minutes left" } ?: "Sleep timer: off",
            size = BentoSizes.ICON_BUTTON.dp,
            iconSize = BentoSizes.ICON.dp,
            tint = if (data.sleepTimer != null) colors.onTonal else colors.sub,
            background = if (data.sleepTimer != null) colors.tonal else Color.Transparent,
            selected = data.sleepTimer != null,
            badge = timerMinutes?.let { "${it}m" },
            onClick = {
                val next = nextBentoSleepTimerMinutes(data.sleepTimer?.duration)
                if (next == null) {
                    context.symphony.radio.clearSleepTimer()
                } else {
                    context.symphony.radio.setSleepTimer(
                        duration = next * 60_000L,
                        quitOnEnd = data.sleepTimer?.quitOnEnd ?: false,
                    )
                }
            },
            onLongClick = onShowSleepTimer,
        )
    }
}

private enum class PlayerMode(val label: String) {
    REPEAT_OFF("Repeat off"),
    REPEAT_ALL("Repeat all"),
    REPEAT_ONE("Repeat one"),
    SHUFFLE("Shuffle"),
}

private fun resolvePlayerMode(loopMode: RadioQueue.LoopMode, shuffle: Boolean) = when {
    shuffle -> PlayerMode.SHUFFLE
    loopMode == RadioQueue.LoopMode.Song -> PlayerMode.REPEAT_ONE
    loopMode == RadioQueue.LoopMode.Queue -> PlayerMode.REPEAT_ALL
    else -> PlayerMode.REPEAT_OFF
}

@Composable
internal fun BentoPlayerExtras(
    context: ViewContext,
    data: BentoPlayerData,
    stage: BentoPlayerStage,
    compact: Boolean,
    onStageChange: (BentoPlayerStage) -> Unit,
    onShowSpeed: () -> Unit,
    onShowPitch: () -> Unit,
    onShowSleepTimer: () -> Unit,
    onClosePlayer: () -> Unit,
) {
    var showMore by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlayerExtraButton(
            icon = { Icon(Icons.AutoMirrored.Outlined.Article, null) },
            label = context.symphony.t.Lyrics,
            selected = stage == BentoPlayerStage.LYRICS,
            onClick = {
                onStageChange(
                    if (stage == BentoPlayerStage.LYRICS) BentoPlayerStage.COVER
                    else BentoPlayerStage.LYRICS,
                )
            },
        )
        PlayerExtraButton(
            icon = { Icon(Icons.AutoMirrored.Rounded.QueueMusic, null) },
            label = context.symphony.t.Queue,
            selected = stage == BentoPlayerStage.QUEUE,
            onClick = {
                onStageChange(
                    if (stage == BentoPlayerStage.QUEUE) BentoPlayerStage.COVER
                    else BentoPlayerStage.QUEUE,
                )
            },
        )
        Box {
            PlayerExtraButton(
                icon = { Icon(Icons.Outlined.MoreHoriz, null) },
                label = context.symphony.t.More,
                selected = null,
                onClick = { showMore = true },
            )
            BentoPlayerMoreMenu(
                context = context,
                data = data,
                compact = compact,
                expanded = showMore,
                onDismissRequest = { showMore = false },
                onShowSpeed = onShowSpeed,
                onShowPitch = onShowPitch,
                onShowSleepTimer = onShowSleepTimer,
                onClosePlayer = onClosePlayer,
            )
        }
    }
}

@Composable
private fun PlayerExtraButton(
    icon: @Composable () -> Unit,
    label: String,
    selected: Boolean?,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(BentoRadii.CHIP.dp))
            .background(if (selected == true) colors.tonal else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp)
            .semantics {
                role = Role.Button
                selected?.let {
                    toggleableState = if (it) ToggleableState.On else ToggleableState.Off
                }
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides
                if (selected == true) colors.onTonal else colors.sub,
        ) {
            Box(modifier = Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                icon()
            }
        }
        Text(
            label,
            style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
            color = if (selected == true) colors.onTonal else colors.sub,
            maxLines = 1,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlayerIconButton(
    icon: ImageVector,
    label: String,
    size: Dp,
    iconSize: Dp,
    tint: Color,
    background: Color = Color.Transparent,
    selected: Boolean? = null,
    badge: String? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(background)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                    role = Role.Button,
                )
                .semantics {
                    selected?.let {
                        toggleableState = if (it) ToggleableState.On else ToggleableState.Off
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                icon,
                label,
                tint = tint,
                modifier = Modifier.size(iconSize),
            )
        }
        if (badge != null) {
            Text(
                badge,
                style = bentoTabularStyle(BentoTypography.label.copy(fontWeight = FontWeight.Bold)),
                color = colors.onAccent,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 6.dp, y = (-6).dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(colors.accent)
                    .padding(horizontal = 5.dp),
                maxLines = 1,
            )
        }
    }
}
