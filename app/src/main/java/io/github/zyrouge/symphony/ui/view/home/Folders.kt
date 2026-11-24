package io.github.zyrouge.symphony.ui.view.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.rounded.AccountTree
import androidx.compose.material.icons.rounded.Folder
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
import io.github.zyrouge.symphony.ui.helpers.LocalGrooveVersion
import io.github.zyrouge.symphony.ui.helpers.SlideTransition
import io.github.zyrouge.symphony.ui.helpers.ViewContext
import io.github.zyrouge.symphony.ui.theme.LocalStrings
import io.github.zyrouge.symphony.ui.theme.bento.BentoSpacing
import io.github.zyrouge.symphony.ui.theme.bento.BentoTypography
import io.github.zyrouge.symphony.ui.theme.bento.LocalBentoColors
import io.github.zyrouge.symphony.utils.SimpleFileSystem
import java.util.Stack

/** The two views of the Folders section (the prototype's folder view switch). */
private enum class FoldersMode {
    Groups,
    Browse,
}

/** The chosen Folders view, kept while the app runs, like the prototype's session state. */
private var foldersMode by mutableStateOf(FoldersMode.Groups)

/**
 * The Folders section, which also replaces the Browser and Tree sections: a
 * Groups / Browse switch on top.
 * - Groups (DESIGN.md 5.3): one grouped tile per song-containing folder, in
 *   name order, with the folder icon on `tonal` as cover and the folder's
 *   songs as rows showing the artist. Tapping the cover or name opens the
 *   folder's detail page; its subfolders open on top of it.
 * - Browse: the directory browser, which reopens at the last browsed path.
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
    var folderStack by remember(id) {
        mutableStateOf(emptyList<SimpleFileSystem.Folder>())
    }
    val currentFolder = folderStack.lastOrNull()
    val closeFolder = { folderStack = folderStack.dropLast(1) }

    BackHandler(currentFolder != null) {
        closeFolder()
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
                FolderSongsView(
                    context,
                    folder,
                    onBack = closeFolder,
                    onOpenFolder = { folderStack = folderStack + it },
                )
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    FoldersModeBar(
                        mode = foldersMode,
                        onModeChange = { foldersMode = it },
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        when (foldersMode) {
                            FoldersMode.Groups -> when {
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
                                        onOpen = { folderStack = listOf(folderValue) },
                                    )
                                }
                            }

                            FoldersMode.Browse -> BrowserView(context)
                        }
                    }
                }
            }
        }
    }
}

/**
 * The Groups / Browse switch (the prototype's `folderViewBar`): two 40 dp
 * segments in a `surface` pill, at most 360 dp wide, the chosen one `accent`.
 */
@Composable
private fun FoldersModeBar(mode: FoldersMode, onModeChange: (FoldersMode) -> Unit) {
    val colors = LocalBentoColors.current
    val gutter = when {
        LocalWindowInfo.current.containerDpSize.width < 600.dp -> BentoSpacing.GUTTER_COMPACT.dp
        else -> BentoSpacing.GUTTER_OTHER.dp
    }
    Row(
        modifier = Modifier
            .padding(start = gutter, end = gutter, top = 4.dp, bottom = 4.dp)
            .widthIn(max = 360.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surface)
            .padding(4.dp)
            .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        FoldersModeSegment(
            icon = Icons.Rounded.Folder,
            label = LocalStrings.current.Groups,
            selected = mode == FoldersMode.Groups,
            onClick = { onModeChange(FoldersMode.Groups) },
        )
        FoldersModeSegment(
            icon = Icons.Rounded.AccountTree,
            label = LocalStrings.current.Browse,
            selected = mode == FoldersMode.Browse,
            onClick = { onModeChange(FoldersMode.Browse) },
        )
    }
}

@Composable
private fun RowScope.FoldersModeSegment(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalBentoColors.current
    val fg = when {
        selected -> colors.onAccent
        else -> colors.sub
    }
    Row(
        modifier = Modifier
            .weight(1f)
            .height(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                when {
                    selected -> colors.accent
                    else -> Color.Transparent
                },
            )
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(18.dp))
        Text(
            label,
            style = BentoTypography.secondary.copy(fontWeight = FontWeight.SemiBold),
            color = fg,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
    val grooveVersion = LocalGrooveVersion.current
    val songIds by remember(folder, sortBy, sortReverse, grooveVersion) {
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
            formatBentoDurationMinutes(songs.sumOf { it.duration }, context.symphony.t),
        songs = songs,
        cover = { BentoGroupFolderCover() },
        onOpen = onOpen,
        onPlay = {
            context.symphony.radio.shorty.playQueue(songIds, source = folder.name)
        },
        onPlayAt = { index ->
            context.symphony.radio.shorty.playQueue(
                songIds,
                Radio.PlayOptions(index = index),
                source = folder.name,
            )
        },
        rowSecondaryOf = { it.artists.joinToString() },
        playingSongId = playingSongId,
        playing = playing,
        narrow = narrow,
        showAllLabel = context.symphony.t.ShowAllX(context.symphony.t.XSongs(songs.size.toString())),
        onShowAll = onOpen,
    )
}

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
