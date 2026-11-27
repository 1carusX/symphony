package io.github.zyrouge.symphony.ui.view.player

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.model.UiSong
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * The small player header: artwork, title, artists and album. The platform
 * supplies the artwork.
 */
@Composable
fun PlayerHeaderInfo(
    song: UiSong,
    artwork: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        artwork(
            Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(14.dp)),
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                song.title,
                style = BentoTypography.groupTitle.copy(fontFamily = appFontFamily),
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                listOfNotNull(
                    song.artists.joinToString().takeIf { it.isNotEmpty() },
                    song.album,
                ).joinToString(" · "),
                style = BentoTypography.body.copy(fontFamily = appFontFamily),
                color = colors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** The player close button of the sheet header. */
@Composable
fun PlayerCloseButton(
    contentDescription: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalBentoColors.current
    Box(
        modifier = modifier
            .size(BentoSizes.ICON_BUTTON.dp)
            .clip(CircleShape)
            .background(androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClose),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Filled.Close,
            contentDescription,
            tint = colors.sub,
            modifier = Modifier.size(BentoSizes.ICON.dp),
        )
    }
}
