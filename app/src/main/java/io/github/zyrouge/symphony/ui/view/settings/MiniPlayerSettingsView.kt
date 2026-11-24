package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.ui.components.BentoSheetButton
import io.github.zyrouge.symphony.ui.components.BentoSheetDialog
import io.github.zyrouge.symphony.ui.components.BentoSheetSwitchRow
import io.github.zyrouge.symphony.ui.components.rememberBentoPlayingSong
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass

/**
 * The Mini player settings (prototype v16 `mini`) in a sheet: a preview of
 * the capsule, then which buttons it shows and whether long titles scroll.
 * A sheet rather than a page, so wide windows do not stretch the rows.
 */
@Composable
fun MiniPlayerSettingsDialog(context: ViewContext, onDismissRequest: () -> Unit) {
    val windowClass = rememberBentoWindowClass()
    val settings = context.symphony.settings
    val trackControls by settings.miniPlayerTrackControls.flow.collectAsState()
    val seekControls by settings.miniPlayerSeekControls.flow.collectAsState()
    val marquee by settings.miniPlayerTextMarquee.flow.collectAsState()
    val seekBack by settings.seekBackDuration.flow.collectAsState()
    val seekForward by settings.seekForwardDuration.flow.collectAsState()
    // The capsule shows Previous only where it has room (see BentoPlayerHost).
    val wideCapsule = windowClass != BentoWindowClass.COMPACT &&
        windowClass != BentoWindowClass.SHORT_HEIGHT

    BentoSheetDialog(
        title = context.symphony.t.MiniPlayer,
        onDismissRequest = onDismissRequest,
        actions = {
            BentoSheetButton(
                label = context.symphony.t.Done,
                primary = true,
                onClick = onDismissRequest,
            )
        },
    ) {
        MiniPlayerPreview(
            context = context,
            showPrevious = trackControls && wideCapsule,
            showNext = trackControls,
            showSeek = seekControls,
        )
        BentoSheetSwitchRow(
            title = context.symphony.t.PreviousAndNext,
            subtitle = context.symphony.t.PreviousOnWideOnly,
            checked = trackControls,
            onCheckedChange = { settings.miniPlayerTrackControls.setValue(it) },
        )
        BentoSheetSwitchRow(
            title = context.symphony.t.BackAndForward,
            subtitle = context.symphony.t.JumpXBackYAhead(seekBack.toString(), seekForward.toString()),
            checked = seekControls,
            onCheckedChange = { settings.miniPlayerSeekControls.setValue(it) },
        )
        BentoSheetSwitchRow(
            title = context.symphony.t.ScrollLongTitles,
            checked = marquee,
            onCheckedChange = { settings.miniPlayerTextMarquee.setValue(it) },
        )
    }
}

/**
 * A still capsule (prototype `miniPreview`): the playing song, or a
 * placeholder, with the buttons the current settings turn on.
 */
@Composable
private fun MiniPlayerPreview(
    context: ViewContext,
    showPrevious: Boolean,
    showNext: Boolean,
    showSeek: Boolean,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val playing = rememberBentoPlayingSong(context)
    val song = playing.songId?.let { context.symphony.groove.song.get(it) }
    val shape = RoundedCornerShape(BentoRadii.CAPSULE.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 8.dp)
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .widthIn(max = BentoSizes.CAPSULE_MAX_WIDTH.dp)
                .fillMaxWidth()
                .height(BentoSizes.CAPSULE_HEIGHT.dp)
                .clip(shape)
                .background(colors.high)
                .border(1.dp, colors.edge, shape)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val artModifier = Modifier
                .size(BentoSizes.CAPSULE_ART.dp)
                .clip(CircleShape)
            if (song != null) {
                AsyncImage(
                    song.createArtworkImageRequest(context.symphony).build(),
                    null,
                    contentScale = ContentScale.Crop,
                    modifier = artModifier,
                )
            } else {
                Box(artModifier.background(colors.tonal))
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
            ) {
                Text(
                    song?.title ?: context.symphony.t.SongTitle,
                    style = BentoTypography.rowTitle.copy(fontFamily = appFontFamily),
                    color = colors.fg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    song?.artists?.joinToString()?.takeIf { it.isNotEmpty() } ?: context.symphony.t.Artist,
                    style = BentoTypography.secondary.copy(fontFamily = appFontFamily),
                    color = colors.sub,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(0.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (showPrevious) PreviewIcon(Icons.Filled.SkipPrevious)
                if (showSeek) PreviewIcon(Icons.Filled.FastRewind)
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(colors.accent),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.PlayArrow,
                        null,
                        tint = colors.onAccent,
                        modifier = Modifier.size(20.dp),
                    )
                }
                if (showSeek) PreviewIcon(Icons.Filled.FastForward)
                if (showNext) PreviewIcon(Icons.Filled.SkipNext)
            }
        }
    }
}

@Composable
private fun PreviewIcon(icon: ImageVector) {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier.size(BentoSizes.TOUCH_TARGET_MIN.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, null, tint = colors.sub)
    }
}
