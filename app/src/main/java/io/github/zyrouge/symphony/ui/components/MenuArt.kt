package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii

/** 44 dp artwork for [BentoMenuHeader]: radius 12, or round for artists. */
@Composable
fun BentoMenuArt(request: ImageRequest, round: Boolean = false) {
    AsyncImage(
        request,
        null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(44.dp)
            .clip(
                when {
                    round -> CircleShape
                    else -> RoundedCornerShape(BentoRadii.SONG_ART_44.dp)
                },
            ),
    )
}
