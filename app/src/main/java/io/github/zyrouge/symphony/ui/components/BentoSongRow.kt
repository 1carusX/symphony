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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.services.groove.Song
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.utils.DurationUtils

/**
 * A song row (DESIGN.md 4.4): 60 dp (56 in column mode), 44 dp art, the
 * title (accent while playing, with an equalizer glyph over the art), the
 * artist - album line and the duration. Column mode swaps the line for
 * artist and album cells. With a [menu], a More button ends the row and a
 * long press opens the menu too (unless [onLongClick] is set).
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoSongRow(
    context: ViewContext,
    song: Song,
    playing: Boolean,
    isPlaying: Boolean,
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
                    !columns -> Modifier.height(BentoSizes.SONG_ROW.dp)
                    else -> Modifier.height(BentoSizes.SONG_ROW_COLUMNS.dp)
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
            AsyncImage(
                song.createArtworkImageRequest(context.symphony).build(),
                null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
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
                DurationUtils.formatMs(song.duration),
                style = durationStyle,
                color = colors.muted,
                maxLines = 1,
                textAlign = TextAlign.End,
                modifier = Modifier.width(52.dp),
            )
        } else {
            Column(modifier = Modifier.weight(1f)) {
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
            trailing?.invoke(this)
            Text(
                DurationUtils.formatMs(song.duration),
                style = durationStyle,
                color = colors.muted,
                maxLines = 1,
                textAlign = TextAlign.End,
            )
        }
        menu?.let {
            Box {
                BentoMoreButton(
                    contentDescription = context.symphony.t.More,
                    onClick = { menuExpanded = true },
                )
                it(menuExpanded) { menuExpanded = false }
            }
        }
    }
}

/**
 * The sticky column header of the Songs page in column mode (DESIGN.md 4.4):
 * 40 dp, 12/700 uppercase with 0.06em tracking.
 */
@Composable
fun BentoSongColumnHeader(
    title: String,
    artist: String,
    album: String,
    duration: String,
) {
    val colors = LocalBentoColors.current
    val style = BentoTypography.label.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.06.em,
    )
    // The header is uppercase (DESIGN.md 4.4).
    val upperTitle = title.uppercase()
    val upperArtist = artist.uppercase()
    val upperAlbum = album.uppercase()
    val upperDuration = duration.uppercase()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(Modifier.width(44.dp))
        Text(
            upperTitle,
            style = style,
            color = colors.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(2f),
        )
        Text(
            upperArtist,
            style = style,
            color = colors.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.3f),
        )
        Text(
            upperAlbum,
            style = style,
            color = colors.muted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1.5f),
        )
        Text(
            upperDuration,
            style = style,
            color = colors.muted,
            maxLines = 1,
            textAlign = TextAlign.End,
            modifier = Modifier.width(52.dp),
        )
    }
}
