package io.github.zyrouge.symphony.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.model.UiPlaybackPosition
import io.github.zyrouge.symphony.ui.model.UiSong
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlin.math.min

/** Labels of the Now playing tile; the platform supplies translations. */
data class ForYouNowPlayingLabels(
    val nowPlaying: String,
    val nothingPlaying: String,
    val previous: String,
    val play: String,
    val pause: String,
    val next: String,
)

/**
 * The share of a tile's full height that is on screen. [clippedTop] and
 * [clippedBottom] are its window bounds, already clipped by scrolling
 * ancestors; dividing by their height instead of [fullHeight] would give
 * about 1 whenever any pixel shows.
 */
fun forYouTileVisibleFraction(
    clippedTop: Float,
    clippedBottom: Float,
    windowHeight: Float,
    fullHeight: Float,
): Float {
    if (fullHeight <= 0f) return 0f
    val visibleHeight = (clippedBottom.coerceAtMost(windowHeight) - clippedTop.coerceAtLeast(0f))
        .coerceIn(0f, fullHeight)
    return visibleHeight / fullHeight
}

/**
 * The Now playing tile (DESIGN.md 5.1): square art, an eyebrow with an
 * equalizer glyph, title, artist, seek bar and prev/play/next; on xl or tall
 * tiles the art fills the tile under a dark gradient with white text and the
 * eyebrow becomes a dark pill. Tapping the tile opens the player. The platform
 * supplies the artwork (or its placeholder) and the playback callbacks.
 */
@Composable
fun ForYouNowPlayingTile(
    geometry: BentoTileGeometry,
    song: UiSong?,
    isPlaying: Boolean,
    position: UiPlaybackPosition,
    seekable: Boolean,
    labels: ForYouNowPlayingLabels,
    artwork: @Composable (Modifier) -> Unit,
    timeText: (Long) -> String,
    onOpenPlayer: () -> Unit,
    onSeek: (Long) -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onVisibilityChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val fillsTile = geometry.sizeClass == BentoTileSizeClass.XL || geometry.height >= 260f
    val tall = geometry.height >= 200f
    val windowInfo = LocalWindowInfo.current

    DisposableEffect(Unit) {
        onDispose { onVisibilityChanged(false) }
    }

    BentoTile(
        modifier = modifier.onGloballyPositioned { coordinates ->
            val bounds = coordinates.boundsInWindow()
            val visibleFraction = forYouTileVisibleFraction(
                clippedTop = bounds.top,
                clippedBottom = bounds.bottom,
                windowHeight = windowInfo.containerSize.height.toFloat(),
                fullHeight = coordinates.size.height.toFloat(),
            )
            onVisibilityChanged(visibleFraction >= 0.35f)
        },
        tone = BentoTileTone.RAISED,
        sizeClass = geometry.sizeClass,
        onClick = onOpenPlayer,
        contentPadding = when {
            fillsTile -> 0.dp
            else -> null
        },
    ) {
        if (fillsTile) {
            NowPlayingBackdropContent(
                song = song,
                isPlaying = isPlaying,
                position = position,
                seekable = seekable,
                labels = labels,
                artwork = artwork,
                timeText = timeText,
                appFontFamily = appFontFamily,
                onSeek = onSeek,
                onPrevious = onPrevious,
                onPlayPause = onPlayPause,
                onNext = onNext,
            )
        } else {
            val padding = bentoTilePaddingDp(geometry.sizeClass)
            val artSize = min(
                geometry.height - 2f * padding.value,
                0.45f * (geometry.width - 2f * padding.value),
            )
            Row(modifier = Modifier.fillMaxSize()) {
                artwork(
                    Modifier
                        .size(artSize.dp)
                        .align(Alignment.CenterVertically)
                        .clip(RoundedCornerShape(18.dp)),
                )
                Spacer(Modifier.width(14.dp))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        BentoEqualizerGlyph(
                            color = LocalBentoTileContentColors.current.sub,
                            playing = isPlaying,
                        )
                        Text(
                            labels.nowPlaying,
                            style = BentoTypography.label,
                            color = LocalBentoTileContentColors.current.sub,
                            maxLines = 1,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        song?.title ?: labels.nothingPlaying,
                        style = BentoTypography.title.copy(fontFamily = appFontFamily),
                        color = LocalBentoTileContentColors.current.fg,
                        maxLines = if (tall) 2 else 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    song?.artists?.takeIf { it.isNotEmpty() }?.let {
                        Text(
                            it.joinToString(),
                            style = BentoTypography.body,
                            color = LocalBentoTileContentColors.current.sub,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    BentoSeekWave(
                        position = position,
                        playing = isPlaying,
                        height = 24.dp,
                        seekable = seekable,
                        timeText = timeText,
                        onSeek = onSeek,
                    )
                    NowPlayingTransportControls(
                        isPlaying = isPlaying,
                        labels = labels,
                        playButtonSize = 44.dp,
                        playIconSize = 20,
                        onPrevious = onPrevious,
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                    )
                }
            }
        }
    }
}

/**
 * Now playing content for tiles whose art fills the tile: artwork under a
 * dark gradient, white text, a dark eyebrow pill in the top-left corner.
 */
@Composable
private fun NowPlayingBackdropContent(
    song: UiSong?,
    isPlaying: Boolean,
    position: UiPlaybackPosition,
    seekable: Boolean,
    labels: ForYouNowPlayingLabels,
    artwork: @Composable (Modifier) -> Unit,
    timeText: (Long) -> String,
    appFontFamily: FontFamily?,
    onSeek: (Long) -> Unit,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        artwork(Modifier.matchParentSize())
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0f to Color.Black.copy(alpha = 0f),
                            0.15f to Color.Black.copy(alpha = 0f),
                            0.45f to Color.Black.copy(alpha = 0.3f),
                            1f to Color.Black.copy(alpha = 0.8f),
                        ),
                    ),
                ),
        )
        Row(
            modifier = Modifier
                .padding(start = 14.dp, top = 14.dp)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(start = 10.dp, end = 12.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            BentoEqualizerGlyph(color = Color.White, playing = isPlaying)
            Text(
                labels.nowPlaying,
                style = BentoTypography.label,
                color = Color.White,
                maxLines = 1,
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                song?.title ?: labels.nothingPlaying,
                style = BentoTypography.display.copy(fontFamily = appFontFamily),
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            song?.artists?.takeIf { it.isNotEmpty() }?.let {
                Text(
                    it.joinToString(),
                    style = BentoTypography.rowTitle,
                    color = Color.White.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BentoSeekWave(
                position = position,
                playing = isPlaying,
                height = 24.dp,
                seekable = seekable,
                timeText = timeText,
                onSeek = onSeek,
                accent = Color.White,
                track = Color.White.copy(alpha = 0.32f),
            )
            NowPlayingTransportControls(
                isPlaying = isPlaying,
                labels = labels,
                playButtonSize = 56.dp,
                playIconSize = 24,
                accent = Color.White,
                onAccent = Color.Black,
                overlay = Color.White,
                onPrevious = onPrevious,
                onPlayPause = onPlayPause,
                onNext = onNext,
            )
        }
    }
}

/**
 * Prev / play / next controls of the Now playing tile (DESIGN.md 4.3): 40 dp
 * circles around a play button that is a rounded square while paused and a
 * circle while playing.
 */
@Composable
private fun NowPlayingTransportControls(
    isPlaying: Boolean,
    labels: ForYouNowPlayingLabels,
    playButtonSize: Dp,
    playIconSize: Int,
    onPrevious: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    accent: Color = LocalBentoColors.current.accent,
    onAccent: Color = LocalBentoColors.current.onAccent,
    overlay: Color = LocalBentoTileContentColors.current.fg,
) {
    val radius by animateDpAsState(
        targetValue = playButtonSize * when {
            isPlaying -> 0.5f
            else -> BentoRadii.PLAY_BUTTON_PAUSED_RATIO
        },
        label = "bento-np-play-radius",
    )
    Row(
        modifier = Modifier.offset(x = (-8).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        CircleTransportButton(
            icon = Icons.Filled.SkipPrevious,
            contentDescription = labels.previous,
            overlay = overlay,
            onClick = onPrevious,
        )
        Box(
            modifier = Modifier
                .size(playButtonSize)
                .clip(RoundedCornerShape(radius))
                .background(accent)
                .clickable(onClick = onPlayPause),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                when {
                    isPlaying -> Icons.Filled.Pause
                    else -> Icons.Filled.PlayArrow
                },
                when {
                    isPlaying -> labels.pause
                    else -> labels.play
                },
                tint = onAccent,
                modifier = Modifier.size(playIconSize.dp),
            )
        }
        CircleTransportButton(
            icon = Icons.Filled.SkipNext,
            contentDescription = labels.next,
            overlay = overlay,
            onClick = onNext,
        )
    }
}

@Composable
private fun CircleTransportButton(
    icon: ImageVector,
    contentDescription: String,
    overlay: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(overlay.copy(alpha = 0.1f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription,
            modifier = Modifier.size(24.dp),
            tint = overlay,
        )
    }
}
