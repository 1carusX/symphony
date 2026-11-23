package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
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
    val colors = LocalBentoColors.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val dragOffset = remember { Animatable(0f) }
    var dragStartedAt by remember { mutableStateOf(level) }
    var dragStartedAtMs by remember { mutableLongStateOf(0L) }
    var dragDistanceDp by remember { mutableFloatStateOf(0f) }
    var dragMoved by remember { mutableStateOf(false) }
    var showSpeedDialog by remember { mutableStateOf(false) }
    var showPitchDialog by remember { mutableStateOf(false) }
    var showSleepTimerDialog by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val topInset = max(
            BentoSizes.PHONE_SHEET_TOP_INSET_MIN.dp.value,
            maxHeight.value * BentoSizes.PHONE_SHEET_TOP_INSET_HEIGHT_RATIO,
        ).dp
        val floatingInset = when {
            short -> BentoSizes.FLOATING_PLAYER_LANDSCAPE_INSET.dp
            else -> BentoSizes.FLOATING_PLAYER_INSET.dp
        }
        val availableHeight = maxHeight - when {
            compact -> topInset
            else -> floatingInset * 2
        }
        val smallHeight = min(
            BentoSizes.PHONE_SHEET_SMALL_HEIGHT_MAX.dp.value,
            availableHeight.value,
        ).dp
        val mediumWidth = when {
            compact -> maxWidth
            short -> min(
                BentoSizes.FLOATING_PLAYER_LANDSCAPE_MEDIUM_WIDTH.dp.value,
                (maxWidth - BentoSizes.RAIL_SHORT.dp - floatingInset * 2).value,
            ).dp

            else -> BentoSizes.FLOATING_PLAYER_WIDTH.dp
        }
        val phoneCoverSize = min(
            (mediumWidth - BentoSpacing.PLAYER_CONTENT_INSET.dp * 2).value,
            (availableHeight.value - PHONE_MEDIUM_FIXED_CONTENT_DP).coerceAtLeast(160f),
        ).dp
        val phoneMediumHeight = min(
            availableHeight.value,
            phoneCoverSize.value + PHONE_MEDIUM_FIXED_CONTENT_DP,
        ).dp
        val currentHeight = when {
            !compact -> null
            level == BentoPlayerLevel.SMALL -> smallHeight
            else -> phoneMediumHeight
        }
        val mediumTopDp = maxHeight.value - phoneMediumHeight.value
        val smallTopDp = maxHeight.value - smallHeight.value
        val closedTopDp = maxHeight.value + 24f
        val baseTopDp = currentHeight?.let { maxHeight.value - it.value } ?: 0f

        fun currentOffsetDp() = dragOffset.value / density.density

        fun finishDrag() {
            if (!dragMoved) return
            val offsetDp = currentOffsetDp()
            val elapsedMs = (System.currentTimeMillis() - dragStartedAtMs).coerceAtLeast(1L)
            val target = when {
                compact -> resolveBentoPhonePlayerRelease(
                    currentTopDp = baseTopDp + offsetDp,
                    velocityDpPerMs = offsetDp / elapsedMs,
                    startedAt = dragStartedAt,
                    mediumTopDp = mediumTopDp,
                    smallTopDp = smallTopDp,
                    closedTopDp = closedTopDp,
                )

                else -> resolveBentoFloatingPlayerRelease(offsetDp, dragStartedAt)
            }
            if (target == null) {
                onClose()
                return
            }
            coroutineScope.launch {
                if (compact) {
                    val newHeight = when (target) {
                        BentoPlayerLevel.SMALL -> smallHeight
                        BentoPlayerLevel.MEDIUM -> phoneMediumHeight
                    }
                    val newBaseTop = maxHeight.value - newHeight.value
                    dragOffset.snapTo(with(density) { (baseTopDp + offsetDp - newBaseTop).dp.toPx() })
                }
                onLevelChange(target)
                onScrimAlphaChange(1f)
                dragOffset.animateTo(
                    0f,
                    animationSpec = tween(
                        BentoMotion.PHONE_SHEET_OPEN.durationMs,
                        easing = BentoMotion.DECELERATE,
                    ),
                )
            }
        }

        val shape = when {
            compact -> RoundedCornerShape(
                topStart = BentoRadii.PHONE_SHEET_TOP.dp,
                topEnd = BentoRadii.PHONE_SHEET_TOP.dp,
            )

            else -> RoundedCornerShape(BentoRadii.FLOATING_PLAYER.dp)
        }
        val panelModifier = Modifier
            .align(Alignment.BottomEnd)
            .then(
                when {
                    compact -> Modifier
                    else -> Modifier.padding(floatingInset)
                },
            )
            .then(
                when {
                    compact -> Modifier.fillMaxWidth()
                    else -> Modifier.width(
                        when {
                            short && level == BentoPlayerLevel.MEDIUM -> mediumWidth
                            else -> BentoSizes.FLOATING_PLAYER_WIDTH.dp
                        },
                    )
                },
            )
            .then(
                when {
                    compact || level == BentoPlayerLevel.MEDIUM -> Modifier.height(
                        when {
                            compact && level == BentoPlayerLevel.SMALL -> smallHeight
                            compact -> phoneMediumHeight
                            short -> availableHeight
                            else -> min(
                                availableHeight.value,
                                BentoSizes.FLOATING_PLAYER_MEDIUM_MAX_HEIGHT.dp.value,
                            ).dp
                        },
                    )

                    else -> Modifier.wrapContentHeight()
                },
            )
            .graphicsLayer { translationY = dragOffset.value }
            .shadow(
                elevation = when {
                    compact -> 6.dp
                    else -> colors.shadow.layers.first().offsetY.dp
                },
                shape = shape,
                ambientColor = when {
                    compact -> colors.phoneSheetShadow.color
                    else -> colors.shadow.layers.first().color
                },
                spotColor = when {
                    compact -> colors.phoneSheetShadow.color
                    else -> colors.shadow.layers.last().color
                },
            )
            .clip(shape)
            .then(
                when {
                    compact -> Modifier
                    else -> Modifier.border(1.dp, colors.edge, shape)
                },
            )
            .background(if (compact) colors.surface else colors.`float`)
            .pointerInput(Unit) {
                // Swallow taps that miss a control so they do not reach the page
                // scrolling underneath the floating panel.
                detectTapGestures(onTap = {})
            }

        Column(modifier = panelModifier) {
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
                        dragStartedAtMs = System.currentTimeMillis()
                        dragDistanceDp = 0f
                        dragMoved = false
                    },
                    onDrag = { amountPx ->
                        dragDistanceDp += amountPx / density.density
                        if (!dragMoved && abs(dragDistanceDp) < 6f) {
                            return@PlayerPanelHeader
                        }
                        dragMoved = true
                        val offsetDp = when {
                            compact -> {
                                val currentTop = bentoPhonePlayerDragTop(
                                    baseTopDp = baseTopDp,
                                    dragDistanceDp = dragDistanceDp,
                                    mediumTopDp = mediumTopDp,
                                )
                                val alpha = when {
                                    currentTop <= smallTopDp -> 1f
                                    else -> 1f - (currentTop - smallTopDp) /
                                        (closedTopDp - smallTopDp)
                                }
                                onScrimAlphaChange(alpha.coerceIn(0f, 1f))
                                currentTop - baseTopDp
                            }

                            else -> bentoFloatingPlayerDragOffset(dragDistanceDp)
                        }
                        coroutineScope.launch {
                            dragOffset.snapTo(with(density) { offsetDp.dp.toPx() })
                        }
                    },
                    onDragEnd = ::finishDrag,
                )
            }

            when (level) {
                BentoPlayerLevel.SMALL -> PlayerSmallContent(
                    context = context,
                    data = data,
                    stage = stage,
                    compact = compact,
                    short = short,
                    onStageChange = onStageChange,
                    onShowSpeed = { showSpeedDialog = true },
                    onShowPitch = { showPitchDialog = true },
                    onShowSleepTimer = { showSleepTimerDialog = true },
                    onClosePlayer = onClose,
                )

                BentoPlayerLevel.MEDIUM -> when {
                    short -> PlayerMediumLandscapeContent(
                        context = context,
                        data = data,
                        stage = stage,
                        compact = compact,
                        onStageChange = onStageChange,
                        onShowSpeed = { showSpeedDialog = true },
                        onShowPitch = { showPitchDialog = true },
                        onShowSleepTimer = { showSleepTimerDialog = true },
                        onClosePlayer = onClose,
                    )

                    else -> PlayerMediumContent(
                        context = context,
                        data = data,
                        stage = stage,
                        compact = compact,
                        phoneCoverSize = phoneCoverSize,
                        onStageChange = onStageChange,
                        onShowSpeed = { showSpeedDialog = true },
                        onShowPitch = { showPitchDialog = true },
                        onShowSleepTimer = { showSleepTimerDialog = true },
                        onClosePlayer = onClose,
                    )
                }
            }
        }
    }

    if (showSpeedDialog) {
        NowPlayingSpeedDialog(
            context = context,
            currentSpeed = data.currentSpeed,
            persistedSpeed = data.persistedSpeed,
            onDismissRequest = { showSpeedDialog = false },
        )
    }
    if (showPitchDialog) {
        NowPlayingPitchDialog(
            context = context,
            currentPitch = data.currentPitch,
            persistedPitch = data.persistedPitch,
            onDismissRequest = { showPitchDialog = false },
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
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
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
            BentoPlayerLevel.SMALL -> Row(
                modifier = Modifier
                    .weight(1f)
                    .then(dragModifier)
                    .padding(top = 12.dp, bottom = 4.dp),
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

            BentoPlayerLevel.MEDIUM -> Column(
                modifier = Modifier
                    .weight(1f)
                    .then(dragModifier)
                    .height(48.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    "Playing from",
                    style = BentoTypography.label,
                    color = colors.sub,
                    maxLines = 1,
                )
                Text(
                    context.symphony.t.NowPlaying,
                    style = BentoTypography.body.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                    color = colors.fg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
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
}
