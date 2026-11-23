package io.github.zyrouge.symphony.ui.view.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.zyrouge.symphony.ui.helpers.LocalGrooveVersion
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.view.BentoCollectionPage
import io.github.zyrouge.symphony.ui.view.DetailSideTile
import io.github.zyrouge.symphony.ui.view.detail.DetailFolderEntry
import io.github.zyrouge.symphony.ui.view.detail.DetailFoldersTile
import io.github.zyrouge.symphony.ui.view.detail.DetailIconCoverTile
import io.github.zyrouge.symphony.ui.view.detail.detailListRowSpan
import io.github.zyrouge.symphony.utils.SimpleFileSystem

/** The subfolders tile shows at most this many rows before it scrolls. */
private const val FOLDER_SUBFOLDER_ROWS = 6

/**
 * The folder detail page in the album frame: the folder icon, the songs
 * directly inside the folder, and its subfolders as a side tile that opens
 * them through [onOpenFolder].
 */
@Composable
internal fun FolderSongsView(
    context: ViewContext,
    folder: SimpleFileSystem.Folder,
    onBack: () -> Unit,
    onOpenFolder: (SimpleFileSystem.Folder) -> Unit,
) {
    val grooveVersion = LocalGrooveVersion.current
    val songIds = remember(folder, grooveVersion) {
        folder.children.values.mapNotNull { (it as? SimpleFileSystem.File)?.data as? String }
    }
    val subfolders = remember(folder, grooveVersion) {
        folder.children.values
            .mapNotNull { it as? SimpleFileSystem.Folder }
            .filterNot { it.isEmpty }
            .sortedBy { it.name }
    }
    val parentPath = remember(folder) {
        folder.parent?.fullPath?.parts
            ?.drop(1)
            ?.mapIndexed { index, part ->
                when (index) {
                    0 -> part.removePrefix("primary:")
                    else -> part
                }
            }
            ?.filter { it.isNotEmpty() }
            ?.joinToString(" / ")
            ?.takeIf { it.isNotEmpty() }
    }
    val entries = subfolders.map { subfolder ->
        val folderCount = subfolder.childFoldersCount
        val songCount = subfolder.children.values.count { it is SimpleFileSystem.File }
        DetailFolderEntry(
            name = subfolder.name,
            sub = listOfNotNull(
                folderCount.takeIf { it > 0 }
                    ?.let { context.symphony.t.XFolders(it.toString()) },
                context.symphony.t.XSongs(songCount.toString()),
            ).joinToString(" · "),
            onClick = { onOpenFolder(subfolder) },
        )
    }

    BentoCollectionPage(
        context = context,
        label = "Folder",
        title = folder.name,
        songIds = songIds,
        onBack = onBack,
        emptyMessage = context.symphony.t.DamnThisIsSoEmpty,
        cover = { geometry -> DetailIconCoverTile(Icons.Rounded.Folder, geometry) },
        metaPrefix = parentPath,
        sideTiles = when {
            entries.isEmpty() -> emptyList()
            else -> listOf(
                DetailSideTile(
                    id = "folders",
                    leading = true,
                    rowSpan = { detailListRowSpan(it, minOf(entries.size, FOLDER_SUBFOLDER_ROWS)) },
                ) { geometry ->
                    DetailFoldersTile(
                        label = context.symphony.t.Folders,
                        countLabel = context.symphony.t.XFolders(entries.size.toString()),
                        folders = entries,
                        geometry = geometry,
                    )
                },
            )
        },
    )
}
