package io.github.zyrouge.symphony.ui.view.home

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.services.radio.RadioPlayer
import io.github.zyrouge.symphony.ui.components.BentoEqualizerGlyph
import io.github.zyrouge.symphony.ui.components.BentoSeekWave
import io.github.zyrouge.symphony.ui.components.BentoTile
import io.github.zyrouge.symphony.ui.components.BentoTileGeometry
import io.github.zyrouge.symphony.ui.components.BentoTileSizeClass
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.LocalBentoTileContentColors
import io.github.zyrouge.symphony.ui.components.bentoTilePaddingDp
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlin.math.min

/**
 * The Now playing tile (DESIGN.md 5.1): square art, an eyebrow with an
 * equalizer glyph, title, artist, seek bar and prev/play/next; on xl or tall
 * tiles the art fills the tile under a dark gradient with white text and the
 * eyebrow becomes a dark pill. Tapping the tile opens the player.
 */
@Composable
internal fun ForYouNowPlayingTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
    onOpenPlayer: () -> Unit,
    onNowPlayingTileVisibilityChanged: (Boolean) -> Unit,
) {
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val isPlaying by context.symphony.radio.observatory.isPlaying.collectAsState()
    val playbackPosition by context.symphony.radio.observatory.playbackPosition.collectAsState()
    val song by remember(queue, queueIndex) {
        derivedStateOf {
            queue.getOrNull(queueIndex)?.let { context.symphony.groove.song.get(it) }
        }
    }
    val fillsTile = geometry.sizeClass == BentoTileSizeClass.XL || geometry.height >= 260f
    val tall = geometry.height >= 200f
    val windowInfo = LocalWindowInfo.current

    DisposableEffect(Unit) {
        onDispose { onNowPlayingTileVisibilityChanged(false) }
    }

    BentoTile(
        modifier = Modifier.onGloballyPositioned { coordinates ->
            val bounds = coordinates.boundsInWindow()
            val visibleHeight = (
                bounds.bottom.coerceAtMost(windowInfo.containerSize.height.toFloat()) -
                    bounds.top.coerceAtLeast(0f)
                ).coerceIn(0f, bounds.height)
            onNowPlayingTileVisibilityChanged(visibleHeight / bounds.height >= 0.35f)
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
            ForYouNowPlayingBackdropContent(
                context = context,
                song = song,
                isPlaying = isPlaying,
                playbackPosition = playbackPosition,
                appFontFamily = appFontFamily,
            )
        } else {
            val padding = bentoTilePaddingDp(geometry.sizeClass)
            val artSize = min(
                geometry.height - 2f * padding.value,
                0.45f * (geometry.width - 2f * padding.value),
            )
            Row(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    song?.createArtworkImageRequest(context.symphony)?.build()
                        ?: Assets.createPlaceholderImageRequest(context.symphony).build(),
                    null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
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
                            context.symphony.t.NowPlaying,
                            style = BentoTypography.label,
                            color = LocalBentoTileContentColors.current.sub,
                            maxLines = 1,
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        song?.title ?: context.symphony.t.NothingIsBeingPlayedRightNow,
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
                        context = context,
                        position = playbackPosition,
                        playing = isPlaying,
                        height = 24.dp,
                    )
                    ForYouTransportControls(
                        context = context,
                        isPlaying = isPlaying,
                        playButtonSize = 44.dp,
                        playIconSize = 20,
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
private fun ForYouNowPlayingBackdropContent(
    context: ViewContext,
    song: Song?,
    isPlaying: Boolean,
    playbackPosition: RadioPlayer.PlaybackPosition,
    appFontFamily: FontFamily?,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            song?.createArtworkImageRequest(context.symphony)?.build()
                ?: Assets.createPlaceholderImageRequest(context.symphony).build(),
            null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
        )
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
                context.symphony.t.NowPlaying,
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
                song?.title ?: context.symphony.t.NothingIsBeingPlayedRightNow,
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
                context = context,
                position = playbackPosition,
                playing = isPlaying,
                height = 24.dp,
                accent = Color.White,
                track = Color.White.copy(alpha = 0.32f),
            )
            ForYouTransportControls(
                context = context,
                isPlaying = isPlaying,
                playButtonSize = 56.dp,
                playIconSize = 24,
                accent = Color.White,
                onAccent = Color.Black,
                overlay = Color.White,
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
private fun ForYouTransportControls(
    context: ViewContext,
    isPlaying: Boolean,
    playButtonSize: Dp,
    playIconSize: Int,
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
        BentoCircleTransportButton(
            icon = Icons.Filled.SkipPrevious,
            contentDescription = context.symphony.t.Previous,
            overlay = overlay,
            onClick = { context.symphony.radio.shorty.previous() },
        )
        Box(
            modifier = Modifier
                .size(playButtonSize)
                .clip(RoundedCornerShape(radius))
                .background(accent)
                .clickable { context.symphony.radio.shorty.playPause() },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                when {
                    isPlaying -> Icons.Filled.Pause
                    else -> Icons.Filled.PlayArrow
                },
                when {
                    isPlaying -> context.symphony.t.Pause
                    else -> context.symphony.t.Play
                },
                tint = onAccent,
                modifier = Modifier.size(playIconSize.dp),
            )
        }
        BentoCircleTransportButton(
            icon = Icons.Filled.SkipNext,
            contentDescription = context.symphony.t.Next,
            overlay = overlay,
            onClick = { context.symphony.radio.shorty.skip() },
        )
    }
}

@Composable
private fun BentoCircleTransportButton(
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
