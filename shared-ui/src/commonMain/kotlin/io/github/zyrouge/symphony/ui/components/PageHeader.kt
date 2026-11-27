package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.BentoWindowClass
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * Page header of a bento screen: a back button and the page title, sized per
 * window class (28 sp on phones, 34 sp on rail layouts, 22 sp on short
 * windows).
 */
@Composable
fun BentoPageHeader(
    title: String,
    windowClass: BentoWindowClass,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val (titleStyle, paddings, minHeight) = when (windowClass) {
        BentoWindowClass.COMPACT -> Triple(
            BentoTypography.pageTitleCompact.copy(fontFamily = appFontFamily),
            PaddingValues(start = 8.dp, end = 8.dp, top = 14.dp, bottom = 6.dp),
            BentoSizes.HEADER_MIN_HEIGHT_PHONE.dp,
        )

        BentoWindowClass.SHORT_HEIGHT -> Triple(
            BentoTypography.pageTitleShort.copy(fontFamily = appFontFamily),
            PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 6.dp),
            BentoSizes.HEADER_MIN_HEIGHT_SHORT.dp,
        )

        else -> Triple(
            BentoTypography.pageTitle.copy(fontFamily = appFontFamily),
            PaddingValues(start = 24.dp, end = 24.dp, top = 22.dp, bottom = 14.dp),
            BentoSizes.HEADER_MIN_HEIGHT.dp,
        )
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .padding(paddings),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        IconButton(onClick = onBack) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                null,
                tint = colors.fg,
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = titleStyle,
                color = colors.fg,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = BentoTypography.secondary,
                    color = colors.sub,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        trailing?.invoke()
    }
}
