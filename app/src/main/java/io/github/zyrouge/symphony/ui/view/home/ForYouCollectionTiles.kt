package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import io.github.zyrouge.symphony.ui.helpers.libraryRescanAction
import io.github.zyrouge.symphony.services.groove.Album
import io.github.zyrouge.symphony.services.groove.Groove
import io.github.zyrouge.symphony.ui.components.BentoCallToActionPill
import io.github.zyrouge.symphony.ui.components.BentoCoverGrid
import io.github.zyrouge.symphony.ui.components.BentoStatContent
import io.github.zyrouge.symphony.ui.components.BentoTile
import io.github.zyrouge.symphony.ui.components.formatBentoDurationMinutes
import io.github.zyrouge.symphony.ui.components.BentoTileGeometry
import io.github.zyrouge.symphony.ui.components.BentoTileHeader
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.LocalBentoTileContentColors
import io.github.zyrouge.symphony.ui.components.bentoStatValueStyle
import io.github.zyrouge.symphony.ui.components.bentoTilePaddingDp
import io.github.zyrouge.symphony.ui.components.fitBentoCoverGrid
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.ui.view.AlbumViewRoute
import io.github.zyrouge.symphony.ui.view.ArtistViewRoute
import io.github.zyrouge.symphony.ui.view.HomePage
import io.github.zyrouge.symphony.utils.DurationUtils
import kotlin.math.floor

/**
 * The "Your library" stat tile (DESIGN.md 4.1, 5.1): label, total hours,
 * songs and albums, with a Rescan pill in the top-right corner (the shell's
 * temporary Rescan action retires here).
 */
@Composable
internal fun ForYouLibraryStatTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
    appFontFamily: FontFamily?,
    totalDurationMs: Long,
    songCount: Int,
    albumCount: Int,
) {
    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        Box(modifier = Modifier.fillMaxSize()) {
            BentoStatContent(
                label = context.symphony.t.YourLibrary,
                value = formatBentoDurationMinutes(totalDurationMs),
                valueStyle = bentoStatValueStyle(geometry.sizeClass, appFontFamily),
                sub = "${context.symphony.t.XSongs(songCount.toString())} · " +
                    context.symphony.t.XAlbums(albumCount.toString()),
                geometry = geometry,
            )
            BentoCallToActionPill(
                label = context.symphony.t.Rescan,
                wide = geometry.width >= 320f,
                icon = Icons.Rounded.Refresh,
                tilePadding = bentoTilePaddingDp(geometry.sizeClass),
                onClick = libraryRescanAction(context),
            )
        }
    }
}

/**
 * A collection tile (DESIGN.md 4.1, "Collection"): a header with "See all"
 * and a grid of square album covers that opens the album page.
 */
@Composable
internal fun ForYouAlbumCollectionTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
    label: String,
    albums: List<Album>,
) {
    val tileColors = LocalBentoTileContentColors.current
    val padding = bentoTilePaddingDp(geometry.sizeClass)
    val fit = fitBentoCoverGrid(
        tileWidthDp = geometry.width - 2f * padding.value,
        tileHeightDp = geometry.height - 2f * padding.value - 30f,
        itemCount = albums.size,
    )
    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        Column(modifier = Modifier.fillMaxSize()) {
            BentoTileHeader(
                label = label,
                actionLabel = context.symphony.t.All,
                showAction = geometry.width >= 220f,
                onAction = { selectHomeSection(context, HomePage.Albums) },
            )
            Box(modifier = Modifier.weight(1f)) {
                if (albums.isNotEmpty() && fit.rows > 0) {
                    BentoCoverGrid(
                        itemCount = albums.size,
                        fit = fit,
                    ) { index ->
                        val album = albums.getOrNull(index) ?: return@BentoCoverGrid
                        Column {
                            AsyncImage(
                                album.createArtworkImageRequest(context.symphony).build(),
                                null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(fit.coverSize.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        context.navController.navigate(AlbumViewRoute(album.id))
                                    },
                            )
                            if (fit.captioned) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    album.name,
                                    style = BentoTypography.secondary.copy(
                                        fontWeight = FontWeight.SemiBold,
                                    ),
                                    color = tileColors.fg,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(fit.coverSize.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The Artists tile (DESIGN.md 4.1, "People"): a single row of round avatars
 * when short, an avatar grid with names when tall.
 */
@Composable
internal fun ForYouArtistsTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
    artistNames: List<String>,
) {
    val tileColors = LocalBentoTileContentColors.current
    val padding = bentoTilePaddingDp(geometry.sizeClass)

    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        if (geometry.height < 170f) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.width(72.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        context.symphony.t.Artists,
                        style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
                        color = tileColors.sub,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        context.symphony.t.All,
                        style = BentoTypography.body.copy(fontWeight = FontWeight.SemiBold),
                        color = LocalBentoColors.current.accent,
                        maxLines = 1,
                        modifier = Modifier.clickable {
                            selectHomeSection(context, HomePage.Artists)
                        },
                    )
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    artistNames.forEach { artistName ->
                        AsyncImage(
                            context.symphony.groove.artist
                                .createArtworkImageRequest(artistName)
                                .build(),
                            artistName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .clickable {
                                    context.navController.navigate(ArtistViewRoute(artistName))
                                },
                        )
                    }
                }
            }
        } else {
            val fit = fitBentoCoverGrid(
                tileWidthDp = geometry.width - 2f * padding.value,
                tileHeightDp = geometry.height - 2f * padding.value - 30f,
                itemCount = artistNames.size,
                gapDp = 12f,
                minSizeDp = 44f,
                captionHeightDp = 24f,
                captionMinSizeDp = 44f,
            )
            Column(modifier = Modifier.fillMaxSize()) {
                BentoTileHeader(
                    label = context.symphony.t.Artists,
                    actionLabel = context.symphony.t.All,
                    showAction = true,
                    onAction = { selectHomeSection(context, HomePage.Artists) },
                )
                Box(modifier = Modifier.weight(1f)) {
                    if (artistNames.isNotEmpty() && fit.rows > 0) {
                        BentoCoverGrid(
                            itemCount = artistNames.size,
                            fit = fit,
                            gap = 12.dp,
                        ) { index ->
                            val artistName = artistNames.getOrNull(index)
                                ?: return@BentoCoverGrid
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width((fit.coverSize + 16f).dp),
                            ) {
                                AsyncImage(
                                    context.symphony.groove.artist
                                        .createArtworkImageRequest(artistName)
                                        .build(),
                                    artistName,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(fit.coverSize.dp)
                                        .clip(CircleShape)
                                        .clickable {
                                            context.navController.navigate(
                                                ArtistViewRoute(artistName),
                                            )
                                        },
                                )
                                if (fit.captioned) {
                                    Spacer(Modifier.height(6.dp))
                                    Text(
                                        artistName,
                                        style = BentoTypography.label,
                                        color = tileColors.fg,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The Up next tile (DESIGN.md 4.1, "List"): 48 dp rows with art, title,
 * artist and duration; tapping a row jumps the queue there.
 */
@Composable
internal fun ForYouUpNextTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
) {
    val queue by context.symphony.radio.observatory.queue.collectAsState()
    val queueIndex by context.symphony.radio.observatory.queueIndex.collectAsState()
    val upNext by remember(queue, queueIndex) {
        derivedStateOf {
            queue.drop(queueIndex + 1).mapIndexed { index, songId ->
                queueIndex + 1 + index to songId
            }
        }
    }
    val maxRows = maxOf(1, floor((geometry.height - 62f + 2f) / 48f).toInt())
    val rows = upNext.take(maxRows)

    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        Column(modifier = Modifier.fillMaxSize()) {
            BentoTileHeader(
                label = context.symphony.t.UpNext,
                actionLabel = when {
                    upNext.isEmpty() -> "End of queue"
                    else -> context.symphony.t.XSongs(upNext.size.toString())
                },
                showAction = true,
                onAction = null,
            )
            rows.forEach { (queuePosition, songId) ->
                val song = context.symphony.groove.song.get(songId) ?: return@forEach
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { context.symphony.radio.jumpTo(queuePosition) }
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AsyncImage(
                        song.createArtworkImageRequest(context.symphony).build(),
                        null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            song.title,
                            style = BentoTypography.rowTitle.copy(fontSize = 14.sp),
                            color = LocalBentoTileContentColors.current.fg,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        song.artists.takeIf { it.isNotEmpty() }?.let {
                            Text(
                                it.joinToString(),
                                style = BentoTypography.label,
                                color = LocalBentoTileContentColors.current.sub,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Text(
                        DurationUtils.formatMs(song.duration),
                        style = BentoTypography.label,
                        color = LocalBentoTileContentColors.current.sub,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
