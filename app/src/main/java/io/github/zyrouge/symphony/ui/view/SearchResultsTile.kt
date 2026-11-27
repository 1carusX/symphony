package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.i18n.Strings
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.model.SearchHit
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

private const val BENTO_ARTIST_HIT_PREFIX = "artist:"
private const val BENTO_ALBUM_HIT_PREFIX = "album:"

/** The artist and album hits for the query, as shared hit models. */
internal fun searchHitsOf(
    context: ViewContext,
    t: Strings,
    artistNames: List<String>,
    albumIds: List<String>,
) = artistNames.map { artistName ->
    SearchHit(
        key = BENTO_ARTIST_HIT_PREFIX + artistName,
        title = artistName,
        subtitle = t.Artist,
        round = true,
    )
} + albumIds.mapNotNull { albumId ->
    context.symphony.groove.album.get(albumId)?.let { album ->
        SearchHit(
            key = BENTO_ALBUM_HIT_PREFIX + albumId,
            title = album.name,
            subtitle = t.Album,
            round = false,
        )
    }
}

@Composable
internal fun SearchHitArtwork(
    context: ViewContext,
    hit: SearchHit,
    modifier: Modifier,
) {
    when {
        hit.round -> AsyncImage(
            context.symphony.groove.artist
                .createArtworkImageRequest(hit.title)
                .build(),
            null,
            contentScale = ContentScale.Crop,
            modifier = modifier,
        )

        else -> {
            val albumId = hit.key.removePrefix(BENTO_ALBUM_HIT_PREFIX)
            context.symphony.groove.album.get(albumId)?.let { album ->
                AsyncImage(
                    album.createArtworkImageRequest(context.symphony).build(),
                    null,
                    contentScale = ContentScale.Crop,
                    modifier = modifier,
                )
            } ?: Box(modifier = modifier.background(LocalBentoColors.current.raised))
        }
    }
}

internal fun openSearchHit(context: ViewContext, hit: SearchHit) {
    when {
        hit.round -> context.navController.navigate(ArtistViewRoute(hit.title))
        else -> context.navController.navigate(
            AlbumViewRoute(hit.key.removePrefix(BENTO_ALBUM_HIT_PREFIX)),
        )
    }
}

/**
 * The results tile (DESIGN.md 5.5): shared layout with the Android artwork,
 * row menu and playback actions.
 */
