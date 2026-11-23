package io.github.zyrouge.symphony.ui.view.player

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.services.radio.RadioPlayer
import io.github.zyrouge.symphony.services.radio.RadioQueue
import io.github.zyrouge.symphony.ui.components.bentoSystemBottomInset
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoMotion
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/** Everything the bento capsule and panel read from the existing radio. */
data class BentoPlayerData(
    val song: Song,
    val isPlaying: Boolean,
    val playbackPosition: RadioPlayer.PlaybackPosition,
    val currentSongIndex: Int,
    val queueSize: Int,
    val currentLoopMode: RadioQueue.LoopMode,
    val currentShuffleMode: Boolean,
    val currentSpeed: Float,
    val currentPitch: Float,
    val persistedSpeed: Float,
    val persistedPitch: Float,
    val sleepTimer: Radio.SleepTimer?,
    val pauseOnCurrentSongEnd: Boolean,
)

enum class BentoPlayerStage {
    COVER,
    LYRICS,
    QUEUE,
}

@Composable
fun BentoPlayerObserver(
    context: ViewContext,
    content: @Composable (BentoPlayerData?) -> Unit,
) {
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val song by remember(queue, queueIndex) {
        derivedStateOf {
            queue.getOrNull(queueIndex)?.let { context.symphony.groove.song.get(it) }
        }
    }
    val isPlaying by context.symphony.radio.observatory.isPlaying.collectAsState()
    val playbackPosition by context.symphony.radio.observatory.playbackPosition.collectAsState()
    val currentLoopMode by context.symphony.radio.observatory.loopMode.collectAsState()
    val currentShuffleMode by context.symphony.radio.observatory.shuffleMode.collectAsState()
    val currentSpeed by context.symphony.radio.observatory.speed.collectAsState()
    val currentPitch by context.symphony.radio.observatory.pitch.collectAsState()
    val persistedSpeed by context.symphony.radio.observatory.persistedSpeed.collectAsState()
    val persistedPitch by context.symphony.radio.observatory.persistedPitch.collectAsState()
    val sleepTimer by context.symphony.radio.observatory.sleepTimer.collectAsState()
    val pauseOnCurrentSongEnd by context.symphony.radio.observatory.pauseOnCurrentSongEnd.collectAsState()

    content(
        song?.let {
            BentoPlayerData(
                song = it,
                isPlaying = isPlaying,
                playbackPosition = playbackPosition,
                currentSongIndex = queueIndex,
                queueSize = queue.size,
                currentLoopMode = currentLoopMode,
                currentShuffleMode = currentShuffleMode,
                currentSpeed = currentSpeed,
                currentPitch = currentPitch,
                persistedSpeed = persistedSpeed,
                persistedPitch = persistedPitch,
                sleepTimer = sleepTimer,
                pauseOnCurrentSongEnd = pauseOnCurrentSongEnd,
            )
        },
    )
}

/**
 * Hosts the never-fullscreen player above the Home main column. Compact
 * windows get a scrim and bottom sheet; every other class gets a floating
 * window. The capsule is suppressed while the For you tile is visible.
 */
@Composable
fun BentoPlayerHost(
    context: ViewContext,
    windowClass: BentoWindowClass,
    open: Boolean,
    capsuleSuppressed: Boolean,
    hasRail: Boolean,
    onOpenChange: (Boolean) -> Unit,
) {
    val colors = LocalBentoColors.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val compact = windowClass == BentoWindowClass.COMPACT
    val short = windowClass == BentoWindowClass.SHORT_HEIGHT
    val touch = configuration.touchscreen == Configuration.TOUCHSCREEN_FINGER
    val canResize = compact || touch
    val navigationWidth = when {
        // Pages without a rail (detail pages, search, settings) centre the capsule in the window.
        !hasRail -> 0
        else -> when (windowClass) {
        BentoWindowClass.COMPACT -> 0
        BentoWindowClass.MEDIUM, BentoWindowClass.EXPANDED, BentoWindowClass.LARGE ->
            BentoSizes.RAIL_WIDTH

        BentoWindowClass.EXTRA_LARGE -> BentoSizes.RAIL_WIDE
        BentoWindowClass.SHORT_HEIGHT -> BentoSizes.RAIL_SHORT
        }
    }
    val capsuleHiddenOffset = with(density) { 28.dp.roundToPx() }
    val floatingEnterOffset = with(density) { 24.dp.roundToPx() }

    BentoPlayerObserver(context) { data ->
        // Pointer devices open straight at medium (DESIGN.md 6.2).
        val initialLevel = when {
            !compact && !touch -> BentoPlayerLevel.MEDIUM
            else -> BentoPlayerLevel.SMALL
        }
        var level by remember(compact, touch) { mutableStateOf(initialLevel) }
        var stage by remember { mutableStateOf(BentoPlayerStage.COVER) }
        var dragScrimAlpha by remember { mutableFloatStateOf(1f) }
        // Reset when the player opens, not when it closes, so the exit animation
        // keeps showing the level and stage the person closed it from.
        var wasOpen by remember { mutableStateOf(open) }
        if (open != wasOpen) {
            if (open) {
                level = initialLevel
                stage = BentoPlayerStage.COVER
                dragScrimAlpha = 1f
            }
            wasOpen = open
        }
        var previousSleepTimer by remember { mutableStateOf<Radio.SleepTimer?>(null) }

        LaunchedEffect(data == null, open) {
            if (open && data == null) {
                onOpenChange(false)
            }
        }
        LaunchedEffect(data?.sleepTimer) {
            val previous = previousSleepTimer
            if (
                previous != null && data?.sleepTimer == null &&
                System.currentTimeMillis() >= previous.endsAt
            ) {
                Toast.makeText(
                    context.activity,
                    context.symphony.t.SleepTimerEnded,
                    Toast.LENGTH_SHORT,
                ).show()
            }
            previousSleepTimer = data?.sleepTimer
        }

        BackHandler(enabled = open && data != null) {
            onOpenChange(false)
        }

        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedVisibility(
                visible = open && compact && data != null,
                modifier = Modifier.fillMaxSize(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.scrim.copy(alpha = colors.scrim.alpha * dragScrimAlpha))
                        .clickable { onOpenChange(false) },
                )
            }

            AnimatedVisibility(
                visible = open && data != null,
                modifier = Modifier.fillMaxSize(),
                enter = when {
                    compact -> slideInVertically(
                        animationSpec = tween(
                            BentoMotion.PHONE_SHEET_OPEN.durationMs,
                            easing = BentoMotion.PHONE_SHEET_OPEN.easing!!,
                        ),
                        initialOffsetY = { it },
                    ) + fadeIn()

                    else -> slideInVertically(
                        animationSpec = tween(
                            BentoMotion.FLOATING_PLAYER.durationMs,
                            easing = BentoMotion.FLOATING_PLAYER.easing!!,
                        ),
                        initialOffsetY = { floatingEnterOffset },
                    ) + scaleIn(
                        initialScale = 0.92f,
                        transformOrigin = TransformOrigin(1f, 1f),
                        animationSpec = tween(
                            BentoMotion.FLOATING_PLAYER.durationMs,
                            easing = BentoMotion.FLOATING_PLAYER.easing!!,
                        ),
                    ) + fadeIn()
                },
                exit = when {
                    compact -> slideOutVertically(
                        animationSpec = tween(
                            BentoMotion.PHONE_SHEET_CLOSE.durationMs,
                            easing = BentoMotion.PHONE_SHEET_CLOSE.easing!!,
                        ),
                        targetOffsetY = { it },
                    ) + fadeOut()

                    else -> slideOutVertically(targetOffsetY = { floatingEnterOffset }) +
                        scaleOut(targetScale = 0.92f, transformOrigin = TransformOrigin(1f, 1f)) +
                        fadeOut()
                },
            ) {
                data?.let {
                    BentoPlayerPanel(
                        context = context,
                        data = it,
                        level = level,
                        stage = stage,
                        compact = compact,
                        short = short,
                        canResize = canResize,
                        onLevelChange = { target -> level = target },
                        onStageChange = { target ->
                            stage = target
                            if (target != BentoPlayerStage.COVER && level == BentoPlayerLevel.SMALL) {
                                level = BentoPlayerLevel.MEDIUM
                            }
                        },
                        onClose = { onOpenChange(false) },
                        onScrimAlphaChange = { alpha -> dragScrimAlpha = alpha },
                    )
                }
            }

            val capsuleVisible = data != null && !open && !capsuleSuppressed

            AnimatedVisibility(
                visible = capsuleVisible,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    // Same bottom inset as scroll content, so the capsule clears the gesture area
                    // on MIUI/HyperOS versions that report no navigation bar.
                    .padding(bottom = bentoSystemBottomInset())
                    .padding(
                        start = (16 + navigationWidth).dp,
                        end = 16.dp,
                        bottom = when {
                            short -> BentoSizes.CAPSULE_BOTTOM_INSET_SHORT.dp
                            compact -> BentoSizes.CAPSULE_BOTTOM_INSET_PHONE.dp
                            else -> BentoSizes.CAPSULE_BOTTOM_INSET_OTHER.dp
                        },
                    ),
                enter = slideInVertically(
                    animationSpec = tween(
                        BentoMotion.CAPSULE_SHOW_HIDE.durationMs,
                        easing = BentoMotion.CAPSULE_SHOW_HIDE.easing!!,
                    ),
                    initialOffsetY = { capsuleHiddenOffset },
                ) + scaleIn(initialScale = 0.94f) + fadeIn(),
                exit = slideOutVertically(
                    animationSpec = tween(
                        BentoMotion.CAPSULE_SHOW_HIDE.durationMs,
                        easing = BentoMotion.CAPSULE_SHOW_HIDE.easing!!,
                    ),
                    targetOffsetY = { capsuleHiddenOffset },
                ) + scaleOut(targetScale = 0.94f) + fadeOut(),
            ) {
                data?.let {
                    BentoPlayerCapsule(
                        context = context,
                        data = it,
                        short = short,
                        showPrevious = !compact && !short,
                        onOpen = { onOpenChange(true) },
                    )
                }
            }
        }
    }
}
