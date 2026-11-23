package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.bentoSystemBottomInset
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.roundToInt

private val LevelSpring = spring<Float>(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow)

/**
 * The phone player sheet (DESIGN.md 6.2, 6.5). One surface whose visible
 * height is the single animated value behind every level change: a drag, a
 * header tap, opening lyrics or the queue. Between the small and the medium
 * height the sheet grows upward: the controls stay pinned to the bottom, the
 * cover grows and fades in above them and the two headers cross-fade. Below
 * the small height the sheet slides off the screen.
 */
@Composable
internal fun BentoPhonePlayerSheet(
    context: ViewContext,
    data: BentoPlayerData,
    level: BentoPlayerLevel,
    stage: BentoPlayerStage,
    containerHeight: Dp,
    smallHeight: Dp,
    mediumHeight: Dp,
    coverSize: Dp,
    onLevelChange: (BentoPlayerLevel) -> Unit,
    onStageChange: (BentoPlayerStage) -> Unit,
    onClose: () -> Unit,
    onScrimAlphaChange: (Float) -> Unit,
    onShowSpeed: () -> Unit,
    onShowPitch: () -> Unit,
    onShowSleepTimer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    // The sheet reaches under the gesture bar, so both heights add the bottom inset.
    val navigationBottom = bentoSystemBottomInset()
    val smallDp = (smallHeight + navigationBottom).value
    val mediumDp = (mediumHeight + navigationBottom).value.coerceAtMost(containerHeight.value)
        .coerceAtLeast(smallDp + 1f)
    val latestLevel by rememberUpdatedState(level)

    fun heightOf(target: BentoPlayerLevel) = when (target) {
        BentoPlayerLevel.SMALL -> smallDp
        BentoPlayerLevel.MEDIUM -> mediumDp
    }

    // Visible height in dp: the sheet's size, position and the content morph all read it.
    val visible = remember { Animatable(heightOf(level)) }
    // Set while a drag runs its own settle animation, so the level effect does not restart it.
    var settlingTo by remember { mutableStateOf<BentoPlayerLevel?>(null) }
    var dragging by remember { mutableStateOf(false) }

    LaunchedEffect(level, smallDp, mediumDp) {
        if (dragging || settlingTo == level) return@LaunchedEffect
        visible.animateTo(heightOf(level), LevelSpring)
    }

    fun progress() = ((visible.value - smallDp) / (mediumDp - smallDp)).coerceIn(0f, 1f)

    val velocityTracker = remember { VelocityTracker() }
    var dragStartLevel by remember { mutableStateOf(level) }
    var dragStartHeight by remember { mutableFloatStateOf(0f) }
    var dragTotalPx by remember { mutableFloatStateOf(0f) }

    fun release(velocityPxPerSecond: Float) {
        dragging = false
        // Positive velocity moves down the screen, which shrinks the visible height.
        val velocityDpPerMs = velocityPxPerSecond / density.density / 1000f
        val target = resolveBentoPhonePlayerRelease(
            currentTopDp = containerHeight.value - visible.value,
            velocityDpPerMs = velocityDpPerMs,
            startedAt = dragStartLevel,
            mediumTopDp = containerHeight.value - mediumDp,
            smallTopDp = containerHeight.value - smallDp,
            closedTopDp = containerHeight.value + 24f,
        )
        if (target == null) {
            onClose()
            return
        }
        onScrimAlphaChange(1f)
        settlingTo = target
        coroutineScope.launch {
            visible.animateTo(
                targetValue = heightOf(target),
                animationSpec = LevelSpring,
                initialVelocity = -velocityPxPerSecond / density.density,
            )
            settlingTo = null
        }
        if (target != latestLevel) onLevelChange(target)
    }

    val dragModifier = Modifier
        .pointerInput(Unit) {
            detectTapGestures {
                onLevelChange(
                    when (latestLevel) {
                        BentoPlayerLevel.SMALL -> BentoPlayerLevel.MEDIUM
                        BentoPlayerLevel.MEDIUM -> BentoPlayerLevel.SMALL
                    },
                )
            }
        }
        .pointerInput(Unit) {
            detectVerticalDragGestures(
                onDragStart = {
                    dragging = true
                    dragStartLevel = latestLevel
                    dragStartHeight = visible.value
                    dragTotalPx = 0f
                    velocityTracker.resetTracking()
                },
                onVerticalDrag = { change, dragAmount ->
                    change.consume()
                    dragTotalPx += dragAmount
                    // The header moves with the finger, so track the drag distance, not the local position.
                    velocityTracker.addPosition(change.uptimeMillis, Offset(0f, dragTotalPx))
                    val raw = dragStartHeight - dragTotalPx / density.density
                    // Above medium the sheet rubber-bands at 30 % (DESIGN.md 6.5).
                    val next = when {
                        raw > mediumDp -> mediumDp + (raw - mediumDp) * 0.3f
                        else -> raw.coerceAtLeast(0f)
                    }
                    coroutineScope.launch { visible.snapTo(next) }
                    // The scrim fades out between the small position and closed.
                    onScrimAlphaChange((next / smallDp).coerceIn(0f, 1f))
                },
                onDragEnd = { release(velocityTracker.calculateVelocity().y) },
                onDragCancel = { release(0f) },
            )
        }

    val shape = RoundedCornerShape(
        topStart = BentoRadii.PHONE_SHEET_TOP.dp,
        topEnd = BentoRadii.PHONE_SHEET_TOP.dp,
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .layout { measurable, constraints ->
                val visiblePx = visible.value.dp.roundToPx()
                val smallPx = smallDp.dp.roundToPx()
                val heightPx = max(visiblePx, smallPx)
                val placeable = measurable.measure(
                    constraints.copy(minHeight = heightPx, maxHeight = heightPx),
                )
                layout(placeable.width, heightPx) {
                    // Below the small height the whole sheet slides down instead of shrinking.
                    placeable.place(0, max(0, smallPx - visiblePx))
                }
            }
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = colors.phoneSheetShadow.color,
                spotColor = colors.phoneSheetShadow.color,
            )
            .clip(shape)
            .background(colors.surface)
            .pointerInput(Unit) {
                // Swallow taps that miss a control so they do not reach the page underneath.
                detectTapGestures(onTap = {})
            },
    ) {
        Box(modifier = Modifier.fillMaxWidth().then(dragModifier)) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp)
                    .size(36.dp, 4.dp)
                    .clip(CircleShape)
                    .background(colors.muted.copy(alpha = 0.7f)),
            )
            Row(
                modifier = Modifier.padding(start = 20.dp, end = 8.dp, top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CrossFadeHeader(
                    progress = ::progress,
                    modifier = Modifier.weight(1f),
                    small = {
                        PlayerSmallHeaderInfo(
                            context = context,
                            data = data,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
                        )
                    },
                    medium = { PlayerMediumHeaderInfo(context = context) },
                )
                PlayerCloseButton(onClose = onClose)
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = BentoSpacing.PLAYER_CONTENT_INSET.dp),
        ) {
            BentoPlayerStageView(
                context = context,
                data = data,
                stage = stage,
                onStageChange = onStageChange,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .growingSquare(coverSize, ::progress),
            )
            Box(modifier = Modifier.collapseVertically(::progress)) {
                Box(modifier = Modifier.padding(top = 14.dp)) {
                    BentoPlayerTitle(context, data)
                }
            }
        }
        Column(
            modifier = Modifier.padding(
                start = BentoSpacing.PLAYER_CONTENT_INSET.dp,
                end = BentoSpacing.PLAYER_CONTENT_INSET.dp,
                top = 12.dp,
                bottom = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            BentoPlayerSeek(context, data)
            BentoPlayerTransport(context, data, false, onShowSleepTimer)
            BentoPlayerExtras(
                context = context,
                data = data,
                stage = stage,
                compact = true,
                onStageChange = onStageChange,
                onShowSpeed = onShowSpeed,
                onShowPitch = onShowPitch,
                onShowSleepTimer = onShowSleepTimer,
                onClosePlayer = onClose,
            )
        }
        Spacer(modifier = Modifier.height(navigationBottom))
    }
}

/**
 * The small header (art, title, artist) and the medium header ("Playing
 * from") share one slot: its height follows the progress and each header
 * fades over half of it, so the two never show at full strength together.
 */
@Composable
private fun CrossFadeHeader(
    progress: () -> Float,
    small: @Composable () -> Unit,
    medium: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier.clipToBounds(),
        content = {
            Box { small() }
            Box { medium() }
        },
    ) { measurables, constraints ->
        val loose = constraints.copy(minHeight = 0)
        val smallPlaceable = measurables[0].measure(loose)
        val mediumPlaceable = measurables[1].measure(loose)
        val f = progress()
        val height = (smallPlaceable.height + (mediumPlaceable.height - smallPlaceable.height) * f)
            .roundToInt()
        layout(constraints.maxWidth, height) {
            smallPlaceable.placeWithLayer(0, 0) { alpha = (1f - f * 2f).coerceIn(0f, 1f) }
            mediumPlaceable.placeWithLayer(0, (height - mediumPlaceable.height) / 2) {
                alpha = ((f - 0.5f) * 2f).coerceIn(0f, 1f)
            }
        }
    }
}

/**
 * Sizes the child to a square of at most [maxSide] that fits the space it is
 * given, bottom-centred so the cover grows upward from the title, and fades it
 * with the progress.
 */
private fun Modifier.growingSquare(maxSide: Dp, progress: () -> Float) = layout { measurable, constraints ->
    val width = constraints.maxWidth
    val height = if (constraints.hasBoundedHeight) constraints.maxHeight else maxSide.roundToPx()
    val side = minOf(maxSide.roundToPx(), width, height).coerceAtLeast(0)
    val placeable = measurable.measure(Constraints.fixed(side, side))
    layout(width, height) {
        placeable.placeWithLayer((width - side) / 2, height - side) { alpha = progress() }
    }
}

/** Shows the child's height scaled by the progress, clipped, and fades it in. */
private fun Modifier.collapseVertically(progress: () -> Float) = this
    .clipToBounds()
    .layout { measurable, constraints ->
        val placeable = measurable.measure(constraints.copy(minHeight = 0))
        val f = progress()
        layout(placeable.width, (placeable.height * f).roundToInt()) {
            placeable.placeWithLayer(0, 0) { alpha = f }
        }
    }
