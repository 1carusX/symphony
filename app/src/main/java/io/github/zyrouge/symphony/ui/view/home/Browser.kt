package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.SimplePath

/**
 * The Browse view of the Folders section: the directory browser, which
 * reopens at the last browsed path. The Folders section hosts the loader.
 */
@Composable
fun BrowserView(context: ViewContext) {
    val id by context.symphony.groove.song.id.collectAsState()
    val explorer = context.symphony.groove.song.explorer
    val lastUsedFolderPath by context.symphony.settings.lastUsedBrowserPath.flow.collectAsState()

    BentoFileExplorer(
        context = context,
        key = id,
        initialPath = lastUsedFolderPath?.let { SimplePath(it) },
        explorer = explorer,
        onPathChange = { path ->
            context.symphony.settings.lastUsedBrowserPath.setValue(path.pathString)
        },
    )
}
