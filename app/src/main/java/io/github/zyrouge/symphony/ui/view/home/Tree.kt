package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.helpers.ViewContext

@Composable
fun TreeView(context: ViewContext) {
    val isUpdating by context.symphony.groove.song.isUpdating.collectAsState()
    val songIds by context.symphony.groove.song.all.collectAsState()
    val disabledTreePaths by context.symphony.settings.lastDisabledTreePaths.flow.collectAsState()

    LoaderScaffold(context, isLoading = isUpdating) {
        BentoTreeExplorer(
            context = context,
            key = songIds.size,
            explorer = context.symphony.groove.song.explorer,
            disabledPaths = disabledTreePaths,
        )
    }
}
