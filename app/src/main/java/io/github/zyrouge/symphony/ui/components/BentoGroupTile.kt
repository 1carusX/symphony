package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Folder
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.helpers.Assets
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.utils.DurationUtils

/**
 * The genre swatch palette (the previous Genre page colors, reused for the
 * gradient swatch covers).
 */
private object BentoGenreSwatch {
    val colors = listOf(
        0xFFEF4444,
        0xFFF97316,
        0xFFF59E0B,
        0xFF16A34A,
        0xFF06B6B4,
        0xFF8B5CF6,
        0xFFD946EF,
        0xFFF43F5E,
        0xFF6366F1,
        0xFFA855F7,
    ).map { Color(it) }

    fun colorAt(index: Int): Color {
        val bounded = ((index % colors.size) + colors.size) % colors.size
        return colors[bounded]
    }
}

internal fun Color.darkened(): Color = Color(red * 0.55f, green * 0.55f, blue * 0.55f, alpha)

/**
 * A grouped tile (DESIGN.md 5.3): a 64 dp cover, the group title and
 * subtitle, a 44 dp accent play button, a More button, and up to six song
 * rows; longer groups show five rows and a "Show all" row that opens the
 * group's page. The tile is as tall as its content.
 *
 * The More button, a long press on the cover or title, and a right click on
 * the header open [menu]. Without one, the menu offers Shuffle play, Play
 * next, Add to queue and Add to playlist for [songs].
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoGroupTile(
    context: ViewContext,
    title: String,
    subtitle: String,
    songs: List<Song>,
    cover: @Composable () -> Unit,
    onOpen: () -> Unit,
    onPlay: () -> Unit,
    onPlayAt: (index: Int) -> Unit,
    trackNumberOf: ((index: Int, song: Song) -> Int?)? = null,
    rowSecondaryOf: ((song: Song) -> String?)? = null,
    playingSongId: String?,
    playing: Boolean,
    narrow: Boolean,
    showAllLabel: String?,
    onShowAll: (() -> Unit)?,
    menu: (@Composable (expanded: Boolean, onDismissRequest: () -> Unit) -> Unit)? = null,
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
                    contentDescription = context.symphony.t.More,
                    onClick = { menuExpanded = true },
                )
                val onDismissMenu = { menuExpanded = false }
                when (menu) {
                    null -> GenericSongListDropdown(
                        context = context,
                        songIds = remember(songs) { songs.map { it.id } },
                        expanded = menuExpanded,
                        onDismissRequest = onDismissMenu,
                        source = title,
                        header = {
                            BentoMenuHeader(
                                title = title,
                                subtitle = subtitle,
                                art = { BentoGroupMenuArt(cover) },
                            )
                        },
                    )

                    else -> menu(menuExpanded, onDismissMenu)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        visibleRows.forEachIndexed { index, song ->
            BentoGroupTileRow(
                context = context,
                song = song,
                trackNumber = trackNumberOf?.invoke(index, song),
                secondary = rowSecondaryOf?.invoke(song),
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

/** The tile's 64 dp cover drawn at 44 dp for the menu header. */
@Composable
private fun BentoGroupMenuArt(cover: @Composable () -> Unit) {
    val scale = 44f / 64f
    Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .requiredSize(64.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                },
        ) {
            cover()
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
    context: ViewContext,
    song: Song,
    trackNumber: Int?,
    secondary: String?,
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
            .height(BentoSizes.GROUP_ROW.dp)
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
                AsyncImage(
                    song.createArtworkImageRequest(context.symphony).build(),
                    null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
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
        Text(
            DurationUtils.formatMs(song.duration),
            style = bentoTabularStyle(BentoTypography.secondary),
            color = colors.muted,
            maxLines = 1,
        )
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
            .height(BentoSizes.GROUP_ROW.dp)
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

/** A 64 dp album-art cover of a grouped tile (radius 16). */
@Composable
fun BentoGroupAlbumCover(request: ImageRequest) {
    AsyncImage(
        request,
        null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(BentoRadii.GROUP_COVER_64.dp)),
    )
}

/** A 64 dp round artist avatar of a grouped tile. */
@Composable
fun BentoGroupArtistCover(request: ImageRequest, artistName: String) {
    AsyncImage(
        request,
        artistName,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape),
    )
}

/** A 64 dp gradient swatch of a grouped genre tile. */
@Composable
fun BentoGroupGenreCover(index: Int) {
    val base = BentoGenreSwatch.colorAt(index)
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(BentoRadii.GROUP_COVER_64.dp))
            .background(
                Brush.linearGradient(listOf(base, base.darkened())),
            ),
    )
}

/** A 64 dp 2 x 2 mosaic of album art for a grouped playlist tile. */
@Composable
fun BentoGroupPlaylistCover(context: ViewContext, songIds: List<String>) {
    val cells = (0 until 4).map { index ->
        songIds.getOrNull(index)
            ?.let { songId ->
                context.symphony.groove.song.get(songId)
                    ?.createArtworkImageRequest(context.symphony)?.build()
            }
            ?: Assets.createPlaceholderImageRequest(context.symphony).build()
    }
    Column(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(BentoRadii.GROUP_COVER_64.dp)),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        cells.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                row.forEach { request ->
                    AsyncImage(
                        request,
                        null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(31.dp)
                            .clip(RoundedCornerShape(2.dp)),
                    )
                }
                if (row.size == 1) {
                    Spacer(Modifier.size(31.dp))
                }
            }
        }
    }
}

/** A 64 dp folder cover of a grouped tile: the folder icon on `tonal`. */
@Composable
fun BentoGroupFolderCover() {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(BentoRadii.GROUP_COVER_64.dp))
            .background(colors.tonal),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.Folder,
            null,
            tint = colors.onTonal,
            modifier = Modifier.size(28.dp),
        )
    }
}

// ---------------------------------------------------------------------------
// Grouped tile columns (DESIGN.md 5.3)
// ---------------------------------------------------------------------------
