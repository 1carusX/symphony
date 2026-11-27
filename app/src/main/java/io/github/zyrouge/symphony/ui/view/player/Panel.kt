package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.ui.components.bentoTransportScale
import io.github.zyrouge.symphony.ui.adapters.toUiSong
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.LocalStrings
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlin.math.max
import kotlin.math.min

private const val PHONE_MEDIUM_FIXED_CONTENT_DP = 349f

/** Small/medium player panel, including the gestures from DESIGN.md 6.5. */
@Composable
internal fun BentoPlayerPanel(
    context: ViewContext,
    data: BentoPlayerData,
    level: BentoPlayerLevel,
    stage: BentoPlayerStage,
    compact: Boolean,
    short: Boolean,
    canResize: Boolean,
    onLevelChange: (BentoPlayerLevel) -> Unit,
    onStageChange: (BentoPlayerStage) -> Unit,
    onClose: () -> Unit,
    onScrimAlphaChange: (Float) -> Unit,
) {
    var showSpeedPitchDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    val additionalInfo by context.symphony.settings.nowPlayingAdditionalInfo.flow.collectAsState()
    val seekControls by context.symphony.settings.nowPlayingSeekControls.flow.collectAsState()

    // Sizes of the sheet that grows between the small and the medium player.
    fun sheetSizes(contentWidth: Dp, availableHeight: Dp): Triple<Dp, Dp, Dp> {
        val smallHeight = min(
            BentoSizes.PHONE_SHEET_SMALL_HEIGHT_MAX.dp.value,
            availableHeight.value,
        ).dp
        // The format line adds height under the title; a shrunken transport row gives some back.
        val transportScale = bentoTransportScale(
            contentWidth.value,
            BentoSizes.PLAY_BUTTON_LG,
            seekControls,
        )
        val fixedContent = PHONE_MEDIUM_FIXED_CONTENT_DP -
            BentoSizes.PLAY_BUTTON_LG * (1f - transportScale) +
            when {
                additionalInfo && bentoPlayerFormatLine(context, data.song) != null ->
                    PLAYER_FORMAT_LINE_HEIGHT_DP

                else -> 0f
            }
        val coverSize = min(
            contentWidth.value,
            (availableHeight.value - fixedContent).coerceAtLeast(160f),
        ).dp
        val mediumHeight = min(
            availableHeight.value,
            coverSize.value + fixedContent,
        ).dp
        return Triple(smallHeight, mediumHeight, coverSize)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val topInset = max(
            BentoSizes.PHONE_SHEET_TOP_INSET_MIN.dp.value,
            maxHeight.value * BentoSizes.PHONE_SHEET_TOP_INSET_HEIGHT_RATIO,
        ).dp
        if (compact) {
            val (smallHeight, mediumHeight, coverSize) = sheetSizes(
                contentWidth = maxWidth - BentoSpacing.PLAYER_CONTENT_INSET.dp * 2,
                availableHeight = maxHeight - topInset,
            )
            BentoPhonePlayerSheet(
                context = context,
                data = data,
                level = level,
                stage = stage,
                containerHeight = maxHeight,
                smallHeight = smallHeight,
                mediumHeight = mediumHeight,
                coverSize = coverSize,
                onLevelChange = onLevelChange,
                onStageChange = onStageChange,
                onClose = onClose,
                onScrimAlphaChange = onScrimAlphaChange,
                onShowSpeed = { showSpeedPitchDialog = true },
                onShowPitch = { showSpeedPitchDialog = true },
                onShowSleepTimer = { showSleepTimerDialog = true },
                modifier = Modifier.align(Alignment.BottomCenter),
            )
        } else if (!short) {
            // Tablets, foldables and desktop windows: the same growing sheet as a
            // floating window, so the medium player opens with the finger.
            val inset = BentoSizes.FLOATING_PLAYER_INSET.dp
            val (smallHeight, mediumHeight, coverSize) = sheetSizes(
                contentWidth = BentoSizes.FLOATING_PLAYER_WIDTH.dp -
                    BentoSpacing.PLAYER_CONTENT_INSET.dp * 2,
                availableHeight = min(
                    (maxHeight - inset * 2).value,
                    BentoSizes.FLOATING_PLAYER_MEDIUM_MAX_HEIGHT.toFloat(),
                ).dp,
            )
            BentoPhonePlayerSheet(
                context = context,
                data = data,
                level = level,
                stage = stage,
                containerHeight = maxHeight - inset * 2,
                smallHeight = smallHeight,
                mediumHeight = mediumHeight,
                coverSize = coverSize,
                onLevelChange = onLevelChange,
                onStageChange = onStageChange,
                onClose = onClose,
                onScrimAlphaChange = {},
                onShowSpeed = { showSpeedPitchDialog = true },
                onShowPitch = { showSpeedPitchDialog = true },
                onShowSleepTimer = { showSleepTimerDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(inset)
                    .width(BentoSizes.FLOATING_PLAYER_WIDTH.dp),
                floating = true,
                canResize = canResize,
            )
        } else {
            BentoFloatingPlayerWindow(
                context = context,
                data = data,
                level = level,
                stage = stage,
                canResize = canResize,
                onLevelChange = onLevelChange,
                onStageChange = onStageChange,
                onClose = onClose,
                onShowSpeed = { showSpeedPitchDialog = true },
                onShowPitch = { showSpeedPitchDialog = true },
                onShowSleepTimer = { showSleepTimerDialog = true },
            )
        }
    }

    if (showSpeedPitchDialog) {
        NowPlayingSpeedPitchDialog(
            context = context,
            currentSpeed = data.currentSpeed,
            currentPitch = data.currentPitch,
            persistedSpeed = data.persistedSpeed,
            persistedPitch = data.persistedPitch,
            onDismissRequest = { showSpeedPitchDialog = false },
        )
    }
    if (showSleepTimerDialog) {
        data.sleepTimer?.let {
            NowPlayingSleepTimerDialog(
                context = context,
                sleepTimer = it,
                onDismissRequest = { showSleepTimerDialog = false },
            )
        } ?: NowPlayingSleepTimerSetDialog(
            context = context,
            onDismissRequest = { showSleepTimerDialog = false },
        )
    }
}

/**
 * The floating player window of short (phone landscape) windows: the shared
 * container supplies the geometry and gestures, the app supplies both layouts.
 */
@Composable
private fun BoxWithConstraintsScope.BentoFloatingPlayerWindow(
    context: ViewContext,
    data: BentoPlayerData,
    level: BentoPlayerLevel,
    stage: BentoPlayerStage,
    canResize: Boolean,
    onLevelChange: (BentoPlayerLevel) -> Unit,
    onStageChange: (BentoPlayerStage) -> Unit,
    onClose: () -> Unit,
    onShowSpeed: () -> Unit,
    onShowPitch: () -> Unit,
    onShowSleepTimer: () -> Unit,
) {
    val colors = LocalBentoColors.current
    FloatingPlayerWindow(
        level = level,
        canResize = canResize,
        onLevelChange = onLevelChange,
        onClose = onClose,
        header = { shownLevel, drag, onToggleLevel ->
            Box(modifier = Modifier.fillMaxWidth()) {
                if (canResize) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 6.dp)
                            .size(36.dp, 4.dp)
                            .clip(CircleShape)
                            .background(colors.muted.copy(alpha = 0.7f)),
                    )
                }
                PlayerPanelHeader(
                    context = context,
                    data = data,
                    level = shownLevel,
                    canResize = canResize,
                    onClose = onClose,
                    onToggleLevel = onToggleLevel,
                    onDragStart = drag.onDragStart,
                    onDrag = drag.onDrag,
                    onDragEnd = drag.onDragEnd,
                )
            }
        },
        small = {
            PlayerSmallContent(
                context = context,
                data = data,
                stage = stage,
                compact = false,
                short = true,
                onStageChange = onStageChange,
                onShowSpeed = onShowSpeed,
                onShowPitch = onShowPitch,
                onShowSleepTimer = onShowSleepTimer,
                onClosePlayer = onClose,
            )
        },
        medium = {
            PlayerMediumLandscapeContent(
                context = context,
                data = data,
                stage = stage,
                compact = false,
                onStageChange = onStageChange,
                onShowSpeed = onShowSpeed,
                onShowPitch = onShowPitch,
                onShowSleepTimer = onShowSleepTimer,
                onClosePlayer = onClose,
            )
        },
    )
}

@Composable
private fun PlayerPanelHeader(
    context: ViewContext,
    data: BentoPlayerData,
    level: BentoPlayerLevel,
    canResize: Boolean,
    onClose: () -> Unit,
    onToggleLevel: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (uptimeMillis: Long, amountPx: Float) -> Unit,
    onDragEnd: () -> Unit,
) {
    val dragModifier = when {
        canResize -> Modifier
            .pointerInput(level) {
                detectTapGestures { onToggleLevel() }
            }
            .pointerInput(level) {
                detectVerticalDragGestures(
                    onDragStart = { onDragStart() },
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(change.uptimeMillis, dragAmount)
                    },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd,
                )
            }

        else -> Modifier
    }

    Row(
        modifier = Modifier.padding(start = 20.dp, end = 8.dp, top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (level) {
            BentoPlayerLevel.SMALL -> PlayerSmallHeaderInfo(
                context = context,
                data = data,
                modifier = Modifier
                    .weight(1f)
                    .then(dragModifier)
                    .padding(top = 12.dp, bottom = 4.dp),
            )

            BentoPlayerLevel.MEDIUM -> PlayerMediumHeaderInfo(
                context = context,
                modifier = Modifier
                    .weight(1f)
                    .then(dragModifier),
            )
        }
        PlayerCloseButton(contentDescription = LocalStrings.current.ClosePlayer, onClose = onClose)
    }
}

/** Small-level header: 56 dp art with the title and "artist · album". */
@Composable
internal fun PlayerSmallHeaderInfo(
    context: ViewContext,
    data: BentoPlayerData,
    modifier: Modifier = Modifier,
) {
    PlayerHeaderInfo(
        song = data.song.toUiSong(),
        artwork = { artworkModifier ->
            AsyncImage(
                data.song.createArtworkImageRequest(context.symphony).build(),
                null,
                contentScale = ContentScale.Crop,
                modifier = artworkModifier,
            )
        },
        modifier = modifier,
    )
}

/** Medium-level header: where the queue is playing from. */
@Composable
internal fun PlayerMediumHeaderInfo(
    context: ViewContext,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current
    val source by context.symphony.radio.observatory.queueSource.collectAsState()
    Column(
        modifier = modifier.height(48.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            context.symphony.t.PlayingFrom,
            style = BentoTypography.label,
            color = colors.sub,
            maxLines = 1,
        )
        Text(
            source ?: context.symphony.t.Queue,
            style = BentoTypography.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
            color = colors.fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
