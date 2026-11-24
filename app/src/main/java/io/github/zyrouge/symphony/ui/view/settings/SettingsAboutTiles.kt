package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import io.github.zyrouge.symphony.ui.helpers.libraryRescanAction
import io.github.zyrouge.symphony.R
import io.github.zyrouge.symphony.services.AppMeta
import io.github.zyrouge.symphony.services.groove.Groove
import io.github.zyrouge.symphony.ui.components.BentoCallToActionPill
import io.github.zyrouge.symphony.ui.components.BentoStatContent
import io.github.zyrouge.symphony.ui.components.BentoTile
import io.github.zyrouge.symphony.ui.components.BentoTileGeometry
import io.github.zyrouge.symphony.ui.components.BentoTileTone
import io.github.zyrouge.symphony.ui.components.LocalBentoTileContentColors
import io.github.zyrouge.symphony.ui.components.bentoStatValueStyle
import io.github.zyrouge.symphony.ui.components.bentoTilePaddingDp
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.utils.ActivityUtils

/**
 * The About tile: app name and version, matching the app info block of the
 * previous settings home. It is laid out like a medium shortcut tile, an icon
 * well beside the texts, so it reads as one of the tiles around it.
 */
@Composable
internal fun SettingsAboutTileContent(context: ViewContext) {
    val tileColors = LocalBentoTileContentColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(tileColors.fg.copy(alpha = 0.13f)),
            contentAlignment = Alignment.Center,
        ) {
            // The launcher glyph fills the middle 40 % of its canvas, so it is
            // drawn at 50 dp for a 20 dp glyph, the size of the other wells' icons.
            Icon(
                painterResource(R.drawable.ic_launcher_monochrome),
                null,
                tint = tileColors.fg,
                modifier = Modifier.requiredSize(50.dp),
            )
        }
        Column {
            Text(
                AppMeta.appName,
                style = BentoTypography.rowTitle.copy(fontFamily = appFontFamily),
                color = tileColors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            // The line breaker does not break after a hyphen that follows digits,
            // so a narrow tile would split "debug"; the zero-width space lets the
            // version wrap after its hyphen instead.
            Text(
                AppMeta.version.replace("-", "-​"),
                style = BentoTypography.secondary,
                color = tileColors.sub,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            AppMeta.latestVersion?.takeIf { AppMeta.version != it }?.let {
                Text(
                    context.symphony.t.NewVersionAvailableX(it),
                    style = BentoTypography.secondary,
                    color = tileColors.sub,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * The Library stat tile (DESIGN.md 5.6): song and folder counts with a
 * Rescan pill.
 */
@Composable
internal fun SettingsLibraryStatTile(
    context: ViewContext,
    geometry: BentoTileGeometry,
    appFontFamily: FontFamily?,
    songCount: Int,
    folderCount: Int,
) {
    BentoTile(tone = BentoTileTone.PLAIN, sizeClass = geometry.sizeClass) {
        Box(modifier = Modifier.fillMaxSize()) {
            BentoStatContent(
                label = context.symphony.t.Library,
                value = context.symphony.t.XSongs(songCount.toString()),
                valueStyle = bentoStatValueStyle(geometry.sizeClass, appFontFamily),
                sub = context.symphony.t.InX(context.symphony.t.XFolders(folderCount.toString())),
                geometry = geometry,
                // A narrow tile has no room for the value beside the pill, so the
                // icon-only pill sits in the bottom-right corner instead.
                bottomEndInset = if (geometry.width >= 320f) 0.dp else 44.dp,
            )
            BentoCallToActionPill(
                label = context.symphony.t.Rescan,
                wide = geometry.width >= 320f,
                icon = Icons.Rounded.Refresh,
                tilePadding = bentoTilePaddingDp(geometry.sizeClass),
                onClick = libraryRescanAction(context),
                bottom = geometry.width < 320f,
            )
        }
    }
}

/**
 * The community links of the previous settings home, kept under the grid
 * because the kit does not design them.
 */
@Composable
internal fun SettingsLegacyLinksRow(context: ViewContext) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp),
    ) {
        LinkChip(
            context,
            icon = Icons.Rounded.BugReport,
            label = context.symphony.t.ReportAnIssue,
            url = AppMeta.githubIssuesUrl,
        )
        LinkChip(
            context,
            label = context.symphony.t.Github,
            url = AppMeta.githubRepositoryUrl,
        )
        LinkChip(
            context,
            label = context.symphony.t.Discord,
            url = AppMeta.discordUrl,
        )
        LinkChip(
            context,
            label = context.symphony.t.Reddit,
            url = AppMeta.redditUrl,
        )
        LinkChip(
            context,
            label = context.symphony.t.PlayStore,
            url = AppMeta.playStoreUrl,
        )
        LinkChip(
            context,
            label = context.symphony.t.FDroid,
            url = AppMeta.fdroidUrl,
        )
        LinkChip(
            context,
            label = context.symphony.t.IzzyOnDroid,
            url = AppMeta.izzyOnDroidUrl,
        )
    }
}

/** A link pill (prototype `gchip`): 36 dp, radius 18, `surface` fill, 14/600 label. */
@Composable
private fun LinkChip(context: ViewContext, icon: ImageVector? = null, label: String, url: String) {
    val colors = LocalBentoColors.current
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .height(36.dp)
            .clip(shape)
            .then(
                when (LocalBentoColorScheme.current) {
                    BentoColorScheme.CONTRAST -> Modifier.border(1.dp, colors.line, shape)
                    else -> Modifier
                },
            )
            .background(colors.surface)
            .clickable {
                ActivityUtils.startBrowserActivity(context.activity, url.toUri())
            }
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        icon?.let {
            Icon(
                it,
                null,
                modifier = Modifier.size(18.dp),
                tint = colors.accent,
            )
        }
        Text(
            label,
            style = BentoTypography.body.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = MaterialTheme.typography.titleLarge.fontFamily,
            ),
            color = colors.fg,
            maxLines = 1,
        )
    }
}
