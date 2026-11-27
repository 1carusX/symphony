package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * A browsing card: artwork over a title and a secondary line, used by the
 * album, artist, genre, playlist and folder grids. The platform supplies the
 * artwork (or its placeholder).
 */
@Composable
fun BentoMediaCard(
    title: String,
    subtitle: String,
    artwork: @Composable (Modifier) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    round: Boolean = false,
    height: Dp = 240.dp,
    appFontFamily: FontFamily? = MaterialTheme.typography.titleLarge.fontFamily,
) {
    val colors = LocalBentoColors.current
    BentoToneSurface(
        modifier = modifier.fillMaxWidth().height(height),
        onClick = onClick,
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(
                        when {
                            round -> CircleShape
                            else -> RoundedCornerShape(16.dp)
                        },
                    ),
            ) {
                artwork(Modifier.fillMaxSize())
            }
            Spacer(Modifier.height(10.dp))
            Text(
                title,
                style = BentoTypography.body.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = appFontFamily,
                ),
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                subtitle,
                style = BentoTypography.secondary.copy(fontFamily = appFontFamily),
                color = colors.sub,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
