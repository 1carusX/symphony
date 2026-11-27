package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.components.bentoFloatingSurface
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.roundToInt

private val LandscapeLevelSpring = spring<Float>(dampingRatio = 0.86f, stiffness = Spring.StiffnessMediumLow)

/** Drag callbacks handed to the platform header of the floating player window. */
data class FloatingPlayerDrag(
    val onDragStart: () -> Unit,
    val onDrag: (uptimeMillis: Long, amountPx: Float) -> Unit,
    val onDragEnd: () -> Unit,
)

/**
 * The floating player window of short (phone landscape) windows, where the
 * medium player is a wide two-column window. One progress value (0 small,
 * 1 medium) drives the window's width and height and a cross-fade of the two
 * layouts, anchored at the bottom-right corner; a drag on the header moves it
 * with the finger and a release settles it (DESIGN.md 6.5). Dragging on down
 * from the small window closes it. The platform supplies both layouts and the
 * header through the slots.
 */
@Composable
fun BoxWithConstraintsScope.FloatingPlayerWindow(
    level: BentoPlayerLevel,
    canResize: Boolean,
    onLevelChange: (BentoPlayerLevel) -> Unit,
    onClose: () -> Unit,
    header: @Composable (
        level: BentoPlayerLevel,
        drag: FloatingPlayerDrag,
        onToggleLevel: () -> Unit,
    ) -> Unit,
    small: @Composable ColumnScope.() -> Unit,
    medium: @Composable ColumnScope.() -> Unit,
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

    val drag = FloatingPlayerDrag(
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
    val onToggleLevel: () -> Unit = {
        onLevelChange(
            when (latestLevel) {
                BentoPlayerLevel.SMALL -> BentoPlayerLevel.MEDIUM
                BentoPlayerLevel.MEDIUM -> BentoPlayerLevel.SMALL
            },
        )
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
                header(BentoPlayerLevel.SMALL, drag, onToggleLevel)
                small()
            }
        }
        if (showMedium) {
            Column(
                modifier = Modifier
                    .wrapContentSize(Alignment.BottomEnd, unbounded = true)
                    .size(mediumWidth, mediumHeight)
                    .graphicsLayer { alpha = ((progress.value - 0.5f) * 2f).coerceIn(0f, 1f) },
            ) {
                header(BentoPlayerLevel.MEDIUM, drag, onToggleLevel)
                medium()
            }
        }
    }
}
