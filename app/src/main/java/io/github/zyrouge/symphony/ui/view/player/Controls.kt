package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Song
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
    // While the thumb is dragged (or waits for the seek to land) the times follow it.
    var scrubRatio by remember { mutableStateOf<Float?>(null) }
    val played = scrubRatio?.let { (position.total * it).toLong() } ?: position.played
    Column {
        BentoSeekWave(
            context = context,
            position = position,
            playing = data.isPlaying,
            height = BentoSizes.SEEK_HEIGHT.dp,
            onScrub = { scrubRatio = it },
        )
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                DurationUtils.formatMs(played),
                style = bentoTabularStyle(BentoTypography.secondary),
                color = colors.sub,
            )
            Spacer(Modifier.weight(1f))
            Text(
                "-${DurationUtils.formatMs((position.total - played).coerceAtLeast(0L))}",
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
    val additionalInfo by context.symphony.settings.nowPlayingAdditionalInfo.flow.collectAsState()
    val formatLine = if (additionalInfo) bentoPlayerFormatLine(context, data.song) else null
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
            formatLine?.let {
                Text(
                    it,
                    style = bentoTabularStyle(
                        BentoTypography.label.copy(
                            fontWeight = FontWeight.Medium,
                            fontFamily = appFontFamily,
                        ),
                    ),
                    color = colors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = PLAYER_FORMAT_LINE_TOP_PADDING_DP.dp),
                )
            }
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
    val seekControls by context.symphony.settings.nowPlayingSeekControls.flow.collectAsState()
    val seekBack by context.symphony.settings.seekBackDuration.flow.collectAsState()
    val seekForward by context.symphony.settings.seekForwardDuration.flow.collectAsState()
    val playSize = if (short) BentoSizes.PLAY_BUTTON_MD else BentoSizes.PLAY_BUTTON_LG
    val timerMinutes by produceState(
        initialValue = data.sleepTimer?.let {
            bentoSleepTimerMinutesLeft(it.endsAt, System.currentTimeMillis())
        },
        key1 = data.sleepTimer,
    ) {
        val timer = data.sleepTimer
        if (timer != null) {
            // Wake exactly when the shown minute changes.
            while (true) {
                val now = System.currentTimeMillis()
                value = bentoSleepTimerMinutesLeft(timer.endsAt, now)
                delay(bentoSleepTimerNextMinuteDelayMs(timer.endsAt, now) ?: break)
            }
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        // Shrink every button by the same factor when the row would not fit, e.g. with the
        // seek buttons on a 360 dp phone or in the small floating window.
        val scale = bentoTransportScale(maxWidth.value, playSize, seekControls)
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
                label = context.symphony.t.PlayModeX(
                    when (playMode) {
                        PlayerMode.REPEAT_OFF -> context.symphony.t.RepeatOff
                        PlayerMode.REPEAT_ALL -> context.symphony.t.RepeatAll
                        PlayerMode.REPEAT_ONE -> context.symphony.t.RepeatOne
                        PlayerMode.SHUFFLE -> context.symphony.t.Shuffle
                    },
                ),
                size = BentoSizes.ICON_BUTTON.dp * scale,
                iconSize = BentoSizes.ICON.dp,
                tint = if (playMode == PlayerMode.REPEAT_OFF) colors.muted else colors.sub,
                onClick = {
                    val nextMode = nextPlayerMode(playMode)
                    context.symphony.radio.queue.run {
                        // Only a change reorders the queue, so turning shuffle on shuffles once.
                        if (nextMode.shuffle != currentShuffleMode) setShuffleMode(nextMode.shuffle)
                        setLoopMode(nextMode.loopMode)
                    }
                },
            )
            PlayerIconButton(
                icon = Icons.Filled.SkipPrevious,
                label = context.symphony.t.Previous,
                size = BentoSizes.PREV_NEXT_BUTTON.dp * scale,
                iconSize = BentoSizes.PREV_NEXT_ICON.dp * scale,
                tint = colors.fg,
                onClick = { context.symphony.radio.shorty.previous() },
            )
            if (seekControls) {
                PlayerIconButton(
                    icon = Icons.Filled.FastRewind,
                    label = context.symphony.t.BackXSeconds(seekBack.toString()),
                    size = BentoSizes.ICON_BUTTON.dp * scale,
                    iconSize = BentoSizes.ICON.dp,
                    tint = colors.sub,
                    onClick = { context.symphony.radio.shorty.seekFromCurrent(-seekBack) },
                )
            }
            BentoPlayButton(
                playing = data.isPlaying,
                size = playSize.dp * scale,
                iconSize = (if (short) 24.dp else 34.dp) * scale,
                label = if (data.isPlaying) context.symphony.t.Pause else context.symphony.t.Play,
                onClick = { context.symphony.radio.shorty.playPause() },
            )
            if (seekControls) {
                PlayerIconButton(
                    icon = Icons.Filled.FastForward,
                    label = context.symphony.t.ForwardXSeconds(seekForward.toString()),
                    size = BentoSizes.ICON_BUTTON.dp * scale,
                    iconSize = BentoSizes.ICON.dp,
                    tint = colors.sub,
                    onClick = { context.symphony.radio.shorty.seekFromCurrent(seekForward) },
                )
            }
            PlayerIconButton(
                icon = Icons.Filled.SkipNext,
                label = context.symphony.t.Next,
                size = BentoSizes.PREV_NEXT_BUTTON.dp * scale,
                iconSize = BentoSizes.PREV_NEXT_ICON.dp * scale,
                tint = colors.fg,
                onClick = { context.symphony.radio.shorty.skip() },
            )
            PlayerIconButton(
                icon = Icons.Outlined.Timer,
                label = timerMinutes?.let { context.symphony.t.SleepTimerXMinutesLeft(it.toString()) } ?: context.symphony.t.SleepTimerOff,
                size = BentoSizes.ICON_BUTTON.dp * scale,
                iconSize = BentoSizes.ICON.dp,
                tint = if (data.sleepTimer != null) colors.onTonal else colors.sub,
                background = if (data.sleepTimer != null) colors.tonal else Color.Transparent,
                selected = data.sleepTimer != null,
                badge = timerMinutes?.let { context.symphony.t.XMinutesShort(it.toString()) },
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
}

private const val PLAYER_FORMAT_LINE_TOP_PADDING_DP = 2

/** Height the format line adds under the player title, in dp at the default font scale. */
internal const val PLAYER_FORMAT_LINE_HEIGHT_DP = 18f

/** The player's format line for [song], or null when nothing about its format is known. */
internal fun bentoPlayerFormatLine(context: ViewContext, song: Song) = bentoAudioFormatLine(
    path = song.path,
    samplingRateK = song.samplingRateK,
    bitrateK = song.bitrateK,
    kHz = { context.symphony.t.XKHz(it) },
    kbps = { context.symphony.t.XKbps(it) },
)

/**
 * "FLAC · 44.1 kHz · 920 kbps": the file extension, the sampling rate and the
 * bitrate, leaving out whatever is unknown. Null when all three are missing.
 */
internal fun bentoAudioFormatLine(
    path: String,
    samplingRateK: Float?,
    bitrateK: Long?,
    kHz: (String) -> String,
    kbps: (String) -> String,
): String? = listOfNotNull(
    path.substringAfterLast('/').substringAfterLast('.', "").uppercase().takeIf { it.isNotEmpty() },
    samplingRateK?.let { kHz(it.toString().removeSuffix(".0")) },
    bitrateK?.let { kbps(it.toString()) },
).joinToString(" · ").takeIf { it.isNotEmpty() }

/**
 * Factor (at most 1) that shrinks the transport buttons so the row fits in
 * [availableWidthDp]: play mode, previous, play, next and the sleep timer,
 * plus back and forward when [seekControls] is on.
 */
internal fun bentoTransportScale(availableWidthDp: Float, playSizeDp: Int, seekControls: Boolean): Float {
    val iconButtons = if (seekControls) 4 else 2
    val naturalWidth = iconButtons * BentoSizes.ICON_BUTTON + 2 * BentoSizes.PREV_NEXT_BUTTON + playSizeDp
    return (availableWidthDp / naturalWidth).coerceIn(0f, 1f)
}

internal enum class PlayerMode(
    val loopMode: RadioQueue.LoopMode,
    val shuffle: Boolean,
) {
    REPEAT_OFF(RadioQueue.LoopMode.None, false),
    REPEAT_ALL(RadioQueue.LoopMode.Queue, false),
    REPEAT_ONE(RadioQueue.LoopMode.Song, false),

    // Shuffle keeps repeating the queue, so playback does not stop after the last shuffled song.
    SHUFFLE(RadioQueue.LoopMode.Queue, true),
}

/** The play mode button cycles repeat all -> repeat one -> shuffle -> repeat all. */
internal fun nextPlayerMode(mode: PlayerMode) = when (mode) {
    PlayerMode.REPEAT_OFF -> PlayerMode.REPEAT_ALL
    PlayerMode.REPEAT_ALL -> PlayerMode.REPEAT_ONE
    PlayerMode.REPEAT_ONE -> PlayerMode.SHUFFLE
    PlayerMode.SHUFFLE -> PlayerMode.REPEAT_ALL
}

internal fun resolvePlayerMode(loopMode: RadioQueue.LoopMode, shuffle: Boolean) = when {
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
    val labels = listOf(context.symphony.t.Lyrics, context.symphony.t.Queue, context.symphony.t.More)
    val textMeasurer = rememberTextMeasurer()
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        // At a large font or content scale the labelled buttons stop fitting and the
        // last one would be pushed out of reach, so they drop to icons only.
        val chromeWidth = with(LocalDensity.current) {
            (PLAYER_EXTRA_BUTTON_CHROME_DP * labels.size).dp.roundToPx()
        }
        val labelledWidth = chromeWidth + labels.sumOf {
            textMeasurer.measure(it, PlayerExtraLabelStyle, maxLines = 1).size.width
        }
        val showLabels = labelledWidth <= constraints.maxWidth
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlayerExtraButton(
                icon = { Icon(Icons.AutoMirrored.Outlined.Article, null) },
                label = context.symphony.t.Lyrics,
                selected = stage == BentoPlayerStage.LYRICS,
                showLabel = showLabels,
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
                showLabel = showLabels,
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
                    showLabel = showLabels,
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
}

@Composable
private fun PlayerExtraButton(
    icon: @Composable () -> Unit,
    label: String,
    selected: Boolean?,
    showLabel: Boolean,
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
                if (!showLabel) contentDescription = label
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
        if (showLabel) {
            Text(
                label,
                style = PlayerExtraLabelStyle,
                color = if (selected == true) colors.onTonal else colors.sub,
                maxLines = 1,
            )
        }
    }
}

private val PlayerExtraLabelStyle = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold)

/** A labelled extra button's padding (14 dp a side), icon (20 dp) and icon gap (6 dp). */
private const val PLAYER_EXTRA_BUTTON_CHROME_DP = 54

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
