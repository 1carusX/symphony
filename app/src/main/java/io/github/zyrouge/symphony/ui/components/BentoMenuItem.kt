package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import io.github.zyrouge.symphony.ui.theme.ThemeColors
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * A menu item (DESIGN.md 4.5): 48 dp, radius 16, a 22 dp icon in `sub`, the
 * label at 15/500 and an optional value pill in `tonal`. A null [icon] keeps
 * the icon slot so labels stay aligned. [destructive] items turn the icon and
 * label red.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoMenuItem(
    label: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    value: String? = null,
    trailingIcon: ImageVector? = null,
    destructive: Boolean = false,
) {
    val colors = LocalBentoColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val focused by interactionSource.collectIsFocusedAsState()
    val labelColor = when {
        destructive -> ThemeColors.Red
        else -> colors.fg
    }
    val iconColor = when {
        destructive -> ThemeColors.Red
        else -> colors.sub
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = BentoSizes.MENU_ITEM.dp)
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .background(
                when {
                    hovered || pressed || focused -> colors.fg.copy(alpha = 0.08f)
                    else -> Color.Transparent
                },
            )
            .hoverable(interactionSource)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(modifier = Modifier.size(22.dp), contentAlignment = Alignment.Center) {
            icon?.let {
                Icon(it, null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
        }
        Text(
            label,
            style = BentoTypography.rowTitle.copy(fontWeight = FontWeight.Medium),
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 4.dp),
        )
        value?.let {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.tonal)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(
                    it,
                    style = bentoTabularStyle(
                        BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
                    ),
                    color = colors.onTonal,
                    maxLines = 1,
                )
            }
        }
        trailingIcon?.let {
            Icon(it, null, tint = colors.muted, modifier = Modifier.size(20.dp))
        }
    }
}

/** The line between groups of menu items (prototype mn-sep). */
@Composable
fun BentoMenuDivider() {
    val colors = LocalBentoColors.current
    Box(
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.line),
    )
}

/**
 * A menu header (DESIGN.md 4.5): 44 dp art, the title and a subtitle. Pass
 * it as [BentoMenu]'s header.
 */
@Composable
fun BentoMenuHeader(
    title: String,
    subtitle: String?,
    art: @Composable () -> Unit,
) {
    val colors = LocalBentoColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            art()
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                title,
                style = BentoTypography.rowTitle,
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            subtitle?.takeIf { it.isNotEmpty() }?.let {
                Text(
                    it,
                    style = BentoTypography.secondary,
                    color = colors.sub,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

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

/**
 * The round More button that opens a row or tile menu (prototype sn-more
 * and gt-more): a `muted` icon that brightens with a 10 % wash on hover.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BentoMoreButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val colors = LocalBentoColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val active = hovered || pressed

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(
                when {
                    active -> colors.fg.copy(alpha = 0.10f)
                    else -> Color.Transparent
                },
            )
            .hoverable(interactionSource)
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            Icons.Rounded.MoreVert,
            contentDescription,
            tint = when {
                active -> colors.fg
                else -> colors.muted
            },
            modifier = Modifier.size(20.dp),
        )
    }
}
