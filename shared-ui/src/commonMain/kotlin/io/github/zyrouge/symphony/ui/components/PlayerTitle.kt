package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.model.UiSong
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

private const val PLAYER_FORMAT_LINE_TOP_PADDING_DP = 2

/**
 * The player title area: song title, artists and album, an optional format
 * line, and a favorite toggle on the end.
 */
@Composable
fun BentoPlayerTitle(
    song: UiSong,
    isFavorite: Boolean,
    favoriteLabel: String,
    unfavoriteLabel: String,
    formatLine: String?,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    showFavorite: Boolean = true,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                song.title,
                style = BentoTypography.title.copy(fontFamily = appFontFamily),
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                listOfNotNull(
                    song.artists.joinToString().takeIf { it.isNotEmpty() },
                    song.album,
                ).joinToString(" · "),
                style = BentoTypography.rowTitle.copy(
                    fontWeight = FontWeight.Normal,
                    fontFamily = appFontFamily,
                ),
                color = colors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            formatLine?.let {
                Text(
                    it,
                    style = bentoTabularStyle(
                        BentoTypography.label.copy(
                            fontWeight = FontWeight.Medium,
                            fontFamily = appFontFamily,
                        ),
                    ),
                    color = colors.muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = PLAYER_FORMAT_LINE_TOP_PADDING_DP.dp),
                )
            }
        }
        if (showFavorite) {
            BentoPlayerIconButton(
                icon = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                label = if (isFavorite) unfavoriteLabel else favoriteLabel,
                size = BentoSizes.ICON_BUTTON.dp,
                iconSize = BentoSizes.ICON.dp,
                tint = if (isFavorite) colors.accent else colors.sub,
                selected = isFavorite,
                onClick = onToggleFavorite,
            )
        }
    }
}
