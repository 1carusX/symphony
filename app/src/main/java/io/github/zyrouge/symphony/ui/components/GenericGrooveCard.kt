package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

@Composable
fun GenericGrooveCard(
    image: ImageRequest?,
    imageLabel: (@Composable () -> Unit)? = null,
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
    options: (@Composable (expanded: Boolean, onDismissRequest: () -> Unit) -> Unit)?,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(BentoSizes.SONG_ROW.dp)
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.padding(end = 12.dp)) {
            if (image != null) {
                AsyncImage(
                    image,
                    null,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(BentoRadii.SONG_ART_44.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(BentoRadii.SONG_ART_44.dp))
                        .background(colors.raised),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.MusicNote,
                        null,
                        tint = colors.muted,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            imageLabel?.let { label ->
                    Box(
                        modifier = Modifier
                            .offset(y = 8.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                colors.raised,
                                RoundedCornerShape(BentoRadii.TILE_SMALL.dp),
                            )
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                    ) {
                        label()
                    }
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            BentoTypography.rowTitle.let { style ->
                androidx.compose.material3.ProvideTextStyle(
                    androidx.compose.ui.text.TextStyle(
                        color = colors.fg,
                        fontSize = style.fontSize,
                        fontWeight = style.fontWeight,
                    ),
                ) { title() }
            }
            subtitle?.let {
                androidx.compose.material3.ProvideTextStyle(
                    androidx.compose.ui.text.TextStyle(
                        color = colors.sub,
                        fontSize = BentoTypography.secondary.fontSize,
                    ),
                ) { it() }
            }
        }
        options?.let {
            var showOptionsMenu by remember { mutableStateOf(false) }
            IconButton(onClick = { showOptionsMenu = !showOptionsMenu }) {
                Icon(Icons.Filled.MoreVert, null, tint = colors.sub)
                it(showOptionsMenu) { showOptionsMenu = false }
            }
        }
    }
}
