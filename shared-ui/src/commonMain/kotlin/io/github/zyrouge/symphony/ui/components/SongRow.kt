package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.model.UiSong
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * A song row (DESIGN.md 4.4): the platform supplies the 44 dp artwork through
 * [artwork]; the shared layout keeps the title, metadata, duration, menu and
 * playing indicator in sync across Android and desktop.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongRow(
    song: UiSong,
    playing: Boolean,
    isPlaying: Boolean,
    durationText: String,
    moreContentDescription: String,
    artwork: @Composable (Modifier) -> Unit,
    columns: Boolean = false,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    menu: (@Composable (expanded: Boolean, onDismissRequest: () -> Unit) -> Unit)? = null,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val durationStyle = bentoTabularStyle(BentoTypography.secondary)
    val artistsLine = song.artists.joinToString()
    var menuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                when {
                    !columns -> Modifier.heightIn(min = BentoSizes.SONG_ROW.dp)
                    else -> Modifier.heightIn(min = BentoSizes.SONG_ROW_COLUMNS.dp)
                },
            )
            .hoverable(interactionSource)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongClick ?: menu?.let { { menuExpanded = true } },
            )
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .background(
                when {
                    hovered || pressed -> colors.raised
                    else -> Color.Transparent
                },
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box {
            artwork(
                Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(BentoRadii.SONG_ART_44.dp)),
            )
            if (playing) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(BentoRadii.SONG_ART_44.dp))
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center,
                ) {
                    BentoEqualizerGlyph(color = Color.White, playing = isPlaying)
                }
            }
        }
        if (columns) {
            Text(
                song.title,
                style = BentoTypography.rowTitle,
                color = when {
                    playing -> colors.accent
                    else -> colors.fg
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(2f),
            )
            Text(
                artistsLine,
                style = BentoTypography.body,
                color = colors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1.3f),
            )
            Text(
                song.album ?: "",
                style = BentoTypography.body,
                color = colors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1.5f),
            )
            Text(
                durationText,
                style = durationStyle,
                color = colors.muted,
                maxLines = 1,
                textAlign = TextAlign.End,
                modifier = Modifier.width(52.dp),
            )
        } else {
            BentoTextWithMeta(
                modifier = Modifier.weight(1f),
                gap = 12.dp,
                em = BentoTypography.rowTitle.fontSize,
                meta = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        trailing?.invoke(this)
                        Text(
                            durationText,
                            style = durationStyle,
                            color = colors.muted,
                            maxLines = 1,
                            textAlign = TextAlign.End,
                        )
                    }
                },
            ) {
                Column {
                    Text(
                        song.title,
                        style = BentoTypography.rowTitle,
                        color = when {
                            playing -> colors.accent
                            else -> colors.fg
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (artistsLine.isNotEmpty() || !song.album.isNullOrEmpty()) {
                        Text(
                            listOfNotNull(
                                artistsLine.takeIf { it.isNotEmpty() },
                                song.album,
                            ).joinToString(" · "),
                            style = BentoTypography.secondary,
                            color = colors.sub,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        menu?.let {
            Box {
                BentoMoreButton(
                    contentDescription = moreContentDescription,
                    onClick = { menuExpanded = true },
                )
                it(menuExpanded) { menuExpanded = false }
            }
        }
    }
}
