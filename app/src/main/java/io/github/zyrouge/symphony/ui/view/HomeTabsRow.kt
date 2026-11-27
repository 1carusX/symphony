package io.github.zyrouge.symphony.ui.view

import androidx.compose.runtime.Composable
import io.github.zyrouge.symphony.ui.components.HomeTab
import io.github.zyrouge.symphony.ui.components.HomeTabsRow as BentoHomeTabsRow
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
internal fun HomeTabsRow(
    context: ViewContext,
    sections: List<HomePage>,
    currentTab: HomePage,
    onSelectSection: (HomePage) -> Unit,
) {
    BentoHomeTabsRow(
        items = sections.map { section ->
            HomeTab(
                key = section.name,
                label = section.label(context),
            )
        },
        selectedKey = currentTab.name,
        onSelect = { key -> sections.firstOrNull { it.name == key }?.let(onSelectSection) },
    )
}
