package io.github.zyrouge.symphony.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.theme.BentoAppDensity
import io.github.zyrouge.symphony.ui.theme.bento.BentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.BentoRadii
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColorScheme
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors

/** One sort option of [BentoSortMenu]. */
@Immutable
data class BentoSortOption<T>(val value: T, val label: String)

/**
 * The sort menu (DESIGN.md 4.5): a title, one row per option with a check mark
 * on the current one, then a reverse-order row with an On/Off value pill.
 */
@Composable
fun <T> BentoSortMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    title: String,
    options: List<BentoSortOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    reverse: Boolean,
    reverseLabel: String,
    onReverseChange: (Boolean) -> Unit,
    onValue: String,
    offValue: String,
) {
    val colors = LocalBentoColors.current
    val colorScheme = LocalBentoColorScheme.current
    val menuScroll = rememberScrollState()
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = Modifier.bentoScrollbar(menuScroll, colors.muted, BentoRadii.MENU.dp),
        scrollState = menuScroll,
        containerColor = colors.high,
        shape = RoundedCornerShape(BentoRadii.MENU.dp),
        shadowElevation = 16.dp,
        border = when (colorScheme) {
            BentoColorScheme.CONTRAST -> BorderStroke(1.dp, colors.line)
            else -> null
        },
    ) {
        BentoAppDensity {
            Box(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                Column {
                    Text(
                        title,
                        style = BentoTypography.secondary.copy(fontWeight = FontWeight.Bold),
                        color = colors.muted,
                        maxLines = 1,
                        modifier = Modifier.padding(start = 12.dp, top = 6.dp, bottom = 8.dp),
                    )
                    options.forEach { option ->
                        MenuRow(
                            leading = {
                                if (option.value == selected) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        option.label,
                                        tint = colors.sub,
                                        modifier = Modifier.size(22.dp),
                                    )
                                } else {
                                    Spacer(Modifier.size(22.dp))
                                }
                            },
                            label = option.label,
                            value = null,
                            onClick = {
                                onDismissRequest()
                                onSelect(option.value)
                            },
                        )
                    }
                    MenuRow(
                        leading = {
                            Icon(
                                Icons.AutoMirrored.Rounded.Sort,
                                reverseLabel,
                                tint = colors.sub,
                                modifier = Modifier.size(22.dp),
                            )
                        },
                        label = reverseLabel,
                        value = if (reverse) onValue else offValue,
                        onClick = {
                            onDismissRequest()
                            onReverseChange(!reverse)
                        },
                    )
                }
            }
        }
    }
}
