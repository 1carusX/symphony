package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/**
 * Details sheet: an optional [header] (see [InformationHeader]), then
 * label/value rows ([InformationKeyValue]), extra [actions] and a close
 * button.
 */
@Composable
fun InformationDialog(
    context: ViewContext,
    content: @Composable (ColumnScope.() -> Unit),
    onDismissRequest: () -> Unit,
    title: String? = null,
    header: (@Composable () -> Unit)? = null,
    actions: (@Composable RowScope.() -> Unit)? = null,
) {
    val colors = LocalBentoColors.current
    BentoSheetDialog(
        title = title ?: context.symphony.t.Details,
        onDismissRequest = onDismissRequest,
        actions = {
            actions?.invoke(this)
            BentoSheetButton(
                label = context.symphony.t.Done,
                primary = true,
                onClick = onDismissRequest,
            )
        },
    ) {
        header?.let {
            Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                it()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.line),
            )
        }
        Column(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            content()
        }
    }
}

/** Cover, title and subtitle above the rows of an [InformationDialog]. */
@Composable
fun InformationHeader(
    image: ImageRequest,
    title: @Composable () -> Unit,
    subtitle: String?,
) {
    val colors = LocalBentoColors.current
    Row(
        modifier = Modifier.padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            image,
            null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(BentoRadii.GROUP_COVER_64.dp))
                .background(colors.raised),
        )
        Column(modifier = Modifier.weight(1f)) {
            ProvideTextStyle(BentoTypography.groupTitle.copy(color = colors.fg)) {
                title()
            }
            subtitle?.let {
                Text(
                    it,
                    style = BentoTypography.secondary,
                    color = colors.sub,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/** One label/value line; [mono] sets the value in a monospace face (paths, IDs). */
@Composable
fun InformationKeyValue(
    key: String,
    mono: Boolean = false,
    value: @Composable () -> Unit,
) {
    val colors = LocalBentoColors.current
    // The key sits in a 112 dp column beside the value. When that leaves the value
    // too little room for its longest word (large font or content scale), the key
    // goes above the value instead, so words are not split across lines.
    Layout(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 7.dp),
        content = {
            Text(
                key,
                style = BentoTypography.secondary,
                color = colors.muted,
                modifier = Modifier.padding(top = 1.dp),
            )
            Box {
                ProvideTextStyle(
                    value = when {
                        mono -> BentoTypography.secondary.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.5.sp,
                            color = colors.fg,
                        )

                        else -> BentoTypography.body.copy(
                            fontWeight = FontWeight.Medium,
                            color = colors.fg,
                        )
                    },
                ) {
                    value()
                }
            }
        },
    ) { measurables, constraints ->
        val width = constraints.maxWidth
        val keyWidth = 112.dp.roundToPx()
        val valueBeside = (width - keyWidth - 16.dp.roundToPx()).coerceAtLeast(0)
        val stacked = when {
            // Paths and IDs break anywhere anyway; they only move once their column is narrower than the key's.
            mono -> valueBeside < keyWidth
            else -> measurables[1].minIntrinsicWidth(Constraints.Infinity) > valueBeside
        }
        val keyPlaceable = measurables[0].measure(
            Constraints.fixedWidth(if (stacked) width else keyWidth),
        )
        val valuePlaceable = measurables[1].measure(
            Constraints.fixedWidth(if (stacked) width else valueBeside),
        )
        val stackGap = 2.dp.roundToPx()
        val height = when {
            stacked -> keyPlaceable.height + stackGap + valuePlaceable.height
            else -> maxOf(keyPlaceable.height, valuePlaceable.height)
        }
        layout(width, height) {
            keyPlaceable.placeRelative(0, 0)
            when {
                stacked -> valuePlaceable.placeRelative(0, keyPlaceable.height + stackGap)
                else -> valuePlaceable.placeRelative(width - valueBeside, 0)
            }
        }
    }
}
