package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.ui.components.bentoFloatingSurface
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoMotion
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlinx.coroutines.launch
import kotlin.math.abs
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

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val topInset = max(
            BentoSizes.PHONE_SHEET_TOP_INSET_MIN.dp.value,
            maxHeight.value * BentoSizes.PHONE_SHEET_TOP_INSET_HEIGHT_RATIO,
        ).dp
        if (compact) {
            val availableHeight = maxHeight - topInset
            val smallHeight = min(
                BentoSizes.PHONE_SHEET_SMALL_HEIGHT_MAX.dp.value,
                availableHeight.value,
            ).dp
            val contentWidth = maxWidth - BentoSpacing.PLAYER_CONTENT_INSET.dp * 2
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
        } else {
            BentoFloatingPlayerWindow(
                context = context,
                data = data,
                level = level,
                stage = stage,
                short = short,
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
 * The floating player window (tablets, foldables, desktop). Level changes
 * cross-fade the content and animate the window's size; touch windows also
 * follow the finger (DESIGN.md 6.5).
 */
@Composable
private fun BoxWithConstraintsScope.BentoFloatingPlayerWindow(
    context: ViewContext,
    data: BentoPlayerData,
    level: BentoPlayerLevel,
    stage: BentoPlayerStage,
    short: Boolean,
    canResize: Boolean,
    onLevelChange: (BentoPlayerLevel) -> Unit,
    onStageChange: (BentoPlayerStage) -> Unit,
    onClose: () -> Unit,
    onShowSpeed: () -> Unit,
    onShowPitch: () -> Unit,
    onShowSleepTimer: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }
    var dragStartedAt by remember { mutableStateOf(level) }
    var dragDistanceDp by remember { mutableFloatStateOf(0f) }
    var dragMoved by remember { mutableStateOf(false) }

    val floatingInset = when {
        short -> BentoSizes.FLOATING_PLAYER_LANDSCAPE_INSET.dp
        else -> BentoSizes.FLOATING_PLAYER_INSET.dp
    }
    val availableHeight = maxHeight - floatingInset * 2
    val mediumWidth = when {
        short -> min(
            BentoSizes.FLOATING_PLAYER_LANDSCAPE_MEDIUM_WIDTH.dp.value,
            (maxWidth - BentoSizes.RAIL_SHORT.dp - floatingInset * 2).value,
        ).dp

        else -> BentoSizes.FLOATING_PLAYER_WIDTH.dp
    }
    val mediumHeight = when {
        short -> availableHeight
        else -> min(availableHeight.value, BentoSizes.FLOATING_PLAYER_MEDIUM_MAX_HEIGHT.dp.value).dp
    }

    fun finishDrag() {
        if (!dragMoved) return
        val target = resolveBentoFloatingPlayerRelease(dragOffset.value / density.density, dragStartedAt)
        if (target == null) {
            onClose()
            return
        }
        coroutineScope.launch {
            onLevelChange(target)
            dragOffset.animateTo(
                0f,
                animationSpec = tween(
                    BentoMotion.PHONE_SHEET_OPEN.durationMs,
                    easing = BentoMotion.DECELERATE,
                ),
            )
        }
    }

    val shape = RoundedCornerShape(BentoRadii.FLOATING_PLAYER.dp)
    Column(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(floatingInset)
            .width(
                when {
                    short && level == BentoPlayerLevel.MEDIUM -> mediumWidth
                    else -> BentoSizes.FLOATING_PLAYER_WIDTH.dp
                },
            )
            .graphicsLayer { translationY = dragOffset.value }
            .bentoFloatingSurface(shape, colors.`float`)
            .pointerInput(Unit) {
                // Swallow taps that miss a control so they do not reach the page
                // scrolling underneath the floating panel.
                detectTapGestures(onTap = {})
            },
    ) {
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
                level = level,
                canResize = canResize,
                onClose = onClose,
                onToggleLevel = {
                    onLevelChange(
                        when (level) {
                            BentoPlayerLevel.SMALL -> BentoPlayerLevel.MEDIUM
                            BentoPlayerLevel.MEDIUM -> BentoPlayerLevel.SMALL
                        },
                    )
                },
                onDragStart = {
                    dragStartedAt = level
                    dragDistanceDp = 0f
                    dragMoved = false
                },
                onDrag = { amountPx ->
                    dragDistanceDp += amountPx / density.density
                    if (!dragMoved && abs(dragDistanceDp) < 6f) {
                        return@PlayerPanelHeader
                    }
                    dragMoved = true
                    val offsetDp = bentoFloatingPlayerDragOffset(dragDistanceDp)
                    coroutineScope.launch {
                        dragOffset.snapTo(with(density) { offsetDp.dp.toPx() })
                    }
                },
                onDragEnd = ::finishDrag,
            )
        }

        // The content cross-fades and the window resizes between the levels (DESIGN.md 3.3).
        AnimatedContent(
            targetState = level,
            transitionSpec = {
                fadeIn(
                    tween(BentoMotion.LEVEL_CONTENT_FADE.durationMs, delayMillis = 60),
                ) togetherWith fadeOut(tween(120)) using SizeTransform(clip = true) { _, _ ->
                    spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessMediumLow)
                }
            },
            label = "player-level",
        ) { shownLevel ->
            when (shownLevel) {
                BentoPlayerLevel.SMALL -> PlayerSmallContent(
                    context = context,
                    data = data,
                    stage = stage,
                    compact = false,
                    short = short,
                    onStageChange = onStageChange,
                    onShowSpeed = onShowSpeed,
                    onShowPitch = onShowPitch,
                    onShowSleepTimer = onShowSleepTimer,
                    onClosePlayer = onClose,
                )

                BentoPlayerLevel.MEDIUM -> Column(modifier = Modifier.height(mediumHeight)) {
                    when {
                        short -> PlayerMediumLandscapeContent(
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

                        else -> PlayerMediumContent(
                            context = context,
                            data = data,
                            stage = stage,
                            compact = false,
                            phoneCoverSize = 0.dp,
                            onStageChange = onStageChange,
                            onShowSpeed = onShowSpeed,
                            onShowPitch = onShowPitch,
                            onShowSleepTimer = onShowSleepTimer,
                            onClosePlayer = onClose,
                        )
                    }
                }
            }
        }
    }
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
    onDrag: (Float) -> Unit,
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
                        onDrag(dragAmount)
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
        PlayerCloseButton(onClose = onClose)
    }
}

/** Small-level header: 56 dp art with the title and "artist · album". */
@Composable
internal fun PlayerSmallHeaderInfo(
    context: ViewContext,
    data: BentoPlayerData,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AsyncImage(
            data.song.createArtworkImageRequest(context.symphony).build(),
            null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp)),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                data.song.title,
                style = BentoTypography.groupTitle.copy(fontFamily = appFontFamily),
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                listOfNotNull(
                    data.song.artists.joinToString().takeIf { it.isNotEmpty() },
                    data.song.album,
                ).joinToString(" · "),
                style = BentoTypography.body.copy(fontFamily = appFontFamily),
                color = colors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
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
            "Playing from",
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

@Composable
internal fun PlayerCloseButton(onClose: () -> Unit) {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier
            .size(BentoSizes.ICON_BUTTON.dp)
            .clip(CircleShape)
            .clickable(onClick = onClose),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.Close,
            "Close player",
            tint = colors.sub,
            modifier = Modifier.size(BentoSizes.ICON.dp),
        )
    }
}
