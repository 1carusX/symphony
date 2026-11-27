package io.github.zyrouge.symphony.ui.components

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
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.zyrouge.symphony.i18n.Strings
import io.github.zyrouge.symphony.ui.model.UiAlbum
import io.github.zyrouge.symphony.ui.model.UiSong
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * The "Your library" stat tile (DESIGN.md 4.1, 5.1): label, total hours,
 * songs and albums, with a Rescan pill in the top-right corner.
 */
@Composable
fun ForYouLibraryStatTile(
    geometry: BentoTileGeometry,
    appFontFamily: FontFamily?,
    totalDurationMs: Long,
    songCount: Int,
    albumCount: Int,
    strings: Strings,
    onRescan: () -> Unit,
) {
    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        Box(modifier = Modifier.fillMaxSize()) {
            BentoStatContent(
                label = strings.YourLibrary,
                value = formatBentoDurationMinutes(totalDurationMs, strings),
                valueStyle = bentoStatValueStyle(geometry.sizeClass, appFontFamily),
                // Beside the corner pill of a narrow tile, two short lines fit where one long one does not.
                sub = strings.XSongs(songCount.toString()) +
                    (if (geometry.width >= 320f) " · " else "\n") +
                    strings.XAlbums(albumCount.toString()),
                geometry = geometry,
                // A narrow tile has no room for the value beside the pill, so the
                // icon-only pill sits in the bottom-right corner instead.
                bottomEndInset = if (geometry.width >= 320f) 0.dp else 44.dp,
            )
            BentoCallToActionPill(
                label = strings.Rescan,
                wide = geometry.width >= 320f,
                icon = Icons.Rounded.Refresh,
                tilePadding = bentoTilePaddingDp(geometry.sizeClass),
                onClick = onRescan,
                bottom = geometry.width < 320f,
            )
        }
    }
}

/**
 * A collection tile (DESIGN.md 4.1, "Collection"): a header with "See all"
 * and a grid of square album covers. The platform supplies the artwork and
 * the click targets.
 */
@Composable
fun ForYouAlbumCollectionTile(
    geometry: BentoTileGeometry,
    label: String,
    seeAllLabel: String,
    albums: List<UiAlbum>,
    artwork: @Composable (UiAlbum, Modifier) -> Unit,
    onAlbumClick: (UiAlbum) -> Unit,
    onSeeAll: () -> Unit,
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
                actionLabel = seeAllLabel,
                showAction = geometry.width >= 220f,
                onAction = onSeeAll,
            )
            Box(modifier = Modifier.weight(1f)) {
                if (albums.isNotEmpty() && fit.rows > 0) {
                    BentoCoverGrid(
                        itemCount = albums.size,
                        fit = fit,
                    ) { index ->
                        val album = albums.getOrNull(index) ?: return@BentoCoverGrid
                        Column {
                            Box(
                                modifier = Modifier
                                    .size(fit.coverSize.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { onAlbumClick(album) },
                            ) {
                                artwork(album, Modifier.fillMaxSize())
                            }
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
fun ForYouArtistsTile(
    geometry: BentoTileGeometry,
    label: String,
    seeAllLabel: String,
    artistNames: List<String>,
    artwork: @Composable (String, Modifier) -> Unit,
    onArtistClick: (String) -> Unit,
    onSeeAll: () -> Unit,
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
                        label,
                        style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
                        color = tileColors.sub,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        seeAllLabel,
                        style = BentoTypography.body.copy(fontWeight = FontWeight.SemiBold),
                        color = LocalBentoColors.current.accent,
                        maxLines = 1,
                        modifier = Modifier.clickable(onClick = onSeeAll),
                    )
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    artistNames.forEach { artistName ->
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .clickable { onArtistClick(artistName) },
                        ) {
                            artwork(artistName, Modifier.fillMaxSize())
                        }
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
                    label = label,
                    actionLabel = seeAllLabel,
                    showAction = true,
                    onAction = onSeeAll,
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
                                Box(
                                    modifier = Modifier
                                        .size(fit.coverSize.dp)
                                        .clip(CircleShape)
                                        .clickable { onArtistClick(artistName) },
                                ) {
                                    artwork(artistName, Modifier.fillMaxSize())
                                }
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

/** One row of the Up next tile, with the queue position it jumps to. */
data class ForYouUpNextEntry(
    val position: Int,
    val song: UiSong,
    val durationText: String,
)

/**
 * The Up next tile (DESIGN.md 4.1, "List"): 48 dp rows with art, title,
 * artist and duration; tapping a row jumps the queue there.
 */
@Composable
fun ForYouUpNextTile(
    geometry: BentoTileGeometry,
    label: String,
    actionLabel: String,
    entries: List<ForYouUpNextEntry>,
    artwork: @Composable (UiSong, Modifier) -> Unit,
    onJump: (position: Int) -> Unit,
) {
    val tileColors = LocalBentoTileContentColors.current
    val maxRows = maxOf(1, ((geometry.height - 60f) / 48f).toInt())
    val rows = entries.take(maxRows)

    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        Column(modifier = Modifier.fillMaxSize()) {
            BentoTileHeader(
                label = label,
                actionLabel = actionLabel,
                showAction = true,
                onAction = null,
            )
            rows.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(BentoRadii.ROW.dp))
                        .clickable { onJump(entry.position) }
                        .padding(horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp)),
                    ) {
                        artwork(entry.song, Modifier.fillMaxSize())
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            entry.song.title,
                            style = BentoTypography.rowTitle.copy(fontSize = 14.sp),
                            color = tileColors.fg,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        entry.song.artists.takeIf { it.isNotEmpty() }?.let {
                            Text(
                                it.joinToString(),
                                style = BentoTypography.label,
                                color = tileColors.sub,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Text(
                        entry.durationText,
                        style = BentoTypography.label,
                        color = tileColors.sub,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}
