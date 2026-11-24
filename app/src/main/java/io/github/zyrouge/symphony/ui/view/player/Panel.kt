package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.BoxWithConstraintsScope
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.ui.components.bentoFloatingSurface
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.LocalStrings
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

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

/** Settles the landscape window between its levels, like the phone sheet. */
private val LandscapeLevelSpring = spring<Float>(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow)

/**
 * The floating player window of short (phone landscape) windows, where the
 * medium player is a wide two-column window. One progress value (0 small,
 * 1 medium) drives the window's width and height and a cross-fade of the two
 * layouts, anchored at the bottom-right corner; a drag on the header moves it
 * with the finger and a release settles it (DESIGN.md 6.5). Dragging on down
 * from the small window closes it.
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
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val latestLevel by rememberUpdatedState(level)

    val floatingInset = BentoSizes.FLOATING_PLAYER_LANDSCAPE_INSET.dp
    val smallWidth = BentoSizes.FLOATING_PLAYER_WIDTH.dp
    val mediumWidth = min(
        BentoSizes.FLOATING_PLAYER_LANDSCAPE_MEDIUM_WIDTH.dp.value,
        (maxWidth - BentoSizes.RAIL_SHORT.dp - floatingInset * 2).value,
    ).dp
    val mediumHeight = maxHeight - floatingInset * 2
    // The small window is as tall as its content, measured as it is laid out.
    var smallHeightPx by remember { mutableIntStateOf(0) }

    fun progressOf(target: BentoPlayerLevel) = when (target) {
        BentoPlayerLevel.SMALL -> 0f
        BentoPlayerLevel.MEDIUM -> 1f
    }

    val progress = remember { Animatable(progressOf(level)) }
    // How far a downward drag has pulled the small window towards closing, in px.
    val closeOffset = remember { Animatable(0f) }
    var dragging by remember { mutableStateOf(false) }
    // Set while a release runs its own settle animation, so the level effect does not restart it.
    var settlingTo by remember { mutableStateOf<BentoPlayerLevel?>(null) }
    LaunchedEffect(level) {
        if (dragging || settlingTo == level) return@LaunchedEffect
        progress.animateTo(progressOf(level), LandscapeLevelSpring)
    }

    val velocityTracker = remember { VelocityTracker() }
    var dragTotalPx by remember { mutableFloatStateOf(0f) }
    var dragStartProgress by remember { mutableFloatStateOf(0f) }
    fun rangePx() = with(density) {
        val small = if (smallHeightPx > 0) smallHeightPx.toFloat() else mediumHeight.toPx() / 2f
        (mediumHeight.toPx() - small).coerceAtLeast(1f)
    }

    fun release(velocityPxPerSecond: Float) {
        dragging = false
        val velocityDpPerMs = velocityPxPerSecond / density.density / 1000f
        val closedDp = closeOffset.value / density.density
        val target = when {
            closedDp > 40f -> null
            closedDp > 0f && velocityDpPerMs > 0.45f -> null
            velocityDpPerMs < -0.45f -> BentoPlayerLevel.MEDIUM
            velocityDpPerMs > 0.45f -> BentoPlayerLevel.SMALL
            progress.value >= 0.5f -> BentoPlayerLevel.MEDIUM
            else -> BentoPlayerLevel.SMALL
        }
        if (target == null) {
            onClose()
            return
        }
        settlingTo = target
        coroutineScope.launch { closeOffset.animateTo(0f, LandscapeLevelSpring) }
        coroutineScope.launch {
            progress.animateTo(
                targetValue = progressOf(target),
                animationSpec = LandscapeLevelSpring,
                initialVelocity = -velocityPxPerSecond / rangePx(),
            )
            settlingTo = null
        }
        if (target != latestLevel) onLevelChange(target)
    }

    val header: @Composable (BentoPlayerLevel) -> Unit = { shownLevel ->
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
                onToggleLevel = {
                    onLevelChange(
                        when (latestLevel) {
                            BentoPlayerLevel.SMALL -> BentoPlayerLevel.MEDIUM
                            BentoPlayerLevel.MEDIUM -> BentoPlayerLevel.SMALL
                        },
                    )
                },
                onDragStart = {
                    dragging = true
                    dragTotalPx = 0f
                    dragStartProgress = progress.value
                    velocityTracker.resetTracking()
                },
                onDrag = { uptimeMillis, amountPx ->
                    dragTotalPx += amountPx
                    // The header moves with the window, so track the drag distance, not the position.
                    velocityTracker.addPosition(uptimeMillis, Offset(0f, dragTotalPx))
                    val raw = dragStartProgress - dragTotalPx / rangePx()
                    coroutineScope.launch {
                        progress.snapTo(raw.coerceIn(0f, 1f))
                        // Past the small level the window follows the finger down towards closing.
                        closeOffset.snapTo(if (raw < 0f) -raw * rangePx() else 0f)
                    }
                },
                onDragEnd = { release(velocityTracker.calculateVelocity().y) },
            )
        }
    }

    val showSmall by remember { derivedStateOf { progress.value < 1f } }
    val showMedium by remember { derivedStateOf { progress.value > 0f } }
    val shape = RoundedCornerShape(BentoRadii.FLOATING_PLAYER.dp)
    Box(
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(floatingInset)
            .graphicsLayer { translationY = closeOffset.value }
            .layout { measurable, _ ->
                val p = progress.value
                val smallHeight = when {
                    smallHeightPx > 0 -> smallHeightPx.toFloat()
                    else -> mediumHeight.toPx() / 2f
                }
                val width = smallWidth.toPx() + (mediumWidth.toPx() - smallWidth.toPx()) * p
                val height = smallHeight + (mediumHeight.toPx() - smallHeight) * p
                val placeable = measurable.measure(
                    Constraints.fixed(width.roundToInt(), height.roundToInt()),
                )
                layout(placeable.width, placeable.height) { placeable.place(0, 0) }
            }
            .bentoFloatingSurface(shape, colors.`float`)
            .pointerInput(Unit) {
                // Swallow taps that miss a control so they do not reach the page
                // scrolling underneath the floating panel.
                detectTapGestures(onTap = {})
            },
    ) {
        // Each layout keeps its own size, anchored at the bottom-right corner; the
        // window clips whichever is larger and the two cross-fade over its growth.
        if (showSmall) {
            Column(
                modifier = Modifier
                    .wrapContentSize(Alignment.BottomEnd, unbounded = true)
                    .width(smallWidth)
                    .onSizeChanged { smallHeightPx = it.height }
                    .graphicsLayer { alpha = (1f - progress.value * 2f).coerceIn(0f, 1f) },
            ) {
                header(BentoPlayerLevel.SMALL)
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
            }
        }
        if (showMedium) {
            Column(
                modifier = Modifier
                    .wrapContentSize(Alignment.BottomEnd, unbounded = true)
                    .size(mediumWidth, mediumHeight)
                    .graphicsLayer { alpha = ((progress.value - 0.5f) * 2f).coerceIn(0f, 1f) },
            ) {
                header(BentoPlayerLevel.MEDIUM)
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
            LocalStrings.current.ClosePlayer,
            tint = colors.sub,
            modifier = Modifier.size(BentoSizes.ICON.dp),
        )
    }
}
