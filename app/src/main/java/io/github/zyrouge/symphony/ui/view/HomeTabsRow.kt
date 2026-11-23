package io.github.zyrouge.symphony.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.bento.BentoSizes
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import kotlin.math.roundToInt

/**
 * Phone section tabs (DESIGN.md §2.2): 40 dp pills, radius 20, 8 dp apart,
 * horizontally scrolling; the selected pill uses accent / on-accent and
 * scrolls into view.
 */
@Composable
internal fun HomeTabsRow(
    context: ViewContext,
    sections: List<HomePage>,
    currentTab: HomePage,
    onSelectSection: (HomePage) -> Unit,
) {
    val colors = LocalBentoColors.current
    val appFontFamily = MaterialTheme.typography.titleLarge.fontFamily
    val tabStyle = BentoTypography.body.copy(
        fontWeight = FontWeight.SemiBold,
        fontFamily = appFontFamily,
    )
    val scrollState = rememberScrollState()
    var viewportWidth by remember { mutableFloatStateOf(0f) }
    var tabCenters by remember { mutableStateOf(emptyMap<HomePage, Float>()) }
    var initialScroll by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.bg)
            .onGloballyPositioned { viewportWidth = it.size.width.toFloat() }
            .padding(top = 4.dp, bottom = 12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = BentoSpacing.GUTTER_COMPACT.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            sections.forEach { section ->
                val selected = section == currentTab
                Box(
                    modifier = Modifier
                        .onGloballyPositioned {
                            val center = it.positionInParent().x + it.size.width / 2f
                            tabCenters = tabCenters + (section to center)
                        }
                        .height(BentoSizes.TAB.dp)
                        .clip(RoundedCornerShape((BentoSizes.TAB / 2).dp))
                        .background(
                            when {
                                selected -> colors.accent
                                else -> colors.surface
                            }
                        )
                        .clickable { onSelectSection(section) }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        section.label(context),
                        style = tabStyle,
                        color = when {
                            selected -> colors.onAccent
                            else -> colors.sub
                        },
                        maxLines = 1,
                    )
                }
            }
        }
    }

    LaunchedEffect(currentTab, tabCenters) {
        val center = tabCenters[currentTab] ?: return@LaunchedEffect
        if (viewportWidth <= 0f) return@LaunchedEffect
        val target = (center - viewportWidth / 2f)
            .coerceIn(0f, scrollState.maxValue.toFloat())
            .roundToInt()
        if (initialScroll) {
            scrollState.scrollTo(target)
            initialScroll = false
        } else {
            scrollState.animateScrollTo(target)
        }
    }
}
