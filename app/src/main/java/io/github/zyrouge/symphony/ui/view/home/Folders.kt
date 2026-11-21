package io.github.zyrouge.symphony.ui.view.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.zyrouge.symphony.services.radio.Radio
import io.github.zyrouge.symphony.ui.components.BentoGroupFolderCover
import io.github.zyrouge.symphony.ui.components.BentoGroupTile
import io.github.zyrouge.symphony.ui.components.BentoGroupedTileColumns
import io.github.zyrouge.symphony.ui.components.IconTextBody
import io.github.zyrouge.symphony.ui.components.LoaderScaffold
import io.github.zyrouge.symphony.ui.components.bentoGroupTileHeightDp
import io.github.zyrouge.symphony.ui.components.bentoNamesGroupComparator
import io.github.zyrouge.symphony.ui.components.formatBentoDurationMinutes
import io.github.zyrouge.symphony.ui.components.rememberBentoPlayingSong
import io.github.zyrouge.symphony.ui.helpers.FadeTransition
import io.github.zyrouge.symphony.ui.helpers.SlideTransition
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.utils.SimpleFileSystem
import java.util.Stack

/**
 * The Folders page (DESIGN.md 5.3): one grouped tile per song-containing
 * folder, in name order, with the folder icon on `tonal` as cover and the
 * folder's songs as rows showing the artist. Tapping the cover or name opens
 * the folder's existing browse view, which the kit does not redesign.
 */
@Composable
fun FoldersView(context: ViewContext) {
    val isUpdating by context.symphony.groove.song.isUpdating.collectAsState()
    val id by context.symphony.groove.song.id.collectAsState()
    val caseSensitive by context.symphony.settings.caseSensitiveSorting.flow.collectAsState()
    val playingSong = rememberBentoPlayingSong(context)
    val explorer = context.symphony.groove.song.explorer

    val folders = remember(id) {
        val entities = mutableMapOf<String, SimpleFileSystem.Folder>()
        val stack = Stack<SimpleFileSystem.Folder>()
        stack.add(explorer)
        while (stack.isNotEmpty()) {
            val current = stack.pop()
            if (current.isEmpty) continue
            var hasSongs = false
            current.children.values.forEach {
                when (it) {
                    is SimpleFileSystem.Folder -> stack.push(it)
                    is SimpleFileSystem.File -> {
                        hasSongs = true
                    }
                }
            }
            if (hasSongs) {
                entities[current.fullPath.pathString] = current
            }
        }
        entities.toMap()
    }
    val sortedFolders by remember(folders, caseSensitive) {
        derivedStateOf {
            folders.values.sortedWith(
                bentoNamesGroupComparator(
                    nameOf = { it.name },
                    caseSensitive = caseSensitive,
                ),
            )
        }
    }
    var currentFolder by remember(id) {
        mutableStateOf<SimpleFileSystem.Folder?>(null)
    }

    BackHandler(currentFolder != null) {
        currentFolder = null
    }

    LoaderScaffold(context, isLoading = isUpdating) {
        AnimatedContent(
            label = "folders-view-content",
            targetState = currentFolder,
            transitionSpec = {
                val enter = when {
                    targetState != null -> SlideTransition.slideUp.enterTransition()
                    else -> FadeTransition.enterTransition()
                }
                enter.togetherWith(FadeTransition.exitTransition())
            },
        ) { folder ->
            if (folder != null) {
                FolderSongsView(context, folder, onBack = { currentFolder = null })
            } else {
                when {
                    sortedFolders.isEmpty() -> IconTextBody(
                        icon = { modifier ->
                            Icon(
                                Icons.Filled.FolderCopy,
                                null,
                                modifier = modifier,
                            )
                        },
                        content = { Text(context.symphony.t.DamnThisIsSoEmpty) },
                    )

                    else -> BentoGroupedTileColumns(
                        entries = sortedFolders,
                        entryHeightDp = { folder ->
                            bentoGroupTileHeightDp(folder.getSortedSongIds(context).size)
                        },
                        modifier = Modifier.padding(top = 4.dp),
                    ) { folderValue, tileWidth ->
                        FolderGroupTile(
                            context = context,
                            folder = folderValue,
                            playingSongId = playingSong.songId,
                            playing = playingSong.isPlaying,
                            narrow = tileWidth < 340f,
                            onOpen = { currentFolder = folderValue },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FolderGroupTile(
    context: ViewContext,
    folder: SimpleFileSystem.Folder,
    playingSongId: String?,
    playing: Boolean,
    narrow: Boolean,
    onOpen: () -> Unit,
) {
    val sortBy by context.symphony.settings.lastUsedSongsSortBy.flow.collectAsState()
    val sortReverse by context.symphony.settings.lastUsedSongsSortReverse.flow.collectAsState()
    val songIds by remember(folder, sortBy, sortReverse) {
        derivedStateOf {
            context.symphony.groove.song.sort(
                folder.getSortedSongIds(context),
                sortBy,
                sortReverse,
            )
        }
    }
    val songs by remember(songIds) {
        derivedStateOf {
            songIds.mapNotNull { context.symphony.groove.song.get(it) }
        }
    }

    BentoGroupTile(
        context = context,
        title = folder.name,
        subtitle = context.symphony.t.XSongs(songs.size.toString()) +
            " · " +
            formatBentoDurationMinutes(songs.sumOf { it.duration }),
        songs = songs,
        cover = { BentoGroupFolderCover() },
        onOpen = onOpen,
        onPlay = {
            context.symphony.radio.shorty.playQueue(songIds)
        },
        onPlayAt = { index ->
            context.symphony.radio.shorty.playQueue(
                songIds,
                Radio.PlayOptions(index = index),
            )
        },
        rowSecondaryOf = { it.artists.joinToString() },
        playingSongId = playingSongId,
        playing = playing,
        narrow = narrow,
        showAllLabel = "Show all " + context.symphony.t.XSongs(songs.size.toString()),
        onShowAll = onOpen,
    )
}

/**
 * The in-folder browse view, unchanged: the kit does not design the folder
 * "detail", so it keeps the existing header and song list.
 */
private fun SimpleFileSystem.Folder.getSortedSongIds(context: ViewContext): List<String> {
    val songIds = children.values.mapNotNull {
        when (it) {
            is SimpleFileSystem.File -> it.data as String
            else -> null
        }
    }
    return context.symphony.groove.song.sort(
        songIds,
        context.symphony.settings.lastUsedSongsSortBy.value,
        context.symphony.settings.lastUsedSongsSortReverse.value,
    )
}
