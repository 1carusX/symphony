package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MenuRow(
    leading: @Composable () -> Unit,
    label: String,
    value: String?,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    BentoAdaptiveRow(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = BentoSizes.MENU_ITEM.dp)
            .clip(RoundedCornerShape(BentoRadii.ROW.dp))
            .combinedClickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        gap = 14.dp,
        leading = leading,
        trailing = value?.let {
            {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.tonal)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        it,
                        style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
                        color = colors.onTonal,
                        maxLines = 1,
                    )
                }
            }
        },
    ) {
        Text(
            label,
            style = BentoTypography.rowTitle.copy(fontWeight = FontWeight.Medium),
            color = colors.fg,
            modifier = Modifier.padding(vertical = 4.dp),
        )
    }
}
