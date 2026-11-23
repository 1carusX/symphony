package io.github.zyrouge.symphony.ui.view.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import io.github.zyrouge.symphony.ui.components.BentoPageHeader
import io.github.zyrouge.symphony.ui.components.BentoSettingGroup
import io.github.zyrouge.symphony.ui.components.BentoSettingSwitchRow
import io.github.zyrouge.symphony.ui.components.BentoSettingsColumns
import io.github.zyrouge.symphony.ui.components.bentoGestureBarPadding
import io.github.zyrouge.symphony.ui.components.rememberBentoPlayingSong
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.theme.bento.rememberBentoWindowClass
import kotlinx.serialization.Serializable

@Serializable
object MiniPlayerSettingsViewRoute

/**
 * The Mini player settings page (prototype v16 `mini`): a preview of the
 * capsule, then which buttons it shows and whether long titles scroll.
 */
@Composable
fun MiniPlayerSettingsView(context: ViewContext) {
    val windowClass = rememberBentoWindowClass()
    val colors = LocalBentoColors.current
    val settings = context.symphony.settings
    val trackControls by settings.miniPlayerTrackControls.flow.collectAsState()
    val seekControls by settings.miniPlayerSeekControls.flow.collectAsState()
    val marquee by settings.miniPlayerTextMarquee.flow.collectAsState()
    val seekBack by settings.seekBackDuration.flow.collectAsState()
    val seekForward by settings.seekForwardDuration.flow.collectAsState()
    // The capsule shows Previous only where it has room (see BentoPlayerHost).
    val wideCapsule = windowClass != BentoWindowClass.COMPACT &&
        windowClass != BentoWindowClass.SHORT_HEIGHT

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bg),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            BentoPageHeader(
                title = "Mini player",
                windowClass = windowClass,
                onBack = { context.navController.popBackStack() },
            )
            BentoSettingsColumns {
                BentoSettingGroup(title = "Preview") {
                    MiniPlayerPreview(
                        context = context,
                        showPrevious = trackControls && wideCapsule,
                        showNext = trackControls,
                        showSeek = seekControls,
                    )
                }
                BentoSettingGroup(title = "Buttons and text") {
                    BentoSettingSwitchRow(
                        label = "Previous and next",
                        sub = "Previous shows on wider windows only",
                        checked = trackControls,
                        onCheckedChange = { settings.miniPlayerTrackControls.setValue(it) },
                    )
                    BentoSettingSwitchRow(
                        label = "Back and forward",
                        sub = "Jump $seekBack s back or $seekForward s ahead",
                        checked = seekControls,
                        onCheckedChange = { settings.miniPlayerSeekControls.setValue(it) },
                    )
                    BentoSettingSwitchRow(
                        label = "Scroll long titles",
                        checked = marquee,
                        onCheckedChange = { settings.miniPlayerTextMarquee.setValue(it) },
                    )
                }
            }
            Box(Modifier.bentoGestureBarPadding())
        }
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
                    song?.title ?: "Song title",
                    style = BentoTypography.rowTitle.copy(fontFamily = appFontFamily),
                    color = colors.fg,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    song?.artists?.joinToString()?.takeIf { it.isNotEmpty() } ?: "Artist",
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
