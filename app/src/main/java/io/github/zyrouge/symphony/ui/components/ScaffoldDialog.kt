package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

@Suppress("ConstPropertyName")
object ScaffoldDialogDefaults {
    const val PreferredMaxHeight = 0.8f
}

@Composable
fun ScaffoldDialog(
    title: @Composable () -> Unit,
    titleLeading: (@Composable () -> Unit)? = null,
    titleTrailing: (@Composable () -> Unit)? = null,
    topBar: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
    actions: (@Composable RowScope.() -> Unit)? = null,
    contentHeight: Float? = null,
    removeActionsVerticalPadding: Boolean = false,
    onDismissRequest: () -> Unit,
) {
    val windowSize = LocalWindowInfo.current.containerDpSize
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily

    BentoAppDialog(onDismissRequest = onDismissRequest) {
        val materialColors = MaterialTheme.colorScheme
        MaterialTheme(
            colorScheme = materialColors.copy(
                primary = colors.accent,
                onPrimary = colors.onAccent,
                surface = colors.float,
                background = colors.bg,
            ),
        ) {
            Column(
            modifier = Modifier
                .widthIn(max = 560.dp)
                .run {
                    val maxHeight = windowSize.height * 0.9f
                    when {
                        contentHeight != null -> height(maxHeight.times(contentHeight))
                        else -> requiredHeightIn(max = maxHeight)
                    }
                }
                .clip(RoundedCornerShape(BentoRadii.TILE_HOVER.dp))
                .background(colors.float)
                .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 8.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                titleLeading?.invoke()
                Box(modifier = Modifier.weight(1f)) {
                    ProvideTextStyle(
                        value = BentoTypography.title.copy(fontFamily = appFontFamily),
                    ) {
                        title()
                    }
                }
                titleTrailing?.invoke()
            }
            Spacer(modifier = Modifier.height(12.dp))
            topBar?.invoke()
            Box(
                modifier = Modifier.run {
                    contentHeight?.let { weight(it) } ?: weight(1f, fill = false)
                },
            ) {
                content()
            }
            actions?.let {
                if (!removeActionsVerticalPadding) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    actions()
                }
                if (!removeActionsVerticalPadding) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        }
    }
}
