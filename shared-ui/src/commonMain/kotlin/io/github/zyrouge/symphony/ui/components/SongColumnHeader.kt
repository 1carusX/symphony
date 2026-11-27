package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

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
        Spacer(Modifier.width(40.dp))
    }
}
