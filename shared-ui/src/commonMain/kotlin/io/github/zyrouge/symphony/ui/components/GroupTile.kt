package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
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
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.model.UiSong
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/** The number of rows a grouped tile shows before "Show all" takes the last one. */
const val BENTO_GROUP_MAX_ROWS = 6

/**
 * A grouped tile (DESIGN.md 5.3): a 64 dp cover, the title and subtitle, a
 * play button, a more button, up to six song rows and a "Show all" row.
 * Artwork and the menu stay with the platform.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoGroupTile(
    title: String,
    subtitle: String,
    songs: List<UiSong>,
    cover: @Composable () -> Unit,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    onPlayAt: (index: Int) -> Unit,
    durationText: (UiSong) -> String,
    artwork: @Composable (UiSong, Modifier) -> Unit,
    moreContentDescription: String,
    playingSongId: String?,
    playing: Boolean,
    narrow: Boolean,
    showAllLabel: String?,
    onShowAll: (() -> Unit)?,
    menu: @Composable (expanded: Boolean, onDismissRequest: () -> Unit) -> Unit,
    trackNumberOf: ((index: Int, song: UiSong) -> Int?)? = null,
    rowSecondaryOf: ((song: UiSong) -> String?)? = null,
) {
    val colors = LocalBentoColors.current
    val colorScheme = LocalBentoColorScheme.current
    val tileShape = RoundedCornerShape(BentoRadii.TILE.dp)
    val openInteraction = remember { MutableInteractionSource() }
    val openHovered by openInteraction.collectIsHoveredAsState()
    var menuExpanded by remember { mutableStateOf(false) }
    val visibleRows = songs.take(
        when {
            songs.size > BENTO_GROUP_MAX_ROWS -> BENTO_GROUP_MAX_ROWS - 1
            else -> BENTO_GROUP_MAX_ROWS
        },
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(tileShape)
            .then(
                when (colorScheme) {
                    BentoColorScheme.CONTRAST -> Modifier.border(1.dp, colors.line, tileShape)
                    else -> Modifier
                },
            )
            .background(colors.surface)
            .padding(start = 8.dp, end = 8.dp, top = 16.dp, bottom = 8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .onBentoSecondaryClick { menuExpanded = true }
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .hoverable(openInteraction)
                    .combinedClickable(
                        interactionSource = openInteraction,
                        indication = null,
                        onClick = onOpen,
                        onLongClick = { menuExpanded = true },
                    )
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                cover()
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        title,
                        style = BentoTypography.groupTitle,
                        color = when {
                            openHovered -> colors.accent
                            else -> colors.fg
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        subtitle,
                        style = BentoTypography.secondary,
                        color = colors.sub,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(colors.accent)
                    .combinedClickable(onClick = onPlay),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Filled.PlayArrow,
                    title,
                    tint = colors.onAccent,
                    modifier = Modifier.size(20.dp),
                )
            }
            Box {
                BentoMoreButton(
                    contentDescription = moreContentDescription,
                    onClick = { menuExpanded = true },
                )
                menu(menuExpanded) { menuExpanded = false }
            }
        }
        Spacer(Modifier.height(8.dp))
        visibleRows.forEachIndexed { index, song ->
            BentoGroupTileRow(
                song = song,
                trackNumber = trackNumberOf?.invoke(index, song),
                secondary = rowSecondaryOf?.invoke(song),
                durationText = durationText(song),
                artwork = { modifier -> artwork(song, modifier) },
                playing = song.id == playingSongId,
                isPlaying = playing,
                narrow = narrow,
                onClick = { onPlayAt(index) },
            )
        }
        if (songs.size > BENTO_GROUP_MAX_ROWS && showAllLabel != null && onShowAll != null) {
            BentoGroupShowAllRow(
                label = showAllLabel,
                onClick = onShowAll,
            )
        }
    }
}

/**
 * Calls [onClick] on a secondary (right) mouse press and consumes it, so the
 * children's click handlers do not see it. [onClick] is captured once, so it
 * should only flip remembered state.
 */
private fun Modifier.onBentoSecondaryClick(onClick: () -> Unit): Modifier =
    pointerInput(Unit) {
        awaitEachGesture {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                event.changes.forEach { it.consume() }
                onClick()
            }
        }
    }

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BentoGroupTileRow(
    song: UiSong,
    trackNumber: Int?,
    secondary: String?,
    durationText: String,
    artwork: @Composable (Modifier) -> Unit,
    playing: Boolean,
    isPlaying: Boolean,
    narrow: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = BentoSizes.GROUP_ROW.dp)
            .hoverable(interactionSource)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
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
        when (trackNumber) {
            null -> Box {
                artwork(
                    Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(BentoRadii.GROUP_ROW_ART_30.dp)),
                )
                if (playing) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(BentoRadii.GROUP_ROW_ART_30.dp))
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        BentoEqualizerGlyph(color = Color.White, playing = isPlaying)
                    }
                }
            }

            else -> Text(
                trackNumber.toString(),
                style = bentoTabularStyle(BentoTypography.secondary),
                color = when {
                    playing -> colors.accent
                    else -> colors.muted
                },
                maxLines = 1,
                modifier = Modifier.width(22.dp),
                textAlign = TextAlign.Center,
            )
        }
        // At a large scale the duration gives way to the title.
        BentoTextWithMeta(
            modifier = Modifier.weight(1f),
            gap = 12.dp,
            em = BentoTypography.rowTitle.fontSize,
            meta = {
                Text(
                    durationText,
                    style = bentoTabularStyle(BentoTypography.secondary),
                    color = colors.muted,
                    maxLines = 1,
                )
            },
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    song.title,
                    style = BentoTypography.rowTitle.copy(fontWeight = FontWeight.Medium),
                    color = when {
                        playing -> colors.accent
                        else -> colors.fg
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (!narrow) {
                    secondary?.let {
                        Text(
                            it,
                            style = BentoTypography.secondary,
                            color = colors.muted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(0.55f, fill = false),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BentoGroupShowAllRow(
    label: String,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = BentoSizes.GROUP_ROW.dp)
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .combinedClickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            label,
            style = BentoTypography.rowTitle,
            color = colors.accent,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.Rounded.ChevronRight,
            label,
            tint = colors.accent,
            modifier = Modifier.size(18.dp),
        )
    }
}
